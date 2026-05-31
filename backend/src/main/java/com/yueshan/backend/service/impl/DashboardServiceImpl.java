package com.yueshan.backend.service.impl;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.yueshan.backend.dto.DashboardSummaryResponse;
import com.yueshan.backend.dto.DashboardSummaryResponse.ActivityItem;
import com.yueshan.backend.dto.DashboardSummaryResponse.Metrics;
import com.yueshan.backend.dto.DashboardSummaryResponse.RecentSurgeryItem;
import com.yueshan.backend.dto.DashboardSummaryResponse.TodoItem;
import com.yueshan.backend.service.DashboardService;

@Service
public class DashboardServiceImpl implements DashboardService {
    private final JdbcTemplate jdbcTemplate;

    public DashboardServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public DashboardSummaryResponse summary() {
        Metrics metrics = new Metrics(
                count("""
                        SELECT COUNT(DISTINCT patient_id)
                        FROM inpatient_admission
                        WHERE status = 'IN_HOSPITAL'
                        """),
                count("""
                        SELECT COUNT(*)
                        FROM inpatient_admission
                        WHERE DATE(admission_time) = CURRENT_DATE
                          AND status <> 'CANCELLED'
                        """),
                count("""
                        SELECT COUNT(*)
                        FROM inpatient_admission
                        WHERE status = 'DISCHARGED'
                          AND discharge_time IS NOT NULL
                          AND DATE(discharge_time) = CURRENT_DATE
                        """),
                count("""
                        SELECT COUNT(*)
                        FROM medical_record
                        WHERE deleted = false
                          AND status IN ('DRAFT', 'SUBMITTED')
                        """),
                countByStatus("medical_record", "DRAFT"),
                countByStatus("medical_record", "SUBMITTED"),
                countByStatus("medical_record", "ARCHIVED"),
                countByStatus("surgery_operation", "APPLIED"),
                countByStatus("surgery_operation", "SCHEDULED"),
                countByStatus("surgery_operation", "IN_PROGRESS"),
                countByStatus("surgery_operation", "COMPLETED"),
                countByStatus("surgery_operation", "COMPLETED"),
                count("""
                        SELECT COUNT(*)
                        FROM surgery_operation
                        WHERE deleted = false
                          AND DATE(COALESCE(planned_time, actual_start_time, actual_end_time, created_at)) = CURRENT_DATE
                        """),
                money("""
                        SELECT COALESCE(SUM(total_amount), 0)
                        FROM inpatient_fee
                        WHERE deleted = false
                          AND status <> 'CANCELLED'
                          AND (source_type = 'SURGERY' OR item_category = 'SURGERY')
                          AND DATE(fee_time) = CURRENT_DATE
                        """),
                money("""
                        SELECT COALESCE(SUM(f.total_amount), 0)
                        FROM inpatient_fee f
                        JOIN inpatient_admission a ON a.id = f.admission_id
                        WHERE f.deleted = false
                          AND f.status <> 'CANCELLED'
                          AND a.status = 'IN_HOSPITAL'
                        """),
                money("""
                        SELECT COALESCE(SUM(total_amount), 0)
                        FROM inpatient_fee
                        WHERE deleted = false
                          AND status = 'UNSETTLED'
                        """),
                money("""
                        SELECT COALESCE(SUM(total_amount), 0)
                        FROM inpatient_fee
                        WHERE deleted = false
                          AND status = 'SETTLED'
                        """),
                count("""
                        SELECT COUNT(*)
                        FROM medical_order
                        WHERE deleted = false
                          AND status IN ('SUBMITTED', 'CHECKED')
                        """),
                count("""
                        SELECT COUNT(*)
                        FROM medical_order
                        WHERE deleted = false
                          AND status IN ('EXECUTED', 'BILLED')
                          AND executed_time IS NOT NULL
                          AND DATE(executed_time) = CURRENT_DATE
                        """),
                count("""
                        SELECT COUNT(*)
                        FROM nursing_record
                        WHERE deleted = false
                          AND DATE(record_time) = CURRENT_DATE
                        """),
                count("""
                        SELECT COUNT(*)
                        FROM nursing_record
                        WHERE deleted = false
                          AND nursing_type = 'VITAL_SIGN'
                          AND (
                            temperature >= 37.30
                            OR pulse >= 100
                            OR respiration >= 24
                          )
                        """),
                count("""
                        SELECT COUNT(*)
                        FROM medical_order
                        WHERE deleted = false
                          AND status = 'EXECUTED'
                          AND billed = false
                        """),
                count("""
                        SELECT COUNT(*)
                        FROM nursing_record
                        WHERE deleted = false
                          AND status IN ('RECORDED', 'EXECUTED', 'REVIEWED')
                          AND billable = true
                          AND billed = false
                        """),
                count("""
                        SELECT COUNT(*)
                        FROM exam_lab_request
                        WHERE deleted = false
                          AND status IN ('REQUESTED', 'SUBMITTED')
                        """),
                count("""
                        SELECT COUNT(*)
                        FROM exam_lab_request
                        WHERE deleted = false
                          AND status = 'COMPLETED'
                        """),
                count("""
                        SELECT COUNT(*)
                        FROM exam_lab_request
                        WHERE deleted = false
                          AND abnormal_flag = true
                        """),
                count("""
                        SELECT COUNT(*)
                        FROM exam_lab_request
                        WHERE deleted = false
                          AND status IN ('COMPLETED', 'REPORTED')
                          AND billed = false
                        """),
                count("""
                        SELECT COUNT(*)
                        FROM inpatient_discharge
                        WHERE deleted = false
                          AND status IN ('DRAFT', 'PENDING_SETTLEMENT')
                        """),
                count("""
                        SELECT COUNT(*)
                        FROM patient_appointment
                        WHERE deleted = false
                          AND appointment_date = CURRENT_DATE
                        """),
                countByStatus("patient_appointment", "REQUESTED"),
                countByStatus("patient_appointment", "COMPLETED"),
                countByStatus("patient_appointment", "CANCELLED"));

        return new DashboardSummaryResponse(metrics, todos(), recentSurgeries(), todayActivities());
    }

