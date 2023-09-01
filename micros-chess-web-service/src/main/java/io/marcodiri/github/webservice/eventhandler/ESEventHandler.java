package io.marcodiri.github.webservice.eventhandler;

import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.RecordedEvent;
import com.eventstore.dbclient.ResolvedEvent;
import com.eventstore.dbclient.SubscribeToStreamOptions;
import com.eventstore.dbclient.Subscription;
import com.eventstore.dbclient.SubscriptionListener;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.marcodiri.gameservice.api.web.CreateGameRequest;
import io.github.marcodiri.gameservice.api.web.CreateGameResponse;
import io.github.marcodiri.lobbyservice.api.event.GameProposalAccepted;
import io.github.marcodiri.lobbyservice.api.event.GameProposalEventType;

public class ESEventHandler implements AutoCloseable {

    private static final Logger LOGGER = LogManager.getLogger(ESEventHandler.class);

    private final EventStoreDBClient clientES;
    private final WebClient clientHttp;

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

    public ESEventHandler(final EventStoreDBClient clientES, final WebClient clientHttp) {
        this.clientES = clientES;
        this.clientHttp = clientHttp;

        SubscribeToStreamOptions options = SubscribeToStreamOptions.get()
                .fromStart()
                .resolveLinkTos();

        clientES.subscribeToStream(
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

                            CreateGameRequest createGameRequest = new CreateGameRequest(
                                    gameProposalAcceptedEvent.getCreatorId(),
                                    gameProposalAcceptedEvent.getAcceptorId());
                            LOGGER.info("POSTing to endpoint /game/create-game " + createGameRequest);

                            CreateGameResponse response = clientHttp.post()
                                    .uri("/game/create-game")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .bodyValue(createGameRequest)
                                    .retrieve()
                                    .bodyToMono(CreateGameResponse.class)
                                    .block();
                            LOGGER.info("Received response " + response);
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
