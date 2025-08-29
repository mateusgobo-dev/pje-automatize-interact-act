package br.jus.cnj.pje.view;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.contexts.Contexts;
import org.jbpm.graph.def.Transition;
import org.jbpm.taskmgmt.exe.TaskInstance;

import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.jus.cnj.pje.entidades.vo.ConsultaProcessoVO;
import br.jus.cnj.pje.entidades.vo.DocumentoAssinaturaVO;
import br.jus.cnj.pje.entidades.vo.PesquisaProcessoVO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.CaixaManager;
import br.jus.cnj.pje.nucleo.manager.PainelMagistradoSessaoBetaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.cnj.pje.nucleo.manager.TipoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualImpl;
import br.jus.cnj.pje.servicos.AtividadesLoteService;
import br.jus.pje.nucleo.entidades.Caixa;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * Componente de controle da tela do magistrado na sessÃ£o, correspondente ao
 * xhtml /Painel/painel_usuario/sessao.xhtml.
 * 
 * @author Eduardo V. Paulo
 * 
 */

@Name(PainelMagistradoSessaoBetaV3Action.NAME)
@Scope(ScopeType.PAGE)
public class PainelMagistradoSessaoBetaV3Action {
	
	public static final String  NAME  = "painelMagistradoSessaoBetaV3Action";
	
	
	@In
	private PainelMagistradoSessaoBetaManager painelMagistradoSessaoBetaManager;
	
	@In
	private transient TramitacaoProcessualImpl tramitacaoProcessualService;
	
	@In
	private ProcessoTrfManager processoTrfManager;
	
	private ConsultaProcessoVO processoSelecionado = null;
	
	private String paramValorAgrupador;
	private Boolean isAgrupadorPorTarefas = false;
	private Boolean edicao = false;
	
	private Map<String, Long> listaTarefasUsuario = new HashMap<String, Long>(0);
	private Map<String, Long> listaAssinaturasUsuario = new HashMap<String, Long>(0);
	private List<ConsultaProcessoVO> tarefasProcessos ;
	private List<Caixa> caixaTarefaList;
	private Map<Long,ConsultaProcessoVO> tarefasList = new HashMap<Long,ConsultaProcessoVO>();
	private Boolean marcaTodos = Boolean.FALSE;
	private Caixa caixaSelecionada;
	
	private UsuarioLocalizacao usuarioLocalizacaoAtual;
	private List<Integer> idsLocalizacoesFisicas;
	private OrgaoJulgadorColegiado orgaoJulgadorColegiado;
	private boolean isServidorExclusivoOJC;
	private boolean visualizaSigiloso;
	private Integer nivelAcessoSigilo;
	private Integer indiceListaSelecionado;
	private Integer idProcessoTag;
	private String nomeTag;
	private PesquisaProcessoVO criteriosPesquisa;
	private String areaSelecionada = "tarefas";
	private ProtocolarDocumentoBean protocolarDocumentoBean;
	
	@Create
	public void init(){
		Contexts.getSessionContext().set("renderTopo", false);
		usuarioLocalizacaoAtual = Authenticator.getUsuarioLocalizacaoAtual();
		idsLocalizacoesFisicas = Authenticator.getIdsLocalizacoesFilhasAtuaisList();
		isServidorExclusivoOJC = Authenticator.isServidorExclusivoColegiado();
		orgaoJulgadorColegiado = Authenticator.getOrgaoJulgadorColegiadoAtual();
		visualizaSigiloso = Authenticator.isVisualizaSigiloso();
		nivelAcessoSigilo = Authenticator.recuperarNivelAcessoUsuarioLogado();
		carregarListaTarefasMagistrado();
		carregarQuantidadeMinutasEmElaboracaoPorTipoDocumento();
		criteriosPesquisa = new PesquisaProcessoVO();
	}
		
	private Integer getIdOrgaoJulgadorColegiadoAtual(){
		Integer idOrgaoJulgadorColegiadoAtual = null;
		if(orgaoJulgadorColegiado != null){
			idOrgaoJulgadorColegiadoAtual = orgaoJulgadorColegiado.getIdOrgaoJulgadorColegiado();	
		}
		return idOrgaoJulgadorColegiadoAtual;
	}
	
