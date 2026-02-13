package br.jus.csjt.pje.persistence.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.itx.util.EntityUtil;
import br.jus.pje.jt.entidades.VaraItinerante;
import br.jus.pje.nucleo.entidades.Municipio;

/**
 * @author Rafael Barros / Sérgio Pacheco
 * @since 1.2.0
 * @see
 * @category PJE-JT
 * @class VaraItineranteList
 * @description Classe responsável por consultas da entidade VaraItinerante
 */
@Name(VaraItineranteList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class VaraItineranteList extends EntityList<VaraItinerante> {

	public static final String NAME = "varaItineranteList";
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from VaraItinerante o, JurisdicaoMunicipio j"
			+ " where o.jurisdicaoMunicipio.jurisdicao.idJurisdicao = #{jurisdicaoHome.jurisdicaoIdJurisdicao}"
			+ "   and o.jurisdicaoMunicipio.idJurisdicaoMunicipio = j.idJurisdicaoMunicipio";
													
	private static final String DEFAULT_ORDER = "o.jurisdicaoMunicipio.municipio.municipio";

	@Override
	protected void addSearchFields() {
		addSearchField("jurisdicaoMunicipio.municipio.municipio", SearchCriteria.contendo);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("municipio", "o.jurisdicaoMunicipio.municipio.municipio");
		return map;
	}

	@Override
	protected String getDefaultEjbql() {
		return DEFAULT_EJBQL;
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	@SuppressWarnings("unchecked")
	public Boolean isMunicipioAtendido(Municipio municipio) {
		String sql = " select o from VaraItinerante " + " o where o.jurisdicaoMunicipio.municipio = :municipio ";

		EntityManager entityManager = EntityUtil.getEntityManager();
		Query query = entityManager.createQuery(sql);
		query.setParameter("municipio", municipio);
		List<VaraItinerante> result = query.getResultList();

		if ((result != null) && (result.size() > 0))
			return true;

		return false;
	}

}
