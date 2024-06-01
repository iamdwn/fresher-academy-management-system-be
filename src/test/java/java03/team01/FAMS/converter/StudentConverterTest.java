package java03.team01.FAMS.converter;

import java03.team01.FAMS.model.entity.Student;
import java03.team01.FAMS.model.payload.dto.StudentDto;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StudentConverterTest {

    @Test
    public void testToDto() {
        Student entity = new Student();
        entity.setId(1L);
        entity.setStudentCode("S123");
        entity.setFullName("John Doe");
        entity.setDob(LocalDate.of(2000, 1, 1));
        entity.setGender("Male");
        entity.setPhone("123456789");
        entity.setEmail("john.doe@example.com");
        entity.setSchool("Example University");
        entity.setMajor("Computer Science");
        entity.setGraduatedDate(LocalDate.of(2023, 5, 15));
        entity.setGpa(3.5f);
        entity.setAddress("123 Main St, City");
        entity.setStatus("Active");
        entity.setReCer("ABC123");
        entity.setJoinedDate(LocalDate.of(2020, 9, 1));
        entity.setArea("Urban");
        entity.setFaAccount("FA123");
        entity.setType("Regular");

        StudentConverter converter = new StudentConverter();
        StudentDto dto = converter.toDto(entity);

        assertEquals(entity.getId(), dto.getId());
        assertEquals(entity.getStudentCode(), dto.getStudentCode());
        assertEquals(entity.getFullName(), dto.getFullName());
        assertEquals(entity.getDob(), dto.getDob());
        assertEquals(entity.getGender(), dto.getGender());
        assertEquals(entity.getPhone(), dto.getPhone());
        assertEquals(entity.getEmail(), dto.getEmail());
        assertEquals(entity.getSchool(), dto.getSchool());
        assertEquals(entity.getMajor(), dto.getMajor());
        assertEquals(entity.getGraduatedDate(), dto.getGraduatedDate());
        assertEquals(entity.getGpa(), dto.getGpa());
        assertEquals(entity.getAddress(), dto.getAddress());
        assertEquals(entity.getStatus(), dto.getStatus());
        assertEquals(entity.getReCer(), dto.getReCer());
        assertEquals(entity.getJoinedDate(), dto.getJoinedDate());
        assertEquals(entity.getArea(), dto.getArea());
        assertEquals(entity.getFaAccount(), dto.getFaAccount());
        assertEquals(entity.getType(), dto.getType());
    }

    @Test
    public void testToEntity() {
        StudentDto dto = new StudentDto();
        dto.setId(1L);
        dto.setStudentCode("S123");
        dto.setFullName("John Doe");
        dto.setDob(LocalDate.of(2000, 1, 1));
        dto.setGender("Male");
        dto.setPhone("123456789");
        dto.setEmail("john.doe@example.com");
        dto.setSchool("Example University");
        dto.setMajor("Computer Science");
        dto.setGraduatedDate(LocalDate.of(2023, 5, 15));
        dto.setGpa(3.5f);
        dto.setAddress("123 Main St, City");
        dto.setStatus("Active");
        dto.setReCer("ABC123");
        dto.setJoinedDate(LocalDate.of(2020, 9, 1));
        dto.setArea("Urban");
        dto.setFaAccount("FA123");
        dto.setType("Regular");

        StudentConverter converter = new StudentConverter();
        Student entity = converter.toEntity(dto);

        assertEquals(dto.getId(), entity.getId());
        assertEquals(dto.getStudentCode(), entity.getStudentCode());
        assertEquals(dto.getFullName(), entity.getFullName());
        assertEquals(dto.getDob(), entity.getDob());
        assertEquals(dto.getGender(), entity.getGender());
        assertEquals(dto.getPhone(), entity.getPhone());
        assertEquals(dto.getEmail(), entity.getEmail());
        assertEquals(dto.getSchool(), entity.getSchool());
        assertEquals(dto.getMajor(), entity.getMajor());
        assertEquals(dto.getGraduatedDate(), entity.getGraduatedDate());
        assertEquals(dto.getGpa(), entity.getGpa());
        assertEquals(dto.getAddress(), entity.getAddress());
        assertEquals(dto.getStatus(), entity.getStatus());
        assertEquals(dto.getReCer(), entity.getReCer());
        assertEquals(dto.getJoinedDate(), entity.getJoinedDate());
        assertEquals(dto.getArea(), entity.getArea());
        assertEquals(dto.getFaAccount(), entity.getFaAccount());
        assertEquals(dto.getType(), entity.getType());
    }

    @Test
    public void testConvertToStudentDTOList() {
        // Tạo danh sách các đối tượng Student và thêm vào studentList
        List<Student> studentList = new ArrayList<>();
        Student student1 = new Student();
        student1.setId(1L);
        student1.setStudentCode("S123");
        student1.setFullName("John Doe");
        student1.setDob(LocalDate.of(2000, 1, 1));
        student1.setGender("Male");
        student1.setPhone("123456789");
        student1.setEmail("john.doe@example.com");
        student1.setSchool("Example University");
        student1.setMajor("Computer Science");
        student1.setGraduatedDate(LocalDate.of(2023, 5, 15));
        student1.setGpa(3.5f);
        student1.setAddress("123 Main St, City");
        student1.setStatus("Active");
        student1.setReCer("ABC123");
        student1.setJoinedDate(LocalDate.of(2020, 9, 1));
        student1.setArea("Urban");
        student1.setFaAccount("FA123");
        student1.setType("Regular");

        studentList.add(student1);

        Student student2 = new Student();
        student2.setId(2L);
        student2.setStudentCode("S1234");
        student2.setFullName("John Doe2");
        student2.setDob(LocalDate.of(2000, 1, 2));
        student2.setGender("FeMale");
        student2.setPhone("1234567890");
        student2.setEmail("john.doe@example2.com");
        student2.setSchool("Example University2");
        student2.setMajor("Computer Science2");
        student2.setGraduatedDate(LocalDate.of(2023, 5, 16));
        student2.setGpa(3.5f);
        student2.setAddress("123 Main St, City2");
        student2.setStatus("Active");
        student2.setReCer("ABC1234");
        student2.setJoinedDate(LocalDate.of(2020, 9, 2));
        student2.setArea("Urban");
        student2.setFaAccount("FA123");
        student2.setType("Regular");
        studentList.add(student2);

        // Khởi tạo một đối tượng StudentConverter
        StudentConverter converter = new StudentConverter();

        // Chuyển đổi danh sách Student thành danh sách StudentDto
        List<StudentDto> studentDtoList = converter.convertToStudentDTOList(studentList);

        // Kiểm tra xem số lượng phần tử có khớp không
        assertEquals(studentList.size(), studentDtoList.size());

        // Kiểm tra các thuộc tính của mỗi DTO trong danh sách
        for (int i = 0; i < studentList.size(); i++) {
            StudentDto dto = studentDtoList.get(i);
            Student student = studentList.get(i);

            assertEquals(student.getId(), dto.getId());
            assertEquals(student.getStudentCode(), dto.getStudentCode());
            assertEquals(student.getFullName(), dto.getFullName());
            assertEquals(student.getDob(), dto.getDob());
            // Kiểm tra các thuộc tính khác...
        }
    }

    @Test
    public void testConvertToStudentList() {
        // Tạo danh sách các đối tượng StudentDto và thêm vào studentDtoList
        List<StudentDto> studentDtoList = new ArrayList<>();
        StudentDto dto1 = new StudentDto();
        dto1.setId(1L);
        dto1.setStudentCode("S123");
        dto1.setFullName("John Doe");
        dto1.setDob(LocalDate.of(2000, 1, 1));
        dto1.setGender("Male");
        dto1.setPhone("123456789");
        dto1.setEmail("john.doe@example.com");
        dto1.setSchool("Example University");
        dto1.setMajor("Computer Science");
        dto1.setGraduatedDate(LocalDate.of(2023, 5, 15));
        dto1.setGpa(3.5f);
        dto1.setAddress("123 Main St, City");
        dto1.setStatus("Active");
        dto1.setReCer("ABC123");
        dto1.setJoinedDate(LocalDate.of(2020, 9, 1));
        dto1.setArea("Urban");
        dto1.setFaAccount("FA123");
        dto1.setType("Regular");
        studentDtoList.add(dto1);

        StudentDto dto2 = new StudentDto();
        dto2.setId(2L);
        dto2.setStudentCode("S1234");
        dto2.setFullName("John Doe2");
        dto2.setDob(LocalDate.of(2000, 1, 2));
        dto2.setGender("FeMale");
        dto2.setPhone("1234567890");
        dto2.setEmail("john.doe@example2.com");
        dto2.setSchool("Example University2");
        dto2.setMajor("Computer Science2");
        dto2.setGraduatedDate(LocalDate.of(2023, 5, 16));
        dto2.setGpa(3.5f);
        dto2.setAddress("123 Main St, City2");
        dto2.setStatus("Active");
        dto2.setReCer("ABC1234");
        dto2.setJoinedDate(LocalDate.of(2020, 9, 2));
        dto2.setArea("Urban");
        dto2.setFaAccount("FA123");
        dto2.setType("Regular");
        studentDtoList.add(dto2);

        StudentConverter converter = new StudentConverter();

        List<Student> studentList = converter.convertToStudentList(studentDtoList);

        assertEquals(studentDtoList.size(), studentList.size());

        for (int i = 0; i < studentDtoList.size(); i++) {
            StudentDto dto = studentDtoList.get(i);
            Student student = studentList.get(i);

            assertEquals(dto.getId(), student.getId());
            assertEquals(dto.getStudentCode(), student.getStudentCode());
            assertEquals(dto.getFullName(), student.getFullName());
            assertEquals(dto.getDob(), student.getDob());
            assertEquals(dto.getGender(), student.getGender());
            assertEquals(dto.getPhone(), student.getPhone());
            assertEquals(dto.getEmail(), student.getEmail());
            assertEquals(dto.getSchool(), student.getSchool());
            assertEquals(dto.getMajor(), student.getMajor());
            assertEquals(dto.getGraduatedDate(), student.getGraduatedDate());
            assertEquals(dto.getGpa(), student.getGpa());
            assertEquals(dto.getAddress(), student.getAddress());
            assertEquals(dto.getStatus(), student.getStatus());
            assertEquals(dto.getReCer(), student.getReCer());
            assertEquals(dto.getJoinedDate(), student.getJoinedDate());
            assertEquals(dto.getArea(), student.getArea());
            assertEquals(dto.getFaAccount(), student.getFaAccount());
            assertEquals(dto.getType(), student.getType());
        }
    }
}