	private void carregarListaTarefasMagistrado(){
		
		setListaTarefasUsuario(painelMagistradoSessaoBetaManager.carregarListaTarefasMagistrado(
				getIdOrgaoJulgadorColegiadoAtual(), isServidorExclusivoOJC,
				null,Authenticator.getIdUsuarioLogado(),
				idsLocalizacoesFisicas,
				usuarioLocalizacaoAtual.getLocalizacaoFisica().getIdLocalizacao(),
				usuarioLocalizacaoAtual.getLocalizacaoModelo().getIdLocalizacao(),
				usuarioLocalizacaoAtual.getPapel().getIdPapel(),
				visualizaSigiloso, nivelAcessoSigilo));
	}	
	
	private void  carregarQuantidadeMinutasEmElaboracaoPorTipoDocumento(){
		setListaAssinaturasUsuario(painelMagistradoSessaoBetaManager.carregarListaDocumentoAssinatura(
				getIdOrgaoJulgadorColegiadoAtual(), Authenticator.isServidorExclusivoColegiado(),
				null, Authenticator.getIdUsuarioLogado(),
				idsLocalizacoesFisicas,
				usuarioLocalizacaoAtual.getLocalizacaoModelo().getIdLocalizacao(),
				usuarioLocalizacaoAtual.getPapel().getIdPapel(), visualizaSigiloso,
				nivelAcessoSigilo, null));
	}
	/**
	  * Grava uma variável de tarefa(taskinstance)
	  * 
	  * @param nome: String, valor: Boolean
	  * @author Eduardo V. Paulo
	  * @return N/A
	  */
	public void gravarVariavelTarefa(TaskInstance ti,String nome, Boolean valor){ 
		tramitacaoProcessualService.gravaVariavelTarefa(ti,nome, valor);
	}
	 /**
	  * Apaga uma variável de tarefa(taskinstance)
	  * 
	  * @param nome: String
	  * @author Eduardo V. Paulo
	  * @return N/A
	  */
	public void apagarVariavelTarefa(TaskInstance ti,String nome){
		tramitacaoProcessualService.apagaVariavelTarefa(ti, nome);
	}

	 /**
	  * Ação do botão Conferir/Desconferir, grava ou apaga uma variável de tarefa.
	  * 
	  * @param nome: String, valor: Boolean
	  * @author Eduardo V. Paulo
	  * @return N/A
	  */
	
	public void conferirDesconferir(ConsultaProcessoVO processo){
		if(processo.getIdTaskInstance() == null){
			return;
		}
		TaskInstance taskInstance = ManagedJbpmContext.instance().getTaskInstance(processo.getIdTaskInstance());
		
		if(processo.getConferido() == null || !processo.getConferido()){
			gravarVariavelTarefa(taskInstance,Variaveis.CONFERIR_PROCESSO_ASSINATURA, true);
			processo.setConferido(true);
		}else{
			apagarVariavelTarefa(taskInstance,Variaveis.CONFERIR_PROCESSO_ASSINATURA);
			processo.setConferido(false);
		}
		try{
			recuperarInformacoesProcesso(getTarefasProcessos().get(indiceListaSelecionado+1), indiceListaSelecionado+1);
			setEdicao(false);
		}
		catch(Exception e){
			//
		}
	}
	
	 	/**
	 * Recupera as informações de acordo com o id do Processo informado.
	 * @param idProcessoTrf
	 * @throws PJeBusinessException 
	 */
	public void recuperarInformacoesProcesso(ConsultaProcessoVO processo, Integer index) throws PJeBusinessException{
		processoSelecionado = processo;
		indiceListaSelecionado = index;
	}
		
	public String getParamValorAgrupador() {
		return paramValorAgrupador;
	}
	
	public Boolean getIsAgrupadorPorTarefas() {
		return isAgrupadorPorTarefas;
	}
	
