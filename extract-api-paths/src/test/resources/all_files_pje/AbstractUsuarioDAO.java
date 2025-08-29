package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import br.jus.pje.nucleo.entidades.Usuario;

public abstract class AbstractUsuarioDAO<E extends Usuario> extends BaseDAO<E>{
	
	public static final String PARAMETRO_MODELO_EMAIL_SENHA = "idModeloEMailMudancaSenha" ;
	
	@SuppressWarnings("unchecked")
	public E findByLogin(String login){
		String query = "SELECT u FROM "+getEntityClass().getCanonicalName()+" u WHERE u.login = :login";
		Query q = entityManager.createQuery(query).setParameter("login", login);
		try{
			return (E)q.getSingleResult();
		}catch (NoResultException e) {
			return null;
		}catch(NonUniqueResultException e){
			throw new IllegalStateException(e);
		}
	}
	
	/**
	 * verifica se o login esta disponivel
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Boolean checkLogin(String login, Integer idUsuario){
		String query = "SELECT u FROM Usuario u WHERE u.login = :login and u.idUsuario != :idUsuario";
		Query q = entityManager.createQuery(query);
		q.setParameter("login", login);
		q.setParameter("idUsuario", idUsuario);
		
		List<Usuario> usuarios = q.getResultList();
		return (usuarios == null || usuarios.isEmpty());
	}

	@SuppressWarnings("unchecked")
	public List<E> findByNome(String nome) {
		String query = "SELECT u FROM "+getEntityClass().getCanonicalName()+" AS u WHERE upper(u.nome) like upper(:nome)";
		Query q = entityManager.createQuery(query).setParameter("nome", nome);
		try {
			return (List<E>) q.getResultList();
		} catch (NoResultException e) {
			return null;
		} catch (NonUniqueResultException e) {  
			return null;
		}
	}
}
