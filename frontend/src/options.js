export const genderOptions = [
  { label: '男', value: 'MALE' },
  { label: '女', value: 'FEMALE' }
]

export const patientStatusOptions = [
  { label: '正常', value: 'ACTIVE' },
  { label: '停用', value: 'DISABLED' }
]

export const admissionStatusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已登记', value: 'REGISTERED' },
  { label: '在院', value: 'IN_HOSPITAL' },
  { label: '已出院', value: 'DISCHARGED' },
  { label: '已取消', value: 'CANCELLED' }
]

export const bedStatusOptions = [
  { label: '空床', value: 'EMPTY' },
  { label: '可用', value: 'AVAILABLE' },
  { label: '占用', value: 'OCCUPIED' },
  { label: '预留', value: 'RESERVED' },
  { label: '停用', value: 'DISABLED' }
]

export const departmentOptions = [
  { label: '住院内科', value: '住院内科' },
  { label: '住院外科', value: '住院外科' },
  { label: '急诊科', value: '急诊科' },
  { label: '影像科', value: '影像科' },
  { label: '药房', value: '药房' }
]

export const wardOptions = [
  { label: '一病区', value: '一病区' },
  { label: '二病区', value: '二病区' },
  { label: '三病区', value: '三病区' },
  { label: '重症病区', value: '重症病区' }
]

export const doctorOptions = [
  { label: '王医生', value: '王医生' },
  { label: '李医生', value: '李医生' },
  { label: '张医生', value: '张医生' }
]

export const nurseOptions = [
  { label: '赵护士', value: '赵护士' },
  { label: '钱护士', value: '钱护士' },
  { label: '孙护士', value: '孙护士' }
]

export const orderTypeOptions = [
  { label: '长期医嘱', value: 'LONG_TERM' },
  { label: '临时医嘱', value: 'TEMPORARY' }
]

export const orderCategoryOptions = [
  { label: '药品', value: 'DRUG' },
  { label: '检查', value: 'EXAM' },
  { label: '检验', value: 'LAB' },
  { label: '治疗', value: 'TREATMENT' },
  { label: '护理', value: 'NURSING' },
  { label: '饮食', value: 'DIET' },
  { label: '其他', value: 'OTHER' }
]

export const feeCategoryOptions = [
  { label: '药品费', value: 'DRUG' },
  { label: '检查费', value: 'EXAM' },
  { label: '检验费', value: 'LAB' },
  { label: '治疗费', value: 'TREATMENT' },
  { label: '手术费', value: 'SURGERY' },
  { label: '床位费', value: 'BED' },
  { label: '护理费', value: 'NURSING' },
  { label: '其他', value: 'OTHER' }
]

export const feeSourceOptions = [
  { label: '医嘱', value: 'ORDER' },
  { label: '药房发药', value: 'DRUG_DISPENSE' },
  { label: '退药冲正', value: 'DRUG_RETURN' },
  { label: '手术', value: 'SURGERY' },
  { label: '床位费', value: 'BED' },
  { label: '手工计费', value: 'MANUAL' },
  { label: '其他', value: 'OTHER' }
]

export const feeStatusOptions = [
  { label: '未结算', value: 'UNSETTLED' },
  { label: '已结算', value: 'SETTLED' },
  { label: '已取消', value: 'CANCELLED' }
]

export const dischargeStatusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已结算', value: 'SETTLED' },
  { label: '已取消', value: 'CANCELLED' }
]

export const paymentMethodOptions = [
  { label: '现金', value: 'CASH' },
  { label: '微信', value: 'WECHAT' },
  { label: '支付宝', value: 'ALIPAY' },
  { label: '银行卡', value: 'BANK_CARD' },
  { label: '医保', value: 'MEDICAL_INSURANCE' },
  { label: '其他', value: 'OTHER' }
]

export const chargeTypeOptions = [
  { label: '医保', value: '医保' },
  { label: '自费', value: '自费' },
  { label: '公费', value: '公费' },
  { label: '其他', value: '其他' }
]

export const nursingLevelOptions = [
  { label: '特级护理', value: '特级护理' },
  { label: '一级护理', value: '一级护理' },
  { label: '二级护理', value: '二级护理' },
  { label: '三级护理', value: '三级护理' }
]

export const surgeryStatusOptions = [
  { label: '已申请', value: 'APPLIED' },
  { label: '已安排', value: 'SCHEDULED' },
  { label: '手术中', value: 'IN_PROGRESS' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已计费', value: 'BILLED' },
  { label: '已取消', value: 'CANCELLED' }
]

export const surgeryLevelOptions = [
  { label: 'I级', value: 'I' },
  { label: 'II级', value: 'II' },
  { label: 'III级', value: 'III' },
  { label: 'IV级', value: 'IV' }
]

export const surgeryTypeOptions = [
  { label: '择期', value: '择期' },
  { label: '急诊', value: '急诊' },
  { label: '日间', value: '日间' }
]

export const anesthesiaMethodOptions = [
  { label: '全身麻醉', value: '全身麻醉' },
  { label: '椎管内麻醉', value: '椎管内麻醉' },
  { label: '局部麻醉', value: '局部麻醉' },
  { label: '神经阻滞', value: '神经阻滞' }
]

export const medicalRecordTypeOptions = [
  { label: '入院记录', value: 'ADMISSION_RECORD' },
  { label: '首次病程记录', value: 'FIRST_COURSE_RECORD' },
  { label: '日常病程记录', value: 'DAILY_COURSE_RECORD' },
  { label: '上级医师查房记录', value: 'SUPERIOR_ROUND_RECORD' },
  { label: '出院记录', value: 'DISCHARGE_RECORD' },
  { label: '护理记录', value: 'NURSING_RECORD' },
  { label: '病程记录', value: 'PROGRESS_NOTE' },
  { label: '手术记录', value: 'OPERATION_RECORD' },
  { label: '会诊记录', value: 'CONSULTATION_RECORD' },
  { label: '其他', value: 'OTHER' }
]

export const medicalRecordStatusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已提交', value: 'SUBMITTED' },
  { label: '已审核', value: 'REVIEWED' },
  { label: '已归档', value: 'ARCHIVED' },
  { label: '已作废', value: 'CANCELLED' }
]
