package my.love.mother.AppWebFlux.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.love.mother.AppWebFlux.domain.Anime;
import my.love.mother.AppWebFlux.service.AnimeService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("anime")
@Slf4j
@RequiredArgsConstructor
@SecurityScheme(
        name = "Basic Authentication",
        type = SecuritySchemeType.HTTP,
        scheme = "basic"
)
public class AnimeController {
    private final AnimeService animeService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all animes",
            security = @SecurityRequirement(name = "Basic Authentication"),
            tags = {"anime"})
    public Flux<Anime> listAll() {
        return animeService.findAll();
    }

    @GetMapping(path = "{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            security = @SecurityRequirement(name = "Basic Authentication"),
            tags = {"anime"})
    public Mono<Anime> findById(@PathVariable long id) {
        return animeService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            security = @SecurityRequirement(name = "Basic Authentication"),
            tags = {"anime"})
    public Mono<Anime> save(@Valid @RequestBody Anime anime) {
        return animeService.save(anime);
    }

    @PostMapping("batch")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            security = @SecurityRequirement(name = "Basic Authentication"),
            tags = {"anime"})
    public Flux<Anime> batchSave(@RequestBody List<Anime> animeList) {
        return   animeService.saveAll(animeList);
    }

    @PutMapping(path = "{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            security = @SecurityRequirement(name = "Basic Authentication"),
            tags = {"anime"})
    public Mono<Void> update(@PathVariable long id, @Valid @RequestBody Anime anime) {
        return animeService.update(anime.withId(id));
    }

    @DeleteMapping(path = "{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            security = @SecurityRequirement(name = "Basic Authentication"),
            tags = {"anime"})
    public Mono<Void> delete(@PathVariable long id) {
        return animeService.delete(id);
    }
}
