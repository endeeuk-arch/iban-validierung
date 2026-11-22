package de.erdenemandal.iban;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class IbanService {

    private final IbanValidator validator;
    private final BankDirectory bankDirectory;
    private final IbanCheckRepository repository;
    private final ExternalBankClient externalBankClient;
    private final BankRepository bankRepository;

    public IbanService(IbanValidator validator, BankDirectory bankDirectory, IbanCheckRepository repository,
            ExternalBankClient externalBankClient, BankRepository bankRepository) {
        this.validator = validator;
        this.bankDirectory = bankDirectory;
        this.repository = repository;
        this.bankRepository = bankRepository;
        this.externalBankClient = externalBankClient;
    }

    public IbanResult check(String input) {
        String ibanNorm = validator.normalize(input);
        boolean isValid = validator.isValid(ibanNorm);

        // Bank code (BLZ) is part of every valid German IBAN
        String blz = isValid && ibanNorm.startsWith("DE") ? ibanNorm.substring(4, 12) : null;

        // Bank name: known banks → saved banks → external API
        String bankName = !isValid ? null
                : bankDirectory.findBankName(ibanNorm)
                        .or(() -> blz == null ? Optional.empty() : bankRepository.findById(blz).map(Bank::getName))
                        .or(() -> externalBankClient.findBankName(ibanNorm))
                        .orElse(null);

        // Save a newly found bank, so the next lookup needs no external call
        Bank bank = null;
        if (blz != null && bankName != null) {
            bank = bankRepository.findById(blz)
                    .orElseGet(() -> bankRepository.save(new Bank(blz, bankName)));
        }

        repository.save(new IbanCheck(ibanNorm, bank, isValid));

        return new IbanResult(isValid, ibanNorm, blz, bankName);
    }

    public List<IbanCheck> latestChecks() {
        return repository.findTop20ByOrderByCreatedAtDesc();
    }
}