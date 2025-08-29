/**
 * 
 */
package br.jus.cnj.pje.util;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import br.jus.cnj.pje.extensao.servico.ParametroService;
import br.jus.cnj.pje.nucleo.Parametros;

/**
 * @author antonio.martins
 *
 */
@Name("parametrosFactory")
public class ParametrosFactory {
	@In(create=true)
	ParametroService parametroService;
	
	@Factory(autoCreate=true, scope=ScopeType.APPLICATION)
	public String getSegmentoJustica() {
		String ret = parametroService.valueOf(Parametros.TIPOJUSTICA);
		if(ret == null) {
			ret = "JC";
		}
		return ret;
	}
}
