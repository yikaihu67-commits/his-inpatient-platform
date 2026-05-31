package com.yueshan.backend.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yueshan.backend.domain.InpatientAdmission;
import com.yueshan.backend.domain.InpatientDischarge;
import com.yueshan.backend.dto.DischargeCreateRequest;
import com.yueshan.backend.dto.DischargeQueryRequest;
import com.yueshan.backend.dto.DischargeSettleRequest;
import com.yueshan.backend.dto.DischargeUpdateRequest;
import com.yueshan.backend.dto.InpatientDepositRequest;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.exception.BusinessException;
import com.yueshan.backend.mapper.AdmissionMapper;
import com.yueshan.backend.mapper.BedMapper;
import com.yueshan.backend.mapper.InpatientDepositMapper;
import com.yueshan.backend.mapper.InpatientDischargeMapper;
import com.yueshan.backend.mapper.InpatientFeeMapper;
import com.yueshan.backend.service.InpatientDepositService;
import com.yueshan.backend.service.InpatientDischargeService;

@Service
public class InpatientDischargeServiceImpl implements InpatientDischargeService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final Set<String> STATUSES = Set.of("DRAFT", "SETTLED", "CANCELLED");

    private final InpatientDischargeMapper mapper;
    private final AdmissionMapper admissionMapper;
    private final InpatientFeeMapper feeMapper;
    private final InpatientDepositMapper depositMapper;
    private final InpatientDepositService depositService;
    private final BedMapper bedMapper;

    public InpatientDischargeServiceImpl(InpatientDischargeMapper mapper, AdmissionMapper admissionMapper,
                                         InpatientFeeMapper feeMapper, InpatientDepositMapper depositMapper,
                                         InpatientDepositService depositService, BedMapper bedMapper) {
        this.mapper = mapper;
        this.admissionMapper = admissionMapper;
        this.feeMapper = feeMapper;
        this.depositMapper = depositMapper;
        this.depositService = depositService;
        this.bedMapper = bedMapper;
    }

    @Transactional
    public InpatientDischarge create(DischargeCreateRequest request) {
        InpatientAdmission admission = requireAdmission(request.getAdmissionId());
        validatePatient(admission, request.getPatientId());
        if (mapper.countActiveByAdmission(request.getAdmissionId()) > 0) {
            throw new BusinessException("该住院记录已存在未取消的出院结算");
        }
        InpatientDischarge discharge = buildDischarge(request, null, "DRAFT");
        mapper.insert(discharge);
        return mapper.findById(discharge.getId());
    }

    public PageResponse<InpatientDischarge> findPage(DischargeQueryRequest query) {
        DischargeQueryRequest q = normalizeQuery(query);
        int page = q.getPage();
        int pageSize = q.getPageSize();
        long total = mapper.countPage(q);
        List<InpatientDischarge> records = total == 0 ? List.of() : mapper.findPage(q, (page - 1) * pageSize, pageSize);
        return new PageResponse<>(total, page, pageSize, records);
    }

    public Optional<InpatientDischarge> findById(Long id) {
        return Optional.ofNullable(mapper.findById(id));
    }

    @Transactional
    public Optional<InpatientDischarge> update(Long id, DischargeUpdateRequest request) {
        InpatientDischarge current = mapper.findById(id);
        if (current == null) {
            return Optional.empty();
        }
        requireDraft(current, "只有草稿状态出院结算允许修改");
        InpatientAdmission admission = requireAdmission(request.getAdmissionId());
        validatePatient(admission, request.getPatientId());
        if (mapper.countActiveByAdmissionExcludeId(request.getAdmissionId(), id) > 0) {
            throw new BusinessException("该住院记录已存在未取消的出院结算");
        }
        InpatientDischarge discharge = buildDischarge(request, current, "DRAFT");
        discharge.setId(id);
        mapper.update(discharge);
        return Optional.ofNullable(mapper.findById(id));
    }

    @Transactional
    public boolean deleteById(Long id) {
        InpatientDischarge current = mapper.findById(id);
        if (current == null) {
            return false;
        }
        if ("SETTLED".equals(current.getStatus())) {
            throw new BusinessException("已结算记录不能删除");
        }
        return mapper.logicDeleteById(id) > 0;
    }

    @Transactional
    public Optional<InpatientDischarge> settle(Long id, DischargeSettleRequest request) {
        InpatientDischarge current = mapper.findById(id);
        if (current == null) {
            return Optional.empty();
        }
        requireDraft(current, "只有草稿状态出院结算可以执行结算");
        InpatientAdmission admission = requireAdmission(current.getAdmissionId());
        if (!"IN_HOSPITAL".equals(admission.getStatus())) {
            throw new BusinessException("只有在院状态的住院记录可以办理出院结算");
        }

        AccountSnapshot snapshot = snapshot(current.getAdmissionId());
        BigDecimal actualPayment = money(request == null ? null : request.getActualPayment());
        if (request == null || request.getActualPayment() == null) {
            actualPayment = snapshot.unpaidAmount();
        }
        if (actualPayment.compareTo(snapshot.unpaidAmount()) != 0) {
            throw new BusinessException("实际支付金额必须等于当前欠费金额");
        }

        if (actualPayment.compareTo(BigDecimal.ZERO) > 0) {
            InpatientDepositRequest payRequest = new InpatientDepositRequest();
            payRequest.setAdmissionId(current.getAdmissionId());
            payRequest.setPatientId(current.getPatientId());
            payRequest.setAmount(actualPayment);
            payRequest.setPaymentMethod(paymentMethod(request));
            payRequest.setOperatorName(operatorName(request, current.getOperatorName()));
            payRequest.setRemark("出院结算补交");
            depositService.pay(payRequest);
        } else {
            BigDecimal refundable = snapshot.depositBalance().subtract(snapshot.totalFeeAmount()).max(BigDecimal.ZERO);
            if (refundable.compareTo(BigDecimal.ZERO) > 0) {
                InpatientDepositRequest refundRequest = new InpatientDepositRequest();
                refundRequest.setAdmissionId(current.getAdmissionId());
                refundRequest.setPatientId(current.getPatientId());
                refundRequest.setAmount(refundable);
                refundRequest.setPaymentMethod(paymentMethod(request));
                refundRequest.setOperatorName(operatorName(request, current.getOperatorName()));
                refundRequest.setRemark("出院结算退还余额");
                depositService.refund(refundRequest);
            }
        }

        LocalDateTime dischargeTime = request != null && request.getDischargeTime() != null
                ? request.getDischargeTime()
                : LocalDateTime.now();
        AccountSnapshot finalSnapshot = snapshot(current.getAdmissionId());
        mapper.settle(id, finalSnapshot.totalFeeAmount(), finalSnapshot.totalDepositAmount(), finalSnapshot.totalRefundAmount(),
                finalSnapshot.depositBalance(), finalSnapshot.unpaidAmount(), actualPayment, dischargeTime,
                operatorName(request, current.getOperatorName()), remark(request, current.getRemark()));
        feeMapper.settleUnsettledByAdmission(current.getAdmissionId());
        admissionMapper.markDischarged(current.getAdmissionId(), dischargeTime);
        bedMapper.releaseByAdmissionId(current.getAdmissionId());
        return Optional.ofNullable(mapper.findById(id));
    }

    @Transactional
    public Optional<InpatientDischarge> cancel(Long id, DischargeSettleRequest request) {
        InpatientDischarge current = mapper.findById(id);
        if (current == null) {
            return Optional.empty();
        }
        requireDraft(current, "只有草稿状态出院结算可以取消");
        mapper.cancel(id, operatorName(request, current.getOperatorName()), remark(request, current.getRemark()));
        return Optional.ofNullable(mapper.findById(id));
    }

    private InpatientDischarge buildDischarge(DischargeCreateRequest request, InpatientDischarge current, String status) {
        AccountSnapshot snapshot = snapshot(request.getAdmissionId());
        InpatientDischarge discharge = new InpatientDischarge();
        discharge.setDischargeNo(resolveDischargeNo(request.getDischargeNo(), current));
        discharge.setAdmissionId(request.getAdmissionId());
        discharge.setPatientId(request.getPatientId());
        discharge.setTotalFeeAmount(snapshot.totalFeeAmount());
        discharge.setTotalDepositAmount(snapshot.totalDepositAmount());
        discharge.setTotalRefundAmount(snapshot.totalRefundAmount());
        discharge.setDepositBalance(snapshot.depositBalance());
        discharge.setUnpaidAmount(snapshot.unpaidAmount());
        discharge.setActualPayment(snapshot.unpaidAmount());
        discharge.setDischargeTime(request.getDischargeTime() == null ? LocalDateTime.now() : request.getDischargeTime());
        discharge.setOperatorName(trimToNull(request.getOperatorName()));
        discharge.setStatus(status);
        discharge.setRemark(trimToNull(request.getRemark()));
        discharge.setDeleted(false);
        return discharge;
    }

    private AccountSnapshot snapshot(Long admissionId) {
        InpatientAdmission admission = requireAdmission(admissionId);
        BigDecimal totalFee = money(feeMapper.sumNotCancelledByAdmission(admissionId));
        BigDecimal totalDeposit = money(depositMapper.sumSuccessByAdmissionAndType(admissionId, "PAY"));
        BigDecimal totalRefund = money(depositMapper.sumSuccessByAdmissionAndType(admissionId, "REFUND"));
        BigDecimal depositBalance = money(totalDeposit.subtract(totalRefund));
        BigDecimal unpaid = money(totalFee.subtract(depositBalance).max(BigDecimal.ZERO));
        return new AccountSnapshot(admission.getPatientId(), totalFee, totalDeposit, totalRefund, depositBalance, unpaid);
    }

    private InpatientAdmission requireAdmission(Long admissionId) {
        InpatientAdmission admission = admissionMapper.findById(admissionId);
        if (admission == null) {
            throw new BusinessException("住院记录不存在");
        }
        return admission;
    }

    private void validatePatient(InpatientAdmission admission, Long patientId) {
        if (patientId == null || !admission.getPatientId().equals(patientId)) {
            throw new BusinessException("患者ID与住院记录不匹配");
        }
    }

    private void requireDraft(InpatientDischarge discharge, String message) {
        if (!"DRAFT".equals(discharge.getStatus())) {
            throw new BusinessException(message);
        }
    }

    private DischargeQueryRequest normalizeQuery(DischargeQueryRequest query) {
        DischargeQueryRequest q = new DischargeQueryRequest();
        if (query == null) {
            return q;
        }
        q.setPage(query.getPage() == null ? 1 : query.getPage());
        q.setPageSize(query.getPageSize() == null ? 10 : query.getPageSize());
        q.setDischargeNo(trimToNull(query.getDischargeNo()));
        q.setAdmissionId(query.getAdmissionId());
        q.setPatientId(query.getPatientId());
        q.setStatus(normalizeStatusOrNull(query.getStatus()));
        return q;
    }

    private String resolveDischargeNo(String dischargeNo, InpatientDischarge current) {
        String value = trimToNull(dischargeNo);
        if (value == null) {
            if (current != null) {
                return current.getDischargeNo();
            }
            return generateDischargeNo();
        }
        boolean duplicated = current == null
                ? mapper.countByDischargeNo(value) > 0
                : mapper.countByDischargeNoExcludeId(value, current.getId()) > 0;
        if (duplicated) {
            throw new BusinessException("出院结算编号已存在");
        }
        return value;
    }

    private String generateDischargeNo() {
        for (int i = 0; i < 5; i++) {
            String no = "CY" + LocalDateTime.now().format(FORMATTER);
            if (mapper.countByDischargeNo(no) == 0) {
                return no;
            }
        }
        throw new BusinessException("出院结算编号生成失败，请重试");
    }

    private String normalizeStatusOrNull(String status) {
        String value = trimToNull(status);
        if (value == null) {
            return null;
        }
        value = value.toUpperCase();
        if (!STATUSES.contains(value)) {
            throw new BusinessException("出院结算状态不正确");
        }
        return value;
    }

    private String paymentMethod(DischargeSettleRequest request) {
        String value = request == null ? null : trimToNull(request.getPaymentMethod());
        return value == null ? "CASH" : value;
    }

    private String operatorName(DischargeSettleRequest request, String fallback) {
        String value = request == null ? null : trimToNull(request.getOperatorName());
        return value == null ? fallback : value;
    }

    private String remark(DischargeSettleRequest request, String fallback) {
        String value = request == null ? null : trimToNull(request.getRemark());
        return value == null ? fallback : value;
    }

    private BigDecimal money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private record AccountSnapshot(Long patientId, BigDecimal totalFeeAmount, BigDecimal totalDepositAmount,
                                   BigDecimal totalRefundAmount, BigDecimal depositBalance, BigDecimal unpaidAmount) {
    }
}
