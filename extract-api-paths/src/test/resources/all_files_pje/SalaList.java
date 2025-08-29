package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.Sala;

@Name(SalaList.NAME)
@BypassInterceptors
@Scope(ScopeType.CONVERSATION)
public class SalaList extends EntityList<Sala> {

	public static final String NAME = "salaList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from Sala o ";
	private static final String DEFAULT_ORDER = "idSala";

	private static final String R1 = "o in(select oj.sala from SalaHorario oj "
			+ "where oj.ativo = #{salaList.entity.ativo})";
	private static final String R2 = "o.orgaoJulgadorColegiado= #{salaList.entity.orgaoJulgadorColegiado}";
	private static final String R3 = "o.orgaoJulgador = #{salaList.entity.orgaoJulgador}";

	@Override
	protected void addSearchFields() {
		//A combo "órgão julgador" só é exibida para o perfil de administrador
		if ((Authenticator.getPapelAtual().getIdentificador().equals("admin"))
				|| (Authenticator.getPapelAtual().getIdentificador().equals("administrador"))) {
			if(!ParametroUtil.instance().isPrimeiroGrau()) {
				addSearchField("orgaoJulgadorColegiado", SearchCriteria.igual, R2);
			}
			addSearchField("orgaoJulgador", SearchCriteria.igual, R3);
		}
		addSearchField("sala", SearchCriteria.contendo);
		addSearchField("ativo", SearchCriteria.igual, R1);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	@Override
	protected String getDefaultEjbql() {
		StringBuffer filtroPorOrgaoJulgador = new StringBuffer();
		if ((!Authenticator.getPapelAtual().getIdentificador().equals("admin"))
				&& (!Authenticator.getPapelAtual().getIdentificador().equals("administrador"))
				&& (ParametroUtil.instance().isPrimeiroGrau())) {
			filtroPorOrgaoJulgador.append("where o.orgaoJulgador = #{authenticator.getOrgaoJulgadorAtual()} ");
		}
		if((!Authenticator.getPapelAtual().getIdentificador().equals("admin"))
				&& (!Authenticator.getPapelAtual().getIdentificador().equals("administrador"))
				&& !ParametroUtil.instance().isPrimeiroGrau()) {
			filtroPorOrgaoJulgador.append("where o.orgaoJulgadorColegiado = #{authenticator.getOrgaoJulgadorColegiadoAtual()} ");
		}
		return DEFAULT_EJBQL.concat(filtroPorOrgaoJulgador.toString());
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

}