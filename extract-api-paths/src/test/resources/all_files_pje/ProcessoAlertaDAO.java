/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.ProcessoAlerta;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;

/**
 * Componente de acesso a dados da entidade {@link ProcessoAlerta}.
 * 
 * @author flavioreis
 *
 */
@Name("processoAlertaDAO")
public class ProcessoAlertaDAO extends BaseDAO<ProcessoAlerta> {
	
	@Override
	public Integer getId(ProcessoAlerta e) {
		return e.getIdProcessoAlerta();
	}

	/**
	 * Indica se um determinado processo judicial tem pelo menos um
	 * alerta, de qualquer criticidade, ativo.
	 * 
	 * @param processoJudicial o processo a respeito do qual se pretende
	 * recuperar a informação
	 * @return true, se houver pelo menos um alerta ativo no processo
	 */
	public boolean possuiAlertasAtivos(ProcessoTrf processoJudicial) {
		String query = "SELECT COUNT(pa.idProcessoAlerta) " +
				"FROM ProcessoAlerta AS pa " +
				"	WHERE pa.processoTrf = :processo " +
				"		AND pa.ativo = true " +
				"		AND pa.alerta.ativo = true";
		
		OrgaoJulgador orgaoJulgador = Authenticator.getOrgaoJulgadorAtual();		
		OrgaoJulgadorColegiado orgaoJulgadorColegiado = Authenticator.getOrgaoJulgadorColegiadoAtual();
		
		if (orgaoJulgador != null){
			query += "	AND pa.alerta.orgaoJulgador.idOrgaoJulgador = :idOrgaoJulgador ";
		}
		
		if (orgaoJulgadorColegiado != null){
			query += " AND pa.alerta.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado = :idOrgaoJulgadorColegiado ";
		}
		
		Query q = entityManager.createQuery(query);
		q.setParameter("processo", processoJudicial);
		
		if (orgaoJulgador != null){
			q.setParameter("idOrgaoJulgador", orgaoJulgador.getIdOrgaoJulgador());
		}
		if (orgaoJulgadorColegiado != null){
			q.setParameter("idOrgaoJulgadorColegiado", orgaoJulgadorColegiado.getIdOrgaoJulgadorColegiado());
		}
		q.setMaxResults(1);
		Number cont = (Number) q.getSingleResult();
		return cont.intValue() > 0;
	}

	/**
	 * Retorna os ProcessoAlerta's ativos vinculados a um alerta.
	 * @link http://www.cnj.jus.br/jira/browse/PJEII-18551
	 * @param idAlerta - id do alerta
	 * @return Lista de ProcessoAlerta de um alerta
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoAlerta> consultaProcessosAlertasAtivos(Integer idAlerta) {
		StringBuilder jpql = new StringBuilder();
		jpql.append("select o ");
		jpql.append("  from ProcessoAlerta o ");
		jpql.append(" where o.alerta.idAlerta = :idAlerta ");
		jpql.append("   and o.ativo = :ativo ");
		jpql.append("   and o.alerta.ativo = :ativo ");
		Query query = entityManager.createQuery(jpql.toString());
		query.setParameter("idAlerta", idAlerta);
		query.setParameter("ativo", true);
		return query.getResultList();
	}

	public boolean existemProcessosAlertasAtivos(Integer idAlerta) {
		StringBuilder jpql = new StringBuilder();
		jpql.append("select count(o) ");
		jpql.append(" from ProcessoAlerta o ");
		jpql.append(" where o.alerta.idAlerta = :idAlerta ");
		jpql.append(" and o.ativo = :ativo ");
		jpql.append(" and o.alerta.ativo = :ativo ");
		Query query = entityManager.createQuery(jpql.toString());
		query.setParameter("idAlerta", idAlerta);
		query.setParameter("ativo", true);
		Long resultado = (Long) query.getSingleResult();
		boolean existemProcessosAlertasAtivos = resultado > 0;
		return existemProcessosAlertasAtivos;
	}
	
	@SuppressWarnings("unchecked")
	public List<ProcessoAlerta> getAlertasProcesso(Integer idProcesso){
		
		StringBuilder query = new StringBuilder("from ProcessoAlerta pa where pa.processoTrf.idProcessoTrf = :idProcesso and pa.ativo = :ativo and pa.alerta.ativo = :ativo");
		
		OrgaoJulgador oj = Authenticator.getOrgaoJulgadorAtual();		
		OrgaoJulgadorColegiado ojc = Authenticator.getOrgaoJulgadorColegiadoAtual();
		
		if(oj != null){
			query.append("	AND pa.alerta.orgaoJulgador.idOrgaoJulgador = :idOrgaoJulgador ");
		}
		if(ojc != null){
			query.append(" AND pa.alerta.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado = :idOrgaoJulgadorColegiado ");
		}

		Query q = getEntityManager().createQuery(query.toString());

		q.setParameter("idProcesso", idProcesso);
		q.setParameter("ativo", true);
		
		if(oj != null){
			q.setParameter("idOrgaoJulgador", oj.getIdOrgaoJulgador());
		}
		if(ojc != null){
			q.setParameter("idOrgaoJulgadorColegiado", ojc.getIdOrgaoJulgadorColegiado());
		}
		

		List<ProcessoAlerta> processoAlertaList = q.getResultList();
		
		if(processoAlertaList.size() > 0){
		
			Collections.sort(processoAlertaList, new Comparator<ProcessoAlerta>(){
	
				@Override
				public int compare(ProcessoAlerta o1, ProcessoAlerta o2){
	
					// na ordem inversa
					/*
					 * Mudança de comparação realizada para ordenar por data desc by thiago.vieira
					 */
					if (o1.getAlerta().getDataAlerta().after(o2.getAlerta().getDataAlerta())){
						return -1;
					}
					else if (o1.getAlerta().getDataAlerta().before(o2.getAlerta().getDataAlerta())){
						return 1;
					}
					else{
						return 0;
					}
				}
			});
		}
		return processoAlertaList;
	}
	
	public void inativarAlertaProcesso(Integer idProcessoAlerta) {
		String hql = "update ProcessoAlerta set ativo = :ativo where idProcessoAlerta = :idProcessoAlerta";
		Query q = getEntityManager().createQuery(hql);
		q.setParameter("ativo", false);
		q.setParameter("idProcessoAlerta", idProcessoAlerta);
		q.executeUpdate();
	}

	public void inativarTodosAlertasProcesso(Integer idProcesso) {
		
		List<ProcessoAlerta> processoAlertaList = this.getAlertasProcesso(idProcesso);
		
		if (processoAlertaList.size() > 0){
			for (ProcessoAlerta pa : processoAlertaList){
				pa.setAtivo(false);
			}
			flush();
		}
	}
}