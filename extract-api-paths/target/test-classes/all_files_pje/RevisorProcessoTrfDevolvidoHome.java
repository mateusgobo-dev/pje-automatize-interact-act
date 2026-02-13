package br.com.infox.cliente.home;

import java.util.Date;

import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.util.Strings;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.RevisorProcessoTrf;
import br.jus.pje.nucleo.entidades.RevisorProcessoTrfDevolvido;

@Name(RevisorProcessoTrfDevolvidoHome.NAME)
@Scope(ScopeType.PAGE)
@BypassInterceptors
public class RevisorProcessoTrfDevolvidoHome extends AbstractHome<RevisorProcessoTrfDevolvido> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "revisorProcessoTrfDevolvidoHome";

	public static RevisorProcessoTrfDevolvidoHome instance() {
		return (RevisorProcessoTrfDevolvidoHome) Component.getInstance(NAME);
	}

	public boolean motivoVazio() {
		return Strings.isEmpty(getInstance().getMotivo());
	}

	public void gravarDevolucao(Integer id) {
		if (Strings.isEmpty(getInstance().getMotivo())) {
			return;
		}
		RevisorProcessoTrf revisor = aguardandoRevisao(id);
		if (revisor != null) {
			try {
				revisor.setDataFinal(new Date());
				getEntityManager().merge(revisor);

				getInstance().setRevisorProcessoTrf(revisor);
				getInstance().setUsuarioDevolucao(Authenticator.getUsuarioLogado());
				super.persist();
			} catch (Exception e) {
				e.printStackTrace();
			}
			getEntityManager().flush();
		}
	}

	public RevisorProcessoTrf aguardandoRevisao(Integer id) {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from RevisorProcessoTrf o where ");
		sb.append("o.orgaoJulgadorRevisor = :oj and ");
		sb.append("o.dataFinal = null ");
		sb.append("and o.processoTrf.revisado = false ");
		sb.append("and o.processoTrf.prontoRevisao = true ");
		sb.append("and o.processoTrf.idProcessoTrf = :id");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("id", id);
		q.setParameter("oj", Authenticator.getOrgaoJulgadorAtual());
		return EntityUtil.getSingleResult(q);
	}

}
