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
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.manager.GenericManager;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.bean.EstatisticaProcessoDistribuidoJulgadoBean;
import br.com.infox.pje.bean.ProcessoDistribuidoJulgadoListBean;
import br.com.infox.pje.list.EstatisticaProcessosDistribuidosJulgadosList;
import br.com.infox.pje.manager.EstatisticaEventoProcessoManager;
import br.com.infox.pje.manager.HistoricoEstatisticaEventoProcessoManager;
import br.com.infox.pje.manager.SecaoJudiciariaManager;
import br.com.itx.component.FileHome;
import br.com.itx.component.Util;
import br.jus.pje.nucleo.entidades.RelatorioLog;
import br.jus.pje.nucleo.entidades.SecaoJudiciaria;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * Classe action controladora do listView de /EstatisticaProcesso/
 * DistribuidosRankingSessao/
 * 
 * @author Daniel
 * 
 */
@Name(value = EstatisticaProcessosDistribuidosJulgadosAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class EstatisticaProcessosDistribuidosJulgadosAction implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "estatisticaProcessosDistribuidosJulgadosAction";

	private EstatisticaProcessosDistribuidosJulgadosList distribuidosJulgadosList = new EstatisticaProcessosDistribuidosJulgadosList();
	private static final String TEMPLATE_XLS_PATH = "/EstatisticaProcesso/DistribuidosJulgados/procDistribuidosJulgadosTemplate.xls";

	private String dataInicio;
	private String dataFim;
	private String dataInicioFormatada;
	private String dataFimFormatada;
	private double porcDistribuidosJulgados;
	private double totalGeralDistribuidos;
	private double totalGeralJulgados;
	private double totalGeralPorcentagem;
	private String msgErroAtualizacaoSecao = "";
	private List<EstatisticaProcessoDistribuidoJulgadoBean> estatisticaBeanList;
	private List<SecaoJudiciaria> secaoJudiciariaList;
	private SecaoJudiciaria secaoJudiciaria;
	@In
	private SecaoJudiciariaManager secaoJudiciariaManager;
	@In
	private EstatisticaEventoProcessoManager estatisticaEventoProcessoManager;
	@In
	private HistoricoEstatisticaEventoProcessoManager historicoEstatisticaEventoProcessoManager;
	@In
	private GenericManager genericManager;

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
	 * EstatisticaProcessoDistribuidoJulgadoBean.java
	 * 
	 * @return lista com os dados para Distribuidos/Julgados
	 */
	public List<EstatisticaProcessoDistribuidoJulgadoBean> estatisticaRankingList() {
		if (getEstatisticaBeanList() == null) {
			dataInicioFormatada = formatarAnoMes(dataInicio);
			dataFimFormatada = formatarAnoMes(dataFim);
			setEstatisticaBeanList(new ArrayList<EstatisticaProcessoDistribuidoJulgadoBean>());
			msgErroAtualizacaoSecao = "";
			List<Object[]> resultList = getDistribuidosJulgadosList().getResultList();
			resultList = invertList(resultList);
			setPorcDistribuidosJulgados(0);
			totalGeralDistribuidos = 0;
			totalGeralJulgados = 0;
			totalGeralPorcentagem = 0;
			if (resultList != null && resultList.size() > 0) {
				EstatisticaProcessoDistribuidoJulgadoBean epdjb = new EstatisticaProcessoDistribuidoJulgadoBean();
				getEstatisticaBeanList().add(epdjb);
				for (Object[] obj : resultList) {
					if (epdjb.getCodEstado() == null) {
						epdjb.setCodEstado(obj[0].toString());
						if (!ParametroUtil.instance().isPrimeiroGrau()) {
							if (historicoEstatisticaEventoProcessoManager
									.getDataAtualizacaoSessao(epdjb.getCodEstado()) != null) {
								msgErroAtualizacaoSecao = "SJ"
										+ epdjb.getCodEstado()
										+ " (atualizado até o dia "
										+ historicoEstatisticaEventoProcessoManager.getDataAtualizacaoSessao(epdjb
												.getCodEstado()) + ")";
							}
						}
					} else if (!epdjb.getCodEstado().equals(obj[0].toString())) {
						epdjb = new EstatisticaProcessoDistribuidoJulgadoBean();
						getEstatisticaBeanList().add(epdjb);
						epdjb.setCodEstado(obj[0].toString());
						if (!ParametroUtil.instance().isPrimeiroGrau()) {
							if (historicoEstatisticaEventoProcessoManager
									.getDataAtualizacaoSessao(epdjb.getCodEstado()) != null) {
								if (msgErroAtualizacaoSecao.length() > 0) {
									msgErroAtualizacaoSecao += ", SJ"
											+ epdjb.getCodEstado()
											+ " (atualizado até o dia "
											+ historicoEstatisticaEventoProcessoManager.getDataAtualizacaoSessao(epdjb
													.getCodEstado()) + ")";
								}
							}
						}
					}
					double nrProcessosDistribuidos = Double.parseDouble(obj[3].toString());
					double nrProcessosJulgados = Double.parseDouble(obj[4].toString());

					Double totalDistribuidos = epdjb.getTotalProcDistribuidos();
					epdjb.setTotalProcDistribuidos(totalDistribuidos == null ? nrProcessosDistribuidos
							: nrProcessosDistribuidos + totalDistribuidos);
					Double totalJulgados = epdjb.getTotalProcJulgados();
					epdjb.setTotalProcJulgados(totalJulgados == null ? nrProcessosJulgados : nrProcessosJulgados
							+ totalJulgados);

					setPorcDistribuidosJulgados(getPorcDistribuidosJulgados()
							+ ((nrProcessosJulgados / nrProcessosDistribuidos) * 100));

					Integer varasEstado = epdjb.getTotalVarasEstado();
					epdjb.setTotalVarasEstado(varasEstado == null ? 1 : varasEstado + 1);
					epdjb.setSomaPercDistribuidosJulgados(getPorcDistribuidosJulgados());

					ProcessoDistribuidoJulgadoListBean pdjlb = new ProcessoDistribuidoJulgadoListBean();
					pdjlb.setVara(getVara(obj));
					if (nrProcessosJulgados == 0 && nrProcessosDistribuidos == 0) {
						pdjlb.setPercentProcDistribuidosJulgados(0d);
					} else {
						pdjlb.setPercentProcDistribuidosJulgados((nrProcessosJulgados / nrProcessosDistribuidos) * 100);
					}
					pdjlb.setQtdProcDistribuidos(nrProcessosDistribuidos);
					pdjlb.setQtdProcJulgados(nrProcessosJulgados);
					epdjb.getDistribuidoJulgadoListBean().add(pdjlb);
					epdjb.setPercTotalDistribuidosJulgados(epdjb.getSomaPercDistribuidosJulgados()
							/ epdjb.getTotalVarasEstado());

					totalGeralDistribuidos += Double.parseDouble(obj[3].toString());
					totalGeralJulgados += Double.parseDouble(obj[4].toString());
					totalGeralPorcentagem = ((totalGeralJulgados / totalGeralDistribuidos) * 100);
				}
				if (!ParametroUtil.instance().isPrimeiroGrau() && !Strings.isEmpty(msgErroAtualizacaoSecao)) {
					if (msgErroAtualizacaoSecao.lastIndexOf(',') != -1) {
						int pos = msgErroAtualizacaoSecao.lastIndexOf(',');
						StringBuilder sb = new StringBuilder();
						sb.append(msgErroAtualizacaoSecao);
						sb.replace(pos, pos + 1, " e");
						sb.insert(msgErroAtualizacaoSecao.length() + 1, '.');
						msgErroAtualizacaoSecao = sb.toString();
					} else {
						StringBuilder sb = new StringBuilder();
						sb.append(msgErroAtualizacaoSecao);
						sb.insert(msgErroAtualizacaoSecao.length(), '.');
						msgErroAtualizacaoSecao = sb.toString();
					}
				}
			}
			setEstatisticaBeanList(invertList(getEstatisticaBeanList()));
		}
		return getEstatisticaBeanList();
	}

	/**
	 * Formata a vara para a exibição no relatório do ranking da seção
	 * 
	 * @param Object
	 *            [] o, sendo [1] = descrção do orgão julgador e [2] = descricao
	 *            da jurisdição
	 * @return
	 */
	private String getVara(Object[] o) {
		StringBuilder sb = new StringBuilder();
		sb.append(StringUtil.limparCharsNaoNumericos(o[1].toString())).append("ª ").append(o[2]).append(" ")
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
			relatorioLog.setDescricao("Estatística de Processos Distribuídos e Julgados (%)");
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
	public void exportarProcessosDistribuidosJulgadosSecaoXLS() {
		try {
			if (getEstatisticaBeanList().size() > 0) {
				exportarXLS(TEMPLATE_XLS_PATH, "DistribuidosJulgados.xls", "estatisticaBeanList",
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
		String rand = RandomStringUtils.randomAlphanumeric(6);// String
																// randomica
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
		map.put("secaoJudiciaria", secaoJudiciaria == null ? "Todas" : secaoJudiciaria);
		if (dataInicio != null && dataFim != null) {
			map.put("dataInicio", dataInicio);
			map.put("dataFim", dataFim);
		}
		if (!msgErroAtualizacaoSecao.isEmpty()) {
			map.put("msgErroAtualizacaoSecao", "Dados não computados na(s) Seção(ões): " + msgErroAtualizacaoSecao);
		}

		map.put("titulo", "ESTATÍSTICA DE PROCESSOS DISTRIBUÍDOS E JULGADOS (%)");
		map.put("nomeSecaoJudiciaria", ParametroUtil.getParametro("nomeSecaoJudiciaria").toUpperCase());
		map.put("nomeSistema", ParametroUtil.getParametro("nomeSistema"));
		map.put("totalGeralDistribuidos", totalGeralDistribuidos);
		map.put("totalGeralJulgados", totalGeralJulgados);
		map.put("totalGeralPorcentagem", totalGeralPorcentagem);
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
	public EstatisticaProcessosDistribuidosJulgadosList getDistribuidosJulgadosList() {
		return distribuidosJulgadosList;
	}

	public void setDistribuidosJulgadosList(EstatisticaProcessosDistribuidosJulgadosList distribuidosJulgadosList) {
		this.distribuidosJulgadosList = distribuidosJulgadosList;
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

	public void setPorcDistribuidosJulgados(double porcDistribuidosJulgados) {
		this.porcDistribuidosJulgados = porcDistribuidosJulgados;
	}

	public double getPorcDistribuidosJulgados() {
		return porcDistribuidosJulgados;
	}

	public void setTotalGeralDistribuidos(double totalGeralDistribuidos) {
		this.totalGeralDistribuidos = totalGeralDistribuidos;
	}

	public double getTotalGeralDistribuidos() {
		return totalGeralDistribuidos;
	}

	public void setTotalGeralJulgados(double totalGeralJulgados) {
		this.totalGeralJulgados = totalGeralJulgados;
	}

	public double getTotalGeralJulgados() {
		return totalGeralJulgados;
	}

	public void setTotalGeralPorcentagem(double totalGeralPorcentagem) {
		this.totalGeralPorcentagem = totalGeralPorcentagem;
	}

	public double getTotalGeralPorcentagem() {
		return totalGeralPorcentagem;
	}

	public String getMsgErroAtualizacaoSecao() {
		return msgErroAtualizacaoSecao;
	}

	public void setMsgErroAtualizacaoSecao(String msgErroAtualizacaoSecao) {
		this.msgErroAtualizacaoSecao = msgErroAtualizacaoSecao;
	}

	public void setEstatisticaBeanList(List<EstatisticaProcessoDistribuidoJulgadoBean> estatisticaBeanList) {
		this.estatisticaBeanList = estatisticaBeanList;
	}

	public List<EstatisticaProcessoDistribuidoJulgadoBean> getEstatisticaBeanList() {
		return estatisticaBeanList;
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