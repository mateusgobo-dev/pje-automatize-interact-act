package br.jus.cnj.pje.business.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.dto.MunicipioDTO;
import br.jus.pje.nucleo.entidades.Municipio;

@Name(MunicipioDAO.NAME)
public class MunicipioDAO extends BaseDAO<Municipio> {

	public static final String NAME = "municipioDAO";

	@Override
	public Object getId(Municipio e) {
		return e.getIdMunicipio();
	}
	
	@SuppressWarnings("unchecked")
	public List<Municipio> findByUf(final String uf){
		Query query = getEntityManager().createQuery("SELECT m FROM Municipio m WHERE m.estado.codEstado = :uf order by to_ascii(m.municipio)");
		query.setParameter("uf", uf);
		return query.getResultList();
	}
	
	public Municipio findByUfAndDescricao(String uf, String descricao){
		Query query = getEntityManager().createQuery("SELECT m FROM Municipio m WHERE m.estado.codEstado = :uf"
				+ " and lower(to_ascii(m.municipio)) = lower(to_ascii(:descricao))");
		query.setParameter("uf", uf);
		query.setParameter("descricao", descricao);
		Municipio municipio = null;
		try{
			 municipio =(Municipio) query.getSingleResult(); 
		}
		catch(NoResultException e){
			return null;
		}
		return municipio; 
	}
	
	/**
	 * Recupera o objeto {@link Municipio} que representa o município de nascimento do usuário.
	 * 
	 * @param id Identificador do usuário no sistema.
	 * @return {@link Municipio} que representa o município de nascimento do usuário.
	 */
	public Municipio getMunicipioByIdPessoa(final Object id) {
		Query q = this.entityManager.createQuery("SELECT pf.municipioNascimento FROM PessoaFisica AS pf WHERE pf.idPessoa = :id");
		q.setParameter("id", id);

		try {
			return (Municipio) q.getSingleResult();
		} catch(NoResultException ex) {
			return null;
		}
	}
	
	public Municipio getMunicipioByCodigoIBGE(final String codigoIbge) {
		Query q = this.entityManager.createQuery("SELECT m FROM Municipio AS m WHERE m.codigoIbge  = :codigoIbge");
		q.setParameter("codigoIbge", codigoIbge);

		try {
			return (Municipio) q.getSingleResult();
		} catch(NoResultException ex) {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<Municipio> findAllByIdEstado(Integer idEstado){
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT o FROM Municipio o WHERE o.estado.idEstado = :idEstado");
		
		Query q = this.getEntityManager().createQuery(sb.toString(), Municipio.class);
		q.setParameter("idEstado", idEstado);
		
		return q.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<MunicipioDTO> findAllByIdEstadoDTO(Integer idEstado){
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT new br.jus.pje.nucleo.dto.MunicipioDTO(o.idMunicipio, o.municipio) ");
		sb.append("FROM Municipio o ");
		sb.append("WHERE o.estado.idEstado = :idEstado");
		
		Query q = this.getEntityManager().createQuery(sb.toString());
		q.setParameter("idEstado", idEstado);
		
		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Municipio> findAllByIdJurisdicao(Integer idJurisidcao){
		StringBuilder sb = new StringBuilder();
		
		sb.append("SELECT o.municipio FROM JurisdicaoMunicipio o WHERE o.jurisdicao.idJurisdicao = :idJurisdicao");
		Query q = this.getEntityManager().createQuery(sb.toString());
		q.setParameter("idJurisdicao", idJurisidcao);
		
		return q.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Municipio> recuperarPorJurisdicao(Integer idJurisidcao) {
		Query query = this.entityManager.createQuery(
				"SELECT o.municipio FROM JurisdicaoMunicipio o WHERE o.jurisdicao.idJurisdicao = :idJurisdicao ORDER BY o.municipio");

		query.setParameter("idJurisdicao", idJurisidcao);
		return query.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Municipio> filtrarMunicipios(String texto, Integer idEstado) {
		if (idEstado == null) {
			return new ArrayList<Municipio>();
		}
		StringBuilder sb = new StringBuilder();
		sb.append("select o from Municipio o ");
		sb.append("where o.estado.idEstado = ");
		sb.append(idEstado);
		sb.append(" and ");
		sb.append("lower(o.municipio) like lower(concat('");
		sb.append(texto);
		sb.append("', '%'))) ");
		sb.append("order by o.municipio");
		Query query = this.entityManager.createQuery(sb.toString());
		return (List<Municipio>)query.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Municipio> recuperarPorEstadoComJurisdicao(Integer idEstado) {
		Query query = this.entityManager.createQuery(
				"SELECT o.municipio FROM JurisdicaoMunicipio o JOIN o.jurisdicao p WHERE p.estado.idEstado = :idEstado ORDER BY o.municipio");

		query.setParameter("idEstado", idEstado);
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Municipio> recuperarPorEstadoComJurisdicaoCompetenciaAtiva(Integer idEstado) {
		Query query = this.entityManager.createQuery(
				"SELECT DISTINCT q.municipio FROM CompetenciaAreaDireito o JOIN o.jurisdicao p JOIN p.municipioList q WHERE p.estado.idEstado = :idEstado ORDER BY q.municipio.municipio");

		query.setParameter("idEstado", idEstado);
		return query.getResultList();
	}

}
