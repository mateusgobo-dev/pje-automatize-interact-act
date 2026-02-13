package br.com.infox.cliente.component.suggest;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.home.ClasseJudicialHome;
import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.TipoCertidao;

@Name("tipoCertidaoSuggest")
@BypassInterceptors
public class TipoCertidaoSuggestBean extends AbstractSuggestBean<TipoCertidao> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from TipoCertidao o where o.ativo = true and ");
		sb.append("lower(TO_ASCII(o.tipoCertidao)) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) ");
		sb.append("and o.idTipoCertidao not in "
				+ "(select c.tipoCertidao.idTipoCertidao from ClasseJudicialTipoCertidao c "
				+ "where c.classeJudicial.idClasseJudicial = "
				+ ClasseJudicialHome.instance().getInstance().getIdClasseJudicial() + ") ");
		sb.append("order by o.tipoCertidao");
		return sb.toString();
	}

}
