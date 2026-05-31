param(
  [switch]$Force
)

$ErrorActionPreference = "Stop"

chcp 65001 | Out-Null
[Console]::InputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$dbHost = "localhost"
$dbPort = "5432"
$dbName = "his_inpatient"
$dbUser = "postgres"
$env:PGPASSWORD = "123456"

Write-Host "This script will clear demo/business data and reinsert standard UTF-8 demo data." -ForegroundColor Yellow
Write-Host ("Database: {0}@{1}:{2}, user: {3}" -f $dbName, $dbHost, $dbPort, $dbUser) -ForegroundColor Yellow
Write-Host "Table structures will be kept. Business/demo table data will be removed." -ForegroundColor Yellow
Write-Host "Do not run this script against a production database." -ForegroundColor Yellow

if (-not $Force) {
  $confirm = Read-Host "Type RESET to continue"
  if ($confirm -ne "RESET") {
    Write-Host "Cancelled." -ForegroundColor Cyan
    exit 0
  }
} else {
  Write-Host "Force mode enabled. Confirmation prompt skipped." -ForegroundColor Cyan
}

$psqlCommand = Get-Command psql -ErrorAction SilentlyContinue
$psqlPath = if ($psqlCommand) { $psqlCommand.Source } else { $null }
if (-not $psqlPath) {
  $psqlPath = Get-ChildItem "C:\Program Files\PostgreSQL" -Recurse -Filter psql.exe -ErrorAction SilentlyContinue |
    Sort-Object FullName -Descending |
    Select-Object -First 1 -ExpandProperty FullName
}
if (-not $psqlPath) {
  $psqlPath = Get-ChildItem "C:\Program Files (x86)\PostgreSQL" -Recurse -Filter psql.exe -ErrorAction SilentlyContinue |
    Sort-Object FullName -Descending |
    Select-Object -First 1 -ExpandProperty FullName
}
if (-not $psqlPath) {
  throw "psql was not found. Install PostgreSQL client tools or add psql to PATH."
}

Write-Host ("Using psql: {0}" -f $psqlPath) -ForegroundColor Cyan

# SQL uses PostgreSQL Unicode escape strings (U&'...') so this script remains
# safe even when legacy Windows PowerShell reads source files with ANSI encoding.
$sql = @'
SET client_encoding = 'UTF8';
SET client_min_messages = warning;

