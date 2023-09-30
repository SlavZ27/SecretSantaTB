package secretSanta.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import secretSanta.security.Roles;
import secretSanta.entity.Authority;
import secretSanta.entity.User;
import secretSanta.repository.AuthorityRepository;

import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthorityService {

    private final AuthorityRepository authorityRepository;

    /**
     * Create a new authority and save it to repository
     *
     * @param user the user
     * @param role the role
     * @return {@link Authority}
     */
    public Authority addAuthority(User user, Roles role) {
        Authority tempAuthority = new Authority();
        tempAuthority.setAuthority(role);
        tempAuthority.setUser(user);
        Authority newAuthority = authorityRepository.save(tempAuthority);
        log.info("New Authority {} has been created with user {}",
                newAuthority.getAuthority(), user.getUsername());
        return newAuthority;
    }

    public Set<Authority> getAuthorities(User user) {
        return authorityRepository.getAllByUsername(user.getUsername());
    }

    public Set<Authority> getAuthorities(String username) {
        return authorityRepository.getAllByUsername(username);
    }
}
