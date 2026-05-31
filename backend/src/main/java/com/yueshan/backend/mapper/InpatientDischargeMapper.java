package com.yueshan.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.yueshan.backend.domain.InpatientDischarge;
import com.yueshan.backend.dto.DischargeQueryRequest;

@Mapper
public interface InpatientDischargeMapper {
    @Select("""
            <script>
            SELECT d.id, d.discharge_no, d.admission_id, d.patient_id, p.name AS patient_name,
                   d.total_fee_amount, d.total_deposit_amount, d.total_refund_amount,
                   d.deposit_balance, d.unpaid_amount, d.actual_payment, d.discharge_time,
                   d.operator_name, d.status, d.remark, d.deleted, d.created_at, d.updated_at
            FROM inpatient_discharge d
            LEFT JOIN patients p ON p.id = d.patient_id
            <where>
                d.deleted = false
                <if test="query.dischargeNo != null and query.dischargeNo != ''">AND d.discharge_no ILIKE CONCAT('%', #{query.dischargeNo}, '%')</if>
                <if test="query.admissionId != null">AND d.admission_id = #{query.admissionId}</if>
                <if test="query.patientId != null">AND d.patient_id = #{query.patientId}</if>
                <if test="query.status != null and query.status != ''">AND d.status = #{query.status}</if>
            </where>
            ORDER BY d.id DESC LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<InpatientDischarge> findPage(@Param("query") DischargeQueryRequest query, @Param("offset") int offset, @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM inpatient_discharge d
            <where>
                d.deleted = false
                <if test="query.dischargeNo != null and query.dischargeNo != ''">AND d.discharge_no ILIKE CONCAT('%', #{query.dischargeNo}, '%')</if>
                <if test="query.admissionId != null">AND d.admission_id = #{query.admissionId}</if>
                <if test="query.patientId != null">AND d.patient_id = #{query.patientId}</if>
                <if test="query.status != null and query.status != ''">AND d.status = #{query.status}</if>
            </where>
            </script>
            """)
    long countPage(@Param("query") DischargeQueryRequest query);

    @Select("""
            SELECT d.id, d.discharge_no, d.admission_id, d.patient_id, p.name AS patient_name,
                   d.total_fee_amount, d.total_deposit_amount, d.total_refund_amount,
                   d.deposit_balance, d.unpaid_amount, d.actual_payment, d.discharge_time,
                   d.operator_name, d.status, d.remark, d.deleted, d.created_at, d.updated_at
            FROM inpatient_discharge d
            LEFT JOIN patients p ON p.id = d.patient_id
            WHERE d.id = #{id} AND d.deleted = false
            """)
    InpatientDischarge findById(Long id);

    @Select("SELECT COUNT(1) FROM inpatient_discharge WHERE discharge_no = #{dischargeNo}")
    int countByDischargeNo(String dischargeNo);

    @Select("SELECT COUNT(1) FROM inpatient_discharge WHERE discharge_no = #{dischargeNo} AND id != #{excludeId}")
    int countByDischargeNoExcludeId(@Param("dischargeNo") String dischargeNo, @Param("excludeId") Long excludeId);

    @Select("""
            SELECT COUNT(1)
            FROM inpatient_discharge
            WHERE admission_id = #{admissionId}
              AND status IN ('DRAFT', 'SETTLED')
              AND deleted = false
            """)
    int countActiveByAdmission(Long admissionId);

    @Select("""
            SELECT COUNT(1)
            FROM inpatient_discharge
            WHERE admission_id = #{admissionId}
              AND status IN ('DRAFT', 'SETTLED')
              AND deleted = false
              AND id != #{excludeId}
            """)
    int countActiveByAdmissionExcludeId(@Param("admissionId") Long admissionId, @Param("excludeId") Long excludeId);

    @Insert("""
            INSERT INTO inpatient_discharge (
                discharge_no, admission_id, patient_id, total_fee_amount, total_deposit_amount,
                total_refund_amount, deposit_balance, unpaid_amount, actual_payment,
                discharge_time, operator_name, status, remark, deleted, created_at, updated_at
            ) VALUES (
                #{dischargeNo}, #{admissionId}, #{patientId}, #{totalFeeAmount}, #{totalDepositAmount},
                #{totalRefundAmount}, #{depositBalance}, #{unpaidAmount}, #{actualPayment},
                #{dischargeTime}, #{operatorName}, #{status}, #{remark}, false, NOW(), NOW()
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(InpatientDischarge discharge);

    @Update("""
            UPDATE inpatient_discharge
            SET discharge_no=#{dischargeNo}, admission_id=#{admissionId}, patient_id=#{patientId},
                total_fee_amount=#{totalFeeAmount}, total_deposit_amount=#{totalDepositAmount},
                total_refund_amount=#{totalRefundAmount}, deposit_balance=#{depositBalance},
                unpaid_amount=#{unpaidAmount}, actual_payment=#{actualPayment},
                discharge_time=#{dischargeTime}, operator_name=#{operatorName},
                status=#{status}, remark=#{remark}, updated_at=NOW()
            WHERE id=#{id} AND deleted=false
            """)
    int update(InpatientDischarge discharge);

    @Update("""
            UPDATE inpatient_discharge
            SET status='SETTLED',
                total_fee_amount=#{totalFeeAmount},
                total_deposit_amount=#{totalDepositAmount},
                total_refund_amount=#{totalRefundAmount},
                deposit_balance=#{depositBalance},
                unpaid_amount=#{unpaidAmount},
                actual_payment=#{actualPayment},
                discharge_time=#{dischargeTime},
                operator_name=COALESCE(#{operatorName}, operator_name),
                remark=COALESCE(#{remark}, remark),
                updated_at=NOW()
            WHERE id=#{id} AND status='DRAFT' AND deleted=false
            """)
    int settle(@Param("id") Long id,
               @Param("totalFeeAmount") java.math.BigDecimal totalFeeAmount,
               @Param("totalDepositAmount") java.math.BigDecimal totalDepositAmount,
               @Param("totalRefundAmount") java.math.BigDecimal totalRefundAmount,
               @Param("depositBalance") java.math.BigDecimal depositBalance,
               @Param("unpaidAmount") java.math.BigDecimal unpaidAmount,
               @Param("actualPayment") java.math.BigDecimal actualPayment,
               @Param("dischargeTime") java.time.LocalDateTime dischargeTime,
               @Param("operatorName") String operatorName, @Param("remark") String remark);

    @Update("""
            UPDATE inpatient_discharge
            SET status='CANCELLED', operator_name=COALESCE(#{operatorName}, operator_name),
                remark=COALESCE(#{remark}, remark), updated_at=NOW()
            WHERE id=#{id} AND status='DRAFT' AND deleted=false
            """)
    int cancel(@Param("id") Long id, @Param("operatorName") String operatorName, @Param("remark") String remark);

    @Update("UPDATE inpatient_discharge SET deleted=true, updated_at=NOW() WHERE id=#{id} AND deleted=false")
    int logicDeleteById(Long id);
}
