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
    //    teacher , student
    private String username; // Email address
    //    teacher , student
    private String password; // Password
    //    teacher
    @Column(unique = true)
    private String identifyNumber; // National ID or other identification number

    //    teacher
    @ManyToOne
    @JoinColumn(name = "department_id")
    private DepartmentEntity department;

    //    student
    @ManyToOne
    @JoinColumn(name = "class_id")
    private ClassEntity classes;

    //    teacher , student
    private String khmerFirstName;  // ជាអក្សរខ្មែរ
    //    teacher , student
    private String khmerLastName; // ជាអក្សរខ្មែរ
    //    teacher , student
    private String englishFirstName; // ជាអក្សរឡាតាំង
    //    teacher , student
    private String englishLastName; // ជាអក្សរឡាតាំង

    private String profileUrl; // URL of the image

    //    teacher , student
    @Enumerated(EnumType.STRING)
    private GenderEnum gender; // ភេទ

    //    teacher , student
    private LocalDate dateOfBirth; // ថ្ងៃខែឆ្នាំកំណើត
    //    teacher , student
    private String nationality; // សញ្ជាតិ
    //    teacher , student
    private String ethnicity; // ជនជាតិ
    //    teacher
    private String disability; // ពិការ
    //    teacher
    private String staffId; // អត្តលេខមន្ត្រី
    //    teacher
    private String nationalId; // លេខអត្តសញ្ញាណបណ្ណ
    //    teacher , student
    private String placeOfBirth; // ទីកន្លែងកំណើត
    //    teacher
    private String payrollAccountNumber; // លេខគណនីបៀវត្ស
    //    teacher
    private String cppMembershipNumber; // លេខសមាជិកបសបខ

    //    teacher
    private LocalDate startWorkDate;  // ថ្ងៃខែឆ្នាំចូលបម្រើការងារ
    //    teacher
    private LocalDate currentPositionDate; // ថ្ងៃខែឆ្នាំតែងតាំងស៊ុប
    //    teacher
    private String employeeWork; // អង្គភាពបម្រើការងារ

    // Address Information
    //    teacher
    private String province; // ខេត្ត
    //    teacher
    private String district; // ស្រុក
    //    teacher
    private String commune; // ឃុំ
    //    teacher
    private String village; // ភូមិ
    //    teacher
    private String officeName; // ការិយាល័យ

    //    teacher
    private String currentPosition; // មុខដំណែង
    //    teacher
    private String decreeFinal; // ប្រកាស

    //    teacher
    private String rankAndClass; // ឋាននន្តរស័ក្តិ និងថ្នាក់
    //    teacher
    private String referenceNote; // យោង
    //    teacher
    private String serialNumber; // លេខរៀង
    //    teacher
    private LocalDate lastSalaryIncrementDate; // ថ្ងៃខែឡើងការប្រាក់ចុងក្រោយ
    //    teacher
    private LocalDate issuedDate; // ចុះថ្ងៃទី
    //    teacher
    private String academicYearTaught; // បង្រៀននៅឆ្នាំសិក្សា

    //    teacher
    private String taughtEnglish; // បង្រៀនភាសាអង់គ្លេស
    //    teacher
    private String threeLevelClass; // ថ្នាក់គួបបីកម្រិត
    //    teacher
    private String technicalTeamLeader; // ប្រធានក្រុមបច្ចេកទេស
    //    teacher
    private String assistInTeaching; // ជួយបង្រៀន
    //    teacher
    private String twoLevelClass; // ពីរថ្នាក់ណីរពេល
    //    teacher
    private String classResponsibility; // ទទួលបន្ទុកថ្នាក់
    //    teacher
    private String teachAcrossSchools; // បង្រៀនឆ្លងសាលា
    //    teacher
    private String overtimeHours; // ម៉ោងលើស
    //    teacher
    private String suitableClass; // ថ្នាក់គួប
    //    teacher
    private String bilingual; // ពីរភាសា
    //    teacher

    // Work History
    //    teacher
    private String workHistory; // ស្ថានភាព

    // Family Information
    //    teacher
    private String maritalStatus; // ស្ថានភាពគ្រួសារ
    //    teacher
    private String mustBe; // ត្រូវជា
    //    teacher
    private String affiliatedProfession; // មុខរបរសហព័ទ្ធ
    //    teacher
    private String federationName; // ឈ្មោះសហទ័ព្ធ
    //    teacher
    private String affiliatedOrganization; // អង្គភាពសហព័ទ្ធ
    //    teacher
    private LocalDate federationEstablishmentDate; // ថ្ងៃខែឆ្នាំកំណើតសហព័ទ្ធ
    //    teacher
    private String wivesSalary; // ប្រាក់ខែប្រពន្ធ

    // Contact Information
    //    teacher , student
    private String phoneNumber; // លេខទូរស័ព្ទ
    //    teacher , student
    private String email; // អ៊ីមែល
    //    teacher , student
    private String currentAddress; // អាសយដ្ឋានបច្ចុប្បន្ន

    //    student
    private String memberSiblings; // ចំនួនសមាជិកបងប្អូន
    //    student
    private String numberOfSiblings; // ចំនួនបងប្អូនស្រី

    //    teacher
    // ឋានៈវិជ្ជាជីវៈគ្រូបង្រៀន
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeachersProfessionalRankEntity> teachersProfessionalRank = new ArrayList<>();

    //    teacher
    // ប្រវត្តិការងារបន្តបន្ទាប់
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeacherExperienceEntity> teacherExperience = new ArrayList<>();

    //    teacher
    // ការសរសើរ/ ស្តីបន្ទោស
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeacherPraiseOrCriticismEntity> teacherPraiseOrCriticism = new ArrayList<>();

    //    teacher
    // កម្រិតវប្បធម៌
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeacherEducationEntity> teacherEducation = new ArrayList<>();

    //    teacher
    // វគ្គគរុកោសល្យ
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeacherVocationalEntity> teacherVocational = new ArrayList<>();

    //    teacher
    // វគ្គខ្លីៗ
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeacherShortCourseEntity> teacherShortCourse = new ArrayList<>();

    //    teacher
    // ភាសាបរទេស
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeacherLanguageEntity> teacherLanguage = new ArrayList<>();

    //    teacher
    // ស្ថានភាពគ្រួសារ
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeacherFamilyEntity> teacherFamily = new ArrayList<>();

    //    student
    // ប្រវត្តិការសិក្សា
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentStudiesHistoryEntity> studentStudiesHistory = new ArrayList<>();

    //    student
    // ព័ត៌មានគ្រួសារ
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentParentEntity> studentParent = new ArrayList<>();

    //    student
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