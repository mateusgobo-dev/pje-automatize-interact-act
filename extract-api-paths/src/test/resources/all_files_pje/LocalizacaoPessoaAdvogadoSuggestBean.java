package br.com.infox.cliente.component.suggest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.Localizacao;

@Name("localizacaoPessoaAdvogadoSuggestBean")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class LocalizacaoPessoaAdvogadoSuggestBean extends AbstractSuggestBean<Localizacao> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder("select l from Localizacao l ")
			.append("join fetch l.usuarioLocalizacaoList u join fetch u.papel p ")
			.append("where lower(l.localizacao) like lower(concat('%',TO_ASCII(:input), '%')) ")
			.append("and  p.idPapel = #{parametroUtil.getPapelAdvogado().getIdPapel()} and l.ativo = true ")
			.append("order by l.localizacao");
		
		return sb.toString();
	}
	
}
