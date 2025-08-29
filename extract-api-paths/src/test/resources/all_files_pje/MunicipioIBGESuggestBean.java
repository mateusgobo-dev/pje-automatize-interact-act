package br.jus.csjt.pje.commons.util;

import java.util.Collections;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.Util;
import br.com.infox.component.suggest.AbstractSuggestBean;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.jt.entidades.MunicipioIBGE;
import br.jus.pje.nucleo.entidades.Estado;

@Name(MunicipioIBGESuggestBean.NAME)
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class MunicipioIBGESuggestBean extends AbstractSuggestBean<MunicipioIBGE> {

	public static final String NAME = "municipioIBGESuggestBean";
	private static final int LIMIT_SUGGEST_DEFAULT = 25;

	private static final long serialVersionUID = 1L;
	private Estado estado;

	@Override
	public String getEjbql() {
		String hql;
		hql = "select m from MunicipioIBGE m ";

		if (getEstado() != null) {
			hql += "where m.uf.idEstado = " + getEstado().getIdEstado();
		} else {
			hql += "where 1=2 ";
		}

		hql += " and lower(TO_ASCII(m.nomeMunicipio)) like lower " + " (concat('%', TO_ASCII(:" + INPUT_PARAMETER
				+ "), '%')) " + " order by m.nomeMunicipio ";

		return hql;
	}
	
	public static MunicipioIBGESuggestBean instance(){
		return ComponentUtil.getComponent(NAME);
	}
	
	/*
	 * [PJEII-2143] PJE-JT: Cristiano Nascimento : PJE-1.4.4
	 * Sobrescrevi o método suggestList da classe AbstractSuggestBean para validar somente os caracteres do suggestion box município. 
	 * Caso o usuário digite algum caracter unicode, o método não monta a consulta, 
	 * retornando vazia a Lista de Municípios. 
	 */
	
	@Override
	@SuppressWarnings("unchecked")
	public List<MunicipioIBGE> suggestList(Object typed){
		List<MunicipioIBGE> result = null;
		String q = getEjbql();
		
		//validação dos caracteres unicode
		if (q != null && Util.isStringSemCaracterUnicode(typed.toString())){
			Query query = EntityUtil.createQuery(q).setParameter(INPUT_PARAMETER, typed);
			// PJEII-2448 - aumento do limite para possiveis sugestoes.
			query.setMaxResults(LIMIT_SUGGEST_DEFAULT);
			result = query.getResultList();
		}
		else{
			result = Collections.emptyList();
		}
		return result;
	}

	public void setEstado(Estado estado) {
		this.estado = estado;
	}

	public Estado getEstado() {
		return estado;
	}

	@Override
	public void setInstance(MunicipioIBGE instance) {
		super.setInstance(instance);
		if (instance != null) {
			setEstado(instance.getUf());
		} else {
			setEstado(null);
		}
	}

	@Override
	public String getDefaultValue() {
		return getInstance() != null ? getInstance().toString() : "";
	}

}
