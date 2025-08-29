package br.com.infox.pje.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
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
import br.com.infox.pje.bean.EstatisticaDistribuidosRankingRegionalBean;
import br.com.infox.pje.bean.EstatisticaDistribuidosRankingRegionalListBean;
import br.com.infox.pje.list.EstatisticaProcessosDistribuidosRankingRegionalList;
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
 * DistribuidosRankingSessao/
 * 
 * @author Daniel
 * 
 */
@Name(value = EstatisticaProcessosDistribuidosRankingRegionalAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class EstatisticaProcessosDistribuidosRankingRegionalAction implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3837795863179611647L;

	public static final String NAME = "estatisticaProcessosDistribuidosRankingRegionalAction";

	private EstatisticaProcessosDistribuidosRankingRegionalList rankingList = new EstatisticaProcessosDistribuidosRankingRegionalList();
	private static final String TEMPLATE_XLS_PATH = "/EstatisticaProcesso/DistribuidosRankingRegional/procDistribuidosRankingTemplate.xls";
	private static final String DOWNLOAD_XLS_NAME = "Distribuidos.xls";

	private String dataInicio;
	private String dataFim;
	private String dataInicioFormatada;
	private String dataFimFormatada;
	private long totalProcessos;
	private int totalVaras;
	private String rodape;
	private String erroAtualizarSecao = "";

	private List<EstatisticaDistribuidosRankingRegionalBean> estatisticaBeanList;
	@In
	private EstatisticaEventoProcessoManager estatisticaEventoProcessoManager;
	@In
	private GenericManager genericManager;
	@In
	private HistoricoEstatisticaEventoProcessoManager historicoEstatisticaEventoProcessoManager;

	/**
	 * Método que recebe a data "MM/yyyy" e transforma para "yyyy-MM"
	 * 
	 * @param data
	 * @return
	 */
	public String formatarAnoMes(String data) {
		return data.substring(3) + "-" + data.substring(0, 2);
	}

	/**
	 * Obtem o relatório da estatística convertendo a lista proveniente do banco
	 * para uma lista no esquema da exibição necessária para a tela utilizando o
	 * EstatisticaProcessosDistribuidosRankingRegionalAction.java
	 * 
	 * @return lista com os dados para o ranking regional
	 */
	public List<EstatisticaDistribuidosRankingRegionalBean> estatisticaRankingList() {
		String erro = "";
		StringBuilder erroTemp = new StringBuilder();
		String rodapeTemp = "";
		String qntProcessosTemp = "";
		if (estatisticaBeanList == null) {
			dataInicioFormatada = formatarAnoMes(dataInicio);
			dataFimFormatada = formatarAnoMes(dataFim);
			estatisticaBeanList = new ArrayList<EstatisticaDistribuidosRankingRegionalBean>();
			List<Object[]> resultList = getRankingList().getResultList();
			totalProcessos = 0;
			totalVaras = 0;
			if (resultList != null && resultList.size() > 0) {
				EstatisticaDistribuidosRankingRegionalBean ersb = new EstatisticaDistribuidosRankingRegionalBean();
				estatisticaBeanList.add(ersb);
				int ranking = 0;
				for (Object[] o : resultList) {
					totalVaras++;
					Integer varasEstado = ersb.getTotalVaras();
					ersb.setTotalVaras(varasEstado == null ? 1 : varasEstado + 1);
					long numeroProcessos = Long.parseLong(o[2].toString());
					Long processosEstado = ersb.getTotalProcessos();
					ersb.setTotalProcessos(processosEstado == null ? numeroProcessos : processosEstado
							+ numeroProcessos);
					totalProcessos += numeroProcessos;
					EstatisticaDistribuidosRankingRegionalListBean erlb = new EstatisticaDistribuidosRankingRegionalListBean();
					erlb.setVaras(getVaraFormatada(o[0].toString(), o[1].toString()));
					erlb.setQntProcessos(String.valueOf(numeroProcessos));
					if (!erlb.getQntProcessos().equals(qntProcessosTemp)) {
						ranking++;
					}
					qntProcessosTemp = String.valueOf(numeroProcessos);
					erlb.setRankingRegiao(ranking + "º");
					ersb.getEstatisticaRankingRegionalListBean().add(erlb);
				}
			}
			for (EstatisticaDistribuidosRankingRegionalBean bean : estatisticaBeanList) {
				List<EstatisticaDistribuidosRankingRegionalListBean> lista = new ArrayList<EstatisticaDistribuidosRankingRegionalListBean>();
				lista.addAll(bean.getEstatisticaRankingRegionalListBean());
				bean.getEstatisticaRankingRegionalListBean().clear();
				bean.getEstatisticaRankingRegionalListBean().addAll(lista);
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
	 * Formata a vara para a exibição no relatório do ranking da região
	 * 
	 * @param orgaoJulgador
	 *            descrição do orgão julgador
	 * @param jurisdicao
	 *            descrição da jurisdição
	 */
	private String getVaraFormatada(String orgaoJulgador, String jurisdicao) {
		StringBuilder sb = new StringBuilder();
		sb.append(StringUtil.limparCharsNaoNumericos(orgaoJulgador)).append("ª - ").append(jurisdicao).append(" ")
				.append(getCompetenciaList(orgaoJulgador));
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
			relatorioLog.setDescricao("Estatística de Processos Distribuídos - Ranking Regional");
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
		} catch (ExcelExportException e) {
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
		map.put("estatisticaBeanList", estatisticaBeanList);
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
		map.put("titulo", "ESTATÍSTICA DE PROCESSOS DISTRIBUÍDOS - RANKING REGIONAL");
		map.put("subNomeSistema", ParametroUtil.getParametro("nomeSecaoJudiciaria").toUpperCase());
		map.put("nomeSistema", ParametroUtil.getParametro("nomeSistema"));

		return map;
	}

	public List<EstatisticaDistribuidosRankingRegionalListBean> ordenarVaras(
			List<EstatisticaDistribuidosRankingRegionalListBean> lista) {
		List<Integer> lista1 = new ArrayList<Integer>();

		// pega todos os números das varas e coloca em uma lista para ordenação
		// posterior
		for (EstatisticaDistribuidosRankingRegionalListBean o : lista) {
			String vara = o.getVaras();
			if (vara.indexOf("ª") > 0) {
				CharSequence subSequence = vara.subSequence(0, vara.indexOf("ª"));
				lista1.add(Integer.valueOf(subSequence.toString().trim()));
			}
		}

		// ordena as varas da lista de forma crescente
		Collections.sort(lista1);

		// cria a lista de retorno ao usuário ordenado de acordo com o número da
		// vara
		List<EstatisticaDistribuidosRankingRegionalListBean> lista3 = new ArrayList<EstatisticaDistribuidosRankingRegionalListBean>();
		for (Integer u : lista1) {
			for (EstatisticaDistribuidosRankingRegionalListBean o : lista) {
				String varas = o.getVaras();
				CharSequence subSequence = varas.subSequence(0, varas.indexOf("ª"));
				if (varas.indexOf("ª") > 0) {
					if (Integer.valueOf(subSequence.toString().trim()).equals(u) && !lista3.contains(o)) {
						lista3.add(o);
					}
				}
			}
		}

		// adiciona as varas que não tem número
		for (EstatisticaDistribuidosRankingRegionalListBean o : lista) {
			if (!lista3.contains(o)) {
				lista3.add(o);
			}
		}

		return lista3;
	}

	/*
	 * Inicio - Getters and Setters
	 */

	public void setRankingList(EstatisticaProcessosDistribuidosRankingRegionalList rankingList) {
		this.rankingList = rankingList;
	}

	public String getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(String dataInicio) {
		this.dataInicio = dataInicio;
	}

	public String getDataFim() {
		return dataFim;
	}

	public void setDataFim(String dataFim) {
		this.dataFim = dataFim;
	}

	public EstatisticaProcessosDistribuidosRankingRegionalList getRankingList() {
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

	public List<EstatisticaDistribuidosRankingRegionalBean> getEstatisticaBeanList() {
		return estatisticaBeanList;
	}

	public void setEstatisticaBeanList(List<EstatisticaDistribuidosRankingRegionalBean> estatisticaBeanList) {
		this.estatisticaBeanList = estatisticaBeanList;
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

	public static EstatisticaProcessosDistribuidosRankingRegionalAction instance() {
		return ComponentUtil.getComponent(NAME);
	}

	public void limpaSearch() {
		dataInicio = null;
		dataFim = null;
		erroAtualizarSecao = "";
		rodape = null;
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