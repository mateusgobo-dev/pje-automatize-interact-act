/**
 * UsuarioLoginDAO.java
 * 
 * Data: 01/02/2016
 */
package br.jus.cnj.pje.business.dao;

import java.io.Serializable;

import javax.persistence.Query;
import java.util.List;
import org.jboss.seam.annotations.Name;
import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;

/**
 * Classe responsável pela consulta e/ou persistência de dados da entidade UsuarioLogin.
 * 
 * @author Adriano Pamplona.
 */
@Name("usuarioLoginDAO")
public class UsuarioLoginDAO extends BaseDAO<UsuarioLogin> implements Serializable{

	@Override
	public Object getId(UsuarioLogin e) {
		return e.getIdUsuario();
	}

	/**
	 * Retorna o UsuarioLogin do 'login' passado por parâmetro.
	 * 
	 * @param login
	 * @return UsuarioLogin
	 */
	public UsuarioLogin findByLogin(String login) {
		StringBuilder hql = new StringBuilder();
		hql.append("from UsuarioLogin where login = :login");
		
		Query query = getEntityManager().createQuery(hql.toString());
		query.setParameter("login", login);
		
		return getSingleResult(query);
	}
	
	@SuppressWarnings("unchecked")
	public List<UsuarioLogin> findByName(String nomeInserido) {
		StringBuilder sb = new StringBuilder(0);
		sb.append(" SELECT o ");
		sb.append(" FROM UsuarioLogin o");
		sb.append(" WHERE o.ativo = 't' and LOWER(o.nome) like (LOWER(:nome))");
		Query query = entityManager.createQuery(sb.toString());
		query.setParameter("nome", nomeInserido+"%");
		return query.getResultList();
	}

	public UsuarioLogin getReference(Integer idUsuario) {
		return entityManager.getReference(UsuarioLogin.class, idUsuario);
	}
}