package br.com.infox.pje.action;

import java.io.Serializable;
import java.util.ArrayList;
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
import br.com.infox.pje.bean.EstatisticaRankingRegionalBean;
import br.com.infox.pje.bean.EstatisticaRankingRegionalListBean;
import br.com.infox.pje.list.EstatisticaProcessosArquivadosRankingRegionalList;
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
 * Classe action controladora do listView de
 * /EstatisticaProcesso/ArquivadosRankingRegional/
 * 
 * @author thiago
 * 
 */
@Name(value = ArquivadosRankingRegionalAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class ArquivadosRankingRegionalAction implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2687614199152569991L;
	public static final String NAME = "arquivadosRankingRegionalAction";
	private EstatisticaProcessosArquivadosRankingRegionalList rankingList = new EstatisticaProcessosArquivadosRankingRegionalList();
	private static final String TEMPLATE_XLS_PATH = "/EstatisticaProcesso/ArquivadosRankingRegional/procArquivadosRankingRegionalTemplate.xls";
	private static final String DOWNLOAD_XLS_NAME = "ArquivadosRankingRegional.xls";
	private String dataInicioFormatada; 
	private String dataFimFormatada;
	private String dataInicio;
	private String dataFim;
	private long totalProcessos;
	private int totalVaras;
	private String rodape;
	private String erroAtualizarSecao = "";
	private List<EstatisticaRankingRegionalBean> estatisticaBeanList;

	@In
	private EstatisticaEventoProcessoManager estatisticaEventoProcessoManager;
	@In
	private HistoricoEstatisticaEventoProcessoManager historicoEstatisticaEventoProcessoManager;
	@In
	private GenericManager genericManager;

	/**
	 * Método que recebe a data "MM/yyyy" e transforma para "yyyy-MM"
	 * @param data
	 * @return
	 */
	public String formatarAnoMes(String data) {
		return data.substring(3) + "-" + data.substring(0, 2); 
	}
	
	/**
	 * Obtem o relatório da estatística convertendo a lista proveniente do banco
	 * para uma lista no esquema da exibição necessária para a tela utilizando o
	 * EstatisticaRankingRegionalBean.java
	 * 
	 * @return lista com os dados para o ranking
	 */
	public List<EstatisticaRankingRegionalBean> estatisticaRankingList() {
		String erro = "";
		StringBuilder erroTemp = new StringBuilder();
		String rodapeTemp = "";
		if (estatisticaBeanList == null) {
			dataInicioFormatada = formatarAnoMes(dataInicio); 
			dataFimFormatada = formatarAnoMes(dataFim); 
			estatisticaBeanList = new ArrayList<EstatisticaRankingRegionalBean>();
			List<Object[]> resultList = getRankingList().getResultList();
			totalProcessos = 0;
			totalVaras = 0;
			if (resultList != null && resultList.size() > 0) {
				EstatisticaRankingRegionalBean errb = new EstatisticaRankingRegionalBean();
				estatisticaBeanList.add(errb);
				int rankingRegional = 0;
				for (Object[] o : resultList) {
					totalVaras++;
					rankingRegional++;

					EstatisticaRankingRegionalListBean errlb = new EstatisticaRankingRegionalListBean();
					errlb.setVara(getVara(o));
					long numeroProcessos = Long.parseLong(o[2].toString());
					errlb.setQtdProcessos(numeroProcessos);
					totalProcessos += numeroProcessos;
					errlb.setRankingRegional(rankingRegional + "º");

					errb.getEstatisticaRankingListBean().add(errlb);
				}
			}
			estatisticaBeanList = invertList(estatisticaBeanList);

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
					StringBuilder sb = new StringBuilder();
					sb.append(erroTemp);
					sb.replace(pos, pos + 2, " e ");
					sb.insert(erroTemp.length() + 1, '.');
					erroTemp = sb;
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
	 *            , sendo [1] = descrição do orgão julgador e [2] = descricao da
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
		if (estatisticaRankingList().size() > 0) {
			RelatorioLog relatorioLog = new RelatorioLog();
			relatorioLog.setDataSolicitacao(new Date());
			relatorioLog.setDescricao("Estatística de Processos Arquivados –- Ranking Regional");
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
				exportarXLS(TEMPLATE_XLS_PATH, DOWNLOAD_XLS_NAME);
			} else {
				FacesMessages.instance().add(Severity.INFO, "Não há dados para exportar!");
			}
		} catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao exportar arquivo." + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Método que exporta lits em planilhas do excel
	 * 
	 * @param dirNomeTemplate
	 *            Caminho com nome do template excel
	 * @param nomeArqDown
	 *            Nome usado para download do arquivo
	 * @param nomeListaTemplate
	 *            Nome da lista usada dentro do template
	 * @param lista
	 *            Lista com os dados a serem exportados
	 * @throws ExcelExportException
	 */
	public void exportarXLS(String dirNomeTemplate, String nomeArqDown) throws ExcelExportException {
		String urlTemplate = new Util().getContextRealPath() + dirNomeTemplate;
		ExcelExportUtil.downloadXLS(urlTemplate, beanExportarXLS(), nomeArqDown);
	}

	private Map<String, Object> beanExportarXLS() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("estatisticaRankingList", estatisticaRankingList());
		map.put("titulo", "ESTATÍSTICA DE PROCESSOS ARQUIVADOS - RANKING/REGIONAL");
		map.put("subNomeSistema", ParametroUtil.getParametro("nomeSecaoJudiciaria").toUpperCase());
		map.put("nomeSistema", ParametroUtil.getParametro("nomeSistema"));
		map.put("totalProcessos", totalProcessos);
		map.put("totalVaras", totalVaras);
		if (!erroAtualizarSecao.isEmpty()) {
			map.put("rodape", rodape);
			map.put("erroAtualizarSecao", erroAtualizarSecao);
		}
		if (dataInicio != null && dataFim != null) {
			map.put("dataInicio", dataInicio);
			map.put("dataFim", dataFim);
		}
		return map;
	}

	/**
	 * Inverte a ordem da lista informada. Último vira primeiro e primeiro vira
	 * último.
	 * 
	 * @param <T>
	 * @param resultList
	 * @return
	 */
	private <T> List<T> invertList(List<T> resultList) {
		List<T> listaInvertida = new ArrayList<T>();
		for (int i = (resultList.size() - 1); i >= 0; i--) {
			listaInvertida.add(resultList.get(i));
		}
		return listaInvertida;
	}

	/*
	 * Inicio - Getters and Setters
	 */
	public EstatisticaProcessosArquivadosRankingRegionalList getRankingList() {
		return rankingList;
	}

	public void setRankingList(EstatisticaProcessosArquivadosRankingRegionalList rankingList) {
		this.rankingList = rankingList;
	}

	public void setDataInicio(String dataInicio) {
		this.dataInicio = dataInicio;
	}

	public String getDataInicio() {
		return dataInicio;
	}

	public void setDataFim(String dataFim) {
		this.dataFim = dataFim;
	}

	public String getDataFim() {
		return dataFim;
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
	public static EstatisticaProcessosJulgadosRankingSecaoAction instance() {
		return ComponentUtil.getComponent(NAME);
	}

	public void pesquisar() {
		// limpa a lista para uma nova pesquisa
		estatisticaBeanList = null;
	}

	public void limparFiltros() {
		dataInicio = null;
		dataFim = null;
		erroAtualizarSecao = "";
		rodape = null;
	}

	public void setDataInicioFormatada(String dataInicioFormatada) {
		this.dataInicioFormatada = dataInicioFormatada;
	}

	public String getDataInicioFormatada() {
		return dataInicioFormatada;
	}

	public void setDataFimFormatada(String dataFimFormatada) {
		this.dataFimFormatada = dataFimFormatada;
	}

	public String getDataFimFormatada() {
		return dataFimFormatada;
	}

}