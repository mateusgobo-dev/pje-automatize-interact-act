package br.com.infox.pje.action;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.certificado.VerificaCertificado;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinManager;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.pje.nucleo.util.StringUtil;

@Name(VerificacaoAmbienteAction.NAME)
@Scope(ScopeType.EVENT)
public class VerificacaoAmbienteAction implements Serializable, ArquivoAssinadoUploader {

	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "verificacaoAmbienteAction";
	public static final String FRASETESTE = "fraseteste"; // frase de teste de certificado
	public static final byte[] FRASETESTEBYTES = FRASETESTE.getBytes();
	public static final String FRASETESTEMD5 = "fe5de830e72b37ec2f70583c6184f015";

	public String getDownloadUrl() {
		return "id=-1&codIni=-1&md5=" + FRASETESTEMD5 + "&isBin=false";
	}

	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash) throws Exception {

		StringBuilder resultado = new StringBuilder();
		
		if (!VerificaCertificado.instance().isModoTesteCertificado()) {
			
			ProcessoDocumentoBinManager pdbManager = ComponentUtil.getComponent("processoDocumentoBinManager");
			
			try {
				// Assinatura vazia e certChain vazio
				if (StringUtil.isNullOrEmpty(arquivoAssinadoHash.getAssinatura()) && StringUtil.isNullOrEmpty(arquivoAssinadoHash.getCadeiaCertificado())) {
					resultado.append("Erro ao assinar. Verifique se o cartão ou token estão conectados.");
				} 
				else {
					pdbManager.verificaAssinatura(FRASETESTEBYTES, arquivoAssinadoHash.getAssinatura(), arquivoAssinadoHash.getCadeiaCertificado(), "MD5", null);
				}
			} 
			catch (PJeBusinessException e) {
				resultado.append(e.getLocalizedMessage());
			}
			catch (Exception e) {
				resultado.append(e.getLocalizedMessage());
			}
		}
		
		if (resultado.length() > 0) {
			throw new Exception(resultado.toString());
		}
	}

	@Override
	public String getActionName() {
		return NAME;
	}
}