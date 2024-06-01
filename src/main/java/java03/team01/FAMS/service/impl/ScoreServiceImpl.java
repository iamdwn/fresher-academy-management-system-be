package java03.team01.FAMS.service.impl;

import jakarta.persistence.IdClass;
import jakarta.transaction.Transactional;
import java03.team01.FAMS.model.entity.*;
import java03.team01.FAMS.model.entity.Class;
import java03.team01.FAMS.model.entity.Module;
import java03.team01.FAMS.model.exception.FamsApiException;
import java03.team01.FAMS.model.exception.ResourceNotFoundException;
import java03.team01.FAMS.model.payload.requestModel.CustomCertificateRequest;
import java03.team01.FAMS.model.payload.requestModel.CustomScoreRequest;
import java03.team01.FAMS.model.payload.responseModel.*;
import java03.team01.FAMS.repository.*;
import java03.team01.FAMS.service.ScoreService;
import java03.team01.FAMS.utils.Utils;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional
@Service
public class ScoreServiceImpl implements ScoreService {

    private final StudentRepository studentRepository;
    private final ModuleRepository moduleRepository;
    private final AssignmentRepository assignmentRepository;
    private final StudentClassRepository studentClassRepository;
    private final StudentModuleRepository studentModuleRepository;
    private final ScoreRepository scoreRepository;
    private final ModelMapper modelMapper;
    private final ClassRepository classRepository;


    @Override
    public ScoreClassResponse getScoreOfClass(int pageNo, int pageSize, Long idClass) {
        try {
            classRepository.findClassById(idClass)
                    .orElseThrow(() -> new FamsApiException(HttpStatus.NOT_FOUND, "Unknown class"));

            Pageable pageable = PageRequest.of(pageNo, pageSize);
            Page<Object> students = studentClassRepository.getPageStudentByClassId(pageable, idClass);

            List<Long> studentIds = studentClassRepository.getStudentIdByClassId(idClass);
            List<Long> moduleIds;
            List<CustomScoreResponse> customScoreResponses = new ArrayList<>();

            for (Long studentId : studentIds) {
                Student student = studentRepository.findById(studentId)
                        .orElseThrow(() -> new FamsApiException(HttpStatus.NOT_FOUND, "No student in this class"));

                moduleIds = studentModuleRepository.getModuleByStudentId(studentId);

                customScoreResponses.add(new CustomScoreResponse(
                                student.getId(),
                                student.getFullName(),
                                student.getFaAccount(),
                                getModule(moduleIds, student.getId())
                        )
                );
            }

            // avg module score sort desc
            customScoreResponses.sort(Comparator.comparingDouble(CustomScoreResponse::getAverageModuleScore).reversed());

            ScoreClassResponse scoreClassResponse = new ScoreClassResponse();
            scoreClassResponse.setContent(customScoreResponses);
            scoreClassResponse.setPageNo(students.getNumber());
            scoreClassResponse.setPageSize(students.getSize());
            scoreClassResponse.setTotalElements(students.getTotalElements());
            scoreClassResponse.setTotalPages(students.getTotalPages());
            scoreClassResponse.setLast(students.isLast());
            return scoreClassResponse;

        } catch (FamsApiException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new FamsApiException(HttpStatus.NOT_FOUND, "Failed");
        }
    }

    public List<CustomAssignResponse> getAssignment(Long moduleId, Long studentId) {
        List<CustomAssignResponse> customAssignResponses = new ArrayList<>();
        List<Assignment> assignmentList = assignmentRepository.getListAssignmentByModuleId(moduleId);
        for (Assignment a : assignmentList) {
            customAssignResponses.add(new CustomAssignResponse(
                    a.getId(),
                    a.getAssignmentType(),
                    a.getAssignmentName(),
                    getScoresForAssignment(a.getId(), studentId))
            );
        }

        return customAssignResponses;
    }

    //TODO: get score assignment for student
    public Set<Float> getScoresForAssignment(Long assignmentId, Long studentId) {
        return scoreRepository.getScoreByAssignmentIdAndStudentId(assignmentId, studentId)
                .stream()
                .map(Score::getScore)
                .collect(Collectors.toSet());
    }

