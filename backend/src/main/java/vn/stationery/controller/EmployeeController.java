package vn.stationery.controller;
import org.springframework.web.bind.annotation.*; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.validation.annotation.Validated;
import lombok.RequiredArgsConstructor; import vn.stationery.service.EmployeeService; import vn.stationery.dto.EmployeeDto;
@RestController @RequestMapping("/api/employees") @RequiredArgsConstructor @Validated
public class EmployeeController { private final EmployeeService service;
 @GetMapping @PreAuthorize("hasAuthority('EMPLOYEE_READ')") public Object list(@RequestParam(defaultValue="0") @javax.validation.constraints.Min(0) int page,@RequestParam(defaultValue="25") int size) { return service.list(page,size); }
 @PostMapping @PreAuthorize("hasAuthority('EMPLOYEE_WRITE')") public EmployeeDto create(@javax.validation.Valid @RequestBody EmployeeDto dto) { return service.save(null,dto); }
 @PutMapping("/{id}") @PreAuthorize("hasAuthority('EMPLOYEE_WRITE')") public EmployeeDto update(@PathVariable Long id,@javax.validation.Valid @RequestBody EmployeeDto dto) { return service.save(id,dto); }
}
