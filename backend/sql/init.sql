SET client_encoding = 'UTF8';

CREATE TABLE IF NOT EXISTS patients (
    id BIGSERIAL PRIMARY KEY,
    patient_no VARCHAR(32) NOT NULL,
    name VARCHAR(50) NOT NULL,
    gender VARCHAR(10),
    id_card VARCHAR(32),
    phone VARCHAR(20),
    birth_date DATE,
    address VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE patients ADD COLUMN IF NOT EXISTS patient_no VARCHAR(32);
ALTER TABLE patients ADD COLUMN IF NOT EXISTS name VARCHAR(50);
ALTER TABLE patients ADD COLUMN IF NOT EXISTS gender VARCHAR(10);
ALTER TABLE patients ADD COLUMN IF NOT EXISTS id_card VARCHAR(32);
ALTER TABLE patients ADD COLUMN IF NOT EXISTS phone VARCHAR(20);
ALTER TABLE patients ADD COLUMN IF NOT EXISTS birth_date DATE;
ALTER TABLE patients ADD COLUMN IF NOT EXISTS address VARCHAR(255);
ALTER TABLE patients ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE patients ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE patients ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE UNIQUE INDEX IF NOT EXISTS uk_patients_patient_no ON patients (patient_no);
CREATE UNIQUE INDEX IF NOT EXISTS uk_patients_id_card ON patients (id_card) WHERE id_card IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uk_patients_phone ON patients (phone) WHERE phone IS NOT NULL;

CREATE TABLE IF NOT EXISTS inpatient_admission (
    id BIGSERIAL PRIMARY KEY,
    admission_no VARCHAR(32) NOT NULL,
    patient_id BIGINT NOT NULL,
    department_name VARCHAR(50) NOT NULL,
    ward_name VARCHAR(50),
    admission_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    discharge_time TIMESTAMP,
    admission_diagnosis VARCHAR(255),
    charge_type VARCHAR(30),
    nursing_level VARCHAR(30),
    doctor_name VARCHAR(50),
    status VARCHAR(30) NOT NULL DEFAULT 'REGISTERED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE inpatient_admission ADD COLUMN IF NOT EXISTS admission_no VARCHAR(32);
ALTER TABLE inpatient_admission ADD COLUMN IF NOT EXISTS patient_id BIGINT;
ALTER TABLE inpatient_admission ADD COLUMN IF NOT EXISTS department_name VARCHAR(50);
ALTER TABLE inpatient_admission ADD COLUMN IF NOT EXISTS ward_name VARCHAR(50);
ALTER TABLE inpatient_admission ADD COLUMN IF NOT EXISTS admission_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE inpatient_admission ADD COLUMN IF NOT EXISTS discharge_time TIMESTAMP;
ALTER TABLE inpatient_admission ADD COLUMN IF NOT EXISTS admission_diagnosis VARCHAR(255);
ALTER TABLE inpatient_admission ADD COLUMN IF NOT EXISTS charge_type VARCHAR(30);
ALTER TABLE inpatient_admission ADD COLUMN IF NOT EXISTS nursing_level VARCHAR(30);
ALTER TABLE inpatient_admission ADD COLUMN IF NOT EXISTS doctor_name VARCHAR(50);
ALTER TABLE inpatient_admission ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'REGISTERED';
ALTER TABLE inpatient_admission ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE inpatient_admission ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE UNIQUE INDEX IF NOT EXISTS uk_inpatient_admission_admission_no ON inpatient_admission (admission_no);
CREATE INDEX IF NOT EXISTS idx_inpatient_admission_patient_id ON inpatient_admission (patient_id);
CREATE INDEX IF NOT EXISTS idx_inpatient_admission_status ON inpatient_admission (status);
CREATE INDEX IF NOT EXISTS idx_inpatient_admission_admission_time ON inpatient_admission (admission_time);

CREATE TABLE IF NOT EXISTS bed (
    id BIGSERIAL PRIMARY KEY,
    bed_no VARCHAR(32) NOT NULL,
    ward_name VARCHAR(50) NOT NULL,
    room_no VARCHAR(32),
    bed_type VARCHAR(30),
    status VARCHAR(30) NOT NULL DEFAULT 'EMPTY',
    current_admission_id BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE bed ADD COLUMN IF NOT EXISTS bed_no VARCHAR(32);
ALTER TABLE bed ADD COLUMN IF NOT EXISTS ward_name VARCHAR(50);
ALTER TABLE bed ADD COLUMN IF NOT EXISTS room_no VARCHAR(32);
ALTER TABLE bed ADD COLUMN IF NOT EXISTS bed_type VARCHAR(30);
ALTER TABLE bed ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'EMPTY';
ALTER TABLE bed ADD COLUMN IF NOT EXISTS current_admission_id BIGINT;
ALTER TABLE bed ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE bed ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE bed ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

UPDATE bed SET deleted = true WHERE status = 'DELETED';
UPDATE bed SET status = 'DISABLED' WHERE status = 'DELETED';

DROP INDEX IF EXISTS uk_bed_ward_bed_no;
DROP INDEX IF EXISTS uk_bed_current_admission;

CREATE UNIQUE INDEX IF NOT EXISTS uk_bed_ward_bed_no ON bed (ward_name, bed_no) WHERE deleted = false;
CREATE UNIQUE INDEX IF NOT EXISTS uk_bed_current_admission ON bed (current_admission_id) WHERE current_admission_id IS NOT NULL AND status = 'OCCUPIED' AND deleted = false;
CREATE INDEX IF NOT EXISTS idx_bed_status ON bed (status);
CREATE INDEX IF NOT EXISTS idx_bed_ward_name ON bed (ward_name);
CREATE INDEX IF NOT EXISTS idx_bed_deleted ON bed (deleted);

CREATE TABLE IF NOT EXISTS medical_order (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL,
    admission_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    order_type VARCHAR(30) NOT NULL,
    order_category VARCHAR(30) NOT NULL,
    item_name VARCHAR(100) NOT NULL,
    dosage VARCHAR(50),
    dosage_unit VARCHAR(20),
    frequency VARCHAR(50),
    route VARCHAR(50),
    start_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP,
    doctor_name VARCHAR(50) NOT NULL,
    nurse_name VARCHAR(50),
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    remark VARCHAR(255),
    deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE medical_order ADD COLUMN IF NOT EXISTS order_no VARCHAR(32);
ALTER TABLE medical_order ADD COLUMN IF NOT EXISTS admission_id BIGINT;
ALTER TABLE medical_order ADD COLUMN IF NOT EXISTS patient_id BIGINT;
ALTER TABLE medical_order ADD COLUMN IF NOT EXISTS order_type VARCHAR(30);
ALTER TABLE medical_order ADD COLUMN IF NOT EXISTS order_category VARCHAR(30);
ALTER TABLE medical_order ADD COLUMN IF NOT EXISTS order_content VARCHAR(500);
ALTER TABLE medical_order ADD COLUMN IF NOT EXISTS item_name VARCHAR(100);
ALTER TABLE medical_order ADD COLUMN IF NOT EXISTS dosage VARCHAR(50);
ALTER TABLE medical_order ADD COLUMN IF NOT EXISTS dosage_unit VARCHAR(20);
ALTER TABLE medical_order ADD COLUMN IF NOT EXISTS frequency VARCHAR(50);
ALTER TABLE medical_order ADD COLUMN IF NOT EXISTS route VARCHAR(50);
ALTER TABLE medical_order ADD COLUMN IF NOT EXISTS start_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE medical_order ADD COLUMN IF NOT EXISTS end_time TIMESTAMP;
ALTER TABLE medical_order ADD COLUMN IF NOT EXISTS doctor_name VARCHAR(50);
ALTER TABLE medical_order ADD COLUMN IF NOT EXISTS execution_department VARCHAR(50);
ALTER TABLE medical_order ADD COLUMN IF NOT EXISTS executor_name VARCHAR(50);
ALTER TABLE medical_order ADD COLUMN IF NOT EXISTS executed_time TIMESTAMP;
ALTER TABLE medical_order ADD COLUMN IF NOT EXISTS nurse_name VARCHAR(50);
ALTER TABLE medical_order ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'DRAFT';
ALTER TABLE medical_order ADD COLUMN IF NOT EXISTS unit_price NUMERIC(12,2) NOT NULL DEFAULT 0;
ALTER TABLE medical_order ADD COLUMN IF NOT EXISTS quantity NUMERIC(12,2) NOT NULL DEFAULT 1;
ALTER TABLE medical_order ADD COLUMN IF NOT EXISTS total_amount NUMERIC(12,2) NOT NULL DEFAULT 0;
ALTER TABLE medical_order ADD COLUMN IF NOT EXISTS billed BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE medical_order ADD COLUMN IF NOT EXISTS remark VARCHAR(255);
ALTER TABLE medical_order ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE medical_order ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE medical_order ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE UNIQUE INDEX IF NOT EXISTS uk_medical_order_order_no ON medical_order (order_no);
CREATE INDEX IF NOT EXISTS idx_medical_order_admission_id ON medical_order (admission_id);
CREATE INDEX IF NOT EXISTS idx_medical_order_patient_id ON medical_order (patient_id);
CREATE INDEX IF NOT EXISTS idx_medical_order_order_type ON medical_order (order_type);
CREATE INDEX IF NOT EXISTS idx_medical_order_order_category ON medical_order (order_category);
CREATE INDEX IF NOT EXISTS idx_medical_order_status ON medical_order (status);
CREATE INDEX IF NOT EXISTS idx_medical_order_billed ON medical_order (billed);
CREATE INDEX IF NOT EXISTS idx_medical_order_deleted ON medical_order (deleted);

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

ALTER TABLE nursing_record ADD COLUMN IF NOT EXISTS record_no VARCHAR(32);
ALTER TABLE nursing_record ADD COLUMN IF NOT EXISTS order_id BIGINT;
ALTER TABLE nursing_record ADD COLUMN IF NOT EXISTS admission_id BIGINT;
ALTER TABLE nursing_record ADD COLUMN IF NOT EXISTS patient_id BIGINT;
ALTER TABLE nursing_record ADD COLUMN IF NOT EXISTS nursing_type VARCHAR(30);
ALTER TABLE nursing_record ADD COLUMN IF NOT EXISTS nursing_content VARCHAR(500);
ALTER TABLE nursing_record ADD COLUMN IF NOT EXISTS temperature NUMERIC(5,2);
ALTER TABLE nursing_record ADD COLUMN IF NOT EXISTS pulse INTEGER;
ALTER TABLE nursing_record ADD COLUMN IF NOT EXISTS respiration INTEGER;
ALTER TABLE nursing_record ADD COLUMN IF NOT EXISTS blood_pressure VARCHAR(30);
ALTER TABLE nursing_record ADD COLUMN IF NOT EXISTS intake_amount NUMERIC(12,2);
ALTER TABLE nursing_record ADD COLUMN IF NOT EXISTS output_amount NUMERIC(12,2);
ALTER TABLE nursing_record ADD COLUMN IF NOT EXISTS nursing_level VARCHAR(30);
ALTER TABLE nursing_record ADD COLUMN IF NOT EXISTS nurse_name VARCHAR(50);
ALTER TABLE nursing_record ADD COLUMN IF NOT EXISTS record_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE nursing_record ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'RECORDED';
ALTER TABLE nursing_record ADD COLUMN IF NOT EXISTS billable BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE nursing_record ADD COLUMN IF NOT EXISTS nursing_fee NUMERIC(12,2) NOT NULL DEFAULT 0;
ALTER TABLE nursing_record ADD COLUMN IF NOT EXISTS billed BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE nursing_record ADD COLUMN IF NOT EXISTS remark VARCHAR(255);
ALTER TABLE nursing_record ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE nursing_record ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE nursing_record ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE UNIQUE INDEX IF NOT EXISTS uk_nursing_record_no ON nursing_record (record_no) WHERE deleted = false;
CREATE INDEX IF NOT EXISTS idx_nursing_record_order_id ON nursing_record (order_id);
CREATE INDEX IF NOT EXISTS idx_nursing_record_admission_id ON nursing_record (admission_id);
CREATE INDEX IF NOT EXISTS idx_nursing_record_patient_id ON nursing_record (patient_id);
CREATE INDEX IF NOT EXISTS idx_nursing_record_type ON nursing_record (nursing_type);
CREATE INDEX IF NOT EXISTS idx_nursing_record_status ON nursing_record (status);
CREATE INDEX IF NOT EXISTS idx_nursing_record_billed ON nursing_record (billed);
CREATE INDEX IF NOT EXISTS idx_nursing_record_deleted ON nursing_record (deleted);

CREATE TABLE IF NOT EXISTS nursing_order_execution (
    id BIGSERIAL PRIMARY KEY,
    execution_no VARCHAR(32) NOT NULL,
    order_id BIGINT NOT NULL,
    admission_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    scheduled_time TIMESTAMP,
    executed_time TIMESTAMP,
    nurse_name VARCHAR(50),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    result VARCHAR(255),
    remark VARCHAR(255),
    deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE nursing_order_execution ADD COLUMN IF NOT EXISTS execution_no VARCHAR(32);
ALTER TABLE nursing_order_execution ADD COLUMN IF NOT EXISTS order_id BIGINT;
ALTER TABLE nursing_order_execution ADD COLUMN IF NOT EXISTS admission_id BIGINT;
ALTER TABLE nursing_order_execution ADD COLUMN IF NOT EXISTS patient_id BIGINT;
ALTER TABLE nursing_order_execution ADD COLUMN IF NOT EXISTS scheduled_time TIMESTAMP;
ALTER TABLE nursing_order_execution ADD COLUMN IF NOT EXISTS executed_time TIMESTAMP;
ALTER TABLE nursing_order_execution ADD COLUMN IF NOT EXISTS nurse_name VARCHAR(50);
ALTER TABLE nursing_order_execution ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'PENDING';
ALTER TABLE nursing_order_execution ADD COLUMN IF NOT EXISTS result VARCHAR(255);
ALTER TABLE nursing_order_execution ADD COLUMN IF NOT EXISTS remark VARCHAR(255);
ALTER TABLE nursing_order_execution ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE nursing_order_execution ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE nursing_order_execution ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE UNIQUE INDEX IF NOT EXISTS uk_nursing_execution_no ON nursing_order_execution (execution_no);
CREATE INDEX IF NOT EXISTS idx_nursing_execution_order_id ON nursing_order_execution (order_id);
CREATE INDEX IF NOT EXISTS idx_nursing_execution_admission_id ON nursing_order_execution (admission_id);
CREATE INDEX IF NOT EXISTS idx_nursing_execution_status ON nursing_order_execution (status);
CREATE INDEX IF NOT EXISTS idx_nursing_execution_deleted ON nursing_order_execution (deleted);

CREATE TABLE IF NOT EXISTS drug (
    id BIGSERIAL PRIMARY KEY,
    drug_code VARCHAR(32) NOT NULL,
    drug_name VARCHAR(100) NOT NULL,
    specification VARCHAR(100),
    unit VARCHAR(20),
    price NUMERIC(12,2) NOT NULL DEFAULT 0,
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    stock_lower_limit INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'ENABLED',
    deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE drug ADD COLUMN IF NOT EXISTS drug_code VARCHAR(32);
ALTER TABLE drug ADD COLUMN IF NOT EXISTS drug_name VARCHAR(100);
ALTER TABLE drug ADD COLUMN IF NOT EXISTS specification VARCHAR(100);
ALTER TABLE drug ADD COLUMN IF NOT EXISTS unit VARCHAR(20);
ALTER TABLE drug ADD COLUMN IF NOT EXISTS price NUMERIC(12,2) NOT NULL DEFAULT 0;
ALTER TABLE drug ADD COLUMN IF NOT EXISTS stock_quantity INTEGER NOT NULL DEFAULT 0;
ALTER TABLE drug ADD COLUMN IF NOT EXISTS stock_lower_limit INTEGER NOT NULL DEFAULT 0;
ALTER TABLE drug ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'ENABLED';
ALTER TABLE drug ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE drug ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE drug ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE UNIQUE INDEX IF NOT EXISTS uk_drug_code ON drug (drug_code) WHERE deleted = false;
CREATE INDEX IF NOT EXISTS idx_drug_name ON drug (drug_name);
CREATE INDEX IF NOT EXISTS idx_drug_status ON drug (status);
CREATE INDEX IF NOT EXISTS idx_drug_deleted ON drug (deleted);

CREATE TABLE IF NOT EXISTS pharmacy_dispense (
    id BIGSERIAL PRIMARY KEY,
    dispense_no VARCHAR(32) NOT NULL,
    order_id BIGINT NOT NULL,
    admission_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    drug_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    pharmacist_name VARCHAR(50),
    dispense_time TIMESTAMP,
    return_time TIMESTAMP,
    status VARCHAR(30) NOT NULL DEFAULT 'CREATED',
    remark VARCHAR(255),
    deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE pharmacy_dispense ADD COLUMN IF NOT EXISTS dispense_no VARCHAR(32);
ALTER TABLE pharmacy_dispense ADD COLUMN IF NOT EXISTS order_id BIGINT;
ALTER TABLE pharmacy_dispense ADD COLUMN IF NOT EXISTS admission_id BIGINT;
ALTER TABLE pharmacy_dispense ADD COLUMN IF NOT EXISTS patient_id BIGINT;
ALTER TABLE pharmacy_dispense ADD COLUMN IF NOT EXISTS drug_id BIGINT;
ALTER TABLE pharmacy_dispense ADD COLUMN IF NOT EXISTS quantity INTEGER;
ALTER TABLE pharmacy_dispense ADD COLUMN IF NOT EXISTS pharmacist_name VARCHAR(50);
ALTER TABLE pharmacy_dispense ADD COLUMN IF NOT EXISTS dispense_time TIMESTAMP;
ALTER TABLE pharmacy_dispense ADD COLUMN IF NOT EXISTS return_time TIMESTAMP;
ALTER TABLE pharmacy_dispense ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'CREATED';
ALTER TABLE pharmacy_dispense ADD COLUMN IF NOT EXISTS remark VARCHAR(255);
ALTER TABLE pharmacy_dispense ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE pharmacy_dispense ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE pharmacy_dispense ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE UNIQUE INDEX IF NOT EXISTS uk_pharmacy_dispense_no ON pharmacy_dispense (dispense_no);
CREATE INDEX IF NOT EXISTS idx_pharmacy_dispense_order_id ON pharmacy_dispense (order_id);
CREATE INDEX IF NOT EXISTS idx_pharmacy_dispense_drug_id ON pharmacy_dispense (drug_id);
CREATE INDEX IF NOT EXISTS idx_pharmacy_dispense_status ON pharmacy_dispense (status);
CREATE INDEX IF NOT EXISTS idx_pharmacy_dispense_deleted ON pharmacy_dispense (deleted);

CREATE TABLE IF NOT EXISTS inpatient_fee (
    id BIGSERIAL PRIMARY KEY,
    fee_no VARCHAR(32) NOT NULL,
    admission_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    source_type VARCHAR(30) NOT NULL DEFAULT 'MANUAL',
    source_id BIGINT,
    item_code VARCHAR(50),
    item_name VARCHAR(100) NOT NULL,
    item_category VARCHAR(30) NOT NULL,
    quantity NUMERIC(12,2) NOT NULL,
    unit VARCHAR(20),
    unit_price NUMERIC(12,2) NOT NULL,
    total_amount NUMERIC(12,2) NOT NULL,
    fee_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(30) NOT NULL DEFAULT 'UNSETTLED',
    remark VARCHAR(255),
    deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE inpatient_fee ADD COLUMN IF NOT EXISTS fee_no VARCHAR(32);
ALTER TABLE inpatient_fee ADD COLUMN IF NOT EXISTS admission_id BIGINT;
ALTER TABLE inpatient_fee ADD COLUMN IF NOT EXISTS patient_id BIGINT;
ALTER TABLE inpatient_fee ADD COLUMN IF NOT EXISTS source_type VARCHAR(30) NOT NULL DEFAULT 'MANUAL';
ALTER TABLE inpatient_fee ADD COLUMN IF NOT EXISTS source_id BIGINT;
ALTER TABLE inpatient_fee ADD COLUMN IF NOT EXISTS item_code VARCHAR(50);
ALTER TABLE inpatient_fee ADD COLUMN IF NOT EXISTS item_name VARCHAR(100);
ALTER TABLE inpatient_fee ADD COLUMN IF NOT EXISTS item_category VARCHAR(30);
ALTER TABLE inpatient_fee ADD COLUMN IF NOT EXISTS quantity NUMERIC(12,2);
ALTER TABLE inpatient_fee ADD COLUMN IF NOT EXISTS unit VARCHAR(20);
ALTER TABLE inpatient_fee ADD COLUMN IF NOT EXISTS unit_price NUMERIC(12,2);
ALTER TABLE inpatient_fee ADD COLUMN IF NOT EXISTS total_amount NUMERIC(12,2);
ALTER TABLE inpatient_fee ADD COLUMN IF NOT EXISTS fee_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE inpatient_fee ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'UNSETTLED';
ALTER TABLE inpatient_fee ADD COLUMN IF NOT EXISTS remark VARCHAR(255);
ALTER TABLE inpatient_fee ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE inpatient_fee ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE inpatient_fee ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE UNIQUE INDEX IF NOT EXISTS uk_inpatient_fee_no ON inpatient_fee (fee_no);
CREATE UNIQUE INDEX IF NOT EXISTS uk_inpatient_fee_source ON inpatient_fee (source_type, source_id) WHERE source_id IS NOT NULL AND deleted = false;
CREATE INDEX IF NOT EXISTS idx_inpatient_fee_admission_id ON inpatient_fee (admission_id);
CREATE INDEX IF NOT EXISTS idx_inpatient_fee_patient_id ON inpatient_fee (patient_id);
CREATE INDEX IF NOT EXISTS idx_inpatient_fee_item_category ON inpatient_fee (item_category);
CREATE INDEX IF NOT EXISTS idx_inpatient_fee_status ON inpatient_fee (status);
CREATE INDEX IF NOT EXISTS idx_inpatient_fee_deleted ON inpatient_fee (deleted);

CREATE TABLE IF NOT EXISTS inpatient_deposit (
    id BIGSERIAL PRIMARY KEY,
    deposit_no VARCHAR(32) NOT NULL,
    admission_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    amount NUMERIC(12,2) NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    transaction_type VARCHAR(30) NOT NULL,
    transaction_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    operator_name VARCHAR(50),
    status VARCHAR(30) NOT NULL DEFAULT 'SUCCESS',
    remark VARCHAR(255),
    deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE inpatient_deposit ADD COLUMN IF NOT EXISTS deposit_no VARCHAR(32);
ALTER TABLE inpatient_deposit ADD COLUMN IF NOT EXISTS admission_id BIGINT;
ALTER TABLE inpatient_deposit ADD COLUMN IF NOT EXISTS patient_id BIGINT;
ALTER TABLE inpatient_deposit ADD COLUMN IF NOT EXISTS amount NUMERIC(12,2);
ALTER TABLE inpatient_deposit ADD COLUMN IF NOT EXISTS payment_method VARCHAR(30);
ALTER TABLE inpatient_deposit ADD COLUMN IF NOT EXISTS transaction_type VARCHAR(30);
ALTER TABLE inpatient_deposit ADD COLUMN IF NOT EXISTS transaction_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE inpatient_deposit ADD COLUMN IF NOT EXISTS operator_name VARCHAR(50);
ALTER TABLE inpatient_deposit ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'SUCCESS';
ALTER TABLE inpatient_deposit ADD COLUMN IF NOT EXISTS remark VARCHAR(255);
ALTER TABLE inpatient_deposit ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE inpatient_deposit ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE inpatient_deposit ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE UNIQUE INDEX IF NOT EXISTS uk_inpatient_deposit_no ON inpatient_deposit (deposit_no);
CREATE INDEX IF NOT EXISTS idx_inpatient_deposit_admission_id ON inpatient_deposit (admission_id);
CREATE INDEX IF NOT EXISTS idx_inpatient_deposit_patient_id ON inpatient_deposit (patient_id);
CREATE INDEX IF NOT EXISTS idx_inpatient_deposit_transaction_type ON inpatient_deposit (transaction_type);
CREATE INDEX IF NOT EXISTS idx_inpatient_deposit_status ON inpatient_deposit (status);
CREATE INDEX IF NOT EXISTS idx_inpatient_deposit_deleted ON inpatient_deposit (deleted);

CREATE TABLE IF NOT EXISTS inpatient_discharge (
    id BIGSERIAL PRIMARY KEY,
    discharge_no VARCHAR(32) NOT NULL,
    admission_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    total_fee_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_deposit_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_refund_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    deposit_balance NUMERIC(12,2) NOT NULL DEFAULT 0,
    unpaid_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    actual_payment NUMERIC(12,2) NOT NULL DEFAULT 0,
    discharge_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    operator_name VARCHAR(50),
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    remark VARCHAR(255),
    deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE inpatient_discharge ADD COLUMN IF NOT EXISTS discharge_no VARCHAR(32);
ALTER TABLE inpatient_discharge ADD COLUMN IF NOT EXISTS admission_id BIGINT;
ALTER TABLE inpatient_discharge ADD COLUMN IF NOT EXISTS patient_id BIGINT;
ALTER TABLE inpatient_discharge ADD COLUMN IF NOT EXISTS total_fee_amount NUMERIC(12,2) NOT NULL DEFAULT 0;
ALTER TABLE inpatient_discharge ADD COLUMN IF NOT EXISTS total_deposit_amount NUMERIC(12,2) NOT NULL DEFAULT 0;
ALTER TABLE inpatient_discharge ADD COLUMN IF NOT EXISTS total_refund_amount NUMERIC(12,2) NOT NULL DEFAULT 0;
ALTER TABLE inpatient_discharge ADD COLUMN IF NOT EXISTS deposit_balance NUMERIC(12,2) NOT NULL DEFAULT 0;
ALTER TABLE inpatient_discharge ADD COLUMN IF NOT EXISTS unpaid_amount NUMERIC(12,2) NOT NULL DEFAULT 0;
ALTER TABLE inpatient_discharge ADD COLUMN IF NOT EXISTS actual_payment NUMERIC(12,2) NOT NULL DEFAULT 0;
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
ALTER TABLE inpatient_discharge ADD COLUMN IF NOT EXISTS discharge_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE inpatient_discharge ADD COLUMN IF NOT EXISTS operator_name VARCHAR(50);
ALTER TABLE inpatient_discharge ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'DRAFT';
ALTER TABLE inpatient_discharge ADD COLUMN IF NOT EXISTS remark VARCHAR(255);
ALTER TABLE inpatient_discharge ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE inpatient_discharge ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE inpatient_discharge ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE UNIQUE INDEX IF NOT EXISTS uk_inpatient_discharge_no ON inpatient_discharge (discharge_no);
CREATE UNIQUE INDEX IF NOT EXISTS uk_inpatient_discharge_active_admission
    ON inpatient_discharge (admission_id)
    WHERE status IN ('DRAFT', 'SETTLED') AND deleted = false;
CREATE INDEX IF NOT EXISTS idx_inpatient_discharge_admission_id ON inpatient_discharge (admission_id);
CREATE INDEX IF NOT EXISTS idx_inpatient_discharge_patient_id ON inpatient_discharge (patient_id);
CREATE INDEX IF NOT EXISTS idx_inpatient_discharge_status ON inpatient_discharge (status);
CREATE INDEX IF NOT EXISTS idx_inpatient_discharge_deleted ON inpatient_discharge (deleted);

CREATE TABLE IF NOT EXISTS exam_lab_request (
    id BIGSERIAL PRIMARY KEY,
    request_no VARCHAR(32) NOT NULL,
    admission_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    request_type VARCHAR(20) NOT NULL,
    item_code VARCHAR(50),
    item_name VARCHAR(100) NOT NULL,
    item_category VARCHAR(30) NOT NULL,
    doctor_name VARCHAR(50) NOT NULL,
    request_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    scheduled_time TIMESTAMP,
    result_summary VARCHAR(255),
    result_detail TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    remark VARCHAR(255),
    deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE exam_lab_request ADD COLUMN IF NOT EXISTS request_no VARCHAR(32);
ALTER TABLE exam_lab_request ADD COLUMN IF NOT EXISTS admission_id BIGINT;
ALTER TABLE exam_lab_request ADD COLUMN IF NOT EXISTS patient_id BIGINT;
ALTER TABLE exam_lab_request ADD COLUMN IF NOT EXISTS request_type VARCHAR(20);
ALTER TABLE exam_lab_request ADD COLUMN IF NOT EXISTS item_code VARCHAR(50);
ALTER TABLE exam_lab_request ADD COLUMN IF NOT EXISTS item_name VARCHAR(100);
ALTER TABLE exam_lab_request ADD COLUMN IF NOT EXISTS request_content VARCHAR(500);
ALTER TABLE exam_lab_request ADD COLUMN IF NOT EXISTS item_category VARCHAR(30);
ALTER TABLE exam_lab_request ADD COLUMN IF NOT EXISTS doctor_name VARCHAR(50);
ALTER TABLE exam_lab_request ADD COLUMN IF NOT EXISTS request_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE exam_lab_request ADD COLUMN IF NOT EXISTS scheduled_time TIMESTAMP;
ALTER TABLE exam_lab_request ADD COLUMN IF NOT EXISTS execution_department VARCHAR(50);
ALTER TABLE exam_lab_request ADD COLUMN IF NOT EXISTS executor_name VARCHAR(50);
ALTER TABLE exam_lab_request ADD COLUMN IF NOT EXISTS executed_time TIMESTAMP;
ALTER TABLE exam_lab_request ADD COLUMN IF NOT EXISTS report_doctor_name VARCHAR(50);
ALTER TABLE exam_lab_request ADD COLUMN IF NOT EXISTS report_time TIMESTAMP;
ALTER TABLE exam_lab_request ADD COLUMN IF NOT EXISTS result_summary VARCHAR(255);
ALTER TABLE exam_lab_request ADD COLUMN IF NOT EXISTS result_detail TEXT;
ALTER TABLE exam_lab_request ADD COLUMN IF NOT EXISTS abnormal_flag BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE exam_lab_request ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'DRAFT';
ALTER TABLE exam_lab_request ADD COLUMN IF NOT EXISTS unit_price NUMERIC(12,2) NOT NULL DEFAULT 0;
ALTER TABLE exam_lab_request ADD COLUMN IF NOT EXISTS quantity NUMERIC(12,2) NOT NULL DEFAULT 1;
ALTER TABLE exam_lab_request ADD COLUMN IF NOT EXISTS total_amount NUMERIC(12,2) NOT NULL DEFAULT 0;
ALTER TABLE exam_lab_request ADD COLUMN IF NOT EXISTS billed BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE exam_lab_request ADD COLUMN IF NOT EXISTS remark VARCHAR(255);
ALTER TABLE exam_lab_request ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE exam_lab_request ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE exam_lab_request ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE UNIQUE INDEX IF NOT EXISTS uk_exam_lab_request_no ON exam_lab_request (request_no);
CREATE INDEX IF NOT EXISTS idx_exam_lab_request_admission_id ON exam_lab_request (admission_id);
CREATE INDEX IF NOT EXISTS idx_exam_lab_request_patient_id ON exam_lab_request (patient_id);
CREATE INDEX IF NOT EXISTS idx_exam_lab_request_request_type ON exam_lab_request (request_type);
CREATE INDEX IF NOT EXISTS idx_exam_lab_request_item_category ON exam_lab_request (item_category);
CREATE INDEX IF NOT EXISTS idx_exam_lab_request_status ON exam_lab_request (status);
CREATE INDEX IF NOT EXISTS idx_exam_lab_request_billed ON exam_lab_request (billed);
CREATE INDEX IF NOT EXISTS idx_exam_lab_request_deleted ON exam_lab_request (deleted);

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
CREATE INDEX IF NOT EXISTS idx_medical_record_doctor_name ON medical_record (doctor_name);
CREATE INDEX IF NOT EXISTS idx_medical_record_deleted ON medical_record (deleted);

CREATE TABLE IF NOT EXISTS department (
    id BIGSERIAL PRIMARY KEY,
    dept_code VARCHAR(32) NOT NULL,
    dept_name VARCHAR(100) NOT NULL,
    dept_type VARCHAR(30) NOT NULL,
    parent_id BIGINT,
    status VARCHAR(30) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(255),
    deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE department ADD COLUMN IF NOT EXISTS dept_code VARCHAR(32);
ALTER TABLE department ADD COLUMN IF NOT EXISTS dept_name VARCHAR(100);
ALTER TABLE department ADD COLUMN IF NOT EXISTS dept_type VARCHAR(30);
ALTER TABLE department ADD COLUMN IF NOT EXISTS parent_id BIGINT;
ALTER TABLE department ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'ENABLED';
ALTER TABLE department ADD COLUMN IF NOT EXISTS remark VARCHAR(255);
ALTER TABLE department ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE department ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE department ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE UNIQUE INDEX IF NOT EXISTS uk_department_dept_code ON department (dept_code) WHERE deleted = false;
CREATE INDEX IF NOT EXISTS idx_department_dept_name ON department (dept_name);
CREATE INDEX IF NOT EXISTS idx_department_dept_type ON department (dept_type);
CREATE INDEX IF NOT EXISTS idx_department_status ON department (status);
CREATE INDEX IF NOT EXISTS idx_department_deleted ON department (deleted);

CREATE TABLE IF NOT EXISTS staff (
    id BIGSERIAL PRIMARY KEY,
    staff_no VARCHAR(32) NOT NULL,
    staff_name VARCHAR(50) NOT NULL,
    gender VARCHAR(10),
    phone VARCHAR(20),
    department_id BIGINT NOT NULL,
    role_type VARCHAR(30) NOT NULL,
    title VARCHAR(50),
    status VARCHAR(30) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(255),
    deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE staff ADD COLUMN IF NOT EXISTS staff_no VARCHAR(32);
ALTER TABLE staff ADD COLUMN IF NOT EXISTS staff_name VARCHAR(50);
ALTER TABLE staff ADD COLUMN IF NOT EXISTS gender VARCHAR(10);
ALTER TABLE staff ADD COLUMN IF NOT EXISTS phone VARCHAR(20);
ALTER TABLE staff ADD COLUMN IF NOT EXISTS department_id BIGINT;
ALTER TABLE staff ADD COLUMN IF NOT EXISTS role_type VARCHAR(30);
ALTER TABLE staff ADD COLUMN IF NOT EXISTS title VARCHAR(50);
ALTER TABLE staff ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'ENABLED';
ALTER TABLE staff ADD COLUMN IF NOT EXISTS remark VARCHAR(255);
ALTER TABLE staff ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE staff ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE staff ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE UNIQUE INDEX IF NOT EXISTS uk_staff_staff_no ON staff (staff_no) WHERE deleted = false;
CREATE INDEX IF NOT EXISTS idx_staff_staff_name ON staff (staff_name);
CREATE INDEX IF NOT EXISTS idx_staff_department_id ON staff (department_id);
CREATE INDEX IF NOT EXISTS idx_staff_role_type ON staff (role_type);
CREATE INDEX IF NOT EXISTS idx_staff_status ON staff (status);
CREATE INDEX IF NOT EXISTS idx_staff_deleted ON staff (deleted);

CREATE TABLE IF NOT EXISTS dict_item (
    id BIGSERIAL PRIMARY KEY,
    dict_type VARCHAR(50) NOT NULL,
    dict_code VARCHAR(50) NOT NULL,
    dict_name VARCHAR(100) NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(255),
    deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE dict_item ADD COLUMN IF NOT EXISTS dict_type VARCHAR(50);
ALTER TABLE dict_item ADD COLUMN IF NOT EXISTS dict_code VARCHAR(50);
ALTER TABLE dict_item ADD COLUMN IF NOT EXISTS dict_name VARCHAR(100);
ALTER TABLE dict_item ADD COLUMN IF NOT EXISTS sort_order INTEGER NOT NULL DEFAULT 0;
ALTER TABLE dict_item ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'ENABLED';
ALTER TABLE dict_item ADD COLUMN IF NOT EXISTS remark VARCHAR(255);
ALTER TABLE dict_item ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE dict_item ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE dict_item ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE UNIQUE INDEX IF NOT EXISTS uk_dict_item_type_code ON dict_item (dict_type, dict_code) WHERE deleted = false;
CREATE INDEX IF NOT EXISTS idx_dict_item_type ON dict_item (dict_type);
CREATE INDEX IF NOT EXISTS idx_dict_item_code ON dict_item (dict_code);
CREATE INDEX IF NOT EXISTS idx_dict_item_name ON dict_item (dict_name);
CREATE INDEX IF NOT EXISTS idx_dict_item_status ON dict_item (status);
CREATE INDEX IF NOT EXISTS idx_dict_item_deleted ON dict_item (deleted);

CREATE TABLE IF NOT EXISTS operation_log (
    id BIGSERIAL PRIMARY KEY,
    module_name VARCHAR(100) NOT NULL,
    operation_type VARCHAR(30) NOT NULL,
    business_id BIGINT,
    business_no VARCHAR(100),
    operator_name VARCHAR(50),
    request_method VARCHAR(20),
    request_uri VARCHAR(255),
    request_params TEXT,
    result_status VARCHAR(30) NOT NULL,
    error_message TEXT,
    operation_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

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

ALTER TABLE operation_log ADD COLUMN IF NOT EXISTS module_name VARCHAR(100);
ALTER TABLE operation_log ADD COLUMN IF NOT EXISTS operation_type VARCHAR(30);
ALTER TABLE operation_log ADD COLUMN IF NOT EXISTS business_id BIGINT;
ALTER TABLE operation_log ADD COLUMN IF NOT EXISTS business_no VARCHAR(100);
ALTER TABLE operation_log ADD COLUMN IF NOT EXISTS operator_name VARCHAR(50);
ALTER TABLE operation_log ADD COLUMN IF NOT EXISTS request_method VARCHAR(20);
ALTER TABLE operation_log ADD COLUMN IF NOT EXISTS request_uri VARCHAR(255);
ALTER TABLE operation_log ADD COLUMN IF NOT EXISTS request_params TEXT;
ALTER TABLE operation_log ADD COLUMN IF NOT EXISTS result_status VARCHAR(30);
ALTER TABLE operation_log ADD COLUMN IF NOT EXISTS error_message TEXT;
ALTER TABLE operation_log ADD COLUMN IF NOT EXISTS operation_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE operation_log ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_operation_log_module_name ON operation_log (module_name);
CREATE INDEX IF NOT EXISTS idx_operation_log_operation_type ON operation_log (operation_type);
CREATE INDEX IF NOT EXISTS idx_operation_log_business_id ON operation_log (business_id);
CREATE INDEX IF NOT EXISTS idx_operation_log_operator_name ON operation_log (operator_name);
CREATE INDEX IF NOT EXISTS idx_operation_log_result_status ON operation_log (result_status);
CREATE INDEX IF NOT EXISTS idx_operation_log_operation_time ON operation_log (operation_time);
