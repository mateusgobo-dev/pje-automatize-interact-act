/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.AplicacaoClasseDAO;
import br.jus.pje.nucleo.entidades.AplicacaoClasse;

/**
 * @author cristof
 *
 */
@Name(AplicacaoClasseManager.NAME)
public class AplicacaoClasseManager extends BaseManager<AplicacaoClasse> {
	
	public static final String NAME = "aplicacaoClasseManager";
	
	@In
	private AplicacaoClasseDAO aplicacaoClasseDAO;

	@Override
	protected AplicacaoClasseDAO getDAO() {
		return aplicacaoClasseDAO;
	}

	
	public AplicacaoClasse findByCodigo(String codigoAplicacaoSistema) {
		AplicacaoClasse ret = null;
		if(codigoAplicacaoSistema != null) {
			if(!codigoAplicacaoSistema.endsWith("G")) {
				codigoAplicacaoSistema = codigoAplicacaoSistema.concat("G");
			}
			ret = this.getDAO().findByCodigo(codigoAplicacaoSistema);
		}
		return ret;
	}
}
