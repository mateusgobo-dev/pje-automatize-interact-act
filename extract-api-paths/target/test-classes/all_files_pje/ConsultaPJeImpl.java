package br.jus.cnj.pje.ws;

import static br.jus.cnj.pje.ws.ConsultaPJeUtil.getFluxo;
import static br.jus.cnj.pje.ws.ConsultaPJeUtil.toAssuntoJudicial;
import static br.jus.cnj.pje.ws.ConsultaPJeUtil.toClasseJudicial;
import static br.jus.cnj.pje.ws.ConsultaPJeUtil.toCompetencia;
import static br.jus.cnj.pje.ws.ConsultaPJeUtil.toJurisdicao;
import static br.jus.cnj.pje.ws.ConsultaPJeUtil.toOrgaoJulgador;
import static br.jus.cnj.pje.ws.ConsultaPJeUtil.toOrgaoJulgadorColegiado;
import static br.jus.cnj.pje.ws.ConsultaPJeUtil.toPapel;
import static br.jus.cnj.pje.ws.ConsultaPJeUtil.toPrioridadeProcesso;
import static br.jus.cnj.pje.ws.ConsultaPJeUtil.toSalaAudiencia;
import static br.jus.cnj.pje.ws.ConsultaPJeUtil.toTipoAudiencia;
import static br.jus.cnj.pje.ws.ConsultaPJeUtil.toTipoDocumentoProcessual;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.xml.ws.WebServiceContext;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.contexts.Lifecycle;

import br.com.infox.cliente.NumeroProcessoUtil;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.cnj.pje.nucleo.manager.TipoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.servicos.AutuacaoService;
import br.jus.pje.nucleo.entidades.AplicacaoClasse;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sala;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.util.StringUtil;


@javax.jws.WebService(serviceName = "ConsultaPJeService", portName = "ConsultaPJePort", targetNamespace = "http://ws.pje.cnj.jus.br/", endpointInterface = "br.jus.cnj.pje.ws.ConsultaPJe")
@org.apache.cxf.interceptor.InInterceptors (interceptors = {"br.jus.cnj.pje.util.PJESoapActionInInterceptor"})
public class ConsultaPJeImpl implements ConsultaPJe {
	
	@Resource
	private WebServiceContext ctx;

	@Override
	@WebMethod
	@SuppressWarnings("unchecked")
	public List<Jurisdicao> consultarJurisdicoes() {
		Lifecycle.beginCall();
		List<Jurisdicao> jurisdicaoList = new ArrayList<Jurisdicao>();
		StringBuilder sql = new StringBuilder();
		sql.append("select o from Jurisdicao o where o.ativo = true");

		Query query = EntityUtil.getEntityManager().createQuery(sql.toString());
		List<br.jus.pje.nucleo.entidades.Jurisdicao> list = query
				.getResultList();
		for (br.jus.pje.nucleo.entidades.Jurisdicao jurisdicaoEntity : list) {
			jurisdicaoList.add(toJurisdicao(jurisdicaoEntity));
		}
		Lifecycle.endCall();
		return jurisdicaoList;
	}

	@Override
	@SuppressWarnings("unchecked")
	@WebMethod
	public List<OrgaoJulgador> consultarOrgaosJulgadores() {
		Lifecycle.beginCall();
		List<OrgaoJulgador> orgaoJulgadorList = new ArrayList<OrgaoJulgador>();
		StringBuilder sql = new StringBuilder();
		sql.append("select o from OrgaoJulgador o where o.ativo = true");

		Query query = EntityUtil.getEntityManager().createQuery(sql.toString());
		List<br.jus.pje.nucleo.entidades.OrgaoJulgador> list = query
				.getResultList();
		for (br.jus.pje.nucleo.entidades.OrgaoJulgador orgaoJulgadorEntity : list) {
			orgaoJulgadorList.add(toOrgaoJulgador(orgaoJulgadorEntity));
		}
		Lifecycle.endCall();
		return orgaoJulgadorList;
	}

