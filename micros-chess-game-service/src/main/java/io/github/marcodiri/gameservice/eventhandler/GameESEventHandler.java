package io.github.marcodiri.gameservice.eventhandler;

import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.RecordedEvent;
import com.eventstore.dbclient.ResolvedEvent;
import com.eventstore.dbclient.SubscribeToStreamOptions;
import com.eventstore.dbclient.Subscription;
import com.eventstore.dbclient.SubscriptionListener;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.marcodiri.gameservice.domain.GameService;
import io.github.marcodiri.lobbyservice.api.event.GameProposalAccepted;
import io.github.marcodiri.lobbyservice.api.event.GameProposalEventType;

public class GameESEventHandler implements AutoCloseable {

    private static final Logger LOGGER = LogManager.getLogger(GameESEventHandler.class);

    private EventStoreDBClient client;
    // Injected singleton
    private GameService gameService;

    private abstract class GameESListener extends SubscriptionListener {

        @Override
        public void onEvent(Subscription subscription, ResolvedEvent event) {
            LOGGER.info("Received event"
                    + event.getOriginalEvent().getRevision()
                    + "@" + event.getOriginalEvent().getStreamId());
        }

        @Override
        public void onError(Subscription subscription, Throwable throwable) {
            LOGGER.info("Subscription was dropped due to " + throwable.getMessage());
            // TODO: reconnect if client not shutdown
        }

        @Override
        public void onCancelled(Subscription subscription) {
            LOGGER.info("Subscription is cancelled");
        }

    };

    public GameESEventHandler(EventStoreDBClient client, GameService gameService) {
        this.client = client;
        this.gameService = gameService;

        SubscribeToStreamOptions options = SubscribeToStreamOptions.get()
                .fromStart()
                .resolveLinkTos();

        client.subscribeToStream(
                "$et-" + GameProposalEventType.ACCEPTED.toString(),
                new GameESListener() {
                    @Override
                    public void onEvent(Subscription subscription, ResolvedEvent event) {
                        super.onEvent(subscription, event);
                        try {
                            // $et-* are streams of links
                            // use getEvent() to get the actual event instead of the link event
                            RecordedEvent originalEvent = event.getEvent();
                            GameProposalAccepted gameProposalAcceptedEvent = new ObjectMapper().readValue(
                                    originalEvent.getEventData(),
                                    GameProposalAccepted.class);
                            gameService.createGame(
                                    gameProposalAcceptedEvent.getCreatorId(),
                                    gameProposalAcceptedEvent.getAcceptorId());
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage());
                        }
                    }
                },
                options);
    }

    @Override
    public void close() throws ExecutionException, InterruptedException {
        client.shutdown();
    }

    @Override
    protected void finalize() throws Throwable {
        if (!client.isShutdown()) {
            LOGGER.warn("Client is not shut down");
        }
    }

}
