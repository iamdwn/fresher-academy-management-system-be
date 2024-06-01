package java03.team01.FAMS.repository;

import java03.team01.FAMS.model.entity.EmailSend;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailSendRepository extends JpaRepository<EmailSend, Long> {
}
