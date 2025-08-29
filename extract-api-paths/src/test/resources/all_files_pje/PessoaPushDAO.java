package br.jus.cnj.pje.business.dao;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.PessoaPush;

@Name(PessoaPushDAO.NAME)
public class PessoaPushDAO extends BaseDAO<PessoaPush> {
	public static final String NAME = "pessoaPushDAO";
	
	/**
	 * Método responsável por recuperar o objeto {@link PessoaPush} pelo login.
	 * 
	 * @param login Login.
	 * @return {@link PessoaPush}.
	 */
	public PessoaPush recuperarPessoaPushByLogin(String login){
		StringBuilder sb = new StringBuilder("SELECT o FROM PessoaPush o ");
		sb.append("WHERE (o.nrDocumento = :login OR o.email = :login)");
		
		Query query = EntityUtil.createQuery(sb.toString());
		query.setParameter("login", login);
		
		return EntityUtil.getSingleResult(query);
	}
	
	/**
	 * Método responsável por recuperar o objeto {@link PessoaPush} pelo código do hash. 
	 * 
	 * @param cdHash Código do hash.
	 * @return {@link PessoaPush}.
	 */
	public PessoaPush recuperarPessoaPushByHash(String cdHash){
		Query q = EntityUtil.createQuery("select a from PessoaPush a where a.nrDocumento = " + 
			"(select b.nrDocumento from CadastroTempPush b where b.cdHash = :cdHash)");
		
		q.setParameter("cdHash", cdHash);
		return EntityUtil.getSingleResult(q);
	}

	@Override
	public Object getId(PessoaPush e) {
		return e.getIdPessoaPush();
	}
}
