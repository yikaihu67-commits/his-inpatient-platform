package com.yueshan.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.yueshan.backend.domain.NursingOrderExecution;
import com.yueshan.backend.dto.NursingExecutionQueryRequest;

@Mapper
public interface NursingOrderExecutionMapper {
    @Select("""
            <script>
            SELECT e.id, e.execution_no, e.order_id, o.order_no, e.admission_id, e.patient_id, p.name AS patient_name,
                   e.scheduled_time, e.executed_time, e.nurse_name, e.status, e.result, e.remark,
                   e.deleted, e.created_at, e.updated_at
            FROM nursing_order_execution e
            LEFT JOIN medical_order o ON o.id = e.order_id
            LEFT JOIN patients p ON p.id = e.patient_id
            <where>
                e.deleted = false
                <if test="query.orderId != null">AND e.order_id = #{query.orderId}</if>
                <if test="query.admissionId != null">AND e.admission_id = #{query.admissionId}</if>
                <if test="query.patientId != null">AND e.patient_id = #{query.patientId}</if>
                <if test="query.status != null and query.status != ''">AND e.status = #{query.status}</if>
            </where>
            ORDER BY e.id DESC LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<NursingOrderExecution> findPage(@Param("query") NursingExecutionQueryRequest query, @Param("offset") int offset, @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1) FROM nursing_order_execution e
            <where>
                e.deleted = false
                <if test="query.orderId != null">AND e.order_id = #{query.orderId}</if>
                <if test="query.admissionId != null">AND e.admission_id = #{query.admissionId}</if>
                <if test="query.patientId != null">AND e.patient_id = #{query.patientId}</if>
                <if test="query.status != null and query.status != ''">AND e.status = #{query.status}</if>
            </where>
            </script>
            """)
    long countPage(@Param("query") NursingExecutionQueryRequest query);

    @Select("""
            SELECT e.id, e.execution_no, e.order_id, o.order_no, e.admission_id, e.patient_id, p.name AS patient_name,
                   e.scheduled_time, e.executed_time, e.nurse_name, e.status, e.result, e.remark,
                   e.deleted, e.created_at, e.updated_at
            FROM nursing_order_execution e
            LEFT JOIN medical_order o ON o.id = e.order_id
            LEFT JOIN patients p ON p.id = e.patient_id
            WHERE e.id = #{id} AND e.deleted = false
            """)
    NursingOrderExecution findById(Long id);

    @Select("SELECT COUNT(1) FROM nursing_order_execution WHERE execution_no = #{executionNo}")
    int countByExecutionNo(String executionNo);

    @Insert("""
            INSERT INTO nursing_order_execution (
                execution_no, order_id, admission_id, patient_id, scheduled_time, executed_time,
                nurse_name, status, result, remark, deleted, created_at, updated_at
            ) VALUES (
                #{executionNo}, #{orderId}, #{admissionId}, #{patientId}, #{scheduledTime}, #{executedTime},
                #{nurseName}, #{status}, #{result}, #{remark}, false, NOW(), NOW()
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(NursingOrderExecution execution);

    @Update("""
            UPDATE nursing_order_execution
            SET scheduled_time = #{scheduledTime}, nurse_name = #{nurseName}, result = #{result},
                remark = #{remark}, updated_at = NOW()
            WHERE id = #{id} AND deleted = false
            """)
    int update(NursingOrderExecution execution);

    @Update("""
            UPDATE nursing_order_execution
            SET status = #{status}, executed_time = #{executedTime}, nurse_name = COALESCE(#{nurseName}, nurse_name),
                result = COALESCE(#{result}, result), remark = COALESCE(#{remark}, remark), updated_at = NOW()
            WHERE id = #{id} AND deleted = false
            """)
    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("executedTime") java.time.LocalDateTime executedTime,
                     @Param("nurseName") String nurseName, @Param("result") String result, @Param("remark") String remark);

    @Update("UPDATE nursing_order_execution SET deleted = true, updated_at = NOW() WHERE id = #{id} AND deleted = false")
    int logicDeleteById(Long id);
}
