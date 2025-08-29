package br.com.infox.editor.dao;

import java.util.List;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import br.com.infox.core.dao.GenericDAO;
import br.com.infox.editor.bean.Estilo;
import br.com.infox.editor.query.CssDocumentoQuery;
import br.jus.pje.nucleo.entidades.editor.CssDocumento;

@Name(CssDocumentoDao.NAME)
@AutoCreate
public class CssDocumentoDao extends GenericDAO implements CssDocumentoQuery {
	
	public static final String NAME = "cssDocumentoDao";
	
	@SuppressWarnings("unchecked")
	public List<CssDocumento> getCssDocumentos() {
		return entityManager.createQuery(CSS_LIST).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Estilo> getEstilos() {
		return entityManager.createQuery(CSS_ESTILOS_LIST).getResultList();
	}
}
