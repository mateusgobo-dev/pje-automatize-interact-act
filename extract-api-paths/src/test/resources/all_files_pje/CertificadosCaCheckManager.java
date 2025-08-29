package br.com.infox.core.certificado;

import java.io.File;
import java.io.FileFilter;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.core.certificado.util.DigitalSignatureUtils;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.certificado.CertificadoICP;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinManager;
import br.jus.cnj.pje.nucleo.service.CertificadoDigitalService;

/**
 * Componente responsavel pelo teste de validade dos certificados confrontando 
 * a corrente de certificados com a lista das certificadoras que o ICP-Brasil fornece.
 */
@Name(CertificadosCaCheckManager.NAME)
@BypassInterceptors
public class CertificadosCaCheckManager {

	public static final String NAME = "certificadosCaCheckManager";

	public void verificaCertificado(String certChain) throws CertificadoException, CertificateException {
		X509Certificate[] x509Certificates = DigitalSignatureUtils.loadCertFromBase64String(certChain);
		verificaCertificado(x509Certificates);
	}

	public void verificaCertificado(X509Certificate[] certChain) throws CertificateException {
		CertificadoDigitalService certificadoDigitalService = ComponentUtil.getComponent(CertificadoDigitalService.class);
		ProcessoDocumentoBinManager processoDocumentoBinManager = ComponentUtil.getComponent(ProcessoDocumentoBinManager.class);
		
		try {
			CertificadoICP certificadoICP = processoDocumentoBinManager.obterCertificadoICP(certChain);
			certificadoDigitalService.validate(certificadoICP.getX509Certificate(), certChain, processoDocumentoBinManager.recuperarTimeout());
		} catch (PJeBusinessException ex) {
			throw new CertificateException("A validade do certificado não pode ser verificada junto ao ICP-Brasil.");
		}
	}

	public static CertificadosCaCheckManager instance() {
		return ComponentUtil.getComponent(NAME, ScopeType.APPLICATION);
	}

	public class CertFilter implements FileFilter {

		@Override
		public boolean accept(File pathname) {
			String name = pathname.getName().toLowerCase();
			return name.endsWith(".crt") || name.endsWith(".cer");
		}

	}
	
}
