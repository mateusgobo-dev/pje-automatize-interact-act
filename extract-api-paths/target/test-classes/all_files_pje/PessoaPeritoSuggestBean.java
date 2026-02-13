package br.com.infox.cliente.component.suggest;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import br.com.infox.component.suggest.AbstractSuggestBean;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.PessoaPerito;

@Name("pessoaPeritoSuggest")
@BypassInterceptors
public class PessoaPeritoSuggestBean extends AbstractSuggestBean<PessoaPerito> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		String idLocalizacoesUsuario = Authenticator.getIdsLocalizacoesFilhasAtuais();
		
		StringBuilder query = new StringBuilder("SELECT DISTINCT o ")
				.append(" FROM PessoaPerito o ")
				.append(" JOIN o.orgaoJulgadorPessoaPeritoList ojList ")
				.append(" WHERE 1=1 ")
				.append(" AND (LOWER(TO_ASCII(o.nome)) LIKE LOWER(CONCAT('%',TO_ASCII(:" + INPUT_PARAMETER + "), '%')) ")
				.append(" OR o.login LIKE (CONCAT(TO_ASCII(:" + INPUT_PARAMETER + "), '%') )) ")
				.append(" AND ojList.orgaoJulgador.localizacao.idLocalizacao IN ("+idLocalizacoesUsuario+")")
				.append(" ORDER BY o.nome");
				
		return query.toString();
	}

}
