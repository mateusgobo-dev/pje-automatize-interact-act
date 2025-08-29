package br.com.infox.cliente.component.suggest;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.AssuntoTrf;

/**
 * Componente para formulários que realiza consulta de Assuntos para campo auto-complete
 * 
 * @author Thiago Nascimento Figueiredo
 *
 */
@Name(AssuntoTrfComCompetenciaSuggestBean.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class AssuntoTrfComCompetenciaSuggestBean extends AbstractSuggestBean<AssuntoTrf> {
	
	private static final long serialVersionUID = 8881472247080573715L;

	public static final String NAME = "assuntoTrfComCompetenciaSuggest";

	/**
	 * Consulta implementada conforme super classe.
	 * @see br.com.infox.component.suggest.SuggestBean#getEjbql()
	 */
	@Override
	public String getEjbql() {
		StringBuilder query = new StringBuilder();
		query.append(" SELECT distinct n FROM AssuntoTrf n INNER JOIN n.competenciaClasseAssuntoList c ");
		query.append(" WHERE lower(TO_ASCII(n.assuntoTrf)) LIKE lower(concat('%',TO_ASCII(:");
		query.append(INPUT_PARAMETER);
		query.append("), '%')) ");
		query.append(" ORDER BY n.assuntoTrf ");
		return query.toString();
	}

	public static AssuntoTrfComCompetenciaSuggestBean instance() {
		return (AssuntoTrfComCompetenciaSuggestBean) Component.getInstance(NAME);
	}

}