    private List<TodoItem> todos() {
        List<TodoItem> items = new ArrayList<>();
        items.addAll(recordTodos("待审核病历", "SUBMITTED", "/app/medical-records?status=SUBMITTED", 5));
        items.addAll(recordTodos("草稿病历", "DRAFT", "/app/medical-records?status=DRAFT", 5));
        items.addAll(surgeryTodos("待排班手术", "APPLIED", "/app/surgeries?status=APPLIED", 5));
        items.addAll(surgeryTodos("手术待计费", "COMPLETED", "/app/surgeries?status=COMPLETED", 5));
        items.addAll(orderTodos("待执行医嘱", "SUBMITTED", "/app/orders?status=SUBMITTED", 5));
        items.addAll(orderTodos("医嘱待计费", "EXECUTED", "/app/orders?status=EXECUTED", 5));
        items.addAll(nursingTodos(5));
        items.addAll(appointmentTodos("待确认患者预约", "REQUESTED", "/app/patient-appointments?status=REQUESTED", 5));
        items.addAll(todayAppointmentTodos(5));
        items.addAll(unsettledFeeTodos(5));
        items.addAll(inHospitalTodos(5));
        return items.stream()
                .sorted((left, right) -> nullSafeTime(right.time()).compareTo(nullSafeTime(left.time())))
                .limit(30)
                .toList();
    }

