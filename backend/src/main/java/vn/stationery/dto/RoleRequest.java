package vn.stationery.dto;
import javax.validation.constraints.*;
public class RoleRequest {
 @NotBlank @Pattern(regexp="[A-Z][A-Z0-9_]{1,63}") public String code;
 @NotBlank @Size(max=255) public String name;
 @NotEmpty public java.util.Set<@NotNull @Positive Long> permissionIds;
}
