package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.Bairro;

@Name(BairroList.NAME)
@BypassInterceptors
@Scope(ScopeType.CONVERSATION)
public class BairroList extends EntityList<Bairro> {

	private static final long serialVersionUID = 7916530899164387328L;
	public static final String NAME = "bairroList";
	private static int localizacao = Authenticator.getIdLocalizacaoAtual();

	private static final String DEFAULT_EJBQL = 
		" select o from Bairro o, CentralMandadoLocalizacao cl " +
		" where cl.localizacao.idLocalizacao = " + localizacao + 
		" and cl.centralMandado.idCentralMandado = o.area.centralMandado.idCentralMandado";
	
	private static final String DEFAULT_ORDER = "o.dsBairro";

	@Override
	protected void addSearchFields() {
		addSearchField("dsBairro", SearchCriteria.contendo);
		addSearchField("ativo", SearchCriteria.igual);
		addSearchField("area.idArea", SearchCriteria.igual);
		addSearchField("municipio.idMunicipio", SearchCriteria.igual);
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

}
