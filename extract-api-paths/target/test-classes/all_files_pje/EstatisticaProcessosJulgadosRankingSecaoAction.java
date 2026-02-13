package br.com.infox.pje.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jxls.exception.ParsePropertyException;
import net.sf.jxls.transformer.XLSTransformer;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.util.RandomStringUtils;
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.manager.GenericManager;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.bean.EstatisticaRankingListBean;
import br.com.infox.pje.bean.EstatisticaRankingSecaoBean;
import br.com.infox.pje.list.EstatisticaProcessosJulgadosRankingSecaoList;
import br.com.infox.pje.manager.EstatisticaEventoProcessoManager;
import br.com.infox.pje.manager.HistoricoEstatisticaEventoProcessoManager;
import br.com.infox.pje.manager.SecaoJudiciariaManager;
import br.com.itx.component.FileHome;
import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.RelatorioLog;
import br.jus.pje.nucleo.entidades.SecaoJudiciaria;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * Classe action controladora do listView de /EstatisticaProcesso/
 * JulgadosRankingSessao/
 * 
 * @author Geldo
 * 
 */
@Name(value = EstatisticaProcessosJulgadosRankingSecaoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class EstatisticaProcessosJulgadosRankingSecaoAction implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -670936697081739854L;

	public static final String NAME = "estatisticaProcessosJulgadosRankingSecaoAction";

	private EstatisticaProcessosJulgadosRankingSecaoList rankingList = new EstatisticaProcessosJulgadosRankingSecaoList();
	private static final String TEMPLATE_XLS_PATH = "/EstatisticaProcesso/JulgadosRankingSecao/procJulgadosRankingSecaoTemplate.xls";

	private String dataInicio;
	private String dataFim;
	private String dataInicioFormatada;
	private String dataFimFormatada;
	private long totalProcessos;
	private int totalVaras;
	private int totalGeralProcessos;
	private int totalGeralVaras;
	private List<EstatisticaRankingSecaoBean> estatisticaBeanList;
	private List<SecaoJudiciaria> secaoJudiciariaList;
	private SecaoJudiciaria secaoJudiciaria;
	private boolean incluiEmbargosDeclaracao;
	private String codEstado;
	private String msgErroAtualizacaoSecao = "";
	@In
	private EstatisticaEventoProcessoManager estatisticaEventoProcessoManager;
	@In
	private GenericManager genericManager;
	@In
	private SecaoJudiciariaManager secaoJudiciariaManager;
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
	 * EstatisticaRankingSecaoBean.java
	 * 
	 * @return lista com os dados para o ranking
	 */
	public List<EstatisticaRankingSecaoBean> estatisticaRankingList() {
		if (getEstatisticaBeanList() == null) {
			dataInicioFormatada = formatarAnoMes(dataInicio);
			dataFimFormatada = formatarAnoMes(dataFim);
			setEstatisticaBeanList(new ArrayList<EstatisticaRankingSecaoBean>());
			msgErroAtualizacaoSecao = "";
			List<Object[]> resultList = getRankingList().getResultList();
			totalProcessos = 0;
			totalVaras = 0;
			totalGeralProcessos = 0;
			totalGeralVaras = 0;
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
					totalGeralProcessos += Integer.parseInt(o[3].toString());
					totalGeralVaras += totalVaras;
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
				Collections.sort(ersb.getEstatisticaRankingListBean(), getVaraComparator());
			}
		}
		return getEstatisticaBeanList();
	}

	@SuppressWarnings("unchecked")
	private Comparator<? super EstatisticaRankingListBean> getVaraComparator() {
		return new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				EstatisticaRankingListBean bean1 = (EstatisticaRankingListBean) o1;
				EstatisticaRankingListBean bean2 = (EstatisticaRankingListBean) o2;
				Integer splitedVara1 = Integer.parseInt(bean1.getRankingSecao().split("º")[0]);
				Integer splitedVara2 = Integer.parseInt(bean2.getRankingSecao().split("º")[0]);
				return splitedVara1.compareTo(splitedVara2);
			}
		};
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
			relatorioLog.setDescricao("Estatística de Processos Julgados - Ranking/Seção");
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
				exportarXLS(TEMPLATE_XLS_PATH, "Julgados.xls", "estatisticaBeanList", getEstatisticaBeanList());
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
	@SuppressWarnings("unchecked")
	public void exportarXLS(String dirNomeTemplate, String nomeArqDown, String nomeListaTemplate, List lista)
			throws ParsePropertyException, InvalidFormatException, IOException {

		String dirProj = new Util().getContextRealPath();// Caminho da pasta do
															// projeto
		String rand = RandomStringUtils.randomAlphanumeric(6);// String radomica
																// para o nome
																// do arquivo
		String dirTemp = dirProj + "/WEB-INF/temp/";// Caminho completo para
													// pasta temporária
		int idUsuario = Authenticator.getUsuarioLogado().getIdUsuario();
		// Caminho com nome para o arquivo temporario
		String nomeDirArquivoTemp = MessageFormat.format("{0}{1,date,kkmmss}{2}{3}.xls", dirTemp + File.separatorChar,
				new Date(), rand, idUsuario);
		// Caminho completo do template
		String urlTemplate = dirProj + dirNomeTemplate;

		Map map = new HashMap();
		map.put(nomeListaTemplate, lista);
		map.put("totalProcessos", totalProcessos);
		map.put("totalVaras", totalVaras);
		if (dataInicio != null && dataFim != null) {
			map.put("dataInicio", dataInicio);
			map.put("dataFim", dataFim);
		}
		if (!msgErroAtualizacaoSecao.isEmpty()) {
			map.put("msgErroAtualizacaoSecao", "Dados não computados na(s) Seção(ões): " + msgErroAtualizacaoSecao);
		}

		map.put("titulo", "ESTATÍSTICA DE PROCESSSOS JULGADOS - RANKING/SEÇÃO");
		map.put("subNomeSistema", ParametroUtil.getParametro("nomeSecaoJudiciaria").toUpperCase());
		map.put("nomeSistema", ParametroUtil.getParametro("nomeSistema"));
		map.put("totalGeralProcessos", totalGeralProcessos);
		map.put("incluiEmbargosDeclaracao", incluiEmbargosDeclaracao ? " SIM" : " NÃO");
		map.put("secaoJudiciaria", secaoJudiciaria == null ? "Todas" : secaoJudiciaria);
		XLSTransformer transformer = new XLSTransformer();
		// Exportaçao do arquivo para pasta temporaria
		transformer.transformXLS(urlTemplate, map, nomeDirArquivoTemp);
		// Pegando arquivo importado
		FileInputStream fis = new FileInputStream(nomeDirArquivoTemp);
		byte[] bytes = new byte[fis.available()];
		fis.read(bytes, 0, fis.available());
		fis.close();
		// Apagando arquivo temporario
		new File(nomeDirArquivoTemp).delete();
		// Efetuando o download
		FileHome home = FileHome.instance();
		home.setData(bytes);
		home.setContentType("application/xls");
		home.setFileName(nomeArqDown);
		home.download();
	}

	/*
	 * Inicio - Getters and Setters
	 */
	public void setSecaoJudiciariaList(List<SecaoJudiciaria> secaoJudiciariaList) {
		this.secaoJudiciariaList = secaoJudiciariaList;
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

	public void setRankingList(EstatisticaProcessosJulgadosRankingSecaoList rankingList) {
		this.rankingList = rankingList;
	}

	public EstatisticaProcessosJulgadosRankingSecaoList getRankingList() {
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

	public String getMsgErroAtualizacaoSecao() {
		return msgErroAtualizacaoSecao;
	}

	public void setMsgErroAtualizacaoSecao(String msgErroAtualizacaoSecao) {
		this.msgErroAtualizacaoSecao = msgErroAtualizacaoSecao;
	}

	public void setTotalGeralProcessos(int totalGeralProcessos) {
		this.totalGeralProcessos = totalGeralProcessos;
	}

	public int getTotalGeralProcessos() {
		return totalGeralProcessos;
	}

	public void setTotalGeralVaras(int totalGeralVaras) {
		this.totalGeralVaras = totalGeralVaras;
	}

	public int getTotalGeralVaras() {
		return totalGeralVaras;
	}

	public void setIncluiEmbargosDeclaracao(boolean incluiEmbargosDeclaracao) {
		this.incluiEmbargosDeclaracao = incluiEmbargosDeclaracao;
	}

	public boolean isIncluiEmbargosDeclaracao() {
		return incluiEmbargosDeclaracao;
	}

	/*
	 * Fim - Getters and Setters
	 */
	public static EstatisticaProcessosJulgadosRankingSecaoAction instance() {
		return ComponentUtil.getComponent(NAME);
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