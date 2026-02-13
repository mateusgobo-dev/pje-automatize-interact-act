package br.com.jt.pje.dao;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Expressions;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.HibernateUtil;
import br.com.jt.pje.manager.SessaoManager;
import br.com.jt.pje.query.SessaoQuery;
import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.pje.jt.entidades.SessaoJT;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SalaHorario;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.enums.TipoSituacaoPautaEnum;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

@Name(SessaoDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class SessaoDAO extends BaseDAO<Sessao> implements SessaoQuery, Serializable{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "sessaoDAO";

	public static final String ID_SESSAO = "idSessao";

	@Override
	public Object getId(Sessao e) {
		return e.getIdSessao();
	}
	
	public boolean existeSessao(Date date, SalaHorario salaHorario){
		Query q = getEntityManager().createQuery(EXISTE_SESSAO_QUERY);
		q.setParameter(QUERY_PARAMETER_DATA, date);
		q.setParameter(QUERY_PARAMETER_SALA_HORARIO, salaHorario);
		boolean result = EntityUtil.getSingleResult(q) != null;
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<SessaoJT> getSessoesComDataFechamentoPautaDiaCorrente(){
		Query q = getEntityManager().createQuery(SESSOES_COM_DATA_FECHAMENTO_PAUTA_DIA_CORRENTE_QUERY);
		List<SessaoJT> list = q.getResultList();
		return list;
	}

	public boolean existemVariasSessoes(OrgaoJulgador orgaoJulgador, OrgaoJulgadorColegiado orgaoJulgadorColegiado){
		Usuario usuario = Authenticator.getUsuarioLogado();
		Date dataPesquisa = null;
		StringBuilder sb = new StringBuilder();
		
		if("JT".equals(ParametroUtil.instance().getTipoJustica())){
			dataPesquisa = (Date) Expressions.instance().createValueExpression("#{agendaSessaoJT.currentDate}").getValue();
			sb.append("select count(s.idSessao) from SessaoJT s where ");
		}else{
			dataPesquisa = (Date) Expressions.instance().createValueExpression("#{agendaSessao.currentDate}").getValue();
			sb.append("select count(s.idSessao) from SessaoJT s where ");
		}
		
		Date dataInico = DateUtil.getBeginningOfDay(dataPesquisa);
		Date dataFim = DateUtil.getEndOfDay(dataPesquisa);
		
		if (orgaoJulgador != null){
			sb.append("s in (select cs.sessao from ComposicaoSessao cs ");
			sb.append("		 where cs.orgaoJulgador = #{orgaoJulgadorAtual}) and ");
		}
		if (orgaoJulgadorColegiado != null){
			sb.append("s.orgaoJulgadorColegiado = #{orgaoJulgadorColegiadoAtual} ");
		}
		else{
			sb.append("s.pessoaProcurador.idUsuario = :idUsuario ");
		}
		sb.append("and s.dataSessao between :dtIni and :dtFim ");
		
		Query query = EntityUtil.createQuery(sb.toString());
		query.setParameter("dtIni", dataInico);
		query.setParameter("dtFim", dataFim);
		
		if(orgaoJulgadorColegiado == null){
			query.setParameter("idUsuario", usuario.getIdUsuario());
		}
		
		try{
			Number qtd = (Number) query.getSingleResult();
			return qtd.intValue() > 1;
		}catch(NoResultException e){
			return Boolean.FALSE;
		}
	}

	public int getIdSessaoDoDia(OrgaoJulgadorColegiado orgaoJulgadorColegiado){
		Usuario usuario = Authenticator.getUsuarioLogado();
		Date dataPesquisa = null;
		StringBuilder sql = new StringBuilder();
		
		if("JT".equals(ParametroUtil.instance().getTipoJustica())){
			dataPesquisa = (Date) Expressions.instance().createValueExpression("#{agendaSessaoJT.currentDate}").getValue();
			sql.append("select o.idSessao from SessaoJT o ");
		}else{
			dataPesquisa = (Date) Expressions.instance().createValueExpression("#{agendaSessao.currentDate}").getValue();
			sql.append("select o.idSessao from Sessao o ");
		}
		
		Date dataInico = DateUtil.getBeginningOfDay(dataPesquisa);
		Date dataFim = DateUtil.getEndOfDay(dataPesquisa);
		
		sql.append("where o.dataSessao between :dtIni and :dtFim ");
		if(Authenticator.getOrgaoJulgadorAtual() != null){
	    	sql.append("and o in (select cs.sessao from ComposicaoSessao cs ");
	    	sql.append("		   where cs.orgaoJulgador = #{orgaoJulgadorAtual}) ");
		}
		if (orgaoJulgadorColegiado != null){
			sql.append("and o.orgaoJulgadorColegiado = #{orgaoJulgadorColegiadoAtual}");
		}
		else{
			sql.append("and o.pessoaProcurador.idUsuario = :idUsuario ");
		}
		
		Query query = EntityUtil.createQuery(sql.toString());
		query.setParameter("dtIni", dataInico);
		query.setParameter("dtFim", dataFim);
		if(orgaoJulgadorColegiado == null){
			query.setParameter("idUsuario", usuario.getIdUsuario());
		}
		
		try{
			return (Integer) query.getSingleResult();
		}catch(NoResultException e){
			return 0;
		}
	}
        
	/**
	 * [PJEII-4329]
	 * @param OJC orgão julgador colegiado
	 * @param OJ orgão julgador
	 * @param dataMinimaSessao Data mínima da sessão a ser pesquisada
	 * @return Uma lista das sessões que estão em andamento a partir de um orgão julgador e um orgão julgador colegiado
	 */
	@SuppressWarnings("unchecked")
	public List<Sessao> getSessoesJulgamento(OrgaoJulgadorColegiado ojc, OrgaoJulgador oj, Date dataMinimaSessao){
		Query q = getEntityManager().createQuery(SESSAO_JULGAMENTO_POR_ORGAO_JULGADOR_E_ORGAO_JULGADOR_COLEGIADO);
		q.setParameter(SessaoQuery.QUERY_PARAMETER_DATA, dataMinimaSessao);
		q.setParameter(SessaoQuery.QUERY_PARAMETER_ORGAO_JULGADOR_COLEGIADO, ojc);
		q.setParameter(SessaoQuery.QUERY_PARAMETER_ORGAO_JULGADOR, oj);

		List<Sessao> sessaoTemp = q.getResultList();
		return sessaoTemp;
	}
        
        
    /**
     * Criada na solicitação [PJEII-4330]
     * 
     * @param sessao A Sessão de julgamento
     * @return Lista de Orgaos Julgadores da Sessao selecionada
     */
    @SuppressWarnings("unchecked")
	public List<OrgaoJulgador> getOrgaosJulgadoresDaSessao(Sessao sessao) {
        Query query = getEntityManager().createQuery(ORGAOS_JULGADORES_DA_SESSAO);
        query.setParameter("sessao", sessao);

        List<OrgaoJulgador> ojList = query.getResultList();

        return ojList;
    }
        
    /**
     * Retorna todas as sessões de julgamento de determinado ano ordenadas pelo apelido da sessão.
     * 
     * @param ano Representa o ano das sessões de julgamento a ser pesquisada.
     * @return Todas as sessões de julgamento de determinado ano ordenadas pelo apelido da sessão.
     */
	@SuppressWarnings("unchecked")
	public List<Sessao> getSessoesJulgamento(Integer ano) {
		String jpql = "select o from Sessao o where YEAR(o.dataSessao) = :ano order by o.apelido";
		Query q = getEntityManager().createQuery(jpql);
		q.setParameter("ano", ano);
		return q.getResultList();
	}
	
	/**
	 * Recupera a proxima sessão de julgamento do processo.
	 * 
	 * @param idProcessoTrf Identificador do processo.
	 * @return Sessao
	 */
	public Sessao getProximaSessaoProcesso(ProcessoTrf processoTrf) throws NoSuchFieldException{
		Search search = new Search(SessaoPautaProcessoTrf.class);
		search.addCriteria(br.jus.pje.search.Criteria.equals("processoTrf.idProcessoTrf", processoTrf.getIdProcessoTrf()));
		search.addCriteria(br.jus.pje.search.Criteria.isNull("dataExclusaoProcessoTrf"));
		search.addCriteria(br.jus.pje.search.Criteria.isNull("sessao.dataRealizacaoSessao"));
		search.addCriteria(br.jus.pje.search.Criteria.greaterOrEquals("sessao.dataSessao", new Date()));
		search.setMax(1);
		search.addOrder("sessao.dataSessao", Order.ASC);
		List<SessaoPautaProcessoTrf> sessaoPautaProcessoTrfList = list(search);
		
		if (!sessaoPautaProcessoTrfList.isEmpty()){
			SessaoPautaProcessoTrf sessaoPautaProcessoTrf = (SessaoPautaProcessoTrf) sessaoPautaProcessoTrfList.get(0);
			
			if (sessaoPautaProcessoTrf != null) {
				return sessaoPautaProcessoTrf.getSessao();
			}
		}
		return null;	
	}
	
	
	/**
	 * Recupera a data da última sessão de julgamento do processo.
	 * 
	 * @param idProcessoTrf Identificador do processo.
	 * @return A data da ultima sessão de julgamento do processo.
	 */
	public Sessao getUltimaSessaoProcesso(Integer idProcessoTrf){
		
		Criteria criteria = HibernateUtil.getSession().createCriteria(SessaoPautaProcessoTrf.class);
		criteria.createCriteria("processoTrf").add(Restrictions.eq("idProcessoTrf", idProcessoTrf));
		criteria.add(Restrictions.isNull("dataExclusaoProcessoTrf"));
		criteria.createCriteria("sessao")
			.add(Restrictions.isNotNull("dataRealizacaoSessao"))
			.addOrder(org.hibernate.criterion.Order.desc("dataRealizacaoSessao"));
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		SessaoPautaProcessoTrf sessaoPautaProcessoTrf = (SessaoPautaProcessoTrf) criteria.uniqueResult();
		if (sessaoPautaProcessoTrf != null) {
			return sessaoPautaProcessoTrf.getSessao();
		}
		return null;
	}
	
	/**
	 * Recupera as sessões de julgamento de um processo.
	 * 
	 * @return Lista de {@link Sessao}.
	 */
	@SuppressWarnings("unchecked")
	public List<Sessao> getSessoesProcesso(Integer idProcessoTrf){
		Criteria criteria = HibernateUtil.getSession().createCriteria(Sessao.class);
		criteria.createCriteria("sessaoPautaProcessoTrfList")
			.add(Restrictions.eq("processoTrf.idProcessoTrf", idProcessoTrf))
			.add(Restrictions.isNull("dataExclusaoProcessoTrf"));
		
		return criteria.list();
	}
	
    /**
     * Recupera uma sessão pelo seu id
     * @param id O id da sessão
     * @return 
     */
	public Sessao recuperarPorId(Integer id) {
		return find(id);
	}
	
	
	/**
	 * Metodo recupera as datas das sessoes nao realizadas
	 */
	@SuppressWarnings("unchecked")
	public List<Date> getDatasSessoesNaoFinalizadas(){
		StringBuilder query = new StringBuilder();
		query.append(" SELECT DISTINCT sessao.dataSessao FROM Sessao sessao ");
		query.append(" WHERE sessao.dataFechamentoSessao IS NULL");
		query.append(" ORDER BY sessao.dataSessao DESC");
		
		Query querie = getEntityManager().createQuery(query.toString());
		return querie.getResultList();
	}
	
	/**
	 * Metodo recupera as datas das sessoes nao realizadas
	 *  Apresenta sessão de mesma data que o usuário esteja liberando para publicação, 
	 *  caso a hora da operação seja inferior a hora recuperada da variável de tarefa pje:fluxo:horaLimitePublicacao;
	 *  Apresentar sessões de datas futuras.
	 * @param dataHojeSemHora 
	 */
	@SuppressWarnings("unchecked")
	public List<Sessao> getSessoesNaoFinalizadas(Date horaLimitePublicacao) {
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT s FROM Sessao AS s ");
		builder.append(" WHERE s.dataRealizacaoSessao IS NULL ");
		builder.append(" AND s.dataSessao > :dataHojeSemHora ");
		builder.append(" OR (s.dataSessao = :dataHojeSemHora AND now() <= :horaLimitePublicacao) ");
		builder.append(" order by s.dataSessao ");
		String hql = builder.toString();
		Query q = entityManager.createQuery(hql);
		q.setParameter("dataHojeSemHora", DateUtil.getDataSemHora(new Date()));
		q.setParameter("horaLimitePublicacao", horaLimitePublicacao);
		return q.getResultList();
	}

	/**
	 * metodo responsavel por recuperar todas as sessoes incluidas pela pessoa passada em parametro.
	 * @param _pessoa
	 * @param isBuscaPessoaInclusora
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Sessao> recuperarSessaoPessoa(Pessoa _pessoa, boolean isBuscaPessoaInclusora) {
		Criteria criteria = HibernateUtil.getSession().createCriteria(Sessao.class); 		
		if(isBuscaPessoaInclusora) { 			
			criteria.add(Restrictions.eq("usuarioInclusao.idUsuario", _pessoa.getIdPessoa())); 		
		} else { 
			criteria.add(Restrictions.eq("usuarioExclusao.idUsuario", _pessoa.getIdPessoa())); 		
		} 		
		return criteria.list();
	}

    @SuppressWarnings("unchecked")
    public List<Sessao> getSessoesJulgamento(OrgaoJulgadorColegiado ojc, Date dataMinimaSessao){
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT s FROM Sessao as s where s.dataAberturaSessao = null and s.dataSessao >= :dataMinimaSessao " );
        builder.append("and s.dataRealizacaoSessao is null and s.dataFechamentoPauta is null and s.orgaoJulgadorColegiado = :orgaoJulgadorColegiado order by s.dataSessao " );
        String hql = builder.toString();
        Query q = getEntityManager().createQuery(hql);
        q.setParameter("dataMinimaSessao", dataMinimaSessao);
        q.setParameter("orgaoJulgadorColegiado", ojc);
        List<Sessao> sessaoTemp = q.getResultList();
        return sessaoTemp;
    }
    
    @SuppressWarnings("unchecked")
	public List<ProcessoTrf> recuperarProcessosJulgadoNaoAssinado(Integer idSessao, TipoProcessoDocumento tipoDocumento) {
    	StringBuilder hql = new StringBuilder("SELECT o.processoTrf FROM SessaoPautaProcessoTrf o ")
    		.append("WHERE o.sessao.idSessao = :idSessao AND o.dataExclusaoProcessoTrf IS NULL AND o.situacaoJulgamento = :situacao AND o.processoTrf NOT IN ( ")
    			.append("SELECT p.processoDocumento.processoTrf FROM SessaoProcessoDocumento p ")
    			.append("WHERE p.sessao.idSessao = :idSessao AND p.processoDocumento.ativo = true AND p.processoDocumento.tipoProcessoDocumento = :tipoDocumento ")
    			.append("AND p.processoDocumento.processoDocumentoBin.signatarios IS NOT EMPTY")
    		.append(")");

    	Query query = this.entityManager.createQuery(hql.toString());
    	query.setParameter(ID_SESSAO, idSessao);
    	query.setParameter("situacao", TipoSituacaoPautaEnum.JG);
    	query.setParameter("tipoDocumento", tipoDocumento);

    	return query.getResultList();
    }
    
    @SuppressWarnings("unchecked")
	public List<ProcessoTrf> recuperarProcessosSemMovimentacaoJulgamento(Integer idSessao, Date dataLimite) {
    	StringBuilder hql = new StringBuilder("SELECT o.processoTrf FROM SessaoPautaProcessoTrf o JOIN FETCH o.processoTrf.processo p ")
    		.append("WHERE o.sessao.idSessao = :idSessao AND o.dataExclusaoProcessoTrf IS NULL AND p NOT IN ( ")
    			.append("SELECT q.processo FROM ProcessoEvento q ")
    			.append("WHERE q.ativo = true AND q.processoEventoExcludente IS NULL AND q.dataAtualizacao >= :dataLimite AND q.evento.codEvento IN (:codsEvento)")
    		.append(")");

    	Query query = this.entityManager.createQuery(hql.toString());
    	query.setParameter(ID_SESSAO, idSessao);
    	query.setParameter("dataLimite", dataLimite);
    	query.setParameter("codsEvento", ComponentUtil.getComponent(SessaoManager.class).listarEventosDeliberacaoSessao());

    	return query.getResultList();
    }

	public String getSessaoObservacao(Integer idSessao){
		String jpql = "select o.observacao from Sessao o where o.idSessao = :idSessao";
		Query q = getEntityManager().createQuery(jpql);
		q.setParameter(ID_SESSAO, idSessao);

		String result = "";
		try {
			result = (String) q.getSingleResult();
		}catch (Exception e){
			return result;
		}
		return result;
	}

}
