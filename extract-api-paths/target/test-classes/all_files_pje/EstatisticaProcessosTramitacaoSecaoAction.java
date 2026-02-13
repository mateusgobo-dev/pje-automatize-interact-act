package br.com.infox.pje.action;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.manager.GenericManager;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.bean.EstatisticaProcessosTramitacaoSecaoBean;
import br.com.infox.pje.bean.EstatisticaProcessosTramitacaoSecaoListBean;
import br.com.infox.pje.list.EstatisticaProcessosTramitacaoSecaoList;
import br.com.infox.pje.manager.EstatisticaEventoProcessoManager;
import br.com.infox.pje.manager.SecaoJudiciariaManager;
import br.com.itx.component.Util;
import br.com.itx.exception.ExcelExportException;
import br.com.itx.util.ExcelExportUtil;
import br.jus.pje.nucleo.entidades.RelatorioLog;
import br.jus.pje.nucleo.entidades.SecaoJudiciaria;
import br.jus.pje.nucleo.entidades.Usuario;

/**
 * Classe action controladora do listView de /EstatisticaProcesso/
 * EstatisticaProcessosTramitacaoSecao/
 * 
 * @author Allan
 * 
 */
@Name(value = EstatisticaProcessosTramitacaoSecaoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class EstatisticaProcessosTramitacaoSecaoAction implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 468189708667664833L;

	public static final String NAME = "estatisticaProcessosTramitacaoSecaoAction";

	private EstatisticaProcessosTramitacaoSecaoList estatisticaProcessosTramitacaoSecaoList = new EstatisticaProcessosTramitacaoSecaoList();
	private static final String TEMPLATE_SINTETICO_XLS_PATH = "/EstatisticaProcesso/EstatisticaProcessosTramitacaoSecao/relatorioSinteticoTemplate.xls";
	private static final String TEMPLATE_ANALITICO_XLS_PATH = "/EstatisticaProcesso/EstatisticaProcessosTramitacaoSecao/relatorioAnaliticoTemplate.xls";
	private static final String DOWNLOAD_XLS_NAME = "EstatisticaTramitacaoSecao.xls";

	private Date data;
	private boolean relatorioSintetico = true;
	private boolean dataDistribuicao = true;
	private boolean processosJulgados = true;
	private boolean tipoApelacao = false;
	private boolean remessa = false;
	private int totalRegiao;
	private int totalJulgadosRegiao;
	private SecaoJudiciaria secaoJudiciaria;
	private List<SecaoJudiciaria> secaoJudiciariaList;
	private List<EstatisticaProcessosTramitacaoSecaoBean> estatisticaProcessosTramitacaoSecaoBeanList;
	@In
	private EstatisticaEventoProcessoManager estatisticaEventoProcessoManager;
	@In
	private GenericManager genericManager;
	@In
	private SecaoJudiciariaManager secaoJudiciariaManager;

	/**
	 * Obtem o relatório da estatística convertendo a lista proveniente do banco
	 * para uma lista no esquema da exibição necessária para a tela utilizando o
	 * MapaProdutividadeSecaoJudiciariaBean.java
	 * 
	 * @return lista com os dados para a produtividade
	 */
	public List<EstatisticaProcessosTramitacaoSecaoBean> estatisticaProcessosTramitacaoSecaoBeanList() {
		if (estatisticaProcessosTramitacaoSecaoBeanList == null) {
			totalRegiao = 0;
			totalJulgadosRegiao = 0;
			estatisticaProcessosTramitacaoSecaoBeanList = new ArrayList<EstatisticaProcessosTramitacaoSecaoBean>();
			List<Object[]> resultList = getEstatisticaProcessosTramitacaoSecaoList().getResultList();
			if (resultList != null && resultList.size() > 0) {
				EstatisticaProcessosTramitacaoSecaoBean estProcesso = new EstatisticaProcessosTramitacaoSecaoBean();
				estatisticaProcessosTramitacaoSecaoBeanList.add(estProcesso);
				for (Object[] o : resultList) {
					String codEstado = o[0].toString();
					if (estProcesso.getCodEstado() == null) {
						estProcesso.setCodEstado(codEstado);
					} else if (!estProcesso.getCodEstado().equals(codEstado)) {
						estProcesso = new EstatisticaProcessosTramitacaoSecaoBean();
						estatisticaProcessosTramitacaoSecaoBeanList.add(estProcesso);
						estProcesso.setCodEstado(codEstado);
					}
					EstatisticaProcessosTramitacaoSecaoListBean listBean = new EstatisticaProcessosTramitacaoSecaoListBean();
					String orgaoJulgador = o[1].toString();
					listBean.setLocalizacao(orgaoJulgador);
					listBean.setTotal(Integer.parseInt(o[2].toString()));
					if (relatorioSintetico && processosJulgados) {
						listBean.setJulgados(estatisticaEventoProcessoManager
								.quantidadeProcessosJulgadosByEstadoOrgaoJulgador(data, codEstado, orgaoJulgador));
					} else if (!relatorioSintetico) {
						listBean.setEstatisticaProcTramitacaoListBean(estatisticaEventoProcessoManager
								.listDadosProcessosByEstadoOrgaoJulgador(data, codEstado, orgaoJulgador));
					}

					if (!relatorioSintetico) {
						listBean.setTotal(listBean.getEstatisticaProcTramitacaoListBean().size());
					}
					estProcesso.getEstatisticaProcessosTramitacaoSecaoListBean().add(listBean);
				}
			}
		}

		// somatório de totais
		totalRegiao = 0;
		totalJulgadosRegiao = 0;
		for (EstatisticaProcessosTramitacaoSecaoBean b : estatisticaProcessosTramitacaoSecaoBeanList) {
			for (EstatisticaProcessosTramitacaoSecaoListBean c : b.getEstatisticaProcessosTramitacaoSecaoListBean()) {
				totalRegiao += c.getTotal();
				if (!relatorioSintetico) {
					totalJulgadosRegiao += c.getTotalJulgados();
				} else {
					totalJulgadosRegiao += c.getJulgados();
				}
			}
		}

		return estatisticaProcessosTramitacaoSecaoBeanList;
	}

	public int quantidadeColSpanDinamicaAnalitico() {
		int span = 3;
		if (dataDistribuicao) {
			span++;
		}

		if (tipoApelacao) {
			span++;
		}

		if (remessa) {
			span++;
		}

		return span;
	}

	/**
	 * Método que grava o log das consultas de relarório caso a consulta retorne
	 * registros.
	 */
	public void gravarLogRelatorio() {
		estatisticaProcessosTramitacaoSecaoBeanList = null;
		if (estatisticaProcessosTramitacaoSecaoBeanList().size() > 0) {
			RelatorioLog relatorioLog = new RelatorioLog();
			relatorioLog.setDataSolicitacao(new Date());
			relatorioLog.setDescricao("Processos em Tramitação nas Seções da 5ª Região por seção e localização");
			Usuario usuario = Authenticator.getUsuarioLogado();
			relatorioLog.setIdUsuarioSolicitacao(usuario);
			genericManager.persist(relatorioLog);
		}
	}

	public int getRowspanTotalRelatorio() {
		int rowspan = 4;
		if (dataDistribuicao) {
			rowspan += 1;
		}
		if (tipoApelacao) {
			rowspan += 1;
		}
		if (remessa) {
			rowspan += 1;
		}
		return rowspan;
	}

	/**
	 * Método que exporta o resultado da consulta para excel, caso a consulta
	 * retorne registros
	 */
	public void exportarEstatisticaProcessosTramitacaoSecaoXLS() {
		try {
			if (estatisticaProcessosTramitacaoSecaoBeanList.size() > 0) {
				exportarXLS(relatorioSintetico ? TEMPLATE_SINTETICO_XLS_PATH : TEMPLATE_ANALITICO_XLS_PATH,
						DOWNLOAD_XLS_NAME);
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
	 * @throws ExcelExportException
	 */
	public void exportarXLS(String dirNomeTemplate, String nomeArqDown) throws ExcelExportException {
		String urlTemplate = new Util().getContextRealPath() + dirNomeTemplate;
		ExcelExportUtil util = new ExcelExportUtil(urlTemplate, nomeArqDown);
		util.setBean(beanExportarXLS());
		util.setColumnsToHide(hideColumn());
		util.download();
	}

	/**
	 * define quais colunas devem aparecer no relatório da planilha eletrônica
	 * 
	 * @return array com as colunas a serem removidas
	 */
	private short[] hideColumn() {
		short[] columns = new short[] { -1, -1, -1, -1 };
		if (!relatorioSintetico) {
			if (!dataDistribuicao) {
				columns[0] = 2;
			}

			if (!tipoApelacao) {
				columns[1] = 3;
			}

			if (!remessa) {
				columns[2] = 4;
			}
		}

		if (!processosJulgados) {
			columns[3] = (short) (!relatorioSintetico ? 5 : 1);
		}

		return columns;
	}

	private Map<String, Object> beanExportarXLS() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("estatisticaTramitacaoSecaoBeanList", estatisticaProcessosTramitacaoSecaoBeanList);
		map.put("totalRegiao", totalRegiao);
		map.put("totalJulgadosRegiao", totalJulgadosRegiao);
		map.put("secao", secaoJudiciaria == null ? "Todas" : secaoJudiciaria.getSecaoJudiciaria());
		if (data != null) {
			map.put("data", new SimpleDateFormat("dd/MM/yyyy").format(data));
		}
		map.put("titulo", "PROCESSOS EM TRAMITAÇÃO NAS SEÇÕES DA 5ª REGIÃO");
		map.put("subNomeSistema", ParametroUtil.getParametro("nomeSecaoJudiciaria").toUpperCase());
		map.put("nomeSistema", ParametroUtil.getParametro("nomeSistema"));
		map.put("relatorio", relatorioSintetico ? "Sintético" : "Analítico");
		map.put("totalRegiaoJulgados", totalJulgadosRegiao);
		map.put("horaAtual", "Emitido em: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));

		return map;
	}

	public void limparLista() {
		// limpa a lista para uma nova pesquisa
		if (!ParametroUtil.instance().isPrimeiroGrau()) {
			secaoJudiciaria = null;
		}
		estatisticaProcessosTramitacaoSecaoBeanList = null;
		data = null;
		processosJulgados = true;
		dataDistribuicao = true;
		processosJulgados = true;
		tipoApelacao = false;
		remessa = false;
	}

	public void limparCampos() {
		relatorioSintetico = true;
	}

	/*
	 * Inicio - Getters and Setters
	 */
	public void setData(Date data) {
		this.data = data;
	}

	public Date getData() {
		return data;
	}

	public void setEstatisticaProcessosTramitacaoSecaoList(
			EstatisticaProcessosTramitacaoSecaoList estatisticaProcessosTramitacaoSecaoList) {
		this.estatisticaProcessosTramitacaoSecaoList = estatisticaProcessosTramitacaoSecaoList;
	}

	public EstatisticaProcessosTramitacaoSecaoList getEstatisticaProcessosTramitacaoSecaoList() {
		return estatisticaProcessosTramitacaoSecaoList;
	}

	public void setRelatorioSintetico(boolean relatorioSintetico) {
		this.relatorioSintetico = relatorioSintetico;
	}

	public boolean isRelatorioSintetico() {
		return relatorioSintetico;
	}

	public void setDataDistribuicao(boolean dataDistribuicao) {
		this.dataDistribuicao = dataDistribuicao;
	}

	public boolean isDataDistribuicao() {
		return dataDistribuicao;
	}

	public void setProcessosJulgados(boolean processosJulgados) {
		this.processosJulgados = processosJulgados;
	}

	public boolean isProcessosJulgados() {
		return processosJulgados;
	}

	public void setTipoApelacao(boolean tipoApelacao) {
		this.tipoApelacao = tipoApelacao;
	}

	public boolean isTipoApelacao() {
		return tipoApelacao;
	}

	public void setRemessa(boolean remessa) {
		this.remessa = remessa;
	}

	public boolean isRemessa() {
		return remessa;
	}

	public void setTotalJulgadosRegiao(int totalJulgadosRegiao) {
		this.totalJulgadosRegiao = totalJulgadosRegiao;
	}

	public int getTotalJulgadosRegiao() {
		return totalJulgadosRegiao;
	}

	public void setTotalRegiao(int totalRegiao) {
		this.totalRegiao = totalRegiao;
	}

	public int getTotalRegiao() {
		return totalRegiao;
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
}