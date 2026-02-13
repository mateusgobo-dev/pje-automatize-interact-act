package br.com.infox.core.certificado.util;

import java.security.cert.X509Certificate;
import org.jboss.seam.util.Strings;
import br.com.infox.core.certificado.Certificado;
import br.com.infox.core.certificado.CertificadoException;
import br.com.infox.core.certificado.DadosCertificado;
import br.com.infox.core.certificado.VerificaCertificado;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.util.StringUtil;

public class VerificaCertificadoPessoa{

	public static void verificaCertificadoPessoa(String certChainBase64Encoded, Pessoa pessoa) throws CertificadoException{

		X509Certificate[] x509Certificates = DigitalSignatureUtils.loadCertFromBase64String(certChainBase64Encoded);
		VerificaCertificado.verificaValidadeCertificado(x509Certificates);
		if (pessoa instanceof PessoaFisica){
			PessoaFisica pf = (PessoaFisica) pessoa;
			verificaCertificadoPessoaFisica(x509Certificates, pf);
			return;
		}else if (pessoa instanceof PessoaJuridica){
			PessoaJuridica pj = (PessoaJuridica) pessoa;
			verificaCertificadoPessoaJuridica(x509Certificates, pj);
			return;
		}else{
			Certificado c = new Certificado(x509Certificates);
			DadosCertificado cert = DadosCertificado.parse(c);
			if(pessoa.getDocumentoCpfCnpj() == null){
				throw new CertificadoException("Não foi possível obter o documento de identificação do signatário.");
			}
			String inscricaoMF = pessoa.getDocumentoCpfCnpj().replaceAll("\\.", "").replaceAll("/", "").replaceAll("-", "");
			if(inscricaoMF.length() == 11){
				String numeroCertificado = cert.getValor(DadosCertificado.CPF);
				if(numeroCertificado == null){
					throw new CertificadoException("pje.verificaCertificadoPessoa.error.verificaCertificadoPessoaFisica.cpfDivergente");
				}else if(!numeroCertificado.equals(inscricaoMF)){
					throw new CertificadoException("pje.verificaCertificadoPessoa.error.verificaCertificadoPessoaFisica.cpfDivergente");
				}
			}else if(inscricaoMF.length() == 14){
				String numeroCertificado = cert.getValor(DadosCertificado.PJ_CNPJ);
				if(numeroCertificado == null){
					throw new CertificadoException("O numero do CNPJ da pessoa está diferente do encontrado no cartão");
				}else if(!numeroCertificado.equals(inscricaoMF)){
					throw new CertificadoException("O numero do CNPJ da pessoa está diferente do encontrado no cartão");
				}
			}
		}
	}

	public static void verificaCertificadoPessoa(String certChainBase64Encoded) throws CertificadoException{
		X509Certificate[] x509Certificates = DigitalSignatureUtils.loadCertFromBase64String(certChainBase64Encoded);
		VerificaCertificado.verificaValidadeCertificado(x509Certificates);
	}

	public static void verificaCertificadoPessoaFisica(X509Certificate[] x509Certificates, PessoaFisica pessoaFisica) throws CertificadoException{
		Certificado c = new Certificado(x509Certificates);
		verificaCertificadoPessoaFisica(c, pessoaFisica);
	}

	public static void verificaCertificadoPessoaFisica(Certificado certificado, PessoaFisica pessoaFisica) throws CertificadoException{

		DadosCertificado dadosCertificado = DadosCertificado.parse(certificado);
		String numeroCpfPessoa = pessoaFisica.getNumeroCPF().replaceAll("\\.", "").replaceAll("-", "");
		String numeroCpfCertificado = dadosCertificado.getValor(DadosCertificado.CPF);
		if (!numeroCpfPessoa.equals(numeroCpfCertificado)){
			throw new CertificadoException("pje.verificaCertificadoPessoa.error.verificaCertificadoPessoaFisica.cpfDivergente");
		}

	}

	public static void verificaCertificadoPessoaJuridica(X509Certificate[] x509Certificates, PessoaJuridica pessoaJuridica) throws CertificadoException{
		Certificado c = new Certificado(x509Certificates);
		DadosCertificado dadosCertificado = DadosCertificado.parse(c);
		String numeroCpfPessoa = StringUtil.retiraZerosEsquerda(pessoaJuridica.getNumeroCNPJ().replaceAll("\\.", "")
				.replaceAll("-", ""));
		String numeroCpfCertificado = StringUtil.retiraZerosEsquerda(dadosCertificado
				.getValor(DadosCertificado.PJ_CNPJ));
		if (!numeroCpfPessoa.equals(numeroCpfCertificado)){
			throw new CertificadoException("O numero do CNPJ da pessoa está diferente do encontrado no cartão");
		}
	}

	public static void verificaCertificadoPessoaLogada(String certChainBase64Encoded) throws CertificadoException{
		Pessoa pessoa = Authenticator.getPessoaLogada();
		verificaCertificadoValidoESePertenceAPessoa(certChainBase64Encoded, pessoa);
	}

	public static void verificaCertificadoValidoESePertenceAPessoa(String certChainBase64Encoded, Pessoa pessoa)
			throws CertificadoException{
		if (VerificaCertificado.instance().isModoTesteCertificado()){
			return;
		}
		verificaCertificadoPessoa(certChainBase64Encoded, pessoa);
	}

	public static void verificaCertificadoPessoaNaoLogada(String certChainBase64Encoded, Pessoa pessoa)
			throws CertificadoException{
		if (Strings.isEmpty(pessoa.getCertChain())){
			throw new CertificadoException("O cadastro do usuário não está assinado.");
		}
		if (!pessoa.checkCertChain(certChainBase64Encoded)){
			throw new CertificadoException("O certificado não é o mesmo do cadastro do usuario");
		}
		VerificaCertificado.verificaValidadeCertificado(certChainBase64Encoded);
	}

	public static boolean verificaCertificadoELocalizacaoPessoa(Pessoa p) {
		return (p.getCertChain() == null || p.getCertChain().isEmpty()) && p.getLocalizacoes().length == 0;
	}

}