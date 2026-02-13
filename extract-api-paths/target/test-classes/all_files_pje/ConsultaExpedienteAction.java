package br.jus.cnj.pje.view;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.home.ProcessoDocumentoComparator;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.itx.component.Util;
import br.jus.cnj.pje.extensao.PontoExtensaoException;
import br.jus.cnj.pje.nucleo.manager.LogConsultaExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.csjt.pje.business.pdf.GeradorPdfUnificado;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.util.Crypto;

@Name("consultaExpedienteAction")
@Scope(ScopeType.CONVERSATION)
public class ConsultaExpedienteAction implements Serializable {

	private static final long serialVersionUID = 7323676136678404503L;
	
	@In
	private FacesMessages facesMessages;
	
	@Logger
	private Log log;
	
	@In
	private ProcessoExpedienteManager processoExpedienteManager;

	@In
	private ProcessoParteExpedienteManager processoParteExpedienteManager;
	
	@In
	private LogConsultaExpedienteManager logConsultaExpedienteManager;
	
	private String idProcessoExpedienteString;
	public String getIdProcessoExpedienteString() {
		log.info("PEGANDO O ID EXPEDIENTE " + idProcessoExpedienteString);
		return idProcessoExpedienteString;
	}
	public void setIdProcessoExpedienteString(String idProcessoExpedienteString) {
		log.info("ATRIBUINDO O ID EXPEDIENTE " + idProcessoExpedienteString);
		this.idProcessoExpedienteString = idProcessoExpedienteString;
	}
	
	@Create
	public void init() {
		log.info("CRIANDO O OBJETO consultaExpedienteAction");
	}
	
