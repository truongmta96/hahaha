package vn.stationery.service;
import lombok.RequiredArgsConstructor; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import vn.stationery.dto.*; import vn.stationery.entity.*; import vn.stationery.repository.*; import java.util.*;
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class ReceiptService {
 private final StockReceiptRepository receipts; private final ReceiptLineRepository lines; private final ApprovalRepository approvals;
 private final WarehouseRepository warehouses; private final ProductRepository products; private final InventoryService inventory; private final AuditService audit; private final ActorService actor;
 @Transactional public StockReceipt create(DocumentRequest request) {
  if(!warehouses.existsById(request.warehouseId)) throw new IllegalArgumentException("Kho không tồn tại");
  Set<Long> unique=new HashSet<>();
  for(DocumentRequest.Line l:request.lines) if(!unique.add(l.productId)||!products.existsById(l.productId)) throw new IllegalArgumentException("Sản phẩm trùng hoặc không tồn tại");
  StockReceipt r=new StockReceipt(); r.setReference("IN-"+UUID.randomUUID()); r.setWarehouseId(request.warehouseId); r.setSupplier(request.supplier); r.setStatus("PENDING"); r.setCreatedBy(actor.id()); receipts.save(r);
  for(DocumentRequest.Line l:request.lines) { ReceiptLine line=new ReceiptLine(); line.setReceiptId(r.getId()); line.setProductId(l.productId); line.setQuantity(l.quantity); line.setUnitCost(l.unitPrice); lines.save(line); }
  audit.record("CREATE","StockReceipt",r.getId(),"Submitted for two-level approval"); return r;
 }
 @Transactional public StockReceipt decide(Long id,DecisionRequest request) {
  StockReceipt r=receipts.lock(id).orElseThrow(() -> new IllegalArgumentException("Phiếu không tồn tại"));
  if(!"PENDING".equals(r.getStatus())) throw new IllegalArgumentException("Phiếu đã xử lý");
  Long who=actor.id();
  if(who.equals(r.getCreatedBy())||who.equals(r.getFirstApproverId())) throw new IllegalArgumentException("Người lập và hai người duyệt phải khác nhau");
  String required=r.getApprovalLevel()==0?"RECEIPT_APPROVE_1":"RECEIPT_APPROVE_2";
  boolean allowed=org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(required));
  if(!allowed) throw new org.springframework.security.access.AccessDeniedException("Thiếu quyền "+required);
  Approval a=new Approval(); a.setReceiptId(id); a.setActorId(who); a.setApprovalLevel(r.getApprovalLevel()+1); a.setDecision(request.decision); a.setNote(request.note); approvals.save(a);
  if("REJECT".equals(request.decision)) r.setStatus("REJECTED");
  else {
   r.setApprovalLevel(r.getApprovalLevel()+1);
   if(r.getApprovalLevel()==1) r.setFirstApproverId(who);
   if(r.getApprovalLevel()==2) {
    for(ReceiptLine l:lines.findByReceiptIdOrderByProductIdAsc(id)) inventory.change(r.getWarehouseId(),l.getProductId(),l.getQuantity(),"RECEIPT",id);
    r.setStatus("APPROVED");
   }
  }
  audit.record(request.decision,"StockReceipt",id,"Level "+a.getApprovalLevel()); return r;
 }
 public Object detail(Long id) { StockReceipt r=receipts.findById(id).orElseThrow(() -> new IllegalArgumentException("Phiếu không tồn tại")); return Map.of("receipt",r,"lines",lines.findByReceiptIdOrderByProductIdAsc(id),"approvals",approvals.findByReceiptIdOrderByIdAsc(id)); }
}
