package java03.team01.FAMS.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java03.team01.FAMS.model.entity.ReservedClass;
import java03.team01.FAMS.model.exception.FamsApiException;
import java03.team01.FAMS.model.payload.dto.ReservedClassDto;
import java03.team01.FAMS.model.payload.responseModel.CustomClassResponse;
import java03.team01.FAMS.model.payload.responseModel.CustomReservationResponse;
import java03.team01.FAMS.model.payload.responseModel.ImportExcelResponse;
import java03.team01.FAMS.model.payload.responseModel.ReservedStudentResponse;
import java03.team01.FAMS.service.ReservationService;
import java03.team01.FAMS.utils.AppConstants;
import java03.team01.FAMS.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {


    @Autowired
    private ReservationService reservationService;

    @Autowired
    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @SecurityRequirement(name = "Bear Authentication")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<CustomReservationResponse> createReservation(@Valid @RequestBody ReservedClassDto reservedClassDto) {
        CustomReservationResponse savedReservedClassDto = reservationService.createReservation(reservedClassDto);
        return new ResponseEntity<>(savedReservedClassDto, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ReservedStudentResponse> getReservationList(
            @RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(name = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir,
            @RequestParam(name = "id", required = false) Long id,
            @RequestParam(name = "fullName", defaultValue = "", required = false) String fullName,
            @RequestParam(name = "email", defaultValue = "", required = false) String email
    ) {
        return ResponseEntity.ok(reservationService.getReservedList(pageNo, pageSize, sortBy, sortDir, id, fullName, email));
    }

    @SecurityRequirement(name = "Bear Authentication")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/dropout/{classId}/{studentId}")
    public ResponseEntity<String> dropoutReservation(@PathVariable(name = "classId") Long classId,
                                                     @PathVariable(name = "studentId") Long studentId) {
        reservationService.dropoutStudent(classId,studentId);
        return ResponseEntity.ok("Dropout successfully");
    }

    @SecurityRequirement(name = "Bear Authentication")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/reclass/{classId}/{studentId}")
    public ResponseEntity<String> reclassStudent(@PathVariable(name = "classId") Long classId,
                                                 @PathVariable(name = "studentId") Long studentId) {
        reservationService.reclassStudent(classId,studentId);
        return ResponseEntity.ok("Reclass successfully");
    }

    @SecurityRequirement(name = "Bear Authentication")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ImportExcelResponse> importFile(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(reservationService.importDataFromXlsx(file));
    }

    @GetMapping("/students")
    public ResponseEntity<List<String>> remindStudentToReClass() {
        return ResponseEntity.ok(reservationService.remindStudentToReClass());
    }

    @GetMapping("classes/{classId}/students/{studentId}")
    public ResponseEntity<List<CustomClassResponse>> getReservationByClassIdAndStudentId(@PathVariable(name = "classId") Long classId,
                                                                                         @PathVariable(name = "studentId") Long studentId) {
        return ResponseEntity.ok(reservationService.findNewClassForReservedStudent(studentId, classId));
    }

}
