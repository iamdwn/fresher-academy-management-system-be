package java03.team01.FAMS.controller;

import java03.team01.FAMS.model.exception.FamsApiException;

import java.util.logging.Logger;
import java03.team01.FAMS.model.payload.dto.StudentDto;
import java03.team01.FAMS.model.payload.dto.UpdateAttendingStatusDto;
import java03.team01.FAMS.service.StudentClassService;
import java03.team01.FAMS.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


import java.io.FileOutputStream;
import java.io.OutputStream;

@RestController
@RequestMapping("/api/v1/auth/student")
public class StudentController {


    @Autowired
    StudentService studentService;
    @Autowired
    StudentClassService studentClassService;
    @PostMapping("/create")
    public ResponseEntity<?> createStudent(@RequestBody StudentDto studentDto) {
        Logger logger = Logger.getLogger(StudentController.class.getName());
        try {
            StudentDto createdStudent = studentService.createStudent(studentDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdStudent);
        } catch (Exception e) {
            // Log the exception for debugging purposes
            logger.severe("An error occurred while creating the student: " + e.getMessage());
            // Throw an appropriate error response using FamsApiException
            throw new FamsApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Error occurred while creating the student");
        }
    }


    @DeleteMapping("/delete/{studentCode}")
    public ResponseEntity<String> deleteStudent(@PathVariable String studentCode) {
        boolean deletionResult = studentService.deteleStudent(studentCode);

        if (deletionResult) {
            return new ResponseEntity<>("Student deleted successfully.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Unable to delete student. Please check the student code or the student's status.", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<StudentDto>> viewListAllStudents(){
        try {
            List<StudentDto> allStudenList = studentService.viewListAllStudent();
            if(allStudenList != null){
                return new ResponseEntity<>(allStudenList, HttpStatus.OK);
            }else
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }catch (Exception e){
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }


    @GetMapping("/viewStudentByClassId/{classId}")
    public ResponseEntity<List<StudentDto>> viewListStudentByClassId(@PathVariable Long classId) {
        try {
            List<StudentDto> allStudent = studentService.viewListStudentByClass(classId);
            if (!allStudent.isEmpty()) {
                return ResponseEntity.ok(allStudent);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/viewStudentDetails/{studentCode}")
    public ResponseEntity<StudentDto> viewStudentDetails(@PathVariable String studentCode) {
        try {
            StudentDto student = studentService.viewStudentDetails(studentCode);
            if (student != null) {
                return ResponseEntity.ok(student);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private static final String UPLOAD_DIR = "src//main//java//java03.team01.FAMS//data";

    @PostMapping(value = "/upload-students", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadStudents(@RequestPart("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return "Please select a file to upload.";
        }

        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        Path uploadPath = Paths.get(UPLOAD_DIR);
        Path filePath = uploadPath.resolve(fileName);
        try {
            // Lưu tệp trung gian
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Files.copy(file.getInputStream(), filePath);

            // Gọi phương thức service để xử lý tệp CSV
            studentService.saveStudentsFromCSV(filePath.toString());

            return "File uploaded successfully and students saved.";
        } catch (IOException e) {
            return "Failed to upload file: " + e.getMessage();
        } finally {
            Files.delete(filePath);
        }
    }


    @PostMapping("/updateAttendingStatus")
    public ResponseEntity<?> updateAttendingStatusOfStudents(@RequestBody UpdateAttendingStatusDto updateAttendingStatusDto){
        boolean updateAttendingStatusResult = studentClassService.updateAttendingStatusOfStudents(updateAttendingStatusDto.getStudentIds(),updateAttendingStatusDto.getClassId(),updateAttendingStatusDto.getNewStatus());
        if(updateAttendingStatusResult){
            return ResponseEntity.ok().body("Update status successfully");
        } else {
            return ResponseEntity.badRequest().body("Cannot update status");
        }
    }
    @PostMapping("/export")
    public ResponseEntity<byte[]> exportStudent(@RequestBody List<Long> ids) {
        byte[] excelBytes = studentService.exportStudent(ids);
        if (excelBytes != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "students.xlsx");
            try (OutputStream outputStream = new FileOutputStream("src/main/resources/students.xlsx")) {
                outputStream.write(excelBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PutMapping("/edit/{id}")
    public ResponseEntity<StudentDto> editInfoStudent(@PathVariable long id,@RequestBody StudentDto studentDto){
        StudentDto editStudent = studentService.editInfoStudent(studentDto, id);
        if(editStudent != null){
            return new ResponseEntity<>(editStudent,HttpStatus.ACCEPTED);
        }else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
