package vn.stationery.repository;
import vn.stationery.entity.OrderLine;
import org.springframework.data.jpa.repository.JpaRepository;
public interface OrderLineRepository extends JpaRepository<OrderLine,Long> {
 java.util.List<OrderLine> findByOrderId(Long orderId);
}
