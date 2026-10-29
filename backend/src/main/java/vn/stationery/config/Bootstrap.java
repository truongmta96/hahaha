package vn.stationery.config;
import lombok.RequiredArgsConstructor; import org.springframework.stereotype.Component; import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value; import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.transaction.annotation.Transactional;
import vn.stationery.entity.UserAccount; import vn.stationery.repository.UserAccountRepository;
@Component @RequiredArgsConstructor
public class Bootstrap implements CommandLineRunner {
 private final UserAccountRepository users; private final PasswordEncoder encoder; private final JdbcTemplate jdbc;
 @Value("${app.bootstrap-password}") private String password;
 @Override @Transactional public void run(String... args) {
  if (users.count()!=0) return;
  if(password.length()<12 || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72) throw new IllegalStateException("Set BOOTSTRAP_ADMIN_PASSWORD to 12+ characters and at most 72 UTF-8 bytes");
  UserAccount user=new UserAccount(); user.setUsername("admin"); user.setPasswordHash(encoder.encode(password)); users.save(user);
  jdbc.update("INSERT INTO user_roles(user_id,role_id) SELECT ?,id FROM roles WHERE code='ADMIN'",user.getId());
 }
}
