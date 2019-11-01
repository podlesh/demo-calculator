package cz.podlesh.demo.calculator.api;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

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
     * Note that is is <b>always</b> serialized as string; JSON numbers are very, very limited.
     */
    @JsonSerialize(using = ToStringSerializer.class)
    public BigDecimal result;

    public FullOperationResult() {
    }

    public FullOperationResult(FullOperation request, String error, BigDecimal result) {
        super(request);
        this.error = error;
        this.result = result;
    }

    @Override
    public String toString() {
        return super.toString() + " -> " + result + (error == null ? "" : " / " + error);
    }
}
