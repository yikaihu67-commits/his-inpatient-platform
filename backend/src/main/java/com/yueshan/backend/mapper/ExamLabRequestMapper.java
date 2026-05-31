package com.yueshan.backend.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.yueshan.backend.domain.ExamLabRequest;
import com.yueshan.backend.dto.ExamLabRequestQueryRequest;

@Mapper
public interface ExamLabRequestMapper {
    String COLUMNS = """
             r.id, r.request_no, r.admission_id, r.patient_id, p.name AS patient_name,
             r.request_type, r.item_code, r.item_name, r.request_content, r.item_category, r.doctor_name,
             r.request_time, r.scheduled_time, r.execution_department, r.executor_name, r.executed_time,
             r.report_doctor_name, r.report_time, r.result_summary, r.result_detail, r.abnormal_flag,
             r.status, r.unit_price, r.quantity, r.total_amount, r.billed,
             r.remark, r.deleted, r.created_at, r.updated_at
            """;

    @Select("""
            <script>
            SELECT """ + COLUMNS + """
            FROM exam_lab_request r
            LEFT JOIN patients p ON p.id = r.patient_id
            <where>
                r.deleted = false
                <if test="query.admissionId != null">AND r.admission_id = #{query.admissionId}</if>
                <if test="query.patientId != null">AND r.patient_id = #{query.patientId}</if>
                <if test="query.requestType != null and query.requestType != ''">AND r.request_type = #{query.requestType}</if>
                <if test="query.itemCategory != null and query.itemCategory != ''">AND r.item_category = #{query.itemCategory}</if>
                <if test="query.status != null and query.status != ''">AND r.status = #{query.status}</if>
                <if test="query.itemName != null and query.itemName != ''">AND r.item_name ILIKE CONCAT('%', #{query.itemName}, '%')</if>
            </where>
            ORDER BY r.id DESC LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<ExamLabRequest> findPage(@Param("query") ExamLabRequestQueryRequest query, @Param("offset") int offset, @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM exam_lab_request r
            <where>
                r.deleted = false
                <if test="query.admissionId != null">AND r.admission_id = #{query.admissionId}</if>
                <if test="query.patientId != null">AND r.patient_id = #{query.patientId}</if>
                <if test="query.requestType != null and query.requestType != ''">AND r.request_type = #{query.requestType}</if>
                <if test="query.itemCategory != null and query.itemCategory != ''">AND r.item_category = #{query.itemCategory}</if>
                <if test="query.status != null and query.status != ''">AND r.status = #{query.status}</if>
                <if test="query.itemName != null and query.itemName != ''">AND r.item_name ILIKE CONCAT('%', #{query.itemName}, '%')</if>
            </where>
            </script>
            """)
    long countPage(@Param("query") ExamLabRequestQueryRequest query);

    @Select("""
            SELECT """ + COLUMNS + """
            FROM exam_lab_request r
            LEFT JOIN patients p ON p.id = r.patient_id
            WHERE r.id = #{id} AND r.deleted = false
            """)
    ExamLabRequest findById(Long id);

    @Select("SELECT COUNT(1) FROM exam_lab_request WHERE request_no = #{requestNo}")
    int countByRequestNo(String requestNo);

    @Select("SELECT COUNT(1) FROM exam_lab_request WHERE request_no = #{requestNo} AND id != #{excludeId}")
    int countByRequestNoExcludeId(@Param("requestNo") String requestNo, @Param("excludeId") Long excludeId);

    @Insert("""
            INSERT INTO exam_lab_request (
                request_no, admission_id, patient_id, request_type, item_code, item_name, request_content,
                item_category, doctor_name, request_time, scheduled_time, execution_department,
                result_summary, result_detail, abnormal_flag, status, unit_price, quantity, total_amount, billed,
                remark, deleted, created_at, updated_at
            ) VALUES (
                #{requestNo}, #{admissionId}, #{patientId}, #{requestType}, #{itemCode}, #{itemName}, #{requestContent},
                #{itemCategory}, #{doctorName}, #{requestTime}, #{scheduledTime}, #{executionDepartment},
                #{resultSummary}, #{resultDetail}, #{abnormalFlag}, #{status}, #{unitPrice}, #{quantity}, #{totalAmount}, #{billed},
                #{remark}, false, NOW(), NOW()
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(ExamLabRequest request);

    @Update("""
            UPDATE exam_lab_request
            SET request_no=#{requestNo}, admission_id=#{admissionId}, patient_id=#{patientId},
                request_type=#{requestType}, item_code=#{itemCode}, item_name=#{itemName}, request_content=#{requestContent},
                item_category=#{itemCategory}, doctor_name=#{doctorName}, request_time=#{requestTime},
                scheduled_time=#{scheduledTime}, execution_department=#{executionDepartment},
                result_summary=#{resultSummary}, result_detail=#{resultDetail}, abnormal_flag=#{abnormalFlag},
                status=#{status}, unit_price=#{unitPrice}, quantity=#{quantity}, total_amount=#{totalAmount},
                billed=#{billed}, remark=#{remark}, updated_at=NOW()
            WHERE id=#{id} AND deleted=false
            """)
    int update(ExamLabRequest request);

    @Update("""
            UPDATE exam_lab_request
            SET status=#{status},
                scheduled_time=COALESCE(#{scheduledTime}, scheduled_time),
                execution_department=COALESCE(#{executionDepartment}, execution_department),
                executor_name=COALESCE(#{executorName}, executor_name),
                executed_time=COALESCE(#{executedTime}, executed_time),
                report_doctor_name=COALESCE(#{reportDoctorName}, report_doctor_name),
                report_time=COALESCE(#{reportTime}, report_time),
                result_summary=COALESCE(#{resultSummary}, result_summary),
                result_detail=COALESCE(#{resultDetail}, result_detail),
                abnormal_flag=COALESCE(#{abnormalFlag}, abnormal_flag),
                remark=COALESCE(#{remark}, remark), updated_at=NOW()
            WHERE id=#{id} AND deleted=false
            """)
    int updateStatus(@Param("id") Long id,
                     @Param("status") String status,
                     @Param("scheduledTime") LocalDateTime scheduledTime,
                     @Param("executionDepartment") String executionDepartment,
                     @Param("executorName") String executorName,
                     @Param("executedTime") LocalDateTime executedTime,
                     @Param("reportDoctorName") String reportDoctorName,
                     @Param("reportTime") LocalDateTime reportTime,
                     @Param("resultSummary") String resultSummary,
                     @Param("resultDetail") String resultDetail,
                     @Param("abnormalFlag") Boolean abnormalFlag,
                     @Param("remark") String remark);

    @Update("""
            UPDATE exam_lab_request
            SET status='BILLED', billed=true, updated_at=NOW()
            WHERE id=#{id} AND deleted=false
            """)
    int markBilled(Long id);

    @Update("UPDATE exam_lab_request SET deleted=true, updated_at=NOW() WHERE id=#{id} AND deleted=false")
    int logicDeleteById(Long id);
}
