package br.jus.cnj.pje.business.dao;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumentoPapel;
import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.enums.ExigibilidadeAssinaturaEnum;

@Name("tipoProcessoDocumentoPapelDAO")
public class TipoProcessoDocumentoPapelDAO extends BaseDAO<TipoProcessoDocumentoPapel>{

	public Boolean verificarExigibilidadeNaoAssina(Papel papel, TipoProcessoDocumento tipoProcessoDocumento){
		StringBuilder queryStr = new StringBuilder();
		queryStr.append("SELECT tpProcDocPapel FROM TipoProcessoDocumentoPapel tpProcDocPapel ");
		queryStr.append("WHERE tpProcDocPapel.tipoProcessoDocumento = :tipoProcessoDocumento ");
		queryStr.append("AND tpProcDocPapel.papel = :papel ");
		queryStr.append("AND tpProcDocPapel.exigibilidade in (:exigibilidade) ");
		
		Query query = this.entityManager.createQuery(queryStr.toString());
		query.setParameter("tipoProcessoDocumento", tipoProcessoDocumento);
		query.setParameter("papel", papel);
		query.setParameter("exigibilidade", ExigibilidadeAssinaturaEnum.getListaNaoPermiteAssinar());
		
		TipoProcessoDocumentoPapel tpProcessoDocumentoPapel = null;
		Boolean retorno = Boolean.TRUE;
		try{
			tpProcessoDocumentoPapel = (TipoProcessoDocumentoPapel) query.getSingleResult();
			if (tpProcessoDocumentoPapel != null){
				retorno = Boolean.TRUE;
			}
		} catch (NoResultException e){
			retorno = Boolean.FALSE;
		} catch (NonUniqueResultException e){
			throw new IllegalStateException("Há mais de um TipoProcessoDocumentoPapel com os parametros "
					+ " tipoProcessoDocumento [" + tipoProcessoDocumento.getTipoProcessoDocumento() + "] "
					+ " e papel ["+papel.getNome()+"]");
		}
		return retorno;
	}

	public Boolean verificarExigibilidadeAssina(Papel papel, TipoProcessoDocumento tipoProcessoDocumento){
		StringBuilder queryStr = new StringBuilder();
		queryStr.append("SELECT tpProcDocPapel FROM TipoProcessoDocumentoPapel tpProcDocPapel ");
		queryStr.append("WHERE tpProcDocPapel.tipoProcessoDocumento = :tipoProcessoDocumento ");
		queryStr.append("AND tpProcDocPapel.papel = :papel ");
		queryStr.append("AND tpProcDocPapel.exigibilidade in (:exigibilidade) ");
		
		Query query = this.entityManager.createQuery(queryStr.toString());
		query.setParameter("tipoProcessoDocumento", tipoProcessoDocumento);
		query.setParameter("papel", papel);
		query.setParameter("exigibilidade", ExigibilidadeAssinaturaEnum.getListaPermiteAssinar());
		
		TipoProcessoDocumentoPapel tpProcessoDocumentoPapel = null;
		Boolean retorno = Boolean.TRUE;
		try{
			tpProcessoDocumentoPapel = (TipoProcessoDocumentoPapel) query.getSingleResult();
			if (tpProcessoDocumentoPapel != null){
				retorno = Boolean.TRUE;
			}
		} catch (NoResultException e){
			retorno = Boolean.FALSE;
		} catch (NonUniqueResultException e){
			throw new IllegalStateException("Há mais de um TipoProcessoDocumentoPapel com os parametros "
					+ " tipoProcessoDocumento [" + tipoProcessoDocumento.getTipoProcessoDocumento() + "] "
					+ " e papel ["+papel.getNome()+"]");
		}
		return retorno;
	}

	@Override
	public Object getId(TipoProcessoDocumentoPapel e) {
		return e.getIdTipoProcessoDocumentoPapel();
	}

}
