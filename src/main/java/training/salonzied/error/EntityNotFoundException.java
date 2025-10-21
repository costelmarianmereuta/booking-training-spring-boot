package training.salonzied.error;

import lombok.Getter;

@Getter
public class EntityNotFoundException extends DomainException {

        private final String entity;
        private final String id;

        public EntityNotFoundException(String entity, String id) {
            super(entity + " not found: " + id, "ENTITY_NOT_FOUND");
            this.entity = entity;
            this.id = id;
        }
    }

