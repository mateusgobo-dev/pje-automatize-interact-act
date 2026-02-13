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
import br.com.infox.cliente.home.PessoaFisicaHome;
import br.com.infox.component.suggest.AbstractSuggestBean;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Municipio;

@Name(PessoaFisicaMunicipioSuggestBean.NAME)
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class PessoaFisicaMunicipioSuggestBean extends AbstractSuggestBean<Municipio> {

	private static final long serialVersionUID = 1L;
	private static final int LIMIT_SUGGEST_DEFAULT = 50;
	
	public static final String NAME = "pessoaFisicaMunicipioSuggest";
	
	private String defaultValue = null;

	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from Municipio o ");
		sb.append("where o.estado.estado = '");
		sb.append(getHome().getEstado());
		sb.append("' and ");
		sb.append("lower(TO_ASCII(o.municipio)) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) order by o.municipio");
		return getHome().getEstado() == null ? null : sb.toString();
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

	protected PessoaFisicaHome getHome() {
		return (PessoaFisicaHome) Component.getInstance("pessoaFisicaHome");
	}
	
	@Override
  	public String getDefaultValue() {
		if(defaultValue == null)
			return super.getDefaultValue();
		return defaultValue;
  	}

  	@Override
  	public void setDefaultValue(String defaultValue) {
  		this.defaultValue = defaultValue;
  	}

}
