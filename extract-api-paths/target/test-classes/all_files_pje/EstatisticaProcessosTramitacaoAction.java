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
import br.com.infox.pje.bean.EstatisticaProcessosTramitacaoBean;
import br.com.infox.pje.bean.EstatisticaProcessosTramitacaoSubTableBean;
import br.com.infox.pje.list.EstatisticaProcessosTramitacaoList;
import br.com.infox.pje.manager.EstatisticaEventoProcessoManager;
import br.com.infox.pje.manager.HistoricoEstatisticaEventoProcessoManager;
import br.com.infox.pje.manager.SecaoJudiciariaManager;
import br.com.itx.component.FileHome;
import br.com.itx.component.Util;
import br.jus.pje.nucleo.entidades.RelatorioLog;
import br.jus.pje.nucleo.entidades.SecaoJudiciaria;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * Classe action controladora do listView de /EstatisticaProcesso/
 * ProcessosTramitacao/
 * 
 * @author Rafael
 * 
 */
@Name(value = EstatisticaProcessosTramitacaoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class EstatisticaProcessosTramitacaoAction implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7311811453740316187L;

	public static final String NAME = "estatisticaProcessosTramitacaoAction";

	private EstatisticaProcessosTramitacaoList processosTramitacaoList = new EstatisticaProcessosTramitacaoList();
	private static final String TEMPLATE_XLS_PATH = "/EstatisticaProcesso/ProcessosTramitacao/processosTramitacaoTemplate.xls";

	private String dataInicio;
	private String dataFim;
	private String dataInicioFormatada;
	private String dataFimFormatada;
	private String msgErroAtualizacaoSecao = "";
	private SecaoJudiciaria secaoJudiciaria;
	private List<EstatisticaProcessosTramitacaoBean> tramitacaoBeanList;
	private List<SecaoJudiciaria> secaoJudiciariaList;
	private long[] totalGeralMes = { 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L };
	@In
	private SecaoJudiciariaManager secaoJudiciariaManager;
	@In
	private EstatisticaEventoProcessoManager estatisticaEventoProcessoManager;
	@In
	private HistoricoEstatisticaEventoProcessoManager historicoEstatisticaEventoProcessoManager;
	@In
	private GenericManager genericManager;

	public List<EstatisticaProcessosTramitacaoBean> estatisticaDistribuidosArquivadosList() {
		if (tramitacaoBeanList == null) {
			dataInicioFormatada = formatarAnoMes(dataInicio);
			dataFimFormatada = formatarAnoMes(dataFim);
			for (int i = 0; i < totalGeralMes.length; i++) {
				totalGeralMes[i] = 0;
			}
			msgErroAtualizacaoSecao = "";
			tramitacaoBeanList = buildProcessosTramitacaoList();
			for (EstatisticaProcessosTramitacaoBean obj : tramitacaoBeanList) {
				for (int i = 0; i < 12; i++) {
					totalGeralMes[(i)] = totalGeralMes[(i)] + obj.getTotalMes()[(i)];
				}
			}
		}
		return tramitacaoBeanList;
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

	private List<EstatisticaProcessosTramitacaoBean> buildProcessosTramitacaoList() {
		List<Map<String, Object>> mapList = getProcessosTramitacaoList().getResultList();
		List<EstatisticaProcessosTramitacaoBean> processosTramitacaoList = new ArrayList<EstatisticaProcessosTramitacaoBean>();
		EstatisticaProcessosTramitacaoBean bean = new EstatisticaProcessosTramitacaoBean();
		for (Map<String, Object> map : mapList) {
			if (bean.getCodEstado() == null) {
				bean.setCodEstado(map.get("secao").toString());
				bean.setTotalGeral(map.get("numProcess").toString());
				if (!ParametroUtil.instance().isPrimeiroGrau()) {
					if (historicoEstatisticaEventoProcessoManager.getDataAtualizacaoSessao(bean.getCodEstado()) != null) {
						msgErroAtualizacaoSecao = "SJ"
								+ bean.getCodEstado()
								+ " (atualizado até o dia "
								+ historicoEstatisticaEventoProcessoManager.getDataAtualizacaoSessao(bean
										.getCodEstado()) + ")";
					}
				}
			} else if (!bean.getCodEstado().equals(map.get("secao").toString())) {
				bean = new EstatisticaProcessosTramitacaoBean();
				bean.setCodEstado(map.get("secao").toString());
				bean.setTotalGeral(map.get("numProcess").toString());
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

			bean.setSubList(estatisticaEventoProcessoManager.buscaListaVarasSecaoTramitacao(bean.getCodEstado(),
					dataInicioFormatada, dataFimFormatada));

			for (int i = 0; i < 12; i++) {
				bean.getTotalMes()[i] = 0L;
			}
			for (int i = 0; i < bean.getSubList().size(); i++) {
				String vara = bean.getSubList().get(i).getVara();
				String jurisd = bean.getSubList().get(i).getJurisdicao();
				String o = getVara(vara, jurisd);
				bean.getSubList().get(i).setVara(o);
			}

			for (EstatisticaProcessosTramitacaoSubTableBean obj : bean.getSubList()) {
				for (Integer chave : obj.getQtPorMes().keySet()) {
					bean.getTotalMes()[(chave - 1)] = bean.getTotalMes()[(chave - 1)] + obj.getQtPorMes().get(chave);
				}
			}
			List<EstatisticaProcessosTramitacaoSubTableBean> lista = new ArrayList<EstatisticaProcessosTramitacaoSubTableBean>();
			lista.addAll(bean.getSubList());
			bean.getSubList().clear();
			bean.getSubList().addAll(ordenarLista(lista));
			processosTramitacaoList.add(bean);
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
		return processosTramitacaoList;
	}

	public EstatisticaProcessosTramitacaoList getProcessosTramitacaoList() {
		return processosTramitacaoList;
	}

	public void setProcessosTramitacaoList(EstatisticaProcessosTramitacaoList processosTramitacaoList) {
		this.processosTramitacaoList = processosTramitacaoList;
	}

	public List<EstatisticaProcessosTramitacaoBean> getTramitacaoBeanList() {
		return tramitacaoBeanList;
	}

	public void setTramitacaoBeanList(List<EstatisticaProcessosTramitacaoBean> tramitacaoBeanList) {
		this.tramitacaoBeanList = tramitacaoBeanList;
	}

	/**
	 * Formata a vara para a exibição no relatório do ranking da seção
	 * 
	 * @param o
	 *            , sendo [1] = descrção do orgão julgador e [2] = descricao da
	 *            jurisdição
	 * @return
	 */
	private String getVara(String vara, String jurisdicao) {
		StringBuilder sb = new StringBuilder();
		sb.append(StringUtil.limparCharsNaoNumericos(vara)).append("ª - " + jurisdicao).append(" ")
				.append(getCompetenciaList(vara));
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
			} else {
				sb.append("");
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
		tramitacaoBeanList = null;
		if (estatisticaDistribuidosArquivadosList() != null && estatisticaDistribuidosArquivadosList().size() > 0) {
			RelatorioLog relatorioLog = new RelatorioLog();
			relatorioLog.setDataSolicitacao(new Date());
			relatorioLog.setDescricao("Estatística de Processos em Tramitação");
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
	public void exportarProcessosTramitacaoXLS() {
		try {
			if (tramitacaoBeanList.size() > 0) {
				exportarXLS(TEMPLATE_XLS_PATH, "processosTramitacao.xls", "tramitacaoBeanList", tramitacaoBeanList);
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
		if (dataInicio != null && dataFim != null) {
			map.put("dataInicio", dataInicio);
			map.put("dataFim", dataFim);
		}
		if (!msgErroAtualizacaoSecao.isEmpty()) {
			map.put("msgErroAtualizacaoSecao", "Dados não computados na(s) Seção(ões): " + msgErroAtualizacaoSecao);
		}
		map.put("secao", secaoJudiciaria == null ? "Todas" : secaoJudiciaria);
		map.put("titulo", "ESTATÍSTICA DE PROCESSOS EM TRAMITAÇÃO");
		map.put("subNomeSistema", ParametroUtil.getParametro("nomeSecaoJudiciaria").toUpperCase());
		map.put("nomeSistema", ParametroUtil.getParametro("nomeSistema"));
		for (int x = 0; x < totalGeralMes.length; x++) {
			map.put("totMes" + DateUtil.getMesExtenso(x + 1), totalGeralMes[x]);
		}

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

	public List<EstatisticaProcessosTramitacaoSubTableBean> ordenarLista(
			List<EstatisticaProcessosTramitacaoSubTableBean> lista) {
		List<Integer> lista1 = new ArrayList<Integer>();

		// pega todos os números das varas e coloca em uma lista para ordenação
		// posterior
		for (EstatisticaProcessosTramitacaoSubTableBean o : lista) {
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
		List<EstatisticaProcessosTramitacaoSubTableBean> lista3 = new ArrayList<EstatisticaProcessosTramitacaoSubTableBean>();
		for (Integer u : lista1) {
			for (EstatisticaProcessosTramitacaoSubTableBean o : lista) {
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
		for (EstatisticaProcessosTramitacaoSubTableBean o : lista) {
			if (!lista3.contains(o)) {
				lista3.add(o);
			}
		}

		return lista3;
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

	public void setTotalGeralMes(long[] totalGeralMes) {
		this.totalGeralMes = totalGeralMes;
	}

	public long[] getTotalGeralMes() {
		return totalGeralMes;
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

	public void secaoJudiciaria1Grau() {
		secaoJudiciaria = secaoJudiciariaManager.secaoJudiciaria1Grau();
	}

	/*
	 * Fim - Getters and Setters
	 */

	public void limparFiltros() {
		if (!ParametroUtil.instance().isPrimeiroGrau()) {
			msgErroAtualizacaoSecao = "";
			secaoJudiciaria = null;
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