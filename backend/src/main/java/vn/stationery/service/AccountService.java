package vn.stationery.service;
import lombok.RequiredArgsConstructor; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate; import org.springframework.security.crypto.password.PasswordEncoder;
import vn.stationery.dto.UserRequest; import vn.stationery.entity.UserAccount; import vn.stationery.repository.*;
@Service @RequiredArgsConstructor
public class AccountService {
 private final UserAccountRepository users; private final EmployeeRepository employees; private final RoleRepository roles; private final JdbcTemplate jdbc; private final PasswordEncoder encoder; private final AuditService audit;
 @Transactional public UserAccount create(UserRequest request) {
  if(request.password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72) throw new IllegalArgumentException("Mật khẩu vượt 72 byte UTF-8");
  if(request.employeeId!=null&&!employees.existsById(request.employeeId)) throw new IllegalArgumentException("Nhân viên không tồn tại");
  for(Long id:request.roleIds) if(!roles.existsById(id)) throw new IllegalArgumentException("Vai trò không tồn tại");
  UserAccount u=new UserAccount(); u.setUsername(request.username); u.setPasswordHash(encoder.encode(request.password)); u.setEmployeeId(request.employeeId); users.save(u);
  for(Long id:request.roleIds) jdbc.update("INSERT INTO user_roles(user_id,role_id) VALUES(?,?)",u.getId(),id);
  audit.record("CREATE","UserAccount",u.getId(),"Account and roles created"); return u;
 }
 @Transactional public void enabled(Long id,boolean enabled) { UserAccount u=users.findById(id).orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại")); u.setEnabled(enabled); audit.record("UPDATE","UserAccount",id,"enabled="+enabled); }
 @Transactional public void assign(Long id,java.util.Set<Long> ids) {
  if(!users.existsById(id)||ids==null||ids.isEmpty()||ids.stream().anyMatch(x -> x==null||!roles.existsById(x))) throw new IllegalArgumentException("Tài khoản hoặc vai trò không hợp lệ");
  jdbc.update("DELETE FROM user_roles WHERE user_id=?",id);
  for(Long role:ids) jdbc.update("INSERT INTO user_roles(user_id,role_id) VALUES(?,?)",id,role);
  audit.record("ASSIGN_ROLES","UserAccount",id,ids.toString());
 }
}
