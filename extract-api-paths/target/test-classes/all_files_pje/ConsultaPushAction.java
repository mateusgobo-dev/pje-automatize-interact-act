package br.com.infox.pje.action;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;

import br.com.infox.cliente.NumeroProcessoUtil;
import br.com.infox.cliente.component.securitytoken.SecurityTokenControler;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.com.infox.utils.Constantes;
import br.com.itx.component.UrlUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoPushManager;
import br.jus.pje.nucleo.entidades.ProcessoPush;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(ConsultaPushAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class ConsultaPushAction implements Serializable {
	private static final long serialVersionUID = -3778739003464690092L;
	public static final String NAME = "consultaPushAction";

	private List<ProcessoPush> processosCadastrados;
	private List<ProcessoTrf> processosRelacionados;
	private ProcessoPush processoPush;
	private String numeroProcesso;
	private String observacao;
	private HashMap<Integer, Boolean> processoSelectedIds = new HashMap<Integer, Boolean>(0);
	private String link;
	private boolean exibeAlert;
	private Integer idProcessoSelecionado;
	
	@In
	private ProcessoParteExpedienteManager processoParteExpedienteManager;
	
	@In
	private ProcessoPushManager processoPushManager;
	
	@In
	private ProcessoParteManager processoParteManager;
	
	@In
	private ProcessoTrfManager processoTrfManager;
	
	@In
	private Identity identity;
	
	@Logger
	private Log log;
	
	/**
	 * Método responsável por verificar a validade do número do processo pesquisado e, 
	 * caso o processo seja encontrado, incluí-lo na lista de processos do push.
	 */
	public void incluir() {
		ProcessoTrf processoTrf = processoTrfManager.recuperarProcesso(numeroProcesso, Authenticator.getPessoaLogada());
		
		if (processoTrf != null) {
			incluir(processoTrf);
		} else {
			FacesMessages.instance().add(Severity.ERROR, "Processo não encontrado.");
		}
	}

	/**
	 * Método responsável por incluir um processo na lista de processos do push.
	 * 
	 * @param processoTrf Processo.
	 */
	private void incluir(ProcessoTrf processoTrf) {
		try {
			processoPushManager.inserirNoPush(processoTrf, Authenticator.getPessoaLogada(), Authenticator.getPessoaPushLogada(), observacao);
			finalizar();
		} catch (PJeBusinessException ex) {
			FacesMessages.instance().add(Severity.ERROR, ex.getLocalizedMessage());
		}
	}

	/**
	 * Método responsável por adicionar os processos selecionados na lista de processos do push.
	 */
	public void adicionar() {
		for (Map.Entry<Integer, Boolean> entry: processoSelectedIds.entrySet()) {
			if (entry.getValue()) {
				incluir(processoTrfManager.find(ProcessoTrf.class, entry.getKey()));
			}
		}
	}

	/**
	 * Método responsável por excluir um processo da lista de processos do push.
	 * 
	 * @param processoPush {@link ProcessoPush}.
	 */
	public void excluir(ProcessoPush processoPush) {
		try {
			processoPush.setDtExclusao(Calendar.getInstance().getTime());
			this.processoPushManager.persist(processoPush);
			finalizar();
		} catch (PJeBusinessException ex) {
			FacesMessages.instance().add(Severity.ERROR, ex.getLocalizedMessage());
		}
	}
	
	/**
	 * Método responsável por atualizar o campo observação de um processo presente na lista de processos do push.
	 */
	public void atualizar() {
		try {
			this.processoPush.setDsObservacao(this.observacao);
			this.processoPushManager.persist(this.processoPush);
			finalizar();
		} catch (PJeBusinessException ex) {
			FacesMessages.instance().add(Severity.ERROR, ex.getLocalizedMessage());
		}
	}
	
	/**
	 * Método responsável por finalizar a operação de criação/atualização do objeto {@link ProcessoPush}. 
	 * 
	 * @param processoPush {@link ProcessoPush}.
	 * @throws PJeBusinessException Caso algo de errado ocorra.
	 */
	private void finalizar() throws PJeBusinessException {
		processoPushManager.flush();
		atualizarListas();
		resetarInstancia();
		FacesMessages.instance().add(Severity.INFO, "Operação concluída com sucesso.");
	}

	private void atualizarListas() {
		atualizarListaProcessosCadastrados();
		atualizarListaProcessosRelacionados();
	}

	/**
	 * Método responsável por atualizar o valor das variáveis de instância com as informações do {@link ProcessoPush} selecionado.
	 * 
	 * @param processoPush {@link ProcessoPush}.
	 */
	public void atualizarInstancia(ProcessoPush processoPush) {
		this.processoPush = processoPush;
		this.numeroProcesso = processoPush.getProcessoTrf().getNumeroProcesso();
		this.observacao = processoPush.getDsObservacao();
	}
	
	/**
	 * Método responsável por inicializar o valor das variáveis de instância.
	 */
	public void resetarInstancia() {
		this.processoPush = null;
		this.numeroProcesso = null;
		this.observacao = null;
		this.processoSelectedIds = new HashMap<Integer, Boolean>(0);
	}
	
	/**
	 * Método responsável pela lógica de acesso aos detalhes do processo.
	 */
	public void construirLogicaAcesso() {
		this.link = montarLink(this.idProcessoSelecionado);
		this.exibeAlert = exibirAlert(this.idProcessoSelecionado);
	}
	
	/**
	 * Método responsável por montar o link de acesso aos detalhes do processo.
	 * 
	 * @param idProcessoTrf Identificador do processo.
	 * @return Link de acesso aos detalhes do processo.
	 */
	private String montarLink(Integer idProcessoTrf) {
		String chave = SecurityTokenControler.instance().gerarChaveAcessoProcesso(idProcessoTrf);
		String retorno = StringUtils.EMPTY;
		if (isPessoaPush() || (Authenticator.isJusPostulandi() && !hasPermissaoVisualizarProcesso(idProcessoTrf))) {
			retorno = UrlUtil.montarLinkConsultaPublica(idProcessoTrf, chave);
		} else if (identity.hasRole("advogado") || identity.hasRole("assistAdvogado") || Authenticator.isJusPostulandi()) {
			retorno = UrlUtil.montarLinkDetalheProcesso(Constantes.URL_DETALHE_PROCESSO.PROCESSO_COMPLETO_ADVOGADO, idProcessoTrf, chave); 
		} else {
			retorno = UrlUtil.montarLinkDetalheProcesso(Constantes.URL_DETALHE_PROCESSO.PROCESSO_COMPLETO, idProcessoTrf, chave); 
		}
		return retorno;
	}
	
	/**
	 * Método responsável pela lógica de exibição do alerta da resolução CNJ n.º 121.
	 * 
	 * @param idProcessoTrf Identificador do processo.
	 * @return Verdadeiro se o usuário não faz parte do processo ou não possui expediente aberto. Falso, caso contrário.
	 */
	private boolean exibirAlert(Integer idProcessoTrf) {
		if (!isPessoaPush() && Authenticator.isUsuarioExterno()) {
			return !hasPermissaoVisualizarProcesso(idProcessoTrf);
		}
		return false;
	}

	/**
	 * Verifica se o usuário logado tem permissão para visualizar dados do processo.
	 * @param idProcessoTrf Identificador do processo.
	 * @return true se o usuário logado for uma parte no processo
	 * ou um representante ou se possuir um expediente aberto.
	 */
	private boolean hasPermissaoVisualizarProcesso(Integer idProcessoTrf) {
		ProcessoTrf processoTrf = this.processoTrfManager.find(ProcessoTrf.class, idProcessoTrf);
		
		boolean parte = this.processoParteManager.isParte(processoTrf, Authenticator.getPessoaLogada());
		
		boolean representante = this.processoParteManager.isParte(
			idProcessoTrf, Authenticator.getPessoaLogada(), 
				(Integer)Contexts.getSessionContext().get(Authenticator.ID_PROCURADORIA_ATUAL));
		
		boolean expedienteAberto = processoParteExpedienteManager.verificarExpedienteAberto(
			processoTrf, Authenticator.getPessoaLogada());
		
		return parte || representante || expedienteAberto;
	}
	
	/**
	 * Método responsável por verificar se o usuário logado é um usuário push.
	 * 
	 * @return Verdadeiro se o usuário logado é um usuário push. Falso, caso contrário. 
	 */
	public boolean isPessoaPush() {
		return Authenticator.getPessoaPushLogada() != null;
	}
	
	/**
	 * Método responsável por verificar se a instância de {@link ProcessoPush} é gerenciada.
	 * 
	 * @return Verdadeiro se a instância de {@link ProcessoPush} é diferente de NULL. Falso, caso contrário.
	 */
	public boolean isManaged() {
		return this.processoPush != null;
	}
	
	/**
	 * Método responsável por atualizar a lista de processos cadastrados no push.
	 */
	private void atualizarListaProcessosCadastrados() {
		this.processosCadastrados = this.isPessoaPush() ?  
			this.processoPushManager.recuperarProcessosPush(Authenticator.getPessoaPushLogada(), Boolean.TRUE) :
				this.processoPushManager.recuperarProcessosPush(Authenticator.getPessoaLogada(), Boolean.TRUE);
	}
	
	/**
	 * Método responsável por atualizar a lista de processos relacionados.
	 */
	private void atualizarListaProcessosRelacionados() {		
		this.processosRelacionados = this.processoTrfManager.recuperarProcessosRelacionados(Authenticator.getPessoaLogada());
	}
	
	// GETTERs AND SETTERs
	
	public List<ProcessoPush> getProcessosCadastrados() {
		if (this.processosCadastrados == null) {
			atualizarListaProcessosCadastrados();
		}
		return this.processosCadastrados;
	}
	
	public List<ProcessoTrf> getProcessosRelacionados() {
		if (this.processosRelacionados == null) {
			atualizarListaProcessosRelacionados();
		}
		return this.processosRelacionados;
	}

	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}
	
	public HashMap<Integer, Boolean> getProcessoSelectedIds() {
		return processoSelectedIds;
	}

	public void setProcessoSelectedIds(HashMap<Integer, Boolean> processoSelectedIds) {
		this.processoSelectedIds = processoSelectedIds;
	}

	public String getLink() {
		return link;
	}

	public boolean isExibeAlert() {
		return exibeAlert;
	}

	public Integer getIdProcessoSelecionado() {
		return idProcessoSelecionado;
	}

	public void setIdProcessoSelecionado(Integer idProcessoSelecionado) {
		this.idProcessoSelecionado = idProcessoSelecionado;
	}
	
}