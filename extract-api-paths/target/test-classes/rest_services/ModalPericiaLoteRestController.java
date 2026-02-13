package br.jus.cnj.pje.webservice.controller.modalPericiaLote;

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

import br.jus.cnj.pje.webservice.IPericiaService;

@Name("modalPericiaLoteRestController")
@Scope(ScopeType.EVENT)
@Path("pje-legacy/modalPericiaLote")
@Restrict("#{identity.loggedIn}")
public class ModalPericiaLoteRestController {
	
	@In(create=true)
	private IPericiaService periciaService;

    @GET
    @Path("/especialidades")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obterEspecialidades() {
    	return Response.ok(periciaService.obterEspecialidadesAtiva()).build();
    }
    
    @GET
    @Path("/peritos/{idEspecialidade}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obterPeritos(@PathParam("idEspecialidade") Integer idEspecialidade, @QueryParam("idsOJ") List<Integer> idsOrgaoJulgador) {
    	return Response.ok(periciaService.obterPeritosAtivo(idEspecialidade, idsOrgaoJulgador)).build();
    }
    
    @GET
    @Path("/designar/{idTarefa}/{idProcesso}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response designarPericia(@PathParam("idTarefa") Long idTarefa, @PathParam("idProcesso") Long idProcesso, 
    	@QueryParam("idEspecialidade") Integer idEspecialidade, @QueryParam("idPerito") Integer idPerito, 
    	@QueryParam("valor") Double valor, @QueryParam("dataInicio") String dataInicio) {
    	
    	Date data = null;
    	if (StringUtils.isNotBlank(dataInicio)) {
           	data = new Date(dataInicio);
    	}
    	try {    		
    		return Response.ok(periciaService.designarPericia(
    			idTarefa, idProcesso, idEspecialidade, idPerito, valor, data != null ? data.getTime() : null)).build();
    	} catch (Exception ex) {
    		return Response.serverError().entity(Json.createObjectBuilder().add("msgError", ex.getLocalizedMessage()).build()).build();
    	}
    }

}
