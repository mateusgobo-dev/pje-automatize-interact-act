package br.com.infox.pje.action;

import java.io.IOException;
import java.io.Serializable;
import java.text.Format;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.Messages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.component.securitytoken.TokenManager;
import br.com.infox.cliente.component.tree.ClasseJudicialTreeHandler;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.bean.EstatisticaConclusaoProcessoBean;
import br.com.infox.pje.bean.EstatisticaJFConclusaoListaProcessosClasse;
import br.com.infox.pje.bean.EstatisticaJFConclusaoProcessoVara;
import br.com.infox.pje.list.EstatisticaConclusaoProcessoClasseList;
import br.com.infox.pje.list.EstatisticaConclusaoProcessoConclusosSentencaList;
import br.com.infox.pje.list.EstatisticaConclusaoProcessoConvertidosDiligenciaList;
import br.com.infox.pje.list.EstatisticaConclusaoProcessoDevolvidosSentencaList;
import br.com.infox.pje.list.EstatisticaConclusaoProcessoList;
import br.com.infox.pje.list.EstatisticaConclusaoProcessoRemanescenteList;
import br.com.infox.pje.manager.EstatisticaProcessoJusticaFederalManager;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.com.infox.pje.manager.RelatorioLogManager;
import br.com.itx.component.SelectItemsQuery;
import br.com.itx.component.Util;
import br.com.itx.exception.ExcelExportException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.ExcelExportUtil;
import br.jus.pje.nucleo.entidades.Cargo;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SecaoJudiciaria;
import br.jus.pje.nucleo.entidades.Usuario;

/**
 * Classe action controladora do listView de /Estatística de Conclusão de Processos/EstatísticaConclusao
 * @author Wilson
 *
 */
