package br.com.infox.cliente.home;

import java.util.Date;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;

import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.ProcessoSegredo;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;
import br.jus.pje.nucleo.enums.ProcessoTrfApreciadoEnum;
import br.jus.pje.nucleo.enums.SegredoStatusEnum;

@Name("processoSegredoHome")
@BypassInterceptors
public class ProcessoSegredoHome extends AbstractProcessoSegredoHome<ProcessoSegredo> {

	private static final long serialVersionUID = 1L;
	private Boolean segredoJustica;
	private ProcessoTrf processoTrf;

	public void setarProcessoSegredo(ProcessoTrf obj) {
		processoTrf = obj;
		setSegredoJustica(obj.getSegredoJustica());
		obj.setSegredoJustica(!getSegredoJustica());
	}

	public void inserirMotivoSegredoJustica() {
		processoTrf.setSegredoJustica(getSegredoJustica());
		processoTrf.setApreciadoSegredo(getSegredoJustica() ? ProcessoTrfApreciadoEnum.S : ProcessoTrfApreciadoEnum.N);
		getEntityManager().merge(processoTrf);
		getEntityManager().flush();

		getInstance().setProcessoTrf(processoTrf);
		String query = "select distinct(ps) from ProcessoSegredo ps "
				+ "where ps.processoTrf.idProcessoTrf = :idProcessoTrf and ps.apreciado = false) ";
		Query q = getEntityManager().createQuery(query);
		q.setParameter("idProcessoTrf", processoTrf.getIdProcessoTrf());
		if (q.getResultList().size() <= 0) {
			getInstance().setApreciado(Boolean.TRUE);
		}
		if (segredoJustica) {
			instance.setStatus(SegredoStatusEnum.C);
		} else {
			instance.setStatus(SegredoStatusEnum.R);
		}
		persist();
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		Context session = Contexts.getSessionContext();
		UsuarioLogin usuarioLogin = (UsuarioLogin) session.get("usuarioLogado");
		getInstance().setUsuarioLogin(usuarioLogin);
		getInstance().setDtAlteracao(new Date());
		return super.beforePersistOrUpdate();
	}

	@Override
	public String persist() {
		String ret = super.persist();
		if ("persisted".equals(ret)) {
			ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();
			processoTrf.setSegredoJustica(segredoJustica);
			getEntityManager().merge(processoTrf);
			refreshGrid("detalheProcessoSegredoJusticaGrid");
		}
		return ret;
	}

	public static ProcessoSegredoHome instance() {
		return ComponentUtil.getComponent("processoSegredoHome");
	}

	public void setSegredoJustica(Boolean segredoJustica) {
		this.segredoJustica = segredoJustica;
	}

	public Boolean getSegredoJustica() {
		if (segredoJustica == null) {
			segredoJustica = ProcessoTrfHome.instance().getInstance().getSegredoJustica();
		}
		return segredoJustica;
	}

}
