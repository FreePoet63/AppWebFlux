package my.love.mother.AppWebFlux.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@With
@Table("anime")
public class Anime {
    @Id
    private Long id;
    @NotNull
    @NotEmpty(message = "название этого аниме не может быть пустым")
    private String name;
}
