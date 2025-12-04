package training.salonzied.error;

public class BusinessRuleException extends DomainException {
  protected BusinessRuleException(String message) {
    super("BUSINESS_RULE_VIOLATION", message);
  }
}
