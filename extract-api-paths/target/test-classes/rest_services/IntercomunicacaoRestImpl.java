/**
 * Please modify this class to meet your needs
 * This class is not complete
 */

package br.jus.cnj.pje.intercomunicacao.v222.servico;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import br.jus.cnj.intercomunicacao.v222.beans.ConfirmacaoRecebimento;
import br.jus.cnj.intercomunicacao.v222.beans.ManifestacaoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.RequisicaoConsultaAlteracao;
import br.jus.cnj.intercomunicacao.v222.beans.RequisicaoConsultaAvisosPendentes;
import br.jus.cnj.intercomunicacao.v222.beans.RequisicaoConsultaProcesso;
import br.jus.cnj.intercomunicacao.v222.beans.RequisicaoConsultarTeorComunicacao;
import br.jus.cnj.intercomunicacao.v222.beans.RespostaConfirmacaoRecebimento;
import br.jus.cnj.intercomunicacao.v222.beans.RespostaConsultaAlteracao;
import br.jus.cnj.intercomunicacao.v222.beans.RespostaConsultaAvisosPendentes;
import br.jus.cnj.intercomunicacao.v222.beans.RespostaConsultaProcesso;
import br.jus.cnj.intercomunicacao.v222.beans.RespostaConsultarTeorComunicacao;
import br.jus.cnj.intercomunicacao.v222.beans.RespostaManifestacaoProcessual;
import br.jus.cnj.intercomunicacao.v222.servico.Intercomunicacao;

@Produces({ MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_JSON })
@Path("pje-legacy/api/v1/servico-intercomunicacao-2.2.2")
public class IntercomunicacaoRestImpl extends IntercomunicacaoAbstract {

	public static final String NAME = "intercomunicacaoRestImpl";

	@Context
	private HttpServletRequest request;
	
	/**
	 * @see Intercomunicacao#consultarAvisosPendentes(RequisicaoConsultaAvisosPendentes)
	 */
	@POST
	@Path("/consultar-avisos-pendentes")
	public RespostaConsultaAvisosPendentes consultarAvisosPendentes(RequisicaoConsultaAvisosPendentes parameters) {
		return super.consultarAvisosPendentes(parameters);
	}

	/**
	 * @see Intercomunicacao#consultarTeorComunicacao(RequisicaoConsultarTeorComunicacao)
	 */
	@POST
	@Path("/consultar-teor-comunicacao")
	public RespostaConsultarTeorComunicacao consultarTeorComunicacao(RequisicaoConsultarTeorComunicacao parameters) {
		return super.consultarTeorComunicacao(parameters);
	}

	/**
	 * @see Intercomunicacao#consultarProcesso(RequisicaoConsultaProcesso)
	 */
	@POST
	@Path("/consultar-processo")
	public RespostaConsultaProcesso consultarProcesso(RequisicaoConsultaProcesso parameters) {
		return super.consultarProcesso(parameters);
	}

	/**
	 * @see Intercomunicacao#entregarManifestacaoProcessual(ManifestacaoProcessual)
	 */
	@POST
	@Path("/entregar-manifestacao-processual")
	public RespostaManifestacaoProcessual entregarManifestacaoProcessual(ManifestacaoProcessual parameters) {
		return super.entregarManifestacaoProcessual(parameters);
	}

	/**
	 * @see Intercomunicacao#consultarAlteracao(RequisicaoConsultaAlteracao)
	 */
	@POST
	@Path("/consultar-alteracao")
	public RespostaConsultaAlteracao consultarAlteracao(RequisicaoConsultaAlteracao parameters) {
		return super.consultarAlteracao(parameters);
	}

	/**
	 * @see Intercomunicacao#confirmarRecebimento(ConfirmacaoRecebimento)
	 */
	@POST
	@Path("/confirmar-recebimento")
	public RespostaConfirmacaoRecebimento confirmarRecebimento(ConfirmacaoRecebimento parameters) {
		return super.confirmarRecebimento(parameters);
	}
	
	@Override
	protected HttpServletRequest getRequest() {
		return request;
	}
	
	@Override
	protected void finalizarChamadaSeam() {
		// Não faz nada.
	}
}
