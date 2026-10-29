package vn.stationery.controller;
import org.springframework.web.bind.annotation.*; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.validation.annotation.Validated;
import lombok.RequiredArgsConstructor; import vn.stationery.service.ProductService; import vn.stationery.dto.ProductDto;
@RestController @RequestMapping("/api/products") @RequiredArgsConstructor @Validated
public class ProductController { private final ProductService service;
 @GetMapping @PreAuthorize("hasAuthority('PRODUCT_READ')") public Object list(@RequestParam(defaultValue="0") @javax.validation.constraints.Min(0) long after,@RequestParam(defaultValue="25") int size,@RequestParam(defaultValue="") String sku) { return service.list(after,size,sku); }
 @PostMapping @PreAuthorize("hasAuthority('PRODUCT_WRITE')") public ProductDto create(@javax.validation.Valid @RequestBody ProductDto dto) { return service.save(null,dto); }
 @PutMapping("/{id}") @PreAuthorize("hasAuthority('PRODUCT_WRITE')") public ProductDto update(@PathVariable Long id,@javax.validation.Valid @RequestBody ProductDto dto) { return service.save(id,dto); }
}
