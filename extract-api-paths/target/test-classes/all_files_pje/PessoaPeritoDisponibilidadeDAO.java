package br.com.infox.pje.dao;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.pje.nucleo.entidades.Especialidade;
import br.jus.pje.nucleo.entidades.PessoaPerito;
import br.jus.pje.nucleo.entidades.PessoaPeritoDisponibilidade;
import br.jus.pje.nucleo.enums.SemanaEnum;

@Name("pessoaPeritoDisponibilidadeDAO")
@AutoCreate
public class PessoaPeritoDisponibilidadeDAO extends BaseDAO<PessoaPeritoDisponibilidade> {

	@Override
	public Object getId(PessoaPeritoDisponibilidade e) {
		return e.getIdPessoaPeritoDisponibilidade();
	}
	
	@SuppressWarnings("unchecked")
	public List<PessoaPeritoDisponibilidade> recuperarAtivos(Especialidade especialidade, PessoaPerito perito, SemanaEnum semana) {
		StringBuilder jpql = new StringBuilder("SELECT ppd FROM PessoaPeritoDisponibilidade ppd ")
			.append("WHERE ppd.pessoaPeritoEspecialidade.especialidade.idEspecialidade = :idEspecialidade ")
			.append("AND ppd.pessoaPeritoEspecialidade.pessoaPerito.idUsuario = :idPerito ")
			.append("AND ppd.ativo = true ");
		
		if (semana != null) {
			jpql.append("AND ppd.diaSemana = :diaSemana");
		}
		
		Query query = getEntityManager().createQuery(jpql.toString())
			.setParameter("idEspecialidade", especialidade.getIdEspecialidade())
			.setParameter("idPerito", perito.getIdUsuario());
		
		if (semana != null) {
			query.setParameter("diaSemana", semana);
		}
		
		return query.getResultList();
	}

}
