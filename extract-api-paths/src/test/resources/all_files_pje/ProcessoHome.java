/*
 IBPM - Ferramenta de produtividade Java
 Copyright (c) 1986-2009 Infox Tecnologia da Informação Ltda.

 Este programa é software livre; você pode redistribuí-lo e/ou modificá-lo 
 sob os termos da GNU GENERAL PUBLIC LICENSE (GPL) conforme publicada pela 
 Free Software Foundation; versão 2 da Licença.
 Este programa é distribuído na expectativa de que seja útil, porém, SEM 
 NENHUMA GARANTIA; nem mesmo a garantia implícita de COMERCIABILIDADE OU 
 ADEQUAÇÃO A UMA FINALIDADE ESPECÍFICA.
 
 Consulte a GNU GPL para mais detalhes.
 Você deve ter recebido uma cópia da GNU GPL junto com este programa; se não, 
 veja em http://www.gnu.org/licenses/   
 */
package br.com.infox.ibpm.home;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;
import javax.persistence.Transient;

import org.hibernate.AssertionFailure;
import org.hibernate.SQLQuery;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.bpm.BusinessProcess;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.bpm.ProcessInstance;
import org.jboss.seam.bpm.TaskInstance;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.Strings;
import org.jbpm.taskmgmt.exe.SwimlaneInstance;

import br.com.infox.bpm.taskPage.FGPJE.TaskNamesPrimeiroGrau;
import br.com.infox.cliente.home.ProcessoDocumentoHome;
import br.com.infox.cliente.jbpm.actions.JbpmEventsHandler;
import br.com.infox.cliente.util.MimetypeUtil;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.component.tree.AbstractTreeHandler;
import br.com.infox.core.certificado.VerificaCertificado;
import br.com.infox.editor.action.DocumentoAction;
import br.com.infox.ibpm.component.tree.AutomaticEventsTreeHandler;
import br.com.infox.ibpm.component.tree.EventsHomologarMovimentosTreeHandler;
import br.com.infox.ibpm.component.tree.EventsTreeHandler;
import br.com.infox.ibpm.component.tree.IniciarFluxoTreeHandler;
import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.infox.ibpm.jbpm.assignment.LocalizacaoAssignment;
import br.com.infox.ibpm.service.AssinaturaDocumentoService;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoInstanceManager;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceManager;
import br.jus.cnj.pje.view.ProtocolarDocumentoBean;
import br.jus.csjt.pje.business.service.LancadorMovimentosService;
import br.jus.csjt.pje.business.service.TipoProcessoDocumentoService;
import br.jus.csjt.pje.view.action.TipoProcessoDocumentoAction;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.Fluxo;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCargo;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBinPessoaAssinatura;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoInstance;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SituacaoProcesso;
import br.jus.pje.nucleo.entidades.Tarefa;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.editor.ProcessoDocumentoEstruturado;
import br.jus.pje.nucleo.util.Crypto;

@Name(ProcessoHome.NAME)
@BypassInterceptors
public class ProcessoHome extends AbstractProcessoHome<Processo> {
	private static final String LAST_ID_PROCESSO_DOCUMENTO = "lastIdProcessoDocumento";
	private static final String LAST_ID_TASK_INSTANCE = "lastIdTaskInstance";
	public static final String NAME = "processoHome";

	public static final String EVENT_ATUALIZAR_PROCESSO_DOCUMENTO_FLUXO = "atualizarProcessoDocumentoFluxo";
	public static final String AFTER_UPDATE_PD_FLUXO_EVENT = "afterUpdatePdFluxoEvent";

	private static final LogProvider log = Logging.getLogProvider(ProcessoHome.class);

	private static final long serialVersionUID = 1L;

	private ModeloDocumento modeloDocumento;
	private TipoProcessoDocumento tipoProcessoDocumento;
	private ProcessoDocumentoBin processoDocumentoBin = new ProcessoDocumentoBin();
	private String observacaoMovimentacao;
	private boolean iniciaExterno;
	private String signature;
	private String certChain;
	private Integer idAgrupamentos;
	private boolean renderEventsTree;
	private ProcessoDocumento pdFluxo;
	private Integer idProcessoDocumento;
	private boolean showComponentesFluxo = Boolean.TRUE;

	private long taskId;
	private long taskInstanceId;
	
	private transient ProtocolarDocumentoBean protocolarDocumentoBean;
		
	public void iniciarNovoFluxo() {
		limpar();
		Redirect redirect = Redirect.instance();
		redirect.setViewId("/Processo/movimentar.xhtml");
		redirect.execute();

	}

	public void limpar() {
		modeloDocumento = null;
		tipoProcessoDocumento = null;
		newInstance();
	}

	@Observer("processoHomeSetId")
	@Override
	public void setId(Object id) {
		super.setId(id);
	}

	public void setModeloDocumentoCombo(ModeloDocumento modeloDocumentoCombo) {
		this.modeloDocumento = modeloDocumentoCombo;
	}

	public ModeloDocumento getModeloDocumentoCombo() {
		return modeloDocumento;
	}

	// [PJEII-4481] - Para viabilizar tipo de documento com default quando há uma única opção
	@Transient
	public TipoProcessoDocumento getTipoProcessoDocumentoComDefault() {
		TipoProcessoDocumento tipoProcessoDocumentoLocal = this.getTipoProcessoDocumento();
		if( tipoProcessoDocumentoLocal==null ) {
			TipoProcessoDocumentoService servico = 
					ComponentUtil.getComponent(TipoProcessoDocumentoService.NAME);			
			List<TipoProcessoDocumento> lista = servico.getTipoDocumentoItems(Variaveis.MINUTA_EM_ELABORACAO);
			if ( lista.size()==1 ) {
				tipoProcessoDocumentoLocal = lista.get(0);
			}
		}		
		return tipoProcessoDocumentoLocal;
	}
	
	// [PJEII-4481] - Para viabilizar tipo de documento com default quando há uma única opção
	@Transient
	public void setTipoProcessoDocumentoComDefault(TipoProcessoDocumento tipoProcessoDocumentoLocal) {
		this.setTipoProcessoDocumento(tipoProcessoDocumentoLocal);
	}

	public TipoProcessoDocumento getTipoProcessoDocumento() {
		return tipoProcessoDocumento;
	}

	public void setTipoProcessoDocumento(TipoProcessoDocumento tipoProcessoDocumento) {
		this.tipoProcessoDocumento = tipoProcessoDocumento;
	}

	public String getObservacaoMovimentacao() {
		return observacaoMovimentacao;
	}

	public void setObservacaoMovimentacao(String observacaoMovimentacao) {
		this.observacaoMovimentacao = observacaoMovimentacao;
	}

	public void setProcessoDocumentoFaseAtual(ProcessoDocumento processoDocumentoFaseAtual) {
	}

	public void setProcessoDocumentoFaseAnterior(ProcessoDocumento processoDocumentoFaseAtual) {
	}

