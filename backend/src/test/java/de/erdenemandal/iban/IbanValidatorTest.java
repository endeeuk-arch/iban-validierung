package de.erdenemandal.iban;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IbanValidatorTest {

    private final IbanValidator validator = new IbanValidator();


    @Test
    void normalize_entferntTrennzeichen() {

        assertEquals("DE89370400440532013000", validator.normalize("de89 3704-0044.0532/-0130 00"));
    }

    @Test
    void normalize_null_ergibtLeerenString() {
        assertEquals("", validator.normalize(null));
    }

    @Test
    void isValid_gueltigeDeutscheIban() {
        assertTrue(validator.isValid("DE89 3704 0044 0532 0130 00"));
    }

    @Test
    void isValid_falschePruefziffer() {
        assertFalse(validator.isValid("DE89 3704 0044 0532 0130 01"));
    }

    @Test
    void isValid_falscheLaenge() {
        assertFalse(validator.isValid("DE89 3704 0044 0532 0130"));
    }

    @Test
    void isValid_andereLaender() {
        assertTrue(validator.isValid("GB82 WEST 1234 5698 7654 32"));
    }

    @Test
    void isValid_leerOderNull() {
        assertFalse(validator.isValid(""));
        assertFalse(validator.isValid(null));
    }

    @Test
    void isValid_zuKurz_stuerztNichtAb() {
        assertFalse(validator.isValid("DE"));
        assertFalse(validator.isValid("DE89"));
    }

    @Test
    void isValid_oesterreich_20Zeichen() {
        assertTrue(validator.isValid("AT61 1904 3002 3457 3201"));
    }

    @Test
    void findError_falscheLaenge() {
        assertEquals(IbanError.WRONG_LENGTH, validator.findError("DE89 3704 0044 0532 0130").orElseThrow());
    }

    @Test
    void findError_falschePruefziffer() {
        assertEquals(IbanError.INVALID_CHECK_DIGITS, validator.findError("DE89 3704 0044 0532 0130 01").orElseThrow());
    }

    @Test
    void findError_leer() {
        assertEquals(IbanError.EMPTY, validator.findError("  ").orElseThrow());
    }
}
