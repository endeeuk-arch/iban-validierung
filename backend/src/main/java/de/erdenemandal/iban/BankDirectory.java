package de.erdenemandal.iban;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

@Component
public class BankDirectory {

    private static final Map<String, String> BANKS = Map.of(
            "37040044", "Commerzbank",
            "37050198", "Sparkasse KölnBonn",
            "37070060", "Deutsche Bank");

    /** Expects a normalized IBAN. Only German IBANs contain a BLZ (bank code). */
    public Optional<String> findBankName(String iban) {
       if (!iban.startsWith("DE")) {
            return Optional.empty();
}
        String blz = iban.substring(4, 12);
        return Optional.ofNullable(BANKS.get(blz));
    }
}