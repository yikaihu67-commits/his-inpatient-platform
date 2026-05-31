package com.yueshan.backend.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yueshan.backend.service.DemoDataService;

@Service
public class DemoDataServiceImpl implements DemoDataService {
    private final JdbcTemplate jdbcTemplate;

    public DemoDataServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public Map<String, Object> reset() {
        jdbcTemplate.execute("""
                TRUNCATE TABLE
                  operation_log,
                  inpatient_discharge,
                  inpatient_deposit,
                  inpatient_fee,
                  pharmacy_dispense,
                  nursing_record,
                  nursing_order_execution,
                  medical_order,
                  exam_lab_request,
                  medical_record,
                  surgery_operation,
                  bed,
                  inpatient_admission,
                  drug,
                  staff,
                  department,
                  dict_item,
                  patients
                RESTART IDENTITY CASCADE
                """);

        Long deptId = insertDepartment();
        insertStaff("DOC-DEMO-001", "王医生", "MALE", "13800000001", deptId, "DOCTOR", "主治医师");
        insertStaff("NUR-DEMO-001", "赵护士", "FEMALE", "13800000002", deptId, "NURSE", "护士");
        Long drugId = insertDrug("DRUG-DEMO-001", "阿莫西林胶囊", "0.25g*24粒", "盒", new BigDecimal("15.50"), 100, 20);
        Long dispensedDrugId = insertDrug("DRUG-DEMO-002", "头孢克肟胶囊", "0.1g*12粒", "盒", new BigDecimal("28.00"), 18, 10);
        insertDrug("DRUG-DEMO-003", "低库存演示药", "10mg*10片", "盒", new BigDecimal("99.00"), 1, 5);
        Long patientId = insertPatient();
        Long admissionId = insertAdmission(patientId);
        Long bedId = insertBed(admissionId);
        insertAvailableBed();
        insertMedicalRecords(admissionId, patientId);
        Long orderId = insertMedicalOrder(admissionId, patientId);
        insertSubmittedOrder("YZ-DEMO-002", admissionId, patientId, "头颅CT检查", "EXAM", "TEMPORARY", "检查执行");
        insertSubmittedOrder("YZ-DEMO-003", admissionId, patientId, "血常规检验", "LAB", "TEMPORARY", "检验执行");
        Long executedOrderId = insertExecutedOrder(admissionId, patientId);
        Long dispensedOrderId = insertCheckedDrugOrder("YZ-DEMO-005", admissionId, patientId, "头孢克肟胶囊", "0.1", "g");
        insertCheckedDrugOrder("YZ-DEMO-006", admissionId, patientId, "低库存演示药", "10", "mg");
        insertNursingExecution(executedOrderId, admissionId, patientId);
        insertNursingRecords(executedOrderId, admissionId, patientId);
        Long dispenseId = insertDispensedRecord(dispensedOrderId, admissionId, patientId, dispensedDrugId);
        insertFees(admissionId, patientId, orderId, bedId);
        insertDrugDispenseFee(admissionId, patientId, dispenseId, "DRUG-DEMO-002", "头孢克肟胶囊", new BigDecimal("2.00"), "盒", new BigDecimal("28.00"));
        insertDrugReturnFee(admissionId, patientId, dispenseId, "DRUG-DEMO-002", "头孢克肟胶囊", new BigDecimal("-1.00"), "盒", new BigDecimal("28.00"));
        insertSurgery(admissionId, patientId, "SS-DEMO-003", "腹腔镜阑尾切除术", "COMPLETED", new BigDecimal("2800.00"));
        Long billedSurgeryId = insertSurgery(admissionId, patientId, "SS-DEMO-004", "甲状腺结节切除术", "BILLED", new BigDecimal("2600.00"));
        insertSurgery(admissionId, patientId, "SS-DEMO-001", "胆囊切除术", "APPLIED", new BigDecimal("3200.00"));
        insertSurgery(admissionId, patientId, "SS-DEMO-002", "胃镜下息肉切除术", "SCHEDULED", new BigDecimal("1800.00"));
        insertSurgeryFee(admissionId, patientId, billedSurgeryId, "SS-DEMO-004", "甲状腺结节切除术", new BigDecimal("2600.00"));
        insertDeposit(admissionId, patientId);
        insertDictItems();

        return Map.of(
                "patientId", patientId,
                "admissionId", admissionId,
                "bedId", bedId,
                "patientName", "李四",
                "address", "江苏省南京市玄武区",
                "admissionStatus", "IN_HOSPITAL");
    }