	@Override
	@SuppressWarnings("unchecked")
	@WebMethod
	public List<OrgaoJulgadorColegiado> consultarOrgaosJulgadoresColegiados() {
		Lifecycle.beginCall();
		List<OrgaoJulgadorColegiado> orgaoJulgadorColegiadoList = new ArrayList<OrgaoJulgadorColegiado>();
		StringBuilder sql = new StringBuilder();
		sql.append("select o from OrgaoJulgadorColegiado o where o.ativo = true");

		Query query = EntityUtil.getEntityManager().createQuery(sql.toString());
		List<br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado> list = query
				.getResultList();
		for (br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado orgaoJulgadorColegiadoEntity : list) {
			orgaoJulgadorColegiadoList
					.add(toOrgaoJulgadorColegiado(orgaoJulgadorColegiadoEntity));
		}
		Lifecycle.endCall();
		return orgaoJulgadorColegiadoList;
	}

	@Override
	@WebMethod
	@SuppressWarnings("unchecked")
	public List<ClasseJudicial> consultarClassesJudiciais(
			@WebParam Jurisdicao jurisdicao) {
		Lifecycle.beginCall();
		List<ClasseJudicial> classeJudicialList = new ArrayList<ClasseJudicial>();
		AplicacaoClasse aplicacaoSistema = ParametroUtil.instance().getAplicacaoSistema();
		if (jurisdicao == null || aplicacaoSistema == null) {
			return classeJudicialList;
		}
		List<br.jus.pje.nucleo.entidades.ClasseJudicial> list = obterQueryClassesJudiciais(jurisdicao.getId(), 
				aplicacaoSistema.getIdAplicacaoClasse(), false).getResultList();
		for (br.jus.pje.nucleo.entidades.ClasseJudicial classeJudicialEntity : list) {
			classeJudicialList.add(toClasseJudicial(classeJudicialEntity));
		}
		Lifecycle.endCall();
		return classeJudicialList;
	}

	@Override
	@WebMethod
	@SuppressWarnings("unchecked")
	public List<AssuntoJudicial> consultarAssuntosJudiciais(
			@WebParam Jurisdicao jurisdicao,
			@WebParam ClasseJudicial classeJudicial) {
		Lifecycle.beginCall();
		List<AssuntoJudicial> assuntoJudicialList = new ArrayList<AssuntoJudicial>();

		if (jurisdicao == null || classeJudicial == null) {
			return assuntoJudicialList;
		}

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("select o from AssuntoTrf o");
		stringBuilder.append("			where ");
		if (ParametroUtil.instance().isPrimeiroGrau()) {
			stringBuilder.append("				EXISTS (select 1 from OrgaoJulgador oj");
			stringBuilder.append("						inner join oj.orgaoJulgadorCompetenciaList compList");
			stringBuilder.append("						inner join compList.competencia c");
			stringBuilder.append("						inner join c.competenciaClasseAssuntoList clAssunto");
			stringBuilder.append("						inner join clAssunto.assuntoTrf a");
			stringBuilder.append("		                where ");
			stringBuilder.append("		                oj.jurisdicao.numeroOrigem = :numeroOrigem");
			stringBuilder.append("		                and o = a ");		
			stringBuilder.append("		                and current_date >= compList.dataInicio ");
			stringBuilder.append("		                and c.ativo = true");
			stringBuilder.append("						and (compList.dataFim >= current_date or compList.dataFim is null)");
			stringBuilder.append("		                and clAssunto.classeAplicacao.classeJudicial.codClasseJudicial = :codigoClasse");
			stringBuilder.append("		                and current_date >= clAssunto.dataInicio");
			stringBuilder.append("		                and (clAssunto.dataFim >= current_date or clAssunto.dataFim is null)");
			stringBuilder.append("                	) ");
		} else {
			stringBuilder.append("				EXISTS (select 1 from OrgaoJulgadorColegiado ojc");
			stringBuilder.append("                       	inner join ojc.orgaoJulgadorColegiadoCompetenciaList compCList");
			stringBuilder.append("                       	inner join compCList.competencia cc");
			stringBuilder.append("                       	inner join cc.competenciaClasseAssuntoList clcAssunto");
			stringBuilder.append("                       	inner join clcAssunto.assuntoTrf ac");
			stringBuilder.append("                       	where ojc.jurisdicao.numeroOrigem = :numeroOrigem");
			stringBuilder.append("                       	and o = ac ");
			stringBuilder.append("                       	and current_date >= compCList.dataInicio ");
			stringBuilder.append("						    and cc.ativo = true");
			stringBuilder.append("                       	and (compCList.dataFim >= current_date or compCList.dataFim is null)");
			stringBuilder.append("                       	and clcAssunto.classeAplicacao.classeJudicial.codClasseJudicial = :codigoClasse");
			stringBuilder.append("                       	and current_date >= clcAssunto.dataInicio");
			stringBuilder.append("                       	and (clcAssunto.dataFim >= current_date or clcAssunto.dataFim is null)");
			stringBuilder.append("                  )");
		}
		stringBuilder.append("			AND o.ativo = true");
		stringBuilder.append("			order by o.assuntoTrf");
		String sql = stringBuilder.toString();
		Query query = EntityUtil.getEntityManager().createQuery(sql);
		query.setParameter("numeroOrigem", jurisdicao.getId());
		query.setParameter("codigoClasse", classeJudicial.getCodigo());

		List<br.jus.pje.nucleo.entidades.AssuntoTrf> list = query
				.getResultList();
		for (br.jus.pje.nucleo.entidades.AssuntoTrf assuntoTrfEntity : list) {
			assuntoJudicialList.add(toAssuntoJudicial(assuntoTrfEntity));
		}
		Lifecycle.endCall();
		return assuntoJudicialList;
	}

