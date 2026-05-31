package com.yueshan.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.yueshan.backend.domain.MedicalRecord;
import com.yueshan.backend.dto.MedicalRecordQueryRequest;

@Mapper
public interface MedicalRecordMapper {
    @Select("""
            <script>
            SELECT r.id, r.record_no, r.admission_id, r.patient_id, p.name AS patient_name,
                   r.record_type, r.title, r.content, r.doctor_name, r.record_time,
                   r.status, r.remark, r.deleted, r.created_at, r.updated_at
            FROM medical_record r
            LEFT JOIN patients p ON p.id = r.patient_id
            <where>
                r.deleted = false
                <if test="query.admissionId != null">AND r.admission_id = #{query.admissionId}</if>
                <if test="query.patientId != null">AND r.patient_id = #{query.patientId}</if>
                <if test="query.recordType != null and query.recordType != ''">AND r.record_type = #{query.recordType}</if>
                <if test="query.status != null and query.status != ''">AND r.status = #{query.status}</if>
                <if test="query.doctorName != null and query.doctorName != ''">AND r.doctor_name ILIKE CONCAT('%', #{query.doctorName}, '%')</if>
                <if test="query.title != null and query.title != ''">AND r.title ILIKE CONCAT('%', #{query.title}, '%')</if>
            </where>
            ORDER BY r.id DESC LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<MedicalRecord> findPage(@Param("query") MedicalRecordQueryRequest query, @Param("offset") int offset, @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM medical_record r
            <where>
                r.deleted = false
                <if test="query.admissionId != null">AND r.admission_id = #{query.admissionId}</if>
                <if test="query.patientId != null">AND r.patient_id = #{query.patientId}</if>
                <if test="query.recordType != null and query.recordType != ''">AND r.record_type = #{query.recordType}</if>
                <if test="query.status != null and query.status != ''">AND r.status = #{query.status}</if>
                <if test="query.doctorName != null and query.doctorName != ''">AND r.doctor_name ILIKE CONCAT('%', #{query.doctorName}, '%')</if>
                <if test="query.title != null and query.title != ''">AND r.title ILIKE CONCAT('%', #{query.title}, '%')</if>
            </where>
            </script>
            """)
    long countPage(@Param("query") MedicalRecordQueryRequest query);

    @Select("""
            SELECT r.id, r.record_no, r.admission_id, r.patient_id, p.name AS patient_name,
                   r.record_type, r.title, r.content, r.doctor_name, r.record_time,
                   r.status, r.remark, r.deleted, r.created_at, r.updated_at
            FROM medical_record r
            LEFT JOIN patients p ON p.id = r.patient_id
            WHERE r.id = #{id} AND r.deleted = false
            """)
    MedicalRecord findById(Long id);

    @Select("SELECT COUNT(1) FROM medical_record WHERE record_no = #{recordNo}")
    int countByRecordNo(String recordNo);

    @Select("SELECT COUNT(1) FROM medical_record WHERE record_no = #{recordNo} AND id != #{excludeId}")
    int countByRecordNoExcludeId(@Param("recordNo") String recordNo, @Param("excludeId") Long excludeId);

    @Insert("""
            INSERT INTO medical_record (
                record_no, admission_id, patient_id, record_type, title, content,
                doctor_name, record_time, status, remark, deleted, created_at, updated_at
            ) VALUES (
                #{recordNo}, #{admissionId}, #{patientId}, #{recordType}, #{title}, #{content},
                #{doctorName}, #{recordTime}, #{status}, #{remark}, false, NOW(), NOW()
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(MedicalRecord record);

    @Update("""
            UPDATE medical_record
            SET record_no=#{recordNo}, admission_id=#{admissionId}, patient_id=#{patientId},
                record_type=#{recordType}, title=#{title}, content=#{content}, doctor_name=#{doctorName},
                record_time=#{recordTime}, status=#{status}, remark=#{remark}, updated_at=NOW()
            WHERE id=#{id} AND deleted=false
            """)
    int update(MedicalRecord record);

    @Update("""
            UPDATE medical_record
            SET status=#{status}, remark=COALESCE(#{remark}, remark), updated_at=NOW()
            WHERE id=#{id} AND deleted=false
            """)
    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("remark") String remark);

    @Update("UPDATE medical_record SET deleted=true, updated_at=NOW() WHERE id=#{id} AND deleted=false")
    int logicDeleteById(Long id);
}
