package co.edu.eci.blueprints.persistence.entity;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "blueprints")
public class BlueprintEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String author;
    private String name;

    @OneToMany(mappedBy = "blueprint",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<PointEntity> points = new ArrayList<>();

    public BlueprintEntity() {
    }

    public BlueprintEntity(String author, String name) {
        this.author = author;
        this.name = name;
    }

    // Getters y Setters

    public Long getId() { return id; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<PointEntity> getPoints() { return points; }
    public void setPoints(List<PointEntity> points) {
        this.points = points;
    }

    public void addPoint(PointEntity point) {
        points.add(point);
        point.setBlueprint(this);
    }
}