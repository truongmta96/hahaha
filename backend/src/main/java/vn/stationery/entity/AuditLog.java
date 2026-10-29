package vn.stationery.entity;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Entity @Table(name="audit_logs") @Getter @Setter
public class AuditLog {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private Long actorId;
    private String action;
    private String entityType;
    private Long entityId;
    private String details;
    private java.time.Instant createdAt = java.time.Instant.now();
}
