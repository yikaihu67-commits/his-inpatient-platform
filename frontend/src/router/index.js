import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import MainLayout from '../views/MainLayout.vue'
import { canAccessMenu } from '../permissions'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: Login },
  { path: '/kiosk', redirect: '/kiosk/login' },
  { path: '/kiosk/login', component: () => import('../views/kiosk/KioskLogin.vue') },
  { path: '/kiosk/home', component: () => import('../views/kiosk/KioskHome.vue') },
  { path: '/kiosk/home-info', component: () => import('../views/kiosk/KioskAdmissionInfo.vue') },
  { path: '/kiosk/bill', component: () => import('../views/kiosk/KioskBill.vue') },
  { path: '/kiosk/bill-detail', component: () => import('../views/kiosk/KioskBillDetail.vue') },
  { path: '/kiosk/exam-lab', component: () => import('../views/kiosk/KioskExamLab.vue') },
  { path: '/kiosk/surgery', component: () => import('../views/kiosk/KioskSurgery.vue') },
  { path: '/kiosk/discharge', component: () => import('../views/kiosk/KioskDischarge.vue') },
  { path: '/mobile', redirect: '/mobile/login' },
  { path: '/mobile/login', component: () => import('../views/mobile/MobileLogin.vue') },
  { path: '/mobile/home', component: () => import('../views/mobile/MobileHome.vue') },
  { path: '/mobile/bill', component: () => import('../views/mobile/MobileBill.vue') },
  { path: '/mobile/bill-detail', component: () => import('../views/mobile/MobileBillDetail.vue') },
  { path: '/mobile/appointments', component: () => import('../views/mobile/MobileAppointments.vue') },
  { path: '/mobile/appointments/new', component: () => import('../views/mobile/MobileAppointmentNew.vue') },
  { path: '/mobile/reports', component: () => import('../views/mobile/MobileReports.vue') },
  { path: '/mobile/surgery', component: () => import('../views/mobile/MobileSurgery.vue') },
  { path: '/mobile/discharge', component: () => import('../views/mobile/MobileDischarge.vue') },
  {
    path: '/app',
    component: MainLayout,
    redirect: '/app/dashboard',
    children: [
      { path: 'dashboard', component: () => import('../views/Dashboard.vue') },
      { path: 'doctor-station', component: () => import('../views/DoctorStationPage.vue') },
      { path: 'nurse-station', component: () => import('../views/NurseStationPage.vue') },
      { path: 'patients', component: () => import('../views/PatientPage.vue') },
      { path: 'admissions', component: () => import('../views/AdmissionPage.vue') },
      { path: 'beds', component: () => import('../views/BedPage.vue') },
      { path: 'orders', component: () => import('../views/OrderPage.vue') },
      { path: 'nursing-executions', component: () => import('../views/NursingExecutionPage.vue') },
      { path: 'nursing-records', component: () => import('../views/NursingRecordPage.vue') },
      { path: 'drugs', component: () => import('../views/DrugPage.vue') },
      { path: 'pharmacy-station', component: () => import('../views/PharmacyStationPage.vue') },
      { path: 'dispenses', component: () => import('../views/DispensePage.vue') },
      { path: 'fees', component: () => import('../views/FeePage.vue') },
      { path: 'charging-station', component: () => import('../views/ChargingStationPage.vue') },
      { path: 'deposits', component: () => import('../views/DepositPage.vue') },
      { path: 'discharges', component: () => import('../views/DischargePage.vue') },
      { path: 'exam-labs', component: () => import('../views/ExamLabPage.vue') },
      { path: 'exam-lab', redirect: '/app/exam-labs' },
      { path: 'medical-records', component: () => import('../views/MedicalRecordPage.vue') },
      { path: 'surgeries', component: () => import('../views/SurgeryPage.vue') },
      { path: 'patient-appointments', component: () => import('../views/PatientAppointmentPage.vue') },
      { path: 'departments', component: () => import('../views/DepartmentPage.vue') },
      { path: 'staff', component: () => import('../views/StaffPage.vue') },
      { path: 'dict-items', component: () => import('../views/DictPage.vue') },
      { path: 'operation-logs', component: () => import('../views/OperationLogPage.vue') },
      { path: 'not-found', component: () => import('../views/NotFound.vue') }
    ]
  },
  { path: '/:pathMatch(.*)*', component: () => import('../views/NotFound.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  if (to.path.startsWith('/kiosk/') && to.path !== '/kiosk/login') {
    const session = localStorage.getItem('kioskSession')
    if (!session) return '/kiosk/login'
  }
  if (to.path.startsWith('/mobile/') && to.path !== '/mobile/login') {
    const session = localStorage.getItem('patientMobileSession')
    if (!session) return '/mobile/login'
  }
  if (!to.path.startsWith('/app/') || to.path === '/app/not-found') return true
  const role = localStorage.getItem('demoRole') || 'ADMIN'
  if (canAccessMenu(role, to.path)) return true
  return '/app/not-found'
})

export default router
