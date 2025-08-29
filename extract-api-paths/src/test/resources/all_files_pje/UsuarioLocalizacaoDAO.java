/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.exception.AplicationException;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;

/**
 * Componente de acesso a dados da entidade {@link UsuarioLocalizacao}.
 * 
 * @author cristof
 *
 */
@Name("usuarioLocalizacaoDAO")
public class UsuarioLocalizacaoDAO extends BaseDAO<UsuarioLocalizacao> {
	
	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.business.dao.BaseDAO#getId(java.lang.Object)
	 */
	@Override
	public Object getId(UsuarioLocalizacao e) {
		return e.getIdUsuarioLocalizacao();
	}

	@SuppressWarnings("unchecked")
	public List<UsuarioLocalizacao> getLocalizacoesAtuais(Usuario usuario) {
		String query = "SELECT ul FROM UsuarioLocalizacao AS ul " +
				"	JOIN FETCH ul.localizacaoFisica AS lf " +
				"	LEFT JOIN FETCH ul.localizacaoModelo AS lm " +
				"	JOIN FETCH ul.papel AS p " +
				"	LEFT OUTER JOIN ul.usuarioLocalizacaoMagistradoServidor AS lms " +
				"		WHERE ul.usuario = :usuario " +
				"			AND " +
				"			(" +
				"				(lms IS NULL)" +
				"			OR " +
				"				(CAST(lms.dtInicio AS date) <= CURRENT_DATE " +
				"					AND " +
				"					(lms.dtFinal IS NULL OR (CAST(lms.dtFinal AS date) >= CURRENT_DATE)))" +
				"			) " +
				"		ORDER BY ul.localizacaoFisica, ul.papel";
		Query q = entityManager.createQuery(query);
		q.setParameter("usuario", usuario);
		return q.getResultList();
	}

	/**
	 * Recupera a localização física indicada da pessoa, se existente.
	 * 
	 * @param pessoa a pessoa cuja localização se pretende recuperar
	 * @param loc a localização específica
	 * @param papel o papel afetado à localização
	 * @return a {@link UsuarioLocalizacao} específica, ou null se ela não existir.
	 */
	public UsuarioLocalizacao getLocalizacao(PessoaFisica pessoa, Localizacao loc, Papel papel) {
		String query = "SELECT ul FROM UsuarioLocalizacao AS ul " +
				"	WHERE ul.usuario = :pessoa " +
				"	AND ul.localizacaoFisica = :loc" +
				"	AND ul.papel = :papel";
		Query q = entityManager.createQuery(query);
		q.setParameter("pessoa", pessoa);
		q.setParameter("loc", loc);
		q.setParameter("papel", papel);
		try{
			return (UsuarioLocalizacao) q.getSingleResult();
		}catch (NoResultException e){
			return null;
		}catch (NonUniqueResultException e) {
			throw new AplicationException(
					"Foram encontrados mais de um registro de localização e papel idênticos para o usuário.");
		}
	}
	
	public UsuarioLocalizacao findByUsuarioLocalizacaoPapel(Usuario usuario, Localizacao loc, Papel papel) {
		String query = "SELECT ul FROM UsuarioLocalizacao AS ul " +
				"	WHERE ul.usuario = :usuario " +
				"	AND ul.localizacaoFisica = :loc" +
				"	AND ul.papel = :papel";
		Query q = entityManager.createQuery(query);
		q.setParameter("usuario", usuario);
		q.setParameter("loc", loc);
		q.setParameter("papel", papel);
		try{
			return (UsuarioLocalizacao) q.getSingleResult();
		}catch (NoResultException e){
			return null;
		}catch (NonUniqueResultException e) {
			throw new AplicationException(
					"Foram encontrados mais de um registro de localização e papel idênticos para o usuário.");
		}
	}	

	/**
	 * Indica se a pessoa indicada mantém o papel indicado em razão de alguma de suas
	 * localizações pessoais.
	 * 
	 * @param pessoa a pessoa a respeito da qual se pretende recuperar a informação
	 * @param papel o papel que se pretende investigar
	 * @return true, se houver pelo menos uma localização pessoal que ostente o papel indicado
	 */
	public boolean mantemPapel(Pessoa pessoa, Papel papel) {
		String query = "SELECT COUNT(ul.idUsuarioLocalizacao) FROM UsuarioLocalizacao AS ul " +
				"	WHERE ul.usuario = :pessoa " +
				"	AND ul.papel = :papel";
		Query q = entityManager.createQuery(query);
		q.setParameter("pessoa", pessoa);
		q.setParameter("papel", papel);
		q.setMaxResults(1);
		Number cont = (Number) q.getSingleResult();
		return cont.intValue() > 0;
	}
	
