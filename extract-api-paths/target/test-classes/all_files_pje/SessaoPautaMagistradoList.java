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
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoPessoa;

@Name(SessaoPautaMagistradoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class SessaoPautaMagistradoList extends EntityList<SessaoPautaProcessoTrf> {

	private NumeroProcesso numeroProcesso = new NumeroProcesso();
	private OrgaoJulgador orgaoJulgador;
	private ClasseJudicial classeJudicial;
	private AssuntoTrf assuntoTrf;
	private TipoPessoa tipoPessoa;
	private String nomeParte;

	public static final String NAME = "sessaoPautaMagistradoList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from SessaoPautaProcessoTrf o ";
	private static final String DEFAULT_ORDER = "o.idSessaoPautaProcessoTrf";

	private static final String R1 = "o.sessao = #{sessaoHome.instance} and o.dataExclusaoProcessoTrf is null";
	private static final String R2 = "o.processoTrf.numeroSequencia = #{processoAdiadoVistaList.numeroProcesso.numeroSequencia}";
	private static final String R3 = "o.processoTrf.ano = #{sessaoPautaMagistradoList.numeroProcesso.ano}";
	private static final String R4 = "o.processoTrf.numeroDigitoVerificador = #{sessaoPautaMagistradoList.numeroProcesso.numeroDigitoVerificador}";
	private static final String R5 = "o.processoTrf.numeroOrgaoJustica = #{sessaoPautaMagistradoList.numeroProcesso.numeroOrgaoJustica}";
	private static final String R6 = "o.processoTrf.numeroOrigem = #{sessaoPautaMagistradoList.numeroProcesso.numeroOrigem}";
	private static final String R7 = "o.processoTrf.orgaoJulgador = #{sessaoPautaMagistradoList.orgaoJulgador}";
	private static final String R8 = "o.processoTrf.classeJudicial = #{sessaoPautaMagistradoList.classeJudicial}";
	private static final String R9 = "exists (select distinct p from ProcessoTrf p "
			+ "inner join p.assuntoTrfList assuntoList "
			+ "where p = o.processoTrf and assuntoList = #{sessaoPautaMagistradoList.assuntoTrf} ";
	private static final String R10 = "o.processoTrf in (select pp from ProcessoParte pp where "
			+ "pp.pessoa.idUsuario IN (select pdi.pessoa.idUsuario from PessoaDocumentoIdentificacao pdi "
			+ "where pdi.tipoDocumento.codTipo = 'CPF' and pdi.numeroDocumento like concat('%', #{consultaProcessoHome.instance.numeroCPF} ,'%')))";
	private static final String R11 = "o.processoTrf in (select pp from ProcessoParte pp where "
			+ "pp.pessoa.idUsuario IN (select pdi.pessoa.idUsuario from PessoaDocumentoIdentificacao pdi "
			+ "where pdi.tipoDocumento.codTipo = 'CPJ' and pdi.numeroDocumento like concat('%', #{consultaProcessoHome.instance.numeroCNPJ} ,'%')))";
	private static final String R12 = "o.processoTrf in (select pp.processoTrf from ProcessoParte pp where pp.pessoa.tipoPessoa =  = #{sessaoPautaMagistradoList.tipoPessoa})";
	private static final String R13 = "exists (select pp from ProcessoParte pp where pp.processoTrf = o.processoTrf and "
			+ "        lower(to_ascii(pp.pessoa.nome)) like '%' || lower(to_ascii(#{sessaoPautaMagistradoList.nomeParte})) || '%')";

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
		addSearchField("processoTrf.cargo", SearchCriteria.igual, R10);
		addSearchField("processoTrf.estruturaInicial", SearchCriteria.contendo, R11);
		addSearchField("processoTrf.processoStatus", SearchCriteria.contendo, R12);
		addSearchField("processoTrf.dtTransitadoJulgado", SearchCriteria.contendo, R13);
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

	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}

	public String getNomeParte() {
		return nomeParte;
	}

}