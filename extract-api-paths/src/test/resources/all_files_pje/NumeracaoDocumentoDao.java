package br.com.infox.editor.dao;

import java.util.List;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import br.com.infox.core.dao.GenericDAO;
import br.com.infox.editor.query.NumeracaoDocumentoQuery;
import br.jus.pje.nucleo.entidades.editor.NumeracaoDocumento;

@Name(NumeracaoDocumentoDao.NAME)
@AutoCreate
public class NumeracaoDocumentoDao extends GenericDAO implements NumeracaoDocumentoQuery {

	public static final String NAME = "numeracaoDocumentoDao";

	@SuppressWarnings("unchecked")
	public List<NumeracaoDocumento> getNumeracaoDocumentoList() {
		return entityManager.createQuery(NUMERACAO_DOCUMENTO_LIST_QUERY).getResultList();
	}

}
