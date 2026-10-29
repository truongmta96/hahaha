package vn.stationery.entity;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Entity @Table(name="user_accounts") @Getter @Setter
public class UserAccount {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private String username;
    @com.fasterxml.jackson.annotation.JsonIgnore private String passwordHash;
    private Long employeeId;
    private boolean enabled = true;
}
