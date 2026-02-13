package br.com.infox.pje.dao;

import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.home.ProcessoDocumentoHome;
import br.com.infox.pje.query.ProcessoDocumentoTrfLocalQuery;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoTrfLocal;
import br.jus.pje.jt.entidades.DocumentoVoto;
import org.jboss.seam.annotations.Scope;

/**
 * Classe DAO para ProcessoDocumentoTrfLocal.
 * 
 * @author Daniel
 * 
 */
@Name(ProcessoDocumentoTrfLocalDAO.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class ProcessoDocumentoTrfLocalDAO extends BaseDAO<ProcessoDocumentoTrfLocal> implements
		ProcessoDocumentoTrfLocalQuery{
    public static final String NAME = "processoDocumentoTrfLocalDAO";

	@Override
	public Integer getId(ProcessoDocumentoTrfLocal e){
		return e.getIdProcessoDocumentoTrf();
	}

	public ProcessoDocumentoTrfLocal getProcDocTrfByProcDoc(ProcessoDocumento pd){
		Query q = getEntityManager().createQuery(LIST_PD_TRF_BY_PD_QUERY);
		q.setParameter(QUERY_PARAM_PROCESSO_DOCUMENTO, pd.getIdProcessoDocumento());

		ProcessoDocumentoTrfLocal result = EntityUtil.getSingleResult(q);
		return result;
	}
	
	/**
	 * remove um ProcessoDocumentoTrfLocal e o ProcessoDocumento associado ao mesmo.
	 * @param idProcessoDocumento id do documento a ser removido.
	 */
	public void removerProcessoDocumentoTrfEProcessoDocumentoNaoAssinados(Integer idProcessoDocumento){
		ProcessoDocumentoTrfLocal processoDocumentoTrfLocal = EntityUtil.find(ProcessoDocumentoTrfLocal.class, idProcessoDocumento);
		if (processoDocumentoTrfLocal != null){
			//query nativa pois a remoção do processoDocumento já utiliza query nativa e ia dar erro se remover o processoDocumento primeiro.
			String sql = "delete from tb_processo_documento_trf where id_processo_documento_trf = " + idProcessoDocumento;
			Query q = EntityUtil.createNativeQuery(sql, "tb_processo_documento_trf");
			q.executeUpdate();
			ProcessoDocumento pd =	EntityUtil.getEntityManager().find(ProcessoDocumento.class, idProcessoDocumento);
			if (pd != null){
				ProcessoDocumentoHome processoDocumentoHome = (ProcessoDocumentoHome) Component.getInstance(ProcessoDocumentoHome.NAME) ;
				processoDocumentoHome.excluirDocNaoAssinado(pd);
			}
		}

	}
	
	public void removerDaTabelaProcessoDocumentoTrf(DocumentoVoto documentoVoto) {
		String hql = "delete from tb_processo_documento_trf where id_processo_documento_trf = :pd";
		EntityUtil.createNativeQuery(getEntityManager(), hql, "tb_processo_documento_trf").setParameter("pd", documentoVoto.getIdProcessoDocumento()).executeUpdate();
		EntityUtil.flush(getEntityManager());
	}

}