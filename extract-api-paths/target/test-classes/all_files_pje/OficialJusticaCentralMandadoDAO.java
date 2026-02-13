package br.jus.cnj.pje.business.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.StatusMessage.Severity;

import br.jus.csjt.pje.commons.exception.BusinessException;
import br.jus.pje.nucleo.entidades.CentralMandado;
import br.jus.pje.nucleo.entidades.OficialJusticaCentralMandado;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;

@Name(OficialJusticaCentralMandadoDAO.NAME)
public class OficialJusticaCentralMandadoDAO extends BaseDAO<OficialJusticaCentralMandado> {

	public static final String NAME = "oficialJusticaCentralMandadoDAO";
	
	/**
	 * Retorna o cadastro, se existir, com base na Central de Mandado e no UsuarioLocalizacao informados.
	 * @param central
	 * @param ul
	 * @return
	 */
	public OficialJusticaCentralMandado getOficialJusticaCentralMandado(CentralMandado central, List<UsuarioLocalizacao> ul) {

		try{	
			StringBuilder hql = new StringBuilder("SELECT ojcm from OficialJusticaCentralMandado ojcm where ojcm.centralMandado = :central AND ojcm.usuarioLocalizacao in (:ul) ");
			Query query = getEntityManager().createQuery(hql.toString());
			query.setParameter("central", central);
			query.setParameter("ul", ul);
			return (OficialJusticaCentralMandado) query.getSingleResult();
		} catch (NoResultException nre) {
			return new OficialJusticaCentralMandado();
		} catch (NonUniqueResultException nure) {
			throw new BusinessException(Severity.ERROR, "oficialJusticaCentralMandado.erro.maisDeUmCadastro");
		} catch (Exception e) {
			e.printStackTrace();
			throw new BusinessException(Severity.ERROR, "oficialJusticaCentralMandado.erro.acessoAoBanco");
		}
	}
	
	/**
	 * Retorna lista de cadastros, se existir, com base no UsuarioLocalizacao informado.
	 * @param ul
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<OficialJusticaCentralMandado> getOficialJusticaCentralMandadoList(UsuarioLocalizacao ul) {
		try{	
			StringBuilder hql = new StringBuilder("SELECT ojcm from OficialJusticaCentralMandado ojcm where ojcm.usuarioLocalizacao = :ul ");
			Query query = getEntityManager().createQuery(hql.toString());
			query.setParameter("ul", ul);
			return (ArrayList<OficialJusticaCentralMandado>) query.getResultList();
		} catch (NoResultException nre) {
			return new ArrayList<OficialJusticaCentralMandado>(0);
		}catch (Exception e) {
			throw new BusinessException(Severity.ERROR, "oficialJusticaCentralMandado.erro.acessoAoBanco");
		}
	}
	
	@Override
	public Integer getId(OficialJusticaCentralMandado e) {
		return e.getIdOficialJusticaCentralMandado();
	}
}
