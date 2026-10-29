package vn.stationery.entity;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Entity @Table(name="inventory_balances") @Getter @Setter
public class InventoryBalance {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private Long warehouseId;
    private Long productId;
    private long quantity;
}
