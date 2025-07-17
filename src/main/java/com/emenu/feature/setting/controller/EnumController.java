package com.emenu.feature.setting.controller;

import com.emenu.enumations.GenderEnum;
import com.emenu.enumations.RoleEnum;
import com.emenu.enumations.Status;
import com.menghor.ksit.enumations.*;
import com.emenu.exceptoins.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/api/v1/enums")
public class EnumController {

    @GetMapping("/attendance-final-status")
    public ApiResponse<List<Map<String, Object>>> getAllAttendanceFinalStatus() {
        List<Map<String, Object>> days = Arrays.stream(AttendanceFinalizationStatus.values())
                .map(day -> {
                    Map<String, Object> dayMap = new HashMap<>();
                    dayMap.put("id", day.ordinal() + 1);
                    dayMap.put("value", day.name());
                    dayMap.put("label", day.name().charAt(0) + day.name().substring(1).toLowerCase());
                    return dayMap;
                })
                .collect(Collectors.toList());

        return ApiResponse.success("Get all attendance finalization status enum values successfully...!", days);
    }

    @GetMapping("/attendance-status")
    public ApiResponse<List<Map<String, Object>>> getAllAttendanceStatus() {
        List<Map<String, Object>> statuses = Arrays.stream(AttendanceStatus.values())
                .map(status -> {
                    Map<String, Object> statusMap = new HashMap<>();
                    statusMap.put("id", status.ordinal() + 1);
                    statusMap.put("value", status.name());
                    statusMap.put("label", status.name().charAt(0) + status.name().substring(1).toLowerCase());
                    return statusMap;
                })
                .collect(Collectors.toList());

        return ApiResponse.success("Get all attendance status enum values successfully...!", statuses);
    }

    @GetMapping("/attendance-type")
    public ApiResponse<List<Map<String, Object>>> getAllAttendanceType() {
        List<Map<String, Object>> types = Arrays.stream(AttendanceType.values())
                .map(type -> {
                    Map<String, Object> typeMap = new HashMap<>();
                    typeMap.put("id", type.ordinal() + 1);
                    typeMap.put("value", type.name());
                    typeMap.put("label", type.name().charAt(0) + type.name().substring(1).toLowerCase());
                    return typeMap;
                })
                .collect(Collectors.toList());

        return ApiResponse.success("Get all attendance type enum values successfully...!", types);
    }

    @GetMapping("/days-of-week")
    public ApiResponse<List<Map<String, Object>>> getAllDaysOfWeek() {
        List<Map<String, Object>> days = Arrays.stream(DayOfWeek.values())
                .map(day -> {
                    Map<String, Object> dayMap = new HashMap<>();
                    dayMap.put("id", day.ordinal() + 1);
                    dayMap.put("value", day.name());
                    dayMap.put("label", day.name().charAt(0) + day.name().substring(1).toLowerCase());
                    return dayMap;
                })
                .collect(Collectors.toList());

        return ApiResponse.success("Get all enum day of week successfully...!", days);
    }

    @GetMapping("/degree")
    public ApiResponse<List<Map<String, Object>>> getAllDegree() {
        List<Map<String, Object>> degrees = Arrays.stream(DegreeEnum.values())
                .map(degree -> {
                    Map<String, Object> degreeMap = new HashMap<>();
                    degreeMap.put("id", degree.ordinal() + 1);
                    degreeMap.put("value", degree.name());
                    degreeMap.put("label", degree.name().charAt(0) + degree.name().substring(1).toLowerCase());
                    return degreeMap;
                })
                .collect(Collectors.toList());

        return ApiResponse.success("Get all degree enum values successfully...!", degrees);
    }

    @GetMapping("/education-level")
    public ApiResponse<List<Map<String, Object>>> getAllEducationLevel() {
        List<Map<String, Object>> levels = Arrays.stream(EducationLevelEnum.values())
                .map(level -> {
                    Map<String, Object> levelMap = new HashMap<>();
                    levelMap.put("id", level.ordinal() + 1);
                    levelMap.put("value", level.name());

                    String displayName = Arrays.stream(level.name().split("_"))
                            .map(word -> word.charAt(0) + word.substring(1).toLowerCase())
                            .collect(Collectors.joining(" "));

                    levelMap.put("label", displayName);
                    return levelMap;
                })
                .collect(Collectors.toList());

        return ApiResponse.success("Get all education level successfully...!", levels);
    }

