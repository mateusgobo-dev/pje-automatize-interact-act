package br.com.infox.pje.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
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
import br.com.infox.pje.bean.EstatisticaDistribuidosListBean;
import br.com.infox.pje.bean.EstatisticaProcessosDistribuidosBean;
import br.com.infox.pje.list.ProcessosDistribuidosList;
import br.com.infox.pje.manager.EstatisticaEventoProcessoManager;
import br.com.infox.pje.manager.HistoricoEstatisticaEventoProcessoManager;
import br.com.infox.pje.manager.SecaoJudiciariaManager;
import br.com.itx.component.FileHome;
import br.com.itx.component.Util;
import br.jus.pje.nucleo.entidades.RelatorioLog;
import br.jus.pje.nucleo.entidades.SecaoJudiciaria;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.util.DateUtil;

/**
 * Classe action controladora do listView de /EstatisticaProcesso/ Distribuidos/
 * 
 * @author wILSON
 * 
 */
@Name(value = EstatisticaProcessosDistribuidosAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class EstatisticaProcessosDistribuidosAction implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "estatisticaProcessosDistribuidosAction";

	private ProcessosDistribuidosList estatisticaProcessosDistribuidosList = new ProcessosDistribuidosList();
	private static final String TEMPLATE_XLS_PATH = "/EstatisticaProcesso/Distribuidos/processosDistribuidosTemplate.xls";

	private String dataInicio;
	private String dataFim;
	private String dataInicioFormatada;
	private String dataFimFormatada;
	private long totalProcessos;
	private int totalVaras;
	private List<EstatisticaProcessosDistribuidosBean> estatisticaBeanList;
	private List<SecaoJudiciaria> secaoJudiciariaList;
	private String codEstado;
	private long totalGeral;
	private SecaoJudiciaria secaoJudiciaria;
	private String msgErroAtualizacaoSecao = "";
	private long[] totalGeralMes = { 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L };
	@In
	private GenericManager genericManager;
	@In
	private EstatisticaEventoProcessoManager estatisticaEventoProcessoManager;
	@In
	private SecaoJudiciariaManager secaoJudiciariaManager;
	@In
	private HistoricoEstatisticaEventoProcessoManager historicoEstatisticaEventoProcessoManager;

	/**
	 * Obtem o relatório da estatística convertendo a lista proveniente do banco
	 * para uma lista no esquema da exibição necessária para a tela utilizando o
	 * EstatisticaDistribuidosListBean.java
	 * 
	 * @return lista com os dados para os processos distribuidos
	 */
	public List<EstatisticaProcessosDistribuidosBean> estatisticaProcessosDistribuidosList() {
		if (estatisticaBeanList == null) {
			for (int i = 0; i < totalGeralMes.length; i++) {
				totalGeralMes[i] = 0;
			}
			dataInicioFormatada = formatarAnoMes(dataInicio);
			dataFimFormatada = formatarAnoMes(dataFim);
			totalGeral = 0;
			msgErroAtualizacaoSecao = "";
			estatisticaBeanList = buildEstatisticaProcessosDistribuidosList();
			for (EstatisticaProcessosDistribuidosBean obj : estatisticaBeanList) {
				for (int i = 0; i < 12; i++) {
					totalGeralMes[(i)] = totalGeralMes[(i)] + obj.getTotalMes()[(i)];
				}
			}
		}
		return estatisticaBeanList;
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

	private List<EstatisticaProcessosDistribuidosBean> buildEstatisticaProcessosDistribuidosList() {
		List<Map<String, Object>> mapList = getEstatisticaProcessosDistribuidosList().getResultList();
		List<EstatisticaProcessosDistribuidosBean> estatisticaProcessosDistribuidosBeanList = new ArrayList<EstatisticaProcessosDistribuidosBean>();
		EstatisticaProcessosDistribuidosBean bean = new EstatisticaProcessosDistribuidosBean();
		for (Map<String, Object> map : mapList) {
			if (bean.getCodEstado() == null) {
				bean.setCodEstado(map.get("codEstado").toString());
				if (!ParametroUtil.instance().isPrimeiroGrau()) {
					if (historicoEstatisticaEventoProcessoManager.getDataAtualizacaoSessao(bean.getCodEstado()) != null) {
						msgErroAtualizacaoSecao = "SJ"
								+ bean.getCodEstado()
								+ " (atualizado até o dia "
								+ historicoEstatisticaEventoProcessoManager.getDataAtualizacaoSessao(bean
										.getCodEstado()) + ")";
					}
				}
			} else if (!bean.getCodEstado().equals(map.get("codEstado").toString())) {
				bean = new EstatisticaProcessosDistribuidosBean();
				bean.setCodEstado(map.get("codEstado").toString());
				if (!ParametroUtil.instance().isPrimeiroGrau()) {
					if (historicoEstatisticaEventoProcessoManager.getDataAtualizacaoSessao(bean.getCodEstado()) != null) {
						if (msgErroAtualizacaoSecao.length() > 0) {
							msgErroAtualizacaoSecao += ", SJ"
									+ bean.getCodEstado()
									+ " (atualizado até o dia "
									+ historicoEstatisticaEventoProcessoManager.getDataAtualizacaoSessao(bean
											.getCodEstado()) + ")";
						}
					}
				}
			}

			bean.setEstatisticaDistribuidosListBean(estatisticaEventoProcessoManager.listVaraBySecao(
					dataInicioFormatada, dataFimFormatada, bean.getCodEstado()));
			for (EstatisticaDistribuidosListBean obj : bean.getEstatisticaDistribuidosListBean()) {
				for (Integer chave : obj.getQtPorMes().keySet()) {
					bean.getTotalMes()[(chave - 1)] = bean.getTotalMes()[(chave - 1)] + obj.getQtPorMes().get(chave);
				}
			}
			List<EstatisticaDistribuidosListBean> lista = new ArrayList<EstatisticaDistribuidosListBean>();
			lista.addAll(bean.getEstatisticaDistribuidosListBean());
			bean.getEstatisticaDistribuidosListBean().clear();
			bean.getEstatisticaDistribuidosListBean().addAll(ordenarVaras(lista));
			estatisticaProcessosDistribuidosBeanList.add(bean);
			totalGeral += bean.getTotalDistribuidos();
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
		return estatisticaProcessosDistribuidosBeanList;
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
		if (estatisticaProcessosDistribuidosList().size() > 0) {
			RelatorioLog relatorioLog = new RelatorioLog();
			relatorioLog.setDataSolicitacao(new Date());
			relatorioLog.setDescricao("Estatística de Processos Distribuídos");
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
	public void exportarEstatiscaProcessosDistribuidosXLS() {
		try {
			if (estatisticaBeanList.size() > 0) {
				exportarXLS(TEMPLATE_XLS_PATH, "ProcessosDistribuidos.xls", "estatisticaBeanList", estatisticaBeanList);
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
		map.put("titulo", "ESTATÍSTICA DE PROCESSOS DISTRIBUÍDOS");
		map.put("subNomeSistema", ParametroUtil.getParametro("nomeSecaoJudiciaria").toUpperCase());
		map.put("nomeSistema", ParametroUtil.getParametro("nomeSistema"));

		for (int i = 0; i < totalGeralMes.length; i++) {
			map.put("totMes" + DateUtil.getMesExtenso(i + 1), totalGeralMes[i]);
		}
		map.put("totGeralDist", totalGeral);
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

	public List<EstatisticaDistribuidosListBean> ordenarVaras(List<EstatisticaDistribuidosListBean> lista) {
		List<Integer> lista1 = new ArrayList<Integer>();

		// pega todos os números das varas e coloca em uma lista para ordenação
		// posterior
		for (EstatisticaDistribuidosListBean o : lista) {
			String vara = o.getVara();
			if (vara.indexOf("ª") > 0) {
				CharSequence subSequence = vara.subSequence(0, vara.indexOf("ª"));
				lista1.add(Integer.valueOf(subSequence.toString().trim()));
			}
		}

		// ordena as varas da lista de forma crescente
		Collections.sort(lista1);

		// cria a lista de retorno ao usuário ordenado de acordo com o número da
		// vara
		List<EstatisticaDistribuidosListBean> lista3 = new ArrayList<EstatisticaDistribuidosListBean>();
		for (Integer u : lista1) {
			for (EstatisticaDistribuidosListBean o : lista) {
				String varas = o.getVara();
				CharSequence subSequence = varas.subSequence(0, varas.indexOf("ª"));
				if (varas.indexOf("ª") > 0) {
					if (Integer.valueOf(subSequence.toString().trim()).equals(u) && !lista3.contains(o)) {
						lista3.add(o);
					}
				}
			}
		}

		// adiciona as varas que não tem número
		for (EstatisticaDistribuidosListBean o : lista) {
			if (!lista3.contains(o)) {
				lista3.add(o);
			}
		}

		return lista3;
	}

	/*
	 * Inicio - Getters and Setters
	 */
	public ProcessosDistribuidosList getEstatisticaProcessosDistribuidosList() {
		return estatisticaProcessosDistribuidosList;
	}

	public void setEstatisticaProcessosDistribuidosList(ProcessosDistribuidosList estatisticaProcessosDistribuidosList) {
		this.estatisticaProcessosDistribuidosList = estatisticaProcessosDistribuidosList;
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

	public long getTotalProcessos() {
		return totalProcessos;
	}

	public void setTotalProcessos(long totalProcessos) {
		this.totalProcessos = totalProcessos;
	}

	public int getTotalVaras() {
		return totalVaras;
	}

	public void setTotalVaras(int totalVaras) {
		this.totalVaras = totalVaras;
	}

	public List<EstatisticaProcessosDistribuidosBean> getEstatisticaBeanList() {
		return estatisticaBeanList;
	}

	public void setEstatisticaBeanList(List<EstatisticaProcessosDistribuidosBean> estatisticaBeanList) {
		this.estatisticaBeanList = estatisticaBeanList;
	}

	public String getCodEstado() {
		return codEstado;
	}

	public void setCodEstado(String codEstado) {
		this.codEstado = codEstado;
	}

	public void limparFiltros() {
		if (!ParametroUtil.instance().isPrimeiroGrau()) {
			secaoJudiciaria = null;
		}
		msgErroAtualizacaoSecao = "";
		dataFim = null;
		dataInicio = null;
	}

	public long getTotalGeral() {
		return totalGeral;
	}

	public void setTotalGeral(long totalGeral) {
		this.totalGeral = totalGeral;
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

	public String getMsgErroAtualizacaoSecao() {
		return msgErroAtualizacaoSecao;
	}

	public void setMsgErroAtualizacaoSecao(String msgErroAtualizacaoSecao) {
		this.msgErroAtualizacaoSecao = msgErroAtualizacaoSecao;
	}

	private List<SecaoJudiciaria> secaoJudiciariaItems() {
		return secaoJudiciariaManager.secaoJudiciariaItems();
	}

	public void secaoJudiciaria1Grau() {
		secaoJudiciaria = secaoJudiciariaManager.secaoJudiciaria1Grau();
	}

	public void setTotalGeralMes(long[] totalGeralMes) {
		this.totalGeralMes = totalGeralMes;
	}

	public long[] getTotalGeralMes() {
		return totalGeralMes;
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