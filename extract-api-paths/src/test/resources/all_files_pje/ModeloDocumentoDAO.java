/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.Arrays;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.HibernateUtil;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.TipoModeloDocumento;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.enums.TipoEditorEnum;

/**
 * @author cristof
 * 
 */
@Name("modeloDocumentoDAO")
public class ModeloDocumentoDAO extends BaseDAO<ModeloDocumento>{

	private static final int TAMANHO_BUILDER_SQL = 300;

	@SuppressWarnings("unchecked")
	public List<ModeloDocumento> findByIds(Integer... ids){
		String queryStr = "SELECT m FROM ModeloDocumento AS m WHERE m.idModeloDocumento IN (?1) ORDER BY m.tituloModeloDocumento ASC";
		Query q = this.entityManager.createQuery(queryStr);
		q.setParameter(1, Arrays.asList(ids));
		return q.getResultList();
	}

	@Override
	public Integer getId(ModeloDocumento e){
		return e.getIdModeloDocumento();
	}

	/**
	 * Método que busca a localização de um dado modelo.
	 * @author Ronny Paterson
	 * @since 1.4.7.2
	 * @param md Modelo cuja localização é desejadas
	 * @return Localizacao a localização do modelo informado
	 */
	public Localizacao getLocalizacaoModelo(ModeloDocumento md) {
		String query = "SELECT mdl.localizacao from ModeloDocumentoLocal mdl " +
				"	WHERE mdl.idModeloDocumento = :idModeloDocumento " +
				"		AND mdl.ativo = true ";
		Query q = this.entityManager.createQuery(query);
		q.setParameter("idModeloDocumento", md.getIdModeloDocumento());
		
		Localizacao localizacaoModelo = null;
		
		try {
			localizacaoModelo = (Localizacao) q.getSingleResult();			
		} catch(NoResultException nre) {
			return null;
		} catch(NonUniqueResultException nre) {
			return null;
		}
		return localizacaoModelo;
	}

	
	/**
	 * Método que recupera a lista de modelos de um dado tipo de documento, considerando, 
	 * se existente, as localizações informadas
	 * @param tipo Tipo de Documento escolhido
	 * @param locais Localizações do usuário
	 * @return Lista de Modelos
	 */
	public List<ModeloDocumento> getModelos(TipoProcessoDocumento tipo, List<Localizacao> locais){
		String query = 	"SELECT mdl from ModeloDocumentoLocal AS mdl " +
						" WHERE mdl.tipoProcessoDocumento =:prm1 " +
						"       AND mdl.localizacao IN (:prm2) " +
						"	    AND mdl.ativo = true " +
						"		AND mdl.tipoEditor != :prm3 "+
						" ORDER BY upper(mdl.tituloModeloDocumento)";

		org.hibernate.Query qh = HibernateUtil.getSession().createQuery(query);
		qh.setParameter("prm1", tipo);
		qh.setParameterList("prm2",locais);
		qh.setParameter("prm3",TipoEditorEnum.L);
	
		return (List<ModeloDocumento>) qh.list();
		
	}
	
	/**
	 * Método que recupera a lista de modelos de uma dada lista de tipos de documento, considerando, 
	 * se existente, as localizações informadas
	 * @param tipos Tipos de Documento escolhido
	 * @param locais Localizações do usuário
	 * @return Lista de Modelos
	 */
	public List<ModeloDocumento> getModelos(List<TipoProcessoDocumento> tipos, List<Localizacao> locais){
		String query = 	"SELECT mdl from ModeloDocumentoLocal AS mdl " +
						" WHERE mdl.tipoProcessoDocumento IN (:prm1) " +
						"       AND mdl.localizacao IN (:prm2) " +
						"	    AND mdl.ativo = true " +
						" ORDER BY upper(mdl.tituloModeloDocumento)";

		org.hibernate.Query qh = HibernateUtil.getSession().createQuery(query);
		qh.setParameter("prm1", tipos);
		qh.setParameterList("prm2",locais);
	
		return (List<ModeloDocumento>) qh.list();
	}
	
	/**
	 * Método que recupera a lista de modelos de um dado tipo de documento, considerando, 
	 * se existente, as localizações informadas e a descrição do titulo ou conteudo do modelo informadas pelo usuario 
	 * @param tipo Tipo de Documento escolhido
	 * @param locais Localizações do usuário
	 * @param String tituloOuDescricao contendo a pesquisa do usuario pelo titulo ou conteudo do modelo
	 * @return Lista de Modelos
	 * @author eduardo.pereira@tse.jus.br
	 */
	@SuppressWarnings("unchecked")
	public List<ModeloDocumento> getModelos(TipoProcessoDocumento tipo, List<Localizacao> locais, 
			String tituloOuDescricao, Integer... idsModelos){

		StringBuilder query = new StringBuilder();
		query.append(" SELECT mdl from ModeloDocumentoLocal AS mdl ");
		query.append(" WHERE mdl.tipoProcessoDocumento =:prm1 ");
		query.append(" AND mdl.localizacao IN (:prm2) ");
		query.append(" AND mdl.ativo = true ");
		query.append(" AND (lower(to_ascii(mdl.tituloModeloDocumento)) like lower(to_ascii(:prm3)) OR lower(to_ascii(mdl.modeloDocumento)) like lower(to_ascii(:prm3))) ");
		
		if(idsModelos != null && !Arrays.asList(idsModelos).isEmpty()) {
			query.append(" AND mdl.idModeloDocumento IN (:prm4) ");
		}
		
		query.append(" ORDER BY upper(mdl.tituloModeloDocumento) ");

		org.hibernate.Query qh = HibernateUtil.getSession().createQuery(query.toString());
		qh.setParameter("prm1", tipo);
		qh.setParameterList("prm2",locais);
		qh.setParameter("prm3", "%"+tituloOuDescricao+"%");
		
		if(idsModelos != null && !Arrays.asList(idsModelos).isEmpty()) {
			qh.setParameterList("prm4", idsModelos);
		}
		
		return qh.list();
	}
	
