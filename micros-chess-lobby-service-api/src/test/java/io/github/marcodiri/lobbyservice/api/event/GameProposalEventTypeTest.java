package io.github.marcodiri.lobbyservice.api.event;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class GameProposalEventTypeTest {

    private final String TYPE_CREATED = "game-proposal-created";
    private final String TYPE_CANCELED = "game-proposal-canceled";
    private final String TYPE_ACCEPTED = "game-proposal-accepted";

    @Test
    void fromStringReturnsCorrectEnum() {
        assertThat(GameProposalEventType.fromString(TYPE_CREATED)).isEqualTo(GameProposalEventType.CREATED);
        assertThat(GameProposalEventType.fromString(TYPE_CANCELED)).isEqualTo(GameProposalEventType.CANCELED);
        assertThat(GameProposalEventType.fromString(TYPE_ACCEPTED)).isEqualTo(GameProposalEventType.ACCEPTED);
    }

    @Test
    void toStringReturnsCorrectType() {
        assertThat(GameProposalEventType.CREATED.toString()).isEqualTo(TYPE_CREATED);
        assertThat(GameProposalEventType.CANCELED.toString()).isEqualTo(TYPE_CANCELED);
        assertThat(GameProposalEventType.ACCEPTED.toString()).isEqualTo(TYPE_ACCEPTED);
    }

    @Test
    void getEventClassReturnsEventClass() {
        assertThat(GameProposalEventType.CREATED.getEventClass()).isSameAs(GameProposalCreated.class);
        assertThat(GameProposalEventType.CANCELED.getEventClass()).isSameAs(GameProposalCanceled.class);
        assertThat(GameProposalEventType.ACCEPTED.getEventClass()).isSameAs(GameProposalAccepted.class);
    }

}
