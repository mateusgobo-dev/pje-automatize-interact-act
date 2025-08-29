package br.com.jt.pje.dao;

import java.io.Serializable;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.dao.GenericDAO;
import br.com.itx.util.EntityUtil;
import br.jus.pje.jt.entidades.AssistenteAdmissibilidade;


@Name(AssistenteAdmissiblidadeDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class AssistenteAdmissiblidadeDAO extends GenericDAO implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String NAME = "assistenteAdmissiblidadeDAO";

  	public AssistenteAdmissibilidade getUltimoAssistenteBy(Integer idProcesso) {
  		StringBuffer sb = new StringBuffer();
		sb.append("select o from AssistenteAdmissibilidade o ");
		sb.append(" where o.idAssistenteAdmissibilidade = (select max(o2.idAssistenteAdmissibilidade) from AssistenteAdmissibilidade o2 ");
		sb.append("   									    where exists (select 1 from ProcessoDocumento pd ");
		sb.append("														   where pd.idProcessoDocumento = o2.idDocumentoRecorrido ");
		sb.append("															 and pd.processo.idProcesso = :idProcesso ))");
		Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
		q.setParameter("idProcesso", idProcesso);
		
		AssistenteAdmissibilidade assistenteAdmissibilidade = EntityUtil.getSingleResult(q);
  		return assistenteAdmissibilidade;
  	}
}
