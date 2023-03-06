package my.love.mother.AppWebFlux.controller;

import my.love.mother.AppWebFlux.domain.Anime;
import my.love.mother.AppWebFlux.service.AnimeService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
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
class AnimeControllerTest {
    @InjectMocks
    private AnimeController controller;

    @Mock
    private AnimeService animeService;

    private final Anime anime = createValidAnime();

    @BeforeAll
    public static void BlockHoundSetUp() {
        BlockHound.install();
    }

    @BeforeEach
    public void setUp() {
        BDDMockito.when(animeService.findAll())
                .thenReturn(Flux.just(anime));

        BDDMockito.when(animeService.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.just(anime));

        BDDMockito.when(animeService.save(createAnimeToBeSaved()))
                .thenReturn(Mono.just(anime));

        BDDMockito.when(animeService
                        .saveAll(Arrays.asList(createAnimeToBeSaved(), createAnimeToBeSaved())))
                        .thenReturn(Flux.just(anime, anime));

        BDDMockito.when(animeService.delete(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.empty());

        BDDMockito.when(animeService.update(createValidAnime()))
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
    @DisplayName("listAll returns a flux of anime")
    public void findAll_ReturnsFluxOfAnime_WhenSuccessful() {
        StepVerifier.create(controller.listAll())
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("findById returns a mono with of anime when it exists")
    public void findById_ReturnMonoAnime_WhenSuccessful() {
        StepVerifier.create(controller.findById(1L))
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("save create an anime when successful")
    public void save_CreateAnime_WhenSuccessful() {
        Anime animeToBeSaved = createAnimeToBeSaved();
        StepVerifier.create(controller.save(animeToBeSaved))
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("saveBatch create a list of anime when successful")
    public void saveBatch_CreateListOfAnime_WhenSuccessful() {
        Anime animeToBeSaved = createAnimeToBeSaved();
        StepVerifier.create(controller.batchSave(Arrays.asList(animeToBeSaved, animeToBeSaved)))
                .expectSubscription()
                .expectNext(anime, anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("delete removes the anime when successful")
    public void delete_RemoveAnime_WhenSuccessful() {
        StepVerifier.create(controller.delete(1L))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    @DisplayName("update save updated anime and returns empty mono when successful")
    public void update_SaveUpdatedAnime_WhenSuccessful() {
        StepVerifier.create(controller.update(1L, createValidAnime()))
                .expectSubscription()
                .verifyComplete();
    }
}