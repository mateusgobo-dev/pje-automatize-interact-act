package br.com.infox.cliente.component.suggest;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.PessoaLocalizacao;

@Name("entidadeLocalizacaoSuggest")
@BypassInterceptors
public class EntidadeLocalizacaoSuggestBean extends AbstractSuggestBean<PessoaLocalizacao> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from PessoaLocalizacao o ");
		sb.append("where o.pessoa in ");
		sb.append("(select p from PessoaJuridica p ");
		sb.append("where lower(TO_ASCII(p.nome)) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) ");
		sb.append(" and p.tipoPessoa in ");
		sb.append("  (select list from TipoPessoa tp ");
		sb.append("    inner join tp.tipoPessoaList list ");
		sb.append("   where tp.tipoPessoa = 'Entidades'))");
		return sb.toString();
	}

}
