package vn.stationery.dto;
import javax.validation.constraints.*;
public class CustomerDto { public Long id; @NotBlank @Size(max=64) public String code; @NotBlank @Size(max=255) public String name; @Size(max=32) public String phone; @Email @Size(max=255) public String email; }
