package my.love.mother.AppWebFlux.service;

import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.love.mother.AppWebFlux.domain.Anime;
import my.love.mother.AppWebFlux.repository.AnimeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnimeService {
    private final AnimeRepository animeRepository;

    public Flux<Anime> findAll() {
        return animeRepository.findAll();
    }

    public Mono<Anime> findById(long id) {
        return animeRepository.findById(id)
                .switchIfEmpty(monoResponseStatusException())
                .log();
    }

    public <T> Mono<T> monoResponseStatusException() {
        return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "сообщение не найдено"));
    }

    public Mono<Anime> save(Anime anime) {
        return animeRepository.save(anime);
    }

    public Mono<Void> update(Anime anime) {
        return findById(anime.getId())
                .flatMap(validAnime -> animeRepository.save(anime))
                .then();
    }

    public Mono<Void> delete(long id) {
        return findById(id)
                .flatMap(animeRepository::delete);
    }

    @Transactional
    public Flux<Anime> saveAll(List<Anime> animeList) {
        return animeRepository.saveAll(animeList)
                .doOnNext(this::throwResponseStatusExceptionWhenEmptyName);
    }

    private void throwResponseStatusExceptionWhenEmptyName(Anime anime) {
        if (StringUtil.isNullOrEmpty(anime.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid name");
        }
    }
}
