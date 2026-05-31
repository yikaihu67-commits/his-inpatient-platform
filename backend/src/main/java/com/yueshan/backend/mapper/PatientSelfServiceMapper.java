package com.yueshan.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.yueshan.backend.dto.selfservice.PatientSelfServiceVerifyRequest;
import com.yueshan.backend.dto.selfservice.SelfServiceDischargeResponse;
import com.yueshan.backend.dto.selfservice.SelfServiceVerifyRow;

@Mapper
public interface PatientSelfServiceMapper {

    @Select("""
            <script>
            SELECT p.id AS patient_id, p.patient_no, p.name AS patient_name, p.gender,
                   p.phone, p.birth_date, a.id AS admission_id, a.admission_no,
                   a.department_name, a.ward_name, a.admission_time, a.discharge_time,
                   a.doctor_name, a.status AS admission_status
            FROM patients p
            JOIN inpatient_admission a ON a.patient_id = p.id
            <where>
                p.status != 'DELETED'
                AND a.status != 'DELETED'
                <if test="request.patientId != null">
                    AND p.id = #{request.patientId}
                </if>
                <if test="request.admissionId != null">
                    AND a.id = #{request.admissionId}
                </if>
                <if test="request.admissionNo != null and request.admissionNo != ''">
                    AND a.admission_no = #{request.admissionNo}
                </if>
                <if test="request.phone != null and request.phone != ''">
                    AND p.phone = #{request.phone}
                </if>
                <if test="request.patientName != null and request.patientName != ''">
                    AND p.name = #{request.patientName}
                </if>
                <if test="request.idCardLast4 != null and request.idCardLast4 != ''">
                    AND RIGHT(p.id_card, 4) = #{request.idCardLast4}
                </if>
            </where>
            ORDER BY CASE WHEN a.status = 'IN_HOSPITAL' THEN 0 ELSE 1 END, a.id DESC
            LIMIT 10
            </script>
            """)
    List<SelfServiceVerifyRow> findMatchingAdmissions(@Param("request") PatientSelfServiceVerifyRequest request);

    @Select("""
            SELECT status, discharge_diagnosis, discharge_summary, discharge_order,
                   take_home_drug_instruction, unpaid_amount, discharge_time
            FROM inpatient_discharge
            WHERE admission_id = #{admissionId}
              AND patient_id = #{patientId}
              AND deleted = false
            ORDER BY id DESC
            LIMIT 1
            """)
    SelfServiceDischargeResponse findLatestDischarge(@Param("admissionId") Long admissionId, @Param("patientId") Long patientId);
}
