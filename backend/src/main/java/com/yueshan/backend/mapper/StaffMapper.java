package com.yueshan.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.yueshan.backend.domain.Staff;
import com.yueshan.backend.dto.StaffQueryRequest;

@Mapper
public interface StaffMapper {
    @Select("""
            <script>
            SELECT s.id, s.staff_no, s.staff_name, s.gender, s.phone, s.department_id,
                   d.dept_name AS department_name, s.role_type, s.title, s.status,
                   s.remark, s.deleted, s.created_at, s.updated_at
            FROM staff s
            LEFT JOIN department d ON d.id = s.department_id
            <where>
                s.deleted = false
                <if test="query.staffNo != null and query.staffNo != ''">AND s.staff_no ILIKE CONCAT('%', #{query.staffNo}, '%')</if>
                <if test="query.staffName != null and query.staffName != ''">AND s.staff_name ILIKE CONCAT('%', #{query.staffName}, '%')</if>
                <if test="query.departmentId != null">AND s.department_id = #{query.departmentId}</if>
                <if test="query.roleType != null and query.roleType != ''">AND s.role_type = #{query.roleType}</if>
                <if test="query.status != null and query.status != ''">AND s.status = #{query.status}</if>
            </where>
            ORDER BY s.id DESC LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<Staff> findPage(@Param("query") StaffQueryRequest query, @Param("offset") int offset, @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM staff s
            <where>
                s.deleted = false
                <if test="query.staffNo != null and query.staffNo != ''">AND s.staff_no ILIKE CONCAT('%', #{query.staffNo}, '%')</if>
                <if test="query.staffName != null and query.staffName != ''">AND s.staff_name ILIKE CONCAT('%', #{query.staffName}, '%')</if>
                <if test="query.departmentId != null">AND s.department_id = #{query.departmentId}</if>
                <if test="query.roleType != null and query.roleType != ''">AND s.role_type = #{query.roleType}</if>
                <if test="query.status != null and query.status != ''">AND s.status = #{query.status}</if>
            </where>
            </script>
            """)
    long countPage(@Param("query") StaffQueryRequest query);

    @Select("""
            SELECT s.id, s.staff_no, s.staff_name, s.gender, s.phone, s.department_id,
                   d.dept_name AS department_name, s.role_type, s.title, s.status,
                   s.remark, s.deleted, s.created_at, s.updated_at
            FROM staff s
            LEFT JOIN department d ON d.id = s.department_id
            WHERE s.id=#{id} AND s.deleted=false
            """)
    Staff findById(Long id);

    @Select("SELECT COUNT(1) FROM staff WHERE staff_no=#{staffNo} AND deleted=false")
    int countByStaffNo(String staffNo);

    @Select("SELECT COUNT(1) FROM staff WHERE staff_no=#{staffNo} AND id != #{excludeId} AND deleted=false")
    int countByStaffNoExcludeId(@Param("staffNo") String staffNo, @Param("excludeId") Long excludeId);

    @Insert("""
            INSERT INTO staff (staff_no, staff_name, gender, phone, department_id, role_type, title, status, remark, deleted, created_at, updated_at)
            VALUES (#{staffNo}, #{staffName}, #{gender}, #{phone}, #{departmentId}, #{roleType}, #{title}, #{status}, #{remark}, false, NOW(), NOW())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(Staff staff);

    @Update("""
            UPDATE staff
            SET staff_no=#{staffNo}, staff_name=#{staffName}, gender=#{gender}, phone=#{phone},
                department_id=#{departmentId}, role_type=#{roleType}, title=#{title}, status=#{status},
                remark=#{remark}, updated_at=NOW()
            WHERE id=#{id} AND deleted=false
            """)
    int update(Staff staff);

    @Update("UPDATE staff SET status=#{status}, updated_at=NOW() WHERE id=#{id} AND deleted=false")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Update("UPDATE staff SET deleted=true, updated_at=NOW() WHERE id=#{id} AND deleted=false")
    int logicDeleteById(Long id);
}
