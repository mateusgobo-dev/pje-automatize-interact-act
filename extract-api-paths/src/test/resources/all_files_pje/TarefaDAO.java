package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.Tarefa;


@Name(TarefaDAO.NAME)
public class TarefaDAO extends BaseDAO<Tarefa> {
	
	public static final String NAME = "tarefaDAO";

	@Override
	public Object getId(Tarefa e) {
		return e.getIdTarefa();
	}
	
	
	@SuppressWarnings("unchecked")
	public List<Tarefa> findByName(String name){
		Query q = getEntityManager().createQuery("select o from Tarefa o where o.fluxo.ativo = true and o.tarefa = :name");
		q.setParameter("name", name);

		return q.getResultList();
	}
}