	@Override
	@WebMethod
	public List<Competencia> consultarCompetencias(
			@WebParam Jurisdicao jurisdicao,
			@WebParam ClasseJudicial classeJudicial,
			@WebParam List<AssuntoJudicial> assuntos) {

		List<Competencia> competenciaList = new ArrayList<Competencia>();
		
		if (jurisdicao != null) {
	
			try {
				Lifecycle.beginCall();
				
				ProcessoTrf processo = ConsultaPJeUtil.novoProcessoTrf(jurisdicao, classeJudicial, assuntos);
				
				List<br.jus.pje.nucleo.entidades.Competencia> list = AutuacaoService.instance().recuperaCompetenciasPossiveis(processo);
				for (br.jus.pje.nucleo.entidades.Competencia competenciaEntity : list) {
					competenciaList.add(toCompetencia(competenciaEntity));
				}
				
			} catch (PJeException e) {
				String mensagem = "Erro ao consultar as competências. Erro: %s";
				throw new RuntimeException(String.format(mensagem, e.getLocalizedMessage()));
			} finally {
				Lifecycle.endCall();
			}
			
		}
		return competenciaList;
	}
	
	@Override
	@WebMethod
	public List<TipoAudiencia> consultarTiposAudiencia() {
		Lifecycle.beginCall();
		List<TipoAudiencia> tipoAudienciaList = new ArrayList<TipoAudiencia>();
		StringBuilder sql = new StringBuilder();
		sql.append("select o from TipoAudiencia o where o.ativo = true");

		Query query = EntityUtil.getEntityManager().createQuery(sql.toString());
		@SuppressWarnings("unchecked")
		List<br.jus.pje.nucleo.entidades.TipoAudiencia> list = query
				.getResultList();
		for (br.jus.pje.nucleo.entidades.TipoAudiencia tipoAudienciaEntity : list) {
			tipoAudienciaList.add(toTipoAudiencia(tipoAudienciaEntity));
		}
		Lifecycle.endCall();
		return tipoAudienciaList;
	}

	@Override
	@WebMethod
	public List<TipoDocumentoProcessual> consultarTiposDocumentoProcessual(
			String identificadorPapel) {
		Lifecycle.beginCall();
		
		List<TipoDocumentoProcessual> tipoDocumentoProcessualList = new ArrayList<TipoDocumentoProcessual>();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT distinct tipoProcDoc FROM TipoProcessoDocumentoPapel tpProcDocPapel ");
		sql.append("JOIN tpProcDocPapel.tipoProcessoDocumento tipoProcDoc ");
		sql.append("WHERE tipoProcDoc.ativo = true ");

		if (identificadorPapel != null && !identificadorPapel.isEmpty()) {
			sql.append(" AND tpProcDocPapel.papel.identificador = :idPapel");
		}
		sql.append(" ORDER BY tipoProcDoc.tipoProcessoDocumento");
		
		Query query = EntityUtil.getEntityManager().createQuery(sql.toString());

		if (identificadorPapel != null && !identificadorPapel.isEmpty()) {
			query.setParameter("idPapel", identificadorPapel);
		}

		@SuppressWarnings("unchecked")
		List<TipoProcessoDocumento> list = query.getResultList();
		for (TipoProcessoDocumento tipoProcessoDocumento : list) {
			tipoDocumentoProcessualList.add(toTipoDocumentoProcessual(tipoProcessoDocumento));
		}
		Lifecycle.endCall();
		return tipoDocumentoProcessualList;
	}