	/**
	 * Método responsável por obter uma lista de {@link UsuarioLocalizacao} de
	 * uma determinada {@link Pessoa} a partir da sua lista de papéis.
	 * 
	 * @param pessoa
	 *            a pessoa cujas localizações se pretende obter
	 * @param papeis
	 *            os papéis de interesse
	 * @return <code>List<code>, de localizações
	 */
	@SuppressWarnings("unchecked")
	public List<UsuarioLocalizacao> getLocalizacoesAtuais(Pessoa pessoa, List<Papel> papeis) {
		String query = "SELECT ul FROM UsuarioLocalizacao AS ul " +
				"	WHERE ul.usuario = :pessoa ";
		if(papeis != null && papeis.size() > 0) {
			query += "	AND ul.papel IN (:papeis) ";
		}
		Query q = entityManager.createQuery(query);
		q.setParameter("pessoa", pessoa);
		if(papeis != null && papeis.size() > 0) {
			q.setParameter("papeis", papeis);
		}
		return q.getResultList();
	}

	/**
	 * Recupera a lista de localizações ativas da pessoa indicada, quando vinculadas a um dado papel.
	 * 
	 * @param pessoa a pessoa cujas localizações se pretende obter
	 * @param papel o papel de interesse
	 * @return a lista de localizações
	 */
	@SuppressWarnings("unchecked")
	public List<UsuarioLocalizacao> getLocalizacoesAtuais(Pessoa pessoa, Papel papel) {
		String query = "SELECT ul FROM UsuarioLocalizacao AS ul " +
				"	WHERE ul.usuario = :pessoa " +
				"	AND ul.papel = :papel";
		Query q = entityManager.createQuery(query);
		q.setParameter("pessoa", pessoa);
		q.setParameter("papel", papel);
		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<UsuarioLocalizacao> getLocalizacoesAtuaisMagistrado(Usuario usuario) {
		String query = "SELECT ul FROM UsuarioLocalizacao AS ul " +
				"	LEFT OUTER JOIN ul.usuarioLocalizacaoMagistradoServidor AS lms " +
				"		WHERE ul.usuario = :usuario " +
				"			AND " +
				"			(" +
				"				(lms IS NULL)" +
				"			OR " +
				"				(CAST(lms.dtInicio AS date) <= CURRENT_DATE " +
				"					AND " +
				"					(lms.dtFinal IS NULL OR (CAST(lms.dtFinal AS date) >= CURRENT_DATE)))" +
				"			) AND ul.papel = :papel" +
				"		ORDER BY ul.localizacao";
		Query q = entityManager.createQuery(query);
		q.setParameter("usuario", usuario);
		q.setParameter("papel", ParametroUtil.instance().getPapelMagistrado());
		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<UsuarioLocalizacao> getLocalizacoesAtuais(Pessoa pessoa, Papel papel,Localizacao l) {
		String query = "SELECT ul FROM UsuarioLocalizacao AS ul " +
				"	WHERE ul.usuario = :pessoa " +
				"	AND ul.papel = :papel" +
				"   AND ul.localizacaoFisica = :localizacao";
		Query q = entityManager.createQuery(query);
		q.setParameter("pessoa", pessoa);
		q.setParameter("localizacao", l);
		q.setParameter("papel", papel);
		return q.getResultList();
	}
	
	/**
	 * Recupera todas as localizações do usuário.
	 * 
	 * @param idUsuario Identificador do usuário.
	 * @return As localizações do usuário.
	 */
	@SuppressWarnings("unchecked")
	public List<UsuarioLocalizacao> recuperarLocalizacoes(Integer idUsuario) {
		StringBuilder sb = new StringBuilder("SELECT ul FROM UsuarioLocalizacao AS ul ");
		sb.append("WHERE ul.usuario.idUsuario = :idUsuario ");

		Query query = entityManager.createQuery(sb.toString());
		query.setParameter("idUsuario", idUsuario);

		return query.getResultList();
	}

	/**
	 * Consulta as localizações onde o magistrado é atuante, ou seja, serão retornadas todas as 
	 * localizações onde o cargo é diferente de nulo, pois se o cargo é diferente de nulo então 
	 * a localização foi atribuída devido ao cadastro do magistrado no Orgão Julgador.
	 * 
	 * @param magistrado PessoaMagistrado
	 * @return localizações.
	 */
	@SuppressWarnings("unchecked")
	public List<UsuarioLocalizacao> consultarLocalizacoesDeMagistradoAtuante(PessoaMagistrado magistrado) {
		StringBuilder hql = new StringBuilder();
		hql.append("select o.usuarioLocalizacao ");
		hql.append("from UsuarioLocalizacaoMagistradoServidor o ");
		hql.append("where ");
		hql.append("	o.usuarioLocalizacao.usuario = :magistrado and ");
		hql.append("	o.orgaoJulgadorCargo is not null");
		
		Query query = getEntityManager().createQuery(hql.toString());
		query.setParameter("magistrado", magistrado.getPessoa());
		
		return query.getResultList();	
	}
	
	/**
 	 * Metodo que verifica se o usurio logado  magistrado auxiliar
 	 * @return Boolean 
 	 */
 	public Boolean isMagistradoAuxiliar(){
 		StringBuilder sb = new StringBuilder();
 		sb.append("select count(o) from UsuarioLocalizacaoMagistradoServidor o ");
 		sb.append(" join o.orgaoJulgadorCargo orgaoJulgadorCargo ");
 		sb.append(" join o.orgaoJulgador orgaoJulgador ");
 		sb.append(" join orgaoJulgador.localizacao localizacaoFisica ");
 		sb.append(" join o.usuarioLocalizacao usuarioLocalizacao ");
 		sb.append(" join usuarioLocalizacao.usuario usuario ");
 		sb.append("where orgaoJulgadorCargo.auxiliar = true ");
 		sb.append("and localizacaoFisica.idLocalizacao = :idLocalizacao ");
 		sb.append("and usuario.idUsuario = :idPessoaLogada ");
 		sb.append("and orgaoJulgador.idOrgaoJulgador = :idOrgaoJulgador ");
 		
 		Query q = getEntityManager().createQuery(sb.toString());
 		q.setParameter("idLocalizacao", Authenticator.getIdLocalizacaoAtual());
 		q.setParameter("idPessoaLogada", Authenticator.getIdUsuarioLogado());
 		q.setParameter("idOrgaoJulgador", Authenticator.getIdOrgaoJulgadorAtual());
 		try {
 			Long retorno = (Long) q.getSingleResult();
 			return retorno > 0;
 		} catch (NoResultException no) {
 			return Boolean.FALSE;
 		}
	}
 	
 	@SuppressWarnings("unchecked")
 	public List<UsuarioLocalizacao> consultarUsuarioLocalizacaoPorPapelHerdado(Papel papel){
 		StringBuilder sb = new StringBuilder();
 		
 		sb.append("SELECT DISTINCT ul FROM UsuarioLocalizacao ul ");
 		sb.append("INNER JOIN ul.papel as p ");
 		sb.append("WHERE p.idsPapeisInferiores like :idPapel ");
 		
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("idPapel", "%:" + String.valueOf(papel.getIdPapel()) + ":%");
		
		List<UsuarioLocalizacao> lista = q.getResultList(); 
		
		return CollectionUtilsPje.isEmpty(lista) ? new ArrayList<UsuarioLocalizacao>(0) : lista;
 	}

 	/**
 	 * Consulta as localizações mapeadas para os parâmetros informados.
 	 * 
 	 * @param tipoPessoa
 	 * @param localizacao
 	 * @param papel
 	 * @return Lista de localizações de pessoas.
 	 */
 	public List<UsuarioLocalizacao> findByTipoPessoaLocalizacaoPapel(TipoPessoaEnum tipoPessoa, Localizacao localizacao, Papel papel) {
		String query = "SELECT ul FROM UsuarioLocalizacao AS ul " +
				"	WHERE ul.usuario.inTipoPessoa = :inTipoPessoa " +
				"	AND ul.localizacaoFisica = :localizacao" +
				"	AND ul.papel = :papel";
		Query q = entityManager.createQuery(query);
		q.setParameter("inTipoPessoa", tipoPessoa);
		q.setParameter("localizacao", localizacao);
		q.setParameter("papel", papel);
		return q.getResultList();
	}
}
