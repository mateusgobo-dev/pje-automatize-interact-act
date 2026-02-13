package br.com.jt.pje.dao;

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.dao.GenericDAO;
import br.com.itx.util.EntityUtil;
import br.jus.pje.jt.entidades.DocumentoVoto;

@Name(DocumentoValidacaoHashDAO.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class DocumentoValidacaoHashDAO extends GenericDAO implements Serializable{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "documentoValidacaoHashDAO";

	public void removerDaTabelaDocumentoValidacaoHash(DocumentoVoto documentoVoto) {
		String hql = "delete from tb_doc_validacao_hash where id_processo_documento = :pd";
		EntityUtil.createNativeQuery(getEntityManager(), hql, "tb_doc_validacao_hash").setParameter("pd", documentoVoto.getIdProcessoDocumento()).executeUpdate();
		EntityUtil.flush(getEntityManager());
	}

}