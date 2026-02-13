/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PessimisticLockException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.pje.nucleo.entidades.Parametro;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

/**
 * @author cristof
 * 
 */
@Name("parametroDAO")
public class ParametroDAO extends BaseDAO<Parametro>{

	@Logger
	private Log logger;

	public Parametro findByName(String name){
		Parametro par = null;
		String queryStr = "SELECT p FROM Parametro AS p WHERE p.nomeVariavel = :nome";
		Query q = this.entityManager.createQuery(queryStr);
		q.setParameter("nome", name);
		try{
			par = (Parametro) q.getSingleResult();
		} catch (NoResultException e){
			logger.warn("Não há parâmetro ativo com nome [{0}].", name);
			return null;
		} catch (NonUniqueResultException e){
			logger.error("Há mais de um parâmetro com o nome [{0}]. Utilize a função findMultipleByName.", name);
			return null;
		}
		return par;
	}

	@SuppressWarnings("unchecked")
	public List<Parametro> findMultipleByName(String name){
		String queryStr = "SELECT p FROM Parametro AS p WHERE p.nomeVariavel = :nome";
		Query q = this.entityManager.createQuery(queryStr);
		q.setParameter("nome", name);
		return q.getResultList();
	}

	public String valueOf(String parameterName){
		String queryStr = "SELECT p FROM Parametro AS p WHERE p.ativo = true AND p.nomeVariavel = :nome";
		Query q = this.entityManager.createQuery(queryStr);
		q.setParameter("nome", parameterName);
		Parametro par = null;
		try{
			par = (Parametro) q.getSingleResult();
		} catch (NoResultException e){
			logger.warn("Não há parâmetro ativo com nome [{0}].", parameterName);
			return null;
		} catch (NonUniqueResultException e){
			logger.warn("Há mais de um parâmetro ativo com o nome [{0}]. Utilize a função multipleValuesByName.",
					parameterName);
			return null;
		}
		return par.getValorVariavel();
	}

	@SuppressWarnings("unchecked")
	public List<String> multipleValuesByName(String parameterName){
		String queryStr = "SELECT p FROM Parametro AS p WHERE p.nome = :nomeVariavel AND p.ativo = true";
		Query q = this.entityManager.createQuery(queryStr);
		q.setParameter("nome", parameterName);
		List<Parametro> params = q.getResultList();
		List<String> ret = new ArrayList<String>(params.size());
		for (Parametro p : params){
			ret.add(p.getValorVariavel());
		}
		return ret;
	}

	@Override
	public Integer getId(Parametro e){
		return e.getIdParametro();
	}
	
	public <T> T valueOf(Class<T> clazz, String nome){
		String valor = null;
		try{
			 valor = valueOf(nome);
			if(valor == null){
				return null;
			}
			Integer id = new Integer(valor);
			return getEntity(clazz, id);
		}catch (NumberFormatException e){
			logger.error("Não foi possível converter o valor da variável [{0}] ({1}) para um número identificador de parâmetro.", nome, valor);
			return null;
		}
	}
	
	private <T, U> T getEntity(Class<T> clazz, U id){
		return (T) entityManager.find(clazz, id);
	}

	/**
	 * metodo responsavel por recuperar todos os parametros cadastrados pela pessoa passada em parametro
	 * @param pessoa
	 * @return
	 * @throws Exception 
	 */
	public List<Parametro> recuperarParametrosCadastrados(Pessoa pessoa) throws Exception {
		List<Parametro> resultado = null;
		Search search = new Search(Parametro.class);
		try {
			search.addCriteria(Criteria.equals("usuarioModificacao.idUsuario", pessoa.getIdPessoa()));
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		try {
			resultado = list(search);
		} catch (EntityNotFoundException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("Ocorreu um erro ao tentar recuperar os parametros cadastrados pela pessoa ");
			sb.append(pessoa.getNome());
			sb.append(". Por favor, contacte o suporte do tribunal.");
			
			throw new Exception(sb.toString());
		}
		return resultado;
	}

	public void atualizaDataHoraUltimaExecucaoJOB(String dataDaUltimaExecucaoJOB) throws Exception {
		String parametro = Parametros.PJE_DOMICILIO_ELETRONICO_DATA_HORA_ULTIMA_EXECUCAO_JOB_TCD;

		if (entityManager == null || !entityManager.isOpen()) {
			String errorMessage = "EntityManager nulo ou fechado ao atualizar a data/hora do parametro [{0}].";

			throw new Exception(MessageFormat.format(errorMessage, parametro));
		}

		StringBuilder sql = new StringBuilder();

		sql.append("\n").append(" UPDATE core.tb_parametro                     ");
		sql.append("\n").append(" SET vl_variavel = :dataHoraUltimaExecucaoJOB ");
		sql.append("\n").append(" WHERE nm_variavel = :parametro               ");
		sql.append("\n").append(" AND in_ativo = true                          ");

		Query query = entityManager.createNativeQuery(sql.toString());

		query.setParameter("dataHoraUltimaExecucaoJOB", dataDaUltimaExecucaoJOB);
		query.setParameter("parametro", parametro);

		Integer rowsUpdate = 0;

		final int MAX_TENTATIVAS = 60;

		// TENTA REALIZAR O UPDATE POR ATÉ 60 SEGUNDOS (60 TENTATIVAS DE 1 SEGUNDO CADA)
		for (int indiceTentativa = 0; indiceTentativa < MAX_TENTATIVAS; ++indiceTentativa) {
			try {
				rowsUpdate = query.executeUpdate();

				break;
			} catch (PessimisticLockException e) {
				logger.warn("Lock no banco ao atualizar o parametro [{0}]. Erro: [{1}]", parametro, e);
			} catch (Exception e) {
				String exceptionMessage = "Houve um erro ao atualizar o parametro [{0}]. Erro: [{1}]";

				throw new Exception(MessageFormat.format(exceptionMessage, parametro, e));
			}

			TimeUnit.SECONDS.sleep(1);
		}

		if (rowsUpdate <= 0) {
			String errorMessage = "Não foi possivel atualizar a data execução do parametro [{0}].";
			logger.warn(MessageFormat.format(errorMessage, parametro));
		}
	}
}
