package com.alert.news.controller;

import com.alert.news.dto.CustomerDto;
import com.alert.news.model.Customer;
import com.alert.news.service.CustomerService;
import com.alert.news.service.WebSocketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 고객사 REST API 컨트롤러
 * 
 * 고객사 관리, 인증, 연결 상태 등의 REST API를 제공합니다.
 * Swagger/OpenAPI 문서화를 위한 어노테이션을 포함합니다.
 */
@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Customer", description = "고객사 관련 API")
public class CustomerController {

    private final CustomerService customerService;
    private final WebSocketService webSocketService;

    @Autowired
    public CustomerController(CustomerService customerService, WebSocketService webSocketService) {
        this.customerService = customerService;
        this.webSocketService = webSocketService;
    }

    /**
     * 특정 고객사 조회
     */
    @GetMapping("/{id}")
    @Operation(summary = "고객사 조회", description = "ID로 특정 고객사를 조회합니다.")
    public ResponseEntity<CustomerDto> getCustomerById(
            @Parameter(description = "고객사 ID", required = true)
            @PathVariable String id) {
        
        Customer customer = customerService.getCustomerById(id);
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }
        
        CustomerDto customerDto = customerService.convertToDto(customer);
        return ResponseEntity.ok(customerDto);
    }

    /**
     * 고객사 인증
     */
    @PostMapping("/auth")
    @Operation(summary = "고객사 인증", description = "고객사 ID와 토큰으로 인증을 수행합니다.")
    public ResponseEntity<CustomerDto> authenticateCustomer(
            @Parameter(description = "고객사 ID", required = true)
            @RequestParam String customerId,
            
            @Parameter(description = "인증 토큰", required = true)
            @RequestParam String token) {
        
        boolean isAuthenticated = customerService.authenticateCustomer(customerId, token);
        if (!isAuthenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Customer customer = customerService.getCustomerById(customerId);
        CustomerDto customerDto = customerService.convertToDto(customer);
        return ResponseEntity.ok(customerDto);
    }

    /**
     * 고객사 연결 상태 확인
     */
    @GetMapping("/{id}/connections")
    @Operation(summary = "연결 상태 확인", description = "특정 고객사의 WebSocket 연결 상태를 확인합니다.")
    public ResponseEntity<WebSocketService.ConnectionStatus> getCustomerConnectionStatus(
            @Parameter(description = "고객사 ID", required = true)
            @PathVariable String id) {
        
        boolean isConnected = webSocketService.isCustomerConnected(id);
        if (!isConnected) {
            return ResponseEntity.ok(new WebSocketService.ConnectionStatus(0, 0));
        }
        
        WebSocketService.ConnectionStatus status = webSocketService.getConnectionStatus();
        return ResponseEntity.ok(status);
    }

    /**
     * 활성 고객사 목록 조회
     */
    @GetMapping("/active")
    @Operation(summary = "활성 고객사 목록", description = "활성 상태인 고객사 목록을 조회합니다.")
    public ResponseEntity<List<CustomerDto>> getActiveCustomers() {
        List<Customer> activeCustomers = customerService.getActiveCustomers();
        List<CustomerDto> activeCustomersDto = activeCustomers.stream()
                .map(customerService::convertToDto)
                .toList();
        
        return ResponseEntity.ok(activeCustomersDto);
    }

    /**
     * 연결된 고객사 목록 조회
     */
    @GetMapping("/connected")
    @Operation(summary = "연결된 고객사 목록", description = "현재 WebSocket에 연결된 고객사 목록을 조회합니다.")
    public ResponseEntity<List<CustomerDto>> getConnectedCustomers() {
        List<Customer> connectedCustomers = customerService.getConnectedCustomers();
        List<CustomerDto> connectedCustomersDto = connectedCustomers.stream()
                .map(customerService::convertToDto)
                .toList();
        
        return ResponseEntity.ok(connectedCustomersDto);
    }

    /**
     * 새로운 고객사 생성
     */
    @PostMapping
    @Operation(summary = "고객사 생성", description = "새로운 고객사를 생성합니다.")
    public ResponseEntity<CustomerDto> createCustomer(
            @Parameter(description = "고객사명", required = true)
            @RequestParam String name) {
        
        try {
            Customer customer = customerService.createCustomer(name);
            CustomerDto customerDto = customerService.convertToDto(customer);
            return ResponseEntity.status(HttpStatus.CREATED).body(customerDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 토큰 갱신
     */
    @PostMapping("/{id}/refresh-token")
    @Operation(summary = "토큰 갱신", description = "고객사의 인증 토큰을 갱신합니다.")
    public ResponseEntity<CustomerDto> refreshToken(
            @Parameter(description = "고객사 ID", required = true)
            @PathVariable String id) {
        
        boolean isRefreshed = customerService.refreshToken(id);
        if (!isRefreshed) {
            return ResponseEntity.notFound().build();
        }
        
        Customer customer = customerService.getCustomerById(id);
        CustomerDto customerDto = customerService.convertToDto(customer);
        return ResponseEntity.ok(customerDto);
    }

    /**
     * 고객사 비활성화
     */
    @PostMapping("/{id}/deactivate")
    @Operation(summary = "고객사 비활성화", description = "고객사를 비활성 상태로 변경합니다.")
    public ResponseEntity<Void> deactivateCustomer(
            @Parameter(description = "고객사 ID", required = true)
            @PathVariable String id) {
        
        Customer customer = customerService.getCustomerById(id);
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }
        
        customer.setActive(false);
        customerService.saveCustomer(customer);
        
        return ResponseEntity.ok().build();
    }

    /**
     * 고객사 활성화
     */
    @PostMapping("/{id}/activate")
    @Operation(summary = "고객사 활성화", description = "고객사를 활성 상태로 변경합니다.")
    public ResponseEntity<Void> activateCustomer(
            @Parameter(description = "고객사 ID", required = true)
            @PathVariable String id) {
        
        Customer customer = customerService.getCustomerById(id);
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }
        
        customer.setActive(true);
        customerService.saveCustomer(customer);
        
        return ResponseEntity.ok().build();
    }

    /**
     * WebSocket 연결 상태 조회
     */
    @GetMapping("/websocket/status")
    @Operation(summary = "WebSocket 연결 상태", description = "전체 WebSocket 연결 상태를 조회합니다.")
    public ResponseEntity<WebSocketService.ConnectionStatus> getWebSocketStatus() {
        WebSocketService.ConnectionStatus status = webSocketService.getConnectionStatus();
        return ResponseEntity.ok(status);
    }
}
