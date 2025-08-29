package br.jus.cnj.pje.servicos;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.AgrupamentoClasseJudicial;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.ClasseJudicialAgrupamento;

public class AgrupamentoClasseJudicialService {

	public static AgrupamentoClasseJudicial getAgrupamentoClasseJudicial(String codigoAgrupamento) {
		EntityManager em = EntityUtil.getEntityManager();
		StringBuilder sql = new StringBuilder();
		sql.append("select o from AgrupamentoClasseJudicial o ").append("where o.codAgrupamento = :codigoAgrupamento ");
		Query query = em.createQuery(sql.toString());
		query.setParameter("codigoAgrupamento", codigoAgrupamento);
		query.setMaxResults(1);
		try {
			return (AgrupamentoClasseJudicial) query.getSingleResult();
		} catch (NoResultException no) {
			return null;
		}
	}

	public static List<ClasseJudicial> getClassesJudiciais(List<ClasseJudicialAgrupamento> classeJudicialAgrupamentoList) {
		if (classeJudicialAgrupamentoList == null || classeJudicialAgrupamentoList.isEmpty()) {
			return null;
		}

		List<ClasseJudicial> classeJudicialList = new ArrayList<>(0);
		for (ClasseJudicialAgrupamento classeJudicialAgrupamento : classeJudicialAgrupamentoList) {
			classeJudicialList.add(classeJudicialAgrupamento.getClasse());
		}
		return classeJudicialList;
	}

}
