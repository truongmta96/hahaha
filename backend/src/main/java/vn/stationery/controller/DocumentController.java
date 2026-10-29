package vn.stationery.controller;
import lombok.RequiredArgsConstructor; import org.springframework.web.bind.annotation.*; import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.*; import org.springframework.validation.annotation.Validated;
import vn.stationery.service.*; import vn.stationery.dto.*; import vn.stationery.repository.*;
@RestController @RequestMapping("/api") @RequiredArgsConstructor @Validated
public class DocumentController {
 private final ReceiptService receiptService; private final OrderService orderService; private final StockReceiptRepository receipts; private final SalesOrderRepository orders; private final InventoryBalanceRepository balances; private final AuditLogRepository audit;
 private Pageable page(int page,int size) { return PageRequest.of(page,Math.min(Math.max(size,1),100),Sort.by("id").descending()); }
 @GetMapping("/receipts") @PreAuthorize("hasAuthority('RECEIPT_READ')") public Object receipts(@RequestParam(defaultValue="0") @javax.validation.constraints.Min(0) int page,@RequestParam(defaultValue="25") int size) { return receipts.findAll(page(page,size)); }
 @GetMapping("/receipts/{id}") @PreAuthorize("hasAuthority('RECEIPT_READ')") public Object receipt(@PathVariable Long id) { return receiptService.detail(id); }
 @PostMapping("/receipts") @PreAuthorize("hasAuthority('RECEIPT_WRITE')") public Object receipt(@javax.validation.Valid @RequestBody DocumentRequest r) { return receiptService.create(r); }
 @PostMapping("/receipts/{id}/decision") @PreAuthorize("hasAnyAuthority('RECEIPT_APPROVE_1','RECEIPT_APPROVE_2')") public Object decision(@PathVariable Long id,@javax.validation.Valid @RequestBody DecisionRequest r) { return receiptService.decide(id,r); }
 @GetMapping("/orders") @PreAuthorize("hasAuthority('ORDER_READ')") public Object orders(@RequestParam(defaultValue="0") @javax.validation.constraints.Min(0) int page,@RequestParam(defaultValue="25") int size,@RequestParam(required=false) Long customerId) { return customerId==null?orders.findAll(page(page,size)):orders.findByCustomerId(customerId,page(page,size)); }
 @GetMapping("/orders/{id}") @PreAuthorize("hasAuthority('ORDER_READ')") public Object order(@PathVariable Long id) { return orderService.detail(id); }
 @PostMapping("/orders") @PreAuthorize("hasAuthority('ORDER_WRITE')") public Object order(@javax.validation.Valid @RequestBody DocumentRequest r) { return orderService.create(r); }
 @PostMapping("/orders/{id}/fulfill") @PreAuthorize("hasAuthority('ORDER_WRITE')") public Object fulfill(@PathVariable Long id) { return orderService.fulfill(id); }
 @GetMapping("/inventory") @PreAuthorize("hasAuthority('INVENTORY_READ')") public Object inventory(@RequestParam(defaultValue="1") Long warehouseId,@RequestParam(defaultValue="0") @javax.validation.constraints.Min(0) int page,@RequestParam(defaultValue="25") int size) { return balances.findByWarehouseId(warehouseId,page(page,size)); }
 @GetMapping("/audit") @PreAuthorize("hasAuthority('AUDIT_READ')") public Object audit(@RequestParam(defaultValue="0") @javax.validation.constraints.Min(0) int page,@RequestParam(defaultValue="25") int size) { return audit.findAll(page(page,size)); }
}
