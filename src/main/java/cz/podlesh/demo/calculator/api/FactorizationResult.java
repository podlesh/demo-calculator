package cz.podlesh.demo.calculator.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.util.Arrays;

/**
 * Result of full operation: contains whole request, too.
 */
public class FactorizationResult extends FullOperation {

    /**
     * Error: present if the value is not accepted (too big, probably)
     */
    public String error;
    /**
     * Result: only when the result is not an error.
     * Note that is is <b>always</b> serialized as string; JSON numbers are very, very limited.
     */
    @JsonSerialize(using = ToStringSerializer.class)
    public long[] result;

    @JsonProperty()
    public Boolean isPrime() {
        return result != null && result.length == 1;
    }

    public FactorizationResult() {
    }

    public FactorizationResult(FullOperation copyFrom, String error, long[] result) {
        super(copyFrom);
        this.error = error;
        this.result = result;
    }

    @Override
    public String toString() {
        return super.toString() + " -> " + Arrays.toString(result) + (error == null ? "" : " / " + error);
    }

}
