package de.erdenemandal.iban;

import java.math.BigInteger;

import org.springframework.stereotype.Component;

@Component
public class IbanValidator {

    /**
     * Removes separators (spaces, -, ., /) and converts to upper case.
     * Example: "de89 3704-0044.0532 0130 00" -> "DE89370400440532013000"
     * null -> ""
     */
    public String normalize(String input) {
       
        if (input ==  null)
        {
            return "";
        }
        // Keep only letters and digits, then convert to upper case
        input = input.replaceAll("[^A-Za-z0-9]", "");
        input = input.toUpperCase();    
        return input;

    }

    /**
     * Validates an IBAN:
     * - DE: exactly 22 characters, other countries: 15 to 34 characters
     * - Check digits according to ISO 13616 (modulo 97 == 1)
     */
    public boolean isValid(String input) {
     
        String ibanCheck = normalize(input);

        if (ibanCheck.isEmpty())
        {
            return false;
        }
        // German IBANs must have exactly 22 characters
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
