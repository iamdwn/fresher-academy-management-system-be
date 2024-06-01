package java03.team01.FAMS.repository;

import java03.team01.FAMS.model.entity.EmailTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {
    EmailTemplate findByDescription(String description);
    EmailTemplate findByDescriptionContainingIgnoreCase(String description);

    Page<EmailTemplate> findByCategory(String category, Pageable pageable);
}
