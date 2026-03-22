package tn.recruti.recruti_backend.Exception;

public class CandidatureDejaExisteException extends RuntimeException {
    public CandidatureDejaExisteException(String s) {
        super(s);
    }
}