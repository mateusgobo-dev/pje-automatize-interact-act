/* $Id: SalaList.java 15574 2010-12-23 13:52:05Z edsonaraujo $ */

package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoVoto;

@Name(SessaoPautaProcessoVotoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class SessaoPautaProcessoVotoList extends EntityList<SessaoPautaProcessoVoto> {

	public static final String NAME = "sessaoPautaProcessoVotoList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from SessaoPautaProcessoVoto o "
			+ "where (o.usuarioRelator != o.usuarioDesembargadorAcompanhado or o.usuarioRelator is null)";
	private static final String DEFAULT_ORDER = "idSessaoPautaProcessoVoto";

	private static final String R1 = "o.sessaoPautaProcessoTrf = #{sessaoPautaProcessoTrfHome.instance}";
	private static final String R2 = "o.processoTrf = #{sessaoPautaProcessoVotoHome.processo}";

	@Override
	protected void addSearchFields() {
		addSearchField("sessaoPautaProcessoTrf", SearchCriteria.igual, R1);
		addSearchField("processoTrf", SearchCriteria.igual, R2);
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