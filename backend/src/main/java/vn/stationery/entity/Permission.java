package vn.stationery.entity;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Entity @Table(name="permissions") @Getter @Setter
public class Permission {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private String code;
    private String name;
}