    @GetMapping("/gender")
    public ApiResponse<List<Map<String, Object>>> getAllGender() {
        List<Map<String, Object>> genders = Arrays.stream(GenderEnum.values())
                .map(gender -> {
                    Map<String, Object> genderMap = new HashMap<>();
                    genderMap.put("id", gender.ordinal() + 1);
                    genderMap.put("value", gender.name());
                    genderMap.put("label", gender.name().charAt(0) + gender.name().substring(1).toLowerCase());
                    return genderMap;
                })
                .collect(Collectors.toList());

        return ApiResponse.success("Get all gender enum values successfully...!", genders);
    }

    @GetMapping("/grade")
    public ApiResponse<List<Map<String, Object>>> getAllGrade() {
        List<Map<String, Object>> grades = Arrays.stream(GradeLevel.values())
                .map(grade -> {
                    Map<String, Object> gradeMap = new HashMap<>();
                    gradeMap.put("id", grade.ordinal() + 1);
                    gradeMap.put("value", grade.name());
                    gradeMap.put("label", grade.name().charAt(0) + grade.name().substring(1).toLowerCase());
                    return gradeMap;
                })
                .collect(Collectors.toList());

        return ApiResponse.success("Get all grade enum values successfully...!", grades);
    }

    @GetMapping("/parent")
    public ApiResponse<List<Map<String, Object>>> getAllParent() {
        List<Map<String, Object>> parents = Arrays.stream(ParentEnum.values())
                .map(parent -> {
                    Map<String, Object> parentMap = new HashMap<>();
                    parentMap.put("id", parent.ordinal() + 1);
                    parentMap.put("value", parent.name());
                    parentMap.put("label", parent.name().charAt(0) + parent.name().substring(1).toLowerCase());
                    return parentMap;
                })
                .collect(Collectors.toList());

        return ApiResponse.success("Get all parent enum values successfully...!", parents);
    }

    @GetMapping("/position-type")
    public ApiResponse<List<Map<String, Object>>> getAllPositionType() {
        List<Map<String, Object>> positions = Arrays.stream(PositionType.values())
                .map(position -> {
                    Map<String, Object> positionMap = new HashMap<>();
                    positionMap.put("id", position.ordinal() + 1);
                    positionMap.put("value", position.name());

                    String displayName = Arrays.stream(position.name().split("_"))
                            .map(word -> word.charAt(0) + word.substring(1).toLowerCase())
                            .collect(Collectors.joining(" "));

                    positionMap.put("label", displayName);
                    return positionMap;
                })
                .collect(Collectors.toList());

        return ApiResponse.success("Get all position type successfully...!", positions);
    }

    @GetMapping("/role")
    public ApiResponse<List<Map<String, Object>>> getAllRole() {
        List<Map<String, Object>> roles = Arrays.stream(RoleEnum.values())
                .map(role -> {
                    Map<String, Object> roleMap = new HashMap<>();
                    roleMap.put("id", role.ordinal() + 1);
                    roleMap.put("value", role.name());
                    roleMap.put("label", role.name().charAt(0) + role.name().substring(1).toLowerCase());
                    return roleMap;
                })
                .collect(Collectors.toList());

        return ApiResponse.success("Get all role enum values successfully...!", roles);
    }

    @GetMapping("/status-request")
    public ApiResponse<List<Map<String, Object>>> getAllStatusRequest() {
        List<Map<String, Object>> statuses = Arrays.stream(RequestStatus.values())
                .map(status -> {
                    Map<String, Object> statusMap = new HashMap<>();
                    statusMap.put("id", status.ordinal() + 1);
                    statusMap.put("value", status.name());
                    statusMap.put("label", status.name().charAt(0) + status.name().substring(1).toLowerCase());
                    return statusMap;
                })
                .collect(Collectors.toList());

        return ApiResponse.success("Get all request status values successfully...!", statuses);
    }

