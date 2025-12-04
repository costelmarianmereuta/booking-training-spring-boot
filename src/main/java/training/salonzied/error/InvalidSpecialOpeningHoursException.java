package training.salonzied.error;

public class InvalidSpecialOpeningHoursException extends BusinessRuleException {

    public InvalidSpecialOpeningHoursException(String message) {
        super(message + " (invalid special opening hours)");
    }
}
