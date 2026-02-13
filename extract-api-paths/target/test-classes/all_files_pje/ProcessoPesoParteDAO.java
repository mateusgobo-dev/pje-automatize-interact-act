package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.ProcessoPesoParte;

@Name(ProcessoPesoParteDAO.NAME)
public class ProcessoPesoParteDAO extends BaseDAO<ProcessoPesoParte>{
	
	public static final String NAME = "processoPesoParteDAO";

	@Override
	public Integer getId(ProcessoPesoParte e){
		return e.getIdProcessoPesoParte();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProcessoPesoParte> buscarPesosPartes() {
		EntityManager em = getEntityManager();
		String query = "select o from ProcessoPesoParte o";
		Query q = em.createQuery(query);
		return q.getResultList();
	}
	
}