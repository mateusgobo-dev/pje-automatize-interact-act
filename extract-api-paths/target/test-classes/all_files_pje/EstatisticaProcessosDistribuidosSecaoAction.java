package br.com.infox.pje.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.Format;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
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
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.util.RandomStringUtils;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.manager.GenericManager;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.bean.LocalizacaoProcessosDistribuidoSessaoListBean;
import br.com.infox.pje.bean.ProcessosDistribuidoSessaoBean;
import br.com.infox.pje.bean.SecaoLocalizacaoProcessosDistribuidoSessaoListBean;
import br.com.infox.pje.list.EstatisticaProcessosDistribuidosSecaoList;
import br.com.infox.pje.manager.EstatisticaEventoProcessoManager;
import br.com.infox.pje.manager.SecaoJudiciariaManager;
import br.com.itx.component.FileHome;
import br.com.itx.component.Util;
import br.jus.pje.nucleo.entidades.RelatorioLog;
import br.jus.pje.nucleo.entidades.SecaoJudiciaria;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.enums.EstadosBrasileirosEnum;

/**
 * Classe action controladora do listView de
 * /EstatisticaProcesso/ConclusaoDeProcessos/
 * 
 * @author Edson
 * 
 */
@Name(value = EstatisticaProcessosDistribuidosSecaoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class EstatisticaProcessosDistribuidosSecaoAction implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "estatisticaProcessosDistribuidosSecaoAction";
	private EstatisticaProcessosDistribuidosSecaoList distribuidosSecaoList = new EstatisticaProcessosDistribuidosSecaoList();
	private Date dataInicio;
	private Date dataFim;
	private String dataInicioFormatada;
	private String dataFimFormatada;
	private boolean sintetico = true;
	private List<SecaoLocalizacaoProcessosDistribuidoSessaoListBean> listaResultGrid;
	private static final String TEMPLATE_XLS_ANALITICO_PATH = "/EstatisticaProcesso/ProcessosDistribuidosSecoes5RegiaoSecaoLocalizacao/distribuidosSecaoAnalitico.xls";
	private static final String TEMPLATE_XLS_SINTETICO_PATH = "/EstatisticaProcesso/ProcessosDistribuidosSecoes5RegiaoSecaoLocalizacao/distribuidosSecaoSintetico.xls";
	private SecaoJudiciaria secaoJudiciaria;
	private List<SecaoJudiciaria> secaoJudiciariaList;
	@In
	private EstatisticaEventoProcessoManager estatisticaEventoProcessoManager;
	@In
	private GenericManager genericManager;
	@In
	private SecaoJudiciariaManager secaoJudiciariaManager;

	/**
	 * Monta a lista de registros que preencherão o grid para exibição
	 * 
	 * @return Lista de ArquivadosRankingSessaoBean.
	 */
	public List<SecaoLocalizacaoProcessosDistribuidoSessaoListBean> preencheGrid() {
		if (null == listaResultGrid && null != dataInicio && null != dataFim) {
			dataInicioFormatada = formatarAnoMes(dataInicio);
			dataFimFormatada = formatarAnoMes(dataFim);
			// instancia lista dos ressultados da grid
			listaResultGrid = new ArrayList<SecaoLocalizacaoProcessosDistribuidoSessaoListBean>();

			// pega a lista de resultados do select
			List<String> resultList = getDistribuidosSecaoList().getResultList();

			// verifica se houve registro encontrado na base e monta a lista
			// principal
			if (resultList != null && resultList.size() > 0) {
				String codEstado = "";
				for (int i = 0; i < resultList.size(); i++) {
					if (!codEstado.equals(resultList.get(i))) {
						codEstado = resultList.get(i);
						SecaoLocalizacaoProcessosDistribuidoSessaoListBean secaoBean = new SecaoLocalizacaoProcessosDistribuidoSessaoListBean();
						// seta a sigla do estado
						secaoBean.setCodEstado(codEstado);

						// seta a lista de varas (localizações) na seção
						secaoBean.setListaSecaoLocalizacao(pegarListaVarasEventoDistribuidos(codEstado));

						// getVaraComparator(secaoBean.getListaSecaoLocalizacao());

						// adiciona a secaoBean na lista de exibição da grid
						listaResultGrid.add(secaoBean);
					}
				}
			}
		}
		return listaResultGrid;
	}

	/**
	 * Método que recebe uma data e transforma para "yyyy-MM-dd"
	 * 
	 * @param data
	 * @return
	 */
	public String formatarAnoMes(Date data) {
		Format formatter = new SimpleDateFormat("yyyy-MM-dd");
		return formatter.format(data);
	}

	public long getTotal5Regiao() {
		long total = 0;
		for (SecaoLocalizacaoProcessosDistribuidoSessaoListBean o : listaResultGrid) {
			total += o.getQtdProcSecao();
		}
		return total;
	}

	/**
	 * A partir de um objeto (String) cod_secao devolve a lista das varas para o
	 * processo evento dristribuido
	 * 
	 * @param secao
	 *            Código da seção esperado
	 * @return
	 */
	private List<LocalizacaoProcessosDistribuidoSessaoListBean> pegarListaVarasEventoDistribuidos(String secao) {
		// pega a lista de varas (localizações) para a seção "o"
		List<String> resultListObj = estatisticaEventoProcessoManager.pegarListaVarasSecaoEventoDistribuicao(secao,
				dataInicioFormatada, dataFimFormatada);
		List<LocalizacaoProcessosDistribuidoSessaoListBean> listLocalizacaoTemp = new ArrayList<LocalizacaoProcessosDistribuidoSessaoListBean>();

		// varre a lista de resultados preenchendo a lista de varas da seção "o"
		// seções
		for (String obj : resultListObj) {
			LocalizacaoProcessosDistribuidoSessaoListBean localizacaoBeanTemp = new LocalizacaoProcessosDistribuidoSessaoListBean();
			localizacaoBeanTemp.setOrgaoJulgador(obj);
			localizacaoBeanTemp.setListaProcessos(pegarListaPocessosDistribuidosSecao(secao, obj));
			listLocalizacaoTemp.add(localizacaoBeanTemp);
		}

		return listLocalizacaoTemp;
	}

	/**
	 * Retorna a lista de ProcessosDistribuidoSessaoBean distribuidos para uma
	 * seção
	 * 
	 * @param secao
	 *            Código da seção esperado
	 * @param oj
	 *            Localização esperada
	 * @return Retorna uma lista de ProcessosDistribuidoSessaoBean
	 */
	private List<ProcessosDistribuidoSessaoBean> pegarListaPocessosDistribuidosSecao(String secao, String oj) {
		List<ProcessosDistribuidoSessaoBean> listaProcVaraSecTemp = new ArrayList<ProcessosDistribuidoSessaoBean>();
		// pega a lista de processos (localizações) para a seção "secao"
		List<Object[]> resultListObj = estatisticaEventoProcessoManager.pegarListaProcVarasSecaoEventoDistribuicao(
				secao, oj, dataInicioFormatada, dataFimFormatada);
		for (Object[] obj : resultListObj) {
			ProcessosDistribuidoSessaoBean procDistSecTemp = new ProcessosDistribuidoSessaoBean();
			procDistSecTemp.setProcesso((String) obj[0]);
			procDistSecTemp.setClasseJudicial((String) obj[1]);
			procDistSecTemp.setDataDistribuicao((Date) obj[2]);
			listaProcVaraSecTemp.add(procDistSecTemp);
		}
		return listaProcVaraSecTemp;
	}

	/**
	 * Método que grava o log das consultas de relarório caso a consulta retorne
	 * registros.
	 */
	public void gravarLogRelatorio() {
		listaResultGrid = null;
		if (listaResultGrid != null && listaResultGrid.size() > 0) {
			RelatorioLog relatorioLog = new RelatorioLog();
			relatorioLog.setDataSolicitacao(new Date());
			relatorioLog.setDescricao("Processos Distribuídos nas Seções da 5ª Região");
			Usuario usuario = Authenticator.getUsuarioLogado();
			relatorioLog.setIdUsuarioSolicitacao(usuario);
			genericManager.persist(relatorioLog);
		}
	}

	public void limparLista() {
		// limpa a lista para uma nova pesquisa
		if (!ParametroUtil.instance().isPrimeiroGrau()) {
			secaoJudiciaria = null;
		}
		listaResultGrid = null;
		dataInicio = null;
		dataFim = null;
	}

	public void limparCampos() {
		sintetico = true;
		limparLista();
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
			if (listaResultGrid != null && listaResultGrid.size() > 0) {
				if (!sintetico) {
					exportarXLS(TEMPLATE_XLS_ANALITICO_PATH, "distribuidosSecaoAnalitico.xls");
				} else {
					exportarXLS(TEMPLATE_XLS_SINTETICO_PATH, "distribuidosSecaoSintetico.xls");
				}
			} else {
				FacesMessages.instance().add(Severity.INFO, "Não há dados para exportar!");
			}
		} catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao exportar arquivo." + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Método que exporta o relatório de estatística de processos em
	 * distribuição nas seções da 5ª região em planilhas eletrônica
	 * 
	 * @param dirNomeTemplate
	 *            caminho do arquivo no sistema
	 * @param nomeArqDown
	 *            nome do arquivo ao ser baixado pelo usuário
	 * @throws ParsePropertyException
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public void exportarXLS(String dirNomeTemplate, String nomeArqDown) throws ParsePropertyException,
			InvalidFormatException, IOException {

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
		map.put("distribuidoSecaoTrfLista", listaResultGrid);
		if (dataInicio != null && dataFim != null) {
			map.put("dataInicio", new SimpleDateFormat("dd/MM/yyyy").format(dataInicio));
			map.put("dataFim", new SimpleDateFormat("dd/MM/yyyy").format(dataFim));
		}

		map.put("titulo", "PROCESSOS DISTRIBUÍDOS NAS SEÇÕES DA 5ª REGIÃO");
		map.put("subNomeSistema", ParametroUtil.getParametro("nomeSecaoJudiciaria").toUpperCase());
		map.put("nomeSistema", ParametroUtil.getParametro("nomeSistema"));
		map.put("relatorio", sintetico ? "Sintético" : "Analítico");
		map.put("totalTrf", getTotal5Regiao());
		map.put("secao", secaoJudiciaria == null ? "Todas" : secaoJudiciaria.getSecaoJudiciaria());

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
	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	public Date getDataFim() {
		return dataFim;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = dataFim;
	}

	public List<SecaoLocalizacaoProcessosDistribuidoSessaoListBean> getListaResultGrid() {
		return listaResultGrid;
	}

	public void setListaResultGrid(List<SecaoLocalizacaoProcessosDistribuidoSessaoListBean> listaResultGrid) {
		this.listaResultGrid = listaResultGrid;
	}

	public EstatisticaProcessosDistribuidosSecaoList getDistribuidosSecaoList() {
		return distribuidosSecaoList;
	}

	public void setDistribuidosSecaoList(EstatisticaProcessosDistribuidosSecaoList distribuidosSecaoList) {
		this.distribuidosSecaoList = distribuidosSecaoList;
	}

	public boolean getSintetico() {
		return sintetico;
	}

	public void setSintetico(boolean sintetico) {
		this.sintetico = sintetico;
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

	public String labelEstadoBrasileiro(String codEstado) {
		return EstadosBrasileirosEnum.valueOf(codEstado).getLabel();
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