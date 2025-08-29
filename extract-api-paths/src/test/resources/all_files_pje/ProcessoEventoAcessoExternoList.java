package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.ProcessoEvento;

@Name("processoEventoAcessoExternoList")
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ProcessoEventoAcessoExternoList extends EntityList<ProcessoEvento> {

	private static final long serialVersionUID = 1L;
	
	private static final String DEFAULT_EJBQL = "select o from ProcessoEvento o LEFT JOIN o.processoDocumento p "
			+ "where ((p is null and o.evento.segredoJustica = false and o.visibilidadeExterna = true) "
			+ "or (p is not null and p.dataJuntada is not null and p.documentoSigiloso = false and o.visibilidadeExterna = true))";
	
	private static final String DEFAULT_ORDER = "to_char(o.dataAtualizacao, 'yyyy-mm-dd') desc";

	private static final String R1 = "o.processo.idProcesso = #{processoTrfHome.instance.idProcessoTrf} ";

	@Override
	protected void addSearchFields() {
		addSearchField("processo", SearchCriteria.igual, R1);
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