    public List<CustomModuleResponse> getModule(List<Long> moduleIds, Long studentId) {
        List<StudentModule> studentModules = new ArrayList<>();
        List<CustomModuleResponse> customModuleResponses = new ArrayList<>();
        for (Long moduleId : moduleIds) {
            StudentModule studentModule = studentModuleRepository.findByModuleIdAndStudentId(moduleId, studentId)
                    .orElseThrow(() -> new FamsApiException(HttpStatus.NOT_FOUND, "Unknown student_module"));
            studentModules.add(studentModule);
        }

        for (StudentModule m : studentModules) {
            Module module = m.getModule();
            if (module == null) {
                throw new FamsApiException(HttpStatus.NOT_FOUND, "Student " + m.getStudent() + " missing a module");
            } else {
                module = moduleRepository.findById(m.getModule().getId())
                        .orElseThrow(() -> new FamsApiException(HttpStatus.NOT_FOUND, "Unknown module"));
                customModuleResponses.add(new CustomModuleResponse(
                        module.getId(), module.getModuleName(),
                        studentModuleRepository.getModuleScoreByStudentIdAndModuleId(m.getStudent().getId(), m.getModule().getId()),
                        getAssignment(m.getModule().getId(), studentId),
                        m.getStatus()
                ));
            }
        }

        return customModuleResponses;
    }

    @Override
    public CustomObjectResponse getScoreOfClassAndStudentCode(Long idClass, String studentCode) {
        Optional<Class> classEntity = classRepository.findClassById(idClass);
        Student studentEntity = studentRepository.getStudentByStudentCode(studentCode);
        Optional<StudentClass> studentClass;

        if (!classEntity.isPresent())
            return new CustomObjectResponse("BAD_REQUEST", "Not found this class", "Searched failed");
        if (studentEntity == null)
            return new CustomObjectResponse("BAD_REQUEST", "Not found this student", "Searched failed");

        Long studentId;
        studentId = studentClassRepository.getStudentIdByStudentCode(studentEntity.getId(), idClass);
        if (studentId == null) throw new FamsApiException(HttpStatus.NOT_FOUND, "Student not in this class");

        List<Long> moduleIds;
        moduleIds = studentModuleRepository.getModuleByStudentId(studentId);
        studentClass = studentClassRepository.getStudentClassByStudentId(studentId, idClass);

        //CustomScoreResponse customScoreResponses = new CustomScoreResponse();
        ScoreDetailResponse ScoreDetailResponse = new ScoreDetailResponse(
                studentEntity.getId(),
                studentEntity.getFullName(),
                studentEntity.getFaAccount(),
                getModule(moduleIds, studentEntity.getId()),
                classEntity.get().getClassCode(),
                classEntity.get().getDuration(),
                studentClass.get().getFinalScore(),
                studentClass.get().getGpaLevel()
        );
        return new CustomObjectResponse("SUCCESS", "", ScoreDetailResponse);
    }


