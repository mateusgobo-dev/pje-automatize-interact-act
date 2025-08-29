package br.com.infox.pje.list;

import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.cliente.home.ConsultaProcessoHome;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoPessoa;

@Name(SessaoPautaProcessoTrfSessaoAbertaList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class SessaoPautaProcessoTrfSessaoAbertaList extends EntityList<SessaoPautaProcessoTrf> {

	public static final String NAME = "sessaoPautaProcessoTrfSessaoAbertaList";

	private static final long serialVersionUID = 1L;

	private String numeroProcesso;
	private OrgaoJulgador orgaoJulgador;
	private ClasseJudicial classeJudicial;
	private AssuntoTrf assuntoTrf;
	private TipoPessoa tipoPessoa;
	private String nomeParte;

	private static final String DEFAULT_EJBQL = "select o from SessaoPautaProcessoTrf o ";
	private static final String DEFAULT_ORDER = "o.idSessaoPautaProcessoTrf";

	private static final String R1 = "o.sessao = #{sessaoHome.instance} and o.dataExclusaoProcessoTrf is null";
	private static final String R2 = "o.processoTrf.processo.numeroProcesso like concat('%', #{sessaoPautaProcessoTrfSessaoAbertaList.numeroProcesso}, '%')";
	private static final String R3 = "o.processoTrf.orgaoJulgador = #{sessaoPautaProcessoTrfSessaoAbertaList.orgaoJulgador}";
	private static final String R4 = "o.processoTrf.classeJudicial = #{sessaoPautaProcessoTrfSessaoAbertaList.classeJudicial}";
	/**
	 * Correção de bugs - PJE-JT
	 * 
	 * @author rodrigo -- 15/11/2011 -- [PJE-888][PJE-881] A HQL foi alterada.
	 *         Troquei as chamadas à lista assuntoTrfList por
	 *         processoAssuntoList, pois a lista assuntoTrfList é um atributo
	 *         transiente de ProcessoTrf.
	 */
	private static final String R5 = "exists (select distinct p from ProcessoTrf p "
			+ "inner join p.processoAssuntoList assuntoList "
			+ "where p = o.processoTrf and assuntoList.assuntoTrf = #{sessaoPautaProcessoTrfSessaoAbertaList.assuntoTrf})";
	/**
	 * PJE-JT: Fim
	 */
	private static final String R6 = "o.processoTrf in (select pp.processoTrf from ProcessoParte pp where "
			+ "                  pp.pessoa.idUsuario IN (select pdi.pessoa.idUsuario from PessoaDocumentoIdentificacao pdi "
			+ "                                              where pdi.tipoDocumento.codTipo = 'CPF' "
			+ "                                              and pdi.numeroDocumento like concat('%', #{consultaProcessoHome.instance.numeroCPF} ,'%')))";
	private static final String R7 = "o.processoTrf in (select pp.processoTrf from ProcessoParte pp where "
			+ "                  pp.pessoa.idUsuario IN (select pdi.pessoa.idUsuario from PessoaDocumentoIdentificacao pdi "
			+ "                                              where pdi.tipoDocumento.codTipo = 'CPJ' "
			+ "                                              and pdi.numeroDocumento like concat('%', #{consultaProcessoHome.instance.numeroCNPJ} ,'%')))";
	private static final String R8 = "o.processoTrf in (select pp.processoTrf from ProcessoParte pp where pp.pessoa.tipoPessoa = #{sessaoPautaProcessoTrfSessaoAbertaList.tipoPessoa})";
	private static final String R9 = "exists (select pp from ProcessoParte pp where pp.processoTrf = o.processoTrf and "
			+ "        lower(to_ascii(pp.pessoa.nome)) like '%' || lower(to_ascii(#{sessaoPautaProcessoTrfSessaoAbertaList.nomeParte})) || '%')";
	private static final String R10 = "o.tipoInclusao = #{sessaoPautaProcessoTrfSessaoAbertaList.entity.tipoInclusao}";

	@Override
	protected void addSearchFields() {
		addSearchField("idSessaoPautaProcessoTrf", SearchCriteria.igual, R1);
		addSearchField("processoTrf", SearchCriteria.igual, R2);
		addSearchField("sessao", SearchCriteria.igual, R3);
		addSearchField("dataInclusaoProcessoTrf", SearchCriteria.igual, R4);
		addSearchField("dataExclusaoProcessoTrf", SearchCriteria.igual, R5);
		addSearchField("sustentacaoOral", SearchCriteria.igual, R6);
		addSearchField("preferencia", SearchCriteria.igual, R7);
		addSearchField("finalizadoPedidoVista", SearchCriteria.igual, R8);
		addSearchField("retiradaJulgamento", SearchCriteria.igual, R9);
		addSearchField("tipoInclusao", SearchCriteria.igual, R10);
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
		map.put("processoTrf", "o.processoTrf.processo.numeroProcesso");
		map.put("classeJudicial", "o.processoTrf.classeJudicial.classeJudicial");
		return map;
	}

	@Override
	public void newInstance() {
		numeroProcesso = null;
		orgaoJulgador = null;
		classeJudicial = null;
		assuntoTrf = null;
		tipoPessoa = null;
		nomeParte = null;
		ConsultaProcessoHome.instance().getInstance().setNumeroCNPJ(null);
		ConsultaProcessoHome.instance().getInstance().setNumeroCPF(null);
		super.newInstance();
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public String getNumeroProcesso() {
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

	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}

	public String getNomeParte() {
		return nomeParte;
	}
}