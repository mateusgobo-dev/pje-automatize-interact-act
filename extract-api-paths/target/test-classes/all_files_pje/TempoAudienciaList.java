package br.com.infox.pje.list;

import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.security.Identity;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.TempoAudienciaOrgaoJulgador;
import br.jus.pje.nucleo.entidades.TipoAudiencia;

@Name(TempoAudienciaList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class TempoAudienciaList extends EntityList<TempoAudienciaOrgaoJulgador> {

	public static final String NAME = "tempoAudienciaList";

	private static final long serialVersionUID = 1L;

	private OrgaoJulgador orgaoJulgador;
	private TipoAudiencia tipoAudiencia;

	private static final String DEFAULT_EJBQL = "select o from TempoAudienciaOrgaoJulgador o ";
	private static final String DEFAULT_ORDER = "o.orgaoJulgador";

	private static final String R1 = "o.orgaoJulgador = #{tempoAudienciaList.orgaoJulgador}";
	private static final String R2 = "o.tipoAudiencia = #{tempoAudienciaList.tipoAudiencia}";

	@Override
	protected void addSearchFields() {
		addSearchField("orgaoJulgador", SearchCriteria.igual, R1);
		addSearchField("tipoAudiencia", SearchCriteria.igual, R2);
		addSearchField("ativo", SearchCriteria.igual);
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

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setTipoAudiencia(TipoAudiencia tipoAudiencia) {
		this.tipoAudiencia = tipoAudiencia;
	}

	public TipoAudiencia getTipoAudiencia() {
		return tipoAudiencia;
	}

	@Override
	public void newInstance() {
		setOrgaoJulgador(null);
		setTipoAudiencia(null);
		super.newInstance();
	}
	
	@Override
	public List<TempoAudienciaOrgaoJulgador> list(int maxResult) {
		info("OJ: {0}", Authenticator.getOrgaoJulgadorAtual());
		if (!Identity.instance().hasRole("administrador") && !Identity.instance().hasRole("admin")) {
			info("orgao julgador atual: {0}", Authenticator.getOrgaoJulgadorAtual().getOrgaoJulgador());
			setOrgaoJulgador(Authenticator.getOrgaoJulgadorAtual());
		}
		
		return super.list(maxResult);
	}
	
}