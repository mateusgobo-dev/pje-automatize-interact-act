/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.lang.time.DateUtils;
import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.SubstituicaoMagistrado;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;

@Name("substituicaoMagistradoDAO")
public class SubstituicaoMagistradoDAO extends BaseDAO<SubstituicaoMagistrado> {

	@Override
	public Object getId(SubstituicaoMagistrado e) {
		return e.getIdSubstituicaoMagistrado();
	}

	/**
	 * Dada uma substituição, verifica se existe alguma outra substituição
	 * com período conflitante com a substituição em questão, 
	 * para o mesmo magistrado afastado.  
	 * @param substituicaoMagistrado a substituição a ser analisada
	 * @return true caso existam substituições conflitantes.
	 */
	public boolean existeSubstituicaoConflitante(
			SubstituicaoMagistrado substituicaoMagistrado) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("select o from SubstituicaoMagistrado o 							 ");
		sb.append("where o.orgaoJulgador = :orgaoJulgador						 	 ");
		sb.append("and ( o.dataInicio between :novaDataInicial and :novaDataFinal or ");
		sb.append("      :novaDataInicial between o.dataInicio and o.dataFim )		 ");
		sb.append("and o.idSubstituicaoMagistrado <> :idSubstituicaoMagistrado 	 	 ");
		
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("orgaoJulgador", substituicaoMagistrado.getOrgaoJulgador());
		q.setParameter("novaDataInicial", substituicaoMagistrado.getDataInicio());
		q.setParameter("novaDataFinal", substituicaoMagistrado.getDataFim());
		
		Integer paramIdSubstituicao = (substituicaoMagistrado.getIdSubstituicaoMagistrado() != null ? substituicaoMagistrado.getIdSubstituicaoMagistrado() : -1);
		q.setParameter("idSubstituicaoMagistrado", paramIdSubstituicao);
		
		@SuppressWarnings("unchecked")
		List<SubstituicaoMagistrado> resultList = q.getResultList();		
		return resultList.size() > 0;
	}

	/**
	 * Método que obtém todas substituições cadastradas para determinado magistrado em um Órgão Julgador
	 * @param orgaoJulgadorColegiado
	 * @param orgaoJulgador
	 * @param magistrado
	 * @return lista com substituições em que o magistrado dado atuou.
	 */
	public List<SubstituicaoMagistrado> obterSubstituicoesMagistrado(OrgaoJulgadorColegiado orgaoJulgadorColegiado, OrgaoJulgador orgaoJulgador, Usuario magistrado) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("select o from SubstituicaoMagistrado o 							 	");
		sb.append("where o.magistradoSubstituto.idUsuario = :idUsuarioMagistrado		");
		sb.append("and o.orgaoJulgador = :orgaoJulgador 								");
		
		if (orgaoJulgadorColegiado != null) {
			sb.append("and o.orgaoJulgadorColegiado = :orgaoJulgadorColegiado 			");
		}
		sb.append("order by dataFim desc"); 
		
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("idUsuarioMagistrado", magistrado.getIdUsuario());
		q.setParameter("orgaoJulgador", orgaoJulgador);
		
		if (orgaoJulgadorColegiado != null) {
			q.setParameter("orgaoJulgadorColegiado", orgaoJulgadorColegiado);
		}
		
		@SuppressWarnings("unchecked")
		List<SubstituicaoMagistrado> resultList = q.getResultList();		
		return resultList;
	}
	

	/**
	 * Método resposável por retornar a substituição vigente para um perfil de
	 * magistrado dado.
	 * 
	 * @param perfilMagistradoSubstituto
	 *            localização do magistrado que está como substituto
	 *            
	 * @return A substituicao vigente caso o perfil esteja cadastrado como
	 *         substituto ou null caso nada seja encontrado
	 */
	public SubstituicaoMagistrado obterSubstituicaoMagistradoVigente(OrgaoJulgador orgaoJulgador, OrgaoJulgadorColegiado orgaoJulgadorColegiado,
			UsuarioLocalizacaoMagistradoServidor perfilMagistradoSubstituto) {
		Date hoje = DateUtils.truncate(new Date(), Calendar.DATE);
		StringBuilder sb = new StringBuilder();
			
		sb.append("select o from SubstituicaoMagistrado o ");
		sb.append("where o.magistradoSubstituto.idUsuario = :idUsuarioMagistrado ");
		sb.append("and o.orgaoJulgador = :oj ");
		sb.append("and o.orgaoJulgadorColegiado = :ojc ");
		sb.append("and (o.dataFim IS NULL or o.dataFim >= :hoje) ");
		sb.append("order by o.dataFim desc "); 
		
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("idUsuarioMagistrado", perfilMagistradoSubstituto.getUsuarioLocalizacao().getUsuario().getIdUsuario());
		q.setParameter("oj", orgaoJulgador);
		q.setParameter("ojc", orgaoJulgadorColegiado);
		q.setParameter("hoje", hoje);
		
		try {
			return (SubstituicaoMagistrado) q.getSingleResult();			
		} catch (NoResultException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public SubstituicaoMagistrado obterSubstituicaoVigentePorMagistradoAfastado(OrgaoJulgador orgaoJulgador,
			OrgaoJulgadorColegiado orgaoJulgadorColegiado, Date dataReferencia) {

		SubstituicaoMagistrado substituicaoVigente = null;

		StringBuilder hql = new StringBuilder("select o from SubstituicaoMagistrado o ")
				.append("where o.orgaoJulgador = :oj ")
				.append("and o.orgaoJulgadorColegiado = :ojc ")
				.append("and :dataReferencia between o.dataInicio and o.dataFim ")
				.append("order by o.dataFim desc ");

		Query query = getEntityManager().createQuery(hql.toString());
		query.setParameter("oj", orgaoJulgador);
		query.setParameter("ojc", orgaoJulgadorColegiado);
		query.setParameter("dataReferencia", dataReferencia);
		query.setMaxResults(1);

		List<SubstituicaoMagistrado> substituicoesVigentes = query.getResultList();
		if (!substituicoesVigentes.isEmpty()) {
			substituicaoVigente = substituicoesVigentes.get(0);
		}
		return substituicaoVigente;
	}
}
