/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.GrupoOficialJustica;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaOficialJustica;
import br.jus.pje.nucleo.entidades.PessoaServidor;

/**
 * Componente de acesso a dados da entidade {@link PessoaOficialJustica}.
 * 
 * @author cristof
 *
 */
@Name("pessoaOficialJusticaDAO")
public class PessoaOficialJusticaDAO extends AbstractPessoaFisicaEspecializadaDAO<PessoaOficialJustica> {
	
	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.business.dao.BaseDAO#getId(java.lang.Object)
	 */
	@Override
	public Integer getId(PessoaOficialJustica e) {
		return e.getIdUsuario();
	}
	
	/**
	 * Atribui a uma pessoa física dada um perfil de oficial de justiça.
	 * 
	 * @param pessoa a pessoa física a quem será atribuído o perfil
	 * @return a {@link PessoaServidor} que foi atribuída à pessoa física.
	 */
	public PessoaOficialJustica especializa(PessoaFisica pessoa){
		if(!entityManager.contains(pessoa)){
			entityManager.persist(pessoa);
		}
		entityManager.flush();
		String query = "INSERT INTO tb_pessoa_oficial_justica (id) VALUES (?1)";
		Query q = EntityUtil.createNativeQuery(entityManager, query, "tb_pessoa_oficial_justica");
		q.setParameter(1, pessoa.getIdUsuario());
		if(q.executeUpdate() > 0) {
			return entityManager.find(PessoaOficialJustica.class, pessoa.getIdUsuario());
		} else {
			return null;
		}
	}
	
	/**
	 * Suprime de uma pessoa física o perfil de oficial de justiça
	 * @param pessoa
	 * @return
	 */
	public PessoaOficialJustica desespecializa(PessoaFisica pessoa){
		PessoaOficialJustica ofj = null;
		ofj = (PessoaOficialJustica)entityManager.find(PessoaOficialJustica.class, pessoa.getIdPessoa());
		if(ofj != null){
			ofj.getPessoa().suprimePessoaEspecializada(ofj);
			entityManager.flush();
			return ofj;
		}
		
		return null;
	}	

	@SuppressWarnings("unchecked")
	public List<PessoaOficialJustica> listPessoaOficialJusticaByGrupoOficialJustica(GrupoOficialJustica grupoOficialJustica){
		List<PessoaOficialJustica> result = new ArrayList<PessoaOficialJustica>(0);
		StringBuilder sb = new StringBuilder();

		sb.append("select ofj.id, ul.ds_nome, ul.in_ativo ");
		sb.append("from tb_pessoa_oficial_justica ofj ");
		sb.append("inner join tb_usuario_login ul on ul.id_usuario = ofj.id ");
		sb.append("where exists ");
		sb.append("( ");
		sb.append("  select 1 from tb_pess_gpo_oficial_jstica pgoj ");
		sb.append("  inner join tb_grupo_oficial_justica goj on goj.id_grupo_oficial_justica = pgoj.id_grupo_oficial_justica ");
		sb.append("  inner join tb_central_mandado cm on cm.id_central_mandado = goj.id_central_mandado ");
		sb.append("  inner join tb_central_mandado_localiz cml on cml.id_central_mandado = cm.id_central_mandado ");
		sb.append("  where id_pessoa = ul.id_usuario ");
		sb.append("  and cml.id_localizacao = :idLocalizacaoUsuario ");
		if (grupoOficialJustica != null) {
			sb.append(" and goj.id_grupo_oficial_justica = :grupoOficial ");
		}
		sb.append(") ");
		sb.append(" order by ul.ds_nome ");
		
		Query query = getEntityManager().createNativeQuery(sb.toString());
		query.setParameter("idLocalizacaoUsuario", Authenticator.getIdLocalizacaoAtual());
		
		if (grupoOficialJustica != null) {
			query.setParameter("grupoOficial", grupoOficialJustica);
		}
		
		List<Object[]> objectList = query.getResultList();

		for (Object[] obj : objectList){
			PessoaOficialJustica of = new PessoaOficialJustica();
			PessoaFisica pf = new PessoaFisica();
			pf.setIdPessoa((Integer)obj[0]);
			pf.setNome((String)obj[1]);
			pf.setAtivo((Boolean)obj[2]);
			of.setPessoa(pf);
			of.setIdUsuario((Integer)obj[0]);
			of.setNome((String)obj[1]);
			of.setAtivo((Boolean)obj[2]);
			result.add(of);
		}

		return result;
	}
	
	
}
