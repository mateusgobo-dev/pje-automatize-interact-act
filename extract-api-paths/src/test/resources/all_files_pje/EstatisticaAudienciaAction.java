package br.com.infox.pje.action;

import java.io.IOException;
import java.io.Serializable;
import java.text.Format;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.bean.EstatisticaJFProcessosAudienciaVara;
import br.com.infox.pje.bean.EstatisticaProcessoAudienciaBean;
import br.com.infox.pje.bean.EstatisticaProcessosAudienciaClasses;
import br.com.infox.pje.list.EstatisticaAudienciaProcessoAdiadosList;
import br.com.infox.pje.list.EstatisticaAudienciaProcessoCanceladosList;
import br.com.infox.pje.list.EstatisticaAudienciaProcessoClasseList;
import br.com.infox.pje.list.EstatisticaAudienciaProcessoDesignadosList;
import br.com.infox.pje.list.EstatisticaAudienciaProcessoRealizadosList;
import br.com.infox.pje.list.EstatisticaAudienciaProcessoRemanescenteList;
import br.com.infox.pje.list.EstatisticaAudienciaProcessoSuspensosList;
import br.com.infox.pje.manager.EstatisticaProcessoJusticaFederalManager;
import br.com.infox.pje.manager.ProcessoAudienciaManager;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.com.infox.pje.manager.RelatorioLogManager;
import br.com.infox.pje.manager.SecaoJudiciariaManager;
import br.com.itx.component.Util;
import br.com.itx.exception.ExcelExportException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.ExcelExportUtil;
import br.jus.cnj.pje.nucleo.manager.UsuarioLocalizacaoMagistradoServidorManager;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SecaoJudiciaria;
import br.jus.pje.nucleo.entidades.Usuario;

@Name(value = EstatisticaAudienciaAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class EstatisticaAudienciaAction implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5218648395846128754L;

	public static final String NAME = "estatisticaAudienciaAction";

	private static final String TEMPLATE_SINTETICO_XLS_PATH = "/EstatisticaProcessoJusticaFederal/EstatisticaAudiencia/relatorioSinteticoTemplate.xls";
	private static final String TEMPLATE_ANALITICO_XLS_PATH = "/EstatisticaProcessoJusticaFederal/EstatisticaAudiencia/relatorioAnaliticoTemplate.xls";
	private static final String DOWNLOAD_XLS_NAME = "AudienciaProcessos.xls";

	@In
	private EstatisticaProcessoJusticaFederalManager estatisticaProcessoJusticaFederalManager;
	@In
	private ProcessoAudienciaManager processoAudienciaManager;
	@In
	private SecaoJudiciariaManager secaoJudiciariaManager;
	@In
	private ProcessoTrfManager processoTrfManager;
	@In
	private RelatorioLogManager relatorioLogManager;
	@In
	private UsuarioLocalizacaoMagistradoServidorManager usuarioLocalizacaoMagistradoServidorManager;

	private List<SecaoJudiciaria> secaoJudiciariaList;
	private SecaoJudiciaria secaoJudiciaria;
	private OrgaoJulgador orgaoJulgador;
	private Date dataInicio;
	private Date dataFim;
	private String dataInicioStr;
	private String dataFimStr;
	private String dataInicioFormatada;
	private String dataFimFormatada;
	private EstatisticaProcessoAudienciaBean audienciaBean;
	private PessoaMagistrado juiz;
	private boolean relatorioSinteticoAnalitico = true;
	private long totalAcordosHomologados;
	private double valorAcordosHomologados;
	private String token;

	public EstatisticaProcessoAudienciaBean estatisticaAudienciaList() {
		audienciaBean = buildAudienciaProcessoAnalitico();
		return audienciaBean;
	}

	private EstatisticaProcessoAudienciaBean buildAudienciaProcessoAnalitico() {
		EstatisticaProcessoAudienciaBean bean = new EstatisticaProcessoAudienciaBean();
		EstatisticaJFProcessosAudienciaVara vara = new EstatisticaJFProcessosAudienciaVara();
		Map<ClasseJudicial, EstatisticaProcessosAudienciaClasses> subList = new HashMap<ClasseJudicial, EstatisticaProcessosAudienciaClasses>();
		bean.setSecaoJustica(secaoJudiciaria.getSecaoJudiciaria());
		vara.setVara(getOrgaoJulgador().getOrgaoJulgador());
		if (!relatorioSinteticoAnalitico) {
			dataInicioFormatada = formatarDataAnalitico(dataInicioStr, true);
			dataFimFormatada = formatarDataAnalitico(dataInicioStr, false);
		} else {
			dataInicioFormatada = formatarAnoMesDia(dataInicio);
			dataFimFormatada = formatarAnoMesDia(dataFim);
		}
		/**
		 * Percorrendo o Map para retornar as classes da Seção
		 */
		EstatisticaAudienciaProcessoClasseList classesList = new EstatisticaAudienciaProcessoClasseList();
		Map<ClasseJudicial, List<ProcessoTrf>> existente = criarLista(classesList.getResultList());
		for (Map.Entry<ClasseJudicial, List<ProcessoTrf>> entry : existente.entrySet()) {
			ClasseJudicial classe = entry.getKey();

			if (!subList.containsKey(classe)) {
				EstatisticaProcessosAudienciaClasses classeJudicial = new EstatisticaProcessosAudienciaClasses();
				classeJudicial.setClasse(classe);
				subList.put(classe, classeJudicial);
			}
		}
		/**
		 * Laço para retornar os Processos Remanescente da Seção
		 */
		EstatisticaAudienciaProcessoRemanescenteList processosRemanescentesList = new EstatisticaAudienciaProcessoRemanescenteList();
		Map<ClasseJudicial, List<ProcessoTrf>> existenteRemanescente = criarLista(processosRemanescentesList
				.getResultList());
		for (Map.Entry<ClasseJudicial, List<ProcessoTrf>> entry : existenteRemanescente.entrySet()) {
			ClasseJudicial classe = entry.getKey();
			List<ProcessoTrf> processos = entry.getValue();

			if (!subList.containsKey(classe)) {
				EstatisticaProcessosAudienciaClasses classeProcessos = new EstatisticaProcessosAudienciaClasses();
				classeProcessos.setClasse(classe);
				subList.put(classe, classeProcessos);
			}
			subList.get(classe).setListProcessRemanescente(processos);
			subList.get(classe).setListProcessPendentes(processos);
		}

		/**
		 * Laço para retornar os Processos Designados
		 */
		EstatisticaAudienciaProcessoDesignadosList processosDesignadosList = new EstatisticaAudienciaProcessoDesignadosList();
		Map<ClasseJudicial, List<ProcessoTrf>> existenteDesignados = criarLista(processosDesignadosList.getResultList());
		for (Map.Entry<ClasseJudicial, List<ProcessoTrf>> entry : existenteDesignados.entrySet()) {
			ClasseJudicial classe = entry.getKey();
			List<ProcessoTrf> processos = entry.getValue();

			if (!subList.containsKey(classe)) {
				EstatisticaProcessosAudienciaClasses classeProcessos = new EstatisticaProcessosAudienciaClasses();
				classeProcessos.setClasse(classe);
				subList.put(classe, classeProcessos);
			}

			subList.get(classe).setListProcessDesignado(processos);
			subList.get(classe).setListProcessPendentes(processos);
		}

		/**
		 * Laço para retornar os Processos Realizados
		 */
		EstatisticaAudienciaProcessoRealizadosList processosRealizadosList = new EstatisticaAudienciaProcessoRealizadosList();
		Map<ClasseJudicial, List<ProcessoTrf>> existenteRealizadosList = criarLista(processosRealizadosList
				.getResultList());
		for (Map.Entry<ClasseJudicial, List<ProcessoTrf>> entry : existenteRealizadosList.entrySet()) {
			ClasseJudicial classe = entry.getKey();
			List<ProcessoTrf> processos = entry.getValue();

			if (!subList.containsKey(classe)) {
				EstatisticaProcessosAudienciaClasses classeProcessos = new EstatisticaProcessosAudienciaClasses();
				classeProcessos.setClasse(classe);
				subList.put(classe, classeProcessos);
			}
			subList.get(classe).setListProcessRealizados(processos);
		}

		/**
		 * Laço para retornar os Processos Adiados
		 */
		EstatisticaAudienciaProcessoAdiadosList processosAdiadosList = new EstatisticaAudienciaProcessoAdiadosList();
		Map<ClasseJudicial, List<ProcessoTrf>> existenteAdiados = criarLista(processosAdiadosList.getResultList());
		for (Map.Entry<ClasseJudicial, List<ProcessoTrf>> entry : existenteAdiados.entrySet()) {
			ClasseJudicial classe = entry.getKey();
			List<ProcessoTrf> processos = entry.getValue();

			if (!subList.containsKey(classe)) {
				EstatisticaProcessosAudienciaClasses classeProcessos = new EstatisticaProcessosAudienciaClasses();
				classeProcessos.setClasse(classe);
				subList.put(classe, classeProcessos);
			}
			subList.get(classe).setListProcessAdiados(processos);
		}

		/**
		 * Laço para retornar os Processos Cancelados
		 */
		EstatisticaAudienciaProcessoCanceladosList canceladosList = new EstatisticaAudienciaProcessoCanceladosList();
		Map<ClasseJudicial, List<ProcessoTrf>> cancelados = criarLista(canceladosList.getResultList());
		for (Map.Entry<ClasseJudicial, List<ProcessoTrf>> entry : cancelados.entrySet()) {
			ClasseJudicial classe = entry.getKey();
			List<ProcessoTrf> processos = entry.getValue();

			if (!subList.containsKey(classe)) {
				EstatisticaProcessosAudienciaClasses classeProcessos = new EstatisticaProcessosAudienciaClasses();
				classeProcessos.setClasse(classe);
				subList.put(classe, classeProcessos);
			}
			subList.get(classe).setListProcessCancelados(processos);
		}

		/**
		 * Laço para retornar os Processos Suspensos
		 */
		EstatisticaAudienciaProcessoSuspensosList suspensosList = new EstatisticaAudienciaProcessoSuspensosList();
		Map<ClasseJudicial, List<ProcessoTrf>> suspensos = criarLista(suspensosList.getResultList());
		for (Map.Entry<ClasseJudicial, List<ProcessoTrf>> entry : suspensos.entrySet()) {
			ClasseJudicial classe = entry.getKey();
			List<ProcessoTrf> processos = entry.getValue();

			if (!subList.containsKey(classe)) {
				EstatisticaProcessosAudienciaClasses classeProcessos = new EstatisticaProcessosAudienciaClasses();
				classeProcessos.setClasse(classe);
				subList.put(classe, classeProcessos);
			}
			subList.get(classe).setListProcessSuspensos(processos);
		}
		// FIM dos laços para setar cada processo a sua classe
		List<EstatisticaProcessosAudienciaClasses> list = new ArrayList<EstatisticaProcessosAudienciaClasses>(
				subList.values());
		vara.setSubList(list);
		bean.setVara(vara);
		if (relatorioSinteticoAnalitico) {
			if (getJuiz() != null) {
				setTotalAcordosHomologados(processoAudienciaManager.totalAcordosHomologadosJuiz(
						getDataInicioFormatada(), getDataFimFormatada(), getOrgaoJulgador(), getJuiz().getNome()));
				setValorAcordosHomologados(processoAudienciaManager.valorAcordosHomologadosJuiz(
						getDataInicioFormatada(), getDataFimFormatada(), getOrgaoJulgador(), getJuiz().getNome()));
			} else {
				setTotalAcordosHomologados(processoAudienciaManager.totalAcordosHomologados(getDataInicioFormatada(),
						getDataFimFormatada(), getOrgaoJulgador()));
				setValorAcordosHomologados(processoAudienciaManager.valorAcordosHomologados(getDataInicioFormatada(),
						getDataFimFormatada(), getOrgaoJulgador()));
			}
		}
		return bean;
	}

	private Map<ClasseJudicial, List<ProcessoTrf>> criarLista(List<Map<String, Object>> banco) {
		Map<ClasseJudicial, List<ProcessoTrf>> existente = new HashMap<ClasseJudicial, List<ProcessoTrf>>();
		for (Map<String, Object> bancoMap : banco) {
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
	 * Método que recebe a data e retorna a data de início: "yyyy-MM-01" e a
	 * data final: "yyyy-MM-dd (último dia do mês)
	 * 
	 * @param data
	 * @return
	 */
	public String formatarDataAnalitico(String dtInicio, boolean inicio) {
		String mes = dtInicio.substring(0, 2);
		String ano = dtInicio.substring(3);
		Calendar dataTemp = Calendar.getInstance();
		if (!inicio) {
			dataTemp.set(Integer.parseInt(ano), Integer.parseInt(mes) - 1, 1);
			Integer dia = dataTemp.getActualMaximum(Calendar.DAY_OF_MONTH);
			dataTemp.set(Integer.parseInt(ano), Integer.parseInt(mes) - 1, dia);
		} else {
			dataTemp.set(Integer.parseInt(ano), Integer.parseInt(mes) - 1, 1);
		}
		Format formatter = new SimpleDateFormat("yyyy-MM-dd");
		return formatter.format(dataTemp.getTime());
	}

	/**
	 * Método que recebe uma data e transforma para "yyyy-MM-dd"
	 * 
	 * @param data
	 * @return
	 */
	public String formatarAnoMesDia(Date data) {
		Format formatter = new SimpleDateFormat("yyyy-MM-dd");
		return formatter.format(data);
	}

	/**
	 * Retorna todas as varas de uma determinada seção, caso o usuário tenha OJ
	 * retorna o dele.
	 * 
	 * @return
	 */
	public List<OrgaoJulgador> listOrgaoJulgadorItems() {
		if (ParametroUtil.instance().isPrimeiroGrau()) {
			secaoJudiciaria = secaoJudiciariaManager.secaoJudiciaria1Grau();
		}
		List<OrgaoJulgador> items = new ArrayList<OrgaoJulgador>();
		if (Authenticator.getOrgaoJulgadorAtual() != null) {
			items.add(Authenticator.getOrgaoJulgadorAtual());
			orgaoJulgador = Authenticator.getOrgaoJulgadorAtual();
			return items;
		} else if (secaoJudiciaria != null) {
			for (OrgaoJulgador s : estatisticaProcessoJusticaFederalManager
					.buscaListaOrgaoJulgador(getSecaoJudiciaria().getCdSecaoJudiciaria())) {
				if (s != null) {
					items.add(s);
				}
			}
		}
		return items;
	}

	/**
	 * Retorna todos os juizes.
	 * 
	 * @return
	 */
	public List<Usuario> listJuizPorOrgaoJulgadorItems() {
		List<Usuario> items = new ArrayList<Usuario>();
		for (Usuario u : usuarioLocalizacaoMagistradoServidorManager.juizes()) {
			items.add(u);
		}
		return items;
	}

	public int getRowspan() {
		int rowspan = 0;
		if (audienciaBean != null) {
			if (!relatorioSinteticoAnalitico) {
				rowspan = (audienciaBean.getVara().getSubList().size() * 8) + 3;
			} else {
				rowspan = (audienciaBean.getVara().getSubList().size() + 7);
			}
		}
		return rowspan;
	}

	public static EstatisticaAudienciaAction intance() {
		return ComponentUtil.getComponent(NAME);
	}

	/**
	 * Método que grava o log das consultas de relarório caso a consulta retorne
	 * registros.
	 * 
	 * @param registros
	 *            quantidade de registros da lista
	 */
	public void gravarLogRelatorio() {
		audienciaBean = null;
		if (estatisticaAudienciaList() != null) {
			relatorioLogManager.persist("Estatística de Audiências de Processos", Authenticator.getUsuarioLogado());
		}
	}

	/**
	 * Método que exporta o resultado da consulta para excel, caso a consulta
	 * retorne registros
	 * 
	 * @param registros
	 *            total de registros da consulta
	 */
	public void exportarAudienciaProcessoXLS() {
		String template = null;
		if (relatorioSinteticoAnalitico) {
			template = TEMPLATE_SINTETICO_XLS_PATH;
		} else {
			template = TEMPLATE_ANALITICO_XLS_PATH;
		}
		try {
			if (audienciaBean != null) {
				exportarXLS(template);
			} else {
				FacesMessages.instance().add(Severity.INFO, "Não há dados para exportar!");
			}
		} catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao exportar arquivo." + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Traz o nome do Diretor da Vara selecionada (Orgão Julgador)
	 * 
	 * @return String com o nome
	 */
	public String getDiretorVara() {
		Usuario diretorVara = processoTrfManager.getDiretorVara(orgaoJulgador);
		return diretorVara != null ? diretorVara.getNome() : "";
	}

	/**
	 * Traz o nome do Juiz Federal da Vara selecionada (Orgão Julgador)
	 * 
	 * @return String com o nome
	 */
	public String getJuizFederal() {
		Usuario juizFederal = processoTrfManager.getJuizFederal(orgaoJulgador);
		return juizFederal != null ? juizFederal.getNome() : "";
	}

	public void createToken() {
		try {
			if (secaoJudiciaria.getUrlAplicacao() != null && !secaoJudiciaria.getUrlAplicacao().isEmpty()) {
				token = TokenManager.instance().getRemoteToken(secaoJudiciaria.getUrlAplicacao());
			} else {
				FacesMessages.instance().add(
						Severity.ERROR,
						MessageFormat.format("URL da aplicação não está definida para a seção escolhida: {0}",
								secaoJudiciaria.getSecaoJudiciaria()));
			}
		} catch (IOException e) {
			String msgErro = MessageFormat.format("URL do Webservice não esta acessivel no estado {0}: {1}",
					secaoJudiciaria.getSecaoJudiciaria(), e.getMessage());
			FacesMessages.instance().add(Severity.ERROR, msgErro);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void validarToken() {
		TokenManager.instance().validateToken(token);
	}

	public void exportarXLS(String template) throws ExcelExportException {
		String urlTemplate = new Util().getContextRealPath() + template;
		ExcelExportUtil.downloadXLS(urlTemplate, beanExportarXLS(), DOWNLOAD_XLS_NAME);
	}

	private Map<String, Object> beanExportarXLS() {
		List<EstatisticaProcessoAudienciaBean> list = new ArrayList<EstatisticaProcessoAudienciaBean>();
		list.add(getAudienciaBean());
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("audienciaProcessoBean", list);
		map.put("titulo", Messages.instance().get("estatisticaProcessosAudiencia.relatorioTitulo"));
		map.put("subNomeSistema", ParametroUtil.getParametro("nomeSecaoJudiciaria").toUpperCase());
		map.put("nomeSistema", ParametroUtil.getParametro("nomeSistema"));
		map.put("orgaoJulgador", orgaoJulgador);
		map.put("secaoJudiciaria", secaoJudiciaria);
		map.put("juizVara", getJuizFederal());
		map.put("diretorSecretaria", getDiretorVara());
		map.put("totalAcordos", getTotalAcordosHomologados());
		map.put("valorAcordos", getValorAcordosHomologados());
		map.put("juiz", juiz);
		if (relatorioSinteticoAnalitico) {
			String dataFormatada = "";
			SimpleDateFormat formatInicio = new SimpleDateFormat("dd/MM/yyyy");
			dataFormatada = formatInicio.format(dataInicio);
			String dataFormatadaFim = "";
			SimpleDateFormat formatFim = new SimpleDateFormat("dd/MM/yyyy");
			dataFormatadaFim = formatFim.format(dataFim);
			map.put("dataFormatada", dataFormatada);
			map.put("dataFormatadaFim", dataFormatadaFim);
		} else {
			map.put("dataInicioStr", dataInicioStr);
		}

		return map;
	}

	public void limparFiltros() {
		setRelatorioSinteticoAnalitico(true);
	}

	public void acaoRadio() {
		if (!ParametroUtil.instance().isPrimeiroGrau()) {
			secaoJudiciaria = null;
		}
		orgaoJulgador = null;
		dataInicio = null;
		dataFim = null;
		dataInicioStr = null;
		dataFimStr = null;
		juiz = null;
	}

	/*
	 * Inicio - Getters and Setters
	 */
	public void setSecaoJudiciariaList(List<SecaoJudiciaria> secaoJudiciariaList) {
		this.secaoJudiciariaList = secaoJudiciariaList;
	}

	public void secao() {
		if (ParametroUtil.instance().isPrimeiroGrau()) {
			secaoJudiciariaManager.listSecaoJudiciaria1Grau();
		} else {
			secaoJudiciariaItems();
		}
	}

	public List<SecaoJudiciaria> getSecaoJudiciariaList() {
		if (secaoJudiciariaList == null) {
			secaoJudiciariaList = secaoJudiciariaItems();
		}
		return secaoJudiciariaList;
	}

	public void setSecaoJudiciaria(SecaoJudiciaria secaoJudiciaria) {
		this.secaoJudiciaria = secaoJudiciaria;
	}

	public SecaoJudiciaria getSecaoJudiciaria() {
		return secaoJudiciaria;
	}

	private List<SecaoJudiciaria> secaoJudiciariaItems() {
		return secaoJudiciariaManager.secaoJudiciariaItems();
	}

	public void secaoJudiciaria1Grau() {
		secaoJudiciaria = secaoJudiciariaManager.secaoJudiciaria1Grau();
	}

	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
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
		this.dataFim = dataFim;
	}

	public String getDataInicioStr() {
		return dataInicioStr;
	}

	public void setDataInicioStr(String dataInicioStr) {
		this.dataInicioStr = dataInicioStr;
	}

	public PessoaMagistrado getJuiz() {
		return juiz;
	}

	public void setJuiz(PessoaMagistrado juiz) {
		this.juiz = juiz;
	}

	public boolean isRelatorioSinteticoAnalitico() {
		return relatorioSinteticoAnalitico;
	}

	public void setRelatorioSinteticoAnalitico(boolean relatorioSinteticoAnalitico) {
		this.relatorioSinteticoAnalitico = relatorioSinteticoAnalitico;
	}

	public EstatisticaProcessoAudienciaBean getAudienciaBean() {
		return audienciaBean;
	}

	public void setAudienciaBean(EstatisticaProcessoAudienciaBean audienciaBean) {
		this.audienciaBean = audienciaBean;
	}

	public void setDataFimStr(String dataFimStr) {
		this.dataFimStr = dataFimStr;
	}

	public String getDataFimStr() {
		return dataFimStr;
	}

	public long getTotalAcordosHomologados() {
		return totalAcordosHomologados;
	}

	public void setTotalAcordosHomologados(long totalAcordosHomologados) {
		this.totalAcordosHomologados = totalAcordosHomologados;
	}

	public double getValorAcordosHomologados() {
		return valorAcordosHomologados;
	}

	public void setValorAcordosHomologados(double valorAcordosHomologados) {
		this.valorAcordosHomologados = valorAcordosHomologados;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getDataInicioFormatada() {
		return dataInicioFormatada;
	}

	public void setDataInicioFormatada(String dataInicioFormatada) {
		this.dataInicioFormatada = dataInicioFormatada;
	}

	public String getDataFimFormatada() {
		return dataFimFormatada;
	}

	public void setDataFimFormatada(String dataFimFormatada) {
		this.dataFimFormatada = dataFimFormatada;
	}
}