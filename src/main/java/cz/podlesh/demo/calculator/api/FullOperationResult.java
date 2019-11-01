package cz.podlesh.demo.calculator.api;

import java.math.BigDecimal;

/**
 * Result of full operation: contains whole request, too.
 */
public class FullOperationResult extends FullOperation {

    /**
     * Error: present if the result is an arithmetic error (for example, division by zero).
     */
    public String error;
    /**
     * Result: only when the result is not an error.
     */
    public BigDecimal result;

    public FullOperationResult() {
    }

    public FullOperationResult(FullOperation request, String error, BigDecimal result) {
        super(request);
        this.error = error;
        this.result = result;
    }

}
