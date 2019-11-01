package cz.podlesh.demo.calculator.api;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Request for full operation: operator and all arguments.
 */
public class FullOperation {

    /**
     * Operand; must be one of the supported operands.
     */
    @JsonAlias({"op"})
    public String operator;

    @JsonProperty
    @JsonAlias({"args", "arg", "argument"})
    @JsonSerialize(contentUsing = ToStringSerializer.class)
    public BigDecimal[] arguments;

    /**
     * Operator, as defined by the user.
     */
    public String getOperator() {
        return operator;
    }

    /**
     * Get all the arguments as a list. <code>null</code> is replaced by empty list.
     */
    @JsonIgnore
    @Nonnull
    public List<BigDecimal> getArguments() {
        if (arguments == null)
            return Collections.emptyList();
        return Arrays.asList(arguments);
    }

    public FullOperation() {
    }

    public FullOperation(String operator, BigDecimal[] arguments) {
        this.operator = operator;
        this.arguments = arguments;
    }

    public FullOperation(FullOperation copyFrom) {
        this(copyFrom.operator, copyFrom.arguments);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + operator + "/" + Arrays.toString(arguments);
    }
}