    @Override
    public CustomObjectResponse updateScoreOfClass(CustomScoreRequest customScoreRequest) {
//        Optional<Score> scoreEntity = scoreRepository.findById(customScoreRequest.getScoreId());
//        if(scoreEntity.isEmpty())
//        return new CustomObjectResponse("BAD_REQUEST", "Not found any scores of this student", "Updated failed");

        Long studentId = scoreRepository.getStudentIdByScoreId(customScoreRequest.getScoreId());
        Optional<Student> student = studentRepository.findById(studentId);

        Long assignmentId = scoreRepository.getAssignmentIdByScoreId(customScoreRequest.getScoreId());
        Long moduleId = assignmentRepository.getModuleIdByAssignmentId(assignmentId);
        Optional<Module> moduleEntity = moduleRepository.findById(moduleId);

        Optional<StudentModule> studentModuleEntity = studentModuleRepository.findByModuleIdAndStudentId(moduleId, studentId);
        List<Assignment> assignmentList = assignmentRepository.getAssignmentByModuleIdAndStudentId(moduleId, studentId);
        Optional<Score> score = scoreRepository.getScoreAssignmentIdAndStudentId(
//                customScoreRequest.getAssignmentId(),
                assignmentId,
//                customScoreRequest.getStudentId(),
                studentId,
                customScoreRequest.getScoreId()
        );

        if (student.isEmpty())
            return new CustomObjectResponse("BAD_REQUEST", "Not found this student", "Updated failed");
        if (studentModuleEntity.isEmpty())
            return new CustomObjectResponse("BAD_REQUEST", "Not found any module scores of this student", "Updated failed");
        if (assignmentList.isEmpty())
            return new CustomObjectResponse("BAD_REQUEST", "Not found any assignments", "Updated failed");
        if (score.isEmpty())
            return new CustomObjectResponse("BAD_REQUEST", "Not found any scores of this student", "Updated failed");
        if (customScoreRequest.getScoreValue() < 0
                || customScoreRequest.getScoreValue() > 10)
            return new CustomObjectResponse("BAD_REQUEST", "Value of this score is out of range", "Updated failed");

//        if (studentModuleEntity.isEmpty())
//        {
//            StudentModule studentModule = new StudentModule(
//                    new StudentModuleKey(studentId, moduleId),
//                    student.get(),
//                    moduleEntity.get(),
//                    customScoreRequest.getScoreValue(),
//                    1F,
//                    "Failed"
//            );
//
//            studentModuleRepository.save(studentModule);
//        }

        score.get().setScore(customScoreRequest.getScoreValue());

        scoreRepository.save(score.get());

        Double avrScore = studentModuleRepository.getAverageModuleScore(studentId, moduleId);

        studentModuleEntity.get().setModuleScore(avrScore.floatValue());

        if (avrScore.floatValue() >= 6)
            studentModuleEntity.get().setStatus("Passed");
        else studentModuleEntity.get().setStatus("Failed");

        studentModuleRepository.save(studentModuleEntity.get());

        Float avrModule = studentModuleRepository.getAverageGPA(studentId);

        student.get().setGpa(avrModule);

//        if (avrModule >= 6)
//            student.get().setStatus("Passed");
//        else student.get().setStatus("Failed");

        studentRepository.save(student.get());

        return new CustomObjectResponse("SUCCESS", "The scores have been successfully updated", "");
    }

    @Override
    public CustomObjectResponse updateCertificate(CustomCertificateRequest customCertificateRequest) {
        Optional<Student> student = studentRepository.findById(customCertificateRequest.getStudentId());
        List<StudentModule> studentModules = studentModuleRepository.findByStudentId(customCertificateRequest.getStudentId());
        List<Assignment> assignmentList;
//        Optional<StudentClass> studentClass = studentClassRepository.getStudentClassByStudentId(customCertificateRequest.getStudentId());

        if (student.isEmpty())
            return new CustomObjectResponse("BAD_REQUEST", "Not found this student", "Updated failed");

        for (StudentModule sm : studentModules) {
            if (sm.getModuleScore().isNaN() || sm.getModuleScore().equals(0))
                return new CustomObjectResponse("BAD_REQUEST", "This student does not have full module points", "Updated failed");
            if (sm.getModule() == null)
                return new CustomObjectResponse("BAD_REQUEST", "Not found any modules of this student", "Updated failed");


            assignmentList = assignmentRepository.getAssignmentByModuleIdAndStudentId(sm.getModule().getId(), customCertificateRequest.getStudentId());
            for (Assignment a : assignmentList) {
                if (a.getScores().isEmpty() || a.getScores() == null)
                    return new CustomObjectResponse("BAD_REQUEST", "This student does not have full assignment points", "Updated failed");
            }

            if (!sm.getStatus().equalsIgnoreCase("Passed")) {
//                studentClass.get().setCertificationStatus("Not yet");
//                studentRepository.save(student.get());
                return new CustomObjectResponse("BAD_REQUEST", "This student does not have passed all modules", "Updated failed");
            }
        }

//        student.get().setStatus(customCertificateRequest.getCertificateStatus());
//        studentRepository.save(student.get());

        return new CustomObjectResponse("SUCCESS", "This student is qualified to receive a certificate", "Updated successfully");
    }


