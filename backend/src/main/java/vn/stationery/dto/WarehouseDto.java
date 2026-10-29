package vn.stationery.dto;
import javax.validation.constraints.*;
public class WarehouseDto { public Long id; @NotBlank @Size(max=64) public String code; @NotBlank @Size(max=255) public String name; }
