package br.jus.cnj.pje.business.dao;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.apache.commons.io.FileUtils;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.util.Strings;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.DocumentoBinManager;
import br.jus.pje.nucleo.dto.AutoProcessualDTO;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBinPessoaAssinatura;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.enums.TipoOrigemAcaoEnum;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

@Name(ListProcessoCompletoBetaDAO.NAME)
public class ListProcessoCompletoBetaDAO extends BaseDAO<AutoProcessualDTO> {
	
	public final static String NAME = "listProcessoCompletoBetaDAO";

	private boolean visualizaSigiloso = Authenticator.isVisualizaSigiloso();
	private List<Integer> idsLocalizacoesFisicasList = Authenticator.getIdsLocalizacoesFilhasAtuaisList();
	private boolean isServidorExclusivoOJC = Authenticator.isServidorExclusivoColegiado();
	private Integer idOrgaoJulgadorColegiado = Authenticator.getIdOrgaoJulgadorColegiadoAtual();
	private Integer idProcuradoria = Authenticator.getIdProcuradoriaAtualUsuarioLogado();
	private Integer idUsuario = Authenticator.getIdUsuarioLogado();
    private static final String SELECT_O = "select o.* from (";
    private static final String UNION_ALL = " union all ";
    private static final String AS_O =  ") as o ";
    private static final String WHERE_N_DOCUMENTO_EXISTENTE =  "          and pe.in_documento_existente = true ";
    private static final String WHERE_ID_PROCESSO_TRF = " and id_processo_trf = :idProcesso ) ) ";

	@Override
	public Object getId(AutoProcessualDTO e) {
		return null;
	}
	
	public AutoProcessualDTO recuperarAuto(Integer idProcesso, Integer idDocumento) {
		return getAuto(idProcesso, idDocumento, null);
	}

	public AutoProcessualDTO recuperarPrimeiroAuto(Integer idProcesso) {
		return getAuto(true, idProcesso, null, "ASC", false, false);
	}

	public AutoProcessualDTO recuperarUltimoAuto(Integer idProcesso) {
		return getAuto(true, idProcesso, null, "DESC", false, true);
	}

	public AutoProcessualDTO recuperarUltimoAutoPrincipal(Integer idProcesso) {
		return getAuto(true, idProcesso, null, "DESC", true, true);
	}

	@SuppressWarnings("unchecked")
	public AutoProcessualDTO recuperarProximoAuto(Integer idProcesso, Integer idDocumentoReferencia) {
		if(idDocumentoReferencia == null) {
			return recuperarPrimeiroAuto(idProcesso);
		}
		
		Integer idProximoDocumento = null;
		AutoProcessualDTO auto = new AutoProcessualDTO();
		
		StringBuilder sql = new StringBuilder("select o.* from (");
		sql.append(getSqlProximoDocumento());
		sql.append(") as o ");
		sql.append("where o.id_processo_documento = :idDocumento");
	
		Query query  = getEntityManager().createNativeQuery(sql.toString());
		setParameters(query, idProcesso, idDocumentoReferencia, null, true);
		query.setMaxResults(1);
		
		List<Object[]> rs = query.getResultList();
		if(rs != null && rs.size() > 0) {
			Object[] r = rs.get(0);
			idProximoDocumento = (Integer)r[1];
			auto = getAuto(idProcesso, idProximoDocumento, "ASC");
		}
		
		return auto;
	}

	@SuppressWarnings("unchecked")
	public AutoProcessualDTO recuperarAutoAnterior(Integer idProcesso, Integer idDocumentoReferencia) {
		if(idDocumentoReferencia == null) {
			return recuperarUltimoAuto(idProcesso);
		}
		
		Integer idDocumentoAnterior = null;
		AutoProcessualDTO auto = new AutoProcessualDTO();
		
		StringBuilder sql = new StringBuilder("select o.* from (");
		sql.append(getSqlDocumentoAnterior());
		sql.append(") as o ");
		sql.append("where o.id_processo_documento = :idDocumento");
	
		Query query  = getEntityManager().createNativeQuery(sql.toString());
		setParameters(query, idProcesso, idDocumentoReferencia, null, true);
		query.setMaxResults(1);
		
		List<Object[]> rs = query.getResultList();
		if(rs != null && rs.size() > 0) {
			Object[] r = rs.get(0);
			idDocumentoAnterior = (Integer)r[1];
			auto = getAuto(idProcesso, idDocumentoAnterior, "ASC");
		}
		
		return auto;
	}

	@SuppressWarnings("unchecked")
	public List<TipoProcessoDocumento> recuperarTiposDocumentosAutos(Integer idProcesso) {
		List<TipoProcessoDocumento> tipos = new ArrayList<TipoProcessoDocumento>(0); 
		StringBuilder sql = new StringBuilder();
		sql.append("select distinct tpd.id_tipo_processo_documento, tpd.ds_tipo_processo_documento ");
		sql.append("from tb_processo_documento prd ");
		sql.append("inner join tb_tipo_processo_documento tpd on tpd.id_tipo_processo_documento = prd.id_tipo_processo_documento ");
		sql.append("inner join tb_processo_trf ptf on prd.id_processo = ptf.id_processo_trf ");
		sql.append("inner join tb_orgao_julgador oj on (oj.id_orgao_julgador = ptf.id_orgao_julgador) ");
		sql.append("inner join tb_processo pro on pro.id_processo = ptf.id_processo_trf ");
		sql.append("left join tb_processo_documento pri on pri.id_processo_documento = prd.id_documento_principal ");
		sql.append("left join tb_proc_documento_favorito fav on fav.id_processo_documento = prd.id_processo_documento and fav.id_usuario = :idUsuario "); 
		sql.append(getSqlWhere());
		sql.append("order by tpd.ds_tipo_processo_documento");
		
		Query query  = getEntityManager().createNativeQuery(sql.toString());

		setParameters(query, idProcesso, null, null, true);

		List<Object[]> resultList = query.getResultList();
		for (Object[] r: resultList) {
			TipoProcessoDocumento tipo = new TipoProcessoDocumento();
			tipo.setIdTipoProcessoDocumento((Integer)r[0]);
			tipo.setTipoProcessoDocumento((String)r[1]);
			tipos.add(tipo);
		}
		
		return tipos;
	}
	
	@SuppressWarnings("unchecked")
	public List<AutoProcessualDTO> recuperarAutos(Integer idProcesso, boolean documentos, boolean movimentos, Search search){
		
		if(!documentos && !movimentos) {
			return new ArrayList<AutoProcessualDTO>(0);
		}
		
		boolean documentosMovimentos = (documentos && movimentos);
		Map<String, Object> parametrosPesquisa = new HashMap<String,Object>();
		Map<String, Object> parametrosPesquisaDocumentos = new HashMap<String,Object>();
		Map<String, Object> parametrosPesquisaMovimentos = new HashMap<String,Object>();

		Map<String, Order> orderBy = (search == null ? null : search.getOrders());
		String order = "DESC";

		if(orderBy != null && orderBy.size() > 0) {
			Order o = orderBy.entrySet().iterator().next().getValue();
			order = o.toString();
		}
		
		int first = (search == null ? 0 : (search.getFirst() ==  null ? 0 : search.getFirst()));
		int max = (search == null ? 0 : (search.getMax() == null ? 0 : search.getMax()));

		StringBuilder queryCompleta = new StringBuilder("select o.* from (");
		
		if(documentos) {
			if (Authenticator.isUsuarioExterno())
				queryCompleta.append(getSqlDocumentos(search, parametrosPesquisaDocumentos, !movimentos, true, true));
			else
				queryCompleta.append(getSqlDocumentos(search, parametrosPesquisaDocumentos, !movimentos, false, true));
		}
		if(documentosMovimentos) {
			queryCompleta.append(" union all ");
		}
		if(movimentos) {
			queryCompleta.append(getSqlMovimentos(search, parametrosPesquisaMovimentos, documentos, true));
		}
		queryCompleta.append(") as o ");
		
		queryCompleta.append(getOrderBy("o", order));
		
		Query query  = getEntityManager().createNativeQuery(queryCompleta.toString());

		parametrosPesquisa.putAll(parametrosPesquisaDocumentos);
		parametrosPesquisa.putAll(parametrosPesquisaMovimentos);

		setParameters(query, idProcesso, null, parametrosPesquisa, documentos);		

		query.setFirstResult(first);
		if(max != 0){
			query.setMaxResults(max);
		}

		return processarAutos(query.getResultList());
	}
	@SuppressWarnings("unchecked")
public List<AutoProcessualDTO> recuperarTodosAutos(Integer idProcesso, boolean documentos, boolean movimentos, Search search){
		
		if(!documentos && !movimentos) {
			return new ArrayList<>(0);
		}
		
		boolean documentosMovimentos = (documentos && movimentos);
		Map<String, Object> parametrosPesquisa = new HashMap<>();
		Map<String, Object> parametrosPesquisaDocumentos = new HashMap<>();
		Map<String, Object> parametrosPesquisaMovimentos = new HashMap<>();

		Map<String, Order> orderBy = (search == null ? null : search.getOrders());
		String order = "DESC";

		if(orderBy != null && orderBy.size() > 0) {
			Order o = orderBy.entrySet().iterator().next().getValue();
			order = o.toString();
		}
		
		

		StringBuilder queryCompleta = new StringBuilder(SELECT_O);
		
		if(documentos) {
			if (Authenticator.isUsuarioExterno())
				queryCompleta.append(getSqlDocumentos(search, parametrosPesquisaDocumentos, !movimentos, true, true));
			else
				queryCompleta.append(getSqlDocumentos(search, parametrosPesquisaDocumentos, !movimentos, false, true));
		}
		if(documentosMovimentos) {
			queryCompleta.append(UNION_ALL);
		}
		if(movimentos) {
			queryCompleta.append(getSqlMovimentos(search, parametrosPesquisaMovimentos, documentos, true));
		}
		queryCompleta.append(AS_O);
		
		queryCompleta.append(getOrderBy("o", order));
		
		Query query  = getEntityManager().createNativeQuery(queryCompleta.toString());

		parametrosPesquisa.putAll(parametrosPesquisaDocumentos);
		parametrosPesquisa.putAll(parametrosPesquisaMovimentos);

		setParameters(query, idProcesso, null, parametrosPesquisa, documentos);		

		return processarAutos(query.getResultList());
	}

