package br.jus.cnj.pje.business.dao;

import java.util.List;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.Especialidade;

@Name("especialidadeDAO")
public class EspecialidadeDAO extends BaseDAO<Especialidade> {

	@Override
	public Object getId(Especialidade e) {
		return e.getIdEspecialidade();
	}

	public List<Especialidade> recuperarAtivas() {
		return getEntityManager().createQuery(
			"SELECT o FROM Especialidade o WHERE o.ativo = true ORDER BY o.especialidade", Especialidade.class).getResultList();
	}
	
}
