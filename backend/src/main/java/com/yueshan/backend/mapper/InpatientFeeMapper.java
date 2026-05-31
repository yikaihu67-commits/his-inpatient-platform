package com.yueshan.backend.mapper;

import java.math.BigDecimal;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.yueshan.backend.domain.InpatientFee;
import com.yueshan.backend.dto.FeeCategorySummary;
import com.yueshan.backend.dto.InpatientFeeQueryRequest;

@Mapper
public interface InpatientFeeMapper {
    @Select("""
            <script>
            SELECT f.id, f.fee_no, f.admission_id, f.patient_id, p.name AS patient_name,
                   f.source_type, f.source_id, f.item_code, f.item_name, f.item_category,
                   f.quantity, f.unit, f.unit_price, f.total_amount, f.fee_time,
                   f.status, f.remark, f.deleted, f.created_at, f.updated_at
            FROM inpatient_fee f
            LEFT JOIN patients p ON p.id = f.patient_id
            <where>
                f.deleted = false
                <if test="query.admissionId != null">AND f.admission_id = #{query.admissionId}</if>
                <if test="query.patientId != null">AND f.patient_id = #{query.patientId}</if>
                <if test="query.itemName != null and query.itemName != ''">AND f.item_name ILIKE CONCAT('%', #{query.itemName}, '%')</if>
                <if test="query.itemCategory != null and query.itemCategory != ''">AND f.item_category = #{query.itemCategory}</if>
                <if test="query.status != null and query.status != ''">AND f.status = #{query.status}</if>
            </where>
            ORDER BY f.id DESC LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<InpatientFee> findPage(@Param("query") InpatientFeeQueryRequest query, @Param("offset") int offset, @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM inpatient_fee f
            <where>
                f.deleted = false
                <if test="query.admissionId != null">AND f.admission_id = #{query.admissionId}</if>
                <if test="query.patientId != null">AND f.patient_id = #{query.patientId}</if>
                <if test="query.itemName != null and query.itemName != ''">AND f.item_name ILIKE CONCAT('%', #{query.itemName}, '%')</if>
                <if test="query.itemCategory != null and query.itemCategory != ''">AND f.item_category = #{query.itemCategory}</if>
                <if test="query.status != null and query.status != ''">AND f.status = #{query.status}</if>
            </where>
            </script>
            """)
    long countPage(@Param("query") InpatientFeeQueryRequest query);

    @Select("""
            SELECT f.id, f.fee_no, f.admission_id, f.patient_id, p.name AS patient_name,
                   f.source_type, f.source_id, f.item_code, f.item_name, f.item_category,
                   f.quantity, f.unit, f.unit_price, f.total_amount, f.fee_time,
                   f.status, f.remark, f.deleted, f.created_at, f.updated_at
            FROM inpatient_fee f
            LEFT JOIN patients p ON p.id = f.patient_id
            WHERE f.id = #{id} AND f.deleted = false
            """)
    InpatientFee findById(Long id);

    @Select("""
            SELECT f.id, f.fee_no, f.admission_id, f.patient_id, p.name AS patient_name,
                   f.source_type, f.source_id, f.item_code, f.item_name, f.item_category,
                   f.quantity, f.unit, f.unit_price, f.total_amount, f.fee_time,
                   f.status, f.remark, f.deleted, f.created_at, f.updated_at
            FROM inpatient_fee f
            LEFT JOIN patients p ON p.id = f.patient_id
            WHERE f.source_type = #{sourceType}
              AND f.source_id = #{sourceId}
              AND f.deleted = false
            LIMIT 1
            """)
    InpatientFee findBySource(@Param("sourceType") String sourceType, @Param("sourceId") Long sourceId);

    @Select("SELECT COUNT(1) FROM inpatient_fee WHERE fee_no = #{feeNo}")
    int countByFeeNo(String feeNo);

    @Select("""
            SELECT COUNT(1)
            FROM inpatient_fee
            WHERE admission_id = #{admissionId}
              AND deleted = false
              AND status <> 'CANCELLED'
            """)
    int countActiveByAdmissionId(Long admissionId);

    @Select("SELECT COUNT(1) FROM inpatient_fee WHERE fee_no = #{feeNo} AND id != #{excludeId}")
    int countByFeeNoExcludeId(@Param("feeNo") String feeNo, @Param("excludeId") Long excludeId);

    @Insert("""
            INSERT INTO inpatient_fee (
                fee_no, admission_id, patient_id, source_type, source_id, item_code, item_name,
                item_category, quantity, unit, unit_price, total_amount, fee_time, status,
                remark, deleted, created_at, updated_at
            ) VALUES (
                #{feeNo}, #{admissionId}, #{patientId}, #{sourceType}, #{sourceId}, #{itemCode}, #{itemName},
                #{itemCategory}, #{quantity}, #{unit}, #{unitPrice}, #{totalAmount}, #{feeTime}, #{status},
                #{remark}, false, NOW(), NOW()
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(InpatientFee fee);

    @Update("""
            UPDATE inpatient_fee
            SET fee_no=#{feeNo}, admission_id=#{admissionId}, patient_id=#{patientId},
                source_type=#{sourceType}, source_id=#{sourceId}, item_code=#{itemCode},
                item_name=#{itemName}, item_category=#{itemCategory}, quantity=#{quantity},
                unit=#{unit}, unit_price=#{unitPrice}, total_amount=#{totalAmount},
                fee_time=#{feeTime}, status=#{status}, remark=#{remark}, updated_at=NOW()
            WHERE id=#{id} AND deleted=false
            """)
    int update(InpatientFee fee);

    @Update("UPDATE inpatient_fee SET deleted=true, updated_at=NOW() WHERE id=#{id} AND deleted=false")
    int logicDeleteById(Long id);

    @Update("UPDATE inpatient_fee SET status='CANCELLED', updated_at=NOW() WHERE id=#{id} AND deleted=false")
    int cancel(Long id);

    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM inpatient_fee WHERE admission_id=#{admissionId} AND deleted=false")
    BigDecimal sumAllByAdmission(Long admissionId);

    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM inpatient_fee WHERE admission_id=#{admissionId} AND status=#{status} AND deleted=false")
    BigDecimal sumByAdmissionAndStatus(@Param("admissionId") Long admissionId, @Param("status") String status);

    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM inpatient_fee WHERE admission_id=#{admissionId} AND status != 'CANCELLED' AND deleted=false")
    BigDecimal sumNotCancelledByAdmission(Long admissionId);

    @Select("""
            SELECT item_category, COALESCE(SUM(total_amount), 0) AS amount
            FROM inpatient_fee
            WHERE admission_id = #{admissionId}
              AND status != 'CANCELLED'
              AND deleted = false
            GROUP BY item_category
            ORDER BY item_category
            """)
    List<FeeCategorySummary> sumCategoryByAdmission(Long admissionId);

    @Update("""
            UPDATE inpatient_fee
            SET status='SETTLED', updated_at=NOW()
            WHERE admission_id=#{admissionId}
              AND status='UNSETTLED'
              AND deleted=false
            """)
    int settleUnsettledByAdmission(Long admissionId);
}
