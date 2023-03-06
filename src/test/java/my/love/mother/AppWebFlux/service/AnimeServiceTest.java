package my.love.mother.AppWebFlux.service;

import my.love.mother.AppWebFlux.domain.Anime;
import my.love.mother.AppWebFlux.repository.AnimeRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static my.love.mother.AppWebFlux.util.AnimeCreator.createAnimeToBeSaved;
import static my.love.mother.AppWebFlux.util.AnimeCreator.createValidAnime;

@ExtendWith(SpringExtension.class)
class AnimeServiceTest {
    @InjectMocks
    private AnimeService animeService;

    @Mock
    private AnimeRepository repository;

    private final Anime anime = createValidAnime();

    @BeforeAll
    public static void BlockHoundSetUp() {
        BlockHound.install();
    }

    @BeforeEach
    public void setUp() {
        BDDMockito.when(repository.findAll())
                .thenReturn(Flux.just(anime));

        BDDMockito.when(repository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.just(anime));

        BDDMockito.when(repository.save(createAnimeToBeSaved()))
                .thenReturn(Mono.just(anime));

        BDDMockito.when(repository
                        .saveAll(Arrays.asList(createAnimeToBeSaved(), createAnimeToBeSaved())))
                        .thenReturn(Flux.just(anime, anime));

        BDDMockito.when(repository.delete(ArgumentMatchers.any(Anime.class)))
                .thenReturn(Mono.empty());

        BDDMockito.when(repository.save(createValidAnime()))
                .thenReturn(Mono.empty());
    }

    @Test
    public void blackHoundWorks() {
        try {
            FutureTask<?> task = new FutureTask<>(() -> {
                Thread.sleep(0);
                return "";
            });
            Schedulers.parallel().schedule(task);
            task.get(10, TimeUnit.SECONDS);
            Assertions.fail("should fail");
        } catch (Exception e) {
            Assertions.assertTrue(e.getCause() instanceof BlockingOperationError);
        }
    }

    @Test
    @DisplayName("findAll returns a flux of anime")
    public void findAll_ReturnsFluxOfAnime_WhenSuccessful() {
        StepVerifier.create(animeService.findAll())
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("findById returns a mono with of anime when it exists")
    public void findById_ReturnMonoAnime_WhenSuccessful() {
        StepVerifier.create(animeService.findById(1L))
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("findById returns mono error when anime does not exists")
    public void findById_ReturnMonoError_WhenEmptyMonoReturned() {
        BDDMockito.when(repository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.empty());
        StepVerifier.create(animeService.findById(1L))
                .expectSubscription()
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    @DisplayName("save create an anime when successful")
    public void save_CreateAnime_WhenSuccessful() {
        Anime animeToBeSaved = createAnimeToBeSaved();
        StepVerifier.create(animeService.save(animeToBeSaved))
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("saveAll create a list of anime when successful")
    public void saveAll_CreateListOfAnime_WhenSuccessful() {
        Anime animeToBeSaved = createAnimeToBeSaved();
        StepVerifier.create(animeService.saveAll(Arrays.asList(animeToBeSaved, animeToBeSaved)))
                .expectSubscription()
                .expectNext(anime, anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("saveAll returns mono error one of the object empty or null name")
    public void saveAll_ReturnMonoError_WhenContainsInvalidName() {
        Anime animeToBeSaved = createAnimeToBeSaved();
        BDDMockito.when(repository
                        .saveAll(ArgumentMatchers.anyIterable()))
                        .thenReturn(Flux.just(anime, anime.withName("")));
        StepVerifier.create(animeService.saveAll(Arrays.asList(animeToBeSaved, animeToBeSaved.withName(""))))
                .expectSubscription()
                .expectNext(anime)
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    @DisplayName("delete removes the anime when successful")
    public void delete_RemoveAnime_WhenSuccessful() {
        StepVerifier.create(animeService.delete(1L))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    @DisplayName("delete returns mono error when anime does not exists")
    public void delete_ReturnMonoError_WhenEmptyMonoIsReturned() {
        BDDMockito.when(repository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.empty());
        StepVerifier.create(animeService.delete(1L))
                .expectSubscription()
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    @DisplayName("update save updated anime and returns empty mono when successful")
    public void update_SaveUpdatedAnime_WhenSuccessful() {
        StepVerifier.create(animeService.update(createValidAnime()))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    @DisplayName("update return mono error when anime does not exists")
    public void update_ReturnedMonoError_WhenEmptyMonoIsReturned() {
        BDDMockito.when(repository.findById(ArgumentMatchers.anyLong()))
                        .thenReturn(Mono.empty());
        StepVerifier.create(animeService.update(createValidAnime()))
                .expectSubscription()
                .expectError(ResponseStatusException.class)
                .verify();
    }
}