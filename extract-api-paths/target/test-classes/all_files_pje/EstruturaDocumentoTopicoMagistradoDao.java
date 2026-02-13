package br.com.infox.editor.dao;

import javax.persistence.Query;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import br.com.infox.core.dao.GenericDAO;
import br.com.infox.editor.query.EstruturaDocumentoTopicoMagistradoQuery;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.editor.EstruturaDocumentoTopico;
import br.jus.pje.nucleo.entidades.editor.EstruturaDocumentoTopicoMagistrado;

@Name(EstruturaDocumentoTopicoMagistradoDao.NAME)
@AutoCreate
public class EstruturaDocumentoTopicoMagistradoDao extends GenericDAO implements EstruturaDocumentoTopicoMagistradoQuery {

	public static final String NAME = "estruturaDocumentoTopicoMagistradoDao";
	
	public EstruturaDocumentoTopicoMagistrado getEstruturaDocumentoTopicoMagistrado(EstruturaDocumentoTopico estruturaDocumentoTopico, PessoaMagistrado pessoaMagistrado) {
		Query query = entityManager.createQuery(ESTRUTURA_DOCUMENTO_TOPICO_MAGISTRADO_QUERY)
								   .setParameter(ESTRUTURA_DOCUMENTO_TOPICO_PARAM, estruturaDocumentoTopico)
								   .setParameter(PESSOA_MAGISTRADO_PARAM, pessoaMagistrado);
		return EntityUtil.getSingleResult(query);
	}

	public void removerTopicoMagistradoAssociado(EstruturaDocumentoTopico estruturaDocumentoTopico) {
		entityManager.createQuery("delete from EstruturaDocumentoTopicoMagistrado o where o.estruturaDocumentoTopico = :estruturaDocumentoTopico")
					 .setParameter("estruturaDocumentoTopico", estruturaDocumentoTopico)
					 .executeUpdate();
	}

}
