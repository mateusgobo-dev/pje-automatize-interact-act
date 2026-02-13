/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import br.com.itx.util.EntityUtil;

import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.Especialidade;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaPerito;
import br.jus.pje.nucleo.entidades.PessoaServidor;

/**
 * Componente de acesso a dados da entidade {@link PessoaPerito}.
 */
@Name("pessoaPeritoDAO")
public class PessoaPeritoDAO extends AbstractPessoaFisicaEspecializadaDAO<PessoaPerito> {
	
	@Override
	public Integer getId(PessoaPerito e) {
		return e.getIdUsuario();
	}
	
	/**
	 * Atribui a uma pessoa física dada um perfil de perito.
	 * 
	 * @param pessoa a pessoa física a quem será atribuído o perfil
	 * @return a {@link PessoaServidor} que foi atribuída à pessoa física.
	 */
	public PessoaPerito especializa(PessoaFisica pessoa){
		if(!entityManager.contains(pessoa)){
			entityManager.persist(pessoa);
		}
		entityManager.flush();
		String query = "INSERT INTO tb_pessoa_perito (id) VALUES (?1)";
		Query q = EntityUtil.createNativeQuery(entityManager, query, "tb_pessoa_perito");
		q.setParameter(1, pessoa.getIdUsuario());
		if(q.executeUpdate() > 0) {
			return entityManager.find(PessoaPerito.class, pessoa.getIdUsuario());
		} else {
			return null;
		}
	}
	
	/**
	 * Suprime de uma pessoa física o perfil de perito
	 * @param pessoa
	 * @return
	 */
	public PessoaPerito desespecializa(PessoaFisica pessoa){
		PessoaPerito per = null;
		per = (PessoaPerito)entityManager.find(PessoaPerito.class, pessoa.getIdPessoa());
		if(per != null){
			per.getPessoa().suprimePessoaEspecializada(per);
			entityManager.flush();
			return per;
		}
		
		return null;
	}	
	
	public List<PessoaPerito> recuperar(Especialidade especialidade, OrgaoJulgador orgaoJulgador) {
		StringBuilder jpql = new StringBuilder("SELECT p FROM PessoaPerito AS p ")
			.append("JOIN p.pessoaPeritoEspecialidadeList AS e ")
			.append("JOIN p.orgaoJulgadorPessoaPeritoList AS o ")
			.append("WHERE e.especialidade.idEspecialidade = :idEspecialidade ")
			.append("AND o.orgaoJulgador.idOrgaoJulgador = :idOrgaoJulgador");
				
		return entityManager.createQuery(jpql.toString(), PessoaPerito.class)
			.setParameter("idEspecialidade", especialidade.getIdEspecialidade())
			.setParameter("idOrgaoJulgador", orgaoJulgador.getIdOrgaoJulgador())
			.getResultList();
	}
	
	public List<PessoaPerito> recuperarAtivos(Integer idEspecialidade, Set<Integer> idsOrgaoJulgador) {
		StringBuilder jpql = new StringBuilder("SELECT p FROM PessoaPerito AS p ")
			.append("JOIN p.pessoaPeritoEspecialidadeList AS e JOIN p.orgaoJulgadorPessoaPeritoList AS o ")
			.append("WHERE p.ativo = true AND e.especialidade.idEspecialidade = :idEspecialidade ")
			.append("AND o.orgaoJulgador.idOrgaoJulgador IN (:idsOrgaoJulgador) ORDER BY p.nome");
			
		return getEntityManager().createQuery(jpql.toString(), PessoaPerito.class)
			.setParameter("idEspecialidade", idEspecialidade)
			.setParameter("idsOrgaoJulgador", idsOrgaoJulgador)
			.getResultList();
	}

}
