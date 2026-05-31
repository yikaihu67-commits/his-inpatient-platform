package com.yueshan.backend.mapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.yueshan.backend.domain.SurgeryOperation;
import com.yueshan.backend.dto.SurgeryQueryRequest;

@Mapper
public interface SurgeryOperationMapper {
    @Select("""
            <script>
            SELECT s.id, s.surgery_no, s.patient_id, p.name AS patient_name, s.admission_id, a.admission_no,
                   s.surgery_name, s.preoperative_diagnosis, s.surgery_level, s.surgery_type,
                   s.operating_room, s.planned_time, s.actual_start_time, s.actual_end_time,
                   s.primary_doctor_name, s.assistant_doctor_name, s.anesthesia_method,
                   s.anesthesiologist_name, s.status, s.surgery_fee, s.remark, s.deleted,
                   s.created_at, s.updated_at
            FROM surgery_operation s
            LEFT JOIN patients p ON p.id = s.patient_id
            LEFT JOIN inpatient_admission a ON a.id = s.admission_id
            <where>
                s.deleted = false
                <if test="query.patientId != null">AND s.patient_id = #{query.patientId}</if>
                <if test="query.admissionId != null">AND s.admission_id = #{query.admissionId}</if>
                <if test="query.surgeryNo != null and query.surgeryNo != ''">AND s.surgery_no ILIKE CONCAT('%', #{query.surgeryNo}, '%')</if>
                <if test="query.surgeryName != null and query.surgeryName != ''">AND s.surgery_name ILIKE CONCAT('%', #{query.surgeryName}, '%')</if>
                <if test="query.status != null and query.status != ''">AND s.status = #{query.status}</if>
            </where>
            ORDER BY s.id DESC
            LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<SurgeryOperation> findPage(@Param("query") SurgeryQueryRequest query, @Param("offset") int offset, @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM surgery_operation s
            <where>
                s.deleted = false
                <if test="query.patientId != null">AND s.patient_id = #{query.patientId}</if>
                <if test="query.admissionId != null">AND s.admission_id = #{query.admissionId}</if>
                <if test="query.surgeryNo != null and query.surgeryNo != ''">AND s.surgery_no ILIKE CONCAT('%', #{query.surgeryNo}, '%')</if>
                <if test="query.surgeryName != null and query.surgeryName != ''">AND s.surgery_name ILIKE CONCAT('%', #{query.surgeryName}, '%')</if>
                <if test="query.status != null and query.status != ''">AND s.status = #{query.status}</if>
            </where>
            </script>
            """)
    long countPage(@Param("query") SurgeryQueryRequest query);

    @Select("""
            SELECT s.id, s.surgery_no, s.patient_id, p.name AS patient_name, s.admission_id, a.admission_no,
                   s.surgery_name, s.preoperative_diagnosis, s.surgery_level, s.surgery_type,
                   s.operating_room, s.planned_time, s.actual_start_time, s.actual_end_time,
                   s.primary_doctor_name, s.assistant_doctor_name, s.anesthesia_method,
                   s.anesthesiologist_name, s.status, s.surgery_fee, s.remark, s.deleted,
                   s.created_at, s.updated_at
            FROM surgery_operation s
            LEFT JOIN patients p ON p.id = s.patient_id
            LEFT JOIN inpatient_admission a ON a.id = s.admission_id
            WHERE s.id = #{id} AND s.deleted = false
            """)
    SurgeryOperation findById(Long id);

    @Select("SELECT COUNT(1) FROM surgery_operation WHERE surgery_no = #{surgeryNo}")
    int countBySurgeryNo(String surgeryNo);

    @Select("SELECT COUNT(1) FROM surgery_operation WHERE surgery_no = #{surgeryNo} AND id <> #{excludeId}")
    int countBySurgeryNoExcludeId(@Param("surgeryNo") String surgeryNo, @Param("excludeId") Long excludeId);

    @Insert("""
            INSERT INTO surgery_operation (
                surgery_no, patient_id, admission_id, surgery_name, preoperative_diagnosis,
                surgery_level, surgery_type, operating_room, planned_time, actual_start_time,
                actual_end_time, primary_doctor_name, assistant_doctor_name, anesthesia_method,
                anesthesiologist_name, status, surgery_fee, remark, deleted, created_at, updated_at
            ) VALUES (
                #{surgeryNo}, #{patientId}, #{admissionId}, #{surgeryName}, #{preoperativeDiagnosis},
                #{surgeryLevel}, #{surgeryType}, #{operatingRoom}, #{plannedTime}, #{actualStartTime},
                #{actualEndTime}, #{primaryDoctorName}, #{assistantDoctorName}, #{anesthesiaMethod},
                #{anesthesiologistName}, #{status}, #{surgeryFee}, #{remark}, false, NOW(), NOW()
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(SurgeryOperation item);

    @Update("""
            UPDATE surgery_operation
            SET surgery_no=#{surgeryNo}, patient_id=#{patientId}, admission_id=#{admissionId},
                surgery_name=#{surgeryName}, preoperative_diagnosis=#{preoperativeDiagnosis},
                surgery_level=#{surgeryLevel}, surgery_type=#{surgeryType}, operating_room=#{operatingRoom},
                planned_time=#{plannedTime}, actual_start_time=#{actualStartTime}, actual_end_time=#{actualEndTime},
                primary_doctor_name=#{primaryDoctorName}, assistant_doctor_name=#{assistantDoctorName},
                anesthesia_method=#{anesthesiaMethod}, anesthesiologist_name=#{anesthesiologistName},
                status=#{status}, surgery_fee=#{surgeryFee}, remark=#{remark}, updated_at=NOW()
            WHERE id=#{id} AND deleted=false
            """)
    int update(SurgeryOperation item);

    @Update("UPDATE surgery_operation SET deleted=true, updated_at=NOW() WHERE id=#{id} AND deleted=false")
    int logicDeleteById(Long id);

    @Update("""
            UPDATE surgery_operation
            SET status=#{status},
                planned_time=COALESCE(#{plannedTime}, planned_time),
                actual_start_time=COALESCE(#{actualStartTime}, actual_start_time),
                actual_end_time=COALESCE(#{actualEndTime}, actual_end_time),
                operating_room=COALESCE(#{operatingRoom}, operating_room),
                primary_doctor_name=COALESCE(#{primaryDoctorName}, primary_doctor_name),
                assistant_doctor_name=COALESCE(#{assistantDoctorName}, assistant_doctor_name),
                anesthesia_method=COALESCE(#{anesthesiaMethod}, anesthesia_method),
                anesthesiologist_name=COALESCE(#{anesthesiologistName}, anesthesiologist_name),
                surgery_fee=COALESCE(#{surgeryFee}, surgery_fee),
                remark=COALESCE(#{remark}, remark),
                updated_at=NOW()
            WHERE id=#{id} AND deleted=false
            """)
    int updateStatus(@Param("id") Long id, @Param("status") String status,
            @Param("plannedTime") LocalDateTime plannedTime,
            @Param("actualStartTime") LocalDateTime actualStartTime,
            @Param("actualEndTime") LocalDateTime actualEndTime,
            @Param("operatingRoom") String operatingRoom,
            @Param("primaryDoctorName") String primaryDoctorName,
            @Param("assistantDoctorName") String assistantDoctorName,
            @Param("anesthesiaMethod") String anesthesiaMethod,
            @Param("anesthesiologistName") String anesthesiologistName,
            @Param("surgeryFee") BigDecimal surgeryFee,
            @Param("remark") String remark);
}
