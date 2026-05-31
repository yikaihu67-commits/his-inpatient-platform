package com.yueshan.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.yueshan.backend.domain.NursingRecord;
import com.yueshan.backend.dto.NursingRecordQueryRequest;

@Mapper
public interface NursingRecordMapper {

    String SELECT_COLUMNS = """
             nr.id, nr.record_no, nr.order_id, nr.admission_id, a.admission_no,
            nr.patient_id, p.name AS patient_name, nr.nursing_type, nr.nursing_content,
            nr.temperature, nr.pulse, nr.respiration, nr.blood_pressure, nr.intake_amount,
            nr.output_amount, nr.nursing_level, nr.nurse_name, nr.record_time, nr.status,
            nr.billable, nr.nursing_fee, nr.billed, nr.remark, nr.deleted, nr.created_at, nr.updated_at
            """;

    @Select("""
            <script>
            SELECT """ + SELECT_COLUMNS + """
            FROM nursing_record nr
            LEFT JOIN inpatient_admission a ON a.id = nr.admission_id
            LEFT JOIN patients p ON p.id = nr.patient_id
            <where>
              nr.deleted = false
              <if test="query.orderId != null">AND nr.order_id = #{query.orderId}</if>
              <if test="query.admissionId != null">AND nr.admission_id = #{query.admissionId}</if>
              <if test="query.patientId != null">AND nr.patient_id = #{query.patientId}</if>
              <if test="query.nursingType != null and query.nursingType != ''">AND nr.nursing_type = #{query.nursingType}</if>
              <if test="query.status != null and query.status != ''">AND nr.status = #{query.status}</if>
              <if test="query.nurseName != null and query.nurseName != ''">AND nr.nurse_name ILIKE CONCAT('%', #{query.nurseName}, '%')</if>
            </where>
            ORDER BY nr.id DESC
            LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<NursingRecord> findPage(@Param("query") NursingRecordQueryRequest query, @Param("offset") int offset, @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM nursing_record nr
            <where>
              nr.deleted = false
              <if test="query.orderId != null">AND nr.order_id = #{query.orderId}</if>
              <if test="query.admissionId != null">AND nr.admission_id = #{query.admissionId}</if>
              <if test="query.patientId != null">AND nr.patient_id = #{query.patientId}</if>
              <if test="query.nursingType != null and query.nursingType != ''">AND nr.nursing_type = #{query.nursingType}</if>
              <if test="query.status != null and query.status != ''">AND nr.status = #{query.status}</if>
              <if test="query.nurseName != null and query.nurseName != ''">AND nr.nurse_name ILIKE CONCAT('%', #{query.nurseName}, '%')</if>
            </where>
            </script>
            """)
    long countPage(@Param("query") NursingRecordQueryRequest query);

    @Select("""
            SELECT """ + SELECT_COLUMNS + """
            FROM nursing_record nr
            LEFT JOIN inpatient_admission a ON a.id = nr.admission_id
            LEFT JOIN patients p ON p.id = nr.patient_id
            WHERE nr.id = #{id}
              AND nr.deleted = false
            """)
    NursingRecord findById(Long id);

    @Select("SELECT COUNT(1) FROM nursing_record WHERE record_no = #{recordNo} AND deleted = false")
    int countByRecordNo(String recordNo);

    @Insert("""
            INSERT INTO nursing_record (
              record_no, order_id, admission_id, patient_id, nursing_type, nursing_content,
              temperature, pulse, respiration, blood_pressure, intake_amount, output_amount,
              nursing_level, nurse_name, record_time, status, billable, nursing_fee, billed,
              remark, deleted, created_at, updated_at
            )
            VALUES (
              #{recordNo}, #{orderId}, #{admissionId}, #{patientId}, #{nursingType}, #{nursingContent},
              #{temperature}, #{pulse}, #{respiration}, #{bloodPressure}, #{intakeAmount}, #{outputAmount},
              #{nursingLevel}, #{nurseName}, #{recordTime}, #{status}, #{billable}, #{nursingFee}, #{billed},
              #{remark}, false, NOW(), NOW()
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(NursingRecord item);

    @Update("""
            UPDATE nursing_record
            SET order_id = #{orderId},
                admission_id = #{admissionId},
                patient_id = #{patientId},
                nursing_type = #{nursingType},
                nursing_content = #{nursingContent},
                temperature = #{temperature},
                pulse = #{pulse},
                respiration = #{respiration},
                blood_pressure = #{bloodPressure},
                intake_amount = #{intakeAmount},
                output_amount = #{outputAmount},
                nursing_level = #{nursingLevel},
                nurse_name = #{nurseName},
                record_time = #{recordTime},
                billable = #{billable},
                nursing_fee = #{nursingFee},
                remark = #{remark},
                updated_at = NOW()
            WHERE id = #{id}
              AND deleted = false
            """)
    int update(NursingRecord item);

    @Update("""
            UPDATE nursing_record
            SET status = #{status},
                nurse_name = COALESCE(#{nurseName}, nurse_name),
                remark = COALESCE(#{remark}, remark),
                updated_at = NOW()
            WHERE id = #{id}
              AND deleted = false
            """)
    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("nurseName") String nurseName, @Param("remark") String remark);

    @Update("""
            UPDATE nursing_record
            SET status = 'BILLED',
                billed = true,
                remark = COALESCE(#{remark}, remark),
                updated_at = NOW()
            WHERE id = #{id}
              AND deleted = false
            """)
    int markBilled(@Param("id") Long id, @Param("remark") String remark);
}
