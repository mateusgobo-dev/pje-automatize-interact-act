package br.com.infox.cliente.component.suggest;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.com.infox.utils.Constantes;
import br.jus.pje.nucleo.entidades.AssuntoTrf;

@Name("assuntoCodigoAtoInfracionalSuggest")
@BypassInterceptors
public class AssuntoCodigoAtoInfracionalSuggestBean extends AbstractSuggestBean<AssuntoTrf> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		return " select o.assunto from AssuntoAgrupamento o "
			 + " where lower(to_ascii(o.assunto.tituloCodigoAssunto)) like " + " lower(concat('%', TO_ASCII(:" + INPUT_PARAMETER
			 + "), '%')) " + " and o.agrupamento.codAgrupamento = '"+Constantes.COD_AGRUPAMENTO_ATO_INFRACIONAL + "' and o.assunto.ativo = true ";
	}

}
