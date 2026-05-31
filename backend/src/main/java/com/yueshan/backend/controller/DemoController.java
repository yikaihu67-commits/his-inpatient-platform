package com.yueshan.backend.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yueshan.backend.common.ApiResponse;
import com.yueshan.backend.exception.BusinessException;
import com.yueshan.backend.service.DemoDataService;

@RestController
@RequestMapping(value = "/api/demo", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
public class DemoController {
    private final DemoDataService demoDataService;
    private final boolean resetEnabled;

    public DemoController(DemoDataService demoDataService,
            @Value("${demo.reset.enabled:true}") boolean resetEnabled) {
        this.demoDataService = demoDataService;
        this.resetEnabled = resetEnabled;
    }

    @PostMapping("/reset")
    public ApiResponse<Map<String, Object>> resetDemoData() {
        if (!resetEnabled) {
            throw new BusinessException("演示数据重置接口未启用");
        }
        return ApiResponse.success("演示数据重置成功", demoDataService.reset());
    }
}
