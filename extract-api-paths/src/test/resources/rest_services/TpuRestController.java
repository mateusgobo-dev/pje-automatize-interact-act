package br.jus.pje.api.controllers.v1;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.AssuntoTrfManager;
import br.jus.cnj.pje.nucleo.manager.TipoParteManager;
import br.jus.cnj.pje.pjecommons.model.services.PjeResponse;
import br.jus.cnj.pje.pjecommons.model.services.PjeResponseStatus;
import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.AssuntoProcessual;
import br.jus.cnj.pje.webservice.PjeJSONProvider;
import br.jus.pje.api.converters.AssuntoProcessualConverter;
import br.jus.pje.nucleo.dto.TipoParteDTO;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.util.StringUtil;

@Name(TpuRestController.NAME)
@Scope(ScopeType.EVENT)
@Path("/pje-legacy/api/v1/tpu-pje")
@Restrict("#{identity.loggedIn}")
public class TpuRestController implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "tpuRestController";

	@Logger
	private Log logger;

	@In
	private AssuntoTrfManager assuntoTrfManager;

	@In
	private TipoParteManager tipoParteManager;

	@Create
	public void init() {
		// Autenticar?
		// Aqui deveria haver um mecanismo de autenticação mais leve do que o praticado
		// pelo Authenticator
	}

	@Destroy
	public void destroy() {
		// Logout?
	}

	@GET
	@Path("/assuntos")
	@Produces(MediaType.APPLICATION_JSON)
	public PjeResponse<List<AssuntoProcessual>> recuperarAssuntos(@QueryParam("simpleFilter") String simpleFilter) {
		PjeResponse<List<AssuntoProcessual>> response = new PjeResponse<>(PjeResponseStatus.OK, "200", null, null);

		if (!StringUtil.isEmpty(simpleFilter)) {
			try {
				JsonNode jsonNode = this.getMapper().readTree(simpleFilter);
				JsonNode node = jsonNode.get("codigoNacional");
				List<String> listaCodigos = new ObjectMapper().convertValue(node, ArrayList.class);
				List<AssuntoTrf> assuntosTrf = this.assuntoTrfManager.findAllAssuntoTrfByListCodigo(listaCodigos);
				List<AssuntoProcessual> listOfAssuntosProcessuais = new ArrayList<>();
				if (!assuntosTrf.isEmpty()) {
					for (AssuntoTrf assuntoTrf : assuntosTrf) {
						AssuntoProcessualConverter converter = new AssuntoProcessualConverter();
						AssuntoProcessual assuntoProcessual = converter.convertFrom(assuntoTrf);
						listOfAssuntosProcessuais.add(assuntoProcessual);
					}
					response.setResult(listOfAssuntosProcessuais);
				}
			} catch (IOException e) {
				List<String> messages = new ArrayList<>();
				logger.error(e.getLocalizedMessage());
				messages.add("Não foi possível converter o filtro");				
				response = new PjeResponse<>(PjeResponseStatus.ERROR, "415", messages, null);
			}
		}
		
		return response;
	}

	@GET
	@Path("/tipos-partes")
	@Produces(MediaType.APPLICATION_JSON)
	public PjeResponse<List<TipoParteDTO>> recuperarTiposPartes(@QueryParam("simpleFilter") String simpleFilter) {
		PjeResponse<List<TipoParteDTO>> response = new PjeResponse<List<TipoParteDTO>>(PjeResponseStatus.OK, "200",
				null, new ArrayList<>());
		List<String> messages = new ArrayList<>();

		try {
			List<TipoParteDTO> tiposPartes = new ArrayList<>();
			List<TipoParte> tiposParteEntity = new ArrayList<>();

			if (simpleFilter != null) {
				JsonNode jsonNode = this.getMapper().readTree(simpleFilter);
				String nomeParticipacao = jsonNode.get("nomeParticipacao").asText();
				tiposParteEntity = tipoParteManager.findByNomeParticipacao(nomeParticipacao);
			} else {
				tiposParteEntity = tipoParteManager.findAll();
			}

			for (TipoParte tipoParte : tiposParteEntity) {
				TipoParteDTO tipoParteDto = new TipoParteDTO(tipoParte);
				tiposPartes.add(tipoParteDto);
				response.setResult(tiposPartes);
			}
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage());
			messages.add("Não foi possível converter o filtro");
			response = new PjeResponse<>(PjeResponseStatus.OK, "200", messages, null);
		} catch (PJeBusinessException pbe) {
			logger.error(pbe.getLocalizedMessage());
			messages.add(pbe.getLocalizedMessage());
			response = new PjeResponse<List<TipoParteDTO>>(PjeResponseStatus.OK, "500", messages, null);			
		}

		return response;
	}

	private ObjectMapper getMapper() {
		PjeJSONProvider jsonProvider = new PjeJSONProvider();
		return jsonProvider.getObjectMapper();
	}
}
