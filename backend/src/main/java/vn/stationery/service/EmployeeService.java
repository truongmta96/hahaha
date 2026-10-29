package vn.stationery.service;
import vn.stationery.entity.Employee; import vn.stationery.dto.EmployeeDto; import vn.stationery.mapper.EmployeeMapper; import vn.stationery.repository.EmployeeRepository;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import lombok.RequiredArgsConstructor;
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class EmployeeService {
 private final EmployeeRepository repo; private final AuditService audit;
 public org.springframework.data.domain.Page<EmployeeDto> list(int page,int size) { return repo.findAll(org.springframework.data.domain.PageRequest.of(page,Math.min(Math.max(size,1),100),org.springframework.data.domain.Sort.by("id").descending())).map(EmployeeMapper::dto); }
 @Transactional public EmployeeDto save(Long id,EmployeeDto dto) {
  Employee e=id==null?new Employee():repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dữ liệu"));
  EmployeeMapper.apply(dto,e); repo.save(e); audit.record(id==null?"CREATE":"UPDATE","Employee",e.getId(),"Catalog change"); return EmployeeMapper.dto(e);
 }
}
