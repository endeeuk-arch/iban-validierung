package de.erdenemandal.iban;

import java.math.BigInteger;
import java.util.Optional;

import org.springframework.stereotype.Component;

@Component
public class IbanValidator {

    /**
     * Removes separators (spaces, -, ., /) and converts to upper case.
     * Example: "de89 3704-0044.0532 0130 00" -> "DE89370400440532013000"
     * null -> ""
     */
    public String normalize(String input) {

        if (input == null) {
            return "";
        }
        // remove whitspaces and convert to Uppercase
        input = input.replaceAll("[^A-Za-z0-9]", "");
        input = input.toUpperCase();
        return input;

    }

    /**
     * Validates an IBAN and returns the reason if it is invalid:
     * - DE: exactly 22 characters, other countries: 15 to 34 characters
     * - Check digits according to ISO 13616 (modulo 97 == 1)
     */
    public Optional<IbanError> findError(String input) {
        String iban = normalize(input);

        if (iban.isEmpty()) {
            return Optional.of(IbanError.EMPTY);
        }
        // German IBANs must have exactly 22 characters
        if (iban.startsWith("DE") && iban.length() != 22) {
            return Optional.of(IbanError.WRONG_LENGTH);
        }
        if (iban.length() < 15 || iban.length() > 34) {
            return Optional.of(IbanError.WRONG_LENGTH);
        }

        String rearranged = iban.substring(4) + iban.substring(0, 4);
        StringBuilder digits = new StringBuilder();
        for (char c : rearranged.toCharArray()) {
            digits.append(Character.getNumericValue(c));
        }
        BigInteger number = new BigInteger(digits.toString());
        if (number.mod(BigInteger.valueOf(97)).intValue() != 1) {
            return Optional.of(IbanError.INVALID_CHECK_DIGITS);
        }
        return Optional.empty();
    }

    public boolean isValid(String input) {
        return findError(input).isEmpty();
    }

}
