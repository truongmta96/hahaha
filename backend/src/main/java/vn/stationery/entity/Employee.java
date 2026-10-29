package vn.stationery.entity;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Entity @Table(name="employees") @Getter @Setter
public class Employee {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private String code;
    private String name;
    private String email;
    private String department;
    private boolean active = true;
}
