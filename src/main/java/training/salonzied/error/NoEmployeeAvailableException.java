package training.salonzied.error;

public class NoEmployeeAvailableException extends BusinessRuleException {
    public NoEmployeeAvailableException(String message) {
        super(message);
    }
}
