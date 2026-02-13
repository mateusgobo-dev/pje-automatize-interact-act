package br.com.infox.pje.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.manager.GenericManager;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.bean.RemanescentesTramitacaoBean;
import br.com.infox.pje.bean.RemanescentesTramitacaoSubTableBean;
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
@Name(value = RemanescentesTramitacaoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class RemanescentesTramitacaoAction implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4051446408642335811L;

	public static final String NAME = "remanescentesTramitacaoAction";

	private static final String TEMPLATE_XLS_PATH = "/EstatisticaProcesso/RemanescentesTramitacao/remanescentesTramitacaoTemplate.xls";

	private Date dataInicio;
	private Date dataFim;
	private String dataInicioStr;
	private String dataFimStr;
	private String erroAtualizarSecao = "";
	private List<RemanescentesTramitacaoBean> remanescentesList;
	private List<SecaoJudiciaria> secaoJudiciariaList;
	private SecaoJudiciaria secaoJudiciaria;
	private long[] totalGeralProcessos = { 0L, 0L, 0L, 0L, 0L };
	@In
	private EstatisticaEventoProcessoManager estatisticaEventoProcessoManager;
	@In
	private HistoricoEstatisticaEventoProcessoManager historicoEstatisticaEventoProcessoManager;
	@In
	private GenericManager genericManager;
	@In
	private SecaoJudiciariaManager secaoJudiciariaManager;

	public List<RemanescentesTramitacaoBean> getRemanescentesList() {
		if (remanescentesList == null && getDataInicio() != null && getDataFim() != null) {
			String erro = "";
			StringBuilder erroTemp = new StringBuilder();
			erroAtualizarSecao = "";
			int pos = 0;
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			Calendar dataTramitacao = Calendar.getInstance();
			dataTramitacao.setTime(dataFim);
			dataTramitacao.set(Calendar.DAY_OF_MONTH, 1);
			Calendar dataRemancescente = Calendar.getInstance();
			dataRemancescente.setTime(dataInicio);
			dataRemancescente.set(Calendar.DAY_OF_MONTH, 1);
			dataRemancescente.set(Calendar.MONTH, 0);
			List<Object[]> resultList = null;
			if (!ParametroUtil.instance().isPrimeiroGrau() && getSecaoJudiciaria() != null) {
				resultList = estatisticaEventoProcessoManager.listProcessosRemaDisJulArqTramitacaoComSessao(
						dataInicioStr, dataFimStr, ParametroUtil.instance().getEventoProcessualDistribuicao()
								.getCodEvento(), ParametroUtil.instance().getEventoJulgamentoProcessual()
								.getCodEvento(), ParametroUtil.instance().getEventoArquivamento().getCodEvento(),
						ParametroUtil.instance().getEventoArquivamentoDefinitivoProcessual().getCodEvento(),
						ParametroUtil.instance().getEventoBaixaDefinitivaProcessual().getCodEvento(), ParametroUtil
								.instance().getEventoArquivamentoProvisorio().getCodEvento(),
						sdf.format(dataRemancescente.getTime()), getSecaoJudiciaria().getCdSecaoJudiciaria());
			} else {
				resultList = estatisticaEventoProcessoManager.listProcessosRemaDisJulArqTramitacao(dataInicioStr,
						dataFimStr, ParametroUtil.instance().getEventoProcessualDistribuicao().getCodEvento(),
						ParametroUtil.instance().getEventoJulgamentoProcessual().getCodEvento(), ParametroUtil
								.instance().getEventoArquivamento().getCodEvento(), ParametroUtil.instance()
								.getEventoArquivamentoDefinitivoProcessual().getCodEvento(), ParametroUtil.instance()
								.getEventoBaixaDefinitivaProcessual().getCodEvento(), ParametroUtil.instance()
								.getEventoArquivamentoProvisorio().getCodEvento(),
						sdf.format(dataRemancescente.getTime()));
			}
			for (int i = 0; i < totalGeralProcessos.length; i++) {
				totalGeralProcessos[i] = 0;
			}
			if (resultList != null && resultList.size() > 0) {
				remanescentesList = new ArrayList<RemanescentesTramitacaoBean>();
				RemanescentesTramitacaoBean bean = new RemanescentesTramitacaoBean();
				remanescentesList.add(bean);
				for (Object[] o : resultList) {
					if (bean.getCodEstado() == null) {
						bean.setCodEstado(o[0].toString());
					} else if (!bean.getCodEstado().equals(o[0].toString())) {
						bean = new RemanescentesTramitacaoBean();
						remanescentesList.add(bean);
						bean.setCodEstado(o[0].toString());
					}
					RemanescentesTramitacaoSubTableBean listBean = new RemanescentesTramitacaoSubTableBean();
					listBean.setVara(getVara(o));
					listBean.setDistribuidos(Long.parseLong(o[3] != null ? o[3].toString() : "0"));
					listBean.setJulgados(Long.parseLong(o[4] != null ? o[4].toString() : "0"));
					listBean.setArquivados(Long.parseLong(o[5] != null ? o[5].toString() : "0"));
					listBean.setTramitacao(Long.parseLong(o[6] != null ? o[6].toString() : "0"));
					listBean.setRemanescentes(Long.parseLong(o[7] != null ? o[7].toString() : "0"));

					bean.getSubList().add(listBean);

					bean.setTotalDistribuidos(bean.getTotalDistribuidos()
							+ Long.parseLong(o[3] == null ? "0" : o[3].toString()));
					bean.setTotalJulgados(bean.getTotalJulgados()
							+ Long.parseLong(o[4] == null ? "0" : o[4].toString()));
					bean.setTotalArquivados(bean.getTotalArquivados()
							+ Long.parseLong(o[5] == null ? "0" : o[5].toString()));
					bean.setTotalTramitacao(bean.getTotalTramitacao()
							+ Long.parseLong(o[6] == null ? "0" : o[6].toString()));
					bean.setTotalRemanescentes(bean.getTotalRemanescentes()
							+ Long.parseLong(o[7] == null ? "0" : o[7].toString()));

					Collections.sort(bean.getSubList(), getVaraComparator());

				}
				for (RemanescentesTramitacaoBean rtb : remanescentesList) {
					totalGeralProcessos[0] += rtb.getTotalRemanescentes();
					totalGeralProcessos[1] += rtb.getTotalDistribuidos();
					totalGeralProcessos[2] += rtb.getTotalJulgados();
					totalGeralProcessos[3] += rtb.getTotalArquivados();
					totalGeralProcessos[4] += rtb.getTotalTramitacao();
				}
				if (!ParametroUtil.instance().isPrimeiroGrau()) {
					List<Object[]> resultListAtualizacao = historicoEstatisticaEventoProcessoManager
							.listSecaoNaoAtualizada();
					if (resultListAtualizacao.size() > 0) {
						for (Object[] objects : resultListAtualizacao) {
							if (getSecaoJudiciaria() != null
									&& getSecaoJudiciaria().getCdSecaoJudiciaria().equals(objects[0].toString())) {
								erro = ("SJ" + objects[0] + "(atualização até o dia " + objects[1] + "). ");
								erroTemp.append(erro);
								break;
							}
							if (getSecaoJudiciaria() == null) {
								erro = ("SJ" + objects[0] + "(atualização até o dia " + objects[1] + "), ");
								erroTemp.append(erro);
							}
						}
					}
					pos = erroTemp.lastIndexOf(",");
					if (pos > 0) {
						erroTemp = new StringBuilder(erroTemp.substring(0, erroTemp.lastIndexOf(",")));
						StringBuilder sb = new StringBuilder();
						sb.append(erroTemp);
						pos = erroTemp.lastIndexOf(",");
						sb.replace(pos, pos + 2, " e ");
						sb.insert(erroTemp.length() + 1, '.');
						erroTemp = sb;
					}
					setErroAtualizarSecao(erroTemp.toString());
					erroTemp = new StringBuilder();
				}
			}
		}
		return remanescentesList;
	}

	@SuppressWarnings("unchecked")
	private Comparator<? super RemanescentesTramitacaoSubTableBean> getVaraComparator() {
		return new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				RemanescentesTramitacaoSubTableBean bean1 = (RemanescentesTramitacaoSubTableBean) o1;
				RemanescentesTramitacaoSubTableBean bean2 = (RemanescentesTramitacaoSubTableBean) o2;
				Integer splitedVara1 = Integer.parseInt(bean1.getVara().split("ª")[0]);
				Integer splitedVara2 = Integer.parseInt(bean2.getVara().split("ª")[0]);
				return splitedVara1.compareTo(splitedVara2);
			}
		};
	}

	public void setRemanescentesList(List<RemanescentesTramitacaoBean> remanescentesList) {
		this.remanescentesList = remanescentesList;
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
				.append(getCompetencia(o[1].toString()));
		return sb.toString();
	}

	/**
	 * obtem a lista de competencias concatenadas com + dentro de um parenteses,
	 * especifico pra exibição do relatório.
	 * 
	 * @param orgaoJulgador
	 * @return lista de competencias concatenadas.
	 */
	private String getCompetencia(String orgaoJulgador) {
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
		remanescentesList = null;
		if (getRemanescentesList() != null && getRemanescentesList().size() > 0) {
			RelatorioLog relatorioLog = new RelatorioLog();
			relatorioLog.setDataSolicitacao(new Date());
			relatorioLog.setDescricao("Estatística de Proc. Distribuido - Rema., Arqui., Jul. e em Tram.");
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
			if (remanescentesList.size() > 0) {
				exportarXLS(TEMPLATE_XLS_PATH, "Distribuidos.xls", "remanescentesList", remanescentesList);
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
		map.put("totalGeralRemanescentes", totalGeralProcessos[0]);
		map.put("totalGeralDistribuidos", totalGeralProcessos[1]);
		map.put("totalGeralJulgados", totalGeralProcessos[2]);
		map.put("totalGeralArquivados", totalGeralProcessos[3]);
		map.put("totalGeralTramitacao", totalGeralProcessos[4]);
		if (dataInicio != null && dataFim != null) {
			String dataFormatada = "";
			SimpleDateFormat formatoInicio = new SimpleDateFormat("MM/yyyy");
			dataFormatada = formatoInicio.format(dataInicio);
			map.put("dataInicio", dataFormatada);
			SimpleDateFormat formatoFim = new SimpleDateFormat("MM/yyyy");
			dataFormatada = formatoFim.format(dataFim);
			map.put("dataFim", dataFormatada);
		}
		if (!erroAtualizarSecao.isEmpty()) {
			map.put("erroAtualizarSecao", "Dados não computados na(s) Seção(ões): " + erroAtualizarSecao);
		}

		map.put("titulo", "ESTATÍSTICA DE PROCESSOS REMANESCENTES, DISTRIBUÍDOS, JULGADOS, ARQUIVADOS E EM TRAMITAÇÃO");
		map.put("subNomeSistema", ParametroUtil.getParametro("nomeSecaoJudiciaria").toUpperCase());
		map.put("nomeSistema", ParametroUtil.getParametro("nomeSistema"));
		map.put("secao", secaoJudiciaria == null ? "Todas" : secaoJudiciaria);
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

	public void limparFiltros() {
		if (!ParametroUtil.instance().isPrimeiroGrau()) {
			erroAtualizarSecao = "";
			secaoJudiciaria = null;
		}
		dataFim = null;
		dataFimStr = null;
		dataInicio = null;
		dataInicioStr = null;
	}

	/*
	 * Inicio - Getters and Setters
	 */
	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	public Date getDataInicio() {
		if (getDataInicioStr() != null) {
			String[] data = getDataInicioStr().split("/");
			int mes = Integer.valueOf(data[0]);
			int ano = Integer.valueOf(data[1]);
			Calendar c = Calendar.getInstance();
			c.set(ano, mes - 1, 1);
			dataInicio = c.getTime();
		}
		return dataInicio;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = dataFim;
	}

	public Date getDataFim() {
		if (getDataFimStr() != null) {
			String[] data = getDataFimStr().split("/");
			int mes = Integer.valueOf(data[0]);
			int ano = Integer.valueOf(data[1]);
			Calendar c = Calendar.getInstance();
			c.set(ano, mes, 0);
			dataFim = c.getTime();
		}
		return dataFim;
	}

	public String getDataInicioStr() {
		return dataInicioStr;
	}

	public void setDataInicioStr(String dataInicioStr) {
		this.dataInicioStr = dataInicioStr;
	}

	public String getDataFimStr() {
		return dataFimStr;
	}

	public void setDataFimStr(String dataFimStr) {
		this.dataFimStr = dataFimStr;
	}

	public SecaoJudiciaria getSecaoJudiciaria() {
		return secaoJudiciaria;
	}

	public void setSecaoJudiciaria(SecaoJudiciaria secaoJudiciaria) {
		this.secaoJudiciaria = secaoJudiciaria;
	}

	public long[] getTotalGeralProcessos() {
		return totalGeralProcessos;
	}

	public void setTotalGeralProcessos(long[] totalGeralProcessos) {
		this.totalGeralProcessos = totalGeralProcessos;
	}

	public String getErroAtualizarSecao() {
		return erroAtualizarSecao;
	}

	public void setErroAtualizarSecao(String erroAtualizarSecao) {
		this.erroAtualizarSecao = erroAtualizarSecao;
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

	private List<SecaoJudiciaria> secaoJudiciariaItems() {
		return secaoJudiciariaManager.secaoJudiciariaItems();
	}

	public void secaoJudiciaria1Grau() {
		secaoJudiciaria = secaoJudiciariaManager.secaoJudiciaria1Grau();
	}

	/*
	 * Fim - Getters and Setters
	 */

}