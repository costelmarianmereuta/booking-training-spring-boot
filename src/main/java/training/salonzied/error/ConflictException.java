package training.salonzied.error;

import lombok.Getter;

@Getter
public class ConflictException extends DomainException {
  protected ConflictException(String message) {
    super(message, "CONFLICT");
  }
}
