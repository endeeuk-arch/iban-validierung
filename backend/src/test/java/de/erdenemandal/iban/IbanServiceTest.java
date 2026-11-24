package de.erdenemandal.iban;

import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import de.erdenemandal.iban.ExternalBankClient.BankInfo;
import static org.junit.jupiter.api.Assertions.*;

class IbanServiceTest {

    private final IbanCheckRepository repository = mock(IbanCheckRepository.class);
    private final ExternalBankClient externalClient = mock(ExternalBankClient.class);
    private final BankRepository bankRepository = mock(BankRepository.class);
    private final IbanService service = new IbanService(
            new IbanValidator(), repository, externalClient, bankRepository);

    private final IbanValidator validator = new IbanValidator();

    @Test
    void eingabeMitLeerzeichen_findetBank() {
        when(bankRepository.findById("37040044"))
                .thenReturn(Optional.of(new Bank("37040044", "Commerzbank", "COBADEFFXXX")));

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

    @Test
    void pruefung_wirdGespeichert() {
        service.check("DE89 3704 0044 0532 0130 00");
        verify(repository, times(1)).save(any(IbanCheck.class));
    }

    @Test
    void unbekannteBank_externeApiLiefertNamen() {
        when(externalClient.findBank("DE02120300000000202051"))
                .thenReturn(Optional.of(new BankInfo("Deutsche Kreditbank Berlin", "BYLADEM1001")));

        IbanResult result = service.check("DE02 1203 0000 0000 2020 51");

        assertEquals("Deutsche Kreditbank Berlin", result.bankName());
    }

    @Test
    void externeApiFaelltAus_keinAbsturz() {
        when(externalClient.findBank(any())).thenReturn(Optional.empty());

        IbanResult result = service.check("DE02 1203 0000 0000 2020 51");

        assertTrue(result.valid());
        assertNull(result.bankName());
    }

    @Test
    void neueBankAusExternerApi_wirdGespeichert() {
        when(externalClient.findBank("DE02120300000000202051"))
                .thenReturn(Optional.of(new BankInfo("Deutsche Kreditbank Berlin", "BYLADEM1001")));

        service.check("DE02 1203 0000 0000 2020 51");

        verify(bankRepository).save(any(Bank.class));
    }

    @Test
    void falscheLaenge_grundWirdAngezeigt() {
        IbanResult result = service.check("DE89 3704 0044 0532 0130");

        assertFalse(result.valid());
        assertEquals("Wrong length: 20 characters, German IBANs have 22", result.error());
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