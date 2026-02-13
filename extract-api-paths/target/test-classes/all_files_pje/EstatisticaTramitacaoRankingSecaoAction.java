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
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.manager.GenericManager;
import br.com.infox.pje.bean.EstatisticaRankingListBean;
import br.com.infox.pje.bean.EstatisticaRankingSecaoBean;
import br.com.infox.pje.list.EstatisticaProcessosTramitacaoRankingList;
import br.com.infox.pje.manager.EstatisticaEventoProcessoManager;
import br.com.infox.pje.manager.HistoricoEstatisticaEventoProcessoManager;
import br.com.infox.pje.manager.SecaoJudiciariaManager;
import br.com.itx.component.Util;
import br.com.itx.exception.ExcelExportException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.ExcelExportUtil;
import br.jus.pje.nucleo.entidades.RelatorioLog;
import br.jus.pje.nucleo.entidades.SecaoJudiciaria;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * Classe action controladora do listView de /EstatisticaProcesso/
 * TramitacaoRankingSecao/
 * 
 * @author Laércio
 * 
 */
@Name(value = EstatisticaTramitacaoRankingSecaoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class EstatisticaTramitacaoRankingSecaoAction implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3703659919604790436L;

	public static final String NAME = "estatisticaTramitacaoRankingSecaoAction";

	private EstatisticaProcessosTramitacaoRankingList tramitacaoRankingList = new EstatisticaProcessosTramitacaoRankingList();
	private static final String TEMPLATE_XLS_PATH = "/EstatisticaProcesso/TramitacaoRankingSecao/tramitacaoRankingSecaoTemplate.xls";
	private static final String DOWNLOAD_XLS_NAME = "TramitacaoRankingSeção.xls";
	@In
	private EstatisticaEventoProcessoManager estatisticaEventoProcessoManager;
	@In
	private GenericManager genericManager;
	@In
	private SecaoJudiciariaManager secaoJudiciariaManager;
	private SecaoJudiciaria secaoJudiciaria;
	private List<SecaoJudiciaria> secaoJudiciariaList;
	private String dataInicio;
	private String dataFim;
	private String dataInicioFormatada;
	private String dataFimFormatada;
	private long totalProcessos;
	private int totalVaras;
	private String msgErroAtualizacaoSecao = "";
	private List<EstatisticaRankingSecaoBean> estatisticaBeanList;
	@In
	private HistoricoEstatisticaEventoProcessoManager historicoEstatisticaEventoProcessoManager;

	public static EstatisticaTramitacaoRankingSecaoAction instance() {
		return ComponentUtil.getComponent(NAME);
	}

	/**
	 * Obtem o relatório da estatística convertendo a lista proveniente do banco
	 * para uma lista no esquema da exibição necessária para a tela utilizando o
	 * EstatisticaRankingSecaoBean.java
	 * 
	 * @return lista com os dados para o ranking
	 */
	public List<EstatisticaRankingSecaoBean> estatisticaRankingList() {
		if (getEstatisticaBeanList() == null) {
			dataInicioFormatada = formatarAnoMes(dataInicio);
			dataFimFormatada = formatarAnoMes(dataFim);
			totalProcessos = 0;
			totalVaras = 0;
			msgErroAtualizacaoSecao = "";
			setEstatisticaBeanList(new ArrayList<EstatisticaRankingSecaoBean>());
			List<Object[]> resultList = getTramitacaoRankingList().getResultList();
			if (resultList != null && resultList.size() > 0) {
				EstatisticaRankingSecaoBean ersb = new EstatisticaRankingSecaoBean();
				getEstatisticaBeanList().add(ersb);
				int rankingEstado = 0;
				for (Object[] o : resultList) {
					totalVaras++;
					rankingEstado++;
					if (ersb.getCodEstado() == null) {
						ersb.setCodEstado(o[0].toString());
						if (!ParametroUtil.instance().isPrimeiroGrau()) {
							if (historicoEstatisticaEventoProcessoManager.getDataAtualizacaoSessao(ersb.getCodEstado()) != null) {
								msgErroAtualizacaoSecao = "SJ"
										+ ersb.getCodEstado()
										+ " (atualizado até o dia "
										+ historicoEstatisticaEventoProcessoManager.getDataAtualizacaoSessao(ersb
												.getCodEstado()) + ")";
							}
						}
					} else if (!ersb.getCodEstado().equals(o[0].toString())) {
						ersb = new EstatisticaRankingSecaoBean();
						getEstatisticaBeanList().add(ersb);
						ersb.setCodEstado(o[0].toString());
						rankingEstado = 1;
						if (!ParametroUtil.instance().isPrimeiroGrau()) {
							if (historicoEstatisticaEventoProcessoManager.getDataAtualizacaoSessao(ersb.getCodEstado()) != null) {
								if (msgErroAtualizacaoSecao.length() > 0) {
									msgErroAtualizacaoSecao += ", SJ"
											+ ersb.getCodEstado()
											+ " (atualizado até o dia "
											+ historicoEstatisticaEventoProcessoManager.getDataAtualizacaoSessao(ersb
													.getCodEstado()) + ")";
								}
							}
						}
					}
					Integer varasEstado = ersb.getTotalVarasEstado();
					ersb.setTotalVarasEstado(varasEstado == null ? 1 : varasEstado + 1);
					long numeroProcessos = Long.parseLong(o[3].toString());
					Long processosEstado = ersb.getTotalProcessosEstado();
					ersb.setTotalProcessosEstado(processosEstado == null ? numeroProcessos : processosEstado
							+ numeroProcessos);
					totalProcessos += numeroProcessos;
					EstatisticaRankingListBean erlb = new EstatisticaRankingListBean();
					erlb.setVaras(getVara(o));
					erlb.setQntProcessos(String.valueOf(numeroProcessos));
					erlb.setRankingSecao(rankingEstado + "º");
					ersb.getEstatisticaRankingListBean().add(erlb);
				}
				if (!ParametroUtil.instance().isPrimeiroGrau()) {
					if (msgErroAtualizacaoSecao.lastIndexOf(',') != -1) {
						int pos = msgErroAtualizacaoSecao.lastIndexOf(',');
						StringBuilder sb = new StringBuilder();
						sb.append(msgErroAtualizacaoSecao);
						sb.replace(pos, pos + 1, " e");
						if (!Strings.isEmpty(msgErroAtualizacaoSecao)) {
							sb.insert(msgErroAtualizacaoSecao.length() + 1, '.');
						}
						msgErroAtualizacaoSecao = sb.toString();
					} else {
						StringBuilder sb = new StringBuilder();
						sb.append(msgErroAtualizacaoSecao);
						if (!Strings.isEmpty(msgErroAtualizacaoSecao)) {
							sb.insert(msgErroAtualizacaoSecao.length(), '.');
						}
						msgErroAtualizacaoSecao = sb.toString();
					}
				}
			}
		}
		return getEstatisticaBeanList();
	}

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
	 * Formata a vara para a exibição no relatório do ranking da seção
	 * 
	 * @param o
	 *            , sendo [1] = descrção do orgão julgador e [2] = descricao da
	 *            jurisdição
	 * @return
	 */
	private String getVara(Object[] o) {
		StringBuilder sb = new StringBuilder();
		sb.append(StringUtil.limparCharsNaoNumericos(o[1].toString())).append("ª - ").append(o[2]).append(" ")
				.append(getCompetenciaList(o[1].toString()));
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
		setEstatisticaBeanList(null);
		if (estatisticaRankingList().size() > 0) {
			RelatorioLog relatorioLog = new RelatorioLog();
			relatorioLog.setDataSolicitacao(new Date());
			relatorioLog.setDescricao("Estatística de Processos em Tramitação Ranking/Seção");
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
	public void exportarTramitacaoRankingSecaoXLS() {
		try {
			if (getEstatisticaBeanList().size() > 0) {
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
		map.put("estatisticaBeanList", getEstatisticaBeanList());
		if (dataInicio != null && dataFim != null) {
			map.put("dataInicio", dataInicio);
			map.put("dataFim", dataFim);
		}
		map.put("secao", secaoJudiciaria == null ? "Todas" : secaoJudiciaria);
		map.put("totalProcessos", totalProcessos);
		map.put("totalVaras", totalVaras);
		if (!msgErroAtualizacaoSecao.isEmpty()) {
			map.put("msgErroAtualizacaoSecao", "Dados não computados na(s) Seção(ões): " + msgErroAtualizacaoSecao);
		}
		map.put("titulo", "ESTATÍSTICA DE PROCESSOS EM TRAMITAÇÃO - RANKING/SEÇÃO");
		map.put("subNomeSistema", ParametroUtil.getParametro("nomeSecaoJudiciaria").toUpperCase());
		map.put("nomeSistema", ParametroUtil.getParametro("nomeSistema"));
		return map;
	}

	public void limpaDataPeriodo() {
		if (!ParametroUtil.instance().isPrimeiroGrau()) {
			msgErroAtualizacaoSecao = "";
			secaoJudiciaria = null;
		}
		dataInicio = null;
		dataFim = null;
	}

	/*
	 * Inicio - Getters and Setters
	 */
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

	public void setTramitacaoRankingList(EstatisticaProcessosTramitacaoRankingList tramitacaoRankingList) {
		this.tramitacaoRankingList = tramitacaoRankingList;
	}

	public EstatisticaProcessosTramitacaoRankingList getTramitacaoRankingList() {
		return tramitacaoRankingList;
	}

	public void setTotalVaras(int totalVaras) {
		this.totalVaras = totalVaras;
	}

	public int getTotalVaras() {
		return totalVaras;
	}

	public String getMsgErroAtualizacaoSecao() {
		return msgErroAtualizacaoSecao;
	}

	public void setMsgErroAtualizacaoSecao(String msgErroAtualizacaoSecao) {
		this.msgErroAtualizacaoSecao = msgErroAtualizacaoSecao;
	}

	public void setTotalProcessos(long totalProcessos) {
		this.totalProcessos = totalProcessos;
	}

	public long getTotalProcessos() {
		return totalProcessos;
	}

	public void setSecaoJudiciaria(SecaoJudiciaria secaoJudiciaria) {
		this.secaoJudiciaria = secaoJudiciaria;
	}

	public SecaoJudiciaria getSecaoJudiciaria() {
		return secaoJudiciaria;
	}

	public void setSecaoJudiciariaList(List<SecaoJudiciaria> secaoJudiciariaList) {
		this.secaoJudiciariaList = secaoJudiciariaList;
	}

	public List<SecaoJudiciaria> getSecaoJudiciariaList() {
		if (secaoJudiciariaList == null) {
			secaoJudiciariaList = secaoJudiciariaItems();
		}
		return secaoJudiciariaList;
	}

	/*
	 * Fim - Getters and Setters
	 */

	public void secaoJudiciaria1Grau() {
		secaoJudiciaria = secaoJudiciariaManager.secaoJudiciaria1Grau();
	}

	private List<SecaoJudiciaria> secaoJudiciariaItems() {
		return secaoJudiciariaManager.secaoJudiciariaItems();
	}

	public void setEstatisticaBeanList(List<EstatisticaRankingSecaoBean> estatisticaBeanList) {
		this.estatisticaBeanList = estatisticaBeanList;
	}

	public List<EstatisticaRankingSecaoBean> getEstatisticaBeanList() {
		return estatisticaBeanList;
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