package training.salonzied.error;

import lombok.Getter;

@Getter
public class ConflictException extends DomainException {
  public ConflictException(String message) {
    super("CONFLICT", message);
  }
}
