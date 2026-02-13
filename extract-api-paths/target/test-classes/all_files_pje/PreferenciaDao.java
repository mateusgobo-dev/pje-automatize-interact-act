package br.com.infox.editor.dao;

import javax.persistence.Query;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import br.com.infox.core.dao.GenericDAO;
import br.com.infox.editor.query.PreferenciaQuery;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.editor.Preferencia;
import br.jus.pje.nucleo.enums.editor.PreferenciaEditorEnum;

@Name(PreferenciaDao.NAME)
@AutoCreate
public class PreferenciaDao extends GenericDAO implements PreferenciaQuery {
	
	public static final String NAME = "preferenciaDao";

	public Preferencia getPreferenciaPorUsuario(Usuario usuario, PreferenciaEditorEnum preferencia) {
		Query query = entityManager.createQuery(PreferenciaQuery.PREFERENCIA_POR_USUARIO_E_DESCRICAO_QUERY);
		query.setParameter(PreferenciaQuery.USUARIO_PREFERENCIA_PARAM, usuario);
		query.setParameter(PreferenciaQuery.PREFERENCIA_EDITOR_PARAM, preferencia);
		return EntityUtil.getSingleResult(query);
	}
}
