package br.com.infox.cliente.component.suggest;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;

@Name("pessoaMagistradoSuggest")
@BypassInterceptors
public class PessoaMagistradoSuggestBean extends AbstractSuggestBean<PessoaMagistrado> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from PessoaMagistrado o where ");
		sb.append("o.ativo = true ");
		sb.append("and bitwise_and(o.pessoa.especializacoes, " + PessoaFisica.MAG + ") = " + PessoaFisica.MAG);
		sb.append("and lower(TO_ASCII(o.nome)) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) order by o.nome");
		return sb.toString();
	}

}