@Name(value=EstatisticaConclusaoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class EstatisticaConclusaoAction implements Serializable{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "estatisticaConclusaoAction";

	private EstatisticaConclusaoProcessoList conclusaoProcessoList = new EstatisticaConclusaoProcessoList();
	private static final String TEMPLATE_SINTETICO_XLS_PATH = "/EstatisticaProcessoJusticaFederal/EstatisticaConclusao/relatorioSinteticoTemplate.xls";
	private static final String TEMPLATE_ANALITICO_XLS_PATH = "/EstatisticaProcessoJusticaFederal/EstatisticaConclusao/relatorioAnaliticoTemplate.xls";
	private static final String DOWNLOAD_XLS_NAME = "ConclusaoProcessos.xls";
	
	@In
	private EstatisticaProcessoJusticaFederalManager estatisticaProcessoJusticaFederalManager;
	@In
	private ProcessoTrfManager processoTrfManager;
	@In
	private RelatorioLogManager relatorioLogManager;
	
	private SecaoJudiciaria secaoJudiciaria;
	private OrgaoJulgador orgaoJulgador;
	private String dataFim;
	private String dataInicio;
	private String dtInicio;
	private String dtFim;
	private String dataInicioStr;
	private EstatisticaConclusaoProcessoBean conclusaoBean;
	private Cargo cargoJuiz;
	private PessoaMagistrado juiz;
	private ClasseJudicial classeJudicial;
	private List<ClasseJudicial> classeJudicialList = new ArrayList<ClasseJudicial>();
	private Competencia competencia;
	private boolean relatorioSinteticoAnalitico = true;
	private String token;
	
	public static  EstatisticaConclusaoAction intance(){
		return ComponentUtil.getComponent(NAME);
	}
	
	public EstatisticaConclusaoProcessoBean estatisticaConclusaoProcessoList() {
		conclusaoBean = buildConclusaoProcessoAnalitico();
		return conclusaoBean;
	}
	
	private EstatisticaConclusaoProcessoBean buildConclusaoProcessoAnalitico() {
		if (getDataInicio() != null) {
			setDtInicio(formatarPeriodoExcel(getDataInicioStr(),true));
			setDtFim(formatarPeriodoExcel(getDataInicioStr(),false));
			EstatisticaConclusaoProcessoBean bean = new EstatisticaConclusaoProcessoBean();
			EstatisticaJFConclusaoProcessoVara vara = new EstatisticaJFConclusaoProcessoVara();
			Map<ClasseJudicial, EstatisticaJFConclusaoListaProcessosClasse> subList = new HashMap<ClasseJudicial, EstatisticaJFConclusaoListaProcessosClasse>();
			bean.setSecaoJustica(secaoJudiciaria.getSecaoJudiciaria());
			vara.setVara(getOrgaoJulgador().getOrgaoJulgador());
			
			/**
			 * Percorrendo o Map para retornar as classes da Seção
			 */
			EstatisticaConclusaoProcessoClasseList classesList = new EstatisticaConclusaoProcessoClasseList();
			Map<ClasseJudicial, List<ProcessoTrf>> existente = criarLista(classesList.getResultList());
			for (Map.Entry<ClasseJudicial, List<ProcessoTrf>> entry : existente.entrySet()) {
				ClasseJudicial classe = entry.getKey();
				
				if (!subList.containsKey(classe)) {
					EstatisticaJFConclusaoListaProcessosClasse classeJudicial = new EstatisticaJFConclusaoListaProcessosClasse();
					classeJudicial.setClasse(classe);
					subList.put(classe, classeJudicial);
				}
				
			}
			/**
			 * Laço para retornar os Processos Remanescente da Seção
			 */
			EstatisticaConclusaoProcessoRemanescenteList processosRemanescentesList = new EstatisticaConclusaoProcessoRemanescenteList();
			Map<ClasseJudicial, List<ProcessoTrf>> existenteRemanescente = criarLista(processosRemanescentesList.getResultList());
			for (Map.Entry<ClasseJudicial, List<ProcessoTrf>> entry : existenteRemanescente.entrySet()) {
				ClasseJudicial classe = entry.getKey();
				List<ProcessoTrf> processos = entry.getValue();
				
				if (!subList.containsKey(classe)) {
					EstatisticaJFConclusaoListaProcessosClasse classeProcessos = new EstatisticaJFConclusaoListaProcessosClasse();
					classeProcessos.setClasse(classe);
					subList.put(classe, classeProcessos);
				}
				
				subList.get(classe).setListProcessRemanescente(processos);
				subList.get(classe).setRowspan(subList.get(classe).getRowspan() +1);
				
			}
			
			/**
			 * Laço para retornar os Processos Conclusos para Sentença da Seção
			 */
			EstatisticaConclusaoProcessoConclusosSentencaList processosConclusosSentencaList = new EstatisticaConclusaoProcessoConclusosSentencaList();
			
			Map<ClasseJudicial, List<ProcessoTrf>> existenteConclusosSentenca = criarLista(processosConclusosSentencaList.getResultList());
			for (Map.Entry<ClasseJudicial, List<ProcessoTrf>> entry : existenteConclusosSentenca.entrySet()) {
				ClasseJudicial classe = entry.getKey();
				List<ProcessoTrf> processos = entry.getValue();
				
				if (!subList.containsKey(classe)) {
					EstatisticaJFConclusaoListaProcessosClasse classeProcessos = new EstatisticaJFConclusaoListaProcessosClasse();
					classeProcessos.setClasse(classe);
					subList.put(classe, classeProcessos);
				}
				
				subList.get(classe).setListProcessConclusosSentenca(processos);
				subList.get(classe).setRowspan(subList.get(classe).getRowspan() +1);
				
			}
			
			/**
			 * Laço para retornar os Processos Devolvidos com Sentença da Seção
			 */
			EstatisticaConclusaoProcessoDevolvidosSentencaList processosDevolvidosSentencaList = new EstatisticaConclusaoProcessoDevolvidosSentencaList();
			Map<ClasseJudicial, List<ProcessoTrf>> existenteDevolvidosSentenca = criarLista(processosDevolvidosSentencaList.getResultList());
			for (Map.Entry<ClasseJudicial, List<ProcessoTrf>> entry : existenteDevolvidosSentenca.entrySet()) {
				ClasseJudicial classe = entry.getKey();
				List<ProcessoTrf> processos = entry.getValue();
				
				if (!subList.containsKey(classe)) {
					EstatisticaJFConclusaoListaProcessosClasse classeProcessos = new EstatisticaJFConclusaoListaProcessosClasse();
					classeProcessos.setClasse(classe);
					subList.put(classe, classeProcessos);
				}
				
				subList.get(classe).setListProcessDevolvidosSentenca(processos);
				subList.get(classe).setRowspan(subList.get(classe).getRowspan() +1);
				
			}
			
			/**
			 * Laço para retornar os Processos Convertido em Diligência da Seção
			 */
			EstatisticaConclusaoProcessoConvertidosDiligenciaList processosConvertidosDiligenciaList = new EstatisticaConclusaoProcessoConvertidosDiligenciaList();
			Map<ClasseJudicial, List<ProcessoTrf>> existenteConvertidosDiligencia = criarLista(processosConvertidosDiligenciaList.getResultList());
			for (Map.Entry<ClasseJudicial, List<ProcessoTrf>> entry : existenteConvertidosDiligencia.entrySet()) {
				ClasseJudicial classe = entry.getKey();
				List<ProcessoTrf> processos = entry.getValue();
				
				if (!subList.containsKey(classe)) {
					EstatisticaJFConclusaoListaProcessosClasse classeProcessos = new EstatisticaJFConclusaoListaProcessosClasse();
					classeProcessos.setClasse(classe);
					subList.put(classe, classeProcessos);
				}
				
				subList.get(classe).setListProcessConvertidosDiligencia(processos);
				subList.get(classe).setRowspan(subList.get(classe).getRowspan() +1);
				
				
			}
			
			//FIM dos laços para setar cada processo a sua classe
			List<EstatisticaJFConclusaoListaProcessosClasse> list = new ArrayList<EstatisticaJFConclusaoListaProcessosClasse>(subList.values());
			vara.setSubList(list);	
			bean.setVara(vara);
			Collections.sort(list);
			return bean;
		}
		return null;
	}
	
	
	private Map<ClasseJudicial, List<ProcessoTrf>> criarLista(List<Map<String, Object>> banco){
		Map<ClasseJudicial, List<ProcessoTrf>> existente = new HashMap<ClasseJudicial, List<ProcessoTrf>>();
		for(Map<String, Object> bancoMap : banco){
			ClasseJudicial classe = (ClasseJudicial) bancoMap.get("classe");
			ProcessoTrf processo = (ProcessoTrf) bancoMap.get("processo");
			
			if (!existente.containsKey(classe)) {
				existente.put(classe, new ArrayList<ProcessoTrf>());
			}
			
			existente.get(classe).add(processo);
		}
		return existente;
	}
	
	/**
	 * Retorna todas as varas de uma determinada seção, caso o usauário tenha OJ retorna o dele.
	 * @return
	 */
	public List<OrgaoJulgador> listOrgaoJulgadorItems() {
		List<OrgaoJulgador> items = new ArrayList<OrgaoJulgador>();
		if (ParametroUtil.instance().isPrimeiroGrau() && (Authenticator.getOrgaoJulgadorAtual() != null)) {
			items.add(Authenticator.getOrgaoJulgadorAtual());
			return items;
		}else{
			for(OrgaoJulgador s : estatisticaProcessoJusticaFederalManager.buscaListaOrgaoJulgador(getSecaoJudiciaria().getCdSecaoJudiciaria())) {
				if (s != null) {
					items.add(s);
				}
			}
		}
		return items;
	}
	
	/**
	 * Retorna todas as competencias de um determinado orgao julgador.
	 * @return
	 */
	public List<Competencia> listCompetenciaItems(){
		List<Competencia> items = new ArrayList<Competencia>();
		for(Competencia s : estatisticaProcessoJusticaFederalManager.buscaListaCompetenciaOrgaoJulgador(getOrgaoJulgador())) {
			if (s != null) {
				items.add(s);
			}
		}
		return items;
	}
	
	public int getRowspan(){
		int rowspan= 0;
		if (!relatorioSinteticoAnalitico) {
			rowspan = (conclusaoBean.getVara().getSubList().size() * 5) + 3;
			return rowspan;
		}else{
			rowspan = conclusaoBean.getVara().getSubList().size();
		}
		return rowspan + 2;
	}
	
	public SecaoJudiciaria getSecao() {
		if(ParametroUtil.instance().isPrimeiroGrau()){
			SelectItemsQuery si = ComponentUtil.getComponent("secaoJudiciariaItems");
			secaoJudiciaria = (SecaoJudiciaria) si.getSingleResult();
		}
		return secaoJudiciaria;
	}
	
	/**
	 * Método que grava o log das consultas de relarório caso a consulta retorne
	 * registros.
	 * @param registros quantidade de registros da lista
	 */
	public void gravarLogRelatorio(){
		conclusaoBean = null;
        if(estatisticaConclusaoProcessoList() != null){ 
        	relatorioLogManager.persist("Estatística de Conclusão de Processos", Authenticator.getUsuarioLogado());
        }	
	}
	
	/**
	 * Método que exporta o resultado da consulta para excel, caso a consulta 
	 * retorne registros
	 * @param registros total de registros da consulta
	 */
	public void exportarConclusaoProcessoXLS(){
		String template = null;
		if(relatorioSinteticoAnalitico){
			template = TEMPLATE_SINTETICO_XLS_PATH;
		} else {
			template = TEMPLATE_ANALITICO_XLS_PATH;
		}try {
			if (conclusaoBean != null) {
				exportarXLS(template);
			}else{
					FacesMessages.instance().add(Severity.INFO, "Não há dados para exportar!");
			}
		} catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao exportar arquivo." + e.getMessage());
			e.printStackTrace();
		} 
	}
	
	public void exportarXLS(String template) throws ExcelExportException {
		String urlTemplate = new Util().getContextRealPath() + template;
		ExcelExportUtil.downloadXLS(urlTemplate, beanExportarXLS(), DOWNLOAD_XLS_NAME);
	}
	
	private Map<String, Object> beanExportarXLS() {
		List<EstatisticaConclusaoProcessoBean> list = new ArrayList<EstatisticaConclusaoProcessoBean>();
		list.add(getConclusaoBean());
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("conclusaoProcessoBean", list);
		map.put("titulo", Messages.instance().get("ESTATÍSTICA DE CONCLUSÃO"));
		map.put("subNomeSistema", ParametroUtil.getParametro("nomeSecaoJudiciaria").toUpperCase());
		map.put("nomeSistema", ParametroUtil.getParametro("nomeSistema"));
		map.put("dtInicio", getDtInicio());
		map.put("dtFim", getDtFim());
		map.put("secao", getSecaoJudiciaria().getSecaoJudiciaria());
		map.put("orgaoJulgador", getOrgaoJulgador().getOrgaoJulgador());
		map.put("natureza", "");
		map.put("classe", "");
		map.put("cargoJuiz", "");
		map.put("juiz", "");
		if (getCompetencia() != null) {
			map.put("natureza", getCompetencia());
		}
		if (getClasseJudicial() != null) {
			map.put("classe", getClasseJudicial());
		}
		if (getCargoJuiz() != null) {
			map.put("cargoJuiz", getCargoJuiz());
		}
		if (getJuiz() != null) {
			map.put("juiz", getJuiz());
		}
		return map;
	}

	public void limparFiltros(){
		dataInicio      = null;
		dataInicioStr   = null;
		classeJudicial  = null;
		orgaoJulgador   = null;
		secaoJudiciaria = null;
		competencia     = null;
		cargoJuiz       = null;
		juiz            = null; 
		relatorioSinteticoAnalitico = true;
	}
	
	/**
	 * Traz o nome do Diretor da Vara selecionada (Orgão Julgador)
	 * @return String com o nome
	 */
	public String getDiretorVara(){
		Usuario diretorVara = processoTrfManager.getDiretorVara(orgaoJulgador);
		return diretorVara != null ? diretorVara.getNome() : "";
	}

	/**
	 * Traz o nome do Juiz Federal da Vara selecionada (Orgão Julgador)
	 * @return String com o nome
	 */	
	public String getJuizFederal(){
		Usuario juizFederal = processoTrfManager.getJuizFederal(orgaoJulgador);
		return juizFederal != null ? juizFederal.getNome() : "";
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
	
	public String formatarAnoMes(String data, boolean inicio) {
		String mes = data.substring(0,2);
		String ano = data.substring(3);
		Calendar dataTemp = Calendar.getInstance();
		if (!inicio) {
			dataTemp.set(Integer.parseInt(ano), Integer.parseInt(mes)-1,1);
			Integer dia = dataTemp.getActualMaximum(Calendar.DAY_OF_MONTH);
			dataTemp.set(Integer.parseInt(ano), Integer.parseInt(mes)-1,dia);
		}else{
			dataTemp.set(Integer.parseInt(ano), Integer.parseInt(mes)-1,1);
		}
		Format formatter = new SimpleDateFormat("yyyy-MM-dd");
		return formatter.format(dataTemp.getTime());
	}
	
	public String formatarPeriodoExcel(String data, boolean inicio) {
		String mes = data.substring(0,2);
		String ano = data.substring(3);
		Calendar dataTemp = Calendar.getInstance();
		if (!inicio) {
			dataTemp.set(Integer.parseInt(ano), Integer.parseInt(mes)-1,1);
			Integer dia = dataTemp.getActualMaximum(Calendar.DAY_OF_MONTH);
			dataTemp.set(Integer.parseInt(ano), Integer.parseInt(mes)-1,dia);
		}else{
			dataTemp.set(Integer.parseInt(ano), Integer.parseInt(mes)-1,1);
		}
		Format formatter = new SimpleDateFormat("dd/MM/yyyy");
		return formatter.format(dataTemp.getTime());
	}
	
	public void validarToken(){
		TokenManager.instance().validateToken(token);
	}
	
	/*
	 * Inicio - Getters and Setters 
	 */
	public SecaoJudiciaria getSecaoJudiciaria() {
		if(ParametroUtil.instance().isPrimeiroGrau()){
			SelectItemsQuery si = ComponentUtil.getComponent("secaoJudiciariaItems");
			secaoJudiciaria = (SecaoJudiciaria) si.getSingleResult();
		}
		return secaoJudiciaria;
	}
	
	public void setSecaoJudiciaria(SecaoJudiciaria secaoJudiciaria) {
		this.secaoJudiciaria = secaoJudiciaria;
	}
	
	public EstatisticaConclusaoProcessoList getConclusaoProcessoList() {
		return conclusaoProcessoList;
	}

	public void setConclusaoProcessoList(
			EstatisticaConclusaoProcessoList conclusaoProcessoList) {
		this.conclusaoProcessoList = conclusaoProcessoList;
	}

	public OrgaoJulgador getOrgaoJulgador() {
		if (ParametroUtil.instance().isPrimeiroGrau() && (Authenticator.getOrgaoJulgadorAtual() != null)) {
			orgaoJulgador = Authenticator.getOrgaoJulgadorAtual();
		}
		return orgaoJulgador;
	}
	
	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public String getDataInicio() {
		if(getDataInicioStr() != null){
			dataInicio = formatarAnoMes(getDataInicioStr(),true);
		}
		return dataInicio;
	}

	public String getDtInicio() {
		return dtInicio;
	}

	public void setDtInicio(String dtInicio) {
		this.dtInicio = dtInicio;
	}

	public String getDtFim() {
		return dtFim;
	}

	public void setDtFim(String dtFim) {
		this.dtFim = dtFim;
	}

	public void setDataInicio(String dataInicio) {
		this.dataInicio = dataInicio;
	}
	
	public String getDataFim() {
		if(getDataInicioStr() != null){
			dataFim = formatarAnoMes(getDataInicioStr(),false);
		}
		return dataFim;
	}
	
	public void setDataFim(String dataFim) {
		this.dataFim = dataFim;
	}

	public String getDataInicioStr() {
		return dataInicioStr;
	}
	
	public void setDataInicioStr(String dataInicioStr) {
		this.dataInicioStr = dataInicioStr;
	}

	public Cargo getCargoJuiz() {
		return cargoJuiz;
	}
	
	public void setCargoJuiz(Cargo cargoJuiz) {
		this.cargoJuiz = cargoJuiz;
	}

	public PessoaMagistrado getJuiz() {
		return juiz;
	}
	
	public void setJuiz(PessoaMagistrado juiz) {
		this.juiz = juiz;
	}

	public ClasseJudicial getClasseJudicial() {
		return classeJudicial;
	}
	
	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	public List<ClasseJudicial> getClasseJudicialList() {
		classeJudicialList.clear();
		ClasseJudicialTreeHandler tree = ComponentUtil.getComponent("classeJudicialTree");
		for (ClasseJudicial classe : tree.getSelectedTree()) {
			classeJudicialList.add(classe);
		}
		return classeJudicialList;
	}
	
	public void setClasseJudicialList(List<ClasseJudicial> classeJudicialList) {
		this.classeJudicialList = classeJudicialList;
	}

	public Competencia getCompetencia() {
		return competencia;
	}
	
	public void setCompetencia(Competencia competencia) {
		this.competencia = competencia;
	}

	public boolean isRelatorioSinteticoAnalitico() {
		return relatorioSinteticoAnalitico;
	}
	
	public void setRelatorioSinteticoAnalitico(boolean relatorioSinteticoAnalitico) {
		this.relatorioSinteticoAnalitico = relatorioSinteticoAnalitico;
	}

	public EstatisticaConclusaoProcessoBean getConclusaoBean() {
		return conclusaoBean;
	}
	
	public void setConclusaoBean(
			EstatisticaConclusaoProcessoBean conclusaoBean) {
		this.conclusaoBean = conclusaoBean;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}

}