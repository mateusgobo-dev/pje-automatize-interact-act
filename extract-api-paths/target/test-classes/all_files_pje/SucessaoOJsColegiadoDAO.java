/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.SucessaoOJsColegiado;

@Name("sucessaoOJsColegiadoDAO")
public class SucessaoOJsColegiadoDAO extends BaseDAO<SucessaoOJsColegiado> {

	@Override
	public Object getId(SucessaoOJsColegiado e) {
		return e.getIdSucessaoOJsColegiado();
	}

	/***
	 * Retorna a última sucessão do OJ especificado no colegiado indicado, a partir da data informada
	 * 
	 * @param orgaoJulgadorSucedido
	 * @param orgaoJulgadorColegiado
	 * @param dataReferencia
	 * @return
	 */
	public SucessaoOJsColegiado obterSucessaoPeloOrgaoJulgadorSucedido(
			OrgaoJulgador orgaoJulgadorSucedido, OrgaoJulgadorColegiado orgaoJulgadorColegiado,
			Date dataReferencia) {
 		
		StringBuilder hql = new StringBuilder();
		
		hql.append("select o from SucessaoOJsColegiado o 		  ");
		hql.append(" where o.orgaoJulgadorSucedido = :orgaoJulgadorSucedido ");
		if(dataReferencia != null) {
			hql.append(" AND o.dataSucessao >= :dataReferencia ");
		}
		hql.append(" ORDER BY o.dataSucessao DESC ");

		Query q = getEntityManager().createQuery(hql.toString());
		q.setParameter("orgaoJulgadorSucedido", orgaoJulgadorSucedido);
		
		if(dataReferencia != null) {
			q.setParameter("dataReferencia", dataReferencia);
		}
		
		@SuppressWarnings("unchecked")
		List<SucessaoOJsColegiado> resultList = q.getResultList();		
		
		SucessaoOJsColegiado sucessaoOJs = null;
		
		if (!resultList.isEmpty()){
			sucessaoOJs = resultList.get(0);
		}
		
		return sucessaoOJs;
	}

	public SucessaoOJsColegiado obterSucessaoPeloOrgaoJulgadorSucedido(
			OrgaoJulgador orgaoJulgadorSucedido, OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		
		return this.obterSucessaoPeloOrgaoJulgadorSucedido(orgaoJulgadorSucedido, orgaoJulgadorColegiado, null);
	}
}
