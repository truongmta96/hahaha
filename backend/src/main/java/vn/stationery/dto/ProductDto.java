package vn.stationery.dto;
import javax.validation.constraints.*;
public class ProductDto { public Long id; @NotBlank @Size(max=64) public String sku; @NotBlank @Size(max=255) public String name; @NotBlank @Size(max=120) public String category; @NotBlank @Size(max=32) public String unit; @NotNull @DecimalMin("0.00") @Digits(integer=17,fraction=2) public java.math.BigDecimal price; public boolean active = true; }
