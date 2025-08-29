package br.jus.cnj.pje.view.fluxo;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.ListProcessoCompletoBetaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.cnj.pje.view.ListPaginada;
import br.jus.csjt.pje.business.pdf.GeradorPdfUnificado;
import br.jus.csjt.pje.business.pdf.PdfException;
import br.jus.je.pje.entity.vo.BinarioVO;
import br.jus.je.pje.entity.vo.TrasladoDocumentosVO;
import br.jus.pje.nucleo.dto.AutoProcessualDTO;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

@Name(TrasladoDocumentosAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class TrasladoDocumentosAction extends TramitacaoFluxoAction {
	
	
	public static final String NAME = "trasladoDocumentosAction";

	private TrasladoDocumentosVO processoOrigem = new TrasladoDocumentosVO();
	private List<TrasladoDocumentosVO> processoDocumentoSelecionado = new ArrayList<TrasladoDocumentosVO>();
	private String frameTraslado;
	private int paginaAtual = 0;
	private int countPaginas;
	private List<ItemTimeline> itemTimeline = new ArrayList<ItemTimeline>();
	private Map<String, List<ItemTimeline>> mapDatas = new LinkedHashMap<String, List<ItemTimeline>>();
	private boolean isSelecionarDocumentos = false;
	private FiltroPesquisa filtroPesquisa = new FiltroPesquisa();
	private boolean atualizaDataJuntada = false;
	private ItemTimeline documentoAtual;
	private boolean isSelecionarTodos = false;

	public String obterNomePartes() {
		StringBuilder sb = new StringBuilder();
		sb.append(getProceessoJudicialService().getNomeExibicaoPolo(processoOrigem.getProcesso(),
				ProcessoParteParticipacaoEnum.A));
		sb.append(" X ");
		sb.append(getProceessoJudicialService().getNomeExibicaoPolo(processoOrigem.getProcesso(),
				ProcessoParteParticipacaoEnum.P));
		return sb.toString();
	}

	private ProcessoJudicialService getProceessoJudicialService() {
		return (ProcessoJudicialService) ComponentUtil.getComponent(ProcessoJudicialService.class);
	}

	public void pesquisar() {
		itemTimeline = null;
		paginaAtual = 0;
		paginarTimeline();
	}

	public void listarDocumentos(TrasladoDocumentosVO processoVO) {
		if (processoVO != null) {
			processoOrigem.setProcesso(processoVO.getProcesso());
			processoOrigem.setDocumentosSelecionados(processoVO.getDocumentosSelecionados());
		}
		pesquisar();
		isSelecionarDocumentos = true;
	}

	public void atualizar() {
		filtroPesquisa = new FiltroPesquisa();
		pesquisar();
	}

	public void removerProcessoDocumento(TrasladoDocumentosVO processoVO) {
		processoDocumentoSelecionado.remove(processoVO);
	}

	public void paginarTimeline() {
		if (paginaAtual <= countPaginas) {
			List<Criteria> criterias = getCriteriosPesquisa();
			recuperarAutos(paginaAtual, criterias);
			processarMapDatas();
			paginaAtual += 1;
		}
	}

	private void processarMapDatas() {
		mapDatas = new LinkedHashMap<String, List<ItemTimeline>>();
		if (!itemTimeline.isEmpty()) {
			gerarListaAgrupada();
			visualizarDocumento(itemTimeline.get(0));
		}
	}

	public void visualizarDocumento(ItemTimeline item) {
		documentoAtual = item;
		ProcessoDocumentoBin bin = item.getDocumento().getProcessoDocumentoBin();
		if (bin != null) {
			String extensao = null;
			String html = null;
			if (!bin.isBinario()) {
				extensao = "text/html";
				html = getListProcessoCompletoBetaManager().recuperarConteudoModelo(bin.getIdProcessoDocumentoBin());
			} else {
				extensao = bin.getNomeArquivo() != null && bin.getNomeArquivo().toLowerCase().endsWith(".pdf")
						? "application/pdf" : bin.getExtensao();
			}
			BinarioVO binario = new BinarioVO();
			binario.setIdBinario(bin.getIdProcessoDocumentoBin());
			binario.setMimeType(extensao);
			binario.setNomeArquivo(bin.getNomeArquivo());
			binario.setNumeroStorage(bin.getNumeroDocumentoStorage());
			binario.setHtml(html);
			Contexts.getSessionContext().set("download-binario", binario);
		}
	}

	private ListProcessoCompletoBetaManager getListProcessoCompletoBetaManager() {
		return (ListProcessoCompletoBetaManager) ComponentUtil.getComponent(ListProcessoCompletoBetaManager.class);
	}

	private void gerarListaAgrupada() {
		List<ItemTimeline> listaPorData = new LinkedList<ItemTimeline>();
		Date dataAtual = DateUtil.getDataSemHora(itemTimeline.get(0).getDocumento().getDataJuntada());
		Date dataLaco = new Date();
		for (ItemTimeline time : itemTimeline) {
			dataLaco = DateUtil.getDataSemHora(time.getDocumento().getDataJuntada());
			if (!dataLaco.equals(dataAtual)) {
				mapDatas.put(DateUtil.dateToString(dataAtual, "dd MMM yyyy"), listaPorData);
				dataAtual = dataLaco;
				listaPorData = new LinkedList<ItemTimeline>();
			}
			listaPorData.add(time);
		}

		mapDatas.put(DateUtil.dateToString(dataLaco, "dd MMM yyyy"), listaPorData);
	}

	private List<Criteria> getCriteriosPesquisa() {
		List<Criteria> criterias = new ArrayList<Criteria>(0);
		if (filtroPesquisa.getTextoPesquisa() != null && filtroPesquisa.getTextoPesquisa().trim().length() > 0) {
			try {
				criterias.add(
						Criteria.or(Criteria.contains("documento.processoDocumento", filtroPesquisa.getTextoPesquisa()),
								Criteria.contains("documento.documentoPrincipal", filtroPesquisa.getTextoPesquisa()),
								Criteria.contains("documento.tipoProcessoDocumento.tipoProcessoDocumento",
										filtroPesquisa.getTextoPesquisa()),
								Criteria.contains("movimento.textoFinalExterno", filtroPesquisa.getTextoPesquisa()),
								Criteria.startsWith("documento.idProcessoDocumento",
										filtroPesquisa.getTextoPesquisa().trim())));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (filtroPesquisa.getIdTipoDocumento() != null && filtroPesquisa.getIdTipoDocumento() != 0) {
			criterias.add(Criteria.equals("documento.tipoProcessoDocumento.idTipoProcessoDocumento",
					filtroPesquisa.getIdTipoDocumento()));
		}
		if (filtroPesquisa.getIdDocumentoInicio() != null) {
			criterias.add(
					Criteria.greaterOrEquals("documento.idProcessoDocumento", filtroPesquisa.getIdDocumentoInicio()));
		}
		if (filtroPesquisa.getIdDocumentoFim() != null) {
			criterias.add(Criteria.lessOrEquals("documento.idProcessoDocumento", filtroPesquisa.getIdDocumentoFim()));
		}
		if (filtroPesquisa.getDataInicio() != null) {
			criterias.add(Criteria.greaterOrEquals("documento.dataJuntada", filtroPesquisa.getDataInicio()));
		}
		if (filtroPesquisa.getDataFim() != null) {
			criterias.add(Criteria.lessOrEquals("documento.dataJuntada", filtroPesquisa.getDataFim()));
		}

		return criterias;
	}

	public void selecionarTodos() {
		processoOrigem.setDocumentosSelecionados(new ArrayList<ProcessoDocumento>());
		if (isSelecionarTodos) {
			Search search = new Search(AutoProcessualDTO.class);
			ListAutoProcessual list = new ListAutoProcessual(getListProcessoCompletoBetaManager(), search);
			for (AutoProcessualDTO auto : list.ListPaginada()) {
				processoOrigem.getDocumentosSelecionados().add(auto.getDocumento());
			}
		}

	}

	private void recuperarAutos(int pagina, List<Criteria> criterias) {
		Search search = new Search(AutoProcessualDTO.class);
		search.setMax(50);
		search.setFirst(pagina * 50);
		search.addOrder("documento.dataJuntada", Order.DESC);
		if (criterias != null && criterias.size() > 0) {
			try {
				search.addCriteria(criterias);
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}
		}
		ListAutoProcessual list = new ListAutoProcessual(getListProcessoCompletoBetaManager(), search);
		setarAutos(list);
	}

	private void setarAutos(ListAutoProcessual autos) {
		if (itemTimeline == null || itemTimeline.size() == 0) {
			itemTimeline = new LinkedList<ItemTimeline>();
			countPaginas = autos.getPageCount(autos.count());
		}
		List<AutoProcessualDTO> autosPaginados = new ArrayList<AutoProcessualDTO>(0);
		if (countPaginas > 0) {
			autosPaginados = autos.ListPaginada();

			for (AutoProcessualDTO auto : autosPaginados) {
				itemTimeline.add(setarDocumento(auto.getDocumento()));
			}
		}
	}

	private ItemTimeline setarDocumento(ProcessoDocumento documento) {
		ItemTimeline item = new ItemTimeline();
		item.setDocumento(documento);
		List<ItemTimeline> anexos = new ArrayList<ItemTimeline>();
		for (ProcessoDocumento doc : documento.getDocumentosVinculados()) {
			ItemTimeline itemAnexo = new ItemTimeline();
			itemAnexo.setDocumento(doc);
			anexos.add(itemAnexo);
		}
		item.setDocumentosAnexos(anexos);
		return item;
	}

	public String processarNomeDocumento(ProcessoDocumento documento) {
		StringBuilder sb = new StringBuilder();
		Integer id = documento.getIdProcessoDocumento();
		String texto = documento.getProcessoDocumento();
		String tipo = documento.getTipoProcessoDocumento().getTipoProcessoDocumento();

		if (StringUtil.isNotEmpty(texto)) {
			texto = texto.replaceAll("_", " ").replaceAll("-", " ");
			sb.append(id);
			sb.append(" - ");
			if (texto.equalsIgnoreCase(tipo)) {
				sb.append(texto);
			} else {
				sb.append(tipo);
				sb.append(" (");
				sb.append(texto);
				sb.append(")");
			}
		}
		return sb.toString();
	}

	public List<SelectItem> getTipoDocumentoSelectItems() {
		List<SelectItem> selectList = new ArrayList<SelectItem>();
		List<TipoProcessoDocumento> tipos = getListProcessoCompletoBetaManager()
				.recuperarTiposDocumentosAutos(getProcessoOrigem().getProcesso().getIdProcessoTrf());
		for (TipoProcessoDocumento t : tipos) {
			selectList.add(new SelectItem(t.getIdTipoProcessoDocumento(), t.getTipoProcessoDocumento()));
		}
		return selectList;
	}

	public void cancelarSelecao() {
		limparCampos();
		isSelecionarDocumentos = false;
	}

	public void selecionarDocumentos() {
		int index = processoDocumentoSelecionado.indexOf(processoOrigem);
		if (index == -1) {
			processoDocumentoSelecionado.add(processoOrigem);
		} else {
			processoDocumentoSelecionado.set(index, processoOrigem);
		}
		limparCampos();
		isSelecionarDocumentos = false;
	}

	private void limparCampos() {
		processoOrigem = new TrasladoDocumentosVO();
		filtroPesquisa = new FiltroPesquisa();
		isSelecionarTodos = false;
	}

	public void trasladarDocumentos() throws PJeBusinessException {
		List<ProcessoDocumento> listaDocumentos = new ArrayList<ProcessoDocumento>();
		for (TrasladoDocumentosVO listaVO : processoDocumentoSelecionado) {
			listaDocumentos.addAll(listaVO.getDocumentosSelecionados());
		}
		ProcessoTrf processoDestino = TaskInstanceUtil.instance()
				.getProcesso(TaskInstanceUtil.instance().getProcessInstance().getId());
		limparCampos();
		if (((ProcessoDocumentoManager) ComponentUtil.getComponent(ProcessoDocumentoManager.class))
				.trasladarDocumentos(processoDestino, listaDocumentos, atualizaDataJuntada)) {
			processoDocumentoSelecionado = new ArrayList<TrasladoDocumentosVO>();
			FacesMessages.instance().add(Severity.INFO, "Documentos trasladados com sucesso");
			String defaultTransition = getTransicaoPadrao();
			if (defaultTransition != null) {
				TaskInstanceHome.instance().end(defaultTransition);
			}
		}
	}

	public void removerDocumentoSelecionado(ProcessoDocumento documento) {
		processoOrigem.getDocumentosSelecionados().remove(documento);
	}

	public boolean mostraCampoData() {
		return ParametroUtil.instance().isPermiteAtualizarDataJuntada();
	}

	public void imprimirDocumento(ProcessoDocumento documento) {
		if (documento != null) {
			List<ProcessoDocumento> docs = new ArrayList<ProcessoDocumento>(0);
			docs.add(documento);
			FacesContext facesContext = FacesContext.getCurrentInstance();
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
					.getRequest();
			HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
			response.reset();
			response.setContentType("application/pdf");

			gerarPdf(request, response, docs, false);
			facesContext.responseComplete();
		}
	}

	private void gerarPdf(HttpServletRequest request, HttpServletResponse response, List<ProcessoDocumento> documentos,
			boolean pdfCompleto) {
		OutputStream out = null;
		try {
			out = response.getOutputStream();
			String resourcePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
					+ request.getContextPath();
			GeradorPdfUnificado geradorPdf = new GeradorPdfUnificado();
			geradorPdf.setResurcePath(resourcePath);
			if (pdfCompleto) {
				geradorPdf.setGerarIndiceDosDocumentos(true);
				geradorPdf.gerarPdfUnificado(processoOrigem.getProcesso(), documentos, out);
			} else {
				geradorPdf.gerarPdfSimples(documentos, out);
			}
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (PdfException e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public TrasladoDocumentosVO getProcessoOrigem() {
		return processoOrigem;
	}

	public void setProcessoOrigem(TrasladoDocumentosVO processoOrigem) {
		this.processoOrigem = processoOrigem;
	}

	public String getFrameTraslado() {
		return frameTraslado;
	}

	public void setFrameTraslado(String frameTraslado) {
		this.frameTraslado = frameTraslado;
	}

	public List<TrasladoDocumentosVO> getProcessoDocumentoSelecionado() {
		return processoDocumentoSelecionado;
	}

	public void setProcessoDocumentoSelecionado(List<TrasladoDocumentosVO> processoDocumentoSelecionado) {
		this.processoDocumentoSelecionado = processoDocumentoSelecionado;
	}

	public int getPaginaAtual() {
		return paginaAtual;
	}

	public void setPaginaAtual(int paginaAtual) {
		this.paginaAtual = paginaAtual;
	}

	public int getCountPaginas() {
		return countPaginas;
	}

	public void setCountPaginas(int countPaginas) {
		this.countPaginas = countPaginas;
	}

	public Map<String, List<ItemTimeline>> getMapDatas() {
		return mapDatas;
	}

	public void setMapDatas(Map<String, List<ItemTimeline>> mapDatas) {
		this.mapDatas = mapDatas;
	}

	public boolean isSelecionarDocumentos() {
		return isSelecionarDocumentos;
	}

	public void setSelecionarDocumentos(boolean isSelecionarDocumentos) {
		this.isSelecionarDocumentos = isSelecionarDocumentos;
	}

	public FiltroPesquisa getFiltroPesquisa() {
		return filtroPesquisa;
	}

	public void setFiltroPesquisa(FiltroPesquisa filtroPesquisa) {
		this.filtroPesquisa = filtroPesquisa;
	}

	public boolean isAtualizaDataJuntada() {
		return atualizaDataJuntada;
	}

	public void setAtualizaDataJuntada(boolean atualizaDataJuntada) {
		this.atualizaDataJuntada = atualizaDataJuntada;
	}

	public ItemTimeline getDocumentoAtual() {
		return documentoAtual;
	}

	public void setDocumentoAtual(ItemTimeline documentoAtual) {
		this.documentoAtual = documentoAtual;
	}

	public boolean isSelecionarTodos() {
		return isSelecionarTodos;
	}

	public void setSelecionarTodos(boolean isSelecionarTodos) {
		this.isSelecionarTodos = isSelecionarTodos;
	}

	private class ListAutoProcessual extends ListPaginada<AutoProcessualDTO> {
		private ListProcessoCompletoBetaManager manager;

		public ListAutoProcessual(ListProcessoCompletoBetaManager manager, Search search) {
			this.manager = manager;
			super.setSearch(search);
		}

		@Override
		public List<AutoProcessualDTO> list(Search search) {
			return manager.recuperarAutos(processoOrigem.getProcesso().getIdProcessoTrf(), true, false, search);
		}

		@Override
		public Long count() {
			return manager.countAutos(processoOrigem.getProcesso().getIdProcessoTrf(), true, false, super.getSearch());
		}
	}

	public class FiltroPesquisa {
		private Integer idDocumentoInicio;
		private Integer idDocumentoFim;
		private Date dataInicio;
		private Date dataFim;
		private Integer idTipoDocumento;
		private String textoPesquisa;

		public Integer getIdDocumentoInicio() {
			return idDocumentoInicio;
		}

		public void setIdDocumentoInicio(Integer idDocumentoInicio) {
			this.idDocumentoInicio = idDocumentoInicio;
		}

		public Integer getIdDocumentoFim() {
			return idDocumentoFim;
		}

		public void setIdDocumentoFim(Integer idDocumentoFim) {
			this.idDocumentoFim = idDocumentoFim;
		}

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

		public Integer getIdTipoDocumento() {
			return idTipoDocumento;
		}

		public void setIdTipoDocumento(Integer idTipoDocumento) {
			this.idTipoDocumento = idTipoDocumento;
		}

		public String getTextoPesquisa() {
			return textoPesquisa;
		}

		public void setTextoPesquisa(String textoPesquisa) {
			this.textoPesquisa = textoPesquisa;
		}
	}

	public class ItemTimeline {

		private ProcessoDocumento documento;
		private List<ItemTimeline> documentosAnexos;

		public ItemTimeline() {
		}

		public String getIconeTipoDocumento() {
			String tipo = null;

			ProcessoDocumentoBin bin = documento.getProcessoDocumentoBin();
			String extensao = (bin.getNomeArquivo() != null && bin.getNomeArquivo().toLowerCase().endsWith(".pdf")
					? "application/pdf" : bin.getExtensao());

			if (extensao == null || extensao.equalsIgnoreCase("text/html")) {
				tipo = "file-text";
			}

			else if (extensao.equalsIgnoreCase("application/pdf")) {
				tipo = "file-pdf";
			}

			else if (StringUtils.containsIgnoreCase(extensao, "audio/")) {
				tipo = "file-audio";
			}

			else if (StringUtils.containsIgnoreCase(extensao, "video/")) {
				tipo = "file-video";
			}

			else if (StringUtils.containsIgnoreCase(extensao, "image/")) {
				tipo = "file-image";
			} else {
				tipo = "file";
			}

			return tipo;
		}

		public boolean isSelecionado() {
			return processoOrigem.getDocumentosSelecionados() != null
					&& processoOrigem.getDocumentosSelecionados().contains(this.getDocumento());
		}

		public void setSelecionado(boolean selecionado) {
			if (selecionado) {
				if (!processoOrigem.getDocumentosSelecionados().contains(this.getDocumento())) {
					processoOrigem.getDocumentosSelecionados().add(this.getDocumento());
				}
			} else {
				processoOrigem.getDocumentosSelecionados().remove(this.getDocumento());
			}
		}

		public String getHora() {
			return DateUtil.dateToHour(documento.getDataJuntada());
		}

		public ProcessoDocumento getDocumento() {
			return documento;
		}

		public void setDocumento(ProcessoDocumento documento) {
			this.documento = documento;
		}

		public List<ItemTimeline> getDocumentosAnexos() {
			return documentosAnexos;
		}

		public void setDocumentosAnexos(List<ItemTimeline> documentosAnexos) {
			this.documentosAnexos = documentosAnexos;
		}
	}

	@Override
	protected Map<String, String> getParametrosConfiguracao() {
		return null;
	}
	

}
