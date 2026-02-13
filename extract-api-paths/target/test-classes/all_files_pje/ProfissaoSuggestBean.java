package br.com.infox.cliente.component.suggest;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.Util;
import br.com.infox.component.suggest.AbstractSuggestBean;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.ProfissaoSinonimo;

@Name(ProfissaoSuggestBean.NAME)
@BypassInterceptors
public class ProfissaoSuggestBean extends AbstractSuggestBean<Object> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "profissaoSuggest";

	public static ProfissaoSuggestBean instance() {
		return ComponentUtil.getComponent(ProfissaoSuggestBean.NAME);
	}

	public void newInstance() {
		setInstance(null);
	}

	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from Profissao o ");
		sb.append("where lower(TO_ASCII(o.profissao)) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) order by 1");
		return sb.toString();
	}

	private String getEjbqlProfissaoSinonimo() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProfissaoSinonimo o ");
		sb.append("where lower(TO_ASCII(o.sinonimo)) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) order by 1");
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	@Override
	/**
	 * Faz a pesquisa nas bases de Profissao e ProfissaoSinonimo
	 * retornando uma lista com os resultado
	 */
	public List<Object> suggestList(Object typed) {
		List<Object> result = null;

		Query query = EntityUtil.getEntityManager().createQuery(getEjbql()).setParameter(INPUT_PARAMETER, typed);
		
		Query queryProfissaoSinonimo = EntityUtil.getEntityManager().createQuery(getEjbqlProfissaoSinonimo())
				.setParameter(INPUT_PARAMETER, typed);
		
		if (getLimitSuggest() != null) {
			query.setMaxResults(getLimitSuggest());
			queryProfissaoSinonimo.setMaxResults(getLimitSuggest());			
		}
		
		/*
		 * [PJEII-2139] PJE-JT: Cristiano Nascimento : PJE-1.4.4
		 * Inclusão de validação de caracteres unicode. Caso exista, ele não mostra o resultado da consulta. 
		 * retornando vazia a Lista de Profissões. 
		 */
		if (Util.isStringSemCaracterUnicode(typed.toString())) {
			result = query.getResultList();
			result.addAll(queryProfissaoSinonimo.getResultList());
		}
		
		return result;
	}

	@Override
	public String getDefaultValue() {
		return getInstance() != null ? getInstance().toString() : "";
	}

	@Override
	public void setInstance(Object instance) {
		if (instance instanceof ProfissaoSinonimo) {
			instance = ((ProfissaoSinonimo) instance).getProfissao();
		}

		super.setInstance(instance);
	}

	@Override
	protected String getEventSelected() {
		return "profissaoChangedEvent";
	}

}
