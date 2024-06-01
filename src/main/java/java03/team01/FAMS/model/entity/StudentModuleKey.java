package java03.team01.FAMS.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class StudentModuleKey implements Serializable {
    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "module_id")
    private Long moduleId;
}
