package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.BaseCalculoIr;

@Name(BaseCalculoIrList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class BaseCalculoIrList extends EntityList<BaseCalculoIr> {

	public static final String NAME = "baseCalculoIrList";

	private static final long serialVersionUID = 1L;

	private Integer nrAnoVigencia;

	private static final String DEFAULT_EJBQL = "select o from BaseCalculoIr o";
	private static final String DEFAULT_ORDER = "idBaseCalculoIr";

	private static final String R1 = "to_char(o.dataFimVigencia, 'yyyy') <= #{baseCalculoIrList.anoStr}";

	@Override
	public void newInstance() {
		super.newInstance();
		setNrAnoVigencia(null);
	}

	@Override
	protected void addSearchFields() {
		addSearchField("nrAnoVigencia", SearchCriteria.igual, R1);
	}

	public String getAnoStr() {
		if (this.nrAnoVigencia != null) {
			String r = String.valueOf(nrAnoVigencia);
			return r;
		} else {
			return null;
		}
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	@Override
	protected String getDefaultEjbql() {
		return DEFAULT_EJBQL;
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	public Integer getNrAnoVigencia() {
		return nrAnoVigencia;
	}

	public void setNrAnoVigencia(Integer nrAnoVigencia) {
		this.nrAnoVigencia = nrAnoVigencia;
	}
}