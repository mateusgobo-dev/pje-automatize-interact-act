package br.com.infox.pje.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
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

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.manager.GenericManager;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.bean.EstatisticaRankingTipoVaraBean;
import br.com.infox.pje.bean.EstatisticaRankingTipoVaraListBean;
import br.com.infox.pje.list.EstatisticaProcessosJulgadosRankingList;
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
 * DistribuidosRankingTipoVara/
 * 
 * @author Fabio
 * 
 */
@Name(value = EstatisticaRankingTipoVaraAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class EstatisticaRankingTipoVaraAction implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7511847275914189712L;

	public static final String NAME = "estatisticaRankingTipoVaraAction";

	private EstatisticaProcessosJulgadosRankingList rankingList = new EstatisticaProcessosJulgadosRankingList();
	private static final String TEMPLATE_XLS_PATH = "/EstatisticaProcesso/JulgadosRankingTipoVara/procJulgadosRankingTemplate.xls";

	private String dataInicio;
	private String dataFim;
	private String dataInicioFormatada;
	private String dataFimFormatada;
	private long totalProcessos;
	private int totalVaras;
	private boolean incluiEmbargosDeclaracao = false;
	private List<EstatisticaRankingTipoVaraBean> estatisticaBeanList;
	private String competencia;
	private String erroAtualizarSecao = "";
	private SecaoJudiciaria secaoJudiciaria;
	private List<SecaoJudiciaria> secaoJudiciariaList;
	@In
	private GenericManager genericManager;
	@In
	private SecaoJudiciariaManager secaoJudiciariaManager;
	@In
	private HistoricoEstatisticaEventoProcessoManager historicoEstatisticaEventoProcessoManager;

	/**
	 * Obtem o relatório da estatística convertendo a lista proveniente do banco
	 * para uma lista no esquema da exibição necessária para a tela utilizando o
	 * EstatisticaRankingSecaoBean.java
	 * 
	 * @return lista com os dados para o ranking
	 */
	public List<EstatisticaRankingTipoVaraBean> estatisticaRankingList() {
		String erro = "";
		StringBuilder erroTemp = new StringBuilder();
		int pos = 0;
		if (getEstatisticaBeanList() == null) {
			dataInicioFormatada = formatarAnoMes(dataInicio);
			dataFimFormatada = formatarAnoMes(dataFim);
			setEstatisticaBeanList(new ArrayList<EstatisticaRankingTipoVaraBean>());
			List<Object[]> resultList = getRankingList().getResultList();
			totalProcessos = 0;
			totalVaras = 0;
			erroAtualizarSecao = "";
			if (resultList != null && resultList.size() > 0) {
				EstatisticaRankingTipoVaraBean ertvb = new EstatisticaRankingTipoVaraBean();
				getEstatisticaBeanList().add(ertvb);
				int rankingEstado = 0;
				for (Object[] o : resultList) {
					totalVaras++;
					rankingEstado++;
					if (o[0] != null) {
						if (ertvb.getCompetencia() == null) {
							ertvb.setCompetencia(o[0].toString());
						} else if (!ertvb.getCompetencia().equals(o[0].toString())) {
							ertvb = new EstatisticaRankingTipoVaraBean();
							getEstatisticaBeanList().add(ertvb);
							ertvb.setCompetencia(o[0].toString());
							rankingEstado = 1;
						}
					}
					Integer varasEstado = ertvb.getTotalVarasEstado();
					ertvb.setTotalVarasEstado(varasEstado == null ? 1 : varasEstado + 1);
					long numeroProcessos = Long.parseLong(o[3].toString());
					Long processosEstado = ertvb.getTotalProcessosEstado();
					ertvb.setTotalProcessosEstado(processosEstado == null ? numeroProcessos : processosEstado
							+ numeroProcessos);
					totalProcessos += numeroProcessos;
					EstatisticaRankingTipoVaraListBean erlb = new EstatisticaRankingTipoVaraListBean();
					erlb.setVaras(getVara(o));
					erlb.setQntProcessos(String.valueOf(numeroProcessos));
					erlb.setRankingTipoVara(rankingEstado + "º");
					ertvb.getEstatisticaRankingTipoVaraListBean().add(erlb);
				}
				if (!ParametroUtil.instance().isPrimeiroGrau()) {
					List<Object[]> resultListAtualizacao = historicoEstatisticaEventoProcessoManager
							.listSecaoNaoAtualizada();
					if (resultListAtualizacao.size() > 0) {
						for (Object[] objects : resultListAtualizacao) {
							if (getSecaoJudiciaria() != null
									&& getSecaoJudiciaria().getCdSecaoJudiciaria().equals(objects[0])) {
								erro = ("SJ" + objects[0] + "(atualização até o dia " + objects[1] + "). ");
								erroTemp.append(erro);
								break;
							} else if (getSecaoJudiciaria() == null) {
								erro = ("SJ" + objects[0] + "(atualização até o dia " + objects[1] + "), ");
								erroTemp.append(erro);
							}
						}
					}
					pos = erroTemp.lastIndexOf(",");
					if (pos > 0) {
						erroTemp = new StringBuilder(erroTemp.substring(0, erroTemp.lastIndexOf(",")));
						pos = erroTemp.lastIndexOf(",");
						StringBuilder sb = new StringBuilder();
						sb.append(erroTemp);
						sb.replace(pos, pos + 2, " e ");
						sb.insert(erroTemp.length() + 1, '.');
						erroTemp = sb;
					}
					setErroAtualizarSecao(erroTemp.toString());
					erroTemp = new StringBuilder();
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
		sb.append(StringUtil.limparCharsNaoNumericos(o[1].toString())).append("ª - ").append(o[2]);
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
			relatorioLog.setDescricao("Estatística de Processos Julgados - Ranking/Tipo de Vara");
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
	public void exportarProcessoRankingTipoVaraXLS() {
		try {
			if (getEstatisticaBeanList().size() > 0) {
				exportarXLS(TEMPLATE_XLS_PATH, "Ranking/Tipo de Vara.xls", "estatisticaBeanList",
						getEstatisticaBeanList());
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
		if (!erroAtualizarSecao.isEmpty()) {
			map.put("erroAtualizarSecao", "Dados não computados na(s) Seção(ões): " + erroAtualizarSecao);
		}
		map.put("secaoJudiciaria", secaoJudiciaria == null ? "Todas" : secaoJudiciaria);
		map.put("titulo", "ESTATÍSTICA DE PROCESSOS JULGADOS - RANKING/TIPO DE VARA");
		map.put("subNomeSistema", ParametroUtil.getParametro("nomeSecaoJudiciaria").toUpperCase());
		map.put("nomeSistema", ParametroUtil.getParametro("nomeSistema"));
		map.put("embargosDeclaracao", incluiEmbargosDeclaracao ? "Sim" : "Não");

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

	public void setRankingList(EstatisticaProcessosJulgadosRankingList rankingList) {
		this.rankingList = rankingList;
	}

	public EstatisticaProcessosJulgadosRankingList getRankingList() {
		return rankingList;
	}

	public void setTotalVaras(int totalVaras) {
		this.totalVaras = totalVaras;
	}

	public int getTotalVaras() {
		return totalVaras;
	}

	public boolean isIncluiEmbargosDeclaracao() {
		return incluiEmbargosDeclaracao;
	}

	public void setIncluiEmbargosDeclaracao(boolean incluiEmbargosDeclaracao) {
		this.incluiEmbargosDeclaracao = incluiEmbargosDeclaracao;
	}

	public void setTotalProcessos(long totalProcessos) {
		this.totalProcessos = totalProcessos;
	}

	public long getTotalProcessos() {
		return totalProcessos;
	}

	public void setCompetencia(String competencia) {
		this.competencia = competencia;
	}

	public String getCompetencia() {
		return competencia;
	}

	public void setErroAtualizarSecao(String erroAtualizarSecao) {
		this.erroAtualizarSecao = erroAtualizarSecao;
	}

	public String getErroAtualizarSecao() {
		return erroAtualizarSecao;
	}

	public SecaoJudiciaria getSecaoJudiciaria() {
		return secaoJudiciaria;
	}

	public void setSecaoJudiciaria(SecaoJudiciaria secaoJudiciaria) {
		this.secaoJudiciaria = secaoJudiciaria;
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

	private List<SecaoJudiciaria> secaoJudiciariaItems() {
		return secaoJudiciariaManager.secaoJudiciariaItems();
	}

	public String secaoJudiciariaPrimeiroGrau() {
		if (ParametroUtil.instance().isPrimeiroGrau()) {
			return ParametroUtil.instance().getSecao();
		}
		return null;
	}

	public void secaoJudiciaria1Grau() {
		secaoJudiciaria = secaoJudiciariaManager.secaoJudiciaria1Grau();
	}

	public static EstatisticaRankingTipoVaraAction instance() {
		return ComponentUtil.getComponent(NAME);
	}

	public void setEstatisticaBeanList(List<EstatisticaRankingTipoVaraBean> estatisticaBeanList) {
		this.estatisticaBeanList = estatisticaBeanList;
	}

	public List<EstatisticaRankingTipoVaraBean> getEstatisticaBeanList() {
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