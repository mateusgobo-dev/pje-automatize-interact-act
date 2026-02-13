package br.com.infox.pje.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoExpedienteCentralMandadoManager;
import br.jus.cnj.pje.view.BaseAction;
import br.jus.cnj.pje.view.EntityDataModel;
import br.jus.cnj.pje.view.EntityDataModel.DataRetriever;
import br.jus.pje.nucleo.entidades.GrupoOficialJustica;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaOficialJustica;
import br.jus.pje.nucleo.entidades.ProcessoExpedienteCentralMandado;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.enums.FiltroTempoAgrupadoresEnum;
import br.jus.pje.nucleo.enums.PJeEnum;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

@Name(ProcessoExpedienteMandadoAction.NAME)
@Scope(ScopeType.PAGE)
public class ProcessoExpedienteMandadoAction extends BaseAction<ProcessoExpedienteCentralMandado>{

	private static final long serialVersionUID = -192567765010297824L;
	public static final String NAME = "processoExpedienteMandadoAction";
	private static final int QTD_LISTA_VAZIA = 0;
	private static final int QTD_ELEMENTOS_TABELA = 15;
	
	private static ParametroUtil parametroUtil = ComponentUtil.getComponent(ParametroUtil.NAME);
	private EntityDataModel<ProcessoExpedienteCentralMandado> expedientesDistribuicao;
	private EntityDataModel<ProcessoExpedienteCentralMandado> expedientesRedistribuicao;
	private EntityDataModel<ProcessoExpedienteCentralMandado> expedientesJaDistribuidos;
	private EntityDataModel<ProcessoExpedienteCentralMandado> expedientesEncerradosDistribuidor;
	private EntityDataModel<ProcessoExpedienteCentralMandado> expedientesCumprimento;
	private List<ProcessoExpedienteCentralMandado> listDistribuir = new ArrayList<ProcessoExpedienteCentralMandado>(0);
	private List<ProcessoExpedienteCentralMandado> listRedistribuir = new ArrayList<ProcessoExpedienteCentralMandado>(0);
	private List<ProcessoExpedienteCentralMandado> listDistribuidos = new ArrayList<ProcessoExpedienteCentralMandado>(0);
	
	//fitros expedientesJaDistribuidos
	private Integer numeroSequencia;
	private Integer digitoVerificador;
	private Integer ano;
	private String ramoJustica;
	private String respectivoTribunal;
	private Integer numeroOrigem;
	
	private String nomeParteExpDist;
	private GrupoOficialJustica grupoOficialExpDist;
	private PessoaOficialJustica oficialJusticaExpDist;
	private Boolean oficialJusticaAtivoExpDist = null;
	private TipoProcessoDocumento tpProcessoDocumentoExpDist;
	
	private Date dataRecebimentoMandadoInicial;
	private Date dataRecebimentoMandadoFinal;
	private Date dataDistribuicaoMandadoInicial;
	private Date dataDistribuicaoMandadoFinal;
	private Date dataProximaAudienciaInicial;
	private Date dataProximaAudienciaFinal;
	private Date dataProximaSessaoInicial;
	private Date dataProximaSessaoFinal;
	
	private FiltroTempoAgrupadoresEnum filtroTempoMandadosEncerrados = FiltroTempoAgrupadoresEnum.getFiltroPadrao();
	
	private Jurisdicao jurisdicao;
	private OrgaoJulgador orgaoJulgador;
	private OrgaoJulgadorColegiado orgaoJulgadorColegiado;
	
	//filtros expedientesCumprimento
	private String nomeParte;
	private GrupoOficialJustica grupoOficial;
	private TipoProcessoDocumento tpProcessoDocumento;
	private List<Integer> idsLocalizacoesFilhasInt = new ArrayList<Integer>();
	private UsuarioLocalizacao usuarioLocalizacao;
	
	private Boolean checkAllDistribuir = Boolean.FALSE;
	private Boolean checkAllRedistribuir = Boolean.FALSE;
	private Boolean checkAllJaDistribuido = Boolean.FALSE;

	private Boolean botaoRedistribuir = Boolean.FALSE;
	private Boolean botaoDistribuido = Boolean.FALSE;
	private Boolean botaoDistribuir = Boolean.FALSE;

	/**
	 * Método responsável pela inicialização da classe.
	 */
	@Create
	public void init() {
		idsLocalizacoesFilhasInt = Authenticator.getIdsLocalizacoesFilhasAtuaisList();
		usuarioLocalizacao = Authenticator.getUsuarioLocalizacaoAtual();
		initPesquisaRedistribuicao();
		initPesquisaDistribuicao();
		initPesquisaJaDistribuidos();
		initPesquisaEncerradosDistribuidor();
		initPesquisaCumprimento();
	}

	/**
	 * Método responsável por inicializar os campos de pesquisa do agrupador de expedientes que estão sendo cumpridos.
	 */
	public void limparPesquisa(){
		numeroSequencia = null;
		digitoVerificador = null;
		ano = null;
		ramoJustica = null;
		respectivoTribunal = null;
		numeroOrigem = null;

		nomeParte = null;
		grupoOficial = null;
		tpProcessoDocumento = null;

		nomeParteExpDist = null;
		grupoOficialExpDist = null;
		oficialJusticaExpDist = null;
		oficialJusticaAtivoExpDist = null;
		tpProcessoDocumentoExpDist = null;

		dataRecebimentoMandadoInicial = null;
		dataRecebimentoMandadoFinal = null;
		dataDistribuicaoMandadoInicial = null;
		dataDistribuicaoMandadoFinal = null;
		dataProximaAudienciaInicial = null;
		dataProximaAudienciaFinal = null;
		dataProximaSessaoInicial = null;
		dataProximaSessaoFinal = null;
		filtroTempoMandadosEncerrados = FiltroTempoAgrupadoresEnum.getFiltroPadrao();

		jurisdicao = null;
		orgaoJulgador = null;
		orgaoJulgadorColegiado = null;
	}	
	
