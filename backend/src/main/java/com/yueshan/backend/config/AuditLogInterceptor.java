package com.yueshan.backend.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.yueshan.backend.service.OperationLogService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuditLogInterceptor implements HandlerInterceptor {

    private static final Pattern ID_PATTERN = Pattern.compile("/(\\d+)(?:/|$)");

    private final OperationLogService operationLogService;
    private final Map<String, String> modules = new LinkedHashMap<>();

    public AuditLogInterceptor(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
        modules.put("/api/patients", "患者管理");
        modules.put("/api/admissions", "入院登记");
        modules.put("/api/beds", "床位管理");
        modules.put("/api/orders", "医嘱管理");
        modules.put("/api/nursing/executions", "护士执行");
        modules.put("/api/drugs", "药品管理");
        modules.put("/api/pharmacy/dispenses", "药房发药");
        modules.put("/api/fees", "费用账单");
        modules.put("/api/deposits", "预交金管理");
        modules.put("/api/discharges", "出院结算");
        modules.put("/api/exam-lab-requests", "检查检验");
        modules.put("/api/medical-records", "病历记录");
        modules.put("/api/departments", "科室管理");
        modules.put("/api/staff", "人员管理");
        modules.put("/api/dict-items", "基础字典");
        modules.put("/api/operation-logs", "操作日志");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (!shouldAudit(request)) {
            return;
        }
        String uri = request.getRequestURI();
        String resultStatus = response.getStatus() >= 400 || ex != null ? "FAILED" : "SUCCESS";
        String errorMessage = ex == null ? null : ex.getMessage();
        if (errorMessage == null && response.getStatus() >= 400) {
            errorMessage = "HTTP status " + response.getStatus();
        }
        operationLogService.record(resolveModule(uri), resolveOperationType(request.getMethod(), uri),
                resolveBusinessId(uri), null, resolveOperator(request), resultStatus, errorMessage);
    }

    private boolean shouldAudit(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri != null && uri.startsWith("/api/")
                && !"GET".equalsIgnoreCase(request.getMethod())
                && !uri.startsWith("/api/health");
    }

    private String resolveModule(String uri) {
        for (Map.Entry<String, String> entry : modules.entrySet()) {
            if (uri.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "系统";
    }

    private String resolveOperationType(String method, String uri) {
        if ("DELETE".equalsIgnoreCase(method)) return "DELETE";
        if ("PUT".equalsIgnoreCase(method)) return "UPDATE";
        if (uri.endsWith("/submit")) return "SUBMIT";
        if (uri.endsWith("/check")) return "CHECK";
        if (uri.endsWith("/execute") || uri.endsWith("/dispense") || uri.endsWith("/admit") || uri.endsWith("/assign")) return "EXECUTE";
        if (uri.endsWith("/settle")) return "SETTLE";
        if (uri.endsWith("/cancel") || uri.endsWith("/return") || uri.endsWith("/refund")) return "CANCEL";
        if (uri.endsWith("/enable")) return "ENABLE";
        if (uri.endsWith("/disable") || uri.endsWith("/stop") || uri.endsWith("/fail") || uri.endsWith("/release")) return "DISABLE";
        if ("POST".equalsIgnoreCase(method)) return "CREATE";
        return "OTHER";
    }

    private Long resolveBusinessId(String uri) {
        Matcher matcher = ID_PATTERN.matcher(uri);
        if (!matcher.find()) {
            return null;
        }
        try {
            return Long.valueOf(matcher.group(1));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String resolveOperator(HttpServletRequest request) {
        String operator = request.getHeader("X-Operator-Name");
        if (operator == null || operator.isBlank()) {
            operator = request.getParameter("operatorName");
        }
        return operator == null || operator.isBlank() ? "system" : operator.trim();
    }
}
