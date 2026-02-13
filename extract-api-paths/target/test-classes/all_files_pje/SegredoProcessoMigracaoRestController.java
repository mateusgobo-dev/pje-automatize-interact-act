package br.jus.pje.api.controllers.v1.migrador;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
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
import br.jus.cnj.pje.nucleo.service.SegredoProcessoMigracaoService;

@Name(SegredoProcessoMigracaoRestController.NAME)
@Scope(ScopeType.EVENT)
@Path("/pje-legacy/api/v1/migracao/segredo-processo")
public class SegredoProcessoMigracaoRestController implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "segredoProcessoMigracaoRestController";
	private static final String PROCESSO_SEGREDO_ATUALIZADO = "Segredo do processo atualizado com sucesso";

	@In
	private SegredoProcessoMigracaoService segredoProcessoService;

	@PUT
	@Path("/numero-processo/{numeroProcesso}/nivel-sigilo/{nivelSigilo}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response atualizaSegredoProcesso(@PathParam("numeroProcesso") String numeroProcesso,
												@PathParam("nivelSigilo") String nivelSigilo, 
												@HeaderParam("Authorization") String authorization) {

		try {

			Response validaToken = JwtUtils.validarToken(authorization);
			if (validaToken != null) {
				return validaToken;
			}

			segredoProcessoService.atualizaSegredoProcesso(numeroProcesso, nivelSigilo);
			return montarResposta(Response.Status.OK, PROCESSO_SEGREDO_ATUALIZADO);

		} catch (NegocioException e) {
			return montarResposta(Response.Status.BAD_REQUEST, e.getMensagem());
		} catch (NotFoundException e) {
			return montarResposta(Response.Status.NOT_FOUND, e.getMessage());
		} catch (Exception e) {
			return montarResposta(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	private Response montarResposta(Response.Status status, String mensagem) {
		Map<String, Object> responseBody = new HashMap<String, Object>();
		responseBody.put("mensagem", mensagem);

		return Response.status(status).entity(responseBody).build();
	}
}
