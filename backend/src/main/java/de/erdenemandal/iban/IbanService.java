package de.erdenemandal.iban;

import org.springframework.stereotype.Service;

@Service
public class IbanService {

    private final IbanValidator validator;
    private final BankDirectory bankDirectory;
    private final IbanCheckRepository repository;

    // Constructor injection: Spring provides the dependencies
    public IbanService(IbanValidator validator, BankDirectory bankDirectory, IbanCheckRepository repository) {
        this.validator = validator;
        this.bankDirectory = bankDirectory;
        this.repository = repository;
    }

    public IbanResult check(String input) {
        // Normalize, validate, and look up the bank name (only for valid IBANs)
    String ibanNorm = validator.normalize(input);
    boolean isValid = this.validator.isValid(ibanNorm);
    String bankName = isValid ? this.bankDirectory.findBankName(ibanNorm).orElse(null) : null;

    // Store every check; BLZ only for known banks (foreign key to table bank)
    String blz = (bankName != null) ? ibanNorm.substring(4, 12) : null;
    repository.save(new IbanCheck(ibanNorm, blz, isValid));

    return new IbanResult(isValid, ibanNorm, bankName);
    
    }
}