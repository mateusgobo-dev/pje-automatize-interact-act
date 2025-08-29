/**
 * pje-web
 * Copyright (C) 2009-2014 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.cnj.pje.view;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.home.SessaoPautaProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.jt.pje.manager.DerrubadaVotoManager;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.manager.SessaoJulgamentoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoPautaProcessoTrfManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoVotoManager;
import br.jus.cnj.pje.nucleo.manager.TipoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.TipoVotoManager;
import br.jus.cnj.pje.visao.beans.VotacaoBean;
import br.jus.cnj.pje.visao.beans.VotacaoBean.ObjetoVoto;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;

/**
 * Componente de controle do popup (/Painel/SecretarioSessao/popUpVotoEmentaRelatorio.xhtml) do Painel do secretário da sessão.
 */
@Name(PainelSecretarioSessaoAction.NAME)
@Scope(ScopeType.EVENT)
public class PainelSecretarioSessaoAction {
	
	public static final String NAME = "painelSecretarioSessaoAction";

	@RequestParameter("idSessao")
	private Integer idSessao;
	
	@RequestParameter("idJulgamento")
	private Integer idJulgamento;
	
	@RequestParameter("idOrgaoAcompanhado")
	private Integer idOrgaoAcompanhado;
	
	private Map<String, SessaoProcessoDocumento> elementosJulgamento;

	private HashMap<Integer, VotacaoBean> votacao = new HashMap<Integer, VotacaoBean>();

	private Sessao sessao;

	@In
	private SessaoJulgamentoManager sessaoJulgamentoManager;

	@In
	private SessaoPautaProcessoTrfManager sessaoPautaProcessoTrfManager;

	@In
	private OrgaoJulgadorManager orgaoJulgadorManager;

	@In
	private SessaoProcessoDocumentoManager sessaoProcessoDocumentoManager;

	@In
	private SessaoProcessoDocumentoVotoManager sessaoProcessoDocumentoVotoManager;

	@In
	private TipoVotoManager tipoVotoManager;
	
	@In
	private DocumentoJudicialService documentoJudicialService;
	
	@In
	private TipoProcessoDocumentoManager tipoProcessoDocumentoManager;

	@In
	private DerrubadaVotoManager derrubadaVotoManager;

	@Logger
	private Log logger;
	
	/**
	 * Método responsável por inicializar a variável de instância "sessao" com o objeto {@link Sessao} 
	 * correspondente ao identificador "idSessao" passado via queryString.
	 */
	public void inicializarSessao() {
		try {
			this.sessao = this.sessaoJulgamentoManager.findById(this.idSessao);
		} catch (PJeBusinessException e) {
			e.printStackTrace();
			logger.error(Severity.FATAL, "Erro ao atualizar a Sessao: {0}.", e.getLocalizedMessage());
		}
	}
	
	/**
	 * Método responsável por recuperar o nome do órgão o qual corresponde ao identificador 
	 * "idOrgaoAcompanhado" passado via queryString.
	 * 
	 * @return O nome do órgão o qual corresponde ao identificador "idOrgaoAcompanhado" passado via queryString
	 */
	public String getNomeOrgao() {
		try {
			return this.orgaoJulgadorManager.findById(this.idOrgaoAcompanhado).getOrgaoJulgador();
		} catch (PJeBusinessException e) {
			e.printStackTrace();
			logger.error("Erro ao tentar recuperar o nome do órgão.");
			return StringUtils.EMPTY;
		}
	}
	
	/**
	 * Método responsável por recuperar o conteúdo do(s) voto(s). 
	 * 
	 * @return O conteúdo do(s) voto(s).
	 */
	public Map<String, SessaoProcessoDocumento> getMapElementosJulgamento(){
		Map<String, SessaoProcessoDocumento> mapElementosJulgamento = new HashMap<String, SessaoProcessoDocumento>();
		
		if(this.idJulgamento != null && this.idOrgaoAcompanhado != null){
			for(Map.Entry<String, SessaoProcessoDocumento> entry : getElementosJulgamento().entrySet()) {
				SessaoProcessoDocumento sessaoProcessoDocumento = entry.getValue();
				if(sessaoProcessoDocumento.getOrgaoJulgador().getIdOrgaoJulgador() == this.idOrgaoAcompanhado || 
					(SessaoProcessoDocumentoVoto.class.isAssignableFrom(sessaoProcessoDocumento.getClass()) && 
						!((SessaoProcessoDocumentoVoto) sessaoProcessoDocumento).getImpedimentoSuspeicao() 
						&& ((SessaoProcessoDocumentoVoto) sessaoProcessoDocumento).getOjAcompanhado().getIdOrgaoJulgador() == this.idOrgaoAcompanhado)){
					
					mapElementosJulgamento.put(entry.getKey(), sessaoProcessoDocumento);
				}
			}
		}
		
		return mapElementosJulgamento;
	}
	