	public void adicionarFluxo(Fluxo fluxo, Map<String, Object> variaveis) {
		iniciarProcessoJbpm(fluxo, variaveis);
	}

	public void iniciarProcessoJbpm(Fluxo fluxo, Map<String, Object> variaveis) {
		BusinessProcess.instance().createProcess(fluxo.getFluxo().trim());
		instance.setFluxo(fluxo);
		instance.setIdJbpm(BusinessProcess.instance().getProcessId());
		update();
		insereProcessoInstance(instance.getIdProcesso(), BusinessProcess.instance().getProcessId());
		// grava a variavel processo no jbpm com o numero do processo e-pa
		org.jbpm.graph.exe.ProcessInstance processInstance = ProcessInstance.instance();
		processInstance.getContextInstance().setVariable(Variaveis.VARIAVEL_PROCESSO, instance.getIdProcesso());
		if (variaveis != null) {
			for (Entry<String, Object> entry : variaveis.entrySet()) {
				processInstance.getContextInstance().setVariable(entry.getKey(), entry.getValue());
			}
		}

		// inicia a primeira tarefa do processo
		Collection<org.jbpm.taskmgmt.exe.TaskInstance> taskInstances = processInstance.getTaskMgmtInstance()
				.getTaskInstances();
		if (taskInstances != null && !taskInstances.isEmpty()) {
			BusinessProcess.instance().setTaskId(taskInstances.iterator().next().getId());
			BusinessProcess.instance().startTask();
		}
	}

	/**
	 * Método responsável por criar uma nova instância do fluxo. Este método
	 * recebe como parâmetros as informações de deslocamento do fluxo para que
	 * ele seja criado deslocado.
	 * 
	 * @param idProcesso
	 *            id do {@link ProcessoTrf} para deslocamento
	 * @param idProcessInstance
	 *            id do {@link org.jbpm.graph.exe.ProcessInstance} para criação
	 *            do {@link ProcessInstance}
	 * @param idLocalizacao
	 *            id do {@link Localizacao} para deslocamento. Pode ser nulo
	 *            para não deslocar.
	 * @param idOrgaoJulgadorColegiado
	 *            id do {@link OrgaoJulgadorColegiado} para deslocamento. Pode
	 *            ser nulo para não deslocar.
	 * @param idOrgaoJulgadorCargo
	 *            id do {@link OrgaoJulgadorCargo} para deslocamento. Pode ser
	 *            nulo para não deslocar.
	 * @return o nome {@link ProcessInstance} criado.
	 */
	public static ProcessoInstance insereProcessoInstance(
			Integer idProcesso, Long idProcessInstance,
			Integer idLocalizacao, Integer idOrgaoJulgadorColegiado,
			Integer idOrgaoJulgadorCargo) {
		
		ProcessoInstanceManager manager = (ProcessoInstanceManager) Component.getInstance(ProcessoInstanceManager.class);
		try {
			ProcessoInstance pi = manager.findById(idProcessInstance);
			if(pi!=null){
				return pi;
			}
		} catch (PJeBusinessException e) {
			//swallow
		}
		
		ProcessoInstance prInst = new ProcessoInstance();
		
		prInst.setIdProcesso(idProcesso);
		prInst.setIdProcessoInstance(idProcessInstance);	
		prInst.setIdLocalizacao(idLocalizacao);
		prInst.setOrgaoJulgadorColegiado(idOrgaoJulgadorColegiado);
		prInst.setOrgaoJulgadorCargo(idOrgaoJulgadorCargo);
		
		EntityUtil.getEntityManager().persist(prInst);
		return prInst;
	}
	
	/**
	 * Método responsável por criar uma nova instância do fluxo. 
	 * 
	 * @param idProcesso
	 *            id do {@link ProcessoTrf} para deslocamento
	 * @param idProcessInstance
	 *            id do {@link org.jbpm.graph.exe.ProcessInstance} para criação
	 *            do {@link ProcessInstance}
	 * @return o nome {@link ProcessInstance} criado.
	 */
	public static ProcessoInstance insereProcessoInstance(Integer idProcesso,
			Long idProcessInstance) {
	
		return insereProcessoInstance(idProcesso, idProcessInstance, null,
				null, null);
	}

	public Processo criarProcesso() {
		String numProcessoTemp = instance.getNumeroProcessoTemp();
		newInstance();
		instance.setUsuarioCadastroProcesso(getUsuarioLogado());
		instance.setDataInicio(new Date());
		instance.setNumeroProcesso(null);
		instance.setNumeroProcessoTemp(numProcessoTemp);
		getEntityManager().persist(instance);

		return instance;
	}

	public Processo iniciarProcesso(Fluxo fluxo) {
		BusinessProcess.instance().createProcess(fluxo.getFluxo().trim());
		Date dataCadastro = new Date();
		newInstance();
		Usuario usuario = getUsuarioLogado();
		instance.setUsuarioCadastroProcesso(usuario);
		instance.setFluxo(fluxo);
		instance.setDataInicio(dataCadastro);
		instance.setIdJbpm(BusinessProcess.instance().getProcessId());
		instance.setNumeroProcesso(null);
		persist();
		// grava a variavel processo no jbpm com o numero do processo e-pa
		org.jbpm.graph.exe.ProcessInstance processInstance = ProcessInstance.instance();
		processInstance.getContextInstance().setVariable(Variaveis.VARIAVEL_PROCESSO, instance.getIdProcesso());
		instance.setNumeroProcesso(Integer.toString(instance.getIdProcesso()));
		update();
		// inicia a primeira tarefa do processo
		Collection<org.jbpm.taskmgmt.exe.TaskInstance> taskInstances = processInstance.getTaskMgmtInstance()
				.getTaskInstances();
		if (taskInstances != null && !taskInstances.isEmpty()) {
			BusinessProcess.instance().setTaskId(taskInstances.iterator().next().getId());
			BusinessProcess.instance().startTask();
		}
		FacesMessages.instance().clear();
		FacesMessages.instance().add(StatusMessage.Severity.INFO,
				"Processo #{processoHome.instance.idProcesso} iniciado com sucesso");

		SwimlaneInstance swimlaneInstance = TaskInstance.instance().getSwimlaneInstance();
		String actorsExpression = swimlaneInstance.getSwimlane().getPooledActorsExpression();
		Set<String> pooledActors = LocalizacaoAssignment.instance().getPooledActors(actorsExpression);
		String[] actorIds = pooledActors.toArray(new String[pooledActors.size()]);
		swimlaneInstance.setPooledActors(actorIds);
		return instance;
	}

	public Usuario getUsuarioLogado() {
		Usuario usuario = (Usuario) Contexts.getSessionContext().get("usuarioLogado");
		usuario = getEntityManager().find(usuario.getClass(), usuario.getIdUsuario());
		return usuario;
	}

	public void iniciarTarefaProcesso() {
		JbpmEventsHandler.instance().iniciarTask(instance);
	}

	public Boolean acessarFluxo() {
		if (getInstance().getActorId() == null
				|| getInstance().getActorId().equals(Authenticator.getUsuarioLogado().getLogin())) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}

