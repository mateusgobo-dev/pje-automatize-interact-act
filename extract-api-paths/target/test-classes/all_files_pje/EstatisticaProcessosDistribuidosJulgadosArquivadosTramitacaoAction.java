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
import br.com.infox.pje.bean.EstatisticaDistribuidosJulgadosArquivadosTramitacaoBean;
import br.com.infox.pje.bean.EstatisticaDistribuidosJulgadosArquivadosTramitacaoListBean;
import br.com.infox.pje.list.EstatisticaProcessosDistribuidosJulgadosArquivadosTramitacaoList;
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
 * DistribuidosJulgadosArquivadosTramitacao/
 * 
 * @author Geldo
 * 
 */
@Name(value = EstatisticaProcessosDistribuidosJulgadosArquivadosTramitacaoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class EstatisticaProcessosDistribuidosJulgadosArquivadosTramitacaoAction implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3359238753608948896L;

	public static final String NAME = "estatisticaProcessosDistribuidosJulgadosArquivadosTramitacaoAction";

	private EstatisticaProcessosDistribuidosJulgadosArquivadosTramitacaoList distribuidosJulgadosArquivadosTramitacaoList = new EstatisticaProcessosDistribuidosJulgadosArquivadosTramitacaoList();
	private static final String TEMPLATE_XLS_PATH = "/EstatisticaProcesso/DistribuidosJulgadosArquivadosTramitacao/procDistJulgArqTramTemplate.xls";

	private String dataInicio;
	private String dataFim;
	private String dataInicioFormatada;
	private String dataFimFormatada;
	private int totProcessosDistribuidos;
	private int totProcessosJulgados;
	private int totProcessosArquivados;
	private int totProcessosTramitacao;
	private List<EstatisticaDistribuidosJulgadosArquivadosTramitacaoBean> estDistArqJulgTramBeanList;
	private List<SecaoJudiciaria> secaoJudiciariaList;
	private SecaoJudiciaria secaoJudiciaria;
	private String msgErroAtualizacaoSecao = "";
	@In
	private GenericManager genericManager;
	@In
	private EstatisticaEventoProcessoManager estatisticaEventoProcessoManager;
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
	 * EstatisticaDistribuidosJulgadosArquivadosTramitacaoBean.java
	 * 
	 * @return lista com os dados para Distribuídos/Arquivados
	 */
	public List<EstatisticaDistribuidosJulgadosArquivadosTramitacaoBean> estatisticaDistribuidosJulgadosArquivadosTramitacaoList() {
		if (getEstDistArqJulgTramBeanList() == null) {
			dataInicioFormatada = formatarAnoMes(dataInicio);
			dataFimFormatada = formatarAnoMes(dataFim);
			setEstDistArqJulgTramBeanList(new ArrayList<EstatisticaDistribuidosJulgadosArquivadosTramitacaoBean>());
			msgErroAtualizacaoSecao = "";
			List<Object[]> resultList = getDistribuidosJulgadosArquivadosTramitacaoList().getResultList();
			resultList = invertList(resultList);
			totProcessosDistribuidos = 0;
			totProcessosJulgados = 0;
			totProcessosArquivados = 0;
			totProcessosTramitacao = 0;
			if (resultList != null && resultList.size() > 0) {
				EstatisticaDistribuidosJulgadosArquivadosTramitacaoBean epdjatb = new EstatisticaDistribuidosJulgadosArquivadosTramitacaoBean();
				getEstDistArqJulgTramBeanList().add(epdjatb);
				for (Object[] obj : resultList) {
					if (epdjatb.getCodEstado() == null) {
						epdjatb.setCodEstado(obj[0].toString());
						if (!ParametroUtil.instance().isPrimeiroGrau()) {
							if (historicoEstatisticaEventoProcessoManager.getDataAtualizacaoSessao(epdjatb
									.getCodEstado()) != null) {
								msgErroAtualizacaoSecao = "SJ"
										+ epdjatb.getCodEstado()
										+ " (atualizado até o dia "
										+ historicoEstatisticaEventoProcessoManager.getDataAtualizacaoSessao(epdjatb
												.getCodEstado()) + ")";
							}
						}
					} else if (!epdjatb.getCodEstado().equals(obj[0].toString())) {
						epdjatb = new EstatisticaDistribuidosJulgadosArquivadosTramitacaoBean();
						getEstDistArqJulgTramBeanList().add(epdjatb);
						epdjatb.setCodEstado(obj[0].toString());
						if (!ParametroUtil.instance().isPrimeiroGrau()) {
							if (historicoEstatisticaEventoProcessoManager.getDataAtualizacaoSessao(epdjatb
									.getCodEstado()) != null) {
								if (msgErroAtualizacaoSecao.length() > 0) {
									msgErroAtualizacaoSecao += ", SJ"
											+ epdjatb.getCodEstado()
											+ " (atualizado até o dia "
											+ historicoEstatisticaEventoProcessoManager
													.getDataAtualizacaoSessao(epdjatb.getCodEstado()) + ")";
								}
							}
						}
					}
					int qtdProcessosDistribuidos = Integer.parseInt(obj[3] != null ? obj[3].toString() : "0");
					int qtdProcessosJulgados = Integer.parseInt(obj[4] != null ? obj[4].toString() : "0");
					int qtdProcessosArquivados = Integer.parseInt(obj[5] != null ? obj[5].toString() : "0");
					int qtdProcessosTramitacao = Integer.parseInt(obj[6] != null ? obj[6].toString() : "0");

					Integer totalDistribuidos = epdjatb.getTotalProcDistribuidos();
					epdjatb.setTotalProcDistribuidos(totalDistribuidos == null ? qtdProcessosDistribuidos
							: qtdProcessosDistribuidos + totalDistribuidos);

					Integer totalJulgados = epdjatb.getTotalProcJulgados();
					epdjatb.setTotalProcJulgados(totalJulgados == null ? qtdProcessosJulgados : qtdProcessosJulgados
							+ totalJulgados);

					Integer totalArquivados = epdjatb.getTotalProcArquivados();
					epdjatb.setTotalProcArquivados(totalArquivados == null ? qtdProcessosArquivados
							: qtdProcessosArquivados + totalArquivados);

					Integer totalTramitacao = epdjatb.getTotalProcTramitacao();
					epdjatb.setTotalProcTramitacao(totalTramitacao == null ? qtdProcessosTramitacao
							: qtdProcessosTramitacao + totalTramitacao);

					EstatisticaDistribuidosJulgadosArquivadosTramitacaoListBean epdjatlb = new EstatisticaDistribuidosJulgadosArquivadosTramitacaoListBean();
					epdjatlb.setVaras(getVara(obj));

					epdjatlb.setQtdProcessosDistribuidos(qtdProcessosDistribuidos);
					epdjatlb.setQtdProcessosJulgados(qtdProcessosJulgados);
					epdjatlb.setQtdProcessosArquivados(qtdProcessosArquivados);
					epdjatlb.setQtdProcessosTramitacao(qtdProcessosTramitacao);

					epdjatb.getDistribuidosJulgadosArquivadosTramitacaoListBean().add(epdjatlb);

					epdjatb.setTotalProcDistribuidos(epdjatb.getTotalProcDistribuidos());
					epdjatb.setTotalProcJulgados(epdjatb.getTotalProcJulgados());
					epdjatb.setTotalProcArquivados(epdjatb.getTotalProcArquivados());
					epdjatb.setTotalProcTramitacao(epdjatb.getTotalProcTramitacao());

					totProcessosDistribuidos += Integer.parseInt(obj[3] != null ? obj[3].toString() : "0");
					totProcessosJulgados += Integer.parseInt(obj[4] != null ? obj[4].toString() : "0");
					totProcessosArquivados += Integer.parseInt(obj[5] != null ? obj[5].toString() : "0");
					totProcessosTramitacao += Integer.parseInt(obj[6] != null ? obj[6].toString() : "0");
				}
				if (!ParametroUtil.instance().isPrimeiroGrau()) {
					StringBuilder sb = new StringBuilder();
					if (msgErroAtualizacaoSecao.lastIndexOf(',') != -1) {
						int pos = msgErroAtualizacaoSecao.lastIndexOf(',');
						sb.append(msgErroAtualizacaoSecao);
						sb.replace(pos, pos + 1, " e");
						if (!Strings.isEmpty(msgErroAtualizacaoSecao)) {
							sb.insert(msgErroAtualizacaoSecao.length() + 1, '.');
						}
						msgErroAtualizacaoSecao = sb.toString();
					} else {
						sb.append(msgErroAtualizacaoSecao);
						if (!Strings.isEmpty(msgErroAtualizacaoSecao)) {
							sb.insert(msgErroAtualizacaoSecao.length(), '.');
						}
						msgErroAtualizacaoSecao = sb.toString();
					}
				}
			}
			setEstDistArqJulgTramBeanList(invertList(getEstDistArqJulgTramBeanList()));
		}

		return getEstDistArqJulgTramBeanList();
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
		setEstDistArqJulgTramBeanList(null);
		if (estatisticaDistribuidosJulgadosArquivadosTramitacaoList().size() > 0) {
			RelatorioLog relatorioLog = new RelatorioLog();
			relatorioLog.setDataSolicitacao(new Date());
			relatorioLog.setDescricao("Estatística de Processos Distribuídos, Julgados, Arquivados e em tramitação");
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
	public void exportarProcessoDistribuidosJulgadosArquivadosTramitacaoXLS() {
		try {
			if (getEstDistArqJulgTramBeanList().size() > 0) {
				exportarXLS(TEMPLATE_XLS_PATH, "DistribuidosJulgadosArquivadosTramitacao.xls",
						"estDistArqJulgTramBeanList", getEstDistArqJulgTramBeanList());
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

		map.put("titulo", "ESTATÍSTICA DE PROCESSOS DISTRIBUÍDOS, JULGADOS, ARQUIVADOS E EM TRAMITAÇÃO");
		map.put("nomeSecaoJudiciaria", ParametroUtil.getParametro("nomeSecaoJudiciaria").toUpperCase());
		map.put("nomeSistema", ParametroUtil.getParametro("nomeSistema"));
		map.put("secaoJudiciaria", secaoJudiciaria == null ? "Todas" : secaoJudiciaria);
		map.put("totProcessosDistribuidos", totProcessosDistribuidos);
		map.put("totProcessosJulgados", totProcessosJulgados);
		map.put("totProcessosArquivados", totProcessosArquivados);
		map.put("totProcessosTramitacao", totProcessosTramitacao);
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

	public EstatisticaProcessosDistribuidosJulgadosArquivadosTramitacaoList getDistribuidosJulgadosArquivadosTramitacaoList() {
		return distribuidosJulgadosArquivadosTramitacaoList;
	}

	public void setDistribuidosJulgadosArquivadosTramitacaoList(
			EstatisticaProcessosDistribuidosJulgadosArquivadosTramitacaoList distribuidosJulgadosArquivadosTramitacaoList) {
		this.distribuidosJulgadosArquivadosTramitacaoList = distribuidosJulgadosArquivadosTramitacaoList;
	}

	public static EstatisticaProcessosDistribuidosJulgadosArquivadosTramitacaoAction instance() {
		return ComponentUtil.getComponent(NAME);
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

	public int getTotProcessosDistribuidos() {
		return totProcessosDistribuidos;
	}

	public void setTotProcessosDistribuidos(int totProcessosDistribuidos) {
		this.totProcessosDistribuidos = totProcessosDistribuidos;
	}

	public int getTotProcessosJulgados() {
		return totProcessosJulgados;
	}

	public void setTotProcessosJulgados(int totProcessosJulgados) {
		this.totProcessosJulgados = totProcessosJulgados;
	}

	public int getTotProcessosArquivados() {
		return totProcessosArquivados;
	}

	public void setTotProcessosArquivados(int totProcessosArquivados) {
		this.totProcessosArquivados = totProcessosArquivados;
	}

	public int getTotProcessosTramitacao() {
		return totProcessosTramitacao;
	}

	public void setTotProcessosTramitacao(int totProcessosTramitacao) {
		this.totProcessosTramitacao = totProcessosTramitacao;
	}

	public void setEstDistArqJulgTramBeanList(
			List<EstatisticaDistribuidosJulgadosArquivadosTramitacaoBean> estDistArqJulgTramBeanList) {
		this.estDistArqJulgTramBeanList = estDistArqJulgTramBeanList;
	}

	public List<EstatisticaDistribuidosJulgadosArquivadosTramitacaoBean> getEstDistArqJulgTramBeanList() {
		return estDistArqJulgTramBeanList;
	}

	public void limpaDataPeriodo() {
		if (!ParametroUtil.instance().isPrimeiroGrau()) {
			secaoJudiciaria = null;
			msgErroAtualizacaoSecao = "";
		}
		dataInicio = null;
		dataFim = null;
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