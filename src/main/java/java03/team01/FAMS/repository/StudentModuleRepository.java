package java03.team01.FAMS.repository;

import java03.team01.FAMS.model.entity.StudentModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentModuleRepository extends JpaRepository<StudentModule, Long> {
    @Query(value = "SELECT module_id FROM student_modules " +
            "where student_id = :studentId", nativeQuery = true)
    List<Long> getModuleByStudentId(Long studentId);

    @Query(value = "SELECT student_id FROM student_modules " +
            "where module_id = :moduleId", nativeQuery = true)
    Long getStudentByModuleId(Long moduleId);

    @Query(value = "SELECT module_score FROM student_modules " +
            "where student_id = :studentId and module_id = :moduleId", nativeQuery = true)
    Float getModuleScoreByStudentIdAndModuleId(Long studentId, Long moduleId);

    @Query(value = "SELECT * FROM student_modules " +
            "where student_id = :studentId and module_id = :moduleId", nativeQuery = true)
    Optional<StudentModule> findByModuleIdAndStudentId(Long moduleId, Long studentId);

    @Query(value = "SELECT * FROM student_modules " +
            "where student_id = :studentId", nativeQuery = true)
    List<StudentModule> findByStudentId(Long studentId);

    @Query(value = "select avg(score) from scores s " +
            "join assignments a " +
            "on s.assignment_id = a.assignment_id " +
            "where student_id = :studentId and a.module_id = :moduleId",
            nativeQuery = true)
    Double getAverageModuleScore(Long studentId, Long moduleId);

    @Query(value = "select avg(module_score) from student_modules sm " +
            "join students s " +
            "on s.student_id = sm.student_id " +
            "where s.status = 'Keep Class' AND s.student_id = :studentId",
            nativeQuery = true)
    Float getAverageGPA(Long studentId);
}
