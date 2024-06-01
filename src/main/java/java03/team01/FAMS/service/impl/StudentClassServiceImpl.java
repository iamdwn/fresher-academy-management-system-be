package java03.team01.FAMS.service.impl;

import java03.team01.FAMS.model.entity.StudentClass;
import java03.team01.FAMS.repository.StudentClassRepository;
import java03.team01.FAMS.service.StudentClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentClassServiceImpl implements StudentClassService {

    @Autowired
    StudentClassRepository studentClassRepository;

    @Override
    public boolean updateAttendingStatusOfStudents(List<Long> studentIds, Long classId, String attendingStatus) {
        for (Long studentId : studentIds) {
            StudentClass studentClass = studentClassRepository.getStudentClassByStudentIdAndClassId(studentId, classId);
            if (studentClass != null) {
                studentClass.setAttendingStatus(attendingStatus);
                studentClassRepository.save(studentClass);
            } else {
                return false;
            }
        }
        return true;
    }
}
