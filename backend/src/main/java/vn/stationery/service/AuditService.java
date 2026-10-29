package vn.stationery.service;
import org.springframework.stereotype.Service; import lombok.RequiredArgsConstructor;
import vn.stationery.entity.AuditLog; import vn.stationery.repository.AuditLogRepository;
@Service @RequiredArgsConstructor
public class AuditService { private final AuditLogRepository logs; private final ActorService actor;
 public void record(String action,String type,Long id,String details) { AuditLog a=new AuditLog(); a.setActorId(actor.id()); a.setAction(action); a.setEntityType(type); a.setEntityId(id); a.setDetails(details); logs.save(a); }
}