	public void setaParametrosPainelProcessos(String paramValorAgrupador, Boolean isAgrupadorPorTarefas){
		this.isAgrupadorPorTarefas = isAgrupadorPorTarefas;
		this.paramValorAgrupador = paramValorAgrupador;
		tarefasProcessos = null;
		caixaSelecionada = null;
		areaSelecionada = "tarefas";
		setCaixaTarefaList(null);
	}
	
	

	public boolean isEdicao() {
		return edicao;
	}

	public void setEdicao(boolean edicao) {
		this.edicao = edicao;
	}
	
	public ConsultaProcessoVO getProcessoSelecionado() {
		return processoSelecionado;
	}

	public Map<String, Long> getListaTarefasUsuario() {
		return listaTarefasUsuario;
	}

	public void setListaTarefasUsuario(Map<String, Long> listaTarefasUsuario) {
		this.listaTarefasUsuario = listaTarefasUsuario;
	}

	public Map<String, Long> getListaAssinaturasUsuario() {
		return listaAssinaturasUsuario;
	}

	public void setListaAssinaturasUsuario(Map<String, Long> listaAssinaturasUsuario) {
		this.listaAssinaturasUsuario = listaAssinaturasUsuario;
	}
	
	public List<ConsultaProcessoVO> recuperaProcessos() throws PJeBusinessException{
		if(StringUtil.isEmpty(paramValorAgrupador)){
			return new ArrayList<ConsultaProcessoVO>();
		}
		
		Integer idOrgaoJulgadorColegiadoAtual = null;
		if(orgaoJulgadorColegiado != null){
			idOrgaoJulgadorColegiadoAtual = orgaoJulgadorColegiado.getIdOrgaoJulgadorColegiado();	
		}
		Integer idUsuarioLogado = Authenticator.getIdUsuarioLogado();
		visualizaSigiloso = Authenticator.isVisualizaSigiloso();
		
		Integer idCaixaSelecionada = caixaSelecionada != null ? caixaSelecionada.getIdCaixa() : null;
		boolean isServidorExclusivoOJC = Authenticator.isServidorExclusivoColegiado();
		
		if(!isAgrupadorPorTarefas){
			TipoProcessoDocumentoManager tpdManager = (TipoProcessoDocumentoManager)Component.getInstance(TipoProcessoDocumentoManager.class);
			Integer idTipoDocumento = null;
			TipoProcessoDocumento tipo = tpdManager.findByDescricaoDocumento(paramValorAgrupador);
			idTipoDocumento = tipo.getIdTipoProcessoDocumento();
			List<ConsultaProcessoVO> processos = painelMagistradoSessaoBetaManager.carregarListaProcessosAssinatura(idsLocalizacoesFisicas,
					idOrgaoJulgadorColegiadoAtual, isServidorExclusivoOJC,
					null, idUsuarioLogado,
					usuarioLocalizacaoAtual.getLocalizacaoFisica().getIdLocalizacao(),
					usuarioLocalizacaoAtual.getLocalizacaoModelo().getIdLocalizacao(),
					usuarioLocalizacaoAtual.getPapel().getIdPapel(), visualizaSigiloso,idTipoDocumento,idCaixaSelecionada,criteriosPesquisa);
			return processos;
		}
		else{
			return painelMagistradoSessaoBetaManager.carregarListaProcessosTarefas(idsLocalizacoesFisicas,
					idOrgaoJulgadorColegiadoAtual, isServidorExclusivoOJC, null,
					usuarioLocalizacaoAtual.getLocalizacaoFisica().getIdLocalizacao(),
					idUsuarioLogado,usuarioLocalizacaoAtual.getLocalizacaoModelo().getIdLocalizacao(),
					usuarioLocalizacaoAtual.getPapel().getIdPapel(), visualizaSigiloso,paramValorAgrupador,idCaixaSelecionada,criteriosPesquisa);
		}
		
	}

	public List<ConsultaProcessoVO> getTarefasProcessos() throws PJeBusinessException {
		if(tarefasProcessos ==null){
			tarefasProcessos = recuperaProcessos();
		}
		return tarefasProcessos;
	}

	public void setTarefasProcessos(List<ConsultaProcessoVO> tarefasProcessos) {
		this.tarefasProcessos = tarefasProcessos;
	}

