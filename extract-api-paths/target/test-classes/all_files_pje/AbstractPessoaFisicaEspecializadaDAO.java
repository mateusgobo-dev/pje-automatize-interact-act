package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaFisicaEspecializada;
import br.jus.pje.nucleo.entidades.Usuario;

public abstract class AbstractPessoaFisicaEspecializadaDAO<E extends PessoaFisicaEspecializada>
		extends BaseDAO<E> {
	
	public abstract E especializa(PessoaFisica pessoa);
	public abstract E desespecializa(PessoaFisica pessoa);	

	
	/**
	 * Retorna as pessoas fisicas especializadas portadoras do login informado
	 * @param login
	 * @return E
	 */	
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
	 * verifica se o login foi cadastrado para outra pessoa em Usuario
	 * @param login
	 * @param idPessoaFisica
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Boolean checkLogin(String login, Integer idPessoaFisica){
		String query = "SELECT u FROM Usuario u WHERE u.login = :login"+
	                   " AND u.idUsuario != :idPessoaFisica ";
		Query q = entityManager.createQuery(query);
		q.setParameter("login", login);
		q.setParameter("idPessoaFisica", idPessoaFisica);
		List<Usuario> usuarios = q.getResultList();
		return (usuarios == null || usuarios.isEmpty());
	}
	
	/**
	 * Retorna a pessoa fisica especializada portadora do CPF informado
	 * @param cpf
	 * @return E
	 */
	@SuppressWarnings("unchecked")
	public E findByCPF(String cpf){
		String queryString = "SELECT p FROM "+getEntityClass().getCanonicalName()+" p " 
			+ "	INNER JOIN p.pessoaDocumentoIdentificacaoList AS d"
			+ "		WHERE d.tipoDocumento.codTipo = 'CPF'" 
			+ "			AND d.numeroDocumento = :cpf";
		Query q = this.entityManager.createQuery(queryString);
		q.setParameter("cpf", cpf);
		E pessoa = null;
		try{
			pessoa = (E) q.getSingleResult();
		} catch (NonUniqueResultException e){
			throw new IllegalStateException();
		} catch (NoResultException e){
			return null;
		}
		return pessoa;
	}
	
	/**
	 * verifica se o CPF esta disponivel
	 * @param cpf
	 * @param idPessoaFisica
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Boolean checkCPF(String cpf, Integer idPessoaFisica){
		String queryString = "SELECT p FROM PessoaFisica p " 
				+ "	INNER JOIN p.pessoaDocumentoIdentificacaoList AS d"
				+ "		WHERE d.tipoDocumento.codTipo = 'CPF'" 
				+ "			AND d.numeroDocumento = :cpf";
		
				if(idPessoaFisica != null){
					queryString +=  "         AND p.idUsuario <> :idUsuario ";
				}
				
			Query q = this.entityManager.createQuery(queryString);
			q.setParameter("cpf", cpf);
			q.setParameter("idUsuario", idPessoaFisica);
			
			List<PessoaFisica> result = q.getResultList();
			
			return  (result != null && !result.isEmpty());
	}
	
	@SuppressWarnings("unchecked")
	public E findByNome(String nome) {
		String query = "SELECT u FROM "+getEntityClass().getCanonicalName()+" AS u WHERE upper(u.nome) like upper(:nome)";
		Query q = entityManager.createQuery(query).setParameter("nome", nome);
		try {
			return (E) q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		} catch (NonUniqueResultException e) {  
			return null;  
		}
	}
}