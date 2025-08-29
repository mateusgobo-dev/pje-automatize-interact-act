package br.jus.pje.api.controllers.v1;

import br.jus.cnj.pje.webservice.client.bnmp.GuiaRecolhimentoRestClient;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Name(GuiaRecolhimentoRestController.NAME)
@Scope(ScopeType.EVENT)
@Path("/pje-legacy/api/v1")
//@Restrict("#{identity.loggedIn}")
public class GuiaRecolhimentoRestController {

    public static final String NAME = "guiaRecolhimentoRestController";

    @GET
    @Path("/guia-recolhimento/desvincular/{idProc}/{dataDesvinculacao}/{numeroGrerj}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response desvincular(@PathParam("idProc") String idProc,
                                @PathParam("dataDesvinculacao") String dataDesvinculacao,
                                @PathParam("numeroGrerj") String numeroGrerj) {
        return Response.ok().entity(String.format("Desvinculado com sucesso! %s", numeroGrerj)).build();
    }
}