	public Long countAutos(Integer idProcesso, boolean documentos, boolean movimentos, Search search) {
		if(!documentos && !movimentos) {
			return 0L;
		}

		Map<String, Object> parametrosPesquisa = new HashMap<String,Object>();
		Map<String, Object> parametrosPesquisaDocumentos = new HashMap<String,Object>();
		Map<String, Object> parametrosPesquisaMovimentos = new HashMap<String,Object>();
		boolean documentosMovimentos = (documentos && movimentos);
		
		StringBuilder queryCompleta = new StringBuilder("select count(1) from (");

		if(documentos) {
			queryCompleta.append(getSqlDocumentosCount(search, parametrosPesquisaDocumentos, !movimentos));
		}
		if(documentosMovimentos) {
			queryCompleta.append(" union all ");
		}
		if(movimentos) {
			queryCompleta.append(getSqlMovimentosCount(search, parametrosPesquisaMovimentos, (documentosMovimentos)));
		}
		queryCompleta.append(") as o ");

		Query query  = getEntityManager().createNativeQuery(queryCompleta.toString());
		
		parametrosPesquisa.putAll(parametrosPesquisaDocumentos);
		parametrosPesquisa.putAll(parametrosPesquisaMovimentos);
		
		setParameters(query, idProcesso, null, parametrosPesquisa, documentos);

		return ((BigInteger)query.getSingleResult()).longValue();
	}
	
	public Long countDocumentosNaoLidosAutos(Integer idProcesso) {		
		StringBuilder queryCompleta = new StringBuilder("select count(1) from (")
			.append(getSqlDocumentosPrincipaisNaoLidos())
			.append(") as o ");
		
		Query query  = getEntityManager().createNativeQuery(queryCompleta.toString());
		setParameters(query, idProcesso, null, new HashMap<>(), true);
		return ((BigInteger)query.getSingleResult()).longValue();
	}

	private AutoProcessualDTO getAuto(Integer idProcesso, Integer idDocumento, String order) {
		return getAuto(true, idProcesso, idDocumento, order, false, false);
	}
	
	@SuppressWarnings("unchecked")
	private AutoProcessualDTO getAuto(boolean desconsiderarExcluidos, Integer idProcesso, Integer idDocumento, String order, 
			boolean documentoPrincipal, Boolean buscarUltimo) {
		StringBuilder sql = new StringBuilder("select o.* from (");
		sql.append(getSqlDocumentos(null, null, true, desconsiderarExcluidos, true));
		sql.append(") as o ");
		sql.append("where 1=1 ");
		
		if(idDocumento != null) {
			sql.append("and o.id_processo_documento = :idDocumento ");
		}
		
		if(documentoPrincipal) {
			sql.append("and o.id_documento_principal is null ");
		}
		
		if(order != null) {
			sql.append(getOrderBy("o", order));
		}
		
		Query query  = getEntityManager().createNativeQuery(sql.toString());
		setParameters(query, idProcesso, (idDocumento == null ? null : idDocumento), null, true);
		Object[] r = null;
		
		query.setMaxResults(1);
		List<Object[]> resultList = query.getResultList();
		if (resultList.isEmpty()) {
			return new AutoProcessualDTO();
		}
		r = resultList.get(0);
		
		int indice = ((BigInteger)r[33]).intValue();
		Integer idDocumentoFavorito = (Integer)r[34];
		ProcessoDocumento documento = processarDocumento(new Processo(), r);
		AutoProcessualDTO auto = new AutoProcessualDTO(documento, null, indice, idDocumentoFavorito);
		
		if(auto != null && auto.getDocumento() != null) {
			recuperarAssinaturas(auto.getDocumento().getProcessoDocumentoBin());
		}
		return auto;
	}
	
