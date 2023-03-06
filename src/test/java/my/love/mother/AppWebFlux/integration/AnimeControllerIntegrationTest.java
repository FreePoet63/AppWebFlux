package my.love.mother.AppWebFlux.integration;

import my.love.mother.AppWebFlux.domain.Anime;
import my.love.mother.AppWebFlux.repository.AnimeRepository;
import my.love.mother.AppWebFlux.util.WebTestClientUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static my.love.mother.AppWebFlux.util.AnimeCreator.createAnimeToBeSaved;
import static my.love.mother.AppWebFlux.util.AnimeCreator.createValidAnime;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class AnimeControllerIntegrationTest {
    @Autowired
    private WebTestClientUtil webTestClientUtil;

    @MockBean
    private AnimeRepository repository;

    private WebTestClient testUser;
    private WebTestClient testAdmin;
    private WebTestClient testInvalid;

    private final Anime anime = createValidAnime();

    @BeforeAll
    public static void BlockHoundSetUp() {
        BlockHound.install();
    }

    @BeforeEach
    public void setUp() {
        testUser = webTestClientUtil.authClient("user","mama");
        testAdmin = webTestClientUtil.authClient("Nata","mama");
        testInvalid = webTestClientUtil.authClient("x","x");

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
    @DisplayName("listAll returns unauthorized when user is not authenticated")
    public void listAll_ReturnsUnauthorized_WhenUserIsNotAuthenticated() {
        testInvalid
                .get()
                .uri("/anime")
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    @DisplayName("listAll returns forbidden when user is successfully authenticated and does not have role ADMIN")
    public void listAll_ReturnForbidden_WhenUserDoesNotHaveRoleAdmin() {
        testUser
                .get()
                .uri("/anime")
                .exchange()
                .expectStatus()
                .isForbidden();
    }


    @Test
    @DisplayName("listAll returns a flux of anime is successfully authenticated and has role ADMIN")
    public void findAll_ReturnsFluxOfAnime_WhenSuccessful() {
        testAdmin
                .get()
                .uri("/anime")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.[0].id")
                .isEqualTo(anime.getId())
                .jsonPath("$.[0].name")
                .isEqualTo(anime.getName());
    }

    @Test
    @DisplayName("listAll returns a flux of anime when user is successfully authenticated and has role ADMIN")
    public void findAll_Result2_ReturnsFluxOfAnime_WhenSuccessful() {
        testAdmin
                .get()
                .uri("/anime")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Anime.class)
                .hasSize(1)
                .contains(anime);
    }

    @Test
    @DisplayName("findById returns a mono with of anime when it exists and user is successfully authenticated and has role USER")
    public void findById_ReturnMonoAnime_WhenSuccessful() {
        testUser
                .get()
                .uri("/anime/{id}", 1)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Anime.class)
                .isEqualTo(anime);
    }

    @Test
    @DisplayName("findById returns mono error when anime does not exists  and user is successfully authenticated and has role USER")
    public void findById_ReturnMonoError_WhenEmptyMonoReturned() {
        BDDMockito.when(repository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.empty());
        testUser
                .get()
                .uri("/anime/{id}", 1)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody()
                .jsonPath("$.status")
                .isEqualTo(404)
                .jsonPath("$.developerMessage")
                .isEqualTo("A ResponseStatusException Happened");
    }

    @Test
    @DisplayName("save create an anime when successful and user is successfully authenticated and has role ADMIN")
    public void save_CreateAnime_WhenSuccessful() {
        Anime animeToBeSaved = createAnimeToBeSaved();
        testAdmin
                .post()
                .uri("/anime")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(animeToBeSaved))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Anime.class)
                .isEqualTo(anime);
    }

    @Test
    @DisplayName("saveBatch creates a list of anime when successful and user is successfully authenticated and has role ADMIN")
    public void saveBatch_CreateListOfAnime_WhenSuccessful() {
        Anime animeToBeSaved = createAnimeToBeSaved();
        testAdmin
                .post()
                .uri("/anime/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(Arrays.asList(animeToBeSaved, animeToBeSaved)))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBodyList(Anime.class)
                .hasSize(2)
                .contains(anime);
    }

    @Test
    @DisplayName("saveBatch returns Mono error when one of the objects in the list contains empty or null " +
            "name and user is successfully authenticated and has role ADMIN")
    public void saveBatch_ReturnsMonoError_WhenContainsInvalidName() {
        Anime animeToBeSaved = createAnimeToBeSaved();
        BDDMockito.when(repository
                        .saveAll(ArgumentMatchers.anyIterable()))
                        .thenReturn(Flux.just(anime, anime.withName("")));
        testAdmin
                .post()
                .uri("/anime/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(Arrays.asList(animeToBeSaved, animeToBeSaved)))
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .jsonPath("$.status")
                .isEqualTo(400);
    }

    @Test
    @DisplayName("save returns mono error with bad request when name is empty and " +
            "user is successfully authenticated and has role ADMIN")
    public void save_ReturnError_WhenSuccessful() {
        Anime animeToBeSaved = createAnimeToBeSaved().withName("");
        testAdmin
                .post()
                .uri("/anime")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(animeToBeSaved))
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .jsonPath("$.status")
                .isEqualTo(400);
    }

    @Test
    @DisplayName("delete removes the anime when successful and user is successfully authenticated and has role ADMIN")
    public void delete_RemoveAnime_WhenSuccessful() {
        testAdmin
                .delete()
                .uri("/anime/{id}", 1)
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    @DisplayName("delete returns Mono error when anime does not exist and " +
            "user is successfully authenticated and has role ADMIN")
    public void delete_ReturnMonoError_WhenEmptyMonoIsReturned() {
        BDDMockito.when(repository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.empty());
        testAdmin
                .delete()
                .uri("/anime/{id}", 1)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody()
                .jsonPath("$.status")
                .isEqualTo(404)
                .jsonPath("$.developerMessage")
                .isEqualTo("A ResponseStatusException Happened");
    }

    @Test
    @DisplayName("update save updated anime and returns empty mono when successful and " +
            "user is successfully authenticated and has role ADMIN")
    public void update_SaveUpdatedAnime_WhenSuccessful() {
        testAdmin
                .put()
                .uri("/anime/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(anime))
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    @DisplayName("update returns Mono error when anime does not exist and " +
            "user is successfully authenticated and has role ADMIN")
    public void update_ReturnedMonoError_WhenEmptyMonoIsReturned() {
        BDDMockito.when(repository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.empty());
        testAdmin
                .put()
                .uri("/anime/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(anime))
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody()
                .jsonPath("$.status")
                .isEqualTo(404)
                .jsonPath("$.developerMessage")
                .isEqualTo("A ResponseStatusException Happened");
    }
}