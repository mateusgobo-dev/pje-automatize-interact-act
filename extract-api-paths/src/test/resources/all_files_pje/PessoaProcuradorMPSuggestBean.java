package br.com.infox.cliente.component.suggest;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.PessoaProcurador;

@Name("pessoaProcuradorMPSuggest")
@BypassInterceptors
public class PessoaProcuradorMPSuggestBean extends AbstractSuggestBean<PessoaProcurador> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct p from PessoaProcuradoria o left join o.pessoa p where ");
		sb.append("o.procuradoria.acompanhaSessao = true ");
		sb.append("and o.pessoa.ativo = true ");
		sb.append("and lower(TO_ASCII(p.nome)) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER + "), '%'))");
		sb.append(" order by p.nome asc");
		return sb.toString();
	}
	
	@Override
	public List<PessoaProcurador> suggestList(Object typed){
		List<PessoaProcurador> listaTemp = super.suggestList(typed);
		
		List<PessoaProcurador> listaRetorno;
		
		if(listaTemp.size() > 0) {
			listaRetorno = new ArrayList<PessoaProcurador>(listaTemp.size());
			for (PessoaProcurador pessoaProcurador : listaTemp) {
				if(pessoaProcurador.getProcuradorAtivo()) {
					listaRetorno.add(pessoaProcurador);
				}
			}
			return listaRetorno;
		}else {
			return listaTemp;
		}
	}
}