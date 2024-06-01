package java03.team01.FAMS.repository;

import java03.team01.FAMS.model.entity.ReservedClass;
import java03.team01.FAMS.model.entity.Student;

import java03.team01.FAMS.model.entity.StudentClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StudentClassRepository extends JpaRepository<StudentClass, Long> {
    @Query(value = "select *\n" +
            "from student_classes\n" +
            "where class_id = :classId",nativeQuery = true)
    List<StudentClass> getStudentClassByClassId(Long classId);

    StudentClass findByStudentIdAndClassObjId(Long studentId, Long classId);

    StudentClass findByStudentIdAndClassObjIdAndAttendingStatus(Long studentId, Long classId, String attendingStatus);

    @Query(value = "SELECT * FROM student_classes rc " +
            "WHERE rc.student_id = :studentId " +
            "AND rc.class_id = :classId " +
            "AND (rc.attending_status LIKE :status OR rc.attending_status LIKE 'Reserved' OR rc.attending_status LIKE 'Back To Class')", nativeQuery = true)
    Optional<StudentClass> findByStudentAndClassId(@Param("studentId") Long studentId,
                                                   @Param("classId") Long classId,
                                                   @Param("status") String status);

    @Query(value = "SELECT student_id FROM student_classes where class_id = :classId", nativeQuery = true)
    List<Long> getStudentIdByClassId(Long classId);

    @Query(value = "SELECT * FROM student_classes where class_id = :classId", nativeQuery = true)
    Page<Object> getPageStudentByClassId(Pageable pageable, Long classId);

    @Query(value = "SELECT s.student_id FROM student_classes s " +
            "WHERE s.student_id = :studentId AND s.class_id = :classId" +
            " AND s.attending_status like 'In class' ", nativeQuery = true)
    Long getStudentIdByStudentCode(Long studentId, Long classId);

    @Query(value = "SELECT * FROM student_classes s " +
                   "WHERE s.student_id = :studentId " +
            "AND s.attending_status like 'In class' ", nativeQuery = true)
    Optional<StudentClass> getStudentClassByStudentId(@Param("studentId") Long studentId
                                                    ,@Param("studentId") Long classId);


    @Query(value = "select *\n" +
            "from student_classes\n" +
            "where student_id = :studentId and class_id = :classId",nativeQuery = true)
    StudentClass getStudentClassByStudentIdAndClassId(Long studentId, Long classId);


}
