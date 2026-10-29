package vn.stationery.mapper;
import vn.stationery.entity.Product; import vn.stationery.dto.ProductDto;
public final class ProductMapper {
 private ProductMapper() {}
 public static void apply(ProductDto d, Product e) { e.setSku(d.sku); e.setName(d.name); e.setCategory(d.category); e.setUnit(d.unit); e.setPrice(d.price); e.setActive(d.active); }
 public static ProductDto dto(Product e) { ProductDto d=new ProductDto(); d.id=e.getId(); d.sku=e.getSku(); d.name=e.getName(); d.category=e.getCategory(); d.unit=e.getUnit(); d.price=e.getPrice(); d.active=e.isActive(); return d; }
}