	public void recuperarConteudoBinario(ProcessoDocumentoBin bin) {
		if(bin != null) {
			if(!bin.isBinario()) {
				String sql = "select ds_modelo_documento from tb_processo_documento_bin where id_processo_documento_bin = :id ";
				Query query  = getEntityManager().createNativeQuery(sql);
				query.setParameter("id", bin.getIdProcessoDocumentoBin());
				String html = (String)query.getSingleResult();
				bin.setModeloDocumento(html);
			}
			else {
				DocumentoBinManager manager = (DocumentoBinManager)Component.getInstance(DocumentoBinManager.class);
				try {
					byte[] conteudoBinario = manager.getData(bin.getNumeroDocumentoStorage());
					File arquivoBinario = File.createTempFile(Integer.toString(bin.getIdProcessoDocumentoBin()), ".tmp");
					FileUtils.writeByteArrayToFile(arquivoBinario, conteudoBinario);
					bin.setFile(arquivoBinario);
				} catch (PJeBusinessException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public String recuperarConteudoModelo(int idProcessoDocumentoBin) {
		String sql = "select ds_modelo_documento from tb_processo_documento_bin where id_processo_documento_bin = :id ";
		Query query  = getEntityManager().createNativeQuery(sql);
		query.setParameter("id", idProcessoDocumentoBin);
		return (String)query.getSingleResult();
	}

	public byte[] recuperarConteudoBinario(String numeroDocumentoStorage) {
		DocumentoBinManager manager = (DocumentoBinManager)Component.getInstance(DocumentoBinManager.class);
		byte[] conteudoBinario = null;
		try {
			conteudoBinario = manager.getData(numeroDocumentoStorage);
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
		return conteudoBinario;
	}
	
	@SuppressWarnings("unchecked")
	private void recuperarAssinaturas(ProcessoDocumentoBin bin) {
		if(bin != null) {
			String sql = "select id_processo_doc_bin_pessoa, ds_nome_pessoa, dt_assinatura, ds_assinatura, ds_cert_chain from tb_proc_doc_bin_pess_assin where id_processo_documento_bin = :id ";
			Query query  = getEntityManager().createNativeQuery(sql);
			query.setParameter("id", bin.getIdProcessoDocumentoBin());
				List<Object[]> rs = query.getResultList();
			if(rs != null && rs.size() > 0) {
				List<ProcessoDocumentoBinPessoaAssinatura> assinaturas = new ArrayList<ProcessoDocumentoBinPessoaAssinatura>(0);
				for (Object[] r : rs) {
					ProcessoDocumentoBinPessoaAssinatura assinatura = new ProcessoDocumentoBinPessoaAssinatura();
					assinatura.setIdProcessoDocumentoBinPessoaAssinatura((Integer)r[0]);
					assinatura.setNomePessoa((String)r[1]);
					assinatura.setDataAssinatura((java.util.Date)r[2]);
					assinatura.setAssinatura((String)r[3]);
					assinatura.setCertChain((String)r[4]);
					assinatura.setProcessoDocumentoBin(bin);
					assinaturas.add(assinatura);
				}
				bin.setSignatarios(assinaturas);
			}
		}
	}
	
	private String getOrderBy(String prefix, String order) {
		String orderBy = " order by (CASE WHEN o.id_documento_principal IS NULL THEN o.dt_juntada ELSE o.dt_juntada_principal END) #ORDER," +
				"(CASE WHEN o.id_documento_principal IS NULL THEN o.dt_inclusao ELSE o.dt_inclusao_principal END) #ORDER," +
				"(CASE WHEN o.id_documento_principal IS NULL THEN o.id_processo_documento ELSE o.id_documento_principal END) #ORDER, " +
				"o.id_documento_principal DESC, o.nr_ordem ASC";
		
		return orderBy.replace("o.", prefix + ".")
					.replace("#ORDER", order == null ? Strings.EMPTY : order)
					.replace("prd.dt_juntada_principal", "pri.dt_juntada")
					.replace("prd.dt_inclusao_principal", "pri.dt_inclusao");
	}

	private String getSqlPermiteVisualizarSigiloso() {
		StringBuilder query = new StringBuilder();

			query.append("         or ");
			query.append("         ( ");
			query.append("            (prd.in_documento_sigiloso = true or pri.in_documento_sigiloso = true) ");
			query.append("            and ");
			query.append("            ( ");
			query.append("               exists ");
			query.append("               ( ");
			query.append("                  select 1 from tb_proc_doc_visibi_segredo pdvs ");
			query.append("                  where pdvs.id_processo_documento = case ");
			query.append("												when prd.in_documento_sigiloso = false and pri.in_documento_sigiloso = true then"); 
			query.append("			                                    	pri.id_processo_documento"); 
			query.append("			                                    else ");
			query.append("			                                    	prd.id_processo_documento"); 
			query.append("			                                    end ");
			query.append("                  and ");
			query.append("                  ( ");
			query.append("                     pdvs.id_pessoa = :idUsuario ");
		
			query.append(getSqlProcuradoria());

			query.append("                  ) ");
			query.append("               ) ");
		
			query.append(getSqlVisualizaSigiloso());
		
			query.append("               or ");
			query.append("               ( ");
			query.append("                  prd.id_localizacao = :idLocalizacao ");
		
			query.append(getSqlAssistenteAdvogado());

			query.append(getSqlAssistenteProcuradoria());

			query.append("               ) ");
			query.append("            ) ");
			query.append("         ) ");
		
		return query.toString();
	}
	
	private String getSqlProcuradoria() {
		String query = null;
		
		if(idProcuradoria != null) {
			query = " or pdvs.id_procuradoria = :procuradoriaAtualUsuarioLogado ";
		}
		else {
			query = "";
		}
		
		return query;
	}

	private String getSqlUsuarioExterno() {
		String query = null;
		
		if (Authenticator.isUsuarioExterno()) {
			query = " and (not exists "
			+ " ( "
			+ "   select 1 "
			+ "   from tb_processo_expediente pe "
			+ "   inner join tb_proc_parte_expediente ppe on ppe.id_processo_expediente = pe.id_processo_expediente "
			+ "   where pe.id_processo_documento = prd.id_processo_documento "
			+ "   and pe.id_processo_trf = :idProcesso "
			+ "   and ppe.dt_ciencia_parte is null "
			+ "   and ppe.in_fechado = false "
			+ "   and (ppe.in_tipo_prazo != 'S' or ppe.qt_prazo_legal_parte > 0) "
			+ " )  ";
		
			query +=  " or exists( "
					+ "   select 1 from tb_proc_parte_expediente ppe2 "
					+ "   inner join tb_processo_expediente pe2 on "
					+ "   ppe2.id_processo_expediente = pe2.id_processo_expediente "
					+ "   where pe2.id_processo_documento = prd.id_processo_documento "
					+ "   and ppe2.dt_ciencia_parte is not null "
					+ "   and ( "
					+ "     ppe2.id_pessoa_ciencia  = :idUsuario " 
					+ (idProcuradoria != null ? " or ppe2.id_procuradoria = :procuradoriaAtualUsuarioLogado " : "")
					+ "     or ppe2.id_pessoa_parte = :idUsuario "
					+ "   ) "
					+ " ) "
					+ ") ";
		} else {
			query = "";
		}

		
		return query;
	}
	
	private String getSqlVisualizaSigiloso() {
		StringBuilder query = new StringBuilder();
		
		if(visualizaSigiloso) {
			// verifica se a pessoa é do juizo do processo + OJC do processo
			query.append(" OR ( ");
			if(!isServidorExclusivoOJC) {
				query.append(" 		oj.id_localizacao IN (:idsLocalizacoesFisicas) ");
			}else {
				query.append(" 1=1 ");
			}
			if(idOrgaoJulgadorColegiado != null) {
				query.append(" AND (:orgaoJulgadorColegiado = ptf.id_orgao_julgador_colegiado) ");
			}
			query.append("	) ");
			
			// verifica se a pessoa possui tarefa em sua unidade
			query.append(" OR ( ");
			query.append("	EXISTS (");
			query.append("		select 1 from tb_processo_instance pi ");
			query.append("		WHERE pi.id_processo = ptf.id_processo_trf ");
			if(!isServidorExclusivoOJC) {
				query.append("		AND pi.id_localizacao IN (:idsLocalizacoesFisicas) ");
			}
			if(idOrgaoJulgadorColegiado != null) {
				query.append(" 	AND (:orgaoJulgadorColegiado = pi.id_orgao_julgador_colegiado) ");
			}
			query.append("		) ");
			query.append("	) ");
		}
		
		return query.toString();
	}

	private String getSqlAssistenteAdvogado() {
		String query = null;
		boolean assistenteAdvogado = Authenticator.isAssistenteAdvogado(); 

		if(assistenteAdvogado) {
			query = " or prd.id_usuario_inclusao in "
			+ " ( "
			+ "    select ul.id_usuario "
			+ "    from tb_pessoa_assist_adv_local paal "
			+ "    inner join tb_usuario_localizacao ul on ul.id_usuario_localizacao = paal.id_pessoa_assit_adv_local "
			+ "    where ul.id_localizacao_fisica = :idLocalizacao "
			+ " ) ";
		}
		else {
			query = "";
		}
		
		return query;
	}

	private String getSqlAssistenteProcuradoria() {
		String query = null;
		boolean assistenteProcuradoria = Authenticator.isAssistenteProcurador(); 

		if(assistenteProcuradoria) {
			query = " or prd.id_usuario_inclusao in "
			+ " ( "
			+ "   select ul.id_usuario from tb_pess_assist_proc_local papl "
			+ "   inner join tb_usuario_localizacao ul on (ul.id_usuario_localizacao = papl.id_pessoa_assist_proc_local) "
			+ "   where ul.id_localizacao_fisica = :idLocalizacao AND papl.id_procuradoria = :procuradoriaAtualUsuarioLogado "
			+ " ) ";
		}
		else {
			query = "";
		}
		
		return query;
	}
	
	private String getCamposSqlDocumentos(boolean retornarConteudo) {

		return getCamposSqlDocumento(retornarConteudo);
	}

	private String getCamposSqlDocumento(boolean retornarConteudo) {
		String[] campos;
		if(retornarConteudo) {
			campos = new String[66];
		}
		else {
			campos = new String[65];	
		}

		campos[0] = "pro.id_processo";
		campos[1] = "pro.nr_processo";
		campos[2] = "prd.id_processo_documento";
		campos[3] = "prd.id_documento_principal";
		campos[4] = "prd.ds_processo_documento";
		campos[5] = "pri.ds_processo_documento as ds_documento_principal";
		campos[6] = "prd.dt_juntada";
		campos[7] = "pri.dt_juntada as dt_juntada_principal";
		campos[8] = "prd.dt_inclusao";
		campos[9] = "pri.dt_inclusao as dt_inclusao_principal";
		campos[10] = "prd.nr_ordem";
		campos[11] = "pri.nr_ordem as nr_ordem_principal";
		campos[12] = "prd.in_ativo as in_doc_ativo";
		campos[13] = "pri.in_ativo as in_doc_principal_ativo";
		campos[14] = "pre.id_processo_evento";
		campos[15] = "pre.dt_atualizacao";
		campos[16] = "pre.ds_texto_final_externo";
		campos[17] = "pre.ds_texto_final_interno";
		campos[18] = "pre.in_ativo as in_mov_ativo";
		campos[19] = "bin.id_processo_documento_bin";
		campos[20] = "bin.nm_arquivo";
		campos[21] = "bin.ds_extensao";
		campos[22] = "bin.in_binario";
		campos[23] = "bin.nr_documento_storage";
		campos[24] = "tpd.id_tipo_processo_documento";
		campos[25] = "tpd.ds_tipo_processo_documento";
		campos[26] = "bip.id_processo_documento_bin as id_documento_bin_principal";
		campos[27] = "bip.nm_arquivo as nm_arquivo_principal";
		campos[28] = "bip.ds_extensao as ds_extensao_principal";
		campos[29] = "bip.in_binario as in_binario_principal";
		campos[30] = "bip.nr_documento_storage as nr_storage_principal";
		campos[31] = "tpp.id_tipo_processo_documento as id_tipo_documento_principal";
		campos[32] = "tpp.ds_tipo_processo_documento as ds_tipo_documento_principal";
		campos[33] = String.format("row_number() over(%s) as indice", getOrderBy("prd", "ASC"));
		campos[34] = "fav.id_proc_documento_favorito";
		campos[35] = "uin.id_usuario as id_usuario_inclusao";
		campos[36] = "ual.id_usuario as id_usuario_alteracao";
		campos[37] = "uex.id_usuario as id_usuario_exclusao";
		campos[38] = "uin.ds_nome as ds_usuario_inclusao";
		campos[39] = "ual.ds_nome as ds_usuario_alteracao";
		campos[40] = "uex.ds_nome as ds_usuario_exclusao";
		campos[41] = "prd.ds_nome_usuario_inclusao";
		campos[42] = "prd.ds_nome_usuario_alteracao";
		campos[43] = "prd.ds_nome_usuario_exclusao";
		campos[44] = "prd.nr_documento";
		campos[45] = "prd.in_documento_sigiloso";
		campos[46] = "pri.nr_documento as nr_documento_principal";
		campos[47] = "pri.in_documento_sigiloso as in_documento_principal_sigiloso";
		campos[48] = "pap.id_papel";
		campos[49] = "pap.ds_identificador as ds_identificador_papel";
		campos[50] = "pap.ds_nome as ds_nome_papel";
		campos[51] = "prei.id_processo_evento as id_evento_principal";
		campos[52] = "prei.dt_atualizacao as dt_atualizacao_principal";
		campos[53] = "prei.ds_texto_final_externo as ds_texto_externo_principal";
		campos[54] = "prei.ds_texto_final_interno as ds_texto_interno_principal";
		campos[55] = "prei.in_ativo as in_mov_principal_ativo";
		campos[56] = "prd.ds_nome_usuario_juntada";
		campos[57] = "prd.ds_localizacao_usuario_juntada";
		campos[58] = "prd.id_usuario_juntada";
		campos[59] = "uju.ds_nome as ds_usuario_juntada";
		campos[60] = "prd.in_tipo_origem_juntada";
		campos[61] = "prd.id_localizacao";
		campos[62] = "loc.ds_localizacao";
		campos[63] = "prd.in_lido";
		campos[64] = "bin.nr_tamanho";
		if(retornarConteudo) {
			campos[65] = "bin.ds_modelo_documento";
		}

		return br.jus.pje.nucleo.util.StringUtil.join(campos, ",");
	}

	private String getCamposSqlDocumentoAnterior() {
		String[] campos = new String[2];
		campos[0] = "prd.id_processo_documento";
		campos[1] = String.format("lag(prd.id_processo_documento) over(%s) as id_documento_anterior", getOrderBy("prd", "ASC"));
		
		return br.jus.pje.nucleo.util.StringUtil.join(campos, ",");
	}

	private String getCamposSqlProximoDocumento() {
		String[] campos = new String[2];
		campos[0] = "prd.id_processo_documento";
		campos[1] = String.format("lead(prd.id_processo_documento) over(%s) as id_proximo_documento", getOrderBy("prd", "ASC"));
		
		return br.jus.pje.nucleo.util.StringUtil.join(campos, ",");
	}

	private String getCamposSqlMovimento(boolean retornarConteudo) {
		String[] campos;
		if(retornarConteudo) {
			campos = new String[66];
		}
		else {
			campos = new String[65];	
		}
		
		campos[0] = "pro.id_processo";
		campos[1] = "pro.nr_processo";
		campos[2] = "prdi.id_processo_documento";
		campos[3] = "prdi.id_documento_principal";
		campos[4] = "prdi.ds_processo_documento";
		campos[5] = "prdi.ds_processo_documento as ds_documento_principal";
		campos[6] = "pre.dt_atualizacao as dt_juntada";
		campos[7] = "pre.dt_atualizacao as dt_juntada_principal";
		campos[8] = "pre.dt_atualizacao as dt_inclusao";
		campos[9] = "pre.dt_atualizacao as dt_inclusao_principal";
		campos[10] = "prd.nr_ordem";
		campos[11] = "prd.nr_ordem as nr_ordem_principal";
		campos[12] = "prd.in_ativo as in_doc_ativo";
		campos[13] = "prd.in_ativo as in_doc_principal_ativo";
		campos[14] = "pre.id_processo_evento";
		campos[15] = "pre.dt_atualizacao";
		campos[16] = "pre.ds_texto_final_externo";
		campos[17] = "pre.ds_texto_final_interno";
		campos[18] = "pre.in_ativo as in_mov_ativo";
		campos[19] = "bin.id_processo_documento_bin";
		campos[20] = "bin.nm_arquivo";
		campos[21] = "bin.ds_extensao";
		campos[22] = "bin.in_binario";
		campos[23] = "bin.nr_documento_storage";
		campos[24] = "tpd.id_tipo_processo_documento";
		campos[25] = "tpd.ds_tipo_processo_documento";
		campos[26] = "bin.id_processo_documento_bin as id_documento_bin_principal";
		campos[27] = "bin.nm_arquivo as nm_arquivo_principal";
		campos[28] = "bin.ds_extensao as ds_extensao_principal";
		campos[29] = "bin.in_binario as in_binario_principal";
		campos[30] = "bin.nr_documento_storage as nr_storage_principal";
		campos[31] = "tpd.id_tipo_processo_documento as id_tipo_documento_principal";
		campos[32] = "tpd.ds_tipo_processo_documento as ds_tipo_documento_principal";
		campos[33] = "row_number() over(order by pre.id_processo_evento ASC) as indice";
		campos[34] = "fav.id_proc_documento_favorito";
		campos[35] = "ul.id_usuario as id_usuario_inclusao";
		campos[36] = "ul.id_usuario as id_usuario_alteracao";
		campos[37] = "ul.id_usuario as id_usuario_exclusao";
		campos[38] = "ul.ds_nome as ds_usuario_inclusao";
		campos[39] = "ul.ds_nome as ds_usuario_alteracao";
		campos[40] = "ul.ds_nome as ds_usuario_exclusao";
		campos[41] = "pre.ds_nome_usuario as ds_nome_usuario_inclusao";
		campos[42] = "pre.ds_nome_usuario as ds_nome_usuario_alteracao";
		campos[43] = "pre.ds_nome_usuario as ds_nome_usuario_exclusao";
		campos[44] = "prd.nr_documento";
		campos[45] = "prd.in_documento_sigiloso";
		campos[46] = "prd.nr_documento as nr_documento_principal";
		campos[47] = "prd.in_documento_sigiloso as in_documento_principal_sigiloso";
		campos[48] = "pap.id_papel";
		campos[49] = "pap.ds_identificador as ds_identificador_papel";
		campos[50] = "pap.ds_nome as ds_nome_papel";
		campos[51] = "pre.id_processo_evento as id_evento_principal";
		campos[52] = "pre.dt_atualizacao as dt_atualizacao_principal";
		campos[53] = "pre.ds_texto_final_externo as ds_texto_externo_principal";
		campos[54] = "pre.ds_texto_final_interno as ds_texto_interno_principal";
		campos[55] = "pre.in_ativo as in_mov_principal_ativo";
        campos[56] = "prd.ds_nome_usuario_juntada";
		campos[57] = "prd.ds_localizacao_usuario_juntada";
		campos[58] = "prd.id_usuario_juntada";
		campos[59] = "ul.ds_nome as ds_usuario_juntada";
		campos[60] = "prd.in_tipo_origem_juntada";
		campos[61] = "prd.id_localizacao";
		campos[62] = "loc.ds_localizacao";
		campos[63] = "prd.in_lido";
		campos[64] = "null as nr_tamanho";
		if(retornarConteudo) {
			campos[65] = "null as ds_modelo_documemto";
		}
		return br.jus.pje.nucleo.util.StringUtil.join(campos, ",");
	}

	private String getSqlFrom(boolean somenteDocumentos) {
		StringBuilder from = new StringBuilder();

		from.append("from tb_processo_documento prd ");
		from.append("inner join tb_tipo_processo_documento tpd on tpd.id_tipo_processo_documento = prd.id_tipo_processo_documento ");
		from.append("inner join tb_processo_documento_bin bin on bin.id_processo_documento_bin = prd.id_processo_documento_bin ");
		from.append("inner join tb_processo_trf ptf on prd.id_processo = ptf.id_processo_trf ");
		from.append("inner join tb_orgao_julgador oj on (oj.id_orgao_julgador = ptf.id_orgao_julgador) ");
		from.append("inner join tb_processo pro on pro.id_processo = ptf.id_processo_trf ");
		from.append("left join tb_processo_documento pri on pri.id_processo_documento = prd.id_documento_principal ");
		if(somenteDocumentos) {
			from.append("left join tb_processo_evento pre on pre.id_processo_evento is null ");
			from.append("left join tb_processo_evento prei on prei.id_processo_evento is null ");
		}
		else {
			from.append("left join tb_processo_evento pre on pre.id_processo = :idProcesso and pre.id_processo_documento = prd.id_processo_documento ");
			from.append("left join tb_processo_evento prei on prei.id_processo = :idProcesso and prei.id_processo_documento = pri.id_processo_documento ");
		}
		from.append("left join tb_tipo_processo_documento tpp on tpp.id_tipo_processo_documento = pri.id_tipo_processo_documento ");
		from.append("left join tb_processo_documento_bin bip on bip.id_processo_documento_bin = pri.id_processo_documento_bin ");
		from.append("left join tb_proc_documento_favorito fav on fav.id_processo_documento = prd.id_processo_documento and fav.id_usuario = :idUsuario ");
		from.append("left join tb_usuario_login uin on uin.id_usuario = prd.id_usuario_inclusao ");
		from.append("left join tb_usuario_login ual on ual.id_usuario = prd.id_usuario_alteracao ");
		from.append("left join tb_usuario_login uex on uex.id_usuario = prd.id_usuario_exclusao ");
		from.append("left join tb_usuario_login uju on uju.id_usuario = prd.id_usuario_juntada ");
		from.append("left join tb_papel pap on pap.id_papel = prd.id_papel ");
		from.append("left join tb_localizacao loc on loc.id_localizacao = prd.id_localizacao ");
		
		return from.toString();
	}
	
	private String getSqlWhere() {
		return getSqlWhere(false);
	}
	
	private String getSqlWhere(boolean desconsideraExcluidos) {
		StringBuilder where = new StringBuilder();

		where.append("where pro.id_processo = :idProcesso ");
		where.append("and (prd.id_tipo_processo_documento != :idTipoDocumentoProtocoloDistribuicao) ");
		where.append("and ");
		where.append("( ");
		where.append("   ( ");
		where.append("      prd.dt_juntada is not null ");
		where.append("      and (prd.id_documento_principal is null or pri.dt_juntada is not null) ");

		where.append("      and not exists ");
		where.append("      ( ");
		where.append("      	select 1 from tb_processo_expediente pe ");
		where.append("      	where pe.id_processo_documento = prd.id_processo_documento ");
		where.append("      	and pe.in_documento_existente = true ");
		where.append("      	and (pe.id_proc_documento_vinculado IS NOT NULL AND pe.id_proc_documento_vinculado != pe.id_processo_documento) ");
		where.append("      	and pe.id_processo_trf = :idProcesso ");
		where.append("      ) ");
		
		where.append(getSqlUsuarioExterno());
		
		where.append("      and ");
		where.append("      ( ");
		where.append("         (prd.in_documento_sigiloso = false and (pri is null or pri.in_documento_sigiloso = false)) ");
		
		where.append(getSqlPermiteVisualizarSigiloso());
		
		where.append("      ) ");
		where.append("   ) ");
		where.append(") ");
		if(desconsideraExcluidos) {
			where.append(" and prd.dt_exclusao is null ");
		}
		
		return where.toString();
	}
	
	private String getSqlDocumentos(Search search, Map<String, Object> parametrosPesquisa, boolean somenteDocumentos, 
			boolean desconsideraExcluidos, boolean retornarConteudo) {
		Collection<Criteria> criteriosPesquisa = (search == null ? null : search.getCriterias().values());
		StringBuilder query = new StringBuilder();
		query.append("select ");
		query.append(getCamposSqlDocumentos(retornarConteudo) + " ");
		query.append(getSqlFrom(somenteDocumentos));
		query.append(getSqlWhere(desconsideraExcluidos));
		query.append(limitarCriteriosPesquisa(criteriosPesquisa, parametrosPesquisa, false));
		return query.toString();
	}
	
	private String getSqlDocumentosCount(Search search, Map<String, Object> parametrosPesquisa, boolean somenteDocumentos) {
		Collection<Criteria> criteriosPesquisa = (search == null ? null : search.getCriterias().values());
		StringBuilder query = new StringBuilder();

		query.append("select 1 ");
		query.append(getSqlFrom(somenteDocumentos));
		query.append(getSqlWhere(search == null ? true : false));
		query.append(limitarCriteriosPesquisa(criteriosPesquisa, parametrosPesquisa, false));
		return query.toString();
	}
	
	private String getSqlDocumentosPrincipaisNaoLidos() {
		StringBuilder query = new StringBuilder("select distinct prd.id_processo_documento ");
		query.append("from tb_processo_documento prd ");
		query.append("inner join tb_processo_trf ptf on prd.id_processo = ptf.id_processo_trf ");
		query.append("inner join tb_processo pro on prd.id_processo = pro.id_processo ");
		query.append("inner join tb_orgao_julgador oj on oj.id_orgao_julgador = ptf.id_orgao_julgador ");
		query.append("left join tb_processo_documento pri on pri.id_processo_documento = prd.id_documento_principal ");
		query.append("left join tb_localizacao loc on loc.id_localizacao = oj.id_localizacao ");
		
		query.append(getSqlWhere(true));
		
		query.append("and prd.in_lido = false ");
		query.append("and prd.id_papel in (" + 
			StringUtil.listToString(ComponentUtil.getPapelManager().getPapeisParaDocumentosNaoLidos()
				.stream().map(Papel::getIdPapel).collect(Collectors.toList())) + ") ");
		
		query.append(" and prd.id_documento_principal is null ");
		query.append(" and prd.dt_inclusao > ptf.dt_autuacao ");
		if(!isServidorExclusivoOJC) {
			query.append(" and loc.id_localizacao in (" + StringUtil.listToString(this.idsLocalizacoesFisicasList) + ") ");
		}
		if(idOrgaoJulgadorColegiado != null) {
			query.append("and ptf.id_orgao_julgador_colegiado = " + idOrgaoJulgadorColegiado.toString() + "");
		}
		
		return query.toString();
	}

	private String getSqlProximoDocumento() {
		StringBuilder query = new StringBuilder();
		query.append("select ");
		query.append(getCamposSqlProximoDocumento() + " ");
		query.append(getSqlFrom(true));
		query.append(getSqlWhere(true));
		return query.toString();
	}

	private String getSqlDocumentoAnterior() {
		StringBuilder query = new StringBuilder();
		query.append("select ");
		query.append(getCamposSqlDocumentoAnterior() + " ");
		query.append(getSqlFrom(true));
		query.append(getSqlWhere(true));
		return query.toString();
	}


	private String getSqlMovimentos(Search search, Map<String, Object> parametrosPesquisa, boolean somenteMovimentosAvulsos, boolean retornarConteudo) {

		Collection<Criteria> criteriosPesquisa = (search == null ? null : search.getCriterias().values());
		StringBuilder query = new StringBuilder();
		
		query.append("select " );
		
		query.append(getCamposSqlMovimento(retornarConteudo) + " ");

		query.append("from tb_processo_evento pre ");
		query.append("inner join tb_processo pro on pro.id_processo = pre.id_processo ");
		query.append("inner join tb_processo_trf ptf on pre.id_processo = ptf.id_processo_trf ");
		query.append("inner join tb_orgao_julgador oj on oj.id_orgao_julgador = ptf.id_orgao_julgador ");
		query.append("inner join tb_evento ev on pre.id_evento = ev.id_evento ");
		query.append("left join tb_usuario_login ul on ul.id_usuario = pre.id_usuario ");

		// Faz joins "falsos" com as tabelas referentes a documentos apenas para atender restrição do entitymanager,
		// que não permite o uso de colunas artificiais nas queries. Por exemplo:
		// select 0 as id_processo_documento -- ERRO!
		query.append("left join tb_processo_documento prdi on prdi.id_processo_documento is null ");
		query.append("left join tb_processo_documento prd on pre.id_processo = pro.id_processo and pre.id_processo_documento = prd.id_processo_documento ");
		query.append("left join tb_processo_documento pri on pri.id_processo_documento = prd.id_documento_principal ");
		query.append("left join tb_processo_documento_bin bin on bin.id_processo_documento_bin is null "); 
		query.append("left join tb_tipo_processo_documento tpd on tpd.id_tipo_processo_documento is null ");
		query.append("left join tb_proc_documento_favorito fav on fav.id_proc_documento_favorito is null ");
		query.append("left join tb_papel pap on pap.id_papel is null ");
		query.append("left join tb_localizacao loc on loc.id_localizacao is null ");

		query.append("where pro.id_processo = :idProcesso ");
		query.append(getSqlWhereMovimentos());

		query.append(limitarCriteriosPesquisa(criteriosPesquisa, parametrosPesquisa, true));
		
		if(somenteMovimentosAvulsos) {
			query.append("  and ( pre.id_processo_documento is null or ");
			query.append(" exists ( select 1 from tb_processo_expediente pe ");
			query.append("     where pe.id_processo_documento = pre.id_processo_documento ");
			query.append(WHERE_N_DOCUMENTO_EXISTENTE);
			query.append("     and (pe.id_proc_documento_vinculado IS NOT NULL AND pe.id_proc_documento_vinculado != pe.id_processo_documento) ");
			query.append(WHERE_ID_PROCESSO_TRF);
		}
		return query.toString();
	}

	private String getSqlWhereMovimentos() {
		StringBuilder query = new StringBuilder();
		query.append(" AND ");
		query.append(" ( ");
		query.append("	(prd.id_processo_documento is null ");
		if (Authenticator.isUsuarioExterno()) {
			query.append("AND pre.in_visibilidade_externa = true ");
		}
		query.append(getSqlMovimentoSigiloso());
		query.append("  ) OR (prd.in_documento_sigiloso = false and (pri is null or pri.in_documento_sigiloso = false)) ");
		query.append(getSqlPermiteVisualizarSigiloso());
		query.append(" ) ");
		return query.toString();
	}

	private String getSqlMovimentoSigiloso() {
		StringBuilder query = new StringBuilder();
		query.append("AND ( ");
		query.append("ev.in_segredo_justica = false ");
		if(visualizaSigiloso && (!isServidorExclusivoOJC || idOrgaoJulgadorColegiado != null)) {
			query.append("OR ( ");
			query.append("	ev.in_segredo_justica = true ");
			if(!isServidorExclusivoOJC) {
				query.append(" AND oj.id_localizacao IN (:idsLocalizacoesFisicas) ");
			}
			if(idOrgaoJulgadorColegiado != null) {
				query.append(" AND (:orgaoJulgadorColegiado = ptf.id_orgao_julgador_colegiado) ");
			}
			query.append(") ");
		}
		query.append(") ");
		return query.toString();
	}

	private String getSqlMovimentosCount(Search search, Map<String, Object> parametrosPesquisa, boolean somenteMovimentosAvulsos) {

		Collection<Criteria> criteriosPesquisa = (search == null ? null : search.getCriterias().values());
		StringBuilder query = new StringBuilder();
		
		query.append("select 1 " );
		
		query.append("from tb_processo_evento pre ");
		query.append("inner join tb_processo pro on pro.id_processo = pre.id_processo ");
		query.append("inner join tb_processo_trf ptf on pre.id_processo = ptf.id_processo_trf ");
		query.append("inner join tb_orgao_julgador oj on oj.id_orgao_julgador = ptf.id_orgao_julgador ");
		query.append("inner join tb_evento ev on pre.id_evento = ev.id_evento ");
		query.append("left join tb_processo_documento prd on pre.id_processo = pro.id_processo and pre.id_processo_documento = prd.id_processo_documento ");
		query.append("left join tb_processo_documento pri on pri.id_processo_documento = prd.id_documento_principal ");
		query.append("left join tb_usuario_login ul on ul.id_usuario = pre.id_usuario ");

		// Faz joins "falsos" com as tabelas referentes a documentos apenas para atender restrição do entitymanager,
		// que não permite o uso de colunas artificiais nas queries. Por exemplo:
		// select 0 as id_processo_documento -- ERRO!
		query.append("left join tb_processo_documento_bin bin on bin.id_processo_documento_bin is null ");
		query.append("left join tb_tipo_processo_documento tpd on tpd.id_tipo_processo_documento is null ");
		query.append("left join tb_proc_documento_favorito fav on fav.id_proc_documento_favorito is null ");
		query.append("left join tb_papel pap on pap.id_papel is null ");
		query.append("left join tb_localizacao loc on loc.id_localizacao is null ");

		query.append("where pro.id_processo = :idProcesso ");
		query.append(getSqlWhereMovimentos());
		query.append(" and prd.dt_exclusao is null ");
		
		query.append(limitarCriteriosPesquisa(criteriosPesquisa, parametrosPesquisa, true));
		
		if(somenteMovimentosAvulsos) {
			query.append("and ( pre.id_processo_documento is null or ");
			query.append(" exists ( select 1 from tb_processo_expediente ");
			query.append("      	where id_processo_documento = pre.id_processo_documento ");
			query.append("          and in_documento_existente = true ");
			query.append(" and id_processo_trf = :idProcesso ) ) ");
		}
		return query.toString();
	}

	
	private void setParameters(Query query, Integer idProcesso, Integer idDocumento, Map<String, Object> parametrosPesquisa, boolean processaDocumentos) {
		query.setParameter("idProcesso", idProcesso);
		if(idDocumento != null && !idDocumento.equals(0)) query.setParameter("idDocumento", idDocumento);

		query.setParameter("idUsuario", idUsuario);
			if(visualizaSigiloso) {
				if(!isServidorExclusivoOJC) {
					query.setParameter("idsLocalizacoesFisicas", idsLocalizacoesFisicasList);
				}
				if(idOrgaoJulgadorColegiado != null) {
					query.setParameter("orgaoJulgadorColegiado", idOrgaoJulgadorColegiado);
				}
		}
		if(idProcuradoria != null) query.setParameter("procuradoriaAtualUsuarioLogado", idProcuradoria);

		query.setParameter("idLocalizacao", Authenticator.getIdLocalizacaoFisicaAtual());

		if(processaDocumentos) {
			query.setParameter("idTipoDocumentoProtocoloDistribuicao", ComponentUtil.getParametroUtil().getIdTipoDocumentoProtocoloDistribuicao());
		}
		
		if(parametrosPesquisa != null) {
			for(String key: parametrosPesquisa.keySet()){
				query.setParameter(key, parametrosPesquisa.get(key));
			}
		}
	}

	private String limitarCriteriosPesquisa(Collection<Criteria> criteriosPesquisa, Map<String, Object> parametrosPesquisa, boolean movimentoAvulso) {
		if(criteriosPesquisa != null && criteriosPesquisa.size() > 0) {
			Collection<Criteria> criterios = new ArrayList<Criteria>(criteriosPesquisa); 
			for (Criteria criteria : criteriosPesquisa) {
				if(movimentoAvulso && "movimento.textoFinalExterno".equals(criteria.getAttribute())) {
					criterios = new ArrayList<Criteria>(0);
					List<Object> value = criteria.getValue();
					if(value != null && value.size() > 0) {
						criterios.add(Criteria.contains("movimento.textoFinalExterno", value.get(0).toString()));
					}
				}
				if (criteria.getValue()!=null && criteria.getValue().size() > 1){
					limitarCriteriosPesquisaLista(criteria,criterios);
				}
			}
			StringBuilder sbPesquisa = new StringBuilder();
			return loadNativeCriterias(sbPesquisa, criterios, parametrosPesquisa);
		}
		return "";
	}
	
	private void limitarCriteriosPesquisaLista(Criteria criteria,Collection<Criteria> criterios){
		List<Object> values = criteria.getValue(); 
		int i = 0;
		for (Object object : values) {
			if(object.toString().contains("documento.idProcessoDocumento")) {
				criteria.getValue().set(i,Criteria.startsWith("documento.idProcessoDocumentoStr", ((Criteria) object).getValue().get(0).toString()));
			}
			i++;
		}
	}

	private String loadNativeCriterias(StringBuilder sb, Collection<Criteria> criterias, Map<String, Object> params) {
		String str = null;
		if(criterias != null && criterias.size() > 0) {
			super.loadCriterias(sb, criterias, params);
			str = translateToNative(sb.toString());
		}
		return str;
	}
	
	private String translateToNative(String str) {
		Map<String, String> columns = new LinkedHashMap<String, String>();
		columns.put("o.documento.idProcessoDocumentoStr", "cast(prd.id_processo_documento as varchar)");
		columns.put("o.documento.idProcessoDocumento", "prd.id_processo_documento");
		columns.put("o.documento.dataJuntada", "prd.dt_juntada");
		columns.put("o.documento.processoDocumento", "prd.ds_processo_documento");
		columns.put("o.documento.documentoPrincipal", "pri.ds_processo_documento");
		columns.put("o.documento.tipoProcessoDocumento.idTipoProcessoDocumento", "tpd.id_tipo_processo_documento");
		columns.put("o.documento.tipoProcessoDocumento.tipoProcessoDocumento", "tpd.ds_tipo_processo_documento");
		columns.put("o.documento.dataExclusao", "prd.dt_exclusao");
		columns.put("o.movimento.textoFinalExterno", "pre.ds_texto_final_externo");
		
		for(String key: columns.keySet()){
	        if(str.contains(key)) {
        		str = str.replaceAll(key, columns.get(key).toString());
        	}
		}

	    return " and " + str + " ";
	}

	/**
	 * -------------------------------
	 * Ordem dos campos no resultList:
	 * -------------------------------
	 * 0 - id_processo
	 * 1 - nr_processo
	 * 2 - id_processo_documento
	 * 3 - id_documento_principal
	 * 4 - ds_processo_documento
	 * 5 - ds_documento_principal 
	 * 6 - dt_juntada 
	 * 7 - dt_juntada_principal
	 * 8 - dt_inclusao
	 * 9 - dt_inclusao_principal
	 * 10 - nr_ordem
	 * 11 - nr_ordem_principal
	 * 12 - in_doc_ativo
	 * 13 - in_doc_principal_ativo
	 * 14 - id_processo_evento 
	 * 15 - dt_atualizacao 
	 * 16 - ds_texto_final_externo 
	 * 17 - ds_texto_final_interno
	 * 18 - in_mov_ativo
	 * 19 - id_processo_documento_bin
	 * 20 - nm_arquivo
	 * 21 - ds_extensao
	 * 22 - in_binario
	 * 23 - nr_documento_storage
	 * 24 - id_tipo_processo_documento
	 * 25 - ds_tipo_processo_documento
	 * 26 - id_documento_bin_principal
	 * 27 - nm_arquivo_principal
	 * 28 - ds_extensao_principal
	 * 29 - in_binario_principal
	 * 30 - nr_storage_principal
	 * 31 - id_tipo_documento_principal
	 * 32 - ds_tipo_documento_principal
	 * 33 - indice
	 * 34 - id_proc_documento_favorito
	 * 35 - id_usuario_inclusao
	 * 36 - id_usuario_alteracao
	 * 37 - id_usuario_exclusao
	 * 38 - ds_usuario_inclusao
	 * 39 - ds_usuario_alteracao
	 * 40 - ds_usuario_exclusao
	 * 41 - ds_nome_usuario_inclusao
	 * 42 - ds_nome_usuario_alteracao
	 * 43 - ds_nome_usuario_exclusao
	 * 44 - nr_documento
	 * 45 - in_documento_sigiloso
	 * 46 - nr_documento_principal
	 * 47 - in_documento_principal_sigiloso
	 * 48 - id_papel
	 * 49 - ds_identificador_papel
	 * 50 - ds_nome_papel
	 * 51 - id_evento_principal
	 * 52 - dt_atualizacao_principal
	 * 53 - ds_texto_externo_principal
	 * 54 - ds_texto_interno_principal
	 * 55 - in_mov_principal_ativo
	 * 56 - ds_nome_usuario_juntada
	 * 57 - ds_localizacao_usuario_juntada
	 * 58 - id_usuario_juntada
	 * 59 - ds_usuario_juntada
	 * 60 - in_tipo_origem_juntada
	 * 61 - id_localizacao
	 */
	private List<AutoProcessualDTO> processarAutos(List<Object[]> resultList) {
		Map<String, AutoProcessualDTO> autos = new LinkedHashMap<String,AutoProcessualDTO>(0);
		List<Integer> idsMovimentosVinculados = new ArrayList<Integer>(0);
		for (Object[] r: resultList) {
			int indice = ((BigInteger)r[33]).intValue();
			Integer idDocumentoFavorito = (Integer)r[34];
			Processo processo = new Processo();
			processo.setIdProcesso((Integer)r[0]);
			processo.setNumeroProcesso((String)r[1]);
			
			Usuario usuario = (r[35] == null ? null : new Usuario());
			
			if(usuario != null) {
				usuario.setIdUsuario((Integer)r[35]);
				usuario.setNome((String)r[38]);
			}

			//Verifica se o auto do processo é um documento
			if(r[2] != null && !((Integer)r[2]).equals(0)) {
				ProcessoDocumento documento = processarDocumento(processo, r);
				
				//Trata-se de um documento principal
				if(documento.getDocumentoPrincipal() ==  null) {
					//Verifica se possui movimentação processual vinculada ao documento
					if(r[14] != null) {
						
						Object[] rmov = new Object[5];
						rmov[0] = r[14];
						rmov[1] = r[15];
						rmov[2] = r[16];
						rmov[3] = r[17];
						rmov[4] = r[18];

						ProcessoEvento movimento = processarMovimento(processo, usuario, rmov);
						movimento.setProcessoDocumento(documento);

						AutoProcessualDTO auto = autos.get("d" + documento.getIdProcessoDocumento());
						if(auto != null) {
							documento.setProcessoEventoList(auto.getDocumento().getProcessoEventoList());
							auto.getDocumento().setLido(documento.getLido());
						}
						if(!idsMovimentosVinculados.contains(movimento.getIdProcessoEvento())) {
							documento.getProcessoEventoList().add(movimento);
							idsMovimentosVinculados.add(movimento.getIdProcessoEvento());
						}
					}

					if(!autos.containsKey("d"+documento.getIdProcessoDocumento())) {
						autos.put("d"+documento.getIdProcessoDocumento(), new AutoProcessualDTO(documento, null, indice, idDocumentoFavorito));
					} else {
						autos.get("d"+documento.getIdProcessoDocumento()).getDocumento().setLido(documento.getLido());
					}
				}
				//Trata-se de um anexo de documento principal
				else {
					ProcessoDocumento documentoPrincipal = null;
					AutoProcessualDTO auto = autos.get("d" + documento.getDocumentoPrincipal().getIdProcessoDocumento());
					if(auto == null) {
						documentoPrincipal = documento.getDocumentoPrincipal();
					}
					else {
						documentoPrincipal = auto.getDocumento();
					}
					documentoPrincipal.getDocumentosVinculados().add(documento);

					//Verifica se possui movimentação processual vinculada ao documento
					if(r[51] != null) {
						if(!idsMovimentosVinculados.contains(r[51])) {
							Object[] rmov = new Object[5];
							rmov[0] = r[51];
							rmov[1] = r[52];
							rmov[2] = r[53];
							rmov[3] = r[54];
							rmov[4] = r[55];
							ProcessoEvento movimento = processarMovimento(processo, usuario, rmov);
							movimento.setProcessoDocumento(documentoPrincipal);
							documentoPrincipal.getProcessoEventoList().add(movimento);
							idsMovimentosVinculados.add(movimento.getIdProcessoEvento());
						}
					}

					if(!autos.containsKey("d"+documentoPrincipal.getIdProcessoDocumento())) {
						autos.put("d"+documentoPrincipal.getIdProcessoDocumento(), new AutoProcessualDTO(documentoPrincipal, null, indice, idDocumentoFavorito));
					}
				}
			}
			//Verifica se o auto do processo é um movimento avulso, ou seja, não vinculado a um documento processual
			else {
				Object[] rmov = new Object[5];
				rmov[0] = r[14];
				rmov[1] = r[15];
				rmov[2] = r[16];
				rmov[3] = r[17];
				rmov[4] = r[18];

				ProcessoEvento movimento = processarMovimento(processo, usuario, rmov);
				autos.put("m"+movimento.getIdProcessoEvento(), new AutoProcessualDTO(null, movimento, indice, null));
			}
		}
		
		List<AutoProcessualDTO> autosList = new LinkedList<AutoProcessualDTO>();
		for (String key: autos.keySet()) {
			autosList.add(autos.get(key));
		}
		return autosList; 	
	}
	
	/**
	 * -------------------------------
	 * Ordem dos campos no resultList:
	 * -------------------------------
	 * 0 - id_processo
	 * 1 - nr_processo
	 * 2 - id_processo_documento
	 * 3 - id_documento_principal
	 * 4 - ds_processo_documento
	 * 5 - ds_documento_principal 
	 * 6 - dt_juntada 
	 * 7 - dt_juntada_principal
	 * 8 - dt_inclusao
	 * 9 - dt_inclusao_principal
	 * 10 - nr_ordem
	 * 11 - nr_ordem_principal
	 * 12 - in_doc_ativo
	 * 13 - in_doc_principal_ativo
	 * 14 - id_processo_evento 
	 * 15 - dt_atualizacao 
	 * 16 - ds_texto_final_externo 
	 * 17 - ds_texto_final_interno
	 * 18 - in_mov_ativo
	 * 19 - id_processo_documento_bin
	 * 20 - nm_arquivo
	 * 21 - ds_extensao
	 * 22 - in_binario
	 * 23 - nr_documento_storage
	 * 24 - id_tipo_processo_documento
	 * 25 - ds_tipo_processo_documento
	 * 26 - id_documento_bin_principal
	 * 27 - nm_arquivo_principal
	 * 28 - ds_extensao_principal
	 * 29 - in_binario_principal
	 * 30 - nr_storage_principal
	 * 31 - id_tipo_documento_principal
	 * 32 - ds_tipo_documento_principal
	 * 33 - indice
	 * 34 - id_proc_documento_favorito
	 * 35 - id_usuario_inclusao
	 * 36 - id_usuario_alteracao
	 * 37 - id_usuario_exclusao
	 * 38 - ds_usuario_inclusao
	 * 39 - ds_usuario_alteracao
	 * 40 - ds_usuario_exclusao
	 * 41 - ds_nome_usuario_inclusao
	 * 42 - ds_nome_usuario_alteracao
	 * 43 - ds_nome_usuario_exclusao
	 * 44 - nr_documento
	 * 45 - in_documento_sigiloso
	 * 46 - nr_documento_principal
	 * 47 - in_documento_principal_sigiloso
	 * 48 - id_papel
	 * 49 - ds_identificador_papel
	 * 50 - ds_nome_papel
	 * 51 - id_evento_principal
	 * 52 - dt_atualizacao_principal
	 * 53 - ds_texto_externo_principal
	 * 54 - ds_texto_interno_principal
	 * 55 - in_mov_principal_ativo
	 * 56 - ds_nome_usuario_juntada
	 * 57 - ds_localizacao_usuario_juntada
	 * 58 - id_usuario_juntada
	 * 59 - ds_usuario_juntada
	 * 60 - in_tipo_origem_juntada
	 * 61 - id_localizacao
	 * 62 - ds_localizacao
	 */
	private ProcessoDocumento processarDocumento(Processo processo, Object[] r) {
		Usuario usuarioInclusao = (r[35] == null ? null : new Usuario());
		Usuario usuarioAlteracao = (r[36] == null ? null : new Usuario());
		Usuario usuarioExclusao = (r[37] == null ? null : new Usuario());
		Usuario usuarioJuntada = (r[58] == null ? null : new Usuario());
		Papel papel = (r[48] == null ? null : new Papel());
		Localizacao localizacao = (r[61] == null ? null : new Localizacao());
		
		if(usuarioInclusao != null) {
			usuarioInclusao.setIdUsuario((Integer)r[35]);
			usuarioInclusao.setNome((String)r[38]);
		}
		
		if(usuarioAlteracao != null) {
			usuarioAlteracao.setIdUsuario((Integer)r[36]);
			usuarioAlteracao.setNome((String)r[39]);
		}
		
		if(usuarioExclusao != null) {
			usuarioExclusao.setIdUsuario((Integer)r[37]);
			usuarioExclusao.setNome((String)r[40]);
		}

		if(usuarioJuntada != null) {
			usuarioJuntada.setIdUsuario((Integer)r[58]);
			usuarioJuntada.setNome((String)r[59]);
		}
		
		if(papel != null) {
			papel.setIdPapel((Integer)r[48]);
			papel.setIdentificador((String)r[49]);
			papel.setNome((String)r[50]);
		}
		
		if(localizacao != null) {
			localizacao.setIdLocalizacao((Integer) r[61]);
			localizacao.setLocalizacao((String) r[62]);
		}
		
		ProcessoDocumentoBin bin = new ProcessoDocumentoBin();
		bin.setIdProcessoDocumentoBin((Integer)r[19]);
		bin.setNomeArquivo((String)r[20]);
		bin.setExtensao((String)r[21]);
		bin.setBinario((Boolean)r[22]);
		bin.setNumeroDocumentoStorage((String)r[23]);

		bin.setSize((Integer)r[64]);
		if(r.length == 66) {
			bin.setModeloDocumento((String) r[65]);
		}

		TipoProcessoDocumento tipoDocumento = new TipoProcessoDocumento();
		tipoDocumento.setIdTipoProcessoDocumento((Integer)r[24]);
		tipoDocumento.setTipoProcessoDocumento((String)r[25]);

		ProcessoDocumento documento = new ProcessoDocumento();
		documento.setIdProcessoDocumento((Integer)r[2]);
		documento.setProcessoDocumento((String)r[4]);
		documento.setDataJuntada((java.util.Date)r[6]);
		documento.setDataInclusao((java.util.Date)r[8]);
		documento.setNumeroOrdem(r[10] == null ? null : ((BigDecimal)r[10]).intValue());
		documento.setAtivo((Boolean)r[12]);
		documento.setProcesso(processo);

		documento.setTipoProcessoDocumento(tipoDocumento);
		documento.setProcessoDocumentoBin(bin);
		documento.setUsuarioInclusao(usuarioInclusao);
		documento.setUsuarioAlteracao(usuarioAlteracao);
		documento.setUsuarioExclusao(usuarioExclusao);
		documento.setUsuarioJuntada(usuarioJuntada);
		documento.setPapel(papel);
		documento.setLido((Boolean)r[63]);
		documento.setLocalizacao(localizacao);
		documento.setNomeUsuarioInclusao((String)r[41]);
		documento.setNomeUsuarioAlteracao((String)r[42]);
		documento.setNomeUsuarioExclusao((String)r[43]);
		documento.setNumeroDocumento((String)r[44]);
		documento.setDocumentoSigiloso((Boolean)r[45]);
        documento.setNomeUsuarioJuntada((String) r[56]);
        documento.setLocalizacaoJuntada((String) r[57]);
        documento.setInTipoOrigemJuntada(r[60] == null ? null : TipoOrigemAcaoEnum.valueOf((String) r[60]));
		
		//Trata-se de um anexo de documento principal
		if(r[3] != null) {
			ProcessoDocumentoBin binPrincipal = new ProcessoDocumentoBin();
			binPrincipal.setIdProcessoDocumentoBin((Integer)r[26]);
			binPrincipal.setNomeArquivo((String)r[27]);
			binPrincipal.setExtensao((String)r[28]);
			binPrincipal.setBinario((Boolean)r[29]);
			binPrincipal.setNumeroDocumentoStorage((String)r[30]);
			
			TipoProcessoDocumento tipoDocumentoPrincipal = new TipoProcessoDocumento();
			tipoDocumentoPrincipal.setIdTipoProcessoDocumento((Integer)r[31]);
			tipoDocumentoPrincipal.setTipoProcessoDocumento((String)r[32]);

			ProcessoDocumento documentoPrincipal = new ProcessoDocumento();
			documentoPrincipal.setIdProcessoDocumento((Integer)r[3]);
			documentoPrincipal.setProcessoDocumento((String)r[5]);
			documentoPrincipal.setDataJuntada((java.util.Date)r[7]);
			documentoPrincipal.setDataInclusao((java.util.Date)r[9]);
			documentoPrincipal.setNumeroOrdem(r[11] == null ? null : ((BigDecimal)r[11]).intValue());
			documentoPrincipal.setAtivo((Boolean)r[13]);
			documentoPrincipal.setProcesso(processo);
			documentoPrincipal.setTipoProcessoDocumento(tipoDocumentoPrincipal);
			documentoPrincipal.setProcessoDocumentoBin(binPrincipal);
			documentoPrincipal.setUsuarioInclusao(usuarioInclusao);
			documentoPrincipal.setUsuarioAlteracao(usuarioAlteracao);
			documentoPrincipal.setUsuarioExclusao(usuarioExclusao);
			documentoPrincipal.setUsuarioJuntada(usuarioJuntada);
			documentoPrincipal.setNomeUsuarioInclusao((String)r[41]);
			documentoPrincipal.setNomeUsuarioAlteracao((String)r[42]);
			documentoPrincipal.setNomeUsuarioExclusao((String)r[43]);
			documentoPrincipal.setPapel(papel);
	        documentoPrincipal.setLocalizacao(localizacao);
			documentoPrincipal.setNumeroDocumento((String)r[46]);
			documentoPrincipal.setDocumentoSigiloso((Boolean)r[47]);
	        documentoPrincipal.setNomeUsuarioJuntada((String) r[56]);
	        documentoPrincipal.setLocalizacaoJuntada((String) r[57]);
	        documentoPrincipal.setInTipoOrigemJuntada(r[60] == null ? null : TipoOrigemAcaoEnum.valueOf((String) r[60]));

			documento.setDocumentoPrincipal(documentoPrincipal);
		}
		
		return documento;
	}

	private ProcessoEvento processarMovimento(Processo processo, Usuario usuario, Object[] r) {
		ProcessoEvento movimento = new ProcessoEvento();
		movimento.setIdProcessoEvento((Integer)r[0]);
		movimento.setDataAtualizacao((java.util.Date)r[1]);
		movimento.setTextoFinalExterno((String)r[2]);
		movimento.setTextoFinalInterno((String)r[3]);
		movimento.setAtivo((Boolean)r[4]);
		movimento.setProcesso(processo);
		movimento.setUsuario(usuario);
		
		return movimento;
	}
	
	public void marcarDocumentosComoLidosAutos(Integer idProcesso) {
		Query query  = getEntityManager().createNativeQuery(this.getSqlDocumentosPrincipaisNaoLidos());
		setParameters(query, idProcesso, null, new HashMap<>(), true);
		
		@SuppressWarnings("unchecked")
		List<Integer> idsDocumentosNaoLidos = query.getResultList();
		
		if (idsDocumentosNaoLidos != null && idsDocumentosNaoLidos.size() > 0) {
			EntityUtil.createNativeQuery(
				"update tb_processo_documento set in_lido = true where id_processo_documento in ("
					+ StringUtil.listToString(idsDocumentosNaoLidos) + ")")
				.executeUpdate();
		}
	}

}
