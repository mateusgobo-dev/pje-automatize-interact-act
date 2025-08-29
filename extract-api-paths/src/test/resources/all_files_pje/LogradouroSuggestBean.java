/*
 IBPM - Ferramenta de produtividade Java
 Copyright (c) 1986-2009 Infox Tecnologia da Informação Ltda.

 Este programa é software livre; você pode redistribuí-lo e/ou modificá-lo 
 sob os termos da GNU GENERAL PUBLIC LICENSE (GPL) conforme publicada pela 
 Free Software Foundation; versão 2 da Licença.
 Este programa é distribuído na expectativa de que seja útil, porém, SEM 
 NENHUMA GARANTIA; nem mesmo a garantia implícita de COMERCIABILIDADE OU 
 ADEQUAÇÃO A UMA FINALIDADE ESPECÍFICA.

 Consulte a GNU GPL para mais detalhes.
 Você deve ter recebido uma cópia da GNU GPL junto com este programa; se não, 
 veja em http://www.gnu.org/licenses/   
 */
package br.com.infox.ibpm.component.suggest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.Cep;

@Name("logradouroSuggest")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class LogradouroSuggestBean extends AbstractSuggestBean<Cep> {

	private static final long serialVersionUID = 1L;

	private String defaultValue;

	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from Cep o ");
		sb.append("where lower(to_ascii(o.nomeLogradouro)) like lower(concat('%', TO_ASCII(:" + INPUT_PARAMETER
				+ "), '%')) ");
		sb.append("order by 1");
		return sb.toString();
	}

	@Override
	protected String getEventSelected() {
		return "cepChangedEvent";
	}

	@Override
	public String getDefaultValue() {
		if (defaultValue == null) {
			if (getInstance() != null && getInstance().getNomeLogradouro() != null) {
				return getInstance().getNomeLogradouro();
			} else {
				return "";
			}
		} else {
			return defaultValue;
		}
	}

	@Override
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

}
