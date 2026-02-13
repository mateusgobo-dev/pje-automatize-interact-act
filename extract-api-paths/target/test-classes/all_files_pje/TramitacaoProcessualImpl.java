/**
 * 
 */
package br.jus.cnj.pje.nucleo.service;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.Log;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;

import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.infox.pje.service.AssinaturaDocumentoService;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.FacesUtil;
import br.jus.cnj.pje.entidades.vo.ProcessoProcessInstanceVO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.AgrupamentoClasseJudicialManager;
import br.jus.cnj.pje.nucleo.manager.AgrupamentoManager;
import br.jus.cnj.pje.nucleo.manager.AssuntoTrfManager;
import br.jus.cnj.pje.nucleo.manager.EventoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoAssuntoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoEventoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoInstanceManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoMagistradoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoTrfConexaoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoPautaProcessoTrfManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoVotoManager;
import br.jus.cnj.pje.nucleo.manager.SituacaoProcessualManager;
import br.jus.cnj.pje.nucleo.manager.TipoComplementoManager;
import br.jus.cnj.pje.nucleo.manager.TipoSituacaoProcessualManager;
import br.jus.pje.nucleo.entidades.Agrupamento;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.EventoAgrupamento;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCargo;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.ProcessoAssunto;
import br.jus.pje.nucleo.entidades.ProcessoInstance;
import br.jus.pje.nucleo.entidades.ProcessoMagistrado;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.entidades.SituacaoProcessual;
import br.jus.pje.nucleo.entidades.TipoSituacaoProcessual;
import br.jus.pje.nucleo.entidades.lancadormovimento.TipoComplemento;
import br.jus.pje.nucleo.enums.AdiadoVistaEnum;
import br.jus.pje.nucleo.enums.JulgamentoEnum;
import br.jus.pje.nucleo.enums.TipoAtuacaoMagistradoEnum;
import br.jus.pje.nucleo.enums.TipoRelacaoProcessoMagistradoEnum;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * Implementação da interface {@link TramitacaoProcessualService}.
 * 
 * @author cristof
 *
 */
@Name("tramitacaoProcessualService")
@Scope(ScopeType.EVENT)
public class TramitacaoProcessualImpl implements TramitacaoProcessualService, Serializable{
	
	private static final long serialVersionUID = -5724673020805848667L;

	@In(required=false)
	private ProcessInstance processInstance;
	
	@In(required=false)
	private TaskInstance taskInstance;
	
	@In
	private SessaoPautaProcessoTrfManager sessaoPautaProcessoTrfManager;

	@In
	private SessaoProcessoDocumentoVotoManager sessaoProcessoDocumentoVotoManager;
	
	@In
	private AgrupamentoClasseJudicialManager agrupamentoClasseJudicialManager;
	
	@In
	private AgrupamentoManager agrupamentoManager;
	
	@In
	private AssuntoTrfManager assuntoTrfManager;
	
	@In
	private EventoManager eventoManager;
	
	@In
	private ProcessoAssuntoManager processoAssuntoManager;
	
	@In
	private ProcessoEventoManager processoEventoManager;
	
	@In
	private ProcessoJudicialManager processoJudicialManager;
	
	@In
	private ProcessoJudicialService processoJudicialService;
	
	@In
	private TipoComplementoManager tipoComplementoManager;
	
	@In
	private ProcessoTrfConexaoManager processoTrfConexaoManager;
	
	@In
	private ProcessoInstanceManager processoInstanceManager;
	
	@In
	private SituacaoProcessualManager situacaoProcessualManager;
	
	@In
	private TipoSituacaoProcessualManager tipoSituacaoProcessualManager;
	
	@In
	private ProcessoMagistradoManager processoMagistradoManager;
	
	@In
	private OrgaoJulgadorManager orgaoJulgadorManager;
	
	@Logger
	private Log logger;

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService#recuperaVariavel(java.lang.String)
	 */
	@Override
	public Object recuperaVariavel(String nome) {
		if(processInstance == null){
			throw new IllegalStateException("Não há instância de processo de negócio disponível para recuperação da variável.");
		}
		return processInstance.getContextInstance().getVariable(nome);
	}
	
