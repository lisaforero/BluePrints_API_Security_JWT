package co.edu.eci.blueprints.realtime;

import co.edu.eci.blueprints.model.Point;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DrawPointMessage(
        @NotBlank String author,
        @NotBlank String name,
        @NotNull @Valid Point point
) {
}
