package java03.team01.FAMS.repository;


import java03.team01.FAMS.model.entity.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {
    Set<Score> getScoreByAssignmentIdAndStudentId(Long assignmentId, Long studentId);
    Optional<Score> getByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    @Query("SELECT s FROM Score s " +
            "WHERE s.assignment.id = :assignmentId " +
            "AND s.student.id = :studentId " +
            "AND s.id = :scoreId")
    Optional<Score> getScoreAssignmentIdAndStudentId(Long assignmentId, Long studentId, Long scoreId);

    @Query("SELECT s.student.id from Score s WHERE s.assignment.id = :assignmentId GROUP BY s.student.id")
    List<Long> getStudentIds(@Param("assignmentId") Long assignmentId);

    @Query("SELECT s.assignment.id from Score s WHERE s.id = :scoreId")
    Long getAssignmentIdByScoreId(@Param("scoreId") Long scoreId);

    @Query("SELECT s.student.id from Score s WHERE s.id = :scoreId")
    Long getStudentIdByScoreId(@Param("scoreId") Long scoreId);

    @Query(value = "select *\n" +
            "from scores\n" +
            "where student_id = :id", nativeQuery = true)
    Score getScoreByStudentId(Long id);
}
