package br.com.infox.cliente.component.suggest;

import java.util.Collections;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.Util;
import br.com.infox.component.suggest.AbstractSuggestBean;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name("processoOriginarioSuggest")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class ProcessoOriginarioSuggestBean extends AbstractSuggestBean<ProcessoTrf> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		return null;
	}

	
	/*
	 * [PJEII-2421] PJE-JT: Cristiano Nascimento : PJE-1.4.5
	 * Sobrescrevi o método suggestList para validar somente os caracteres do suggestion pesquisar processo. 
	 * Caso o usuário digite algum caracter que o sistema não consiga converter para ISO8859, o método não monta a consulta, 
	 * retornando vazia a Lista de Processos. 
	 */
	
	@Override
	@SuppressWarnings("unchecked")
	public List<ProcessoTrf> suggestList(Object typed){
		List<ProcessoTrf> result = null;
		
		//validação dos caracteres unicode
		if (Util.isStringSemCaracterUnicode(typed.toString())){
			ProcessoJudicialManager processoJudicialManager = ComponentUtil.getComponent("processoJudicialManager");
			result = processoJudicialManager.recuperarProcessosUsuariosPorNumeroProcesso(typed.toString());
		}
		else{
			result = Collections.emptyList();
		}
		return result;
	}

}
