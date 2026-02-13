package br.jus.cnj.pje.business.dao;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.lancadormovimento.TipoComplemento;

/**
 * Componente de acesso a dados da entidade {@link TipoComplemento}.
 * 
 * @author cristof
 *
 */
@Name("tipoComplementoDAO")
public class TipoComplementoDAO extends BaseDAO<TipoComplemento> {
	
	@Override
	public Long getId(TipoComplemento e) {
		return e.getIdTipoComplemento();
	}

	/**
	 * Recupera o tipo de complemento ativo que tem por código o dado.
	 * 
	 * @param codigo o código a ser pesquisado
	 * @return o tipo de complemento que tem o código dado, ou null se ele inexistir.
	 */
	public TipoComplemento findByCodigo(String codigo) {
		String query = "SELECT tc FROM TipoComplemento AS tc WHERE tc.ativo = true AND tc.codigo = :codigo";
		Query q = entityManager.createQuery(query);
		q.setParameter("codigo", codigo);
		try{
			return (TipoComplemento) q.getSingleResult();
		}catch (NoResultException e){
			return null;
		}
	}
	
	/**
	 * Método responsável por recuperar um tipo de complemento pelo nome.
	 * @param nome Nome do tipo de complemento.
	 * @return {@link TipoComplemento}.
	 */
	public TipoComplemento recuperarTipoComplemento(String nome) {
		Query query = entityManager.createQuery("FROM TipoComplemento WHERE nome = :nome");
		query.setParameter("nome", nome);
		
		try {
			return (TipoComplemento)query.getSingleResult();
		} catch (NoResultException ex) {
			return null;
		}
	}

}
