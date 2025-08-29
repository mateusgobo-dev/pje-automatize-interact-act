package br.com.infox.pje.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;

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
import br.com.infox.pje.bean.MapaDeProdutividadeDaSecaoJudiciariaBean;
import br.com.infox.pje.bean.MapaDeProdutividadeDaSecaoJudiciariaListBean;
import br.com.infox.pje.list.MapaDeProdutividadeDaSecaoJudiciariaList;
import br.com.infox.pje.manager.EstatisticaEventoProcessoManager;
import br.com.infox.pje.manager.HistoricoEstatisticaEventoProcessoManager;
import br.com.itx.component.FileHome;
import br.com.itx.component.Util;
import br.jus.pje.nucleo.entidades.RelatorioLog;
import br.jus.pje.nucleo.entidades.Usuario;

/**
 * 
 * @author Eldson
 * 
 */
@Name(value = MapaDeProdutividadeDaSecaoJudiciariaAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class MapaDeProdutividadeDaSecaoJudiciariaAction implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "mapaDeProdutividadeDaSecaoJudiciariaAction";

	private MapaDeProdutividadeDaSecaoJudiciariaList mapaDeProdutividadeDaSecaoJudiciariaList = new MapaDeProdutividadeDaSecaoJudiciariaList();
	private static final String TEMPLATE_XLS_PATH = "/EstatisticaProcesso/MapaDeProdutividadeDaSecaoJudiciaria/mapaDeProdutividadeDaSecaoJudiciariaTemplate.xls";

	private Date dataInicio;
	private Date dataFim;
	private String dataInicioStr;
	private String dataFimStr;
	private List<MapaDeProdutividadeDaSecaoJudiciariaBean> estatisticaBeanList;
	private List<SelectItem> competenciaList;
	private String competencia;
	private String erroAtualizarSecao = "";
	@In
	private EstatisticaEventoProcessoManager estatisticaEventoProcessoManager;
	@In
	private GenericManager genericManager;
	@In
	private HistoricoEstatisticaEventoProcessoManager historicoEstatisticaEventoProcessoManager;

	/**
	 * Obtem o relatório da estatística convertendo a lista proveniente do banco
	 * para uma lista no esquema da exibição necessária para a tela utilizando o
	 * MapaDeProdutividadeDaSecaoJudiciariaBean.java
	 * 
	 * @return lista com os dados para do mapa da produtividade
	 */
	public List<MapaDeProdutividadeDaSecaoJudiciariaBean> estatisticaList() {
		String erro = "";
		StringBuilder erroTemp = new StringBuilder();
		if (estatisticaBeanList == null) {
			estatisticaBeanList = new ArrayList<MapaDeProdutividadeDaSecaoJudiciariaBean>();
			erroAtualizarSecao = "";
			List<Object[]> resultList = getMapaDeProdutividadeDaSecaoJudiciariaList().getResultList();
			if (resultList != null && resultList.size() > 0) {
				MapaDeProdutividadeDaSecaoJudiciariaBean bean = new MapaDeProdutividadeDaSecaoJudiciariaBean();
				for (Object[] o : resultList) {
					if (bean.getCompetencia() == null) {
						bean.setCompetencia(o[1].toString());
					} else if (!bean.getCompetencia().equals(o[1].toString())) {
						estatisticaBeanList.add(bean);
						bean = new MapaDeProdutividadeDaSecaoJudiciariaBean();
						bean.setCompetencia(o[1].toString());
					}
					bean.setCodEstado(o[0].toString());
					bean.setQtdMeses(1);
					bean.setQtdCompetencias(bean.getQtdCompetencias() + 1);
					bean.setTotalProcessosEstado(Long.valueOf(o[2].toString()));
					MapaDeProdutividadeDaSecaoJudiciariaListBean listBean = new MapaDeProdutividadeDaSecaoJudiciariaListBean();
					listBean.setCodEstado(bean.getCodEstado());
					listBean.setQtdProcessos(bean.getTotalProcessosEstado() / bean.getQtdMeses());
					bean.getMapaDeProdutividadeDaSecaoJudiciariaListBean().add(listBean);
					bean.setMediaPorCompetencia(listBean.getQtdProcessos() + bean.getMediaPorCompetencia());
				}
				estatisticaBeanList.add(bean);

				// calcula a media por competência
				for (MapaDeProdutividadeDaSecaoJudiciariaBean lista : estatisticaBeanList) {
					lista.setMediaPorCompetencia(formataMedia(lista.getMediaPorCompetencia()
							/ lista.getQtdCompetencias()));
				}
				if (!ParametroUtil.instance().isPrimeiroGrau()) {
					List<Object[]> resultListAtualizacao = historicoEstatisticaEventoProcessoManager
							.listSecaoNaoAtualizada();
					if (resultListAtualizacao.size() > 0) {
						for (Object[] objects : resultListAtualizacao) {
							erro = ("SJ" + objects[0] + "(atualização até o dia " + objects[1] + "), ");
							erroTemp.append(erro);
						}
						erroTemp = new StringBuilder(erroTemp.substring(0, erroTemp.lastIndexOf(",")));
						int pos = erroTemp.lastIndexOf(",");
						StringBuilder sb = new StringBuilder();
						sb.append(erroTemp);
						sb.replace(pos, pos + 2, " e ");
						sb.insert(erroTemp.length() + 1, '.');
						erroTemp = sb;
						setErroAtualizarSecao(erroTemp.toString());
						erroTemp = new StringBuilder();
					}
				}
			}
		}
		return estatisticaBeanList;
	}

	/**
	 * Método que formata a média de acordo com a casa decimal: se a casa
	 * decimal >= a 5 arredonda a média pro inteiro acima. se a casa decimal <
	 * que 5 o valor permanece o mesmo.
	 * 
	 * @param Média
	 *            para ser formatada.
	 */
	public double formataMedia(double num) {
		String numero = Double.valueOf(num).toString();
		int count = numero.length();
		for (int i = 0; i < count; i++) {
			if (numero.substring(i, i + 1).equals(".")) {
				int decimal = Integer.parseInt(numero.substring(i + 1, i + 2));
				if (decimal > 4) {
					num = Math.ceil(num);
				}
			}
		}
		return num;
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
		if (estatisticaList().size() > 0) {
			RelatorioLog relatorioLog = new RelatorioLog();
			relatorioLog.setDataSolicitacao(new Date());
			relatorioLog.setDescricao("Mapa de Produtividade da Seção Judiciária");
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
	public void exportarMapaDeProdutividadeDaSecaoJudiciariaActionXLS() {
		try {
			if (estatisticaBeanList != null && estatisticaBeanList.size() > 0) {
				exportarXLS(TEMPLATE_XLS_PATH, "MapaDeProdutividadeDaSecaoJudiciariaActionXLS.xls", "estatisticaList",
						estatisticaBeanList);
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
			String dataFormatada = "";
			SimpleDateFormat formatoInicio = new SimpleDateFormat("MM/yyyy");
			dataFormatada = formatoInicio.format(dataInicio);
			map.put("dataInicio", dataFormatada);
			SimpleDateFormat formatoFim = new SimpleDateFormat("MM/yyyy");
			dataFormatada = formatoFim.format(dataFim);
			map.put("dataFim", dataFormatada);
			if (competencia != null) {
				map.put("competencia", competencia);
			} else {
				competencia = "Todos";
				map.put("competencia", competencia);
			}
		}

		map.put("titulo", "MAPA DE PRODUTIVIDADE DA SEÇÃO JUDICIÁRIA");
		map.put("subNomeSistema", ParametroUtil.getParametro("nomeSecaoJudiciaria").toUpperCase());
		map.put("nomeSistema", ParametroUtil.getParametro("nomeSistema"));
		if (!erroAtualizarSecao.isEmpty()) {
			String labelMsg = "Dados não computados na(s) Seção(ões): ";
			map.put("labelMsgErroSecao", labelMsg);
			map.put("erroAtualizarSecao", erroAtualizarSecao);
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

	/**
	 * Retorna todas competencias.
	 * 
	 * @return
	 */
	private List<SelectItem> listGroupByCompetenciaItems() {
		List<SelectItem> items = new ArrayList<SelectItem>();
		items.add(new SelectItem(null, "Todas"));
		for (String s : estatisticaEventoProcessoManager.listGroupByCompentencia()) {
			if (s != null) {
				items.add(new SelectItem(s, s));
			}
		}
		return items;
	}

	/*
	 * Inicio - Getters and Setters
	 */

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

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
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

	public void setDataFim(Date dataFim) {
		this.dataFim = dataFim;
	}

	public MapaDeProdutividadeDaSecaoJudiciariaList getMapaDeProdutividadeDaSecaoJudiciariaList() {
		return mapaDeProdutividadeDaSecaoJudiciariaList;
	}

	public void setMapaDeProdutividadeDaSecaoJudiciariaList(
			MapaDeProdutividadeDaSecaoJudiciariaList mapaDeProdutividadeDaSecaoJudiciariaList) {
		this.mapaDeProdutividadeDaSecaoJudiciariaList = mapaDeProdutividadeDaSecaoJudiciariaList;
	}

	public void setCompetenciaList(List<SelectItem> competenciaList) {
		this.competenciaList = competenciaList;
	}

	public List<SelectItem> getCompetenciaList() {
		if (competenciaList == null) {
			competenciaList = listGroupByCompetenciaItems();
		}
		return competenciaList;
	}

	public void setEstatisticaBeanList(List<MapaDeProdutividadeDaSecaoJudiciariaBean> estatisticaBeanList) {
		this.estatisticaBeanList = estatisticaBeanList;
	}

	public List<MapaDeProdutividadeDaSecaoJudiciariaBean> getEstatisticaBeanList() {
		return estatisticaBeanList;
	}

	public void setCompetencia(String competencia) {
		this.competencia = competencia;
	}

	public String getCompetencia() {
		return competencia;
	}

	public void setDataInicioStr(String dataInicioStr) {
		this.dataInicioStr = dataInicioStr;
	}

	public String getDataInicioStr() {
		return dataInicioStr;
	}

	public void setDataFimStr(String dataFimStr) {
		this.dataFimStr = dataFimStr;
	}

	public String getDataFimStr() {
		return dataFimStr;
	}

	public void limparFiltros() {
		dataFim = null;
		dataInicio = null;
		getCompetenciaList().set(0, getCompetenciaList().get(0));
		setCompetencia(null);
		setEstatisticaBeanList(null);
	}

	public void setErroAtualizarSecao(String erroAtualizarSecao) {
		this.erroAtualizarSecao = erroAtualizarSecao;
	}

	public String getErroAtualizarSecao() {
		return erroAtualizarSecao;
	}
}