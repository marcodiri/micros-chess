/*
 * Created by Marco Di Rienzo.
 *
 * Inspired by:
 * https://github.com/mp911de/rest-api-test/blob/master/src/test/java/biz/paluch/rest/test/InMemoryRestServer.java
 * and
 * https://gist.github.com/mageddo/782e3c89f36531aae999dc91a4e0409c
 * and
 * https://stackoverflow.com/a/44689665/9525450
 */

package io.github.marcodiri.rest;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.core.ResteasyDeploymentImpl;
import org.jboss.resteasy.plugins.server.embedded.SecurityDomain;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.jboss.resteasy.spi.ResteasyDeployment;

import io.undertow.Undertow;
import io.undertow.servlet.api.DeploymentInfo;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Application;

/**
 * In-memory JAX-RS server using {@link UndertowJaxrsServer}.
 */
public class InMemoryRestServer implements AutoCloseable {

	public static final String HOST = "localhost";
	private final Set<Class<?>> classes = new HashSet<>();
	private final List<Object> objects = new ArrayList<>();

	private int port;
	private UndertowJaxrsServer server;
	private Client client;
	private SecurityDomain securityDomain;

	private InMemoryRestServer(Object... objects) {
		for (Object object : objects) {
			if (object instanceof Class) {
				classes.add((Class<?>) object);
			} else {
				this.objects.add(object);
			}
		}
	}

	/**
	 * Create instance and pass given instances/classes as singletons/providers.
	 *
	 * @param objects
	 * @return running instance of {@link InMemoryRestServer}
	 * @throws IOException
	 */
	public static InMemoryRestServer create(Object... objects) throws IOException {
		return create(null, objects);
	}

	/**
	 * Create instance and pass given instances/classes as singletons/providers.
	 *
	 * @param securityDomain
	 * @param objects
	 * @return running instance of {@link InMemoryRestServer}
	 * @throws IOException
	 */
	public static InMemoryRestServer create(SecurityDomain securityDomain, Object... objects) throws IOException {
		InMemoryRestServer inMemoryRestServer = new InMemoryRestServer(objects);
		inMemoryRestServer.withDefaults(securityDomain);

		inMemoryRestServer.start();
		return inMemoryRestServer;
	}

	private void withDefaults(SecurityDomain securityDomain) {
		this.securityDomain = securityDomain;
		this.client = ResteasyClientBuilder.newClient();
	}

	private void start() throws IOException {
		port = findFreePort();

		server = new UndertowJaxrsServer().start(Undertow.builder().addHttpListener(port, HOST));
		server.setSecurityDomain(securityDomain);

		ResteasyDeployment resteasyDeployment = new ResteasyDeploymentImpl();
		resteasyDeployment.setDeploymentSensitiveFactoryEnabled(true);

		final DeploymentInfo undertowDeployment = server
				.undertowDeployment(resteasyDeployment)
				.setContextPath("/")
				.setDeploymentName("IN-MEMORY SERVER")
				.setClassLoader(Thread.currentThread().getContextClassLoader());

		server.deploy(undertowDeployment);

		for (Object object : objects) {
			resteasyDeployment.getRegistry().addSingletonResource(object);
		}
		resteasyDeployment.setApplication(new Application() {
			@Override
			public Set<Class<?>> getClasses() {
				return classes;
			}
		});

		resteasyDeployment.start();
		server.start();
	}

	public Client getClient() {
		return client;
	}

	/**
	 * Find a free server port.
	 *
	 * @return port number.
	 * @throws IOException
	 */
	public static int findFreePort() throws IOException {
		ServerSocket server = new ServerSocket(0);
		int port = server.getLocalPort();
		server.close();
		return port;
	}

	/**
	 * @return baseURI (http://localhost:PORT) to the REST server.
	 */
	public String baseUri() {
		return String.format("http://%s:%s", HOST, port);
	}

	/**
	 * Begin a new {@link WebTarget}.
	 *
	 * @return WebTarget
	 */
	public WebTarget target() {
		return target("");
	}

	/**
	 * Begin a new {@link WebTarget} with additional, relative path with leading /.
	 *
	 * @param uriTemplate
	 * @return WebTarget
	 */
	public WebTarget target(String uriTemplate) {
		return client.target(baseUri() + uriTemplate);
	}

	/**
	 * Close the server and free resources.
	 */
	@Override
	public void close() {
		if (server != null) {
			server.stop();
			server = null;
		}
		if (client != null) {
			client.close();
			client = null;
		}
	}

}
