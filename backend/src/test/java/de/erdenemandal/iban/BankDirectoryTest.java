package de.erdenemandal.iban;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BankDirectoryTest {

    private final BankDirectory directory = new BankDirectory();

    @Test
    void findetBekannteBank() {
        assertEquals("Commerzbank", directory.findBankName("DE89370400440532013000").orElseThrow());
    }

    @Test
    void unbekannteBank_leer() {
        assertTrue(directory.findBankName("DE02120300000000202051").isEmpty());
    }

    @Test
    void auslaendischeIban_leer() {
        assertTrue(directory.findBankName("GB82WEST12345698765432").isEmpty());
    }
}