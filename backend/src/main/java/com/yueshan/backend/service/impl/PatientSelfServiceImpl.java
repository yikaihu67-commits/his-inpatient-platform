package com.yueshan.backend.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.yueshan.backend.domain.Bed;
import com.yueshan.backend.domain.ExamLabRequest;
import com.yueshan.backend.domain.InpatientAdmission;
import com.yueshan.backend.domain.InpatientFee;
import com.yueshan.backend.domain.Patient;
import com.yueshan.backend.domain.SurgeryOperation;
import com.yueshan.backend.dto.ExamLabRequestQueryRequest;
import com.yueshan.backend.dto.InpatientFeeQueryRequest;
import com.yueshan.backend.dto.SurgeryQueryRequest;
import com.yueshan.backend.dto.selfservice.PatientSelfServiceSessionResponse;
import com.yueshan.backend.dto.selfservice.PatientSelfServiceVerifyRequest;
import com.yueshan.backend.dto.selfservice.SelfServiceAdmissionInfoResponse;
import com.yueshan.backend.dto.selfservice.SelfServiceBillDetailResponse;
import com.yueshan.backend.dto.selfservice.SelfServiceBillSummaryResponse;
import com.yueshan.backend.dto.selfservice.SelfServiceDischargeResponse;
import com.yueshan.backend.dto.selfservice.SelfServiceExamLabResponse;
import com.yueshan.backend.dto.selfservice.SelfServiceSurgeryResponse;
import com.yueshan.backend.dto.selfservice.SelfServiceVerifyRow;
import com.yueshan.backend.exception.BusinessException;
import com.yueshan.backend.mapper.BedMapper;
import com.yueshan.backend.mapper.PatientSelfServiceMapper;
import com.yueshan.backend.service.AdmissionService;
import com.yueshan.backend.service.ExamLabRequestService;
import com.yueshan.backend.service.InpatientFeeService;
import com.yueshan.backend.service.PatientSelfService;
import com.yueshan.backend.service.PatientService;
import com.yueshan.backend.service.SurgeryOperationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PatientSelfServiceImpl implements PatientSelfService {

    private final PatientSelfServiceMapper mapper;
    private final PatientService patientService;
    private final AdmissionService admissionService;
    private final InpatientFeeService feeService;
    private final ExamLabRequestService examLabService;
    private final SurgeryOperationService surgeryService;
    private final BedMapper bedMapper;

    @Override
    public PatientSelfServiceSessionResponse verify(PatientSelfServiceVerifyRequest request) {
        if (request == null || !hasAnyIdentity(request)) {
            throw new BusinessException("请输入住院号、手机号、身份证后四位或患者姓名");
        }
        String last4 = trim(request.getIdCardLast4());
        if (last4 != null && !last4.matches("\\d{4}")) {
            throw new BusinessException("身份证号后四位格式不正确");
        }
        if (trim(request.getPhone()) != null && !trim(request.getPhone()).matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException("手机号格式不正确");
        }
        request.setIdCardLast4(last4);
        request.setPhone(trim(request.getPhone()));
        request.setPatientName(trim(request.getPatientName()));
        request.setAdmissionNo(trim(request.getAdmissionNo()));

        List<SelfServiceVerifyRow> rows = mapper.findMatchingAdmissions(request);
        if (rows.isEmpty()) {
            throw new BusinessException("未找到匹配患者");
        }
        SelfServiceVerifyRow row = rows.get(0);
        PatientSelfServiceSessionResponse response = new PatientSelfServiceSessionResponse();
        response.setPatientId(row.getPatientId());
        response.setAdmissionId(row.getAdmissionId());
        response.setPatientNo(row.getPatientNo());
        response.setPatientName(row.getPatientName());
        response.setGender(row.getGender());
        response.setMaskedPhone(maskPhone(row.getPhone()));
        response.setAdmissionNo(row.getAdmissionNo());
        response.setDepartmentName(row.getDepartmentName());
        response.setWardName(row.getWardName());
        response.setAdmissionTime(row.getAdmissionTime());
        response.setAdmissionStatus(row.getAdmissionStatus());
        return response;
    }

    @Override
    public SelfServiceAdmissionInfoResponse admissionInfo(Long patientId, Long admissionId) {
        Patient patient = requirePatient(patientId);
        InpatientAdmission admission = requireAdmission(patientId, admissionId);
        Bed bed = bedMapper.findOccupiedByAdmissionId(admission.getId());
        SelfServiceDischargeResponse discharge = discharge(patientId, admissionId);

        SelfServiceAdmissionInfoResponse response = new SelfServiceAdmissionInfoResponse();
        response.setPatientName(patient.getName());
        response.setGender(patient.getGender());
        response.setAge(age(patient.getBirthDate()));
        response.setMaskedPhone(maskPhone(patient.getPhone()));
        response.setDepartmentName(admission.getDepartmentName());
        response.setWardName(admission.getWardName());
        response.setBedNo(bed == null ? null : bed.getBedNo());
        response.setRoomNo(bed == null ? null : bed.getRoomNo());
        response.setAdmissionTime(admission.getAdmissionTime());
        response.setDoctorName(admission.getDoctorName());
        response.setAdmissionStatus(admission.getStatus());
        response.setDischargeStatus(discharge.getStatus());
        return response;
    }

    @Override
    public SelfServiceBillSummaryResponse billSummary(Long patientId, Long admissionId) {
        List<InpatientFee> fees = allFees(patientId, admissionId);
        SelfServiceBillSummaryResponse response = new SelfServiceBillSummaryResponse();
        Map<String, BigDecimal> categories = new LinkedHashMap<>();
        for (InpatientFee fee : fees) {
            BigDecimal amount = money(fee.getTotalAmount());
            response.setTotalAmount(response.getTotalAmount().add(amount));
            if ("SETTLED".equals(fee.getStatus())) {
                response.setSettledAmount(response.getSettledAmount().add(amount));
            } else if (!"CANCELLED".equals(fee.getStatus())) {
                response.setUnsettledAmount(response.getUnsettledAmount().add(amount));
            }
            String category = nvl(fee.getItemCategory(), "OTHER");
            categories.merge(category, amount, BigDecimal::add);
            if ("DRUG".equals(category)) response.setDrugAmount(response.getDrugAmount().add(amount));
            else if ("NURSING".equals(category)) response.setNursingAmount(response.getNursingAmount().add(amount));
            else if ("SURGERY".equals(category)) response.setSurgeryAmount(response.getSurgeryAmount().add(amount));
            else if ("EXAM".equals(category)) response.setExamAmount(response.getExamAmount().add(amount));
            else if ("LAB".equals(category)) response.setLabAmount(response.getLabAmount().add(amount));
            else if ("ORDER".equals(fee.getSourceType()) || "TREATMENT".equals(category) || "BED".equals(category)) {
                response.setOrderAmount(response.getOrderAmount().add(amount));
            } else {
                response.setOtherAmount(response.getOtherAmount().add(amount));
            }
        }
        response.setCategoryAmounts(categories);
        return response;
    }

    @Override
    public List<SelfServiceBillDetailResponse> billDetails(Long patientId, Long admissionId, LocalDate startDate, LocalDate endDate,
                                                           String itemCategory, String status) {
        String category = trim(itemCategory);
        String feeStatus = trim(status);
        return allFees(patientId, admissionId).stream()
                .filter(fee -> category == null || category.equals(fee.getItemCategory()))
                .filter(fee -> feeStatus == null || feeStatus.equals(fee.getStatus()))
                .filter(fee -> startDate == null || (fee.getFeeTime() != null && !fee.getFeeTime().toLocalDate().isBefore(startDate)))
                .filter(fee -> endDate == null || (fee.getFeeTime() != null && !fee.getFeeTime().toLocalDate().isAfter(endDate)))
                .map(this::toBillDetail)
                .toList();
    }

    @Override
    public List<SelfServiceExamLabResponse> examLabs(Long patientId, Long admissionId) {
        requireAdmission(patientId, admissionId);
        ExamLabRequestQueryRequest query = new ExamLabRequestQueryRequest();
        query.setPatientId(patientId);
        query.setAdmissionId(admissionId);
        query.setPage(1);
        query.setPageSize(100);
        return examLabService.findPage(query).records().stream().map(this::toExamLab).toList();
    }

    @Override
    public List<SelfServiceSurgeryResponse> surgeries(Long patientId, Long admissionId) {
        requireAdmission(patientId, admissionId);
        SurgeryQueryRequest query = new SurgeryQueryRequest();
        query.setPatientId(patientId);
        query.setAdmissionId(admissionId);
        query.setPage(1);
        query.setPageSize(100);
        return surgeryService.findPage(query).records().stream().map(this::toSurgery).toList();
    }

    @Override
    public SelfServiceDischargeResponse discharge(Long patientId, Long admissionId) {
        requireAdmission(patientId, admissionId);
        SelfServiceDischargeResponse response = mapper.findLatestDischarge(admissionId, patientId);
        if (response == null) {
            response = new SelfServiceDischargeResponse();
            response.setStatus("NOT_APPLIED");
            response.setUnpaidAmount(billSummary(patientId, admissionId).getUnsettledAmount());
        }
        response.setCanSettle(response.getUnpaidAmount() != null
                && response.getUnpaidAmount().compareTo(BigDecimal.ZERO) > 0
                && !"SETTLED".equals(response.getStatus()));
        return response;
    }

    private List<InpatientFee> allFees(Long patientId, Long admissionId) {
        requireAdmission(patientId, admissionId);
        InpatientFeeQueryRequest query = new InpatientFeeQueryRequest();
        query.setPatientId(patientId);
        query.setAdmissionId(admissionId);
        query.setPage(1);
        query.setPageSize(100);
        return feeService.findPage(query).records();
    }

    private SelfServiceBillDetailResponse toBillDetail(InpatientFee fee) {
        SelfServiceBillDetailResponse response = new SelfServiceBillDetailResponse();
        response.setItemName(fee.getItemName());
        response.setItemCategory(fee.getItemCategory());
        response.setUnitPrice(money(fee.getUnitPrice()));
        response.setQuantity(money(fee.getQuantity()));
        response.setTotalAmount(money(fee.getTotalAmount()));
        response.setSourceType(fee.getSourceType());
        response.setFeeTime(fee.getFeeTime());
        response.setStatus(fee.getStatus());
        response.setSettled("SETTLED".equals(fee.getStatus()));
        return response;
    }

    private SelfServiceExamLabResponse toExamLab(ExamLabRequest item) {
        SelfServiceExamLabResponse response = new SelfServiceExamLabResponse();
        response.setItemName(item.getItemName());
        response.setRequestType(item.getRequestType());
        response.setRequestTime(item.getRequestTime());
        response.setExecutedTime(item.getExecutedTime());
        response.setReportTime(item.getReportTime());
        response.setStatus(item.getStatus());
        response.setResultSummary(item.getResultSummary());
        response.setResultDetail(item.getResultDetail());
        response.setAbnormalFlag(Boolean.TRUE.equals(item.getAbnormalFlag()));
        response.setBilled(Boolean.TRUE.equals(item.getBilled()));
        return response;
    }

    private SelfServiceSurgeryResponse toSurgery(SurgeryOperation item) {
        SelfServiceSurgeryResponse response = new SelfServiceSurgeryResponse();
        response.setSurgeryName(item.getSurgeryName());
        response.setStatus(item.getStatus());
        response.setPlannedTime(item.getPlannedTime());
        response.setActualStartTime(item.getActualStartTime());
        response.setActualEndTime(item.getActualEndTime());
        response.setPrimaryDoctorName(item.getPrimaryDoctorName());
        response.setOperatingRoom(item.getOperatingRoom());
        response.setSurgeryFee(money(item.getSurgeryFee()));
        return response;
    }

    private Patient requirePatient(Long patientId) {
        return patientService.findById(patientId).orElseThrow(() -> new BusinessException("未找到匹配患者"));
    }

    private InpatientAdmission requireAdmission(Long patientId, Long admissionId) {
        if (patientId == null || admissionId == null) {
            throw new BusinessException("缺少患者或住院信息");
        }
        InpatientAdmission admission = admissionService.findById(admissionId)
                .orElseThrow(() -> new BusinessException("未找到住院记录"));
        if (!Objects.equals(admission.getPatientId(), patientId)) {
            throw new BusinessException("患者与住院记录不匹配");
        }
        return admission;
    }

    private boolean hasAnyIdentity(PatientSelfServiceVerifyRequest request) {
        return request.getPatientId() != null
                || request.getAdmissionId() != null
                || trim(request.getAdmissionNo()) != null
                || trim(request.getPhone()) != null
                || trim(request.getIdCardLast4()) != null
                || trim(request.getPatientName()) != null;
    }

    private Integer age(LocalDate birthDate) {
        return birthDate == null ? null : Period.between(birthDate, LocalDate.now()).getYears();
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return "-";
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private String trim(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String nvl(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
