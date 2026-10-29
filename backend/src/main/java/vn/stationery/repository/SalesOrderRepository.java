package vn.stationery.repository;
import vn.stationery.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SalesOrderRepository extends JpaRepository<SalesOrder,Long> {
 org.springframework.data.domain.Page<SalesOrder> findByCustomerId(Long customerId, org.springframework.data.domain.Pageable pageable);
 @org.springframework.data.jpa.repository.Lock(javax.persistence.LockModeType.PESSIMISTIC_WRITE)
 @org.springframework.data.jpa.repository.Query("select o from SalesOrder o where o.id=:id")
 java.util.Optional<SalesOrder> lock(@org.springframework.data.repository.query.Param("id") Long id);
}
