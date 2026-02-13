package br.com.infox.pje.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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
import br.com.infox.pje.bean.MapaProdutividadeBean;
import br.com.infox.pje.bean.MapaProdutividadeTipoVaraBean;
import br.com.infox.pje.bean.MapaProdutividadeVaraBean;
import br.com.infox.pje.list.MapaProdutividadeList;
import br.com.infox.pje.manager.EstatisticaEventoProcessoManager;
import br.com.infox.pje.manager.HistoricoEstatisticaEventoProcessoManager;
import br.com.infox.pje.manager.SecaoJudiciariaManager;
import br.com.itx.component.FileHome;
import br.com.itx.component.Util;
import br.jus.pje.nucleo.entidades.RelatorioLog;
import br.jus.pje.nucleo.entidades.SecaoJudiciaria;
import br.jus.pje.nucleo.entidades.Usuario;

@Name(value = MapaProdutividadeVarasAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class MapaProdutividadeVarasAction implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8903655370750193442L;

	public static final String NAME = "mapaProdutividadeVarasAction";

	private MapaProdutividadeList mapaProdutividadeList = new MapaProdutividadeList();
	private static final String TEMPLATE_XLS_PATH = "/EstatisticaProcesso/MapaProdutividadeVaras/mapaProdutividadeVarasTemplate.xls";

	private Date dataInicio;
	private Date dataFim;
	private String dataInicioStr;
	private String dataFimStr;
	private String dataInicioFormatada;
	private String dataFimFormatada;
	private long totalProcessos;
	private int totalVaras;
	private List<MapaProdutividadeBean> estatisticaBeanList;
	private List<SelectItem> estadoList;
	private String codEstado;
	private List<SecaoJudiciaria> secaoJudiciariaList;
	private SecaoJudiciaria secaoJudiciaria;
	private String erroAtualizarSecao = "";
	@In
	private GenericManager genericManager;
	@In
	private EstatisticaEventoProcessoManager estatisticaEventoProcessoManager;
	@In
	private SecaoJudiciariaManager secaoJudiciariaManager;
	@In
	private HistoricoEstatisticaEventoProcessoManager historicoEstatisticaEventoProcessoManager;

	public List<MapaProdutividadeBean> mapaProdutividadeList() {
		String erro = "";
		StringBuilder erroTemp = new StringBuilder();
		if (getEstatisticaBeanList() == null) {
			erroAtualizarSecao = "";
			setEstatisticaBeanList(buildMapaProdutividadeList());
		}
		if (!ParametroUtil.instance().isPrimeiroGrau() && estatisticaBeanList.size() > 0) {
			List<Object[]> resultListAtualizacao = historicoEstatisticaEventoProcessoManager.listSecaoNaoAtualizada();
			if (resultListAtualizacao.size() > 0) {
				for (Object[] objects : resultListAtualizacao) {
					if (getSecaoJudiciaria() != null
							&& getSecaoJudiciaria().getCdSecaoJudiciaria().equals(objects[0].toString())) {
						erro = ("SJ" + objects[0] + "(atualização até o dia " + objects[1] + "). ");
						erroTemp.append(erro);
						break;
					} else {
						if (getSecaoJudiciaria() == null) {
							erro = ("SJ" + objects[0] + "(atualização até o dia " + objects[1] + "), ");
							erroTemp.append(erro);
						}
					}
				}
			}
			int pos = erroTemp.lastIndexOf(",");
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
		return getEstatisticaBeanList();
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

	private List<MapaProdutividadeBean> buildMapaProdutividadeList() {
		dataInicioFormatada = formatarAnoMes(dataInicioStr);
		dataFimFormatada = formatarAnoMes(dataFimStr);
		List<Map<String, Object>> mapList = getMapaProdutividadeList().getResultList();
		List<MapaProdutividadeBean> mapaProdutividadeBeanList = new ArrayList<MapaProdutividadeBean>();
		for (Map<String, Object> map : mapList) {
			MapaProdutividadeBean bean = new MapaProdutividadeBean();
			bean.setCodEstado(map.get("codEstado").toString());
			bean.setMapaProdutividadeTipoVaraBeanList(listaOrdenadaTipoVaraPorEstado(dataInicioFormatada,
					dataFimFormatada, bean));

			mapaProdutividadeBeanList.add(bean);
		}
		return mapaProdutividadeBeanList;
	}

	private List<MapaProdutividadeVaraBean> ordenarVaras(List<MapaProdutividadeVaraBean> listaPVB) {
		List<Integer> lista = new ArrayList<Integer>();
		// pega todos os números das varas e coloca em uma lista para ordenação
		// posterior
		for (MapaProdutividadeVaraBean u : listaPVB) {
			String varas = u.getVara();
			if (varas.indexOf("ª") > 0) {
				CharSequence subSequence = varas.subSequence(0, varas.indexOf("ª"));
				lista.add(Integer.valueOf(subSequence.toString().trim()));
			}
		}

		// ordena as varas da lista de forma crescente
		Collections.sort(lista);

		// cria a lista de retorno ao usuário ordenado de acordo com o número da
		// vara
		List<MapaProdutividadeVaraBean> mapaProdutividadeVaraBean2 = new ArrayList<MapaProdutividadeVaraBean>();
		for (Integer u : lista) {
			for (MapaProdutividadeVaraBean o : listaPVB) {
				String varas = o.getVara();
				CharSequence subSequence = varas.subSequence(0, varas.indexOf("ª") > -1 ? varas.indexOf("ª") : 0);
				if (varas.indexOf("ª") > 0) {
					if (Integer.valueOf(subSequence.toString().trim()).equals(u)
							&& !mapaProdutividadeVaraBean2.contains(o)) {
						mapaProdutividadeVaraBean2.add(o);
					}
				}
			}
		}

		// adiciona as varas que não tem número
		for (MapaProdutividadeVaraBean o : listaPVB) {
			if (!mapaProdutividadeVaraBean2.contains(o)) {
				mapaProdutividadeVaraBean2.add(o);
			}
		}

		return mapaProdutividadeVaraBean2;
	}

	private List<MapaProdutividadeTipoVaraBean> listaOrdenadaTipoVaraPorEstado(String dataIni, String dataFim,
			MapaProdutividadeBean bean) {
		List<MapaProdutividadeTipoVaraBean> listTipoVaraByEstado = estatisticaEventoProcessoManager
				.listTipoVaraByEstado(dataIni, dataFim, bean.getCodEstado());

		// iniciando o desmenbramento para a ordenação das varas
		for (MapaProdutividadeTipoVaraBean o : listTipoVaraByEstado) {
			o.setMapaProdutividadeVaraBeanList(ordenarVaras(o.getMapaProdutividadeVaraBeanList()));
		}

		return listTipoVaraByEstado;
	}

	public void gravarLogRelatorio() {
		setEstatisticaBeanList(null);
		if (mapaProdutividadeList().size() > 0) {
			RelatorioLog relatorioLog = new RelatorioLog();
			relatorioLog.setDataSolicitacao(new Date());
			relatorioLog.setDescricao("Mapa de Produtividade das Varas");
			Usuario usuario = (Usuario) Contexts.getSessionContext().get("usuarioLogado");
			relatorioLog.setIdUsuarioSolicitacao(usuario);
			genericManager.persist(relatorioLog);
		}
	}

	public void exportarMapaProdutividadeVarasXLS() {
		try {
			if (getEstatisticaBeanList().size() > 0) {
				exportarXLS(TEMPLATE_XLS_PATH, "MapaProdutividade.xls", "estatisticaBeanList", getEstatisticaBeanList());
			} else {
				FacesMessages.instance().add(Severity.INFO, "Não há dados para exportar!");
			}
		} catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao exportar arquivo." + e.getMessage());
			e.printStackTrace();
		}
	}

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
		map.put("dataInicio", dataInicioStr);
		map.put("dataFim", dataFimStr);
		map.put("titulo", "MAPA DE PRODUTIVIDADE DAS VARAS");
		map.put("subNomeSistema", ParametroUtil.getParametro("nomeSecaoJudiciaria").toUpperCase());
		map.put("nomeSistema", ParametroUtil.getParametro("nomeSistema"));
		map.put("secaoJudiciaria", secaoJudiciaria == null ? "Todas" : secaoJudiciaria);
		if (!erroAtualizarSecao.isEmpty()) {
			map.put("erroAtualizarSecao", "Dados não computados na(s) Seção(ões): " + erroAtualizarSecao);
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

	public void setTotalVaras(int totalVaras) {
		this.totalVaras = totalVaras;
	}

	public int getTotalVaras() {
		return totalVaras;
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

	public void setTotalProcessos(long totalProcessos) {
		this.totalProcessos = totalProcessos;
	}

	public long getTotalProcessos() {
		return totalProcessos;
	}

	public void setEstadoList(List<SelectItem> estadoList) {
		this.estadoList = estadoList;
	}

	public List<SelectItem> getEstadoList() {
		if (estadoList == null) {
			estadoList = listGroupByEstadoItems();
		}
		return estadoList;
	}

	public void setCodEstado(String codEstado) {
		this.codEstado = codEstado;
	}

	public String getCodEstado() {
		return codEstado;
	}

	/*
	 * Fim - Getters and Setters
	 */

	private List<SelectItem> listGroupByEstadoItems() {
		List<SelectItem> items = new ArrayList<SelectItem>();
		items.add(new SelectItem(null, "Selecione"));
		for (String s : estatisticaEventoProcessoManager.listGroupByEstado()) {
			items.add(new SelectItem(s, s));
		}
		return items;
	}

	public void setMapaProdutividadeList(MapaProdutividadeList mapaProdutividadeList) {
		this.mapaProdutividadeList = mapaProdutividadeList;
	}

	public MapaProdutividadeList getMapaProdutividadeList() {
		return mapaProdutividadeList;
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

	public void limparFiltros() {
		if (!ParametroUtil.instance().isPrimeiroGrau()) {
			erroAtualizarSecao = "";
			secaoJudiciaria = null;
			estatisticaBeanList = null;
		}
		dataFimStr = null;
		dataInicioStr = null;
	}

	public void setEstatisticaBeanList(List<MapaProdutividadeBean> estatisticaBeanList) {
		this.estatisticaBeanList = estatisticaBeanList;
	}

	public List<MapaProdutividadeBean> getEstatisticaBeanList() {
		return estatisticaBeanList;
	}

	public void setErroAtualizarSecao(String erroAtualizarSecao) {
		this.erroAtualizarSecao = erroAtualizarSecao;
	}

	public String getErroAtualizarSecao() {
		return erroAtualizarSecao;
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