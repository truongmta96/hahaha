package vn.stationery.entity;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Entity @Table(name="receipt_lines") @Getter @Setter
public class ReceiptLine {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private Long receiptId;
    private Long productId;
    private long quantity;
    @Column(precision=19, scale=2) private java.math.BigDecimal unitCost;
}
