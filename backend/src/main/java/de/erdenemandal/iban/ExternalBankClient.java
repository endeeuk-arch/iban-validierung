package de.erdenemandal.iban;

import java.time.Duration;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class ExternalBankClient {

    private static final Logger log = LoggerFactory.getLogger(ExternalBankClient.class);

    private final RestClient restClient;

    public ExternalBankClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(2));

        this.restClient = RestClient.builder()
                .baseUrl("https://openiban.com")
                .requestFactory(factory)
                .build();
    }

    /** Bank name and BIC from openiban.com, empty if not found or the API is not reachable. */
    public Optional<BankInfo> findBank(String iban) {
        try {
            OpenIbanResponse response = restClient.get()
                    .uri("/validate/{iban}?getBIC=true", iban)
                    .retrieve()
                    .body(OpenIbanResponse.class);

            if (response == null
                    || response.bankData() == null
                    || response.bankData().name() == null
                    || response.bankData().name().isBlank()) {
                return Optional.empty();
            }

            BankData data = response.bankData();
            String bic = (data.bic() == null || data.bic().isBlank()) ? null : data.bic();
            return Optional.of(new BankInfo(data.name(), bic));

        } catch (RestClientException e) {
            log.warn("External bank lookup failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /** Result for the service: name and BIC of a bank. */
    public record BankInfo(String name, String bic) {}

    record OpenIbanResponse(boolean valid, BankData bankData) {}

    record BankData(String name, String bic) {}
}