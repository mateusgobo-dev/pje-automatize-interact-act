package br.com.infox.pje.action;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jxls.exception.ParsePropertyException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
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
import br.com.infox.pje.bean.ArquivadosRankingSessaoBean;
import br.com.infox.pje.bean.VaraProcessosArquivadosRankingSessaoBean;
import br.com.infox.pje.list.EstatisticaProcessosArquivadosRankingSessaoList;
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
 * Classe action controladora do listView de
 * /EstatisticaProcesso/ArquivadosRankingSessao/
 * 
 * @author Edson
 * 
 */
@Name(value = ArquivadosRankingSessaoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class ArquivadosRankingSessaoAction implements Serializable {

	public static final String NAME = "arquivadosRankingSessaoAction";

	private static final long serialVersionUID = 1L;
	private EstatisticaProcessosArquivadosRankingSessaoList arquivadosSessaoRankingList = new EstatisticaProcessosArquivadosRankingSessaoList();
	private static final String TEMPLATE_XLS_PATH = "/EstatisticaProcesso/ArquivadosRankingSecao/arquivadosRankingSecao.xls";
	private static final String DOWNLOAD_XLS_NAME = "Arquivados.xls";

	private String dataInicio;
	private String dataFim;
	private String dataInicioFormatada;
	private String dataFimFormatada;
	private long totalProcessos;
	private int totalVaras;
	private List<ArquivadosRankingSessaoBean> estatisticaBeanList;
	private SecaoJudiciaria secaoJudiciaria;
	private List<SecaoJudiciaria> secaoJudiciariaList;
	private String msgErroAtualizacaoSecao = "";
	@In
	private EstatisticaEventoProcessoManager estatisticaEventoProcessoManager;
	@In
	private SecaoJudiciariaManager secaoJudiciariaManager;
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
	 * EstatisticaRankingSecaoBean.java
	 * 
	 * @return lista com os dados para o ranking
	 */
	public List<ArquivadosRankingSessaoBean> arquivadosRankingSessaoList() {
		if (getEstatisticaBeanList() == null) {
			setDataInicioFormatada(formatarAnoMes(dataInicio));
			setDataFimFormatada(formatarAnoMes(dataFim));
			setEstatisticaBeanList(new ArrayList<ArquivadosRankingSessaoBean>());
			List<Object[]> resultList = getArquivadosSessaoRankingList().getResultList();
			totalProcessos = 0;
			totalVaras = 0;
			msgErroAtualizacaoSecao = "";
			if (resultList != null && resultList.size() > 0) {
				ArquivadosRankingSessaoBean arsb = new ArquivadosRankingSessaoBean();
				getEstatisticaBeanList().add(arsb);
				int rankingEstado = 0;
				for (Object[] o : resultList) {
					totalVaras++;
					rankingEstado++;
					if (arsb.getCodEstado() == null) {
						arsb.setCodEstado(o[0].toString());
						if (!ParametroUtil.instance().isPrimeiroGrau()) {
							if (historicoEstatisticaEventoProcessoManager.getDataAtualizacaoSessao(arsb.getCodEstado()) != null) {
								msgErroAtualizacaoSecao = "SJ"
										+ arsb.getCodEstado()
										+ " (atualizado até o dia "
										+ historicoEstatisticaEventoProcessoManager.getDataAtualizacaoSessao(arsb
												.getCodEstado()) + ")";
							}
						}
					} else if (!arsb.getCodEstado().equals(o[0].toString())) {
						arsb = new ArquivadosRankingSessaoBean();
						getEstatisticaBeanList().add(arsb);
						arsb.setCodEstado(o[0].toString());
						rankingEstado = 1;
						if (!ParametroUtil.instance().isPrimeiroGrau()) {
							if (historicoEstatisticaEventoProcessoManager.getDataAtualizacaoSessao(arsb.getCodEstado()) != null) {
								if (msgErroAtualizacaoSecao.length() > 0) {
									msgErroAtualizacaoSecao += ", SJ"
											+ arsb.getCodEstado()
											+ " (atualizado até o dia "
											+ historicoEstatisticaEventoProcessoManager.getDataAtualizacaoSessao(arsb
													.getCodEstado()) + ")";
								}
							}
						}
					}
					Integer varasEstado = arsb.getTotalVarasEstado();
					arsb.setTotalVarasEstado(varasEstado == null ? 1 : varasEstado + 1);
					long numeroProcessos = Long.parseLong(o[3].toString());
					Long processosEstado = arsb.getTotalProcessosEstado();
					arsb.setTotalProcessosEstado(processosEstado == null ? numeroProcessos : processosEstado
							+ numeroProcessos);
					totalProcessos += numeroProcessos;
					VaraProcessosArquivadosRankingSessaoBean vparsb = new VaraProcessosArquivadosRankingSessaoBean();
					vparsb.setVaras(getVaraFormatada(o[1].toString(), o[2].toString()));
					vparsb.setQntProcessos(String.valueOf(numeroProcessos));
					vparsb.setRankingSecao((rankingEstado + "º"));
					arsb.getEstatisticaRankingListBean().add(vparsb);
				}
				if (!ParametroUtil.instance().isPrimeiroGrau() && !Strings.isEmpty(msgErroAtualizacaoSecao)) {
					StringBuilder sb = new StringBuilder();
					if (msgErroAtualizacaoSecao.lastIndexOf(',') != -1) {
						int pos = msgErroAtualizacaoSecao.lastIndexOf(',');
						sb.append(msgErroAtualizacaoSecao);
						sb.replace(pos, pos + 1, " e");
						sb.insert(msgErroAtualizacaoSecao.length() + 1, '.');
						msgErroAtualizacaoSecao = sb.toString();
					} else {
						sb.append(msgErroAtualizacaoSecao);
						sb.insert(msgErroAtualizacaoSecao.length(), '.');
						msgErroAtualizacaoSecao = sb.toString();
					}
				}
			}
		}
		return getEstatisticaBeanList();
	}
	
	

	/**
	 * Formata a vara para a exibição no relatório do ranking da seção
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
		setEstatisticaBeanList(null);
		if (arquivadosRankingSessaoList().size() > 0) {
			RelatorioLog relatorioLog = new RelatorioLog();
			relatorioLog.setDataSolicitacao(new Date());
			relatorioLog.setDescricao("Estatística de Processos Arquivados - Ranking/Seção");
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
	public void exportarProcessoRankingSecaoXLS() {
		try {
			if (getEstatisticaBeanList().size() > 0) {
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
	 * @throws ParsePropertyException
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	public void exportarXLS(String dirNomeTemplate, String nomeArqDown) throws ExcelExportException {
		String urlTemplate = new Util().getContextRealPath() + dirNomeTemplate;
		ExcelExportUtil.downloadXLS(urlTemplate, beanExportarXLS(), nomeArqDown);
	}

	private Map<String, Object> beanExportarXLS() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("estatisticaRankingListBean", getEstatisticaBeanList());
		map.put("totalProcessos", totalProcessos);
		map.put("totalVaras", totalVaras);
		map.put("secaoJudiciaria", secaoJudiciaria == null ? "Todas" : secaoJudiciaria);
		if (dataInicio != null && dataFim != null) {
			map.put("dataInicio", dataInicio);
			map.put("dataFim", dataFim);
		}
		if (!msgErroAtualizacaoSecao.isEmpty()) {
			map.put("msgErroAtualizacaoSecao", "Dados não computados na(s) Seção(ões): " + msgErroAtualizacaoSecao);
		}

		map.put("titulo", "ESTATÍSTICA DE PROCESSOS ARQUIVADOS - RANKING/SEÇÃO");
		map.put("subNomeSistema", ParametroUtil.getParametro("nomeSecaoJudiciaria").toUpperCase());
		map.put("nomeSistema", ParametroUtil.getParametro("nomeSistema"));

		return map;
	}

	/*
	 * Inicio - Getters and Setters
	 */
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

	public EstatisticaProcessosArquivadosRankingSessaoList getArquivadosSessaoRankingList() {
		return arquivadosSessaoRankingList;
	}

	public void setArquivadosSessaoRankingList(
			EstatisticaProcessosArquivadosRankingSessaoList arquivadosSessaoRankingList) {
		this.arquivadosSessaoRankingList = arquivadosSessaoRankingList;
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

	public void setTotalVaras(int totalVaras) {
		this.totalVaras = totalVaras;
	}

	public int getTotalVaras() {
		return totalVaras;
	}

	public void setEstatisticaBeanList(List<ArquivadosRankingSessaoBean> estatisticaBeanList) {
		this.estatisticaBeanList = estatisticaBeanList;
	}

	public List<ArquivadosRankingSessaoBean> getEstatisticaBeanList() {
		return estatisticaBeanList;
	}

	/*
	 * Fim - Getters and Setters
	 */

	private List<SecaoJudiciaria> secaoJudiciariaItems() {
		return secaoJudiciariaManager.secaoJudiciariaItems();
	}

	public static ArquivadosRankingSessaoAction instance() {
		return ComponentUtil.getComponent(NAME);
	}

	public void secaoJudiciaria1Grau() {
		secaoJudiciaria = secaoJudiciariaManager.secaoJudiciaria1Grau();
	}

	public void limparFiltros() {
		if (!ParametroUtil.instance().isPrimeiroGrau()) {
			secaoJudiciaria = null;
			msgErroAtualizacaoSecao = "";
		}
		dataFim = null;
		dataInicio = null;
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