package com.yueshan.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.yueshan.backend.domain.Drug;
import com.yueshan.backend.dto.DrugQueryRequest;

@Mapper
public interface DrugMapper {
    @Select("""
            <script>
            SELECT id, drug_code, drug_name, specification, unit, price, stock_quantity, stock_lower_limit, status, deleted, created_at, updated_at
            FROM drug
            <where>
                deleted = false
                <if test="query.drugCode != null and query.drugCode != ''">AND drug_code ILIKE CONCAT('%', #{query.drugCode}, '%')</if>
                <if test="query.drugName != null and query.drugName != ''">AND drug_name ILIKE CONCAT('%', #{query.drugName}, '%')</if>
                <if test="query.status != null and query.status != ''">AND status = #{query.status}</if>
            </where>
            ORDER BY id DESC LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<Drug> findPage(@Param("query") DrugQueryRequest query, @Param("offset") int offset, @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1) FROM drug
            <where>
                deleted = false
                <if test="query.drugCode != null and query.drugCode != ''">AND drug_code ILIKE CONCAT('%', #{query.drugCode}, '%')</if>
                <if test="query.drugName != null and query.drugName != ''">AND drug_name ILIKE CONCAT('%', #{query.drugName}, '%')</if>
                <if test="query.status != null and query.status != ''">AND status = #{query.status}</if>
            </where>
            </script>
            """)
    long countPage(@Param("query") DrugQueryRequest query);

    @Select("SELECT id, drug_code, drug_name, specification, unit, price, stock_quantity, stock_lower_limit, status, deleted, created_at, updated_at FROM drug WHERE id = #{id} AND deleted = false")
    Drug findById(Long id);

    @Select("SELECT COUNT(1) FROM drug WHERE drug_code = #{drugCode} AND deleted = false")
    int countByDrugCode(String drugCode);

    @Select("SELECT COUNT(1) FROM drug WHERE drug_code = #{drugCode} AND id <> #{excludeId} AND deleted = false")
    int countByDrugCodeExcludeId(@Param("drugCode") String drugCode, @Param("excludeId") Long excludeId);

    @Insert("""
            INSERT INTO drug (drug_code, drug_name, specification, unit, price, stock_quantity, stock_lower_limit, status, deleted, created_at, updated_at)
            VALUES (#{drugCode}, #{drugName}, #{specification}, #{unit}, #{price}, #{stockQuantity}, #{stockLowerLimit}, #{status}, false, NOW(), NOW())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(Drug drug);

    @Update("""
            UPDATE drug SET drug_code=#{drugCode}, drug_name=#{drugName}, specification=#{specification}, unit=#{unit},
                price=#{price}, stock_quantity=#{stockQuantity}, stock_lower_limit=#{stockLowerLimit}, status=#{status}, updated_at=NOW()
            WHERE id=#{id} AND deleted=false
            """)
    int update(Drug drug);

    @Update("UPDATE drug SET stock_quantity = stock_quantity - #{quantity}, updated_at = NOW() WHERE id = #{id} AND deleted = false AND stock_quantity >= #{quantity}")
    int decreaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    @Update("UPDATE drug SET stock_quantity = stock_quantity + #{quantity}, updated_at = NOW() WHERE id = #{id} AND deleted = false")
    int increaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    @Update("UPDATE drug SET deleted = true, updated_at = NOW() WHERE id = #{id} AND deleted = false")
    int logicDeleteById(Long id);
}
