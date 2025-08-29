package br.com.infox.cliente.component.suggest;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;

public abstract class AbstractMagistradoSuggestBean extends AbstractSuggestBean<PessoaMagistrado> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from PessoaMagistrado o where ");
		sb.append("bitwise_and(o.pessoa.especializacoes, " + PessoaFisica.MAG + ") = " + PessoaFisica.MAG);
		sb.append("and lower(TO_ASCII(o.nome)) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) order by o.nome");
		return sb.toString();
	}

}
