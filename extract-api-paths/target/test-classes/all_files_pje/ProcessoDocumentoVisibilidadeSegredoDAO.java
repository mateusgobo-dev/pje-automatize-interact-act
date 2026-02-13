/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoVisibilidadeSegredo;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.entidades.Usuario;

/**
 * @author cristof
 * 
 */
@Name("processoDocumentoVisibilidadeSegredoDAO")
public class ProcessoDocumentoVisibilidadeSegredoDAO extends BaseDAO<ProcessoDocumentoVisibilidadeSegredo>{

	@Override
	public Integer getId(ProcessoDocumentoVisibilidadeSegredo e){
		return e.getIdProcessoDocumentoVisibilidadeSegredo();
	}
	
	public boolean visivel(ProcessoDocumento pd, Usuario u){
		return visivel(pd, u, null);
	}

	public boolean visivel(ProcessoDocumento pd, Usuario u, Procuradoria procuradoria){
		StringBuilder str = new StringBuilder("SELECT EXISTS (SELECT 1 FROM ProcessoDocumentoVisibilidadeSegredo pdv WHERE pdv.processoDocumento.idProcessoDocumento = :idPd");
		
		if (procuradoria == null) {
			str.append(" AND pdv.pessoa.idUsuario = :idUsuario");
		} else {
			str.append(" AND ((pdv.procuradoria.idProcuradoria = :idProcuradoria");
			str.append(" AND (EXISTS( SELECT 1 FROM PessoaProcuradoria pp WHERE pp.procuradoria = pdv.procuradoria");
			str.append(" AND pp.pessoa.idUsuario = :idUsuario)");
			str.append(" OR EXISTS( SELECT 1 FROM PessoaAssistenteProcuradoriaLocal papl WHERE papl.procuradoria = pdv.procuradoria");
			str.append(" AND papl.usuario.idUsuario = :idUsuario)))");
			str.append(" OR pdv.pessoa.idPessoa = :idUsuario)");
		}
		str.append(") FROM ProcessoDocumentoVisibilidadeSegredo");

		Query q = entityManager.createQuery(str.toString());
		q.setParameter("idPd", pd.getIdProcessoDocumento());
		q.setParameter("idUsuario", u.getIdUsuario());
		if (procuradoria != null) {
			q.setParameter("idProcuradoria", procuradoria.getIdProcuradoria());
		}
		return EntityUtil.getSingleResult(q);
	}

}
