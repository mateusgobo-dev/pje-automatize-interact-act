package br.com.infox.cliente.component.suggest;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.ProcessoParte;

@Name("processoParteSuggest")
@BypassInterceptors
public class ProcessoParteSuggestBean extends AbstractSuggestBean<ProcessoParte> {

	public static final String PROCESSO_PARTE_SUGGEST_EVENT_SELECTED = "processoParteSuggestEventSelected";
	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT o FROM ProcessoParte o where ");
		sb.append("lower(TO_ASCII(o.pessoa.nome)) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) and o.processoTrf.idProcessoTrf = ");
		sb.append(ProcessoTrfHome.instance().getInstance().getIdProcessoTrf());
		sb.append(" order by o.pessoa.nome");
		return sb.toString();
	}

	@Override
	protected String getEventSelected() {
		return PROCESSO_PARTE_SUGGEST_EVENT_SELECTED;
	}

}
