package io.github.marcodiri.rest;

import static io.restassured.RestAssured.get;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@ExtendWith(MockitoExtension.class)
public class InMemoryRestServerTest {

    @Path("/resource")
    public static class MyResource {

        @GET
        @Path("/method")
        public Response method() {
            System.out.println("called method");
            return Response
                    .ok()
                    .entity("service is online")
                    .build();
        }

    }

    @Path("/resource-with-dependency")
    public static class MyResourceWithDep {

        private ToInject injected;

        @GET
        @Path("/method")
        public Response methodWithDependency() {
            System.out.println(injected.method());
            return Response
                    .ok()
                    .entity("service is online")
                    .build();
        }

    }

    public static class ToInject {
        public String method() {
            return "called dependency method";
        }
    }

    private static InMemoryRestServer server;

    @Nested
    class ResourceWithNoDependency {

        @BeforeAll
        static void setup() throws IOException {
            server = InMemoryRestServer.create(MyResource.class);
        }

        @AfterAll
        static void teardown() {
            server.close();
        }

        @Test
        void uriBuilder() {
            String path = "/resource";

            assertThat(server.target(path).getUri().toString()).isEqualTo(server.baseUri() + path);
        }

        @Test
        void serverServesResourceFromClass() {
            assertThat(server.target("/resource").path("/method").request().get().getStatus())
                    .isEqualTo(200);
        }

        @Test
        void testWithRestAssured() {
            get(server.target("/resource").path("/method").getUri())
                    .then()
                    .assertThat()
                    .statusCode(200)
                    .body(equalTo("service is online"));
        }

    }

    @Nested
    class ResourceWithDependency {

        @Mock
        private ToInject injected;

        @InjectMocks
        private MyResourceWithDep myResourceWithDep;

        @AfterEach
        void teardown() {
            server.close();
        }

        @Test
        void serverServesResourceFromInstanceWithInjectedDependency() throws IOException {
            server = InMemoryRestServer.create(myResourceWithDep);
            assertThat(server.target("/resource-with-dependency").path("/method").request().get().getStatus())
                    .isEqualTo(200);
            verify(injected).method();
        }

        @Test
        void serverServesResourceFromInstanceWithInjectedDependencyAndFromClass() throws IOException {
            server = InMemoryRestServer.create(myResourceWithDep, MyResource.class);
            assertThat(server.target("/resource-with-dependency").path("/method").request().get().getStatus())
                    .isEqualTo(200);
            verify(injected).method();
            assertThat(server.target("/resource").path("/method").request().get().getStatus())
                    .isEqualTo(200);
        }

    }

}
