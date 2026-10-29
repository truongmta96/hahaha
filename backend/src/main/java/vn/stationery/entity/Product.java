package vn.stationery.entity;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Entity @Table(name="products") @Getter @Setter
public class Product {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private String sku;
    private String name;
    private String category;
    private String unit;
    @Column(precision=19, scale=2) private java.math.BigDecimal price;
    private boolean active = true;
}
