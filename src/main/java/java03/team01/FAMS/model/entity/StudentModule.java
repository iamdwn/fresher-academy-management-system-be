package java03.team01.FAMS.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "student_modules")
public class StudentModule {
    @EmbeddedId
    private StudentModuleKey id;

    @ManyToOne
    @MapsId("studentId")
    @JoinColumn(name = "student_id")
    Student student;

    @ManyToOne
    @MapsId("moduleId")
    @JoinColumn(name = "module_id")
    Module module;

    @Column(nullable = false)
    private Float moduleScore;

    @Column(nullable = false)
    private Float moduleLevel;

    @Column(nullable = false)
    private String status;

//    public StudentModule(StudentModuleKey id, Student student, Module module, Float moduleScore, Float moduleLevel, String status) {
//        this.id = id;
//        this.student = student;
//        this.module = module;
//        this.moduleScore = moduleScore;
//        this.moduleLevel = moduleLevel;
//        this.status = status;
//    }
}
