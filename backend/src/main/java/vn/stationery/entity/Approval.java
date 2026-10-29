package vn.stationery.entity;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Entity @Table(name="approvals") @Getter @Setter
public class Approval {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private Long receiptId;
    private Long actorId;
    private int approvalLevel;
    private String decision;
    private String note;
    private java.time.Instant createdAt = java.time.Instant.now();
}
