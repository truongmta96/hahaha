package vn.stationery.repository;
import vn.stationery.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
public interface StockMovementRepository extends JpaRepository<StockMovement,Long> {

}
