package br.com.infox.editor.service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.editor.manager.NumeracaoDocumentoManager;
import br.jus.pje.nucleo.entidades.editor.NumeracaoDocumento;
import br.jus.pje.nucleo.enums.editor.Hierarchical;

@Name(NumeracaoDocumentoService.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class NumeracaoDocumentoService implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "numeracaoDocumentoService";

	@In
	private NumeracaoDocumentoManager numeracaoDocumentoManager;

	private Map<String, String> numeracaoDocumentoMap;
	
	public String getNumeracaoDocumento(Hierarchical hierarchical) {
		if (hierarchical.isNumerado() && hierarchical.getNumeracao() != null) {
			return getNumeracaoDocumento(hierarchical.getNivel(), hierarchical.getNumeracao());
		}
		return "";	
	}

	public String getNumeracaoDocumento(int nivel, int numeracao) {
		String key = createKey(nivel, numeracao);
		if (getNumeracaoDocumentoMap().containsKey(key)) {
			return getNumeracaoDocumentoMap().get(key);
		} else {
			return numeracao + ".";
		}
	}

	private String createKey(int nivel, int numeracao) {
		StringBuilder sb = new StringBuilder();
		sb.append(nivel);
		sb.append(".");
		sb.append(numeracao);
		return sb.toString();
	}

	private Map<String, String> getNumeracaoDocumentoMap() {
		if (numeracaoDocumentoMap == null) {
			numeracaoDocumentoMap = new HashMap<String, String>();
			for (NumeracaoDocumento numeracaoDocumento : numeracaoDocumentoManager.getNumeracaoDocumentoList()) {
				numeracaoDocumentoMap.put(createKey(numeracaoDocumento.getNivel(), numeracaoDocumento.getOrdem()), numeracaoDocumento.getTipo()+numeracaoDocumento.getSeparador());
			}
		}
		return numeracaoDocumentoMap;
	}

}
