package vn.stationery.service;
import lombok.RequiredArgsConstructor; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import vn.stationery.dto.*; import vn.stationery.entity.*; import vn.stationery.repository.*; import java.util.*; import java.math.BigDecimal;
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class OrderService {
 private final SalesOrderRepository orders; private final OrderLineRepository lines; private final ProductRepository products;
 private final CustomerRepository customers; private final WarehouseRepository warehouses; private final InventoryService inventory; private final AuditService audit; private final ActorService actor;
 @Transactional public SalesOrder create(DocumentRequest request) {
  if(request.customerId==null||!customers.existsById(request.customerId)||!warehouses.existsById(request.warehouseId)) throw new IllegalArgumentException("Khách hàng hoặc kho không tồn tại");
  SalesOrder o=new SalesOrder(); o.setReference("SO-"+UUID.randomUUID()); o.setCustomerId(request.customerId); o.setWarehouseId(request.warehouseId); o.setCreatedBy(actor.id()); o.setStatus("PLACED"); o.setTotal(BigDecimal.ZERO); orders.save(o);
  Set<Long> unique=new HashSet<>(); BigDecimal total=BigDecimal.ZERO;
  for(DocumentRequest.Line l:request.lines) {
   Product p=products.findById(l.productId).orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại"));
   if(!p.isActive()||!unique.add(l.productId)) throw new IllegalArgumentException("Sản phẩm trùng hoặc ngừng bán");
   OrderLine line=new OrderLine(); line.setOrderId(o.getId()); line.setProductId(l.productId); line.setQuantity(l.quantity); line.setUnitPrice(p.getPrice()); lines.save(line);
   total=total.add(p.getPrice().multiply(BigDecimal.valueOf(l.quantity)));
  }
  if(total.precision()-total.scale()>17) throw new IllegalArgumentException("Giá trị đơn vượt giới hạn");
  o.setTotal(total); audit.record("CREATE","SalesOrder",o.getId(),"Order placed; stock is not reserved"); return o;
 }
 @Transactional public SalesOrder fulfill(Long id) {
  SalesOrder o=orders.lock(id).orElseThrow(() -> new IllegalArgumentException("Đơn không tồn tại"));
  // Repeating fulfillment is idempotent; only the locked PLACED -> FULFILLED transition moves stock.
  if("FULFILLED".equals(o.getStatus())) return o;
  if(!"PLACED".equals(o.getStatus())) throw new IllegalArgumentException("Trạng thái không hợp lệ");
  List<OrderLine> sorted=lines.findByOrderId(id); sorted.sort(Comparator.comparing(OrderLine::getProductId));
  for(OrderLine l:sorted) inventory.change(o.getWarehouseId(),l.getProductId(),-l.getQuantity(),"SALE",id);
  o.setStatus("FULFILLED"); audit.record("FULFILL","SalesOrder",id,"Stock deducted"); return o;
 }
 public Object detail(Long id) { return Map.of("order",orders.findById(id).orElseThrow(() -> new IllegalArgumentException("Đơn không tồn tại")),"lines",lines.findByOrderId(id)); }
}
