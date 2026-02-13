package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.Identity;

import br.com.infox.cliente.NumeroProcessoUtil;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.pje.manager.ProcessoJbpmManager;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.ConsultaProcessoTrfManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoVisibilidadeSegredoManager;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.cnj.pje.view.BaseAction;
import br.jus.cnj.pje.view.EntityDataModel;
import br.jus.pje.nucleo.entidades.ConsultaProcessoFluxoAbertoTarefaFechadaIbpm;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.ClasseJudicialInicialEnum;

/**
 * Componente de controle da tela de correção de fluxo.
 * 
 */
@Name("processoJbpmAction")
@Scope(ScopeType.PAGE)
public class ProcessoJbpmAction extends BaseAction<ProcessoTrf> implements Serializable {

	private static final long serialVersionUID = -6415558897148687132L;

	@In
	private Identity identity;
	
	@In
	private ProcessoJudicialManager processoJudicialManager;
	
	@In
	private ProcessoParteManager processoParteManager;
	
	@In
	private ProcessoTrfManager processoTrfManager;
	
	@In
	private ProcessoJbpmManager processoJbpmManager;
	
	@In
	private ConsultaProcessoTrfManager consultaProcessoTrfManager;
	
	@In
	private ProcessoVisibilidadeSegredoManager processoVisibilidadeSegredoManager;
	
	@In(create=true)
	private ProcessoJudicialService processoJudicialService;
	
	private String tab = "search";
	private Integer numeroSequencia;
	private Integer digitoVerificador;
	private Integer ano;
	private String ramoJustica;
	private String respectivoTribunal;
	private Integer numeroOrigem;
	private String justificativaCorrecao;
	private boolean VisibleAbaProcesso = false;
	private List<ConsultaProcessoFluxoAbertoTarefaFechadaIbpm> processoList = null;
	public final int proc_flux_ativo_tarefa_encerrada = 1;
	public final int proc_flux_ativo_tarefa_aberta = 2;
	public final int proc_sem_flux_ativo = 3;

	private ProcessoTrf processoParaCorrecao = null;

	private ConsultaProcessoFluxoAbertoTarefaFechadaIbpm consultaProcessoLimbo;

	@Create
	public void init(){
		String numeroOrgaoJustica = ParametroUtil.getParametro("numeroOrgaoJustica");
		if (numeroOrgaoJustica != null) {
			this.ramoJustica = numeroOrgaoJustica.substring(0, 1);
			this.respectivoTribunal = numeroOrgaoJustica.substring(1);
		}
	}

	/**
	 * Método responsável por realizar a pesquisa de acordo com os dados informados 
	 * 
	 */
	public void pesquisar(){
		processoList = processoJbpmManager.processosFluxoAbertoTarefaFechadaIbpm
				(numeroSequencia, digitoVerificador, ano, numeroOrigem, ramoJustica, respectivoTribunal, 15);
	}
	
	/**
	 * Método responsável por setar os realizar as atribuições das variáveis
	 * para edição do processo 
	 * 
	 * @param consultaProc
	 */
	public void editarProcessoAction(ConsultaProcessoFluxoAbertoTarefaFechadaIbpm consultaProc){
		processoParaCorrecao = processoTrfManager.recuperarProcesso(formatarNumeroProcesso(consultaProc), ClasseJudicialInicialEnum.values());
		this.consultaProcessoLimbo = consultaProc;
		setVisibleAbaProcesso(true);
		setTab("tabEdit");
	}
	
	/**
	 * Limpa os campos da pesquisa de processo
	 * 
	 */
	public void limparCamposPesquisa() {
		numeroSequencia = null;
		digitoVerificador = null;
		ano = null;
		numeroOrigem = null;
		processoParaCorrecao = null;
		justificativaCorrecao = null;
		setVisibleAbaProcesso(false);
  	}
	
	/**
	 * Método responsável por formatar o número do processo de acordo com sua composição
	 * 
	 * <li><b>NNNNNNN</b> = Número sequencial do processo no ano</li> <li>
	 * <b>DD</b> = Dígito de verificação</li> <li><b>AAAA</b> = Ano</li> <li>
	 * <b>JTR</b> = Identificação do órgão da justiça</li> <li><b>OOOO</b> = Origem do processo</li> 
	 * 
	 * @param conProc
	 * @return uma <b>String</b> com o número do processo formatado. Ex.: <b>0000000-00.0000.0.00.0000</b> 
	 */
	public String formatarNumeroProcesso(ConsultaProcessoFluxoAbertoTarefaFechadaIbpm conProc) {
		return NumeroProcessoUtil.formatNumeroProcesso(conProc.getNumeroSequencia(), conProc.getNumeroDigitoVerificador(), 
				conProc.getAno(), conProc.getNumeroOrgaoJustica(), conProc.getNumeroOrigem()); 
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

	@Override
	protected BaseManager<ProcessoTrf> getManager() {
		return processoJudicialManager;
	}

	@Override
	public EntityDataModel<ProcessoTrf> getModel() {
		return null;
	}
	
	public List<ConsultaProcessoFluxoAbertoTarefaFechadaIbpm> getProcessoList() {
		return processoList;
	}

	public void setProcessoLimboList(List<ConsultaProcessoFluxoAbertoTarefaFechadaIbpm> processoList) {
		this.processoList = processoList;
	}
	
	public ProcessoTrf getProcessoLimbo() {
		return processoParaCorrecao;
	}

	public void setProcessoLimbo(ProcessoTrf processoLimbo) {
		this.processoParaCorrecao = processoLimbo;
	}

	public ConsultaProcessoFluxoAbertoTarefaFechadaIbpm getConsultaProcessoLimbo() {
		return consultaProcessoLimbo;
	}

	public void setConsultaProcessoLimbo(ConsultaProcessoFluxoAbertoTarefaFechadaIbpm consultaProcessoLimbo) {
		this.consultaProcessoLimbo = consultaProcessoLimbo;
	}

	public String getTab() {
		return tab;
	}

	public void setTab(String tab) {
		this.tab = tab;
	}

	public boolean isVisibleAbaProcesso() {
		return VisibleAbaProcesso;
	}

	public void setVisibleAbaProcesso(boolean visibleAbaProcesso) {
		VisibleAbaProcesso = visibleAbaProcesso;
	}

	public String getJustificativaCorrecao() {
		return justificativaCorrecao;
	}

	public void setJustificativaCorrecao(String justificativaCorrecao) {
		this.justificativaCorrecao = justificativaCorrecao;
	}
}