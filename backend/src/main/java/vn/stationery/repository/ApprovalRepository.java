package vn.stationery.repository;
import vn.stationery.entity.Approval;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ApprovalRepository extends JpaRepository<Approval,Long> {
 java.util.List<Approval> findByReceiptIdOrderByIdAsc(Long receiptId);
}
