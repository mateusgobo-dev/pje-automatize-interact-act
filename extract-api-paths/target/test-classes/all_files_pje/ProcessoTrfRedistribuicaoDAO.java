package br.jus.cnj.pje.business.dao;


import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrfRedistribuicao;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name(ProcessoTrfRedistribuicaoDAO.NAME)
public class ProcessoTrfRedistribuicaoDAO extends BaseDAO<ProcessoTrfRedistribuicao>{

	public static final String NAME = "processoTrfRedistribuicaoDAO";

	@Override
	public Object getId(ProcessoTrfRedistribuicao e) {
		return e.getIdProcessoTrfRedistribuicao();
	}

	public List<ProcessoTrfRedistribuicao> recuperaRedistribuicoesProcessos(Pessoa _pessoa) {
		List<ProcessoTrfRedistribuicao> resultado = null;
		Search search = new Search(ProcessoTrfRedistribuicao.class);
		try {
			search.addCriteria(Criteria.equals("usuario.idUsuario", _pessoa.getIdPessoa()));
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		resultado = list(search);
		return resultado;
	}
	
	@SuppressWarnings("unchecked")
	public List<OrgaoJulgador> recuperar(ProcessoTrf processoTrf, String tiposRedistribuicao) {
		StringBuilder jpql = new StringBuilder("SELECT o.orgaoJulgadorAnterior FROM ProcessoTrfRedistribuicao o ")
			.append("WHERE o.processoTrf = :processoTrf AND o.inTipoRedistribuicao in (")
			.append(tiposRedistribuicao)
			.append(")");
		
		Query query = entityManager.createQuery(jpql.toString())
			.setParameter("processoTrf", processoTrf);
		
		return query.getResultList();
	}
}