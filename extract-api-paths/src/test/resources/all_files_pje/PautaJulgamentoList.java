package br.com.infox.pje.list;

import java.util.Date;
import java.util.Map;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.cliente.component.tree.AssuntoTrfTreeHandler;
import br.com.infox.cliente.component.tree.ClasseJudicialTreeHandler;
import br.com.infox.cliente.component.tree.TipoPessoaTreeHandler;
import br.com.infox.cliente.home.ConsultaProcessoHome;
import br.com.infox.cliente.home.SessaoHome;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.ConsultaProcessoTrfSemFiltro;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.TipoPessoa;

@Name(PautaJulgamentoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class PautaJulgamentoList extends EntityList<ConsultaProcessoTrfSemFiltro> {

	public static final String NAME = "pautaJulgamentoList";

	private static final long serialVersionUID = 1L;

	private String numeroProcesso;
	private AssuntoTrf assuntoTrf;
	private ClasseJudicial classeJudicial;
	private OrgaoJulgador orgaoJulgador;
	private TipoPessoa tipoPessoa;
	private String nomeParte;
	private Date dtDistribuicaoInicio;
	private Date dtDistribuicaoFim;
	private Date dtAtualizacaoInicio;
	private Date dtAtualizacaoFim;
	private String selecionadoPauta;
	private String numeroCPF;
	private String numeroCNPJ;
	private boolean cpf = false;
	private Boolean habilitaCombo;

	private static final String DEFAULT_ORDER = "dtSolicitacaoInclusaoPauta";

	private static final String R1 = "o.processoTrf.orgaoJulgador = #{pautaJulgamentoList.orgaoJulgador}";
	private static final String R2 = "o.numeroProcesso like concat('%', #{pautaJulgamentoList.numeroProcesso}, '%')";
	private static final String R3 = "o.classeJudicialObj = #{pautaJulgamentoList.classeJudicial}";
	private static final String R4 = "o.idProcessoTrf in (select pa.processoTrf.idProcessoTrf from ProcessoAssunto pa "
			+ "where pa.assuntoTrf = #{pautaJulgamentoList.assuntoTrf})";
	private static final String R5 = "exists (select pp.processoTrf.idProcessoTrf from ProcessoParte pp "
			+ "where pp.processoTrf.idProcessoTrf = o.idProcessoTrf "
			+ "and pp.pessoa.idUsuario in (select pdi.pessoa.idUsuario " + "from PessoaDocumentoIdentificacao pdi "
			+ "where pdi.tipoDocumento.codTipo = 'CPF' "
			+ "and pdi.numeroDocumento like concat('%', #{consultaProcessoHome.instance.numeroCPF} ,'%')))";
	private static final String R6 = "exists (select pp.processoTrf.idProcessoTrf from ProcessoParte pp "
			+ "where pp.processoTrf.idProcessoTrf = o.idProcessoTrf "
			+ "and pp.pessoa.idUsuario IN (select pdi.pessoa.idUsuario " + "from PessoaDocumentoIdentificacao pdi "
			+ "where pdi.tipoDocumento.codTipo = 'CPJ' "
			+ "and pdi.numeroDocumento like concat('%', #{consultaProcessoHome.instance.numeroCNPJ} ,'%')))";
	private static final String R7 = "o.idProcessoTrf in (select pp.processoTrf.idProcessoTrf from ProcessoParte pp "
			+ "where pp.pessoa.tipoPessoa = #{pautaJulgamentoList.tipoPessoa}) ";
	private static final String R8 = "exists (select pp.processoTrf.idProcessoTrf from ProcessoParte pp "
			+ "where pp.processoTrf.idProcessoTrf = o.idProcessoTrf "
			+ "and lower(to_ascii(pp.pessoa.nome)) like concat('%', lower(to_ascii(#{pautaJulgamentoList.nomeParte})), '%'))";
	private static final String R9 = "cast(o.processoTrf.dataDistribuicao as date) >= #{pautaJulgamentoList.dtDistribuicaoInicio}";
	private static final String R10 = "cast(o.processoTrf.dataDistribuicao as date) <= #{pautaJulgamentoList.dtDistribuicaoFim}";
	private static final String R11 = "exists (select a.processo.idProcesso from ProcessoEvento a "
			+ "where a.ativo = true and a.processo.idProcesso = o.idProcessoTrf " + "and a.evento.evento like 'Conclusão' "
			+ "and cast(a.dataAtualizacao as date) >= #{pautaJulgamentoList.dtAtualizacaoInicio})";
	private static final String R12 = "exists (select a.processo.idProcesso from ProcessoEvento a "
			+ "where a.ativo = true and a.processo.idProcesso = o.idProcessoTrf " + "and a.evento.evento like 'Conclusão' "
			+ "and cast(a.dataAtualizacao as date) <= #{pautaJulgamentoList.dtAtualizacaoFim})";

	@Override
	protected void addSearchFields() {
		addSearchField("orgaoJulgador", SearchCriteria.igual, R1);
		addSearchField("processo", SearchCriteria.igual, R2);
		addSearchField("classeJudicial", SearchCriteria.igual, R3);
		addSearchField("assuntoTrf", SearchCriteria.igual, R4);
		addSearchField("jurisdicao", SearchCriteria.igual, R5);
		addSearchField("valorCausa", SearchCriteria.igual, R6);
		addSearchField("processoParteList", SearchCriteria.igual, R7);
		addSearchField("listaPartePassivo", SearchCriteria.igual, R8);
		addSearchField("listaParteTerceiro", SearchCriteria.igual, R9);
		addSearchField("listaFiscal", SearchCriteria.contendo, R10);
		addSearchField("inicial", SearchCriteria.contendo, R11);
		addSearchField("processoStatus", SearchCriteria.contendo, R12);
	}

	@Override
	protected String getDefaultEjbql() {
		Integer oj = Authenticator.getIdOrgaoJulgadorAtual();
		Integer ojc = Authenticator.getIdOrgaoJulgadorColegiadoAtual();

		StringBuilder query = new StringBuilder();
		query.append(" SELECT o FROM ConsultaProcessoTrfSemFiltro o ");
		query.append(" WHERE o.processoStatus = 'D' ");
		query.append(" AND o.processoTrf.selecionadoPauta = true ");
		
		if(SessaoHome.instance().getInstance().getContinua()){
			query.append("and o.classeJudicialObj.sessaoContinua = true ");
			query.append("and o.processoTrf.pautaVirtual = true ");
		} else {
			query.append("and o.processoTrf.pautaVirtual = false ");
		}
		// lista apenas processos do OJC da sessão
		query.append(" AND o.idOrgaoJulgadorColegiado = #{sessaoHome.instance.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado} ");
		// lista apenas processos do OJC do usuário logado se este tiver um OJC
		if (ojc != null) {
			query.append(" AND o.idOrgaoJulgadorColegiado = " + ojc);
		}
		// lista apenas processos cujo relator é do OJ do usuário logado ou o revisor é do OJ do usuário logado
		if (oj != null) {
			query.append(" AND (o.idOrgaoJulgador = " + oj);

			query.append(" OR (o.processoTrf.orgaoJulgadorRevisor.idOrgaoJulgador = " + oj + " AND o.processoTrf.exigeRevisor = true ) ");
			query.append(" OR (exists(select rpt.processoTrf from RevisorProcessoTrf rpt where rpt.processoTrf = o.processoTrf and rpt.orgaoJulgadorRevisor.idOrgaoJulgador = " + oj + " and rpt.dataFinal is null) and o.processoTrf.revisado = true))");			
		}
		montarQueryProcessoNaoExigePauta(query);
		montarQuerySessaoRealizadaFinalizada(query);
		montarQueryRemoverPedidoVista(query, ojc, oj);
		montarQueryRemoverAdiados(query, ojc, oj);
		return query.toString();
	}
	
	/**
	 * Query para processos aptos para inclusao em pauta com classe que nao exige pauta.
	 * @param query montada para processos que nao exigem pauta
	 */
	private void montarQueryProcessoNaoExigePauta(StringBuilder query) {
//		query.append(" AND o.classeJudicialObj.pauta = true ");
		query.append(" AND NOT EXISTS ");
		query.append(" 		(");
		query.append("		  SELECT sessaoPautaProcessoTrf.processoTrf.idProcessoTrf FROM  SessaoPautaProcessoTrf sessaoPautaProcessoTrf");
		query.append("		  WHERE sessaoPautaProcessoTrf.sessao = #{sessaoHome.instance} ");
		query.append("		  AND sessaoPautaProcessoTrf.processoTrf.idProcessoTrf = o.idProcessoTrf ");
		query.append("		  AND sessaoPautaProcessoTrf.dataExclusaoProcessoTrf IS NULL");
		query.append(" 		)");
	}
	
	/**
	 * Query para processos aptos para inclusao em pauta em sessoes de julgamento na situacao 'realizada' ou 'finalizada'.
	 * @param query montada para processos com situacao 'realizada' ou 'finalizada'.
	 */
	private void montarQuerySessaoRealizadaFinalizada(StringBuilder query) {
		query.append(" AND NOT EXISTS(");
		query.append(" 				SELECT sessaoPautaProcessoTrf.processoTrf.idProcessoTrf FROM  SessaoPautaProcessoTrf sessaoPautaProcessoTrf");
		query.append(" 				WHERE sessaoPautaProcessoTrf.processoTrf.idProcessoTrf = o.idProcessoTrf");
		query.append(" 				AND sessaoPautaProcessoTrf.dataExclusaoProcessoTrf IS NULL");
		query.append("				AND sessaoPautaProcessoTrf.sessao.dataRealizacaoSessao IS NULL");
		query.append("				AND sessaoPautaProcessoTrf.sessao.dataExclusao IS NULL");
		query.append("           )");
	}
	
	private void montarQueryRemoverPedidoVista(StringBuilder query, Integer ojc, Integer oj) {
		query.append(" AND NOT EXISTS (");
		query.append(" SELECT tsptpv.processoTrf.idProcessoTrf FROM SessaoPautaProcessoTrf tsptpv");
		query.append(" WHERE tsptpv.adiadoVista = 'PV' ");
		query.append(" AND tsptpv.dataInclusaoProcessoTrf = (");
		query.append(" SELECT max(dataInclusaoProcessoTrf) FROM SessaoPautaProcessoTrf tsptpv2");
		query.append(" WHERE tsptpv2.processoTrf.idProcessoTrf = o.idProcessoTrf");
		query.append(" AND tsptpv2.dataExclusaoProcessoTrf IS NULL)");
		query.append(" AND tsptpv.orgaoJulgadorPedidoVista IS NOT NULL");
		query.append(" AND EXISTS (SELECT tsptpv3.processoTrf FROM SessaoPautaProcessoTrf tsptpv3 ");
		query.append(" WHERE tsptpv3.processoTrf.idProcessoTrf = o.idProcessoTrf AND tsptpv3.dataExclusaoProcessoTrf IS NULL ");
		query.append(" AND tsptpv3.sessao.dataRealizacaoSessao IS NOT NULL ");
		query.append(" AND (tsptpv3.sessao.dataRegistroEvento IS NOT NULL OR tsptpv3.sessao.dataFechamentoSessao IS NOT NULL)) ");
		if (ojc != null) {
			query.append(" AND tsptpv.processoTrf.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado = " + ojc);
		} else if (oj != null) {
			query.append(" AND tsptpv.orgaoJulgadorPedidoVista.idOrgaoJulgador = " + oj);
		}
		query.append(" AND tsptpv.sessao.continua IS " + SessaoHome.instance().getInstance().getContinua().toString());
		query.append(" )");
	}
	
	private void montarQueryRemoverAdiados(StringBuilder query, Integer ojc, Integer oj) {
		query.append(" AND NOT EXISTS (");
		query.append(" SELECT tsptad.processoTrf.idProcessoTrf FROM SessaoPautaProcessoTrf tsptad");
		query.append(" WHERE tsptad.adiadoVista = 'AD' ");
		query.append(" AND tsptad.dataInclusaoProcessoTrf = (");
		query.append(" SELECT max(dataInclusaoProcessoTrf) FROM SessaoPautaProcessoTrf tsptad2");
		query.append(" WHERE tsptad2.processoTrf.idProcessoTrf = o.idProcessoTrf");
		query.append(" AND tsptad2.dataExclusaoProcessoTrf IS NULL)");
		if (ojc != null) {
			query.append(" AND tsptad.processoTrf.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado = " + ojc);
		}
		query.append(" AND NOT EXISTS (SELECT tsptad3.processoTrf FROM SessaoPautaProcessoTrf tsptad3 ");
		query.append(" WHERE tsptad3.processoTrf.idProcessoTrf = o.idProcessoTrf AND tsptad3.dataExclusaoProcessoTrf IS NULL ");
		query.append(" AND tsptad3.sessao.dataRealizacaoSessao IS NULL ");
		query.append(" AND (tsptad3.sessao.dataRegistroEvento IS NULL OR tsptad3.sessao.dataFechamentoSessao IS NOT NULL)) ");
		
		if(SessaoHome.instance().getInstance().getContinua()){
			query.append(" AND tsptad.sessao.continua IS true ");
			if(!Authenticator.getIdentificadorPapelAtual().equals("idSecretarioSessao")){
				query.append(" AND tsptad.orgaoJulgadorRetiradaJulgamento.idOrgaoJulgador = o.idOrgaoJulgador ");
			}
		}else{
			query.append(" AND (tsptad.sessao.continua IS false ");
			query.append(" OR (tsptad.sessao.continua IS true ");
			query.append(" AND tsptad.orgaoJulgadorRetiradaJulgamento.idOrgaoJulgador != o.idOrgaoJulgador)) ");
		}
		
		if (Authenticator.isPapelPermissaoSecretarioSessao() || oj == null) {
			query.append(" AND ("
					+ " tsptad.processoTrf.selecionadoJulgamento = true OR tsptad.processoTrf.selecionadoPauta = true"
					+ " ) ");
			query.append(" AND tsptad.processoTrf.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado = " + ojc);
			
		} else {
			query.append(" AND (tsptad.processoTrf.orgaoJulgador.idOrgaoJulgador = " + oj +" OR ");
			query.append(" EXISTS ");
			query.append(" (SELECT rptad.processoTrf FROM RevisorProcessoTrf rptad ");
			query.append(" WHERE rptad.orgaoJulgadorRevisor.idOrgaoJulgador = " + oj );
			query.append(" AND rptad.processoTrf.idProcessoTrf = o.idProcessoTrf ");
			query.append(" AND rptad.dataFinal IS NULL )");
			query.append(" OR (tsptad.processoTrf.orgaoJulgadorRevisor.idOrgaoJulgador = ").append(oj).append(" AND tsptad.processoTrf.exigeRevisor = true )) ");
		}
		query.append(" )");
	}
	
	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public AssuntoTrf getAssuntoTrf() {
		return assuntoTrf;
	}

	public void setAssuntoTrf(AssuntoTrf assuntoTrf) {
		this.assuntoTrf = assuntoTrf;
	}

	public TipoPessoa getTipoPessoa() {
		return tipoPessoa;
	}

	public void setTipoPessoa(TipoPessoa tipoPessoa) {
		this.tipoPessoa = tipoPessoa;
	}

	public String getNomeParte() {
		return nomeParte;
	}

	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}

	public void setDtDistribuicaoInicio(Date dtDistribuicaoInicio) {
		this.dtDistribuicaoInicio = dtDistribuicaoInicio;
	}

	public Date getDtDistribuicaoInicio() {
		return dtDistribuicaoInicio;
	}

	public void setDtDistribuicaoFim(Date dtDistribuicaoFim) {
		this.dtDistribuicaoFim = dtDistribuicaoFim;
	}

	public Date getDtDistribuicaoFim() {
		return dtDistribuicaoFim;
	}

	public void setDtAtualizacaoInicio(Date dtAtualizacaoInicio) {
		this.dtAtualizacaoInicio = dtAtualizacaoInicio;
	}

	public Date getDtAtualizacaoInicio() {
		return dtAtualizacaoInicio;
	}

	public void setDtAtualizacaoFim(Date dtAtualizacaoFim) {
		this.dtAtualizacaoFim = dtAtualizacaoFim;
	}

	public Date getDtAtualizacaoFim() {
		return dtAtualizacaoFim;
	}

	public String getSelecionadoPauta() {
		return selecionadoPauta;
	}

	public void setSelecionadoPauta(String selecionadoPauta) {
		this.selecionadoPauta = selecionadoPauta;
	}

	private void limparTrees() {
		ClasseJudicialTreeHandler treeClasse = (ClasseJudicialTreeHandler) Component.getInstance("classeJudicialTree");
		AssuntoTrfTreeHandler assuntoTree = (AssuntoTrfTreeHandler) Component.getInstance("assuntoTrfTree");
		TipoPessoaTreeHandler tipoTree = (TipoPessoaTreeHandler) Component.getInstance("tipoPessoaTree");
		treeClasse.clearTree();
		assuntoTree.clearTree();
		tipoTree.clearTree();
	}

	private void limparCampos() {
		numeroProcesso = null;
		selecionadoPauta = null;
		nomeParte = null;
		dtDistribuicaoInicio = null;
		dtDistribuicaoFim = null;
		dtAtualizacaoInicio = null;
		dtAtualizacaoFim = null;
		setNumeroCPF(null);
		setNumeroCNPJ(null);
		setCpf(false);
		ConsultaProcessoHome.instance().newInstance();
	}

	public void limparCamposCPFCNPJ() {
		setNumeroCPF(null);
		setNumeroCNPJ(null);
	}

	@Override
	public void newInstance() {
		setAssuntoTrf(null);
		setTipoPessoa(null);
		limparTrees();
		limparCampos();
		super.newInstance();
	}

	public void setNumeroCPF(String numeroCPF) {
		this.numeroCPF = numeroCPF;
	}

	public String getNumeroCPF() {
		return numeroCPF;
	}

	public void setNumeroCNPJ(String numeroCNPJ) {
		this.numeroCNPJ = numeroCNPJ;
	}

	public String getNumeroCNPJ() {
		return numeroCNPJ;
	}

	public void setCpf(boolean cpf) {
		this.cpf = cpf;
	}

	public boolean isCpf() {
		return cpf;
	}

	public ClasseJudicial getClasseJudicial() {
		return classeJudicial;
	}

	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public Boolean getHabilitaCombo() {
		return habilitaCombo;
	}

	public void setHabilitaCombo(Boolean habilitaCombo) {
		this.habilitaCombo = habilitaCombo;
	}
}
