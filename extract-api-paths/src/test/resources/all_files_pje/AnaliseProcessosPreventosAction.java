package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jbpm.graph.exe.ProcessInstance;

import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoTrfConexaoManager;
import br.jus.cnj.pje.nucleo.manager.TipoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualImpl;
import br.jus.cnj.pje.servicos.PrevencaoService;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrfConexao;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.enums.PrevencaoEnum;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;

@Name(AnaliseProcessosPreventosAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class AnaliseProcessosPreventosAction implements Serializable {

	@Logger
	private Log log;

	private static final long serialVersionUID = 1L;

	public static final String NAME = "analiseProcessosPreventosAction";

	public static final String PROCESSOS_CONEXOS_VARIABLE = "pje:fluxo:prevencao:processos-conexos";

	public static final String HABILITA_GRAVAR_VARIABLE	= "pje:fluxo:confirmaPrevencao";

	private List<ProcessoTrfConexao> processoTrfConexaoList;

	private Boolean	habilitaGravar;

	private Map<Integer, PrevencaoEnum>	processoTrfConexaoMap;
	
	private ProcessoTrf processoTrf = null;

	@In(create = true)
	private PrevencaoService prevencaoService;
	
	
	@Create
	@SuppressWarnings("unchecked")
	public void init() {
		TramitacaoProcessualImpl tramitacaoProcessualService = ComponentUtil.getComponent(TramitacaoProcessualImpl.class);
		processoTrf = tramitacaoProcessualService.recuperaProcesso();

		if (processoTrf == null) {
			throw new IllegalStateException("O processo não está definido");
		}

		this.processoTrfConexaoList = ComponentUtil.getComponent(ProcessoTrfConexaoManager.class).getProcessosPreventosPendentesAnalise(Integer.valueOf(processoTrf.getIdProcessoTrf()));

		this.habilitaGravar = (Boolean) tramitacaoProcessualService.recuperaVariavel(AnaliseProcessosPreventosAction.HABILITA_GRAVAR_VARIABLE);

		this.processoTrfConexaoMap = (Map<Integer, PrevencaoEnum>) tramitacaoProcessualService.recuperaVariavel(AnaliseProcessosPreventosAction.PROCESSOS_CONEXOS_VARIABLE);

		if (this.processoTrfConexaoMap == null) {
			this.processoTrfConexaoMap = new HashMap<Integer, PrevencaoEnum>();
		}

		for (ProcessoTrfConexao conexao : this.processoTrfConexaoList) {
			Integer id = Integer.valueOf(conexao.getIdProcessoTrfConexao());
			if (!this.processoTrfConexaoMap.containsKey(id)) {
				this.processoTrfConexaoMap.put(id, conexao.getPrevencao());
			}
		}
		tramitacaoProcessualService.gravaVariavel(AnaliseProcessosPreventosAction.PROCESSOS_CONEXOS_VARIABLE, this.processoTrfConexaoMap);
	}

	public String gravarDadosBanco() throws Exception {
		boolean ok = true;
		if (this.processoTrfConexaoMap != null) {
			for (Entry<Integer, PrevencaoEnum> entry : this.processoTrfConexaoMap.entrySet()) {
				PrevencaoEnum prevencao = entry.getValue();
				if ((prevencao == null) || (prevencao == PrevencaoEnum.PE)) {
					ok = false;
					break;
				}
			}
		}

		if (!ok) {
			FacesMessages.instance().add(Severity.ERROR, "Favor confirmar todos os processos preventos");
			return null;
		}

		TramitacaoProcessualImpl tramitacaoProcessualService = ComponentUtil.getComponent(TramitacaoProcessualImpl.class);
		String transicaoSaida = (String) tramitacaoProcessualService.recuperaVariavelTarefa(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);

		if ((transicaoSaida == null) || (transicaoSaida.isEmpty())) {
			throw new IllegalStateException("Transição de saída não definida!!!");
		}

		Integer idAtoProferido = (Integer) tramitacaoProcessualService.recuperaVariavel(Variaveis.ATO_PROFERIDO);
		ProcessoDocumento processoDocumento = null;

		if (idAtoProferido != null) {
			processoDocumento = ComponentUtil.getComponent(ProcessoDocumentoManager.class).findById(idAtoProferido);
		} else {
			ParametroService parametroService = ComponentUtil.getComponent(ParametroService.class);
			String idTipoDocumentoDecisao = parametroService.valueOf(Parametros.TIPODOCUMENTODECISAO);
			String idTipoDocumentoDespacho = parametroService.valueOf(Parametros.TIPODOCUMENTODESPACHO);

			if ((idTipoDocumentoDecisao == null) || (idTipoDocumentoDecisao.isEmpty())) {
				throw new IllegalStateException("Tipo de documento 'decisão' não definido!!!");
			}

			if ((idTipoDocumentoDespacho == null) || (idTipoDocumentoDespacho.isEmpty())) {
				throw new IllegalStateException("Tipo de documento 'despacho' não definido!!!");
			}

			TipoProcessoDocumentoManager tipoProcessoDocumentoManager = ComponentUtil.getComponent(TipoProcessoDocumentoManager.class);
			TipoProcessoDocumento tipoProcessoDocumentoDecisao = tipoProcessoDocumentoManager.findById(Integer.valueOf(idTipoDocumentoDecisao));
			TipoProcessoDocumento tipoProcessoDocumentoDespacho = tipoProcessoDocumentoManager.findById(Integer.valueOf(idTipoDocumentoDespacho));

			if (tipoProcessoDocumentoDecisao == null) {
				throw new IllegalStateException("Tipo de documento 'decisão' não encontrado!!!");
			}

			if (tipoProcessoDocumentoDespacho == null) {
				throw new IllegalStateException("Tipo de documento 'despacho' não encontrado!!!");
			}

			ProcessoTrf processoTrf = tramitacaoProcessualService.recuperaProcesso();

			// Recupera o ultimo processo documento
			ProcessoDocumentoManager processoDocumentoManager = ComponentUtil.getComponent(ProcessoDocumentoManager.class);
			ProcessoDocumento processoDocumentoDecisao = processoDocumentoManager.getUltimoProcessoDocumentoAssinado(tipoProcessoDocumentoDecisao, processoTrf.getProcesso());

			ProcessoDocumento processoDocumentoDespacho = processoDocumentoManager.getUltimoProcessoDocumentoAssinado(tipoProcessoDocumentoDespacho, processoTrf.getProcesso());

			processoDocumento = processoDocumentoDecisao != null ? processoDocumentoDecisao : processoDocumentoDespacho;
		}

		if (processoDocumento == null) {
			FacesMessages.instance().add(Severity.ERROR, "Despacho de prevenção não encontrado");
			return null;
		}

		if (this.processoTrfConexaoMap != null) {
			ProcessoTrfConexaoManager processoTrfConexaoManager = ComponentUtil.getComponent(ProcessoTrfConexaoManager.class);
			for (Entry<Integer, PrevencaoEnum> entry : this.processoTrfConexaoMap.entrySet()) {
				Integer id = entry.getKey();
				PrevencaoEnum prevencao = entry.getValue();
				this.log.info("Atualizando " + id + " para " + prevencao);
				processoTrfConexaoManager.defineTipoPrevencao(id, prevencao, processoDocumento);
			}
		}
		limparVariaveisFluxo(tramitacaoProcessualService);

		TaskInstanceHome.instance().end(transicaoSaida);

		this.processoTrfConexaoList = Collections.emptyList();

		return null;
	}

	public boolean existeProcessoPrevento() {
		ProcessoTrf processoTrf = ComponentUtil.getComponent(TramitacaoProcessualImpl.class).recuperaProcesso();
		int quantidade = ComponentUtil.getComponent(ProcessoTrfConexaoManager.class).getQuantidadeProcessosPreventosPendentesAnalise(Integer.valueOf(processoTrf.getIdProcessoTrf()));
		if (quantidade > 0) {
			return true;
		}
		return false;
	}
	
	
	/**
	 * Metodo que grava o tipo de prevenção, utilizado no fim da transição do processo pelo fluxo 'Processar análise de prevenção'
	 * Busca os processos conexos via variavel de fluxo:
	 * @param idAtoProferido
	 */
	public void finalizarPrevencao(Integer atoProferido) {
		try {
			if (atoProferido != null) {
				TramitacaoProcessualImpl tramitacaoProcessualService = ComponentUtil.getComponent(TramitacaoProcessualImpl.class);
				if (processoTrf != tramitacaoProcessualService.recuperaProcesso()) {
					init();
				}
				if (this.processoTrfConexaoMap != null && !this.processoTrfConexaoMap.isEmpty()) {
					for (Entry<Integer, PrevencaoEnum> entry : processoTrfConexaoMap.entrySet()) {
						Integer idProcessoTrfConexao = entry.getKey();
						PrevencaoEnum prevencao = entry.getValue();
						ProcessoDocumentoManager processoDocumentoManager = ComponentUtil.getComponent(ProcessoDocumentoManager.class);
						ProcessoDocumento processoDocumento = processoDocumentoManager.findById(atoProferido);
						ProcessoTrfConexaoManager processoTrfConexaoManager = ComponentUtil.getComponent(ProcessoTrfConexaoManager.class);
						processoTrfConexaoManager.defineTipoPrevencao(idProcessoTrfConexao,
								prevencao.equals(PrevencaoEnum.PE) ? PrevencaoEnum.RE : prevencao, processoDocumento);
					}
				}
				limparVariaveisFluxo(tramitacaoProcessualService);
			}
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
	}
	
	private void limparVariaveisFluxo(TramitacaoProcessualImpl tramitacaoProcessualService) {
		tramitacaoProcessualService.apagaVariavel(AnaliseProcessosPreventosAction.PROCESSOS_CONEXOS_VARIABLE);
		tramitacaoProcessualService.apagaVariavel(AnaliseProcessosPreventosAction.HABILITA_GRAVAR_VARIABLE);
	}

	public void atualizarMapa() {
		ComponentUtil.getComponent(TramitacaoProcessualImpl.class).gravaVariavel(AnaliseProcessosPreventosAction.PROCESSOS_CONEXOS_VARIABLE, this.processoTrfConexaoMap);
	}

	public List<ProcessoTrfConexao> getProcessoTrfConexaoList() {
		return this.processoTrfConexaoList;
	}

	public void setProcessoTrfConexaoList(final List<ProcessoTrfConexao> processoTrfConexaoList) {
		this.processoTrfConexaoList = processoTrfConexaoList;
	}

	public Boolean getHabilitaGravar() {
		return this.habilitaGravar;
	}

	public void setHabilitaGravar(Boolean habilitaGravar) {
		this.habilitaGravar = habilitaGravar;
	}

	public PrevencaoEnum[] getPrevencaoEnumValues() {
		PrevencaoEnum[] valoresEnum = { PrevencaoEnum.PR, PrevencaoEnum.RE };
		return valoresEnum;
	}

	public Map<Integer, PrevencaoEnum> getProcessoTrfConexaoMap() {
		return this.processoTrfConexaoMap;
	}

	public String getAutor(ProcessoTrf processo) {
		return this.getNomePolo(processo, ProcessoParteParticipacaoEnum.A);
	}

	public String getReu(ProcessoTrf processo) {
		return this.getNomePolo(processo, ProcessoParteParticipacaoEnum.P);
	}

	private String getNomePolo(ProcessoTrf processo, ProcessoParteParticipacaoEnum polo) {
		List<ProcessoParte> parte = null;

		if (ProcessoParteParticipacaoEnum.A.equals(polo)) {
			parte = processo.getProcessoPartePoloAtivoSemAdvogadoList();
		} else if (ProcessoParteParticipacaoEnum.P.equals(polo)) {
			parte = processo.getProcessoPartePoloPassivoSemAdvogadoList();
		} else {
			parte = processo.getListaPartePrincipal(polo);
		}

		if (parte.size() == 0) {
			return "Não definido";
		}

		StringBuilder sb = new StringBuilder();
		if(parte.get(0).getIsBaixado() || parte.get(0).getIsSuspenso()){
			sb.append("<span class=text-strike>"+ parte.get(0).getNomeParte().toUpperCase() +"</span>");
		} else {
			sb.append( parte.get(0).getNomeParte().toUpperCase() );
		}
		if (parte.size() > 1) {
			sb.append(" e outros");
		}
		return sb.toString();

	}
	
	public void verificarPrevencao(boolean apagarHistoricoDaPrevencao) {
		ProcessInstance processInstance = org.jboss.seam.bpm.TaskInstance.instance().getProcessInstance();
		processInstance.getRootToken().getProcessInstance().getContextInstance().deleteVariable(Variaveis.HOUVE_FALHA_PREVENCAO_EXTERNA);

		try {
			prevencaoService.verificarPrevencao(processoTrf, apagarHistoricoDaPrevencao);
		} catch (Exception e) {
			processInstance.getRootToken().getProcessInstance().getContextInstance().setVariable(Variaveis.HOUVE_FALHA_PREVENCAO_EXTERNA, Boolean.TRUE);
		} 
	}
	
}
