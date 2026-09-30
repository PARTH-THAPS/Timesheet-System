package tss.logic;

import jakarta.ejb.ApplicationException;

/**
 * Exception thrown when contract termination is blocked due to unresolved or in-progress timesheets.
 * Triggers a transaction rollback.
 */
@ApplicationException(rollback = true)
public class TerminationBlockedException extends RuntimeException {
    /**
     * Constructs a new exception with the specified detail message.
     * @param message The detail message explaining the reason for the exception.
     */
    public TerminationBlockedException(String message) {
        super(message);
    }
}