package com.yueshan.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.yueshan.backend.domain.InpatientAdmission;
import com.yueshan.backend.dto.AdmissionQueryRequest;

@Mapper
public interface AdmissionMapper {

    @Select("""
            <script>
            SELECT a.id, a.admission_no, a.patient_id, p.patient_no, p.name AS patient_name,
                   a.department_name, a.ward_name, a.admission_time, a.discharge_time, a.admission_diagnosis,
                   a.charge_type, a.nursing_level, a.doctor_name, a.status, a.created_at, a.updated_at
            FROM inpatient_admission a
            LEFT JOIN patients p ON p.id = a.patient_id
            <where>
                a.status != 'DELETED'
                <if test="query.admissionNo != null and query.admissionNo != ''">
                    AND a.admission_no ILIKE CONCAT('%', #{query.admissionNo}, '%')
                </if>
                <if test="query.patientId != null">
                    AND a.patient_id = #{query.patientId}
                </if>
                <if test="query.patientName != null and query.patientName != ''">
                    AND p.name ILIKE CONCAT('%', #{query.patientName}, '%')
                </if>
                <if test="query.departmentName != null and query.departmentName != ''">
                    AND a.department_name ILIKE CONCAT('%', #{query.departmentName}, '%')
                </if>
                <if test="query.doctorName != null and query.doctorName != ''">
                    AND a.doctor_name ILIKE CONCAT('%', #{query.doctorName}, '%')
                </if>
                <if test="query.status != null and query.status != ''">
                    AND a.status = #{query.status}
                </if>
            </where>
            ORDER BY a.id DESC
            LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<InpatientAdmission> findPage(
            @Param("query") AdmissionQueryRequest query,
            @Param("offset") int offset,
            @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM inpatient_admission a
            LEFT JOIN patients p ON p.id = a.patient_id
            <where>
                a.status != 'DELETED'
                <if test="query.admissionNo != null and query.admissionNo != ''">
                    AND a.admission_no ILIKE CONCAT('%', #{query.admissionNo}, '%')
                </if>
                <if test="query.patientId != null">
                    AND a.patient_id = #{query.patientId}
                </if>
                <if test="query.patientName != null and query.patientName != ''">
                    AND p.name ILIKE CONCAT('%', #{query.patientName}, '%')
                </if>
                <if test="query.departmentName != null and query.departmentName != ''">
                    AND a.department_name ILIKE CONCAT('%', #{query.departmentName}, '%')
                </if>
                <if test="query.doctorName != null and query.doctorName != ''">
                    AND a.doctor_name ILIKE CONCAT('%', #{query.doctorName}, '%')
                </if>
                <if test="query.status != null and query.status != ''">
                    AND a.status = #{query.status}
                </if>
            </where>
            </script>
            """)
    long countPage(@Param("query") AdmissionQueryRequest query);

    @Select("""
            SELECT a.id, a.admission_no, a.patient_id, p.patient_no, p.name AS patient_name,
                   a.department_name, a.ward_name, a.admission_time, a.discharge_time, a.admission_diagnosis,
                   a.charge_type, a.nursing_level, a.doctor_name, a.status, a.created_at, a.updated_at
            FROM inpatient_admission a
            LEFT JOIN patients p ON p.id = a.patient_id
            WHERE a.id = #{id}
              AND a.status <> 'DELETED'
            """)
    InpatientAdmission findById(Long id);

    @Select("""
            SELECT a.id, a.admission_no, a.patient_id, p.patient_no, p.name AS patient_name,
                   a.department_name, a.ward_name, a.admission_time, a.discharge_time, a.admission_diagnosis,
                   a.charge_type, a.nursing_level, a.doctor_name, a.status, a.created_at, a.updated_at
            FROM inpatient_admission a
            LEFT JOIN patients p ON p.id = a.patient_id
            WHERE a.admission_no = #{admissionNo}
              AND a.status <> 'DELETED'
            """)
    InpatientAdmission findByAdmissionNo(String admissionNo);

    @Select("SELECT COUNT(1) FROM inpatient_admission WHERE admission_no = #{admissionNo}")
    int countByAdmissionNo(String admissionNo);

    @Select("""
            SELECT COUNT(1)
            FROM inpatient_admission
            WHERE admission_no = #{admissionNo}
              AND id <> #{excludeId}
            """)
    int countByAdmissionNoExcludeId(@Param("admissionNo") String admissionNo, @Param("excludeId") Long excludeId);

    @Select("""
            SELECT COUNT(1)
            FROM inpatient_admission
            WHERE patient_id = #{patientId}
              AND status = 'IN_HOSPITAL'
            """)
    int countOpenByPatientId(Long patientId);

    @Select("""
            SELECT COUNT(1)
            FROM inpatient_admission
            WHERE patient_id = #{patientId}
              AND status = 'IN_HOSPITAL'
              AND id <> #{excludeId}
            """)
    int countOpenByPatientIdExcludeId(@Param("patientId") Long patientId, @Param("excludeId") Long excludeId);

    @Insert("""
            INSERT INTO inpatient_admission (
                admission_no, patient_id, department_name, ward_name, admission_time, discharge_time,
                admission_diagnosis, charge_type, nursing_level, doctor_name, status,
                created_at, updated_at
            )
            VALUES (
                #{admissionNo}, #{patientId}, #{departmentName}, #{wardName}, #{admissionTime}, #{dischargeTime},
                #{admissionDiagnosis}, #{chargeType}, #{nursingLevel}, #{doctorName}, #{status},
                NOW(), NOW()
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(InpatientAdmission admission);

    @Update("""
            UPDATE inpatient_admission
            SET admission_no = #{admissionNo},
                patient_id = #{patientId},
                department_name = #{departmentName},
                ward_name = #{wardName},
                admission_time = #{admissionTime},
                discharge_time = #{dischargeTime},
                admission_diagnosis = #{admissionDiagnosis},
                charge_type = #{chargeType},
                nursing_level = #{nursingLevel},
                doctor_name = #{doctorName},
                status = #{status},
                updated_at = NOW()
            WHERE id = #{id}
              AND status <> 'DELETED'
            """)
    int update(InpatientAdmission admission);

    @Update("""
            UPDATE inpatient_admission
            SET status = 'DELETED',
                updated_at = NOW()
            WHERE id = #{id}
              AND status <> 'DELETED'
            """)
    int logicDeleteById(Long id);

    @Update("""
            UPDATE inpatient_admission
            SET status = 'IN_HOSPITAL',
                admission_time = COALESCE(admission_time, NOW()),
                updated_at = NOW()
            WHERE id = #{id}
              AND status IN ('DRAFT', 'REGISTERED')
            """)
    int markInHospital(Long id);

    @Update("""
            UPDATE inpatient_admission
            SET status = 'DISCHARGED',
                discharge_time = COALESCE(#{dischargeTime}, NOW()),
                updated_at = NOW()
            WHERE id = #{id}
              AND status = 'IN_HOSPITAL'
            """)
    int markDischarged(@Param("id") Long id, @Param("dischargeTime") java.time.LocalDateTime dischargeTime);

    @Update("""
            UPDATE inpatient_admission
            SET status = 'CANCELLED',
                updated_at = NOW()
            WHERE id = #{id}
              AND status IN ('DRAFT', 'REGISTERED')
            """)
    int cancel(Long id);
}
