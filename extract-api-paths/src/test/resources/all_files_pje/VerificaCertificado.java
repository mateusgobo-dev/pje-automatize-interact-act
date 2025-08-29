package br.com.infox.core.certificado;

import java.security.cert.CertificateExpiredException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.Calendar;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.certificado.crl.CrlCheckControler;
import br.com.infox.core.certificado.crl.CrlCheckException;
import br.com.infox.core.certificado.crl.jobs.LoginCrlCertTestJobActionListner;
import br.com.infox.core.certificado.util.DigitalSignatureUtils;
import br.jus.pje.nucleo.util.StringUtil;

@Name("verificaCertificado")
@Scope(ScopeType.APPLICATION)
public class VerificaCertificado {

	public static final String NAME = "verificaCertificado";
	private static final LogProvider log = Logging.getLogProvider(VerificaCertificado.class);

	public static void verificaValidadeCertificado(String certChainBase64Encoded) throws CertificadoException {
		X509Certificate[] x509Certificates = DigitalSignatureUtils.loadCertFromBase64String(certChainBase64Encoded);
		verificaValidadeCertificado(x509Certificates);
	}

	public static void verificaValidadeCertificado(X509Certificate[] x509Certificates) throws CertificadoException {
		try {
			CertificadosCaCheckManager instance = CertificadosCaCheckManager.instance();
			if (instance != null) {
				instance.verificaCertificado(x509Certificates);
			}
		} catch (Exception e) {
			throw new CertificadoException("Erro ao validar certificado: " + e.getMessage(), e);
		}
	}

	public static void verificaRevogacaoCertificado(String certChainBase64Encoded) throws CertificadoException {
		try {
			Certificado c = new Certificado(certChainBase64Encoded);
			try {
				boolean certificadoRevogado = CrlCheckControler.instance().isCertificadoRevogado(c,
						new LoginCrlCertTestJobActionListner());
				if (certificadoRevogado) {
					throw new CertificadoException("Certificado revogado");
				}
			} catch (CrlCheckException e) {
				log.warn("Erro ao verificar Crl: " + e.getMessage());
			}
		} catch (Exception e) {
			throw new CertificadoException("Erro ao válidar certificado: " + e.getMessage(), e);
		}
	}

	public static VerificaCertificado instance() {
		return (VerificaCertificado) Component.getInstance(NAME);
	}

	public boolean isModoTesteCertificado() {
		String parametroProducao =(String) Component.getInstance("producao", ScopeType.APPLICATION);
		Boolean producao = Boolean.TRUE.toString().equals(parametroProducao);
		if (producao != null && producao == true) {
			return false;
		}
		String modoTesteCertificado = (String) Contexts.getApplicationContext().get("modoTesteCertificado");
		return Boolean.TRUE.toString().equals(modoTesteCertificado);
	}
	
	public static boolean isCertificadoProximoDeExpirar(Certificado certificado)
			throws CertificadoException {

		Integer qtdDiasAlertaExpiracaoCertificado = ParametroUtil.instance()
				.getQtdDiasAlertaExpiracaoCertificado();
		if (qtdDiasAlertaExpiracaoCertificado == null || certificado == null) {
			return false;
		}
		Calendar dataProximaDaExpiracao = Calendar.getInstance();
		dataProximaDaExpiracao.add(Calendar.DATE,
				qtdDiasAlertaExpiracaoCertificado);
		return dataProximaDaExpiracao.getTime().after(
				certificado.getDataValidadeFim())
				|| dataProximaDaExpiracao.getTime().equals(
						certificado.getDataValidadeFim());
	}

	public static boolean isCertificadoProximoDeExpirar(
			String certChainBase64Encoded) throws CertificadoException {
		if (StringUtil.isEmpty(certChainBase64Encoded) || !DigitalSignatureUtils.isValidCertPath(certChainBase64Encoded)) {
			return false;
		}
		Certificado certificado = new Certificado(certChainBase64Encoded);
		return isCertificadoProximoDeExpirar(certificado);
	}

	public static void verificaDataValidade(String certChain) throws CertificateExpiredException {
		Certificado certificado;
		try {
			certificado = new Certificado(certChain);
			if (certificado.getDataValidadeFim().before(Calendar.getInstance().getTime())) {
				throw new CertificateExpiredException(
					"Certificado expirado em: " + DateFormat.getDateInstance().format(certificado.getDataValidadeFim()));
			}
		} catch (CertificadoException e) {
			log.error(e);
		}
	}
}