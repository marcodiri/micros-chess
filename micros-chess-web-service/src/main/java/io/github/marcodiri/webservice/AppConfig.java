package io.github.marcodiri.webservice;

import java.net.URI;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.EventStoreDBClientSettings;
import com.eventstore.dbclient.EventStoreDBConnectionString;

@Configuration
public class AppConfig {

    @Bean
    public URI gameServiceBaseUri() {
        return URI.create("http://game-service:8080/micros-chess/rest");
    }

    // This is set to the name of the event store docker container
    private static final String hostname = "eventstore";
    private static final int port = 2113;
    private static final String CONNECTION_STRING = String.format("esdb://%s:%s?tls=false", hostname, port);

    private EventStoreDBClientSettings setts = EventStoreDBConnectionString.parseOrThrow(CONNECTION_STRING);

    @Bean
    public EventStoreDBClient eventStoreDBClient() {
        return EventStoreDBClient.create(setts);
    }

}
