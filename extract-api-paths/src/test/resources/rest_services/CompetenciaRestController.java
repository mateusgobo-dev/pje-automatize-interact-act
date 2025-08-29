package br.jus.cnj.pje.webservice.controller.competencia;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import br.jus.cnj.pje.nucleo.manager.CompetenciaClasseAssuntoManager;
import br.jus.cnj.pje.pjecommons.model.services.PjeResponse;
import br.jus.cnj.pje.pjecommons.model.services.PjeResponseStatus;
import br.jus.cnj.pje.webservice.controller.competencia.dto.CompetenciaClasseAssuntoDTO;
import br.jus.pje.nucleo.dto.EntityPageDTO;
import br.jus.pje.nucleo.util.StringUtil;

@Name(CompetenciaRestController.NAME)
@Scope(ScopeType.EVENT)
@Path("pje-legacy/competencias")
@Restrict("#{identity.loggedIn}")
public class CompetenciaRestController implements Serializable{
	
	private static final long serialVersionUID = 1L;
	public static final String NAME = "competenciasRestController";
	
	@Logger
	private Log logger;	
	
	@Create
	public void init(){
		logger.info(CompetenciaRestController.NAME + " inicializado!");
	}
	
	@GET
	@Path("/classes-assuntos")
	@Produces(MediaType.APPLICATION_JSON)
	public PjeResponse<EntityPageDTO<CompetenciaClasseAssuntoDTO>> pesquisarCompetenciasClasseAssunto(@PathParam("page") Integer page, @PathParam("size") Integer size, @QueryParam(value = "simpleFilter") String simpleFilter){
		ObjectMapper mapper = new ObjectMapper();
		CompetenciaClasseAssuntoDTO comp;
		PjeResponse<EntityPageDTO<CompetenciaClasseAssuntoDTO>> res = null;
		try {
			comp = !StringUtil.isEmpty(simpleFilter) ? mapper.readValue(simpleFilter, CompetenciaClasseAssuntoDTO.class) : new CompetenciaClasseAssuntoDTO();
			if(comp != null) {
				EntityPageDTO<CompetenciaClasseAssuntoDTO> paginaComps = CompetenciaClasseAssuntoManager.instance().recuperarCompetenciaClasseAssunto(page, size, comp);

				res = new PjeResponse<>(PjeResponseStatus.OK, Response.ok().toString(), null, paginaComps);
			}

		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}
	
	@PUT
	@Path("/classes-assuntos")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response atualizarCompetenciaClasseAssunto(List<CompetenciaClasseAssuntoDTO> listaAtualizar){
		Response res = Response.noContent().build();
		
		try {
			CompetenciaClasseAssuntoManager.instance().atualizarCompetenciaClasseAssunto(listaAtualizar);
			res = Response.ok().build();
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
			e.printStackTrace();
			res = Response.serverError().build();
		}
		
		return res;	
	}
}
