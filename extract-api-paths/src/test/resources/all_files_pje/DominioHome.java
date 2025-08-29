/* $Id: DominioHome.java 10746 2010-08-12 23:23:46Z jplacerda $ */

package br.com.infox.ibpm.home;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.FacesUtil;
import br.jus.pje.nucleo.entidades.lancadormovimento.Dominio;

 
@Name(DominioHome.NAME)
@BypassInterceptors
public class DominioHome extends AbstractHome<Dominio>{

	public static final String NAME = "dominioHome";
	private static final long serialVersionUID = 1L;
	
	public static DominioHome instance() {
		return ComponentUtil.getComponent(DominioHome.NAME);
	}
	
	@Override
	public String remove(Dominio obj) {
		
		String retorno = super.remove(obj);
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, FacesUtil.getMessage("entity_messages", "dominio_deleted"));
		
		return retorno;
	}
	
	public Dominio getDominio(){
		
		return getInstance();
		
	}
	
	public void setDominioId(Long id) {
		setId(id);
	}

	public Long getDominioId() {
		return (Long) getId();
	}
	
	@SuppressWarnings("unchecked")

	public List<Dominio> getListDominioAtivo() {
		Query q = getEntityManager().createQuery("from Dominio ss where ss.ativo = :ativo");
		q.setParameter("ativo", true);
		return (List<Dominio>) q.getResultList();
	}

}
