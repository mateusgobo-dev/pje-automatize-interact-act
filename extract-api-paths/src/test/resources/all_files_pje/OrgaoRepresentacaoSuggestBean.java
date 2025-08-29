package br.com.infox.cliente.component.suggest;

import br.jus.cnj.pje.view.PessoaProcuradoriaAction;
import java.util.Collections;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.Util;
import br.com.infox.component.suggest.AbstractSuggestBean;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Procuradoria;

@Name(OrgaoRepresentacaoSuggestBean.NAME)
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class OrgaoRepresentacaoSuggestBean extends AbstractSuggestBean<Procuradoria> {

	private static final long serialVersionUID = 1L;
	private static final int LIMIT_SUGGEST_DEFAULT = 25;
	
	public static final String NAME = "orgaoRepresentacaoSuggest";
	
	private String defaultValue = null;

	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from Procuradoria o ");
		sb.append("where o.tipo = '");
		sb.append(getHome().getTipoProcuradoria());
		sb.append("' and ");
		sb.append("lower(TO_ASCII(o.nome)) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) order by 1");
		return getHome().getTipoProcuradoria() == null ? null : sb.toString();
	}
	

	@Override
	@SuppressWarnings("unchecked")
	public List<Procuradoria> suggestList(Object typed){
		String q = this.getEjbql();
		if (q != null && Util.isStringSemCaracterUnicode(typed.toString())){
			Query query = EntityUtil.createQuery(q).setParameter(INPUT_PARAMETER, typed);
			query.setMaxResults(LIMIT_SUGGEST_DEFAULT);
			return (List<Procuradoria>) query.getResultList();
		}
		return Collections.emptyList();
	}

	protected PessoaProcuradoriaAction getHome() {
		return (PessoaProcuradoriaAction) Component.getInstance("pessoaProcuradoriaAction");
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
