package com.yueshan.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.yueshan.backend.domain.PharmacyDispense;
import com.yueshan.backend.dto.PharmacyDispenseQueryRequest;

@Mapper
public interface PharmacyDispenseMapper {
    @Select("""
            <script>
            SELECT d.id, d.dispense_no, d.order_id, o.order_no, d.admission_id, d.patient_id, p.name AS patient_name,
                   d.drug_id, g.drug_code, g.drug_name, d.quantity, d.pharmacist_name, d.dispense_time, d.return_time,
                   d.status, d.remark, d.deleted, d.created_at, d.updated_at
            FROM pharmacy_dispense d
            LEFT JOIN medical_order o ON o.id = d.order_id
            LEFT JOIN patients p ON p.id = d.patient_id
            LEFT JOIN drug g ON g.id = d.drug_id
            <where>
                d.deleted = false
                <if test="query.orderId != null">AND d.order_id = #{query.orderId}</if>
                <if test="query.admissionId != null">AND d.admission_id = #{query.admissionId}</if>
                <if test="query.patientId != null">AND d.patient_id = #{query.patientId}</if>
                <if test="query.drugId != null">AND d.drug_id = #{query.drugId}</if>
                <if test="query.status != null and query.status != ''">AND d.status = #{query.status}</if>
            </where>
            ORDER BY d.id DESC LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<PharmacyDispense> findPage(@Param("query") PharmacyDispenseQueryRequest query, @Param("offset") int offset, @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1) FROM pharmacy_dispense d
            <where>
                d.deleted = false
                <if test="query.orderId != null">AND d.order_id = #{query.orderId}</if>
                <if test="query.admissionId != null">AND d.admission_id = #{query.admissionId}</if>
                <if test="query.patientId != null">AND d.patient_id = #{query.patientId}</if>
                <if test="query.drugId != null">AND d.drug_id = #{query.drugId}</if>
                <if test="query.status != null and query.status != ''">AND d.status = #{query.status}</if>
            </where>
            </script>
            """)
    long countPage(@Param("query") PharmacyDispenseQueryRequest query);

    @Select("""
            SELECT d.id, d.dispense_no, d.order_id, o.order_no, d.admission_id, d.patient_id, p.name AS patient_name,
                   d.drug_id, g.drug_code, g.drug_name, d.quantity, d.pharmacist_name, d.dispense_time, d.return_time,
                   d.status, d.remark, d.deleted, d.created_at, d.updated_at
            FROM pharmacy_dispense d
            LEFT JOIN medical_order o ON o.id = d.order_id
            LEFT JOIN patients p ON p.id = d.patient_id
            LEFT JOIN drug g ON g.id = d.drug_id
            WHERE d.id = #{id} AND d.deleted = false
            """)
    PharmacyDispense findById(Long id);

    @Select("SELECT COUNT(1) FROM pharmacy_dispense WHERE dispense_no = #{dispenseNo}")
    int countByDispenseNo(String dispenseNo);

    @Select("""
            SELECT COUNT(1)
            FROM pharmacy_dispense
            WHERE order_id = #{orderId}
              AND deleted = false
              AND status IN ('CREATED', 'DISPENSED')
            """)
    int countActiveByOrderId(Long orderId);

    @Insert("""
            INSERT INTO pharmacy_dispense (
                dispense_no, order_id, admission_id, patient_id, drug_id, quantity, pharmacist_name,
                dispense_time, return_time, status, remark, deleted, created_at, updated_at
            ) VALUES (
                #{dispenseNo}, #{orderId}, #{admissionId}, #{patientId}, #{drugId}, #{quantity}, #{pharmacistName},
                #{dispenseTime}, #{returnTime}, #{status}, #{remark}, false, NOW(), NOW()
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(PharmacyDispense dispense);

    @Update("""
            UPDATE pharmacy_dispense SET status=#{status}, pharmacist_name=COALESCE(#{pharmacistName}, pharmacist_name),
                dispense_time=#{dispenseTime}, return_time=#{returnTime}, remark=COALESCE(#{remark}, remark), updated_at=NOW()
            WHERE id=#{id} AND deleted=false
            """)
    int updateStatus(PharmacyDispense dispense);
}
