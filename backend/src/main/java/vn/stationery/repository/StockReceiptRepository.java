package vn.stationery.repository;
import vn.stationery.entity.StockReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
public interface StockReceiptRepository extends JpaRepository<StockReceipt,Long> {
 @org.springframework.data.jpa.repository.Lock(javax.persistence.LockModeType.PESSIMISTIC_WRITE)
 @org.springframework.data.jpa.repository.Query("select r from StockReceipt r where r.id=:id")
 java.util.Optional<StockReceipt> lock(@org.springframework.data.repository.query.Param("id") Long id);
}
