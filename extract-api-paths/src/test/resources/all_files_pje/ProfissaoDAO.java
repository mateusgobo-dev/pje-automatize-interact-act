package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.Util;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Profissao;

@Name(ProfissaoDAO.NAME)
public class ProfissaoDAO extends BaseDAO<Profissao>{

	public static final String NAME = "profissaoDAO";
	protected static final String INPUT_PARAMETER = "input";

	@Override
	public Object getId(Profissao e) {
		return e.getIdProfissao();
	}
	
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from Profissao o ");
		sb.append("where lower(TO_ASCII(o.profissao)) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) order by 1");
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	public List<Profissao> suggestList(String profissao) {
		List<Profissao> result = null;

		Query query = EntityUtil.getEntityManager().createQuery(getEjbql()).setParameter(INPUT_PARAMETER, profissao);	

		
		if (Util.isStringSemCaracterUnicode(profissao.toString())) {
			result = query.getResultList();
		}
		
		return result;
	}
	
}
