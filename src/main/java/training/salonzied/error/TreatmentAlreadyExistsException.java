package training.salonzied.error;

public class TreatmentAlreadyExistsException extends ConflictException{
    public TreatmentAlreadyExistsException(String treatmentName) {
        super("Treatment with name '" + treatmentName + "' already exists.");
    }
}
