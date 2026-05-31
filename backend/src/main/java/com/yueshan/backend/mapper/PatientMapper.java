package com.yueshan.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.yueshan.backend.domain.Patient;
import com.yueshan.backend.dto.PatientQueryRequest;

@Mapper
public interface PatientMapper {

    @Select("""
            SELECT id, patient_no, name, gender, id_card, phone, birth_date, address, status, created_at, updated_at
            FROM patients
            WHERE status <> 'DELETED'
            ORDER BY id DESC
            """)
    List<Patient> findAll();

    @Select("""
            <script>
            SELECT id, patient_no, name, gender, id_card, phone, birth_date, address, status, created_at, updated_at
            FROM patients
            <where>
                status != 'DELETED'
                <if test="query.patientNo != null and query.patientNo != ''">
                    AND patient_no ILIKE CONCAT('%', #{query.patientNo}, '%')
                </if>
                <if test="query.name != null and query.name != ''">
                    AND name ILIKE CONCAT('%', #{query.name}, '%')
                </if>
                <if test="query.idCard != null and query.idCard != ''">
                    AND id_card ILIKE CONCAT('%', #{query.idCard}, '%')
                </if>
                <if test="query.phone != null and query.phone != ''">
                    AND phone ILIKE CONCAT('%', #{query.phone}, '%')
                </if>
            </where>
            ORDER BY id DESC
            LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<Patient> findPage(
            @Param("query") PatientQueryRequest query,
            @Param("offset") int offset,
            @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM patients
            <where>
                status != 'DELETED'
                <if test="query.patientNo != null and query.patientNo != ''">
                    AND patient_no ILIKE CONCAT('%', #{query.patientNo}, '%')
                </if>
                <if test="query.name != null and query.name != ''">
                    AND name ILIKE CONCAT('%', #{query.name}, '%')
                </if>
                <if test="query.idCard != null and query.idCard != ''">
                    AND id_card ILIKE CONCAT('%', #{query.idCard}, '%')
                </if>
                <if test="query.phone != null and query.phone != ''">
                    AND phone ILIKE CONCAT('%', #{query.phone}, '%')
                </if>
            </where>
            </script>
            """)
    long countPage(@Param("query") PatientQueryRequest query);

    @Select("""
            SELECT id, patient_no, name, gender, id_card, phone, birth_date, address, status, created_at, updated_at
            FROM patients
            WHERE id = #{id}
              AND status <> 'DELETED'
            """)
    Patient findById(Long id);

    @Select("""
            SELECT id, patient_no, name, gender, id_card, phone, birth_date, address, status, created_at, updated_at
            FROM patients
            WHERE patient_no = #{patientNo}
              AND status <> 'DELETED'
            """)
    Patient findByPatientNo(String patientNo);

    @Select("SELECT COUNT(1) FROM patients WHERE patient_no = #{patientNo}")
    int countByPatientNo(String patientNo);

    @Select("""
            SELECT COUNT(1)
            FROM patients
            WHERE patient_no = #{patientNo}
              AND id <> #{excludeId}
            """)
    int countByPatientNoExcludeId(@Param("patientNo") String patientNo, @Param("excludeId") Long excludeId);

    @Select("SELECT COUNT(1) FROM patients WHERE id_card = #{idCard}")
    int countByIdCard(String idCard);

    @Select("""
            SELECT COUNT(1)
            FROM patients
            WHERE id_card = #{idCard}
              AND id <> #{excludeId}
            """)
    int countByIdCardExcludeId(@Param("idCard") String idCard, @Param("excludeId") Long excludeId);

    @Select("SELECT COUNT(1) FROM patients WHERE phone = #{phone}")
    int countByPhone(String phone);

    @Select("""
            SELECT COUNT(1)
            FROM patients
            WHERE phone = #{phone}
              AND id <> #{excludeId}
            """)
    int countByPhoneExcludeId(@Param("phone") String phone, @Param("excludeId") Long excludeId);

    @Insert("""
            INSERT INTO patients (patient_no, name, gender, id_card, phone, birth_date, address, status, created_at, updated_at)
            VALUES (#{patientNo}, #{name}, #{gender}, #{idCard}, #{phone}, #{birthDate}, #{address}, #{status}, NOW(), NOW())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(Patient patient);

    @Update("""
            UPDATE patients
            SET patient_no = #{patientNo},
                name = #{name},
                gender = #{gender},
                id_card = #{idCard},
                phone = #{phone},
                birth_date = #{birthDate},
                address = #{address},
                status = #{status},
                updated_at = NOW()
            WHERE id = #{id}
            """)
    int update(Patient patient);

    @Update("""
            UPDATE patients
            SET status = 'DELETED',
                updated_at = NOW()
            WHERE id = #{id}
              AND status <> 'DELETED'
            """)
    int logicDeleteById(Long id);
}