	@SuppressWarnings("unchecked")
	@Override
	@WebMethod
	public Fluxo recuperarInformacoesFluxo(
			@WebParam ClasseJudicial classeJudicial) {
		Fluxo fluxo = new Fluxo();
		Lifecycle.beginCall();
		StringBuilder sql = new StringBuilder();
		sql.append("select o from ClasseJudicial o where o.ativo = true and o.codClasseJudicial = :codClasseJudicial");

		Query query = EntityUtil.getEntityManager().createQuery(sql.toString());
		query.setParameter("codClasseJudicial", classeJudicial.getCodigo());

		List<br.jus.pje.nucleo.entidades.ClasseJudicial> classeList = query
				.getResultList();

		if (classeList != null && !classeList.isEmpty()) {
			br.jus.pje.nucleo.entidades.ClasseJudicial classeJudicialEntity = classeList
					.get(0);
			if (classeJudicialEntity.getFluxo() != null
					&& classeJudicialEntity.getFluxo().getPublicado()) {
				fluxo = getFluxo(classeJudicialEntity);
			}
		}

		Lifecycle.endCall();
		return fluxo;
	}

	
	@Override
	@SuppressWarnings("unchecked")
	public List<SalaAudiencia> consultarSalasAudiencia(
			@WebParam(name = "orgaoJulgador", targetNamespace = "")OrgaoJulgador orgaoJulgador) {
		Lifecycle.beginCall();
		List<SalaAudiencia> salas = new ArrayList<SalaAudiencia>(0);

		StringBuilder sql = new StringBuilder();
		sql.append("select o from Sala o where o.ativo = true");
		if (orgaoJulgador != null) {
			sql.append(" and o.orgaoJulgador.idOrgaoJulgador = :idOrgaoJulgador");
		}
		Query query = EntityUtil.getEntityManager().createQuery(sql.toString());

		if (orgaoJulgador != null) {
			query.setParameter("idOrgaoJulgador", orgaoJulgador.getId());
		}
		
		List<Sala> salasPJe = query.getResultList();
		for (Sala salaPJe : salasPJe) {
			salas.add(toSalaAudiencia(salaPJe, orgaoJulgador));
		}
		Lifecycle.endCall();
		return salas;
	}

	/**
	 * Consulta as prioridades do processo.
	 * 
	 * @return Prioridades do processo.
	 */
	@Override
	@SuppressWarnings("unchecked")
	@WebMethod
	public List<PrioridadeProcesso> consultarPrioridadeProcesso() {
		Lifecycle.beginCall();
		List<PrioridadeProcesso> resultado = new ArrayList<PrioridadeProcesso>(0);
		
		StringBuilder hql = new StringBuilder();
		hql.append("select o ");
		hql.append("from PrioridadeProcesso o ");
		hql.append("where o.ativo = true ");
		hql.append("order by o.idPrioridadeProcesso");
		
		Query query = EntityUtil.getEntityManager().createQuery(hql.toString());
		
		List<br.jus.pje.nucleo.entidades.PrioridadeProcesso> prioridades = query.getResultList();
		if (prioridades != null) {
			for (br.jus.pje.nucleo.entidades.PrioridadeProcesso prioridade : prioridades) {
				resultado.add(toPrioridadeProcesso(prioridade));
			}
		}
		Lifecycle.endCall();
		return resultado;
	}

	@Override
	public List<TipoDocumentoProcessual> consultarTodosTiposDocumentoProcessual() {
		Lifecycle.beginCall();
		
		List<TipoDocumentoProcessual> resultado = new ArrayList<TipoDocumentoProcessual>();
		
		List<TipoProcessoDocumento> tipos = getTipoProcessoDocumentoManager().consultarTodosDisponiveis();
		resultado.addAll(toTipoDocumentoProcessual(tipos));
		
		Lifecycle.endCall();
		return resultado;
	}
	
