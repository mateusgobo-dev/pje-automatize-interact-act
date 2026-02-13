package br.com.infox.pje.list;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.Component;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.cliente.component.NumeroProcesso;
import br.com.infox.cliente.component.tree.AssuntoTrfTreeHandler;
import br.com.infox.cliente.component.tree.ClasseJudicialTreeHandler;
import br.com.infox.cliente.component.tree.TipoPessoaTreeHandler;
import br.com.infox.cliente.home.ConsultaProcessoHome;
import br.com.infox.cliente.home.SessaoProcessoDocumentoHome;
import br.com.infox.cliente.home.SessaoProcessoDocumentoVotoHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoPessoa;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.enums.ProcessoDocumentoStatusEnum;
import br.jus.pje.nucleo.enums.RelatorioStatusEnum;
import br.jus.pje.nucleo.enums.RevisadoEnum;
import br.jus.pje.nucleo.enums.TipoInclusaoEnum;
import br.jus.pje.nucleo.enums.VotoStatusEnum;

/**
 * Classe base para a listagem dos processos pautados em uma sessão.
 * @author lourival
 *
 * @param <T>
 */
public abstract class AbstractSessaoPautaList<T extends SessaoPautaProcessoTrf> extends EntityList<T> {

	private static final long serialVersionUID = -1185799299374028947L;

	private static final String R1 = "o.processoTrf.orgaoJulgador in " + "(select a.orgaoJulgador "
			+ "from SessaoComposicaoOrdem a where a.sessao is #{sessaoPautaProcessoTrfHome.instance.sessao})";
	private static final String R2 = "o.processoTrf.orgaoJulgador in "
			+ "(select p.orgaoJulgador from SessaoComposicaoOrdem p where p.orgaoJulgadorRevisor = #{orgaoJulgadorAtual}))";
	private static final String R3 = "o.processoTrf.orgaoJulgador = #{__COMPONENT_NAME__.orgaoJulgador}";
	private static final String R4 = "o.processoTrf.classeJudicial = #{__COMPONENT_NAME__.classeJudicial}";
	private static final String R5 = "o.processoTrf.idProcessoTrf in (select distinct p.idProcessoTrf from ProcessoTrf p "
			+ "inner join p.assuntoTrfList assuntoList "
			+ "where p = o.processoTrf and assuntoList = #{__COMPONENT_NAME__.assunto})";

