/**
 * ProcessoTrfManifestacaoProcessualManager.java
 * 
 * Data: 17/05/2016
 */
package br.jus.cnj.pje.nucleo.manager;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.pje.dao.ProcessoTrfDAO;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.ProcessoTrfManifestacaoProcessualDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.EnderecoWsdl;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrfManifestacaoProcessual;

/**
 * Classe de negócio responsável pela manipulação dos objetos relacionados à
 * entidade ProcessoTrfManifestacaoProcessual.
 * 
 * @author Adriano Pamplona
 */
@Name("processoTrfManifestacaoProcessualManager")
public class ProcessoTrfManifestacaoProcessualManager extends BaseManager<ProcessoTrfManifestacaoProcessual> {

	@In
	private ProcessoTrfManifestacaoProcessualDAO processoTrfManifestacaoProcessualDAO;

	@In
	private ProcessoTrfDAO processoTrfDAO;
	
	@Override
	protected ProcessoTrfManifestacaoProcessualDAO getDAO() {
		return this.processoTrfManifestacaoProcessualDAO;
	}
	
	/**
	 * @return instância de ProcessoTrfManifestacaoProcessualManager
	 */
	public static ProcessoTrfManifestacaoProcessualManager instance(){
		return ComponentUtil.getComponent(ProcessoTrfManifestacaoProcessualManager.class);
	}
	
	@Override
	public ProcessoTrfManifestacaoProcessual persist(ProcessoTrfManifestacaoProcessual entity) throws PJeBusinessException{
		ProcessoTrfManifestacaoProcessual resultado = null;
		if (entity != null && entity.getEnderecoWsdl() != null && !getDAO().isExiste(entity)) {
			resultado = getDAO().persist(entity);
		}
		return resultado;
	};
	
	/**
	 * Retorna o último registro inserido do processo passado por parâmetro. O registro refere-se
	 * à ultima remessa/retorno.
	 * 
	 * @param numeroProcesso
	 * @return ProcessoTrfManifestacaoProcessual
	 */
	public ProcessoTrfManifestacaoProcessual obterUltimo(String numeroProcesso) {
		ProcessoTrfManifestacaoProcessual resultado = null;
		
		if (StringUtils.isNotBlank(numeroProcesso)) {
			ProcessoTrf processo = processoTrfDAO.recuperarProcesso(numeroProcesso);
			resultado = getDAO().obterUltimo(processo);
		}
		return resultado;
		
	}
	
	/**
	 * Retorna o último registro inserido do processo passado por parâmetro. O registro refere-se
	 * à ultima remessa/retorno.
	 * 
	 * @param processo
	 * @return ProcessoTrfManifestacaoProcessual
	 */
	public ProcessoTrfManifestacaoProcessual obterUltimo(ProcessoTrf processo) {
		ProcessoTrfManifestacaoProcessual resultado = null;
		
		if (processo != null) {
			resultado = getDAO().obterUltimo(processo);
		}
		return resultado;
		
	}
	
	/**
	 * Retorna o EnderecoWsdl registrado para o ProcessoTrf recebido pelo sistema. Se não houver EnderecoWsdl registrado 
	 * então será recuperado o EnderecoWsdl configurado nos parâmetros como Wsdl de integração.
	 * 
	 * @param processo ProcessoTrf com número do processo
	 * @return EnderecoWsdl registrado para o ProcessoTrf recebido pelo sistema.
	 */
	public EnderecoWsdl obterEnderecoWsdlDaManifestacao(ProcessoTrf processo) {
		String numero = (processo != null ? processo.getNumeroProcesso() : null);
		
		return obterEnderecoWsdlDaManifestacao(numero);
	}
	
	/**
	 * Retorna o EnderecoWsdl registrado para o ProcessoTrf recebido pelo sistema. Se não houver EnderecoWsdl registrado 
	 * então será recuperado o EnderecoWsdl configurado nos parâmetros como Wsdl de integração.
	 * 
	 * @param numeroProcesso Número do ProcessoTrf
	 * @return EnderecoWsdl registrado para o ProcessoTrf recebido pelo sistema.
	 */
	public EnderecoWsdl obterEnderecoWsdlDaManifestacao(String numeroProcesso) {
		EnderecoWsdl resposta = null;
		
		if (StringUtils.isNotBlank(numeroProcesso)) {
			ProcessoTrfManifestacaoProcessual manifestacao = obterUltimo(numeroProcesso);
			resposta = (manifestacao != null ? manifestacao.getEnderecoWsdl() : null);
			
			if (resposta == null) {
				resposta = ParametroUtil.instance().getEnderecoWsdlIntegracao();
			}
		}
		return resposta;
	}
}
