package java03.team01.FAMS.service;

import java03.team01.FAMS.model.entity.ReservedClass;
import java03.team01.FAMS.model.payload.dto.ReservedClassDto;
import java03.team01.FAMS.model.payload.responseModel.CustomClassResponse;
import java03.team01.FAMS.model.payload.responseModel.CustomReservationResponse;
import java03.team01.FAMS.model.payload.responseModel.ImportExcelResponse;
import java03.team01.FAMS.model.payload.responseModel.ReservedStudentResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@Service
public interface ReservationService {
    CustomReservationResponse createReservation(ReservedClassDto reservedClassDto);

    ReservedStudentResponse getReservedList(int pageNo, int pageSize, String sortBy, String sortDir, Long id, String fullName, String email);

    void reclassStudent(Long classId, Long studentId);

    void dropoutStudent(Long classId, Long studentId);

    ImportExcelResponse importDataFromXlsx(MultipartFile file);

    List<String> remindStudentToReClass();

    List<CustomClassResponse> findNewClassForReservedStudent(Long studentId, Long classId);

}