    @Override
    public ImportExcelResponse importScoresFromExcel(File file) throws IOException {
        ImportExcelResponse importExcelResponse = new ImportExcelResponse();
        List<String> successList = new ArrayList<>();
        List<String> failList = new ArrayList<>();
        FileInputStream fis = new FileInputStream(file);
        Workbook workbook = WorkbookFactory.create(fis);
        try {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                try {
                    if (row.getRowNum() == 0) {
                        continue;
                    }
//                    Cell scoreIdCell = row.getCell(0);
                    Cell assignmentIdCell = row.getCell(1);
                    Cell scoreCell = row.getCell(2);
                    Cell submissionDateCell = row.getCell(3);
                    Cell studentIdCell = row.getCell(4);

                    if (studentIdCell == null || assignmentIdCell == null || scoreCell == null || submissionDateCell == null) {
                        System.out.println("One or more cells in the row are null. Skipping this row.");
                        break;
                    }

//                    Long scoreId = (long) row.getCell(0).getNumericCellValue();

//                    if (!Utils.isValidNumber(row.getCell(0).getStringCellValue())
//                            || !Utils.isValidNumber(row.getCell(3).getStringCellValue())
//                            || !Utils.isValidNumber(row.getCell(4).getStringCellValue())) {
//                        throw new FamsApiException(HttpStatus.BAD_REQUEST, "At Row " + row.getRowNum() + ". The value of score format is invalid, the format must be number");
//                    }

//                    Float scoreValue = Float.parseFloat(row.getCell(0).getStringCellValue());
//                    Long assignmentId = Long.parseLong(row.getCell(3).getStringCellValue());
//                    Long studentId = Long.parseLong(row.getCell(4).getStringCellValue());

                    Float scoreValue = (float) row.getCell(1).getNumericCellValue();
                    if (!Utils.isValidNumber(String.valueOf(scoreValue))) {
                        throw new FamsApiException(HttpStatus.BAD_REQUEST, "At Row " + row.getRowNum() + ". The value of score format is invalid, the format must be number");
                    }

                    if (scoreValue < 0 || scoreValue > 10)
                        throw new FamsApiException(HttpStatus.BAD_REQUEST, "At Row " + row.getRowNum() + ". Value of score is out of range");


                    if (!Utils.DateTimeFormatCheck(row.getCell(2).getStringCellValue())) {
                        throw new FamsApiException(HttpStatus.BAD_REQUEST, "At Row " + row.getRowNum() + ". The date time format is invalid, the format must be (yyyy-MM-dd)");
                    }

//                    LocalDate submissionDate = row.getCell(2).getLocalDateTimeCellValue().toLocalDate();
                    Long assignmentId = (long) row.getCell(3).getNumericCellValue();
                    Assignment assignment = assignmentRepository.findById(assignmentId)
                            .orElseThrow(() -> new ResourceNotFoundException("At Row " + row.getRowNum() + ". Assignment", "id", Math.round(assignmentId)));

                    Long studentId = (long) row.getCell(4).getNumericCellValue();
                    Student student = studentRepository.findById(studentId)
                            .orElseThrow(() -> new ResourceNotFoundException("At Row " + row.getRowNum() + ". Student", "id", Math.round(studentId)));

                    Long moduleId = assignmentRepository.getModuleIdByAssignmentId(assignmentId);
                    if (!moduleRepository.findById(moduleId).isPresent()) {
                        throw new FamsApiException(HttpStatus.BAD_REQUEST, "At Row " + row.getRowNum() + ". The module is not exist");
                    }

                    Optional<StudentModule> studentModuleEntity = studentModuleRepository.findByModuleIdAndStudentId(moduleId, studentId);
                    if (!studentModuleEntity.isPresent()) {
                        throw new FamsApiException(HttpStatus.BAD_REQUEST, "At Row " + row.getRowNum() + ". Student is not belong to module");
                    }

                    Optional<Score> scoreEntity = scoreRepository.getByAssignmentIdAndStudentId(assignment.getId(), student.getId());
                    if (!scoreEntity.isPresent()) {
                        Score score = new Score();
                        score.setStudent(student);
                        score.setAssignment(assignment);
                        score.setScore(scoreValue);
                        score.setSubmissionDate(LocalDate.parse(row.getCell(2).getStringCellValue()));
                        scoreRepository.save(score);
                    } else {
                        CustomScoreRequest request = new CustomScoreRequest(
                                scoreEntity.get().getId(),
                                scoreValue
                        );
                        updateScoreOfClass(request);
                    }
                    successList.add("Score of student: " + student + " add successfully has value: " + scoreValue);
                } catch (FamsApiException e) {
                    failList.add(e.getMessage());
                } catch (ResourceNotFoundException re) {
                    failList.add(re.getMessage());
                }
            }
            workbook.close();
            fis.close();
//            List<Student> studentList = studentRepository.findAll();
//            List<Module> moduleList = moduleRepository.findAll();
//            List<Score> scoreList =scoreRepository.findAll();
//
//            for (Student s : studentList) {
//                for (Module m : moduleList) {
//                    Optional<StudentModule> studentModuleEntity = studentModuleRepository.findByMoSWduleIdAndStudentId(m.getId(), s.getId());
//
//                    Double avrScore = studentModuleRepository.getAverageModuleScore(s.getId(), m.getId());
//
//                    if (avrScore == null) break;
//
//                    studentModuleEntity.get().setModuleScore(avrScore.floatValue());
//
//                    studentModuleRepository.save(studentModuleEntity.get());
//                }
//
//                Float avrModule = studentModuleRepository.getAverageGPA(s.getId());
//
//                if (avrModule == null) break;
//
//                s.setGpa(avrModule);
//
//                studentRepository.save(s);
//            }

            importExcelResponse.setSuccessList(successList);
            importExcelResponse.setFailList(failList);
            return importExcelResponse;
        } catch (IOException e) {
            throw new FamsApiException(HttpStatus.BAD_REQUEST, "Import failed!");
        }
    }


    @Override
    public ImportExcelResponse uploadFile(MultipartFile file) throws IOException {
        try {
            if (!Utils.getFileType(file.getOriginalFilename()).equalsIgnoreCase("xlsx")) {
                throw new FamsApiException(HttpStatus.BAD_REQUEST, "Only .xlsx file is supported!");
            }

            if (file.isEmpty()) {
                throw new IllegalArgumentException("File is empty");
            }

            String uploadDir = file.getOriginalFilename();

            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            byte[] bytes = file.getBytes();
            Path filePath = Paths.get(uploadDir + File.separator + file.getOriginalFilename());
            Files.write(filePath, bytes);
            return importScoresFromExcel(filePath.toFile());
        } catch (IOException e) {
            throw new FamsApiException(HttpStatus.BAD_REQUEST, "Import failed!");
        }
    }


    public ByteArrayInputStream generateExcelFromScores(List<Score> scoreList) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Scores");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("score_id");
            headerRow.createCell(1).setCellValue("score");
            headerRow.createCell(2).setCellValue("submission_date");
            headerRow.createCell(3).setCellValue("assignment_id");
            headerRow.createCell(4).setCellValue("student_id");

            int rowNum = 1;
            for (Score score : scoreList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(score.getId());
                row.createCell(1).setCellValue(score.getScore());
                row.createCell(2).setCellValue(score.getSubmissionDate().toString());
                row.createCell(3).setCellValue(score.getAssignment().getId());
                row.createCell(4).setCellValue(score.getStudent().getId());
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);

            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    public ByteArrayInputStream generateTemplateExcel() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Scores");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("score_id");
            headerRow.createCell(1).setCellValue("score");
            headerRow.createCell(2).setCellValue("submission_date");
            headerRow.createCell(3).setCellValue("assignment_id");
            headerRow.createCell(4).setCellValue("student_id");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);

            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}
