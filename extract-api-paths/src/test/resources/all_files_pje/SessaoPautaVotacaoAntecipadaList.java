package br.com.infox.pje.list;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.cliente.component.NumeroProcesso;
import br.com.infox.cliente.component.tree.AssuntoTrfTreeHandler;
import br.com.infox.cliente.component.tree.ClasseJudicialTreeHandler;
import br.com.infox.cliente.component.tree.TipoPessoaTreeHandler;
import br.com.infox.cliente.home.ConsultaProcessoHome;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoPessoa;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.enums.RevisadoEnum;
import br.jus.pje.nucleo.enums.TipoInclusaoEnum;

@Name(SessaoPautaVotacaoAntecipadaList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class SessaoPautaVotacaoAntecipadaList extends EntityList<SessaoPautaProcessoTrf> {

	public static final String NAME = "sessaoPautaVotacaoAntecipadaList";

	private static final long serialVersionUID = 1L;

	private OrgaoJulgador orgaoJulgador;
	private ClasseJudicial classeJudicial;
	private AssuntoTrf assunto;
	private Date dataInicio;
	private Date dataFim;
	private Date dataInicioAtualizacao;
	private Date dataFimAtualizacao;
	private String relator;
	private Integer numeroOrdem;
	private TipoPessoa tipoPessoa;
	private String nomeParte;
	private Boolean relatorio = Boolean.FALSE;
	private Boolean anotacoes = Boolean.FALSE;
	private Boolean selecionadoPauta;
	private RevisadoEnum revisado;
	private NumeroProcesso numeroProcesso = new NumeroProcesso();
	private Boolean processoDestaque = null;
	private String numeroCNPJ;
	private String numeroCPF;
	private boolean cpf = false;

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from SessaoPautaProcessoTrf o ");
		sb.append("join fetch o.consultaProcessoTrf cons where ");
		if (Authenticator.getOrgaoJulgadorAtual() != null) {
			sb.append("o.retiradaJulgamento = false ");
			sb.append("and o.dataExclusaoProcessoTrf IS NULL ");
			sb.append("and o.sessao.idSessao = #{sessaoHome.instance.idSessao} ");
			sb.append("and o.processoTrf.orgaoJulgador != #{orgaoJulgadorAtual} ");
		} else {
			sb.append("o.retiradaJulgamento = false ");
			sb.append("and o.dataExclusaoProcessoTrf IS NULL ");
			sb.append("and  o.processoTrf.processo in (select spd.processoDocumento.processo from SessaoProcessoDocumento spd  ");
			sb.append("where spd.liberacao = true ");
			sb.append("and spd.processoDocumento.dataExclusao = null ");
			sb.append("and spd.processoDocumento.ativo = true) ");
			sb.append("and o.sessao.idSessao = #{sessaoHome.instance.idSessao} ");
		}
		if (this.processoDestaque != null && this.processoDestaque) {
			sb.append("and exists (select spdv from SessaoProcessoDocumentoVoto spdv ");
			sb.append("where spdv.destaqueSessao = true ");
			sb.append("and spdv.sessao = o.sessao ");
			sb.append("and spdv.processoDocumento.processo.idProcesso = o.processoTrf.processo.idProcesso) ");
		} else if (this.processoDestaque != null && !this.processoDestaque) {
			sb.append("and not exists (select spdv from SessaoProcessoDocumentoVoto spdv ");
			sb.append("where spdv.destaqueSessao = true ");
			sb.append("and spdv.sessao = o.sessao ");
			sb.append("and spdv.processoDocumento.processo.idProcesso = o.processoTrf.processo.idProcesso) ");
		}
		if (anotacoes != null && anotacoes) {
			sb.append(" and exists (select 1 from NotaSessaoJulgamento nsj where nsj.processoTrf = o.processoTrf and nsj.sessao = o.sessao and nsj.ativo = true) ");
		}		
		return sb.toString();
	}

	@Override
	public List<SessaoPautaProcessoTrf> getResultList() {
		setEjbql(getDefaultEjbql());
		return super.getResultList();
	}

	private static final String DEFAULT_ORDER = "o.numeroOrdem";

	private static final String R1 = "o.processoTrf.orgaoJulgador in " + "(select a.orgaoJulgador "
			+ "from SessaoComposicaoOrdem a where a.sessao is #{sessaoPautaProcessoTrfHome.instance.sessao})";

	private static final String R2 = "o.processoTrf.orgaoJulgador.localizacao = #{usuarioLogadoLocalizacaoAtual.localizacaoFisica} and o.processoTrf.orgaoJulgador in "
			+ "(select p.orgaoJulgadorRevisor from SessaoComposicaoOrdem p where p.orgaoJulgadorRevisor.localizacao = #{usuarioLogadoLocalizacaoAtual.localizacaoFisica})";

	/**
	 * Restricao por seleção de um orgaoJulgador
	 */
	private static final String R3 = "o.processoTrf.orgaoJulgador = #{sessaoPautaVotacaoAntecipadaList.orgaoJulgador}";

	/**
	 * Restricao por seleção de um classeJudicial
	 */
	private static final String R4 = "o.processoTrf.classeJudicial = #{sessaoPautaVotacaoAntecipadaList.classeJudicial}";

	/**
	 * Restricao por seleção de um assuntoTrf
	 */
	private static final String R5 = "o.processoTrf.idProcessoTrf in (select distinct p.idProcessoTrf from ProcessoTrf p "
			+ "inner join p.processoAssuntoList assuntoList "
			+ "where p = o.processoTrf and assuntoList.assuntoTrf = #{sessaoPautaVotacaoAntecipadaList.assunto})";


	/**
	 * Restricao por seleção de um periodo de datas de Distribuição
	 */
	private static final String R6 = "cast(o.processoTrf.dataDistribuicao as date) >= #{sessaoPautaVotacaoAntecipadaList.dataInicio}";
	private static final String R7 = "cast(o.processoTrf.dataDistribuicao as date) <= #{sessaoPautaVotacaoAntecipadaList.dataFim}";

	/**
	 * Restricao por seleção de um relator
	 */
	private static final String R8 = " o.processoTrf.orgaoJulgador in (select ojc.orgaoJulgador from OrgaoJulgadorCargo ojc, UsuarioLocalizacaoMagistradoServidor pm "
			+ "where ojc.idOrgaoJulgadorCargo = pm.orgaoJulgadorCargo.idOrgaoJulgadorCargo "
			+ "and ojc.orgaoJulgador = o.processoTrf.orgaoJulgador "
			+ "and ojc.cargo = o.processoTrf.cargo "
			+ "and lower(to_ascii(pm.usuarioLocalizacao.usuario.nome)) like "
			+ "'%' || lower(to_ascii(#{sessaoPautaVotacaoAntecipadaList.relator})) || '%')";

	/**
	 * Restricao por seleção de uma parte
	 */
	private static final String R9 = "exists (select pp.processoTrf.idProcessoTrf from ProcessoParte pp "
			+ "where pp.processoTrf.idProcessoTrf = o.processoTrf.idProcessoTrf "
			+ "and lower(to_ascii(pp.pessoa.nome)) like "
			+ "'%' || lower(to_ascii(#{sessaoPautaVotacaoAntecipadaList.nomeParte})) || '%')";

	/**
	 * Restricao por seleção de um tipoPessoa
	 */
	private static final String R11 = "exists (select pp.processoTrf.idProcessoTrf from ProcessoParte pp "
			+ "where pp.processoTrf.idProcessoTrf = o.processoTrf.idProcessoTrf "
			+ "and pp.pessoa.tipoPessoa = #{sessaoPautaVotacaoAntecipadaList.tipoPessoa})";

	/**
	 * Restricao por seleção de um periodo de datas de Atualização
	 */
	private static final String R12 = "(select  cast(max(pe.dataAtualizacao) as date) from ProcessoEvento pe where pe.ativo = true and pe.evento.evento like 'Conclusão' and pe.processo.idProcesso = o.processoTrf.processo.idProcesso) >= #{sessaoPautaVotacaoAntecipadaList.dataInicioAtualizacao}";
	private static final String R13 = "(select  cast(max(pe.dataAtualizacao) as date) from ProcessoEvento pe where pe.ativo = true and pe.evento.evento like 'Conclusão' and pe.processo.idProcesso = o.processoTrf.processo.idProcesso) <= #{sessaoPautaVotacaoAntecipadaList.dataFimAtualizacao}";

	/**
	 * Restrição por seleção do check relatorio
	 */
	private static final String R14 = "exists (select pd from ProcessoDocumento pd "
			+ "where pd.tipoProcessoDocumento.tipoProcessoDocumento like #{sessaoPautaRelacaoJulgamentoList.relatorio ? 'Relatório' : null} "
			+ "and pd.processo.idProcesso = o.processoTrf.processo.idProcesso)";

	/**
	 * Restrição por seleção do check relator
	 */
	private static final String R15 = "o.processoTrf.selecionadoPauta = #{sessaoPautaVotacaoAntecipadaList.selecionadoPauta}";

	/**
	 * Restrição por seleção do check revisado
	 */
	private static final String R16 = "o.processoTrf.revisado = #{sessaoPautaVotacaoAntecipadaList.testeRevisado()}";

	/**
	 * Restrição por número de CPF ou CNPJ
	 */
	private static final String R17 = "exists (select pp.processoTrf.idProcessoTrf from ProcessoParte pp "
			+ "where pp.processoTrf.idProcessoTrf = o.processoTrf.idProcessoTrf "
			+ "and pp.pessoa.idUsuario IN (select pdi.pessoa.idUsuario from PessoaDocumentoIdentificacao pdi "
			+ "where pdi.tipoDocumento.codTipo = 'CPF' and pdi.numeroDocumento like concat('%', #{sessaoPautaVotacaoAntecipadaList.numeroCPF}} ,'%')))";

	private static final String R18 = "exists (select pp.processoTrf.idProcessoTrf from ProcessoParte pp "
			+ "where pp.processoTrf.idProcessoTrf = o.processoTrf.idProcessoTrf "
			+ "and pp.pessoa.idUsuario IN (select pdi.pessoa.idUsuario from PessoaDocumentoIdentificacao pdi "
			+ "where pdi.tipoDocumento.codTipo = 'CPJ' and pdi.numeroDocumento like concat('%', #{sessaoPautaVotacaoAntecipadaList.numeroCNPJ} ,'%')))";
	private static final String R19 = "o.tipoInclusao = #{sessaoPautaVotacaoAntecipadaList.entity.tipoInclusao}";
	private static final String R20 = "o.processoTrf.numeroSequencia = #{sessaoPautaVotacaoAntecipadaList.numeroProcesso.numeroSequencia}";
	private static final String R21 = "o.processoTrf.ano = #{sessaoPautaVotacaoAntecipadaList.numeroProcesso.ano}";
	private static final String R22 = "o.processoTrf.numeroDigitoVerificador = #{sessaoPautaVotacaoAntecipadaList.numeroProcesso.numeroDigitoVerificador}";
	private static final String R23 = "o.processoTrf.numeroOrgaoJustica = #{sessaoPautaVotacaoAntecipadaList.numeroProcesso.numeroOrgaoJustica}";
	private static final String R24 = "o.processoTrf.numeroOrigem = #{sessaoPautaVotacaoAntecipadaList.numeroProcesso.numeroOrigem}";
	private static final String R25 = "o.numeroOrdem = #{sessaoPautaVotacaoAntecipadaList.numeroOrdem}";

	@Override
	protected void addSearchFields() {
		if (Authenticator.isPapelPermissaoSecretarioSessao()) {
			addSearchField("processoTrf.orgaoJulgador", SearchCriteria.igual, R1);
		} else if (Authenticator.isUsuarioInterno()) {
			addSearchField("processoTrf.orgaoJulgador", SearchCriteria.igual, R2);
		}
		addSearchField("processoTrf.orgaoJulgador", SearchCriteria.contendo, R3);
		addSearchField("processoTrf.classeJudicial", SearchCriteria.igual, R4);
		addSearchField("processoTrf.assuntoTrfList.assuntoTrf", SearchCriteria.igual, R5);
		addSearchField("processoTrf.dataDistribuicao", SearchCriteria.igual, R6);
		addSearchField("processoTrf.valorCausaIncidente", SearchCriteria.igual, R7);
		addSearchField("processoTrf.nomeParte", SearchCriteria.igual, R8);
		addSearchField("processoTrf.pessoaMarcouPauta", SearchCriteria.igual, R9);
		addSearchField("processoTrf.tipoPessoa", SearchCriteria.igual, R11);
		addSearchField("usuarioInclusao", SearchCriteria.igual, R12);
		addSearchField("usuarioExclusao", SearchCriteria.igual, R13);
		addSearchField("processoTrf.apreciadoTutela", SearchCriteria.igual, R14);
		addSearchField("processoTrf.selecionadoPauta", SearchCriteria.igual, R15);
		addSearchField("processoTrf.revisado", SearchCriteria.igual, R16);
		addSearchField("preferencia", SearchCriteria.igual, R17);
		addSearchField("sustentacaoOral", SearchCriteria.igual, R18);
		addSearchField("tipoInclusao", SearchCriteria.igual, R19);
		addSearchField("processoTrf.numeroSequencia", SearchCriteria.igual, R20);
		addSearchField("processoTrf.ano", SearchCriteria.igual, R21);
		addSearchField("processoTrf.numeroDigitoVerificador", SearchCriteria.igual, R22);
		addSearchField("processoTrf.numeroOrgaoJustica", SearchCriteria.igual, R23);
		addSearchField("processoTrf.numeroOrigem", SearchCriteria.igual, R24);
		addSearchField("numeroOrdem", SearchCriteria.igual, R25);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
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

	public String getRelator() {
		return relator;
	}

	public void setRelator(String relator) {
		this.relator = relator;
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

	public void setRelatorio(Boolean relatorio) {
		this.relatorio = relatorio;
	}

	public Boolean getRelatorio() {
		if (relatorio == null) {
			relatorio = false;
		}
		return relatorio;
	}

	public Boolean getSelecionadoPauta() {
		return selecionadoPauta;
	}

	public void setSelecionadoPauta(Boolean selecionadoPauta) {
		this.selecionadoPauta = selecionadoPauta;
	}

	public RevisadoEnum getRevisado() {
		return revisado;
	}

	public void setRevisado(RevisadoEnum revisado) {
		this.revisado = revisado;
	}

	public RevisadoEnum[] getRevisadoValues() {
		return RevisadoEnum.values();
	}

	public TipoInclusaoEnum[] getTipoInclusaoValues() {
		return TipoInclusaoEnum.values();
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
		ConsultaProcessoHome.instance().newInstance();
		setNumeroProcesso(new NumeroProcesso());
		setTipoPessoa(new TipoPessoa());
		setRelator(new String());
		setNomeParte(new String());
		setDataInicio(null);
		setDataFim(null);
		setDataInicioAtualizacao(null);
		setDataFimAtualizacao(null);
		setRevisado(null);
		setOrgaoJulgador(null);
		setAssunto(null);
		setClasseJudicial(null);
		setTipoPessoa(null);
		limparTrees();
		setNumeroCPF(null);
		setNumeroCNPJ(null);
		setCpf(false);
		setNumeroOrdem(null);
		super.newInstance();
	}

	public void limparCamposCPFCNPJ() {
		setNumeroCPF(null);
		setNumeroCNPJ(null);
	}

	public NumeroProcesso getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(NumeroProcesso numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public Boolean testeRevisado() {
		if (getRevisado() != null) {
			if (getRevisado().equals(RevisadoEnum.S)) {
				return true;
			} else if (getRevisado().equals(RevisadoEnum.N)) {
				return false;
			}
		}
		return null;
	}

	public Boolean getProcessoDestaque() {
		return processoDestaque;
	}

	public void setProcessoDestaque(Boolean processoDestaque) {
		this.processoDestaque = processoDestaque;
	}

	public void setNumeroCNPJ(String numeroCNPJ) {
		this.numeroCNPJ = numeroCNPJ;
	}

	public String getNumeroCNPJ() {
		return numeroCNPJ;
	}

	public void setNumeroCPF(String numeroCPF) {
		this.numeroCPF = numeroCPF;
	}

	public String getNumeroCPF() {
		return numeroCPF;
	}

	public void setCpf(boolean cpf) {
		this.cpf = cpf;
	}

	public boolean isCpf() {
		return cpf;
	}

	public Boolean getAnotacoes() {
		return anotacoes;
	}

	public void setAnotacoes(Boolean anotacoes) {
		this.anotacoes = anotacoes;
	}

	public Integer getNumeroOrdem() {
		return numeroOrdem;
	}

	public void setNumeroOrdem(Integer numeroOrdem) {
		this.numeroOrdem = numeroOrdem;
	}
}
