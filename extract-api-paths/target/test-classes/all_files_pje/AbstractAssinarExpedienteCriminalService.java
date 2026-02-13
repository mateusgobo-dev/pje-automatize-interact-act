package br.com.infox.pje.service;

import org.jboss.seam.annotations.In;

import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.ProcessoExpedienteCriminal;

public abstract class AbstractAssinarExpedienteCriminalService<E extends ProcessoExpedienteCriminal> extends BNMPService{

	private static final long serialVersionUID = -6076228867620704180L;	
	
	
	@In
	private DocumentoJudicialService documentoJudicialService;

	public E adicionarAssinatura(E expediente, String assinatura, String encodedCertChain){
		if (expediente.getProcessoDocumento().getProcessoDocumentoBin().getCertChain() == null){
			expediente.getProcessoDocumento().getProcessoDocumentoBin().setCertChain(encodedCertChain);
			expediente.getProcessoDocumento().getProcessoDocumentoBin().setSignature(assinatura);
		}

		return expediente;
	}

	public E assinarExpedienteCriminal(E expediente, String assinatura, String encodedCertChain, Long jbpmTask)
			throws PJeBusinessException{
		try{
			// verificando se o usuário logado é o mesmo do token
			//VerificaCertificadoPessoa.verificaCertificadoPessoaLogada(encodedCertChain);

			if (!(Pessoa.instanceOf(Authenticator.getUsuarioLogado(), PessoaMagistrado.class))){
				throw new PJeBusinessException("pje.abstractAssinarExpedienteCriminalService.error.apenasMagistradoAssina");
			}

			E assinado = adicionarAssinatura(expediente, assinatura, encodedCertChain);
			//assinado.setPessoaMagistrado((PessoaMagistrado) Authenticator.getUsuarioLogado());
			documentoJudicialService.finalizaDocumento(assinado.getProcessoDocumento(), assinado.getProcessoTrf(),
					jbpmTask, true, true);
			return assinado;
		} catch (PJeDAOException e){
			throw new PJeBusinessException(e);
		}
	}
}
