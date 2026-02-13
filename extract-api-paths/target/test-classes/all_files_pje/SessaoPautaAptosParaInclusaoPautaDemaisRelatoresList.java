package br.com.infox.pje.list;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.cliente.component.NumeroProcesso;
import br.com.infox.cliente.component.tree.AssuntoTrfTreeHandler;
import br.com.infox.cliente.component.tree.ClasseJudicialTreeHandler;
import br.com.infox.cliente.component.tree.TipoPessoaTreeHandler;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoPessoa;

@Name(SessaoPautaAptosParaInclusaoPautaDemaisRelatoresList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class SessaoPautaAptosParaInclusaoPautaDemaisRelatoresList extends EntityList<ProcessoTrf> {

	public static final String NAME = "sessaoPautaAptosParaInclusaoPautaDemaisRelatoresList";

	private static final long serialVersionUID = 1L;

	private OrgaoJulgador orgaoJulgador;
	private ClasseJudicial classeJudicial;
	private AssuntoTrf assunto;
	private Date dataInicio;
	private Date dataFim;
	private Date dataInicioAtualizacao;
	private Date dataFimAtualizacao;
	private String selecionadoPauta;
	private TipoPessoa tipoPessoa;
	private String nomeParte;
	private NumeroProcesso numeroProcesso = new NumeroProcesso();
	private String numeroCNPJ;
	private String numeroCPF;
	private boolean cpf = false;

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProcessoTrf o where ");
		sb.append("o.orgaoJulgador.instancia in ('2','3') ");
		sb.append("and o.processo in (select pe.processo from ProcessoEvento pe ");
		sb.append("where pe.ativo = true and (pe.evento = #{parametroUtil.eventoConclusao} "
				+ "or pe.evento.eventoSuperior = #{parametroUtil.eventoConclusao}) "
				+ "and o = pe.processo and pe.evento != #{parametroUtil.eventoInclusaoPauta}) ");
		sb.append("and o.classeJudicial.pauta = true ");
		sb.append("and o.selecionadoPauta = true ");
		sb.append("and not exists (select s.processoTrf from SessaoPautaProcessoTrf s ");
		sb.append("where s.processoTrf = o and s.dataExclusaoProcessoTrf is null ");
		sb.append("and s.sessao.dataFechamentoSessao is null) ");
		sb.append("and o.orgaoJulgador != #{orgaoJulgadorAtual} ");
		sb.append("and o.orgaoJulgador in (select sco.orgaoJulgador from SessaoComposicaoOrdem sco "
				+ "where sco.sessao = #{sessaoHome.instance}) ");
		return sb.toString();
	}

	private static final String DEFAULT_ORDER = "o.idProcessoTrf";

	/**
	 * Restricao por seleção de um orgaoJulgador
	 */
	private static final String R1 = "o.orgaoJulgador = #{sessaoPautaAptosParaInclusaoPautaDemaisRelatoresList.orgaoJulgador}";

	/**
	 * Restricao por seleção de um classeJudicial
	 */
	private static final String R2 = "o.classeJudicial = #{sessaoPautaAptosParaInclusaoPautaDemaisRelatoresList.classeJudicial}";

	/**
	 * Restricao por seleção de um assuntoTrf
	 */
	private static final String R3 = "o.idProcessoTrf in (select distinct p.idProcessoTrf from ProcessoTrf p "
			+ "inner join p.assuntoTrfList assuntoList "
			+ "where p = o and assuntoList = #{sessaoPautaAptosParaInclusaoPautaDemaisRelatoresList.assunto})";

	/**
	 * Restricao por seleção de um periodo de datas de Distribuição
	 */
	private static final String R4 = "cast(o.dataDistribuicao as date) >= #{sessaoPautaAptosParaInclusaoPautaDemaisRelatoresList.dataInicio}";
	private static final String R5 = "cast(o.dataDistribuicao as date) <= #{sessaoPautaAptosParaInclusaoPautaDemaisRelatoresList.dataFim}";

	/**
	 * Restricao por seleção de um relator
	 */
	private static final String R6 = " o.orgaoJulgador in (select ojc.orgaoJulgador from OrgaoJulgadorCargo ojc, UsuarioLocalizacaoMagistradoServidor pm "
			+ "where ojc.idOrgaoJulgadorCargo = pm.orgaoJulgadorCargo.idOrgaoJulgadorCargo "
			+ "and ojc.orgaoJulgador = o.orgaoJulgador "
			+ "and ojc.cargo = o.cargo "
			+ "and lower(to_ascii(pm.usuarioLocalizacao.usuario.nome)) like "
			+ "'%' || lower(to_ascii(#{sessaoPautaAptosParaInclusaoPautaDemaisRelatoresList.selecionadoPauta})) || '%')";

	/**
	 * Restricao por seleção de uma parte
	 */
	private static final String R7 = "exists (select pp.processoTrf.idProcessoTrf from ProcessoParte pp "
			+ "where pp.processoTrf.idProcessoTrf = o.idProcessoTrf " + "and lower(to_ascii(pp.pessoa.nome)) like "
			+ "'%' || lower(to_ascii(#{sessaoPautaAptosParaInclusaoPautaDemaisRelatoresList.nomeParte})) || '%')";

	/**
	 * Restricao por seleção de um tipoPessoa
	 */
	private static final String R9 = "exists (select pp.processoTrf.idProcessoTrf from ProcessoParte pp "
			+ "where pp.processoTrf.idProcessoTrf = o.idProcessoTrf "
			+ "and pp.pessoa.tipoPessoa = #{sessaoPautaAptosParaInclusaoPautaDemaisRelatoresList.tipoPessoa})";

	/**
	 * Restricao por seleção de um periodo de datas de última conclusão para
	 * julgamento
	 */
	private static final String R10 = "(select cast(max(pe.dataAtualizacao) as date) from ProcessoEvento pe where pe.ativo = true and pe.evento.evento like 'Conclusão' and pe.processo.idProcesso = o.processo.idProcesso) >= #{sessaoPautaAptosParaInclusaoPautaDemaisRelatoresList.dataInicioAtualizacao}";
	private static final String R11 = "(select cast(max(pe.dataAtualizacao) as date) from ProcessoEvento pe where pe.ativo = true and pe.evento.evento like 'Conclusão' and pe.processo.idProcesso = o.processo.idProcesso) <= #{sessaoPautaAptosParaInclusaoPautaDemaisRelatoresList.dataFimAtualizacao}";

	/**
	 * Restrição por número de CPF ou CNPJ
	 */
	private static final String R12 = "exists (select pp.processoTrf.idProcessoTrf from ProcessoParte pp "
			+ "where pp.processoTrf.idProcessoTrf = o.idProcessoTrf "
			+ "and pp.pessoa.idUsuario IN (select pdi.pessoa.idUsuario from PessoaDocumentoIdentificacao pdi "
			+ "where pdi.tipoDocumento.codTipo = 'CPF' and pdi.numeroDocumento like concat('%', #{sessaoPautaAptosParaInclusaoPautaDemaisRelatoresList.numeroCPF} ,'%')))";

	private static final String R13 = "exists (select pp.processoTrf.idProcessoTrf from ProcessoParte pp "
			+ "where pp.processoTrf.idProcessoTrf = o.idProcessoTrf "
			+ "and pp.pessoa.idUsuario IN (select pdi.pessoa.idUsuario from PessoaDocumentoIdentificacao pdi "
			+ "where pdi.tipoDocumento.codTipo = 'CPJ' and pdi.numeroDocumento like concat('%', #{sessaoPautaAptosParaInclusaoPautaDemaisRelatoresList.numeroCNPJ} ,'%')))";

	private static final String R14 = "o.numeroSequencia = #{sessaoPautaAptosParaInclusaoPautaDemaisRelatoresList.numeroProcesso.numeroSequencia}";
	private static final String R15 = "o.ano = #{sessaoPautaAptosParaInclusaoPautaDemaisRelatoresList.numeroProcesso.ano}";
	private static final String R16 = "o.numeroDigitoVerificador = #{sessaoPautaAptosParaInclusaoPautaDemaisRelatoresList.numeroProcesso.numeroDigitoVerificador}";
	private static final String R17 = "o.numeroOrgaoJustica = #{sessaoPautaAptosParaInclusaoPautaDemaisRelatoresList.numeroProcesso.numeroOrgaoJustica}";
	private static final String R18 = "o.numeroOrigem = #{sessaoPautaAptosParaInclusaoPautaDemaisRelatoresList.numeroProcesso.numeroOrigem}";

	@Override
	protected void addSearchFields() {
		addSearchField("orgaoJulgador", SearchCriteria.contendo, R1);
		addSearchField("classeJudicial", SearchCriteria.igual, R2);
		addSearchField("processoTrf.assunto", SearchCriteria.igual, R3);
		addSearchField("dataDistribuicao", SearchCriteria.igual, R4);
		addSearchField("valorCausaIncidente", SearchCriteria.igual, R5);
		addSearchField("nomeParte", SearchCriteria.igual, R6);
		addSearchField("pessoaMarcouPauta", SearchCriteria.igual, R7);
		addSearchField("tipoPessoa", SearchCriteria.igual, R9);
		addSearchField("pessoaRelator", SearchCriteria.igual, R10);
		addSearchField("desProcReferencia", SearchCriteria.igual, R11);
		addSearchField("sustentacaoOral", SearchCriteria.igual, R12);
		addSearchField("pessoaRelator", SearchCriteria.igual, R13);
		addSearchField("numeroSequencia", SearchCriteria.igual, R14);
		addSearchField("ano", SearchCriteria.igual, R15);
		addSearchField("numeroDigitoVerificador", SearchCriteria.igual, R16);
		addSearchField("numeroOrgaoJustica", SearchCriteria.igual, R17);
		addSearchField("numeroOrigem", SearchCriteria.igual, R18);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("numeroProcesso", "processo.numeroProcesso");
		map.put("classeJudicial", "classeJudicial");
		map.put("orgaoJulgador", "orgaoJulgador");
		map.put("idDataSugestaoSessao", "sessaoSugerida");
		return map;
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public ClasseJudicial getClasseJudicial() {
		return classeJudicial;
	}

	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	public AssuntoTrf getAssunto() {
		return assunto;
	}

	public void setAssunto(AssuntoTrf assunto) {
		this.assunto = assunto;
	}

	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	public Date getDataFim() {
		return dataFim;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = dataFim;
	}

	public String getSelecionadoPauta() {
		return selecionadoPauta;
	}

	public void setSelecionadoPauta(String selecionadoPauta) {
		this.selecionadoPauta = selecionadoPauta;
	}

	public void setTipoPessoa(TipoPessoa tipoPessoa) {
		this.tipoPessoa = tipoPessoa;
	}

	public TipoPessoa getTipoPessoa() {
		return tipoPessoa;
	}

	public String getNomeParte() {
		return nomeParte;
	}

	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}

	public Date getDataInicioAtualizacao() {
		return dataInicioAtualizacao;
	}

	public void setDataInicioAtualizacao(Date dataInicioAtualizacao) {
		this.dataInicioAtualizacao = dataInicioAtualizacao;
	}

	public Date getDataFimAtualizacao() {
		return dataFimAtualizacao;
	}

	public void setDataFimAtualizacao(Date dataFimAtualizacao) {
		this.dataFimAtualizacao = dataFimAtualizacao;
	}

	private void limparTrees() {
		ClasseJudicialTreeHandler treeClasse = (ClasseJudicialTreeHandler) Component.getInstance("classeJudicialTree");
		AssuntoTrfTreeHandler assuntoTree = (AssuntoTrfTreeHandler) Component.getInstance("assuntoTrfTree");
		TipoPessoaTreeHandler tipoTree = (TipoPessoaTreeHandler) Component.getInstance("tipoPessoaTree");
		treeClasse.clearTree();
		assuntoTree.clearTree();
		tipoTree.clearTree();
	}

	@Override
	public void newInstance() {
		setDataInicio(null);
		setDataFim(null);
		setDataInicioAtualizacao(null);
		setDataFimAtualizacao(null);
		setSelecionadoPauta(new String());
		setTipoPessoa(new TipoPessoa());
		setNomeParte(new String());
		setNumeroProcesso(new NumeroProcesso());
		setAssunto(null);
		setClasseJudicial(null);
		setTipoPessoa(null);
		setOrgaoJulgador(null);
		limparTrees();
		setNumeroCPF(null);
		setNumeroCNPJ(null);
		setCpf(false);
		super.newInstance();
	}

	public NumeroProcesso getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(NumeroProcesso numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
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

}