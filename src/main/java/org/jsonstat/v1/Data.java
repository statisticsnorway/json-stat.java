package org.jsonstat.v1;


import java.util.Optional;

/**
 * Created with IntelliJ IDEA.
 * User: erlhamna
 * Date: 16.10.13
 * Time: 16:32
 * To change this template use File | Settings | File Templates.
 */
public final class Data {
    private final Object value;
    private final Optional<String> status;

    public Data(Object value, Optional<String> status) {
        this.value = value;
        this.status = status;
    }

    public Object getValue() {
        return value;
    }

    public Optional<String> getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
