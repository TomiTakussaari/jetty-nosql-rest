package org.eclipse.jetty.nosql.rest;

import javax.servlet.Servlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

public class EmbeddedJettyServer extends Thread {
	private final Server server;
	private final WebAppContext root;
	private final SelectChannelConnector connector;

	public EmbeddedJettyServer(int port,boolean useRestSession) throws Exception {
		server = new Server();
		connector = new SelectChannelConnector();
		connector.setPort(port);
		server.addConnector(connector);

		root = new WebAppContext();
		root.setContextPath("/");
		root.setResourceBase(".");
		if(useRestSession) {
			RestSessionIdManager idManager = new RestSessionIdManager();
			RestSessionManager sessionManager = new RestSessionManager();
			server.setSessionIdManager(idManager);
			sessionManager.setSessionIdManager(idManager);
			SessionHandler sessionHandler = new SessionHandler();
			sessionHandler.setSessionManager(sessionManager);
			root.setSessionHandler(sessionHandler);
			root.setClassLoader(getContextClassLoader());
		}

		server.setHandler(root);
		server.start();
		while(!server.isStarted()) {
			Thread.sleep(100);
		}
	}
	
	public void bindServlet(Class<? extends Servlet> servletClass,String path) {
		root.addServlet(servletClass, path);
	}
	
	public void bindServlet(ServletHolder holder, String path) {
		root.addServlet(holder, path);
	}

	public int getPort() {
		return connector.getLocalPort();
	}

	public void run() {
		try {
			server.join();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void shutdown() {
		try {
			server.stop();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}