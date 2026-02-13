package br.com.infox.editor.dao;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import br.com.infox.core.dao.GenericDAO;
import br.com.infox.editor.query.AutoTextoQuery;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.editor.AutoTexto;

@Name(AutoTextoDao.NAME)
@AutoCreate
public class AutoTextoDao extends GenericDAO implements AutoTextoQuery {

	public static final String NAME = "autoTextoDao";
	
	@SuppressWarnings("unchecked")
	public List<AutoTexto> getAutoTextoPorDescricaoLocalizacaoList(String descricao, Localizacao localizacao) {
		Query query = entityManager.createQuery(AutoTextoQuery.AUTOTEXTO_POR_DESCRICAO_E_LOCALIZACAO_LIST_QUERY);
		query.setParameter(AutoTextoQuery.DESCRICAO_AUTOTEXTO_PARAM, descricao);
		query.setParameter(AutoTextoQuery.LOCALIZACAO_AUTOTEXTO_PARAM, localizacao);
		return query.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<AutoTexto> getAutoTextoPorDescricaoUsuarioList(String descricao, Usuario usuario) {
		Query query = entityManager.createQuery(AutoTextoQuery.AUTOTEXTO_POR_DESCRICAO_E_USUARIO_LIST_QUERY);
		query.setParameter(AutoTextoQuery.DESCRICAO_AUTOTEXTO_PARAM, descricao);
		query.setParameter(AutoTextoQuery.USUARIO_AUTOTEXTO_PARAM, usuario);
		return query.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<AutoTexto> getAutoTextoPorLocalizacaoList(Localizacao localizacao) {
		Query query = entityManager.createQuery(AutoTextoQuery.AUTOTEXTO_POR_LOCALIZACAO_LIST_QUERY);
		query.setParameter(AutoTextoQuery.LOCALIZACAO_AUTOTEXTO_PARAM, localizacao);
		return query.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<AutoTexto> getAutoTextoPorUsuarioList(Usuario usuario) {
		Query query = entityManager.createQuery(AutoTextoQuery.AUTOTEXTO_POR_USUARIO_LIST_QUERY);
		query.setParameter(AutoTextoQuery.USUARIO_AUTOTEXTO_PARAM, usuario);
		return query.getResultList();
	}
}
