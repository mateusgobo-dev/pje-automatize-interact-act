package br.com.infox.pje.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.pje.nucleo.entidades.Especialidade;
import br.jus.pje.nucleo.entidades.PessoaPerito;
import br.jus.pje.nucleo.entidades.PessoaPeritoIndisponibilidade;

@Name("pessoaPeritoIndisponibilidadeDAO")
@AutoCreate
public class PessoaPeritoIndisponibilidadeDAO extends BaseDAO<PessoaPeritoIndisponibilidade> {

	@Override
	public Object getId(PessoaPeritoIndisponibilidade e) {
		return e.getIdPeritoIndisponibilidade();
	}
	
	@SuppressWarnings("unchecked")
	public List<PessoaPeritoIndisponibilidade> recuperarAtivos(Especialidade especialidade, PessoaPerito perito, Date data) {
		StringBuilder jpql = new StringBuilder("SELECT ppi FROM PessoaPeritoIndisponibilidade ppi ")
			.append("WHERE ppi.pessoaPeritoEspecialidade.especialidade.idEspecialidade = :idEspecialidade ")
			.append("AND ppi.pessoaPeritoEspecialidade.pessoaPerito.idUsuario = :idPerito ")
			.append("AND :data BETWEEN ppi.dtInicio AND ppi.dtFim ")
			.append("AND ppi.ativo = true");
		
		Query query = getEntityManager().createQuery(jpql.toString())
			.setParameter("idEspecialidade", especialidade.getIdEspecialidade())
			.setParameter("idPerito", perito.getIdUsuario())
			.setParameter("data", data);

		return query.getResultList();
	}

}