    @GetMapping("/semester")
    public ApiResponse<List<Map<String, Object>>> getAllSemester() {
        List<Map<String, Object>> semesters = Arrays.stream(SemesterEnum.values())
                .map(semester -> {
                    Map<String, Object> semesterMap = new HashMap<>();
                    semesterMap.put("id", semester.ordinal() + 1);
                    semesterMap.put("value", semester.name());

                    String displayName = Arrays.stream(semester.name().split("_"))
                            .map(word -> word.charAt(0) + word.substring(1).toLowerCase())
                            .collect(Collectors.joining(" "));

                    semesterMap.put("label", displayName);
                    return semesterMap;
                })
                .collect(Collectors.toList());

        return ApiResponse.success("Get all semester successfully...!", semesters);
    }

    @GetMapping("/semester-type")
    public ApiResponse<List<Map<String, Object>>> getAllSemesterType() {
        List<Map<String, Object>> semesterTypes = Arrays.stream(SemesterType.values())
                .map(type -> {
                    Map<String, Object> typeMap = new HashMap<>();
                    typeMap.put("id", type.ordinal() + 1);
                    typeMap.put("value", type.name());
                    typeMap.put("label", type.name().charAt(0) + type.name().substring(1).toLowerCase());
                    return typeMap;
                })
                .collect(Collectors.toList());

        return ApiResponse.success("Get all semester type enum values successfully...!", semesterTypes);
    }

    @GetMapping("/status")
    public ApiResponse<List<Map<String, Object>>> getAllStatus() {
        List<Map<String, Object>> statuses = Arrays.stream(Status.values())
                .map(status -> {
                    Map<String, Object> statusMap = new HashMap<>();
                    statusMap.put("id", status.ordinal() + 1);
                    statusMap.put("value", status.name());
                    statusMap.put("label", status.name().charAt(0) + status.name().substring(1).toLowerCase());
                    return statusMap;
                })
                .collect(Collectors.toList());

        return ApiResponse.success("Get all status enum values successfully...!", statuses);
    }

    @GetMapping("/student-type-payment")
    public ApiResponse<List<Map<String, Object>>> getAllStudentType() {
        List<Map<String, Object>> studentTypes = Arrays.stream(StudentTypePayment.values())
                .map(type -> {
                    Map<String, Object> typeMap = new HashMap<>();
                    typeMap.put("id", type.ordinal() + 1);
                    typeMap.put("value", type.name());

                    String displayName = Arrays.stream(type.name().split("_"))
                            .map(word -> word.charAt(0) + word.substring(1).toLowerCase())
                            .collect(Collectors.joining(" "));

                    typeMap.put("label", displayName);
                    return typeMap;
                })
                .collect(Collectors.toList());

        return ApiResponse.success("Get all student type payment successfully...!", studentTypes);
    }

    @GetMapping("/submission-status")
    public ApiResponse<List<Map<String, Object>>> getAllSubmissionStatus() {
        List<Map<String, Object>> statuses = Arrays.stream(SubmissionStatus.values())
                .map(status -> {
                    Map<String, Object> statusMap = new HashMap<>();
                    statusMap.put("id", status.ordinal() + 1);
                    statusMap.put("value", status.name());

                    String displayName = Arrays.stream(status.name().split("_"))
                            .map(word -> word.charAt(0) + word.substring(1).toLowerCase())
                            .collect(Collectors.joining(" "));

                    statusMap.put("label", displayName);
                    return statusMap;
                })
                .collect(Collectors.toList());

        return ApiResponse.success("Get all submission status successfully...!", statuses);
    }

    @GetMapping("/year-level")
    public ApiResponse<List<Map<String, Object>>> getAllYearLevel() {
        List<Map<String, Object>> yearLevels = Arrays.stream(YearLevelEnum.values())
                .map(year -> {
                    Map<String, Object> yearMap = new HashMap<>();
                    yearMap.put("id", year.ordinal() + 1);
                    yearMap.put("value", year.name());

                    String displayName = Arrays.stream(year.name().split("_"))
                            .map(word -> word.charAt(0) + word.substring(1).toLowerCase())
                            .collect(Collectors.joining(" "));

                    yearMap.put("label", displayName);
                    return yearMap;
                })
                .collect(Collectors.toList());

        return ApiResponse.success("Get all year level successfully...!", yearLevels);
    }
}