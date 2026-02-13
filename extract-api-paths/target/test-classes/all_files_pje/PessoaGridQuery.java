package br.com.itx.component.grid;

import org.jboss.seam.core.Expressions.ValueExpression;

public class PessoaGridQuery extends GridQuery {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	@Override
	protected boolean verificarFiltroPreenchido() {
		boolean resultado = false;
		parseEjbql();
		for (int i=0; i < getRestrictions().size(); i++){
			Object parameter = ((ValueExpression<?>)getRestrictionParameters().get(i)).getExpressionString();
			Object parameterValue = ((ValueExpression<?>)getRestrictionParameters().get(i)).getValue();
			if(!parameter.equals("#{pessoaFisicaSearch.selDocumentoAtivoInativo}") &&
					!parameter.equals("#{pessoaFisicaSearch.ativo}") &&
					!parameter.equals("#{pessoaJuridicaSearch.selDocumentoAtivoInativo}") &&		
					!parameter.equals("#{pessoaJuridicaSearch.ativo}") &&
					!parameter.equals("#{pessoaAutoridadeSearch.ativo}")){
				
				if (isRestrictionParameterSet(parameterValue)) {
					resultado = true;
					break;
				}
			}
		}
		return resultado;
	}
}
