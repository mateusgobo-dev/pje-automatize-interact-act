package br.com.infox.pje.list;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.util.Strings;
import org.jbpm.taskmgmt.exe.TaskInstance;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.pje.dao.SituacaoProcessoDAO;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.LocalizacaoUtil;
import br.jus.cnj.pje.nucleo.manager.LocalizacaoManager;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.entidades.Fluxo;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SituacaoProcesso;
import br.jus.pje.nucleo.entidades.Tarefa;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.nucleo.util.StringUtil;

@Name(ConsultaProcessoSimplesList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ConsultaProcessoSimplesList extends EntityList<SituacaoProcesso> {

	public static final String NAME = "consultaProcessoSimplesList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_ORDER = "o.processoTrf.processo.numeroProcesso";

	private static final String R1 = "o.processoTrf.processo.numeroProcesso like concat('%', #{consultaProcessoSimplesList.numeroProcesso}, '%')";
	private static final String R2 = "o.idLocalizacao IN (#{consultaProcessoSimplesList.getIdsLocalizacoesFisicasFilhas()})";
	private static final String R3 = "o.idOrgaoJulgadoColegiado = #{consultaProcessoSimplesList.getIdOrgaoJulgadorColegiado()}";
	private static final String R4 = "exists (select l from ConsultaProcessoIbpm l where l.idProcesso = o.idProcesso and l.tarefaJbpm.tarefa = #{consultaProcessoSimplesList.tarefa})";
	private static final String R5 = "false = #{consultaProcessoSimplesList.validarAcaoBotao or not util.ajaxRequest}";
	private static final String R6 = "o.dataChegadaTarefa >= #{consultaProcessoSimplesList.dataEntradaInicio}";
	private static final String R7 = "o.dataChegadaTarefa <= #{consultaProcessoSimplesList.dataEntradaFimCorrigida}";
	private static final String R8 = "o.nomeFluxo = #{consultaProcessoSimplesList.fluxo.fluxo}";
	
	private Integer idProcesso = null;
	private ProcessoTrf processoTrf;
	private Tarefa tarefa;
	private String tab;
	private Integer numeroSequencia;
	private Integer digitoVerificador;
	private Integer ano;
	private String ramoJustica;
	private String respectivoTribunal;
	private Integer numeroOrigem;
	private Date dataEntradaInicio;
	private Date dataEntradaFim;
	private Date dataEntradaFimCorrigida;
	private Fluxo fluxo;
	private Integer tipoVisualizacaoTarefas;
	private OrgaoJulgadorColegiado orgaoJulgadorColegiado;
	private Localizacao localizacaoFisica;
	
	/**
	 * Variável responsável por garantir que a ação do botão Limpar além de realizar tal ação nos
	 * campos do formulário, traga também uma tabela sem tuplas retornadas pela EJBQL padrão da classe.
	 */
	private Boolean validarAcaoBotao;
	
	/**
	 * Variável responsável por armazenar a mensagem de erro na validação dos campos
	 * de pesquisa para que a mesma possa ser exibida posteriormente.
	 */
	private String mensagemErroValidacao;
		
	@Override
	protected void addSearchFields() {
		addSearchField("processoTrf.numeroProcesso", SearchCriteria.contendo, R1);
		addSearchField("localizacaoFisica", SearchCriteria.igual, R2);
		addSearchField("orgaoJulgadorColegiado", SearchCriteria.igual, R3);
		addSearchField("localizacaoInicial", SearchCriteria.igual, R4);
		addSearchField("validarAcaoBotao", SearchCriteria.igual, R5);
		addSearchField("DataInicio", SearchCriteria.maiorIgual, R6);
		addSearchField("DataFim", SearchCriteria.menorIgual, R7);
		addSearchField("FiltroFluxo", SearchCriteria.igual, R8);
	}

	@Override
	public void newInstance() {
		this.validarAcaoBotao = true;
		
		String numeroOrgaoJustica = ParametroUtil.getParametro("numeroOrgaoJustica");
		if (numeroOrgaoJustica != null) {
			this.respectivoTribunal = numeroOrgaoJustica.substring(1);
			this.ramoJustica = numeroOrgaoJustica.substring(0, 1);
		}
		
		this.idProcesso = null;
		this.numeroSequencia = null;
		this.digitoVerificador = null;
		this.ano = null;
		this.numeroOrigem = null;
		this.tarefa = null;
		this.dataEntradaInicio=null;
		this.dataEntradaFim=null;
		this.fluxo=null;
		this.setTipoVisualizacaoTarefas(0);
		this.orgaoJulgadorColegiado = Authenticator.getOrgaoJulgadorColegiadoAtual();
		this.localizacaoFisica = Authenticator.getLocalizacaoFisicaAtual();
		super.newInstance();
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	@Override
	protected String getDefaultEjbql() {
		StringBuilder hql = new StringBuilder("SELECT o ");
		
		SituacaoProcessoDAO situacaoProcessoDAO = (SituacaoProcessoDAO) Component.getInstance(SituacaoProcessoDAO.class, true);
		hql.append(situacaoProcessoDAO.getQueryFromTarefasPermissoes("o", true, Authenticator.isVisualizaSigiloso(), Authenticator.getIdsLocalizacoesFilhasAtuais(), 
				Authenticator.isServidorExclusivoColegiado(), Authenticator.getIdOrgaoJulgadorColegiadoAtual()));
		
		return hql.toString();
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	public boolean showGrid() {
		ProcessoTrf processo = getEntity().getProcessoTrf();
		return (processo != null && !Strings.isEmpty(processo.getNumeroProcesso()))
				|| (processo != null && (processo.getOrgaoJulgador() != null || processo.getOrgaoJulgadorColegiado() != null));
	}

	public Integer getIdProcesso() {
		return idProcesso;
	}

	public void setIdProcesso(Integer idProcesso) {
		this.idProcesso = idProcesso; 
		setProcessoTrf(idProcesso);
	}
	
	public Map<String, List<String>> getMapaAtributosView(ProcessoTrf processoTrf) {
		Map<String, List<String>> retorno = new HashMap<>();
		
		List<String> tarefas = new ArrayList<>();
		List<String> datas = new ArrayList<>();
				
		List<TaskInstance> tasksAbertas = getTasksAbertas(processoTrf);
		for (TaskInstance taskInstance : tasksAbertas){
			tarefas.add(taskInstance.getName());
			datas.add(DateUtil.getDataFormatada(taskInstance.getCreate(), "dd/MM/yyyy HH:mm"));
		}
		
		retorno.put("tarefas", tarefas);
		retorno.put("datas", datas);
		
		return retorno;
	}

	public List<TaskInstance> getTasksAbertas(ProcessoTrf processoTrf){
		List<TaskInstance> tasks = null;
		List<TaskInstance> tasksAbertas = new ArrayList<>();
		
		try {
			if(processoTrf.getProcesso() != null){
				tasks = ProcessoHome.instance().retornaTasks(processoTrf.getProcesso());
			}
			if(tasks != null) {
				for (TaskInstance taskInstance : tasks){
					if (taskInstance.isOpen() && !taskInstance.isCancelled()) {
						tasksAbertas.add(taskInstance);
					}
				}
			}
		}
		catch (Exception e) { // Para evitar exception se não encontrar o process instance
			e.printStackTrace();
		}
		
		return tasksAbertas;
	}
	
	public String getNumeroProcesso() {
		String[] numeroProcesso = new String[]{
				numeroSequencia == null ? "_______" : StringUtil.completaZeros(numeroSequencia, 7), 
				"-", 
				digitoVerificador == null ? "__" : StringUtil.completaZeros(digitoVerificador,2), 
				".", 
				ano == null ? "____" : StringUtil.completaZeros(ano, 4), 
				".", 
				StringUtil.isEmpty(ramoJustica) ? "_" : ramoJustica, 
				".", 
				StringUtil.isEmpty(respectivoTribunal) ? "__" : StringUtil.completaZeros(respectivoTribunal, 2), 
				".", 
				numeroOrigem == null ? "____" : StringUtil.completaZeros(numeroOrigem, 4)};
		
		return StringUtils.join(numeroProcesso);
	}

	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	public void setProcessoTrf(Integer idProcessoTrf) {
		processoTrf = EntityUtil.find(ProcessoTrf.class, idProcessoTrf);
	}

	public Tarefa getTarefa() {
		return tarefa;
	}

	public void setTarefa(Tarefa tarefa) {
		this.tarefa = tarefa;
	}

	public String getTab() {
		return tab;
	}

	public void setTab(String tab) {
		this.tab = tab;
	}
	
	public Boolean getValidarAcaoBotao() {
		return validarAcaoBotao;
	}

	public void setValidarAcaoBotao(Boolean validarAcaoBotao) {
		this.validarAcaoBotao = validarAcaoBotao;
	}
	
	public String getMensagemErroValidacao(){
		return mensagemErroValidacao;
	}
	
	public void setMensagemErroValidacao(String mensagemErroValidacao){
		this.mensagemErroValidacao = mensagemErroValidacao;
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

	public Integer getNumeroOrigem() {
		return numeroOrigem;
	}

	public void setNumeroOrigem(Integer numeroOrigem) {
		this.numeroOrigem = numeroOrigem;
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

	public Date getDataEntradaInicio() {
		return dataEntradaInicio;
	}

	public void setDataEntradaInicio(Date dataEntradaInicio) {
		this.dataEntradaInicio = dataEntradaInicio;
	}

	public Date getDataEntradaFim() {
		return dataEntradaFim;
	}

	public void setDataEntradaFim(Date dataEntradaFim) {
		if((dataEntradaInicio !=null && dataEntradaFim!=null)) {
			setdataEntradaFimCorrigida(DateUtil.dataMaisDias(dataEntradaFim, 1));
		}
		this.dataEntradaFim = dataEntradaFim;
	}

	public Date getdataEntradaFimCorrigida() {
		return dataEntradaFimCorrigida;
	}

	public void setdataEntradaFimCorrigida(Date dataEntradaFimCorrigida) {
		this.dataEntradaFimCorrigida = dataEntradaFimCorrigida;
	}

	public Fluxo getFluxo() {
		return fluxo;
	}

	public void setFluxo(Fluxo fluxo) {
		this.fluxo = fluxo;
	}

	public Integer getTipoVisualizacaoTarefas() {
		return tipoVisualizacaoTarefas;
	}

	public void setTipoVisualizacaoTarefas(Integer tipoVisualizacaoTarefas) {
		this.tipoVisualizacaoTarefas = tipoVisualizacaoTarefas;
	}
	
	/** 
	 * Descrição: Este método cria um select para consultar as tarefas dos processos.
	 *            Podem ser consultadas todas as tarefas ou restritas para aquelas da hierarquia ou colegiado do usuario
	 * @return sqlTarefasItems - select para retornar as tarefas para posterior consulta
	 */
	public String getSqlTarefasItems() {
		StringBuilder hql = new StringBuilder("SELECT DISTINCT tar FROM Tarefa tar ");
		if(isTipoVisualizacaoTarefasRestrito()) {
			hql.append(" WHERE EXISTS (SELECT sitpro ");
			
			SituacaoProcessoDAO situacaoProcessoDAO = (SituacaoProcessoDAO) Component.getInstance(SituacaoProcessoDAO.class, true);
			hql.append(situacaoProcessoDAO.getQueryFromTarefasPermissoes("sitpro", true, Authenticator.isVisualizaSigiloso(), Authenticator.getIdsLocalizacoesFilhasAtuais(), 
					Authenticator.isServidorExclusivoColegiado(), Authenticator.getIdOrgaoJulgadorColegiadoAtual()));
			
			hql.append(" AND sitpro.idTarefa = tar.idTarefa ");
			hql.append(")");
		}
		hql.append(" ORDER BY tar.tarefa ");

		return hql.toString();
	}
	
	/**
	 * Descrição: Retorna true se o tipo de visualização das tarefas for selecionada pela usuário como 'Restrito'.
	 * Essa visualização ignora a raia do processo, leva em consideração apenas a localização física do usuário e a do processo
	 * 
	 * @return isTipoVisualizacaoTarefasRestrito - retorna true se o tipo de visualização das tarefas for selecionada pela usuário como 'Restrito'.
	 */
	public boolean isTipoVisualizacaoTarefasRestrito(){
		return this.tipoVisualizacaoTarefas != null && this.tipoVisualizacaoTarefas == 0;
	}

	/**
	 * 
	 * Descrição: Mostra a data em que o processo entrou na tarefa especificada e quantos dias o processo está na tarefa, 
	 * inclusive se o processo estiver na tarefa há um dia ou tenha entrado em menos de 24 horas, mostrando 
	 * respectivamente: "(1 dia)" e "(menos de um dia)".
	 * 
	 * @param dataDeEntrada - Data de entrada
	 * @return a data de entrada com dias já na tarefa
	 */
	public String getMensagemDataDeEntradaComDias(Date dataDeEntrada) {
		Date dataAtual = new Date();
		return String.format("%s (%s)",
				DateUtil.getDataFormatada(dataDeEntrada, "dd/MM/yyyy HH:mm"),
				getDiferencaEmDiasPorExtenso(dataAtual, dataDeEntrada));
	}

	/**
	 * 
	 * Descrição: Retorna a diferencia em dias entre a data inicial e final informadas no formato String.
	 * Exemplo de retorno: 1 dia.
	 * 
	 * @param dataFinal - Data final
	 * @param dataInicial - Data Inicial
	 * @return A diferencas em dias das datas informadas.
	 */
	public String getDiferencaEmDiasPorExtenso(final Date dataFinal,
			final Date dataInicial) {
		if (dataFinal == null || dataInicial == null) {
			return "";
		}
		long diferencaDias = DateUtil.diferencaDias(dataFinal, dataInicial);
		if (diferencaDias < 1) {
			return "menos de um dia";
		}
		StringBuilder mensagemFormatada = new StringBuilder();
		mensagemFormatada.append(diferencaDias);
		mensagemFormatada.append(diferencaDias == 1 ? " dia" : " dias");
		return mensagemFormatada.toString();
	}

	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiado() {
		return orgaoJulgadorColegiado;
	}

	public void setOrgaoJulgadorColegiado(
			OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}
	
	public Long getIdOrgaoJulgadorColegiado() {
		return this.orgaoJulgadorColegiado != null ? Long.valueOf(this.orgaoJulgadorColegiado.getIdOrgaoJulgadorColegiado()) : null;
	}
	
	public List<Long> getIdsLocalizacoesFisicasFilhas(){
		List<Long> idsLocalizacoesFisicasList = null;
		if(this.localizacaoFisica != null) {
			LocalizacaoManager localizacaomanager = ComponentUtil.getComponent(LocalizacaoManager.class);
			List<Localizacao> localizacoesFilhas = localizacaomanager.getArvoreDescendente(this.localizacaoFisica.getIdLocalizacao(), true);
			String idsLocalizacoesFilhas = LocalizacaoUtil.converteLocalizacoesList(localizacoesFilhas);
			List<Long> idsLocalizacoesFisicasListLong = CollectionUtilsPje.convertStringToLongList(idsLocalizacoesFilhas);
			if(CollectionUtilsPje.isNotEmpty(idsLocalizacoesFisicasListLong)) {
				idsLocalizacoesFisicasList = idsLocalizacoesFisicasListLong;
			}
		}
		return idsLocalizacoesFisicasList;
	}
	
	public Localizacao getLocalizacaoFisica() {
		return localizacaoFisica;
	}

	public void setLocalizacaoFisica(Localizacao localizacaoFisica) {
		this.localizacaoFisica = localizacaoFisica;
	}
}
