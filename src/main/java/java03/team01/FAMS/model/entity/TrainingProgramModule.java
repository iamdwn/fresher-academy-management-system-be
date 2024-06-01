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
@Table(name = "training_program_modules")
public class TrainingProgramModule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="program_module_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "program_id", referencedColumnName = "program_id")
    private TrainingProgram trainingProgram;

    @ManyToOne
    @JoinColumn(name = "module_id", referencedColumnName = "module_id")
    private Module module;
}
