package com.yueshan.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record DashboardSummaryResponse(
        Metrics metrics,
        List<TodoItem> todos,
        List<RecentSurgeryItem> recentSurgeries,
        List<ActivityItem> todayActivities) {

    public record Metrics(
            long currentInHospitalPatientCount,
            long todayAdmissionCount,
            long todayDischargeCount,
            long pendingMedicalRecordCount,
            long draftMedicalRecordCount,
            long pendingReviewMedicalRecordCount,
            long archivedMedicalRecordCount,
            long surgeryAppliedCount,
            long surgeryScheduledCount,
            long surgeryInProgressCount,
            long surgeryCompletedCount,
            long surgeryPendingBillingCount,
            long todaySurgeryCount,
            BigDecimal todaySurgeryFeeAmount,
            BigDecimal currentInpatientFeeAmount,
            BigDecimal unsettledFeeAmount,
            BigDecimal settledFeeAmount,
            long pendingExecuteOrderCount,
            long todayExecutedOrderCount,
            long todayNursingRecordCount,
            long abnormalVitalSignCount,
            long orderPendingBillingCount,
            long nursingPendingBillingCount,
            long pendingScheduleExamLabCount,
            long pendingReportExamLabCount,
            long abnormalExamLabCount,
            long examLabPendingBillingCount,
            long pendingDischargeSettlementCount,
            long todayPatientAppointmentCount,
            long pendingPatientAppointmentCount,
            long completedPatientAppointmentCount,
            long cancelledPatientAppointmentCount) {
    }

    public record TodoItem(
            String type,
            String title,
            String patientName,
            String admissionNo,
            Long admissionId,
            String status,
            LocalDateTime time,
            String targetPath) {
    }

    public record RecentSurgeryItem(
            Long id,
            String patientName,
            String surgeryName,
            String status,
            LocalDateTime plannedTime,
            String primaryDoctorName,
            BigDecimal surgeryFee,
            boolean billed,
            String targetPath) {
    }

    public record ActivityItem(
            String type,
            String title,
            String patientName,
            String status,
            LocalDateTime time,
            String targetPath) {
    }
}
