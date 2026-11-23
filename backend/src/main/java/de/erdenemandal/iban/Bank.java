package de.erdenemandal.iban;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "bank")
public class Bank {

    @Id
    private String blz;

    private String name;

    private String bic;

    protected Bank() {
        // Required by JPA
    }

    public Bank(String blz, String name, String bic) {
        this.blz = blz;
        this.name = name;
        this.bic = bic;
    }

    public String getBlz() {
        return blz;
    }

    public String getName() {
        return name;
    }

    public String getBic() {
        return bic;
    }
}