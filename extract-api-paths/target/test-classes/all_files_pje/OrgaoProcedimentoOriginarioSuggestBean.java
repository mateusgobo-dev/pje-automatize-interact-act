package br.com.infox.cliente.component.suggest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.home.ProcessoProcedimentoOrigemAction;
import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.OrgaoProcedimentoOriginario;

@Name("orgaoProcedimentoOriginarioSuggest")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class OrgaoProcedimentoOriginarioSuggestBean extends AbstractSuggestBean<OrgaoProcedimentoOriginario> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		String hql;
		hql = " select o from OrgaoProcedimentoOriginario o ";

		if (ProcessoProcedimentoOrigemAction.instance().getProcessoProcedimentoOrigemDTO().getTipoOrigem() != null) {
			hql += " where o.tipoOrigem.id = "
					+ ProcessoProcedimentoOrigemAction.instance().getProcessoProcedimentoOrigemDTO().getTipoOrigem().getId();
		} else {
			hql += " where 1 = 2";
		}

		hql += " and lower(TO_ASCII(o.dsNomeOrgao)) like " + " lower(concat('%', TO_ASCII(:" + INPUT_PARAMETER
				+ "), '%')) " + " order by o.dsNomeOrgao ";

		return hql;
	}

	@Override
	public void setInstance(OrgaoProcedimentoOriginario instance) {
		super.setInstance(instance);
	}

	@Override
	public String getDefaultValue() {
		return getInstance() != null ? getInstance().toString() : "";
	}
}
