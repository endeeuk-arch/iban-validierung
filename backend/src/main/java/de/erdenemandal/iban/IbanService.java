package de.erdenemandal.iban;

import org.springframework.stereotype.Service;
import de.erdenemandal.iban.ExternalBankClient.BankInfo;

import java.util.List;
import java.util.Optional;

@Service
public class IbanService {

    private final IbanValidator validator;
    private final IbanCheckRepository repository;
    private final ExternalBankClient externalBankClient;
    private final BankRepository bankRepository;

    public IbanService(IbanValidator validator, IbanCheckRepository repository,
            ExternalBankClient externalBankClient, BankRepository bankRepository) {
        this.validator = validator;
        this.repository = repository;
        this.bankRepository = bankRepository;
        this.externalBankClient = externalBankClient;
    }

    public IbanResult check(String input) {
        String ibanNorm = validator.normalize(input);
        Optional<IbanError> error = validator.findError(ibanNorm);
        boolean isValid = error.isEmpty();

        String country = ibanNorm.length() >= 2 ? ibanNorm.substring(0, 2) : null;
        // Bank code (BLZ) is part of every valid German IBAN
        String blz = isValid && "DE".equals(country) ? ibanNorm.substring(4, 12) : null;

        // Bank: saved banks (with BIC) -> external API
        Optional<Bank> savedBank = blz == null
                ? Optional.empty() // BLZ is empty
                : bankRepository.findById(blz); // search from DB with the given BLZ

        Optional<BankInfo> info = !isValid
                ? Optional.empty() // invalid IBAN, no bank info
                : savedBank.map(b -> new BankInfo(b.getName(), b.getBic())) // BANK found in DB
                        .or(() -> externalBankClient.findBank(ibanNorm)); // if not found in DB, call external API

        // Save a newly found bank, so the next lookup needs no external call
        Bank bank = savedBank.orElse(null);
        if (bank == null && blz != null && info.isPresent()) { // save only if BLZ is present and bank info is found
            bank = bankRepository.save(new Bank(blz, info.get().name(), info.get().bic()));
        }

        repository.save(new IbanCheck(ibanNorm, bank, isValid));

        return new IbanResult(isValid, ibanNorm, country, blz,
                info.map(BankInfo::name).orElse(null),
                info.map(BankInfo::bic).orElse(null),
                error.map(e -> errorText(e, ibanNorm)).orElse(null));
    }

    private String errorText(IbanError error, String iban) {
        return switch (error) {
            case EMPTY -> "Please enter an IBAN";
            case WRONG_LENGTH -> "Wrong length: " + iban.length() + " characters"
                    + (iban.startsWith("DE") ? ", German IBANs have 22" : "");
            case INVALID_CHECK_DIGITS -> "Check digits do not match (typo?)";
        };
    }

    public List<IbanCheck> latestChecks() {
        return repository.findTop20ByOrderByCreatedAtDesc();
    }
}