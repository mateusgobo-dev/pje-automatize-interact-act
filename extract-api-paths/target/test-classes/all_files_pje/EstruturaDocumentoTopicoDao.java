package br.com.infox.editor.dao;

import java.util.List;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import br.com.infox.core.dao.GenericDAO;
import br.com.infox.editor.query.EstruturaDocumentoTopicoQuery;
import br.jus.pje.nucleo.entidades.editor.EstruturaDocumento;
import br.jus.pje.nucleo.entidades.editor.EstruturaDocumentoTopico;

@Name(EstruturaDocumentoTopicoDao.NAME)
@AutoCreate
public class EstruturaDocumentoTopicoDao extends GenericDAO implements EstruturaDocumentoTopicoQuery {

	public static final String NAME = "estruturaDocumentoTopicoDao";
	
	public boolean temProcessoDocumentoAssociado(EstruturaDocumentoTopico estruturaDocumentoTopico) {
		Long count = (Long) entityManager.createQuery(COUNT_PROCESSO_DOCUMENTO_POR_EST_DOC_TOPICO_QUERY)
											 .setParameter(ID_ESTRUTURA_DOCUMENTO_TOPICO_PARAM, estruturaDocumentoTopico.getIdEstruturaDocumentoTopico())
											 .getSingleResult();
		return  count > 0;
	}

	@SuppressWarnings("unchecked")
	public List<EstruturaDocumentoTopico> getEstruturaDocumentoTopicoList(EstruturaDocumento estruturaDocumento) {
		return entityManager.createQuery(ESTRUTURA_DOCUMENTO_TOPICO_LIST)
							.setParameter(ESTRUTURA_DOCUMENTO_PARAM, estruturaDocumento)
							.getResultList();
	}
	
}