	public long getTaskId() {
		return taskId;
	}

	public String mostraProcesso(int id, String destino) {
		setId(id);
		return destino;
	}

	public int getMovimentacaoInicial() {
		return 0;
	}

	public boolean isIniciaExterno() {
		return iniciaExterno;
	}

	public void setIniciaExterno(boolean iniciaExterno) {
		this.iniciaExterno = iniciaExterno;
	}

	public void iniciaExterno(String viewId) {
		iniciaExterno = true;
		Redirect r = Redirect.instance();
		r.setViewId(viewId);
		setTab("tabParticipante");
		r.execute();

	}

	public String getNumeroProcesso(int idProcesso) {
		String ret = idProcesso + "";
		Processo processo = EntityUtil.find(Processo.class, idProcesso);
		if (processo != null) {
			ret = processo.getNumeroProcesso();
		}
		return ret;
	}

	/**
	 * Pega o TUA selecionado na tree e inicia o processo
	 */
	@Observer("fluxoSelecionadoProcesso")
	public void iniciaTuaProcessoInterno() {
		Fluxo fluxo = getTreeHandlerInterno().getSelected().getFluxo();
		iniciarProcesso(fluxo);
		Redirect redirect = Redirect.instance();
		redirect.setViewId("/Processo/movimentar.xhtml");
		redirect.execute();
	}

	private IniciarFluxoTreeHandler getTreeHandlerInterno() {
		return getComponent("iniciarFluxoTreeView");
	}

	public static ProcessoHome instance() {
		return ComponentUtil.getComponent(NAME);
	}

	public void atualizarProcessoEvento(ProcessoDocumento pd) {
		String sql = "select max(o.idProcessoEvento) from ProcessoEvento o";
		Query q = EntityUtil.getEntityManager().createQuery(sql);

		Integer resultado = (Integer) q.getSingleResult();

		ProcessoEvento processoEvento = EntityUtil.find(ProcessoEvento.class, resultado);
		processoEvento.setProcessoDocumento(pd);
		getEntityManager().merge(processoEvento);
		EntityUtil.flush();
	}

	public Integer salvarProcessoDocumentoFluxo(String value, Integer idDoc, Boolean assinado, String label,Long taskId) {
		setTaskInstanceId(taskId);
		try {
			return salvarProcessoDocumentoFluxo(value, idDoc, assinado, label);
		} catch (PJeBusinessException e) {
			e.printStackTrace();
			FacesMessages.instance().add(Severity.ERROR, "Erro ao gravar o documento: {0}", e.getLocalizedMessage());
			return 0;
		}
	}
	
	private Integer salvarProcessoDocumentoFluxo(String value, Integer idDoc, Boolean assinado, String label) throws PJeBusinessException {
		Integer result = 0;
		ProcessoDocumentoManager processoDocumentoManager = (ProcessoDocumentoManager) Component.getInstance("processoDocumentoManager");
		ProcessoDocumento processoDocumento = null;
		if(idDoc != null){
			processoDocumento = processoDocumentoManager.findById(idDoc);
		}
		ProcessoHome.instance().setIdProcessoDocumento(idDoc);
		if (processoDocumento == null) {
			result = inserirProcessoDocumentoFluxo(value, label, assinado);
			if (result == 0) {
				// Erro ao inserir o documento
				return result;
			}
			processoDocumento = processoDocumentoManager.findById(result);
		} else {
			AssinaturaDocumentoService assinaturaDocumentoService = ComponentUtil.getComponent(AssinaturaDocumentoService.NAME);
			if (assinaturaDocumentoService.isDocumentoAssinado(idDoc)) {
				result = inserirProcessoDocumentoFluxo(value, label, assinado);
			}
			if (result == 0) {
				result = idDoc;
				atualizarProcessoDocumentoFluxo(value, idDoc, assinado);
			}
		}
		ProtocolarDocumentoBean pdb = getProtocolarDocumentoBean(assinado);
        if (!pdb.getArquivos().isEmpty()){
            Integer hash = processoDocumento.getDocumentosVinculados().hashCode();
            for (ProcessoDocumento pd : pdb.getArquivos()) {
                if (!processoDocumento.getDocumentosVinculados().contains(pd)){
                    pd.setDocumentoPrincipal(processoDocumento);
                    processoDocumento.getDocumentosVinculados().add(pd);
                }
            }
            if (processoDocumento.getDocumentosVinculados().hashCode() != hash){
                EntityUtil.getEntityManager().flush();
            }
        }       

		setIdProcessoDocumento(result);
		FacesMessages.instance().add(StatusMessage.Severity.INFO, "Registro gravado com sucesso!");
		Events.instance().raiseEvent(AFTER_UPDATE_PD_FLUXO_EVENT, idProcessoDocumento);
		return result;
	}
	

	public Integer salvarProcessoDocumentoFluxoJaAssinado(String value, Integer idDoc, Boolean assinado, String label, Long taskId) 
	{
		setTaskInstanceId(taskId);
		Integer result = 0;

		ProcessoDocumento processoDocumento = EntityUtil.find(ProcessoDocumento.class, idDoc);
		ProcessoHome.instance().setIdProcessoDocumento(idDoc);
		if (processoDocumento == null) {
			result = inserirProcessoDocumentoFluxoAssinatura(value, label, assinado);
			setIdProcessoDocumento(result);
			
			if (result == 0) {
				return result;
			}
		} 
		else 
		{
			getProcessoDocumentoBin().setModeloDocumento(String.valueOf(value));
			atualizarProcessoDocumentoFluxoEditorTexto(value, idDoc, label, assinado);
		}
		FacesMessages.instance().add(StatusMessage.Severity.INFO, "Registro gravado com sucesso!");
		Events.instance().raiseEvent(AFTER_UPDATE_PD_FLUXO_EVENT, idProcessoDocumento);
		return result;
	}

	public Boolean verificarPessoaAssinatura(ProcessoDocumento pd) {
		String sql = "select count(o.idProcessoDocumentoBinPessoaAssinatura) "
				     + " from ProcessoDocumentoBinPessoaAssinatura o " 
				     + " where o.processoDocumentoBin = :pdb";
		Query q = getEntityManager().createQuery(sql);
		q.setParameter("pdb", pd.getProcessoDocumentoBin());

		Long count = 0L;
		try {
			count = (Long) q.getSingleResult();			
		} catch(NoResultException ex) {
			return false;
		} catch (AssertionFailure e){
			/*
			 * PJE-JT - Daniel Rocha
			 * Esta exceção pode ser lançada no momento de assinatura do despacho no nó "Análise do Despacho ou Decisão".
			 * Apesar desta exceção não estar impactando diretamente no problema da issues PJEII-2866 e PJEII-3185, verifiquei
			 * que acontecia algo semelhante no problema da issue PJEII-1954, então trouxe a solução para este 
			 * trecho de código.
			 */			
			getEntityManager().flush();
			count = (Long) q.getSingleResult();
		}
		return count.compareTo(0L) > 0;
	}

