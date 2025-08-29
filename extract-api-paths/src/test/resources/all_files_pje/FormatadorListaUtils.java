package br.jus.cnj.pje.util;

import java.util.List;
import org.jboss.seam.annotations.Name;
import br.jus.cnj.pje.util.formatadorLista.FormatadorLista;

@Name("formatadorListaUtils")
public class FormatadorListaUtils {
	
	public FormatadorLista setLista (List<?> lista) {
		return new FormatadorLista().setLista(lista);
	}
	
}
