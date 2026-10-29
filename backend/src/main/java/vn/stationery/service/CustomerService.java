package vn.stationery.service;
import vn.stationery.entity.Customer; import vn.stationery.dto.CustomerDto; import vn.stationery.mapper.CustomerMapper; import vn.stationery.repository.CustomerRepository;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import lombok.RequiredArgsConstructor;
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class CustomerService {
 private final CustomerRepository repo; private final AuditService audit;
 public org.springframework.data.domain.Page<CustomerDto> list(int page,int size) { return repo.findAll(org.springframework.data.domain.PageRequest.of(page,Math.min(Math.max(size,1),100),org.springframework.data.domain.Sort.by("id").descending())).map(CustomerMapper::dto); }
 @Transactional public CustomerDto save(Long id,CustomerDto dto) {
  Customer e=id==null?new Customer():repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dữ liệu"));
  CustomerMapper.apply(dto,e); repo.save(e); audit.record(id==null?"CREATE":"UPDATE","Customer",e.getId(),"Catalog change"); return CustomerMapper.dto(e);
 }
}