	/**
	 * Método que recupera a lista de modelos da localização informada, considerando um dado tipo
	 * @param tipo Tipo de Modelo escolhido
	 * @param locais Localização escolhida
	 * @return Lista de Modelos
	 */
	@SuppressWarnings("unchecked")
	public List<ModeloDocumento> getModelos(TipoModeloDocumento tipo, Localizacao local, Papel papel){
		StringBuilder query = new StringBuilder("SELECT mdl from ModeloDocumentoLocal AS mdl ")
			.append(" WHERE 1=1 ")
			.append(" AND mdl.ativo = true ");
		if(local != null) {
			query.append(" AND mdl.localizacao = :prm1 ");
		}
		if(tipo != null) {
			query.append(" AND mdl.tipoModeloDocumento =:prm2 ");
		}
		boolean filtrarPapel = false;
		if(papel != null && !Authenticator.isPermissaoCadastroTodosPapeis(papel.getIdentificador())) {
			query.append(" AND mdl.idModeloDocumento IN (SELECT md.idModeloDocumento FROM ModeloDocumento md ")
				.append(" JOIN md.tipoModeloDocumento tmd ")
				.append(" JOIN tmd.papeis papeis WHERE tmd.ativo IS TRUE ")
				.append(" AND papeis.identificador = :prm3")
				.append(" ) ");
			filtrarPapel = true;
		}
		query.append(" ORDER BY upper(mdl.tituloModeloDocumento)");
		org.hibernate.Query qh = HibernateUtil.getSession().createQuery(query.toString());
		if(local != null) {
			qh.setParameter("prm1",local);
		}
		if(tipo != null) {
			qh.setParameter("prm2", tipo);
		}
		if(filtrarPapel) {
			qh.setParameter("prm3", papel.getIdentificador());
		}
	
		return (List<ModeloDocumento>) qh.list();
	}
	
	public List<ModeloDocumento> getModelos(TipoModeloDocumento tipo, Localizacao local){
		return this.getModelos(tipo, local, null);
	}
	
	/**
	 * Método que recupera a lista de modelos da localização informada.
	 * 
	 * @param locais Localização escolhida
	 * @return Lista de Modelos
	 */
	public List<ModeloDocumento> getModelos(Localizacao local){
		return this.getModelos(null, local);
	}

	/**
	 * Método que recupera a lista de modelos considerando as localizações informadas.
	 * @param locais Localizações do usuário
	 * @return Lista de Modelos
	 */
	@SuppressWarnings("unchecked")
	public List<ModeloDocumento> obterModelosPorLocalizacao(List<Localizacao> localizacoes){
		StringBuilder sql = new StringBuilder(TAMANHO_BUILDER_SQL);

		sql.append(" SELECT mdl from ModeloDocumentoLocal AS mdl ");
		sql.append(" WHERE mdl.localizacao IN (:localizacoes) ");
		sql.append(" AND mdl.ativo = :ativo ");
		sql.append(" ORDER BY upper(mdl.tituloModeloDocumento) ");

		org.hibernate.Query qh = HibernateUtil.getSession().createQuery(sql.toString());
		qh.setParameterList("localizacoes", localizacoes);
		qh.setParameter("ativo", true);
	
		return (List<ModeloDocumento>) qh.list();
	}
	
	/**
	 * Metodo que retorna lista de modelos por tipoProcessoDocumento
	 * @param tipoProcessoDocumento
	 * @return Lista de modelos
	 */
	@SuppressWarnings("unchecked")
	public List<ModeloDocumento> recuperaModelosPorTipoProcessoDocumento(TipoProcessoDocumento tipoProcessoDocumento) {
		StringBuilder sql = new StringBuilder("select o from ModeloDocumentoLocal o ");
		sql.append(" where o.ativo = true ");
		sql.append(" and o.tipoProcessoDocumento = :tipoProcessoDocumento ");
		
		org.hibernate.Query query = HibernateUtil.getSession().createQuery(sql.toString());
		query.setParameter("tipoProcessoDocumento",tipoProcessoDocumento);
	
		return (List<ModeloDocumento>) query.list();
	}
	
	/**
	 * Metodo que verifica a existencia de um modelo no banco de dados
	 * @param tipoProcessoDocumento
	 * @return true ou false
	 */
	public boolean existeModelosProcessoDocumento(String modelo) {
		StringBuilder sql = new StringBuilder("select 1 from ModeloDocumento o ");
		sql.append(" where lower(to_ascii(o.tituloModeloDocumento)) = lower(to_ascii(:modelo)) ");
		
		org.hibernate.Query query = HibernateUtil.getSession().createQuery(sql.toString());
		query.setParameter("modelo",modelo);
		return !query.list().isEmpty();
	}
	
}
