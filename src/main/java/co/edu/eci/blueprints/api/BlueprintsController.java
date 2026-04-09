package co.edu.eci.blueprints.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import co.edu.eci.blueprints.model.Blueprint;
import co.edu.eci.blueprints.model.Point;
import co.edu.eci.blueprints.persistence.BlueprintNotFoundException;
import co.edu.eci.blueprints.persistence.BlueprintPersistenceException;
import co.edu.eci.blueprints.services.BlueprintsServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v2/blueprints")
@Tag(name = "Blueprints", description = "Business endpoints for blueprint management")
public class BlueprintsController {

    private final BlueprintsServices services;

    public BlueprintsController(BlueprintsServices services) { 
        this.services = services; 
    }

    @Operation(summary = "Get all blueprints")
    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_blueprints.read')")
    public ResponseEntity<ApiResponseLab<Set<Blueprint>>> getAll() {
        Set<Blueprint> data = services.getAllBlueprints();
        return ResponseEntity.ok(new ApiResponseLab<>(200, "execute ok", data));
    }

    @Operation(summary = "Get all blueprints by author with total points")
    @GetMapping(params = "author")
    @PreAuthorize("hasAuthority('SCOPE_blueprints.read')")
    public ResponseEntity<ApiResponseLab<?>> byAuthorQuery(@RequestParam("author") String author) {
        try {
            var data = services.getBlueprintsByAuthor(author).stream()
                    .map(bp -> new BlueprintSummaryResponse(bp.getAuthor(), bp.getName(), bp.getPoints().size()))
                    .toList();
            return ResponseEntity.ok(new ApiResponseLab<>(200, "execute ok", data));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseLab<>(404, e.getMessage(), null));
        }
    }

    @Operation(summary = "Get all blueprints by author")
    @GetMapping("/{author}")
    @PreAuthorize("hasAuthority('SCOPE_blueprints.read')")
    public ResponseEntity<ApiResponseLab<?>> byAuthor(@PathVariable String author) {
        try {
            var data = services.getBlueprintsByAuthor(author);
            return ResponseEntity.ok(new ApiResponseLab<>(200, "execute ok", data));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseLab<>(404, e.getMessage(), null));
        }
    }

    @Operation(summary = "Get blueprint by author and name")
    @GetMapping("/{author}/{bpname}")
    @PreAuthorize("hasAuthority('SCOPE_blueprints.read')")
    public ResponseEntity<ApiResponseLab<?>> byAuthorAndName(@PathVariable String author, @PathVariable String bpname) {
        try {
            var data = services.getBlueprint(author, bpname); 
            return ResponseEntity.ok(new ApiResponseLab<>(200, "execute ok", data));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseLab<>(404, e.getMessage(), null));
        }
    }

    @Operation(summary = "Create a new blueprint")
    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_blueprints.write')")
    public ResponseEntity<?> add(@Valid @RequestBody NewBlueprintRequest req) {
        try {
            Blueprint bp = new Blueprint(req.author(), req.name(), req.points());
            services.addNewBlueprint(bp);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponseLab<>(201, "created", bp));
        } catch (BlueprintPersistenceException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponseLab<>(400, e.getMessage(), null));
        }
    }

    @Operation(summary = "Add a point to an existing blueprint")
    @PutMapping("/{author}/{bpname}/points")
    @PreAuthorize("hasAuthority('SCOPE_blueprints.write')")
    public ResponseEntity<?> addPoint(@PathVariable String author, @PathVariable String bpname,
                                      @RequestBody Point p) {
        try {
            services.addPoint(author, bpname, p.x(), p.y());
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(new ApiResponseLab<>(202, "point added", null));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseLab<>(404, e.getMessage(), null));
        }
    }

    @Operation(summary = "Update an existing blueprint")
    @PutMapping("/{author}/{bpname}")
    @PreAuthorize("hasAuthority('SCOPE_blueprints.write')")
    public ResponseEntity<?> update(@PathVariable String author,
                                    @PathVariable String bpname,
                                    @Valid @RequestBody UpdateBlueprintRequest req) {
        try {
            Blueprint updated = new Blueprint(author, bpname, req.points());
            services.updateBlueprint(updated);
            return ResponseEntity.ok(new ApiResponseLab<>(200, "updated", updated));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseLab<>(404, e.getMessage(), null));
        }
    }

    @Operation(summary = "Delete an existing blueprint")
    @DeleteMapping("/{author}/{bpname}")
    @PreAuthorize("hasAuthority('SCOPE_blueprints.write')")
    public ResponseEntity<?> delete(@PathVariable String author, @PathVariable String bpname) {
        try {
            services.deleteBlueprint(author, bpname);
            return ResponseEntity.noContent().build();
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseLab<>(404, e.getMessage(), null));
        }
    }
    
    public record NewBlueprintRequest(
            @NotBlank String author,
            @NotBlank String name,
            @Valid java.util.List<Point> points
    ) { }

        public record UpdateBlueprintRequest(
            @Valid List<Point> points
        ) { }

        public record BlueprintSummaryResponse(
            String author,
            String name,
            int totalPoints
        ) { }
}
