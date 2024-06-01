package java03.team01.FAMS.model.payload.responseModel;

import lombok.*;

import java.time.Duration;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ScoreDetailResponse extends CustomScoreResponse{
    String classCode;
    Duration duration;
    float finalScore;
    String gpaLevel;

    public ScoreDetailResponse(Long id, String fullName, String account, List<CustomModuleResponse> module, String classCode, Duration duration, float finalScore, String gpaLevel) {
        super(id, fullName, account, module);
        this.classCode = classCode;
        this.duration = duration;
        this.finalScore = finalScore;
        this.gpaLevel = gpaLevel;
    }

}
