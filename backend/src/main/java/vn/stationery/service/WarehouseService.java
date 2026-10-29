package vn.stationery.service;
import vn.stationery.entity.Warehouse; import vn.stationery.dto.WarehouseDto; import vn.stationery.mapper.WarehouseMapper; import vn.stationery.repository.WarehouseRepository;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import lombok.RequiredArgsConstructor;
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class WarehouseService {
 private final WarehouseRepository repo; private final AuditService audit;
 public org.springframework.data.domain.Page<WarehouseDto> list(int page,int size) { return repo.findAll(org.springframework.data.domain.PageRequest.of(page,Math.min(Math.max(size,1),100),org.springframework.data.domain.Sort.by("id").descending())).map(WarehouseMapper::dto); }
 @Transactional public WarehouseDto save(Long id,WarehouseDto dto) {
  Warehouse e=id==null?new Warehouse():repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dữ liệu"));
  WarehouseMapper.apply(dto,e); repo.save(e); audit.record(id==null?"CREATE":"UPDATE","Warehouse",e.getId(),"Catalog change"); return WarehouseMapper.dto(e);
 }
}
