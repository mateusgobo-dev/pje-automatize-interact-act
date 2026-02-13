/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.infox.pje.query.PessoaFisicaQuery;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.PessoaFisica;

/**
 * @author cristof
 * 
 */
@Name("pessoaFisicaDAO")
public class PessoaFisicaDAO extends AbstractUsuarioDAO<PessoaFisica> implements PessoaFisicaQuery{

	public PessoaFisica findByCPF(String cpf){
		StringBuilder queryString = new StringBuilder();
        queryString.append("SELECT p FROM PessoaFisica AS p ");
        queryString.append("	INNER JOIN p.pessoaDocumentoIdentificacaoList AS d");
        queryString.append("		WHERE d.tipoDocumento.codTipo = 'CPF'");
        queryString.append("			AND d.numeroDocumento = :cpf");
        queryString.append("			AND p.unificada = false");
		Query q = this.entityManager.createQuery(queryString.toString());
		q.setParameter("cpf", cpf);
		PessoaFisica pessoa = null;
		try{
			pessoa = (PessoaFisica) q.getSingleResult();
			pessoa.getDocumentoCpfCnpj();
		} catch (NonUniqueResultException e){
			throw new IllegalStateException();
		} catch (NoResultException e){
			// Nothing to do.
		}
		return pessoa;
	}
	
	public List<PessoaFisica> findByMultipleCPF(List<String> listaCpf){
		return this.findByMultipleCPF(listaCpf, 15);
	}
	
	@SuppressWarnings("unchecked")
	public List<PessoaFisica> findByMultipleCPF(List<String> listaCpf, Integer maxResults){
		StringBuilder queryString = new StringBuilder();
		List<PessoaFisica> listaPessoas = new ArrayList<PessoaFisica>();
        
		queryString.append("SELECT p FROM PessoaFisica AS p ");
        queryString.append("	INNER JOIN p.pessoaDocumentoIdentificacaoList AS d");
        queryString.append("		WHERE d.tipoDocumento.codTipo = 'CPF'");
        queryString.append("			AND d.numeroDocumento IN (:listaCpf) ");
		
        Query q = this.entityManager.createQuery(queryString.toString());
		q.setParameter("listaCpf", listaCpf);
		q.setMaxResults(15);
		
		listaPessoas = q.getResultList();

		return listaPessoas;
	}	

	@Override
	public Integer getId(PessoaFisica e){
		return e.getIdUsuario();
	}

	public PessoaFisica getPessoaFisicaByNome(String nomePessoa){
		Query q = getEntityManager().createQuery(PESSOA_FISICA_BY_NOME_QUERY);
		q.setParameter(QUERY_PARAMETER_NOME, nomePessoa);

		PessoaFisica singleResult = EntityUtil.getSingleResult(q);
		return singleResult;
	}
		
	@SuppressWarnings("unchecked")
	public List<PessoaFisica> getServidores(OrgaoJulgador orgao) {
		String query = "SELECT DISTINCT pf FROM PessoaFisica AS pf, UsuarioLocalizacaoMagistradoServidor AS ulms" +
				" WHERE ulms.orgaoJulgador = :orgao "+
				" and ulms.usuarioLocalizacao.usuario.idUsuario = pf.idUsuario";
		Query q = entityManager.createQuery(query);
		q.setParameter("orgao", orgao);
		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<PessoaFisica> getServidores(OrgaoJulgadorColegiado orgao){
		String query = "SELECT DISTINCT pf FROM PessoaFisica AS pf, UsuarioLocalizacaoMagistradoServidor AS ulms" +			
				" WHERE ulms.orgaoJulgadorColegiado = :orgao " +
				" and ulms.usuarioLocalizacao.usuario.idUsuario = pf.idUsuario";
		Query q = entityManager.createQuery(query);
		q.setParameter("orgao", orgao);
		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
 	public List<PessoaFisica> recuperaPessoasFisicasPorDataNascimento(Date dataNasc){
 		StringBuilder sb = new StringBuilder();
 		sb.append("SELECT pf FROM PessoaFisica AS pf  ");
 		sb.append("WHERE dataNascimento = :dataNascimento   ");
 
 		Query q = entityManager.createQuery(sb.toString());
 		q.setParameter("dataNascimento", dataNasc);
 		return q.getResultList();
 	}
	
	public String montaQueryProcessoParteNomeSocial(String nome) {
		return "select 1 from ProcessoParte pp, PessoaFisica pf where pp.idPessoa = pf.idPessoa and pp.processoTrf.idProcessoTrf = o.idProcessoTrf and pp.inSituacao = 'A' "
			+ "	and LOWER(to_ascii(pf.nomeSocial)) LIKE LOWER(to_ascii('%" + nome.replace(' ', '%') + "%'))"; 
	}
	
	@SuppressWarnings("unchecked")
	public List<PessoaFisica> findByNomeAndCPF(String cpf, String nome){
		StringBuilder queryString = new StringBuilder();		
        
		queryString.append("SELECT p FROM PessoaFisica AS p ");
		queryString.append(" INNER JOIN p.pessoaDocumentoIdentificacaoList AS d ");
		queryString.append("WHERE d.tipoDocumento.codTipo = 'CPF'");
		
		if(!cpf.isEmpty()) {
			queryString.append(" AND d.numeroDocumento = :cpf ");
		}
		
		if(!nome.isEmpty()) {
			queryString.append(" AND upper(p.nome) LIKE '%' || upper(:nome) || '%' ");			
		}
		
        Query q = this.entityManager.createQuery(queryString.toString());
        if(!cpf.isEmpty()) {
        	q.setParameter("cpf", cpf);        	
        }
        
        if(!nome.isEmpty()) {
        	q.setParameter(QUERY_PARAMETER_NOME, nome);        	
        }
		
		return q.getResultList();	
	}	

	
}