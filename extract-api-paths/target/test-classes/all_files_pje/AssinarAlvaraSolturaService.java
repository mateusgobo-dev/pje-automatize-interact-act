package br.com.infox.pje.service;

import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.AlvaraSolturaManager;
import br.jus.cnj.pje.nucleo.manager.MandadoPrisaoManager;
import br.jus.pje.nucleo.entidades.AlvaraSoltura;
import br.jus.pje.nucleo.entidades.PessoaFisica;

@Name("assinarAlvaraSolturaService")
@Install(precedence = Install.APPLICATION)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class AssinarAlvaraSolturaService extends AbstractAssinarExpedienteCriminalService<AlvaraSoltura>{

	private static final long serialVersionUID = 1262160316002298420L;
	
	@In
	private AlvaraSolturaManager alvaraSolturaManager;
	
	@In
	private EntityManager entityManager;
	
	@Override
	public AlvaraSoltura assinarExpedienteCriminal(AlvaraSoltura expediente,
			String assinatura, String encodedCertChain, Long jbpmTask)
			throws PJeBusinessException {
		
		expediente.setPessoaMagistrado(((PessoaFisica) Authenticator.getUsuarioLogado()).getPessoaMagistrado());
		
		//substituindo a marcacao ||MAGISTADO|| pelo nome do magistado
		String textoMagistrado = expediente.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento()
				.replace(MandadoPrisaoManager.MARCACAO_MAGISTRADO_REPLACE, expediente.getPessoaMagistrado().getNome());
		expediente.getProcessoDocumento().getProcessoDocumentoBin().setModeloDocumento(textoMagistrado);		
		
		alvaraSolturaManager.persist(expediente);
		expediente = super.assinarExpedienteCriminal(expediente, assinatura, encodedCertChain, jbpmTask);
		entityManager.flush();
		
		return expediente;
	}

}
