package br.com.jt.pje.list;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import java.util.HashMap;
import java.util.Map;


@Name(OrgaoJulgadorSessaoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class OrgaoJulgadorSessaoList extends EntityList<OrgaoJulgador> {
    public static final String NAME = "orgaoJulgadorSessaoList";
    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_EJBQL = "select o.orgaoJulgador from OrgaoJulgadorColegiadoOrgaoJulgador o " +
    	"where o.orgaoJulgador.ativo = true and o.dataInicial <= current_date and (o.dataFinal >= current_date or o.dataFinal is null)";  
    private static final String DEFAULT_ORDER = "o.orgaoJulgador.orgaoJulgador";
    private static final String R1 = "o.orgaoJulgadorColegiado = #{sessaoAction.instance.orgaoJulgadorColegiado}";

    protected void addSearchFields() {
        addSearchField("orgaoJulgadorColegiado", SearchCriteria.igual, R1);
    }

    protected Map<String, String> getCustomColumnsOrder() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("orgaoJulgador", "o.orgaoJulgador.orgaoJulgador");

        return map;
    }

    protected String getDefaultEjbql() {
        return DEFAULT_EJBQL;
    }

    protected String getDefaultOrder() {
        return DEFAULT_ORDER;
    }
}
