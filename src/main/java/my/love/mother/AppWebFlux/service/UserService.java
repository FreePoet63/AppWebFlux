package my.love.mother.AppWebFlux.service;

import lombok.RequiredArgsConstructor;
import my.love.mother.AppWebFlux.repository.UserRepository;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class UserService implements ReactiveUserDetailsService {
    private final UserRepository userRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUsername(username).cast(UserDetails.class);
    }
}
