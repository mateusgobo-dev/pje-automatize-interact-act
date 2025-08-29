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
import br.jus.cnj.pje.nucleo.manager.ContraMandadoManager;
import br.jus.cnj.pje.nucleo.manager.MandadoPrisaoManager;
import br.jus.pje.nucleo.entidades.ContraMandado;
import br.jus.pje.nucleo.entidades.PessoaFisica;

@Name("assinarContraMandadoService")
@Install(precedence = Install.APPLICATION)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class AssinarContraMandadoService extends AbstractAssinarExpedienteCriminalService<ContraMandado>{

	private static final long serialVersionUID = -8326199747085245799L;
	
	@In
	private RevogarMandadoPrisaoService revogarMandadoPrisaoService;
	
	@In
	private ContraMandadoManager contraMandadoManager;
	
	@In
	private EntityManager entityManager;
	
	
	@Override
	public ContraMandado assinarExpedienteCriminal(ContraMandado expediente, String assinatura, String encodedCertChain, Long jbpmTask) throws PJeBusinessException{
		expediente.setPessoaMagistrado(((PessoaFisica) Authenticator.getUsuarioLogado()).getPessoaMagistrado());
		
		//substituindo a marcacao ||MAGISTADO|| pelo nome do magistado
		String textoMagistrado = expediente.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento()
				.replace(MandadoPrisaoManager.MARCACAO_MAGISTRADO_REPLACE, expediente.getPessoaMagistrado().getNome());
		expediente.getProcessoDocumento().getProcessoDocumentoBin().setModeloDocumento(textoMagistrado);		
		
		contraMandadoManager.persist(expediente);		
		expediente = super.assinarExpedienteCriminal(expediente, assinatura, encodedCertChain, jbpmTask);
		entityManager.flush();
 	    //ao assinar o contramandado, deve-se revogar o mandado junto ao BNMP
		revogarMandadoPrisaoService.revogarMandadoPrisao(expediente.getMandadoPrisao());
		entityManager.flush();
		return expediente;
	}
}
