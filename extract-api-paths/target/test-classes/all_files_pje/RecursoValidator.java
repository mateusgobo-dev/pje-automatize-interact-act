package br.com.infox.validator;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

@org.jboss.seam.annotations.faces.Validator(id = "recursoValidator")
@Name("recursoValidator")
@BypassInterceptors
/**
 * verifica se o string passado é iniciada por "/".
 * 
 * 
 *
 */
public class RecursoValidator extends AbstractIdentificadorValidator {

	@Override
	public String tipoIdentificador() {
		return "recurso";
	}

	@Override
	public boolean ehInvalido(String identificador) {
		 
		return (!identificador.startsWith("/"));
	}
	
}