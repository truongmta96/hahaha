package vn.stationery.controller;
import lombok.RequiredArgsConstructor; import org.springframework.web.bind.annotation.*; import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.*; import vn.stationery.service.AccountService; import vn.stationery.repository.*; import vn.stationery.dto.UserRequest;
@RestController @RequestMapping("/api") @RequiredArgsConstructor
public class AccountController {
 private final vn.stationery.service.RoleService roleService; private final AccountService service; private final UserAccountRepository users; private final RoleRepository roles; private final PermissionRepository permissions; private final org.springframework.jdbc.core.JdbcTemplate jdbc;
 @GetMapping("/accounts") @PreAuthorize("hasAuthority('ACCOUNT_WRITE')") public Object accounts(@RequestParam(defaultValue="0") int page) { return users.findAll(PageRequest.of(Math.max(page,0),25,Sort.by("id"))); }
 @PostMapping("/accounts") @PreAuthorize("hasAuthority('ACCOUNT_WRITE')") public Object create(@javax.validation.Valid @RequestBody UserRequest r) { return service.create(r); }
 @PutMapping("/accounts/{id}/enabled") @PreAuthorize("hasAuthority('ACCOUNT_WRITE')") public void enabled(@PathVariable Long id,@RequestParam boolean enabled) { service.enabled(id,enabled); }
 @PutMapping("/accounts/{id}/roles") @PreAuthorize("hasAuthority('ACCOUNT_WRITE')") public void assign(@PathVariable Long id,@RequestBody java.util.Set<Long> ids) { service.assign(id,ids); }
 @PostMapping("/roles") @PreAuthorize("hasAuthority('ACCOUNT_WRITE')") public Object createRole(@javax.validation.Valid @RequestBody vn.stationery.dto.RoleRequest r) { return roleService.save(null,r); }
 @PutMapping("/roles/{id}") @PreAuthorize("hasAuthority('ACCOUNT_WRITE')") public Object updateRole(@PathVariable Long id,@javax.validation.Valid @RequestBody vn.stationery.dto.RoleRequest r) { return roleService.save(id,r); }
 @GetMapping("/accounts/{id}/roles") @PreAuthorize("hasAuthority('ACCOUNT_WRITE')") public Object accountRoles(@PathVariable Long id) { return jdbc.queryForList("SELECT role_id FROM user_roles WHERE user_id=?",Long.class,id); }
 @GetMapping("/roles") @PreAuthorize("hasAuthority('ACCOUNT_WRITE')") public Object roles() { return roles.findAll(); }
 @GetMapping("/permissions") @PreAuthorize("hasAuthority('ACCOUNT_WRITE')") public Object permissions() { return permissions.findAll(); }
 @GetMapping("/roles/{id}/permissions") @PreAuthorize("hasAuthority('ACCOUNT_WRITE')") public Object rolePermissions(@PathVariable Long id) { return jdbc.queryForList("SELECT permission_id FROM role_permissions WHERE role_id=?",Long.class,id); }
}
