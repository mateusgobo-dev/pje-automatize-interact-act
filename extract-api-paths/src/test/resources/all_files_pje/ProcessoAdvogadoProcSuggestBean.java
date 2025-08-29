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
package br.com.infox.cliente.component.suggest;

import java.util.Collections;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.Util;
import br.com.infox.component.suggest.AbstractSuggestBean;
import br.com.infox.pje.list.ProcessoTrfInicialAdvogadoList;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name("processoAdvogadoProcSuggest")
@BypassInterceptors
public class ProcessoAdvogadoProcSuggestBean extends AbstractSuggestBean<ProcessoTrf> {

	private static final long serialVersionUID = 1L;

	/*
	 * [PJEII-2157, PJEII-2158] PJE-JT: Cristiano Nascimento : PJE-1.4.4
	 * Sobrescrevi o método suggestList da classe AbstractSuggestBean para validar somente os caracteres do suggestion box processo. 
	 * Caso o usuário digite algum caracter unicode, o método não monta a consulta, 
	 * retornando vazia a Lista de Processos 
	 */ 
	
	@Override
	@SuppressWarnings("unchecked")
	public List<ProcessoTrf> suggestList(Object typed){
		
		List<ProcessoTrf> result = null;
		String q = getEjbql();
		
		//validação dos caracteres unicodes
		if (q != null && Util.isStringSemCaracterUnicode(typed.toString())){
			Query query = EntityUtil.createQuery(q).setParameter(INPUT_PARAMETER, typed);
			if (getLimitSuggest() != null){
				query.setMaxResults(getLimitSuggest());
			}
			result = query.getResultList();
		}
		else{
			result = Collections.emptyList();
		}

		return result;
	}
	
	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o.processoTrf from ConsultaProcessoTrf o ");
		sb.append("where lower(o.numeroProcesso) like lower(concat ('%', :");
		sb.append(INPUT_PARAMETER);
		sb.append(", '%')) ");
		sb.append("and o.processoTrf.processoStatus = 'D' ");
		ProcessoTrfInicialAdvogadoList processoTrfInicialAdvogadoList = ProcessoTrfInicialAdvogadoList.instance();
		if (processoTrfInicialAdvogadoList.getCaixaPendentes()) {
			sb.append(processoTrfInicialAdvogadoList.getEjbqlFiltroPendentes());
		}
		sb.append("order by o.numeroProcesso");
		return sb.toString();
	}

}