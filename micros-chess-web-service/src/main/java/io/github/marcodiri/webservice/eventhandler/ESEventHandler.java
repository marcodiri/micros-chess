package io.github.marcodiri.webservice.eventhandler;

import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.RecordedEvent;
import com.eventstore.dbclient.ResolvedEvent;
import com.eventstore.dbclient.SubscribeToStreamOptions;
import com.eventstore.dbclient.Subscription;
import com.eventstore.dbclient.SubscriptionListener;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.marcodiri.gameservice.api.event.GameCreated;
import io.github.marcodiri.gameservice.api.event.GameEventType;
import io.github.marcodiri.gameservice.api.event.MovePlayed;
import io.github.marcodiri.lobbyservice.api.event.GameProposalAccepted;
import io.github.marcodiri.lobbyservice.api.event.GameProposalCreated;
import io.github.marcodiri.lobbyservice.api.event.GameProposalEventType;
import io.github.marcodiri.webservice.web.WebController;

@Component
public class ESEventHandler implements AutoCloseable {

    private static final Logger LOGGER = LogManager.getLogger(ESEventHandler.class);

    private final EventStoreDBClient clientES;

    private WebController controller;

    private abstract class ESListener extends SubscriptionListener {

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

    @Autowired
    public ESEventHandler(final EventStoreDBClient clientES, WebController controller) {
        this.clientES = clientES;
        this.controller = controller;

        SubscribeToStreamOptions options = SubscribeToStreamOptions.get()
                .fromStart()
                .resolveLinkTos();

        clientES.subscribeToStream(
                "$et-" + GameProposalEventType.ACCEPTED.toString(),
                new ESListener() {
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

                            controller.sendCreateGameRequest(gameProposalAcceptedEvent);
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage());
                        }
                    }
                },
                options);

        clientES.subscribeToStream(
                "$et-" + GameProposalEventType.CREATED.toString(),
                new ESListener() {
                    @Override
                    public void onEvent(Subscription subscription, ResolvedEvent event) {
                        super.onEvent(subscription, event);
                        try {
                            RecordedEvent originalEvent = event.getEvent();
                            GameProposalCreated gameProposalCreatedEvent = new ObjectMapper().readValue(
                                    originalEvent.getEventData(),
                                    GameProposalCreated.class);

                            controller.notifyClients(gameProposalCreatedEvent);
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage());
                        }
                    }
                },
                options);

        clientES.subscribeToStream(
                "$et-" + GameEventType.CREATED.toString(),
                new ESListener() {
                    @Override
                    public void onEvent(Subscription subscription, ResolvedEvent event) {
                        super.onEvent(subscription, event);
                        try {
                            RecordedEvent originalEvent = event.getEvent();
                            GameCreated gameCreatedEvent = new ObjectMapper().readValue(
                                    originalEvent.getEventData(),
                                    GameCreated.class);

                            controller.notifyClients(gameCreatedEvent);
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage());
                        }
                    }
                },
                options);

        clientES.subscribeToStream(
                "$et-" + GameEventType.MOVE.toString(),
                new ESListener() {
                    @Override
                    public void onEvent(Subscription subscription, ResolvedEvent event) {
                        super.onEvent(subscription, event);
                        try {
                            RecordedEvent originalEvent = event.getEvent();
                            MovePlayed movePlayedEvent = new ObjectMapper().readValue(
                                    originalEvent.getEventData(),
                                    MovePlayed.class);

                            controller.notifyClients(movePlayedEvent);
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage());
                        }
                    }
                },
                options);
    }

    @Override
    public void close() throws ExecutionException, InterruptedException {
        clientES.shutdown();
    }

    @Override
    protected void finalize() throws Throwable {
        if (!clientES.isShutdown()) {
            LOGGER.warn("Client is not shut down");
        }
    }

}
