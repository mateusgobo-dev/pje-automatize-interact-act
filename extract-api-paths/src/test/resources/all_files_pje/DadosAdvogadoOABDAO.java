/**
 * CNJ - Conselho Nacional de Justiça
 * 
 * Data: 30/04/2015
 */
package br.jus.cnj.pje.business.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ProjetoUtil;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.ws.externo.cna.entidades.DadosAdvogadoOAB;

/**
 * Classe responsável pelo acesso aos dados de um advogado.
 * 
 * @author Adriano Pamplona
 */
@SuppressWarnings("unchecked")
@Name(DadosAdvogadoOABDAO.NAME)
public class DadosAdvogadoOABDAO extends BaseDAO<DadosAdvogadoOAB> {
	public static final String NAME = "dadosAdvogadoOABDAO";

	@Override
	public Object getId(DadosAdvogadoOAB e) {
		return e.getIdDadosAdvogadoOAB();
	}

	/**
	 * Consulta os dados dos advogados pelo CPF.
	 * 
	 * @param numCPF
	 *            CPF.
	 * @return Lista de advogados.
	 */
	public List<DadosAdvogadoOAB> consultar(String numCPF) {
		StringBuilder hql = new StringBuilder();
		hql.append("from DadosAdvogadoOAB ");
		hql.append("where ");
		hql.append("	replace(numCPF, '\\D', '', 'g') = :numCPF");

		Query query = getEntityManager().createQuery(hql.toString());
		query.setParameter("numCPF", numCPF);

		return query.getResultList();
	}

	/**
	 * Consulta os dados dos advogados pelo número de inscrição (oab) e a UF.
	 * 
	 * @param numInscricao
	 *            Número de inscrição (OAB).
	 * @param uf
	 *            UF do número de inscrição.
	 * @return Lista de advogados.
	 */
	public List<DadosAdvogadoOAB> consultar(String numInscricao, String uf) {
		StringBuilder hql = new StringBuilder();
		hql.append("from DadosAdvogadoOAB ");
		hql.append("where ");
		hql.append("	numInscricao = :numInscricao and ");
		hql.append("	uf = :uf");

		Query query = getEntityManager().createQuery(hql.toString());
		query.setParameter("numInscricao", numInscricao);
		query.setParameter("uf", uf);

		return query.getResultList();
	}

	/**
	 * Consulta os dados dos advogados pelo CPF, número de inscrição (oab) e a
	 * UF.
	 * 
	 * @param numCPF
	 *            CPF do advogado.
	 * @param numInscricao
	 *            Número de inscrição (OAB).
	 * @param uf
	 *            UF da inscrição.
	 * @return Lista de advogados.
	 */
	public List<DadosAdvogadoOAB> consultar(String numCPF, String numInscricao,
			String uf) {
		StringBuilder hql = new StringBuilder();
		hql.append("from DadosAdvogadoOAB ");
		hql.append("where ");
		hql.append("	replace(numCPF, '\\D', '', 'g') = :numCPF and ");
		hql.append("	numInscricao = :numInscricao and ");
		hql.append("	uf = :uf");

		Query query = getEntityManager().createQuery(hql.toString());
		query.setParameter("numCPF", numCPF);
		query.setParameter("numInscricao", numInscricao);
		query.setParameter("uf", uf);

		return query.getResultList();
	}
	
	/**
	 * Consulta os dados dos advogados pelo CPF e a UF.
	 * 
	 * @param numCPF
	 *            CPF do advogado.
	 * @param uf
	 *            UF da inscrição.
	 * @return Lista de advogados.
	 */
	public List<DadosAdvogadoOAB> consultarPeloCPFeUF(String numCPF, String uf) {
		StringBuilder hql = new StringBuilder();
		hql.append("from DadosAdvogadoOAB ");
		hql.append("where ");
		hql.append("	replace(numCPF, '\\D', '', 'g') = :numCPF and ");
		hql.append("	uf = :uf");

		Query query = getEntityManager().createQuery(hql.toString());
		query.setParameter("numCPF", numCPF);
		query.setParameter("uf", uf);

		return query.getResultList();
	}
	
	/**
	 * Retorna true se existir dados de advogado para os parâmetros passados.
	 * 
	 * @param numCPF
	 *            CPF do advogado.
	 * @param numInscricao
	 *            Número de inscrição (OAB).
	 * @param uf
	 *            UF da inscrição.
	 * @return Booleano
	 */
	public Boolean isExiste(String numCPF, String numInscricao, String uf) {
		StringBuilder hql = new StringBuilder();
		hql.append("select count(idDadosAdvogadoOAB) ");
		hql.append("from DadosAdvogadoOAB ");
		hql.append("where ");
		hql.append("	replace(numCPF, '\\D', '', 'g') = :numCPF and ");
		hql.append("	numInscricao = :numInscricao and ");
		hql.append("	uf = :uf");

		Query query = getEntityManager().createQuery(hql.toString());
		query.setParameter("numCPF", numCPF);
		query.setParameter("numInscricao", numInscricao);
		query.setParameter("uf", uf);

		Number qtd = (Number) query.getSingleResult();
		return qtd.intValue() > 0;
	}
	
	
	/**
	 * Remove uma lista de advogados.
	 * 
	 * @param advogados
	 */
	public void remove(List<DadosAdvogadoOAB> advogados) {
		if (!ProjetoUtil.isVazio(advogados)) {
			for (DadosAdvogadoOAB advogado : advogados) {
				super.remove(advogado);
			}
		}
	}
	
	/**
	 * Recupera Dados Advogado OAB pela Pessoa Advogado
	 * @param advogado
	 * @return List<DadosAdvogadoOAB>
	 */
	public List<DadosAdvogadoOAB> findByPessoaAdvogado(PessoaAdvogado advogado)	{
		List<DadosAdvogadoOAB> result = new ArrayList<>();
		
		String numeroOAB = advogado.getNumeroOAB();
		if (StringUtils.isNotBlank(numeroOAB)) {
			Query query = getEntityManager().createQuery(
				"FROM DadosAdvogadoOAB WHERE numCPF = :cpfAdvogado AND cast(REPLACE(numInscricao, '\\D', '', 'g') as integer) = :numInscricao");
			
			query.setParameter("cpfAdvogado", StringUtil.removeNaoNumericos(advogado.getNumeroCPF()));
			query.setParameter("numInscricao", Integer.parseInt(StringUtil.removeNaoNumericos(numeroOAB.trim())));
			result = query.getResultList();
		}
		
		return result;
	}
}
