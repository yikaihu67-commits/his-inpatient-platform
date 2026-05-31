package com.yueshan.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.yueshan.backend.domain.Bed;
import com.yueshan.backend.dto.BedQueryRequest;

@Mapper
public interface BedMapper {

    @Select("""
            <script>
            SELECT b.id, b.bed_no, b.ward_name, b.room_no, b.bed_type, b.status,
                   b.current_admission_id, b.deleted, a.admission_no, p.name AS patient_name,
                   b.created_at, b.updated_at
            FROM bed b
            LEFT JOIN inpatient_admission a ON a.id = b.current_admission_id
            LEFT JOIN patients p ON p.id = a.patient_id
            <where>
                b.deleted = false
                <if test="query.bedNo != null and query.bedNo != ''">
                    AND b.bed_no ILIKE CONCAT('%', #{query.bedNo}, '%')
                </if>
                <if test="query.wardName != null and query.wardName != ''">
                    AND b.ward_name ILIKE CONCAT('%', #{query.wardName}, '%')
                </if>
                <if test="query.roomNo != null and query.roomNo != ''">
                    AND b.room_no ILIKE CONCAT('%', #{query.roomNo}, '%')
                </if>
                <if test="query.status != null and query.status != ''">
                    AND b.status = #{query.status}
                </if>
            </where>
            ORDER BY b.id DESC
            LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<Bed> findPage(
            @Param("query") BedQueryRequest query,
            @Param("offset") int offset,
            @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM bed b
            <where>
                b.deleted = false
                <if test="query.bedNo != null and query.bedNo != ''">
                    AND b.bed_no ILIKE CONCAT('%', #{query.bedNo}, '%')
                </if>
                <if test="query.wardName != null and query.wardName != ''">
                    AND b.ward_name ILIKE CONCAT('%', #{query.wardName}, '%')
                </if>
                <if test="query.roomNo != null and query.roomNo != ''">
                    AND b.room_no ILIKE CONCAT('%', #{query.roomNo}, '%')
                </if>
                <if test="query.status != null and query.status != ''">
                    AND b.status = #{query.status}
                </if>
            </where>
            </script>
            """)
    long countPage(@Param("query") BedQueryRequest query);

    @Select("""
            SELECT b.id, b.bed_no, b.ward_name, b.room_no, b.bed_type, b.status,
                   b.current_admission_id, b.deleted, a.admission_no, p.name AS patient_name,
                   b.created_at, b.updated_at
            FROM bed b
            LEFT JOIN inpatient_admission a ON a.id = b.current_admission_id
            LEFT JOIN patients p ON p.id = a.patient_id
            WHERE b.id = #{id}
              AND b.deleted = false
            """)
    Bed findById(Long id);

    @Select("""
            SELECT COUNT(1)
            FROM bed
            WHERE bed_no = #{bedNo}
              AND ward_name = #{wardName}
              AND deleted = false
            """)
    int countByBedNoAndWardName(@Param("bedNo") String bedNo, @Param("wardName") String wardName);

    @Select("""
            SELECT COUNT(1)
            FROM bed
            WHERE bed_no = #{bedNo}
              AND ward_name = #{wardName}
              AND deleted = false
              AND id <> #{excludeId}
            """)
    int countByBedNoAndWardNameExcludeId(
            @Param("bedNo") String bedNo,
            @Param("wardName") String wardName,
            @Param("excludeId") Long excludeId);

    @Select("""
            SELECT COUNT(1)
            FROM bed
            WHERE current_admission_id = #{admissionId}
              AND status = 'OCCUPIED'
              AND deleted = false
            """)
    int countOccupiedByAdmissionId(Long admissionId);

    @Select("""
            SELECT id, bed_no, ward_name, room_no, bed_type, status, current_admission_id,
                   deleted, created_at, updated_at
            FROM bed
            WHERE current_admission_id = #{admissionId}
              AND status = 'OCCUPIED'
              AND deleted = false
            LIMIT 1
            """)
    Bed findOccupiedByAdmissionId(Long admissionId);

    @Insert("""
            INSERT INTO bed (bed_no, ward_name, room_no, bed_type, status, current_admission_id, deleted, created_at, updated_at)
            VALUES (#{bedNo}, #{wardName}, #{roomNo}, #{bedType}, #{status}, #{currentAdmissionId}, false, NOW(), NOW())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(Bed bed);

    @Update("""
            UPDATE bed
            SET bed_no = #{bedNo},
                ward_name = #{wardName},
                room_no = #{roomNo},
                bed_type = #{bedType},
                status = #{status},
                current_admission_id = #{currentAdmissionId},
                updated_at = NOW()
            WHERE id = #{id}
              AND deleted = false
            """)
    int update(Bed bed);

    @Update("""
            UPDATE bed
            SET status = 'OCCUPIED',
                current_admission_id = #{admissionId},
                updated_at = NOW()
            WHERE id = #{bedId}
              AND status IN ('EMPTY', 'AVAILABLE')
              AND deleted = false
            """)
    int assign(@Param("bedId") Long bedId, @Param("admissionId") Long admissionId);

    @Update("""
            UPDATE bed
            SET status = 'AVAILABLE',
                current_admission_id = NULL,
                updated_at = NOW()
            WHERE id = #{bedId}
              AND status = 'OCCUPIED'
              AND current_admission_id IS NOT NULL
              AND deleted = false
            """)
    int release(Long bedId);

    @Update("""
            UPDATE bed
            SET status = 'AVAILABLE',
                current_admission_id = NULL,
                updated_at = NOW()
            WHERE current_admission_id = #{admissionId}
              AND status = 'OCCUPIED'
              AND deleted = false
            """)
    int releaseByAdmissionId(Long admissionId);

    @Update("""
            UPDATE bed
            SET deleted = true,
                current_admission_id = NULL,
                updated_at = NOW()
            WHERE id = #{id}
              AND deleted = false
            """)
    int logicDeleteById(Long id);
}
