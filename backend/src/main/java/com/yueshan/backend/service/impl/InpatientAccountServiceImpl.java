package com.yueshan.backend.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;

import com.yueshan.backend.domain.InpatientAdmission;
import com.yueshan.backend.dto.InpatientAccountSummaryResponse;
import com.yueshan.backend.exception.BusinessException;
import com.yueshan.backend.mapper.AdmissionMapper;
import com.yueshan.backend.mapper.InpatientDepositMapper;
import com.yueshan.backend.mapper.InpatientFeeMapper;
import com.yueshan.backend.service.InpatientAccountService;

@Service
public class InpatientAccountServiceImpl implements InpatientAccountService {
    private final AdmissionMapper admissionMapper;
    private final InpatientFeeMapper feeMapper;
    private final InpatientDepositMapper depositMapper;

    public InpatientAccountServiceImpl(AdmissionMapper admissionMapper, InpatientFeeMapper feeMapper, InpatientDepositMapper depositMapper) {
        this.admissionMapper = admissionMapper;
        this.feeMapper = feeMapper;
        this.depositMapper = depositMapper;
    }

    public InpatientAccountSummaryResponse summary(Long admissionId) {
        InpatientAdmission admission = admissionMapper.findById(admissionId);
        if (admission == null) {
            throw new BusinessException("住院记录不存在");
        }
        BigDecimal totalFeeAmount = money(feeMapper.sumNotCancelledByAdmission(admissionId));
        BigDecimal totalDepositAmount = money(depositMapper.sumSuccessByAdmissionAndType(admissionId, "PAY"));
        BigDecimal totalRefundAmount = money(depositMapper.sumSuccessByAdmissionAndType(admissionId, "REFUND"));
        BigDecimal depositBalance = money(totalDepositAmount.subtract(totalRefundAmount));
        BigDecimal unpaidAmount = money(totalFeeAmount.subtract(depositBalance).max(BigDecimal.ZERO));
        BigDecimal availableBalance = money(depositBalance.subtract(totalFeeAmount).max(BigDecimal.ZERO));
        return new InpatientAccountSummaryResponse(admissionId, admission.getPatientId(), totalFeeAmount,
                totalDepositAmount, totalRefundAmount, depositBalance, unpaidAmount, availableBalance);
    }

    private BigDecimal money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }
}