    private Long insertDepartment() {
        return jdbcTemplate.queryForObject("""
                INSERT INTO department (dept_code, dept_name, dept_type, parent_id, status, remark, deleted, created_at, updated_at)
                VALUES (?, ?, ?, NULL, ?, ?, false, NOW(), NOW())
                RETURNING id
                """, Long.class, "DEMO-IN-001", "住院内科", "INPATIENT", "ENABLED", "standard demo inpatient department");
    }

    private void insertStaff(String staffNo, String staffName, String gender, String phone, Long departmentId, String roleType, String title) {
        jdbcTemplate.update("""
                INSERT INTO staff (staff_no, staff_name, gender, phone, department_id, role_type, title, status, remark, deleted, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, 'ENABLED', 'standard demo staff', false, NOW(), NOW())
                """, staffNo, staffName, gender, phone, departmentId, roleType, title);
    }

    private Long insertDrug(String code, String name, String specification, String unit, BigDecimal price,
            Integer stockQuantity, Integer stockLowerLimit) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO drug (drug_code, drug_name, specification, unit, price, stock_quantity, stock_lower_limit, status, deleted, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, 'ENABLED', false, NOW(), NOW())
                RETURNING id
                """, Long.class, code, name, specification, unit, price, stockQuantity, stockLowerLimit);
    }

    private Long insertPatient() {
        return jdbcTemplate.queryForObject("""
                INSERT INTO patients (patient_no, name, gender, id_card, phone, birth_date, address, status, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVE', NOW(), NOW())
                RETURNING id
                """, Long.class, "P-DEMO-001", "李四", "MALE", "320102199001010001", "13900000001",
                LocalDate.of(1990, 1, 1), "江苏省南京市玄武区");
    }

    private Long insertAdmission(Long patientId) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO inpatient_admission (
                  admission_no, patient_id, department_name, ward_name, admission_time,
                  admission_diagnosis, charge_type, nursing_level, doctor_name, status, created_at, updated_at
                )
                VALUES (?, ?, ?, ?, NOW(), ?, ?, ?, ?, 'IN_HOSPITAL', NOW(), NOW())
                RETURNING id
                """, Long.class, "ZY-DEMO-001", patientId, "住院内科", "一病区", "肺部感染", "医保", "二级护理", "王医生");
    }

    private Long insertBed(Long admissionId) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO bed (bed_no, ward_name, room_no, bed_type, status, current_admission_id, deleted, created_at, updated_at)
                VALUES (?, ?, ?, ?, 'OCCUPIED', ?, false, NOW(), NOW())
                RETURNING id
                """, Long.class, "B-DEMO-001", "一病区", "101", "普通床", admissionId);
    }

    private void insertMedicalRecords(Long admissionId, Long patientId) {
        jdbcTemplate.update("""
                INSERT INTO medical_record (
                  record_no, admission_id, patient_id, record_type, title, content,
                  doctor_name, record_time, status, remark, deleted, created_at, updated_at
                )
                VALUES
                (?, ?, ?, 'ADMISSION_RECORD', ?, ?, ?, NOW() - INTERVAL '3 day', 'ARCHIVED', ?, false, NOW(), NOW()),
                (?, ?, ?, 'FIRST_COURSE_RECORD', ?, ?, ?, NOW() - INTERVAL '2 day', 'REVIEWED', ?, false, NOW(), NOW()),
                (?, ?, ?, 'DAILY_COURSE_RECORD', ?, ?, ?, NOW() - INTERVAL '1 day', 'SUBMITTED', ?, false, NOW(), NOW()),
                (?, ?, ?, 'DISCHARGE_RECORD', ?, ?, ?, NOW(), 'DRAFT', ?, false, NOW(), NOW()),
                (?, ?, ?, 'NURSING_RECORD', ?, ?, ?, NOW(), 'DRAFT', ?, false, NOW(), NOW())
                """,
                "BL-DEMO-001", admissionId, patientId, "入院记录", admissionRecordTemplate(), "王医生", "demo admission record",
                "BL-DEMO-002", admissionId, patientId, "首次病程记录", firstCourseTemplate(), "王医生", "demo reviewed record",
                "BL-DEMO-003", admissionId, patientId, "日常病程记录", dailyCourseTemplate(), "王医生", "demo submitted record",
                "BL-DEMO-004", admissionId, patientId, "出院记录草稿", dischargeTemplate(), "王医生", "demo discharge draft",
                "BL-DEMO-005", admissionId, patientId, "护理记录", nursingTemplate(), "赵护士", "demo nursing record");
    }

    private String admissionRecordTemplate() {
        return "主诉：咳嗽、咳痰3天。\n现病史：患者3天前受凉后出现咳嗽咳痰。\n既往史：否认重大慢性病史。\n查体：体温正常，双肺呼吸音稍粗。\n初步诊断：肺部感染。\n处理意见：完善检查，抗感染治疗。";
    }

    private String firstCourseTemplate() {
        return "首次病程记录：患者入院后生命体征平稳。\n诊疗计划：完善血常规、影像检查，给予对症治疗。\n注意事项：观察体温、咳痰及用药反应。";
    }

    private String dailyCourseTemplate() {
        return "日常病程记录：今日患者咳嗽较前缓解，精神可。\n查体：双肺呼吸音较前改善。\n处理意见：继续当前治疗方案，复查相关指标。";
    }

    private String dischargeTemplate() {
        return "出院记录：患者症状好转，生命体征平稳。\n出院诊断：肺部感染。\n出院医嘱：规律服药，门诊复查，如有不适及时就诊。";
    }

    private String nursingTemplate() {
        return "护理记录：患者在院期间配合治疗，饮食睡眠可。\n护理措施：健康宣教，观察生命体征，协助完成检查。";
    }

    private void insertAvailableBed() {
        jdbcTemplate.update("""
                INSERT INTO bed (bed_no, ward_name, room_no, bed_type, status, current_admission_id, deleted, created_at, updated_at)
                VALUES (?, ?, ?, ?, 'AVAILABLE', NULL, false, NOW(), NOW())
                """, "B-DEMO-002", "一病区", "102", "普通床");
    }

    private Long insertMedicalOrder(Long admissionId, Long patientId) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO medical_order (
                  order_no, admission_id, patient_id, order_type, order_category, item_name,
                  dosage, dosage_unit, frequency, route, start_time, end_time, doctor_name,
                  nurse_name, status, remark, deleted, created_at, updated_at
                )
                VALUES (?, ?, ?, 'LONG_TERM', 'DRUG', ?, ?, ?, ?, ?, NOW(), NULL, ?, ?, 'CHECKED', ?, false, NOW(), NOW())
                RETURNING id
                """, Long.class, "YZ-DEMO-001", admissionId, patientId, "阿莫西林胶囊", "0.25", "g",
                "每日三次", "口服", "王医生", "赵护士", "standard demo medical order");
    }

    private Long insertCheckedDrugOrder(String orderNo, Long admissionId, Long patientId, String itemName, String dosage, String unit) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO medical_order (
                  order_no, admission_id, patient_id, order_type, order_category, item_name,
                  dosage, dosage_unit, frequency, route, start_time, end_time, doctor_name,
                  nurse_name, status, remark, deleted, created_at, updated_at
                )
                VALUES (?, ?, ?, 'LONG_TERM', 'DRUG', ?, ?, ?, ?, ?, NOW(), NULL, ?, ?, 'CHECKED', ?, false, NOW(), NOW())
                RETURNING id
                """, Long.class, orderNo, admissionId, patientId, itemName, dosage, unit, "每日两次", "口服",
                "王医生", "赵护士", "pharmacy station demo checked drug order");
    }

    private void insertSubmittedOrder(String orderNo, Long admissionId, Long patientId, String itemName,
            String category, String type, String route) {
        jdbcTemplate.update("""
                INSERT INTO medical_order (
                  order_no, admission_id, patient_id, order_type, order_category, item_name,
                  dosage, dosage_unit, frequency, route, start_time, end_time, doctor_name,
                  nurse_name, status, remark, deleted, created_at, updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, NULL, NULL, ?, ?, NOW(), NULL, ?, NULL, 'SUBMITTED', ?, false, NOW(), NOW())
                """, orderNo, admissionId, patientId, type, category, itemName, "立即执行", route, "王医生", "doctor station demo pending check order");
    }

    private Long insertExecutedOrder(Long admissionId, Long patientId) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO medical_order (
                  order_no, admission_id, patient_id, order_type, order_category, item_name,
                  dosage, dosage_unit, frequency, route, start_time, end_time, doctor_name,
                  nurse_name, status, remark, deleted, created_at, updated_at
                )
                VALUES (?, ?, ?, 'TEMPORARY', 'TREATMENT', ?, NULL, NULL, ?, ?, NOW(), NULL, ?, ?, 'EXECUTED', ?, false, NOW(), NOW())
                RETURNING id
                """, Long.class, "YZ-DEMO-004", admissionId, patientId, "雾化吸入治疗", "立即执行", "治疗执行",
                "王医生", "赵护士", "nurse station demo executed order");
    }

    private void insertNursingExecution(Long orderId, Long admissionId, Long patientId) {
        jdbcTemplate.update("""
                INSERT INTO nursing_order_execution (
                  execution_no, order_id, admission_id, patient_id, scheduled_time, executed_time,
                  nurse_name, status, result, remark, deleted, created_at, updated_at
                )
                VALUES (?, ?, ?, ?, NOW(), NOW(), ?, 'EXECUTED', ?, ?, false, NOW(), NOW())
                """, "ZX-DEMO-001", orderId, admissionId, patientId, "赵护士", "执行成功", "standard demo nursing execution");
    }

    private void insertNursingRecords(Long orderId, Long admissionId, Long patientId) {
        jdbcTemplate.update("""
                INSERT INTO nursing_record (
                  record_no, order_id, admission_id, patient_id, nursing_type, nursing_content,
                  temperature, pulse, respiration, blood_pressure, intake_amount, output_amount,
                  nursing_level, nurse_name, record_time, status, billable, nursing_fee, billed,
                  remark, deleted, created_at, updated_at
                )
                VALUES
                ('HL-DEMO-001', NULL, ?, ?, 'VITAL_SIGN', '生命体征记录', 37.80, 102, 20, '130/82', NULL, NULL, '二级护理', '赵护士', NOW(), 'RECORDED', false, 0, false, 'demo abnormal vital sign', false, NOW(), NOW()),
                ('HL-DEMO-002', NULL, ?, ?, 'DAILY_CARE', '日常护理', NULL, NULL, NULL, NULL, 500, 300, '二级护理', '赵护士', NOW(), 'EXECUTED', true, 30.00, false, 'demo daily nursing', false, NOW(), NOW()),
                ('HL-DEMO-003', ?, ?, ?, 'ORDER_EXECUTION', '医嘱执行护理', NULL, NULL, NULL, NULL, NULL, NULL, '二级护理', '赵护士', NOW(), 'EXECUTED', false, 0, false, 'demo order execution nursing', false, NOW(), NOW()),
                ('HL-DEMO-004', NULL, ?, ?, 'INFUSION', '输液护理', NULL, NULL, NULL, NULL, NULL, NULL, '二级护理', '赵护士', NOW(), 'BILLED', true, 25.00, true, 'demo billed nursing', false, NOW(), NOW())
                """, admissionId, patientId, admissionId, patientId, orderId, admissionId, patientId, admissionId, patientId);
    }

    private Long insertDispensedRecord(Long orderId, Long admissionId, Long patientId, Long drugId) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO pharmacy_dispense (
                  dispense_no, order_id, admission_id, patient_id, drug_id, quantity, pharmacist_name,
                  dispense_time, return_time, status, remark, deleted, created_at, updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NULL, 'DISPENSED', ?, false, NOW(), NOW())
                RETURNING id
                """, Long.class, "FY-DEMO-001", orderId, admissionId, patientId, drugId, 2, "药师", "standard demo dispensed record");
    }

    private void insertFees(Long admissionId, Long patientId, Long orderId, Long bedId) {
        jdbcTemplate.update("""
                INSERT INTO inpatient_fee (
                  fee_no, admission_id, patient_id, source_type, source_id, item_code, item_name,
                  item_category, quantity, unit, unit_price, total_amount, fee_time, status,
                  remark, deleted, created_at, updated_at
                )
                VALUES (?, ?, ?, 'ORDER', ?, ?, ?, 'DRUG', ?, ?, ?, ?, NOW(), 'UNSETTLED', ?, false, NOW(), NOW())
                """, "FEE-DEMO-001", admissionId, patientId, orderId, "DRUG-DEMO-001", "阿莫西林胶囊",
                new BigDecimal("2.00"), "盒", new BigDecimal("15.50"), new BigDecimal("31.00"), "药品费用");

        jdbcTemplate.update("""
                INSERT INTO inpatient_fee (
                  fee_no, admission_id, patient_id, source_type, source_id, item_code, item_name,
                  item_category, quantity, unit, unit_price, total_amount, fee_time, status,
                  remark, deleted, created_at, updated_at
                )
                VALUES (?, ?, ?, 'BED', ?, ?, ?, 'BED', ?, ?, ?, ?, NOW(), 'UNSETTLED', ?, false, NOW(), NOW())
                """, "FEE-DEMO-002", admissionId, patientId, bedId, "BED-DEMO-001", "床位费",
                new BigDecimal("1.00"), "天", new BigDecimal("80.00"), new BigDecimal("80.00"), "床位费");
    }

    private void insertDrugDispenseFee(Long admissionId, Long patientId, Long dispenseId, String drugCode, String drugName,
            BigDecimal quantity, String unit, BigDecimal unitPrice) {
        jdbcTemplate.update("""
                INSERT INTO inpatient_fee (
                  fee_no, admission_id, patient_id, source_type, source_id, item_code, item_name,
                  item_category, quantity, unit, unit_price, total_amount, fee_time, status,
                  remark, deleted, created_at, updated_at
                )
                VALUES (?, ?, ?, 'DRUG_DISPENSE', ?, ?, ?, 'DRUG', ?, ?, ?, ?, NOW(), 'UNSETTLED', ?, false, NOW(), NOW())
                """, "FEE-DEMO-003", admissionId, patientId, dispenseId, drugCode, drugName,
                quantity, unit, unitPrice, quantity.multiply(unitPrice), "发药自动费用");
    }

    private void insertDrugReturnFee(Long admissionId, Long patientId, Long dispenseId, String drugCode, String drugName,
            BigDecimal quantity, String unit, BigDecimal unitPrice) {
        jdbcTemplate.update("""
                INSERT INTO inpatient_fee (
                  fee_no, admission_id, patient_id, source_type, source_id, item_code, item_name,
                  item_category, quantity, unit, unit_price, total_amount, fee_time, status,
                  remark, deleted, created_at, updated_at
                )
                VALUES (?, ?, ?, 'DRUG_RETURN', ?, ?, ?, 'DRUG', ?, ?, ?, ?, NOW(), 'UNSETTLED', ?, false, NOW(), NOW())
                """, "FEE-DEMO-004", admissionId, patientId, dispenseId, drugCode, "退药冲正-" + drugName,
                quantity, unit, unitPrice, quantity.multiply(unitPrice), "退药负费用/冲正费用");
    }

    private Long insertSurgery(Long admissionId, Long patientId, String surgeryNo, String surgeryName,
            String status, BigDecimal surgeryFee) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime planned = now.plusDays(1);
        LocalDateTime start = Set.of("COMPLETED", "BILLED").contains(status) ? now.minusHours(2) : null;
        LocalDateTime end = Set.of("COMPLETED", "BILLED").contains(status) ? now.minusHours(1) : null;
        return jdbcTemplate.queryForObject("""
                INSERT INTO surgery_operation (
                  surgery_no, patient_id, admission_id, surgery_name, preoperative_diagnosis,
                  surgery_level, surgery_type, operating_room, planned_time, actual_start_time,
                  actual_end_time, primary_doctor_name, assistant_doctor_name, anesthesia_method,
                  anesthesiologist_name, status, surgery_fee, remark, deleted, created_at, updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, false, NOW(), NOW())
                RETURNING id
                """, Long.class, surgeryNo, patientId, admissionId, surgeryName, "急性腹痛待查",
                "II级", "择期", "OR-1", planned, start, end, "王医生", "李医生", "全身麻醉",
                "张麻醉", status, surgeryFee, "standard demo surgery");
    }

    private void insertSurgeryFee(Long admissionId, Long patientId, Long surgeryId, String surgeryNo,
            String surgeryName, BigDecimal amount) {
        jdbcTemplate.update("""
                INSERT INTO inpatient_fee (
                  fee_no, admission_id, patient_id, source_type, source_id, item_code, item_name,
                  item_category, quantity, unit, unit_price, total_amount, fee_time, status,
                  remark, deleted, created_at, updated_at
                )
                VALUES (?, ?, ?, 'SURGERY', ?, ?, ?, 'SURGERY', 1.00, ?, ?, ?, NOW(), 'UNSETTLED', ?, false, NOW(), NOW())
                """, "FEE-DEMO-005", admissionId, patientId, surgeryId, surgeryNo, surgeryName,
                "台", amount, amount, "手术费用");
    }

    private void insertDeposit(Long admissionId, Long patientId) {
        jdbcTemplate.update("""
                INSERT INTO inpatient_deposit (
                  deposit_no, admission_id, patient_id, amount, payment_method, transaction_type,
                  transaction_time, operator_name, status, remark, deleted, created_at, updated_at
                )
                VALUES (?, ?, ?, ?, 'CASH', 'PAY', ?, ?, 'SUCCESS', ?, false, NOW(), NOW())
                """, "DEP-DEMO-001", admissionId, patientId, new BigDecimal("500.00"),
                LocalDateTime.now(), "收费员", "预交金");
    }

    private void insertDictItems() {
        jdbcTemplate.update("""
                INSERT INTO dict_item (dict_type, dict_code, dict_name, sort_order, status, remark, deleted, created_at, updated_at)
                VALUES
                  ('charge_type', 'MEDICAL_INSURANCE', '医保', 1, 'ENABLED', 'standard demo dict', false, NOW(), NOW()),
                  ('charge_type', 'SELF_PAY', '自费', 2, 'ENABLED', 'standard demo dict', false, NOW(), NOW())
                """);
    }
}
