package vn.stationery.config;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.jdbc.core.JdbcTemplate;
import vn.stationery.repository.UserAccountRepository;
@Configuration @EnableGlobalMethodSecurity(prePostEnabled=true)
public class SecurityConfig {
 @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
 @Bean UserDetailsService users(UserAccountRepository users, JdbcTemplate jdbc) {
  return username -> {
   vn.stationery.entity.UserAccount u=users.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
   java.util.List<String> permissions=jdbc.queryForList("SELECT DISTINCT p.code FROM permissions p JOIN role_permissions rp ON rp.permission_id=p.id JOIN user_roles ur ON ur.role_id=rp.role_id WHERE ur.user_id=?",String.class,u.getId());
   return User.withUsername(u.getUsername()).password(u.getPasswordHash()).disabled(!u.isEnabled()).authorities(permissions.toArray(new String[0])).build();
  };
 }
 @Bean SecurityFilterChain chain(HttpSecurity http) throws Exception {
  http.csrf().csrfTokenRepository(org.springframework.security.web.csrf.CookieCsrfTokenRepository.withHttpOnlyFalse())
   .and().authorizeRequests().antMatchers("/actuator/health","/api/auth/csrf").permitAll().anyRequest().authenticated()
   .and().httpBasic().and().logout().logoutUrl("/api/auth/logout");
  http.headers().frameOptions().deny();
  return http.build();
 }
}
