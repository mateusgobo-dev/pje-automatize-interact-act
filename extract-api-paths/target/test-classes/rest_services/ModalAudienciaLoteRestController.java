package br.jus.cnj.pje.webservice.controller.modalAudienciaLote;

import java.util.Date;
import java.util.List;

import javax.json.Json;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import br.jus.cnj.pje.webservice.api.IAudienciaService;

@Name("modalAudienciaLoteRestController")
@Scope(ScopeType.EVENT)
@Path("pje-legacy/modalAudienciaLote")
@Restrict("#{identity.loggedIn}")
public class ModalAudienciaLoteRestController {
	
	@In(create=true)
	private IAudienciaService audienciaService;
	
    @GET
    @Path("/tiposAudiencia")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obterTiposAudiencia() {
    	return Response.ok(audienciaService.obterTiposAudiencia()).build();
    }
    
    @GET
    @Path("/salasAudiencia/{idTipoAudiencia}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obterSalasAudiencias(@PathParam("idTipoAudiencia") Integer idTipoAudiencia, @QueryParam("idsOJ") List<Integer> idsOrgaoJulgador) {
    	return Response.ok(audienciaService.obterSalasAudiencias(idTipoAudiencia, idsOrgaoJulgador)).build();
    }
    
    @GET
    @Path("/tempoAudiencia/{idTipoAudiencia}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obterTempoAudiencia(@PathParam("idTipoAudiencia") Integer idTipoAudiencia, @QueryParam("idsOJ") List<Integer> idsOrgaoJulgador) {
    	return Response.ok(audienciaService.obterTempoAudiencia(idTipoAudiencia, idsOrgaoJulgador)).build();
    }
    
	@GET
    @Path("/designar/{idTarefa}/{idProcesso}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response designarAudiencia(@PathParam("idTarefa") Long idTarefa, @PathParam("idProcesso") Long idProcesso, 
    	@QueryParam("idTipoAudiencia") Integer idTipoAudiencia, @QueryParam("idSalaAudiencia") Integer idSalaAudiencia, 
    	@QueryParam("duracao") Integer duracao, @QueryParam("dataInicio") String dataInicio) {
    	
    	Date data = null;
    	if (StringUtils.isNotBlank(dataInicio)) {
           	data = new Date(dataInicio);
    	}
    	try {
    		return Response.ok(audienciaService.designarAudiencia(
    			idTarefa, idProcesso, idTipoAudiencia, idSalaAudiencia, duracao, data != null ? data.getTime() : null)).build();
    	} catch (Exception ex) {
    		return Response.serverError().entity(Json.createObjectBuilder().add("msgError", ex.getLocalizedMessage()).build()).build();
    	}
	}

}