	public Boolean isDocumentoAssinado(Integer idDoc) {
		ProcessoDocumento processoDocumento = EntityUtil.find(ProcessoDocumento.class, idDoc);
		if (processoDocumento != null) {
			if (verificarPessoaAssinatura(processoDocumento)) {
				return Boolean.TRUE;
			} else {
				if (processoDocumento.getProcessoDocumentoBin().getSignatarios().isEmpty()) {
					return Boolean.FALSE;
				} else {
					return Boolean.TRUE;
				}
			}
		}
		return Boolean.FALSE;
	}

	public void atualizarProcessoDocumentoFluxoEditorTexto(Object value, Integer idDoc, String label, Boolean assinado) 
	{
 
		ProcessoDocumento processoDocumento = EntityUtil.find(ProcessoDocumento.class, idDoc);
		processoDocumento.setProcessoDocumento(label);
		getEntityManager().merge(processoDocumento);
		atualizarProcessoDocumentoFluxo(value, idDoc, assinado);
	}

	
	// Método para Atualizar o documento do fluxo
	public void atualizarProcessoDocumentoFluxo(Object value, Integer idDoc, Boolean assinado) {
		if (assinado) {
			try {
				verificaCertificadoUsuarioLogado(certChain, getUsuarioLogado());
			} catch (Exception e1) {
				FacesMessages.instance().add(Severity.ERROR, "Erro ao verificar certificado: " + e1.getMessage());
				return;
			}
		}
		ProcessoDocumento processoDocumento = EntityUtil.find(ProcessoDocumento.class, idDoc);
		if(getTaskInstanceId() != 0){
			processoDocumento.setIdJbpmTask(getTaskInstanceId());
			processoDocumento.setExclusivoAtividadeEspecifica(Boolean.TRUE);
		}

		processoDocumento.setUsuarioAlteracao(Authenticator.getUsuarioLogado());
		processoDocumento.setDataAlteracao(new Date());
		
		ProcessoDocumentoBin processoDocumentoBin = processoDocumento.getProcessoDocumentoBin();
		String modeloDocumentoFluxo = processoDocumentoBin.getModeloDocumento(); 
		
		if (value == null) {
			value = modeloDocumentoFluxo;
			if (value == null) {
				value = new String();
			}
		}
		String modeloDocumento = String.valueOf(value);

		if (!Strings.isEmpty(modeloDocumentoFluxo) && !modeloDocumento.equalsIgnoreCase(modeloDocumentoFluxo)) {
			modeloDocumento = modeloDocumentoFluxo;
		}

		if (Strings.isEmpty(modeloDocumento)) {
			modeloDocumento = " ";
		}
		processoDocumentoBin.setModeloDocumento(modeloDocumento);
		processoDocumentoBin.setCertChain(certChain);
		processoDocumentoBin.setSignature(signature);

		ProcessoDocumentoBinPessoaAssinatura pa = new ProcessoDocumentoBinPessoaAssinatura();

		if (!(Strings.isEmpty(certChain) || Strings.isEmpty(signature))) {
			pa.setAssinatura(processoDocumentoBin.getSignature());
			pa.setCertChain(processoDocumentoBin.getCertChain());
			pa.setDataAssinatura(new Date());
			pa.setNomePessoa(processoDocumento.getUsuarioInclusao().getNome());
			pa.setPessoa(EntityUtil.find(Pessoa.class, processoDocumento.getUsuarioInclusao().getIdUsuario()));
			pa.setProcessoDocumentoBin(processoDocumentoBin);
			// define o papel do documento como o do responssavel pela
			// assinatura
			for (ProcessoDocumento pd : processoDocumentoBin.getProcessoDocumentoList()) {
				pd.setPapel(Authenticator.getPapelAtual());
				EntityUtil.getEntityManager().merge(pd);
			}
			EntityUtil.getEntityManager().flush();
		}

		/*
		 * Se o tipoProcessoDocumento for nulo (caso o componente utilizado seja
		 * o editor sem assinatura digital, o tipoProcessoDOcumento será setado
		 * automaticamente com um valor aleatorio
		 */
		if (tipoProcessoDocumento == null) {
			tipoProcessoDocumento = getTipoProcessoDocumentoFluxo();
		}
		processoDocumento.setTipoProcessoDocumento(tipoProcessoDocumento);

		try {
			getEntityManager().merge(processoDocumento);
			if (!Strings.isEmpty(pa.getAssinatura())) {
				getEntityManager().persist(pa);
			}
			getEntityManager().flush();
			setIdProcessoDocumento(processoDocumento.getIdProcessoDocumento());
		} catch (AssertionFailure e) {
			e.printStackTrace();
		}
		try {
			getEntityManager().merge(processoDocumentoBin);
			getEntityManager().flush();
			if(assinado) {
				try {
					Events.instance().raiseEvent(EVENT_ATUALIZAR_PROCESSO_DOCUMENTO_FLUXO, processoDocumentoBin);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (AssertionFailure e) {
			e.printStackTrace();
		}
	}

	public void verificaCertificadoUsuarioLogado(String certChainBase64Encoded, Usuario usuarioLogado) throws Exception {
		if (!VerificaCertificado.instance().isModoTesteCertificado()) {
			if (Strings.isEmpty(usuarioLogado.getCertChain())) {
				limparAssinatura();
				throw new Exception("O cadastro do usuário não está assinado.");
			}
			if (!usuarioLogado.checkCertChain(certChainBase64Encoded)) {
				limparAssinatura();
				throw new Exception("O certificado não é o mesmo do cadastro do usuario");
			}
		}
	}

	private Integer inserirProcessoDocumentoFluxo(String txt, String label, Boolean assinado) {
		Usuario usuarioLogado = getUsuarioLogado();
		DocumentoJudicialService documentoJudicialService = (DocumentoJudicialService) Component.getInstance(DocumentoJudicialService.class);
		ProcessoTrfManager processoTrfManager = (ProcessoTrfManager) Component.getInstance(ProcessoTrfManager.class);
		ProcessoTrf processoTrf = processoTrfManager.find(ProcessoTrf.class, getId());
		
		if (assinado) {
			try {
				verificaCertificadoUsuarioLogado(certChain, usuarioLogado);
			} catch (Exception e1) {
				FacesMessages.instance().add(Severity.ERROR, "Erro ao verificar certificado: " + e1.getMessage());
				return 0;
			}
		}
		ProcessoDocumento doc = documentoJudicialService.getDocumento();
		doc.setProcessoTrf(processoTrf);
		doc.setDataInclusao(new Date());
		doc.setDataAlteracao(doc.getDataInclusao());
		doc.setProcesso(getInstance());
		doc.setTipoProcessoDocumento(tipoProcessoDocumento == null ? getTipoProcessoDocumentoFluxo() : tipoProcessoDocumento);
		doc.setProcessoDocumento(label == null ? doc.getTipoProcessoDocumento().getTipoProcessoDocumento() : label);
		doc.setUsuarioInclusao(usuarioLogado);
		doc.setUsuarioAlteracao(usuarioLogado);
		if(getTaskInstanceId() != 0){
			doc.setIdJbpmTask(getTaskInstanceId());
			doc.setExclusivoAtividadeEspecifica(Boolean.TRUE);
		}

		ProcessoDocumentoBin conteudo = doc.getProcessoDocumentoBin();
		if (txt == null) {
			txt = processoDocumentoBin.getModeloDocumento() == null ? " " : processoDocumentoBin.getModeloDocumento();
		}
		conteudo.setModeloDocumento(txt);
		conteudo.setCertChain(certChain);
		conteudo.setSignature(signature);
		conteudo.setExtensao(MimetypeUtil.getMimetypeHtml());
		try {
			if(assinado) {
				documentoJudicialService.finalizaDocumento(doc, processoTrf, doc.getIdJbpmTask(), true);
			}
			else {
				documentoJudicialService.persist(doc, true);
			}
		} catch (Exception e) {
			log.error("Erro ao finalizar Documento", e);
			FacesMessages.instance().add(Severity.ERROR, "Erro ao finalizar Documento: " + e.getMessage());
			
		}
		
		return doc.getIdProcessoDocumento();
	}
	
	/**
	 *  
	 * [PJEII-4997] - Inserir documento e assina em modo teste apenas se o parametro passado for verdadeiro 
	 * 
	 * @param value
	 * @param label
	 * @param assinado
	 * @return
	 */
	public Integer inserirProcessoDocumentoFluxoAssinatura(Object value, String label, Boolean assinado) {
		Usuario usuarioLogado = getUsuarioLogado();
		if (assinado) {
			try {
				verificaCertificadoUsuarioLogado(certChain, usuarioLogado);
			} catch (Exception e1) {
				FacesMessages.instance().add(Severity.ERROR, "Erro ao verificar certificado: " + e1.getMessage());
				return 0;
			}
		}

		ProcessoDocumentoBin bin = new ProcessoDocumentoBin();
		ProcessoDocumento doc = new ProcessoDocumento();
		if (value == null) {
			value = processoDocumentoBin.getModeloDocumento();
			if (value == null) {
				value = new String();
			}
		}
		String modeloDocumento = String.valueOf(value);
		if (Strings.isEmpty(modeloDocumento)) {
			modeloDocumento = " ";
		}
		bin.setModeloDocumento(modeloDocumento);
		bin.setDataInclusao(new Date());
		bin.setUsuario(usuarioLogado);
		bin.setCertChain(certChain);
		bin.setSignature(signature);

		doc.setProcessoDocumentoBin(bin);
		if(getTaskInstanceId() != 0){
			doc.setIdJbpmTask(getTaskInstanceId());
			doc.setExclusivoAtividadeEspecifica(Boolean.TRUE);
		}
		doc.setAtivo(Boolean.TRUE);
		doc.setDataInclusao(new Date());
		doc.setUsuarioInclusao(usuarioLogado);
		doc.setProcesso(getInstance());
		doc.setProcessoDocumento(label);
 
		if (tipoProcessoDocumento == null) {
			tipoProcessoDocumento = getTipoProcessoDocumentoFluxo();
		}
		
		doc.setTipoProcessoDocumento(tipoProcessoDocumento);
		
		DocumentoJudicialService documentoJudicialService = (DocumentoJudicialService) Component.getInstance(DocumentoJudicialService.class);
		
		ProcessoTrfManager processoTrfManager = (ProcessoTrfManager) Component.getInstance(ProcessoTrfManager.class);
		
		ProcessoTrf processoTrf = processoTrfManager.find(ProcessoTrf.class, getId());
		
		
		try {
			documentoJudicialService.finalizaDocumentoAssinatura(doc, processoTrf, doc.getIdJbpmTask(), true, assinado);
		} catch (Exception e) {
			log.error("Erro ao finalizar Documento", e);
			FacesMessages.instance().add(Severity.ERROR, "Erro ao finalizar Documento: " + e.getMessage());
			
		}
		
		return doc.getIdProcessoDocumento();
	}

	private void limparAssinatura() {
		certChain = null;
		signature = null;
	}

	public void carregarDadosFluxo(Integer id) {
		Query query = getEntityManager().createQuery("select o from ProcessoDocumentoEstruturado o where o.processoDocumentoTrfLocal.processoDocumento.idProcessoDocumento = :idProcessoDocumento")
						  			    .setParameter("idProcessoDocumento", id);
		
		ProcessoDocumentoEstruturado documentoEstruturado = EntityUtil.getSingleResult(query);
		if(documentoEstruturado != null){
			carregarDocumentoEstruturadoFluxo(documentoEstruturado);
		}else{
			carregarDocumentoFluxo(id);
		}
	}

	private void carregarDocumentoEstruturadoFluxo(ProcessoDocumentoEstruturado documentoEstruturado) {
		DocumentoAction documentoAction = ComponentUtil.getComponent(DocumentoAction.NAME);
		documentoAction.setIdDocumento(documentoEstruturado.getIdProcessoDocumentoEstruturado());
		tipoProcessoDocumento = documentoEstruturado.getProcessoDocumento().getTipoProcessoDocumento();
		ProcessoDocumentoHome.instance().setInstance(documentoEstruturado.getProcessoDocumento());
		ProcessoHome.instance().setIdProcessoDocumento(documentoEstruturado.getProcessoDocumento().getIdProcessoDocumento());
		processoDocumentoBin = documentoEstruturado.getProcessoDocumento().getProcessoDocumentoBin();
		setPdFluxo(documentoEstruturado.getProcessoDocumento());
		onSelectProcessoDocumento();
	}

	private void carregarDocumentoFluxo(Integer id) {
		ProcessoDocumento processoDocumento = EntityUtil.find(ProcessoDocumento.class, id);
		if (processoDocumento != null) {
			setPdFluxo(processoDocumento);
			processoDocumentoBin = processoDocumento.getProcessoDocumentoBin();
			ProcessoDocumentoHome.instance().setInstance(processoDocumento);
			ProcessoHome.instance().setIdProcessoDocumento(processoDocumento.getIdProcessoDocumento());
			setTipoProcessoDocumento(processoDocumento.getTipoProcessoDocumento());
			onSelectProcessoDocumento();
			//se houver arquivos anexados ainda não persistidos, adicioná-los à lista para exibição em tela. 
	        if (processoDocumento.getDocumentosVinculados() != null && !processoDocumento.getDocumentosVinculados().isEmpty()){
	            getProtocolarDocumentoBean().getArquivos().clear();
	            getProtocolarDocumentoBean().getArquivos().addAll(processoDocumento.getDocumentosVinculados());
	        }
	        try {
				TipoProcessoDocumentoAction.instance().getModelosDisponiveis(null, processoDocumento.getTipoProcessoDocumento());
			} catch (Exception e) {
				log.error("Não foi possível recuperar os modelos de documentos", e);
			}
		}
	}

	public TipoProcessoDocumento getTipoProcessoDocumentoFluxo() {
		String sql = "select o from TipoProcessoDocumento o ";
		Query q = EntityUtil.getEntityManager().createQuery(sql);
		return (TipoProcessoDocumento) q.getResultList().get(0);
	}

	public Boolean isSigned() {
		Boolean faltaAssinatura = Boolean.FALSE;
		Boolean somenteMagistrado = Boolean.FALSE;
		List<ProcessoDocumento> lista = getInstance().getProcessoDocumentoList();
		for (ProcessoDocumento procDocList : lista) {
			if (procDocList.getTipoProcessoDocumento().getTipoProcessoDocumento().equals("Sentença")
					|| procDocList.getTipoProcessoDocumento().getTipoProcessoDocumento().equals("Despacho")
					|| procDocList.getTipoProcessoDocumento().getTipoProcessoDocumento().equals("Decisão")) {
				if (procDocList.getProcessoDocumentoBin().getSignatarios().isEmpty()) {
					faltaAssinatura = Boolean.TRUE;
				} else {
					Boolean o = getPessoaAssinatura(procDocList.getProcessoDocumentoBin());
					if (!o) {
						somenteMagistrado = Boolean.TRUE;
					}
				}
			}
		}
		if (somenteMagistrado) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR,
					"Todos os Documentos devem estar assinados digitalmente por Magistrado.");
		}
		return faltaAssinatura;
	}

	public Boolean getPessoaAssinatura(ProcessoDocumentoBin processoDocumentoBin) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT count(pa.pessoa) as pessoa ");
		sb.append("FROM ProcessoDocumentoBinPessoaAssinatura pa ");
		sb.append("WHERE pa.processoDocumentoBin = :processoDocumentoBin ");
		sb.append("AND exists(select o.idUsuario from PessoaMagistrado o) ");
		javax.persistence.Query query = EntityUtil.getEntityManager().createQuery(sb.toString());
		query.setParameter("processoDocumentoBin", processoDocumentoBin);
		try {
			Long retorno = (Long) query.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

	/*
	 * Varre toda a entidade variableInstance, e caso o registro seja do tipo
	 * textEditor e tenha valor na stringInstance, pega esse valor, cria um
	 * documento, seta como null a string e seta o longInstance do o id do
	 * ProcessoDocumento criado.
	 */
	@SuppressWarnings("unchecked")
	public void getListaVariable() {
		// Lista de todos os objetos que sao textEditor
		String sqlListTextEditor = "select o.id from org.jbpm.context.exe.VariableInstance o "
				+ "where o.name like 'textEdit%'";
		org.hibernate.Query q0 = JbpmUtil.getJbpmSession().createQuery(sqlListTextEditor);
		List<Long> lte = q0.list();

		// Verificando quais items dessa lista que estao com o valor da string
		// != null
		List<Long> les = new ArrayList<Long>(0);
		for (Long longId : lte) {
			String sqlListEditorString = "select o.id from org.jbpm.context.exe.variableinstance.StringInstance o "
					+ "where o.id = :id and cast(o.value as string) != ''";
			org.hibernate.Query q = JbpmUtil.getJbpmSession().createQuery(sqlListEditorString);
			q.setParameter("id", longId);
			if (q.list().size() > 0) {
				les.add((Long) q.list().get(0));
			}
		}

		/*
		 * Pega cada registro da variableInstance, verifica o processInstance e
		 * procura o registro com o name processo em q o processInstance seja
		 * igual, para pegar o numero do processo.
		 */
		for (Long longId : les) {
			// Pega o objeto do registro
			String sqlVar = "select o from org.jbpm.context.exe.VariableInstance o " + "where o.id = :id";
			org.hibernate.Query q = JbpmUtil.getJbpmSession().createQuery(sqlVar);
			q.setParameter("id", longId);

			// Pega o variable instance do registro TextEditor q tenha valor na
			// string.
			String pi = "select o.processInstance.id from org.jbpm.context.exe.VariableInstance o "
					+ "where o.id = :id";
			org.hibernate.Query q1 = JbpmUtil.getJbpmSession().createQuery(pi);
			q1.setParameter("id", longId);
			Long resultado = (Long) q1.list().get(0);

			// Pega o id do registro que guarda o numero do processo.
			String np = "select o.id from org.jbpm.context.exe.VariableInstance o "
					+ "where o.processInstance.id = :processInstanceId and o.name like 'processo'";
			org.hibernate.Query q2 = JbpmUtil.getJbpmSession().createQuery(np);
			q2.setParameter("processInstanceId", resultado);
			Long process = (Long) q2.list().get(0);

			// Pega o valor da coluna Long do registro achado anteriormente.
			String processo = "select o.value from org.jbpm.context.exe.variableinstance.LongInstance o "
					+ "where o.id = :id";
			org.hibernate.Query q3 = JbpmUtil.getJbpmSession().createQuery(processo);
			q3.setParameter("id", process);
			long numeroProcesso = (Long) q3.list().get(0);

			int numeroProcessoInt = (int) numeroProcesso;
			// Seta a instancia com o processo achado.
			Processo instanceProcesso = EntityUtil.find(Processo.class, numeroProcessoInt);
			setInstance(instanceProcesso);

			// Pegando o valor do registro para criar um processo documento.
			String doc = "select o.value from org.jbpm.context.exe.variableinstance.StringInstance o "
					+ "where o.id = :id";
			org.hibernate.Query q4 = JbpmUtil.getJbpmSession().createQuery(doc);
			q4.setParameter("id", longId);

			String value = (String) q4.list().get(0);
			int id = inserirProcessoDocumentoFluxo(value, "Certidão", false);

			StringBuilder sba = new StringBuilder();
			sba.append("update jbpm_variableinstance ");
			sba.append("set stringvalue_ = null , longvalue_ = ").append((long) id);
			sba.append(" where id_ = ").append(longId);
			SQLQuery updateString = JbpmUtil.getJbpmSession().createSQLQuery(sba.toString())
					.addSynchronizedQuerySpace("jbpm_variableinstance");
			updateString.executeUpdate();
		}
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getSignature() {
		return signature;
	}

	public void setCertChain(String certChain) {
		this.certChain = certChain;
	}

	public String getCertChain() {
		return certChain;
	}

	/**
	 * Ao encerrar uma sessao, limpa os processos que o servidor estava
	 * trabalhando Obs.: usando session do hibernate pq o EM da erro de
	 * transação
	 */
	@Observer("org.jboss.seam.preDestroyContext.SESSION")
	public void anulaActorId() {
		
	}

	/**
	 * Ao ligar a aplicação, limpa todos os actorIds dos processos
	 */
	public void anulaTodosActorId() {
		try {
			String query = "UPDATE jbpm_taskinstance SET actorid_ = null WHERE actorid_ IS NOT NULL and end_ is null";
			HibernateUtil.getSession().createSQLQuery(query)
					.addSynchronizedQuerySpace("jbpm_taskinstance")
					.executeUpdate();
		} catch (Exception e) {
			log.error("Erro ao executar ProcessoHome.anulaTodosActorId", e);
		}
	}

	public Integer getFirst() {
		List<ProcessoDocumento> processoDocList = getInstance().getProcessoDocumentoList();
		return processoDocList.size();
	}

	public void onSelectProcessoDocumento() {
		onSelectProcessoDocumento(tipoProcessoDocumento, EventsTreeHandler.instance());
	}

	/**
	 * Verifica se existe algum agrupamento vinculado ao tipo de documento
	 * selecionado.
	 */
	public void onSelectProcessoDocumento(TipoProcessoDocumento tipoProcessoDocumento, AbstractTreeHandler<Evento> treeHandler) {
		this.tipoProcessoDocumento = tipoProcessoDocumento;
		renderEventsTree = false;
		idAgrupamentos = null;

		if (tipoProcessoDocumento != null && tipoProcessoDocumento.getAgrupamento() != null) {
			idAgrupamentos = tipoProcessoDocumento.getAgrupamento().getIdAgrupamento();
			if (idAgrupamentos != null && idAgrupamentos > 0) {
				renderEventsTree = true;
				LancadorMovimentosService.instance().setAgrupamentoDeMovimentosTemporarios(ProcessInstance.instance(), idAgrupamentos);
				
				if (Contexts.isPageContextActive()) {
					if (AutomaticEventsTreeHandler.class.isAssignableFrom(treeHandler.getClass())){
						((AutomaticEventsTreeHandler)treeHandler).setRootsSelectedMap(new HashMap<Evento, List<Evento>>());
						((AutomaticEventsTreeHandler)treeHandler).getRoots(idAgrupamentos);						
					} else if (EventsTreeHandler.class.isAssignableFrom(treeHandler.getClass())){
						((EventsTreeHandler)treeHandler).setRootsSelectedMap(new HashMap<Evento, List<Evento>>());
						((EventsTreeHandler)treeHandler).getRoots(idAgrupamentos);
					} else if (EventsHomologarMovimentosTreeHandler.class.isAssignableFrom(treeHandler.getClass())){
						((EventsHomologarMovimentosTreeHandler)treeHandler).setRootsSelectedMap(new HashMap<Evento, List<Evento>>());
						((EventsHomologarMovimentosTreeHandler)treeHandler).getRoots(idAgrupamentos);
					}
				}
			}
		}else {
			LancadorMovimentosService.instance().apagarMovimentosTemporarios(ProcessInstance.instance());			
			if (Contexts.isPageContextActive()) {
				if (AutomaticEventsTreeHandler.class.isAssignableFrom(treeHandler.getClass())){
					((AutomaticEventsTreeHandler)treeHandler).clearList();
					((AutomaticEventsTreeHandler)treeHandler).clearTree();
				} else if (EventsTreeHandler.class.isAssignableFrom(treeHandler.getClass())){
					((EventsTreeHandler)treeHandler).clearList();
					((EventsTreeHandler)treeHandler).clearTree();
				} else if (EventsHomologarMovimentosTreeHandler.class.isAssignableFrom(treeHandler.getClass())){
					((EventsHomologarMovimentosTreeHandler)treeHandler).clearList();
					((EventsHomologarMovimentosTreeHandler)treeHandler).clearTree();
				}
			}
		}
	}

	public Integer getIdAgrupamentos() {
		return idAgrupamentos;
	}

	public boolean getRenderEventsTree() {
		return renderEventsTree;
	}

	public void setProcessoDocumentoBin(ProcessoDocumentoBin processoDocumentoBin) {
		this.processoDocumentoBin = processoDocumentoBin;
	}

	public ProcessoDocumentoBin getProcessoDocumentoBin() {
		return processoDocumentoBin;
	}

	public void setPdFluxo(ProcessoDocumento pdFluxo) {
		this.pdFluxo = pdFluxo;
	}

	public ProcessoDocumento getPdFluxo() {
		return pdFluxo;
	}

	public void setIdProcessoDocumento(Integer idProcessoDocumento) {
		this.idProcessoDocumento = idProcessoDocumento;
	}

	public Integer getIdProcessoDocumento() {
		return idProcessoDocumento;
	}

	public ProcessoEvento getUltimoProcessoEvento(Processo processo) {
		String hql = "select o from ProcessoEvento o where o.processo = :processo "
				+ "order by o.dataAtualizacao desc ";

		Query q = getEntityManager().createQuery(hql);
		q.setParameter("processo", processo);

		return EntityUtil.getSingleResult(q);
	}

	@SuppressWarnings("unchecked")
	public List<Evento> getEventosAgrupamento(String nomeAgrupamento) {
		String hql = "	select e from Agrupamento o " + "	inner join o.eventoAgrupamentoList evtL "
				+ "	inner join evtL.evento e " + "	where o.agrupamento = :nomeAgrupamento ";
		Query query = getEntityManager().createQuery(hql);
		query.setParameter("nomeAgrupamento", nomeAgrupamento);
		return query.getResultList();
	}

	public ProcessoEvento getUltimoProcessoEvento(Processo processo, String nomeAgrupamento) {
		List<Evento> eventosAgrupamento = getEventosAgrupamento(nomeAgrupamento);
		if (eventosAgrupamento == null || eventosAgrupamento.size() == 0) {
			log.warn("Nenhum evento evento encontrado para o agrupamento: " + nomeAgrupamento);
			return null;
		}
		List<Evento> eventoListCompleto = new ArrayList<Evento>();
		for (Evento evento : eventosAgrupamento) {
			eventoListCompleto.addAll(evento.getEventoListCompleto());
		}

		String hql = "select o from ProcessoEvento o " + "where o.processo = :processo and o.evento in ("
				+ ":eventos) " + "order by o.dataAtualizacao desc";
		Query query = getEntityManager().createQuery(hql);
		query.setParameter("processo", processo);
		query.setParameter("eventos", eventoListCompleto);
		return EntityUtil.getSingleResult(query);
	}

	public boolean verificaUltimoEvento(Processo processo, Evento evento) {
		ProcessoEvento processoEvento = getUltimoProcessoEvento(processo);

		if (processoEvento == null) {
			log.info("verificaUltimoEvento: Não existem evento no processo: " + processo);
			return false;
		}

		List<Evento> filhos = evento.getEventoListCompleto();
		return filhos.contains(processoEvento.getEvento());
	}

	public boolean verificaEvento(Processo processo, Evento evento) {
		String hql = "select o.idProcessoEvento from ProcessoEvento o where o.processo = :processo and o.evento in (:eventos)";
		Query q = getEntityManager().createQuery(hql);
		q.setParameter("processo", processo);
		q.setParameter("eventos", Util.isEmpty(evento.getEventoListCompleto())?null:evento.getEventoListCompleto());

		return EntityUtil.getSingleResult(q) != null;
	}

	/**
	 * Método que retorna a data do envio do processo ao segundo grau
	 * 
	 * @return Data de envio do processo ao segundo grau
	 * 
	 */
	public String dataEnvio2Grau() {
		String hql = "Select o from ProcessoEvento o where o.processo = :processo "
				+ "and o.evento = (Select o.idEvento from Evento o " + "where o.codEvento = '123A')";
		Query q = getEntityManager().createQuery(hql);
		q.setParameter("processo", getInstance());
		if (!q.getResultList().isEmpty()) {
			ProcessoEvento pe = (ProcessoEvento) q.getResultList().get(0);
			if (pe != null) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
				return sdf.format(pe.getDataAtualizacao());
			}
		}
		return null;
	}

	public boolean possuiTarefaDefinida(int idProcesso) {
		EntityManager em = EntityUtil.getEntityManager();
		try {
			em.createQuery(
					"select o.idTaskInstance "
							+ "from SituacaoProcesso o where o.idProcesso = :id "
							+ "group by o.idTaskInstance")
							.setParameter("id", idProcesso).getResultList().get(0);
		} catch (IndexOutOfBoundsException e) {
			return false;
		}
		return true;
	}

	/**
	 * Move o processo para a tarefa Controle de Prazo.
	 * 
	 * @param processoTrf
	 */
	public void moverTarefaControlePrazo(ProcessoTrf processoTrf) {
		ProcessoHome.instance().setId(processoTrf.getIdProcessoTrf());
		if (getTarefaProcesso(processoTrf).equals(ParametroUtil.instance().getTarefaDarCienciaPartes())
				|| getTarefaProcesso(processoTrf).equals(ParametroUtil.instance().getTarefaConhecimentoSecretaria())) {
			JbpmEventsHandler jeh = JbpmEventsHandler.instance();
			jeh.iniciarTask(processoTrf.getProcesso());
			TaskInstanceHome.instance().end(TaskNamesPrimeiroGrau.CONTROLE_DE_PRAZO);
		}
	}

	@SuppressWarnings("unchecked")
	private Tarefa getTarefaProcesso(ProcessoTrf processoTrf) {
		StringBuilder sb = new StringBuilder();
		sb.append("Select o from SituacaoProcesso o ");
		sb.append("where o.processoTrf = :processoTrf ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("processoTrf", processoTrf);
		q.setMaxResults(1);
		List<Object> resultList = q.getResultList();
		if (resultList.size() > 0) {
			SituacaoProcesso situacaoProcesso = (SituacaoProcesso) resultList.get(0);
			Tarefa tarefa = EntityUtil.find(Tarefa.class, situacaoProcesso.getIdTarefa());
			return tarefa;
		}
		return null;
	}


	/**
	 * PJE-JT: David Vieira: [PJE-779] Retorna se há um documento gravado para o
	 * TaskInstance corrente.
	 */
	public Boolean getGravouDocumentoParaNoTarefa() {
		Long taskInstanceAtual = TaskInstance.instance().getId();
		Long lastTaskInstanceQueSalvouDocumentoNoFluxo = (Long) JbpmUtil.getProcessVariable(LAST_ID_TASK_INSTANCE);

		if (!taskInstanceAtual.equals(lastTaskInstanceQueSalvouDocumentoNoFluxo)) {
			return false;
		}
		Integer lastIdProcessoDocumento = JbpmUtil.getProcessVariable(LAST_ID_PROCESSO_DOCUMENTO);

		Query query = getEntityManager().createQuery(
				"from ProcessoDocumento o where o.idProcessoDocumento = :idProcessoDocumento").setParameter(
				"idProcessoDocumento", lastIdProcessoDocumento);
		try {
			query.getSingleResult();
		} catch (NoResultException e) {
			return false;
		} catch (NonUniqueResultException e) {
			return true;
		}
		return true;
	}

	/**
	 * PJE-JT:FIM
	 */

	public void setShowComponentesFluxo(boolean showComponentesFluxo) {
		this.showComponentesFluxo = showComponentesFluxo;
	}

	public boolean isShowComponentesFluxo() {
		return showComponentesFluxo;
	}
	
	/**
	 * @author thiago.vieira
	 * @param processo
	 * @return lista de Tarefas já executadas por um processo
	 */
	public List<org.jbpm.taskmgmt.exe.TaskInstance> retornaTasks(Processo processo){
		if(processo == null) {
			return null;
		}
		TaskInstanceManager taskInstanceManager = ComponentUtil.getComponent(TaskInstanceManager.NAME);
		return taskInstanceManager.getTarefasProcessoAsc(processo.getIdProcesso());
	}

	public List<org.jbpm.taskmgmt.exe.TaskInstance> retornaTasksDesc(Processo processo){
		if(processo == null) {
			return null;
		}
		TaskInstanceManager taskInstanceManager = ComponentUtil.getComponent(TaskInstanceManager.NAME);
		return taskInstanceManager.getTarefasProcessoDesc(processo.getIdProcesso());
	}

	public org.jbpm.taskmgmt.exe.TaskInstance retornaUltimaTarefa(Processo processo){
		if(processo == null) {
			return null;
		}
		TaskInstanceManager taskInstanceManager = ComponentUtil.getComponent(TaskInstanceManager.NAME);
		return taskInstanceManager.getUltimaTarefaProcesso(processo.getIdProcesso());
	}
	
	public org.jbpm.graph.exe.ProcessInstance retornaProcessInstance(Processo processo){
		return ManagedJbpmContext.instance().getProcessInstance(processo.getIdJbpm());
		}
	

	public long getTaskInstanceId() {
		return taskInstanceId;
	}

	public void setTaskInstanceId(long taskInstanceId) {
		this.taskInstanceId = taskInstanceId;
	}
	

	public ProtocolarDocumentoBean getProtocolarDocumentoBean(){
		return getProtocolarDocumentoBean(false);
	}
	
	public ProtocolarDocumentoBean getProtocolarDocumentoBean(boolean assinado){
		if(protocolarDocumentoBean == null && this.instance != null && this.instance.getIdProcesso() > 0){
			if(assinado) {
				protocolarDocumentoBean = new ProtocolarDocumentoBean(this.instance.getIdProcesso(), ProtocolarDocumentoBean.EXIGE_DOCUMENTO_PRINCIPAL);
			}
			else {
				protocolarDocumentoBean = new ProtocolarDocumentoBean(this.instance.getIdProcesso(), ProtocolarDocumentoBean.EXIGE_DOCUMENTO_PRINCIPAL | ProtocolarDocumentoBean.RECUPERA_DOCUMENTO_FLUXO);
			}
		}
		return protocolarDocumentoBean;
	}
	
	/**
	 * verifica o status do processo
	 * @param idProcessoTrf
	 * @return
	 */
	public boolean isProcessoRemetidoBloqueado(){
		ProcessoTrfManager p = ComponentUtil.getComponent(ProcessoTrfManager.NAME);
		return p.isProcessoRemetidoBloqueado(this.getProcessoIdProcesso());
	}

}
