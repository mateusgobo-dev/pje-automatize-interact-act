package br.com.infox.cliente.home;

import javax.persistence.Query;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.AssuntoAgrupamento;
import br.jus.pje.nucleo.entidades.AssuntoTrf;

@Name(AssuntoAgrupamentoHome.NAME)
@BypassInterceptors
public class AssuntoAgrupamentoHome extends AbstractHome<AssuntoAgrupamento>{

	public static final String NAME = "assuntoAgrupamentoHome";
	private static final long serialVersionUID = 1L;

	public void incluir(AssuntoTrf assuntoTrf){
		newInstance();
		getInstance().setAssunto(assuntoTrf);
		getInstance().setAgrupamento(AgrupamentoClasseJudicialHome.instance().getInstance());
		persist();
	}

	@Override
	public String remove(AssuntoAgrupamento obj){
		String result = super.remove(obj);
		return result;
	}

	@Override
	protected boolean beforePersistOrUpdate(){
		if (verificaAssuntoAgrupamento()){
			FacesMessages.instance().add(Severity.ERROR, "Registro já cadastrado.");
			return false;
		}

		return super.beforePersistOrUpdate();
	}

	public boolean verificaAssuntoAgrupamento(){
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from AssuntoAgrupamento o ");
		sb.append("where o.assunto.idAssuntoTrf = :idAssuntoTrf ");
		sb.append("and o.agrupamento.idAgrupamento = :idAgrupamento ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("idAssuntoTrf", getInstance().getAssunto().getIdAssuntoTrf());
		q.setParameter("idAgrupamento", getInstance().getAgrupamento().getIdAgrupamento());
		Long count = EntityUtil.getSingleResult(q);
		return count > 0;
	}
}
