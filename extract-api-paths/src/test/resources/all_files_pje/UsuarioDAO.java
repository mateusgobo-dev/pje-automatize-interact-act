/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.Name;

import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.identidade.Papel;

/**
 * @author cristof
 *
 */
@Name("usuarioDAO")
public class UsuarioDAO extends AbstractUsuarioDAO<Usuario> {
	
	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.business.dao.BaseDAO#getId(java.lang.Object)
	 */
	@Override
	public Object getId(Usuario e) {
		return e.getIdUsuario();
	}

	/**
	 * retorna um usuario que possua um determinado login e hash de ativacao
	 * @param login do usuario
	 * @param hashAtivacaoSenha enviado ao email do usuario
	 * @return
	 */
	public Usuario findUsuarioHashAtivacao(String login, String hashAtivacaoSenha){
		String hql = " select o from "+getEntityClass().getCanonicalName()+" o "+
	                 " where o.login = :login "+
	                 " and o.hashAtivacaoSenha = :hashAtivacaoSenha ";
		Query qry = getEntityManager().createQuery(hql);
		qry.setParameter("login", login);
		qry.setParameter("hashAtivacaoSenha", hashAtivacaoSenha);
		try{
			return (Usuario) qry.getSingleResult();
		} catch (NoResultException e) {
			return null;
		} catch (NonUniqueResultException e) {  
			return null;
		}
	}
	
	/**
	 * Recupera a localização inicial do usuário.
	 * 
	 * @param idUsuario Identificador do usuário.
	 * @return A localização inicial do usuário.
	 */
	public String recuperarLocalizacaoInicial(Integer idUsuario) {
		StringBuilder sb = new StringBuilder("SELECT o.usuarioLocalizacaoInicial FROM UsuarioLogin AS o ");
		sb.append("WHERE o.idUsuario = :idUsuario ");

		Query query = entityManager.createQuery(sb.toString());
		query.setParameter("idUsuario", idUsuario);
		
		String localizacaoInicial = EntityUtil.getSingleResult(query);
		return localizacaoInicial != null ? localizacaoInicial : StringUtils.EMPTY;
	}
	
 	@SuppressWarnings("unchecked")
 	public List<Integer> consultarIdsUsuariosPorPapelHerdado(Papel papel){
 		StringBuilder sb = new StringBuilder();
 		
 		sb.append("SELECT DISTINCT ul.usuario.idUsuario FROM UsuarioLocalizacao ul ");
 		sb.append("INNER JOIN ul.papel as p ");
 		sb.append("WHERE p.idsPapeisInferiores like :idPapel ");
 		
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("idPapel", "%:" + String.valueOf(papel.getIdPapel()) + ":%");
		
		List<Integer> lista = q.getResultList(); 
		
		return CollectionUtilsPje.isEmpty(lista) ? new ArrayList<Integer>(0) : lista;
 	}
 	
 	public Integer marcarFlagAtualizaSSOPorPapelHerdado(Papel papel) {
 		
 		StringBuilder sb = new StringBuilder();
 		
 		sb.append("UPDATE acl.tb_usuario_login usu  ");
 		sb.append("SET in_atualiza_sso = true ");
 		sb.append("FROM ( ");
 		sb.append("    SELECT DISTINCT(ul.id_usuario) FROM core.tb_usuario_localizacao as ul ");
 		sb.append("    INNER JOIN acl.tb_papel as pap ON (ul.id_papel = pap.id_papel) ");
 		sb.append("    WHERE pap.ids_papeis_inferiores like :idPapel ");
 		sb.append(") as usuario ");
 		sb.append("WHERE usu.id_usuario = usuario.id_usuario ");
 		
		Query q = getEntityManager().createNativeQuery(sb.toString());
		q.setParameter("idPapel", "%:" + String.valueOf(papel.getIdPapel()) + ":%");
		
		return q.executeUpdate();
 	}
 	
 	public void marcarFlagAtualizaSSO(Integer idUsuario) {
 		StringBuilder sb = new StringBuilder();
 		
 		sb.append("UPDATE acl.tb_usuario_login usu  ");
 		sb.append("SET in_atualiza_sso = true ");
 		sb.append("WHERE usu.id_usuario = :idUsuario ");
 		
		Query q = getEntityManager().createNativeQuery(sb.toString());
		q.setParameter("idUsuario", idUsuario);
		q.executeUpdate();
 	}
 	
 	/**
 	 * Metodo responsavel por verificar se ha algum usuario ativo, associado ao CPF ou ao CNPJ
 	 * que tambem devera estar ativo e nao usuario falsamente, bem como existir uma localizacao
 	 * vinculada ao usuario.
 	 * @param cpfCnpj
 	 * @return
 	 */
 	public boolean isUsuarioAtivoPje(String cpfCnpj) {
		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT 1 ");
		sb.append(" FROM tb_pess_doc_identificacao doc ");
		sb.append(" INNER JOIN acl.tb_usuario_login ulogin ON doc.id_pessoa = ulogin.id_usuario ");
		sb.append(" WHERE 1=1 ");
		sb.append("     AND doc.in_ativo = true ");
		sb.append("     AND doc.in_usado_falsamente = false ");
		sb.append("     AND doc.cd_tp_documento_identificacao IN ('CPF', 'CPJ') ");
		sb.append("     AND doc.nr_documento_identificacao = :documento ");
		sb.append("     AND ulogin.in_ativo = true ");
		sb.append("     AND exists ( ");
		sb.append("         select 1 from core.tb_usuario_localizacao ul ");
		sb.append("         where ul.id_usuario = doc.id_pessoa ");
		sb.append("     ) ");
		
		Query q = getEntityManager().createNativeQuery(sb.toString());
		q.setParameter("documento", cpfCnpj);
		List<Integer> resultado = q.getResultList();
		
		return (resultado != null && !resultado.isEmpty());
	}
}
