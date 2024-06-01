package java03.team01.FAMS.controller;

import java03.team01.FAMS.model.payload.dto.ReservedClassDto;
import java03.team01.FAMS.model.payload.responseModel.CustomReservationResponse;
import java03.team01.FAMS.model.payload.responseModel.ImportExcelResponse;
import java03.team01.FAMS.model.payload.responseModel.ReservedStudentResponse;
import java03.team01.FAMS.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
public class ReservationControllerTest {
    @Mock
    private ReservationService reservationService;
    @InjectMocks
    private ReservationController reservationController;

    @Test
    public void testCreateReservation() {
        when(reservationService.createReservation(any(ReservedClassDto.class))).thenReturn(new CustomReservationResponse());

        ReservedClassDto requestDto = new ReservedClassDto();

        ResponseEntity<CustomReservationResponse> responseEntity = reservationController.createReservation(requestDto);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }

    //unit test for getReservationList
    @Test
    public void testGetReservationList() {
        when(reservationService.getReservedList(0, 10, "id", "asc", 1L, "a", "a@gmail.com")).thenReturn(new ReservedStudentResponse());

        ResponseEntity<ReservedStudentResponse> responseEntity = reservationController.getReservationList(0, 10, "id", "asc", 1L, "a", "a@gmail.com");

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    //unit test for dropoutReservation
    @Test
    public void testDropoutReservation() {
        Long classId = 1L;
        Long studentId = 1L;
        doNothing().when(reservationService).dropoutStudent(classId, studentId);

        ResponseEntity<String> response = reservationController.dropoutReservation(classId, studentId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Dropout successfully");
    }

    //unit test for reclassStudent
    @Test
    public void testReclassStudent() {
        Long classId = 1L;
        Long studentId = 1L;
        doNothing().when(reservationService).reclassStudent(classId, studentId);

        ResponseEntity<String> response = reservationController.reclassStudent(classId, studentId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Reclass successfully");
    }

    //unit test for importFile
    @Test
    public void testImportFile() {
        when(reservationService.importDataFromXlsx(any(MultipartFile.class))).thenReturn(new ImportExcelResponse());
        ResponseEntity<ImportExcelResponse> responseEntity = reservationController.importFile(any(MultipartFile.class));
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    //unit test for remindStudentToReClass
    @Test
    public void testRemindStudentToReClass() {
        when(reservationService.remindStudentToReClass()).thenReturn(null);
        ResponseEntity responseEntity = reservationController.remindStudentToReClass();
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    //unit test for getReservationByClassIdAndStudentId
    @Test
    public void testGetReservationByClassIdAndStudentId() {
        when(reservationService.findNewClassForReservedStudent(1L, 1L)).thenReturn(null);
        ResponseEntity responseEntity = reservationController.getReservationByClassIdAndStudentId(1L, 1L);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

}


