package br.com.infox.cliente.home;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorPessoaPerito;
import br.jus.pje.nucleo.entidades.PessoaPerito;

@Name("orgaoJulgadorPessoaPeritoHome")
@BypassInterceptors
public class OrgaoJulgadorPessoaPeritoHome extends AbstractOrgaoJulgadorPessoaPeritoHome<OrgaoJulgadorPessoaPerito> {

	private static final long serialVersionUID = 1L;

	public static OrgaoJulgadorPessoaPeritoHome instance() {
		return ComponentUtil.getComponent("orgaoJulgadorPessoaPeritoHome");
	}

	public void addOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		if (getInstance() != null) {
			newInstance();
			getInstance().setOrgaoJulgador(orgaoJulgador);
			getInstance().setPessoaPerito(PessoaPeritoHome.instance().getInstance());
			persist();
			getEntityManager().flush();
			newInstance();
			refreshGrid("orgaoJulgadorPessoaGrid");
			refreshGrid("orgaoJulgadorPessoaRightGrid");
		}
	}

	public void removeOrgaoJulgador(OrgaoJulgadorPessoaPerito orgaoJulgadorPessoaPerito) {
		setInstance(orgaoJulgadorPessoaPerito);
		
		/* 
		 * PJEII-1279 - Sérgio Ricardo: 2012-05-24 Correção da remoção da relação perito x orgão julgador
		 */		
		//remove();
		//getEntityManager().flush();
		PessoaPerito pessoa = orgaoJulgadorPessoaPerito.getPessoaPerito();
		pessoa.getOrgaoJulgadorPessoaPeritoList().remove(orgaoJulgadorPessoaPerito);
		getEntityManager().remove(orgaoJulgadorPessoaPerito);
		getEntityManager().flush();
		
		refreshGrid("orgaoJulgadorPessoaGrid");
		refreshGrid("orgaoJulgadorPessoaRightGrid");
	}
}