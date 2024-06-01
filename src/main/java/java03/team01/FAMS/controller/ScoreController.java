package java03.team01.FAMS.controller;


import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java03.team01.FAMS.model.entity.Score;
import java03.team01.FAMS.model.payload.requestModel.CustomCertificateRequest;
import java03.team01.FAMS.model.payload.requestModel.CustomScoreRequest;
import java03.team01.FAMS.model.payload.responseModel.CustomObjectResponse;
import java03.team01.FAMS.model.payload.responseModel.ImportExcelResponse;
import java03.team01.FAMS.model.payload.responseModel.ScoreClassResponse;
import java03.team01.FAMS.repository.ScoreRepository;
import java03.team01.FAMS.service.ScoreService;
import java03.team01.FAMS.utils.AppConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;


@RestController
@CrossOrigin
@Slf4j
@RequestMapping("/api/v1/score")
public class ScoreController {

    @Autowired
    public ScoreService scoreService;


    @Autowired
    private ScoreRepository scoreRepository;


    @GetMapping("/view/{classId}")
    //@PreAuthorize("hasRole('ROLE_TRAINER')")
    public ResponseEntity<ScoreClassResponse> getScoreOfClass(
            @RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @PathVariable(name = "classId", required = true) Long classId) {
        log.info("Has request with pageNo{}, pageSize{}, classId{}:", pageNo, pageSize, classId);
        ScoreClassResponse scoreClassResponse = scoreService.getScoreOfClass(pageNo, pageSize, classId);
        return ResponseEntity.ok(scoreClassResponse);
    }

    @SecurityRequirement(name = "Bear Authentication")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{scoreId}/{scoreValue}")
    public ResponseEntity<CustomObjectResponse> updateScoreOfClass(
//            @Valid @RequestBody CustomScoreRequest customScoreRequest
            @PathVariable(name = "scoreId") Long scoreId,
            @PathVariable(name = "scoreValue") Float scoreValue) {
        CustomScoreRequest customScoreRequest = new CustomScoreRequest(scoreId, scoreValue);
        CustomObjectResponse customScoreResponses = scoreService.updateScoreOfClass(customScoreRequest);
        return ResponseEntity.ok(customScoreResponses);
    }

    @SecurityRequirement(name = "Bear Authentication")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Hidden
    @PutMapping("/certificate")
    public ResponseEntity<CustomObjectResponse> updateCertificate(@Valid @RequestBody CustomCertificateRequest customCertificateRequest) {
        CustomObjectResponse customScoreResponses = scoreService.updateCertificate(customCertificateRequest);
        return ResponseEntity.ok(customScoreResponses);
    }

    @GetMapping("/{classId}/{rollNumber}")
    public ResponseEntity<CustomObjectResponse> getScoreOfClass(@PathVariable(name = "classId", required = true) Long classId,
                                                                @PathVariable(name = "rollNumber", required = true) String studentCode) {
        CustomObjectResponse customScoreResponses = scoreService.getScoreOfClassAndStudentCode(classId, studentCode);
        return ResponseEntity.ok(customScoreResponses);
    }

    @SecurityRequirement(name = "Bear Authentication")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ImportExcelResponse> importScores(@RequestParam MultipartFile file) throws IOException {
        return ResponseEntity.ok(scoreService.uploadFile(file));
    }

    @SecurityRequirement(name = "Bear Authentication")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/download")
    public ResponseEntity<InputStreamResource> downloadExcel() {
        try {
            List<Score> scoreList = scoreRepository.findAll();
            ByteArrayInputStream excelStream = scoreService.generateExcelFromScores(scoreList);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "scores.xlsx");

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(new InputStreamResource(excelStream));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @SecurityRequirement(name = "Bear Authentication")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Hidden
    @PostMapping("/template/download")
    public ResponseEntity<InputStreamResource> downloadTemplateExcel() {
        try {
            ByteArrayInputStream excelStream = scoreService.generateTemplateExcel();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "scores.xlsx");

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(new InputStreamResource(excelStream));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

