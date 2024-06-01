package java03.team01.FAMS.service.impl;

import java03.team01.FAMS.converter.StudentConverter;
import java03.team01.FAMS.model.entity.Score;
import java03.team01.FAMS.model.entity.Student;

import java03.team01.FAMS.model.entity.StudentClass;
import java03.team01.FAMS.model.payload.dto.StudentDto;
import java03.team01.FAMS.repository.ScoreRepository;
import java03.team01.FAMS.repository.StudentClassRepository;
import java03.team01.FAMS.repository.StudentRepository;
import java03.team01.FAMS.service.StudentService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class StudentServiceImpl implements StudentService {
    private final String UPLOAD_DIR = "src//main//java//java03.team01.FAMS//csv//abcd.csv";
    @Autowired
    StudentConverter studentConverter;
    @Autowired
    StudentRepository studentRepository;
    @Autowired
    StudentClassRepository studentClassRepository;
    @Autowired
    ScoreRepository scoreRepository;

    @Override
    public StudentDto createStudent(StudentDto student) {
        Student studentEntity = studentConverter.toEntity(student);
        studentRepository.save(studentEntity);
        return studentConverter.toDto(studentEntity);
    }

    @Override
    public StudentDto viewStudentDetails(String studentCode) {
        return studentConverter.toDto(studentRepository.getStudentByStudentCode(studentCode));
    }

    @Override
    public List<StudentDto> viewListAllStudent() {
        List<Student> studentList = studentRepository.findAll();
        return studentConverter.convertToStudentDTOList(studentList);
    }

    @Override
    public List<StudentDto> viewListStudentByClass(Long classId) {
        if (classId == null) {
            return Collections.emptyList();
        }

        List<StudentClass> studentList = studentClassRepository.getStudentClassByClassId(classId);

        List<Student> students = studentList.stream()
                .map(StudentClass::getStudent)
                .collect(Collectors.toList());

        List<StudentDto> studentDtos = students.stream()
                .map(studentConverter::toDto)
                .collect(Collectors.toList());

        return studentDtos;
    }

    public Boolean deteleStudent(String studentCode) {
        Student student = studentRepository.getStudentByStudentCode(studentCode);
        if (student != null) {
            if (student.getStatus().equals("inactive")) {
                studentRepository.delete(student);
                return true;
            }
            Score score = scoreRepository.getScoreByStudentId(student.getId());
            if (score == null) {

                studentRepository.delete(student);
                return true;

            }
        }
        return false;
    }

    @Override
    public void saveStudentsFromCSV(String filePath) throws IOException {
        List<StudentDto> studentDTOList = readStudentsFromCSV(filePath);
        for (StudentDto studentDTO : studentDTOList) {
            Student student = new Student();
            student.setStudentCode(studentDTO.getStudentCode());
            student.setFullName(studentDTO.getFullName());
            student.setDob(studentDTO.getDob());
            student.setGender(studentDTO.getGender());
            student.setPhone(studentDTO.getPhone());
            student.setEmail(studentDTO.getEmail());
            student.setSchool(studentDTO.getSchool());
            student.setMajor(studentDTO.getMajor());
            student.setGraduatedDate(studentDTO.getGraduatedDate());
            student.setGpa(studentDTO.getGpa());
            student.setAddress(studentDTO.getAddress());
            student.setFaAccount(studentDTO.getFaAccount());
            student.setType(studentDTO.getType());
            student.setStatus(studentDTO.getStatus());
            student.setReCer(studentDTO.getReCer());
            student.setJoinedDate(studentDTO.getJoinedDate());
            student.setArea(studentDTO.getArea());
            studentRepository.save(student);
        }
    }


    public List<StudentDto> readStudentsFromCSV(String filePath) throws IOException {
        List<StudentDto> studentDTOList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                StudentDto studentDTO = new StudentDto();
                studentDTO.setAddress(data[0].trim());
                studentDTO.setArea(data[1].trim());
                studentDTO.setDob(LocalDate.parse(data[2].trim().replace("/", "-")));
                studentDTO.setEmail(data[3].trim());
                studentDTO.setFaAccount(data[4].trim());
                studentDTO.setFullName(data[5].trim());
                studentDTO.setGender(data[6].trim());
                studentDTO.setGpa(Float.parseFloat(data[7].trim()));
                studentDTO.setGraduatedDate(LocalDate.parse(data[8].trim().replace("/", "-")));
                studentDTO.setJoinedDate(LocalDate.parse(data[9].trim().replace("/", "-")));
                studentDTO.setMajor(data[10].trim());
                studentDTO.setPhone(data[11].trim());
                studentDTO.setReCer(data[12].trim());
                studentDTO.setSchool(data[13].trim());
                studentDTO.setStatus(data[14].trim());
                studentDTO.setStudentCode(data[15].trim());
                studentDTO.setType(data[16].trim());
                studentDTOList.add(studentDTO);
            }
        }
        return studentDTOList;
    }

    @Override
    public byte[] exportStudent(List<Long> ids) {

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Students");

            // Tạo hàng đầu tiên với tiêu đề cột
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Full name");
            headerRow.createCell(1).setCellValue("Date of birth");
            headerRow.createCell(2).setCellValue("Email");
            headerRow.createCell(3).setCellValue("Phone");
            headerRow.createCell(4).setCellValue("GPA");
            headerRow.createCell(5).setCellValue("RECer");

            // Đổ dữ liệu từ danh sách StudentDto vào workbook
            int rowNum = 1;

//            for (ExportStudentDTO studentDto : listStudent) {
//                Row row = sheet.createRow(rowNum++);
//                row.createCell(0).setCellValue(studentDto.getFullName());
//                row.createCell(1).setCellValue(studentDto.getDob());
//                row.createCell(2).setCellValue(studentDto.getEmail());
//                row.createCell(3).setCellValue(studentDto.getPhone());
//                row.createCell(4).setCellValue(studentDto.getGpa());
//                row.createCell(5).setCellValue(studentDto.getReCer());
//            }
            for (Long id : ids) {
                StudentDto studentDto = studentConverter.toDto(studentRepository.getReferenceById(id));
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(studentDto.getFullName());
                row.createCell(1).setCellValue(studentDto.getDob().toString());
                row.createCell(2).setCellValue(studentDto.getEmail());
                row.createCell(3).setCellValue(studentDto.getPhone());
                row.createCell(4).setCellValue(studentDto.getGpa());
                row.createCell(5).setCellValue(studentDto.getReCer());
            }

            // Tạo một luồng để ghi workbook vào
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            return outputStream.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return  null;
        }
    }



    public StudentDto editInfoStudent(StudentDto studentDto, long id) {
        Student fromDB = studentRepository.findById(id).orElse(null);
        if(fromDB == null){
            return null;
        }
        String status = fromDB.getStatus().trim().toLowerCase();
        switch (status){
            case "in class":
                // được câp nhật tất cả trừ full name, email\
                fromDB.setStudentCode(studentDto.getStudentCode());
                fromDB.setDob(studentDto.getDob());
                fromDB.setGender(studentDto.getGender());
                fromDB.setPhone(studentDto.getPhone());
                fromDB.setSchool(studentDto.getSchool());
                fromDB.setMajor(studentDto.getMajor());
                fromDB.setGraduatedDate(studentDto.getGraduatedDate());
                fromDB.setGpa(studentDto.getGpa());
                fromDB.setAddress(studentDto.getAddress());
                fromDB.setStatus(studentDto.getStatus());
                fromDB.setReCer(studentDto.getReCer());
                fromDB.setJoinedDate(studentDto.getJoinedDate());
                fromDB.setArea(studentDto.getArea());
                fromDB.setFaAccount(studentDto.getFaAccount());
                fromDB.setType(studentDto.getType());
                break;
            case "drop out":
                // chỉ được cập nhật status thành in class
                fromDB.setStatus("In Class");
                break;
            case "finish":
                // không được cập nhật gì c
                break;
            case "reserve":
                // không được cập nhật gì cả
                break;
            default:
                return null;
        }
        studentRepository.save(fromDB);
        return studentConverter.toDto(fromDB);
    }
}


