package com.menghor.ksit.config;

import com.menghor.ksit.enumations.QuestionTypeEnum;
import com.menghor.ksit.enumations.StatusSurvey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service to handle database migration and data cleanup
 * Fixes issues with existing NULL values and column naming inconsistencies
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Order(4) // Run after menu initialization
public class DataMigrationService {

    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void performDataMigration() {
        log.info("Starting comprehensive data migration...");

        try {
            // Fix user table column names first
            migrateUserTableColumns();

            // Then migrate survey data
            migrateSurveyData();

            log.info("Data migration completed successfully");
        } catch (Exception e) {
            log.error("Error during data migration: {}", e.getMessage(), e);
            // Don't throw exception to prevent application startup failure
        }
    }

    private void migrateUserTableColumns() {
        try {
            log.info("Migrating user table columns...");

            // Check if users table exists
            String checkTableSql = """
                SELECT COUNT(*) FROM information_schema.tables 
                WHERE table_name = 'users'
                """;

            Integer tableCount = jdbcTemplate.queryForObject(checkTableSql, Integer.class);
            if (tableCount == 0) {
                log.info("users table doesn't exist yet, skipping user migration");
                return;
            }

            // List of column mappings to check and fix
            String[][] columnMappings = {
                    {"academicyeartaught", "academic_year_taught"},
                    {"taughtEnglish", "taught_english"},
                    {"threeLevelClass", "three_level_class"},
                    {"technicalTeamLeader", "technical_team_leader"},
                    {"assistInTeaching", "assist_in_teaching"},
                    {"twoLevelClass", "two_level_class"},
                    {"classResponsibility", "class_responsibility"},
                    {"teachAcrossSchools", "teach_across_schools"},
                    {"overtimeHours", "overtime_hours"},
                    {"suitableClass", "suitable_class"},
                    {"workHistory", "work_history"},
                    {"maritalStatus", "marital_status"},
                    {"mustBe", "must_be"},
                    {"affiliatedProfession", "affiliated_profession"},
                    {"federationName", "federation_name"},
                    {"affiliatedOrganization", "affiliated_organization"},
                    {"federationEstablishmentDate", "federation_establishment_date"},
                    {"wivesSalary", "wives_salary"},
                    {"phoneNumber", "phone_number"},
                    {"currentAddress", "current_address"},
                    {"memberSiblings", "member_siblings"},
                    {"numberOfSiblings", "number_of_siblings"}
            };

            // Check and rename columns if needed
            for (String[] mapping : columnMappings) {
                String oldColumn = mapping[0];
                String newColumn = mapping[1];

                if (columnExists("users", oldColumn) && !columnExists("users", newColumn)) {
                    log.info("Renaming column {} to {} in users table", oldColumn, newColumn);
                    try {
                        String renameSql = String.format(
                                "ALTER TABLE users RENAME COLUMN \"%s\" TO \"%s\"",
                                oldColumn, newColumn
                        );
                        jdbcTemplate.execute(renameSql);
                    } catch (Exception e) {
                        log.warn("Could not rename column {} to {}: {}", oldColumn, newColumn, e.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            log.warn("Could not migrate user table columns: {}", e.getMessage());
        }
    }

    private void migrateSurveyData() {
        try {
            // Migrate survey sections
            migrateSurveySections();

            // Migrate survey questions
            migrateSurveyQuestions();

        } catch (Exception e) {
            log.error("Error during survey data migration: {}", e.getMessage(), e);
        }
    }

    private void migrateSurveySections() {
        try {
            log.info("Migrating survey sections...");

            if (!tableExists("survey_sections")) {
                log.info("survey_sections table doesn't exist yet, skipping migration");
                return;
            }

            // Update NULL displayOrder values with proper snake_case column name
            if (columnExists("survey_sections", "display_order")) {
                String updateDisplayOrderSql = """
                    UPDATE survey_sections 
                    SET display_order = COALESCE(display_order, 1) 
                    WHERE display_order IS NULL
                    """;

                int updatedRows = jdbcTemplate.update(updateDisplayOrderSql);
                log.info("Updated {} survey sections with NULL display_order", updatedRows);
            } else if (columnExists("survey_sections", "displayOrder")) {
                // Handle camelCase column name
                String updateDisplayOrderSql = """
                    UPDATE survey_sections 
                    SET "displayOrder" = COALESCE("displayOrder", 1) 
                    WHERE "displayOrder" IS NULL
                    """;

                int updatedRows = jdbcTemplate.update(updateDisplayOrderSql);
                log.info("Updated {} survey sections with NULL displayOrder", updatedRows);
            }

        } catch (Exception e) {
            log.warn("Could not migrate survey sections: {}", e.getMessage());
        }
    }

    private void migrateSurveyQuestions() {
        try {
            log.info("Migrating survey questions...");

            if (!tableExists("survey_questions")) {
                log.info("survey_questions table doesn't exist yet, skipping migration");
                return;
            }

            // Update NULL questionType values
            if (columnExists("survey_questions", "question_type")) {
                String updateQuestionTypeSql = """
                    UPDATE survey_questions 
                    SET question_type = ? 
                    WHERE question_type IS NULL
                    """;

                int updatedQuestionType = jdbcTemplate.update(updateQuestionTypeSql, QuestionTypeEnum.TEXT.name());
                log.info("Updated {} survey questions with NULL question_type", updatedQuestionType);
            } else if (columnExists("survey_questions", "questionType")) {
                String updateQuestionTypeSql = """
                    UPDATE survey_questions 
                    SET "questionType" = ? 
                    WHERE "questionType" IS NULL
                    """;

                int updatedQuestionType = jdbcTemplate.update(updateQuestionTypeSql, QuestionTypeEnum.TEXT.name());
                log.info("Updated {} survey questions with NULL questionType", updatedQuestionType);
            }

            // Update NULL displayOrder values
            if (columnExists("survey_questions", "display_order")) {
                String updateDisplayOrderSql = """
                    UPDATE survey_questions 
                    SET display_order = COALESCE(display_order, 1) 
                    WHERE display_order IS NULL
                    """;

                int updatedDisplayOrder = jdbcTemplate.update(updateDisplayOrderSql);
                log.info("Updated {} survey questions with NULL display_order", updatedDisplayOrder);
            } else if (columnExists("survey_questions", "displayOrder")) {
                String updateDisplayOrderSql = """
                    UPDATE survey_questions 
                    SET "displayOrder" = COALESCE("displayOrder", 1) 
                    WHERE "displayOrder" IS NULL
                    """;

                int updatedDisplayOrder = jdbcTemplate.update(updateDisplayOrderSql);
                log.info("Updated {} survey questions with NULL displayOrder", updatedDisplayOrder);
            }

            // Update NULL required values
            String updateRequiredSql = """
                UPDATE survey_questions 
                SET required = COALESCE(required, false) 
                WHERE required IS NULL
                """;

            int updatedRequired = jdbcTemplate.update(updateRequiredSql);
            log.info("Updated {} survey questions with NULL required", updatedRequired);

            // Update NULL status values
            String updateStatusSql = """
                UPDATE survey_questions 
                SET status = ? 
                WHERE status IS NULL
                """;

            int updatedStatus = jdbcTemplate.update(updateStatusSql, StatusSurvey.ACTIVE.name());
            log.info("Updated {} survey questions with NULL status", updatedStatus);

        } catch (Exception e) {
            log.warn("Could not migrate survey questions: {}", e.getMessage());
        }
    }

    private boolean tableExists(String tableName) {
        try {
            String sql = """
                SELECT COUNT(*) FROM information_schema.tables 
                WHERE table_name = ?
                """;
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tableName);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean columnExists(String tableName, String columnName) {
        try {
            String sql = """
                SELECT COUNT(*) FROM information_schema.columns 
                WHERE table_name = ? AND column_name = ?
                """;
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tableName, columnName);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }
}