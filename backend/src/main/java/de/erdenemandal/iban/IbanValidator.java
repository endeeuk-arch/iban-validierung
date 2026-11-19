package de.erdenemandal.iban;

import java.math.BigInteger;

import org.springframework.stereotype.Component;

@Component
public class IbanValidator {

    /**
     * Entfernt Trennzeichen (Leerzeichen, -, ., /) und wandelt in Großbuchstaben um.
     * Beispiel: "de89 3704-0044.0532 0130 00" -> "DE89370400440532013000"
     * null -> ""
     */
    public String normalize(String input) {
        // TODO return empty if input is NULL

        if (input ==  null)
        {
            return "";
        }
        // remove whitspaces and convert to Uppercase

        //input = input.replaceAll("\\s", "");
        input = input.replaceAll("[^A-Za-z0-9]", "");
        input = input.toUpperCase();    
        return input;
 //   throw new UnsupportedOperationException("not implemented");
    }

    /**
     * Prüft eine IBAN: 
     * - DE: genau 22 Zeichen, andere Länder: 15 bis 34 Zeichen
     * - Prüfziffer nach ISO 13616 (Modulo 97 == 1)
     */
    public boolean isValid(String input) {
     
        String ibanCheck = normalize(input);

        if (ibanCheck.isEmpty())
        {
            return false;
        }
        //  check  Germany IBAN
        if (ibanCheck.startsWith("DE") && ibanCheck.length() != 22)
        {
            return false;
        }


        if (ibanCheck.length() < 15 || ibanCheck.length() > 34)
        {
            return false;
        }

        String rearranged = ibanCheck.substring(4) + ibanCheck.substring(0, 4);
        StringBuilder digits = new StringBuilder();
        for (char c: rearranged.toCharArray())
        {
            digits.append(Character.getNumericValue(c));
        }
        BigInteger num = new BigInteger(digits.toString());
        return num.mod(BigInteger.valueOf(97)).intValue() == 1;

    }
}
