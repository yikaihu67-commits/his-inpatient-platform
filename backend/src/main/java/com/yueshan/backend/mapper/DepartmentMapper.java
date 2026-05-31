package com.yueshan.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.yueshan.backend.domain.Department;
import com.yueshan.backend.dto.DepartmentQueryRequest;

@Mapper
public interface DepartmentMapper {
    @Select("""
            <script>
            SELECT id, dept_code, dept_name, dept_type, parent_id, status, remark, deleted, created_at, updated_at
            FROM department
            <where>
                deleted = false
                <if test="query.deptCode != null and query.deptCode != ''">AND dept_code ILIKE CONCAT('%', #{query.deptCode}, '%')</if>
                <if test="query.deptName != null and query.deptName != ''">AND dept_name ILIKE CONCAT('%', #{query.deptName}, '%')</if>
                <if test="query.deptType != null and query.deptType != ''">AND dept_type = #{query.deptType}</if>
                <if test="query.status != null and query.status != ''">AND status = #{query.status}</if>
            </where>
            ORDER BY id DESC LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<Department> findPage(@Param("query") DepartmentQueryRequest query, @Param("offset") int offset, @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM department
            <where>
                deleted = false
                <if test="query.deptCode != null and query.deptCode != ''">AND dept_code ILIKE CONCAT('%', #{query.deptCode}, '%')</if>
                <if test="query.deptName != null and query.deptName != ''">AND dept_name ILIKE CONCAT('%', #{query.deptName}, '%')</if>
                <if test="query.deptType != null and query.deptType != ''">AND dept_type = #{query.deptType}</if>
                <if test="query.status != null and query.status != ''">AND status = #{query.status}</if>
            </where>
            </script>
            """)
    long countPage(@Param("query") DepartmentQueryRequest query);

    @Select("SELECT id, dept_code, dept_name, dept_type, parent_id, status, remark, deleted, created_at, updated_at FROM department WHERE id=#{id} AND deleted=false")
    Department findById(Long id);

    @Select("SELECT COUNT(1) FROM department WHERE dept_code=#{deptCode} AND deleted=false")
    int countByDeptCode(String deptCode);

    @Select("SELECT COUNT(1) FROM department WHERE dept_code=#{deptCode} AND id != #{excludeId} AND deleted=false")
    int countByDeptCodeExcludeId(@Param("deptCode") String deptCode, @Param("excludeId") Long excludeId);

    @Insert("""
            INSERT INTO department (dept_code, dept_name, dept_type, parent_id, status, remark, deleted, created_at, updated_at)
            VALUES (#{deptCode}, #{deptName}, #{deptType}, #{parentId}, #{status}, #{remark}, false, NOW(), NOW())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(Department department);

    @Update("""
            UPDATE department
            SET dept_code=#{deptCode}, dept_name=#{deptName}, dept_type=#{deptType}, parent_id=#{parentId},
                status=#{status}, remark=#{remark}, updated_at=NOW()
            WHERE id=#{id} AND deleted=false
            """)
    int update(Department department);

    @Update("UPDATE department SET status=#{status}, updated_at=NOW() WHERE id=#{id} AND deleted=false")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Update("UPDATE department SET deleted=true, updated_at=NOW() WHERE id=#{id} AND deleted=false")
    int logicDeleteById(Long id);
}
