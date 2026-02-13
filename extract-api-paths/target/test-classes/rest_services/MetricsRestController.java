package br.jus.cnj.pje.webservice.controller.metrics;

import java.io.Serializable;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

@Name(MetricsRestController.NAME)
@Scope(ScopeType.EVENT)
@Path("pje-legacy/metrics")
@Restrict("#{identity.loggedIn and s:hasRole('admin')}")
public class MetricsRestController implements Serializable {

	private static final long serialVersionUID = 2274688792358339978L;

	public static final String NAME = "metricsRestController";

	@GET
	@Path("/")
	@Produces(MediaType.TEXT_PLAIN)
	public Response metrics(@QueryParam("servername") String serverName, @QueryParam("port") String port) {
		return getMetrics(serverName, port, "");
	}

	@GET
	@Path("/base")
	@Produces(MediaType.TEXT_PLAIN)
	public Response metricsBase(@QueryParam("servername") String serverName, @QueryParam("port") String port) {
		return getMetrics(serverName, port, "base");
	}

	@GET
	@Path("/vendor")
	@Produces(MediaType.TEXT_PLAIN)
	public Response metricsVendor(@QueryParam("servername") String serverName, @QueryParam("port") String port) {
		return getMetrics(serverName, port, "vendor");
	}

	private Response getMetrics(String serverName, String port, String scope) {
		serverName = serverName == null || serverName.isEmpty() ? "localhost" : serverName;
		port = port == null || port.isEmpty() ? "9990" : port;

		String urlAdminServer = "http://" + serverName + ":" + port;

		Client client = ClientBuilder.newClient();

		WebTarget webTarget = client.target(urlAdminServer).path("metrics").path(scope);

		Invocation.Builder invocationBuilder = webTarget.request();

		Response resp = (Response) invocationBuilder.get();

		StringBuilder output = new StringBuilder();

		output.append("Server: ");
		output.append(System.getProperty("jboss.server.name"));
		output.append("\n");
		output.append(resp.readEntity(String.class));

		return Response.ok(output.toString()).build();
	}
}
