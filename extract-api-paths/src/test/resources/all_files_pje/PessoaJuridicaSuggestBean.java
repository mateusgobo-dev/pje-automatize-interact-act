package br.com.infox.cliente.component.suggest;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.PessoaJuridica;

@Name("pessoaJuridicaSuggest")
@BypassInterceptors
public class PessoaJuridicaSuggestBean extends AbstractSuggestBean<PessoaJuridica> {

	private static final long serialVersionUID = 1L;

	public static PessoaJuridicaSuggestBean instance() {
		return ComponentUtil.getComponent("pessoaJuridicaSuggest");
	}

	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from PessoaJuridica o ");
		sb.append("where o.inTipoPessoa = 'J' and lower(TO_ASCII(o.nome)) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) order by 1");
		return sb.toString();
	}

	@Override
	public String getDefaultValue() {
		return getInstance() != null ? getInstance().toString() : "";
	}
}