    private List<TodoItem> recordTodos(String type, String status, String targetPath, int limit) {
        return jdbcTemplate.query("""
                SELECT mr.title, p.name AS patient_name, a.admission_no, mr.admission_id,
                       mr.status, mr.record_time AS event_time
                FROM medical_record mr
                LEFT JOIN patients p ON p.id = mr.patient_id
                LEFT JOIN inpatient_admission a ON a.id = mr.admission_id
                WHERE mr.deleted = false
                  AND mr.status = ?
                ORDER BY mr.record_time DESC, mr.id DESC
                LIMIT ?
                """, (rs, rowNum) -> new TodoItem(
                type,
                text(rs, "title"),
                text(rs, "patient_name"),
                text(rs, "admission_no"),
                rs.getLong("admission_id"),
                text(rs, "status"),
                time(rs, "event_time"),
                targetPath), status, limit);
    }

    private List<TodoItem> surgeryTodos(String type, String status, String targetPath, int limit) {
        return jdbcTemplate.query("""
                SELECT s.surgery_name AS title, p.name AS patient_name, a.admission_no, s.admission_id,
                       s.status, COALESCE(s.planned_time, s.actual_start_time, s.created_at) AS event_time
                FROM surgery_operation s
                LEFT JOIN patients p ON p.id = s.patient_id
                LEFT JOIN inpatient_admission a ON a.id = s.admission_id
                WHERE s.deleted = false
                  AND s.status = ?
                ORDER BY COALESCE(s.planned_time, s.actual_start_time, s.created_at) DESC, s.id DESC
                LIMIT ?
                """, (rs, rowNum) -> new TodoItem(
                type,
                text(rs, "title"),
                text(rs, "patient_name"),
                text(rs, "admission_no"),
                rs.getLong("admission_id"),
                text(rs, "status"),
                time(rs, "event_time"),
                targetPath), status, limit);
    }

    private List<TodoItem> orderTodos(String type, String status, String targetPath, int limit) {
        return jdbcTemplate.query("""
                SELECT o.item_name AS title, p.name AS patient_name, a.admission_no, o.admission_id,
                       o.status, COALESCE(o.executed_time, o.start_time, o.created_at) AS event_time
                FROM medical_order o
                LEFT JOIN patients p ON p.id = o.patient_id
                LEFT JOIN inpatient_admission a ON a.id = o.admission_id
                WHERE o.deleted = false
                  AND o.status = ?
                  AND (? <> 'EXECUTED' OR o.billed = false)
                ORDER BY COALESCE(o.executed_time, o.start_time, o.created_at) DESC, o.id DESC
                LIMIT ?
                """, (rs, rowNum) -> new TodoItem(
                type,
                text(rs, "title"),
                text(rs, "patient_name"),
                text(rs, "admission_no"),
                rs.getLong("admission_id"),
                text(rs, "status"),
                time(rs, "event_time"),
                targetPath), status, status, limit);
    }

    private List<TodoItem> nursingTodos(int limit) {
        return jdbcTemplate.query("""
                SELECT nr.nursing_content AS title, p.name AS patient_name, a.admission_no, nr.admission_id,
                       nr.status, nr.record_time AS event_time
                FROM nursing_record nr
                LEFT JOIN patients p ON p.id = nr.patient_id
                LEFT JOIN inpatient_admission a ON a.id = nr.admission_id
                WHERE nr.deleted = false
                  AND (
                    (nr.billable = true AND nr.billed = false AND nr.status <> 'CANCELLED')
                    OR (nr.nursing_type = 'VITAL_SIGN' AND (nr.temperature >= 37.30 OR nr.pulse >= 100 OR nr.respiration >= 24))
                  )
                ORDER BY nr.record_time DESC, nr.id DESC
                LIMIT ?
                """, (rs, rowNum) -> new TodoItem(
                "护理待办",
                text(rs, "title"),
                text(rs, "patient_name"),
                text(rs, "admission_no"),
                rs.getLong("admission_id"),
                text(rs, "status"),
                time(rs, "event_time"),
                "/app/nursing-records"), limit);
    }

