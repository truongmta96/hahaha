package vn.stationery.service;
import lombok.RequiredArgsConstructor; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate; import vn.stationery.dto.RoleRequest; import vn.stationery.entity.Role; import vn.stationery.repository.*;
@Service @RequiredArgsConstructor
public class RoleService {
 private final RoleRepository roles; private final PermissionRepository permissions; private final JdbcTemplate jdbc; private final AuditService audit;
 @Transactional public Role save(Long id,RoleRequest request) {
  for(Long p:request.permissionIds) if(!permissions.existsById(p)) throw new IllegalArgumentException("Quyền không tồn tại");
  Role role=id==null?new Role():roles.findById(id).orElseThrow(()->new IllegalArgumentException("Vai trò không tồn tại"));
  if("ADMIN".equals(role.getCode())) throw new IllegalArgumentException("Vai trò ADMIN hệ thống không thể thay đổi");
  role.setCode(request.code);role.setName(request.name);roles.save(role);
  jdbc.update("DELETE FROM role_permissions WHERE role_id=?",role.getId());
  for(Long p:request.permissionIds) jdbc.update("INSERT INTO role_permissions(role_id,permission_id) VALUES(?,?)",role.getId(),p);
  audit.record("SAVE_PERMISSIONS","Role",role.getId(),request.permissionIds.toString());return role;
 }
}
