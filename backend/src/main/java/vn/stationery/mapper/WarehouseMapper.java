package vn.stationery.mapper;
import vn.stationery.entity.Warehouse; import vn.stationery.dto.WarehouseDto;
public final class WarehouseMapper {
 private WarehouseMapper() {}
 public static void apply(WarehouseDto d, Warehouse e) { e.setCode(d.code); e.setName(d.name); }
 public static WarehouseDto dto(Warehouse e) { WarehouseDto d=new WarehouseDto(); d.id=e.getId(); d.code=e.getCode(); d.name=e.getName(); return d; }
}
