package br.com.infox.editor.manager;

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.manager.GenericManager;
import br.com.infox.editor.dao.PreferenciaDao;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.editor.Preferencia;
import br.jus.pje.nucleo.enums.editor.PreferenciaEditorEnum;


@Name(PreferenciaManager.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class PreferenciaManager extends GenericManager implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "preferenciaManager";
	
	@In
	private PreferenciaDao preferenciaDao;
	
	public Preferencia getPreferenciaPorUsuario(Usuario usuario, PreferenciaEditorEnum preferencia) {
		return preferenciaDao.getPreferenciaPorUsuario(usuario, preferencia);
	}

}
