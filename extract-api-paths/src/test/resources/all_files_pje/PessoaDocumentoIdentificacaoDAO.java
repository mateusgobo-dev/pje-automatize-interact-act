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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.Name;

import br.com.itx.util.EntityUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.TipoDocumentoIdentificacao;

/**
 * Componente de acesso a dados da entidade {@link PessoaDocumentoIdentificacao}.
 * 
 * @author cristof
 *
 */
@Name("pessoaDocumentoIdentificacaoDAO")
public class PessoaDocumentoIdentificacaoDAO extends BaseDAO<PessoaDocumentoIdentificacao> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Integer getId(PessoaDocumentoIdentificacao doc) {
		return doc.getIdDocumentoIdentificacao();
	}
	
	//------------ importado de infox.PessoaDocumentoIdentificacaoDAO --------------------
	/**
	 * Recupera todos os documentos identificadores ativos de uma dada pessoa.
	 * 
	 * @param pessoa a pessoa cujos documentos se pretende recuperar
	 * @return a lista de documentos ativos da pessoa.
	 */
	@SuppressWarnings("unchecked")
	public List<PessoaDocumentoIdentificacao> getDocumentosAtivos(Pessoa pessoa) {
		String query = "SELECT doc FROM PessoaDocumentoIdentificacao AS doc " +
				"	WHERE doc.ativo = true " +
				"		AND doc.pessoa = :pessoa " +
				"		ORDER BY doc.tipoDocumento ";
		Query q = entityManager.createQuery(query);
		q.setParameter("pessoa", pessoa);
		return q.getResultList();
	}

	/**
	 * Recupera a lista de documentos identificadores ativos que têm por código do documento
	 * o dado.
	 * 
	 * @param codigoDocumento o código do documento dado.
	 * @return a lista de documentos que têm o código dado.
	 */
	@SuppressWarnings("unchecked")
	public List<PessoaDocumentoIdentificacao> findByNumeroDocumento(String codigoDocumento){
		String query = "SELECT doc FROM PessoaDocumentoIdentificacao AS doc " +
				"	WHERE doc.ativo = true " +
				"		AND doc.numeroDocumento = :codigoDocumento";
		Query q = entityManager.createQuery(query);
		q.setParameter("codigoDocumento", codigoDocumento);
		return q.getResultList();
	}
	
	/**
	 * Recupera o documento identificador do tipo CPF que está ativo, não foi utilizado falsamente e
	 * que tem o código dado.
	 * 
	 * [PJEII-3199] Método responsável por recuperar a Pessoa Documento identificação através do cpf.
	 * 
	 * @param cpf o código identificador da inscrição
	 * @return o documento CPF dado, se existente, ativo e não utilizado falsamente, ou null, se não existir. 
	 */
	public PessoaDocumentoIdentificacao findByCPF(String cpf) {
		Criteria criteria = HibernateUtil.getSession().createCriteria(PessoaDocumentoIdentificacao.class);
		criteria.add(Restrictions.eq("numeroDocumento", cpf));
		criteria.add(Restrictions.eq("ativo", true));
		criteria.add(Restrictions.eq("usadoFalsamente", false));
		criteria.createCriteria("tipoDocumento").add(Restrictions.eq("codTipo" , "CPF"));
		return (PessoaDocumentoIdentificacao) criteria.uniqueResult();
	}
	//------------ fim da importação
	
	/**
	 * Recupera o documento ativo e não utilizado falsamente do tipo dado que tem o código de documento informado.
	 * 
	 * @param codigoDocumento o código do documento
	 * @param tipo o tipo de documento que se pretende recuperar
	 * @return o documento identificador indicado, ou nulo se inexistente.
	 */
	public PessoaDocumentoIdentificacao recuperaDocumento(String codigoDocumento, TipoDocumentoIdentificacao tipo){
		String query = "SELECT doc FROM PessoaDocumentoIdentificacao AS doc WHERE " +
				" doc.ativo = true AND " +
				" doc.usadoFalsamente = false AND " +
				" doc.tipoDocumento = :tipo AND " +
				" doc.numeroDocumento = :codigoDocumento ";
		
		Query q = entityManager.createQuery(query);
		q.setParameter("tipo", tipo);
		q.setParameter("codigoDocumento", codigoDocumento);
		
		@SuppressWarnings("unchecked")
		List<Object> resultList = q.getResultList();
		
		/* Assume-se como verdade que o número do documento de identificação seja único para cada indivíduo. 
		 * Porém, alguns dados retornados pela Receita Federal podem conter valores genéricos.
		 * Exemplo: O número do título de eleitor pode vir com o valor 0000000000000. 
		 * Sendo assim, o método getSingleResult() da classe javax.persistence.Query não é apropriado. */
		if (resultList == null || resultList.isEmpty() || resultList.size() > 1) {
			return null;
		}
		return (PessoaDocumentoIdentificacao) resultList.get(0);
	}

	/**
	 * Recupera a lista de documentos do tipo dado não usado falsamente que pertencem à pessoa.
	 * 
	 * @param pessoa a pessoa a que pertencem os documentos
	 * @param tipo o tipo de documento de interesse
	 * @param incluirInativos marca indicativa de que se pretende recuperar também os documentos
	 * inativos do tipo dado vinculado à pessoa.
	 * @return a lista de documentos
	 */
	public List<PessoaDocumentoIdentificacao> recuperaDocumentos(Pessoa pessoa, TipoDocumentoIdentificacao tipo, boolean incluirInativos) {
		return recuperaDocumentos(pessoa, null, tipo, incluirInativos);
	}
	
	public List<PessoaDocumentoIdentificacao> recuperaDocumentos(String codigoDocumento, TipoDocumentoIdentificacao tipo, boolean incluirInativos) {
		return recuperaDocumentos(null, codigoDocumento, tipo, incluirInativos);
	}
	
	public List<PessoaDocumentoIdentificacao> recuperaDocumentos(Pessoa pessoa, String codigoDocumento, TipoDocumentoIdentificacao tipo, boolean incluirInativos) {
		return recuperaDocumentos(pessoa, codigoDocumento, tipo, incluirInativos, false);
	}
	
	@SuppressWarnings("unchecked")
	public List<PessoaDocumentoIdentificacao> recuperaDocumentos(Pessoa pessoa, String codigoDocumento, TipoDocumentoIdentificacao tipo, boolean incluirInativos, boolean somentePrincipal) {
		StringBuilder query = new StringBuilder();
        query.append("SELECT doc FROM PessoaDocumentoIdentificacao AS doc ");
        query.append("	WHERE doc.usadoFalsamente = false ");
        
        if( tipo != null ){
        	query.append(" 		AND doc.tipoDocumento = :tipo ");
        }
        
        if( codigoDocumento != null ) {
        	query.append(" 		AND doc.numeroDocumento = :codigoDocumento ");
        }
        
        if( pessoa != null ) {
        	query.append(" 		AND doc.pessoa.idPessoa = :pessoa ");
        }
		
		if(!incluirInativos){
			query.append(" AND doc.ativo = true ");
		}
		
        if(somentePrincipal) {
        	query.append(" AND doc.documentoPrincipal = true ");
        }
		
		query.append(" ORDER BY doc.documentoPrincipal DESC, doc.numeroDocumento ");
		Query q = entityManager.createQuery(query.toString());
		
		if( codigoDocumento != null )
			q.setParameter("codigoDocumento", codigoDocumento);
		if( tipo != null )
			q.setParameter("tipo", tipo);
		if( pessoa != null )
			q.setParameter("pessoa", pessoa.getIdPessoa());
		
		return q.getResultList();
	}
	
	/**
	 * Recupera todos os documentos de identificação de uma dada pessoa.
	 * 
	 * @param pessoa A pessoa cujos documentos se pretende recuperar.
	 * @return Lista de documentos da pessoa.
	 */
	@SuppressWarnings("unchecked")
	public List<PessoaDocumentoIdentificacao> recuperarDocumentos(Pessoa pessoa) {
		String query = "SELECT doc FROM PessoaDocumentoIdentificacao AS doc " +
				"WHERE doc.pessoa = :pessoa " +
				"ORDER BY doc.tipoDocumento ";
		Query q = entityManager.createQuery(query);
		q.setParameter("pessoa", pessoa);
		return q.getResultList();
	}
	
	/**
	 * Método responsável por recuperar os documentos de identificação temporarios.
	 * 
	 * @param idPessoas Lista de identidicadores das pessoas.
	 * @return {@link PessoaDocumentoIdentificacao}.
	 */
	@SuppressWarnings("unchecked")
	public List<PessoaDocumentoIdentificacao> recuperarDocumentosTemporarios(List<Integer> idPessoas) {
		String query = "SELECT doc FROM PessoaDocumentoIdentificacao AS doc " +
			"WHERE doc.pessoa.idPessoa IN (:ids) AND doc.temporario = true";
		
		Query q = entityManager.createQuery(query);
		q.setParameter("ids", idPessoas);
		
		return q.getResultList();
	}

	/**
	 * Método responsável por atualizar o valor do atributo 'nomeUsuarioLogin' da entidade 'PessoaDocumentoIdentificacao' 
 	 * de acordo com o valor presente no atributo 'nome' da entidade 'UsuarioLogin'.
	 * 
	 * @param pessoa Pessoa.
	 */
	public void atualizarNomePessoaDocumento(Pessoa pessoa) {
		Query query = EntityUtil.createNativeQuery(entityManager, 
				"update tb_pess_doc_identificacao set ds_nome_usuario_login = :nomePessoa where id_pessoa = :idPessoa", "tb_pess_doc_identificacao");
		
		query.setParameter("nomePessoa", pessoa.getNome());
		query.setParameter("idPessoa", pessoa.getIdPessoa());
		query.executeUpdate();
	}

	@SuppressWarnings("unchecked")
	public List<PessoaDocumentoIdentificacao> recuperarDocumentosIdentificacaoPaginados(Integer idPessoa, Boolean ativo, Integer page, Integer pageSize){
		List<PessoaDocumentoIdentificacao> listaDocumentos = new ArrayList<PessoaDocumentoIdentificacao>();
		
		StringBuilder sb = new StringBuilder("SELECT pdi FROM PessoaDocumentoIdentificacao pdi ");
		sb.append("WHERE pdi.pessoa.idPessoa = :idPessoa ");
		
		if(ativo != null){
			sb.append("AND pdi.ativo = :ativo ");			
		}
		
		Query q = this.entityManager.createQuery(sb.toString());
		q.setFirstResult(page);
		q.setMaxResults(pageSize);
		q.setParameter("idPessoa", idPessoa);
		
		if(ativo != null){
			q.setParameter("ativo", ativo);
		}
		
		listaDocumentos = q.getResultList();
		
		return listaDocumentos;
	}

	@SuppressWarnings("unchecked")
	public PessoaDocumentoIdentificacao obterDocumentoCpfPessoa(Pessoa pessoa){
		Query q = getEntityManager().createQuery("select o from PessoaDocumentoIdentificacao o" +
				" where o.ativo is true and" +
				" o.pessoa = :pessoa" +
				" and o.tipoDocumento = 'CPF' order by o.idDocumentoIdentificacao ");
		q.setParameter("pessoa", pessoa);
		List<PessoaDocumentoIdentificacao> ret = q.getResultList();
		return ret.size() > 0 ? ret.get(0) : null;
	}

	
	@SuppressWarnings("unchecked")
	public List<Integer> obterIdsPessoasViaCpf(final Set<String> numCpfs) {
		if (numCpfs == null || !numCpfs.isEmpty()) {
			return new ArrayList<>();
		}
		StringBuilder sqlStmt = new StringBuilder();
		sqlStmt.append(" SELECT DISTINCT d.id_pessoa");
		sqlStmt.append(" FROM client.tb_pess_doc_identificacao d");
		sqlStmt.append(" WHERE in_ativo = true AND cd_tp_documento_identificacao = 'CPF' AND d.nr_documento_identificacao IN (:docs)");
		Query query = EntityUtil.createNativeQuery(entityManager, sqlStmt.toString());
		query.setParameter("docs", numCpfs);
		return query.getResultList();
	}	
	
	
	@SuppressWarnings("unchecked")
	public List<Integer> obterIdsPessoasViaRaizCnpj(final Set<String> numerosDeDocumentos) {
		if (numerosDeDocumentos == null || numerosDeDocumentos.isEmpty()) {
			return new ArrayList<>();
		}
		Set<String> numCnpjs = obterListaDeCnpjRaiz(numerosDeDocumentos);
		
		if (numCnpjs.isEmpty()) {
			return new ArrayList<>();
		}
		
		StringBuilder sqlStmt = new StringBuilder();
		sqlStmt.append(" SELECT DISTINCT d.id_pessoa");
		sqlStmt.append(" FROM client.tb_pess_doc_identificacao d");
		sqlStmt.append(" WHERE in_ativo = true AND cd_tp_documento_identificacao = 'CPJ' AND (");
		
		int index = 1;
		int sizeCnpjs = numCnpjs.size();
		for (; index <= sizeCnpjs; index++) {
			String param = ":doc" + index;
			//A consulta é pelo cnpj raiz.
			sqlStmt.append(" d.nr_documento_identificacao like ").append(param);
			
			if (index < sizeCnpjs) {
				sqlStmt.append(" OR ");
			}
		}
		sqlStmt.append(" )");
		
		Query query = EntityUtil.createNativeQuery(entityManager, sqlStmt.toString());

		index = 1;
		for (String cpfCnpj : numCnpjs) {
			String param = "doc" + index;
			query.setParameter(param, cpfCnpj);
			index++;
		}
		
		return query.getResultList();
	}

	private Set<String> obterListaDeCnpjRaiz(final Set<String> numerosDeDocumentos) {
		Set<String> numCnpjs = new HashSet<>();
		
		for (String documento : numerosDeDocumentos) {
			String cnpjRaiz = InscricaoMFUtil.obtemRaizDeCnpjSemValidacao(documento);
			if (cnpjRaiz != null) {
				numCnpjs.add(cnpjRaiz + "%");
			}
		}
		return numCnpjs;
	}
	
	@SuppressWarnings("unchecked")
	public List<Integer> obterIdsPessoasPorIdsDocs(final Set<Integer> idsDocs) {
		if (idsDocs == null || idsDocs.isEmpty()) {
			return new ArrayList<>();
		}
		StringBuilder sqlStmt = new StringBuilder();
		sqlStmt.append(" SELECT DISTINCT id_pessoa");
		sqlStmt.append(" FROM tb_pess_doc_identificacao");
		sqlStmt.append(" WHERE id_pessoa_doc_identificacao IN (:ids)");
		
		Query query = EntityUtil.createNativeQuery(entityManager, sqlStmt.toString());
		query.setParameter("ids", idsDocs);
		return query.getResultList();
	}		
}

 


