package com.yueshan.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.yueshan.backend.domain.OperationLog;
import com.yueshan.backend.dto.OperationLogQueryRequest;

@Mapper
public interface OperationLogMapper {
    @Select("""
            <script>
            SELECT id, module_name, operation_type, business_id, business_no, operator_name,
                   request_method, request_uri, request_params, result_status, error_message,
                   operation_time, created_at
            FROM operation_log
            <where>
                <if test="query.moduleName != null and query.moduleName != ''">AND module_name ILIKE CONCAT('%', #{query.moduleName}, '%')</if>
                <if test="query.operationType != null and query.operationType != ''">AND operation_type = #{query.operationType}</if>
                <if test="query.operatorName != null and query.operatorName != ''">AND operator_name ILIKE CONCAT('%', #{query.operatorName}, '%')</if>
                <if test="query.businessId != null">AND business_id = #{query.businessId}</if>
                <if test="query.resultStatus != null and query.resultStatus != ''">AND result_status = #{query.resultStatus}</if>
            </where>
            ORDER BY id DESC LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<OperationLog> findPage(@Param("query") OperationLogQueryRequest query, @Param("offset") int offset, @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM operation_log
            <where>
                <if test="query.moduleName != null and query.moduleName != ''">AND module_name ILIKE CONCAT('%', #{query.moduleName}, '%')</if>
                <if test="query.operationType != null and query.operationType != ''">AND operation_type = #{query.operationType}</if>
                <if test="query.operatorName != null and query.operatorName != ''">AND operator_name ILIKE CONCAT('%', #{query.operatorName}, '%')</if>
                <if test="query.businessId != null">AND business_id = #{query.businessId}</if>
                <if test="query.resultStatus != null and query.resultStatus != ''">AND result_status = #{query.resultStatus}</if>
            </where>
            </script>
            """)
    long countPage(@Param("query") OperationLogQueryRequest query);

    @Select("""
            SELECT id, module_name, operation_type, business_id, business_no, operator_name,
                   request_method, request_uri, request_params, result_status, error_message,
                   operation_time, created_at
            FROM operation_log
            WHERE id=#{id}
            """)
    OperationLog findById(Long id);

    @Insert("""
            INSERT INTO operation_log (
                module_name, operation_type, business_id, business_no, operator_name,
                request_method, request_uri, request_params, result_status, error_message,
                operation_time, created_at
            ) VALUES (
                #{moduleName}, #{operationType}, #{businessId}, #{businessNo}, #{operatorName},
                #{requestMethod}, #{requestUri}, #{requestParams}, #{resultStatus}, #{errorMessage},
                #{operationTime}, NOW()
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(OperationLog log);

    @Delete("DELETE FROM operation_log WHERE id=#{id}")
    int deleteById(Long id);
}