	private static final String R6 = "cast(o.processoTrf.dataDistribuicao as date) >= #{__COMPONENT_NAME__.dataInicio}";
	private static final String R7 = "cast(o.processoTrf.dataDistribuicao as date) <= #{__COMPONENT_NAME__.dataFim}";
	private static final String R8 = "o.processoTrf.orgaoJulgadorColegiado in (select a from OrgaoJulgadorCargo a "
			+ "where a = o.processoTrf.orgaoJulgadorColegiado and " + "a.idOrgaoJulgadorCargo in "
			+ "(select plm.orgaoJulgadorCargo.idOrgaoJulgadorCargo from UsuarioLocalizacaoMagistradoServidor plm "
			+ "where lower(to_ascii(plm.usuarioLocalizacao.usuario.nome)) like "
			+ "'%' || lower(to_ascii(#{__COMPONENT_NAME__.relator})) || '%'))";
	private static final String R9 = "exists (select pp.processoTrf.idProcessoTrf from ProcessoParte pp "
			+ "where pp.processoTrf.idProcessoTrf = o.processoTrf.idProcessoTrf "
			+ "and lower(to_ascii(pp.pessoa.nome)) like "
			+ "'%' || lower(to_ascii(#{__COMPONENT_NAME__.nomeParte})) || '%')";
	private static final String R11 = "exists (select pp.processoTrf.idProcessoTrf from ProcessoParte pp "
			+ "where pp.processoTrf.idProcessoTrf = o.processoTrf.idProcessoTrf "
			+ "and pp.pessoa.tipoPessoa = #{__COMPONENT_NAME__.tipoPessoa})";
	private static final String R12 = "(select cast(max(pe.dataAtualizacao) as date) from ProcessoEvento pe where pe.ativo = true and pe.evento.evento like 'Conclusão' and pe.processo.idProcesso = o.processoTrf.processo.idProcesso) >= #{__COMPONENT_NAME__.dataInicioAtualizacao}";
	private static final String R13 = "(select cast(max(pe.dataAtualizacao) as date) from ProcessoEvento pe where pe.ativo = true and pe.evento.evento like 'Conclusão' and pe.processo.idProcesso = o.processoTrf.processo.idProcesso) <= #{__COMPONENT_NAME__.dataFimAtualizacao}";
	private static final String R14 = "exists (select pd from ProcessoDocumento pd "
			+ "where pd.tipoProcessoDocumento.tipoProcessoDocumento like #{__COMPONENT_NAME__.relatorio ? 'Relatório' : null} "
			+ "and pd.processo.idProcesso = o.processoTrf.processo.idProcesso)";
	private static final String R16 = "o.processoTrf.revisado = #{__COMPONENT_NAME__.testeRevisado()}";
	private static final String R17 = "exists (select pp.processoTrf.idProcessoTrf from ProcessoParte pp "
			+ "where pp.processoTrf = o.processoTrf "
			+ "and pp.pessoa.idUsuario IN (select pdi.pessoa.idUsuario from PessoaDocumentoIdentificacao pdi "
			+ "where pdi.tipoDocumento.codTipo = 'CPF' and pdi.numeroDocumento like concat('%', #{__COMPONENT_NAME__.numeroCPF} ,'%')))";
	private static final String R18 = "exists (select pp.processoTrf.idProcessoTrf from ProcessoParte pp "
			+ "where pp.processoTrf = o.processoTrf "
			+ "and pp.pessoa.idUsuario IN (select pdi.pessoa.idUsuario from PessoaDocumentoIdentificacao pdi "
			+ "where pdi.tipoDocumento.codTipo = 'CPJ' and pdi.numeroDocumento like concat('%', #{__COMPONENT_NAME__.numeroCNPJ} ,'%')))";
	private static final String R19 = "o.tipoInclusao = #{__COMPONENT_NAME__.entity.tipoInclusao}";
	private static final String R20 = "o.processoTrf.numeroSequencia = #{__COMPONENT_NAME__.numeroProcesso.numeroSequencia}";
	private static final String R21 = "o.processoTrf.ano = #{__COMPONENT_NAME__.numeroProcesso.ano}";
	private static final String R22 = "o.processoTrf.numeroDigitoVerificador = #{__COMPONENT_NAME__.numeroProcesso.numeroDigitoVerificador}";
	private static final String R24 = "o.processoTrf.numeroOrigem = #{__COMPONENT_NAME__.numeroProcesso.numeroOrigem}";

	private OrgaoJulgador orgaoJulgador;
	private ClasseJudicial classeJudicial;
	private AssuntoTrf assunto;
	private Date dataInicio;
	private Date dataFim;
	private Date dataInicioAtualizacao;
	private Date dataFimAtualizacao;
	private String relator;
	private TipoPessoa tipoPessoa;
	private String nomeParte;
	private Boolean relatorio = Boolean.FALSE;
	private Boolean anotacoes = Boolean.FALSE;
	private RevisadoEnum revisado;
	private NumeroProcesso numeroProcesso = new NumeroProcesso();
	private String visualizacao;
	private String numeroCNPJ;
	private String numeroCPF;
	private boolean cpf = false;
	private VotoStatusEnum tipoStatusVoto;
	private RelatorioStatusEnum tipoStatusRelatorio;
	private ProcessoDocumentoStatusEnum tipoStatusEmenta;
	private List<Integer> ordens = new ArrayList<Integer>(0);

	public AbstractSessaoPautaList() {
		super();
	}
	
	/**
	 * Retorna o nome do componente(Seam) que sera usado na geracao dos HQLs
	 * @return O nome do componente
	 */
	protected abstract String getComponentName();
	
	@Override
	public List<T> list(int maxResult) {
		setEjbql(getDefaultEjbql());
		return super.list(maxResult);
	}
	
	@Override
	public List<T> list() {
		return super.list();
	}

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from SessaoPautaProcessoTrf o ");
		sb.append("join fetch o.consultaProcessoTrf cons ");
		sb.append("where o.dataExclusaoProcessoTrf = null ");
		sb.append("and o.sessao = #{sessaoHome.instance} ");
		OrgaoJulgador oj = Authenticator.getOrgaoJulgadorAtual();
		OrgaoJulgadorColegiado ojc = Authenticator.getOrgaoJulgadorColegiadoAtual();

		// lista apenas processos do OJC da sessão
		sb.append(" AND o.processoTrf.orgaoJulgadorColegiado = #{sessaoHome.instance.orgaoJulgadorColegiado} ");

