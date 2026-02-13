/**
 *  pje
 *  Copyright (C) 2013 Conselho Nacional de Justiça
 *
 *  A propriedade intelectual deste programa, tanto quanto a seu código-fonte
 *  quanto a derivação compilada é propriedade da União Federal, dependendo
 *  o uso parcial ou total de autorização expressa do Conselho Nacional de Justiça.
 * 
 */
package br.jus.cnj.pje.business.dao;

import br.com.itx.util.EntityUtil;
import java.util.Arrays;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.MovimentoDTO;
import br.jus.pje.nucleo.entidades.Evento;

/**
 * Componente de acesso a dados da entidade {@link Evento}.
 * 
 * @author cristof
 *
 */
@Name("eventoDAO")
public class EventoDAO  extends BaseDAO<Evento>{

	@Override
	public Object getId(Evento e) {
		return e.getIdEvento();
	}
	
	/**
	 * Recupera a lista de tipos de movimentação que têm os códigos identificadores dados.
	 * 
	 * @param codigo os códigos identificadores (nacionais ou locais) dos tipos de movimentação 
	 * @return os tipos de movimentação
	 */
	@SuppressWarnings("unchecked")
	public List<Evento> findByIds(String... codigo){
		String select = "SELECT o FROM Evento o WHERE o.codEvento IN (:codigo)";
		Query query = EntityUtil.createQuery(getEntityManager(), select, true, true, "EventoDAO.findByIds")
			.setParameter("codigo", Arrays.asList(codigo));
		return query.getResultList();
	}

	/**
	 * Recupera o tipo de movimentação ativo que tem o código identificador dado.
	 * 
	 * @param codigo o código identificador
	 * @return o tipo de movimentação
	 * @throws PJeDAOException caso não exista tipo de movimentação ativa com o código dado ou quando
	 * houver mais de um tipo ativo com o mesmo código
	 */
	public Evento findByCodigoCNJ(String codigoNacional) throws PJeDAOException{
		String sql = "SELECT e FROM Evento e WHERE e.codEvento = :codigo AND e.ativo = true";
		Query q = EntityUtil.createQuery(getEntityManager(), sql, true, true, "EventoDAO.findByCodigoCNJ")
			.setParameter("codigo", codigoNacional);
		try{
			return (Evento) q.getSingleResult();
		} catch (NoResultException e){
			throw new PJeDAOException("Tipo de movimentação processual com código nacional [" + codigoNacional
				+ "] não encontrado.", e);
		} catch (NonUniqueResultException e){
			throw new PJeDAOException("Foi encontrada mais de uma movimentação processual com código nacional "
				+ codigoNacional, e);
		} catch (Exception e){
			throw new PJeDAOException("Erro ao procurar a movimentação processual com código nacional "
				+ codigoNacional + " não encontrado.", e);
		}
	}
	
	public Evento recuperar(String codEvento) {
		Query query = EntityUtil.createQuery(entityManager, "SELECT o FROM Evento o WHERE o.codEvento = :codEvento AND o.ativo = true", 
				true, true, "EventoDAO.recuperar");
		
		return EntityUtil.getSingleResult(query.setParameter("codEvento", codEvento));
	}
	
	/**
	 * Metodo responsavel por buscar os eventos superiores.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Evento> getEventosSuperiores(Boolean eventoSuperiorAtivo){
		
		StringBuilder sql = new StringBuilder();
		
		sql.append("SELECT distinct eSuperior ");
		sql.append("FROM Evento e ");
		sql.append("INNER JOIN e.eventoSuperior as eSuperior ");
		
		if (eventoSuperiorAtivo != null) {
			sql.append("WHERE ");
			sql.append("eSuperior.ativo = :ativo ");
		}
		
		sql.append("order by eSuperior.evento");
		
		Query q = entityManager.createQuery(sql.toString());
		if (eventoSuperiorAtivo != null) {
			q.setParameter("ativo", eventoSuperiorAtivo);
		}
		
		return q.getResultList();
	}
	
	/**
	 * Metodo responsavel por buscar os eventos ativos.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Evento> getEventosAtivos(){
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT e FROM Evento e WHERE e.ativo = true order by e.evento ");
		
		Query q = entityManager.createQuery(sql.toString());
		return q.getResultList();
	}
	
	public String findComplementoByIdEvento(Integer idEvento) {
		StringBuilder sql = new StringBuilder();
		sql.append("select ds_movimento from tb_evento where id_evento = :idEvento");
		Query query = entityManager.createNativeQuery(sql.toString());
		query.setParameter("idEvento", idEvento);
		return query.getSingleResult().toString();
	}

	@SuppressWarnings("unchecked")
	public List<MovimentoDTO> getEventosAtivosDTO() {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT new br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.MovimentoDTO(e.idEvento, e.caminhoCompleto) ");
		sql.append("FROM Evento e WHERE e.ativo = true order by e.caminhoCompleto ");
		
		Query q = entityManager.createQuery(sql.toString());
		return q.getResultList();
	}

}
