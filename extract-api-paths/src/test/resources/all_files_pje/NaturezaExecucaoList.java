package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.NaturezaClet;

@Name(NaturezaExecucaoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class NaturezaExecucaoList extends EntityList<NaturezaClet> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String NAME = "naturezaExecucaoList";
	private static final String DEFAULT_EJBQL = "select o from NaturezaClet o where o.tipoNatureza = 'E' ";
	private static final String DEFAULT_ORDER = "o.dsNatureza";
	private static final String R1 = "o.ativo = #{naturezaExecucaoList.situacao} ";
	private static final String R2 = "lower(o.dsNatureza) like concat('%', lower(#{naturezaExecucaoList.dsNatureza}), '%') ";

	private Boolean situacao;
	private String dsNatureza;

	@Override
	public void newInstance() {
		setSituacao(null);
		setDsNatureza(null);
		super.newInstance();
	}

	@Override
	protected void addSearchFields() {
		addSearchField("situacao", SearchCriteria.igual, R1);
		addSearchField("natureza", SearchCriteria.igual, R2);
	}

	@Override
	protected String getDefaultEjbql() {
		return DEFAULT_EJBQL;
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	public Boolean getSituacao() {
		return situacao;
	}

	public void setSituacao(Boolean situacao) {
		this.situacao = situacao;
	}

	public String getDsNatureza() {
		return dsNatureza;
	}

	public void setDsNatureza(String dsNatureza) {
		this.dsNatureza = dsNatureza;
	}

}
