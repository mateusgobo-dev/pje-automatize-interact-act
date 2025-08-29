package br.jus.cnj.pje.business.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import br.jus.cnj.pje.webservice.client.correios.ConsultaClienteCorreios;
import br.jus.pje.nucleo.entidades.Cep;

@Name(CepDAO.NAME)
public class CepDAO extends BaseDAO<Cep>{

	public static final String NAME = "cepDAO";

	@Logger
	private Log logger;

	private final int MAX_RESULTS = 20;

	public Cep findByCodigo(String codigo){
		String queryString = "SELECT c FROM Cep AS c WHERE c.numeroCep = :codigo AND c.ativo = true";
		Query q = this.entityManager.createQuery(queryString);
		q.setParameter("codigo", codigo);
		Cep cep = null;
		try{
			cep = (Cep) q.getSingleResult();
		} catch (NoResultException e){
			ConsultaClienteCorreios clienteCorreios = (ConsultaClienteCorreios) Component.getInstance(ConsultaClienteCorreios.class);
			Cep cepConsulta = clienteCorreios.consultaCep(codigo);
			if(cepConsulta != null){
				persist(cepConsulta);
				flush();
				cep = cepConsulta;
			}
		} catch (NonUniqueResultException e){
			String message = "Há mais de um CEP com o mesmo código no banco de dados [" + codigo + "].";
			// throw new IllegalStateException();
			logger.error(message);
			cep = (Cep) q.getResultList().get(0);
		}
		return cep;
	}
	
	public List<Cep> findByNumero(String numero){
		String queryString = "SELECT c FROM Cep AS c WHERE c.numeroCep = :codigo AND c.ativo = true";
		Query q = this.entityManager.createQuery(queryString);
		q.setParameter("codigo", numero);
		List<Cep> ceps = new ArrayList<Cep>();
		ceps = (List<Cep>) q.getResultList();
		if(ceps.size() == 0){
			ConsultaClienteCorreios clienteCorreios = (ConsultaClienteCorreios) Component.getInstance(ConsultaClienteCorreios.class);
			Cep cepConsulta = clienteCorreios.consultaCep(numero);
			if(cepConsulta != null){
				persist(cepConsulta);
				flush();
				ceps.add(cepConsulta);
			}
		}
		return ceps;
	}

	public Cep findById(int id){
		return this.entityManager.find(Cep.class, id);
	}

	@SuppressWarnings("unchecked")
	public List<Cep> findByBegin(String codigo, int firstResult, int size){
		String queryString = "SELECT c FROM Cep AS c WHERE c.numeroCep LIKE :codigo AND c.ativo = true";
		Query q = entityManager.createQuery(queryString);
		q.setParameter("codigo", codigo + "%");
		q.setFirstResult(firstResult);
		if (size != -1){
			q.setMaxResults(size);
		}
		return q.getResultList();
	}

	public List<Cep> findByBegin(String codigo){
		return findByBegin(codigo, 0, MAX_RESULTS);
	}

	@Override
	public Integer getId(Cep e){
		return e.getIdCep();
	}

	/**
	 * Busca o CEP padrão de um município
	 * Algumas cidade possuem um cep único
	 * Será considerado CEP padrão quando os campos: logradouro, bairro e complementos forem nulos.
	 * @param idMunicipio
	 * @return List<Cep>
	 */
	@SuppressWarnings("unchecked")
	public List<Cep> getCepDefaultByIdMunicipio(int idMunicipio) {
		String queryString = "SELECT c FROM Cep AS c WHERE c.municipio.idMunicipio = :idMunicipio AND c.ativo = true "
				+ "AND c.nomeLogradouro is null AND c.nomeBairro is null AND c.complemento is null";
		Query q = this.entityManager.createQuery(queryString);
		q.setParameter("idMunicipio", idMunicipio);
		return q.getResultList();
	}
}