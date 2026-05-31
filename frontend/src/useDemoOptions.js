import { computed, onMounted, ref } from 'vue'
import admissionApi from './api/admission'
import bedApi from './api/bed'
import departmentApi from './api/department'
import patientApi from './api/patient'
import staffApi from './api/staff'
import {
  departmentOptions,
  doctorOptions,
  nurseOptions,
  wardOptions
} from './options'

function recordsOf(page) {
  return page?.records || page?.list || page?.content || page?.rows || []
}

function uniqueOptions(options) {
  const map = new Map()
  options.filter(Boolean).forEach((option) => {
    if (option.value !== undefined && option.value !== null && option.value !== '') {
      map.set(String(option.value), option)
    }
  })
  return [...map.values()]
}

function textOption(value) {
  return value ? { label: value, value } : null
}

export function useDemoOptions() {
  const patients = ref([])
  const admissions = ref([])
  const beds = ref([])
  const departments = ref([])
  const staff = ref([])

  async function loadOptions() {
    const results = await Promise.allSettled([
      patientApi.list({ page: 1, pageSize: 100 }),
      admissionApi.list({ page: 1, pageSize: 100 }),
      bedApi.list({ page: 1, pageSize: 100 }),
      departmentApi.list({ page: 1, pageSize: 100 }),
      staffApi.list({ page: 1, pageSize: 100 })
    ])
    patients.value = results[0].status === 'fulfilled' ? recordsOf(results[0].value) : []
    admissions.value = results[1].status === 'fulfilled' ? recordsOf(results[1].value) : []
    beds.value = results[2].status === 'fulfilled' ? recordsOf(results[2].value) : []
    departments.value = results[3].status === 'fulfilled' ? recordsOf(results[3].value) : []
    staff.value = results[4].status === 'fulfilled' ? recordsOf(results[4].value) : []
  }

  onMounted(loadOptions)

  const patientOptions = computed(() => patients.value.map((item) => ({
    label: `${item.name || '患者'} / ${item.patientNo || '-'} / ID:${item.id}`,
    value: item.id
  })))

  const admissionOptions = computed(() => admissions.value.map((item) => ({
    label: `${item.admissionNo || '住院记录'} / ${item.patientName || `患者${item.patientId}`} / ${item.status || '-'} / ID:${item.id}`,
    value: item.id
  })))

  const activeAdmissionOptions = computed(() => admissions.value
    .filter((item) => ['REGISTERED', 'IN_HOSPITAL'].includes(item.status))
    .map((item) => ({
      label: `${item.admissionNo || '住院记录'} / ${item.patientName || `患者${item.patientId}`} / ${item.status || '-'} / ID:${item.id}`,
      value: item.id
    })))

  const inHospitalAdmissionOptions = computed(() => admissions.value
    .filter((item) => item.status === 'IN_HOSPITAL')
    .map((item) => ({
      label: `${item.admissionNo || '住院记录'} / ${item.patientName || `患者${item.patientId}`} / 在院 / ID:${item.id}`,
      value: item.id
    })))

  const departmentNameOptions = computed(() => uniqueOptions([
    ...departmentOptions,
    ...departments.value.map((item) => textOption(item.deptName)),
    ...admissions.value.map((item) => textOption(item.departmentName))
  ]))

  const wardNameOptions = computed(() => uniqueOptions([
    ...wardOptions,
    ...beds.value.map((item) => textOption(item.wardName)),
    ...admissions.value.map((item) => textOption(item.wardName))
  ]))

  const doctorNameOptions = computed(() => uniqueOptions([
    ...doctorOptions,
    ...staff.value.filter((item) => item.roleType === 'DOCTOR').map((item) => textOption(item.staffName)),
    ...admissions.value.map((item) => textOption(item.doctorName))
  ]))

  const nurseNameOptions = computed(() => uniqueOptions([
    ...nurseOptions,
    ...staff.value.filter((item) => item.roleType === 'NURSE').map((item) => textOption(item.staffName))
  ]))

  return {
    admissionOptions,
    activeAdmissionOptions,
    departmentNameOptions,
    doctorNameOptions,
    findAdmission: (id) => admissions.value.find((item) => Number(item.id) === Number(id)),
    findBedByAdmission: (id) => beds.value.find((item) => Number(item.currentAdmissionId) === Number(id)),
    findPatient: (id) => patients.value.find((item) => Number(item.id) === Number(id)),
    inHospitalAdmissionOptions,
    loadOptions,
    nurseNameOptions,
    patientOptions,
    wardNameOptions
  }
}
