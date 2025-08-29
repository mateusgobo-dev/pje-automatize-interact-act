/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ProjetoUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.Usuario;

/**
 * @author cristof
 * 
 */
@Name("enderecoDAO")
public class EnderecoDAO extends BaseDAO<Endereco>{

	@Override
	public Integer getId(Endereco e){
		return e.getIdEndereco();
	}

	/**
	 * Recupera o endereço mais recente cadastrado para uma dada pessoa.
	 * 
	 * @param p a pessoa cujo endereço se pretende recuperar.
	 * @return o endereço com data de atualização mais recente.
	 */
	@SuppressWarnings("unchecked")
	public Endereco recuperaEnderecoRecente(Pessoa p) {
		String query = "SELECT e FROM Endereco AS e " +
				"	WHERE e.usuario = :usuario" +
				"		ORDER BY e.dataAlteracao DESC";
		Query q = entityManager.createQuery(query);
		q.setParameter("usuario", (Usuario) p);
		q.setMaxResults(1);
		Endereco endereco = null;
		List<Endereco> enderecoList = q.getResultList();
		
		if(enderecoList  != null && !enderecoList.isEmpty()){
			endereco = enderecoList.get(0);
		}
		return endereco;
	}

	/**
	 * Retorna o endereço para o usuário passado por parâmetro.
	 * A consulta do endereço será feita pelos atributos abaixo:
	 * - usuário;
	 * - número cep;
	 * - nome do bairro;
	 * - logradouro; e
	 * - número.
	 * 
	 * @param usuario Usuário que possui o endereço associado.
	 * @param endereco Endereço consultado.
	 * @return Endereco persistente.
	 */
	@SuppressWarnings("unchecked")
	public Endereco obter(Usuario usuario, Endereco endereco) {
		StringBuilder hql = new StringBuilder();
		hql.append("select e ");
		hql.append("from  ");
		hql.append("	Endereco e left join e.cep c ");
		hql.append("where ");
		hql.append("	c.numeroCep = :numeroCep and ");
		hql.append("	e.usuario = :usuario and ");
		
		
		if(endereco.getNomeBairro() != null)
			hql.append("	e.nomeBairro = :nomeBairro and ");
		else
			hql.append("	e.nomeBairro IS NULL and ");
		
		
		if(endereco.getNomeLogradouro() != null)
			hql.append("	e.nomeLogradouro = :nomeLogradouro and ");
		else 
			hql.append("	e.nomeLogradouro IS NULL and ");
		
		
		if(endereco.getNumeroEndereco() != null)
			hql.append("	e.numeroEndereco = :numeroEndereco ");			
		else
			hql.append("	e.numeroEndereco IS NULL ");
		
		hql.append("	order by e.idEndereco DESC ");
		
		Query query = EntityUtil.getEntityManager().createQuery(hql.toString());
		query.setParameter("numeroCep", endereco.getCep().getNumeroCep());
		query.setParameter("usuario", usuario);
		
		if(endereco.getNomeBairro() != null)
			query.setParameter("nomeBairro", endereco.getNomeBairro());
		
		if(endereco.getNomeLogradouro() != null)
			query.setParameter("nomeLogradouro", endereco.getNomeLogradouro());
		
		if(endereco.getNumeroEndereco() != null)
			query.setParameter("numeroEndereco", endereco.getNumeroEndereco());
		
		List<Endereco> enderecos = query.getResultList();
		
		return (ProjetoUtil.getTamanho(enderecos) > 0 ? enderecos.get(0) : null);
	}
	
