package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.log.EntityLog;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name("entityLogDAO")
public class EntityLogDAO extends BaseDAO<EntityLog>{

	@Override
	public Object getId(EntityLog e) {
		return e.getIdEntidade();
	}

	/**
	 * metodo responsavel por recuperar todos os entityLogs da pessoa passada em parametro.
     *
	 * @param _pessoa
	 * @return
	 * @throws Exception
	 */
	public List<EntityLog> recuperarEntityLogs(Pessoa _pessoa) throws Exception {
		List<EntityLog> resultado = null;
		Search search = new Search(EntityLog.class);
		try {
			search.addCriteria(Criteria.equals("idUsuario", _pessoa.getIdPessoa()));
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		try {
			resultado = list(search);
		} catch (EntityNotFoundException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("Ocorreu um erro ao tentar recuperar os entityLogs da pessoa ");
			sb.append(_pessoa.getNome());
			sb.append(". Por favor, contacte o suporte do tribunal.");
			
			throw new Exception(sb.toString());
		}
		return resultado;
	}

    @Override
    protected EntityManager getEntityManager() {
        return (EntityManager) Component.getInstance("entityManagerLog");
    }

}