package com.yueshan.backend.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.yueshan.backend.common.ApiResponse;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBusinessException(BusinessException ex) {
        return ApiResponse.error(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Map<String, String>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        return ApiResponse.error("参数校验失败", collectFieldErrors(ex));
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Map<String, String>> handleBindException(BindException ex) {
        return ApiResponse.error("参数校验失败", collectFieldErrors(ex));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleConstraintViolation(ConstraintViolationException ex) {
        return ApiResponse.error("参数校验失败");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        return ApiResponse.error("请求体格式错误，请检查 JSON 或日期格式");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        return ApiResponse.error("请求 Content-Type 不支持，请使用 application/json; charset=utf-8");
    }

    @ExceptionHandler(TypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleTypeMismatch(TypeMismatchException ex) {
        return ApiResponse.error("参数类型错误，请检查请求参数");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return ApiResponse.error(resolveDataIntegrityMessage(ex));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleException(Exception ex) {
        return ApiResponse.error("系统异常，请稍后重试");
    }

    private String resolveDataIntegrityMessage(DataIntegrityViolationException ex) {
        String message = deepestMessage(ex);
        if (message.contains("uk_patients_patient_no") || message.contains("patient_no")) {
            return "患者编号已存在";
        }
        if (message.contains("uk_patients_id_card") || message.contains("id_card")) {
            return "身份证号已存在";
        }
        if (message.contains("uk_patients_phone") || message.contains("phone")) {
            return "手机号已存在";
        }
        if (message.contains("uk_inpatient_admission_admission_no") || message.contains("admission_no")) {
            return "住院号已存在";
        }
        if (message.contains("uk_bed_ward_bed_no") || message.contains("bed_no")) {
            return "同一病区床号已存在";
        }
        if (message.contains("uk_bed_current_admission") || message.contains("current_admission_id")) {
            return "该住院记录已占用床位，不能重复分配";
        }
        if (message.contains("uk_medical_order_order_no") || message.contains("order_no")) {
            return "医嘱编号已存在";
        }
        if (message.contains("uk_nursing_execution_no") || message.contains("execution_no")) {
            return "护士执行编号已存在";
        }
        if (message.contains("uk_drug_code") || message.contains("drug_code")) {
            return "药品编码已存在";
        }
        if (message.contains("uk_pharmacy_dispense_no") || message.contains("dispense_no")) {
            return "发药单号已存在";
        }
        if (message.contains("uk_inpatient_fee_no") || message.contains("fee_no")) {
            return "费用编号已存在";
        }
        if (message.contains("uk_inpatient_deposit_no") || message.contains("deposit_no")) {
            return "预交金编号已存在";
        }
        if (message.contains("uk_inpatient_discharge_no") || message.contains("discharge_no")) {
            return "出院结算编号已存在";
        }
        if (message.contains("uk_inpatient_discharge_active_admission")) {
            return "该住院记录已存在未取消的出院结算";
        }
        if (message.contains("uk_exam_lab_request_no") || message.contains("request_no")) {
            return "检查检验申请单号已存在";
        }
        if (message.contains("uk_medical_record_no") || message.contains("record_no")) {
            return "病历编号已存在";
        }
        if (message.contains("uk_department_dept_code") || message.contains("dept_code")) {
            return "科室编码已存在";
        }
        if (message.contains("uk_staff_staff_no") || message.contains("staff_no")) {
            return "工号已存在";
        }
        if (message.contains("uk_dict_item_type_code") || (message.contains("dict_type") && message.contains("dict_code"))) {
            return "字典类型和编码已存在";
        }
        if (message.contains("not-null") || message.contains("null value")) {
            return "必填字段不能为空";
        }
        return "数据库保存失败，请检查患者信息";
    }

    private Map<String, String> collectFieldErrors(BindException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        return errors;
    }

    private String deepestMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? "" : current.getMessage();
    }
}
