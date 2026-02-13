package br.com.infox.cliente.component.suggest;

import java.util.List;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.PessoaAutoridade;

@Name(PessoaAutoridadeSuggestBean.NAME)
@BypassInterceptors
public class PessoaAutoridadeSuggestBean extends AbstractSuggestBean<PessoaAutoridade> {

	public static final String NAME = "pessoaAutoridadeSuggest";
	private static final long serialVersionUID = 1L;
	
	private Boolean nenhumResultadoEncontrado;

	private boolean pesquisarApenasComOrgaoVinculacao;	
	
	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from PessoaAutoridade o where ");
		sb.append("o.ativo = true and ");
		
		if (pesquisarApenasComOrgaoVinculacao) {
			sb.append("o.orgaoVinculacao is not null and ");
		}
		
		sb.append("lower(TO_ASCII(o.nome)) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) order by o.nome");
		return sb.toString();
	}
	
	@Override
	public List<PessoaAutoridade> suggestList(Object typed) {
		List<PessoaAutoridade> lista = super.suggestList(typed); 

		if(lista == null || lista.isEmpty()){
			nenhumResultadoEncontrado = Boolean.TRUE;
		} else {
			nenhumResultadoEncontrado = Boolean.FALSE;
		}
				
		return lista;
	}
	
	public Boolean getNenhumResultadoEncontrado() {
		return nenhumResultadoEncontrado;
	}
	
	public void setNenhumResultadoEncontrado(Boolean nenhumResultadoEncontrado) {
		this.nenhumResultadoEncontrado = nenhumResultadoEncontrado;
	}

	public void setPesquisarApenasComOrgaoVinculacao(boolean pesquisarApenasComOrgaoVinculacao) {
		this.pesquisarApenasComOrgaoVinculacao = pesquisarApenasComOrgaoVinculacao;
	}
}
