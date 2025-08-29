package br.com.infox.pje.action;

import java.io.IOException;
import java.io.Serializable;
import java.text.Format;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.Messages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.component.securitytoken.TokenManager;
import br.com.infox.cliente.component.tree.ClasseJudicialTreeHandler;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.bean.EstatisticaProcessosSemConclusaoBean;
import br.com.infox.pje.list.EstatisticaProcessosSemConclusaoList;
import br.com.infox.pje.manager.CargoManager;
import br.com.infox.pje.manager.EstatisticaProcessoJusticaFederalManager;
import br.com.infox.pje.manager.RelatorioLogManager;
import br.com.infox.pje.manager.SecaoJudiciariaManager;
import br.com.itx.component.SelectItemsQuery;
import br.com.itx.component.Util;
import br.com.itx.exception.ExcelExportException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.ExcelExportUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.CompetenciaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoEventoManager;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.pje.nucleo.entidades.Cargo;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.SecaoJudiciaria;


@Name(value=EstatisticaProcessosSemConclusaoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class EstatisticaProcessosSemConclusaoAction implements Serializable{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "estatisticaProcessosSemConclusaoAction";

	private EstatisticaProcessosSemConclusaoList estatisticaProcessosSemConclusaoList = new EstatisticaProcessosSemConclusaoList();
	private static final String TEMPLATE_XLS_PATH = "/EstatisticaProcessoJusticaFederal/ProcessosSemConclusao/processosSemConclusao.xls";
	private static final String DOWNLOAD_XLS_NAME = "ProcessosSemConclusao.xls";
	
	@In
	private RelatorioLogManager relatorioLogManager;
	@In
	private EstatisticaProcessoJusticaFederalManager estatisticaProcessoJusticaFederalManager;
	
	@In
	private ProcessoEventoManager processoEventoManager;
	
	@In
	private ProcessoJudicialService processoJudicialService;
	
	@Logger
	private Log logger;
	
	@In
	private SecaoJudiciariaManager secaoJudiciariaManager;
	@In
	private CargoManager cargoManager;
	@In
	private CompetenciaManager competenciaManager;
	
	private List<EstatisticaProcessosSemConclusaoBean> estatisticaBeanList = new ArrayList<EstatisticaProcessosSemConclusaoBean>();
	private SecaoJudiciaria secaoJudiciaria;
	private OrgaoJulgador orgaoJulgador;
	private ClasseJudicial classeJudicial;
	private List<ClasseJudicial> classeJudicialList = new ArrayList<ClasseJudicial>();
	private Date dataInicio;
	private Date dataFim;
	private String dataFimFormatada;
	private Cargo cargo;
	private Competencia competencia;
	private String opcao = "A";
	private String token;
	
	public static EstatisticaProcessosSemConclusaoAction intance(){
		return ComponentUtil.getComponent(NAME);
	}
	
	public List<EstatisticaProcessosSemConclusaoBean> estatisticaProcessosSemConclusaoBeanList(){
		estatisticaBeanList = buildEstatisticaProcessosSemConclusaoList();
		return estatisticaBeanList;
	}	
	
	private List<EstatisticaProcessosSemConclusaoBean> buildEstatisticaProcessosSemConclusaoList() {
		dataFimFormatada = formatarAnoMes(dataFim);
		if(estatisticaBeanList == null) {
			estatisticaBeanList = new ArrayList<EstatisticaProcessosSemConclusaoBean>();
			List<Object[]> resultList = getEstatisticaProcessosSemConclusaoList().getResultList();
			if(resultList != null && resultList.size() > 0) {
				EstatisticaProcessosSemConclusaoBean epscb;
				ProcessoEvento pe;
				for (Object[] obj : resultList) {
					epscb = new EstatisticaProcessosSemConclusaoBean();					
					Date dtCalculo;
					if(obj[1] != null){
						dtCalculo = (Date)obj[1];
					}else{
						dtCalculo = (Date)obj[2];
					}
					int idProcesso = (Integer) obj[0];
					try {
						pe = processoEventoManager.recuperaUltimaMovimentacao(processoJudicialService.findById(idProcesso), dataFim);
						epscb.setNumeroProcesso(pe.getProcesso().getNumeroProcesso());
						epscb.setUltimaFase(pe.getEvento().getEvento());
						epscb.setDataUltimaFase(pe.getDataAtualizacao());
						epscb.setQntDias(calcQtdDiasSemConlusao(dataFim, dtCalculo));
						estatisticaBeanList.add(epscb);
					} catch (PJeBusinessException e) {
						logger.error("Erro ao tentar recuperar o processo judicial com identifcador {0}: {1}", idProcesso, e.getLocalizedMessage());
					}
				}
			}
		}
		estatisticaBeanList = ordenaList(estatisticaBeanList);
		return estatisticaBeanList;
	}
	
	/**
	 * Faz o calculo da quantidade de dia que o processo esta sem conclusão.
	 * @param dataFim
	 * @param dtCalculo
	 */
	public int calcQtdDiasSemConlusao(Date dataFim, Date dtCalculo) {  
		if (dataFim.getTime() <= dtCalculo.getTime()){
			return 0;
		}
		long dif = ((dataFim.getTime() - dtCalculo.getTime()) / 86400000L);
        return (int) (dif + 1);
    }
	
	/**
	 * Método que recebe uma data e transforma para "yyyy/MM/dd"
	 * @param data
	 * @return
	 */
	public String formatarAnoMes(Date data) {
		Format formatter = new SimpleDateFormat("yyyy/MM/dd");
		return formatter.format(data);
	}
	
	private List<EstatisticaProcessosSemConclusaoBean> ordenaList(List<EstatisticaProcessosSemConclusaoBean> listBean) {
		List<Long> lista = new ArrayList<Long>();
		
		for (EstatisticaProcessosSemConclusaoBean o : listBean) {
				lista.add(o.getQntDias());
		}
		
		Collections.sort(lista);
		Collections.reverse(lista);
		
		List<EstatisticaProcessosSemConclusaoBean> processoListBean = new ArrayList<EstatisticaProcessosSemConclusaoBean>();
		for(Long l : lista){
			for(EstatisticaProcessosSemConclusaoBean o : listBean){
				if(Long.valueOf(o.getQntDias()).equals(l) &&  !processoListBean.contains(o)){
					processoListBean.add(o);
				}
			}
		}
		
		return processoListBean;
	}
	
	public void exportarEstatisticaProcessosSemConclusaoXLS() {
		try {
			if (estatisticaBeanList.size() >0) {
				exportarXLS(TEMPLATE_XLS_PATH,DOWNLOAD_XLS_NAME);
			}else {
				FacesMessages.instance().add(Severity.INFO, "Não há dados para exportar!");
			}
		} catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao exportar arquivo." + e.getMessage());
			e.printStackTrace();
		} 
	}
	
	public void exportarXLS(String dirNomeTemplate, String nomeArqDown) throws ExcelExportException {
		String urlTemplate = new Util().getContextRealPath() + dirNomeTemplate;
		ExcelExportUtil util = new ExcelExportUtil(urlTemplate, nomeArqDown);
		util.setBean(beanExportarXLS());
		util.download();
	}
	
	private Map<String, Object> beanExportarXLS() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("estatisticaBeanList", estatisticaBeanList);
		map.put("titulo", Messages.instance().get("RELATÓRIO DE ESTATÍSTICA DE PROCESSOS SEM CONCLUSÃO"));
		map.put("subNomeSistema", ParametroUtil.getParametro("nomeSecaoJudiciaria").toUpperCase());
		map.put("nomeSistema", ParametroUtil.getParametro("nomeSistema"));
		map.put("secaoJudiciaria", secaoJudiciaria);
		map.put("orgaoJulgador", orgaoJulgador);
		String data = "";
		SimpleDateFormat formatoInicio = new SimpleDateFormat("dd/MM/yyyy");  
        data = formatoInicio.format(dataInicio);
		map.put("dataInicio", data);
		data = formatoInicio.format(dataFim);
		map.put("dataFim", data);
		map.put("filtros", getFiltros());
		return map;
	}
	
	public String getFiltros(){
		StringBuilder filtros = new StringBuilder("");
		if(cargo != null){
			filtros.append("Cargo - ");
			filtros.append(cargo.getCargo());
			filtros.append("; ");
		}
		if(!classeJudicialList.isEmpty()){
			filtros.append("Classe Judicial - ");
			for(int i = 0; i < classeJudicialList.size(); i++){
				filtros.append(classeJudicialList.get(i).getClasseJudicial());
				if(i < classeJudicialList.size() - 1){
					filtros.append(", ");
				}
			}
			filtros.append("; ");
		}
		if(competencia != null){
			filtros.append("Natureza - ");
			filtros.append(competencia.getCompetencia());
			filtros.append("; ");
		}
		String filtro = "";
		if(filtros.toString().trim().isEmpty()){
			filtro = "Nenhuma restrição aplicada";
		}else{
			filtro = filtros.toString().substring(0, filtros.toString().lastIndexOf(";"));
		}
		return filtro;
	}
	
	/**
	 * Retorna todas as varas de uma determinada seção.
	 * @return
	 */
	public List<OrgaoJulgador> orgaoJulgadorItems() {
		return estatisticaProcessoJusticaFederalManager.buscaListaOrgaoJulgador(getSecaoJudiciaria().getCdSecaoJudiciaria());
	}
	
	public List<SecaoJudiciaria> secaoJudiciariaItems(){
		return secaoJudiciariaManager.secaoJudiciariaItems();
	}
	
	public List<Cargo> cargoItems(){
		return cargoManager.cargoItems();
	}
	
	public List<Competencia> competenciaItems(){
		return competenciaManager.competenciaItemsByOrgaoJulgador(orgaoJulgador);
	}
	
	/**
	 * Método que grava o log das consultas de relarório caso a consulta retorne
	 * registros.
	 */
	public void gravarLogRelatorio(){
		estatisticaBeanList = null;
        if(!estatisticaProcessosSemConclusaoBeanList().isEmpty()){ 
        	relatorioLogManager.persist("Estatística de Processos Sem Conclusão", Authenticator.getUsuarioLogado());
        }	
	}
	
	public void limparFiltros(){
		dataFim = null;
		setDataInicio(null);
		setOrgaoJulgador(null);
		setCargo(null);
		setCompetencia(null);
//		setOpcao("A");
		estatisticaBeanList = null;
		setClasseJudicial(null);
		classeJudicialList = new ArrayList<ClasseJudicial>();
		ClasseJudicialTreeHandler tree = ComponentUtil.getComponent("classeJudicialTree");
		tree.clearTree();
	}
	
	public void createToken(){
		try {
			if(secaoJudiciaria.getUrlAplicacao() != null && !secaoJudiciaria.getUrlAplicacao().isEmpty()){
				token = TokenManager.instance().getRemoteToken(secaoJudiciaria.getUrlAplicacao()) ;
			}else{
				FacesMessages.instance().add(Severity.ERROR, MessageFormat.format(
						"URL da aplicação não está definida para a seção escolhida: {0}",
						secaoJudiciaria.getSecaoJudiciaria()));
			}
		} catch (IOException e) {
			String msgErro = MessageFormat.format(
					"URL do Webservice não esta acessivel no estado {0}: {1}",
					secaoJudiciaria.getSecaoJudiciaria(), e.getMessage());
			FacesMessages.instance().add(Severity.ERROR, msgErro);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void validarToken(){
		TokenManager.instance().validateToken(token);
	}
	
	private SecaoJudiciaria getSecaoPrimeiroGrau(){
		SelectItemsQuery si = ComponentUtil.getComponent("secaoJudiciariaItems");
		return (SecaoJudiciaria)si.getSingleResult();
	}

	/*
	 * Inicio - Getters and Setters 
	 */

	public SecaoJudiciaria getSecaoJudiciaria() {
		if(ParametroUtil.instance().isPrimeiroGrau()){
			secaoJudiciaria = getSecaoPrimeiroGrau();
		}
		return secaoJudiciaria;
	}
	
	public void setSecaoJudiciaria(SecaoJudiciaria secaoJudiciaria) {
		this.secaoJudiciaria = secaoJudiciaria;
	}
	
	public ClasseJudicial getClasseJudicial() {
		return classeJudicial;
	}
	
	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	public OrgaoJulgador getOrgaoJulgador() {
		if(Authenticator.getOrgaoJulgadorAtual() != null){
			orgaoJulgador = Authenticator.getOrgaoJulgadorAtual();
		}
		return orgaoJulgador;
	}
	
	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}
	
	public List<ClasseJudicial> getClasseJudicialList() {
		classeJudicialList.clear();
		ClasseJudicialTreeHandler tree = ComponentUtil.getComponent("classeJudicialTree");
		for (ClasseJudicial classe : tree.getSelectedTree()) {
			classeJudicialList.add(classe);
		}	
		if(classeJudicialList.isEmpty() && classeJudicial != null){
			classeJudicialList.add(classeJudicial);
		}
		return classeJudicialList;
	}
	
	public void setClasseJudicialList(List<ClasseJudicial> classeJudicialList) {
		this.classeJudicialList = classeJudicialList;
	}

	public Date getDataInicio() {
		return dataInicio;
	}
	
	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}
	
	public Date getDataFim() {
		return dataFim;
	}
	
	public void setDataFim(Date dataFim) {
		if(dataFim.after(new Date())){
			dataFim = new Date();
		}
		this.dataFim = dataFim;
	}

	public void setEstatisticaProcessosSemConclusaoList(
			EstatisticaProcessosSemConclusaoList estatisticaProcessosSemConclusaoList) {
		this.estatisticaProcessosSemConclusaoList = estatisticaProcessosSemConclusaoList;
	}

	public EstatisticaProcessosSemConclusaoList getEstatisticaProcessosSemConclusaoList() {
		return estatisticaProcessosSemConclusaoList;
	}

	public void setCargo(Cargo cargo) {
		this.cargo = cargo;
	}

	public Cargo getCargo() {
		return cargo;
	}

	public void setCompetencia(Competencia competencia) {
		this.competencia = competencia;
	}

	public Competencia getCompetencia() {
		return competencia;
	}

	public void setOpcao(String opcao) {
		this.opcao = opcao;
	}

	public String getOpcao() {
		return opcao;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	public void setDataFimFormatada(String dataFimFormatada) {
		this.dataFimFormatada = dataFimFormatada;
	}

	public String getDataFimFormatada() {
		return dataFimFormatada;
	}
	
}