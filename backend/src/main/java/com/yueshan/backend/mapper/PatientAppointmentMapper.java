package com.yueshan.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.yueshan.backend.domain.PatientAppointment;
import com.yueshan.backend.dto.PatientAppointmentQueryRequest;

@Mapper
public interface PatientAppointmentMapper {
    @Select("""
            <script>
            SELECT pa.id, pa.patient_id, p.name AS patient_name, pa.admission_id, a.admission_no,
                   pa.appointment_type, pa.appointment_item, pa.appointment_date, pa.time_slot,
                   pa.contact_phone, pa.remark, pa.status, pa.cancel_time, pa.complete_time,
                   pa.deleted, pa.created_at, pa.updated_at
            FROM patient_appointment pa
            LEFT JOIN patients p ON p.id = pa.patient_id
            LEFT JOIN inpatient_admission a ON a.id = pa.admission_id
            <where>
                pa.deleted = false
                <if test="query.patientId != null">AND pa.patient_id = #{query.patientId}</if>
                <if test="query.admissionId != null">AND pa.admission_id = #{query.admissionId}</if>
                <if test="query.appointmentType != null and query.appointmentType != ''">AND pa.appointment_type = #{query.appointmentType}</if>
                <if test="query.status != null and query.status != ''">AND pa.status = #{query.status}</if>
            </where>
            ORDER BY pa.appointment_date DESC, pa.id DESC
            LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<PatientAppointment> findPage(@Param("query") PatientAppointmentQueryRequest query,
                                      @Param("offset") int offset,
                                      @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM patient_appointment pa
            <where>
                pa.deleted = false
                <if test="query.patientId != null">AND pa.patient_id = #{query.patientId}</if>
                <if test="query.admissionId != null">AND pa.admission_id = #{query.admissionId}</if>
                <if test="query.appointmentType != null and query.appointmentType != ''">AND pa.appointment_type = #{query.appointmentType}</if>
                <if test="query.status != null and query.status != ''">AND pa.status = #{query.status}</if>
            </where>
            </script>
            """)
    long countPage(@Param("query") PatientAppointmentQueryRequest query);

    @Select("""
            SELECT pa.id, pa.patient_id, p.name AS patient_name, pa.admission_id, a.admission_no,
                   pa.appointment_type, pa.appointment_item, pa.appointment_date, pa.time_slot,
                   pa.contact_phone, pa.remark, pa.status, pa.cancel_time, pa.complete_time,
                   pa.deleted, pa.created_at, pa.updated_at
            FROM patient_appointment pa
            LEFT JOIN patients p ON p.id = pa.patient_id
            LEFT JOIN inpatient_admission a ON a.id = pa.admission_id
            WHERE pa.id = #{id}
              AND pa.deleted = false
            """)
    PatientAppointment findById(Long id);

    @Insert("""
            INSERT INTO patient_appointment (
                patient_id, admission_id, appointment_type, appointment_item, appointment_date,
                time_slot, contact_phone, remark, status, deleted, created_at, updated_at
            ) VALUES (
                #{patientId}, #{admissionId}, #{appointmentType}, #{appointmentItem}, #{appointmentDate},
                #{timeSlot}, #{contactPhone}, #{remark}, #{status}, false, NOW(), NOW()
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(PatientAppointment item);

    @Update("""
            UPDATE patient_appointment
            SET patient_id=#{patientId}, admission_id=#{admissionId}, appointment_type=#{appointmentType},
                appointment_item=#{appointmentItem}, appointment_date=#{appointmentDate}, time_slot=#{timeSlot},
                contact_phone=#{contactPhone}, remark=#{remark}, updated_at=NOW()
            WHERE id=#{id}
              AND deleted=false
              AND status IN ('REQUESTED', 'CONFIRMED')
            """)
    int update(PatientAppointment item);

    @Update("UPDATE patient_appointment SET status='CONFIRMED', updated_at=NOW() WHERE id=#{id} AND deleted=false AND status='REQUESTED'")
    int confirm(Long id);

    @Update("UPDATE patient_appointment SET status='COMPLETED', complete_time=NOW(), updated_at=NOW() WHERE id=#{id} AND deleted=false AND status IN ('REQUESTED', 'CONFIRMED')")
    int complete(Long id);

    @Update("UPDATE patient_appointment SET status='CANCELLED', cancel_time=NOW(), updated_at=NOW() WHERE id=#{id} AND deleted=false AND status IN ('REQUESTED', 'CONFIRMED')")
    int cancel(Long id);

    @Update("UPDATE patient_appointment SET deleted=true, updated_at=NOW() WHERE id=#{id} AND deleted=false AND status='REQUESTED'")
    int logicDeleteById(Long id);
}
