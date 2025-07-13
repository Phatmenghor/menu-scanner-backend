// Updated UserEntity.java with explicit column mappings to fix naming issues
package com.menghor.ksit.feature.auth.models;

import com.menghor.ksit.enumations.GenderEnum;
import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.school.model.CourseEntity;
import com.menghor.ksit.feature.master.model.ClassEntity;
import com.menghor.ksit.feature.master.model.DepartmentEntity;
import com.menghor.ksit.feature.school.model.ScheduleEntity;
import com.menghor.ksit.utils.database.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class UserEntity extends BaseEntity {

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    private List<Role> roles = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE; // Default status is ACTIVE

    @Column(unique = true)
    private String username; // Email address

    private String password; // Password

    @Column(unique = true, name = "identify_number")
    private String identifyNumber; // National ID or other identification number

    @ManyToOne
    @JoinColumn(name = "department_id")
    private DepartmentEntity department;

    @ManyToOne
    @JoinColumn(name = "class_id")
    private ClassEntity classes;

    @Column(name = "khmer_first_name")
    private String khmerFirstName;  // ជាអក្សរខ្មែរ

    @Column(name = "khmer_last_name")
    private String khmerLastName; // ជាអក្សរខ្មែរ

    @Column(name = "english_first_name")
    private String englishFirstName; // ជាអក្សរឡាតាំង

    @Column(name = "english_last_name")
    private String englishLastName; // ជាអក្សរឡាតាំង

    @Column(name = "profile_url")
    private String profileUrl; // URL of the image

    @Enumerated(EnumType.STRING)
    private GenderEnum gender; // ភេទ

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth; // ថ្ងៃខែឆ្នាំកំណើត

    private String nationality; // សញ្ជាតិ
    private String ethnicity; // ជនជាតិ
    private String disability; // ពិការ

    @Column(name = "staff_id")
    private String staffId; // អត្តលេខមន្ត្រី

    @Column(name = "national_id")
    private String nationalId; // លេខអត្តសញ្ញាណបណ្ណ

    @Column(name = "place_of_birth")
    private String placeOfBirth; // ទីកន្លែងកំណើត

    @Column(name = "payroll_account_number")
    private String payrollAccountNumber; // លេខគណនីបៀវត្ស

    @Column(name = "cpp_membership_number")
    private String cppMembershipNumber; // លេខសមាជិកបសបខ

    @Column(name = "start_work_date")
    private LocalDate startWorkDate;  // ថ្ងៃខែឆ្នាំចូលបម្រើការងារ

    @Column(name = "current_position_date")
    private LocalDate currentPositionDate; // ថ្ងៃខែឆ្នាំតែងតាំងស៊ុប

    @Column(name = "employee_work")
    private String employeeWork; // អង្គភាពបម្រើការងារ

    // Address Information
    private String province; // ខេត្ត
    private String district; // ស្រុក
    private String commune; // ឃុំ
    private String village; // ភូមិ

    @Column(name = "office_name")
    private String officeName; // ការិយាល័យ

    @Column(name = "current_position")
    private String currentPosition; // មុខដំណែង

    @Column(name = "decree_final")
    private String decreeFinal; // ប្រកាស

    @Column(name = "rank_and_class")
    private String rankAndClass; // ឋាននន្តរស័ក្តិ និងថ្នាក់

    @Column(name = "reference_note")
    private String referenceNote; // យោង

    @Column(name = "serial_number")
    private String serialNumber; // លេខរៀង

    @Column(name = "last_salary_increment_date")
    private LocalDate lastSalaryIncrementDate; // ថ្ងៃខែឡើងការប្រាក់ចុងក្រោយ

    @Column(name = "issued_date")
    private LocalDate issuedDate; // ចុះថ្ងៃទី

    @Column(name = "academic_year_taught")
    private String academicYearTaught; // បង្រៀននៅឆ្នាំសិក្សា

    @Column(name = "taught_english")
    private String taughtEnglish; // បង្រៀនភាសាអង់គ្លេស

    @Column(name = "three_level_class")
    private String threeLevelClass; // ថ្នាក់គួបបីកម្រិត

    @Column(name = "technical_team_leader")
    private String technicalTeamLeader; // ប្រធានក្រុមបច្ចេកទេស

    @Column(name = "assist_in_teaching")
    private String assistInTeaching; // ជួយបង្រៀន

    @Column(name = "two_level_class")
    private String twoLevelClass; // ពីរថ្នាក់ណីរពេល

    @Column(name = "class_responsibility")
    private String classResponsibility; // ទទួលបន្ទុកថ្នាក់

    @Column(name = "teach_across_schools")
    private String teachAcrossSchools; // បង្រៀនឆ្លងសាលា

    @Column(name = "overtime_hours")
    private String overtimeHours; // ម៉ោងលើស

    @Column(name = "suitable_class")
    private String suitableClass; // ថ្នាក់គួប

    private String bilingual; // ពីរភាសា

    // Work History
    @Column(name = "work_history")
    private String workHistory; // ស្ថានភាព

    // Family Information
    @Column(name = "marital_status")
    private String maritalStatus; // ស្ថានភាពគ្រួសារ

    @Column(name = "must_be")
    private String mustBe; // ត្រូវជា

    @Column(name = "affiliated_profession")
    private String affiliatedProfession; // មុខរបរសហព័ទ្ធ

    @Column(name = "federation_name")
    private String federationName; // ឈ្មោះសហទ័ព្ធ

    @Column(name = "affiliated_organization")
    private String affiliatedOrganization; // អង្គភាពសហព័ទ្ធ

    @Column(name = "federation_establishment_date")
    private LocalDate federationEstablishmentDate; // ថ្ងៃខែឆ្នាំកំណើតសហព័ទ្ធ

    @Column(name = "wives_salary")
    private String wivesSalary; // ប្រាក់ខែប្រពន្ធ

    // Contact Information
    @Column(name = "phone_number")
    private String phoneNumber; // លេខទូរស័ព្ទ

    private String email; // អ៊ីមែល

    @Column(name = "current_address")
    private String currentAddress; // អាសយដ្ឋានបច្ចុប្បន្ន

    @Column(name = "member_siblings")
    private String memberSiblings; // ចំនួនសមាជិកបងប្អូន

    @Column(name = "number_of_siblings")
    private String numberOfSiblings; // ចំនួនបងប្អូនស្រី

    // ឋានៈវិជ្ជាជីវៈគ្រូបង្រៀន
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeachersProfessionalRankEntity> teachersProfessionalRank = new ArrayList<>();

    // ប្រវត្តិការងារបន្តបន្ទាប់
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeacherExperienceEntity> teacherExperience = new ArrayList<>();

    // ការសរសើរ/ ស្តីបន្ទោស
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeacherPraiseOrCriticismEntity> teacherPraiseOrCriticism = new ArrayList<>();

    // កម្រិតវប្បធម៌
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeacherEducationEntity> teacherEducation = new ArrayList<>();

    // វគ្គគរុកោសល្យ
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeacherVocationalEntity> teacherVocational = new ArrayList<>();

    // វគ្គខ្លីៗ
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeacherShortCourseEntity> teacherShortCourse = new ArrayList<>();

    // ភាសាបរទេស
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeacherLanguageEntity> teacherLanguage = new ArrayList<>();

    // ស្ថានភាពគ្រួសារ
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeacherFamilyEntity> teacherFamily = new ArrayList<>();

    // ប្រវត្តិការសិក្សា
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentStudiesHistoryEntity> studentStudiesHistory = new ArrayList<>();

    // ព័ត៌មានគ្រួសារ
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentParentEntity> studentParent = new ArrayList<>();

    // សមាជិកគ្រួសារ
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentSiblingEntity> studentSibling = new ArrayList<>();

    // course : teacher name
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseEntity> courses = new ArrayList<>();

    // schedule : teacher
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScheduleEntity> schedule = new ArrayList<>();

    // payment
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentEntity> payment = new ArrayList<>();

    // payment student
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PaymentEntity> payments;

    /**
     * Check if user has a specific role
     */
    public boolean hasRole(RoleEnum roleEnum) {
        return roles.stream()
                .anyMatch(role -> role.getName() == roleEnum);
    }

    /**
     * Check if user is a student
     */
    public boolean isStudent() {
        return hasRole(RoleEnum.STUDENT);
    }

    /**
     * Check if user is a teacher staff admin developer
     */
    public boolean isOther() {
        return hasRole(RoleEnum.TEACHER) || hasRole(RoleEnum.STAFF) || hasRole(RoleEnum.ADMIN) || hasRole(RoleEnum.DEVELOPER);
    }
}