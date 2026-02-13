package br.com.infox.cliente.actions.painel.advogado.consultaDocnaoAssinado;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.DAO.EntityList;
import br.com.infox.cliente.home.ProcessoDocumentoBinPessoaAssinaturaHome;
import br.com.infox.cliente.home.ProcessoDocumentoHome;
import br.com.infox.cliente.home.ProcessoExpedienteHome;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.FacesUtil;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.service.TipoProcessoDocumentoPapelService;
import br.jus.cnj.pje.view.PaginatedDataModel;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.cnj.pje.vo.ProcessoDocumentoConsultaNaoAssinadoVO;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;

@Name(ConsultaDocnaoAssinado.NAME)
@Scope(ScopeType.PAGE)
public class ConsultaDocnaoAssinado implements ArquivoAssinadoUploader {

	public static final String NAME = "consultaDocnaoAssinado";

	private String certChain;
	private String signature;
	private Integer contDoc;
	private Boolean checkAll = Boolean.FALSE;
	private Boolean checkHeader = false;
	private boolean exibeModalRemetidos = false;
	private boolean exibeModalMultiplosRemetidos = false;
	private boolean exibeModalAssinarAnexos = false;
	private List<ProcessoDocumento> documentosAssinar = new ArrayList<>(0);
	private List<ProcessoDocumento> anexosAssinar;
	private List<ProcessoTrf> processoTrfList = new ArrayList<>(0);
	private List<ProcessoTrf> processoTrfPendenteList = new ArrayList<>(0);
	private List<ProcessoDocumento> documentosProcessoRemetidoList = new ArrayList<>(0);
	private List<ProcessoTrf> processoTrfRemetidoList = new ArrayList<>(0);
	private ProcessoDocumento pdTemp;
	private boolean checkBox = false;
	private boolean isRemocao = false;
 	private ProcessoDocumentoConsultaNaoAssinadoVO processoDocumentoConsultaNaoAssinadoVO;
 	private List<ArquivoAssinadoHash> arquivosAssinados = new ArrayList<>(0);
 	
 	@Out(required=false)
 	private PaginatedDataModel<ProcessoDocumento> documentos;
 
	@In
	private ProcessoDocumentoManager processoDocumentoManager;
	
	@In
	private DocumentoJudicialService documentoJudicialService;
	
	@In
	private TipoProcessoDocumentoPapelService tipoProcessoDocumentoPapelService;
	
	@Override
	public String getActionName() {
		return NAME;
	}
	
	public void pesquisar() {
		setDocumentos(null);
	}
	
	/**
 	 * Metodo que verifica se é permitido a assinatura pelo papel e tipo do documento
 	 * @param ProcessoDocumento
 	 */
	public Boolean isPermiteAssinaturaPeloPapel(ProcessoDocumento processoDocumento) {
		Boolean retorno = true;
		if (processoDocumento==null){
			retorno = false;
		}else{
			retorno = !tipoProcessoDocumentoPapelService.verificarExigibilidadeNaoAssina(
	 				Authenticator.getPapelAtual(), processoDocumento.getTipoProcessoDocumento());
		}
 		return retorno;
	}
	
	public String getCertChain() {
		return certChain;
	}

