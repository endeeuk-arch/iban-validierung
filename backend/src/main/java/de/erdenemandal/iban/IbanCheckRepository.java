package de.erdenemandal.iban;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IbanCheckRepository extends JpaRepository<IbanCheck, Long> {
     List<IbanCheck> findTop20ByOrderByCreatedAtDesc();
}
