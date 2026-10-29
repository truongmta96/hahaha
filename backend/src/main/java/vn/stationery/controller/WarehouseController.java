package vn.stationery.controller;
import org.springframework.web.bind.annotation.*; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.validation.annotation.Validated;
import lombok.RequiredArgsConstructor; import vn.stationery.service.WarehouseService; import vn.stationery.dto.WarehouseDto;
@RestController @RequestMapping("/api/warehouses") @RequiredArgsConstructor @Validated
public class WarehouseController { private final WarehouseService service;
 @GetMapping @PreAuthorize("hasAuthority('WAREHOUSE_READ')") public Object list(@RequestParam(defaultValue="0") @javax.validation.constraints.Min(0) int page,@RequestParam(defaultValue="25") int size) { return service.list(page,size); }
 @PostMapping @PreAuthorize("hasAuthority('WAREHOUSE_WRITE')") public WarehouseDto create(@javax.validation.Valid @RequestBody WarehouseDto dto) { return service.save(null,dto); }
 @PutMapping("/{id}") @PreAuthorize("hasAuthority('WAREHOUSE_WRITE')") public WarehouseDto update(@PathVariable Long id,@javax.validation.Valid @RequestBody WarehouseDto dto) { return service.save(id,dto); }
}
