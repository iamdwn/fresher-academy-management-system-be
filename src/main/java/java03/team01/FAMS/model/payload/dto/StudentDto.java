package java03.team01.FAMS.model.payload.dto;

import lombok.*;

import java.time.LocalDate;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDto {
    private Long id;
    private String studentCode;
    private String fullName;
    private LocalDate dob;
    private String gender;
    private String phone;
    private String email;
    private String school;
    private String major;
    private LocalDate graduatedDate;
    private Float gpa;
    private String address;
    private String status;
    private String reCer;
    private LocalDate joinedDate;
    private String area;
    private String faAccount;
    private String type;



}
