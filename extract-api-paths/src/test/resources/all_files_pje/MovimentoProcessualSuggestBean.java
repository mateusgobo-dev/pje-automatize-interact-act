package br.com.infox.cliente.component.suggest;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.cnj.pje.view.ConsultaProcessualAction;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.ProcessoEvento;

/**
 * Componente de controle de sugestões para a entidade {@link ProcessoEvento}.
 */
@Name(MovimentoProcessualSuggestBean.NAME)
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class MovimentoProcessualSuggestBean extends AbstractSuggestBean<Evento> {

	private static final long serialVersionUID = 1487581201278814771L;

	public static final String NAME = "movimentoProcessualSuggest";

	private String defaultValue;

	@Override
	public String getEjbql() {
		StringBuilder q = new StringBuilder(" SELECT o FROM Evento AS o ");
		q.append(" WHERE o.ativo = true AND o.segredoJustica = false ");
		q.append(" AND lower(TO_ASCII(o.evento)) like lower(concat('%',TO_ASCII(:");
		q.append(INPUT_PARAMETER);
		q.append("), '%')) ");
		q.append(" OR o.codEvento like (:");
		q.append(INPUT_PARAMETER);
		q.append(") ");
		q.append(" ORDER BY o.evento ASC ");
		return q.toString();
	}

	@Override
	protected String getEventSelected() {
		return "movimentacaoProcessualChangedEvent";
	}

	@Override
	public String getDefaultValue() {
		if (defaultValue == null && getInstance() != null) {
			defaultValue = getInstance().getEvento();
		} else {
			defaultValue = StringUtils.EMPTY;
		}
		return defaultValue;
	}

	@Override
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public static MovimentoProcessualSuggestBean instance() {
		return (MovimentoProcessualSuggestBean) Component.getInstance(MovimentoProcessualSuggestBean.NAME);
	}
	
	@Override
	public void setInstance(Evento instance) {
		super.setInstance(instance);
		ConsultaProcessualAction consultaProcessualAction = (ConsultaProcessualAction) Component.getInstance(ConsultaProcessualAction.NAME);
		consultaProcessualAction.setMovimentacaoProcessual(instance);
	}

	@Override
	public Integer getLimitSuggest() {
		return null;
	}

}
