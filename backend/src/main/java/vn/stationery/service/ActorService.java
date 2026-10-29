package vn.stationery.service;
import org.springframework.stereotype.Service; import org.springframework.security.core.context.SecurityContextHolder;
import lombok.RequiredArgsConstructor; import vn.stationery.repository.UserAccountRepository;
@Service @RequiredArgsConstructor
public class ActorService { private final UserAccountRepository users;
 public Long id() { return users.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow().getId(); }
}
