package br.jus.cnj.pje.business.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.entidades.CentralMandado;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoExpedienteCentralMandado;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.enums.ProcessoExpedienteCentralMandadoStatusEnum;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

@Name(ProcessoExpedienteCentralMandadoDAO.NAME)
public class ProcessoExpedienteCentralMandadoDAO extends BaseDAO<ProcessoExpedienteCentralMandado> {

	public static final String NAME = "processoExpedienteCentralMandadoDAO";
	
	@Override
	public Object getId(ProcessoExpedienteCentralMandado e) {
		return e.getIdProcessoExpedienteCentralMandado();
	}

	/**
	 * Método que retorna uma lista de mandados de acordo com os parametros:
	 * 
	 * @param processoExpedienteCMStatusEnum --> Status do Mandado;
	 * @param search --> Filtros, Ordenação e Paginação;
	 * @param localizacaoCML --> Localização da Central de Mandados (aceita null);
	 * @param locUsuarioLogado --> Localização do Usuário (aceita null);
	 * @param idsLocalizacoesFilhasInt --> Árvore de Localização do Usuário;
	 * @param idUsuario --> Identificado do usuário;
	 * @param nomeParte --> Nome da Parte.
	 * @return List<ProcessoExpedienteCentralMandado>
	 */
	@SuppressWarnings(value = {"unchecked" })
	public List<ProcessoExpedienteCentralMandado> listProcessoExpedienteCentralMandado(
			ProcessoExpedienteCentralMandadoStatusEnum processoExpedienteCMStatusEnum, 
			Search search, Localizacao localizacaoCML, UsuarioLocalizacao localizacaoUsuario, List<Integer> idsLocalizacoesFilhasInt, 
			Integer idUsuario, String nomeParte){
		
		StringBuilder sb = new StringBuilder();
		Map<String, Object> params = new HashMap<String, Object>();
		
		sb.append(" select o ");

		sb.append(prepararHqlProcessoExpedienteCentralMandado(processoExpedienteCMStatusEnum, search, localizacaoCML, localizacaoUsuario, idsLocalizacoesFilhasInt, idUsuario, nomeParte, params));
		
		if(!search.getOrders().isEmpty()){
			loadOrderBy(sb, search.getOrders());
		}else{
			sb.append(" order by o.urgencia desc, o.dtDistribuicaoExpediente asc ");
		}
		
		Query query = getEntityManager().createQuery(sb.toString());
		
		
		loadParameters(query, params);
		
		if(search != null){
			if(search.getFirst() != null && search.getFirst().intValue() > 0){
				query.setFirstResult(search.getFirst());
			}
			
			if(search.getMax() != null && search.getMax().intValue() > 0){
				query.setMaxResults(search.getMax());
			}
		}
		
		return query.getResultList();
	}

