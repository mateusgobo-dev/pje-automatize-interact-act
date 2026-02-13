package br.com.infox.cliente.component.suggest;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.pje.nucleo.entidades.AssuntoTrf;

@Name("assuntoCodigoRemessaSTFSuggestBean")
public class AssuntoCodigoRemessaSTFSuggestBean extends
		AbstractSuggestBean<AssuntoTrf> {

	private static final long serialVersionUID = 1L;

	@In
	private ParametroService parametroService;

	@Override
	public String getEjbql() {
		String codigoAgrupamentoSTF = parametroService
				.valueOf(Parametros.CODIGO_AGRUPAMENTO_ASSUNTO_REMESSA_STF);

		return " select o.assunto from AssuntoAgrupamento o "
				+ " where lower(to_ascii(o.assunto.tituloCodigoAssunto)) like "
				+ " lower(concat('%', TO_ASCII(:" + INPUT_PARAMETER
				+ "), '%')) " + " and o.agrupamento.codAgrupamento = '"
				+ codigoAgrupamentoSTF + "' and o.assunto.ativo = true ";
	}

}
