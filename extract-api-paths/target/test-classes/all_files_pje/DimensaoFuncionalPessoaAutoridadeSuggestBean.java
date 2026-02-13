package br.com.infox.cliente.component.suggest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.PessoaAutoridade;

/**
 * Suggests para autoridades não vinculadas a Dimensões Funcionais
 * 
 * @author RodrigoAR
 * 
 */
@Name(DimensaoFuncionalPessoaAutoridadeSuggestBean.NAME)
@BypassInterceptors
@Scope(ScopeType.SESSION)
public class DimensaoFuncionalPessoaAutoridadeSuggestBean extends AbstractSuggestBean<PessoaAutoridade> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 81002762115026482L;
	public static final String NAME = "dimensaoFuncionalPessoaAutoridadeSuggest";
	
	private static final String STD_QUERY = "SELECT pa FROM PessoaAutoridade AS pa " +
			"WHERE NOT EXISTS " +
			"	(SELECT autaf.autoridade " +
			"		FROM DimensaoFuncional AS df " +
			"		JOIN df.autoridadesAfetadas AS autaf " +
			"		WHERE autaf.autoridade = pa and df.ativo = true) " +
			"AND LOWER(TO_ASCII(pa.nome)) LIKE LOWER(CONCAT('%',TO_ASCII(:" + INPUT_PARAMETER + "), '%')) ORDER BY pa.nome";

	@Override
	public String getEjbql() {
//		StringBuilder sb = new StringBuilder();
//		sb.append("select o from PessoaAutoridade o where ");
//		sb.append("not exists (select a from DimensaoFuncional d join d.autoridadesList a where a.idUsuario = o.idUsuario) ");
//		sb.append("and lower(TO_ASCII(o.nome)) like lower(concat('%',TO_ASCII(:");
//		sb.append(INPUT_PARAMETER);
//		sb.append("), '%')) order by o.nome");
		return STD_QUERY;
	}

}
