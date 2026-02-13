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
import br.jus.pje.jt.entidades.RemessaRecebimento;
import br.jus.pje.nucleo.entidades.lancadormovimento.Dominio;
import br.jus.pje.nucleo.entidades.lancadormovimento.ElementoDominio;

 
@Name(ElementoDominioHome.NAME)
@BypassInterceptors
public class ElementoDominioHome extends AbstractHome<ElementoDominio>{

	public static final String NAME = "elementoDominioHome";
	private static final long serialVersionUID = 1L;
	
	public static ElementoDominioHome instance(){
		return ComponentUtil.getComponent(ElementoDominioHome.NAME);
	}	
	
	@Override
	public String remove(ElementoDominio obj) {
		
		Dominio dominio = obj.getDominio();
		dominio.getElementoDominioList().remove(obj);
		
		String retorno = super.remove(obj);
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, FacesUtil.getMessage("entity_messages", "elementoDominio_deleted"));
		
		return retorno;
	}
	
	@Override
	public String persist() {
		
		DominioHome dominioHome = ComponentUtil.getComponent(DominioHome.NAME);
		getInstance().setDominio(dominioHome.getInstance());
		
		String persist = super.persist();
		
		newInstance();
		
		return persist;
		
	}
	
	@Override
	protected String afterPersistOrUpdate(String ret) {
		FacesMessages.instance().clear();
		if (ret.equals("updated")){
			FacesMessages.instance().add(Severity.INFO,"#{messages['elementoDominio_updated']}");
		}
		if (ret.equals("persisted")){
			FacesMessages.instance().add(Severity.INFO,"#{messages['elementoDominio_created']}");
		}

		return ret;
	}

	public void setElementoDominioId(Long id) {
		setId(id);
	}

	public Long getElementoDominioId() {
		return (Long) getId();
	}	
	
	@SuppressWarnings("unchecked")
	public List<ElementoDominio> getListaElementosPorDominio(String codigoDominio, String listaCodigosElementos){
		
		if (codigoDominio == null){
			return null;
		}
						
		List<ElementoDominio> list = null;
		if (listaCodigosElementos != null) {
			listaCodigosElementos = listaCodigosElementos.replace("'", "");
			listaCodigosElementos = "'" + listaCodigosElementos.replace(",","','") + "'";
			list = getEntityManager().createQuery(
					"SELECT o FROM ElementoDominio o WHERE o.ativo = true "
						+ "AND o.codigoGlossario IN (" + listaCodigosElementos + ") AND o.dominio.codigo = :codigoDominio "
						+ "ORDER BY o.valor ASC ")
					.setParameter("codigoDominio", codigoDominio)
					.getResultList();
		} else {
			list = getEntityManager().createQuery(
					"SELECT o FROM ElementoDominio o WHERE o.ativo = true AND o.dominio.codigo = :codigoDominio"
						+ "ORDER BY o.valor ASC " )
					.setParameter("codigoDominio", codigoDominio)
					.getResultList();
		}		
		
		return list;
	}
	
	public RemessaRecebimento getRemessaRecebimentoByIdRemessa(Long id) {
		Query query = getEntityManager().createQuery("select o from RemessaRecebimento o where o.elementoRemessa.id = :id");
		query.setParameter("id", id);
		
		List resultList = query.getResultList();
		if (resultList == null || resultList.size() == 0){
			return null;
		} else {
			return (RemessaRecebimento) resultList.get(0);
		}
	}
}
