package vn.stationery.repository;
import vn.stationery.entity.ReceiptLine;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ReceiptLineRepository extends JpaRepository<ReceiptLine,Long> {
 java.util.List<ReceiptLine> findByReceiptIdOrderByProductIdAsc(Long receiptId);
}