CREATE TABLE IF NOT EXISTS medical_record (
  id BIGSERIAL PRIMARY KEY,
  record_no VARCHAR(32) NOT NULL,
  admission_id BIGINT NOT NULL,
  patient_id BIGINT NOT NULL,
  record_type VARCHAR(40) NOT NULL,
  title VARCHAR(100) NOT NULL,
  content TEXT NOT NULL,
  doctor_name VARCHAR(50) NOT NULL,
  record_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
  remark VARCHAR(255),
  deleted BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE medical_record ADD COLUMN IF NOT EXISTS record_no VARCHAR(32);
ALTER TABLE medical_record ADD COLUMN IF NOT EXISTS admission_id BIGINT;
ALTER TABLE medical_record ADD COLUMN IF NOT EXISTS patient_id BIGINT;
ALTER TABLE medical_record ADD COLUMN IF NOT EXISTS record_type VARCHAR(40);
ALTER TABLE medical_record ADD COLUMN IF NOT EXISTS title VARCHAR(100);
ALTER TABLE medical_record ADD COLUMN IF NOT EXISTS content TEXT;
ALTER TABLE medical_record ADD COLUMN IF NOT EXISTS doctor_name VARCHAR(50);
ALTER TABLE medical_record ADD COLUMN IF NOT EXISTS record_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE medical_record ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'DRAFT';
ALTER TABLE medical_record ADD COLUMN IF NOT EXISTS remark VARCHAR(255);
ALTER TABLE medical_record ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE medical_record ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE medical_record ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
CREATE UNIQUE INDEX IF NOT EXISTS uk_medical_record_no ON medical_record (record_no);
CREATE INDEX IF NOT EXISTS idx_medical_record_admission_id ON medical_record (admission_id);
CREATE INDEX IF NOT EXISTS idx_medical_record_patient_id ON medical_record (patient_id);
CREATE INDEX IF NOT EXISTS idx_medical_record_record_type ON medical_record (record_type);
CREATE INDEX IF NOT EXISTS idx_medical_record_status ON medical_record (status);
CREATE INDEX IF NOT EXISTS idx_medical_record_deleted ON medical_record (deleted);

CREATE TABLE IF NOT EXISTS surgery_operation (
  id BIGSERIAL PRIMARY KEY,
  surgery_no VARCHAR(32) NOT NULL,
  patient_id BIGINT NOT NULL,
  admission_id BIGINT NOT NULL,
  surgery_name VARCHAR(100) NOT NULL,
  preoperative_diagnosis VARCHAR(255),
  surgery_level VARCHAR(30),
  surgery_type VARCHAR(30),
  operating_room VARCHAR(50),
  planned_time TIMESTAMP,
  actual_start_time TIMESTAMP,
  actual_end_time TIMESTAMP,
  primary_doctor_name VARCHAR(50),
  assistant_doctor_name VARCHAR(100),
  anesthesia_method VARCHAR(50),
  anesthesiologist_name VARCHAR(50),
  status VARCHAR(30) NOT NULL DEFAULT 'APPLIED',
  surgery_fee NUMERIC(12,2) NOT NULL DEFAULT 0,
  remark VARCHAR(255),
  deleted BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE surgery_operation ADD COLUMN IF NOT EXISTS surgery_no VARCHAR(32);
ALTER TABLE surgery_operation ADD COLUMN IF NOT EXISTS patient_id BIGINT;
ALTER TABLE surgery_operation ADD COLUMN IF NOT EXISTS admission_id BIGINT;
ALTER TABLE surgery_operation ADD COLUMN IF NOT EXISTS surgery_name VARCHAR(100);
ALTER TABLE surgery_operation ADD COLUMN IF NOT EXISTS preoperative_diagnosis VARCHAR(255);
ALTER TABLE surgery_operation ADD COLUMN IF NOT EXISTS surgery_level VARCHAR(30);
ALTER TABLE surgery_operation ADD COLUMN IF NOT EXISTS surgery_type VARCHAR(30);
ALTER TABLE surgery_operation ADD COLUMN IF NOT EXISTS operating_room VARCHAR(50);
ALTER TABLE surgery_operation ADD COLUMN IF NOT EXISTS planned_time TIMESTAMP;
ALTER TABLE surgery_operation ADD COLUMN IF NOT EXISTS actual_start_time TIMESTAMP;
ALTER TABLE surgery_operation ADD COLUMN IF NOT EXISTS actual_end_time TIMESTAMP;
ALTER TABLE surgery_operation ADD COLUMN IF NOT EXISTS primary_doctor_name VARCHAR(50);
ALTER TABLE surgery_operation ADD COLUMN IF NOT EXISTS assistant_doctor_name VARCHAR(100);
ALTER TABLE surgery_operation ADD COLUMN IF NOT EXISTS anesthesia_method VARCHAR(50);
ALTER TABLE surgery_operation ADD COLUMN IF NOT EXISTS anesthesiologist_name VARCHAR(50);
ALTER TABLE surgery_operation ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'APPLIED';
ALTER TABLE surgery_operation ADD COLUMN IF NOT EXISTS surgery_fee NUMERIC(12,2) NOT NULL DEFAULT 0;
ALTER TABLE surgery_operation ADD COLUMN IF NOT EXISTS remark VARCHAR(255);
ALTER TABLE surgery_operation ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE surgery_operation ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE surgery_operation ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
CREATE UNIQUE INDEX IF NOT EXISTS uk_surgery_operation_no ON surgery_operation (surgery_no) WHERE deleted = false;
CREATE INDEX IF NOT EXISTS idx_surgery_operation_patient_id ON surgery_operation (patient_id);
CREATE INDEX IF NOT EXISTS idx_surgery_operation_admission_id ON surgery_operation (admission_id);
CREATE INDEX IF NOT EXISTS idx_surgery_operation_status ON surgery_operation (status);
CREATE INDEX IF NOT EXISTS idx_surgery_operation_deleted ON surgery_operation (deleted);

ALTER TABLE medical_order ADD COLUMN IF NOT EXISTS order_content VARCHAR(500);
ALTER TABLE medical_order ADD COLUMN IF NOT EXISTS execution_department VARCHAR(50);
ALTER TABLE medical_order ADD COLUMN IF NOT EXISTS executor_name VARCHAR(50);
ALTER TABLE medical_order ADD COLUMN IF NOT EXISTS executed_time TIMESTAMP;
ALTER TABLE medical_order ADD COLUMN IF NOT EXISTS unit_price NUMERIC(12,2) NOT NULL DEFAULT 0;
ALTER TABLE medical_order ADD COLUMN IF NOT EXISTS quantity NUMERIC(12,2) NOT NULL DEFAULT 1;
ALTER TABLE medical_order ADD COLUMN IF NOT EXISTS total_amount NUMERIC(12,2) NOT NULL DEFAULT 0;
ALTER TABLE medical_order ADD COLUMN IF NOT EXISTS billed BOOLEAN NOT NULL DEFAULT false;
CREATE INDEX IF NOT EXISTS idx_medical_order_billed ON medical_order (billed);

CREATE TABLE IF NOT EXISTS nursing_record (
  id BIGSERIAL PRIMARY KEY,
  record_no VARCHAR(32) NOT NULL,
  order_id BIGINT,
  admission_id BIGINT NOT NULL,
  patient_id BIGINT NOT NULL,
  nursing_type VARCHAR(30) NOT NULL,
  nursing_content VARCHAR(500) NOT NULL,
  temperature NUMERIC(5,2),
  pulse INTEGER,
  respiration INTEGER,
  blood_pressure VARCHAR(30),
  intake_amount NUMERIC(12,2),
  output_amount NUMERIC(12,2),
  nursing_level VARCHAR(30),
  nurse_name VARCHAR(50),
  record_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  status VARCHAR(30) NOT NULL DEFAULT 'RECORDED',
  billable BOOLEAN NOT NULL DEFAULT false,
  nursing_fee NUMERIC(12,2) NOT NULL DEFAULT 0,
  billed BOOLEAN NOT NULL DEFAULT false,
  remark VARCHAR(255),
  deleted BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_nursing_record_no ON nursing_record (record_no) WHERE deleted = false;
CREATE INDEX IF NOT EXISTS idx_nursing_record_admission_id ON nursing_record (admission_id);
CREATE INDEX IF NOT EXISTS idx_nursing_record_patient_id ON nursing_record (patient_id);
CREATE INDEX IF NOT EXISTS idx_nursing_record_status ON nursing_record (status);

ALTER TABLE exam_lab_request ADD COLUMN IF NOT EXISTS request_content VARCHAR(500);
ALTER TABLE exam_lab_request ADD COLUMN IF NOT EXISTS execution_department VARCHAR(50);
ALTER TABLE exam_lab_request ADD COLUMN IF NOT EXISTS executor_name VARCHAR(50);
ALTER TABLE exam_lab_request ADD COLUMN IF NOT EXISTS executed_time TIMESTAMP;
ALTER TABLE exam_lab_request ADD COLUMN IF NOT EXISTS report_doctor_name VARCHAR(50);
ALTER TABLE exam_lab_request ADD COLUMN IF NOT EXISTS report_time TIMESTAMP;
ALTER TABLE exam_lab_request ADD COLUMN IF NOT EXISTS abnormal_flag BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE exam_lab_request ADD COLUMN IF NOT EXISTS unit_price NUMERIC(12,2) NOT NULL DEFAULT 0;
ALTER TABLE exam_lab_request ADD COLUMN IF NOT EXISTS quantity NUMERIC(12,2) NOT NULL DEFAULT 1;
ALTER TABLE exam_lab_request ADD COLUMN IF NOT EXISTS total_amount NUMERIC(12,2) NOT NULL DEFAULT 0;
ALTER TABLE exam_lab_request ADD COLUMN IF NOT EXISTS billed BOOLEAN NOT NULL DEFAULT false;
CREATE INDEX IF NOT EXISTS idx_exam_lab_request_billed ON exam_lab_request (billed);

ALTER TABLE inpatient_discharge ADD COLUMN IF NOT EXISTS discharge_diagnosis VARCHAR(255);
ALTER TABLE inpatient_discharge ADD COLUMN IF NOT EXISTS discharge_summary TEXT;
ALTER TABLE inpatient_discharge ADD COLUMN IF NOT EXISTS discharge_order TEXT;
ALTER TABLE inpatient_discharge ADD COLUMN IF NOT EXISTS take_home_drug_instruction TEXT;
ALTER TABLE inpatient_discharge ADD COLUMN IF NOT EXISTS apply_doctor VARCHAR(50);
ALTER TABLE inpatient_discharge ADD COLUMN IF NOT EXISTS apply_time TIMESTAMP;
ALTER TABLE inpatient_discharge ADD COLUMN IF NOT EXISTS reviewer_name VARCHAR(50);
ALTER TABLE inpatient_discharge ADD COLUMN IF NOT EXISTS review_time TIMESTAMP;
ALTER TABLE inpatient_discharge ADD COLUMN IF NOT EXISTS settled_amount NUMERIC(12,2) NOT NULL DEFAULT 0;
ALTER TABLE inpatient_discharge ADD COLUMN IF NOT EXISTS unsettled_amount NUMERIC(12,2) NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS patient_appointment (
  id BIGSERIAL PRIMARY KEY,
  patient_id BIGINT NOT NULL,
  admission_id BIGINT,
  appointment_type VARCHAR(40) NOT NULL,
  appointment_item VARCHAR(120) NOT NULL,
  appointment_date DATE NOT NULL,
  time_slot VARCHAR(40) NOT NULL,
  contact_phone VARCHAR(20),
  remark VARCHAR(500),
  status VARCHAR(30) NOT NULL DEFAULT 'REQUESTED',
  cancel_time TIMESTAMP,
  complete_time TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_patient_appointment_patient_id ON patient_appointment (patient_id);
CREATE INDEX IF NOT EXISTS idx_patient_appointment_admission_id ON patient_appointment (admission_id);
CREATE INDEX IF NOT EXISTS idx_patient_appointment_type ON patient_appointment (appointment_type);
CREATE INDEX IF NOT EXISTS idx_patient_appointment_status ON patient_appointment (status);
CREATE INDEX IF NOT EXISTS idx_patient_appointment_date ON patient_appointment (appointment_date);
CREATE INDEX IF NOT EXISTS idx_patient_appointment_deleted ON patient_appointment (deleted);

TRUNCATE TABLE
  operation_log,
  patient_appointment,
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
RESTART IDENTITY CASCADE;

DO $$
DECLARE
  v_patient_id BIGINT;
  v_discharged_patient_id BIGINT;
  v_dept_id BIGINT;
  v_doctor_id BIGINT;
  v_nurse_id BIGINT;
  v_drug_id BIGINT;
  v_dispensed_drug_id BIGINT;
  v_low_stock_drug_id BIGINT;
  v_admission_id BIGINT;
  v_discharged_admission_id BIGINT;
  v_bed_id BIGINT;
  v_order_id BIGINT;
  v_executed_order_id BIGINT;
  v_dispensed_order_id BIGINT;
  v_dispense_id BIGINT;
  v_completed_surgery_id BIGINT;
  v_billed_surgery_id BIGINT;
BEGIN
  ALTER TABLE drug ADD COLUMN IF NOT EXISTS stock_lower_limit INTEGER NOT NULL DEFAULT 0;

  INSERT INTO department (dept_code, dept_name, dept_type, parent_id, status, remark, deleted, created_at, updated_at)
  VALUES ('DEMO-IN-001', U&'\4F4F\9662\5185\79D1', 'INPATIENT', NULL, 'ENABLED', 'standard demo inpatient department', false, NOW(), NOW())
  RETURNING id INTO v_dept_id;

  INSERT INTO staff (staff_no, staff_name, gender, phone, department_id, role_type, title, status, remark, deleted, created_at, updated_at)
  VALUES ('DOC-DEMO-001', U&'\738B\533B\751F', 'MALE', '13800000001', v_dept_id, 'DOCTOR', U&'\4E3B\6CBB\533B\5E08', 'ENABLED', 'standard demo doctor', false, NOW(), NOW())
  RETURNING id INTO v_doctor_id;

  INSERT INTO staff (staff_no, staff_name, gender, phone, department_id, role_type, title, status, remark, deleted, created_at, updated_at)
  VALUES ('NUR-DEMO-001', U&'\8D75\62A4\58EB', 'FEMALE', '13800000002', v_dept_id, 'NURSE', U&'\62A4\5E08', 'ENABLED', 'standard demo nurse', false, NOW(), NOW())
  RETURNING id INTO v_nurse_id;

  INSERT INTO drug (drug_code, drug_name, specification, unit, price, stock_quantity, stock_lower_limit, status, deleted, created_at, updated_at)
  VALUES ('DRUG-DEMO-001', U&'\963F\83AB\897F\6797\80F6\56CA', U&'0.25g*24\7C92', U&'\76D2', 15.50, 100, 20, 'ENABLED', false, NOW(), NOW())
  RETURNING id INTO v_drug_id;

  INSERT INTO drug (drug_code, drug_name, specification, unit, price, stock_quantity, stock_lower_limit, status, deleted, created_at, updated_at)
  VALUES ('DRUG-DEMO-002', U&'\5934\5B62\514B\809F\80F6\56CA', U&'0.1g*12\7C92', U&'\76D2', 28.00, 18, 10, 'ENABLED', false, NOW(), NOW())
  RETURNING id INTO v_dispensed_drug_id;

  INSERT INTO drug (drug_code, drug_name, specification, unit, price, stock_quantity, stock_lower_limit, status, deleted, created_at, updated_at)
  VALUES ('DRUG-DEMO-003', U&'\4F4E\5E93\5B58\6F14\793A\836F', U&'10mg*10\7247', U&'\76D2', 99.00, 1, 5, 'ENABLED', false, NOW(), NOW())
  RETURNING id INTO v_low_stock_drug_id;

  INSERT INTO patients (patient_no, name, gender, id_card, phone, birth_date, address, status, created_at, updated_at)
  VALUES ('P-DEMO-001', U&'\674E\56DB', 'MALE', '320102199001010001', '13900000001', '1990-01-01', U&'\6C5F\82CF\7701\5357\4EAC\5E02\7384\6B66\533A', 'ACTIVE', NOW(), NOW())
  RETURNING id INTO v_patient_id;

  INSERT INTO inpatient_admission (
    admission_no, patient_id, department_name, ward_name, admission_time,
    admission_diagnosis, charge_type, nursing_level, doctor_name, status, created_at, updated_at
  )
  VALUES (
    'ZY-DEMO-001', v_patient_id, U&'\4F4F\9662\5185\79D1', U&'\4E00\75C5\533A', NOW(),
    U&'\80BA\90E8\611F\67D3', U&'\533B\4FDD', U&'\4E8C\7EA7\62A4\7406', U&'\738B\533B\751F', 'IN_HOSPITAL', NOW(), NOW()
  )
  RETURNING id INTO v_admission_id;

  INSERT INTO patients (patient_no, name, gender, id_card, phone, birth_date, address, status, created_at, updated_at)
  VALUES ('P-DEMO-002', U&'\738B\4E94', 'FEMALE', '320102199202020002', '13900000002', '1992-02-02', U&'\6C5F\82CF\7701\5357\4EAC\5E02\9F13\697C\533A', 'ACTIVE', NOW() - INTERVAL '7 day', NOW())
  RETURNING id INTO v_discharged_patient_id;

  INSERT INTO inpatient_admission (
    admission_no, patient_id, department_name, ward_name, admission_time, discharge_time,
    admission_diagnosis, charge_type, nursing_level, doctor_name, status, created_at, updated_at
  )
  VALUES (
    'ZY-DEMO-002', v_discharged_patient_id, U&'\4F4F\9662\5185\79D1', U&'\4E8C\75C5\533A', NOW() - INTERVAL '6 day', NOW() - INTERVAL '1 day',
    U&'\6025\6027\80C3\80A0\708E', U&'\533B\4FDD', U&'\4E8C\7EA7\62A4\7406', U&'\738B\533B\751F', 'DISCHARGED', NOW() - INTERVAL '6 day', NOW()
  )
  RETURNING id INTO v_discharged_admission_id;

  INSERT INTO medical_record (
    record_no, admission_id, patient_id, record_type, title, content,
    doctor_name, record_time, status, remark, deleted, created_at, updated_at
  )
  VALUES
  (
    'BL-DEMO-001', v_admission_id, v_patient_id, 'ADMISSION_RECORD', U&'\5165\9662\8BB0\5F55',
    U&'\4E3B\8BC9\FF1A\54B3\55FD\3001\54B3\75F03\5929\3002\000A\73B0\75C5\53F2\FF1A\53D7\51C9\540E\51FA\73B0\54B3\55FD\54B3\75F0\3002\000A\65E2\5F80\53F2\FF1A\5426\8BA4\91CD\5927\6162\6027\75C5\53F2\3002\000A\67E5\4F53\FF1A\53CC\80BA\547C\5438\97F3\7A0D\7C97\3002\000A\521D\6B65\8BCA\65AD\FF1A\80BA\90E8\611F\67D3\3002\000A\5904\7406\610F\89C1\FF1A\5B8C\5584\68C0\67E5\FF0C\6297\611F\67D3\6CBB\7597\3002',
    U&'\738B\533B\751F', NOW() - INTERVAL '3 day', 'ARCHIVED', 'demo admission record', false, NOW(), NOW()
  ),
  (
    'BL-DEMO-002', v_admission_id, v_patient_id, 'FIRST_COURSE_RECORD', U&'\9996\6B21\75C5\7A0B\8BB0\5F55',
    U&'\9996\6B21\75C5\7A0B\8BB0\5F55\FF1A\60A3\8005\5165\9662\540E\751F\547D\4F53\5F81\5E73\7A33\3002\000A\8BCA\7597\8BA1\5212\FF1A\5B8C\5584\8840\5E38\89C4\3001\5F71\50CF\68C0\67E5\FF0C\7ED9\4E88\5BF9\75C7\6CBB\7597\3002',
    U&'\738B\533B\751F', NOW() - INTERVAL '2 day', 'REVIEWED', 'demo reviewed first course record', false, NOW(), NOW()
  ),
  (
    'BL-DEMO-003', v_admission_id, v_patient_id, 'DAILY_COURSE_RECORD', U&'\65E5\5E38\75C5\7A0B\8BB0\5F55',
    U&'\65E5\5E38\75C5\7A0B\8BB0\5F55\FF1A\4ECA\65E5\54B3\55FD\8F83\524D\7F13\89E3\FF0C\7CBE\795E\53EF\3002\000A\5904\7406\610F\89C1\FF1A\7EE7\7EED\5F53\524D\6CBB\7597\65B9\6848\3002',
    U&'\738B\533B\751F', NOW() - INTERVAL '1 day', 'SUBMITTED', 'demo daily course record', false, NOW(), NOW()
  ),
  (
    'BL-DEMO-004', v_admission_id, v_patient_id, 'DISCHARGE_RECORD', U&'\51FA\9662\8BB0\5F55\8349\7A3F',
    U&'\51FA\9662\8BB0\5F55\FF1A\60A3\8005\75C7\72B6\597D\8F6C\FF0C\751F\547D\4F53\5F81\5E73\7A33\3002\000A\51FA\9662\533B\5631\FF1A\89C4\5F8B\670D\836F\FF0C\95E8\8BCA\590D\67E5\3002',
    U&'\738B\533B\751F', NOW(), 'DRAFT', 'demo discharge draft', false, NOW(), NOW()
  ),
  (
    'BL-DEMO-005', v_admission_id, v_patient_id, 'NURSING_RECORD', U&'\62A4\7406\8BB0\5F55',
    U&'\62A4\7406\8BB0\5F55\FF1A\60A3\8005\5728\9662\671F\95F4\914D\5408\6CBB\7597\FF0C\996E\98DF\7761\7720\53EF\3002\000A\62A4\7406\63AA\65BD\FF1A\5065\5EB7\5BA3\6559\FF0C\89C2\5BDF\751F\547D\4F53\5F81\3002',
    U&'\8D75\62A4\58EB', NOW(), 'DRAFT', 'demo nursing record', false, NOW(), NOW()
  );

  INSERT INTO bed (bed_no, ward_name, room_no, bed_type, status, current_admission_id, deleted, created_at, updated_at)
  VALUES ('B-DEMO-001', U&'\4E00\75C5\533A', '101', U&'\666E\901A\5E8A', 'OCCUPIED', v_admission_id, false, NOW(), NOW())
  RETURNING id INTO v_bed_id;

  INSERT INTO bed (bed_no, ward_name, room_no, bed_type, status, current_admission_id, deleted, created_at, updated_at)
  VALUES ('B-DEMO-002', U&'\4E00\75C5\533A', '102', U&'\666E\901A\5E8A', 'AVAILABLE', NULL, false, NOW(), NOW());

  INSERT INTO medical_order (
    order_no, admission_id, patient_id, order_type, order_category, item_name,
    dosage, dosage_unit, frequency, route, start_time, end_time, doctor_name,
    nurse_name, status, remark, deleted, created_at, updated_at
  )
  VALUES (
    'YZ-DEMO-001', v_admission_id, v_patient_id, 'LONG_TERM', 'DRUG', U&'\963F\83AB\897F\6797\80F6\56CA',
    '0.25', 'g', U&'\6BCF\65E5\4E09\6B21', U&'\53E3\670D', NOW(), NULL, U&'\738B\533B\751F',
    U&'\8D75\62A4\58EB', 'CHECKED', 'standard demo medical order', false, NOW(), NOW()
  )
  RETURNING id INTO v_order_id;

  INSERT INTO medical_order (
    order_no, admission_id, patient_id, order_type, order_category, item_name,
    dosage, dosage_unit, frequency, route, start_time, end_time, doctor_name,
    nurse_name, status, remark, deleted, created_at, updated_at
  )
  VALUES
  (
    'YZ-DEMO-002', v_admission_id, v_patient_id, 'TEMPORARY', 'EXAM', U&'\5934\9885CT\68C0\67E5',
    NULL, NULL, U&'\7ACB\5373\6267\884C', U&'\68C0\67E5\6267\884C', NOW(), NULL, U&'\738B\533B\751F',
    NULL, 'SUBMITTED', 'doctor station demo pending check order', false, NOW(), NOW()
  ),
  (
    'YZ-DEMO-003', v_admission_id, v_patient_id, 'TEMPORARY', 'LAB', U&'\8840\5E38\89C4\68C0\9A8C',
    NULL, NULL, U&'\7ACB\5373\6267\884C', U&'\68C0\9A8C\6267\884C', NOW(), NULL, U&'\738B\533B\751F',
    NULL, 'SUBMITTED', 'doctor station demo pending check order', false, NOW(), NOW()
  );

  INSERT INTO medical_order (
    order_no, admission_id, patient_id, order_type, order_category, item_name,
    dosage, dosage_unit, frequency, route, start_time, end_time, doctor_name,
    nurse_name, status, remark, deleted, created_at, updated_at
  )
  VALUES (
    'YZ-DEMO-004', v_admission_id, v_patient_id, 'TEMPORARY', 'TREATMENT', U&'\96FE\5316\5438\5165\6CBB\7597',
    NULL, NULL, U&'\7ACB\5373\6267\884C', U&'\6CBB\7597\6267\884C', NOW(), NULL, U&'\738B\533B\751F',
    U&'\8D75\62A4\58EB', 'EXECUTED', 'nurse station demo executed order', false, NOW(), NOW()
  )
  RETURNING id INTO v_executed_order_id;

  INSERT INTO medical_order (
    order_no, admission_id, patient_id, order_type, order_category, item_name,
    dosage, dosage_unit, frequency, route, start_time, end_time, doctor_name,
    nurse_name, status, remark, deleted, created_at, updated_at
  )
  VALUES (
    'YZ-DEMO-005', v_admission_id, v_patient_id, 'LONG_TERM', 'DRUG', U&'\5934\5B62\514B\809F\80F6\56CA',
    '0.1', 'g', U&'\6BCF\65E5\4E24\6B21', U&'\53E3\670D', NOW(), NULL, U&'\738B\533B\751F',
    U&'\8D75\62A4\58EB', 'CHECKED', 'pharmacy station demo dispensed order', false, NOW(), NOW()
  )
  RETURNING id INTO v_dispensed_order_id;

  INSERT INTO medical_order (
    order_no, admission_id, patient_id, order_type, order_category, item_name,
    dosage, dosage_unit, frequency, route, start_time, end_time, doctor_name,
    nurse_name, status, remark, deleted, created_at, updated_at
  )
  VALUES (
    'YZ-DEMO-006', v_admission_id, v_patient_id, 'LONG_TERM', 'DRUG', U&'\4F4E\5E93\5B58\6F14\793A\836F',
    '10', 'mg', U&'\6BCF\65E5\4E00\6B21', U&'\53E3\670D', NOW(), NULL, U&'\738B\533B\751F',
    U&'\8D75\62A4\58EB', 'CHECKED', 'pharmacy station low stock demo order', false, NOW(), NOW()
  );

  INSERT INTO nursing_order_execution (
    execution_no, order_id, admission_id, patient_id, scheduled_time, executed_time,
    nurse_name, status, result, remark, deleted, created_at, updated_at
  )
  VALUES (
    'ZX-DEMO-001', v_executed_order_id, v_admission_id, v_patient_id, NOW(), NOW(),
    U&'\8D75\62A4\58EB', 'EXECUTED', U&'\6267\884C\6210\529F', 'standard demo nursing execution', false, NOW(), NOW()
  );

  INSERT INTO medical_order (
    order_no, admission_id, patient_id, order_type, order_category, order_content, item_name,
    dosage, dosage_unit, frequency, route, start_time, end_time, doctor_name,
    execution_department, executor_name, nurse_name, status, unit_price, quantity, total_amount, billed,
    remark, deleted, created_at, updated_at
  )
  VALUES
  (
    'YZ-DEMO-007', v_admission_id, v_patient_id, 'TEMPORARY', 'NURSING', U&'\751F\547D\4F53\5F81\76D1\6D4B',
    U&'\751F\547D\4F53\5F81\76D1\6D4B', NULL, NULL, U&'\6BCF\73ED', U&'\62A4\7406\6267\884C', NOW(), NULL, U&'\738B\533B\751F',
    U&'\4E00\75C5\533A', NULL, NULL, 'DRAFT', 0, 1, 0, false, 'demo draft order', false, NOW(), NOW()
  ),
  (
    'YZ-DEMO-008', v_admission_id, v_patient_id, 'TEMPORARY', 'TREATMENT', U&'\5438\6C27\6CBB\7597',
    U&'\5438\6C27\6CBB\7597', NULL, NULL, U&'\7ACB\5373\6267\884C', U&'\6CBB\7597\6267\884C', NOW(), NULL, U&'\738B\533B\751F',
    U&'\4E00\75C5\533A', U&'\8D75\62A4\58EB', U&'\8D75\62A4\58EB', 'BILLED', 45.00, 1, 45.00, true, 'demo billed order', false, NOW(), NOW()
  );

  INSERT INTO nursing_record (
    record_no, order_id, admission_id, patient_id, nursing_type, nursing_content,
    temperature, pulse, respiration, blood_pressure, intake_amount, output_amount,
    nursing_level, nurse_name, record_time, status, billable, nursing_fee, billed,
    remark, deleted, created_at, updated_at
  )
  VALUES
  ('HL-DEMO-001', NULL, v_admission_id, v_patient_id, 'VITAL_SIGN', U&'\751F\547D\4F53\5F81\8BB0\5F55',
   37.80, 102, 20, '130/82', NULL, NULL, U&'\4E8C\7EA7\62A4\7406', U&'\8D75\62A4\58EB', NOW(), 'RECORDED', false, 0, false,
   'demo abnormal vital sign', false, NOW(), NOW()),
  ('HL-DEMO-002', NULL, v_admission_id, v_patient_id, 'DAILY_CARE', U&'\65E5\5E38\62A4\7406',
   NULL, NULL, NULL, NULL, 500, 300, U&'\4E8C\7EA7\62A4\7406', U&'\8D75\62A4\58EB', NOW(), 'EXECUTED', true, 30.00, false,
   'demo daily nursing', false, NOW(), NOW()),
  ('HL-DEMO-003', v_executed_order_id, v_admission_id, v_patient_id, 'ORDER_EXECUTION', U&'\533B\5631\6267\884C\62A4\7406',
   NULL, NULL, NULL, NULL, NULL, NULL, U&'\4E8C\7EA7\62A4\7406', U&'\8D75\62A4\58EB', NOW(), 'EXECUTED', false, 0, false,
   'demo order execution nursing', false, NOW(), NOW()),
  ('HL-DEMO-004', NULL, v_admission_id, v_patient_id, 'INFUSION', U&'\8F93\6DB2\62A4\7406',
   NULL, NULL, NULL, NULL, NULL, NULL, U&'\4E8C\7EA7\62A4\7406', U&'\8D75\62A4\58EB', NOW(), 'BILLED', true, 25.00, true,
   'demo billed nursing', false, NOW(), NOW());

  INSERT INTO exam_lab_request (
    request_no, admission_id, patient_id, request_type, item_code, item_name, request_content,
    item_category, doctor_name, request_time, scheduled_time, execution_department, executor_name,
    executed_time, report_doctor_name, report_time, result_summary, result_detail, abnormal_flag,
    status, unit_price, quantity, total_amount, billed, remark, deleted, created_at, updated_at
  )
  VALUES
  ('JY-DEMO-001', v_admission_id, v_patient_id, 'EXAM', 'EXAM-CT', U&'\80F8\90E8 CT', U&'\80F8\90E8 CT \68C0\67E5',
   'IMAGING', U&'\738B\533B\751F', NOW(), NULL, U&'\5F71\50CF\79D1', NULL, NULL, NULL, NULL, NULL, NULL, false,
   'REQUESTED', 180.00, 1, 180.00, false, 'demo requested exam', false, NOW(), NOW()),
  ('JY-DEMO-002', v_admission_id, v_patient_id, 'LAB', 'LAB-BLOOD', U&'\8840\5E38\89C4', U&'\8840\5E38\89C4\68C0\9A8C',
   'BLOOD', U&'\738B\533B\751F', NOW(), NOW() + INTERVAL '1 hour', U&'\68C0\9A8C\79D1', NULL, NULL, NULL, NULL, NULL, NULL, false,
   'SCHEDULED', 35.00, 1, 35.00, false, 'demo scheduled lab', false, NOW(), NOW()),
  ('JY-DEMO-003', v_admission_id, v_patient_id, 'EXAM', 'EXAM-ECG', U&'\5FC3\7535\56FE', U&'\5FC3\7535\56FE\68C0\67E5',
   'ECG', U&'\738B\533B\751F', NOW(), NOW(), U&'\5FC3\7535\56FE\5BA4', U&'\6280\5E08', NOW(), U&'\62A5\544A\533B\751F', NOW(),
   U&'\7AA6\6027\5FC3\5F8B', U&'\5EFA\8BAE\7ED3\5408\4E34\5E8A', false,
   'REPORTED', 45.00, 1, 45.00, false, 'demo reported exam', false, NOW(), NOW()),
  ('JY-DEMO-004', v_admission_id, v_patient_id, 'LAB', 'LAB-CRP', U&'C \53CD\5E94\86CB\767D', U&'C \53CD\5E94\86CB\767D\68C0\9A8C',
   'BIOCHEMISTRY', U&'\738B\533B\751F', NOW(), NOW(), U&'\68C0\9A8C\79D1', U&'\6280\5E08', NOW(), U&'\62A5\544A\533B\751F', NOW(),
   U&'CRP \5347\9AD8', U&'\63D0\793A\708E\75C7\6307\6807\5F02\5E38', true,
   'REPORTED', 55.00, 1, 55.00, false, 'demo abnormal lab', false, NOW(), NOW()),
  ('JY-DEMO-005', v_admission_id, v_patient_id, 'EXAM', 'EXAM-US', U&'\8179\90E8\8D85\58F0', U&'\8179\90E8\8D85\58F0\68C0\67E5',
   'ULTRASOUND', U&'\738B\533B\751F', NOW(), NOW(), U&'\8D85\58F0\79D1', U&'\6280\5E08', NOW(), U&'\62A5\544A\533B\751F', NOW(),
   U&'\672A\89C1\660E\663E\5F02\5E38', U&'\8179\90E8\8D85\58F0\672A\89C1\660E\663E\5F02\5E38', false,
   'BILLED', 120.00, 1, 120.00, true, 'demo billed exam', false, NOW(), NOW());

  INSERT INTO pharmacy_dispense (
    dispense_no, order_id, admission_id, patient_id, drug_id, quantity, pharmacist_name,
    dispense_time, return_time, status, remark, deleted, created_at, updated_at
  )
  VALUES (
    'FY-DEMO-001', v_dispensed_order_id, v_admission_id, v_patient_id, v_dispensed_drug_id, 2, U&'\836F\5E08',
    NOW(), NULL, 'DISPENSED', 'standard demo dispensed record', false, NOW(), NOW()
  )
  RETURNING id INTO v_dispense_id;

  INSERT INTO surgery_operation (
    surgery_no, patient_id, admission_id, surgery_name, preoperative_diagnosis,
    surgery_level, surgery_type, operating_room, planned_time, actual_start_time,
    actual_end_time, primary_doctor_name, assistant_doctor_name, anesthesia_method,
    anesthesiologist_name, status, surgery_fee, remark, deleted, created_at, updated_at
  )
  VALUES
  ('SS-DEMO-001', v_patient_id, v_admission_id, U&'\80C6\56CA\5207\9664\672F', U&'\6025\6027\8179\75DB\5F85\67E5',
   'II', U&'\62E9\671F', 'OR-1', NOW() + INTERVAL '1 day', NULL, NULL,
   U&'\738B\533B\751F', U&'\674E\533B\751F', U&'\5168\8EAB\9EBB\9189', U&'\5F20\9EBB\9189',
   'APPLIED', 3200.00, 'standard demo surgery applied', false, NOW(), NOW()),
  ('SS-DEMO-002', v_patient_id, v_admission_id, U&'\80C3\955C\4E0B\606F\8089\5207\9664\672F', U&'\6025\6027\8179\75DB\5F85\67E5',
   'I', U&'\62E9\671F', 'OR-2', NOW() + INTERVAL '2 day', NULL, NULL,
   U&'\738B\533B\751F', U&'\674E\533B\751F', U&'\5168\8EAB\9EBB\9189', U&'\5F20\9EBB\9189',
   'SCHEDULED', 1800.00, 'standard demo surgery scheduled', false, NOW(), NOW());

  INSERT INTO surgery_operation (
    surgery_no, patient_id, admission_id, surgery_name, preoperative_diagnosis,
    surgery_level, surgery_type, operating_room, planned_time, actual_start_time,
    actual_end_time, primary_doctor_name, assistant_doctor_name, anesthesia_method,
    anesthesiologist_name, status, surgery_fee, remark, deleted, created_at, updated_at
  )
  VALUES (
    'SS-DEMO-003', v_patient_id, v_admission_id, U&'\8179\8154\955C\9611\5C3E\5207\9664\672F', U&'\6025\6027\8179\75DB\5F85\67E5',
    'II', U&'\62E9\671F', 'OR-1', NOW() - INTERVAL '3 hour', NOW() - INTERVAL '2 hour', NOW() - INTERVAL '1 hour',
    U&'\738B\533B\751F', U&'\674E\533B\751F', U&'\5168\8EAB\9EBB\9189', U&'\5F20\9EBB\9189',
    'COMPLETED', 2800.00, 'standard demo surgery completed', false, NOW(), NOW()
  )
  RETURNING id INTO v_completed_surgery_id;

  INSERT INTO surgery_operation (
    surgery_no, patient_id, admission_id, surgery_name, preoperative_diagnosis,
    surgery_level, surgery_type, operating_room, planned_time, actual_start_time,
    actual_end_time, primary_doctor_name, assistant_doctor_name, anesthesia_method,
    anesthesiologist_name, status, surgery_fee, remark, deleted, created_at, updated_at
  )
  VALUES (
    'SS-DEMO-004', v_patient_id, v_admission_id, U&'\7532\72B6\817A\7ED3\8282\5207\9664\672F', U&'\6025\6027\8179\75DB\5F85\67E5',
    'II', U&'\62E9\671F', 'OR-3', NOW() - INTERVAL '2 day', NOW() - INTERVAL '2 day', NOW() - INTERVAL '2 day' + INTERVAL '1 hour',
    U&'\738B\533B\751F', U&'\674E\533B\751F', U&'\5168\8EAB\9EBB\9189', U&'\5F20\9EBB\9189',
    'BILLED', 2600.00, 'standard demo surgery billed', false, NOW(), NOW()
  )
  RETURNING id INTO v_billed_surgery_id;

  INSERT INTO inpatient_fee (
    fee_no, admission_id, patient_id, source_type, source_id, item_code, item_name,
    item_category, quantity, unit, unit_price, total_amount, fee_time, status,
    remark, deleted, created_at, updated_at
  )
  VALUES
  (
    'FEE-DEMO-001', v_admission_id, v_patient_id, 'ORDER', v_order_id, 'DRUG-DEMO-001', U&'\963F\83AB\897F\6797\80F6\56CA',
    'DRUG', 2.00, U&'\76D2', 15.50, 31.00, NOW(), 'UNSETTLED',
    U&'\836F\54C1\8D39\7528', false, NOW(), NOW()
  ),
  (
    'FEE-DEMO-002', v_admission_id, v_patient_id, 'BED', v_bed_id, 'BED-DEMO-001', U&'\5E8A\4F4D\8D39',
    'BED', 1.00, U&'\5929', 80.00, 80.00, NOW(), 'UNSETTLED',
    U&'\5E8A\4F4D\8D39', false, NOW(), NOW()
  ),
  (
    'FEE-DEMO-003', v_admission_id, v_patient_id, 'DRUG_DISPENSE', v_dispense_id, 'DRUG-DEMO-002', U&'\5934\5B62\514B\809F\80F6\56CA',
    'DRUG', 2.00, U&'\76D2', 28.00, 56.00, NOW(), 'UNSETTLED',
    U&'\53D1\836F\81EA\52A8\8D39\7528', false, NOW(), NOW()
  ),
  (
    'FEE-DEMO-004', v_admission_id, v_patient_id, 'DRUG_RETURN', v_dispense_id, 'DRUG-DEMO-002', U&'\9000\836F\51B2\6B63-\5934\5B62\514B\809F\80F6\56CA',
    'DRUG', -1.00, U&'\76D2', 28.00, -28.00, NOW(), 'UNSETTLED',
    U&'\9000\836F\8D1F\8D39\7528/\51B2\6B63\8D39\7528', false, NOW(), NOW()
  ),
  (
    'FEE-DEMO-005', v_admission_id, v_patient_id, 'SURGERY', v_billed_surgery_id, 'SS-DEMO-004', U&'\7532\72B6\817A\7ED3\8282\5207\9664\672F',
    'SURGERY', 1.00, U&'\53F0', 2600.00, 2600.00, NOW(), 'UNSETTLED',
    U&'\624B\672F\8D39\7528', false, NOW(), NOW()
  ),
  (
    'FEE-DEMO-006', v_admission_id, v_patient_id, 'ORDER', NULL, 'YZ-DEMO-008', U&'\5438\6C27\6CBB\7597',
    'TREATMENT', 1.00, U&'\6B21', 45.00, 45.00, NOW(), 'UNSETTLED',
    U&'\533B\5631\8D39\7528', false, NOW(), NOW()
  ),
  (
    'FEE-DEMO-007', v_admission_id, v_patient_id, 'NURSING', NULL, 'HL-DEMO-004', U&'\8F93\6DB2\62A4\7406',
    'NURSING', 1.00, U&'\6B21', 25.00, 25.00, NOW(), 'UNSETTLED',
    U&'\62A4\7406\8D39\7528', false, NOW(), NOW()
  ),
  (
    'FEE-DEMO-008', v_admission_id, v_patient_id, 'EXAM_LAB', NULL, 'JY-DEMO-005', U&'\8179\90E8\8D85\58F0',
    'EXAM', 1.00, U&'\6B21', 120.00, 120.00, NOW(), 'UNSETTLED',
    U&'\68C0\67E5\8D39\7528', false, NOW(), NOW()
  );

  INSERT INTO inpatient_deposit (
    deposit_no, admission_id, patient_id, amount, payment_method, transaction_type,
    transaction_time, operator_name, status, remark, deleted, created_at, updated_at
  )
  VALUES (
    'DEP-DEMO-001', v_admission_id, v_patient_id, 500.00, 'CASH', 'PAY',
    NOW(), U&'\6536\8D39\5458', 'SUCCESS', U&'\9884\4EA4\91D1', false, NOW(), NOW()
  );

  INSERT INTO inpatient_discharge (
    discharge_no, admission_id, patient_id, total_fee_amount, total_deposit_amount, total_refund_amount,
    deposit_balance, unpaid_amount, actual_payment, discharge_diagnosis, discharge_summary, discharge_order,
    take_home_drug_instruction, apply_doctor, apply_time, status, discharge_time, operator_name,
    remark, deleted, created_at, updated_at
  )
  VALUES (
    'CY-DEMO-001', v_admission_id, v_patient_id, 2929.00, 500.00, 0.00,
    500.00, 2429.00, 2429.00, U&'\80BA\90E8\611F\67D3\597D\8F6C',
    U&'\60A3\8005\75C7\72B6\8F83\524D\597D\8F6C\FF0C\53EF\8FDB\884C\51FA\9662\7ED3\7B97',
    U&'\51FA\9662\540E\6309\65F6\590D\67E5',
    U&'\6309\533B\5631\670D\836F',
    U&'\738B\533B\751F', NOW(), 'DRAFT', NOW(), U&'\6536\8D39\5458',
    'demo discharge pending settlement', false, NOW(), NOW()
  );

  INSERT INTO inpatient_fee (
    fee_no, admission_id, patient_id, source_type, source_id, item_code, item_name,
    item_category, quantity, unit, unit_price, total_amount, fee_time, status,
    remark, deleted, created_at, updated_at
  )
  VALUES (
    'FEE-DEMO-009', v_discharged_admission_id, v_discharged_patient_id, 'OTHER', NULL, 'OTHER-DEMO-001', U&'\4F4F\9662\7EFC\5408\8D39',
    'OTHER', 1.00, U&'\6B21', 300.00, 300.00, NOW() - INTERVAL '1 day', 'SETTLED',
    U&'\5DF2\51FA\9662\60A3\8005\5DF2\7ED3\8D39\7528', false, NOW(), NOW()
  );

  INSERT INTO inpatient_deposit (
    deposit_no, admission_id, patient_id, amount, payment_method, transaction_type,
    transaction_time, operator_name, status, remark, deleted, created_at, updated_at
  )
  VALUES (
    'DEP-DEMO-002', v_discharged_admission_id, v_discharged_patient_id, 300.00, 'WECHAT', 'PAY',
    NOW() - INTERVAL '2 day', U&'\6536\8D39\5458', 'SUCCESS', U&'\5DF2\51FA\9662\60A3\8005\9884\4EA4\91D1', false, NOW(), NOW()
  );

  INSERT INTO inpatient_discharge (
    discharge_no, admission_id, patient_id, total_fee_amount, total_deposit_amount, total_refund_amount,
    deposit_balance, unpaid_amount, actual_payment, discharge_diagnosis, discharge_summary, discharge_order,
    take_home_drug_instruction, apply_doctor, apply_time, status, discharge_time, operator_name,
    remark, deleted, created_at, updated_at
  )
  VALUES (
    'CY-DEMO-002', v_discharged_admission_id, v_discharged_patient_id, 300.00, 300.00, 0.00,
    0.00, 0.00, 0.00, U&'\6025\6027\80C3\80A0\708E\6CBB\6108',
    U&'\60A3\8005\75C7\72B6\597D\8F6C\FF0C\5DF2\5B8C\6210\51FA\9662\7ED3\7B97',
    U&'\51FA\9662\540E\95E8\8BCA\590D\67E5',
    U&'\6E05\6DE1\996E\98DF',
    U&'\738B\533B\751F', NOW() - INTERVAL '1 day', 'SETTLED', NOW() - INTERVAL '1 day', U&'\6536\8D39\5458',
    'demo settled discharged patient', false, NOW(), NOW()
  );

  INSERT INTO patient_appointment (
    patient_id, admission_id, appointment_type, appointment_item, appointment_date,
    time_slot, contact_phone, remark, status, complete_time, deleted, created_at, updated_at
  )
  VALUES
  (
    v_patient_id, v_admission_id, 'EXAM_APPOINTMENT', U&'\80F8\90E8CT\590D\67E5',
    CURRENT_DATE, U&'\4E0A\5348', '13900000001', U&'\79FB\52A8\7AEF\5F85\786E\8BA4\9884\7EA6', 'REQUESTED',
    NULL, false, NOW(), NOW()
  ),
  (
    v_patient_id, v_admission_id, 'FOLLOW_UP', U&'\51FA\9662\540E\590D\8BCA',
    CURRENT_DATE - INTERVAL '1 day', U&'\4E0B\5348', '13900000001', U&'\5DF2\5B8C\6210\590D\8BCA\9884\7EA6', 'COMPLETED',
    NOW() - INTERVAL '1 day', false, NOW(), NOW()
  );

  INSERT INTO dict_item (dict_type, dict_code, dict_name, sort_order, status, remark, deleted, created_at, updated_at)
  VALUES
    ('charge_type', 'MEDICAL_INSURANCE', U&'\533B\4FDD', 1, 'ENABLED', 'standard demo dict', false, NOW(), NOW()),
    ('charge_type', 'SELF_PAY', U&'\81EA\8D39', 2, 'ENABLED', 'standard demo dict', false, NOW(), NOW());
END $$;

SELECT
  p.id AS patient_id,
  p.name,
  p.address,
  a.id AS admission_id,
  a.status AS admission_status
FROM patients p
JOIN inpatient_admission a ON a.patient_id = p.id
WHERE p.patient_no = 'P-DEMO-001';
'@

$tempSql = Join-Path $env:TEMP ("his-inpatient-reset-{0}.sql" -f ([Guid]::NewGuid().ToString("N")))
try {
  $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
  [System.IO.File]::WriteAllText($tempSql, $sql, $utf8NoBom)
  & $psqlPath -h $dbHost -p $dbPort -U $dbUser -d $dbName -v ON_ERROR_STOP=1 -f $tempSql
  if ($LASTEXITCODE -ne 0) {
    throw ("psql failed with exit code {0}." -f $LASTEXITCODE)
  }
}
finally {
  if (Test-Path $tempSql) {
    Remove-Item -LiteralPath $tempSql -Force
  }
}

Write-Host "Demo data reset completed." -ForegroundColor Green
Write-Host "Standard patient: Li Si; address: Jiangsu Province, Nanjing, Xuanwu District." -ForegroundColor Green
