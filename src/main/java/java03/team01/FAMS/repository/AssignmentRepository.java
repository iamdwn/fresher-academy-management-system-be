package java03.team01.FAMS.repository;

import java03.team01.FAMS.model.entity.Assignment;
import java03.team01.FAMS.model.entity.Student;
import java03.team01.FAMS.model.payload.responseModel.CustomAssignResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    Optional<Assignment> findById(long id);

//    @Query(value = "SELECT assignment_id FROM assignments s " +
//                   "WHERE s.module_id in (:moduleIds)", nativeQuery = true)
//    List<Long> getListAssignmentByModuleId(List<Long> moduleIds);

    @Query(value = "SELECT * FROM assignments s " +
                   "WHERE s.module_id = :moduleId", nativeQuery = true)
    List<Assignment> getListAssignmentByModuleId(@Param("moduleId") Long moduleId);

    @Query(value = "SELECT * FROM assignments s " +
                   "WHERE s.module_id = :moduleId", nativeQuery = true)
    List<Long> getListAssignmentByModule(@Param("moduleId") Long moduleId);

//    @Query(value = "SELECT assignment_id FROM assignments s " +
//                   "WHERE s.module_id = :modulesId", nativeQuery = true)
    Optional<Assignment> getAssignmentByIdAndModuleId(Long assignmentId, Long modulesId);

    @Query("SELECT a FROM Assignment a JOIN Score s ON  a.id = s.assignment.id WHERE a.module.id =:moduleId AND s.student.id = :studentId")
    List<Assignment> getAssignmentByModuleIdAndStudentId(Long moduleId, Long studentId);

    @Query("SELECT a.module.id FROM Assignment a WHERE a.id = :assignmentId")
    Long getModuleIdByAssignmentId(@Param("assignmentId") Long assignmentId);
}
