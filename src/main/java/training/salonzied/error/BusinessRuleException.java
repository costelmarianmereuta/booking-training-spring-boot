package training.salonzied.error;

public class BusinessRuleException extends DomainException {
  protected BusinessRuleException(String message) {
    super(message, "BUSINESS_RULE_VIOLATION");
  }
}