		if(ojc != null) {
			sb.append(" AND o.processoTrf.orgaoJulgadorColegiado = #{orgaoJulgadorColegiadoAtual} ");
		}
		
		if (oj != null) {
			if (getVisualizacao() == null || getVisualizacao().equals("Relator")) {
				sb.append(" and (o.processoTrf.orgaoJulgador = #{orgaoJulgadorAtual} ");
				sb.append(" or o.orgaoJulgadorUsuarioInclusao = #{orgaoJulgadorAtual}) ");
			} else {
				sb.append(" and exists (select rpt.processoTrf from RevisorProcessoTrf rpt ");
				sb.append("where rpt.processoTrf = o.processoTrf ");
				sb.append("and rpt.orgaoJulgadorRevisor = #{orgaoJulgadorAtual} ");
				sb.append("and rpt.dataFinal is null) ");
				sb.append("and o.orgaoJulgadorUsuarioInclusao = #{orgaoJulgadorAtual}) ");
			}
		}
		if (anotacoes != null && anotacoes) {
			sb.append(" and exists (select 1 from NotaSessaoJulgamento nsj where nsj.processoTrf = o.processoTrf");
			sb.append(" and nsj.sessao = o.sessao and nsj.ativo = true) ");
		}
		UsuarioLocalizacao usuarioLocalizacaoAtual = Authenticator.getUsuarioLocalizacaoAtual();
		if (usuarioLocalizacaoAtual.getPapel().equals(ParametroUtil.instance().getPapelMagistrado())
				|| usuarioLocalizacaoAtual.getPapel().getIdentificador().equals("Asses")) {
			sb.append(ejbqlStatusRelatorio());
			sb.append(ejbqlStatusVoto());
			sb.append(ejbqlStatusEmenta());
		}
		return sb.toString();
	}

	private String ejbqlStatusRelatorio() {
		StringBuilder sb = new StringBuilder();
		if (this.getTipoStatusRelatorio() == RelatorioStatusEnum.NN) {
			sb.append("and not exists (select spd from SessaoProcessoDocumento spd ");
			sb.append("where spd.processoDocumento.ativo = true ");
			sb.append("and spd.processoDocumento.tipoProcessoDocumento = #{parametroUtil.tipoProcessoDocumentoRelatorio} ");
			sb.append("and spd.sessao = o.sessao ");
			sb.append("and spd.processoDocumento.processo.idProcesso = o.processoTrf.processo.idProcesso)");
		} else if (this.getTipoStatusRelatorio() == RelatorioStatusEnum.FN) {
			sb.append("and exists (select spd from SessaoProcessoDocumento spd ");
			sb.append("where spd.processoDocumento.ativo = true ");
			sb.append("and spd.processoDocumento.tipoProcessoDocumento = #{parametroUtil.tipoProcessoDocumentoRelatorio} ");
			sb.append("and spd.sessao = o.sessao ");
			sb.append("and spd.liberacao = false ");
			sb.append("and spd.processoDocumento.processo.idProcesso = o.processoTrf.processo.idProcesso)");
		} else if (this.getTipoStatusRelatorio() == RelatorioStatusEnum.FL) {
			sb.append("and exists (select spd from SessaoProcessoDocumento spd ");
			sb.append("where spd.processoDocumento.ativo = true ");
			sb.append("and spd.processoDocumento.tipoProcessoDocumento = #{parametroUtil.tipoProcessoDocumentoRelatorio} ");
			sb.append("and spd.sessao = o.sessao ");
			sb.append("and spd.liberacao = true ");
			sb.append("and spd.processoDocumento.processo.idProcesso = o.processoTrf.processo.idProcesso)");
		}
		return sb.toString();
	}

	private String ejbqlStatusVoto() {
		StringBuilder sb = new StringBuilder();
		if (this.getTipoStatusVoto() == VotoStatusEnum.NN) {
			sb.append("and not exists (select spdv from SessaoProcessoDocumentoVoto spdv ");
			sb.append("where spdv.sessao = o.sessao ");
			sb.append("and spdv.processoDocumento.processo.idProcesso = o.processoTrf.processo.idProcesso ");
			sb.append("and spdv.processoDocumento.ativo = true) ");
		} else if (this.getTipoStatusVoto() == VotoStatusEnum.FN) {
			sb.append("and exists (select spdv from SessaoProcessoDocumentoVoto spdv ");
			sb.append("where spdv.sessao = o.sessao ");
			sb.append("and spdv.processoDocumento.processo.idProcesso = o.processoTrf.processo.idProcesso ");
			sb.append("and spdv.liberacao = false ");
			sb.append("and spdv.processoDocumento.ativo = true) ");
		} else if (this.getTipoStatusVoto() == VotoStatusEnum.FL) {
			sb.append("and exists (select spdv from SessaoProcessoDocumentoVoto spdv ");
			sb.append("where spdv.sessao = o.sessao ");
			sb.append("and spdv.processoDocumento.processo.idProcesso = o.processoTrf.processo.idProcesso ");
			sb.append("and spdv.liberacao = true ");
			sb.append("and spdv.processoDocumento.ativo = true) ");
		}
		return sb.toString();
	}

	private String ejbqlStatusEmenta() {
		StringBuilder sb = new StringBuilder();
		if (this.getTipoStatusEmenta() == ProcessoDocumentoStatusEnum.NN) {
			sb.append(" and (not exists (select spd from SessaoProcessoDocumento spd ");
			sb.append("where spd.processoDocumento.tipoProcessoDocumento = #{parametroUtil.getTipoProcessoDocumentoAcordao()} ");
			sb.append("and spd.processoDocumento.ativo = true and o.processoTrf.idProcessoTrf = spd.processoDocumento.processo.idProcesso ");
			sb.append("and spd.sessao.idSessao = o.sessao.idSessao))");
		} else if (this.getTipoStatusEmenta() == ProcessoDocumentoStatusEnum.FN) {
			sb.append(" and o.sessao in (select spd.sessao from SessaoProcessoDocumento spd ");
			sb.append("where spd.processoDocumento.ativo = true ");
			sb.append("and spd.processoDocumento.tipoProcessoDocumento = #{parametroUtil.getTipoProcessoDocumentoAcordao()} and ");
			sb.append("spd.liberacao = false)");
		} else if (this.getTipoStatusEmenta() == ProcessoDocumentoStatusEnum.FL) {
			sb.append(" and o.sessao in (select spd.sessao from SessaoProcessoDocumento spd ");
			sb.append("where spd.processoDocumento.tipoProcessoDocumento = #{parametroUtil.getTipoProcessoDocumentoAcordao()} and ");
			sb.append("spd.liberacao = true ");
			sb.append("and spd.processoDocumento.ativo = true ");
			sb.append("and spd.processoDocumento.processo.idProcesso = o.processoTrf.idProcessoTrf))");
		}
		return sb.toString();
	}

	@Override
	public List<T> getResultList() {
		setEjbql(getDefaultEjbql());
		List<T> lista= super.getResultList();
		int qtd = lista.size();
		if (ordens.size() <= qtd) {
			ordens.clear();
			for(int i = 0 ; i < qtd; i++){
				ordens.add(lista.get(i).getNumeroOrdem());
			}
		}
		return lista;
	}

	@Override
	protected void addSearchFields() {
		UsuarioLocalizacao usuarioLocalizacaoAtual = Authenticator.getUsuarioLocalizacaoAtual();
		if (usuarioLocalizacaoAtual.getPapel().getNome().equals("Secretário da Sessão")) {
			addSearchField("processoTrf.orgaoJulgador", SearchCriteria.igual, this.getCriteria(R1));
		} else if (Authenticator.isUsuarioInterno()) {
			addSearchField("processoTrf.orgaoJulgador", SearchCriteria.igual, this.getCriteria(R2));
		}
		addSearchField("processoTrf.orgaoJulgador", SearchCriteria.contendo, this.getCriteria(R3));
		addSearchField("processoTrf.classeJudicial", SearchCriteria.igual, this.getCriteria(R4));
		addSearchField("processoTrf.assuntoTrfList.assuntoTrf", SearchCriteria.igual, this.getCriteria(R5));
		addSearchField("processoTrf.dataDistribuicao", SearchCriteria.igual, this.getCriteria(R6));
		addSearchField("processoTrf.valorCausaIncidente", SearchCriteria.igual, this.getCriteria(R7));
		addSearchField("processoTrf.nomeParte", SearchCriteria.igual, this.getCriteria(R8));
		addSearchField("processoTrf.pessoaMarcouPauta", SearchCriteria.igual, this.getCriteria(R9));
		addSearchField("processoTrf.tipoPessoa", SearchCriteria.igual, this.getCriteria(R11));
		addSearchField("processoTrf.numeroOrgaoJustica", SearchCriteria.igual, this.getCriteria(R12));
		addSearchField("processoTrf.numeroOrigem", SearchCriteria.igual, this.getCriteria(R13));
		addSearchField("processoTrf.apreciadoTutela", SearchCriteria.igual, this.getCriteria(R14));
		addSearchField("processoTrf.revisado", SearchCriteria.igual, this.getCriteria(R16));
		addSearchField("sustentacaoOral", SearchCriteria.igual, this.getCriteria(R17));
		addSearchField("preferencia", SearchCriteria.igual, this.getCriteria(R18));
		addSearchField("tipoInclusao", SearchCriteria.igual, this.getCriteria(R19));
		addSearchField("processoTrf.numeroSequencia", SearchCriteria.igual, this.getCriteria(R20));
		addSearchField("processoTrf.ano", SearchCriteria.igual, this.getCriteria(R21));
		addSearchField("processoTrf.numeroDigitoVerificador", SearchCriteria.igual, this.getCriteria(R22));
		addSearchField("processoTrf.numeroOrigem", SearchCriteria.igual, this.getCriteria(R24));
	}

	private String getCriteria(String criteria) {
		return criteria.replace("__COMPONENT_NAME__", this.getComponentName());
	}
	
	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("numeroProcesso", "o.processoTrf.processo.numeroProcesso");
		map.put("classeJudicial", "o.processoTrf.classeJudicial");
		map.put("orgaoJulgador", "o.processoTrf.orgaoJulgador");
		map.put("tipoInclusao", "o.tipoInclusao");
		return map;
	}

	@Override
	protected String getDefaultOrder() {
		return "o.numeroOrdem";
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

	public List<String> getVisualizacaoProcesso() {
		List<String> list = new ArrayList<String>(2);
		list.add("Relator");
		list.add("Revisor");
		return list;
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
		SessaoProcessoDocumentoVotoHome.instance().setTipoStatusVoto(null);
		SessaoProcessoDocumentoVotoHome.instance().setTipoStatusEmenta(null);
		SessaoProcessoDocumentoHome.instance().setTipoStatusRelatorio(null);
		ConsultaProcessoHome.instance().newInstance();
		setOrgaoJulgador(null);
		setDataInicio(null);
		setDataFim(null);
		setDataInicioAtualizacao(null);
		setDataFimAtualizacao(null);
		setRelator(new String());
		setNomeParte(new String());
		setRelatorio(false);
		setRevisado(null);
		setNumeroProcesso(new NumeroProcesso());
		setClasseJudicial(null);
		setAssunto(null);
		setTipoPessoa(null);
		setVisualizacao(null);
		setNumeroCPF(null);
		setNumeroCNPJ(null);
		limparTrees();
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

	public void setVisualizacao(String visualizacao) {
		this.visualizacao = visualizacao;
	}

	public String getVisualizacao() {
		return visualizacao;
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

	public boolean isCpf() {
		return cpf;
	}
	
	public void setCpf(boolean cpf) {
		this.cpf = cpf;
	}
	
	public VotoStatusEnum getTipoStatusVoto() {
		return this.tipoStatusVoto;
	}
	
	public void setTipoStatusVoto(VotoStatusEnum tipoStatusVoto) {
		this.tipoStatusVoto = tipoStatusVoto;
	}
	
	public RelatorioStatusEnum getTipoStatusRelatorio() {
		return this.tipoStatusRelatorio;
	}
	
	public void setTipoStatusRelatorio(RelatorioStatusEnum tipoStatusRelatorio) {
		this.tipoStatusRelatorio = tipoStatusRelatorio;
	}
	
	
	public ProcessoDocumentoStatusEnum getTipoStatusEmenta() {
		return this.tipoStatusEmenta;
	}
	
	public void setTipoStatusEmenta(ProcessoDocumentoStatusEnum tipoStatusEmenta) {
		this.tipoStatusEmenta = tipoStatusEmenta;
	}
	
	public List<Integer> getOrdens() {
		return ordens;
	}
	
	public void setOrdens(List<Integer> ordens) {
		this.ordens = ordens;
	}
	
	public Boolean getAnotacoes() {
		return anotacoes;
	}
	
	public void setAnotacoes(Boolean anotacoes) {
		this.anotacoes = anotacoes;
	}

}
