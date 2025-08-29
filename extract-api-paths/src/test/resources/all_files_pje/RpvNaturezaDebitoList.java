package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.RpvNaturezaDebito;

@Name(RpvNaturezaDebitoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class RpvNaturezaDebitoList extends EntityList<RpvNaturezaDebito> {

	public static final String NAME = "rpvNaturezaDebitoList";
	private static final long serialVersionUID = 1L;
	private String codigoTipoDocumento;
	private String codigoRpvNaturezaDebito;
	private String rpvNaturezaDebito;

	private static final String R1 = " o.codigoTipoDocumento = #{rpvNaturezaDebitoList.codigoTipoDocumento}";
	private static final String R2 = " o.codigoRpvNaturezaDebito = #{rpvNaturezaDebitoList.codigoRpvNaturezaDebito}";
	private static final String R3 = " lower(to_ascii(o.rpvNaturezaDebito)) like "
			+ "'%' || lower(to_ascii(#{rpvNaturezaDebitoList.rpvNaturezaDebito})) || '%'";

	@Override
	protected void addSearchFields() {
		addSearchField("codigoTipoDocumento", SearchCriteria.igual, R1);
		addSearchField("codigoRpvNaturezaDebito", SearchCriteria.igual, R2);
		addSearchField("rpvNaturezaDebito", SearchCriteria.igual, R3);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	@Override
	protected String getDefaultEjbql() {
		return "select o from RpvNaturezaDebito o ";
	}

	@Override
	protected String getDefaultOrder() {
		return "o.rpvNaturezaDebito";
	}

	public String getCodigoTipoDocumento() {
		return codigoTipoDocumento;
	}

	public void setCodigoTipoDocumento(String codigoTipoDocumento) {
		this.codigoTipoDocumento = codigoTipoDocumento;
	}

	public String getCodigoRpvNaturezaDebito() {
		return codigoRpvNaturezaDebito;
	}

	public void setCodigoRpvNaturezaDebito(String codigoRpvNaturezaDebito) {
		this.codigoRpvNaturezaDebito = codigoRpvNaturezaDebito;
	}

	public String getRpvNaturezaDebito() {
		return rpvNaturezaDebito;
	}

	public void setRpvNaturezaDebito(String rpvNaturezaDebito) {
		this.rpvNaturezaDebito = rpvNaturezaDebito;
	}
}
