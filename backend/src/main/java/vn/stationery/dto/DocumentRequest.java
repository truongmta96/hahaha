package vn.stationery.dto;
import javax.validation.Valid; import javax.validation.constraints.*; import java.util.List;
public class DocumentRequest {
 @NotNull @Positive public Long warehouseId;
 @Positive public Long customerId;
 @Size(max=255) public String supplier;
 @NotEmpty @Size(max=500) @Valid public List<Line> lines;
 public static class Line {
  @NotNull @Positive public Long productId;
  @Min(1) @Max(1000000000) public long quantity;
  @NotNull @DecimalMin("0.00") @Digits(integer=17,fraction=2) public java.math.BigDecimal unitPrice;
 }
}
