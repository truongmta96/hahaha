package vn.stationery.dto;
import javax.validation.constraints.*;
public class EmployeeDto { public Long id; @NotBlank @Size(max=64) public String code; @NotBlank @Size(max=255) public String name; @Email @Size(max=255) public String email; @Size(max=120) public String department; public boolean active = true; }
