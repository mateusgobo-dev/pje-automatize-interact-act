package br.com.infox.pje.list;

import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.cliente.component.NumeroProcesso;
import br.com.infox.cliente.home.ConsultaProcessoHome;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoPessoa;
import br.jus.pje.nucleo.enums.TipoInclusaoEnum;

@Name(SessaoPautaSecretarioProcessoNuloAguardandoJulgamentoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class SessaoPautaSecretarioProcessoNuloAguardandoJulgamentoList extends EntityList<SessaoPautaProcessoTrf> {

	private NumeroProcesso numeroProcesso = new NumeroProcesso();
	private OrgaoJulgador orgaoJulgador;
	private ClasseJudicial classeJudicial;
	private AssuntoTrf assuntoTrf;
	private TipoPessoa tipoPessoa;
	private String processoParte;
	private Boolean cpf = Boolean.TRUE;
	private String numCpf;
	private String numCnpj;
	private TipoInclusaoEnum tipoInclusao;

	public void clearCpfCnpj() {
		setNumCpf(null);
		setNumCnpj(null);
	}

	public static final String NAME = "sessaoPautaSecretarioProcessoNuloAguardandoJulgamentoList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from SessaoPautaProcessoTrf o where o.dataExclusaoProcessoTrf is null "
			+ "and (o.situacaoJulgamento = 'AJ' or o.situacaoJulgamento = 'EJ') ";

	private static final String DEFAULT_ORDER = "idSessaoPautaProcessoTrf";
	private static final String R1 = "o.sessao = #{sessaoHome.instance}";

	private static final String R2 = "o.processoTrf.numeroSequencia = #{sessaoPautaSecretarioProcessoNuloList.numeroProcesso.numeroSequencia}";
	private static final String R3 = "o.processoTrf.ano = #{sessaoPautaSecretarioProcessoNuloList.numeroProcesso.ano}";
	private static final String R4 = "o.processoTrf.numeroDigitoVerificador = #{sessaoPautaSecretarioProcessoNuloList.numeroProcesso.numeroDigitoVerificador}";
	private static final String R5 = "o.processoTrf.numeroOrgaoJustica = #{sessaoPautaSecretarioProcessoNuloList.numeroProcesso.numeroOrgaoJustica}";
	private static final String R6 = "o.processoTrf.numeroOrigem = #{sessaoPautaSecretarioProcessoNuloList.numeroProcesso.numeroOrigem}";

	private static final String R7 = "o.processoTrf.orgaoJulgador = #{sessaoPautaSecretarioProcessoNuloList.orgaoJulgador}";

	private static final String R8 = "o.processoTrf.classeJudicial = #{sessaoPautaSecretarioProcessoNuloList.classeJudicial}";

	private static final String R9 = "o.processoTrf.idProcessoTrf in (select distinct p.idProcessoTrf from ProcessoTrf p "
			+ "inner join p.assuntoTrfList a "
			+ "where o = p and "
			+ "a = #{sessaoPautaSecretarioProcessoNuloList.assuntoTrf})";

	private static final String R10 = " exists (select pp.processoTrf.idProcessoTrf from ProcessoParte pp "
			+ "where pp.processoTrf.idProcessoTrf = o.processoTrf.idProcessoTrf "
			+ "and pp.pessoa.tipoPessoa = #{sessaoPautaSecretarioProcessoNuloList.tipoPessoa})";

	private static final String R11 = "exists (select pp.processoTrf.idProcessoTrf from ProcessoParte pp "
			+ "where pp.processoTrf.idProcessoTrf = o.processoTrf.idProcessoTrf "
			+ "and lower(to_ascii(pp.pessoa.nome)) like "
			+ "'%' || lower(to_ascii(#{sessaoPautaSecretarioProcessoNuloList.processoParte})) || '%')";

	private static final String R12 = "o.processoTrf IN (select pp from ProcessoParte pp "
			+ "where pp.processoTrf = o.processoTrf and pp.pessoa.idUsuario IN (select pdi.pessoa.idUsuario from PessoaDocumentoIdentificacao pdi "
			+ "where pdi.tipoDocumento.codTipo = 'CPF' and pdi.numeroDocumento like concat('%', #{consultaProcessoHome.instance.numeroCPF} ,'%')))";
	private static final String R13 = "o.processoTrf IN (select pp from ProcessoParte pp "
			+ "where pp.processoTrf = o.processoTrf and pp.pessoa.idUsuario IN (select pdi.pessoa.idUsuario from PessoaDocumentoIdentificacao pdi "
			+ "where pdi.tipoDocumento.codTipo = 'CPJ' and pdi.numeroDocumento like concat('%', #{consultaProcessoHome.instance.numeroCNPJ} ,'%')))";
	private static final String R14 = "o.tipoInclusao = #{sessaoPautaSecretarioProcessoNuloList.tipoInclusao}";

	@Override
	protected void addSearchFields() {
		addSearchField("sessao", SearchCriteria.igual, R1);

		addSearchField("processoTrf.numeroSequencia", SearchCriteria.igual, R2);
		addSearchField("processoTrf.ano", SearchCriteria.igual, R3);
		addSearchField("processoTrf.numeroDigitoVerificador", SearchCriteria.igual, R4);
		addSearchField("processoTrf.numeroOrgaoJustica", SearchCriteria.igual, R5);
		addSearchField("processoTrf.numeroOrigem", SearchCriteria.igual, R6);

		addSearchField("processoTrf.orgaoJulgador", SearchCriteria.igual, R7);
		addSearchField("processoTrf.classeJudicial", SearchCriteria.igual, R8);
		addSearchField("processoTrf.assuntoTrfList", SearchCriteria.igual, R9);
		addSearchField("processoTrf.tipoPessoa", SearchCriteria.igual, R10);
		addSearchField("processoTrf.pessoaMarcouPauta", SearchCriteria.contendo, R11);
		addSearchField("processoTrf.orgaoJulgador.orgaoJulgador", SearchCriteria.contendo, R12);
		addSearchField("processoTrf.orgaoJulgador.localizacao", SearchCriteria.contendo, R13);
		addSearchField("tipoInclusao", SearchCriteria.igual, R14);
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
		map.put("classeJudicial", "processoTrf.classeJudicial.classeJudicial");
		return map;
	}

	public void setNumeroProcesso(NumeroProcesso numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public NumeroProcesso getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	public ClasseJudicial getClasseJudicial() {
		return classeJudicial;
	}

	public void setAssuntoTrf(AssuntoTrf assuntoTrf) {
		this.assuntoTrf = assuntoTrf;
	}

	public AssuntoTrf getAssuntoTrf() {
		return assuntoTrf;
	}

	public void setTipoPessoa(TipoPessoa tipoPessoa) {
		this.tipoPessoa = tipoPessoa;
	}

	public TipoPessoa getTipoPessoa() {
		return tipoPessoa;
	}

	public void setProcessoParte(String processoParte) {
		this.processoParte = processoParte;
	}

	public String getProcessoParte() {
		return processoParte;
	}

	public void setCpf(Boolean cpf) {
		this.cpf = cpf;
	}

	public Boolean getCpf() {
		return cpf;
	}

	public void setNumCpf(String numCpf) {
		this.numCpf = numCpf;
	}

	public String getNumCpf() {
		return numCpf;
	}

	public void setNumCnpj(String numCnpj) {
		this.numCnpj = numCnpj;
	}

	public String getNumCnpj() {
		return numCnpj;
	}

	public TipoInclusaoEnum getTipoInclusao() {
		return tipoInclusao;
	}

	public void setTipoInclusao(TipoInclusaoEnum tipoInclusao) {
		this.tipoInclusao = tipoInclusao;
	}

	@Override
	public void newInstance() {
		numeroProcesso = new NumeroProcesso();
		orgaoJulgador = null;
		classeJudicial = null;
		assuntoTrf = null;
		tipoPessoa = null;
		processoParte = null;
		cpf = Boolean.TRUE;
		numCpf = null;
		numCnpj = null;
		tipoInclusao = null;

		if (ConsultaProcessoHome.instance() != null) {
			ConsultaProcessoHome.instance().getInstance().setNumeroCNPJ(null);
			ConsultaProcessoHome.instance().getInstance().setNumeroCPF(null);
		}
		super.newInstance();
	}
}