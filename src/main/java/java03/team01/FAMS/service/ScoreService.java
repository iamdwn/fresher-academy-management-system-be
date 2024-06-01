package java03.team01.FAMS.service;

import java03.team01.FAMS.model.entity.Score;
import java03.team01.FAMS.model.payload.requestModel.CustomCertificateRequest;
import java03.team01.FAMS.model.payload.requestModel.CustomScoreRequest;
import java03.team01.FAMS.model.payload.responseModel.CustomObjectResponse;
import java03.team01.FAMS.model.payload.responseModel.ImportExcelResponse;
import java03.team01.FAMS.model.payload.responseModel.ScoreClassResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public interface ScoreService {
    ScoreClassResponse getScoreOfClass(int pageNo, int pageSize, Long idClass);

    CustomObjectResponse getScoreOfClassAndStudentCode(Long idClass, String studentCode);

    CustomObjectResponse updateScoreOfClass(CustomScoreRequest customScoreRequest);

    CustomObjectResponse updateCertificate(CustomCertificateRequest customCertificateRequest);

    ImportExcelResponse importScoresFromExcel(File file) throws IOException, IOException;

    ImportExcelResponse uploadFile(MultipartFile file) throws IOException;

    ByteArrayInputStream generateExcelFromScores(List<Score> scoreList) throws IOException;
    ByteArrayInputStream generateTemplateExcel() throws IOException;
}
