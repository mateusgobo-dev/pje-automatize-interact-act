package br.com.jt.pje.dao;

import br.com.infox.core.dao.GenericDAO;

import br.com.itx.util.EntityUtil;

import br.com.jt.pje.query.DocumentoVotoQuery;
import br.jus.pje.jt.entidades.DocumentoVoto;
import br.jus.pje.jt.entidades.Voto;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;


@Name(DocumentoVotoDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class DocumentoVotoDAO extends GenericDAO implements DocumentoVotoQuery,
    Serializable {
    private static final long serialVersionUID = 1L;
    public static final String NAME = "documentoVotoDAO";

    @SuppressWarnings("unchecked")
    public List<DocumentoVoto> getDocumentoVotoByVoto(Voto voto) {
        Query q = getEntityManager()
                      .createQuery(DOCUMENTO_VOTO_BY_VOTO_QUERY);
        q.setParameter(QUERY_PARAMETER_VOTO, voto);

        return q.getResultList();
    }

    public DocumentoVoto getDocumentoVotoByVotoETipo(Voto voto,
        TipoProcessoDocumento tipoProcessoDocumento) {
        Query q = getEntityManager()
                      .createQuery(DOCUMENTO_VOTO_BY_VOTO_E_TIPO_QUERY);
        q.setParameter(QUERY_PARAMETER_TIPO_PROCESSO_DOCUMENTO,
            tipoProcessoDocumento);
        q.setParameter(QUERY_PARAMETER_VOTO, voto);

        DocumentoVoto result = EntityUtil.getSingleResult(q);

        return result;
    }

    public DocumentoVoto getUltimoDocumentoVotoAssinadoByProcessoTipoProcessoDocumento(
        TipoProcessoDocumento tipoProcessoDocumento, Processo processo,
        Voto voto) {
        Query q = getEntityManager()
                      .createQuery(LIST_ULTIMO_DOCUMENTO_VOTO_ASSINADO_BY_TIPO_QUERY);
        q.setParameter(QUERY_PARAMETER_TIPO_PROCESSO_DOCUMENTO,
            tipoProcessoDocumento);
        q.setParameter(QUERY_PARAMETER_PROCESSO, processo);
        q.setParameter(QUERY_PARAMETER_VOTO, voto);

        DocumentoVoto result = EntityUtil.getSingleResult(q);

        return result;
    }

    public DocumentoVoto getUltimoDocumentoVotoByProcessoTipoProcessoDocumento(
        TipoProcessoDocumento tipoProcessoDocumento, Processo processo,
        Voto voto) {
        Query q = getEntityManager()
                      .createQuery(LIST_ULTIMO_DOCUMENTO_VOTO_BY_TIPO_QUERY);
        q.setParameter(QUERY_PARAMETER_TIPO_PROCESSO_DOCUMENTO,
            tipoProcessoDocumento);
        q.setParameter(QUERY_PARAMETER_PROCESSO, processo);
        q.setParameter(QUERY_PARAMETER_VOTO, voto);

        DocumentoVoto result = EntityUtil.getSingleResult(q);

        return result;
    }
    
    public void removerDaTabelaDocumentoVoto(DocumentoVoto documentoVoto) {
		Query q = EntityUtil.createNativeQuery(getEntityManager(), "delete from " + DocumentoVoto.TABLE_NAME + " where id_documento_voto = " + documentoVoto.getIdProcessoDocumento(), DocumentoVoto.TABLE_NAME);
		q.executeUpdate();
	}
}
