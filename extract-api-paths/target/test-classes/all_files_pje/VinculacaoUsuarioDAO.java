/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.VinculacaoUsuario;
import br.jus.pje.nucleo.enums.TipoVinculacaoUsuarioEnum;

@Name("vinculacaoUsuarioDAO")
public class VinculacaoUsuarioDAO extends BaseDAO<VinculacaoUsuario> {

	@Override
	public Object getId(VinculacaoUsuario e) {
		return e.getIdVinculacaoUsuario();
	}

	/**
 	 * Método que obtém vinculações relacionadas a um dado <code>usuario</code>
	 * @param usuario a ser consultado
	 * @param tipoVinculacaoUsuario filtra pelo tipo de vinculação em questão. 
	 * @return lista de vinculações de usuários
	 */
	public List<VinculacaoUsuario> obterVinculacoesUsuarios(Usuario usuario, TipoVinculacaoUsuarioEnum tipoVinculacaoUsuario) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("select o from VinculacaoUsuario o 			 		");
		sb.append("where o.usuario = :usuario					   		");
		sb.append("and o.tipoVinculacaoUsuario = :tipoVinculacaoUsuario ");

		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("usuario", usuario);
		q.setParameter("tipoVinculacaoUsuario", tipoVinculacaoUsuario);
		
		@SuppressWarnings("unchecked")
		List<VinculacaoUsuario> resultList = q.getResultList();		
		return resultList;
	}
}