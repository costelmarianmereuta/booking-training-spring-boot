package training.salonzied.error;

import lombok.Getter;

@Getter
public class EntityNotFoundException extends DomainException {

  private final String entity;
  private final String id;

  public EntityNotFoundException(String entity, String id) {
    super("ENTITY_NOT_FOUND", entity + " not found: " + id);
    this.entity = entity;
    this.id = id;
  }
}
