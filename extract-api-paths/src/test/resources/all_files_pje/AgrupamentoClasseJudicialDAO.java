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

import java.util.Arrays;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.AgrupamentoClasseJudicial;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;

/**
 * Componente de acesso a dados da entidade {@link AgrupamentoClasseJudicial}.
 * 
 * @author cristof
 *
 */
@Name("agrupamentoClasseJudicialDAO")
public class AgrupamentoClasseJudicialDAO extends BaseDAO<AgrupamentoClasseJudicial> {
	
	@Override
	public Integer getId(AgrupamentoClasseJudicial e) {
		return e.getIdAgrupamento();
	}
	
	/**
	 * Recupera um agrupamento de classes pelo seu código identificador.
	 * 
	 * @param codigoAgrupamento o código do agrupamento a ser recuperado
	 * @return o agrupamento com o código específico
	 */
	public AgrupamentoClasseJudicial findByCodigo(String codigoAgrupamento){
		String query = "SELECT a FROM AgrupamentoClasseJudicial AS a WHERE a.codAgrupamento = :codigoAgrupamento";
		Query q = entityManager.createQuery(query);
		q.setParameter("codigoAgrupamento", codigoAgrupamento);
		try{
			return (AgrupamentoClasseJudicial) q.getSingleResult();
		}catch (NoResultException e){
			return null;
		}
	}
	
	/**
	 * Indica se uma determinada classe faz parte do(s) agrupamento(s) indicado(s).
	 * 
	 * @param classe a classe a ser avaliada
	 * @param codigosAgrupamentos os códigos identificadores dos agrupamentos
	 * @return true, se a classe fizer parte de pelo menos um dos agrupamentos indicados
	 */
	public boolean pertence(ClasseJudicial classe, String...codigosAgrupamentos){
		String query = "SELECT COUNT(ca.classe.idClasseJudicial) FROM ClasseJudicialAgrupamento AS ca WHERE ca.agrupamento.codAgrupamento IN (:codigosAgrupamentos) AND ca.classe = :classe";
		Query q = entityManager.createQuery(query);
		q.setParameter("codigosAgrupamentos", Arrays.asList(codigosAgrupamentos));
		q.setParameter("classe", classe);
		Number ret = (Number) q.getSingleResult();
		return ret.intValue() > 0;
	}
	
	/**
	 * Indica se um determinado assunto faz parte do(s) agrupamento(s) indicado(s).
	 * 
	 * @param assunto o assunto a ser avaliado
	 * @param codigosAgrupamentos os códigos identificadores dos agrupamentos
	 * @return true, se o assunto fizer parte de pelo menos um dos agrupamentos indicados
	 */
	public boolean pertence(AssuntoTrf assunto, String...codigosAgrupamentos){
		String query = "SELECT COUNT(aa.assunto.idAssuntoTrf) FROM AssuntoAgrupamento AS aa WHERE aa.agrupamento.codAgrupamento IN (:codigosAgrupamentos) AND aa.assunto = :assunto";
		Query q = entityManager.createQuery(query);
		q.setParameter("codigosAgrupamentos", Arrays.asList(codigosAgrupamentos));
		q.setParameter("assunto", assunto);
		Number ret = (Number) q.getSingleResult();
		return ret.intValue() > 0;
	}

}