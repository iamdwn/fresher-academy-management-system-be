package java03.team01.FAMS.service;

import java.util.List;

public interface StudentClassService {
    boolean updateAttendingStatusOfStudents(List<Long> studentIds, Long classId, String attendingStatus);
}
