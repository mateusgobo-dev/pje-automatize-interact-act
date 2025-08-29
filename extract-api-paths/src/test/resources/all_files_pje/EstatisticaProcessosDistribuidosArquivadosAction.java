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
import br.com.infox.pje.bean.EstatisticaDistribuidosArquivadosBean;
import br.com.infox.pje.bean.EstatisticaDistribuidosArquivadosListBean;
import br.com.infox.pje.list.EstatisticaProcessosDistribuidosArquivadosList;
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
 * DistribuidosArquivados/
 * 
 * @author Wilson
 * 
 */
@Name(value = EstatisticaProcessosDistribuidosArquivadosAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class EstatisticaProcessosDistribuidosArquivadosAction implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "estatisticaProcessosDistribuidosArquivadosAction";

	private EstatisticaProcessosDistribuidosArquivadosList distruibuidosArquivadosList = new EstatisticaProcessosDistribuidosArquivadosList();
	private static final String TEMPLATE_XLS_PATH = "/EstatisticaProcesso/DistribuidosArquivados/procDistribuidosArquivadosTemplate.xls";

	private Long totalDistribuidos;
	private Long totalArquivados;
	private SecaoJudiciaria secaoJudiciaria;
	private List<SecaoJudiciaria> secaoJudiciariaList;
	private String dataInicio;
	private String dataFim;
	private String dataInicioFormatada;
	private String dataFimFormatada;
	private String msgErroAtualizacaoSecao = "";
	private List<EstatisticaDistribuidosArquivadosBean> estatisticaDABeanList;
	@In
	private EstatisticaEventoProcessoManager estatisticaEventoProcessoManager;
	@In
	private GenericManager genericManager;
	@In
	private SecaoJudiciariaManager secaoJudiciariaManager;
	@In
	private HistoricoEstatisticaEventoProcessoManager historicoEstatisticaEventoProcessoManager;

	/**
	 * Obtem o relatório da estatística convertendo a lista proveniente do banco
	 * para uma lista no esquema da exibição necessária para a tela utilizando o
	 * EstatisticaDistribuidosArquivadosBean.java
	 * 
	 * @return lista com os dados para Distribuídos/Arquivados
	 */
	public List<EstatisticaDistribuidosArquivadosBean> estatisticaDistribuidosArquivadosList() {
		if (getEstatisticaDABeanList() == null) {
			dataInicioFormatada = formatarAnoMes(dataInicio);
			dataFimFormatada = formatarAnoMes(dataFim);
			msgErroAtualizacaoSecao = "";
			totalArquivados = 0L;
			totalDistribuidos = 0L;
			setEstatisticaDABeanList(new ArrayList<EstatisticaDistribuidosArquivadosBean>());
			List<Object[]> resultList = getDistruibuidosArquivadosList().getResultList();
			resultList = invertList(resultList);
			if (resultList != null && resultList.size() > 0) {
				EstatisticaDistribuidosArquivadosBean edab = new EstatisticaDistribuidosArquivadosBean();
				getEstatisticaDABeanList().add(edab);
				double porcDistribuidosArquivados = 0;
				for (Object[] obj : resultList) {
					if (edab.getCodEstado() == null) {
						edab.setCodEstado(obj[0].toString());
						if (!ParametroUtil.instance().isPrimeiroGrau()) {
							if (historicoEstatisticaEventoProcessoManager.getDataAtualizacaoSessao(edab.getCodEstado()) != null) {
								msgErroAtualizacaoSecao = "SJ"
										+ edab.getCodEstado()
										+ " (atualizado até o dia "
										+ historicoEstatisticaEventoProcessoManager.getDataAtualizacaoSessao(edab
												.getCodEstado()) + ")";
							}
						}
					} else if (!edab.getCodEstado().equals(obj[0].toString())) {
						edab = new EstatisticaDistribuidosArquivadosBean();
						getEstatisticaDABeanList().add(edab);
						edab.setCodEstado(obj[0].toString());
						if (!ParametroUtil.instance().isPrimeiroGrau()) {
							if (historicoEstatisticaEventoProcessoManager.getDataAtualizacaoSessao(edab.getCodEstado()) != null) {
								if (msgErroAtualizacaoSecao.length() > 0) {
									msgErroAtualizacaoSecao += ", SJ"
											+ edab.getCodEstado()
											+ " (atualizado até o dia "
											+ historicoEstatisticaEventoProcessoManager.getDataAtualizacaoSessao(edab
													.getCodEstado()) + ")";
								}
							}
						}
					}
					Long qtdProcessosDistribuidos = Long.parseLong(obj[3].toString());
					Long qtdProcessosArquivados = Long.parseLong(obj[4].toString());

					Long totalDistribuidos = edab.getTotalProcDistribuidos();
					edab.setTotalProcDistribuidos(totalDistribuidos == null ? qtdProcessosDistribuidos
							: qtdProcessosDistribuidos + totalDistribuidos);
					Long totalArquivados = edab.getTotalProcArquivados();

					edab.setTotalProcArquivados(totalArquivados == null ? qtdProcessosArquivados
							: qtdProcessosArquivados + totalArquivados);

					porcDistribuidosArquivados += (qtdProcessosArquivados / qtdProcessosDistribuidos.doubleValue()) * 100;

					Long varasEstado = edab.getTotalVarasEstados();
					edab.setTotalVarasEstados(varasEstado == null ? 1 : varasEstado + 1);
					edab.setSomaPercDistribuidosArquivados(porcDistribuidosArquivados);

					EstatisticaDistribuidosArquivadosListBean edalb = new EstatisticaDistribuidosArquivadosListBean();
					edalb.setVaras(getVara(obj));
					edalb.setPercenteProcDistribuidosArquivados((qtdProcessosArquivados / qtdProcessosDistribuidos
							.doubleValue()) * 100);
					edalb.setQtdProcessosDistribuidos(qtdProcessosDistribuidos);
					edalb.setQtdProcessosArquivados(qtdProcessosArquivados);

					this.totalArquivados += qtdProcessosArquivados;
					this.totalDistribuidos += qtdProcessosDistribuidos;

					edab.getDistribuidosArquivadosListBean().add(edalb);
					Collections.reverse(edab.getDistribuidosArquivadosListBean());
					Collections.sort(edab.getDistribuidosArquivadosListBean(), getVaraComparator());
					edab.setPercTotalDistribuidosArquivados(edab.getSomaPercDistribuidosArquivados()
							/ edab.getTotalVarasEstados().doubleValue());
					edab.setTotalProcDistribuidos(edab.getTotalProcDistribuidos());
					edab.setTotalProcArquivados(edab.getTotalProcArquivados());
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
		}
		return getEstatisticaDABeanList();
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

	@SuppressWarnings("unchecked")
	private Comparator<? super EstatisticaDistribuidosArquivadosListBean> getVaraComparator() {
		return new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				EstatisticaDistribuidosArquivadosListBean bean1 = (EstatisticaDistribuidosArquivadosListBean) o1;
				EstatisticaDistribuidosArquivadosListBean bean2 = (EstatisticaDistribuidosArquivadosListBean) o2;
				Integer splitedVara1 = Integer.parseInt(bean1.getVaras().split("ª")[0]);
				Integer splitedVara2 = Integer.parseInt(bean2.getVaras().split("ª")[0]);
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
		setEstatisticaDABeanList(null);
		if (estatisticaDistribuidosArquivadosList().size() > 0) {
			RelatorioLog relatorioLog = new RelatorioLog();
			relatorioLog.setDataSolicitacao(new Date());
			relatorioLog.setDescricao("Estatística de Processos Distribuídos e Arquivados (%)");
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
	public void exportarProcessoDistribuidosArquivadosXLS() {
		try {
			if (getEstatisticaDABeanList().size() > 0) {
				exportarXLS(TEMPLATE_XLS_PATH, "DistribuidosArquivados.xls", "estatisticaDABeanList",
						getEstatisticaDABeanList());
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

		if (dataInicio != null && dataFim != null) {
			map.put("dataInicio", dataInicio);
			map.put("dataFim", dataFim);
		}
		if (!msgErroAtualizacaoSecao.isEmpty()) {
			map.put("msgErroAtualizacaoSecao", "Dados não computados na(s) Seção(ões): " + msgErroAtualizacaoSecao);
		}

		map.put("secaoJudiciaria", secaoJudiciaria == null ? "Todas" : secaoJudiciaria);
		map.put("totalArquivados", totalArquivados);
		map.put("totalDistribuidos", totalDistribuidos);
		map.put("totalPercentual", getTotalPercentual());
		map.put("titulo", "ESTATÍSTICA DE PROCESSOS DISTRIBUÍDOS E ARQUIVADOS (%)");
		map.put("nomeSecaoJudiciaria", ParametroUtil.getParametro("nomeSecaoJudiciaria").toUpperCase());
		map.put("nomeSistema", ParametroUtil.getParametro("nomeSistema"));

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

	public String getMsgErroAtualizacaoSecao() {
		return msgErroAtualizacaoSecao;
	}

	public void setMsgErroAtualizacaoSecao(String msgErroAtualizacaoSecao) {
		this.msgErroAtualizacaoSecao = msgErroAtualizacaoSecao;
	}

	public EstatisticaProcessosDistribuidosArquivadosList getDistruibuidosArquivadosList() {
		return distruibuidosArquivadosList;
	}

	public void setDistruibuidosArquivadosList(
			EstatisticaProcessosDistribuidosArquivadosList distruibuidosArquivadosList) {
		this.distruibuidosArquivadosList = distruibuidosArquivadosList;
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

	public Long getTotalDistribuidos() {
		return totalDistribuidos;
	}

	public void setTotalDistribuidos(Long totalDistribuidos) {
		this.totalDistribuidos = totalDistribuidos;
	}

	public Long getTotalArquivados() {
		return totalArquivados;
	}

	public void setTotalArquivados(Long totalArquivados) {
		this.totalArquivados = totalArquivados;
	}

	public void setEstatisticaDABeanList(List<EstatisticaDistribuidosArquivadosBean> estatisticaDABeanList) {
		this.estatisticaDABeanList = estatisticaDABeanList;
	}

	public List<EstatisticaDistribuidosArquivadosBean> getEstatisticaDABeanList() {
		return estatisticaDABeanList;
	}

	public double getTotalPercentual() {
		Double totalPercentual = (totalArquivados.doubleValue() * 100) / totalDistribuidos.doubleValue();
		if (totalPercentual.equals(Double.NaN)) {
			return 0;
		} else {
			return totalPercentual;
		}
	}

	private List<SecaoJudiciaria> secaoJudiciariaItems() {
		return secaoJudiciariaManager.secaoJudiciariaItems();
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