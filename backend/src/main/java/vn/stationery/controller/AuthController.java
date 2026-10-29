package vn.stationery.controller;
import org.springframework.web.bind.annotation.*; import org.springframework.security.web.csrf.CsrfToken;
import java.security.Principal;
@RestController @RequestMapping("/api/auth")
public class AuthController {
 @GetMapping("/csrf") public Object csrf(CsrfToken token) { return java.util.Map.of("token",token.getToken(),"headerName",token.getHeaderName()); }
 @GetMapping("/me") public Object me(Principal principal) {
  org.springframework.security.core.Authentication a=(org.springframework.security.core.Authentication)principal;
  return java.util.Map.of("username",a.getName(),"permissions",a.getAuthorities());
 }
}
