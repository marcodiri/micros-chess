package io.github.marcodiri.gameservice.api.event;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class GameEventTypeTest {

    private final String TYPE_CREATED = "game-created";
    private final String TYPE_MOVE = "game-move-played";

    @Test
    void fromStringReturnsCorrectEnum() {
        assertThat(GameEventType.fromString(TYPE_CREATED)).isEqualTo(GameEventType.CREATED);
        assertThat(GameEventType.fromString(TYPE_MOVE)).isEqualTo(GameEventType.MOVE);
    }

    @Test
    void toStringReturnsCorrectType() {
        assertThat(GameEventType.CREATED.toString()).isEqualTo(TYPE_CREATED);
        assertThat(GameEventType.MOVE.toString()).isEqualTo(TYPE_MOVE);
    }

    @Test
    void getEventClassReturnsEventClass() {
        assertThat(GameEventType.CREATED.getEventClass()).isSameAs(GameCreated.class);
        assertThat(GameEventType.MOVE.getEventClass()).isSameAs(MovePlayed.class);
    }

}
