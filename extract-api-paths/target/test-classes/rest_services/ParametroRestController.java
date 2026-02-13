package br.jus.pje.api.controllers.v1;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.pjecommons.model.services.PjeResponse;
import br.jus.cnj.pje.pjecommons.model.services.PjeResponseStatus;
import br.jus.pje.api.converters.ParametroConverter;
import br.jus.pje.nucleo.dto.ParametroDTO;
import br.jus.pje.nucleo.entidades.Parametro;
import br.jus.pje.nucleo.util.StringUtil;

@Name(ParametroRestController.NAME)
@Scope(ScopeType.EVENT)
@Path("/pje-legacy/api/v1")
@Restrict("#{identity.loggedIn}")
public class ParametroRestController {

	public static final String NAME = "parametroRestController";
	
	private ParametroConverter parametroConverter;
	
	@In
	private ParametroService parametroService;
	
	@Create
	public void init() {
		parametroConverter = new ParametroConverter();
	}
	
	@GET
	@Path("/parametros/{nomeVariavel}")
	@Produces(MediaType.APPLICATION_JSON)
	public PjeResponse<ParametroDTO> recuperarParametro(@PathParam("nomeVariavel") String nomeVariavel){
		PjeResponse<ParametroDTO> response = new PjeResponse<ParametroDTO>(PjeResponseStatus.OK, "200", null, new ParametroDTO());
		List<String> mensagens = new ArrayList<>();
		
		if(!StringUtil.isEmpty(nomeVariavel)) {
			Parametro parametroEntity = parametroService.findByName(nomeVariavel);
			if(parametroEntity != null && (parametroEntity.getDadosSensiveis() == null || parametroEntity.getDadosSensiveis() == false)) {
				ParametroDTO dto = this.parametroConverter.convertFrom(parametroEntity);
				response = new PjeResponse<ParametroDTO>(PjeResponseStatus.OK, "200", null, dto);
			} else {
				mensagens.add("Nenhum parâmetro encontrado para este nome");
				response = new PjeResponse<ParametroDTO>(PjeResponseStatus.OK, "200", null, new ParametroDTO());
			}
		} else {
			mensagens.add("Nenhum nome de variável informado para consulta.");
			response = new PjeResponse<ParametroDTO>(PjeResponseStatus.ERROR, "200", mensagens, new ParametroDTO());
		}
		
		return response;
	}
}
