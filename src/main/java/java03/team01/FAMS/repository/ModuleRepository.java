package java03.team01.FAMS.repository;

import java03.team01.FAMS.model.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
//    @Query("SELECT md FROM Module md WHERE md.id IN :list")
//    List<Module> findAllById(List<Long> idList);
    Optional<Module> findById(Long id);
}
