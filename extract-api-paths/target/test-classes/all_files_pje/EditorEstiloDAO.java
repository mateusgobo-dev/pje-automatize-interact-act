package br.jus.cnj.pje.business.dao;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.editor.EditorEstilo;

@Name("editorEstiloDAO")
public class EditorEstiloDAO extends BaseDAO<EditorEstilo> {

	@Override
	public Object getId(EditorEstilo e) {
		return e.getId();
	}

}
