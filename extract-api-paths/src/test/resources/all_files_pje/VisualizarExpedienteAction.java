package br.jus.cnj.pje.view;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.FacesUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoExpedienteManager;
import br.jus.cnj.pje.nucleo.service.AtoComunicacaoService;
import br.jus.csjt.pje.business.pdf.GeradorPdfUnificado;
import br.jus.csjt.pje.business.pdf.PdfException;
import br.jus.je.pje.entity.vo.BinarioVO;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.util.StringUtil;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;

@Name("visualizarExpedienteAction")
@Scope(ScopeType.PAGE)
public class VisualizarExpedienteAction extends
		BaseAction<ProcessoParteExpediente> {

	private static final long serialVersionUID = -4243532242425702228L;

	@RequestParameter(value = "idProcessoParteExpediente")
	private Integer paramIdProcessoParteExpediente;

	@RequestParameter(value = "paramIdProcessoDocumentoBin")
	private Integer paramIdProcessoDocumentoBin;

	@RequestParameter(value = "paramIdProcessoDocumento")
	private Integer paramIdProcessoDocumento;
	
	@RequestParameter(value = "idProcesso")
 	private Integer paramIdProcesso;
		
	private ProcessoDocumentoBin procDocBin;

	private ProcessoParteExpediente procPartExp;

	private static final int TAMANHO_CODIGO_ID = 14;

	private static final String SF_MASK_DTCADASTRO = "yyMMddHHmmssSSS";
	
	private static final String IdentificadorDoParametroGet = "?x=";
	
	private static final Integer TamanhoLadoQRCodeImg = 125;
	
	private List<ProcessoDocumento> listaDocumentosVinculados;

	@Create
	public void init() {
		try {
			if (paramIdProcessoParteExpediente != null) {
				processarParteExpediente();
			} else if (paramIdProcessoDocumentoBin != null) {
				procDocBin = ComponentUtil.getProcessoDocumentoBinManager().findById(paramIdProcessoDocumentoBin);
				if (isProcDocBinValidadoEAcessivel()) {
					limparObjetosConsultados();
					mostrarMensagemDocumentoNaoEncontrado();
					return;
				}
				if(procDocBin.isBinario()) {
					BinarioVO binario = new BinarioVO();
					binario.setIdBinario(procDocBin.getIdProcessoDocumentoBin());
					binario.setMimeType(procDocBin.getExtensao());
					binario.setNomeArquivo(procDocBin.getNomeArquivo());
					binario.setNumeroStorage(procDocBin.getNumeroDocumentoStorage());
					Contexts.getSessionContext().set("download-binario", binario);
				}
				
				
			}
		} catch (PJeBusinessException e) {
			facesMessages.addFromResourceBundle(Severity.WARN,"msg.documento.nao.encontrado");
			e.printStackTrace();
		}
	}

	private boolean isProcDocBinValidadoEAcessivel() {
		return procDocBin == null 
				|| isInformadoApenasParamIdProcessoDocumentoBin()
				|| (paramIdProcesso != null && procDocBin.getProcessoDocumentoList().stream().noneMatch(pd -> pd.getProcesso().getIdProcesso() == paramIdProcesso.intValue())) 
				|| procDocBin.getProcessoDocumentoList().stream().anyMatch(pd -> !ComponentUtil.getComponent(DocumentoJudicialService.class).isDocumentoVisivel(pd.getIdProcessoDocumento()));
	}

	private boolean isInformadoApenasParamIdProcessoDocumentoBin() {
		return paramIdProcessoDocumentoBin != null && paramIdProcesso == null && paramIdProcessoDocumento == null;
	}

	/**
	 * Método usado para processar o processoDocumento da ParteExpediente.
	 * @throws PJeBusinessException
	 */
 	
	private void processarParteExpediente() throws PJeBusinessException {
		procPartExp = ComponentUtil.getProcessoParteExpedienteManager().findById(paramIdProcessoParteExpediente);
		if(procPartExp != null 
				&& isExpedienteConsultadoCoincidenteComProcessoParametro()
				&& isDocumentoBinDoExpedienteConsultadoCoincidenteComParametro()
				&& ComponentUtil.getComponent(DocumentoJudicialService.class).isDocumentoVisivel(procPartExp.getProcessoDocumento().getIdProcessoDocumento())){
			recuperarProcessoDocumentoBin();
			if(Authenticator.isUsuarioExterno()){
				AtoComunicacaoService atoComunicacaoService = ComponentUtil.getAtoComunicacaoService();
				if(procPartExp.getDtCienciaParte() == null && atoComunicacaoService.aptoParaCiencia(procPartExp)){						
					atoComunicacaoService.registraCienciaPessoal(procPartExp);
				} else if(ComponentUtil.getProcessoParteExpedienteManager().temDocumentoPendenteCiencia(procPartExp)){
					limparObjetosConsultados();
					mostrarMensagemDocumentoPendenteCienciaDestinatario();
				}
			}
		}else{

			mostrarMensagemDocumentoPendenteCienciaDestinatario();
		}
	}
	
	private boolean isExpedienteConsultadoCoincidenteComProcessoParametro() {
		return procPartExp != null && paramIdProcesso != null && procPartExp.getIdProcessoJudicial().equals(paramIdProcesso);
	}
	
	private boolean isDocumentoBinDoExpedienteConsultadoCoincidenteComParametro() {
		return paramIdProcessoDocumentoBin == null || (paramIdProcessoDocumentoBin.equals(procPartExp.getProcessoDocumento().getProcessoDocumentoBin().getIdProcessoDocumentoBin()));
	}

	/**
	 * Metodo usado para mostrar informar que o usuario destinatario ainda nao deu ciencia.
	 * 
	 */
	private void limparObjetosConsultados() {
		procPartExp = null;
		procDocBin = null;
	}

	private void mostrarMensagemDocumentoPendenteCienciaDestinatario() {
		procDocBin = new ProcessoDocumentoBin();
		procDocBin.setModeloDocumento(FacesUtil.getMessage("msg.documento.pendente.ciencia.destinatario"));
		facesMessages.addFromResourceBundle("msg.documento.pendente.ciencia.destinatario");
	}

	private void mostrarMensagemDocumentoNaoEncontrado() {
		facesMessages.addFromResourceBundle("msg.documento.nao.encontrado");
	}

	/**
	 * Metodo usado para recuperar um ProcessoDocumento de uma ParteExpediente, o atributo procPartExp nao deve ser null.
	 * @throws PJeBusinessException
	 */
 	
	private void recuperarProcessoDocumentoBin() throws PJeBusinessException {
		procDocBin = ComponentUtil.getProcessoDocumentoBinManager().findById(procPartExp.getProcessoDocumento().getProcessoDocumentoBin().getIdProcessoDocumentoBin());
	}
 	
	/**
	 * Fornece OutputStream com imagem em PNG de QRCode codificado com URL de validacao de um expediente
	 * @param out
	 * @param data
	 * @throws IOException
	 */
	public void gerarPNGBarcode(OutputStream out, Object data)
			throws IOException {
		if (procDocBin == null) {
			return;
		}
		QRCode.from(geraUrlValidacaoDocumento()).to(ImageType.PNG).withSize(TamanhoLadoQRCodeImg, TamanhoLadoQRCodeImg).writeTo(out);
	}

	private String geraUrlValidacaoDocumento() {
		StringBuilder urlConsulta = new StringBuilder(getUrlValidacao().toString());
		urlConsulta.append(IdentificadorDoParametroGet);
		urlConsulta.append(getCodigoValidacaoDocumento(procDocBin));
		return urlConsulta.toString();
	}

	public String getCodigoValidacaoDocumento(ProcessoDocumentoBin pd) {
		if (pd == null) {
			return null;
		}
		SimpleDateFormat sd = new SimpleDateFormat(SF_MASK_DTCADASTRO);
		String idDoc = Integer.toString(pd.getIdProcessoDocumentoBin());
		idDoc = StringUtil.completaZeros(idDoc, TAMANHO_CODIGO_ID);
		int length = idDoc.length();
		idDoc = length > 14 ? idDoc.substring(length - TAMANHO_CODIGO_ID + 1,
				length - 1) : idDoc;
		return sd.format(pd.getDataInclusao()) + idDoc;
	}

	public String getUrlValidacao() {
		return new Util().getUrlProject()
				+ "/Processo/ConsultaDocumento/listView.seam";
	}

	@Override
	protected BaseManager<ProcessoParteExpediente> getManager() {
		return ComponentUtil.getProcessoParteExpedienteManager();
	}

	@Override
	public EntityDataModel<ProcessoParteExpediente> getModel() {
		return null;
	}

	public ProcessoDocumentoBin getProcDocBin() {
		return procDocBin;
	}

	public void setProcDocBin(ProcessoDocumentoBin procDocBin) {
		this.procDocBin = procDocBin;
	}

	public ProcessoParteExpediente getProcPartExp() {
		return procPartExp;
	}

	public void setProcPartExp(ProcessoParteExpediente procPartExp) {
		this.procPartExp = procPartExp;
	}

	/**
	 * Retorna a lista de processo documento vinculado ao expediente.
	 * 
	 * @return Uma lista de ProcessoDocumento
	 */
	public List<ProcessoDocumento> retornaListaProcessoDocumentoVinculadoExpediente() {
		ProcessoDocumento processoDocumento = null;
		if(listaDocumentosVinculados == null){
			if(procPartExp != null){
				listaDocumentosVinculados = ComponentUtil.getComponent(ProcessoDocumentoExpedienteManager.class).getListaProcessoDocumentoVinculadoExpediente(procPartExp.getProcessoExpediente());
			}else if(paramIdProcessoDocumento != null){
				processoDocumento = EntityUtil.getEntityManager().getReference(ProcessoDocumento.class, paramIdProcessoDocumento);
				listaDocumentosVinculados = ComponentUtil.getProcessoDocumentoManager().getDocumentosVinculados(processoDocumento, true);
			}
		}
		return listaDocumentosVinculados;
	}

	public Integer getParamIdProcessoDocumento() {
		return paramIdProcessoDocumento;
	}

	public void setParamIdProcessoDocumento(Integer paramIdProcessoDocumento) {
		this.paramIdProcessoDocumento = paramIdProcessoDocumento;
	}
	
	/**
 	 * Verifica se um processo foi protocolado
 	 * 
 	 * @return verdadeiro ou falso
 	 */
 	public boolean isProcessoProtocolado(){
 		if(paramIdProcesso != null){
 			return ComponentUtil.getProcessoTrfManager().isProcessoProtocolado(paramIdProcesso);
 		}
 		
 		return false;
 	}
 	
 	public void imprimirPdf(ProcessoDocumento procDoc){
		String extensao = ".pdf";

		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
		response.reset();
		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "attachment; filename=\""	+ 
							procDoc.getProcesso().getNumeroProcesso() + "_" +
							procDoc.getIdProcessoDocumento() + 
							extensao + "\"");
		
		gerarPdfSimples(request, response, procDoc);
		getPjeUtil().registrarCookieTemporizadorDownload(response);
		facesContext.responseComplete();
	}
 	
	private PjeUtil getPjeUtil() {
		return ComponentUtil.getComponent(PjeUtil.class);
	}
 	
 	private void gerarPdfSimples(HttpServletRequest request, HttpServletResponse response, ProcessoDocumento procDoc){
		
		String resourcePath = request.getScheme() + "://"
				+ request.getServerName() + ":" + request.getServerPort()
				+ request.getContextPath();
		
		List<ProcessoDocumento> documentos = new ArrayList<ProcessoDocumento>();
		documentos.add(procDoc);
		OutputStream out = null;
		try {
			GeradorPdfUnificado geradorPdf = new GeradorPdfUnificado();
			geradorPdf.setResurcePath(resourcePath);
			out = response.getOutputStream();
			geradorPdf.gerarPdfSimples(documentos, out);
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
}
