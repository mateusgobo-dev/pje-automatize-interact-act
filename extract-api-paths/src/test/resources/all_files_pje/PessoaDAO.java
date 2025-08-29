package br.jus.cnj.pje.business.dao;

import java.util.Arrays;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import br.com.itx.util.HibernateUtil;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;
import br.jus.pje.nucleo.enums.TipoNomePessoaEnum;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;
import br.jus.pje.nucleo.util.StringUtil;

@Name(PessoaDAO.NAME)
public class PessoaDAO extends AbstractUsuarioDAO<Pessoa>{

	public static final String NAME = "pessoaDAO";

	@Logger
	private Log logger;

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.business.dao.BaseDAO#getId(java.lang.Object)
	 */
	@Override
	public Integer getId(Pessoa e){
		return e.getIdUsuario();
	}

	/**
	 * Recupera as pessoas cujo nome seja o nome dado, seja ele o nome principal cadastrado, 
	 * seja ele qualquer dos nomes constantes nos documentos da pessoa ou que figurem como
	 * seus nomes alternativos.
	 * 
	 * @param name o nome a ser pesquisado
	 * @param firstRow o primeiro a registro a ser recuperado.
	 * @param maxLength o máximo de registros a serem recuperados.
	 * @return a lista de pessoas que tenham o nome dado.
	 */
	@SuppressWarnings("unchecked")
	public List<Pessoa> findByName(String name, int firstRow, int maxLength){
		String qStr = 	"SELECT DISTINCT p " +
						"FROM Pessoa AS p " +
						"LEFT JOIN p.nomesPessoa AS vs " +
						"WHERE " + 
						"  LOWER(to_ascii(vs.nome)) LIKE LOWER(to_ascii( :name )) " + 
						"  and vs.tipo in (:tipos) " ;
		
		Query q = entityManager.createQuery(qStr);
		q.setParameter("name", "%" + name + "%");
		q.setParameter("tipos", Arrays.asList(TipoNomePessoaEnum.A, TipoNomePessoaEnum.D, TipoNomePessoaEnum.C));
		q.setFirstResult(firstRow);
		if (maxLength != -1){
			q.setMaxResults(maxLength);
		}
		return q.getResultList();
	}

	/**
	 * Recupera a lista de pessoas que têm o nome dado.
	 * 
	 * @param name o nome a ser pesquisado.
	 * @return a lista de pessoas que têm o nome dado.
	 * @see #findByName(String, int, int)
	 */
	public List<Pessoa> findByName(String name){
		return this.findByName(name, 0, -1);
	}

	/**
	 * Recupera a lista de pessoas que têm um documento identificador dado.
	 * 
	 * @param documentId o texto do documento a ser pesquisado.
	 * @return a lista de pessoas
	 */
	@SuppressWarnings("unchecked")
	public List<Pessoa> findByDocument(String documentId){
		String qStr = "SELECT DISTINCT p FROM Pessoa AS p " + "JOIN p.pessoaDocumentoIdentificacaoList AS docs "
			+ "WHERE docs.numeroDocumento = :documentoId";
		Query q = entityManager.createNamedQuery(qStr);
		q.setParameter("documentoId", documentId);
		List<Pessoa> list = q.getResultList();
		list = q.getResultList();
		return list;
	}