	public void setCertChain(String certChain) {
		this.certChain = certChain;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getUrlDocsField() {
		StringBuilder sb = new StringBuilder();
		for (ProcessoDocumento pd : getDocumentosAssinar()) {
			if (sb.length() > 0) {
				sb.append(',');
			}
			sb.append(gerarLinkDownload(pd));
		}
		return sb.toString();
	}

	/**
	 * Adiciona ou remove o documento a lista de decumentos que são assinados
	 * 
	 * @param processoDocumento
	 */
	public void addRemoveRowList(ProcessoDocumento processoDocumento) {
		if (getDocumentosAssinar().contains(processoDocumento)) {
			getDocumentosAssinar().remove(processoDocumento);
		} else {
			getDocumentosAssinar().add(processoDocumento);
			if(!anexosAssinar.isEmpty()) {
				setExibeModalAssinarAnexos(true);
			}
		}
		setPdTemp(null);
	}

	private String gerarLinkDownload(ProcessoDocumento pd) {
		StringBuilder sb = new StringBuilder();
		sb.append("id=");
		sb.append(String.valueOf(pd.getIdProcessoDocumento()));
		sb.append("&codIni=");
		sb.append(ProcessoDocumentoHome.instance().getCodData(pd));
		sb.append("&md5=");
		sb.append(pd.getProcessoDocumentoBin().getMd5Documento());
		sb.append("&isBin=");
		sb.append(pd.getProcessoDocumentoBin().getExtensao() != null);
		return sb.toString();
	}

	public List<ProcessoDocumento> getSelectedRowsList() {
		return getDocumentosAssinar();
	}

	public void addRemoveRowProcessoPendenteList(ProcessoTrf processoTrf) {
		if (getProcessoTrfPendenteList().contains(processoTrf)) {
			getProcessoTrfPendenteList().remove(processoTrf);
		} else {
			getProcessoTrfPendenteList().add(processoTrf);
		}
	}

	/**
	 * Realiza a remocao do documento selecionado
	 * 
	 * @param documento, idProcessoTrf
	 */
	public void removerDocumento(ProcessoDocumento documento, Integer idProcessoTrf) {
		ProcessoTrfHome.instance().setId(idProcessoTrf);
		ProcessoDocumentoHome.instance().excluirDocNaoAssinado(documento);
		this.isRemocao = true;
		limparLista();
	}
 
	/**
	 * Limpa a lista de documentos pendentes
	 */
	public void limparLista() {
		selecionarMensagemDocumentoAssinadoOuExcluido();
		setDocumentosAssinar(new ArrayList<ProcessoDocumento>());
		setDocumentos(null);
		setProcessoTrfList(new ArrayList<ProcessoTrf>());
		setProcessoTrfPendenteList(new ArrayList<ProcessoTrf>());
		setCheckHeader(false);
		ProcessoDocumentoHome.instance().setCheckBox(false);
	}
	
	/**
	 * Seleciona a mensagem conforme a situação do documento, assinado ou excluido.
	 */
	public void selecionarMensagemDocumentoAssinadoOuExcluido(){
		FacesMessages.instance().clear();
		if(this.isRemocao){
			FacesMessages.instance().add(Severity.INFO, FacesUtil.getMessage("ConsultaDocnaoAssinado_deleted"));
		}else{
			FacesMessages.instance().add(Severity.INFO, FacesUtil.getMessage("ConsultaDocnaoAssinado.documento.assinado"));
		}
	}

	public void setDocumentosAssinar(List<ProcessoDocumento> documentosAssinar) {
		this.documentosAssinar = documentosAssinar;
	}

	public List<ProcessoDocumento> getDocumentosAssinar() {
		return documentosAssinar;
	}
	

	public List<ProcessoDocumento> getAnexosAssinar() {
		return anexosAssinar;
	}

	public void setAnexosAssinar(List<ProcessoDocumento> anexosAssinar) {
		this.anexosAssinar = anexosAssinar;
	}

	public void setContDoc(Integer contDoc) {
		this.contDoc = contDoc;
	}

	public Integer getContDoc() {
		return contDoc;
	}

	public void checkAll() {
		if (checkBox) {
			
			// limpa a lista dos documentos para assinar
			getDocumentosAssinar().clear();
			List<ProcessoDocumento> listaDocsAssinatura = retornaListaDocumentosParaAssinatura();
			List<ProcessoDocumento> listaDocsPermitidosAssinatura = new ArrayList<ProcessoDocumento>();
			
			for(ProcessoDocumento pd : listaDocsAssinatura)
			{
				if(!pd.getProcessoTrf().getInBloqueiaPeticao()){					
					listaDocsPermitidosAssinatura.add(pd);					
				}					
			}
			
			// adiciona todos com permissão de assinatura na lista de documentos para assinar
			getDocumentosAssinar().addAll(listaDocsPermitidosAssinatura);
			
			// verifica se os documentos possuem anexos e inserem/removem na lista para assinatura
			List<ProcessoDocumento> listaTemp = new ArrayList<>();
			
			for(ProcessoDocumento pd : getDocumentosAssinar()) {
				anexosAssinar = new ArrayList<>();
				anexosAssinar = listaPDFAssociados(pd);
				for(ProcessoDocumento anexo : anexosAssinar) {
					listaTemp.add(anexo);
				}
			}
			for(ProcessoDocumento anexo : listaTemp) {
				addRemoveRowList(anexo);
			}
			// verifica se algum documento tem processo remetido ao segundo grau
			verificaMultiplosRemetidos();
		} else {
			getDocumentosAssinar().clear();
			setExibeModalMultiplosRemetidos(false);
		}
		
	}

	/**
	 * Metodo que retorna a lista de documentos selecionados no marcar todos e 
	 * verifica se o usuario logado tem permissao de assinatura pelo papel e
	 * tipo de documento
	 * - E que o documento (ou seu pai [se houver]) não esteja marcado para ser tratado exclusivamente em atividade especifica
	 * exemplo de atividades exclusivas: diligencia, fluxo, ata de audiencia
	 * @return List<ProcessoDocumento>
	 */
	protected List<ProcessoDocumento> retornaListaDocumentosParaAssinatura() {
		List<ProcessoDocumento> listaDocsAssinatura = new ArrayList<>();
		List<ProcessoDocumento> listaDocsAssinaturaTemp = getDocumentos().getList();
		for (ProcessoDocumento processoDocumento : listaDocsAssinaturaTemp) {
			if (isPermiteAssinaturaPeloPapel(processoDocumento) 
					&& !processoDocumento.getExclusivoAtividadeEspecifica()
					&& (processoDocumento.isDocumentoPai() && (processoDocumento.getDocumentoPrincipal() == null || !processoDocumento.getDocumentoPrincipal().getExclusivoAtividadeEspecifica()))){

				listaDocsAssinatura.add(processoDocumento);	
			}
		}
		return listaDocsAssinatura;

	}

	/**
	 * Verifica quais dos doscumentos são de processo remetidos ao segundo grau
	 */
	public void verificaMultiplosRemetidos() {
		// limpa a lista dos documentos remetidos
		documentosProcessoRemetidoList.clear();

		// limpa lista dos processos remetidos
		processoTrfRemetidoList.clear();

		// varre a lista dos documentos para assinar
		for (ProcessoDocumento pd : documentosAssinar) {
			// pega o ProcessoTrf do processo corrente
			ProcessoTrf processoTrf = EntityUtil.find(ProcessoTrf.class, pd.getProcesso().getIdProcesso());

			// verifica se o processo foi remetido para o segundo grau
			if (ProcessoTrfHome.instance().verificaRemetido2Grau(processoTrf)) {
				// adiciona a lista dos documentos remetidos
				documentosProcessoRemetidoList.add(pd);
				// verifica e adiciona o prosso a lista de processos remetidos
				if (!processoTrfRemetidoList.contains(processoTrf)) {
					processoTrfRemetidoList.add(processoTrf);
				}
			}
		}
		// verifica se exite multiplos documentos remetidos para exibir o modal
		if (!documentosProcessoRemetidoList.isEmpty()) {
			setExibeModalMultiplosRemetidos(true);
		}
	}

	public Boolean getCheckAll() {
		return checkAll;
	}

	public void setCheckAll(Boolean checkAll) {
		this.checkAll = checkAll;
	}

	public void setProcessoTrfList(List<ProcessoTrf> processoTrfList) {
		this.processoTrfList = processoTrfList;
	}

	public List<ProcessoTrf> getProcessoTrfList() {
		return processoTrfList;
	}

	public void setProcessoTrfPendenteList(List<ProcessoTrf> processoTrfPendenteList) {
		this.processoTrfPendenteList = processoTrfPendenteList;
	}

	public List<ProcessoTrf> getProcessoTrfPendenteList() {
		return processoTrfPendenteList;
	}

	public void verificacaoLote() {
		for (ProcessoDocumento pd : getDocumentosAssinar()) {
			ProcessoTrf processoTrf = EntityUtil.find(ProcessoTrf.class, pd.getProcesso().getIdProcesso());
			ProcessoTrfHome.instance().setInstance(processoTrf);
			if (ProcessoExpedienteHome.instance().existeExpedientePendente() && !processoTrfList.contains(processoTrf)) {
				processoTrfList.add(processoTrf);
			}
		}
		if (processoTrfList.isEmpty()) {
			limparLista();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void concluirAssinaturaPJeOffice(){
		try {
			List<ArquivoAssinadoHash> arquivosAssinadosHash = (List<ArquivoAssinadoHash>)Contexts.getSessionContext().get("listaArquivoAssinadoHash");
			if(arquivosAssinadosHash != null && !arquivosAssinadosHash.isEmpty()){
				this.documentoJudicialService.gravarAssinaturaDeProcessoDocumento(arquivosAssinadosHash, documentosAssinar);
				dataJuntadaAposAssinar();
				for(ProcessoDocumento documento : documentosAssinar){
					documentoJudicialService.dispararFluxo(documento);
				}
			}
			Contexts.getSessionContext().remove("listaArquivoAssinadoHash");
		} catch (Exception e) {
			Contexts.getSessionContext().remove("listaArquivoAssinadoHash");
			e.printStackTrace();
		}
	}
	
	/**
	 * Método chamado apos uma assinatura com sucesso
	 */
	public void concluirAssinatura() {
		dataJuntadaAposAssinar();
		for(ProcessoDocumento documento : getDocumentosAssinar()){
			this.documentoJudicialService.dispararFluxo(documento);
		}
	}
	
	/** 
	 * Metodo responsável por setar a data de juntada em uma lista de documentos 
	 * que foram assinados pela opção "Assinar documentos Pendentes" e são de processos distribuidos.
	 */
	private void dataJuntadaAposAssinar() {
		ProcessoTrf processoTrf = null;
		for (ProcessoDocumento pd : documentosAssinar) {
			if(processoTrf == null || processoTrf.getProcesso().getIdProcesso() != pd.getProcesso().getIdProcesso())
				processoTrf = EntityUtil.find(ProcessoTrf.class, pd.getProcesso().getIdProcesso());
			if(processoTrf.getProcessoStatus() == ProcessoStatusEnum.D) {
				pd.setDataJuntada(new Date());
				EntityUtil.getEntityManager().persist(pd);
				EntityUtil.flush();
			}
		}
		
	}
	
	/**
	 * Lançar movimentação de documentos pendentes de assinatura
	 */
	public void lancarMovimentosAposAssinatura() {
		ProcessoTrf processoTrf = null;
		for (ProcessoDocumento pd : documentosAssinar) {
			if(pd.isDocumentoPai() && pd.getProcessoDocumentoBin().getSignatarios() != null && !pd.getProcessoDocumentoBin().getSignatarios().isEmpty()) {
				if(processoTrf == null || processoTrf.getProcesso().getIdProcesso() != pd.getProcesso().getIdProcesso()){
					processoTrf = EntityUtil.find(ProcessoTrf.class, pd.getProcesso().getIdProcesso());
				}
				if (ProcessoStatusEnum.D.equals(processoTrf.getProcessoStatus())) {
					MovimentoAutomaticoService.preencherMovimento().
					deCodigo(85).
					comProximoComplementoVazio().
					doTipoLivre().
					preencherComTexto(pd.getTipoProcessoDocumento().getTipoProcessoDocumento().toLowerCase()).
					associarAoProcesso(processoTrf).
					associarAoDocumento(pd).
					lancarMovimento();
				}
			}
		}
	}

	@SuppressWarnings("serial")
	public EntityList<ProcessoTrf> entityListProcessoTrf() {
		EntityList<ProcessoTrf> entityListProcesso = new EntityList<ProcessoTrf>() {
			@Override
			protected String getDefaultOrder() {
				return null;
			}

			@Override
			protected String getDefaultEjbql() {
				return null;
			}

			@Override
			protected Map<String, String> getCustomColumnsOrder() {
				return null;
			}

			@Override
			protected void addSearchFields() {
			}

			@Override
			public void setOrderedColumn(String order) {
			}

			@Override
			public Long getResultCount() {
				Integer tamanhoLista = processoTrfList.size();
				return Long.parseLong(String.valueOf(tamanhoLista));
			}
		};
		return entityListProcesso;
	}

	public void retirarPendentes() {
		for (ProcessoTrf processoTrf : processoTrfPendenteList) {
			ProcessoTrfHome.instance().setInstance(processoTrf);
			ProcessoExpedienteHome.instance().retirarPendencia();
		}
		limparLista();
	}

	public boolean mostrarModal() {
		boolean showModal = false;
		if (!getProcessoTrfList().isEmpty()) {
			for (ProcessoDocumento pd : getDocumentosAssinar()) {
				ProcessoTrf processoTrf = EntityUtil.find(ProcessoTrf.class, pd.getProcesso().getIdProcesso());
				ProcessoTrfHome.instance().setInstance(processoTrf);
				if (ProcessoExpedienteHome.instance().existeExpedientePendente()) {
					showModal = ProcessoHome.instance().verificarPessoaAssinatura(pd);
				}
			}
		}
		return showModal;
	}

	public void setCheckHeader(Boolean checkHeader) {
		this.checkHeader = checkHeader;
	}

	public Boolean getCheckHeader() {
		return checkHeader;
	}

	public void marcarTodos() {
		processoTrfPendenteList.clear();
		if (checkHeader) {
			processoTrfPendenteList.addAll(processoTrfList);
		}
	}

	public void setExibeModalRemetidos(boolean exibeModalRemetidos) {
		this.exibeModalRemetidos = exibeModalRemetidos;
	}

	public boolean isExibeModalRemetidos() {
		return exibeModalRemetidos;
	}

	public void setDocumentosProcessoRemetidoList(List<ProcessoDocumento> documentosProcessoRemetidoList) {
		this.documentosProcessoRemetidoList = documentosProcessoRemetidoList;
	}

	public List<ProcessoDocumento> getDocumentosProcessoRemetidoList() {
		return documentosProcessoRemetidoList;
	}

	public void setProcessoTrfRemetidoList(List<ProcessoTrf> processoTrfRemetidoList) {
		this.processoTrfRemetidoList = processoTrfRemetidoList;
	}

	public List<ProcessoTrf> getProcessoTrfRemetidoList() {
		return processoTrfRemetidoList;
	}

	public void setPdTemp(ProcessoDocumento pdTemp) {
		this.pdTemp = pdTemp;
	}

	public ProcessoDocumento getPdTemp() {
		return pdTemp;
	}

	public void setCheckBox(boolean checkBox) {
		this.checkBox = checkBox;
	}

	public boolean isCheckBox() {
		return checkBox;
	}

	public void setExibeModalMultiplosRemetidos(boolean exibeModalMultiplosRemetidos) {
		this.exibeModalMultiplosRemetidos = exibeModalMultiplosRemetidos;
	}

	public boolean isExibeModalMultiplosRemetidos() {
		return exibeModalMultiplosRemetidos;
	}

	/**
	 * Verifica se o processo selecionado encontra-se remetido para o segundo
	 * grau para exibição ou não do modal de confirmação.
	 */
	public void verificaRemetido(ProcessoDocumento processoDocumento) {		
		setExibeModalAssinarAnexos(false);
		anexosAssinar = new ArrayList<>();
		if (ProcessoTrfHome.instance().verificaRemetido2Grau(
				EntityUtil.find(ProcessoTrf.class, processoDocumento.getProcesso().getIdProcesso()))) {
			setExibeModalRemetidos(true);
			setPdTemp(processoDocumento);
			return;
		}
		setExibeModalRemetidos(false);
		addRemoveRowList(processoDocumento);
		anexosAssinar = listaPDFAssociados(processoDocumento);
		verificaAnexos();
	}

	public void reRenderGrid() {
		Contexts.removeFromAllContexts("documentosNaoAssinadosGrid");
	}

	/**
	 * Verifica se entre os processo selecionados para assinar exite algum como
	 * remetido para o segundo grau
	 */
	public void preparaAssinatura() {
		for (ProcessoDocumento pd : documentosAssinar) {
			ProcessoTrf processoTrf = EntityUtil.find(ProcessoTrf.class, pd.getProcesso().getIdProcesso());
			if (ProcessoTrfHome.instance().verificaRemetido2Grau(processoTrf)) {
				documentosProcessoRemetidoList.add(pd);
				if (!processoTrfRemetidoList.contains(processoTrf)) {
					processoTrfRemetidoList.add(processoTrf);
				}
			}
		}

		// verifica se exite processo remetidos para exibir o modal
		if (!documentosProcessoRemetidoList.isEmpty()) {
			setExibeModalRemetidos(true);
		}
	}

	/**
	 * Remove os documentos de processos remetidos ao segundo grau da lista de
	 * documentos para assinar e assina os remanecentes
	 */
	public void removeRemetidos() {
		for (ProcessoDocumento pdRemetido : documentosProcessoRemetidoList) {
			documentosAssinar.remove(pdRemetido);
		}
		verificacaoLote();
		FacesMessages.instance().clear();
	}
	
	public boolean isExibeModalAssinarAnexos() {
		return exibeModalAssinarAnexos;
	}

	public void setExibeModalAssinarAnexos(boolean exibeModalAssinarAnexos) {
		this.exibeModalAssinarAnexos = exibeModalAssinarAnexos;
	}

	@SuppressWarnings("unchecked")
	public List<ProcessoDocumento> listaPDFAssociados(ProcessoDocumento pd) {
		ArrayList<ProcessoDocumento> lista = new ArrayList<>();
		if (pd.getIdProcessoDocumento() != 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("select o from ProcessoDocumento o ");
			sb.append("where o.documentoPrincipal.idProcessoDocumento = :id");
			Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
			q.setParameter("id", pd.getIdProcessoDocumento());
			lista = (ArrayList<ProcessoDocumento>) q.getResultList();
		}
		return lista;
	}
	
	public void verificaAnexos() {
		ProcessoDocumentoBinPessoaAssinaturaHome home = ProcessoDocumentoBinPessoaAssinaturaHome.instance();
		
		for(ProcessoDocumento pd : anexosAssinar) {
			ProcessoDocumentoBin bin = pd.getProcessoDocumentoBin();
			int id = bin.getIdProcessoDocumentoBin();
			
			Boolean temAssinatura = home.listaAssinatura(id + "");
			//verifica se o arquivo ainda não foi assinado
			if(temAssinatura == null || !temAssinatura) {
				addRemoveRowList(pd);
			}	
		}
	}
	
	public void limparCamposPesquisa() {
		clearCpfCnpj();
		processoDocumentoConsultaNaoAssinadoVO = new ProcessoDocumentoConsultaNaoAssinadoVO();
	}

	public void clearCpfCnpj(){
		processoDocumentoConsultaNaoAssinadoVO.setCpf(null);
		processoDocumentoConsultaNaoAssinadoVO.setCnpj(null);
	}
	
	/**
	 * Metodo que retorna lista de documentos para assinatura 
	 * @return List<ProcessoDocumento>
	 */
	public PaginatedDataModel<ProcessoDocumento> getDocumentos() {
 		if (documentos==null){
 			documentos = processoDocumentoManager.recuperarDocumentosNaoAssinados(processoDocumentoConsultaNaoAssinadoVO);
 		}
 		return documentos;
	}
	
	public void setDocumentos(PaginatedDataModel<ProcessoDocumento> documentos) {
 		this.documentos = documentos;
	}
	
	public ProcessoDocumentoConsultaNaoAssinadoVO getProcessoDocumentoConsultaNaoAssinadoVO() {
 		if (processoDocumentoConsultaNaoAssinadoVO==null){
 			processoDocumentoConsultaNaoAssinadoVO = new ProcessoDocumentoConsultaNaoAssinadoVO();
 		}
 		return processoDocumentoConsultaNaoAssinadoVO;
	}
	
	public void setProcessoDocumentoConsultaNaoAssinadoVO(
 			ProcessoDocumentoConsultaNaoAssinadoVO processoDocumentoConsultaNaoAssinadoVO) {
 		this.processoDocumentoConsultaNaoAssinadoVO = processoDocumentoConsultaNaoAssinadoVO;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash)	throws Exception {
		addArquivoAssinado(arquivoAssinadoHash);
		List<ArquivoAssinadoHash> arqAssinados = (List<ArquivoAssinadoHash>)Contexts.getSessionContext().get("listaArquivoAssinadoHash");
		if(arqAssinados != null && !arqAssinados.isEmpty()){
			arquivosAssinados.addAll(arqAssinados);
		}
		Contexts.getSessionContext().set("listaArquivoAssinadoHash", getArquivosAssinados());
	}

	public List<ArquivoAssinadoHash> getArquivosAssinados() {
		return arquivosAssinados;
	}

	public void setArquivosAssinados(List<ArquivoAssinadoHash> arquivosAssinados) {
		this.arquivosAssinados = arquivosAssinados;
	}

	/**
	 * Metodo responsavel por adicionar um arquivo assinado pelo assinador
	 * 
	 * @param arquivoAssinadoHash O arquivo assinado que sera adicionado
	 */
	public void addArquivoAssinado(ArquivoAssinadoHash arquivoAssinadoHash) {
		this.arquivosAssinados.add(arquivoAssinadoHash);
	}
}