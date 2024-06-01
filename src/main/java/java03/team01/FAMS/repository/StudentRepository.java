package java03.team01.FAMS.repository;

import java03.team01.FAMS.model.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentCode(String studentCode);
    @Query(value = "select *\n" +
            "from students\n" +
            "where student_code = :studentCode",nativeQuery = true)
    Student getStudentByStudentCode(String studentCode);

//    Student findByStudentCode(String studentCode);
    Student getStudentsByStudentCode(String studentCode);

    Student getStudentById(long id);
    @Query(value = "select *\n" +
            "from students\n" +
            "where student_code = :studentCode",nativeQuery = true)
    Optional<Student> getStudentsByStudentCodeOptional(String studentCode);

    @Query(value = "select * from students ",nativeQuery = true)
    List<Student> getAll();
}
