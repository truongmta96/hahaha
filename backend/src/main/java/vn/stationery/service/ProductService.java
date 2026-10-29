package vn.stationery.service;
import vn.stationery.entity.Product; import vn.stationery.dto.ProductDto; import vn.stationery.mapper.ProductMapper; import vn.stationery.repository.ProductRepository;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import lombok.RequiredArgsConstructor;
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class ProductService {
 private final ProductRepository repo; private final AuditService audit;
 public org.springframework.data.domain.Slice<ProductDto> list(long after,int size,String sku) {
   org.springframework.data.domain.Pageable page=org.springframework.data.domain.PageRequest.of(0,Math.min(Math.max(size,1),100));
   return (sku.isEmpty()? repo.findByIdGreaterThanOrderByIdAsc(after,page):repo.findBySkuStartingWithAndIdGreaterThanOrderByIdAsc(sku,after,page)).map(ProductMapper::dto);
 }
 @Transactional public ProductDto save(Long id,ProductDto dto) {
  Product e=id==null?new Product():repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dữ liệu"));
  ProductMapper.apply(dto,e); repo.save(e); audit.record(id==null?"CREATE":"UPDATE","Product",e.getId(),"Catalog change"); return ProductMapper.dto(e);
 }
}
