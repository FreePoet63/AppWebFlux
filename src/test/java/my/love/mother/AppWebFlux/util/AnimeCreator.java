package my.love.mother.AppWebFlux.util;

import my.love.mother.AppWebFlux.domain.Anime;

public class AnimeCreator {
    public static Anime createAnimeToBeSaved() {
        return Anime.builder()
                .name("Fantastic Country")
                .build();
    }

    public static Anime createValidAnime() {
        return Anime.builder()
                .id(1L)
                .name("Fantastic Country")
                .build();
    }

    public static Anime createValidUpdatedAnime() {
        return Anime.builder()
                .id(1L)
                .name("Fantastic Country Belarus!!!")
                .build();
    }
}
