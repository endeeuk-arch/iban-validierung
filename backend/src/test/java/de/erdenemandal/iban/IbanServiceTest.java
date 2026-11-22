package de.erdenemandal.iban;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class IbanServiceTest {

    private final IbanCheckRepository repository = mock(IbanCheckRepository.class);
    private final ExternalBankClient externalClient = mock(ExternalBankClient.class);

private final IbanService service =
        new IbanService(new IbanValidator(), new BankDirectory(), repository, externalClient);

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
    @Test
    void pruefung_wirdGespeichert() {
    service.check("DE89 3704 0044 0532 0130 00");
    verify(repository, times(1)).save(any(IbanCheck.class));
}
@Test
void unbekannteBank_externeApiLiefertNamen() {
    when(externalClient.findBankName("DE02120300000000202051"))
            .thenReturn(Optional.of("Deutsche Kreditbank Berlin"));

    IbanResult result = service.check("DE02 1203 0000 0000 2020 51");

    assertEquals("Deutsche Kreditbank Berlin", result.bankName());
}

@Test
void externeApiFaelltAus_keinAbsturz() {
    when(externalClient.findBankName(any())).thenReturn(Optional.empty());

    IbanResult result = service.check("DE02 1203 0000 0000 2020 51");

    assertTrue(result.valid());
    assertNull(result.bankName());
}

}