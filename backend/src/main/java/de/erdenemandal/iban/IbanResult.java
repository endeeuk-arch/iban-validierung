package de.erdenemandal.iban;

public record IbanResult(boolean valid, String iban, String country,
                         String blz, String bankName, String bic, String error) {
}