    private List<TodoItem> appointmentTodos(String type, String status, String targetPath, int limit) {
        return jdbcTemplate.query("""
                SELECT pa.appointment_item AS title, p.name AS patient_name, a.admission_no, pa.admission_id,
                       pa.status, pa.created_at AS event_time
                FROM patient_appointment pa
                LEFT JOIN patients p ON p.id = pa.patient_id
                LEFT JOIN inpatient_admission a ON a.id = pa.admission_id
                WHERE pa.deleted = false
                  AND pa.status = ?
                ORDER BY pa.created_at DESC, pa.id DESC
                LIMIT ?
                """, (rs, rowNum) -> new TodoItem(
                type,
                text(rs, "title"),
                text(rs, "patient_name"),
                text(rs, "admission_no"),
                rs.getLong("admission_id"),
                text(rs, "status"),
                time(rs, "event_time"),
                targetPath), status, limit);
    }

    private List<TodoItem> todayAppointmentTodos(int limit) {
        return jdbcTemplate.query("""
                SELECT pa.appointment_item AS title, p.name AS patient_name, a.admission_no, pa.admission_id,
                       pa.status, pa.appointment_date::timestamp AS event_time
                FROM patient_appointment pa
                LEFT JOIN patients p ON p.id = pa.patient_id
                LEFT JOIN inpatient_admission a ON a.id = pa.admission_id
                WHERE pa.deleted = false
                  AND pa.appointment_date = CURRENT_DATE
                ORDER BY pa.id DESC
                LIMIT ?
                """, (rs, rowNum) -> new TodoItem(
                "今日预约服务",
                text(rs, "title"),
                text(rs, "patient_name"),
                text(rs, "admission_no"),
                rs.getLong("admission_id"),
                text(rs, "status"),
                time(rs, "event_time"),
                "/app/patient-appointments"), limit);
    }

    private List<TodoItem> unsettledFeeTodos(int limit) {
        return jdbcTemplate.query("""
                SELECT f.item_name AS title, p.name AS patient_name, a.admission_no, f.admission_id,
                       f.status, f.fee_time AS event_time
                FROM inpatient_fee f
                LEFT JOIN patients p ON p.id = f.patient_id
                LEFT JOIN inpatient_admission a ON a.id = f.admission_id
                WHERE f.deleted = false
                  AND f.status = 'UNSETTLED'
                ORDER BY f.fee_time DESC, f.id DESC
                LIMIT ?
                """, (rs, rowNum) -> new TodoItem(
                "未结算费用",
                text(rs, "title"),
                text(rs, "patient_name"),
                text(rs, "admission_no"),
                rs.getLong("admission_id"),
                text(rs, "status"),
                time(rs, "event_time"),
                "/app/charging-station"), limit);
    }

    private List<TodoItem> inHospitalTodos(int limit) {
        return jdbcTemplate.query("""
                SELECT a.admission_no AS title, p.name AS patient_name, a.admission_no, a.id AS admission_id,
                       a.status, a.admission_time AS event_time
                FROM inpatient_admission a
                LEFT JOIN patients p ON p.id = a.patient_id
                WHERE a.status = 'IN_HOSPITAL'
                ORDER BY a.admission_time DESC, a.id DESC
                LIMIT ?
                """, (rs, rowNum) -> new TodoItem(
                "当前在院患者",
                text(rs, "title"),
                text(rs, "patient_name"),
                text(rs, "admission_no"),
                rs.getLong("admission_id"),
                text(rs, "status"),
                time(rs, "event_time"),
                "/app/doctor-station"), limit);
    }

