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
import br.com.infox.cliente.home.PessoaAdvogadoHome;
import br.com.infox.component.suggest.AbstractSuggestBean;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Municipio;

@Name("pessoaAdvogadoMunicipioSuggest")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class PessoaAdvogadoMunicipioSuggestBean extends AbstractSuggestBean<Municipio> {

	public static final String NAME = "pessoaAdvogadoMunicipioSuggest";
	private static final int LIMIT_SUGGEST_DEFAULT = 50;

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		Estado estado = getEstado();
		if (estado == null) {
			return null;
		} else {
			sb.append("select o from Municipio o ");
			sb.append("where o.estado.idEstado = ");
			sb.append(estado.getIdEstado());
			sb.append(" and ");
			sb.append("lower(TO_ASCII(o.municipio)) like lower(concat('%',TO_ASCII(:");
			sb.append(INPUT_PARAMETER);
			sb.append("), '%')) order by o.municipio");
			return sb.toString();
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Municipio> suggestList(Object typed){
		List<Municipio> result = null;
		String q = getEjbql();
		if (q != null && Util.isStringSemCaracterUnicode(typed.toString())){
			Query query = EntityUtil.createQuery(q).setParameter(INPUT_PARAMETER, typed);
			query.setMaxResults(LIMIT_SUGGEST_DEFAULT);
			result = query.getResultList();
		}
		else{
			result = Collections.emptyList();
		}
		return result;
	}

	protected PessoaAdvogadoHome getHome() {
		return (PessoaAdvogadoHome) Component.getInstance("pessoaAdvogadoHome");
	}

	public Estado getEstado() {
		return PessoaAdvogadoHome.instance().getEstado();
	}

}
