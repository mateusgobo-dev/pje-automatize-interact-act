package br.com.jt.pje.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.dao.GenericDAO;
import br.com.itx.util.EntityUtil;
import br.jus.pje.jt.entidades.AssistenteAdmissibilidadeRecurso;


@Name(AssistenteAdmissiblidadeRecursoDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class AssistenteAdmissiblidadeRecursoDAO extends GenericDAO implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String NAME = "assistenteAdmissiblidadeRecursoDAO";

  	public Integer getMaxPrioridadeBy(Integer idProcesso) {
  		StringBuffer sb = new StringBuffer();
  		sb.append("select cast(max(aar.prioridade) as integer) from AssistenteAdmissibilidadeRecurso aar ");
  		sb.append("inner join aar.processoDocumento pd ");
  		sb.append("where pd.processo.idProcesso = :idProcesso");
  		
  		Query q = getEntityManager().createQuery(sb.toString());
  		q.setParameter("idProcesso", idProcesso);
  		
  		Integer qtd = (Integer) q.getSingleResult();
  		return qtd != null ? qtd : 0;
  	}
  	
  	@SuppressWarnings("unchecked")
	public List<AssistenteAdmissibilidadeRecurso> getRecursosBy(Integer idAssistenteAdmissibilidade){
  		StringBuffer sb = new StringBuffer();
		sb.append("select o from AssistenteAdmissibilidadeRecurso o ");
		sb.append(" where o.assistenteAdmissibilidade.idAssistenteAdmissibilidade = :idAssistenteAdmissibilidade ");
		sb.append(" order by o.prioridade ");
		
		Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
		q.setParameter("idAssistenteAdmissibilidade", idAssistenteAdmissibilidade);
		
		List<AssistenteAdmissibilidadeRecurso> listaRecursos = (List<AssistenteAdmissibilidadeRecurso>) q.getResultList();
		return listaRecursos;
  	} 
}
