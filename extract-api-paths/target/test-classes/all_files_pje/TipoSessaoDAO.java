package br.com.jt.pje.dao;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Query;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import br.com.infox.core.dao.GenericDAO;
import br.com.jt.pje.query.TipoSessaoQuery;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.TipoSessaoDTO;
import br.jus.pje.nucleo.entidades.TipoSessao;

@Name(TipoSessaoDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class TipoSessaoDAO extends GenericDAO implements TipoSessaoQuery, Serializable{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "tipoSessaoDAO";

	@SuppressWarnings("unchecked")
	public List<TipoSessao> getTipoSessaoItems(){
		Query q = getEntityManager().createQuery(TIPO_SESSAO_ITEMS_QUERY);
		List<TipoSessao> list = q.getResultList();
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public List<TipoSessaoDTO> getTipoSessaoDTOItems(){
		StringBuilder sb = new StringBuilder("");
		
		sb.append("SELECT new br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.TipoSessaoDTO(o.idTipoSessao, o.tipoSessao, o.ativo) ");
		sb.append(" from TipoSessao o WHERE o.ativo = true ORDER BY o.tipoSessao ");
		
		Query q = this.getEntityManager().createQuery(sb.toString());
		
		return q.getResultList();
	}

}