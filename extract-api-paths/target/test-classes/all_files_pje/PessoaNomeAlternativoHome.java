package br.com.infox.cliente.home;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.manager.ClasseJudicialManager;
import br.jus.cnj.pje.nucleo.manager.PessoaNomeAlternativoManager;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaNomeAlternativo;
import br.jus.pje.nucleo.entidades.Usuario;

@Name("pessoaNomeAlternativoHome")
@BypassInterceptors
public class PessoaNomeAlternativoHome extends AbstractHome<PessoaNomeAlternativo> {

	private static final long serialVersionUID = 1L;

	public void setPessoaNomeAlternativoId(Integer id) {
		setId(id);
	}

	public Integer getPessoaNomeAlternativoId() {
		return (Integer) getId();
	}

	@Override
	protected PessoaNomeAlternativo createInstance() {
		PessoaNomeAlternativo pessoaDocumentoIdentificacao = new PessoaNomeAlternativo();
		pessoaDocumentoIdentificacao.setPessoa(new Pessoa());
		return pessoaDocumentoIdentificacao;
	}

	public static PessoaNomeAlternativoHome instance() {
		return ComponentUtil.getComponent("pessoaNomeAlternativoHome");
	}

	@Override
	public String remove(PessoaNomeAlternativo pessoaNomeAlternativo) {
		String ret = "";
		PessoaNomeAlternativoManager pessoaNomeAlternativoManager = 
									(PessoaNomeAlternativoManager) Component.getInstance(PessoaNomeAlternativoManager.NAME);
		if(!pessoaNomeAlternativoManager.isNomeAlternativoEstaSendoUsado(pessoaNomeAlternativo)) {
			instance = pessoaNomeAlternativo;
			ret = super.remove();
			newInstance();
			refreshGrid("pessoaNomeAlternativoGrid");
			return ret;
		}			
		else {
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, PessoaNomeAlternativoManager.ERRO_NOME_USADO);
		}
		return ret;
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		// setando o usuário que alterou/incluiu o meio de contato
		Usuario pessoaLogada = (Usuario) Contexts.getSessionContext().get("usuarioLogado");
		getInstance().setUsuarioCadastrador(pessoaLogada);
		return super.beforePersistOrUpdate();
	}

	@Override
	public String persist() {
		String ret = super.persist();
		refreshGrid("pessoaNomeAlternativoPreGrid");
		newInstance();
		return ret;
	}	
	
	@Override
	public void setId(Object id) {		
		if(id != null) {
			PessoaNomeAlternativoManager pessoaNomeAlternativoManager = 
					(PessoaNomeAlternativoManager) Component.getInstance(PessoaNomeAlternativoManager.NAME);
			if(pessoaNomeAlternativoManager.isNomeAlternativoEstaSendoUsado(new Integer(id.toString()))) {
				FacesMessages.instance().addFromResourceBundle(Severity.ERROR,  PessoaNomeAlternativoManager.ERRO_NOME_USADO);
				return;
			}
		}		
		super.setId(id);
	}
}
