<template>
  <KioskShell title="自助查询首页">
    <el-alert v-if="!session" type="warning" title="请先完成身份验证" show-icon :closable="false" />
    <section v-else class="patient-card">
      <div>
        <span>当前患者</span>
        <strong>{{ session.patientName || '-' }}</strong>
      </div>
      <div>
        <span>住院号</span>
        <strong>{{ session.admissionNo || session.admissionId || '-' }}</strong>
      </div>
      <div>
        <span>科室 / 病区</span>
        <strong>{{ session.departmentName || '-' }} / {{ session.wardName || '-' }}</strong>
      </div>
      <div>
        <span>手机号</span>
        <strong>{{ session.maskedPhone || '-' }}</strong>
      </div>
    </section>
    <section class="entry-grid">
      <button v-for="item in entries" :key="item.path" class="entry-card" type="button" @click="$router.push(item.path)">
        <strong>{{ item.title }}</strong>
        <span>{{ item.desc }}</span>
      </button>
    </section>
  </KioskShell>
</template>

<script setup>
import { computed } from 'vue'
import KioskShell from './KioskShell.vue'
import { getKioskSession } from '../../kioskSession'

const session = computed(() => getKioskSession())
const entries = [
  { title: '住院信息查询', desc: '查看科室、床位、主治医生、住院状态', path: '/kiosk/home-info' },
  { title: '账单查询', desc: '查看总费用、已结算、未结算和分类费用', path: '/kiosk/bill' },
  { title: '费用明细', desc: '按项目查看每一笔住院费用', path: '/kiosk/bill-detail' },
  { title: '检查检验报告', desc: '查看报告状态、结果和异常提示', path: '/kiosk/exam-lab' },
  { title: '手术安排', desc: '查看计划时间、手术室和状态', path: '/kiosk/surgery' },
  { title: '出院状态', desc: '查看出院申请、待结算金额和出院医嘱', path: '/kiosk/discharge' }
]
</script>

<style scoped>
.patient-card {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 12px;
  margin-bottom: 22px;
}

.patient-card > div,
.entry-card {
  background: #fff;
  border: 1px solid #dbe4ef;
  border-radius: 8px;
  padding: 22px;
}

.patient-card span,
.entry-card span {
  display: block;
  color: #64748b;
  font-size: 18px;
}

.patient-card strong {
  display: block;
  margin-top: 8px;
  font-size: 26px;
}

.entry-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 18px;
}

.entry-card {
  min-height: 150px;
  text-align: left;
  cursor: pointer;
}

.entry-card strong {
  display: block;
  margin-bottom: 12px;
  font-size: 30px;
}

.entry-card:hover {
  border-color: #2563eb;
  box-shadow: 0 14px 30px rgba(37, 99, 235, 0.12);
}
</style>
