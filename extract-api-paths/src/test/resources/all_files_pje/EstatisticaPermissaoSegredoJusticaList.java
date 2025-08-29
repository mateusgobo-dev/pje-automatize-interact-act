package br.com.infox.pje.list;

import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;

@Name(EstatisticaPermissaoSegredoJusticaList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EstatisticaPermissaoSegredoJusticaList extends EntityList<Object[]> {

	public static final String NAME = "estatisticaPermissaoSegredoJusticaList";
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select distinct(o.processoTrf),o.orgaoJulgador,o.classeJudicial.codClasseJudicial ||' '|| o.classeJudicial.classeJudicial "
			+ "from EstatisticaProcessoJusticaFederal o where o.processoTrf.segredoJustica = true "
			+ "and exists (select pvs from ProcessoVisibilidadeSegredo pvs where "
			+ "pvs.processo.idProcesso = o.processoTrf.idProcessoTrf)";

	private static final String DEFAULT_ORDER = "o.orgaoJulgador,o.processoTrf";

	private static final String R1 = " o.secaoJudiciaria = #{estatisticaJusticaFederalPermissaoSegredoJusticaAction.secao.cdSecaoJudiciaria} ";
	private static final String R2 = " o.orgaoJulgador.idOrgaoJulgador = #{estatisticaJusticaFederalPermissaoSegredoJusticaAction.orgaoJulgador.idOrgaoJulgador} ";
	private static final String R3 = " o.processoTrf.idProcessoTrf = #{estatisticaJusticaFederalPermissaoSegredoJusticaAction.processo.idProcesso} ";
	private static final String R4 = " exists (select pvs from ProcessoVisibilidadeSegredo pvs where "
			+ "pvs.processo.idProcesso = o.processoTrf.idProcessoTrf and "
			+ "pvs.pessoa.idUsuario = #{estatisticaJusticaFederalPermissaoSegredoJusticaAction.usuario.idUsuario})";

	@Override
	protected void addSearchFields() {
		addSearchField("secaoJudiciaria", SearchCriteria.igual, R1);
		addSearchField("orgaoJulgador", SearchCriteria.igual, R2);
		addSearchField("processo", SearchCriteria.igual, R3);
		addSearchField("usuario", SearchCriteria.igual, R4);
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
	public List<Object[]> getResultList() {
		setEjbql(getDefaultEjbql());
		return super.getResultList();
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	protected String getEntityName() {
		return NAME;
	}

	@Override
	public Object[] getEntity() {
		if (entity == null) {
			entity = (Object[]) Contexts.getConversationContext().get(getEntityComponentName());
			if (entity == null) {
				entity = new Object[0];
			}
		}
		return entity;
	}

}
