package de.erdenemandal.iban;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "iban_check")
public class IbanCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // BIGSERIAL: the database generates the ID
    private Long id;

    @Column(nullable = false)
    private String iban;

    private String blz;

    @Column(nullable = false)
    private boolean valid;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected IbanCheck() {
        // Required by JPA
    }

    public IbanCheck(String iban, String blz, boolean valid) {
        this.iban = iban;
        this.blz = blz;
        this.valid = valid;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getIban() { return iban; }
    public String getBlz() { return blz; }
    public boolean isValid() { return valid; }
    public Instant getCreatedAt() { return createdAt; }
}