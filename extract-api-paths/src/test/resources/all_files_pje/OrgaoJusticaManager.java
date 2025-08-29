/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.OrgaoJusticaDAO;
import br.jus.pje.nucleo.entidades.lancadormovimento.OrgaoJustica;

@Name(OrgaoJusticaManager.NAME)
public class OrgaoJusticaManager extends BaseManager<OrgaoJustica> {
	
	public static final String NAME = "orgaoJusticaManager";
	
	@In
	private OrgaoJusticaDAO orgaoJusticaDAO;

	@Override
	protected OrgaoJusticaDAO getDAO() {
		return orgaoJusticaDAO;
	}
	
	public OrgaoJustica findByOrgaoJustica(String orgaoJustica) {
		return this.getDAO().findByOrgaoJustica(orgaoJustica);
	}
}