	/**
 	 * metodo responsavel por persistir o objeto passado em parametro.
 	 * faz uma verificacao pelo objeto na sessao e caso nao exista, persiste. se existir, realiza update.
 	 * @param endereco
 	 * @throws Exception 
 	 */
 	public void salvarEndereco(Endereco endereco) throws Exception {
 		EntityManager em = EntityUtil.getEntityManager();
 		if(em.contains(endereco)) {
 			em.merge(endereco);
 		} else {
 			em.persist(endereco);
 		}
 		em.flush();
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<Endereco> findEnderecosByIdPessoa(Integer idPessoa){
 		StringBuilder sb = new StringBuilder("");
 			
 		sb.append("SELECT e FROM Endereco e ");
 		sb.append("WHERE e.usuario.idUsuario = :idUsuario ");
 		sb.append("ORDER BY e.dataAlteracao DESC ");
 		
 		Query q = this.getEntityManager().createQuery(sb.toString());
 		q.setParameter("idUsuario", idPessoa);

 		List<Endereco> enderecoList = new ArrayList<Endereco>();
 		enderecoList = q.getResultList();
 		
 		return enderecoList;
 	}
 	
 	/**
 	 * Metodo responsavel por buscar os ids dos enderecos de uma pessoa.
 	 * Caso o endereco esteja repetido (com base no endereco completo), 
 	 * o endereco mais antigo sera retornado.
 	 * 
 	 * @param idPessoa
 	 * @param cepFiltro
 	 * @param enderecoCompletoFiltro
 	 * @return
 	 */
 	public List<Integer> getIdsEnderecosUnicosPeloEnderecoCompleto(Integer idPessoa, String cepFiltro, String enderecoCompletoFiltro){
 		StringBuilder sb = new StringBuilder("");
 		
 		sb.append("select a.id_endereco ");
 		sb.append("from ");
 		sb.append("core.tb_endereco a ");
 		sb.append("inner join ( ");
 		sb.append("  select  ");
 		sb.append("  min(e.id_endereco) as min_id_endereco, ");
 		sb.append("  ( ");
 		sb.append("     concat(  ");
 		sb.append("         case when (e.nm_logradouro is null or length(trim(e.nm_logradouro)) = 0) then '' else e.nm_logradouro end ,  ");
 		sb.append("         case when (e.nr_endereco is null or length(trim(e.nr_endereco)) = 0) then '' else ', ' || e.nr_endereco end ,  ");
 		sb.append("         case when (e.ds_complemento is null or length(trim(e.ds_complemento)) = 0) then '' else ', ' || e.ds_complemento end ,  ");
 		sb.append("         case when (e.nm_bairro is null or length(trim(e.nm_bairro)) = 0) then '' else ', ' || e.nm_bairro end ,  ");
 		sb.append("         case when c.id_cep is not null   ");
 		sb.append("         then   ");
 		sb.append("              concat (  ");
 		sb.append("                  case when m.id_municipio is not null   ");
 		sb.append("                  then  ");
 		sb.append("                       coalesce(', ' || m.ds_municipio , '') ||  ");
 		sb.append("                       case when te.id_estado is not null   ");
 		sb.append("                       then  ");
 		sb.append("                          coalesce(' - ' || te.cd_estado , '')  ");
 		sb.append("                       else   ");
 		sb.append("                          ''  ");
 		sb.append("                       end  ");
 		sb.append("                  else  ");
 		sb.append("                      ''  ");
 		sb.append("                  end  ");
 		sb.append("                  ,  ");
 		sb.append("                  '- CEP: ' || c.nr_cep   ");
 		sb.append("              )  ");
 		sb.append("         else   ");
 		sb.append("              ''   ");
 		sb.append("         end  ");
 		sb.append("         )  ");
 		sb.append("    ) as endereco_completo ");
 		sb.append("  from core.tb_endereco e  ");
 		sb.append("  left join core.tb_cep c on c.id_cep = e.id_cep  ");
 		sb.append("  left join core.tb_municipio m on m.id_municipio = c.id_municipio  ");
 		sb.append("  left join core.tb_estado te on te.id_estado = m.id_estado  ");
 		sb.append("  where 1=1 ");
 		sb.append("     and id_usuario  = :idPessoa ");
 		
 		if(cepFiltro != null) {
 			sb.append("     and c.nr_cep = :cep ");
 		}
 		
 		sb.append("  group by endereco_completo ");
 		sb.append(" ) as end_diferentes  ");
 		sb.append("   on  end_diferentes.min_id_endereco = a.id_endereco  ");
 		sb.append("where 1=1 ");

 		if(enderecoCompletoFiltro != null) {
 			sb.append("  and  to_ascii(end_diferentes.endereco_completo) ilike '%' || to_ascii( :enderecoCompleto ) || '%' ");
 		}
 		
 		sb.append("order by a.dt_alteracao_endereco desc ");
 		
 		Query q = this.getEntityManager().createNativeQuery(sb.toString());
 		q.setParameter("idPessoa", idPessoa);
 		
 		if(cepFiltro != null) {
 			q.setParameter("cep", cepFiltro);
 		}
 		
 		if(enderecoCompletoFiltro != null) {
 			q.setParameter("enderecoCompleto", enderecoCompletoFiltro);
 		}
 		
 		return q.getResultList();
 	}

 	/**
 	 * Retorna a quantidade de enderecos de um usuario
 	 * @param idUsuario
 	 * @return
 	 */
 	public Long retornarQuantidadeEnderecosPorUsuario(Integer idUsuario) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(id_endereco) from core.tb_endereco where id_usuario = :id");

		Query q = EntityUtil.getEntityManager().createNativeQuery(sb.toString());
		q.setParameter("id", idUsuario);
		return ((BigInteger) q.getSingleResult()).longValue(); 
	}


}
