/**
 *  pje
 *  Copyright (C) 2013 Conselho Nacional de Justiça
 *
 *  A propriedade intelectual deste programa, tanto quanto a seu código-fonte
 *  quanto a derivação compilada é propriedade da União Federal, dependendo
 *  o uso parcial ou total de autorização expressa do Conselho Nacional de Justiça.
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.TipoDocumentoIdentificacao;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;

/**
 * Componente de acesso a dados da entidade {@link TipoDocumentoIdentificacao}.
 * 
 * @author cristof
 *
 */
@Name("tipoDocumentoIdentificacaoDAO")
public class TipoDocumentoIdentificacaoDAO extends BaseDAO<TipoDocumentoIdentificacao> {

	@Override
	public String getId(TipoDocumentoIdentificacao e) {
		return e.getCodTipo();
	}

	// Importado de br.com.infox.pje.dao.TipoDocumentoIdentificacaoDAO
	@SuppressWarnings("unchecked")
	public List<TipoDocumentoIdentificacao> tipoDocumentoIdentificacaoItems(TipoPessoaEnum tipoPessoa) {
		String query = "SELECT tdi FROM TipoDocumentoIdentificacao AS tdi " +
				"	WHERE tdi.ativo = true " +
				"		AND tdi.tipoPessoa = :tipoPessoa " +
				"		ORDER BY tdi.tipoDocumento ";
		Query q = entityManager.createQuery(query);
		q.setParameter("tipoPessoa", tipoPessoa);
		return q.getResultList();
	}
	
	public TipoDocumentoIdentificacao carregarTipoDocumentoIdentificacao(String codigo, TipoPessoaEnum tipoPessoa){
		String query = "SELECT tdi FROM TipoDocumentoIdentificacao AS tdi " +
				"	WHERE tdi.codTipo = :codigo ";
		if(tipoPessoa != null){
				query += "		AND tdi.tipoPessoa = :tipoPessoa ";
		}
		query += "		AND tdi.ativo = true ";
		
		Query q = entityManager.createQuery(query);
		q.setParameter("codigo", codigo);
		if(tipoPessoa != null){
			q.setParameter("tipoPessoa", tipoPessoa);
		}
		q.setMaxResults(1);
		try{
			return (TipoDocumentoIdentificacao) q.getSingleResult();
		}catch(NoResultException e){
			return null;
		}
	}
	// Fim da importação
	
	
	public TipoDocumentoIdentificacao carregarTipoDocumentoIdentificacao(String codigo){
		return carregarTipoDocumentoIdentificacao(codigo, null);
	}
	
	/**
	 * Método responsável por buscar na base de dados um documento de identificação de uma Pessoa pelo id do documento
	 * 	
	 * @param documentoIdentificacao
	 * @return <code>TipoDocumentoIdentificacao</code>
	 */
	public TipoDocumentoIdentificacao getTipoDocumentoIdentificacaobyDocumento(int documentoIdentificacao) {
		
		StringBuilder sb = new StringBuilder("select pdi.tipoDocumento from PessoaDocumentoIdentificacao pdi ");
		sb.append("where ");
		sb.append("pdi.idDocumentoIdentificacao = :id ");
		Query query = entityManager.createQuery(sb.toString());
		query.setParameter("id", documentoIdentificacao);
		query.setMaxResults(1);

		try {
			return (TipoDocumentoIdentificacao) query.getSingleResult();
		 } catch (NoResultException no) {
			 return null;
		 }
	}
}