	public Integer getIndiceListaSelecionado() {
		return indiceListaSelecionado;
	}

	public void setIndiceListaSelecionado(Integer indiceListaSelecionado) {
		this.indiceListaSelecionado = indiceListaSelecionado;
	}
	
	public String getDownloadLinksIndividual(){
		if(processoSelecionado == null){
			return null;
		}
		List<ConsultaProcessoVO> processos = new ArrayList<ConsultaProcessoVO>();
		processos.add(processoSelecionado);
		return getDownloadLink(processos,true);
	}
	
	public String getDownloadLinks(){
		if(tarefasProcessos == null){
			return null;
		}
		return getDownloadLink(tarefasProcessos,false);
	}
	
	private String getDownloadLink(List<ConsultaProcessoVO> processos, Boolean force){
		StringBuilder sb = new StringBuilder();
		SimpleDateFormat dfCodData = new SimpleDateFormat("HHmmssSSS");
		for(ConsultaProcessoVO vo: processos){
			if(vo.getDocumentoAssinatura() == null || (!force && vo.getConferido() != null && !vo.getConferido())){
				continue;
			}
			createDownloadLink(vo.getDocumentoAssinatura(), sb,dfCodData);
			sb.append(',');
		}
		if (sb.length() > 0 && sb.charAt(sb.length()-1) == ',') {
			sb.setLength(sb.length() - 1);
		}
		return sb.toString();
	}
	
	private void createDownloadLink(DocumentoAssinaturaVO pd, StringBuilder sb, SimpleDateFormat dfCodData) {
		sb.append("id=");
		sb.append(String.valueOf(pd.getIdProcessoDocumento()));
		sb.append("&codIni=");
		sb.append(dfCodData.format(pd.getDataInclusao()));
		sb.append("&md5=");
		sb.append(pd.getMd5());
		sb.append("&isBin=");
		sb.append(pd.getBinario());
	}
	
	public List<String> getTransicoes(ConsultaProcessoVO processo){
		List<String> ret = new ArrayList<String>();
		if(processo == null || processo.getIdTaskInstance() == null){
			return ret;
		}
		return TaskInstanceUtil.instance().getTransitions(processo.getIdTaskInstance());
	}
	
	public List<Caixa> getCaixasPorTarefa(){
		if(paramValorAgrupador == null || getCaixaTarefaList() != null){
			return getCaixaTarefaList();
		}
				
		CaixaManager caixaManager = (CaixaManager) Component.getInstance(CaixaManager.class);
		Integer idLocalizacaoFisica = null;
		if(usuarioLocalizacaoAtual != null && usuarioLocalizacaoAtual.getLocalizacaoFisica() != null) {
			usuarioLocalizacaoAtual.getLocalizacaoFisica().getIdLocalizacao();
		}
		setCaixaTarefaList(caixaManager.getCaixasByNomeTarefa(paramValorAgrupador,idLocalizacaoFisica));
		return getCaixaTarefaList();
	}

	public List<Caixa> getCaixaTarefaList() {
		return caixaTarefaList;
	}

	public void setCaixaTarefaList(List<Caixa> caixaTarefaList) {
		this.caixaTarefaList = caixaTarefaList;
	}

	public void adicionaRemoveTask(ConsultaProcessoVO processo) {
        if (getTarefasList().containsKey(processo.getIdTaskInstance())) {
        	getTarefasList().remove(processo.getIdTaskInstance());
        } else {
        	getTarefasList().put(processo.getIdTaskInstance(),processo);
        }
    }
	
	public void adicionaRemoveTodasTarefas(){
		for (ConsultaProcessoVO processo : tarefasProcessos) {
			if(marcaTodos){
			  if (!getTarefasList().containsKey(processo.getIdTaskInstance())) {
		        	getTarefasList().put(processo.getIdTaskInstance(),processo);
			  }
			}
			else{
				if (getTarefasList().containsKey(processo.getIdTaskInstance())) {
		        	getTarefasList().remove(processo.getIdTaskInstance());
			  }
			}
		}
	}

	public Map<Long,ConsultaProcessoVO> getTarefasList() {
		return tarefasList;
	}