	/**
	 * Método que retorna o total de mandados que atendem as condições dos parâmetros.
	 * 
	 * @param processoExpedienteCMStatusEnum --> Status do Mandado;
	 * @param search --> Filtros, Ordenação e Paginação;
	 * @param localizacaoCML --> Localização da Central de Mandados (aceita null);
	 * @param locUsuarioLogado --> Localização do Usuário (aceita null);
	 * @param idsLocalizacoesFilhasInt --> Árvore de Localização do Usuário;
	 * @param idUsuario --> Identificado do usuário;
	 * @param nomeParte --> Nome da Parte.
	 * @return Long
	 */
	public Long countProcessoExpedienteCentralMandado(ProcessoExpedienteCentralMandadoStatusEnum processoExpedienteCMStatusEnum, 
																					   Search search,
																					   Localizacao localizacaoCML, 
																					   UsuarioLocalizacao localizacaoUsuario,
																					   List<Integer> idsLocalizacoesFilhasInt,
																					   Integer idUsuario, 
																					   String nomeParte) {

		StringBuilder sb = new StringBuilder();
		Map<String, Object> params = new HashMap<String, Object>();

		sb.append(" select count(o) ");

		sb.append(prepararHqlProcessoExpedienteCentralMandado(processoExpedienteCMStatusEnum, search, localizacaoCML, localizacaoUsuario,
				idsLocalizacoesFilhasInt, idUsuario, nomeParte, params));


		Query query = getEntityManager().createQuery(sb.toString());

		loadParameters(query, params);

		return EntityUtil.getSingleResultCount(query);
	}

	
	private StringBuilder prepararHqlProcessoExpedienteCentralMandado(ProcessoExpedienteCentralMandadoStatusEnum processoExpedienteCMStatusEnum, 
																   	  Search search, 
																   	  Localizacao localizacaoCML,
																   	  UsuarioLocalizacao usuarioLocalizacao,
																   	  List<Integer> idsLocalizacoesFilhas,
																   	  Integer  idUsuario,
																   	  String nomeParte,
																   	  Map<String, Object> params){
		
		StringBuilder sb = new StringBuilder();
		
		if(CollectionUtilsPje.isEmpty(idsLocalizacoesFilhas)) {
			idsLocalizacoesFilhas = new ArrayList<Integer>();
			idsLocalizacoesFilhas.add(-1);
		}
		
		sb.append(" from ProcessoExpedienteCentralMandado o ");
		sb.append(" inner join o.centralMandado p ");
		sb.append(" where true = true ");
		sb.append(" AND in_enviado_scm = false ");
		if(processoExpedienteCMStatusEnum == ProcessoExpedienteCentralMandadoStatusEnum.N) {
			sb.append(" AND o.pessoaGrupoOficialJustica is null ");
		}else {
			sb.append(" AND o.pessoaGrupoOficialJustica is not null ");
			if(idUsuario != null){
				sb.append(" and o.pessoaGrupoOficialJustica.pessoa.idUsuario = :idUsuario "); 
				params.put("idUsuario", idUsuario);
			}
			
			if(processoExpedienteCMStatusEnum == ProcessoExpedienteCentralMandadoStatusEnum.R){
				sb.append(" and o.statusExpedienteCentral = 'R' ");
				sb.append(" and o.processoExpedienteCentralMandadoAnterior is not null ");
				sb.append(" and o.idProcessoExpedienteCentralMandado != o.processoExpedienteCentralMandadoAnterior ");
			}else if(processoExpedienteCMStatusEnum == ProcessoExpedienteCentralMandadoStatusEnum.A){
				sb.append(" and o.statusExpedienteCentral = 'A' ");
			}
		}
		
		Integer idLocalizacaoCML = -1;
		if(localizacaoCML != null) {
			idLocalizacaoCML = localizacaoCML.getIdLocalizacao();
		}
		sb.append(" and o.centralMandado.idCentralMandado in ( ");
		sb.append("    select DISTINCT cml.centralMandado.idCentralMandado");
		sb.append("		FROM CentralMandadoLocalizacao cml ");
		
		if(usuarioLocalizacao != null) {
			sb.append(" INNER JOIN cml.centralMandado cm ");
			sb.append(" INNER JOIN cm.oficialJusticaCentralMandadoList ojcml ");
		}
		
		sb.append("     where (cml.localizacao.idLocalizacao = :idLocalizacaoCML 		");
		sb.append(" 		OR cml.localizacao.idLocalizacao IN (:idsLocalizacoesList)) ");
		
		if(usuarioLocalizacao != null) {
			sb.append("		AND ojcml.usuarioLocalizacao.idUsuarioLocalizacao = :idUsuarioLocalizacao ");
			params.put("idUsuarioLocalizacao", usuarioLocalizacao.getIdUsuarioLocalizacao());
		}
		sb.append("	) ");
		
		params.put("idsLocalizacoesList", idsLocalizacoesFilhas);
		params.put("idLocalizacaoCML", idLocalizacaoCML);
				
		if(nomeParte != null){
			sb.append(" and o.processoExpediente.idProcessoExpediente in ( ");
			sb.append(" select distinct ppe.processoExpediente.idProcessoExpediente from ProcessoParteExpediente ppe ");
			sb.append(" where ppe.processoJudicial.idProcessoTrf = o.processoExpediente.processoTrf.idProcessoTrf ");
			sb.append(" and lower(ppe.pessoaParte.nome) like lower('%'||:nomeParte||'%'))");
			
			params.put("nomeParte", nomeParte);
		}
		
		if(search != null && search.getCriterias() != null && !search.getCriterias().isEmpty()){
			sb.append("and ");
			
			loadCriterias(sb, search, params);
		}
		
		return sb;
	}
	
	@Override
	protected void loadOrderBy(StringBuilder sb, Map<String, Order> orders) {
		boolean ordered = false;
		for(Entry<String, Order> e: orders.entrySet()){
			if(!ordered){
				sb.append(" ORDER BY ");
				ordered = true;
			}else{
				sb.append(",");
			}
			String order = e.getKey();
			if(!order.startsWith("o.")) {
				order = "o." + order; 
			}
			sb.append(order);
			if(e.getValue() == Order.DESC){
				sb.append(" DESC");
			}
		}
	}

	@SuppressWarnings("unchecked")
	public ProcessoExpedienteCentralMandado obterPorProcessoExpediente(ProcessoExpediente processoExpediente){
		StringBuilder sbQuery = new StringBuilder();
		sbQuery.append(" SELECT pe ");
		sbQuery.append(" FROM ProcessoExpedienteCentralMandado pe ");
		sbQuery.append(" WHERE pe.processoExpediente = :processoExpediente");
		sbQuery.append("  AND pe.processoExpedienteCentralMandadoAnterior IS NULL");
		
		Query query = getEntityManager().createQuery(sbQuery.toString());
		query.setParameter("processoExpediente", processoExpediente);
		if (!query.getResultList().isEmpty()) {
			return (ProcessoExpedienteCentralMandado) query.getSingleResult();
		}
		return null;
	}
}
