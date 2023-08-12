package learningtests.repository.event_store;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.eventstore.dbclient.EventData;
import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.EventStoreDBClientSettings;
import com.eventstore.dbclient.EventStoreDBConnectionString;
import com.eventstore.dbclient.ReadResult;
import com.eventstore.dbclient.ReadStreamOptions;
import com.eventstore.dbclient.RecordedEvent;
import com.eventstore.dbclient.ResolvedEvent;
import com.eventstore.dbclient.WriteResult;
import com.fasterxml.jackson.databind.ObjectMapper;

class EventStoreLearningIT {

    private static final Logger LOGGER = LogManager.getLogger(EventStoreLearningIT.class);

    private static class AccountCreated {
        private UUID id;
        private String login;

        public UUID getId() {
            return id;
        }

        public String getLogin() {
            return login;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public void setLogin(String login) {
            this.login = login;
        }
    }

    private static EventStoreDBClientSettings setts;
    private static EventStoreDBClient client;

    @BeforeAll
    static void setup() {
        setts = EventStoreDBConnectionString.parseOrThrow("esdb://localhost:2113?tls=false");
        client = EventStoreDBClient.create(setts);
    }

    @Test
    void writeToStreamThenRead() throws InterruptedException, ExecutionException, IOException {
        AccountCreated createdEvent = new AccountCreated();

        createdEvent.setId(UUID.randomUUID());
        createdEvent.setLogin("ouros");

        EventData event = EventData
                .builderAsJson("account-created", createdEvent)
                .build();

        WriteResult writeResult = client
                .appendToStream("accounts", event)
                .get();
        LOGGER.info(writeResult);

        ReadStreamOptions readLastEvent = ReadStreamOptions.get()
                .backwards()
                .fromEnd();

        ReadResult result = client.readStream("accounts", readLastEvent)
                .get();

        ResolvedEvent resolvedEvent = result
                .getEvents()
                .get(0);

        RecordedEvent recordedEvent = resolvedEvent.getOriginalEvent();

        AccountCreated writtenEvent = new ObjectMapper().readValue(recordedEvent.getEventData(),
                AccountCreated.class);

        assertThat(writtenEvent.getId()).isEqualTo(createdEvent.getId());
        assertThat(writtenEvent.getLogin()).isEqualTo(createdEvent.getLogin());
    }

}
