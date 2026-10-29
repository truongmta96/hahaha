package vn.stationery.repository;
import vn.stationery.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProductRepository extends JpaRepository<Product,Long> {
 org.springframework.data.domain.Slice<Product> findByIdGreaterThanOrderByIdAsc(Long after, org.springframework.data.domain.Pageable page);
 org.springframework.data.domain.Slice<Product> findBySkuStartingWithAndIdGreaterThanOrderByIdAsc(String sku, Long after, org.springframework.data.domain.Pageable page);
 long countByActiveTrue();
}
