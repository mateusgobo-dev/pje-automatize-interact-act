package br.com.infox.pje.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.dao.GenericDAO;
import br.com.infox.pje.query.RpvQuery;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Rpv;
import br.jus.pje.nucleo.entidades.RpvPessoaParte;
import br.jus.pje.nucleo.entidades.TipoParte;

@Name(RpvDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class RpvDAO extends GenericDAO implements Serializable, RpvQuery {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "rpvDAO";

	/**
	 * Retorna uma lista de rpv originárias do beneficiario do processo e que
	 * não estejam canceladas ou rejeitadasdo
	 * 
	 * @param processoTrf
	 * @return lista de rpv
	 */
	@SuppressWarnings("unchecked")
	public List<Rpv> listRpvOriginariaByRpv(Rpv rpv) {
		int idRpvStatusCancelado = ParametroUtil.instance().getStatusRpvCancelada().getIdRpvStatus();
		int idRpvStatusRejeitada = ParametroUtil.instance().getStatusRpvRejeitada().getIdRpvStatus();

		Query q = getEntityManager().createQuery(LIST_RPV_ORIGINARIA_BY_RPV_QUERY);
		q.setParameter(QUERY_PARAMETER_ID_PROCESSO_TRF, rpv.getProcessoTrf().getIdProcessoTrf());
		q.setParameter(QUERY_PARAMETER_ID_RPV_STATUS_CANCELADA, idRpvStatusCancelado);
		q.setParameter(QUERY_PARAMETER_ID_RPV_STATUS_REJEITADA, idRpvStatusRejeitada);
		q.setParameter(QUERY_PARAMETER_ID_BENEFICIARIO, rpv.getBeneficiario().getIdUsuario());
		q.setParameter(QUERY_PARAMETER_ID_RPV, rpv.getIdRpv());

		List<Rpv> list = q.getResultList();
		return list;
	}

	/**
	 * Retorna uma lista de rpv do beneficiario do processo que sejam parcial 
	 * incontronversa e que não estejam canceladas ou rejeitadas
	 * @param processoTrf
	 * @return lista de rpv
	 */
	@SuppressWarnings("unchecked")
	public List<Rpv> listRpvParcialByRpv(Rpv rpv) {
		int idRpvStatusCancelado = ParametroUtil.instance().getStatusRpvCancelada().getIdRpvStatus();
		int idRpvStatusRejeitada = ParametroUtil.instance().getStatusRpvRejeitada().getIdRpvStatus();

		Query q = getEntityManager().createQuery(LIST_RPV_PARCIAL_BY_RPV_QUERY);
		q.setParameter(QUERY_PARAMETER_ID_PROCESSO_TRF, rpv.getProcessoTrf().getIdProcessoTrf());
		q.setParameter(QUERY_PARAMETER_ID_RPV_STATUS_CANCELADA, idRpvStatusCancelado);
		q.setParameter(QUERY_PARAMETER_ID_RPV_STATUS_REJEITADA, idRpvStatusRejeitada);
		q.setParameter(QUERY_PARAMETER_ID_BENEFICIARIO, rpv.getBeneficiario().getIdUsuario());
		q.setParameter(QUERY_PARAMETER_ID_RPV, rpv.getBeneficiario().getIdUsuario());

		List<Rpv> list = q.getResultList();
		return list;
	}

	/**
	 * Retorna uma lista de rpv do beneficiario do processo que sejam apenas
	 * ressarcimento de custas e que não estejam canceladas ou rejeitadas
	 * 
	 * @param rpv
	 *            Rpv base para a pesquisa
	 * @return lista de rpv
	 */
	@SuppressWarnings("unchecked")
	public List<Rpv> listRpvRessarcimentoCustasByRpv(Rpv rpv) {
		int idRpvStatusCancelado = ParametroUtil.instance().getStatusRpvCancelada().getIdRpvStatus();
		int idRpvStatusRejeitada = ParametroUtil.instance().getStatusRpvRejeitada().getIdRpvStatus();

		Query q = getEntityManager().createQuery(LIST_RPV_RESSARCIMENTO_CUSTAS_BY_RPV_QUERY);
		q.setParameter(QUERY_PARAMETER_ID_PROCESSO_TRF, rpv.getProcessoTrf().getIdProcessoTrf());
		q.setParameter(QUERY_PARAMETER_ID_RPV_STATUS_CANCELADA, idRpvStatusCancelado);
		q.setParameter(QUERY_PARAMETER_ID_RPV_STATUS_REJEITADA, idRpvStatusRejeitada);
		q.setParameter(QUERY_PARAMETER_ID_BENEFICIARIO, rpv.getBeneficiario().getIdUsuario());
		q.setParameter(QUERY_PARAMETER_ID_RPV, rpv.getIdRpv());

		List<Rpv> list = q.getResultList();
		return list;
	}

	/**
	 * Retorna uma lista de RpvPessoaParte com os Cessionarios da RPV
	 * 
	 * @param rpv
	 * @return lista de RpvPessoaParte
	 */
	@SuppressWarnings("unchecked")
	public List<RpvPessoaParte> listCessionarioByRpv(Rpv rpv) {
		TipoParte tipoParteCessionario = ParametroUtil.instance().getTipoParteCessionario();
		Query q = getEntityManager().createQuery(LIST_CESSIONARIO_BY_RPV_QUERY);
		q.setParameter(QUERY_PARAMETER_RPV, rpv);
		q.setParameter(QUERY_PARAMETER_TIPO_PARTE_CESSIONARIO, tipoParteCessionario);

		List<RpvPessoaParte> list = q.getResultList();
		return list;
	}

	/**
	 * Retrona uma string com o status da oab
	 * 
	 * @param cpf
	 * @param uf
	 * @return status
	 */
	public String getStatusOabAdvogado(String cpf, String uf) {
		Query q = getEntityManager().createQuery(STATUS_OAB_ADVOGADO_QUERY);
		q.setParameter(QUERY_PARAMETER_CPF, cpf);
		q.setParameter(QUERY_PARAMETER_UF, uf);

		String result = EntityUtil.getSingleResult(q);
		return result;
	}
}
