/**
 * pje-web
 * Copyright (C) 2009-2014 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.com.infox.component.suggest;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.core.Events;
import org.jboss.seam.core.Expressions;

import br.com.itx.util.EntityUtil;

/**
 * Componente abstrato destinado a controlar caixas de sugestão.
 * 
 * @author Infox Tecnologia Ltda.
 *
 * @param <E> a classe a ser sugerida
 */
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public abstract class AbstractSuggestBean<E> implements SuggestBean<E>, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4096594162014652404L;

	private static final int LIMIT_SUGGEST_DEFAULT = 15;

	protected static final String INPUT_PARAMETER = "input";

	private E instance;

	private Object typed;
	private Object selected;
	private String expression;

	/* (non-Javadoc)
	 * @see br.com.infox.component.suggest.SuggestBean#suggestList(java.lang.Object)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<E> suggestList(Object typed){
		this.selected = null;
		this.typed = typed;		
		List<E> result = null;
		String q = getEjbql();
		if (q != null){
			Query query = EntityUtil.createQuery(q).setParameter(INPUT_PARAMETER, typed);
			if(getLimitSuggest() != null){
				query.setMaxResults(getLimitSuggest());
			}
			result = query.getResultList();
		}
		else{
			result = Collections.emptyList();
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see br.com.infox.component.suggest.SuggestBean#getInstance()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public E getInstance(){
		if (expression == null){
			return instance;
		}
		return (E) Expressions.instance().createValueExpression(expression).getValue();
	}

	/* (non-Javadoc)
	 * @see br.com.infox.component.suggest.SuggestBean#setInstance(java.lang.Object)
	 */
	@Override
	public void setInstance(E instance){
		this.selected = instance;
		if (expression == null){
			this.instance = instance;
		}else{
			Expressions.instance().createValueExpression(expression).setValue(instance);
		}
		Events.instance().raiseEvent(getEventSelected(), instance);
	}

	/**
	 * Recupera o evento deste componente de sugestão a ser disparado quando selecionado um elemento..
	 * 
	 * @return
	 */
	protected String getEventSelected(){
		return null;
	}

	/**
	 * Recupera a expressão de linguagem (EL) a ser utilizada quando da definição da instância 
	 * de modo a assegurar que eventuais invocações dessa expressão resulte no valor da 
	 * instância definida.
	 * 
	 * @return a expressão de linguagem
	 */
	public String getExpression(){
		return expression;
	}

	/**
	 * Define a expressao de linguagem (EL) a ser utilizada quando da definição da instância.
	 * Ao definir a instância, as chamadas subsequentes da expressão aqui indicada resultará
	 * na instância definida.
	 * 
	 * @param expression a expressão a ser definida
	 */
	public void setExpression(String expression){
		this.expression = "#{" + expression + "}";
	}

	/* (non-Javadoc)
	 * @see br.com.infox.component.suggest.SuggestBean#setDefaultValue(java.lang.String)
	 */
	@Override
	public void setDefaultValue(String obj){
	}

	/* (non-Javadoc)
	 * @see br.com.infox.component.suggest.SuggestBean#getDefaultValue()
	 */
	@Override
	public String getDefaultValue(){
		return getInstance() != null ? getInstance().toString() : "";
	}

	/**
	 * Recupera o número máximo de elementos a serem apresentados para o usuário.
	 * Para suprimir o limite, deve-se sobrescrever o método para passar a recuperar NULL.
	 * 
	 * @return o número máximo de elementos a serem apresentados, ou null para não haver limites
	 */
	public Integer getLimitSuggest(){
		return LIMIT_SUGGEST_DEFAULT;
	}

	public Object getTyped(){
		return typed;
	}

	public void setTyped(Object typed){
		this.typed = typed;
	}

	public Object getSelected(){
		return selected;
	}

	public void setSelected(Object selected){
		this.selected = selected;
	}

}