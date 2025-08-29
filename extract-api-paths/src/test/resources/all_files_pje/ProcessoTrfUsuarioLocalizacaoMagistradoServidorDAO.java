/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrfUsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

/**
 * @author thiago.vieira
 * 
 */
@Name("processoTrfUsuarioLocalizacaoMagistradoServidorDAO")
public class ProcessoTrfUsuarioLocalizacaoMagistradoServidorDAO extends BaseDAO<ProcessoTrfUsuarioLocalizacaoMagistradoServidor> {

	
	@Override
	public Integer getId(ProcessoTrfUsuarioLocalizacaoMagistradoServidor e){
		return e.getIdProcessoTrfUsuarioLocalizacaoMagistradoServidor();
	}

	@SuppressWarnings("unchecked")
	public List<UsuarioLocalizacaoMagistradoServidor> getMagistradosVinculados(
			ProcessoTrf processoJudicial) {
		String query = "SELECT ulms.usuarioLocalizacaoMagistradoServidor " +
				"FROM ProcessoTrfUsuarioLocalizacaoMagistradoServidor AS ulms " +
				" WHERE ulms.processoTrf = :processo";
		Query q = entityManager.createQuery(query);
		q.setParameter("processo", processoJudicial);
		return q.getResultList();
	}

	public List<ProcessoTrfUsuarioLocalizacaoMagistradoServidor> buscaMagistradoNoProcesso(
			UsuarioLocalizacaoMagistradoServidor magistrado,
			ProcessoTrf processoJudicial) throws NoSuchFieldException {
		Search s = new Search(ProcessoTrfUsuarioLocalizacaoMagistradoServidor.class);
		s.addCriteria(Criteria.equals("usuarioLocalizacaoMagistradoServidor", magistrado));
		s.addCriteria(Criteria.equals("processoTrf", processoJudicial));
		s.setMax(1);
		return list(s);
	}
}
