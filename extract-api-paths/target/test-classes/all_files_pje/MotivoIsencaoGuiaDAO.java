package br.jus.cnj.pje.business.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.MotivoIsencaoGuia;

@Name(MotivoIsencaoGuiaDAO.NAME)
@AutoCreate
public class MotivoIsencaoGuiaDAO extends BaseDAO<MotivoIsencaoGuia> implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final String NAME = "motivoIsencaoGuiaDAO";

	@SuppressWarnings("unchecked")
	public List<MotivoIsencaoGuia> findAll() {
		String queryStr = "SELECT o FROM MotivoIsencaoGuia AS o WHERE o.ativo = true ";
		Query q = EntityUtil.getEntityManager().createQuery(queryStr);
		return q.getResultList();
	}

	@Override
	public Object getId(MotivoIsencaoGuia motivo) {
		return motivo.getId();
	}

	public List<MotivoIsencaoGuia> findMotivos() {
		return this.findMotivos(null);
	}

	public MotivoIsencaoGuia findMotivo(String motivoIsencao) {
		StringBuilder jpql = new StringBuilder();
		jpql.append(" select motivo from MotivoIsencaoGuia motivo ");
		jpql.append(" where motivo.dsMotivoIsencao = :motivoIsencao ");
		TypedQuery<MotivoIsencaoGuia> query = getEntityManager().createQuery(jpql.toString(), MotivoIsencaoGuia.class);
		query.setParameter("motivoIsencao", motivoIsencao);
		return query.getSingleResult();
	}

	@SuppressWarnings("unchecked")
	public List<MotivoIsencaoGuia> findMotivos(Boolean ativo) {
		StringBuilder sql = new StringBuilder();
		sql.append("select tmig.id_motivo_isencao, tmig.ds_motivo_isencao, tmig.in_controla_isencao, tmig.in_ativo from tb_motivo_isencao_guia tmig ");
		if (ativo != null) {
			sql.append("where tmig.in_ativo = :ativo ");
		}
		Query query = getEntityManager().createNativeQuery(sql.toString());
		if (ativo != null) {
			query.setParameter("ativo", ativo);
		}
		List<MotivoIsencaoGuia> motivos = new ArrayList<MotivoIsencaoGuia>();
		MotivoIsencaoGuia motivo = null;
		List<Object[]> resultList = query.getResultList();
		for (Object[] borderTypes: resultList) {
			motivo = new MotivoIsencaoGuia();
			motivo.setId((Integer)borderTypes[0]);
			motivo.setDsMotivoIsencao((String)borderTypes[1]);
			motivo.setInControlaIsencao((Boolean)borderTypes[2]);
			motivo.setAtivo((Boolean)borderTypes[3]);
			motivos.add(motivo);
		}
		return motivos;	
	}
}