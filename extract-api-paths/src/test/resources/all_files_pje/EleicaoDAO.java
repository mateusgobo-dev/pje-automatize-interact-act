package br.jus.je.pje.persistence.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.pje.je.entidades.Eleicao;

@Name(EleicaoDAO.NAME)
@AutoCreate
public class EleicaoDAO extends BaseDAO<Eleicao> implements Serializable {

	/**
         *
         */
	private static final long	serialVersionUID	= 1L;

	public static final String	NAME				= "eleicaoDAO";

	@Override
	public Object getId(Eleicao e) {
		return e.getCodObjeto();
	}

	public Eleicao findPorAnoTipo(Integer ano, Integer codigoTipo) {
		Query query = getEntityManager().createQuery("select e from Eleicao e  where e.ano = :ano and e.tipoEleicao.codObjeto = :codigoTipoEleicao");
		query.setParameter("ano", ano);
		query.setParameter("codigoTipoEleicao", codigoTipo);

		try {
			return (Eleicao) query.getSingleResult();
		} catch (NoResultException exception) {
			return null;
		}
	}
	
	public List<Eleicao> findEleicoes(){
		return this.findEleicoes(null);
	}
	
	public List<Eleicao> findEleicoes(Boolean ativo){
		StringBuilder jpql = new StringBuilder();
        
		jpql.append(" select eleicao from Eleicao eleicao ");
		if(ativo != null){
			jpql.append(" where eleicao.ativo = :ativo ");			
		}
        jpql.append(" order by eleicao.ano desc ");
        
        TypedQuery<Eleicao> query = getEntityManager().createQuery(jpql.toString(), Eleicao.class);
        if(ativo != null){
        	query.setParameter("ativo", ativo);
        }
        
		return query.getResultList();
	}

}
