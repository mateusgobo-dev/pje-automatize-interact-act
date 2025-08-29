package br.com.infox.cliente.component.suggest;

import java.util.Collections;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.Util;
import br.com.infox.cliente.home.PessoaServidorHome;
import br.com.infox.component.suggest.AbstractSuggestBean;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Municipio;

@Name(PessoaServidorMunicipioSuggestBean.NAME)
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class PessoaServidorMunicipioSuggestBean extends AbstractSuggestBean<Municipio> {

	private static final long serialVersionUID = 1L;
	private static final int LIMIT_SUGGEST_DEFAULT = 50;
	
	public static final String NAME = "pessoaServidorMunicipioSuggest";

	@Override
	public String getEjbql() {
		return getHome().getEstado() == null ? null : "select o from Municipio o " + "where o.estado.estado = '"
				+ getHome().getEstado() + "' and " + "lower(TO_ASCII(o.municipio)) like lower(concat('%',TO_ASCII(:"
				+ INPUT_PARAMETER + "), '%')) order by o.municipio";
	}
	
	/**
	 * Metodo que retorna sugestoes de Municipios a partir da digitacao do usuario.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Municipio> suggestList(Object typed){
		String q = this.getEjbql();
		if (q != null && Util.isStringSemCaracterUnicode(typed.toString())){
			Query query = EntityUtil.createQuery(q).setParameter(INPUT_PARAMETER, typed);
			query.setMaxResults(LIMIT_SUGGEST_DEFAULT);
			return (List<Municipio>) query.getResultList();
		}
		return Collections.emptyList();
	}

	protected PessoaServidorHome getHome() {
		return (PessoaServidorHome) Component.getInstance("pessoaServidorHome");
	}

}
