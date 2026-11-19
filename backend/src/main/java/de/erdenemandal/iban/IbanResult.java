package de.erdenemandal.iban;

public record IbanResult(boolean valid, String iban, String bankName) {
}
