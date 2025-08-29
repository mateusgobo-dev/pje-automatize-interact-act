package br.jus.cnj.pje.view;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.utils.Constantes;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.pje.nucleo.util.StringUtil;


/**
 * Classe responsavel por realizar os upload de arquivos assinados da applet e do pjeOffice de forma unificada
 * 
 * Este classe recebera os dados da assinatura digital da applet ou do pjeoffice, atraves do atributo action 
 * esta classe ira recuperar a action no contexto do seam e encaminhar os dados da assinatura para esta action 
 * realizar os procedimentos necessarios.
 * 
 * @author pablo-moreira
 */
@Name("arquivoAssinadoUpload")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class ArquivoAssinadoUpload {

	public static final String CHARSET_APPLET = "ISO-8859-1";
	public static final String CHARSET_PJE_OFFICE = "UTF-8";
	
	/**
	 * Define a action que realizara o tratamento do arquivo assinado   
	 */
	private String action;
	
	/**
	 * Define o modo de operacao podendo ser A - Applet ou P - PJeOffice
	 */
	private String modoOperacao;	
	
	/**
	 * Define o id do arquivo assinado
	 */
	private String id;
	
	/**
	 * Define o codIni do arquivo assinado
	 */
	private String codIni;
	
	/**
	 * Define o hash md5 do arquivo assinado, sera utilizado pelo Applet
	 */
	private String md5;
	
	/**
	 * Define o hash do arquivo assinado, sera utilizado pelo PJeOffice
	 */
	private String hash;
	
	/**
	 * Define a assinatura do arquivo assinado, sera utilizado pelo Applet
	 */
	private String signature;
	
	/**
	 * Define a assinatura do arquivo assinado, sera utilizado pelo PJeOffice
	 */
	private String assinatura;
	
	/**
	 * Define a cadeia de certificado do signatario, sera utilizado pelo Applet
	 */
	private String certChain;
	
	/**
	 * Define a cadeia de certificado do signatario, sera utilizado pelo PJeOffice
	 */
	private String cadeiaCertificado;
	
	/**
	 * Define a resposta do servlet
	 */
	private HttpServletResponse response;
	
	/**
	 * Define a requisicao do servlet
	 */
	private HttpServletRequest request;

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getModoOperacao() {
		return modoOperacao;
	}

	public void setModoOperacao(String modoOperacao) {
		this.modoOperacao = modoOperacao;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCodIni() {
		return codIni;
	}

	public void setCodIni(String codIni) {
		this.codIni = codIni;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getCertChain() {
		return certChain;
	}

	public void setCertChain(String certChain) {
		this.certChain = certChain;
	}
	
	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getAssinatura() {
		return assinatura;
	}

	public void setAssinatura(String assinatura) {
		this.assinatura = assinatura;
	}

	public String getCadeiaCertificado() {
		return cadeiaCertificado;
	}

	public void setCadeiaCertificado(String cadeiaCertificado) {
		this.cadeiaCertificado = cadeiaCertificado;
	}

	private void init() {
		
		FacesContext fc = FacesContext.getCurrentInstance();
	
		if (fc != null) {
			this.response = (HttpServletResponse) fc.getExternalContext().getResponse();
			this.request = (HttpServletRequest) fc.getExternalContext().getRequest();
		}
	}
	
	public void doUploadArquivoAssinado() {
				
		init();
		
		try {
			if (StringUtil.isNullOrEmpty(getAction())) {
				throw new Exception("O nome da action não foi fornecido!");
			}
			
			Object component = Component.getInstance(getAction());
			
			if (component == null) {
				throw new Exception("Não foi possível recuperar a action: " + action + " na conversação!");
			}
			
			if (!(component instanceof ArquivoAssinadoUploader)) {
				throw new Exception("A action: " + action + " não implementa a interface ArquivoAssinadoUploader!");
			}
			
			ArquivoAssinadoUploader uploader = (ArquivoAssinadoUploader) component;
			
			ArquivoAssinadoHash arquivoAssinadoHash = criarArquivoAssinadoHash();
			
			if (StringUtil.isNullOrEmpty(arquivoAssinadoHash.getAssinatura())) {
				throw new RuntimeException("A assinatura do arquivo não foi fornecida!");
			}
			
			if (StringUtil.isNullOrEmpty(arquivoAssinadoHash.getCadeiaCertificado())) {
				throw new RuntimeException("A cadeia de certificado do signtario do arquivo não foi fornecida!");
			}
			
			if (StringUtil.isNullOrEmpty(arquivoAssinadoHash.getHash())) {
				throw new RuntimeException("O hash do arquivo assinado não foi fornecido!");
			}

			uploader.doUploadArquivoAssinado(request, arquivoAssinadoHash);
			
			if (isModoOperacaoPJeOffice()) {
				imprimirResposta("Sucesso", getPJeOfficeCharacterEncoding());
			}
			else {
				imprimirResposta("", request.getCharacterEncoding());
			}
		}
		catch (Exception e) {
			if (Constantes.MODO_OPERACAO.PJE_OFFICE.equals(getModoOperacao())) {
				imprimirResposta("Erro:" + e.getMessage(), getPJeOfficeCharacterEncoding());
			}
			else {
				imprimirResposta(e.getMessage(), CHARSET_APPLET);
			}
		}
	}
	
	private boolean isModoOperacaoPJeOffice() {
		return Constantes.MODO_OPERACAO.PJE_OFFICE.equals(getModoOperacao());
	}

	private ArquivoAssinadoHash criarArquivoAssinadoHash() {

		boolean modoOperacaoPJeOffice = isModoOperacaoPJeOffice();
		
		ArquivoAssinadoHash arquivo = new ArquivoAssinadoHash();
		
		arquivo.setId(getId());
		arquivo.setCodIni(getCodIni());
		arquivo.setHash(modoOperacaoPJeOffice ? getHash() : getMd5());
		arquivo.setAssinatura(modoOperacaoPJeOffice ? getAssinatura() : getSignature());
		arquivo.setCadeiaCertificado(modoOperacaoPJeOffice ? getCadeiaCertificado() : getCertChain());
		
		return arquivo;
	}
	
	private void imprimirResposta(String mensagem, String charset) {

		FacesContext facesContext = FacesContext.getCurrentInstance();
			
		response.setContentType("text/plain;charset=" + charset);
		response.setContentLength(mensagem.length());
			
		byte[] data = mensagem.getBytes(Charset.forName(charset));
			
		try {
			OutputStream out = response.getOutputStream();
			out.write(data);
			out.flush();			
			facesContext.responseComplete();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private String getPJeOfficeCharacterEncoding() {
		return (!StringUtil.isNullOrEmpty(request.getCharacterEncoding())) ? request.getCharacterEncoding() : CHARSET_PJE_OFFICE;	
	}
}