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

    public Optional<String> findBankName(String iban) {
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
            return Optional.of(response.bankData().name());
            

        } catch (RestClientException e) {
            log.warn("External bank lookup failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    record OpenIbanResponse(boolean valid, BankData bankData) {}

    record BankData(String name) {}
}