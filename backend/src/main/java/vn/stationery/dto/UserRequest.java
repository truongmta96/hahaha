package vn.stationery.dto;
import javax.validation.constraints.*;
public class UserRequest {
 @NotBlank @Size(max=100) public String username;
 @NotBlank @Size(min=12,max=72) public String password;
 @Positive public Long employeeId;
 @NotEmpty public java.util.Set<@NotNull @Positive Long> roleIds;
}
