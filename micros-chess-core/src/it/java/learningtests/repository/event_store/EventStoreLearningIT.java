package learningtests.repository.event_store;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.eventstore.dbclient.CreatePersistentSubscriptionToStreamOptions;
import com.eventstore.dbclient.EventData;
import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.EventStoreDBClientSettings;
import com.eventstore.dbclient.EventStoreDBConnectionString;
import com.eventstore.dbclient.EventStoreDBPersistentSubscriptionsClient;
import com.eventstore.dbclient.NackAction;
import com.eventstore.dbclient.PersistentSubscription;
import com.eventstore.dbclient.PersistentSubscriptionListener;
import com.eventstore.dbclient.ReadResult;
import com.eventstore.dbclient.ReadStreamOptions;
import com.eventstore.dbclient.RecordedEvent;
import com.eventstore.dbclient.ResolvedEvent;
import com.eventstore.dbclient.SubscribeToStreamOptions;
import com.eventstore.dbclient.Subscription;
import com.eventstore.dbclient.SubscriptionListener;
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
    static void setupClient() {
        setts = EventStoreDBConnectionString.parseOrThrow("esdb://localhost:2113?tls=false");
        client = EventStoreDBClient.create(setts);
    }

    @AfterAll
    static void teardownClient() throws ExecutionException, InterruptedException {
        client.shutdown();
    }

    @Nested
    class ESClient {

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

    @Nested
    class PersistentSubscriptionClient {

        private static EventStoreDBClientSettings setts;
        private static EventStoreDBPersistentSubscriptionsClient subClient;

        @BeforeAll
        static void setup() {
            setts = EventStoreDBConnectionString.parseOrThrow("esdb://localhost:2113?tls=false");
            subClient = EventStoreDBPersistentSubscriptionsClient.create(setts);

            /*
             * The group can also be created manually going to
             * http://localhost:2113 > Persistent Subscriptions
             * and create a New Subscription with
             * Group: subscription-group
             * Stream: $et-account-created
             * Resolve Link Tos: checked
             */
            subClient.createToStream(
                    "$et-account-created",
                    "subscription-group",
                    CreatePersistentSubscriptionToStreamOptions.get()
                            .fromStart()
                            .resolveLinkTos());
        }

        @Test
        void persistentSubscription() throws InterruptedException, ExecutionException {
            PersistentSubscriptionListener listener = new PersistentSubscriptionListener() {
                @Override
                public void onEvent(PersistentSubscription subscription, int retryCount, ResolvedEvent event) {
                    try {
                        LOGGER.info("Received event"
                                + event.getOriginalEvent().getRevision()
                                + "@" + event.getOriginalEvent().getStreamId());
                        subscription.ack(event);
                    } catch (Exception ex) {
                        LOGGER.info(ex.getMessage());
                        subscription.nack(NackAction.Park, ex.getMessage(), event);
                    }
                }

                @Override
                public void onError(PersistentSubscription subscription, Throwable throwable) {
                    LOGGER.info("Subscription was dropped due to " + throwable.getMessage());
                    throw new RuntimeException();
                }

                @Override
                public void onCancelled(PersistentSubscription subscription) {
                    LOGGER.info("Subscription is cancelled");
                    throw new RuntimeException();
                }
            };

            subClient.subscribeToStream(
                    "$et-account-created",
                    "subscription-group",
                    listener);

            // Write something or it won't work
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
        }

    }

    @Nested
    class CatchUpSubscriptionClient {

        @Test
        void catchUpSubscription() throws InterruptedException, ExecutionException {
            SubscriptionListener listener = new SubscriptionListener() {

                @Override
                public void onEvent(Subscription subscription, ResolvedEvent event) {
                    LOGGER.info("Received event"
                            + event.getOriginalEvent().getRevision()
                            + "@" + event.getOriginalEvent().getStreamId());
                }

                @Override
                public void onError(Subscription subscription, Throwable throwable) {
                    LOGGER.info("Subscription was dropped due to " + throwable.getMessage());
                    throw new RuntimeException();
                }

                @Override
                public void onCancelled(Subscription subscription) {
                    LOGGER.info("Subscription is cancelled");
                    throw new RuntimeException();
                }

            };
            client.subscribeToStream(
                    "$et-account-created",
                    listener,
                    SubscribeToStreamOptions.get()
                            .fromStart()
                            .resolveLinkTos());

            // Write something or it won't work
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
        }

    }

}