	/**
	 * Recupera a pessoa que tem a inscrição no Ministério da Fazenda (CPF ou CNPJ) dado, desde que 
	 * o aludido documento esteja marcado como documento principal e não se trate de documento utilizado
	 * falsamente.
	 * 
	 * @param documentId o código de inscrição no cadastro de contribuintes
	 * @return a pessoa que tem o documento dado, ou null, se ela não estiver cadastrada.
	 */
	public Pessoa findByInscricaoMF(String documentId){
		String qStr = "SELECT DISTINCT p " +
				"	FROM Pessoa AS p " +
				"	JOIN p.pessoaDocumentoIdentificacaoList AS docs " +
				"	WHERE docs.documentoPrincipal = true " +
				"		AND docs.usadoFalsamente = false " +
				"		AND docs.tipoDocumento.tipoDocumento IN ('CPF','CPJ') " +
				"		AND docs.numeroDocumento = :documentId";
		Query q = entityManager.createQuery(qStr);
		q.setParameter("documentoId", documentId);
		Pessoa ret = null;
		try{
			ret = (Pessoa) q.getSingleResult();
		} catch (NoResultException e){
			logger.debug("Não foi encontrada pessoa com inscrição no Ministério da Fazenda ativa e válida de número [{0}].", documentId);
		} catch (NonUniqueResultException e){
			String message = String.format("Há mais de uma pessoa no sistema que ostenta o documento [%s] como documento principal, ativo e válido.", documentId);
			logger.error(message);
			throw new IllegalStateException(message);
		}
		return ret;
	}

	//////// Código importado da Infox ///////////////
	@SuppressWarnings("unchecked")
	public List<Pessoa> pesquisarPessoasSemMandados(Integer idProcessoTrf, String nome, String cpf){

		String hql = " select p " + " from ProcessoParte pp " + " inner join pp.pessoa p ";

		if (cpf != null && !cpf.trim().isEmpty()){
			hql += " inner join p.pessoaDocumentoIdentificacaoList d ";
		}

		hql += " where p.inTipoPessoa = :inTipoPessoa " + " and pp.processoTrf.idProcessoTrf = :idProcessoTrf "
			+ " and pp.inSituacao = :inSituacao " + " and pp.inParticipacao = :inParticipacao ";

		if (nome != null && !nome.trim().isEmpty()){
			hql += " and lower(to_ascii(p.nome)) like lower(concat('%', TO_ASCII(:nome), '%')) ";
		}

		if (cpf != null && !cpf.trim().isEmpty()){
			hql += " and d.tipoDocumento.codTipo =:codTipo " + " and d.numeroDocumento =:numeroDocumento ";
		}

		Query qry = getEntityManager().createQuery(hql);
		qry.setParameter("inTipoPessoa", TipoPessoaEnum.F);// apenas pessoas FISICAS
		qry.setParameter("idProcessoTrf", idProcessoTrf);
		qry.setParameter("inSituacao", ProcessoParteSituacaoEnum.A);
		qry.setParameter("inParticipacao", ProcessoParteParticipacaoEnum.P);// apenas polo PASSIVO

		if (nome != null && !nome.trim().isEmpty()){
			qry.setParameter("nome", nome);
		}

		if (cpf != null && !cpf.trim().isEmpty()){
			qry.setParameter("codTipo", "CPF");
			qry.setParameter("numeroDocumento", cpf);
		}

		return qry.getResultList();
	}
	
	public List<Pessoa> findByTypeANDDocument(String typeId, String documentId){
		return findByDocument(documentId, typeId, false);
	}

	//////// Fim do código importado da Infox ///////////////

	public List<Pessoa> findByDocument(String codigoDocumento, String tipoDocumento, boolean excluirFalsos){
		return findByDocument(codigoDocumento, new String[]{tipoDocumento}, excluirFalsos);
	}
	
	@SuppressWarnings("unchecked")
	public List<Pessoa> findByDocument(String codigoDocumento, String[] tiposDocumento, boolean excluirFalsos){
		String query ="SELECT o FROM Pessoa o " + 
				"	JOIN o.pessoaDocumentoIdentificacaoList doc " +
				"	WHERE " +
				"		doc.tipoDocumento.codTipo IN (:codTipo) " +
				"		AND doc.numeroDocumento = :numero" +
				"		AND doc.usadoFalsamente IN (:usadoFalsamente)";
		Query q = entityManager.createQuery(query);
		q.setParameter("numero", codigoDocumento);
		q.setParameter("codTipo", Arrays.asList(tiposDocumento));
		if(excluirFalsos){
			q.setParameter("usadoFalsamente", Arrays.asList(new Boolean[]{Boolean.FALSE}));
		}else{
			q.setParameter("usadoFalsamente", Arrays.asList(new Boolean[]{Boolean.TRUE, Boolean.FALSE}));
		}
		return q.getResultList();
	}
	
