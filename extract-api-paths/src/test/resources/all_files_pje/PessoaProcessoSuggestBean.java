package br.com.infox.cliente.component.suggest;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.Pessoa;

@Name("pessoaProcessoSuggest")
@BypassInterceptors
public class PessoaProcessoSuggestBean extends AbstractSuggestBean<Pessoa>{

	private static final long serialVersionUID = -5231152016419120850L;

	@Override
	public String getEjbql(){
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT o.pessoa FROM ProcessoParte o where o.inSituacao='A'");
		sb.append(" and lower(TO_ASCII(o.pessoa.nome)) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) and o.processoTrf.idProcessoTrf = ");
		sb.append(ProcessoTrfHome.instance().getInstance().getIdProcessoTrf());
		sb.append(" order by o.pessoa.nome  ");
		return sb.toString();
	}

}
