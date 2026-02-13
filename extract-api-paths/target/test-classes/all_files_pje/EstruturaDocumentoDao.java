package br.com.infox.editor.dao;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import br.com.infox.core.dao.GenericDAO;
import br.com.infox.editor.query.EstruturaDocumentoQuery;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.editor.EstruturaDocumento;

@Name(EstruturaDocumentoDao.NAME)
@AutoCreate
public class EstruturaDocumentoDao extends GenericDAO implements EstruturaDocumentoQuery {
	
	public static final String NAME = "estruturaDocumentoDao";
	
	@SuppressWarnings("unchecked")
	public List<EstruturaDocumento> getEstruturaDocumentoList(TipoProcessoDocumento tipoProcessoDocumento) {
		Query query = entityManager.createQuery(ESTRUTURA_DOCUMENTO_POR_TIPO_LIST_QUERY);
		query.setParameter(TIPO_PROCESSO_DOCUMENTO_PARAM, tipoProcessoDocumento);
		return query.getResultList();
	}


}
