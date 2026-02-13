package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.Caixa;

@Name(CaixaDAO.NAME)
public class CaixaDAO extends BaseDAO<Caixa>{

	public static final String NAME = "caixaDAO";
	
	@Override
	public Object getId(Caixa e) {
		return e.getIdCaixa();
	}
	
	@SuppressWarnings("unchecked")
	public List<Caixa> getCaixasByNomeTarefa(String nomeTarefa, Integer idLocalizacaoFisica){
		StringBuilder hql = new StringBuilder();
		hql.append("select o from Caixa o where o.tarefa.tarefa = :nomeTarefa ");
		if(idLocalizacaoFisica != null) {
			hql.append("and o.localizacao.idLocalizacao = :idLocalizacaoFisica ");
		}
		
		Query q = entityManager.createQuery(hql.toString());
		q.setParameter("nomeTarefa", nomeTarefa);
		
		if(idLocalizacaoFisica != null){
			q.setParameter("idLocalizacaoFisica", idLocalizacaoFisica);
		}
		
		List<Caixa> ret = q.getResultList();
		return ret;
	}

}
