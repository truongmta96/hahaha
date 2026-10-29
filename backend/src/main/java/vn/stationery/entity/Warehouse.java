package vn.stationery.entity;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Entity @Table(name="warehouses") @Getter @Setter
public class Warehouse {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private String code;
    private String name;
}
