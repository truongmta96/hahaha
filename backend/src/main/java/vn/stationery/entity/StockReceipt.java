package vn.stationery.entity;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Entity @Table(name="stock_receipts") @Getter @Setter
public class StockReceipt {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private String reference;
    private Long warehouseId;
    private String supplier;
    private String status;
    private int approvalLevel;
    private Long createdBy;
    private Long firstApproverId;
    private java.time.Instant createdAt = java.time.Instant.now();
}
