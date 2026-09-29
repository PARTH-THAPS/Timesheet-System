package tss.logic;

import jakarta.ejb.ApplicationException;


@ApplicationException(rollback = true)
public class TerminationBlockedException extends RuntimeException {
    public TerminationBlockedException(String message) {
        super(message);
    }
}