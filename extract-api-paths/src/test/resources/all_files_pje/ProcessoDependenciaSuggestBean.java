package br.com.infox.cliente.component.suggest;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name("processoDependenciaSuggest")
@BypassInterceptors
public class ProcessoDependenciaSuggestBean extends AbstractSuggestBean<ProcessoTrf> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		ProcessoTrf processo = ProcessoTrfHome.instance().getInstance();
		return "select o from ProcessoTrf o "
				+ "where lower(TO_ASCII(o.processo.numeroProcesso)) like lower(concat(TO_ASCII(:" + INPUT_PARAMETER
				+ "), '%')) AND o.idProcessoTrf != " + processo.getIdProcessoTrf()
				+ "order by o.processo.numeroProcesso";
	}

}
