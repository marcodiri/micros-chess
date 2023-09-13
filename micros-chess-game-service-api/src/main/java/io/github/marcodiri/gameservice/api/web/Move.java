package io.github.marcodiri.gameservice.api.web;

import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Move {

    private String from;
    private String to;

    public Move() {
    }

    public Move(final String from, final String to) {
        this.from = from;
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(final String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(final String to) {
        this.to = to;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Move request = (Move) o;
        return Objects.equals(from, request.from)
                && Objects.equals(to, request.to);
    }

}
