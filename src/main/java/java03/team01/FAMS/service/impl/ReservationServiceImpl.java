package java03.team01.FAMS.service.impl;

import java03.team01.FAMS.model.entity.Class;
import java03.team01.FAMS.model.entity.Module;
import java03.team01.FAMS.model.entity.*;
import java03.team01.FAMS.model.exception.FamsApiException;
import java03.team01.FAMS.model.exception.ResourceNotFoundException;
import java03.team01.FAMS.model.payload.dto.EmailSendDto;
import java03.team01.FAMS.model.payload.dto.EmailSendStudentDto;
import java03.team01.FAMS.model.payload.dto.ReservedClassDto;
import java03.team01.FAMS.model.payload.responseModel.*;
import java03.team01.FAMS.repository.*;
import java03.team01.FAMS.service.EmailService;
import java03.team01.FAMS.service.ReservationService;
import java03.team01.FAMS.utils.Utils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReservationServiceImpl implements ReservationService {
    private TrainingProgramModuleRepository trainingProgramModuleRepository;
    private StudentModuleRepository studentModuleRepository;
    private StudentClassRepository studentClassRepository;
    private ReservationRepository reservationRepository;
    private StudentRepository studentRepository;
    private ModuleRepository moduleRepository;
    private ClassRepository classRepository;
    private EmailTemplateRepository emailTemplateRepository;
    private EmailService emailService;
    private ModelMapper modelMapper;

    @Autowired
    public ReservationServiceImpl(TrainingProgramModuleRepository trainingProgramModuleRepository,
                                  TrainingProgramRepository trainingProgramRepository,
                                  StudentModuleRepository studentModuleRepository,
                                  StudentClassRepository studentClassRepository,
                                  ReservationRepository reservationRepository,
                                  StudentRepository studentRepository,
                                  ModuleRepository moduleRepository,
                                  ClassRepository classRepository,
                                  ModelMapper modelMapper,
                                  EmailTemplateRepository emailTemplateRepository,
                                  EmailService emailService
    ) {
        this.reservationRepository = reservationRepository;
        this.studentModuleRepository = studentModuleRepository;
        this.moduleRepository = moduleRepository;
        this.studentRepository = studentRepository;
        this.classRepository = classRepository;
        this.modelMapper = modelMapper;
        this.studentClassRepository = studentClassRepository;
        this.trainingProgramModuleRepository = trainingProgramModuleRepository;
        this.emailTemplateRepository = emailTemplateRepository;
        this.emailService = emailService;
    }

    @Override
    public CustomReservationResponse createReservation(ReservedClassDto reservedClassDto) {
        Student student = studentRepository.findById(reservedClassDto.getStudentId()).orElseThrow(() -> new ResourceNotFoundException("Student", "id", reservedClassDto.getStudentId()));
        CustomStudentResponse customStudentResponse = modelMapper.map(student, CustomStudentResponse.class);

        Class classObj = classRepository.findById(reservedClassDto.getClassId()).orElseThrow(() -> new ResourceNotFoundException("Class", "id", reservedClassDto.getClassId()));
        CustomClassResponse customClassResponse = modelMapper.map(classObj, CustomClassResponse.class);

        // Check if the class is already reserved
        if (reservationRepository.existsByStudentIdAndAndClassObjIdAndStatus(reservedClassDto.getStudentId(), reservedClassDto.getClassId(), "active"))
            throw new FamsApiException(HttpStatus.BAD_REQUEST, "This student and class is already reserved");

        // ensure the period is no longer than 6 months
        if (reservedClassDto.getStartDate().plusMonths(6).isBefore(reservedClassDto.getEndDate()))
            throw new FamsApiException(HttpStatus.BAD_REQUEST, "The period is no longer than 6 months");

        // ensure start date is after today
//        if (reservedClassDto.getStartDate().isBefore(LocalDate.now()))
//            throw new FamsApiException(HttpStatus.BAD_REQUEST, "Start date must be after today");

        // ensure end date is after start date at least 1 month
        if (reservedClassDto.getEndDate().isBefore(reservedClassDto.getStartDate().plusMonths(1)))
            throw new FamsApiException(HttpStatus.BAD_REQUEST, "End date must be after start date at least 1 month");

        // check if the student is attending the class, change attending status to "Reserve"
        StudentClass studentClass = studentClassRepository.findByStudentIdAndClassObjId(reservedClassDto.getStudentId(), reservedClassDto.getClassId());
        if (studentClass == null)
            throw new FamsApiException(HttpStatus.BAD_REQUEST, "This student is not attending this class");
        if (!studentClass.getAttendingStatus().equals("In class") && !studentClass.getAttendingStatus().equals("Back To Class"))
            throw new FamsApiException(HttpStatus.BAD_REQUEST, "This student is not attending this class anymore");
        studentClass.setAttendingStatus("Reserved");
        studentClassRepository.save(studentClass);

        // save reserved class to database
        ReservedClass reservedClass = new ReservedClass();
        reservedClass.setStudent(student);
        reservedClass.setClassObj(classObj);
        reservedClass.setReason(reservedClassDto.getReason());
        reservedClass.setStartDate(reservedClassDto.getStartDate());
        reservedClass.setEndDate(reservedClassDto.getEndDate());
        reservedClass.setStatus("Active");
        reservationRepository.save(reservedClass);

        // create custom response
        CustomReservationResponse customReservationResponse = new CustomReservationResponse();
        customReservationResponse.setStudent(customStudentResponse);
        customReservationResponse.setClassObj(customClassResponse);
        customReservationResponse.setReason(reservedClassDto.getReason());
        customReservationResponse.setStartDate(reservedClassDto.getStartDate());
        customReservationResponse.setEndDate(reservedClassDto.getEndDate());

        // find training program of the class
        TrainingProgram trainingProgram = classObj.getTrainingProgram();
        List<TrainingProgramModule> trainingProgramModules = trainingProgramModuleRepository.findByTrainingProgramId(trainingProgram.getId());
        List<Module> moduleList = trainingProgramModules.stream().map(trainingProgramModule -> trainingProgramModule.getModule()).collect(Collectors.toList());
        List<String> moduleNameList = moduleList.stream().map(module -> module.getModuleName()).collect(Collectors.toList());
        customReservationResponse.setModuleName(moduleNameList);

        sendMailToStudent(student, classObj, "hành trình học tập tiếp theo", null);
        return customReservationResponse;
    }

    private void sendMailToStudent(Student student, Class classObj, String description, List<Long> studentIdList) {
        //send email to inform student about successful reservation
        EmailTemplate emailTemplate = emailTemplateRepository.findByDescriptionContainingIgnoreCase(description);
        EmailSendDto emailSendDto = new EmailSendDto();
        emailSendDto.setTemplateId(emailTemplate.getId());
        emailSendDto.setSenderId(1L);
        emailSendDto.setReceiverType("student");
        if (description.contains("hành trình học tập tiếp theo")) {
            emailSendDto.setContent("Chúc mừng sinh viên " + student.getFullName().toUpperCase() + " đã bảo lưu lớp " + classObj.getClassName() + " thành công!");
            EmailSendStudentDto emailSendStudentDto = new EmailSendStudentDto();
            emailSendStudentDto.setStudentId(student.getId());
            emailSendDto.setEmailSendStudents(Set.of(emailSendStudentDto));
        } else {
            emailSendDto.setContent("Còn 30 ngày nữa sẽ hết hạn bảo lưu, nhắc nhở sinh viên chuẩn bị quay trở lại học!");
            //create a set of email send student dto
            Set<EmailSendStudentDto> emailSendStudentDtoSet = studentIdList.stream().map(id -> {
                EmailSendStudentDto emailSendStudentDto = new EmailSendStudentDto();
                emailSendStudentDto.setStudentId(id);
                return emailSendStudentDto;
            }).collect(Collectors.toSet());
            emailSendDto.setEmailSendStudents(emailSendStudentDtoSet);
        }
        emailService.sendEmail(emailSendDto);
    }

    @Override
    public ReservedStudentResponse getReservedList(int pageNo, int pageSize, String sortBy, String sortDir, Long id, String fullName, String email) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Page<ReservedClass> reservedClass = reservationRepository.findByIdorFullNameorEmail(id, fullName, email, pageable);
        List<ReservedClass> reservedClassList = reservedClass.getContent();
        List<CustomReservationResponse> content = reservedClassList.stream().map(reservedClass1 -> modelMapper.map(reservedClass1, CustomReservationResponse.class)).collect(Collectors.toList());

        //Set ModuleName
        for (int i = 0; i < content.size(); i++) {
            List<String> moduleList = new ArrayList<>();
            List<Long> moduleIds;
            Long trainingProgramId = reservedClassList.get(i).getClassObj().getTrainingProgram().getId();
            //Query List of Module Id from Training Program Id
            moduleIds = trainingProgramModuleRepository.findModulesByTraningProgramId(trainingProgramId);
            for (Long mIds : moduleIds) {
                Module module = moduleRepository.findById(mIds).orElseThrow(() -> new FamsApiException(HttpStatus.NOT_FOUND, "Unknown module"));
                moduleList.add(module.getModuleName());
                content.get(i).setModuleName(moduleList);
            }
        }

        ReservedStudentResponse reservedStudentResponse = new ReservedStudentResponse();
        reservedStudentResponse.setContent(content);
        reservedStudentResponse.setPageNo(reservedClass.getNumber());
        reservedStudentResponse.setPageSize(reservedClass.getSize());
        reservedStudentResponse.setTotalElements(reservedClass.getTotalElements());
        reservedStudentResponse.setTotalPages(reservedClass.getTotalPages());
        reservedStudentResponse.setLast(reservedClass.isLast());
        return reservedStudentResponse;
    }

    @Override
    public void reclassStudent(Long classId, Long studentId) {
        String status = "Drop Out";
        LocalDate currentDate = LocalDate.now();

        Student student = studentRepository.findById(
                studentId).orElseThrow(() -> new FamsApiException(HttpStatus.BAD_REQUEST, "This student doesn't exist"));

        Class classes = classRepository.findById(
                classId).orElseThrow(() -> new FamsApiException(HttpStatus.BAD_REQUEST, "This class doesn't exist")
        );

        if (classes.getStatus().equalsIgnoreCase("inactive")){
            throw new FamsApiException(HttpStatus.BAD_REQUEST, "This class doesn't exist");
        }

        LocalDate endDate = classes.getEndDate();

        if (endDate.isBefore(currentDate)) {
            throw new FamsApiException(HttpStatus.BAD_REQUEST, "Can't re-class due to the class is finished");
        }

        StudentClass sc = studentClassRepository.findByStudentAndClassId(
                student.getId(),
                classes.getId(),
                status).orElseThrow(() -> new FamsApiException(HttpStatus.BAD_REQUEST, "This student didn't reserve any class")
        );

        ReservedClass rc = reservationRepository.findBystudentIdAndClassId(
                student.getId(),classes.getId()).orElseThrow(() -> new FamsApiException(HttpStatus.BAD_REQUEST, "This student isn't in reservation list ")
        );

        rc.setStatus("Inactive");
        sc.setAttendingStatus("Back To Class");

        studentClassRepository.save(sc);
        reservationRepository.save(rc);

    }

    @Override
    public void dropoutStudent(Long classId, Long studentId) {
        String status = "In class";

        Student student = studentRepository.findById(
                studentId).orElseThrow(() -> new FamsApiException(HttpStatus.BAD_REQUEST, "This student doesn't exist"));

        Class classes = classRepository.findById(
                classId).orElseThrow(() -> new FamsApiException(HttpStatus.BAD_REQUEST, "This class doesn't exist")
        );

        if (classes.getStatus().equalsIgnoreCase("inactive")){
            throw new FamsApiException(HttpStatus.BAD_REQUEST, "This class doesn't exist");
        }

        StudentClass sc = studentClassRepository.findByStudentAndClassId(
                student.getId(),
                classes.getId(),
                status).orElseThrow(() -> new FamsApiException(HttpStatus.BAD_REQUEST, "This student didnt participate in this class")
        );

        ReservedClass rc = reservationRepository.findBystudentIdAndClassId(
                student.getId(),classes.getId()).orElseThrow(() -> new FamsApiException(HttpStatus.BAD_REQUEST, "This student isn't in reserved list yet")
        );

        sc.setAttendingStatus("Drop out");
        rc.setStatus("Inactive");

        studentClassRepository.save(sc);
        reservationRepository.save(rc);
    }

    public List<String> remindStudentToReClass() {
        List<Long> studentIdList = new ArrayList<>();
        List<ReservedClass> needRemindList = new ArrayList<>();
        List<ReservedClass> reservedClassList = reservationRepository.findByStatus("active");
        for (ReservedClass reservedClass : reservedClassList) {
            if (reservedClass.getEndDate().isEqual(LocalDate.now().plusDays(30))
                    && reservedClass.getStudent().getStatus().equals("Keep Class")) {
                needRemindList.add(reservedClass);
                studentIdList.add(reservedClass.getStudent().getId());
            }
        }

        //send email to remind students
        sendMailToStudent(null, null, "Chúc bạn một ngày tốt lành nhé bạn", studentIdList);

        List<String> emailList = needRemindList.stream()
                .map(reservedClass -> reservedClass.getStudent().getEmail())
                .collect(Collectors.toList());
        return emailList;
    }

    @Override
    public List<CustomClassResponse> findNewClassForReservedStudent(Long studentId, Long classId) {
        // check if the student and class exists
        Student student = studentRepository.findById(studentId).orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));
        Class classObj = classRepository.findById(classId).orElseThrow(() -> new ResourceNotFoundException("Class", "id", classId));

        // check if the student belongs to class
        StudentClass studentClass = studentClassRepository.findByStudentIdAndClassObjIdAndAttendingStatus(studentId, classId, "Reserved");
        if (studentClass == null)
            throw new FamsApiException(HttpStatus.BAD_REQUEST, "This student is not attending or reserving this class");

        // check if the student is reserving the class
        ReservedClass reservedClass = reservationRepository.findByStudentIdAndAndClassObjIdAndStatus(studentId, classId, "active");
        if (reservedClass == null)
            throw new FamsApiException(HttpStatus.BAD_REQUEST, "This student is not reserving this class");

        // find current modules belong to class of that student
        TrainingProgram trainingProgram = classObj.getTrainingProgram();
        List<TrainingProgramModule> currentModules = trainingProgramModuleRepository.findByTrainingProgramId(trainingProgram.getId());
        List<Module> currentModuleList = currentModules.stream().map(trainingProgramModule -> trainingProgramModule.getModule()).collect(Collectors.toList());

        // find new class that has the same modules
        List<Class> matchedClassList = new ArrayList<>();
        List<Class> allClass = classRepository.findAll();
        allClass.remove(classObj);
        allClass.forEach(curClass -> {
            List<TrainingProgramModule> trainingProgramModules = trainingProgramModuleRepository.findByTrainingProgramId(curClass.getTrainingProgram().getId());
            List<Module> moduleList = trainingProgramModules.stream().map(trainingProgramModule -> trainingProgramModule.getModule()).collect(Collectors.toList());

            // check if the class has the same modules and the start date is after the end date of reserved class
            if (moduleList.containsAll(currentModuleList) && curClass.getStartDate().isAfter(reservedClass.getEndDate())) {
                matchedClassList.add(curClass);
            }
        });

        if (matchedClassList.isEmpty())
            throw new FamsApiException(HttpStatus.BAD_REQUEST, "There is no class that has the same modules yet");

        return matchedClassList.stream().map(curClass -> modelMapper.map(curClass, CustomClassResponse.class)).collect(Collectors.toList());
    }

    @Override
    public ImportExcelResponse importDataFromXlsx(MultipartFile file) {
        ImportExcelResponse importExcelResponse = new ImportExcelResponse();
        List<String> successList = new ArrayList<>();
        List<String> failList = new ArrayList<>();

        try {
            if (!Utils.getFileType(file.getOriginalFilename()).equalsIgnoreCase("xlsx")) {
                throw new FamsApiException(HttpStatus.BAD_REQUEST, "Only .xlsx file is supported!");
            }
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() != 0) {
                    try {
                        ReservedClass reservedClass = new ReservedClass();
                        //Check if Type of DateTime is valid
                        if (!Utils.DateTimeFormatCheck(row.getCell(1).getStringCellValue())
                                || !Utils.DateTimeFormatCheck(row.getCell(2).getStringCellValue())) {
                            throw new FamsApiException(HttpStatus.BAD_REQUEST,"At Row " + row.getRowNum() +  ". The date time format is invalid, the format must be (yyyy-MM-dd)");
                        }
                        reservedClass.setEndDate(LocalDate.parse(row.getCell(1).getStringCellValue()));
                        reservedClass.setStartDate(LocalDate.parse(row.getCell(2).getStringCellValue()));
                        // ensure the period is no longer than 6 months
                        if (reservedClass.getStartDate().plusMonths(6).isBefore(reservedClass.getEndDate()))
                            throw new FamsApiException(HttpStatus.BAD_REQUEST,"At Row " + row.getRowNum() +  ". The period is no longer than 6 months");

                        Class classObj = classRepository.findById(Math.round(row.getCell(3).getNumericCellValue()))
                                .orElseThrow(() -> new ResourceNotFoundException("At Row " + row.getRowNum() + ". Class", "id", Math.round(row.getCell(3).getNumericCellValue())));


                        Student student = studentRepository.findById(Math.round(row.getCell(4).getNumericCellValue()))
                                .orElseThrow(() -> new ResourceNotFoundException("At Row " + row.getRowNum() + ". Student", "id", Math.round(row.getCell(4).getNumericCellValue())));

                        // check if the student belongs to class
                        StudentClass studentClass = studentClassRepository.findByStudentIdAndClassObjIdAndAttendingStatus(student.getId(), classObj.getId(), "Reserved");
                        if (studentClass != null)
                            throw new FamsApiException(HttpStatus.BAD_REQUEST,"At Row " + row.getRowNum() +  ". The student: " + student.getStudentCode() + " is already reserved this class");

                        //Check if the student are in the class
                        studentClass = studentClassRepository.findByStudentIdAndClassObjIdAndAttendingStatus(student.getId(), classObj.getId(), "In class");
                        if (studentClass == null)
                            throw new FamsApiException(HttpStatus.BAD_REQUEST,"At Row " + row.getRowNum() +  ". The student: " + student.getStudentCode() + " is not attending this class");

                        // check if the student is reserving the class
                        ReservedClass rc = reservationRepository.findByStudentIdAndAndClassObjIdAndStatus(student.getId(), classObj.getId(), "active");
                        if (rc != null)
                            throw new FamsApiException(HttpStatus.BAD_REQUEST,"At Row " + row.getRowNum() +  ". The student: " + student.getStudentCode() + " is already reserved this class");

                        studentClass.setAttendingStatus("Reserved");
                        studentClassRepository.save(studentClass);

                        reservedClass.setClassObj(classObj);
                        reservedClass.setStudent(student);
                        reservedClass.setReason(row.getCell(5).getStringCellValue());
                        reservedClass.setStatus(row.getCell(6).getStringCellValue());
                        reservationRepository.save(reservedClass);
                        successList.add("Student: " + student.getStudentCode() + " reservation add successfully to class: " + classObj.getId());
                    } catch (FamsApiException e) {
                        failList.add( e.getMessage());
                    } catch (ResourceNotFoundException re){
                        failList.add(re.getMessage());
                    }
                }
            }

            workbook.close();
        } catch (IOException e) {
            throw new FamsApiException(HttpStatus.BAD_REQUEST, "Import failed!");
        }

        importExcelResponse.setSuccessList(successList);
        importExcelResponse.setFailList(failList);
        System.out.println("a");
        return importExcelResponse;
    }


}
