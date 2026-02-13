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
package br.com.infox.ibpm.component.suggest;

import java.util.List;

import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.component.suggest.AbstractSuggestBean;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.service.CepService;
import br.jus.pje.nucleo.entidades.Cep;

/**
 * Componente de controle de sugestões para a entidade {@link Cep}.
 * 
 * @author Infox Tecnologia Ltda.
 *
 */
@Name("cepSuggest")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class CepSuggestBean extends AbstractSuggestBean<Cep> {

	private static final long serialVersionUID = 1L;

	private String defaultValue;

	/**
	 * @return CepSuggestBean
	 */
	public static CepSuggestBean instance(){
		return ComponentUtil.getComponent("cepSuggest");
	}
	
	/* (non-Javadoc)
	 * @see br.com.infox.component.suggest.SuggestBean#getEjbql()
	 */
	@Override
	public String getEjbql() {
		String q = "SELECT c FROM Cep AS c WHERE c.ativo = true AND c.numeroCep = :" + INPUT_PARAMETER;
		return q;
	}

	/* (non-Javadoc)
	 * @see br.com.infox.component.suggest.AbstractSuggestBean#getEventSelected()
	 */
	@Override
	protected String getEventSelected() {
		return "cepChangedEvent";
	}

	/* (non-Javadoc)
	 * @see br.com.infox.component.suggest.AbstractSuggestBean#getDefaultValue()
	 */
	@Override
	public String getDefaultValue() {
		if (defaultValue == null) {
			if (getInstance() != null) {
				return getInstance().getNumeroCep();
			} else {
				return "";
			}
		} else {
			return defaultValue;
		}
	}

	/* (non-Javadoc)
	 * @see br.com.infox.component.suggest.AbstractSuggestBean#setDefaultValue(java.lang.String)
	 */
	@Override
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	/* (non-Javadoc)
	 * @see br.com.infox.component.suggest.AbstractSuggestBean#getLimitSuggest()
	 */
	@Override
	public Integer getLimitSuggest() {
		return null;
	}

	/**
	 * Conforme Issue #17206, caso o usuário selecione "gravar" e falte campos
	 * obrigatórios do formulário, o campo cep não recupera o valor
	 * DefaultValue selecionado no suggest, retornando ao valor anterior. O método verifica se o valor
	 * padrão do input do componente está diferente do defaultValue.
	 */
	public void altVlLocalValueComponente() {
		UIComponent component = ComponentUtil.getUIComponent("cadastroPessoaAdvogadoForm:endereco:enderecoCepDecorate:enderecoCep");
		EditableValueHolder evh = (EditableValueHolder) component;
		if(evh != null && evh.getLocalValue()!=null){
			ComponentUtil.clearChildren(component);
		}
	}
	
	@Override
	public List<Cep> suggestList(Object typed){
		CepService service = (CepService)Component.getInstance(CepService.class);
		String numero = (String) typed;
		List<Cep> ceps = service.findByNumero(numero);
		return ceps;
	}
}