	/**
	 * Método responsável por pesquisar os expedientes para redistribuição.
	 */
	public void initPesquisaRedistribuicao() {
		DataRetriever<ProcessoExpedienteCentralMandado> dataRetriever = new ExpedientesCentralMandadosRetriever(
				getProcessoExpedienteCentralMandadoManager(), this.facesMessages, TipoAgrupadorExpedienteMandadoEnum.REDISTRIBUICAO, null, null);
		
		this.expedientesRedistribuicao = new EntityDataModel<ProcessoExpedienteCentralMandado>(
				ProcessoExpedienteCentralMandado.class, this.facesContext, dataRetriever);
	}
	
	/**
	 * Método responsável por pesquisar os expedientes para distribuição.
	 */
	public void initPesquisaDistribuicao() {
		DataRetriever<ProcessoExpedienteCentralMandado> dataRetriever = new ExpedientesCentralMandadosRetriever(
				getProcessoExpedienteCentralMandadoManager(), this.facesMessages, TipoAgrupadorExpedienteMandadoEnum.DISTRUBUICAO, null, null);
		
		this.expedientesDistribuicao = new EntityDataModel<ProcessoExpedienteCentralMandado>(
				ProcessoExpedienteCentralMandado.class, this.facesContext, dataRetriever);
	}
	
	/**
	 * Método responsável por pesquisar os expedientes já distribuídos.
	 */
	public void initPesquisaJaDistribuidos() {
		DataRetriever<ProcessoExpedienteCentralMandado> dataRetriever = new ExpedientesCentralMandadosRetriever(
				getProcessoExpedienteCentralMandadoManager(), this.facesMessages, TipoAgrupadorExpedienteMandadoEnum.JA_DISTRIBUIDO, null, null);
		
		this.expedientesJaDistribuidos = new EntityDataModel<ProcessoExpedienteCentralMandado>(
				ProcessoExpedienteCentralMandado.class, this.facesContext, dataRetriever);
	}

	/**
	 * Método responsável por pesquisar os expedientes encerrado.
	 */
	public void initPesquisaEncerradosDistribuidor() {
		DataRetriever<ProcessoExpedienteCentralMandado> dataRetriever = new ExpedientesCentralMandadosRetriever(
				getProcessoExpedienteCentralMandadoManager(), this.facesMessages, TipoAgrupadorExpedienteMandadoEnum.ENCERRADOS, null, null);
		
		this.expedientesEncerradosDistribuidor = new EntityDataModel<ProcessoExpedienteCentralMandado>(
				ProcessoExpedienteCentralMandado.class, this.facesContext, dataRetriever);
	}

	/**
	 * Método responsável por pesquisar os expedientes do oficial de justiça logado.
	 */
	public void initPesquisaCumprimento() {
		DataRetriever<ProcessoExpedienteCentralMandado> dataRetriever = new ExpedientesCentralMandadosRetriever(
				getProcessoExpedienteCentralMandadoManager(), this.facesMessages, TipoAgrupadorExpedienteMandadoEnum.CUMPRIMENTO, null, null);
		
		this.expedientesCumprimento = new EntityDataModel<ProcessoExpedienteCentralMandado>(
				ProcessoExpedienteCentralMandado.class, this.facesContext, dataRetriever);
	}
	
	public void pesquisarMandadosRedistribuicao() {
		this.pesquisarMandados(TipoAgrupadorExpedienteMandadoEnum.REDISTRIBUICAO);
	}
	
	public void pesquisarMandadosDistribuicao() {
		this.pesquisarMandados(TipoAgrupadorExpedienteMandadoEnum.DISTRUBUICAO);
	}
	
	public void pesquisarMandadosJaDistribuidos() {
		this.pesquisarMandados(TipoAgrupadorExpedienteMandadoEnum.JA_DISTRIBUIDO);
	}

	public void pesquisarMandadosEncerradosDistribuidor() {
		this.pesquisarMandados(TipoAgrupadorExpedienteMandadoEnum.ENCERRADOS);
	}

	public void pesquisarMandadosCumprimento() {
		this.pesquisarMandados(TipoAgrupadorExpedienteMandadoEnum.CUMPRIMENTO);
	}
	
