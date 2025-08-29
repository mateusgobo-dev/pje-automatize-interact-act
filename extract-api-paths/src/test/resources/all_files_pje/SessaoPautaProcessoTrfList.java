package br.com.infox.pje.list;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoSessao;
import br.jus.pje.nucleo.enums.SituacaoPautaEnum;
import br.jus.pje.nucleo.enums.TipoInclusaoEnum;

@Name(SessaoPautaProcessoTrfList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class SessaoPautaProcessoTrfList extends EntityList<SessaoPautaProcessoTrf> {

	public static final String NAME = "sessaoPautaProcessoTrfList";

	private static final long serialVersionUID = 1L;
	private OrgaoJulgador orgaoJulgador;
	private ClasseJudicial classeJudicial;
	private AssuntoTrf assunto;
	private Date dataAberturaSessao;
	private Date dataFechamentoSessao;
	private String usuario;
	private TipoSessao tipoSessao;
	private String relator;
	private String numeroProcesso = null;
	private SituacaoPautaEnum situacaoPauta;
	private Integer anoSessaoJulgamento;
	private Sessao sessaoDeJulgamento;

	private static final String DEFAULT_EJBQL = "select o from SessaoPautaProcessoTrf o where o.dataExclusaoProcessoTrf is null";

	private static final String DEFAULT_ORDER = "idSessaoPautaProcessoTrf";

	/**
	 * Restricao por seleção de um orgaoJulgador para o ProcessoPautaJulgamento
	 */
	private static final String R1 = "o.processoTrf.orgaoJulgador = #{sessaoPautaProcessoTrfList.orgaoJulgador}";

	/**
	 * Restricao por seleção de um classeJudicial para o ProcessoPautaJulgamento
	 */
	private static final String R2 = "o.processoTrf.classeJudicial = #{sessaoPautaProcessoTrfList.classeJudicial}";

	/**
	 * Restricao por seleção de um assuntoTrf para o ProcessoPautaJulgamento
	 */
	private static final String R3 = "o.processoTrf.idProcessoTrf in (select distinct p.idProcessoTrf from ProcessoTrf p "
			+ "inner join p.assuntoTrfList assuntoList "
			+ "where p = o.processoTrf and assuntoList = #{sessaoPautaProcessoTrfList.assunto})";

	/**
	 * Restricao por seleção de um periodo de datas para o
	 * ProcessoPautaJulgamento
	 */
	private static final String R4 = "cast(o.sessao.dataSessao as date) >= #{sessaoPautaProcessoTrfList.dataAberturaSessao}";
	private static final String R5 = "cast(o.sessao.dataSessao as date) <= #{sessaoPautaProcessoTrfList.dataFechamentoSessao}";

	/**
	 * Restricao por seleção de um usuario para o ProcessoPautaJulgamento
	 */
	private static final String R6 = "lower(o.usuarioInclusao.nome) like concat('%',lower(#{sessaoPautaProcessoTrfList.usuario}), '%')";

	/**
	 * Restricao por seleção de uma sessao para o ProcessoPautaJulgamento
	 */
	private static final String R7 = "o.sessao.tipoSessao = #{sessaoPautaProcessoTrfList.tipoSessao}";

	/**
	 * Restricao por seleção de uma relator para o ProcessoPautaJulgamento
	 */
	private static final String R8 = "exists (select a from OrgaoJulgadorCargo a "
			+ "where a.cargo = o.processoTrf.cargo and "
			+ "a.orgaoJulgador = o.processoTrf.orgaoJulgador and "
			+ "a.idOrgaoJulgadorCargo in "
			+ "(select plm.orgaoJulgadorCargo.idOrgaoJulgadorCargo from UsuarioLocalizacaoMagistradoServidor plm "
			+ "where lower(plm.usuarioLocalizacao.usuario.nome) like concat('%',lower(#{sessaoPautaProcessoTrfList.relator}),'%')))";

	/**
	 *  Restricoes para pegar o numero do processo
	 */
	private static final String R9 = "o.processoTrf.processo.numeroProcesso like concat('%', #{sessaoPautaProcessoTrfList.numeroProcesso}, '%')";
	
	
	/**
	 *  Restrição da situação do processo na pauta.
	 */
	private static final String R10 = "o.tipoInclusao = #{sessaoPautaProcessoTrfList.tipoInclusao}";
	
	/**
	 * Restrição do ano da sessão de julgamento.
	 */
	private static final String R11 = "YEAR(o.sessao.dataSessao) = #{sessaoPautaProcessoTrfList.anoSessaoJulgamento}";
	
	/**
	 * Restrição da sessão de julgamento.
	 */
	private static final String R12 = "o.sessao = #{sessaoPautaProcessoTrfList.sessaoDeJulgamento}";
	
	@Override
	protected void addSearchFields() {
		addSearchField("processoTrf", SearchCriteria.igual);
		addSearchField("processoTrf.orgaoJulgador", SearchCriteria.contendo, R1);
		addSearchField("processoTrf.classeJudicial", SearchCriteria.igual, R2);
		addSearchField("processoTrf.assuntoTrfList.assuntoTrf", SearchCriteria.igual, R3);
		addSearchField("sessao.dataAberturaSessao", SearchCriteria.igual, R4);
		addSearchField("sessao.dataFechamentoSessao", SearchCriteria.igual, R5);
		addSearchField("usuarioInclusao.usuario", SearchCriteria.contendo, R6);
		addSearchField("sessao.tipoSessao", SearchCriteria.igual, R7);
		addSearchField("processoTrf.nomeParte", SearchCriteria.igual, R8);
		addSearchField("processoTrf.numeroSequencia", SearchCriteria.contendo, R9);
		addSearchField("sessao.tipoInclusao", SearchCriteria.igual, R10);
		addSearchField("sessao.dataSessao", SearchCriteria.igual, R11);
		addSearchField("sessao", SearchCriteria.igual, R12);
	}

	@Override
	protected String getDefaultEjbql() {
		return DEFAULT_EJBQL;
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("processoTrf.orgaoJulgador", "processoTrf.orgaoJulgador.orgaoJulgador");
		return map;
	}
	
	@Override
	public void newInstance() {
		setNumeroProcesso(null);
		setOrgaoJulgador(null);
		setClasseJudicial(null);
		setAssunto(null);
		setRelator(null);
		setDataAberturaSessao(null);
		setDataFechamentoSessao(null);
		setUsuario(null);
		setTipoSessao(null);
		setSituacaoPauta(null);
		setAnoSessaoJulgamento(null);
		setSessaoDeJulgamento(null);
		super.newInstance();
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

	public Date getDataAberturaSessao() {
		return dataAberturaSessao;
	}

	public void setDataAberturaSessao(Date dataAberturaSessao) {
		this.dataAberturaSessao = dataAberturaSessao;
	}

	public Date getDataFechamentoSessao() {
		return dataFechamentoSessao;
	}

	public void setDataFechamentoSessao(Date dataFechamentoSessao) {
		this.dataFechamentoSessao = dataFechamentoSessao;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public void setTipoSessao(TipoSessao tipoSessao) {
		this.tipoSessao = tipoSessao;
	}

	public TipoSessao getTipoSessao() {
		return tipoSessao;
	}

	public void setRelator(String relator) {
		this.relator = relator;
	}

	public String getRelator() {
		return relator;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public SituacaoPautaEnum getSituacaoPauta() {
		return situacaoPauta;
	}

	public void setSituacaoPauta(SituacaoPautaEnum situacaoPauta) {
		this.situacaoPauta = situacaoPauta;
	}

	public TipoInclusaoEnum getTipoInclusao() {
		if (getSituacaoPauta() != null) {
			return getSituacaoPauta().getTipoInclusaoEnum();
		}
		return null;
	}

	public Integer getAnoSessaoJulgamento() {
		return anoSessaoJulgamento;
	}

	public void setAnoSessaoJulgamento(Integer anoSessaoJulgamento) {
		this.anoSessaoJulgamento = anoSessaoJulgamento;
	}

	public Sessao getSessaoDeJulgamento() {
		return sessaoDeJulgamento;
	}

	public void setSessaoDeJulgamento(Sessao sessaoDeJulgamento) {
		this.sessaoDeJulgamento = sessaoDeJulgamento;
	}
	
}