	public void setTarefasList(Map<Long,ConsultaProcessoVO> tarefasList) {
		this.tarefasList = tarefasList;
	}

	public Boolean getMarcaTodos() {
		return marcaTodos;
	}

	public void setMarcaTodos(Boolean marcaTodos) {
		this.marcaTodos = marcaTodos;
	}
	
	public void conferirRemoverSelecionados(Boolean conferir){
		if(conferir == null){
			return;
		}
		for (Map.Entry<Long, ConsultaProcessoVO> processo : tarefasList.entrySet()){
			if(processo == null){
				continue;
			}
			TaskInstance taskInstance = ManagedJbpmContext.instance().getTaskInstance(processo.getKey());
			if(conferir){
				gravarVariavelTarefa(taskInstance,Variaveis.CONFERIR_PROCESSO_ASSINATURA, true);
			}
			else{
				apagarVariavelTarefa(taskInstance,Variaveis.CONFERIR_PROCESSO_ASSINATURA);
			}
			processo.getValue().setConferido(conferir);
		}
	}

	public Caixa getCaixaSelecionada() {
		return caixaSelecionada;
	}

	public void setCaixaSelecionada(Caixa caixaSelecionada) {
		
		if(caixaSelecionada == null || !(caixaSelecionada.equals(getCaixaSelecionada()))){
			tarefasProcessos = null;
		}
		this.caixaSelecionada = caixaSelecionada;
	}




	public Integer getIdProcessoTag() {
		return idProcessoTag;
	}

	public void setIdProcessoTag(Integer idProcessoTag) {
		this.idProcessoTag = idProcessoTag;
	}

	public String getNomeTag() {
		return nomeTag;
	}

	public void setNomeTag(String nomeTag) {
		this.nomeTag = nomeTag;
	}

	public PesquisaProcessoVO getCriteriosPesquisa() {
		return criteriosPesquisa;
	}

	public void setCriteriosPesquisa(PesquisaProcessoVO criteriosPesquisa) {
		this.criteriosPesquisa = criteriosPesquisa;
	}
	
	public void filtrarProcessos(){
		tarefasProcessos = null;
	}

	public String getAreaSelecionada() {
		return areaSelecionada;
	}

	public void setAreaSelecionada(String areaSelecionada) {
		this.areaSelecionada = areaSelecionada;
	}

	public ProtocolarDocumentoBean getProtocolarDocumentoBean() {
		return protocolarDocumentoBean;
	}

	public void setProtocolarDocumentoBean(ProtocolarDocumentoBean protocolarDocumentoBean) {
		this.protocolarDocumentoBean = protocolarDocumentoBean;
	}
	
	public void concluirAssinaturaIndividual(){
		protocolarDocumentoBean = new ProtocolarDocumentoBean(processoSelecionado.getIdProcessoTrf(), ProtocolarDocumentoBean.EXIGE_DOCUMENTO_PRINCIPAL);
		ProcessoDocumentoManager pdm = (ProcessoDocumentoManager) Component.getInstance(ProcessoDocumentoManager.class);
		ProcessoDocumento pd = null;
		try {
			pd = pdm.findById(processoSelecionado.getDocumentoAssinatura().getIdProcessoDocumento());
		} catch (PJeBusinessException e) {
		}
		protocolarDocumentoBean.setDocumentoPrincipal(pd);
		protocolarDocumentoBean.setArquivosAssinados(PainelMagistradoAssinaturaAction.getAssinaturas());
		protocolarDocumentoBean.concluirAssinatura();
		AtividadesLoteService service = (AtividadesLoteService) Component.getInstance(AtividadesLoteService.class);
		service.lancarMovimentos(processoSelecionado.getIdTaskInstance(), null, pd.getProcessoTrf(), true);
		service.salvarUltimoAtoProferido(processoSelecionado.getIdTaskInstance());
		Transition t = service.getFrameDefaultLeavingTransition(processoSelecionado.getIdTaskInstance());
		TaskInstance ti = service.getTaskInstanceById(processoSelecionado.getIdTaskInstance());
		ti.end(t);
	}
}

