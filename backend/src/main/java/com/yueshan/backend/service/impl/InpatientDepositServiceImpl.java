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
import com.yueshan.backend.domain.InpatientDeposit;
import com.yueshan.backend.dto.InpatientDepositQueryRequest;
import com.yueshan.backend.dto.InpatientDepositRequest;
import com.yueshan.backend.dto.InpatientDepositSummaryResponse;
import com.yueshan.backend.dto.PageResponse;
import com.yueshan.backend.exception.BusinessException;
import com.yueshan.backend.mapper.AdmissionMapper;
import com.yueshan.backend.mapper.InpatientDepositMapper;
import com.yueshan.backend.service.InpatientDepositService;

@Service
public class InpatientDepositServiceImpl implements InpatientDepositService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final Set<String> PAYMENT_METHODS = Set.of("CASH", "WECHAT", "ALIPAY", "BANK_CARD", "MEDICAL_INSURANCE", "OTHER");
    private static final Set<String> TRANSACTION_TYPES = Set.of("PAY", "REFUND");
    private static final Set<String> STATUSES = Set.of("SUCCESS", "CANCELLED");

    private final InpatientDepositMapper mapper;
    private final AdmissionMapper admissionMapper;

    public InpatientDepositServiceImpl(InpatientDepositMapper mapper, AdmissionMapper admissionMapper) {
        this.mapper = mapper;
        this.admissionMapper = admissionMapper;
    }

    @Transactional
    public InpatientDeposit pay(InpatientDepositRequest request) {
        InpatientDeposit deposit = toDeposit(request, "PAY");
        mapper.insert(deposit);
        return mapper.findById(deposit.getId());
    }

    @Transactional
    public InpatientDeposit refund(InpatientDepositRequest request) {
        InpatientDeposit deposit = toDeposit(request, "REFUND");
        BigDecimal balance = summary(request.getAdmissionId()).balance();
        if (deposit.getAmount().compareTo(balance) > 0) {
            throw new BusinessException("退费金额不能超过当前可退余额");
        }
        mapper.insert(deposit);
        return mapper.findById(deposit.getId());
    }

    public PageResponse<InpatientDeposit> findPage(InpatientDepositQueryRequest query) {
        InpatientDepositQueryRequest q = normalizeQuery(query);
        int page = q.getPage();
        int pageSize = q.getPageSize();
        long total = mapper.countPage(q);
        List<InpatientDeposit> records = total == 0 ? List.of() : mapper.findPage(q, (page - 1) * pageSize, pageSize);
        return new PageResponse<>(total, page, pageSize, records);
    }

    public Optional<InpatientDeposit> findById(Long id) {
        return Optional.ofNullable(mapper.findById(id));
    }

    @Transactional
    public Optional<InpatientDeposit> cancel(Long id) {
        InpatientDeposit current = mapper.findById(id);
        if (current == null) {
            return Optional.empty();
        }
        if ("CANCELLED".equals(current.getStatus())) {
            throw new BusinessException("预交金记录已取消，不能重复取消");
        }
        mapper.cancel(id);
        return Optional.ofNullable(mapper.findById(id));
    }

    public InpatientDepositSummaryResponse summary(Long admissionId) {
        InpatientAdmission admission = requireAdmission(admissionId);
        BigDecimal totalPaid = money(mapper.sumSuccessByAdmissionAndType(admissionId, "PAY"));
        BigDecimal totalRefund = money(mapper.sumSuccessByAdmissionAndType(admissionId, "REFUND"));
        return new InpatientDepositSummaryResponse(admissionId, admission.getPatientId(), totalPaid, totalRefund, money(totalPaid.subtract(totalRefund)));
    }

    private InpatientDeposit toDeposit(InpatientDepositRequest request, String transactionType) {
        InpatientAdmission admission = requireAdmission(request.getAdmissionId());
        if (!admission.getPatientId().equals(request.getPatientId())) {
            throw new BusinessException("患者ID与住院记录不匹配");
        }
        BigDecimal amount = money(request.getAmount());
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("金额必须大于0");
        }

        InpatientDeposit deposit = new InpatientDeposit();
        deposit.setDepositNo(resolveDepositNo(request.getDepositNo()));
        deposit.setAdmissionId(request.getAdmissionId());
        deposit.setPatientId(request.getPatientId());
        deposit.setAmount(amount);
        deposit.setPaymentMethod(normalizePaymentMethod(request.getPaymentMethod()));
        deposit.setTransactionType(normalizeTransactionType(transactionType));
        deposit.setTransactionTime(request.getTransactionTime() == null ? LocalDateTime.now() : request.getTransactionTime());
        deposit.setOperatorName(trimToNull(request.getOperatorName()));
        deposit.setStatus("SUCCESS");
        deposit.setRemark(trimToNull(request.getRemark()));
        deposit.setDeleted(false);
        return deposit;
    }

    private InpatientDepositQueryRequest normalizeQuery(InpatientDepositQueryRequest query) {
        InpatientDepositQueryRequest q = new InpatientDepositQueryRequest();
        if (query == null) {
            return q;
        }
        q.setPage(query.getPage() == null ? 1 : query.getPage());
        q.setPageSize(query.getPageSize() == null ? 10 : query.getPageSize());
        q.setAdmissionId(query.getAdmissionId());
        q.setPatientId(query.getPatientId());
        q.setTransactionType(normalizeTransactionTypeOrNull(query.getTransactionType()));
        q.setStatus(normalizeStatusOrNull(query.getStatus()));
        return q;
    }

    private InpatientAdmission requireAdmission(Long admissionId) {
        InpatientAdmission admission = admissionMapper.findById(admissionId);
        if (admission == null) {
            throw new BusinessException("住院记录不存在");
        }
        return admission;
    }

    private String resolveDepositNo(String depositNo) {
        String value = trimToNull(depositNo);
        if (value == null) {
            return generateDepositNo();
        }
        if (mapper.countByDepositNo(value) > 0) {
            throw new BusinessException("预交金编号已存在");
        }
        return value;
    }

    private String generateDepositNo() {
        for (int i = 0; i < 5; i++) {
            String no = "DEP" + LocalDateTime.now().format(FORMATTER);
            if (mapper.countByDepositNo(no) == 0) {
                return no;
            }
        }
        throw new BusinessException("预交金编号生成失败，请重试");
    }

    private BigDecimal money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizePaymentMethod(String paymentMethod) {
        String value = trimToNull(paymentMethod);
        value = value == null ? "CASH" : value.toUpperCase();
        if (!PAYMENT_METHODS.contains(value)) {
            throw new BusinessException("支付方式不正确");
        }
        return value;
    }

    private String normalizeTransactionType(String transactionType) {
        String value = trimToNull(transactionType);
        if (value == null) {
            throw new BusinessException("交易类型不能为空");
        }
        value = value.toUpperCase();
        if (!TRANSACTION_TYPES.contains(value)) {
            throw new BusinessException("交易类型不正确");
        }
        return value;
    }

    private String normalizeTransactionTypeOrNull(String transactionType) {
        String value = trimToNull(transactionType);
        return value == null ? null : normalizeTransactionType(value);
    }

    private String normalizeStatusOrNull(String status) {
        String value = trimToNull(status);
        if (value == null) {
            return null;
        }
        value = value.toUpperCase();
        if (!STATUSES.contains(value)) {
            throw new BusinessException("预交金状态不正确");
        }
        return value;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
