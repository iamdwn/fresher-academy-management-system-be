package java03.team01.FAMS.repository;

import java03.team01.FAMS.model.entity.ReservedClass;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<ReservedClass, Long> {
    Boolean existsByStudentIdAndAndClassObjIdAndStatus(Long studentId, Long classId, String status);
    ReservedClass findByStudentIdAndAndClassObjIdAndStatus(Long studentId, Long classId, String status);

    Boolean existsByStudentIdAndAndClassObjId(Long studentId, Long classId);

    @Query("SELECT rc FROM ReservedClass rc WHERE rc.status like 'Active' " +
            "AND (:id IS NULL OR rc.student.id = :id)" +
            "AND rc.student.fullName LIKE %:fullName% " +
            "And rc.student.email LIKE %:email% ")
    Page<ReservedClass> findByIdorFullNameorEmail(@Param("id") Long id,
                                              @Param("fullName") String fullName,
                                              @Param("email") String email,
                                              Pageable pageable);

    @Query("SELECT rc FROM ReservedClass rc " +
            "Where rc.student.id = :studentId " +
            "AND rc.classObj.id = :classId " +
            "AND rc.status like 'Active'")
    Optional<ReservedClass> findBystudentIdAndClassId(@Param("studentId") Long studentId,
                                                      @Param("classId") Long classId);

    List<ReservedClass> findByStatus(String status);
}