    private List<RecentSurgeryItem> recentSurgeries() {
        return jdbcTemplate.query("""
                SELECT s.id, p.name AS patient_name, s.surgery_name, s.status, s.planned_time,
                       s.primary_doctor_name, s.surgery_fee,
                       CASE WHEN s.status = 'BILLED'
                             OR EXISTS (
                                SELECT 1 FROM inpatient_fee f
                                WHERE f.deleted = false
                                  AND f.source_type = 'SURGERY'
                                  AND f.source_id = s.id
                             )
                            THEN true ELSE false END AS billed
                FROM surgery_operation s
                LEFT JOIN patients p ON p.id = s.patient_id
                WHERE s.deleted = false
                ORDER BY COALESCE(s.planned_time, s.actual_start_time, s.actual_end_time, s.created_at) DESC, s.id DESC
                LIMIT 8
                """, (rs, rowNum) -> new RecentSurgeryItem(
                rs.getLong("id"),
                text(rs, "patient_name"),
                text(rs, "surgery_name"),
                text(rs, "status"),
                time(rs, "planned_time"),
                text(rs, "primary_doctor_name"),
                scale(rs.getBigDecimal("surgery_fee")),
                rs.getBoolean("billed"),
                "/app/surgeries?status=" + text(rs, "status")));
    }

    private List<ActivityItem> todayActivities() {
        List<ActivityItem> activities = new ArrayList<>();
        activities.addAll(jdbcTemplate.query("""
                SELECT '今日入院' AS type, a.admission_no AS title, p.name AS patient_name,
                       a.status, a.admission_time AS event_time, '/app/admissions' AS target_path
                FROM inpatient_admission a
                LEFT JOIN patients p ON p.id = a.patient_id
                WHERE DATE(a.admission_time) = CURRENT_DATE
                ORDER BY a.admission_time DESC
                LIMIT 6
                """, this::activity));
        activities.addAll(jdbcTemplate.query("""
                SELECT '今日手术' AS type, s.surgery_name AS title, p.name AS patient_name,
                       s.status, COALESCE(s.planned_time, s.actual_start_time, s.actual_end_time, s.created_at) AS event_time,
                       '/app/surgeries' AS target_path
                FROM surgery_operation s
                LEFT JOIN patients p ON p.id = s.patient_id
                WHERE s.deleted = false
                  AND DATE(COALESCE(s.planned_time, s.actual_start_time, s.actual_end_time, s.created_at)) = CURRENT_DATE
                ORDER BY event_time DESC
                LIMIT 6
                """, this::activity));
        activities.addAll(jdbcTemplate.query("""
                SELECT '今日费用' AS type, f.item_name AS title, p.name AS patient_name,
                       f.status, f.fee_time AS event_time, '/app/charging-station' AS target_path
                FROM inpatient_fee f
                LEFT JOIN patients p ON p.id = f.patient_id
                WHERE f.deleted = false
                  AND f.status <> 'CANCELLED'
                  AND DATE(f.fee_time) = CURRENT_DATE
                ORDER BY f.fee_time DESC
                LIMIT 6
                """, this::activity));
        return activities.stream()
                .sorted((left, right) -> nullSafeTime(right.time()).compareTo(nullSafeTime(left.time())))
                .limit(12)
                .toList();
    }

    private ActivityItem activity(ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new ActivityItem(
                text(rs, "type"),
                text(rs, "title"),
                text(rs, "patient_name"),
                text(rs, "status"),
                time(rs, "event_time"),
                text(rs, "target_path"));
    }

    private long countByStatus(String tableName, String status) {
        return count("SELECT COUNT(*) FROM " + tableName + " WHERE deleted = false AND status = ?", status);
    }

    private long count(String sql, Object... args) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class, args);
        return value == null ? 0L : value;
    }

    private BigDecimal money(String sql, Object... args) {
        return scale(jdbcTemplate.queryForObject(sql, BigDecimal.class, args));
    }

    private BigDecimal scale(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2);
    }

    private LocalDateTime time(ResultSet rs, String column) throws java.sql.SQLException {
        Timestamp timestamp = rs.getTimestamp(column);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private String text(ResultSet rs, String column) throws java.sql.SQLException {
        String value = rs.getString(column);
        return value == null ? "" : value;
    }

    private LocalDateTime nullSafeTime(LocalDateTime value) {
        return value == null ? LocalDateTime.MIN : value;
    }
}
