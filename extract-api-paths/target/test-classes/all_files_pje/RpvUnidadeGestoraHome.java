package br.com.infox.cliente.home;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.RpvUnidadeGestora;

@Name("rpvUnidadeGestoraHome")
@BypassInterceptors
public class RpvUnidadeGestoraHome extends AbstractHome<RpvUnidadeGestora> {

	private static final long serialVersionUID = 1L;

	public static RpvUnidadeGestoraHome instance() {
		return ComponentUtil.getComponent("rpvUnidadeGestoraHome");
	}

	@SuppressWarnings("unchecked")
	public List<RpvUnidadeGestora> rpvUnidadeGestoraItems() {
		String hql = "select o from RpvUnidadeGestora o";
		Query query = EntityUtil.getEntityManager().createQuery(hql);
		return query.getResultList();
	}
}