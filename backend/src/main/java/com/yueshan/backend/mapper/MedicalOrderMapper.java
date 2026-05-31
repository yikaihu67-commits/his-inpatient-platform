package com.yueshan.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.yueshan.backend.domain.MedicalOrder;
import com.yueshan.backend.dto.MedicalOrderQueryRequest;

@Mapper
public interface MedicalOrderMapper {

    @Select("""
            <script>
            SELECT o.id, o.order_no, o.admission_id, a.admission_no, o.patient_id, p.name AS patient_name,
                   o.order_type, o.order_category, o.item_name, o.dosage, o.dosage_unit,
                   o.frequency, o.route, o.start_time, o.end_time, o.doctor_name, o.nurse_name,
                   o.order_content, o.execution_department, o.executor_name, o.executed_time,
                   o.status, o.unit_price, o.quantity, o.total_amount, o.billed,
                   o.remark, o.deleted, o.created_at, o.updated_at
            FROM medical_order o
            LEFT JOIN inpatient_admission a ON a.id = o.admission_id
            LEFT JOIN patients p ON p.id = o.patient_id
            <where>
                o.deleted = false
                <if test="query.admissionId != null">
                    AND o.admission_id = #{query.admissionId}
                </if>
                <if test="query.patientId != null">
                    AND o.patient_id = #{query.patientId}
                </if>
                <if test="query.orderType != null and query.orderType != ''">
                    AND o.order_type = #{query.orderType}
                </if>
                <if test="query.orderCategory != null and query.orderCategory != ''">
                    AND o.order_category = #{query.orderCategory}
                </if>
                <if test="query.status != null and query.status != ''">
                    AND o.status = #{query.status}
                </if>
                <if test="query.itemName != null and query.itemName != ''">
                    AND o.item_name ILIKE CONCAT('%', #{query.itemName}, '%')
                </if>
            </where>
            ORDER BY o.id DESC
            LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<MedicalOrder> findPage(
            @Param("query") MedicalOrderQueryRequest query,
            @Param("offset") int offset,
            @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM medical_order o
            <where>
                o.deleted = false
                <if test="query.admissionId != null">
                    AND o.admission_id = #{query.admissionId}
                </if>
                <if test="query.patientId != null">
                    AND o.patient_id = #{query.patientId}
                </if>
                <if test="query.orderType != null and query.orderType != ''">
                    AND o.order_type = #{query.orderType}
                </if>
                <if test="query.orderCategory != null and query.orderCategory != ''">
                    AND o.order_category = #{query.orderCategory}
                </if>
                <if test="query.status != null and query.status != ''">
                    AND o.status = #{query.status}
                </if>
                <if test="query.itemName != null and query.itemName != ''">
                    AND o.item_name ILIKE CONCAT('%', #{query.itemName}, '%')
                </if>
            </where>
            </script>
            """)
    long countPage(@Param("query") MedicalOrderQueryRequest query);

    @Select("""
            SELECT o.id, o.order_no, o.admission_id, a.admission_no, o.patient_id, p.name AS patient_name,
                   o.order_type, o.order_category, o.item_name, o.dosage, o.dosage_unit,
                   o.frequency, o.route, o.start_time, o.end_time, o.doctor_name, o.nurse_name,
                   o.order_content, o.execution_department, o.executor_name, o.executed_time,
                   o.status, o.unit_price, o.quantity, o.total_amount, o.billed,
                   o.remark, o.deleted, o.created_at, o.updated_at
            FROM medical_order o
            LEFT JOIN inpatient_admission a ON a.id = o.admission_id
            LEFT JOIN patients p ON p.id = o.patient_id
            WHERE o.id = #{id}
              AND o.deleted = false
            """)
    MedicalOrder findById(Long id);

