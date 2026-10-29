package vn.stationery.mapper;
import vn.stationery.entity.Employee; import vn.stationery.dto.EmployeeDto;
public final class EmployeeMapper {
 private EmployeeMapper() {}
 public static void apply(EmployeeDto d, Employee e) { e.setCode(d.code); e.setName(d.name); e.setEmail(d.email); e.setDepartment(d.department); e.setActive(d.active); }
 public static EmployeeDto dto(Employee e) { EmployeeDto d=new EmployeeDto(); d.id=e.getId(); d.code=e.getCode(); d.name=e.getName(); d.email=e.getEmail(); d.department=e.getDepartment(); d.active=e.isActive(); return d; }
}
