package br.jus.cnj.pje.business.dao;

import java.math.BigInteger;
import java.util.UUID;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.VerificadorPeriodicoLote;

@Name(VerificadorPeriodicoLoteDAO.NAME)
public class VerificadorPeriodicoLoteDAO extends BaseDAO<VerificadorPeriodicoLote> {

	public static final String NAME = "verificadorPeriodicoLoteDAO";

	@Override
	public Object getId(VerificadorPeriodicoLote e) {
		return e.getIdVerificadorPeriodicoLote();
	}

	public Integer getExpedientesProcessadosPorLote(UUID lote) {
		StringBuilder sql = new StringBuilder();

		sql.append("select count(*) ");
		sql.append("from VerificadorPeriodicoLote vpl ");
		sql.append("where vpl.lote = :lote and ");
		sql.append("vpl.processado = :processado");

		Query query = getEntityManager().createQuery(sql.toString());

		query.setParameter("lote", lote);
		query.setParameter("processado", true);

		Integer result = query.getSingleResult() != null ? Integer.parseInt(query.getSingleResult().toString()) : 0;

		return result;
	}

	public VerificadorPeriodicoLote getByIdJobAndLote(Integer idJob, UUID lote) {
		VerificadorPeriodicoLote result = null;

		try {
			StringBuilder sql = new StringBuilder();

			sql.append(" SELECT vpl ");
			sql.append(" FROM VerificadorPeriodicoLote vpl ");
			sql.append(" WHERE vpl.idJob = :idJob ");
			sql.append(" AND vpl.lote = :lote ");

			Query query = entityManager.createQuery(StringUtils.normalizeSpace(sql.toString()),
					VerificadorPeriodicoLote.class);

			query.setParameter("idJob", idJob);
			query.setParameter("lote", lote);

			result = (VerificadorPeriodicoLote) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		} catch (NonUniqueResultException e) {
			logger.warn("Há mais de um registro na tabela com o idJob [" + idJob + "] e lote [" + lote + "].");

			return null;
		} catch (Exception e) {
			e.printStackTrace();

			logger.warn("Houve um erro ao buscar o objeto VerificadorPeriodicoLote com o idJob [" + idJob
					+ "] e com o lote [" + lote + "]." + e.getLocalizedMessage());
		}

		return result;
	}

	public VerificadorPeriodicoLote getProcessado(Integer idRelatorio, String passo) {
		VerificadorPeriodicoLote result = null;

		try {
			StringBuilder sql = new StringBuilder();

			sql.append(" SELECT ");
			sql.append(" sum(vpl.qt_processado_job) AS qtProcessadoJob, ");
			sql.append(" sum(vpl.nr_tamanho_job) AS tamanhoJob ");
			sql.append(" FROM tb_verificador_periodico_lote vpl ");
			sql.append(" WHERE vpl.id_verificador_periodico_lote > :idRelatorio ");
			sql.append(" AND vpl.ds_passo ilike '%' || :passo || '%' ");

			Query query = entityManager.createNativeQuery(StringUtils.normalizeSpace(sql.toString()));

			query.setParameter("idRelatorio", idRelatorio);
			query.setParameter("passo", passo);

			Object[] objectResult = (Object[]) query.getSingleResult();

			if (objectResult == null || objectResult[0] == null || objectResult[1] == null) {
				return result;
			}

			result = new VerificadorPeriodicoLote();

			result.setQtProcessadoJob(((BigInteger) objectResult[0]).intValue());
			result.setTamanhoJob(((BigInteger) objectResult[1]).intValue());
		} catch (NoResultException e) {
			return null;
		} catch (NonUniqueResultException e) {
			logger.warn("Há mais de um registro na tabela com o idRelatorio [" + idRelatorio + "] e passo [" + passo
					+ "].");

			return null;
		} catch (Exception e) {
			e.printStackTrace();

			logger.warn("Houve um erro ao buscar o objeto VerificadorPeriodicoLote com o idRelatorio [" + idRelatorio
					+ "] e com o passo [" + passo + "]." + e.getLocalizedMessage());
		}

		return result;
	}
}