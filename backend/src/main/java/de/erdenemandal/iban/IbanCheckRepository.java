package de.erdenemandal.iban;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IbanCheckRepository extends JpaRepository<IbanCheck, Long> {
}