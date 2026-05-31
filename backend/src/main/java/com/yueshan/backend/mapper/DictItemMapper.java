package com.yueshan.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.yueshan.backend.domain.DictItem;
import com.yueshan.backend.dto.DictItemQueryRequest;

@Mapper
public interface DictItemMapper {
    @Select("""
            <script>
            SELECT id, dict_type, dict_code, dict_name, sort_order, status, remark, deleted, created_at, updated_at
            FROM dict_item
            <where>
                deleted = false
                <if test="query.dictType != null and query.dictType != ''">AND dict_type ILIKE CONCAT('%', #{query.dictType}, '%')</if>
                <if test="query.dictCode != null and query.dictCode != ''">AND dict_code ILIKE CONCAT('%', #{query.dictCode}, '%')</if>
                <if test="query.dictName != null and query.dictName != ''">AND dict_name ILIKE CONCAT('%', #{query.dictName}, '%')</if>
                <if test="query.status != null and query.status != ''">AND status = #{query.status}</if>
            </where>
            ORDER BY dict_type ASC, sort_order ASC, id ASC LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<DictItem> findPage(@Param("query") DictItemQueryRequest query, @Param("offset") int offset, @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM dict_item
            <where>
                deleted = false
                <if test="query.dictType != null and query.dictType != ''">AND dict_type ILIKE CONCAT('%', #{query.dictType}, '%')</if>
                <if test="query.dictCode != null and query.dictCode != ''">AND dict_code ILIKE CONCAT('%', #{query.dictCode}, '%')</if>
                <if test="query.dictName != null and query.dictName != ''">AND dict_name ILIKE CONCAT('%', #{query.dictName}, '%')</if>
                <if test="query.status != null and query.status != ''">AND status = #{query.status}</if>
            </where>
            </script>
            """)
    long countPage(@Param("query") DictItemQueryRequest query);

    @Select("SELECT id, dict_type, dict_code, dict_name, sort_order, status, remark, deleted, created_at, updated_at FROM dict_item WHERE id=#{id} AND deleted=false")
    DictItem findById(Long id);

    @Select("""
            SELECT id, dict_type, dict_code, dict_name, sort_order, status, remark, deleted, created_at, updated_at
            FROM dict_item
            WHERE dict_type=#{dictType} AND status='ENABLED' AND deleted=false
            ORDER BY sort_order ASC, id ASC
            """)
    List<DictItem> findEnabledByType(String dictType);

    @Select("SELECT COUNT(1) FROM dict_item WHERE dict_type=#{dictType} AND dict_code=#{dictCode} AND deleted=false")
    int countByTypeAndCode(@Param("dictType") String dictType, @Param("dictCode") String dictCode);

    @Select("SELECT COUNT(1) FROM dict_item WHERE dict_type=#{dictType} AND dict_code=#{dictCode} AND id != #{excludeId} AND deleted=false")
    int countByTypeAndCodeExcludeId(@Param("dictType") String dictType, @Param("dictCode") String dictCode, @Param("excludeId") Long excludeId);

    @Insert("""
            INSERT INTO dict_item (dict_type, dict_code, dict_name, sort_order, status, remark, deleted, created_at, updated_at)
            VALUES (#{dictType}, #{dictCode}, #{dictName}, #{sortOrder}, #{status}, #{remark}, false, NOW(), NOW())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(DictItem item);

    @Update("""
            UPDATE dict_item
            SET dict_type=#{dictType}, dict_code=#{dictCode}, dict_name=#{dictName}, sort_order=#{sortOrder},
                status=#{status}, remark=#{remark}, updated_at=NOW()
            WHERE id=#{id} AND deleted=false
            """)
    int update(DictItem item);

    @Update("UPDATE dict_item SET deleted=true, updated_at=NOW() WHERE id=#{id} AND deleted=false")
    int logicDeleteById(Long id);
}
