package br.jus.pje.api.converters;

import java.util.Optional;

import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.Assinatura;
import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.AssinaturaDigital;
import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.CadastroIdentificador;
import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.SignatarioSimples;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBinPessoaAssinatura;
import br.jus.pje.nucleo.entidades.Usuario;

public class AssinaturaConverter {
	
	public Assinatura convertFrom(ProcessoDocumentoBin proDocumentoBin) {
		Assinatura assinatura = new Assinatura();
		assinatura.setSignatarioLogin(buildSignatarioLogin(proDocumentoBin.getUsuario()));
		assinatura.setAssinaturaDigital(buildAssinaturaDigital(proDocumentoBin));
		return assinatura;
	}
	
	public SignatarioSimples buildSignatarioLogin(Usuario usuario) {		
		SignatarioSimples signatarioSimples = new SignatarioSimples();		
		signatarioSimples.setIdentificador(buildCadastroIdentificador(usuario.getLogin()));	
		return signatarioSimples;
	}
	
	public CadastroIdentificador buildCadastroIdentificador(String numeroIdentificaco) {
		CadastroIdentificador identificador = new CadastroIdentificador();
		identificador.setValue(numeroIdentificaco);
		return identificador;
	}

	public AssinaturaDigital buildAssinaturaDigital(ProcessoDocumentoBin processoDocumentoBin) {
		AssinaturaDigital assinaturaDigital = new AssinaturaDigital();
		
		Optional<ProcessoDocumentoBinPessoaAssinatura> assinatura = getUltimaAssinatura(processoDocumentoBin);
		
		if(assinatura.isPresent()) {
			assinaturaDigital.setAssinatura(assinatura.get().getAssinatura().getBytes());
			assinaturaDigital.setDataAssinatura(assinatura.get().getDataAssinatura());
			assinaturaDigital.setCadeiaCertificado(assinatura.get().getCertChain());
			assinaturaDigital.setAlgoritmo(assinatura.get().getAlgoritmoDigest());			
		}
		
		return assinaturaDigital;
	}
	
	private Optional<ProcessoDocumentoBinPessoaAssinatura> getUltimaAssinatura(ProcessoDocumentoBin processoDocumentoBin) {
		return processoDocumentoBin.getSignatarios().stream()
				.sorted((x, y) -> x.getDataAssinatura().compareTo(y.getDataAssinatura()))
				.findFirst();		
	}
}