	/**
	 * Indica se a pessoa indicada está ativa e tem registrados certificado digital e assinatura.
	 *  
	 * @param p a pessoa a ser verificada
	 * @return true, se a pessoa estiver ativa e tiver assinatura e certificado digital.
	 */
	public boolean isCertificada(Pessoa p){
		String query = "SELECT COUNT(p.idUsuario) FROM Pessoa AS p " +
				"	WHERE p.idUsuario = :idPessoa " +
				"		AND p.ativo = true " +
				"		AND p.certChain IS NOT NULL " +
				"		AND p.assinatura IS NOT NULL";
		Query q = entityManager.createQuery(query);
		q.setParameter("idPessoa", p.getIdUsuario());
		q.setMaxResults(1);
		Number cont = (Number) q.getSingleResult();
		return cont.longValue() > 0;
	}

	/**
	 * Consulta a pessoa pelo cpf ou cnpj informado.
	 * 
	 * @param cpfCnpj CPF ou CNPJ (não formatado).
	 * @return Pessoa
	 */
	public Pessoa findByCPFouCNPJ(String cpfCnpj) {		
		Session session = HibernateUtil.getSession();
		
		Criteria criteria = session.createCriteria(Pessoa.class);

		Criteria pessoaDocumentoIdentificacao = criteria.createCriteria("pessoaDocumentoIdentificacaoList");
		pessoaDocumentoIdentificacao.createAlias("tipoDocumento", "tipoDocumento");
		pessoaDocumentoIdentificacao.add(Restrictions.eq("ativo", true));
		pessoaDocumentoIdentificacao.add(Restrictions.eq("usadoFalsamente", false));
		pessoaDocumentoIdentificacao.add(Restrictions.in("tipoDocumento.codTipo", new Object[]{"CPF","CPJ"}));
		pessoaDocumentoIdentificacao.add(Restrictions.eq("numeroDocumento", cpfCnpj));
		
		return (Pessoa) criteria.uniqueResult();
	}

	/**
	 * Consulta a pessoa pelo login informado.
	 * 
	 * @param login Login do usuário.
	 * @return Pessoa
	 */
	public Pessoa findByLogin(String login) {
		Session session = HibernateUtil.getSession();
		
		Criteria criteria = session.createCriteria(Pessoa.class);
		criteria.add(Restrictions.eq("login", login));
		
		return (Pessoa) criteria.uniqueResult();
	}
	
	/**
	 * Método responsável por recuperar a lista de pessoas que têm um nome e documento identificador especificado
	 * 
	 * @param nome Nome a ser pesquisado
	 * @param tipoDocumentoIdentificacao Tipo de documento de identificação
	 * @param documentoIdentificacao Texto do documento a ser pesquisado
	 * @return Lista de pessoas
	 */
	@SuppressWarnings("unchecked")
	public List<Pessoa> findByNomeAndDocumentoIdentificacao(String nome, String tipoDocumentoIdentificacao, String documentoIdentificacao) {
		String hql = 
				" SELECT DISTINCT p FROM Pessoa AS p JOIN p.pessoaDocumentoIdentificacaoList AS docs" +
				" WHERE (" + StringUtil.translateSql("docs.nome") + " LIKE " + StringUtil.translateSql(":nome") +  
				" OR " + StringUtil.translateSql("p.nome") + " LIKE " + StringUtil.translateSql(":nome") + 
				" ) AND docs.numeroDocumento = :documentoIdentificacao" +
				" AND docs.tipoDocumento.codTipo = :tipoDocumentoIdentificacao";
		Query q = entityManager.createQuery(hql);
		q.setParameter("nome", nome);
		q.setParameter("documentoIdentificacao", documentoIdentificacao);
		q.setParameter("tipoDocumentoIdentificacao", tipoDocumentoIdentificacao);
		List<Pessoa> list = q.getResultList();
		list = q.getResultList();
		return list;
	}

}
