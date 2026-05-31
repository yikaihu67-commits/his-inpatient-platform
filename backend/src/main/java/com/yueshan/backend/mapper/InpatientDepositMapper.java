package com.yueshan.backend.mapper;

import java.math.BigDecimal;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.yueshan.backend.domain.InpatientDeposit;
import com.yueshan.backend.dto.InpatientDepositQueryRequest;

@Mapper
public interface InpatientDepositMapper {
    @Select("""
            <script>
            SELECT d.id, d.deposit_no, d.admission_id, d.patient_id, p.name AS patient_name,
                   d.amount, d.payment_method, d.transaction_type, d.transaction_time,
                   d.operator_name, d.status, d.remark, d.deleted, d.created_at, d.updated_at
            FROM inpatient_deposit d
            LEFT JOIN patients p ON p.id = d.patient_id
            <where>
                d.deleted = false
                <if test="query.admissionId != null">AND d.admission_id = #{query.admissionId}</if>
                <if test="query.patientId != null">AND d.patient_id = #{query.patientId}</if>
                <if test="query.transactionType != null and query.transactionType != ''">AND d.transaction_type = #{query.transactionType}</if>
                <if test="query.status != null and query.status != ''">AND d.status = #{query.status}</if>
            </where>
            ORDER BY d.id DESC LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<InpatientDeposit> findPage(@Param("query") InpatientDepositQueryRequest query, @Param("offset") int offset, @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM inpatient_deposit d
            <where>
                d.deleted = false
                <if test="query.admissionId != null">AND d.admission_id = #{query.admissionId}</if>
                <if test="query.patientId != null">AND d.patient_id = #{query.patientId}</if>
                <if test="query.transactionType != null and query.transactionType != ''">AND d.transaction_type = #{query.transactionType}</if>
                <if test="query.status != null and query.status != ''">AND d.status = #{query.status}</if>
            </where>
            </script>
            """)
    long countPage(@Param("query") InpatientDepositQueryRequest query);

    @Select("""
            SELECT d.id, d.deposit_no, d.admission_id, d.patient_id, p.name AS patient_name,
                   d.amount, d.payment_method, d.transaction_type, d.transaction_time,
                   d.operator_name, d.status, d.remark, d.deleted, d.created_at, d.updated_at
            FROM inpatient_deposit d
            LEFT JOIN patients p ON p.id = d.patient_id
            WHERE d.id = #{id} AND d.deleted = false
            """)
    InpatientDeposit findById(Long id);

    @Select("SELECT COUNT(1) FROM inpatient_deposit WHERE deposit_no = #{depositNo}")
    int countByDepositNo(String depositNo);

    @Insert("""
            INSERT INTO inpatient_deposit (
                deposit_no, admission_id, patient_id, amount, payment_method, transaction_type,
                transaction_time, operator_name, status, remark, deleted, created_at, updated_at
            ) VALUES (
                #{depositNo}, #{admissionId}, #{patientId}, #{amount}, #{paymentMethod}, #{transactionType},
                #{transactionTime}, #{operatorName}, #{status}, #{remark}, false, NOW(), NOW()
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(InpatientDeposit deposit);

    @Update("UPDATE inpatient_deposit SET status='CANCELLED', updated_at=NOW() WHERE id=#{id} AND deleted=false")
    int cancel(Long id);

    @Select("""
            SELECT COALESCE(SUM(amount), 0)
            FROM inpatient_deposit
            WHERE admission_id=#{admissionId}
              AND transaction_type=#{transactionType}
              AND status='SUCCESS'
              AND deleted=false
            """)
    BigDecimal sumSuccessByAdmissionAndType(@Param("admissionId") Long admissionId, @Param("transactionType") String transactionType);
}
