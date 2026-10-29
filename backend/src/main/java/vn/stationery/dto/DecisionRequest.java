package vn.stationery.dto;
import javax.validation.constraints.*;
public class DecisionRequest { @NotBlank @Pattern(regexp="APPROVE|REJECT") public String decision; @Size(max=1000) public String note; }
