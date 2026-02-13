package br.com.infox.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.BloqueioPauta;

@Name(BloqueioPautaList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class BloqueioPautaList extends EntityList<BloqueioPauta> {

	private static final long serialVersionUID = 6188216335938255171L;

	public static final String NAME = "bloqueioPautaList";
	private final String DEFAULT_EJBQL = "select o from BloqueioPauta o ";
	private final String DEFAULT_ORDER = "idBloqueioPauta";

	private final String R1 = "o.salaAudiencia = #{bloqueioPautaHome.sala}";
	private final String R2 = "o.salaAudiencia.orgaoJulgador = #{bloqueioPautaHome.orgao}";
	private final String R3 = "cast(o.dtInicial as timestamp) >= cast(#{bloqueioPautaHome.dtInicial} as timestamp)";
	private final String R4 = "cast(o.dtFinal as timestamp) <= cast(#{bloqueioPautaHome.dtFinal} as timestamp)";

	@Override
	protected void addSearchFields() {
		addSearchField("salaAudiencia", SearchCriteria.igual, R1);
		addSearchField("salaAudiencia.orgaoJulgador", SearchCriteria.igual, R2);
		addSearchField("ativo", SearchCriteria.igual);
		addSearchField("dtInicial", SearchCriteria.maiorIgual, R3);
		addSearchField("dtFinal", SearchCriteria.maiorIgual, R4);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	@Override
	protected String getDefaultEjbql() {
		StringBuffer filtroPorOrgaoJulgador = new StringBuffer();
  		boolean isAdmin = (Authenticator.getPapelAtual().getIdentificador().equals("admin") || 
  						   Authenticator.getPapelAtual().getIdentificador().equals("administrador"));
  		if (!isAdmin) {
  			if (Authenticator.getOrgaoJulgadorAtual() != null) {
  				filtroPorOrgaoJulgador.append("where o.salaAudiencia.orgaoJulgador = #{authenticator.getOrgaoJulgadorAtual()} ");
  			} else if (Authenticator.getOrgaoJulgadorColegiadoAtual() != null) {
  				filtroPorOrgaoJulgador.append("where o.salaAudiencia.orgaoJulgador in (select ofcoj.orgaoJulgador from OrgaoJulgadorColegiadoOrgaoJulgador ofcoj where ofcoj.orgaoJulgadorColegiado = #{authenticator.getOrgaoJulgadorColegiadoAtual()}) ");
  			} else {
  				filtroPorOrgaoJulgador.append("where o.salaAudiencia.orgaoJulgador is null");
  			}
  		}
		return DEFAULT_EJBQL.concat(filtroPorOrgaoJulgador.toString());
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

}
