package io.github.marcodiri.gameservice.repository.eventstore;

import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.EventStoreDBClientSettings;
import com.eventstore.dbclient.EventStoreDBConnectionString;

import jakarta.enterprise.inject.Produces;

public class EventStoreDBClientFactory {

    // This is set to the name of the event store docker container
    private static final String hostname = "eventstore";
    private static final int port = 2113;
    private static final String CONNECTION_STRING = String.format("esdb://%s:%s?tls=false", hostname, port);

    private EventStoreDBClientSettings setts = EventStoreDBConnectionString.parseOrThrow(CONNECTION_STRING);

    @Produces
    public EventStoreDBClient createClient() {
        return EventStoreDBClient.create(setts);
    }

}