	/**
	 * 
	 * 
	 * @return
	 */
	private Map<String, SessaoProcessoDocumento> getElementosJulgamento(){
		carregarElementos();
		return this.elementosJulgamento;
	}
	
	/**
	 * 
	 */
	private void carregarElementos(){
		if(this.idJulgamento != null && this.elementosJulgamento == null){
			this.elementosJulgamento = getJulgamento(this.idJulgamento).getObjetosVotacao();
		}
	}
	
	/**
	 * 
	 * 
	 * @param idJulgamento
	 * @return
	 */
	private VotacaoBean getJulgamento(Integer idJulgamento){
		if(idJulgamento == null || idJulgamento == 0){
			idJulgamento = this.idJulgamento;
		}
		carregarJulgamento(idJulgamento);
		
		return this.votacao.get(idJulgamento);
	}
	
	/**
	 * 
	 * 
	 * @param idJulgamento
	 */
	private void carregarJulgamento(Integer idJulgamento){
		if(idJulgamento != null && idJulgamento > 0 && this.votacao.get(idJulgamento) == null){
			try {
				SessaoPautaProcessoTrf julgamento = this.sessaoPautaProcessoTrfManager.findById(idJulgamento);
				Map<String, TipoProcessoDocumento> tiposDocumentos = getTiposDocumentos();
				VotacaoBean votacaoBean = new VotacaoBean(
						julgamento.getProcessoTrf(), null, this.sessao, julgamento, true, this.sessaoProcessoDocumentoVotoManager, 
						this.sessaoProcessoDocumentoManager, tiposDocumentos, this.tipoVotoManager, this.documentoJudicialService, this.derrubadaVotoManager);
				this.votacao.put(idJulgamento, votacaoBean);
			} catch (PJeBusinessException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 
	 * 
	 * @return
	 */
	private Map<String, TipoProcessoDocumento> getTiposDocumentos() {
		Map<String, TipoProcessoDocumento> result = new HashMap<String, TipoProcessoDocumento>();
		result.put(ObjetoVoto.RELATORIO.toString(), ParametroUtil.instance().getTipoProcessoDocumentoRelatorio());
		result.put(ObjetoVoto.EMENTA.toString(), ParametroUtil.instance().getTipoProcessoDocumentoEmenta());
		result.put(ObjetoVoto.VOTO.toString(), ParametroUtil.instance().getTipoProcessoDocumentoVoto());
		result.putAll(tipoProcessoDocumentoManager.getMapTipoProcessoDocumento(ParametroUtil.instance().getIdsTipoDocumentoVoto()));
		
		return result;
	}
	
	/**
	 * Verifica se é possível a exibição do conteúdo dos votos antecipados para
	 * os usuário que não são magistrados.
	 * 
	 * Quando o parâmetro de sistema
	 * pje:sessao:ocultarVotosAntecipadosNaoMagistrado estiver setado o sistema
	 * irá liberar apenas se o usuário for magistrado ou então se o documento de
	 * acórdão já estiver juntado aos autos
	 */
	public boolean podeExibirConteudoVotosAntecipados() {
		try {
			SessaoPautaProcessoTrf julgamento = this.sessaoPautaProcessoTrfManager.findById(idJulgamento);
			return sessaoPautaProcessoTrfManager.podeExibirConteudoVotosAntecipados(julgamento.getSessao(), julgamento.getProcessoTrf());
		} catch (PJeBusinessException e){
			e.printStackTrace();
			return false;
		}		
	}

}
