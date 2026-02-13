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

@Name("cepSuggestLocalizacao")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class CepSuggestLocalizacaoBean extends AbstractSuggestBean<Cep> {

	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see br.com.infox.component.suggest.SuggestBean#getEjbql()
	 */
	@Override
	public String getEjbql() {
		String q = "SELECT o FROM Cep AS o WHERE o.numeroCep = :" + INPUT_PARAMETER + " AND o.ativo = true"; 
		return q;
	}

	/* (non-Javadoc)
	 * @see br.com.infox.component.suggest.AbstractSuggestBean#getEventSelected()
	 */
	@Override
	protected String getEventSelected() {
		return "cepLocalizacaoChangedEvent";
	}
	
	@Override
	public Integer getLimitSuggest() {
		return 1;
	}
}