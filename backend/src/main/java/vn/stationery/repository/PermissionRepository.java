package vn.stationery.repository;
import vn.stationery.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PermissionRepository extends JpaRepository<Permission,Long> {

}
