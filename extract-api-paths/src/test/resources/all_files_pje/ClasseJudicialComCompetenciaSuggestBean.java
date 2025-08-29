package br.com.infox.cliente.component.suggest;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.ClasseJudicial;

/**
 * Componente Suggest para recuperar classe judicial com compentência atribuída.
 * 
 * @author Thiago Nascimento Figueiredo
 *
 */
@Name(ClasseJudicialComCompetenciaSuggestBean.NAME)
@Scope(ScopeType.CONVERSATION)
public class ClasseJudicialComCompetenciaSuggestBean extends AbstractSuggestBean<ClasseJudicial> {

	private static final long serialVersionUID = -2771304210259887779L;

	public static final String NAME = "classeJudicialComCompetenciaSuggest";

	/**
	 * Operação que recupera registros de classe judicial com compentência atribuída.
	 */
	@Override
	public String getEjbql() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT distinct o FROM ClasseJudicial o ");
		query.append(" INNER JOIN o.classeAplicacaoList ca INNER JOIN ca.competenciaClasseAssuntoList c ");
		query.append(" WHERE  lower(TO_ASCII(o.classeJudicial)) like lower(concat('%',TO_ASCII(:");
		query.append(INPUT_PARAMETER);
		query.append("), '%')) order by o.classeJudicial");
		return query.toString();
	}

	public static ClasseJudicialComCompetenciaSuggestBean instance() {
		return (ClasseJudicialComCompetenciaSuggestBean) Component.getInstance(NAME);
	}
}
