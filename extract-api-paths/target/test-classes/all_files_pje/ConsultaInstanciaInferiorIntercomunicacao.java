package br.com.infox.trf.webservice;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.manager.ProcessoTrfManifestacaoProcessualManager;
import br.jus.pje.nucleo.entidades.EnderecoWsdl;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Scope(value = ScopeType.EVENT)
@BypassInterceptors
@Name(ConsultaInstanciaInferiorIntercomunicacao.NAME)
/**
 * Classe responsável por consultar dados de processos na instância inferior.
 * As funcionalidades implementadas nesta classe foram generalizadas na classe "ConsultaInstanciaIntercomunicacao".
 * A fim de manter disponível o uso desta classe por fluxos processuais já existentes, esta classe foi mantida.
 * @author Leonardo Inácio
 *
 */
public class ConsultaInstanciaInferiorIntercomunicacao extends ConsultaInstanciaIntercomunicacao {
	// Constantes
	public final static String NAME = "consultaInstanciaInferiorIntercomunicacao";
	
	public static ConsultaInstanciaInferiorIntercomunicacao instance() {
		return ComponentUtil.getComponent(ConsultaInstanciaInferiorIntercomunicacao.NAME);
	}

	/**
	 * Este método consulta um processo na instância inferior.
	 * A consulta é feita através do número do processo passado como parâmetro.
	 * @param numeroProcesso
	 * @return processoInstanciaInferior
	 * @throws Exception
	 */
	public ProcessoTrf consultarProcesso(String numeroProcesso) throws Exception {
		ProcessoTrfManifestacaoProcessualManager manager = ProcessoTrfManifestacaoProcessualManager.instance();
		EnderecoWsdl wsdl = manager.obterEnderecoWsdlDaManifestacao(numeroProcesso);
		return this.consultarProcesso(wsdl, numeroProcesso, false, false);
	}
}