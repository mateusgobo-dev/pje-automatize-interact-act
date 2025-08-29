package br.com.infox.pje.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
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
import br.com.infox.pje.bean.EstatisticaMapaDistribuicaoVarasBean;
import br.com.infox.pje.bean.MapaDistribuicaoVarasListBean;
import br.com.infox.pje.list.EstatisticaMapaDistribuicaoVarasList;
import br.com.infox.pje.manager.EstatisticaEventoProcessoManager;
import br.com.infox.pje.manager.HistoricoEstatisticaEventoProcessoManager;
import br.com.itx.component.Util;
import br.com.itx.exception.ExcelExportException;
import br.com.itx.util.ExcelExportUtil;
import br.jus.pje.nucleo.entidades.RelatorioLog;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * Classe action controladora do listView de /EstatisticaProcesso/
 * MapaDistribuicaoVaras/
 * 
 * @author Rafael Fernandes
 * 
 */
@Name(value = EstatisticaMapaDistribuicaoVarasAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class EstatisticaMapaDistribuicaoVarasAction implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7684417995252947277L;

	public static final String NAME = "estatisticaMapaDistribuicaoVarasAction";

	private EstatisticaMapaDistribuicaoVarasList mapaDistribuicaoList = new EstatisticaMapaDistribuicaoVarasList();
	private static final String TEMPLATE_XLS_PATH = "/EstatisticaProcesso/MapaDistribuicaoVaras/mapaDistribuicaoVarasTemplate.xls";
	private static final String DOWNLOAD_XLS_NAME = "MapaDistribuicaoVaras.xls";

	private String dataMesAno;
	private String dataMesAnoFormatado;
	private int totalVaras;
	private List<EstatisticaMapaDistribuicaoVarasBean> estMapaDistVaraBeanList;
	private String codEstado;
	private String msgErroAtualizacaoSecao = "";
	@In
	private EstatisticaEventoProcessoManager estatisticaEventoProcessoManager;
	@In
	private HistoricoEstatisticaEventoProcessoManager historicoEstatisticaEventoProcessoManager;
	@In
	private GenericManager genericManager;

	/**
	 * Obtem o relatório da estatística convertendo a lista proveniente do banco
	 * para uma lista no esquema da exibição necessária para a tela utilizando o
	 * EstatisticaRankingSecaoBean.java
	 * 
	 * @return lista com os dados para o ranking
	 */
	public List<EstatisticaMapaDistribuicaoVarasBean> estatisticaMapaDistribuicaoVarasList() {
		if (getEstMapaDistVaraBeanList() == null) {
			dataMesAnoFormatado = formatarAnoMes(dataMesAno);
			setEstMapaDistVaraBeanList(new ArrayList<EstatisticaMapaDistribuicaoVarasBean>());
			msgErroAtualizacaoSecao = "";
			List<Object[]> resultList = getMapaDistribuicaoList().getResultList();
			totalVaras = 0;
			if (resultList != null && resultList.size() > 0) {
				EstatisticaMapaDistribuicaoVarasBean mdvb = new EstatisticaMapaDistribuicaoVarasBean();
				getEstMapaDistVaraBeanList().add(mdvb);
				for (Object[] o : resultList) {
					totalVaras++;
					if (mdvb.getCodEstado() == null) {
						mdvb.setCodEstado(o[0].toString());
						if (!ParametroUtil.instance().isPrimeiroGrau()) {
							if (historicoEstatisticaEventoProcessoManager.getDataAtualizacaoSessao(mdvb.getCodEstado()) != null) {
								msgErroAtualizacaoSecao = "SJ"
										+ mdvb.getCodEstado()
										+ " (atualizado até o dia "
										+ historicoEstatisticaEventoProcessoManager.getDataAtualizacaoSessao(mdvb
												.getCodEstado()) + ")";
							}
						}
					} else if (!mdvb.getCodEstado().equals(o[0].toString())) {
						mdvb = new EstatisticaMapaDistribuicaoVarasBean();
						getEstMapaDistVaraBeanList().add(mdvb);
						mdvb.setCodEstado(o[0].toString());
						if (!ParametroUtil.instance().isPrimeiroGrau()) {
							if (historicoEstatisticaEventoProcessoManager.getDataAtualizacaoSessao(mdvb.getCodEstado()) != null) {
								if (msgErroAtualizacaoSecao.length() > 0) {
									msgErroAtualizacaoSecao += ", SJ"
											+ mdvb.getCodEstado()
											+ " (atualizado até o dia "
											+ historicoEstatisticaEventoProcessoManager.getDataAtualizacaoSessao(mdvb
													.getCodEstado()) + ")";
								}
							}
						}
					}
					Integer varasEstado = mdvb.getTotalVarasEstado();
					mdvb.setTotalVarasEstado(varasEstado == null ? 1 : varasEstado + 1);
					MapaDistribuicaoVarasListBean erlb = new MapaDistribuicaoVarasListBean();
					erlb.setVaras(getVaraFormatada(o[1].toString(), o[2].toString()));
					mdvb.getMapaDistribuicaoVarasListBean().add(erlb);
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
			for (EstatisticaMapaDistribuicaoVarasBean bean : getEstMapaDistVaraBeanList()) {
				List<MapaDistribuicaoVarasListBean> lista = new ArrayList<MapaDistribuicaoVarasListBean>();
				lista.addAll(bean.getMapaDistribuicaoVarasListBean());
				bean.getMapaDistribuicaoVarasListBean().clear();
				bean.getMapaDistribuicaoVarasListBean().addAll(ordenarVaras(lista));
			}

		}
		return getEstMapaDistVaraBeanList();
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
		setEstMapaDistVaraBeanList(null);
		if (estatisticaMapaDistribuicaoVarasList().size() > 0) {
			RelatorioLog relatorioLog = new RelatorioLog();
			relatorioLog.setDataSolicitacao(new Date());
			relatorioLog.setDescricao("Mapa de Distribuíção das Varas");
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
	public void exportarMapaDistribuicaoVarasXLS() {
		try {
			if (getEstMapaDistVaraBeanList().size() > 0) {
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
		map.put("estMapaDistVaraBeanList", getEstMapaDistVaraBeanList());
		map.put("totalVaras", totalVaras);
		if (dataMesAno != null) {
			map.put("dataMesAno", dataMesAno);
		}
		if (!msgErroAtualizacaoSecao.isEmpty()) {
			map.put("msgErroAtualizacaoSecao", "Dados não computados na(s) Seção(ões): " + msgErroAtualizacaoSecao);
		}
		map.put("titulo", "MAPA DE DISTRIBUIÇÃO DAS VARAS");
		map.put("subNomeSistema", ParametroUtil.getParametro("nomeSecaoJudiciaria").toUpperCase());
		map.put("nomeSistema", ParametroUtil.getParametro("nomeSistema"));
		map.put("totalVaras", totalVaras);
		return map;
	}

	public List<MapaDistribuicaoVarasListBean> ordenarVaras(List<MapaDistribuicaoVarasListBean> lista) {
		List<Integer> lista1 = new ArrayList<Integer>();

		// pega todos os números das varas e coloca em uma lista para ordenação
		// posterior
		for (MapaDistribuicaoVarasListBean o : lista) {
			String vara = o.getVaras();
			if (vara.indexOf("ª") > 0) {
				CharSequence subSequence = vara.subSequence(0, vara.indexOf("ª"));
				lista1.add(Integer.valueOf(subSequence.toString().trim()));
			}
		}

		// ordena as varas da lista de forma crescente
		Collections.sort(lista1);

		// cria a lista de retorno ao usuário ordenado de acordo com o número da
		// vara
		List<MapaDistribuicaoVarasListBean> lista3 = new ArrayList<MapaDistribuicaoVarasListBean>();
		for (Integer u : lista1) {
			for (MapaDistribuicaoVarasListBean o : lista) {
				String varas = o.getVaras();
				CharSequence subSequence = varas.subSequence(0, varas.indexOf("ª"));
				if (varas.indexOf("ª") > 0) {
					if (Integer.valueOf(subSequence.toString().trim()).equals(u) && !lista3.contains(o)) {
						lista3.add(o);
					}
				}
			}
		}

		// adiciona as varas que não tem número
		for (MapaDistribuicaoVarasListBean o : lista) {
			if (!lista3.contains(o)) {
				lista3.add(o);
			}
		}

		return lista3;
	}

	/*
	 * Inicio - Getters and Setters
	 */
	public String getDataMesAno() {
		return dataMesAno;
	}

	public void setDataMesAno(String dataMesAno) {
		this.dataMesAno = dataMesAno;
	}

	public void setTotalVaras(int totalVaras) {
		this.totalVaras = totalVaras;
	}

	public int getTotalVaras() {
		return totalVaras;
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

	public void setMapaDistribuicaoList(EstatisticaMapaDistribuicaoVarasList mapaDistribuicaoList) {
		this.mapaDistribuicaoList = mapaDistribuicaoList;
	}

	public EstatisticaMapaDistribuicaoVarasList getMapaDistribuicaoList() {
		return mapaDistribuicaoList;
	}

	/*
	 * Fim - Getters and Setters
	 */
	public void limpaFiltros() {
		if (!ParametroUtil.instance().isPrimeiroGrau()) {
			msgErroAtualizacaoSecao = "";
		}
		dataMesAno = null;
	}

	public void setEstMapaDistVaraBeanList(List<EstatisticaMapaDistribuicaoVarasBean> estMapaDistVaraBeanList) {
		this.estMapaDistVaraBeanList = estMapaDistVaraBeanList;
	}

	public List<EstatisticaMapaDistribuicaoVarasBean> getEstMapaDistVaraBeanList() {
		return estMapaDistVaraBeanList;
	}

	public String getDataMesAnoFormatado() {
		return dataMesAnoFormatado;
	}

	public void setDataMesAnoFormatado(String dataMesAnoFormatado) {
		this.dataMesAnoFormatado = dataMesAnoFormatado;
	}

}