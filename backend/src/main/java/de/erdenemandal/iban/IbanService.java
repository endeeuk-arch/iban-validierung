package de.erdenemandal.iban;

import org.springframework.stereotype.Service;

@Service
public class IbanService {

    private final IbanValidator validator;
    private final BankDirectory bankDirectory;

    // Konstruktor: Spring gibt die beiden Objekte automatisch rein (DI)
    public IbanService(IbanValidator validator, BankDirectory bankDirectory) {
        this.validator = validator;
        this.bankDirectory = bankDirectory;
    }

    public IbanResult check(String input) {
        // a) IBAN normalisieren         → validator.normalize(...)
        // b) gültig?                     → validator.isValid(...)
        // c) Bankname nur wenn gültig    → bankDirectory.findBankName(...).orElse(null)
        // d) return new IbanResult(...)
    String ibanNorm = validator.normalize(input);
    boolean isValid = this.validator.isValid(ibanNorm);
    String bankName = isValid ? this.bankDirectory.findBankName(ibanNorm).orElse(null) : null;
    return new IbanResult(isValid, ibanNorm, bankName);
    
    }
}