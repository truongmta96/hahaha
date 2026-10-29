package vn.stationery.mapper;
import vn.stationery.entity.Customer; import vn.stationery.dto.CustomerDto;
public final class CustomerMapper {
 private CustomerMapper() {}
 public static void apply(CustomerDto d, Customer e) { e.setCode(d.code); e.setName(d.name); e.setPhone(d.phone); e.setEmail(d.email); }
 public static CustomerDto dto(Customer e) { CustomerDto d=new CustomerDto(); d.id=e.getId(); d.code=e.getCode(); d.name=e.getName(); d.phone=e.getPhone(); d.email=e.getEmail(); return d; }
}