	private static byte[] longToBytes(long x) {
	    ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE/Byte.SIZE);
	    buffer.putLong(x);
	    return buffer.array();
	}

	private static long bytesToLong(byte[] bytes) {
	    ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE/Byte.SIZE);
	    buffer.put(bytes);
	    buffer.flip();
	    return buffer.getLong();
	}
	
	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
	private static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	private static byte[] hexToBytes(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i + 1), 16));
	    }
	    return data;
	}
	
	public static String criptografaIdProcessoParteExpediente(long idProcessoParteExpediente) {
		Crypto crypto = new Crypto(ProjetoUtil.getChaveCriptografica());
		byte[] idProcessoParteExpedienteBytes = longToBytes(idProcessoParteExpediente);
		idProcessoParteExpedienteBytes = Arrays.copyOfRange(idProcessoParteExpedienteBytes, 4, 8); 
		byte[] idProcessoParteExpedienteCriptografado = crypto.encodeDES(idProcessoParteExpedienteBytes);
		String idProcessoParteExpedienteCriptografadoHex = bytesToHex(idProcessoParteExpedienteCriptografado);
		idProcessoParteExpedienteCriptografadoHex = idProcessoParteExpedienteCriptografadoHex.replaceAll("([0-9A-Fa-f]{4})", "$1-");
		if (idProcessoParteExpedienteCriptografadoHex.endsWith("-")) {
			idProcessoParteExpedienteCriptografadoHex = idProcessoParteExpedienteCriptografadoHex.substring(0, idProcessoParteExpedienteCriptografadoHex.length() - 1);
		}
		return idProcessoParteExpedienteCriptografadoHex;
	}
	
	private static long descriptografaIdProcessoParteExpediente(String idProcessoParteExpedienteCriptografadoHex) {
		idProcessoParteExpedienteCriptografadoHex = idProcessoParteExpedienteCriptografadoHex.replaceAll("[^0-9A-Fa-f]", "");
		Crypto crypto = new Crypto(ProjetoUtil.getChaveCriptografica());
		byte[] idProcessoParteExpedienteCriptografado = hexToBytes(idProcessoParteExpedienteCriptografadoHex);
		byte[] idProcessoParteExpedienteBytes = crypto.decodeDES(idProcessoParteExpedienteCriptografado);
		byte[] idProcessoParteExpedienteBytesLong = new byte[] { 0,0,0,0,0,0,0,0 };
		System.arraycopy(idProcessoParteExpedienteBytes, 0, idProcessoParteExpedienteBytesLong, 4, 4);
		long idProcessoParteExpediente = bytesToLong(idProcessoParteExpedienteBytesLong);
		return idProcessoParteExpediente;
	}
	
	public void downloadDocumentosExpediente() throws PontoExtensaoException {
		facesMessages.clear();
		Context sessionContext = Contexts.getSessionContext();
		
		ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
		sessionContext.remove("org.jboss.seam.captcha.captcha");
		
		Map<String,String> requestParams = ctx.getRequestParameterMap();
		String idProcessoParteExpedienteStringLocal = requestParams.get("formConsultaExpediente:inputTextIdProcessoExpediente");
		
		String idProcessoParteExpedienteCriptografadoHex = idProcessoParteExpedienteStringLocal.replaceAll("[^0-9A-Fa-f]", "");
		if (idProcessoParteExpedienteCriptografadoHex.length() != 16) {
			facesMessages.add(Severity.ERROR, "Código de expediente inválido!");
			return;
		}
		
		try {
			long idProcessoParteExpediente = descriptografaIdProcessoParteExpediente(idProcessoParteExpedienteCriptografadoHex);
			ProcessoParteExpediente processoParteExpediente = processoParteExpedienteManager.findById(new Integer((int)idProcessoParteExpediente));
			HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
			String urlRequisicao = request.getRequestURL().toString();
			String ip = request.getRemoteAddr();
			logConsultaExpedienteManager.registrarConsultaExpediente(idProcessoParteExpedienteStringLocal, processoParteExpediente, urlRequisicao, ip);
			ProcessoExpediente processoExpediente = processoParteExpediente.getProcessoExpediente();
			downloadDocumentosExpediente(processoExpediente, true);
			facesMessages.clear();
		} catch(Throwable e) {
			e.printStackTrace();
			facesMessages.add(Severity.ERROR, "Erro ao baixar os documentos do expediente! ({0})", e.getLocalizedMessage());
			return;
		}
	}
	
	@Transactional
	private void downloadDocumentosExpediente(ProcessoExpediente processoExpediente, boolean isAbrirPdfComoDownload) {
		ProcessoTrf processoTrf = processoExpediente.getProcessoTrf();

		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse)facesContext.getExternalContext().getResponse();
		response.setContentType("application/pdf");
		
		String filename = "expediente";
		if (isAbrirPdfComoDownload) {
			String numeroProcesso = processoTrf.getNumeroProcesso();
			if (numeroProcesso != null && !numeroProcesso.trim().isEmpty()) {
				filename = numeroProcesso;
			}
			String extensao = ".pdf";
			response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + extensao + "\"");
		}

		OutputStream out = null;
		try {
			out = response.getOutputStream();
			geraDocumentosPdfUnificado(processoExpediente, true, out);
			out.flush();
			//registrarCookieTemporizadorDownload(response);
			facesContext.responseComplete();
		} catch (IOException ex) {
			FacesMessages.instance().add(Severity.ERROR, "Error while downloading the file: " + filename);
		} catch (Exception exc) {
			exc.printStackTrace();
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
	
	private void geraDocumentosPdfUnificado(
		ProcessoExpediente processoExpediente,
		boolean isGerarIndiceDosDocumentos,
		OutputStream out
	) throws IOException {
		List<ProcessoDocumento> processoDocumentoList = recuperaDocumentosExpediente(processoExpediente);
		ProcessoTrf processoTrf = processoExpediente.getProcessoTrf();
		geraDocumentosPdfUnificado(processoTrf, processoDocumentoList, isGerarIndiceDosDocumentos, false, out);
	}
	
	private List<ProcessoDocumento> recuperaDocumentosExpediente(ProcessoExpediente processoExpediente) {
		List<ProcessoDocumentoExpediente> processoDocumentoExpedienteList = processoExpediente.getProcessoDocumentoExpedienteList();
		List<ProcessoDocumento> processoDocumentoList = new ArrayList<ProcessoDocumento>(processoDocumentoExpedienteList.size());
		ProcessoDocumento documentoExpediente = processoExpediente.getProcessoDocumento();
		processoDocumentoList.add(documentoExpediente);
		for (ProcessoDocumentoExpediente processoDocumentoExpediente : processoDocumentoExpedienteList) {
			ProcessoDocumento processoDocumentoAnexo = processoDocumentoExpediente.getProcessoDocumento();
			if (processoDocumentoAnexo.getAtivo() 
				&& !processoDocumentoAnexo.getDocumentoSigiloso() 
				&& !listaProcessoDocumentoContemId(processoDocumentoList, processoDocumentoAnexo.getIdProcessoDocumento())
			) {
				processoDocumentoList.add(processoDocumentoAnexo);
			}
		}
		return processoDocumentoList;
	}
	
	private boolean listaProcessoDocumentoContemId(List<ProcessoDocumento> listaDocumentos, int idProcessoDocumento) {
		for (ProcessoDocumento processoDocumento : listaDocumentos) {
			if (processoDocumento.getIdProcessoDocumento() == idProcessoDocumento) {
				return true;
			}
		}
		return false;
	}

	private void geraDocumentosPdfUnificado(
		ProcessoTrf processoTrf, 
		List<ProcessoDocumento> processoDocumentoList,
		boolean isGerarIndiceDosDocumentos, 
		boolean isCrescente,
		OutputStream out
	) throws IOException {
		final boolean ordenarCrescente = isCrescente;

		Comparator<ProcessoDocumento> comparator = new ProcessoDocumentoComparator();
		if(ordenarCrescente){
			Collections.sort(processoDocumentoList,	comparator);	
		}else{
			Collections.sort(processoDocumentoList,	 Collections.reverseOrder(comparator));
		}

		try {
			GeradorPdfUnificado geradorPdf = new GeradorPdfUnificado();
			geradorPdf.setGerarIndiceDosDocumentos(isGerarIndiceDosDocumentos);
			geradorPdf.setResurcePath(new Util().getUrlProject());
			geradorPdf.gerarPdfUnificado(processoTrf, processoDocumentoList, out);
			out.flush();
		} catch (IOException ex) {
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
}