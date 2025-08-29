package br.jus.cnj.pje.business.dao;

import java.io.Serializable;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.Especialidade;
import br.jus.pje.nucleo.entidades.PessoaPerito;
import br.jus.pje.nucleo.entidades.PessoaPeritoEspecialidade;

@Name("pessoaPeritoEspecialidadeDAO")
public class PessoaPeritoEspecialidadeDAO extends BaseDAO<PessoaPeritoEspecialidade> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Integer getId(PessoaPeritoEspecialidade pessoaPeritoEspecialidade) {
		return pessoaPeritoEspecialidade.getIdPessoaPeritoEspecialidade();
	}

	public PessoaPeritoEspecialidade findByPessoaPeritoAndEspecialidade(PessoaPerito pessoaPerito, Especialidade especialidade) {
		String query = "select o from PessoaPeritoEspecialidade o where o.pessoaPerito = :pessoaPerito and o.especialidade.especialidade = :especialidade";
		Query q = getEntityManager().createQuery(query);
		q.setParameter("pessoaPerito", pessoaPerito);
		q.setParameter("especialidade", especialidade.getEspecialidade());
		return getSingleResult(q);
	}
}
