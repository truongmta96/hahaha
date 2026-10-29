package vn.stationery.repository;
import vn.stationery.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
public interface UserAccountRepository extends JpaRepository<UserAccount,Long> {
 java.util.Optional<UserAccount> findByUsername(String username);
}
