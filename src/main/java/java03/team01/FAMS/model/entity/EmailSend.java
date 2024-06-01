package java03.team01.FAMS.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "email_sends")
public class EmailSend {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="email_send_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "template_id", referencedColumnName = "template_id")
    private EmailTemplate emailTemplate;

    @ManyToOne
    @JoinColumn(name = "sender_id", referencedColumnName = "user_id")
    private User sender;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDate sendDate;

    @Column(nullable = false)
    private String receiverType;

    @OneToMany(mappedBy = "emailSend", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EmailSendStudent> emailSendStudents;

    @OneToMany(mappedBy = "emailSend", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EmailSendUser> emailSendUsers;
}
