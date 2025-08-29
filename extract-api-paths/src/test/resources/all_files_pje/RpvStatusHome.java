package br.com.infox.cliente.home;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.RpvStatus;

@Name("rpvStatusHome")
@BypassInterceptors
public class RpvStatusHome extends AbstractHome<RpvStatus> {

	private static final long serialVersionUID = 1L;

	public static RpvStatusHome instance() {
		return ComponentUtil.getComponent("rpvStatusHome");
	}

	@SuppressWarnings("unchecked")
	public List<RpvStatus> getRpvStatusCheckList() {
		String hql = "select o from RpvStatus o where o.ativo = true order by o.rpvStatus";
		Query q = EntityUtil.createQuery(hql);
		return q.getResultList();
	}
}
