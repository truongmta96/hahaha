package vn.stationery.repository;
import vn.stationery.entity.InventoryBalance;
import org.springframework.data.jpa.repository.JpaRepository;
public interface InventoryBalanceRepository extends JpaRepository<InventoryBalance,Long> {
 @org.springframework.data.jpa.repository.Lock(javax.persistence.LockModeType.PESSIMISTIC_WRITE)
 @org.springframework.data.jpa.repository.Query("select b from InventoryBalance b where b.warehouseId=:warehouse and b.productId=:product")
 java.util.Optional<InventoryBalance> lock(@org.springframework.data.repository.query.Param("warehouse") Long warehouse, @org.springframework.data.repository.query.Param("product") Long product);
 org.springframework.data.domain.Page<InventoryBalance> findByWarehouseId(Long warehouseId, org.springframework.data.domain.Pageable pageable);
}
