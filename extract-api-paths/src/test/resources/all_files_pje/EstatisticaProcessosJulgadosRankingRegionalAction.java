package br.com.infox.pje.action;

import java.io.Serializable;
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
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.manager.GenericManager;
import br.com.infox.pje.bean.EstatisticaJulgadosRankingRegionalBean;
import br.com.infox.pje.bean.EstatisticaJulgadosRankingRegionalListBean;
import br.com.infox.pje.list.EstatisticaProcessosJulgadosRankingRegionalList;
import br.com.infox.pje.manager.EstatisticaEventoProcessoManager;
import br.com.infox.pje.manager.HistoricoEstatisticaEventoProcessoManager;
import br.com.itx.component.Util;
import br.com.itx.exception.ExcelExportException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.ExcelExportUtil;
import br.jus.pje.nucleo.entidades.RelatorioLog;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * Classe action controladora do listView de /EstatisticaProcesso/
 * JulgadosRankingRegional/
 * 
 * @author rafael
 * 
 */
@Name(value = EstatisticaProcessosJulgadosRankingRegionalAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class EstatisticaProcessosJulgadosRankingRegionalAction implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8389385742008137486L;

	public static final String NAME = "estatisticaProcessosJulgadosRankingRegionalAction";

	private EstatisticaProcessosJulgadosRankingRegionalList rankingList = new EstatisticaProcessosJulgadosRankingRegionalList();
	private static final String TEMPLATE_XLS_PATH = "/EstatisticaProcesso/JulgadosRankingRegional/procJulgadosRankingRegionalTemplate.xls";

	private Date dataInicio;
	private Date dataFim;
	private String dataInicioStr;
	private String dataFimStr;
	private long totalProcessos;
	private int totalVaras;
	private List<EstatisticaJulgadosRankingRegionalBean> estatisticaBeanList;
	private String codEstado;
	private String competencia;
	private String erroAtualizarSecao = "";
	private String rodape;

	@In
	private EstatisticaEventoProcessoManager estatisticaEventoProcessoManager;
	@In
	private HistoricoEstatisticaEventoProcessoManager historicoEstatisticaEventoProcessoManager;
	@In
	private GenericManager genericManager;

	/**
	 * Obtem o relatório da estatística convertendo a lista proveniente do banco
	 * para uma lista no esquema da exibição necessária para a tela utilizando o
	 * EstatisticaRankingSecaoBean.java
	 * 
	 * @return lista com os dados para o ranking
	 */
	public List<EstatisticaJulgadosRankingRegionalBean> estatisticaRankingRegionalList() {
		String erro = "";
		StringBuilder erroTemp = new StringBuilder();
		String rodapeTemp = "";
		if (estatisticaBeanList == null) {
			estatisticaBeanList = new ArrayList<EstatisticaJulgadosRankingRegionalBean>();
			List<Object[]> resultList = getRankingList().getResultList();
			totalProcessos = 0;
			totalVaras = 0;
			if (resultList != null && resultList.size() > 0) {
				EstatisticaJulgadosRankingRegionalBean ertvb = new EstatisticaJulgadosRankingRegionalBean();
				estatisticaBeanList.add(ertvb);
				int rankingEstado = 0;
				for (Object[] o : resultList) {
					totalVaras++;
					rankingEstado++;
					Integer varasEstado = ertvb.getTotalVarasEstado();
					ertvb.setTotalVarasEstado(varasEstado == null ? 1 : varasEstado + 1);
					long numeroProcessos = Long.parseLong(o[2].toString());
					Long processosEstado = ertvb.getTotalProcessosEstado();
					ertvb.setTotalProcessosEstado(processosEstado == null ? numeroProcessos : processosEstado
							+ numeroProcessos);
					totalProcessos += numeroProcessos;
					EstatisticaJulgadosRankingRegionalListBean erlb = new EstatisticaJulgadosRankingRegionalListBean();
					erlb.setVaras(getVara(o));
					erlb.setQntProcessos(String.valueOf(numeroProcessos));
					erlb.setRankingRegional(rankingEstado + "º");
					ertvb.getEstatisticaRankingRegionalListBean().add(erlb);
				}
			}
			if (!ParametroUtil.instance().isPrimeiroGrau()) {
				List<Object[]> resultListAtualizacao = historicoEstatisticaEventoProcessoManager
						.listSecaoNaoAtualizada();
				if (resultListAtualizacao.size() > 0) {
					for (Object[] objects : resultListAtualizacao) {
						erro = ("SJ" + objects[0] + "(atualizado até o dia " + objects[1] + "), ");
						erroTemp.append(erro);
					}
					erroTemp = new StringBuilder(erroTemp.substring(0, erroTemp.lastIndexOf(",")));

					int pos = erroTemp.lastIndexOf(",");
					StringBuffer sb = new StringBuffer();
					sb.append(erroTemp);
					sb.replace(pos, pos + 2, " e ");
					sb.insert(erroTemp.length() + 1, '.');
					erroTemp = new StringBuilder(sb.toString());
					rodapeTemp = "Dados não computados na(s) Seção(ões): ";
					rodape = rodapeTemp;
					erroAtualizarSecao = erroTemp.toString();
					erroTemp = new StringBuilder();
					rodapeTemp = "";
				}
			}
		}
		return estatisticaBeanList;
	}

	/**
	 * Formata a vara para a exibição no relatório do ranking da seção
	 * 
	 * @param o
	 *            , sendo [1] = descrção do orgão julgador e [2] = descricao da
	 *            jurisdição
	 * @return
	 */
	private String getVara(Object[] o) {
		StringBuilder sb = new StringBuilder();
		sb.append(StringUtil.limparCharsNaoNumericos(o[0].toString())).append("ª - ").append(o[1]).append(" ")
				.append(getCompetenciaList(o[0].toString()));
		return sb.toString();
	}

	/**
	 * obtem a lista de competencias concatenadas com + dentro de um parenteses,
	 * especifico pra exibição do relatório.
	 * 
	 * @param orgaoJulgador
	 * @return lista de competencias concatenadas.
	 */
	private String getCompetenciaList(String orgaoJulgador) {
		List<String> listaCompetencia = estatisticaEventoProcessoManager.listCompentenciaByOrgaoJulgador(orgaoJulgador);
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for (String string : listaCompetencia) {
			if (string != null) {
				if (sb.length() > 1) {
					sb.append(" + ");
				}
				sb.append(string);
			}
		}
		sb.append(")");
		return sb.toString();
	}

	/**
	 * Método que grava o log das consultas de relarório caso a consulta retorne
	 * registros.
	 * 
	 * @param registros
	 *            quantidade de registros da lista
	 */
	public void gravarLogRelatorio() {
		estatisticaBeanList = null;
		if (estatisticaRankingRegionalList().size() > 0) {
			RelatorioLog relatorioLog = new RelatorioLog();
			relatorioLog.setDataSolicitacao(new Date());
			relatorioLog.setDescricao("Estatística de Processos Julgados - Ranking/Regional");
			Usuario usuario = (Usuario) Contexts.getSessionContext().get("usuarioLogado");
			relatorioLog.setIdUsuarioSolicitacao(usuario);
			genericManager.persist(relatorioLog);
		}
	}

	/**
	 * Método que exporta o resultado da consulta para excel, caso a consulta
	 * retorne registros
	 * 
	 * @param registros
	 *            total de registros da consulta
	 */
	public void exportarProcessoRankingRegionalXLS() {
		try {
			if (estatisticaBeanList.size() > 0) {
				exportarXLS(TEMPLATE_XLS_PATH);
			} else {
				FacesMessages.instance().add(Severity.INFO, "Não há dados para exportar!");
			}
		} catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao exportar arquivo." + e.getMessage());
			e.printStackTrace();
		}
	}

	public void exportarXLS(String template) throws ExcelExportException {
		String urlTemplate = new Util().getContextRealPath() + template;
		ExcelExportUtil.downloadXLS(urlTemplate, beanExportarXLS(), "Ranking/Regional.xls");
	}

	private Map<String, Object> beanExportarXLS() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("estatisticaBeanList", getEstatisticaBeanList());
		map.put("totalProcessos", totalProcessos);
		map.put("totalVaras", totalVaras);
		if (!erroAtualizarSecao.isEmpty()) {
			map.put("rodape", rodape);
			map.put("erroAtualizarSecao", erroAtualizarSecao);
		}
		EstatisticaProcessosJulgadosRankingRegionalList rankingList = ComponentUtil
				.getComponent(EstatisticaProcessosJulgadosRankingRegionalList.NAME);
		String incluirEmbargosDeclaracao = rankingList.getIncluiEmbargosDeclaracao() ? "SIM" : "NÃO";
		map.put("IncluirEmbargosDeclaracao", "Incluir Embargos de Declaração: " + incluirEmbargosDeclaracao);
		if (dataInicio != null && dataFim != null) {
			String dataFormatada = "";
			SimpleDateFormat formatoInicio = new SimpleDateFormat("MM/yyyy");
			dataFormatada = formatoInicio.format(dataInicio);
			map.put("dataInicio", dataFormatada);
			SimpleDateFormat formatoFim = new SimpleDateFormat("MM/yyyy");
			dataFormatada = formatoFim.format(dataFim);
			map.put("dataFim", dataFormatada);
		}
		map.put("titulo", "ESTATÍSTICA DE PROCESSOS JULGADOS - RANKING/REGIONAL");
		map.put("subNomeSistema", ParametroUtil.getParametro("nomeSecaoJudiciaria").toUpperCase());
		map.put("nomeSistema", ParametroUtil.getParametro("nomeSistema"));
		return map;
	}

	/*
	 * Inicio - Getters and Setters
	 */
	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	public Date getDataInicio() {
		if (getDataInicioStr() != null) {
			String[] data = getDataInicioStr().split("/");
			int mes = Integer.valueOf(data[0]);
			int ano = Integer.valueOf(data[1]);
			Calendar c = Calendar.getInstance();
			c.set(ano, mes - 1, 1);
			dataInicio = c.getTime();
		}
		return dataInicio;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = dataFim;
	}

	public Date getDataFim() {
		if (getDataFimStr() != null) {
			String[] data = getDataFimStr().split("/");
			int mes = Integer.valueOf(data[0]);
			int ano = Integer.valueOf(data[1]);
			Calendar c = Calendar.getInstance();
			c.set(ano, mes, 0);
			dataFim = c.getTime();
		}
		return dataFim;
	}

	public void setRankingList(EstatisticaProcessosJulgadosRankingRegionalList rankingList) {
		this.rankingList = rankingList;
	}

	public EstatisticaProcessosJulgadosRankingRegionalList getRankingList() {
		return rankingList;
	}

	public void setTotalVaras(int totalVaras) {
		this.totalVaras = totalVaras;
	}

	public int getTotalVaras() {
		return totalVaras;
	}

	public void setTotalProcessos(long totalProcessos) {
		this.totalProcessos = totalProcessos;
	}

	public long getTotalProcessos() {
		return totalProcessos;
	}

	public void setCodEstado(String codEstado) {
		this.codEstado = codEstado;
	}

	public String getCodEstado() {
		return codEstado;
	}

	public void setCompetencia(String competencia) {
		this.competencia = competencia;
	}

	public String getCompetencia() {
		return competencia;
	}

	public String getDataInicioStr() {
		return dataInicioStr;
	}

	public void setDataInicioStr(String dataInicioStr) {
		this.dataInicioStr = dataInicioStr;
	}

	public String getDataFimStr() {
		return dataFimStr;
	}

	public void setDataFimStr(String dataFimStr) {
		this.dataFimStr = dataFimStr;
	}

	public List<EstatisticaJulgadosRankingRegionalBean> getEstatisticaBeanList() {
		return estatisticaBeanList;
	}

	public void setEstatisticaBeanList(List<EstatisticaJulgadosRankingRegionalBean> estatisticaBeanList) {
		this.estatisticaBeanList = estatisticaBeanList;
	}

	public static EstatisticaProcessosJulgadosRankingRegionalAction instance() {
		return ComponentUtil.getComponent(NAME);
	}

	public String getErroAtualizarSecao() {
		return erroAtualizarSecao;
	}

	public void setErroAtualizarSecao(String erroAtualizarSecao) {
		this.erroAtualizarSecao = erroAtualizarSecao;
	}

	public String getRodape() {
		return rodape;
	}

	public void setRodape(String rodape) {
		this.rodape = rodape;
	}

	/*
	 * Fim - Getters and Setters
	 */
	public void limparFiltros() {
		dataFim = null;
		dataFimStr = null;
		dataInicio = null;
		dataInicioStr = null;
		erroAtualizarSecao = "";
		rodape = null;
	}
}