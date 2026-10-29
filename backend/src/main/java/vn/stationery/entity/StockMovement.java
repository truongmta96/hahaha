package vn.stationery.entity;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Entity @Table(name="stock_movements") @Getter @Setter
public class StockMovement {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private Long warehouseId;
    private Long productId;
    private long quantity;
    private String kind;
    private Long documentId;
    private java.time.Instant createdAt = java.time.Instant.now();
}
