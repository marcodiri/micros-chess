package io.github.marcodiri.lobbyservice.repository.eventstore;

import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.EventStoreDBClientSettings;
import com.eventstore.dbclient.EventStoreDBConnectionString;

import jakarta.enterprise.inject.Produces;

public class EventStoreDBClientFactory {

    private static final String CONNECTION_STRING = "esdb://localhost:2113?tls=false";

    EventStoreDBClientSettings setts = EventStoreDBConnectionString.parseOrThrow(CONNECTION_STRING);

    @Produces
    public EventStoreDBClient createClient() {
        return EventStoreDBClient.create(setts);
    }

}
