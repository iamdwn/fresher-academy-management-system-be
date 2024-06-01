package java03.team01.FAMS.service;

import java03.team01.FAMS.model.payload.dto.StudentDto;

import java.io.IOException;
import java.util.List;

public interface StudentService {
    StudentDto createStudent (StudentDto student);

    StudentDto viewStudentDetails(String studentCode);

    List<StudentDto> viewListAllStudent();

    List<StudentDto> viewListStudentByClass(Long classId);

    Boolean deteleStudent(String studentCode);

    byte[] exportStudent(List<Long> ids);

    void saveStudentsFromCSV(String filePath) throws IOException;

    StudentDto editInfoStudent(StudentDto studentDto, long id);

}
