package de.erdenemandal.iban;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IbanServiceTest {

    private final IbanService service = new IbanService(new IbanValidator(), new BankDirectory());

    @Test
    void eingabeMitLeerzeichen_findetBank() {
        IbanResult result = service.check("de89 3704 0044 0532 0130 00");

        assertTrue(result.valid());
        assertEquals("DE89370400440532013000", result.iban());
        assertEquals("Commerzbank", result.bankName());
    }

    @Test
    void ungueltigeIban_keinBankname() {
        IbanResult result = service.check("DE89 3704 0044 0532 0130 01");

        assertFalse(result.valid());
        assertNull(result.bankName());
    }
}