	private void pesquisarMandados(TipoAgrupadorExpedienteMandadoEnum opcao) {
		if (TipoAgrupadorExpedienteMandadoEnum.REDISTRIBUICAO.equals(opcao)) {
			this.getExpedientesRedistribuicao().setRefreshPage(Boolean.TRUE);
		}else {
			if (TipoAgrupadorExpedienteMandadoEnum.DISTRUBUICAO.equals(opcao)) {
				this.getExpedientesDistribuicao().setRefreshPage(Boolean.TRUE);
			}else {
				if (TipoAgrupadorExpedienteMandadoEnum.JA_DISTRIBUIDO.equals(opcao)) {
					this.getExpedientesJaDistribuidos().setRefreshPage(Boolean.TRUE);
				}else {
					if (TipoAgrupadorExpedienteMandadoEnum.ENCERRADOS.equals(opcao)) {
						this.getExpedientesJaDistribuidos().setRefreshPage(Boolean.TRUE);
					}else {
						if (TipoAgrupadorExpedienteMandadoEnum.CUMPRIMENTO.equals(opcao)) {
							this.getExpedientesCumprimento().setRefreshPage(Boolean.TRUE);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Método responsável por aplicar os filtros ao agrupador de "Expedientes para Cumprimento".
	 * 
	 * @param search {@link Search}.
	 * @throws NoSuchFieldException Caso o campo declarado não esteja mapeado.
	 */
	private void aplicarFiltroExpedientesParaCumprimento(Search search) throws NoSuchFieldException {
		this.aplicarFiltrosGeraisPainel(search);
		
		if(this.grupoOficial != null){
			search.addCriteria(Criteria.equals("pessoaGrupoOficialJustica.grupoOficialJustica", grupoOficial));
		}
		
		if (this.tpProcessoDocumento != null) {
			search.addCriteria(Criteria.equals("processoExpediente.tipoProcessoDocumento", tpProcessoDocumento));
		}
	}
	
	/**
	 *	Identifica os valores informados para o número do processo e adiciona à pesquisa de expedientes 
	 * @param search
	 * @throws NoSuchFieldException 
	 */
	private void aplicarFiltroNumeroProcesso(Search search) throws NoSuchFieldException {
		if(this.numeroSequencia != null){
			search.addCriteria(Criteria.equals("processoExpediente.processoTrf.numeroSequencia", numeroSequencia));
		}

		if(this.digitoVerificador != null){
			search.addCriteria(Criteria.equals("processoExpediente.processoTrf.numeroDigitoVerificador", digitoVerificador));
		}

		if(this.ano != null){
			search.addCriteria(Criteria.equals("processoExpediente.processoTrf.ano", ano));
		}
		
		if(this.ramoJustica != null && !this.ramoJustica.isEmpty() && this.respectivoTribunal != null && !this.respectivoTribunal.isEmpty()){
			Integer numeroOrgaoJustica = Integer.parseInt(this.ramoJustica)*100 + Integer.parseInt(this.respectivoTribunal);
			search.addCriteria(Criteria.equals("processoExpediente.processoTrf.numeroOrgaoJustica", numeroOrgaoJustica));
		}
		
		if(this.numeroOrigem != null){
			search.addCriteria(Criteria.equals("processoExpediente.processoTrf.numeroOrigem", numeroOrigem));
		}
	}
	
	private void aplicarFiltroOjJurisdicao(Search search) throws NoSuchFieldException {
		if(this.jurisdicao != null) {
			search.addCriteria(Criteria.equals("processoExpediente.processoTrf.jurisdicao", this.jurisdicao));
		}
		
		if(this.orgaoJulgadorColegiado != null) {
			search.addCriteria(Criteria.equals("processoExpediente.processoTrf.orgaoJulgadorColegiado", this.orgaoJulgadorColegiado));
		}
		
		if(this.orgaoJulgador != null) {
			search.addCriteria(Criteria.equals("processoExpediente.processoTrf.orgaoJulgador", this.orgaoJulgador));
		}
	}
	
	private void aplicarFiltroPrazoAudienciaSessao(Search search) throws NoSuchFieldException {
		if(this.dataProximaAudienciaInicial != null || this.dataProximaAudienciaFinal != null) {
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT 1 FROM ProcessoAudiencia pa ");
			sql.append(" WHERE pa.statusAudiencia = 'M'");
			sql.append(" AND pa.inAtivo = true");
			sql.append(" AND pa.dtCancelamento IS NULL");
			sql.append(" AND pa.processoTrf.idProcessoTrf = o.processoExpediente.processoTrf.idProcessoTrf");
			sql.append(" AND pa.dtInicio >= '"+DateUtil.getDataAtual()+"'");

			if (this.dataProximaAudienciaInicial != null) {
				sql.append(" AND pa.dtInicio >= '"+new java.sql.Date(this.dataProximaAudienciaInicial.getTime())+" 00:00:00'");
			}
			if (this.dataProximaAudienciaFinal != null) {
				sql.append(" AND pa.dtInicio <= '"+new java.sql.Date(this.dataProximaAudienciaFinal.getTime())+" 23:59:59'");
			}
			search.addCriteria(Criteria.and(Criteria.exists(sql.toString())));
		}
		
		if(this.dataProximaSessaoInicial != null || this.dataProximaSessaoFinal != null) {
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT 1 FROM SessaoPautaProcessoTrf spp ");
			sql.append(" WHERE spp.dataExclusaoProcessoTrf IS NULL");
			sql.append(" AND spp.sessao.dataRealizacaoSessao IS NULL");
			sql.append(" AND spp.processoTrf.idProcessoTrf = o.processoExpediente.processoTrf.idProcessoTrf");
			sql.append(" AND spp.sessao.dataSessao >= '"+DateUtil.getDataAtual()+"'");

			if(this.dataProximaSessaoInicial != null) {
				sql.append(" AND spp.sessao.dataSessao >= '"+new java.sql.Date(this.dataProximaSessaoInicial.getTime())+" 00:00:00'");
				
			}
			
			if(this.dataProximaSessaoFinal != null) {
				sql.append(" AND spp.sessao.dataSessao <= '"+new java.sql.Date(this.dataProximaSessaoFinal.getTime())+" 23:59:59'");
			}
			search.addCriteria(Criteria.and(Criteria.exists(sql.toString())));
		}
	}
	
	private void aplicarFiltroDatasMandado(Search search) throws NoSuchFieldException {
		if(this.dataRecebimentoMandadoInicial != null) {
			search.addCriteria(Criteria.greater("dtRecebido", this.dataRecebimentoMandadoInicial));
		}
		
		if(this.dataRecebimentoMandadoFinal != null) {
			search.addCriteria(Criteria.less("dtRecebido", this.dataRecebimentoMandadoFinal));
		}
		
		if(this.dataDistribuicaoMandadoInicial != null) {
			search.addCriteria(Criteria.greater("dtDistribuicaoExpediente", this.dataDistribuicaoMandadoInicial));
		}
		
		if(this.dataDistribuicaoMandadoFinal != null) {
			search.addCriteria(Criteria.less("dtDistribuicaoExpediente", this.dataDistribuicaoMandadoFinal));
		}
	}
	
	private void aplicarFiltrosGeraisPainel(Search search) throws NoSuchFieldException {
		this.aplicarFiltroNumeroProcesso(search);
		this.aplicarFiltroOjJurisdicao(search);
		this.aplicarFiltroPrazoAudienciaSessao(search);
		this.aplicarFiltroDatasMandado(search);
	}
	
	/**
	 * Método responsável por aplicar os filtros ao agrupador de "Expedientes já distribuídos".
	 * 
	 * @param search {@link Search}.
	 * @throws NoSuchFieldException Caso o campo declarado não esteja mapeado.
	 */
	private void aplicarFiltroExpedientesJaDistribuidos(Search search) throws NoSuchFieldException {
		this.aplicarFiltrosGeraisPainel(search);
				
		if(this.grupoOficialExpDist != null){
			search.addCriteria(Criteria.equals("pessoaGrupoOficialJustica.grupoOficialJustica", grupoOficialExpDist));
		}
		
		if (this.oficialJusticaExpDist != null) {
			search.addCriteria(Criteria.equals("pessoaGrupoOficialJustica.pessoa", oficialJusticaExpDist.getPessoa()));
		}
		
		if (this.oficialJusticaAtivoExpDist != null) {
			Criteria condicaoOficialAtivo = Criteria.or(
				Criteria.notEquals("pessoaGrupoOficialJustica.pessoa.especializacoes", PessoaFisica.OFJ), 
				Criteria.equals("pessoaGrupoOficialJustica.ativo", oficialJusticaAtivoExpDist));
			
			search.addCriteria(condicaoOficialAtivo);
		}
		
		if (this.tpProcessoDocumentoExpDist != null) {
			search.addCriteria(Criteria.equals("processoExpediente.tipoProcessoDocumento", tpProcessoDocumentoExpDist));
		}
	}

	/**
	 * Método responsável por aplicar os filtros ao agrupador de "Expedientes para distribuicao".
	 * 
	 * @param search {@link Search}.
	 * @throws NoSuchFieldException Caso o campo declarado não esteja mapeado.
	 */
	private void aplicarFiltroExpedientesParaDistribuicao(Search search) throws NoSuchFieldException {
		this.aplicarFiltrosGeraisPainel(search);
		
		if (this.tpProcessoDocumentoExpDist != null) {
			search.addCriteria(Criteria.equals("processoExpediente.tipoProcessoDocumento", tpProcessoDocumentoExpDist));
		}
	}

	/**
	 * Método responsável por aplicar check a todos os Expedientes do agrupador "Expedientes para redistribuição"
	 */
	public void checkAllListRedistribuir(){
		if (checkAllRedistribuir){
			limparCheckRedistribuir();	
			for (ProcessoExpedienteCentralMandado procExpedienteCentralMandado : expedientesRedistribuicao.getPage()){

				procExpedienteCentralMandado.setCheck(Boolean.TRUE);
				criarListaRedistribuir(procExpedienteCentralMandado);
			}
			checkAllRedistribuir = Boolean.TRUE;
		}else{
			limparCheckRedistribuir();
		}
	}
	
	public void limparCheckRedistribuir(){
		for(ProcessoExpedienteCentralMandado processoExpedienteCentralMandado :this.listRedistribuir){
			processoExpedienteCentralMandado.setCheck(Boolean.FALSE);
		}
		listRedistribuir = new ArrayList<ProcessoExpedienteCentralMandado>(0);
		checkAllRedistribuir = Boolean.FALSE;
	}
	
	/**
	 * Método responsável por aplicar check a todos os Expedientes do agrupador "Expedientes já distribuidos"
	 */
	public void checkAllListDistribuidos(){
		if (checkAllJaDistribuido){
			limparCheckDistribuidos();
			for (ProcessoExpedienteCentralMandado procExpedienteCentralMandado : expedientesJaDistribuidos.getPage()){
				if(!procExpedienteCentralMandado.getProcessoExpediente().getProcessoTrf().getInBloqueioMigracao()) {
					procExpedienteCentralMandado.setCheck(Boolean.TRUE);
					criarListaDistribuidos(procExpedienteCentralMandado);
				}else {
					procExpedienteCentralMandado.setCheck(Boolean.FALSE);
				}				
			}
			checkAllJaDistribuido = Boolean.TRUE;
		
		}else{
			limparCheckDistribuidos();
		}
	}
	
	public void limparCheckDistribuidos(){
		for(ProcessoExpedienteCentralMandado processoExpedienteCentralMandado :this.listDistribuidos){
			processoExpedienteCentralMandado.setCheck(Boolean.FALSE);
		}

		listDistribuidos = new ArrayList<ProcessoExpedienteCentralMandado>(0);
		checkAllJaDistribuido = Boolean.FALSE;
	}
	
	/**
	 * Método responsável por aplicar check a todos os expedientes do agrupador "Expedientes para distribuição"
	 */
	public void checkAllListDistribuir(){
		if (checkAllDistribuir){
			limparCheckDistribuir();
			for (ProcessoExpedienteCentralMandado procExpedienteCentralMandado : expedientesDistribuicao.getPage()){
				procExpedienteCentralMandado.setCheck(Boolean.TRUE);
				criarListaDistribuir(procExpedienteCentralMandado);
			}
			checkAllDistribuir = Boolean.TRUE;
		}
		else{
			limparCheckDistribuir();
		}
	}

	public void limparCheckDistribuir(){
		for(ProcessoExpedienteCentralMandado processoExpedienteCentralMandado :this.listDistribuir){
			processoExpedienteCentralMandado.setCheck(Boolean.FALSE);
		}
		listDistribuir = new ArrayList<ProcessoExpedienteCentralMandado>(0);
		checkAllDistribuir = Boolean.FALSE;
	}
	
	/**
	 * Cria uma Lista de expedientes para redistribuição
	 * 
	 * @param obj - Objeto do tipo ProcessoExpedienteCentralMandado
	 */
	public void criarListaRedistribuir(ProcessoExpedienteCentralMandado obj){
		if (!listRedistribuir.contains(obj)){
			listRedistribuir.add(obj);
		}
		else if (listRedistribuir.contains(obj)){
			listRedistribuir.remove(obj);
		}
	}
	
	/**
	 * Cria uma Lista de expedientes para distribuição/redistribuição
	 * 
	 * @param obj - Objeto do tipo ProcessoExpedienteCentralMandado
	 */
	public void criarListaDistribuidos(ProcessoExpedienteCentralMandado obj){
		if (!listDistribuidos.contains(obj)){
			if(!obj.getProcessoExpediente().getProcessoTrf().getInBloqueioMigracao()) {
				listDistribuidos.add(obj);
			}else {
				obj.setCheck(Boolean.FALSE);
			}			
		}
		else if (listDistribuidos.contains(obj)){
			listDistribuidos.remove(obj);
		}
	}
	
	/**
	 * Cria uma Lista de expedientes para distribuição/redistribuição
	 * 
	 * @param obj - Objeto do tipo ProcessoExpedienteCentralMandado
	 */
	public void criarListaDistribuir(ProcessoExpedienteCentralMandado obj){
		if (!listDistribuir.contains(obj)){
			listDistribuir.add(obj);
		}
		else if (listDistribuir.contains(obj)){
			listDistribuir.remove(obj);
		}
	}

	/**
	 * Método que decide qual agrupador deve se renderizada após conclusão de uma distribuição/redistribuição.
	 * @return Nome da Div que será renderizada.
	 */
	public String atualizarTabelas(){
		if(botaoRedistribuir){
			checkAllRedistribuir = false;
			botaoRedistribuir = false;
			this.pesquisarMandadosRedistribuicao();
		}
		else if(botaoDistribuir){
			checkAllDistribuir = false;
			botaoDistribuir = false;
			this.pesquisarMandadosDistribuicao();
		}
		else if(botaoDistribuido){
			checkAllJaDistribuido = false;
			botaoDistribuido = false;
			this.pesquisarMandadosJaDistribuidos();
		}else {
			this.pesquisarMandadosCumprimento();
		}
		return "panelExpedientes";
	}
	
	@Override
	protected BaseManager<ProcessoExpedienteCentralMandado> getManager() {
		return ComponentUtil.getComponent(ProcessoExpedienteCentralMandadoManager.class);
	}
	
	private ProcessoExpedienteCentralMandadoManager getProcessoExpedienteCentralMandadoManager() {
		return ComponentUtil.getComponent(ProcessoExpedienteCentralMandadoManager.class);
	}

	@Override
	public EntityDataModel<ProcessoExpedienteCentralMandado> getModel() {
		return null;
	}
	
	/* Get e Set */
	
	public EntityDataModel<ProcessoExpedienteCentralMandado> getExpedientesRedistribuicao() {
		return expedientesRedistribuicao;
	}

	public void setExpedientesRedistribuicao(
			EntityDataModel<ProcessoExpedienteCentralMandado> expedientesRedistribuicao) {
		this.expedientesRedistribuicao = expedientesRedistribuicao;
	}

	public EntityDataModel<ProcessoExpedienteCentralMandado> getExpedientesJaDistribuidos() {
		return expedientesJaDistribuidos;
	}

	public void setExpedientesJaDistribuidos(
			EntityDataModel<ProcessoExpedienteCentralMandado> expedientesJaDistribuidos) {
		this.expedientesJaDistribuidos = expedientesJaDistribuidos;
	}
	
	public EntityDataModel<ProcessoExpedienteCentralMandado> getExpedientesEncerradosDistribuidor() {
		return expedientesEncerradosDistribuidor;
	}

	public void setExpedientesEncerradosDistribuidor(
			EntityDataModel<ProcessoExpedienteCentralMandado> expedientesEncerradosDistribuidor) {
		this.expedientesEncerradosDistribuidor = expedientesEncerradosDistribuidor;
	}

	public EntityDataModel<ProcessoExpedienteCentralMandado> getExpedientesCumprimento() {
		return expedientesCumprimento;
	}

	public void setExpedientesCumprimento(
			EntityDataModel<ProcessoExpedienteCentralMandado> expedientesCumprimento) {
		this.expedientesCumprimento = expedientesCumprimento;
	}

	public EntityDataModel<ProcessoExpedienteCentralMandado> getExpedientesDistribuicao() {
		return expedientesDistribuicao;
	}

	public void setExpedientesDistribuicao(
			EntityDataModel<ProcessoExpedienteCentralMandado> expedientesDistribuicao) {
		this.expedientesDistribuicao = expedientesDistribuicao;
	}

	public String getNomeParte() {
		return nomeParte;
	}

	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}

	public GrupoOficialJustica getGrupoOficial() {
		return grupoOficial;
	}

	public void setGrupoOficial(GrupoOficialJustica grupoOficial) {
		this.grupoOficial = grupoOficial;
	}

	public TipoProcessoDocumento getTpProcessoDocumento() {
		return tpProcessoDocumento;
	}

	public void setTpProcessoDocumento(TipoProcessoDocumento tpProcessoDocumento) {
		this.tpProcessoDocumento = tpProcessoDocumento;
	}
	
	public Integer getNumeroSequencia() {
		return numeroSequencia;
	}

	public void setNumeroSequencia(Integer numeroSequencia) {
		this.numeroSequencia = numeroSequencia;
	}

	public Integer getDigitoVerificador() {
		return digitoVerificador;
	}

	public void setDigitoVerificador(Integer digitoVerificador) {
		this.digitoVerificador = digitoVerificador;
	}

	public Integer getAno() {
		return ano;
	}

	public void setAno(Integer ano) {
		this.ano = ano;
	}

	public String getRamoJustica() {
		return ramoJustica;
	}

	public void setRamoJustica(String ramoJustica) {
		this.ramoJustica = ramoJustica;
	}

	public String getRespectivoTribunal() {
		return respectivoTribunal;
	}

	public void setRespectivoTribunal(String respectivoTribunal) {
		this.respectivoTribunal = respectivoTribunal;
	}
	
	public boolean getIsAmbienteColegiado() {
		return !parametroUtil.isPrimeiroGrau();
	}

	public Integer getNumeroOrigem() {
		return numeroOrigem;
	}

	public void setNumeroOrigem(Integer numeroOrigem) {
		this.numeroOrigem = numeroOrigem;
	}

	public String getNomeParteExpDist() {
		return nomeParteExpDist;
	}

	public void setNomeParteExpDist(String nomeParteExpDist) {
		this.nomeParteExpDist = nomeParteExpDist;
	}

	public GrupoOficialJustica getGrupoOficialExpDist() {
		return grupoOficialExpDist;
	}

	public void setGrupoOficialExpDist(GrupoOficialJustica grupoOficialExpDist) {
		this.grupoOficialExpDist = grupoOficialExpDist;
	}

	public PessoaOficialJustica getOficialJusticaExpDist() {
		return oficialJusticaExpDist;
	}

	public void setOficialJusticaExpDist(
			PessoaOficialJustica oficialJusticaExpDist) {
		this.oficialJusticaExpDist = oficialJusticaExpDist;
	}

	public Boolean getOficialJusticaAtivoExpDist() {
		return oficialJusticaAtivoExpDist;
	}

	public void setOficialJusticaAtivoExpDist(Boolean oficialJusticaAtivoExpDist) {
		this.oficialJusticaAtivoExpDist = oficialJusticaAtivoExpDist;
	}

	public TipoProcessoDocumento getTpProcessoDocumentoExpDist() {
		return tpProcessoDocumentoExpDist;
	}

	public void setTpProcessoDocumentoExpDist(
			TipoProcessoDocumento tpProcessoDocumentoExpDist) {
		this.tpProcessoDocumentoExpDist = tpProcessoDocumentoExpDist;
	}
	
	public Date getDataRecebimentoMandadoInicial() {
		return dataRecebimentoMandadoInicial;
	}

	public void setDataRecebimentoMandadoInicial(Date dataRecebimentoMandadoInicial) {
		this.dataRecebimentoMandadoInicial = dataRecebimentoMandadoInicial;
	}

	public Date getDataRecebimentoMandadoFinal() {
		return dataRecebimentoMandadoFinal;
	}

	public void setDataRecebimentoMandadoFinal(Date dataRecebimentoMandadoFinal) {
		this.dataRecebimentoMandadoFinal = DateUtil.getEndOfDay(dataRecebimentoMandadoFinal);
	}

	public Date getDataDistribuicaoMandadoInicial() {
		return dataDistribuicaoMandadoInicial;
	}

	public void setDataDistribuicaoMandadoInicial(Date dataDistribuicaoMandadoInicial) {
		this.dataDistribuicaoMandadoInicial = dataDistribuicaoMandadoInicial;
	}

	public Date getDataDistribuicaoMandadoFinal() {
		return dataDistribuicaoMandadoFinal;
	}

	public void setDataDistribuicaoMandadoFinal(Date dataDistribuicaoMandadoFinal) {
		this.dataDistribuicaoMandadoFinal = DateUtil.getEndOfDay(dataDistribuicaoMandadoFinal);
	}

	public Date getDataProximaAudienciaInicial() {
		return dataProximaAudienciaInicial;
	}

	public void setDataProximaAudienciaInicial(Date dataProximaAudienciaInicial) {
		this.dataProximaAudienciaInicial = dataProximaAudienciaInicial;
	}

	public Date getDataProximaAudienciaFinal() {
		return dataProximaAudienciaFinal;
	}

	public void setDataProximaAudienciaFinal(Date dataProximaAudienciaFinal) {
		this.dataProximaAudienciaFinal = DateUtil.getEndOfDay(dataProximaAudienciaFinal);
	}

	public Date getDataProximaSessaoInicial() {
		return dataProximaSessaoInicial;
	}

	public void setDataProximaSessaoInicial(Date dataProximaSessaoInicial) {
		this.dataProximaSessaoInicial = dataProximaSessaoInicial;
	}

	public Date getDataProximaSessaoFinal() {
		return dataProximaSessaoFinal;
	}

	public void setDataProximaSessaoFinal(Date dataProximaSessaoFinal) {
		this.dataProximaSessaoFinal = DateUtil.getEndOfDay(dataProximaSessaoFinal);
	}

	public FiltroTempoAgrupadoresEnum getFiltroTempoMandadosEncerrados() {
		return filtroTempoMandadosEncerrados;
	}

	public void setFiltroTempoMandadosEncerrados(FiltroTempoAgrupadoresEnum filtroTempoMandadosEncerrados) {
		this.filtroTempoMandadosEncerrados = filtroTempoMandadosEncerrados;
	}

	public Jurisdicao getJurisdicao() {
		return jurisdicao;
	}

	public void setJurisdicao(Jurisdicao jurisdicao) {
		this.jurisdicao = jurisdicao;
	}

	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiado() {
		return orgaoJulgadorColegiado;
	}

	public void setOrgaoJulgadorColegiado(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}

	public Boolean getCheckAllDistribuir() {
		return checkAllDistribuir;
	}

	public void setCheckAllDistribuir(Boolean checkAllDistribuir) {
		this.checkAllDistribuir = checkAllDistribuir;
	}

	public Boolean getCheckAllRedistribuir() {
		return checkAllRedistribuir;
	}

	public void setCheckAllRedistribuir(Boolean checkAllRedistribuir) {
		this.checkAllRedistribuir = checkAllRedistribuir;
	}

	public List<ProcessoExpedienteCentralMandado> getListDistribuir() {
		return listDistribuir;
	}

	public void setListDistribuir(
			List<ProcessoExpedienteCentralMandado> listDistribuir) {
		this.listDistribuir = listDistribuir;
	}

	public List<ProcessoExpedienteCentralMandado> getListRedistribuir() {
		return listRedistribuir;
	}

	public void setListRedistribuir(
			List<ProcessoExpedienteCentralMandado> listRedistribuir) {
		this.listRedistribuir = listRedistribuir;
	}

	public List<ProcessoExpedienteCentralMandado> getListDistribuidos() {
		return listDistribuidos;
	}

	public void setListDistribuidos(
			List<ProcessoExpedienteCentralMandado> listDistribuidos) {
		this.listDistribuidos = listDistribuidos;
	}

	
	public Boolean getCheckAllJaDistribuido() {
		return checkAllJaDistribuido;
	}

	public void setCheckAllJaDistribuido(Boolean checkAllJaDistribuido) {
		this.checkAllJaDistribuido = checkAllJaDistribuido;
	}
	
	public Boolean getBotaoRedistribuir() {
		return botaoRedistribuir;
	}

	public void setBotaoRedistribuir(Boolean botaoRedistribuir) {
		this.botaoRedistribuir = botaoRedistribuir;
	}

	public Boolean getBotaoDistribuido() {
		return botaoDistribuido;
	}

	public void setBotaoDistribuido(Boolean botaoDistribuido) {
		this.botaoDistribuido = botaoDistribuido;
	}

	public Boolean getBotaoDistribuir() {
		return botaoDistribuir;
	}

	public void setBotaoDistribuir(Boolean botaoDistribuir) {
		this.botaoDistribuir = botaoDistribuir;
	}
	
	/* FIM - Get e Set */

	/**
	 * Classe interna responsável pela pesquisa de expedientes que estão na Central de Mandados.
	 * Essa classe implementa {@link DataRetriever}, para que seja possivel navegar entre os resultados de uma pesquisa
	 */
	private class ExpedientesCentralMandadosRetriever implements DataRetriever<ProcessoExpedienteCentralMandado>{
		private Long count;
		private FacesMessages facesMessages;		
		private ProcessoExpedienteCentralMandadoManager processoExpedienteCentralMandadoManager;
		private TipoAgrupadorExpedienteMandadoEnum tipoAgrupadorExpedienteMandado;
		private String attributeOrder;
		private Order order;
		private List<ProcessoExpedienteCentralMandado> expedientesMandadosList;
		
		public ExpedientesCentralMandadosRetriever(ProcessoExpedienteCentralMandadoManager processoExpedienteCentralMandadoManager, 
				FacesMessages facesMessages, TipoAgrupadorExpedienteMandadoEnum tipoAgrupadorExpedienteMandado, String attributeOrder, Order order){
			
			this.processoExpedienteCentralMandadoManager = processoExpedienteCentralMandadoManager;
			this.facesMessages = facesMessages;
			this.attributeOrder = attributeOrder;
			this.order = order;
			this.tipoAgrupadorExpedienteMandado = tipoAgrupadorExpedienteMandado;
		}
		
		@Override
		public Object getId(ProcessoExpedienteCentralMandado obj) {
			return processoExpedienteCentralMandadoManager.getId(obj);
		}

		@Override
		public ProcessoExpedienteCentralMandado findById(Object id) throws Exception {
			return processoExpedienteCentralMandadoManager.findById(id);
		}

		@Override
		public List<ProcessoExpedienteCentralMandado> list(Search search) {
			List<ProcessoExpedienteCentralMandado> expedientesMandadosList = Collections.emptyList();
			try {
				search.getCriterias().clear();
				if(attributeOrder != null){
					search.addOrder(attributeOrder, order);
				}
				search.setMax(QTD_ELEMENTOS_TABELA);
				expedientesMandadosList = pesquisarExpedientesPelaSituacao(this.tipoAgrupadorExpedienteMandado, search);
				
			} catch (Exception nsfe){
				facesMessages.add(Severity.ERROR, "Erro ao tentar recuperar os registros.");
			}
			return expedientesMandadosList;
		}

		@Override
		public long count(Search search) {
			search.getCriterias().clear();
			try {
				search.setMax(0);
				this.count = getCountExpedientesPelaSituacao(this.tipoAgrupadorExpedienteMandado, search);

			} catch (Exception e) {
				this.facesMessages.add(Severity.ERROR, "Erro ao tentar recuperar o número de registros.");
				e.printStackTrace();
				return QTD_LISTA_VAZIA;
			}
			return count;
		}
		
		/**
		 * Método que retorna uma lista de {@link ProcessoExpedienteCentralMandado}.
		 * que estão de acordo com tipo de Agrupador ({@link TipoAgrupadorExpedienteMandadoEnum}).
		 * e que satisfazem as condições do {@link search}.
		 * @param {@link TipoAgrupadorExpedienteMandadoEnum}.
		 * @param {@link search}.
		 * @return Lista de {@link ProcessoExpedienteCentralMandado}.
		 * @throws NoSuchFieldException
		 */
		private List<ProcessoExpedienteCentralMandado> pesquisarExpedientesPelaSituacao(
				TipoAgrupadorExpedienteMandadoEnum opcao, Search search) throws NoSuchFieldException{
			
			if (TipoAgrupadorExpedienteMandadoEnum.REDISTRIBUICAO.equals(opcao)) {
				aplicarFiltroExpedientesJaDistribuidos(search);
				expedientesMandadosList = this.processoExpedienteCentralMandadoManager.listMandadosParaRedistribuicao(
						search, usuarioLocalizacao, idsLocalizacoesFilhasInt, nomeParteExpDist);
			}

			if (TipoAgrupadorExpedienteMandadoEnum.DISTRUBUICAO.equals(opcao)) {
				aplicarFiltroExpedientesParaDistribuicao(search);
				expedientesMandadosList = this.processoExpedienteCentralMandadoManager.listMandadosParaDistribuicao(
						search, usuarioLocalizacao, idsLocalizacoesFilhasInt, nomeParteExpDist);
			}

			if (TipoAgrupadorExpedienteMandadoEnum.JA_DISTRIBUIDO.equals(opcao)) {
				aplicarFiltroExpedientesJaDistribuidos(search);
				expedientesMandadosList = this.processoExpedienteCentralMandadoManager.listMandadosJaDistribuidos(
						search, usuarioLocalizacao, idsLocalizacoesFilhasInt, nomeParteExpDist);
				
				for (ProcessoExpedienteCentralMandado processoExpedienteCentralMandado : expedientesMandadosList) {
					processoExpedienteCentralMandado.setCheck(Boolean.FALSE);
				}
			}

			if (TipoAgrupadorExpedienteMandadoEnum.CUMPRIMENTO.equals(opcao)) {
				aplicarFiltroExpedientesParaCumprimento(search);
				Pessoa pessoaLogada = (Pessoa) Contexts.getSessionContext().get("usuarioLogado");
				expedientesMandadosList = this.processoExpedienteCentralMandadoManager.listMandadosParaCumprimento(
						search, usuarioLocalizacao, idsLocalizacoesFilhasInt, pessoaLogada.getIdUsuario(), nomeParte);
			}

			return expedientesMandadosList;
		}
		
		/**
		 * Método que retorna total de {@link ProcessoExpedienteCentralMandado} 
		 * que estão de acordo com tipo de Agrupador ({@link TipoAgrupadorExpedienteMandadoEnum})
		 * e que satisfazem as condições do {@link search}.
		 * 
		 * @param {@link TipoAgrupadorExpedienteMandadoEnum}.
		 * @param {@link search}.
		 * @return Total de expedientes do agrupador informado como parâmetro.
		 * @throws NoSuchFieldException
		 */
		private Long getCountExpedientesPelaSituacao(TipoAgrupadorExpedienteMandadoEnum opcao, Search search) throws NoSuchFieldException{
			if (TipoAgrupadorExpedienteMandadoEnum.REDISTRIBUICAO.equals(opcao)) {
				aplicarFiltroExpedientesJaDistribuidos(search);
				count = this.processoExpedienteCentralMandadoManager.countMandadosParaRedistribuicao(search, usuarioLocalizacao, idsLocalizacoesFilhasInt, nomeParteExpDist);
			}

			if (TipoAgrupadorExpedienteMandadoEnum.DISTRUBUICAO.equals(opcao)) {
				aplicarFiltroExpedientesParaDistribuicao(search);
				count = this.processoExpedienteCentralMandadoManager.countMandadosParaDistribuicao(search, usuarioLocalizacao, idsLocalizacoesFilhasInt, nomeParteExpDist);
			}

			if (TipoAgrupadorExpedienteMandadoEnum.JA_DISTRIBUIDO.equals(opcao)) {
				aplicarFiltroExpedientesJaDistribuidos(search);
				count = this.processoExpedienteCentralMandadoManager.countMandadosJaDistribuidos(search, usuarioLocalizacao, idsLocalizacoesFilhasInt, nomeParteExpDist);
			}

			if (TipoAgrupadorExpedienteMandadoEnum.CUMPRIMENTO.equals(opcao)) {
				Pessoa pessoaLogada = (Pessoa) Contexts.getSessionContext().get("usuarioLogado");
				aplicarFiltroExpedientesParaCumprimento(search);
				count = this.processoExpedienteCentralMandadoManager.countMandadosParaCumprimento(search, usuarioLocalizacao, idsLocalizacoesFilhasInt, pessoaLogada.getIdUsuario(), nomeParte);
			}
			
			return count;
		}
	}
	
	/**
	 * <b>Enum</b> que identifica os tipos de agrupadores em que um mandado pode está.
	 */
	private enum TipoAgrupadorExpedienteMandadoEnum implements PJeEnum {
		REDISTRIBUICAO("Redistribuição"), 
		DISTRUBUICAO("Distribuição"), 
		JA_DISTRIBUIDO("Já distribuido"), 
		CUMPRIMENTO ("Cumprimento"), 
		ENCERRADOS("Encerrados");

		private String label;
		
		TipoAgrupadorExpedienteMandadoEnum(String label) {
			this.label = label;
		}

		@Override
		public String getLabel() {
			return this.label;
		}
	}
	
}