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

    // Constructor injection: Spring provides the dependencies
    public IbanService(IbanValidator validator, BankDirectory bankDirectory, IbanCheckRepository repository, ExternalBankClient externalclient) {
        this.validator = validator;
        this.bankDirectory = bankDirectory;
        this.repository = repository;
        this.externalBankClient = externalclient;
    }

    public IbanResult check(String input) {
        // Normalize, validate, and look up the bank name (only for valid IBANs)
    String ibanNorm = validator.normalize(input);
    boolean isValid = this.validator.isValid(ibanNorm);

  //  String bankName = isValid ? this.bankDirectory.findBankName(ibanNorm).orElse(null) : null;
  // read bank name from BANKDirectory if IBAN is valid
  
  Optional<String> localBank = isValid ? bankDirectory.findBankName(ibanNorm) : Optional.empty();

    String bankName = localBank
            .or(() -> isValid ? externalBankClient.findBankName(ibanNorm) : Optional.empty())
            .orElse(null);


    // Store every check; BLZ only for known banks (foreign key to table bank)
    String blz = localBank.isPresent() ? ibanNorm.substring(4, 12) : null;
  // String blz = (bankName != null) ? ibanNorm.substring(4, 12) : null;
    repository.save(new IbanCheck(ibanNorm, blz, isValid));

    return new IbanResult(isValid, ibanNorm, bankName);
    
    }

    public List<IbanCheck> latestChecks() {
            return repository.findTop20ByOrderByCreatedAtDesc();
    }
}