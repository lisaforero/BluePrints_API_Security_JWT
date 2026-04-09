package co.edu.eci.blueprints.persistence;

import co.edu.eci.blueprints.persistence.entity.PointEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import co.edu.eci.blueprints.persistence.entity.BlueprintEntity;
import co.edu.eci.blueprints.persistence.repository.BlueprintRepository;
import co.edu.eci.blueprints.model.Blueprint;
import co.edu.eci.blueprints.model.Point;

import java.util.Set;
import java.util.HashSet;

@Component
@Primary
public class PostgresBlueprintPersistence implements BlueprintPersistence {

    @Autowired
    private BlueprintRepository blueprintRepository;

    @Override
    public void saveBlueprint(Blueprint bp) throws BlueprintPersistenceException {

        BlueprintEntity existing =
                blueprintRepository.findByAuthorAndName(bp.getAuthor(), bp.getName());

        if (existing != null) {
            throw new BlueprintPersistenceException("Blueprint already exists");
        }

        BlueprintEntity entity =
                new BlueprintEntity(bp.getAuthor(), bp.getName());

        for (Point p : bp.getPoints()) {
            entity.addPoint(new PointEntity(p.x(), p.y()));
        }

        blueprintRepository.save(entity);
    }

    @Override
    public Blueprint getBlueprint(String author, String name)
            throws BlueprintNotFoundException {

        BlueprintEntity entity =
                blueprintRepository.findByAuthorAndName(author, name);

        if (entity == null) {
            throw new BlueprintNotFoundException("Blueprint not found");
        }

        return convertToModel(entity);
    }

    @Override
    public Set<Blueprint> getBlueprintsByAuthor(String author)
            throws BlueprintNotFoundException {

        var entities = blueprintRepository.findByAuthor(author);

        if (entities.isEmpty()) {
            throw new BlueprintNotFoundException("No blueprints for author");
        }

        Set<Blueprint> result = new HashSet<>();

        for (BlueprintEntity entity : entities) {
            result.add(convertToModel(entity));
        }

        return result;
    }

    @Override
    public Set<Blueprint> getAllBlueprints() {

        Set<Blueprint> result = new HashSet<>();

        blueprintRepository.findAll()
                .forEach(entity -> result.add(convertToModel(entity)));

        return result;
    }

    @Override
    public void updateBlueprint(Blueprint bp) throws BlueprintNotFoundException {
        BlueprintEntity entity =
                blueprintRepository.findByAuthorAndName(bp.getAuthor(), bp.getName());

        if (entity == null) {
            throw new BlueprintNotFoundException("Blueprint not found");
        }

        entity.getPoints().clear();
        for (Point p : bp.getPoints()) {
            entity.addPoint(new PointEntity(p.x(), p.y()));
        }

        blueprintRepository.save(entity);
    }

    @Override
    public void deleteBlueprint(String author, String name) throws BlueprintNotFoundException {
        BlueprintEntity entity =
                blueprintRepository.findByAuthorAndName(author, name);

        if (entity == null) {
            throw new BlueprintNotFoundException("Blueprint not found");
        }

        blueprintRepository.delete(entity);
    }

    @Override
    public void addPoint(String author, String name, int x, int y)
            throws BlueprintNotFoundException {

        BlueprintEntity entity =
                blueprintRepository.findByAuthorAndName(author, name);

        if (entity == null) {
            throw new BlueprintNotFoundException("Blueprint not found");
        }

        entity.addPoint(new PointEntity(x, y));

        blueprintRepository.save(entity);
    }

    private Blueprint convertToModel(BlueprintEntity entity) {

        var points = new java.util.ArrayList<Point>();

        entity.getPoints().forEach(p ->
                points.add(new Point(p.getX(), p.getY()))
        );

        return new Blueprint(entity.getAuthor(), entity.getName(), points);
    }
}
