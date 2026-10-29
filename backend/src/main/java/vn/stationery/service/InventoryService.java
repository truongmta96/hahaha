package vn.stationery.service;
import lombok.RequiredArgsConstructor; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate; import vn.stationery.entity.*; import vn.stationery.repository.*;
@Service @RequiredArgsConstructor
public class InventoryService {
 private final InventoryBalanceRepository balances; private final StockMovementRepository movements; private final JdbcTemplate jdbc;
 @Transactional(propagation=Propagation.MANDATORY)
 public void change(Long warehouse,Long product,long delta,String kind,Long document) {
  // Unique (warehouse_id,product_id) plus InnoDB row locks serialize concurrent changes.
  jdbc.update("INSERT INTO inventory_balances(warehouse_id,product_id,quantity) VALUES(?,?,0) ON DUPLICATE KEY UPDATE id=id",warehouse,product);
  InventoryBalance b=balances.lock(warehouse,product).orElseThrow();
  long next=Math.addExact(b.getQuantity(),delta);
  if(next<0) throw new IllegalArgumentException("Không đủ tồn kho cho sản phẩm "+product);
  b.setQuantity(next); balances.save(b);
  StockMovement m=new StockMovement(); m.setWarehouseId(warehouse); m.setProductId(product); m.setQuantity(delta); m.setKind(kind); m.setDocumentId(document); movements.save(m);
 }
}
