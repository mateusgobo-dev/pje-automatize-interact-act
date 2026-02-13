/**
 * EnderecoWsdlManager.java
 * 
 * Data: 02/05/2016
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.EnderecoWsdlDAO;
import br.jus.pje.nucleo.entidades.EnderecoWsdl;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrfManifestacaoProcessual;

/**
 * Classe de negócio responsável pela manipulação dos objetos relacionados à
 * entidade EnderecoWsdl.
 * 
 * @author Adriano Pamplona
 */
@Name("enderecoWsdlManager")
public class EnderecoWsdlManager extends BaseManager<EnderecoWsdl> {

	@In
	private EnderecoWsdlDAO enderecoWsdlDAO;
	
	@Override
	public EnderecoWsdlDAO getDAO() {
		return this.enderecoWsdlDAO;
	}
	
	public static EnderecoWsdlManager instance(){
		return ComponentUtil.getComponent(EnderecoWsdlManager.class);
	}

	/**
	 * Consulta os endereços wsdl de todas as instâncias, exceto a instância passada por parâmetro.
	 * 
	 * @param instanciaExcecao
	 * @return coleção de EnderecoWsdl
	 */
	public Collection<EnderecoWsdl> consultarEnderecosExceto(String instanciaExcecao) {
		Collection<EnderecoWsdl> resultado = new ArrayList<EnderecoWsdl>();
		
		if (StringUtils.isNotBlank(instanciaExcecao)) {
			resultado = getDAO().consultarEnderecosExceto(instanciaExcecao);
		}
		return resultado;
		
	}
	
	/**
	 * Retorna o EnderecoWsdl de onde o processo foi recebido. A procura se faz na seguinte ordem:
	 * 1) Na tabela ProcessoTrfManifestacaoProcessual (alimentada na remessa/retorno depois da implementação dos multiplos endpoints);
	 * 2) Na tabela ManifestacaoProcessual (alimentada na remessa/retorno);
	 * 3) N tabela de Parametro (retorno o endereço configurado como endereço de integração).
	 * 
	 * @param processoTrf ProcessoTrf
	 * @param obterPadrao True retorna o endereço da instância de integração configurada nos parâmetros.
	 * @return EnderecoWsdl
	 */
	public EnderecoWsdl obterEnderecoWsdl(ProcessoTrf processoTrf, boolean obterPadrao) {
		EnderecoWsdl resultado = null;
		
		// busca na tabela de ProcessoTrfManifestacaoProcessual (carregada na remessa/retorno)
		ProcessoTrfManifestacaoProcessual processoTrfManifestacao = 
				ComponentUtil.getProcessoTrfManifestacaoProcessualManager().obterUltimo(processoTrf);
		if (processoTrfManifestacao != null) {
			resultado = processoTrfManifestacao.getEnderecoWsdl();
		} else {
			// se não encontrado tenta buscar na tabela ManifestacaoProcessual (carregada na remessa/retorno 
			// dos registros antigos)
			br.jus.pje.nucleo.entidades.ManifestacaoProcessual manifestacaoProcessual = 
					ComponentUtil.getManifestacaoProcessualManager().buscaUltimoEntregue(processoTrf);
			
			if (manifestacaoProcessual != null) {
				resultado = new EnderecoWsdl();
				resultado.setWsdlConsulta(manifestacaoProcessual.getWsdlOrigemConsulta());
				resultado.setWsdlIntercomunicacao(manifestacaoProcessual.getWsdlOrigemEnvio());
			} else {
				//se não encontrado busca na configuração do endpoint de integração configurado
				//na tabela de parâmetros.
				if (obterPadrao) {
					resultado = ParametroUtil.instance().getEnderecoWsdlAplicacaoOrigem();
				}
			}
		}
		
		return resultado;
	}

	/**
	 * Consulta o endereço pelo WSDL de intercomunicação.
	 * 
	 * @param enderecoOrigemEnvio
	 * @return EnderecoWsdl
	 */
	public EnderecoWsdl obterPeloWsdlIntercomunicacao(String wsdlIntercomunicacao) {
		EnderecoWsdl resultado = null;
		
		if (StringUtils.isNotBlank(wsdlIntercomunicacao)) {
			resultado = getDAO().obterPeloWsdlIntercomunicacao(wsdlIntercomunicacao);
		}
		return resultado;
	}
	
	/**
	 * Consulta todos os endereços wsdl, exceto o endereço wsdl local.
	 * 
	 * @return coleção de EnderecoWsdl
	 */
	public Collection<EnderecoWsdl> consultarEnderecosExcetoLocal() {
		Collection<EnderecoWsdl> resultado = new ArrayList<EnderecoWsdl>();		

		resultado = getDAO().consultarEnderecosExcetoLocal();

		return resultado;
	}
}
