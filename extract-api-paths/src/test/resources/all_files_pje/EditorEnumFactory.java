package br.com.infox.editor.type;

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.nucleo.enums.TipoNumeracaoEnum;
import br.jus.pje.nucleo.enums.editor.OrigemAutoTextoEnum;

@Name(EditorEnumFactory.NAME)
@Scope(ScopeType.APPLICATION)
public class EditorEnumFactory implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "editorEnumFactory";
	
	@Factory(scope = ScopeType.APPLICATION)
	public OrigemAutoTextoEnum[] getOrigemAutoTextoEnumValues() {
		return OrigemAutoTextoEnum.values();
	}
	
	@Factory(scope = ScopeType.APPLICATION)
	public TipoNumeracaoEnum[] getTipoNumeracaoEnumValues() {
		return TipoNumeracaoEnum.values();
	}
}
