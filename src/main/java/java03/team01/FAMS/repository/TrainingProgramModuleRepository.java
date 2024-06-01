package java03.team01.FAMS.repository;

import java03.team01.FAMS.model.entity.TrainingProgramModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TrainingProgramModuleRepository extends JpaRepository<TrainingProgramModule, Long> {
    List<TrainingProgramModule> findByTrainingProgramId(Long trainingProgramId);

    @Query(value = "SELECT module_id FROM training_program_modules WHERE program_id = :id " ,nativeQuery = true)
    List<Long> findModulesByTraningProgramId (Long id);
}