	@Override
	public List<Papel> consultarPapeis(String login) {
		Lifecycle.beginCall();
		
		List<Papel> resultado = new ArrayList<Papel>();
		
		UsuarioService service = getUsuarioService();
		Pessoa pessoa = (Pessoa) service.findByLogin(login);
		
		if (pessoa != null) {
			List<UsuarioLocalizacao> localizacoes = service.getLocalizacoesAtivas(pessoa);
			resultado.addAll(toPapel(localizacoes));
		} else {
			String mensagem = "Usuário '%s' não é um usuário válido.";
			throw new RuntimeException(String.format(mensagem, login));
		}
		
		Lifecycle.endCall();
		return resultado;
	}
	
	/**
	 * @return TipoProcessoDocumentoManager.
	 */
	protected TipoProcessoDocumentoManager getTipoProcessoDocumentoManager() {
		return ComponentUtil.getComponent(TipoProcessoDocumentoManager.class);
	}

	/**
	 * @return UsuarioService.
	 */
	protected UsuarioService getUsuarioService() {
		return ComponentUtil.getComponent(UsuarioService.class);
	}
	
	@Override
	@WebMethod
	public String consultarProcessoReferencia(@WebParam String numeroProcesso) {
		Lifecycle.beginCall();
		
		if (numeroProcesso == null || "".equals(numeroProcesso)) {
			throw new RuntimeException("Número do processo não informado.");			
		}
		
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("select p from ProcessoTrf p where p.processo.numeroProcesso = :numeroProcesso");
			Query query = EntityUtil.getEntityManager().createQuery(sql.toString());
			query.setParameter("numeroProcesso", NumeroProcessoUtil.mascaraNumeroProcesso(numeroProcesso));
			ProcessoTrf p = (ProcessoTrf) query.getSingleResult();
			
			if (p.getDesProcReferencia() == null) {
				String mensagem = "Processo '%s' sem número de processo de referência.";
				throw new RuntimeException(String.format(mensagem, numeroProcesso));
			}
			
			Lifecycle.endCall();
			
			return p.getDesProcReferencia();
		} catch (NoResultException e) {
			String mensagem = "Processo '%s' não encontrado.";
			throw new RuntimeException(String.format(mensagem, numeroProcesso));
		}
	}	
	
	/**
	 * Metodo responsavel por obter a query de classes judiciais.
	 * 
	 * @param idAplicacaoClasse
	 *            id da aplicacao classe.
	 * @param numeroOrigemJurisdicao
	 *            Número de origem da jurisdicao que se deseja obter as classes judiciais.
	 * @param remessaInstancia
	 *            parametro responsavel por identificar se ser e utilizada na
	 *            remessa entre instancias.
	 * @return <code>String</code>, a consulta das classes judiciais.
	 */
	protected Query obterQueryClassesJudiciais(Integer numeroOrigemJurisdicao, int idAplicacaoClasse, boolean remessaInstancia) {
		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT o FROM ClasseJudicial AS o ");
		sb.append(" WHERE o.ativo = TRUE ");
		sb.append(" AND o.fluxo IS NOT NULL ");
		
		if (remessaInstancia) {
			sb.append(" AND o.remessaInstancia = TRUE ");
		}
		
		sb.append(" AND EXISTS (SELECT 1 FROM ClasseJudicial cj ");
		sb.append(" INNER JOIN cj.classeAplicacaoList cat ");
		sb.append(" INNER JOIN cat.competenciaClasseAssuntoList cca ");
		sb.append(" INNER JOIN cca.competencia comp ");
		sb.append(" WHERE cca.competencia.ativo = TRUE ");
		sb.append(" AND o = cj ");
		sb.append(" AND cat.aplicacaoClasse.idAplicacaoClasse = :idAplicacaoClasse ");
		sb.append(" AND current_date >= cca.dataInicio ");
		sb.append(" AND (cca.dataFim >= current_date OR cca.dataFim is null) ");		
		sb.append(" AND (");
		if (ParametroUtil.instance().isPrimeiroGrau()) {
			sb.append(" EXISTS (SELECT 1 FROM Competencia c "); //orgaoJulgador
			sb.append("				INNER JOIN c.orgaoJulgadorCompetenciaList ojComp ");
			sb.append("             INNER JOIN ojComp.orgaoJulgador oj ");
			sb.append("             WHERE oj.ativo = TRUE ");
			sb.append("              AND comp = c ");
			sb.append("              AND oj.jurisdicao.numeroOrigem = :numeroOrigem ");
			sb.append("              AND oj.aplicacaoClasse = cat.aplicacaoClasse ");
			sb.append("              AND ojComp.competencia = comp ");
			sb.append("              AND CURRENT_DATE >= ojComp.dataInicio ");
			sb.append("              AND (ojComp.dataFim >= CURRENT_DATE OR ojComp.dataFim IS NULL)) ");
		} else {
			sb.append(" EXISTS (SELECT 1 FROM Competencia c "); //orgaoJulgadorColegiado
			sb.append("				INNER JOIN c.orgaoJulgadorColegiadoCompetenciaList ojCompC ");
			sb.append("             INNER JOIN ojCompC.orgaoJulgadorColegiado ojc ");
			sb.append("             WHERE ojc.ativo = TRUE ");
			sb.append("              AND comp = c ");
			sb.append("              AND ojc.jurisdicao.numeroOrigem = :numeroOrigem ");
			sb.append("              AND ojc.aplicacaoClasse = cat.aplicacaoClasse ");
			sb.append("              AND ojCompC.competencia = comp ");
			sb.append("              AND CURRENT_DATE >= ojCompC.dataInicio ");
			sb.append("              AND (ojCompC.dataFim >= CURRENT_DATE OR ojCompC.dataFim IS NULL)) ");
		}		
		sb.append(" ))");
		sb.append(" ORDER BY o" );
		
		Query query = EntityUtil.getEntityManager().createQuery(sb.toString());
		query.setParameter("idAplicacaoClasse",	idAplicacaoClasse);
		query.setParameter("numeroOrigem", numeroOrigemJurisdicao);
			
		return query;
	}
	
	/**
	 * Metodo responsavel por recuperar as classes judiciais para remessa entre
	 * instancias.
	 * 
	 * @param jurisdicao
	 *            a jurisdicao que se deseja pesquisar
	 * 
	 * @return <code>List</code> de classes judiciais.
	 */
	@Override
	@WebMethod
	@SuppressWarnings("unchecked")
	public List<ClasseJudicial> consultarClassesJudiciaisRemessa(@WebParam Jurisdicao jurisdicao) {
		Lifecycle.beginCall();
		
		List<ClasseJudicial> classeJudicialList = new ArrayList<ClasseJudicial>();
		
		AplicacaoClasse aplicacaoSistema = ParametroUtil.instance().getAplicacaoSistema();
		if (jurisdicao == null || aplicacaoSistema == null) {
			return classeJudicialList;
		}
		
		List<br.jus.pje.nucleo.entidades.ClasseJudicial> list = obterQueryClassesJudiciais(jurisdicao.getId(), 
				aplicacaoSistema.getIdAplicacaoClasse(), true).getResultList();
		
		for (br.jus.pje.nucleo.entidades.ClasseJudicial classeJudicialEntity : list) {
			classeJudicialList.add(toClasseJudicial(classeJudicialEntity));
		}
		
		Lifecycle.endCall();
		return classeJudicialList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> consultarProcessosPorProcessoReferencia(String numeroProcessoReferencia) {
		List<String> result = new ArrayList<>();
		
		if (StringUtils.isNotBlank(numeroProcessoReferencia)) {
			Lifecycle.beginCall();
			
			StringBuilder hql = new StringBuilder("SELECT nr_processo FROM tb_processo_trf JOIN tb_processo on id_processo_trf = id_processo ")
				.append("WHERE cd_processo_status = 'D' AND regexp_replace(ds_proc_referencia, '\\D', '', 'g') = :numeroProcessoReferencia");
			
			result = EntityUtil.getEntityManager().createNativeQuery(hql.toString())
				.setParameter("numeroProcessoReferencia", StringUtil.removeNaoNumericos(numeroProcessoReferencia)).getResultList();
			
			Lifecycle.endCall();
		}
		
		return result;
	}
}