	@Override
	public Object recuperaVariavel(ProcessInstance processInstance, String nome) {
		if(processInstance == null){
			return recuperaVariavel(nome);
		}
		return processInstance.getContextInstance().getVariable(nome);
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService#gravaVariavel(java.lang.String, java.lang.Object)
	 */
	@Override
	public void gravaVariavel(String nome, Object value) {
		gravaVariavel(processInstance,nome,value);
	}

	public void gravaVariavel(ProcessInstance pi, String nome, Object value){
		if(pi == null){
			throw new IllegalStateException("Não há instância de processo de negócio disponível para gravação da variável.");
		}
		pi.getContextInstance().setVariable(nome, value);
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService#recuperaVariavelTarefa(java.lang.String)
	 */
	@Override
	public Object recuperaVariavelTarefa(String nome) {
		if(taskInstance == null){
			throw new IllegalStateException("Não há instância de tarefa disponível para recuperação da variável.");
		}
		return taskInstance.getVariableLocally(nome);
	}
	
	@Override
	public Object recuperaVariavelTarefa(TaskInstance ti, String nome) {
		if ( ti==null ) {
			return recuperaVariavelTarefa(nome);
		}
		return ti.getVariableLocally(nome);
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService#gravaVariavelTarefa(java.lang.String, java.lang.Object)
	 */
	@Override
	public void gravaVariavelTarefa(String nome, Object value) {
		gravaVariavelTarefa(taskInstance, nome, value);
	}
	
	public void gravaVariavelTarefa(TaskInstance ti, String nome, Object value) {
		if(ti == null){
			throw new IllegalStateException("Não há instância de tarefa disponível para gravação da variável.");
		}
		ti.setVariableLocally(nome, value);
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService#apagaVariavel(java.lang.String)
	 */
	@Override
	public void apagaVariavel(String nome) {
		apagaVariavel(processInstance,nome);
	}

	public void apagaVariavel(ProcessInstance pi, String nome){
		if(pi == null){
			throw new IllegalStateException("Não há instância de processo de negócio disponível para que a variável seja apagada.");
		}
		pi.getContextInstance().deleteVariable(nome);

	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService#apagaVariavelTarefa(java.lang.String)
	 */
	@Override
	public void apagaVariavelTarefa(String nome) {
		apagaVariavelTarefa(taskInstance, nome);
	}
	
	public void apagaVariavelTarefa(TaskInstance ti, String nome) {
		if(ti == null){
			throw new IllegalStateException("Não há instância de tarefa disponível para que a variável seja apagada.");
		}
		ti.deleteVariableLocally(nome);
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService#recuperaProcesso(java.lang.Integer)
	 */
	@Override
	public ProcessoTrf recuperaProcesso(Integer idProcesso) {
		try {
			ProcessoTrf processo = processoJudicialService.findById(idProcesso);
			if(processo == null){
				String msg = String.format("Não existe processo judicial com o identificador %s", idProcesso);
				throw new IllegalArgumentException(msg);
			}
			return processo;
		} catch (PJeBusinessException e) {
			throw new IllegalArgumentException(e.getLocalizedMessage());
		}
	}
	
	@Override
	public ProcessoProcessInstanceVO recuperaProcessoProcessInstanceVO(Integer idProcesso, Long idProcessInstance) {
		try {
			ProcessoTrf processo = processoJudicialService.findById(idProcesso);
			if(processo == null){
				String msg = String.format("No existe processo judicial com o identificador %s", idProcesso);
				throw new IllegalArgumentException(msg);
			}
			return new ProcessoProcessInstanceVO(processo.getNumeroProcesso(), idProcessInstance);
		} catch (PJeBusinessException e) {
			throw new IllegalArgumentException(e.getLocalizedMessage());
		}
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService#recuperaProcesso()
	 */
	@Override
	public ProcessoTrf recuperaProcesso() {
		return recuperaProcesso(getIdProcessoJudicial());
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService#temUrgencia()
	 */
	@Override
	public boolean temUrgencia() {
		return temUrgencia(getIdProcessoJudicial());
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService#temUrgencia(java.lang.Integer)
	 */
	@Override
	public boolean temUrgencia(Integer idProcesso) {
		ProcessoTrf processo = recuperaProcesso(idProcesso);
		Boolean pedido = processo.getTutelaLiminar();
		Boolean apreciado = processo.getApreciadoTutelaLiminar();
		if(pedido != null && pedido && (apreciado == null || !apreciado)){
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService#temSigilo()
	 */
	@Override
	public boolean sigiloso() {
		return sigiloso(getIdProcessoJudicial());
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService#temSigilo(java.lang.Integer)
	 */
	@Override
	public boolean sigiloso(Integer idProcesso) {
		ProcessoTrf processo = recuperaProcesso(idProcesso);
		return processo.getSegredoJustica();
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService#temAssunto(java.lang.Integer)
	 */
	@Override
	public boolean temAssunto(Integer codigoAssunto) {
		return temAssunto(getIdProcessoJudicial(), codigoAssunto);
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService#temAssunto(java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public boolean temAssunto(Integer idProcesso, Integer codigoAssunto) {
		ProcessoTrf processo = recuperaProcesso(idProcesso);
		try {
			AssuntoTrf assunto = assuntoTrfManager.findByCodigo(codigoAssunto);
			return processoAssuntoManager.temAssunto(processo, assunto);
		} catch (PJeException e) {
			throw new IllegalStateException(e.getLocalizedMessage());
		}
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService#temAssuntoDoGrupo(java.lang.String)
	 */
	@Override
	public boolean temAssuntoDoGrupo(String idGrupo) {
		return temAssuntoDoGrupo(getIdProcessoJudicial(), idGrupo);
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService#temAssuntoDoGrupo(java.lang.Integer, java.lang.String)
	 */
	@Override
	public boolean temAssuntoDoGrupo(Integer idProcesso, String idGrupo) {
		ProcessoTrf processo = recuperaProcesso(idProcesso);
		for(ProcessoAssunto pa: processo.getProcessoAssuntoList()){
			if(agrupamentoClasseJudicialManager.pertence(pa.getAssuntoTrf(), idGrupo)){
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService#temMovimento(java.lang.Integer)
	 */
	@Override
	public boolean temMovimento(String codigoMovimento) {
		return temMovimento(getIdProcessoJudicial(), codigoMovimento, null);
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService#temMovimento(java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public boolean temMovimento(Integer idProcesso, String codigoMovimento) {
		return temMovimento(idProcesso, codigoMovimento, null);
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService#temMovimento(java.lang.Integer, int)
	 */
	@Override
	public boolean temMovimento(String codigoMovimento, Date dataLimite) {
		return temMovimento(getIdProcessoJudicial(), codigoMovimento, dataLimite);
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService#temMovimento(java.lang.Integer, java.lang.Integer, int)
	 */
	@Override
	public boolean temMovimento(Integer idProcesso, String codigoMovimento, Date dataLimite) {
		Evento movimento = eventoManager.findByCodigoCNJ(codigoMovimento);
		try{
			return processoEventoManager.temMovimento(idProcesso, movimento, dataLimite);
		} catch (PJeBusinessException e){
			logger.error("Erro ao tentar identificar se houve o lançamento de uma movimentação: {0}.", e.getLocalizedMessage());
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService#temMovimento(java.lang.Integer, java.lang.Integer, int)
	 */
	public boolean temAlgumMovimento(Integer idProcesso, Date dataLimite, Evento... movimento) {
		try{
			return processoEventoManager.temAlgumMovimento(idProcesso, dataLimite, movimento);
		} catch (PJeBusinessException e){
			logger.error("Erro ao tentar identificar se houve o lançamento de uma movimentação: {0}.", e.getLocalizedMessage());
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService#temMovimento(java.lang.Integer, int, java.lang.String[])
	 */
	@Override
	public boolean temMovimento(String codigoMovimento, Date dataLimite, String... complementos) {
		return temMovimento(getIdProcessoJudicial(), codigoMovimento, dataLimite, complementos);
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService#temMovimento(java.lang.Integer, java.lang.Integer, int, java.lang.String[])
	 */
	@Override
	public boolean temMovimento(Integer idProcesso, String codigoMovimento, Date dataLimite, String... complementos) {
		ProcessoTrf processo = recuperaProcesso(idProcesso);
		Evento movimento = eventoManager.findByCodigoCNJ(codigoMovimento);
		Map<TipoComplemento, String> comps = new HashMap<TipoComplemento, String>(complementos.length);
		try {
			for(String c: complementos){
				String[] dadosComplemento = c.split(":");
				TipoComplemento tipoComplemento = tipoComplementoManager.findByCodigo(dadosComplemento[0]);
				if(dadosComplemento.length > 1){
					comps.put(tipoComplemento, dadosComplemento[1]);
				}
			}
			return processoEventoManager.temMovimento(processo, movimento, dataLimite, comps);
		} catch (PJeBusinessException e) {
			logger.error("Erro ao tentar identificar a existência de movimentação com complementos: {0}", e.getLocalizedMessage());
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService#temMovimentoDoGrupo(java.lang.String)
	 */
	@Override
	public boolean temMovimentoDoGrupo(String idGrupo) {
		return temMovimentoDoGrupo(getIdProcessoJudicial(), idGrupo);
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService#temMovimentoDoGrupo(java.lang.Integer, java.lang.String)
	 */
	@Override
	public boolean temMovimentoDoGrupo(Integer idProcesso, String idGrupo) {
		return temMovimentoDoGrupo(idProcesso, idGrupo, null);
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService#temMovimentoDoGrupo(java.lang.String, int)
	 */
	@Override
	public boolean temMovimentoDoGrupo(String idGrupo, Date dataLimite) {
		return temMovimentoDoGrupo(getIdProcessoJudicial(), idGrupo, dataLimite);
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService#temMovimentoDoGrupo(java.lang.Integer, java.lang.String, int)
	 */
	@Override
	public boolean temMovimentoDoGrupo(Integer idProcesso, String idGrupo, Date dataLimite) {
		boolean found = false;
		try {
			ProcessoTrf processo = recuperaProcesso(idProcesso);
			Agrupamento agrupamento = agrupamentoManager.findByNome(idGrupo);
			if(agrupamento == null){
				logger.warn("Não existe agrupamento com o identificador {0}.", idGrupo);
				return false;
			}
			Iterator<EventoAgrupamento> it = agrupamento.getEventoAgrupamentoList().iterator();
			while(!found && it.hasNext()){
				Evento movimento = it.next().getEvento();
				if(processoEventoManager.temMovimento(processo, movimento, dataLimite)){
					found = true;
				}
			}
		} catch (PJeBusinessException e) {
			logger.error("Erro ao tentar identificar a existência de movimentação: {0}", e.getLocalizedMessage());
		}
		return found;
	}
	
	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService#contagemPreventoPendentes()
	 */
	@Override
	public int contagemPreventoPendentes() {
		Integer id = this.getIdProcessoJudicial();
		return contagemPreventoPendentes(id);
	}
	
	@Override
	public int contagemPreventoPendentes(Integer idProcesso) {
		return this.processoTrfConexaoManager.getQuantidadeProcessosPreventos(idProcesso);
	}
	
	@Override
	public void movimentarProcessoJudicial(String defaultTransition) {
		String transition = (String) ComponentUtil.getTaskInstanceUtil().getVariable(defaultTransition);
		if (transition != null) {
			for (Transition t : TaskInstanceHome.instance().getTransitions(true)) {
				if (t.getName().equals(transition)) {
					ComponentUtil.getTaskInstanceHome().saidaDireta(transition);
					break;
				}
			}
		}
	}
	
	public boolean deslocarFluxoParaOrgaoDiverso(){
		Integer id = this.getIdProcessoJudicial();
		
		return deslocarFluxoParaOrgaoDiverso(id);
	}
	
	public boolean deslocarFluxoParaOrgaoDiverso(Integer idProcessoJudicial){
		Integer idLocalizacao = parseVariableToInteger(processInstance.getContextInstance().getVariable(br.jus.cnj.pje.nucleo.Variaveis.VARIAVEL_SUBSEVENTUAL_LOCALIZACAO));
		Integer idOrgao = parseVariableToInteger(processInstance.getContextInstance().getVariable(br.jus.cnj.pje.nucleo.Variaveis.VARIAVEL_SUBSEVENTUAL_ORGAO));
		Integer idCargoJudicial = parseVariableToInteger(processInstance.getContextInstance().getVariable(br.jus.cnj.pje.nucleo.Variaveis.VARIAVEL_SUBSEVENTUAL_CARGO));
		Integer idColegiado = parseVariableToInteger(processInstance.getContextInstance().getVariable(br.jus.cnj.pje.nucleo.Variaveis.VARIAVEL_SUBSEVENTUAL_COLEGIADO));
		
		return this.deslocarFluxoParaOrgaoDiverso(idProcessoJudicial, idOrgao, idCargoJudicial, idColegiado, idLocalizacao);
	}
	
	public boolean deslocarFluxoParaOrgaoVista(){
		ProcessoTrf proc = recuperaProcesso();
		if(proc == null || proc.getIdProcessoTrf() == 0){
			logger.error("[DESLOCAMENTO DE ORGAO JULGADOR POR PEDIDO DE VISTA - ERROR] - Não foi possível identificar o processo.");
			return false;
		}
		SessaoPautaProcessoTrf sessao = sessaoPautaProcessoTrfManager.getSessaoPautaProcessoTrfNaoJulgado(proc);
		if (sessao != null && sessao.getAdiadoVista() == AdiadoVistaEnum.PV) {
			OrgaoJulgadorCargo orgaoJulgadorCargoOrigem = proc.getOrgaoJulgadorCargo();
			OrgaoJulgador orgaoJulgadorDestino = sessao.getOrgaoJulgadorPedidoVista();
			
			// O órgão julgador colegiado de destino deverá ser o mesmo órgão julgador colegiado do processo
			OrgaoJulgadorColegiado orgaoJulgadorColegiadoDestino = proc.getOrgaoJulgadorColegiado();
			
			OrgaoJulgadorCargo orgaoJulgadorCargoDestino = null;
			List<OrgaoJulgadorCargo> orgaoJulgadorCargoDestinoList = orgaoJulgadorDestino.getOrgaoJulgadorCargoList();
			if(orgaoJulgadorCargoDestinoList != null && orgaoJulgadorCargoOrigem != null && orgaoJulgadorCargoOrigem.getCargo() != null){
				if(orgaoJulgadorCargoDestinoList.size() == 1){
					orgaoJulgadorCargoDestino = orgaoJulgadorCargoDestinoList.get(0);
				}
				else{
					for(OrgaoJulgadorCargo orgaoJulgadorCargoDestinoAux: orgaoJulgadorCargoDestinoList){
						if(orgaoJulgadorCargoDestinoAux.getCargo() != null && orgaoJulgadorCargoDestinoAux.getCargo().getCargo().equalsIgnoreCase(orgaoJulgadorCargoOrigem.getCargo().getCargo())){
							orgaoJulgadorCargoDestino = orgaoJulgadorCargoDestinoAux;
							break;
						}
					}
				}
					
			}
			
			Integer idOrgao = orgaoJulgadorDestino.getIdOrgaoJulgador();
			Integer idCargoJudicial = orgaoJulgadorCargoDestino.getIdOrgaoJulgadorCargo();
			Integer idColegiado = orgaoJulgadorColegiadoDestino.getIdOrgaoJulgadorColegiado();
			
			//gravando ids do destino
			gravaVariavel("pje:fluxo:deslocamento:orgaoDestino", idOrgao);
			gravaVariavel("pje:fluxo:deslocamento:orgaoCargoDestino", idCargoJudicial);
			gravaVariavel("pje:fluxo:deslocamento:colegiadoDestino", idColegiado);
			
			return deslocarFluxoParaOrgaoDiverso(proc.getIdProcessoTrf(),idOrgao,idCargoJudicial,idColegiado);
		} else {
			logger.error("[DESLOCAMENTO DE ORGAO JULGADOR POR PEDIDO DE VISTA - ERROR] - Não consta pedido de vista para o processo.");
			
			return false;
		}
	}

	public boolean registrarOrgaoVencedor(){
		boolean retorno = false;
		ProcessoTrf proc = recuperaProcesso();
		if(proc == null || proc.getIdProcessoTrf() == 0){
			logger.error("[REGISTRAR ÓRGÃO VENCEDOR - ERROR] - Não foi possível identificar o processo.");
		} else {
			SessaoPautaProcessoTrf sessao = sessaoPautaProcessoTrfManager.getSessaoPautaProcessoTrfJulgado(proc);
			if (sessao != null) {
				OrgaoJulgadorCargo orgaoJulgadorCargoOrigem = proc.getOrgaoJulgadorCargo();
				OrgaoJulgador orgaoJulgadorVencedor = sessao.getOrgaoJulgadorVencedor();
				if(orgaoJulgadorVencedor != null ) {
					OrgaoJulgadorCargo orgaoJulgadorCargoVencedor = null;
					List<OrgaoJulgadorCargo> orgaoJulgadorCargoVencedorList = orgaoJulgadorVencedor.getOrgaoJulgadorCargoList();
					if(orgaoJulgadorCargoVencedorList != null && orgaoJulgadorCargoOrigem != null && orgaoJulgadorCargoOrigem.getCargo() != null){
						if(orgaoJulgadorCargoVencedorList.size() == 1){
							orgaoJulgadorCargoVencedor = orgaoJulgadorCargoVencedorList.get(0);
						}
						else{
							for(OrgaoJulgadorCargo orgaoJulgadorCargoVencedorAux: orgaoJulgadorCargoVencedorList){
								if(orgaoJulgadorCargoVencedorAux.getCargo() != null && orgaoJulgadorCargoVencedorAux.getCargo().getCargo().equalsIgnoreCase(orgaoJulgadorCargoOrigem.getCargo().getCargo())){
									orgaoJulgadorCargoVencedor = orgaoJulgadorCargoVencedorAux;
									break;
								}
							}
						}
							
					}
					if(orgaoJulgadorCargoVencedor != null) {
						//gravando ids do vencedor
						gravaVariavel(Variaveis.VARIAVEL_FLUXO_JULGAMENTO_COLEGIADO_VENCEDOR, orgaoJulgadorVencedor.getIdOrgaoJulgador());
						gravaVariavel(Variaveis.VARIAVEL_FLUXO_JULGAMENTO_COLEGIADO_CARGO_VENCEDOR, orgaoJulgadorCargoVencedor.getIdOrgaoJulgadorCargo());
						retorno = true;
					} else {
						logger.error("[REGISTRAR ÓRGÃO VENCEDOR - ERROR] - Erro ao recuperar o cargo do órgão vencedor do julgamento.");
					}
				} else {
					logger.error("[REGISTRAR ÓRGÃO VENCEDOR - ERROR] - Erro ao recuperar o órgão vencedor do julgamento.");
				}
			} else {
				logger.error("[REGISTRAR ÓRGÃO VENCEDOR - ERROR] - Erro ao recuperar sessão na qual o processo foi julgado.");
			}
		}
		return retorno;
	}

	/**
	 * Recupera o processo conforme o idProcessoJudicial. Caso não o encontre, o metodo lançara a excecao 
	 * PJeBusinessException.
	 * @return	retorna uma instancia do objeto processoTrf preenchido conforme idProcessoJudicial
	 */
	private ProcessoTrf obterProcesso(){
		ProcessoTrf processo = null;
		Integer id = this.getIdProcessoJudicial();
		try {
			processo = processoJudicialManager.findById(id);

		} catch (PJeBusinessException e) {
			logger.error(FacesUtil.getMessage("entity_messages", "tramitacaoProcessual.erro.pjeBusinessException", e.getMessage()));
			e.printStackTrace();
		}
		
		return processo;
	}
	
	/**
	 * Verifica se o processo foi identificado. Para isso, devera ter um numero diferente de zero.
	 * @param processo
	 * @return	verdadeiro se o processo estiver instanciado e seu id for diferente de zero.
	 */
	private boolean isProcessoIdentificado (ProcessoTrf processo){
		boolean retorno = false;
		
		if (processo != null && processo.getIdProcessoTrf() != 0) {
			retorno = true;
		} else {
			logger.error(FacesUtil.getMessage("tramitacaoProcessual.erro.processo.nao.identificado"));
		}
		
		return retorno;
	}

	public boolean deslocarFluxoParaLocalizacao(Integer idProcessoJudicial, Integer idLocalizacao){
		return this.deslocarFluxoParaOrgaoDiverso(idProcessoJudicial, null, null, null, idLocalizacao);
	}

	public boolean deslocarFluxoParaOrgaoDiverso(Integer idProcessoJudicial, Integer idOrgao, Integer idCargoJudicial, Integer idColegiado){
		try {
			Integer idLocalizacao = getIdLocalizacao(idOrgao);
			return this.deslocarFluxoParaOrgaoDiverso(idProcessoJudicial, idOrgao, idCargoJudicial, idColegiado, idLocalizacao);
		} catch (Exception e) {
			return false;
		}
	}
	

	public boolean deslocarFluxoParaOrgaoDiverso(Integer idProcessoJudicial, Integer idOrgao, Integer idCargoJudicial, Integer idColegiado, Integer idLocalizacao){
		try {
			ProcessoInstance pi = recuperaProcessoInstance(idProcessoJudicial);
			if(pi == null) {
				return false;
			}
			
			if(idLocalizacao == null && idOrgao != null && idOrgao > 0) {
				idLocalizacao = getIdLocalizacao(idOrgao);
			}

			pi.setIdLocalizacao((idLocalizacao != null && idLocalizacao > 0) ? idLocalizacao : null);
			pi.setOrgaoJulgadorCargo((idCargoJudicial != null && idCargoJudicial > 0) ? idCargoJudicial : null);
			pi.setOrgaoJulgadorColegiado((idColegiado != null && idColegiado > 0) ? idColegiado : null);
			processoInstanceManager.persistAndFlush(pi);
			
			Events.instance().raiseTransactionSuccessEvent(AutomacaoTagService.EVENTO_AUTOMACAO_TAG, idProcessoJudicial);
			
			return true;
		} catch (Exception e) {
			return false;
		}
	}	
	
	private ProcessoInstance recuperaProcessoInstance(Integer idProcessoJudicial) {
		if(processInstance == null){
			return null;
		}
		try {
			ProcessoInstance pi = processoInstanceManager.findById(processInstance.getId());
			if(pi == null){
				pi = new ProcessoInstance();
				pi.setIdProcesso(idProcessoJudicial);
				pi.setIdProcessoInstance(processInstance.getId());
			}
			
			if(idProcessoJudicial != null && pi != null && pi.getIdProcesso() != null && !pi.getIdProcesso().equals(idProcessoJudicial)) {
				return null;
			}
			
			return pi;
		} catch (Exception e) {
			return null;
		}
	}
	
	private Integer getIdLocalizacao(Integer idOrgaoJulgador) throws PJeBusinessException {
	    OrgaoJulgador oj = (idOrgaoJulgador == null || idOrgaoJulgador == 0) ? null : 
    		ComponentUtil.getComponent(OrgaoJulgadorManager.class).findById(idOrgaoJulgador);
    
	    Integer idLocalizacaoOJ = (oj == null || oj.getLocalizacao() == null) ? null : oj.getLocalizacao().getIdLocalizacao();
		
	    return idLocalizacaoOJ;
	}
	
	/**
	 * Método responsável por deslocar o fluxo atual para o revisor do processo.
	 * 
	 * Primeiro será verificado se existe no processo algum vínculo de revisor
	 * (@see {@link ProcessoMagistrado}). Caso nehum vínculo seja encontrado o
	 * processo será deslocado para o titular o Orgão Julgador setado em
	 * {@link ProcessoTrf#getOrgaoJulgadorRevisor()}
	 * 
	 * @see #deslocarFluxoParaOrgaoDiverso(Integer, Integer, Integer, Integer)
	 * 
	 * @param processoTrf
	 *            {@link ProcessoTrf} que terá o fluxo deslocado.
	 * 
	 * @return <code>true</code> caso tenha sucesso no deslocamento para o
	 *         revisor 
	 *         <code>false</code>caso o processo não tenha revisor
	 *         definido e não seja feito deslocamento de fluxo
	 */
	public boolean deslocarFluxoParaRevisor(ProcessoTrf processoTrf) {
				
		ProcessoMagistrado vinculacaoRevisor = processoMagistradoManager.obterUltimaVinculacao(processoTrf,
				TipoRelacaoProcessoMagistradoEnum.REGIM, TipoAtuacaoMagistradoEnum.REVIS);
		
		boolean retorno = false;
		
		if (vinculacaoRevisor != null) {
			deslocarFluxoParaOrgaoDiverso(processoTrf.getIdProcessoTrf(), 
					vinculacaoRevisor.getOrgaoJulgador().getIdOrgaoJulgador(), 
					vinculacaoRevisor.getOrgaoJulgadorCargo().getIdOrgaoJulgadorCargo(), 
					vinculacaoRevisor.getOrgaoJulgadorColegiado().getIdOrgaoJulgadorColegiado());
			retorno = true;
			
		} else if (processoTrf.getOrgaoJulgadorRevisor() != null){
			
			OrgaoJulgador orgaoJulgadorRevisor = processoTrf.getOrgaoJulgadorRevisor();
						
			OrgaoJulgadorCargo orgaoJulgadorCargoEmExercicio = orgaoJulgadorManager.recuperarCargoResponsavel(
					orgaoJulgadorRevisor, processoTrf.getOrgaoJulgadorColegiado(), DateUtil.getDataAtual());
			
			deslocarFluxoParaOrgaoDiverso(processoTrf.getIdProcessoTrf(), 
					orgaoJulgadorRevisor.getIdOrgaoJulgador(), 
					orgaoJulgadorCargoEmExercicio.getIdOrgaoJulgadorCargo(), 
					processoTrf.getOrgaoJulgadorColegiado().getIdOrgaoJulgadorColegiado());
			
			retorno = true;
			
		}
		
		return retorno;
	}
	
	private Integer parseVariableToInteger(Object value){
		if(value == null){
			return null;
		} else if(Number.class.isAssignableFrom(value.getClass())){
			return ((Number) value).intValue();
		} else if(String.class.isAssignableFrom(value.getClass())){
			return Integer.parseInt((String) value);
		} else {
			throw new IllegalArgumentException("O valor repassado não pode ser transformado em número inteiro.");
		}
	}
	
	private Integer getIdProcessoJudicial(){
		try{
			Integer idProcesso = (Integer) recuperaVariavel(Variaveis.VARIAVEL_PROCESSO);
			if(idProcesso == null){
				throw new IllegalStateException("O processo de negócio não tem a variável [processo] definida.");
			}
			return idProcesso;
		}catch(ClassCastException e){
			throw new IllegalStateException("O valor da variável [processo] não pode ser convertido para um identificador de processo judicial.", e);
		}
	}	

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService#acrescentarSituacao(java.lang.String)
	 */
	@Override
	public void acrescentarSituacao(String codigoTipoSituacao) {
		acrescentarSituacao(getIdProcessoJudicial(), codigoTipoSituacao);
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService#acrescentarSituacao(java.lang.Integer, java.lang.String)
	 */
	@Override
	public void acrescentarSituacao(Integer idProcesso, String codigoTipoSituacao) {
		TipoSituacaoProcessual tipoSituacaoProcessual = tipoSituacaoProcessualManager.findByCodigo(codigoTipoSituacao);
		if(tipoSituacaoProcessual == null){
			throw new IllegalArgumentException("Não há tipo de situação com o código " + codigoTipoSituacao);
		}
		ProcessoTrf proc = recuperaProcesso(idProcesso);
		if(processoJudicialManager.temSituacaoIncompativel(proc, tipoSituacaoProcessual)){
			new IllegalStateException("Tentativa de incluir situação de tipo incompatível com situação ainda ativa no processo");
		}
		SituacaoProcessual sit = situacaoProcessualManager.criarSituacao(proc, tipoSituacaoProcessual);
		try{
			situacaoProcessualManager.persist(sit);
			proc.getSituacoes().add(sit);
			situacaoProcessualManager.flush();
		}catch(PJeBusinessException e){
			logger.error("Erro ao tentar incluir situação no processo: {0}", e.getLocalizedMessage());
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService#acrescentarSituacao(br.jus.pje.nucleo.entidades.ProcessoTrf, java.lang.String)
	 */
	@Override
	public void acrescentarSituacao(ProcessoTrf processo, String codigoTipoSituacao) {
		acrescentarSituacao(processo.getIdProcessoTrf(), codigoTipoSituacao);
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService#removerSituacao(java.lang.String)
	 */
	@Override
	public void removerSituacao(String codigoTipoSituacao) {
		removerSituacao(getIdProcessoJudicial(), codigoTipoSituacao);
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService#removerSituacao(java.lang.Integer, java.lang.String)
	 */
	@Override
	public void removerSituacao(Integer idProcesso, String codigoTipoSituacao) {
		if(!temSituacao(idProcesso, codigoTipoSituacao)){
			return;
		}
		ProcessoTrf proc = recuperaProcesso(idProcesso);
		List<SituacaoProcessual> situacoes = processoJudicialManager.recuperaSituacao(proc, codigoTipoSituacao);
		Date now = new Date();
		try{
			for(SituacaoProcessual s: situacoes){
				if(s.getDataFinal() == null){
					s.setDateFinal(now);
					s.setAtivo(false);
				}
			}
			situacaoProcessualManager.flush();
		}catch(PJeBusinessException e){
			logger.error("Erro ao tentar incluir situação no processo: {0}", e.getLocalizedMessage());
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService#removerSituacao(br.jus.pje.nucleo.entidades.ProcessoTrf, java.lang.String)
	 */
	@Override
	public void removerSituacao(ProcessoTrf processo, String codigoTipoSituacao) {
		removerSituacao(processo.getIdProcessoTrf(), codigoTipoSituacao);
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService#temSituacao(java.lang.String)
	 */
	@Override
	public boolean temSituacao(String codigoSituacao) {
		return temSituacao(recuperaProcesso(), codigoSituacao);
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService#temSituacao(java.lang.Integer, java.lang.String)
	 */
	@Override
	public boolean temSituacao(Integer idProcesso, String codigoSituacao) {
		return temSituacao(recuperaProcesso(idProcesso), codigoSituacao);
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService#temSituacao(br.jus.pje.nucleo.entidades.ProcessoTrf, java.lang.String)
	 */
	@Override
	public boolean temSituacao(ProcessoTrf processo, String codigoSituacao) {
		return processoJudicialManager.temSituacao(processo, codigoSituacao);
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService#temSituacao(br.jus.pje.nucleo.entidades.ProcessoTrf, java.lang.String, java.util.Date)
	 */
	@Override
	public boolean temSituacao(ProcessoTrf processo, String codigoSituacao, Date dataReferencia) {
		return processoJudicialManager.temSituacao(processo, codigoSituacao, dataReferencia);
	}

	@Override
	public Transition recuperarTransicaoPadrao(TaskInstance taskInstance) {
		if(taskInstance != null){
			
			Object variavel = recuperaVariavelTarefa(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);
			
			if(variavel != null){
				if(variavel instanceof String){
					
					String varString = (String) variavel;
					
					for(Transition t: taskInstance.getAvailableTransitions()){
						if(t.getName().equals(varString)){
							return t;
						}
					}
					
					logger.warn("O nó [{0}] tem configurada como padrão a transição de nome [{1}], mas não há transição tal disponível.",taskInstance.getName(), variavel);
				}
				else{
					logger.warn("O nó [{0}] tem configurada na variável de transição padrão valor de classe diversa de java.lang.String ([{1}]).",taskInstance.getName(), variavel.getClass().getCanonicalName());
				}
			}
		}
		return null;
	}   
	
	@Override
	public boolean magistradoRelatorVencedor() {
		boolean isRelatorVencedor = false;
		SessaoPautaProcessoTrf sessaoPautaProcessoTrf = obterSessaoPautaProcesso();
		if(sessaoPautaProcessoTrf != null) {
			if( sessaoPautaProcessoTrf.getOrgaoJulgadorRelator() != null && sessaoPautaProcessoTrf.getOrgaoJulgadorVencedor() != null 
					&& sessaoPautaProcessoTrf.getOrgaoJulgadorRelator() == sessaoPautaProcessoTrf.getOrgaoJulgadorVencedor() ) {
				isRelatorVencedor = true;
			}
		}
		return isRelatorVencedor;
	}
	
	@Override
	public boolean votoRelatorAssinado(){
		boolean votoRelatorAssinado = false;
		SessaoProcessoDocumentoVoto votoRelator = recuperaVotoRelator();
		if(votoRelator != null) {
			votoRelatorAssinado = isVotoAssinado(votoRelator);
		}
		return votoRelatorAssinado;
	}

	/**
	* Recupera o voto do relator do processo
	* @return o voto do relator do processo
	*/
	private SessaoProcessoDocumentoVoto recuperaVotoRelator() {
		SessaoProcessoDocumentoVoto votoRelator = null;
		SessaoPautaProcessoTrf sessaoPautaProcessoTrf = obterSessaoPautaProcesso();
		if(sessaoPautaProcessoTrf != null) {
			votoRelator = sessaoProcessoDocumentoVotoManager.recuperarVoto(sessaoPautaProcessoTrf.getSessao(), sessaoPautaProcessoTrf.getProcessoTrf(), sessaoPautaProcessoTrf.getOrgaoJulgadorRelator());
		}
		return votoRelator;
	}
	
	/**
	* Verifica se existe o documento de voto, e se este foi assinado
	* @param voto
	* @return verdadeiro se existir documento de voto e se este estiver assinado
	*/
	private boolean isVotoAssinado(SessaoProcessoDocumentoVoto voto) {
		boolean isVotoAssinado = false;
        if (voto != null && voto.getProcessoDocumento() != null) {
            AssinaturaDocumentoService assinaturaDocumentoService = ComponentUtil.getComponent(AssinaturaDocumentoService.NAME);
            isVotoAssinado = assinaturaDocumentoService.isDocumentoAssinado(voto.getProcessoDocumento());
        }
        return isVotoAssinado;
	}

	/**
	* Obtem o objeto SessaoPautaProcessoTrf, ou seja, as informações da Sessão em que o Processo foi pautado
	* @return a sessao do processo
	*/
	private SessaoPautaProcessoTrf obterSessaoPautaProcesso(){
		SessaoPautaProcessoTrf sessao = null;
		ProcessoTrf processo = obterProcesso();
		if (isProcessoIdentificado(processo)) {
			sessao = sessaoPautaProcessoTrfManager.getSessaoPautaProcessoTrfJulgado(processo);
		}
		return sessao;
	}

	public Boolean isProcessInstanceNula(){
		return processInstance == null;
	}
	
	@Override
	public Boolean isTransicaoDispensaRequeridos(String transition) {
		String transicoesComDispensaRequeridos = (String) this.recuperaVariavelTarefa("pje:fluxo:transicao:dispensaRequeridos");
		return (!StringUtil.isEmpty(transicoesComDispensaRequeridos) && br.com.infox.cliente.Util.listaContem(transicoesComDispensaRequeridos, transition));
	}

	public void setTaskInstance(TaskInstance taskInstance) {
		this.taskInstance = taskInstance;
	}
	
	@Override
	public boolean isNullTaskInstance(){
		return taskInstance == null;
	}

	@Override
	public boolean contemVariavel(String nome) {
		if(taskInstance == null){
			throw new IllegalStateException("Não há instância de tarefa disponível para recuperação da variável.");
		}
		return taskInstance.hasVariableLocally(nome);
	}
	
	@Override
	public boolean ultimoJulgamentoTipo(ProcessoTrf processoTrf, String letra) {
		boolean retorno = false;
		if (processoTrf==null){
			throw new IllegalArgumentException("O processo deve ser informado");
		}
		if (StringUtil.isEmpty(letra)){
			throw new IllegalArgumentException("A letra com o tipo deve ser informado");
		}
		SessaoPautaProcessoTrf sppt = sessaoPautaProcessoTrfManager.recuperaUltimaPautaProcesso(processoTrf,false);
		if(sppt!=null && sppt.getJulgamentoEnum()!=null){
			retorno = JulgamentoEnum.valueOf(letra.toUpperCase()).equals(sppt.getJulgamentoEnum());
		}
		return retorno;
	}

	@Override
	public boolean ultimoJulgamentoTipo(String letra) {
		return ultimoJulgamentoTipo(obterProcesso(),letra);
	}

	public Object recuperaVariavelDoFluxoRaiz(String nome) {
		if (processInstance == null) {
			throw new IllegalStateException("Não há instância de processo de negócio disponível para recuperação da variável.");
		}
		return processInstance.getRootToken().getProcessInstance().getContextInstance().getVariable(nome);
	}

	@Override
	public void gravaVariavelNoFluxoRaiz(String nome, Object value) {
		if (processInstance == null) {
			throw new IllegalStateException("Não há instância de processo de negócio disponível para gravação da variável.");
		}
		processInstance.getRootToken().getProcessInstance().getContextInstance().setVariable(nome, value);
	}

	@Override
	public void apagaVariavelDoFluxoRaiz(String nome) {
		if (processInstance == null) {
			throw new IllegalStateException("Não há instância de processo de negócio disponível para que a variável seja apagada.");
		}
		processInstance.getRootToken().getProcessInstance().getContextInstance().deleteVariable(nome);
	}
}
