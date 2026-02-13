package br.jus.pje.api.controllers.v1.migrador;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.exceptions.NegocioException;
import br.jus.cnj.pje.nucleo.service.ValidaProcessoMigracaoService;

@Name(ValidaProcessoMigracaoRestController.NAME)
@Scope(ScopeType.EVENT)
@Path("/pje-legacy/api/v1/migracao/valida-processo-migracao")
public class ValidaProcessoMigracaoRestController implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "validaProcessoMigracaoRestController";
	private static final String PROCESSO_ELEGIVEL_MIGRACAO = "Processo elegível para migração.";

	@In
	private ValidaProcessoMigracaoService validaProcessoMigracaoService;

	@GET
	@Path("/numero-processo/{numeroProcesso}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response validarProcesso(@PathParam("numeroProcesso") String numeroProcesso, @HeaderParam("Authorization") String authorization) {

		Response validaToken = JwtUtils.validarToken(authorization);
		if (validaToken != null) {
			return validaToken;
		}

		try {

			validaProcessoMigracaoService.validarSinalizacaoMigracao(numeroProcesso, true);
			return montarResposta(Response.Status.OK, true, PROCESSO_ELEGIVEL_MIGRACAO);

		} catch (NegocioException e) {
			return montarResposta(Response.Status.BAD_REQUEST, false, e.getMensagem());
		} catch (NotFoundException e) {
			return montarResposta(Response.Status.NOT_FOUND, false, e.getMessage());
		} catch (Exception e) {
			return montarResposta(Response.Status.INTERNAL_SERVER_ERROR, false, e.getMessage());
		}
	}

	private Response montarResposta(Response.Status status, boolean apto, String mensagem) {
		Map<String, Object> responseBody = new HashMap<String, Object>();
		responseBody.put("apto", apto);
		responseBody.put("mensagem", mensagem);

		return Response.status(status).entity(responseBody).build();
	}
}