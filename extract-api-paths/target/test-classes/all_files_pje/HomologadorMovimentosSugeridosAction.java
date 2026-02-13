package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.bpm.TaskInstance;
import org.jbpm.graph.exe.ProcessInstance;

import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.infox.pje.manager.EventoAgrupamentoManager;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeRestException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.cnj.pje.nucleo.service.MovimentacaoSugeridaService;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.csjt.pje.business.service.LancadorMovimentosService;
import br.jus.pje.nucleo.dto.sinapses.MovimentacaoSugeridaResponse;
import br.jus.pje.nucleo.entidades.EventoAgrupamento;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(HomologadorMovimentosSugeridosAction.NAME)
@Scope(ScopeType.PAGE)
public class HomologadorMovimentosSugeridosAction implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "homologadorMovimentosSugeridosAction";
	
	public static final String EVENTO_RECUPERA_MOVIMENTOS_SUGERIDOS = "pje:movimento:sugestao:recuperar";

	@In(create = true, required = true)
	private TramitacaoProcessualService tramitacaoProcessualService;

	@In(create = true, required = true)
	private MovimentacaoSugeridaService movimentacaoSugeridaService;	

	@In(create = true, required = true)
	private transient TaskInstanceUtil taskInstanceUtil;
	
	/**
	 * Indica qual o identificador desta action, caso na tela seja renderizadas várias destas actions, consegue-se saber qual é esta
	 * Este valor é utilizado para indicar à TaskInstanceHome o estado desta action 
	 */
	private String actionInstanceId;
	private ProcessInstance pi;
	private ProcessoTrf processoTrf;
	private ProcessoDocumento processoDocumento;
	private Integer idProcessoDocumento;
	
	private Map<Integer, Boolean> tipoDocumentoAgrupamentoCache = new HashMap<>();
	
	private boolean possuiConfiguracaoMovimentosSugeridos = false;
	private boolean possuiAgrupamentoMovimentosValido = false;
	private MovimentacaoSugeridaResponse movimentosSugeridos = null;
	private boolean possuiMovimentosSugeridos = false;
	
	private ProcessoEvento movimentoConvictoSugerido = null;
	private boolean movimentoSugeridoJaSelecionado = false;
	
	private boolean houveAlgumaPesquisa = false;
	private boolean pesquisando = false;
	private boolean pesquisaMovimentosFalhou = false;
	
	public static final String NOME_VARIAVEL_MODELO_MOVIMENTACAO_SUGERIDA = Variaveis.PJE_FLUXO_IA_MOVIMENTACAO_SUGERIDA_PATH;
	public static final String NOME_VARIAVEL_QUANTIDADE_SUGESTOES_SOLICITADAS = Variaveis.PJE_FLUXO_IA_MOVIMENTACAO_SUGERIDA_QUANTIDADE_SUGESTOES;
	
	/**
	 * Indica que o sistema deverá selecionar automaticamente o movimento convicto sugerido se nao houver nenhum movimento previamente selecionado
	 */
	private static final boolean SELECIONAR_AUTOMATICAMENTE_MOVIMENTO_SUGERIDO_QUANDO_VAZIO = true;
	private static final boolean PERMITIR_APENAS_UM_MOVIMENTO_SELECIONADO = false;
		
	@Create
	public void load() throws Exception {
		this.geraActionInstanceId();
		this.pi = taskInstanceUtil.getProcessInstance();
		this.recuperarProcessoTrf(this.pi);
		this.recuperarIdProcessoDocumentoMinutaEmElaboracao(TaskInstance.instance());
		
		this.verificarConfiguracaoMovimentosSugeridos();
		this.verificaExistenciaAgrupamentoMovimentos();
		
		this.validarAction();
	}

	private void recuperarProcessoTrf(ProcessInstance pi) throws PJeBusinessException {
		Integer procId = (Integer) pi.getContextInstance().getVariable(Variaveis.VARIAVEL_PROCESSO);
		ProcessoJudicialManager processoJudicialManager = ComponentUtil.getComponent(ProcessoJudicialManager.NAME);
		processoTrf = processoJudicialManager.findById(procId);
	}
	
	private void recuperarIdProcessoDocumentoMinutaEmElaboracao(org.jbpm.taskmgmt.exe.TaskInstance taskInstance) {
		idProcessoDocumento = JbpmUtil.instance().recuperarIdMinutaEmElaboracao(taskInstance);
	}
	
	public ProcessoDocumento recuperarProcessoDocumento() {
		if(processoDocumento == null) {
			if(idProcessoDocumento != null) {
				try {
					processoDocumento = ComponentUtil.getComponent(ProcessoDocumentoManager.class).findById(idProcessoDocumento);
				} catch (PJeBusinessException e) {
					e.printStackTrace();
				}
			}
		}
		return processoDocumento;
	}
	
	public boolean verificaExistenciaAgrupamentoMovimentos() {
		possuiAgrupamentoMovimentosValido = false;
		if(this.recuperarProcessoDocumento() != null && this.processoDocumento.getTipoProcessoDocumento() != null) {
			if(tipoDocumentoAgrupamentoCache.get(this.processoDocumento.getTipoProcessoDocumento().getIdTipoProcessoDocumento()) != null) {
				possuiAgrupamentoMovimentosValido = tipoDocumentoAgrupamentoCache.get(this.processoDocumento.getTipoProcessoDocumento().getIdTipoProcessoDocumento());
			}else {
				if(this.processoDocumento.getTipoProcessoDocumento().getAgrupamento() != null) {
					List<EventoAgrupamento> geList = ComponentUtil.getComponent(EventoAgrupamentoManager.class).recuperarEventoAgrupamentos(
							this.processoDocumento.getTipoProcessoDocumento().getAgrupamento());
					
					possuiAgrupamentoMovimentosValido = CollectionUtilsPje.isNotEmpty(geList);
				}
			}
			tipoDocumentoAgrupamentoCache.put(this.processoDocumento.getTipoProcessoDocumento().getIdTipoProcessoDocumento(), possuiAgrupamentoMovimentosValido);
		}
		return possuiAgrupamentoMovimentosValido;
	}
	
	/**
	 * Verifica se há configuração no fluxo para comunicacao com modelo de movimentacao sugerida
	 * 
	 */
	private void verificarConfiguracaoMovimentosSugeridos() {
		boolean configuracaoMovimentacao = false;
		if(tramitacaoProcessualService.recuperaVariavel(NOME_VARIAVEL_MODELO_MOVIMENTACAO_SUGERIDA) != null) {
			if(tramitacaoProcessualService.recuperaVariavel(NOME_VARIAVEL_QUANTIDADE_SUGESTOES_SOLICITADAS) != null) {
				configuracaoMovimentacao = true;
			}
		}
		
		this.possuiConfiguracaoMovimentosSugeridos = configuracaoMovimentacao;
	}
	
	private List<ProcessoEvento> recuperarMovimentacaoSelecionada() {
		return LancadorMovimentosService.instance().getProcessoEventoListTemporario(org.jboss.seam.bpm.ProcessInstance.instance());
	}

	/**
	 * Verifica se o movimento sugerido já está selecionado
	 */
	public void verificaSelecaoMovimentoSugerido() {
		this.movimentoSugeridoJaSelecionado = false;
		if(this.movimentoConvictoSugerido != null) {
			String codEventoSugerido = this.movimentoConvictoSugerido.getEvento().getCodEvento();
			List<ProcessoEvento> movimentosSelecionados = recuperarMovimentacaoSelecionada();
			if(CollectionUtilsPje.isNotEmpty(movimentosSelecionados)) {
				for (ProcessoEvento processoEvento : movimentosSelecionados) {
					String codigoMovimento = processoEvento.getEvento().getCodEvento();
					if(codEventoSugerido.equals(codigoMovimento)) {
						this.movimentoSugeridoJaSelecionado = true;
						break;
					}
				}
			}
		}
	}


	/**
	 * Busca no servico de sugestao de movimentos um movimento sugerido para o conteúdo do documento indicado, caso haja um movimento sugerido convicto, pre-seleciona esse movimento
	 */
	public void recuperarMovimentosSugeridos() {
		if(this.possuiConfiguracaoMovimentosSugeridos && this.idProcessoDocumento > 0 && this.recuperarProcessoDocumento() != null  
				&& this.processoDocumento.getProcessoDocumentoBin() != null && !this.processoDocumento.getProcessoDocumentoBin().getModeloDocumento().isEmpty()) {

			try {
				this.movimentoConvictoSugerido = null;
				this.pesquisando = true;
				this.houveAlgumaPesquisa = true;
				this.pesquisaMovimentosFalhou = false;
				this.movimentoSugeridoJaSelecionado = false;
				
				String pathModelo = (String) tramitacaoProcessualService.recuperaVariavel(NOME_VARIAVEL_MODELO_MOVIMENTACAO_SUGERIDA);
				Long qtdClasses = (Long) tramitacaoProcessualService.recuperaVariavel(NOME_VARIAVEL_QUANTIDADE_SUGESTOES_SOLICITADAS);
				
				movimentosSugeridos = movimentacaoSugeridaService.recuperarMovimentacaoSugerida(this.processoDocumento.getIdProcessoDocumento(), pathModelo, qtdClasses.intValue());
				possuiMovimentosSugeridos = movimentosSugeridos != null && CollectionUtilsPje.isNotEmpty(movimentosSugeridos.getResultados());
				
				if(possuiMovimentosSugeridos && movimentosSugeridos.getClasseConvicto() != null && movimentosSugeridos.getClasseConvicto().getCodigo() != null) {
					LancadorMovimentosService lancadorMovimentosService = ComponentUtil.getComponent(LancadorMovimentosService.NAME);
					this.movimentoConvictoSugerido = lancadorMovimentosService.converteEmProcessoEventoByCodigoCNJ(processoTrf, processoDocumento, movimentosSugeridos.getClasseConvicto().getCodigo());
					this.verificaSelecaoMovimentoSugerido();
					
					if(SELECIONAR_AUTOMATICAMENTE_MOVIMENTO_SUGERIDO_QUANDO_VAZIO) {
						List<ProcessoEvento> movimentacaoSelecionada = recuperarMovimentacaoSelecionada();
						if(CollectionUtilsPje.isEmpty(movimentacaoSelecionada)) {
							this.selecionarMovimentoSugerido();
						}
					}
				}
			
			} catch (PJeBusinessException | PJeRestException e) {
				e.printStackTrace();
				this.pesquisaMovimentosFalhou = true;
			}
			this.pesquisando = false;
		}
	}
	
	/**
	 * Adiciona à lista de movimentos selecionados o movimento convicto sugerido
	 * - se a constante PERMITIR_APENAS_UM_MOVIMENTO_SELECIONADO = false - apenas adiciona o movimento sugerido na lista dos já selecionados
	 * - se a constante PERMITIR_APENAS_UM_MOVIMENTO_SELECIONADO = true - substitui a lista pelo movimento sugerido
	 */
	public void selecionarMovimentoSugerido() {
		if(this.movimentoConvictoSugerido != null && !this.movimentoSugeridoJaSelecionado) {
			List<ProcessoEvento> movimentosSelecionados = recuperarMovimentacaoSelecionada();
			if(CollectionUtilsPje.isEmpty(movimentosSelecionados) || !PERMITIR_APENAS_UM_MOVIMENTO_SELECIONADO) {
				if(CollectionUtilsPje.isEmpty(movimentosSelecionados)) {
					movimentosSelecionados = new ArrayList<>();
				}
				movimentosSelecionados.add(movimentoConvictoSugerido);
				this.movimentoSugeridoJaSelecionado = true;
				LancadorMovimentosService.instance().setMovimentosTemporariosProcessoEvento(org.jboss.seam.bpm.ProcessInstance.instance(), movimentosSelecionados);
			}
		}
	}

	public boolean validarAction() {
		boolean valido = true;
		String mensagem = "";
		this.gravaInformacaoValidacao(valido, mensagem);
		return valido;
	}
	
	private void gravaInformacaoValidacao(boolean valido, String mensagem) {
		TaskInstance.instance().setVariableLocally(Variaveis.PJE_PREFIXO_VARIAVEL_TAREFA_VALIDACAO_COMPONENTE_RESULTADO.concat(this.getActionInstanceId()), valido);
		TaskInstance.instance().setVariableLocally(Variaveis.PJE_PREFIXO_VARIAVEL_TAREFA_VALIDACAO_COMPONENTE_MENSAGEM.concat(this.getActionInstanceId()), mensagem);
	}
	
	public String getActionName() {
		return NAME;
	}
	
	private void geraActionInstanceId() {
		actionInstanceId = this.getActionName();
	}

	public String getActionInstanceId() {
		return actionInstanceId;
	}

	public void setActionInstanceId(String actionInstanceId) {
		this.actionInstanceId = actionInstanceId;
	}

	public TramitacaoProcessualService getTramitacaoProcessualService() {
		return tramitacaoProcessualService;
	}

	public void setTramitacaoProcessualService(TramitacaoProcessualService tramitacaoProcessualService) {
		this.tramitacaoProcessualService = tramitacaoProcessualService;
	}

	public boolean isPossuiMovimentosSugeridos() {
		return possuiMovimentosSugeridos;
	}

	public void setPossuiMovimentosSugeridos(boolean possuiMovimentosSugeridos) {
		this.possuiMovimentosSugeridos = possuiMovimentosSugeridos;
	}

	public MovimentacaoSugeridaResponse getMovimentosSugeridos() {
		return movimentosSugeridos;
	}

	public void setMovimentosSugeridos(MovimentacaoSugeridaResponse movimentosSugeridos) {
		this.movimentosSugeridos = movimentosSugeridos;
	}

	public boolean isPossuiConfiguracaoMovimentosSugeridos() {
		return possuiConfiguracaoMovimentosSugeridos;
	}

	public void setPossuiConfiguracaoMovimentosSugeridos(boolean possuiConfiguracaoMovimentosSugeridos) {
		this.possuiConfiguracaoMovimentosSugeridos = possuiConfiguracaoMovimentosSugeridos;
	}

	public ProcessoEvento getMovimentoConvictoSugerido() {
		return movimentoConvictoSugerido;
	}

	public void setMovimentoConvictoSugerido(ProcessoEvento movimentoConvictoSugerido) {
		this.movimentoConvictoSugerido = movimentoConvictoSugerido;
	}

	public boolean isPossuiAgrupamentoMovimentosValido() {
		return possuiAgrupamentoMovimentosValido;
	}

	public void setPossuiAgrupamentoMovimentosValido(boolean possuiAgrupamentoMovimentosValido) {
		this.possuiAgrupamentoMovimentosValido = possuiAgrupamentoMovimentosValido;
	}
	
	public boolean isMovimentoSugeridoJaSelecionado() {
		return movimentoSugeridoJaSelecionado;
	}

	public void setMovimentoSugeridoJaSelecionado(boolean movimentoSugeridoJaSelecionado) {
		this.movimentoSugeridoJaSelecionado = movimentoSugeridoJaSelecionado;
	}

	public boolean isHouveAlgumaPesquisa() {
		return houveAlgumaPesquisa;
	}

	public void setHouveAlgumaPesquisa(boolean houveAlgumaPesquisa) {
		this.houveAlgumaPesquisa = houveAlgumaPesquisa;
	}

	public boolean isPesquisando() {
		return pesquisando;
	}

	public void setPesquisando(boolean pesquisando) {
		this.pesquisando = pesquisando;
	}

	public boolean isPesquisaMovimentosFalhou() {
		return pesquisaMovimentosFalhou;
	}

	public void setPesquisaMovimentosFalhou(boolean pesquisaMovimentosFalhou) {
		this.pesquisaMovimentosFalhou = pesquisaMovimentosFalhou;
	}

	public Integer getIdProcessoDocumento() {
		return idProcessoDocumento;
	}

	public void setIdProcessoDocumento(Integer idProcessoDocumento) {
		if(this.idProcessoDocumento == null || !this.idProcessoDocumento.equals(idProcessoDocumento)) {
			this.processoDocumento = null;
		}
		this.idProcessoDocumento = idProcessoDocumento;
	}
}
