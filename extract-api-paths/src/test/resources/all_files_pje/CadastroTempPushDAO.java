package br.jus.cnj.pje.business.dao;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.CadastroTempPush;

@Name(CadastroTempPushDAO.NAME)
public class CadastroTempPushDAO extends BaseDAO<CadastroTempPush> {
	public static final String NAME = "cadastroTempPushDAO";

	@Override
	public Object getId(CadastroTempPush e) {
		return e.getIdCadastroTempPush();
	}

	/**
	 * Método responsável por recuperar o objeto {@link CadastroTempPush} pelo login.
	 * 
	 * @param login Login.
	 * @return {@link CadastroTempPush}.
	 */
	public CadastroTempPush recuperarCadastroTempPushByLogin(String login){
		Query q = EntityUtil.createQuery("select o from CadastroTempPush o where (o.nrDocumento = :login or o.dsEmail = :login)");
		q.setParameter("login", login);
		return EntityUtil.getSingleResult(q);
	}
	
	/**
	 * Método responsável por recuperar o objeto {@link CadastroTempPush} pelo código do hash.
	 * 
	 * @param cdHash Código do hash.
	 * @return {@link CadastroTempPush}.
	 */
	public CadastroTempPush recuperarCadastroTempPushByHash(String cdHash) {
		Query q = EntityUtil.createQuery("select o from CadastroTempPush o where o.cdHash = :cdHash");
		q.setParameter("cdHash", cdHash);
		return EntityUtil.getSingleResult(q);
	}
	
}
