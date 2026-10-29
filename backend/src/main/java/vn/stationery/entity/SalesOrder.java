package vn.stationery.entity;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Entity @Table(name="sales_orders") @Getter @Setter
public class SalesOrder {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private String reference;
    private Long customerId;
    private Long warehouseId;
    private String status;
    @Column(precision=19, scale=2) private java.math.BigDecimal total;
    private Long createdBy;
    private java.time.Instant createdAt = java.time.Instant.now();
}
