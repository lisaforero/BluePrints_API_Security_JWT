package co.edu.eci.blueprints.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import co.edu.eci.blueprints.persistence.entity.BlueprintEntity;

import java.util.List;

public interface BlueprintRepository extends JpaRepository<BlueprintEntity, Long> {

    List<BlueprintEntity> findByAuthor(String author);

    BlueprintEntity findByAuthorAndName(String author, String name);
}