package vn.stationery.controller;
import org.springframework.web.bind.annotation.*; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.validation.annotation.Validated;
import lombok.RequiredArgsConstructor; import vn.stationery.service.CustomerService; import vn.stationery.dto.CustomerDto;
@RestController @RequestMapping("/api/customers") @RequiredArgsConstructor @Validated
public class CustomerController { private final CustomerService service;
 @GetMapping @PreAuthorize("hasAuthority('CUSTOMER_READ')") public Object list(@RequestParam(defaultValue="0") @javax.validation.constraints.Min(0) int page,@RequestParam(defaultValue="25") int size) { return service.list(page,size); }
 @PostMapping @PreAuthorize("hasAuthority('CUSTOMER_WRITE')") public CustomerDto create(@javax.validation.Valid @RequestBody CustomerDto dto) { return service.save(null,dto); }
 @PutMapping("/{id}") @PreAuthorize("hasAuthority('CUSTOMER_WRITE')") public CustomerDto update(@PathVariable Long id,@javax.validation.Valid @RequestBody CustomerDto dto) { return service.save(id,dto); }
}
