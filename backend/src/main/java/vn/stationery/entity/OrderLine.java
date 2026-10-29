package vn.stationery.entity;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Entity @Table(name="order_lines") @Getter @Setter
public class OrderLine {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private Long orderId;
    private Long productId;
    private long quantity;
    @Column(precision=19, scale=2) private java.math.BigDecimal unitPrice;
}
