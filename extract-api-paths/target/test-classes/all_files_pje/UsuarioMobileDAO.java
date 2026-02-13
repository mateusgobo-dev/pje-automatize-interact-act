package br.jus.cnj.pje.business.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.dao.GenericDAO;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioMobile;
import br.jus.pje.nucleo.enums.PlataformaDispositivoEnum;

@Name(value = UsuarioMobileDAO.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class UsuarioMobileDAO extends GenericDAO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String NAME = "usuarioMobileDAO";
	
	@SuppressWarnings("unchecked")
	public List<UsuarioMobile> listaUsuarioMobile (Usuario usuario) {
		StringBuilder sql = new StringBuilder();
		
		sql.append(" SELECT o FROM UsuarioMobile o ");
		sql.append("  WHERE o.usuario = :usuario   ");
		sql.append("    AND o.ativo = true ");
		sql.append("    AND o.pareamentoRealizado = true order by o.id desc ");

		Query q = EntityUtil.createQuery(sql.toString());
		q.setParameter("usuario", usuario);
		return q.getResultList();
	}

	public UsuarioMobile recuperarUsuarioMobile(String cpf, String email, String codPareamento) {
		
		StringBuilder sql = new StringBuilder();
		
		sql.append(" SELECT o from UsuarioMobile o ");
		sql.append(" WHERE o.usuario.login = :cpf ");
		if ( StringUtils.isNotEmpty(email) ) 
			sql.append(" AND  o.usuario.email = :email ");
		if ( StringUtils.isNotEmpty(codPareamento) ) 
			sql.append(" AND  o.codigoPareamento = :codPareamento ");
		
		
		Query q = EntityUtil.getEntityManager().createQuery(sql.toString());
		q.setParameter("cpf", cpf.replaceAll("[\\.-]", ""));
		if ( StringUtils.isNotEmpty(email) ) 
			q.setParameter("email", email);
		if ( StringUtils.isNotEmpty(codPareamento) ) 
			q.setParameter("codPareamento", codPareamento);
		
		return EntityUtil.getSingleResult(q);
	}
	
	public boolean checkCodigoPareamento(String codigoPareamento) {

		StringBuilder sql = new StringBuilder();
		
		sql.append(" SELECT count(o) from UsuarioMobile o ");
		sql.append(" WHERE  o.codigoPareamento = :codigoPareamento ");
		
		Query q = EntityUtil.getEntityManager().createQuery(sql.toString());
		q.setParameter("codigoPareamento", codigoPareamento);
		Long count =  (Long) q.getSingleResult();
		return count > 0;
	}

	public void inativarUsuarioMobile(UsuarioMobile usuarioMobile) {

		StringBuilder sql = new StringBuilder();
		
		sql.append(" update UsuarioMobile o set o.ativo = false");
		sql.append(" WHERE  o.idUsuarioMobile = :idUsuarioMobile ");
		
		Query q = EntityUtil.getEntityManager().createQuery(sql.toString());
		q.setParameter("idUsuarioMobile", usuarioMobile.getIdUsuarioMobile());
		q.executeUpdate();
	}

	public boolean usuarioMobilePareado(UsuarioMobile usuarioMobile) {

		StringBuilder sql = new StringBuilder();
		
		sql.append(" SELECT o from UsuarioMobile o ");
		sql.append(" WHERE  o = :usuarioMobile ");
		sql.append(" and  o.pareamentoRealizado is true ");
		
		Query q = EntityUtil.getEntityManager().createQuery(sql.toString());
		q.setParameter("usuarioMobile", usuarioMobile);
		q.setMaxResults(1);
		try {
			UsuarioMobile u = (UsuarioMobile) q.getSingleResult();
			return u != null;
		} catch (NoResultException e) {
			return false;
		} catch (NonUniqueResultException e) {
			throw new IllegalStateException(e);
		}
	}

	public void parearDispositivo(PlataformaDispositivoEnum plataforma, UsuarioMobile usuarioMobile, String versaoPlataforma, String nomeDispositivo) {
		
		StringBuilder sql = new StringBuilder();
		
		sql.append(" UPDATE UsuarioMobile o ");
		sql.append(" SET o.plataforma = :plataforma, ");
		sql.append(" o.versaoPlataforma = :versaoPlataforma, ");
		sql.append(" o.nomeDispositivo = :nomeDispositivo, ");
		sql.append(" o.pareamentoRealizado = true ");
		sql.append(" WHERE o.idUsuarioMobile = :idUsuarioMobile ");
		
		Query q = EntityUtil.getEntityManager().createQuery(sql.toString());
		q.setParameter("plataforma", plataforma);
		q.setParameter("versaoPlataforma", versaoPlataforma);
		q.setParameter("nomeDispositivo", nomeDispositivo);
		q.setParameter("idUsuarioMobile", usuarioMobile.getIdUsuarioMobile());
		q.executeUpdate();

	}

	public UsuarioMobile getUsuarioMobileParaPareamento(String codigoPareamento, String cpf, String email) {
		
		StringBuilder sql = new StringBuilder();
		
		sql.append(" SELECT o from UsuarioMobile o ");
		sql.append(" WHERE  o.codigoPareamento = :codigoPareamento ");
		sql.append(" AND  o.usuario.login = :cpf ");
		sql.append(" AND  o.usuario.email = :email ");
		sql.append(" AND  o.ativo is true ");
		sql.append(" AND  o.pareamentoRealizado is false ");
		
		Query q = EntityUtil.getEntityManager().createQuery(sql.toString());
		q.setParameter("codigoPareamento", codigoPareamento);
		q.setParameter("cpf", cpf.replaceAll("[\\.-]", ""));
		q.setParameter("email", email);
		
		return EntityUtil.getSingleResult(q);
	}
	
	public UsuarioMobile getUsuarioMobilePareado(String codigoPareamento) {
		
		StringBuilder sql = new StringBuilder();
		
		sql.append(" SELECT o from UsuarioMobile o ");
		sql.append(" WHERE  o.codigoPareamento = :codigoPareamento ");
		sql.append(" AND  o.ativo is true ");
		sql.append(" AND  o.pareamentoRealizado is true ");
		
		Query q = EntityUtil.getEntityManager().createQuery(sql.toString());
		q.setParameter("codigoPareamento", codigoPareamento);
		
		return EntityUtil.getSingleResult(q);
	}
	

}
