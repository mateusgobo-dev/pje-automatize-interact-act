/* $Id: CargoList.java 13417 2010-10-28 13:49:03Z allan $ */

package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.cliente.util.ParametroUtil;
import br.jus.pje.nucleo.entidades.Evento;

@Name(TipoRedistribuicaoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class TipoRedistribuicaoList extends EntityList<Evento> {

	public static final String NAME = "tipoRedistribuicaoList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from Evento o " + "where o.ativo = true "
			+ "and o.eventoSuperior.idEvento = " + ParametroUtil.instance().getEventoTipoRedistribuicao().getIdEvento();
	private static final String DEFAULT_ORDER = "idEvento";

	private static final String R1 = "o not in (select mer.eventoRedistribuicao from MotivoEventoRedistribuicao mer "
			+ "where mer.motivoRedistribuicao = #{motivoRedistribuicaoHome.instance})";
	private static final String R2 = "o.evento like concat('%', #{motivoRedistribuicaoHome.eventoRedistribuicao}, '%')";

	@Override
	protected void addSearchFields() {
		addSearchField("idEventoRedistribuicao", SearchCriteria.contendo, R1);
		addSearchField("eventoRedistribuicao", SearchCriteria.contendo, R2);
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
}
