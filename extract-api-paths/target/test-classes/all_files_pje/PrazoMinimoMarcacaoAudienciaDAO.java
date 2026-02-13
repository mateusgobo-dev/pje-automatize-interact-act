package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.PrazoMinimoMarcacaoAudiencia;
import br.jus.pje.nucleo.entidades.TipoAudiencia;

@Name(PrazoMinimoMarcacaoAudienciaDAO.NAME)
public class PrazoMinimoMarcacaoAudienciaDAO extends BaseDAO<PrazoMinimoMarcacaoAudiencia> {
	
	public static final String NAME = "prazoMinimoMarcacaoAudienciaDAO";

	@Override
	public Object getId(PrazoMinimoMarcacaoAudiencia e) {
		return e.getIdPrazoMinimoMarcacaoAudiencia();
	}
	
	@SuppressWarnings("unchecked")
	public List<PrazoMinimoMarcacaoAudiencia> getPrazoMinimoMarcacaoAudienciaList(OrgaoJulgador orgaoJulgador){
		Query q = EntityUtil.getEntityManager().createQuery(
				"SELECT p FROM PrazoMinimoMarcacaoAudiencia p WHERE p.orgaoJulgador = :orgaoJulgador");
		
		q.setParameter("orgaoJulgador", orgaoJulgador);
		return q.getResultList();
	}
	
	public Integer getPrazoMinimoMarcacaoAudienciaPorTipo(OrgaoJulgador orgaoJulgador, TipoAudiencia tipoAudiencia){
		StringBuilder sb = new StringBuilder("SELECT vl_prazo FROM tb_prazo_min_marc_aud ")
			.append("WHERE id_orgao_julgador = :idorgaoJulgador and id_tipo_audiencia = :idTipoAudiencia");
		
		Query q = this.entityManager.createNativeQuery(sb.toString())
			.setParameter("idorgaoJulgador", orgaoJulgador.getIdOrgaoJulgador())
			.setParameter("idTipoAudiencia", tipoAudiencia.getIdTipoAudiencia())
			.setMaxResults(1);
		
		try {
			return (Integer) q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}
}
