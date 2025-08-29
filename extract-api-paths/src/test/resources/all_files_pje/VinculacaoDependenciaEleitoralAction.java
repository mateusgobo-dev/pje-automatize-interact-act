package br.jus.cnj.pje.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.VinculacaoDependenciaEleitoralManager;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.cnj.pje.servicos.DistribuicaoService;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.VinculacaoDependenciaEleitoral;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * Componente controller da entidade {@link VinculacaoDependenciaEleitoral}.
 * 
 * @author eduardo.pereira
 *
 * @see <a href="http://www.cnj.jus.br/jira/browse/PJEII-5681">PJEII-5681</a>
 */
@Name("vinculacaoDependenciaEleitoralAction")
@Scope(ScopeType.CONVERSATION)
public class VinculacaoDependenciaEleitoralAction implements Serializable {

	private static final long serialVersionUID = -5240889356935919123L;

	@Logger
	private Log logger;

	@In
	private FacesMessages facesMessages;

	@In
	private VinculacaoDependenciaEleitoralManager vinculacaoDependenciaEleitoralManager;

	@In
	private ProcessoJudicialService processoJudicialService;

	@In(create=true)
	private DistribuicaoService distribuicaoService;

	private VinculacaoDependenciaEleitoral vinculacaoDependenciaEleitoral = null;

	private ProcessoTrf processoTrf = null;

	private ProcessoTrf processoParadigma;

	private List<ProcessoTrf> cadeiaProcessos = null;

	private String idProcesso;

	/**
	 * Recupera a instancia corrente do objeto processoTrf caso o mesmo esteja
	 * vazio pelo parametro id na requisição Se o objeto já estiver
	 * carregado o metodo apenas retorna a instancia corrente de ProcessoTrf.
	 * 
	 * @return processoTrf
	 * @throws Exception
	 */
	private ProcessoTrf carregarInstanciaProcessoTrf() {
		if(this.processoTrf == null) {
			try {
				if(StringUtil.isNotEmpty(idProcesso) && idProcesso != "0") {
					this.processoTrf = processoJudicialService.findById(Integer.valueOf(idProcesso));
				} else {
					ListProcessoCompletoBetaAction listProcessoCompletoBetaAction = ComponentUtil.getComponent(ListProcessoCompletoBetaAction.NAME);
					this.processoTrf = listProcessoCompletoBetaAction.getProcessoSelecionado();
				}
			} catch (Exception e) {
				facesMessages.add(Severity.ERROR, "Erro ao tentar recuperar a instância do processo.");
				logger.error("Erro ao tentar recuperar a instância do processo");
			}
		}
		return this.processoTrf;
	}

	/**
	 * Retorna uma lista de processos (ProcessoTrf) que representa cadeia
	 * formada pelo art. 260 do CE ao qual o processo detalhado faz parte.
	 * 
	 * @return List<ProcessoTrf> com os processos pesentes na mesma cadeia,
	 *         levando em conta mesma eleição, enquadramento no agrupamento de
	 *         classes e assuntos "PEO" e orgão julgador.
	 */
	public List<ProcessoTrf> getCadeiaProcessos(){
		try {
			if(this.carregarInstanciaProcessoTrf() == null) {
				facesMessages.clear();
				facesMessages.add(Severity.INFO, "Não foi possível recuperar a cadeia de processos vinculados, pois a instância do processo está vazia.");
				return Collections.emptyList();
			}
			vinculacaoDependenciaEleitoral = vinculacaoDependenciaEleitoralManager.recuperaVinculacaoDependencia(this.processoTrf);
			cadeiaProcessos = vinculacaoDependenciaEleitoralManager.recuperarProcessosAssociadosVinculacaoDependencia(this.vinculacaoDependenciaEleitoral);
			
 		 	if (cadeiaProcessos != null) {
 		 		cadeiaProcessos.remove(this.processoTrf);
 		 		removerProcessosSigilosos();
		 	} else {
		 		cadeiaProcessos = Collections.emptyList();
		 	}
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Houve um erro ao tentar recuperar a cadeia de processos preventos pelo art. 260 do CE a qual este processo está associado. Tente novamente mais tarde ou contate o administrador.");
			logger.error("Erro ao tentar recuperar a cadeia de processos.");
		} 
		return cadeiaProcessos;
	}

	/**
	 * Remove os processos sigilosos, da cadeia de processos prevento 260, 
	 * caso o usuário não possua permissão de visualização de processos sigilosos.
	 */
	private void removerProcessosSigilosos() {
		List<ProcessoTrf> processosSigilosos = new ArrayList<ProcessoTrf>(0);
		for(ProcessoTrf processo : cadeiaProcessos) {
			if(!isPodeVisualizarProcesso(processo)) {
				processosSigilosos.add(processo);
			}
		}
		cadeiaProcessos.removeAll(processosSigilosos);
	}
	
 	/**
 	 * Verifica se o usuário tem permissão para visualizar determinado processo
 	 * na aba de processo associados 260 nos autos digitais.
 	 * 
 	 * @see ProcessoJudicialService#visivel(ProcessoTrf, br.jus.pje.nucleo.entidades.UsuarioLocalizacao, Identity)
 	 */
 	public boolean isPodeVisualizarProcesso(ProcessoTrf processoTrf) {
 		if (processoTrf == null || !processoTrf.getSegredoJustica()) {
 			return true;
 		} else {
 			return processoJudicialService.visivel(processoTrf, Authenticator.getUsuarioLocalizacaoAtual(), Identity.instance());
 		}			
 	}

