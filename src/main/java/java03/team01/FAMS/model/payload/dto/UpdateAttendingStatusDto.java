package java03.team01.FAMS.model.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAttendingStatusDto {
    List<Long> studentIds;
    Long classId;
    String newStatus;
}
