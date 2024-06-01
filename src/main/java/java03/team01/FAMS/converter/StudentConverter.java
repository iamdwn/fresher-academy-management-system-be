package java03.team01.FAMS.converter;

import java03.team01.FAMS.model.entity.Student;
import java03.team01.FAMS.model.payload.dto.StudentDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class StudentConverter {
    public StudentDto toDto(Student entity) {
        StudentDto dto = new StudentDto();
        dto.setId(entity.getId());
        dto.setStudentCode(entity.getStudentCode());
        dto.setFullName(entity.getFullName());
        dto.setDob(entity.getDob());
        dto.setGender(entity.getGender());
        dto.setPhone(entity.getPhone());
        dto.setEmail(entity.getEmail());
        dto.setSchool(entity.getSchool());
        dto.setMajor(entity.getMajor());
        dto.setGraduatedDate(entity.getGraduatedDate());
        dto.setGpa(entity.getGpa());
        dto.setAddress(entity.getAddress());
        dto.setStatus(entity.getStatus());
        dto.setReCer(entity.getReCer());
        dto.setJoinedDate(entity.getJoinedDate());
        dto.setArea(entity.getArea());
        dto.setFaAccount(entity.getFaAccount());
        dto.setType(entity.getType());
        return dto;
    }

    public Student toEntity(StudentDto dto) {
        Student entity = new Student();
        entity.setId(dto.getId());
        entity.setStudentCode(dto.getStudentCode());
        entity.setFullName(dto.getFullName());
        entity.setDob(dto.getDob());
        entity.setGender(dto.getGender());
        entity.setPhone(dto.getPhone());
        entity.setEmail(dto.getEmail());
        entity.setSchool(dto.getSchool());
        entity.setMajor(dto.getMajor());
        entity.setGraduatedDate(dto.getGraduatedDate());
        entity.setGpa(dto.getGpa());
        entity.setAddress(dto.getAddress());
        entity.setStatus(dto.getStatus());
        entity.setReCer(dto.getReCer());
        entity.setJoinedDate(dto.getJoinedDate());
        entity.setArea(dto.getArea());
        entity.setFaAccount(dto.getFaAccount());
        entity.setType(dto.getType());
        return entity;
    }

    public List<StudentDto> convertToStudentDTOList(List<Student> studentList) {
        List<StudentDto> studentDTOList = new ArrayList<>();
        for (Student studentLists : studentList) {
            studentDTOList.add(toDto(studentLists));
        }
        return studentDTOList;
    }
    public List<Student> convertToStudentList(List<StudentDto> studentDtoList) {
        List<Student> studentList = new ArrayList<>();
        for (StudentDto studentDto : studentDtoList) {
            studentList.add(toEntity(studentDto));
        }
        return studentList;
    }
}