    @Select("""
            SELECT o.id, o.order_no, o.admission_id, a.admission_no, o.patient_id, p.name AS patient_name,
                   o.order_type, o.order_category, o.item_name, o.dosage, o.dosage_unit,
                   o.frequency, o.route, o.start_time, o.end_time, o.doctor_name, o.nurse_name,
                   o.order_content, o.execution_department, o.executor_name, o.executed_time,
                   o.status, o.unit_price, o.quantity, o.total_amount, o.billed,
                   o.remark, o.deleted, o.created_at, o.updated_at
            FROM medical_order o
            LEFT JOIN inpatient_admission a ON a.id = o.admission_id
            LEFT JOIN patients p ON p.id = o.patient_id
            WHERE o.order_no = #{orderNo}
              AND o.deleted = false
            """)
    MedicalOrder findByOrderNo(String orderNo);

    @Select("SELECT COUNT(1) FROM medical_order WHERE order_no = #{orderNo}")
    int countByOrderNo(String orderNo);

    @Select("""
            SELECT COUNT(1)
            FROM medical_order
            WHERE admission_id = #{admissionId}
              AND deleted = false
            """)
    int countByAdmissionId(Long admissionId);

    @Select("""
            SELECT COUNT(1)
            FROM medical_order
            WHERE order_no = #{orderNo}
              AND id <> #{excludeId}
            """)
    int countByOrderNoExcludeId(@Param("orderNo") String orderNo, @Param("excludeId") Long excludeId);

    @Insert("""
            INSERT INTO medical_order (
                order_no, admission_id, patient_id, order_type, order_category, order_content, item_name,
                dosage, dosage_unit, frequency, route, start_time, end_time, doctor_name,
                execution_department, executor_name, nurse_name, status, unit_price, quantity, total_amount, billed,
                remark, deleted, created_at, updated_at
            )
            VALUES (
                #{orderNo}, #{admissionId}, #{patientId}, #{orderType}, #{orderCategory}, #{orderContent}, #{itemName},
                #{dosage}, #{dosageUnit}, #{frequency}, #{route}, #{startTime}, #{endTime}, #{doctorName},
                #{executionDepartment}, #{executorName}, #{nurseName}, #{status}, #{unitPrice}, #{quantity}, #{totalAmount}, #{billed},
                #{remark}, false, NOW(), NOW()
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(MedicalOrder order);

    @Update("""
            UPDATE medical_order
            SET order_no = #{orderNo},
                admission_id = #{admissionId},
                patient_id = #{patientId},
                order_type = #{orderType},
                order_category = #{orderCategory},
                order_content = #{orderContent},
                item_name = #{itemName},
                dosage = #{dosage},
                dosage_unit = #{dosageUnit},
                frequency = #{frequency},
                route = #{route},
                start_time = #{startTime},
                end_time = #{endTime},
                doctor_name = #{doctorName},
                execution_department = #{executionDepartment},
                executor_name = #{executorName},
                nurse_name = #{nurseName},
                unit_price = #{unitPrice},
                quantity = #{quantity},
                total_amount = #{totalAmount},
                remark = #{remark},
                updated_at = NOW()
            WHERE id = #{id}
              AND deleted = false
            """)
    int update(MedicalOrder order);

    @Update("""
            UPDATE medical_order
            SET status = #{status},
                nurse_name = COALESCE(#{nurseName}, nurse_name),
                executor_name = COALESCE(#{nurseName}, executor_name),
                executed_time = CASE WHEN #{status} = 'EXECUTED' THEN NOW() ELSE executed_time END,
                remark = COALESCE(#{remark}, remark),
                updated_at = NOW()
            WHERE id = #{id}
              AND deleted = false
            """)
    int updateStatus(
            @Param("id") Long id,
            @Param("status") String status,
            @Param("nurseName") String nurseName,
            @Param("remark") String remark);

    @Update("""
            UPDATE medical_order
            SET status = 'BILLED',
                billed = true,
                remark = COALESCE(#{remark}, remark),
                updated_at = NOW()
            WHERE id = #{id}
              AND deleted = false
            """)
    int markBilled(@Param("id") Long id, @Param("remark") String remark);

    @Update("""
            UPDATE medical_order
            SET deleted = true,
                updated_at = NOW()
            WHERE id = #{id}
              AND deleted = false
            """)
    int logicDeleteById(Long id);
}
