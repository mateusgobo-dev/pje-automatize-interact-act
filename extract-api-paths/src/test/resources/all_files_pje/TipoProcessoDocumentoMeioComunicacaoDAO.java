package br.jus.cnj.pje.business.dao;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.TipoProcessoDocumentoMeioComunicacao;
import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.com.itx.util.EntityUtil;

@Name(TipoProcessoDocumentoMeioComunicacaoDAO.NAME)
public class TipoProcessoDocumentoMeioComunicacaoDAO extends BaseDAO<TipoProcessoDocumentoMeioComunicacao> {
	public final static String NAME = "tipoProcessoDocumentoMeioComunicacaoDAO";
	
	public boolean verificaSeJaTemCadastrado(TipoProcessoDocumento tipoProcessoDocumento, 
			ExpedicaoExpedienteEnum meioComunicacao){
		String sql = "select count(o) from TipoProcessoDocumentoMeioComunicacao o " +
				" where o.tipoProcessoDocumento = :tipoProcessoDocumento " +
				" and o.meioComunicacao = :meioComunicacao";
		Query q = EntityUtil.createQuery(sql);
		q.setParameter("tipoProcessoDocumento", tipoProcessoDocumento);
		q.setParameter("meioComunicacao", meioComunicacao);
		Long numero = (Long) q.getSingleResult();
		return (numero > 0);
	}

	@Override
	public Object getId(TipoProcessoDocumentoMeioComunicacao e) {
		return e.getId();
	}

}
