package java03.team01.FAMS.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "student_classes")
public class StudentClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="student_class_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", referencedColumnName = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "class_id", referencedColumnName = "class_id")
    private Class classObj;

    @Column(nullable = false)
    private String attendingStatus;

    @Column(nullable = false)
    private String result;

    @Column(nullable = false)
    private Float finalScore;

    @Column(nullable = false)
    private String gpaLevel;

    @Column(nullable = false)
    private String certificationStatus;

    @Column(nullable = false)
    private LocalDate certificationDate;

    @Column(nullable = false)
    private String method;
}
