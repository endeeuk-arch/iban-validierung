package de.erdenemandal.iban;

import org.springframework.stereotype.Component;

@Component
public class IbanValidator {

    /**
     * Entfernt Trennzeichen (Leerzeichen, -, ., /) und wandelt in Großbuchstaben um.
     * Beispiel: "de89 3704-0044.0532 0130 00" -> "DE89370400440532013000"
     * null -> ""
     */
    public String normalize(String input) {
        // TODO
        throw new UnsupportedOperationException("not implemented");
    }

    /**
     * Prüft eine IBAN:
     * - DE: genau 22 Zeichen, andere Länder: 15 bis 34 Zeichen
     * - Prüfziffer nach ISO 13616 (Modulo 97 == 1)
     */
    public boolean isValid(String input) {
        // TODO
        throw new UnsupportedOperationException("not implemented");
    }
}