	/**
	 * Verifica se o processo passado no parametro é ou não o paradigma de uma
	 * cadeia formada pela distribuição/redistribuição do art. 260 do CE
	 * 
	 * @return <code>true</code> se o processo detalhado é o paradigma e
	 *         <code>false</code> caso contrario
	 */
	public boolean isProcessoParadigma(ProcessoTrf processo){
		boolean ret = false;
		if(this.processoParadigma != null){
			ret = this.processoParadigma.equals(processo); 
		}
		return ret;
	}

	public String getIdProcesso() {
		return idProcesso;
	}

	public void setIdProcesso(String idProcesso) {
		this.idProcesso = idProcesso;
	}

	public ProcessoTrf getProcessoParadigma() {
		if(this.processoParadigma == null){
			try {
				this.processoParadigma = distribuicaoService.buscarProcessoPrevencaoEleicaoOrigemProcesso(carregarInstanciaProcessoTrf());
			} catch (PJeBusinessException e) {
				facesMessages.add(Severity.ERROR, "Houve um erro ao tentar recuperar o processo paradigma da cadeia.  Tente novamente mais tarde ou contate o administrador.");
				logger.error("Erro ao tentar recuperar o processo paradigma da cadeia.");
			}
		}
		return processoParadigma;
	}
	
	/**
 	 * Metodo que recupera o processo da instancia setada no sistema.
 	 * 
 	 * @return ProcessoTrf Processo
 	 */
 	public ProcessoTrf getProcessoParadigmaInstancia() {
 		if(this.processoParadigma == null){
 			try {
 				this.processoParadigma = distribuicaoService.buscarProcessoPrevencaoEleicaoOrigemProcesso(ProcessoTrfHome.instance().getInstance());
 			} catch (PJeBusinessException e) {
 				facesMessages.add(Severity.ERROR, "Houve um erro ao tentar recuperar o processo paradigma da cadeia.  Tente novamente mais tarde ou contate o administrador.");
 				logger.error("Erro ao tentar recuperar o processo paradigma da cadeia.");
 			}
 		}
 		return processoParadigma;
 	}
 	
 	/**
 	 * Metodo que retorna o numero do processo de referencia.
 	 * 
 	 * @return Numero do processo de referencia.
 	 */
 	public String getNumeroProcessoReferencia(){
 		String retorno = "";
 		if (ProcessoTrfHome.instance().getInstance().getDesProcReferencia() != null){
 			retorno = ProcessoTrfHome.instance().getInstance().getDesProcReferencia();
 		}
 		return retorno;
 	}
 	
 	/**
 	 * Metodo que retorna o municipio da eleicao do processo.
 	 * 
 	 * @return Nome do municipio.
 	 */
 	public String getMunicipioEleicao(){
 		String retorno = "";
 		if(ProcessoTrfHome.instance().getInstance().getComplementoJE() != null 
 				&& ProcessoTrfHome.instance().getInstance().getComplementoJE().getMunicipioEleicao() != null){
 			retorno = ProcessoTrfHome.instance().getInstance().getComplementoJE().getMunicipioEleicao().getMunicipio();
 		}
 		return retorno;
 	}
 	
 	/**
 	 * Metodo que retorna o ano da eleição do processo.
 	 * 
 	 * @return Ano da eleicao
 	 */
 	public String getAnoEleicao(){
 		String retorno = "";
 		if(ProcessoTrfHome.instance().getInstance().getComplementoJE() != null 
 				&& ProcessoTrfHome.instance().getInstance().getComplementoJE().getEleicao() != null){
 			retorno = ProcessoTrfHome.instance().getInstance().getComplementoJE().getEleicao().getAno().toString();
 		}
 		return retorno;
 	}
 	
 	/**
 	 * Metodo que retorna o nome do orgão julgado (Relator do Processo).
 	 * 
 	 * @return Numero do processo
 	 */
 	public String getNumeroProcessoParadigma(){
 		String retorno = "";
 		if (getProcessoParadigmaInstancia() != null && getProcessoParadigmaInstancia().getNumeroProcesso() != null){
 			retorno = getProcessoParadigmaInstancia().getNumeroProcesso();
 		}
 		return retorno;
 	}
 	
 	/**
 	 * Metodo que retorna o nome do orgao julgado (Relator do Processo)
 	 * 
 	 * @return Nome do orgao julgador.
 	 */
 	public String getRelatorProcessoParadigma(){
 		String retorno = "";
 		if (getProcessoParadigmaInstancia() != null && getProcessoParadigmaInstancia().getOrgaoJulgador() != null){
 			retorno = getProcessoParadigmaInstancia().getOrgaoJulgador().getOrgaoJulgador();
 		}
 		return retorno;
 	}
 	
 	/**
 	 * Metodo que retorna o nome do estado do processo.
 	 * 
 	 * @return Nome do estado.
 	 */
 	public String getEstadoProcessoParadigma(){
 		String retorno = "";
 		if (getProcessoParadigmaInstancia() != null && getProcessoParadigmaInstancia().getComplementoJE().getEstadoEleicao().getEstado() != null){
 			retorno = getProcessoParadigmaInstancia().getComplementoJE().getEstadoEleicao().getEstado();
 		}
 		return retorno;
 	}
 	
 	/**
 	 * Metodo que retorna o nome do estado do processo.
 	 * 
 	 * @return Municipio do processo.
 	 */
 	public String getMunicipioProcessoParadigma(){
 		String retorno = "";
 		if (getProcessoParadigmaInstancia() != null && getProcessoParadigmaInstancia().getComplementoJE().getEleicao().isMunicipal()){
 			retorno = getProcessoParadigmaInstancia().getComplementoJE().getMunicipioEleicao().getMunicipio();
 		}
 		return retorno;
	}
	
}
