package vn.stationery.controller;
import lombok.RequiredArgsConstructor; import org.springframework.web.bind.annotation.*; import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.jdbc.core.JdbcTemplate; import org.springframework.cache.annotation.Cacheable;
@RestController @RequestMapping("/api/dashboard") @RequiredArgsConstructor
public class DashboardController {
 private final JdbcTemplate jdbc;
 @GetMapping @PreAuthorize("hasAuthority('REPORT_READ')") @Cacheable("dashboard")
 public java.util.Map<String,Object> stats() { return jdbc.queryForMap("SELECT (SELECT COUNT(*) FROM products) products,(SELECT COUNT(*) FROM customers) customers,(SELECT COUNT(*) FROM sales_orders) orders,(SELECT COALESCE(SUM(total),0) FROM sales_orders WHERE status='FULFILLED') revenue,(SELECT COUNT(*) FROM stock_receipts WHERE status='PENDING') pendingReceipts"); }
}
