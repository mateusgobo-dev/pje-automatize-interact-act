package br.com.infox.cliente.home;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.lancadormovimento.OrgaoJustica;

/**
 * Classe para operações com "OrgaoJustica"
 * 
 */
@Name("orgaoJusticaHome")
@BypassInterceptors
public class OrgaoJusticaHome extends AbstractHome<OrgaoJustica>{

	public static final String NAME = "orgaoJusticaHome";
	private static final long serialVersionUID = 1L;

	@Override
	public void newInstance(){
		super.newInstance();
	}

	@Override
	public String inactive(OrgaoJustica orgaoJustica){
		return super.inactive(orgaoJustica);
	}

	/*@Override
	public String update(){
		if (!getInstance().getAtivo()){
			return "updated";
		}
		else{
			return super.update();
		}
	}*/

	@Override
	public void setId(Object id){
		super.setId(id);
	}

	public void setIdOrgaoJustica(Long id){
		setId(id);
	}

	public Long getIdOrgaoJustica(){
		return (Long) getId();
	}

}
