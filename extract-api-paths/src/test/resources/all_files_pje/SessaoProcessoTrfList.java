package br.com.infox.pje.list;

import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.DAO.SearchField;
import br.com.infox.pje.action.SessaoJulgamentoAction;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.enums.AdiadoVistaEnum;
import br.jus.pje.nucleo.enums.SituacaoVotoEnum;
import br.jus.pje.nucleo.enums.TipoSituacaoPautaEnum;

@Name(SessaoProcessoTrfList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class SessaoProcessoTrfList extends EntityList<SessaoPautaProcessoTrf> {

	public static final String NAME = "sessaoProcessoTrfList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select distinct(o) from SessaoPautaProcessoTrf o inner join "
			+ "o.sessao s inner join s.sessaoComposicaoOrdemList scol " + "where o.dataExclusaoProcessoTrf is null ";
	private static final String DEFAULT_ORDER = "o.processoTrf";

	private static final String R0 = "o.orgaoJulgadorUsuarioInclusao = #{sessaoJulgamentoAction.orgaoJulgadorUsuarioInclusao}";
	private static final String R1 = "s = #{sessaoJulgamentoAction.sessao}";
	private static final String R2 = "o.processoTrf.processo.numeroProcesso like #{sessaoJulgamentoAction.numeroProcesso}";
	private static final String R3 = "o.processoTrf.orgaoJulgador = #{sessaoJulgamentoAction.orgaoJulgadorSessao}";
	private static final String R4 = "o.processoTrf.classeJudicial = #{sessaoJulgamentoAction.classeJudicial}";
	private static final String R5 = "exists (select pa from ProcessoAssunto pa "
			+ "where pa.processoTrf = o.processoTrf and " + "pa.assuntoTrf = #{sessaoJulgamentoAction.assuntoTrf})";

	private String r6ComVoto = "o.dataExclusaoProcessoTrf is null "
			+ "and exists (select spdv from SessaoProcessoDocumentoVoto spdv "
			+ "where spdv.processoDocumento.processo.idProcesso = o.processoTrf.idProcessoTrf "
			+ "and spdv.sessao = s "
			+ "and spdv.processoDocumento.dataExclusao is null "
			+ "and spdv.orgaoJulgador.idOrgaoJulgador = #{authenticator.getOrgaoJulgadorAtual().getIdOrgaoJulgador()} "
			+ "and (exists (select spd from SessaoProcessoDocumento spd "
			+ "where spd.processoDocumento.tipoProcessoDocumento = #{parametroUtil.tipoProcessoDocumentoAcordao} "
			+ "and spd.processoDocumento.processo.idProcesso = o.processoTrf.processo.idProcesso  "
			+ "and spd.processoDocumento.ativo = true  "
			+ "and spd.processoDocumento.processoDocumentoBin in (select pd.processoDocumentoBin from ProcessoDocumentoBinPessoaAssinatura pd "
			+ "inner join pd.processoDocumentoBin.processoDocumentoList list "
			+ "where list.tipoProcessoDocumento = #{parametroUtil.tipoProcessoDocumentoAcordao} "
			+ "and list.processo.idProcesso =  spd.processoDocumento.processo.idProcesso "
			+ "and cast(pd.dataAssinatura as date) < cast(spdv.processoDocumento.dataInclusao as date)) "
			+ "order by spd.processoDocumento.dataInclusao desc ) " + "or "
			+ "not exists (select spd from SessaoProcessoDocumento spd "
			+ "where spd.processoDocumento.tipoProcessoDocumento = #{parametroUtil.tipoProcessoDocumentoAcordao} "
			+ "and spd.processoDocumento.processo.idProcesso = o.processoTrf.processo.idProcesso "
			+ "and spd.processoDocumento.ativo = true " + "order by spd.processoDocumento.dataInclusao desc )) " + ") ";

	private String r6SemVoto = "o.dataExclusaoProcessoTrf is null "
			+ "and not exists (select spdv from SessaoProcessoDocumentoVoto spdv "
			+ "where spdv.processoDocumento.processo.idProcesso = o.processoTrf.idProcessoTrf "
			+ "and spdv.sessao = s "
			+ "and spdv.processoDocumento.dataExclusao is null "
			+ "and spdv.orgaoJulgador.idOrgaoJulgador = #{authenticator.getOrgaoJulgadorAtual().getIdOrgaoJulgador()} "
			+ "and (exists (select spd from SessaoProcessoDocumento spd "
			+ "where spd.processoDocumento.tipoProcessoDocumento = #{parametroUtil.tipoProcessoDocumentoAcordao} "
			+ "and spd.processoDocumento.processo.idProcesso = o.processoTrf.processo.idProcesso  "
			+ "and spd.processoDocumento.ativo = true  "
			+ "and spd.processoDocumento.processoDocumentoBin in (select pd.processoDocumentoBin from ProcessoDocumentoBinPessoaAssinatura pd "
			+ "inner join pd.processoDocumentoBin.processoDocumentoList list "
			+ "where list.tipoProcessoDocumento = #{parametroUtil.tipoProcessoDocumentoAcordao} "
			+ "and list.processo.idProcesso =  spd.processoDocumento.processo.idProcesso "
			+ "and cast(pd.dataAssinatura as date) < cast(spdv.processoDocumento.dataInclusao as date)) "
			+ "order by spd.processoDocumento.dataInclusao desc ) " + "or "
			+ "not exists (select spd from SessaoProcessoDocumento spd "
			+ "where spd.processoDocumento.tipoProcessoDocumento = #{parametroUtil.tipoProcessoDocumentoAcordao} "
			+ "and spd.processoDocumento.processo.idProcesso = o.processoTrf.processo.idProcesso "
			+ "and spd.processoDocumento.ativo = true " + "order by spd.processoDocumento.dataInclusao desc )) " + ") ";

	private static final String R7 = "exists (select ul.usuario " + "from UsuarioLocalizacaoMagistradoServidor ulms "
			+ "inner join ulms.usuarioLocalizacao ul " + "where ulms.orgaoJulgadorCargo.recebeDistribuicao = true "
			+ "and ulms.orgaoJulgadorCargo.orgaoJulgador = o.processoTrf.orgaoJulgador "
			+ "and upper(ul.usuario.nome) like upper(concat('%', "
			+ "to_ascii(#{sessaoJulgamentoAction.nomeRelator}), '%')))";
	private static final String R8 = "exists (select pp.processoTrf.idProcessoTrf from ProcessoParte pp "
			+ "where pp.processoTrf.idProcessoTrf = o.processoTrf.idProcessoTrf "
			+ "and lower(to_ascii(pp.pessoa.nome)) like concat('%', lower(to_ascii"
			+ "(#{sessaoJulgamentoAction.nomeParte})), '%'))";
	private static final String R11 = "o.situacaoJulgamento = '" + TipoSituacaoPautaEnum.AJ + "'";
	private static final String R12 = "o.situacaoJulgamento = '" + TipoSituacaoPautaEnum.EJ + "'";
	private static final String R13 = "o.situacaoJulgamento = '" + TipoSituacaoPautaEnum.JG + "'";
	private static final String R14 = "o.preferencia = true";
	private static final String R15 = "o.adiadoVista = '" + AdiadoVistaEnum.PV + "'";
	private static final String R16 = "o.sustentacaoOral = true";
	private static final String R17 = "o.adiadoVista = '" + AdiadoVistaEnum.AD + "' and "
			+ "o.retiradaJulgamento = false";
	private static final String R18 = "o.retiradaJulgamento = true";
	private static final String R19 = "o.orgaoJulgadorPedidoVista = #{sessaoJulgamentoAction.orgaoJulgadorPedidoVista}";
	private static final String R20 = "o.sustentacaoOral = #{sessaoJulgamentoAction.sustentacaoOral}";
	private static final String R21 = "o.preferencia = #{sessaoJulgamentoAction.preferencia}";
	private static final String R22 = "exists(select spd from SessaoProcessoDocumentoVoto spd "
			+ "where spd.processoDocumento.processo.idProcesso = o.processoTrf.idProcessoTrf "
			+ "and spd.sessao = s and spd.destaqueSessao = #{sessaoJulgamentoAction.destaqueDiscussao})";

	@Override
	protected void addSearchFields() {
		addSearchField("orgaoJulgadorUI", SearchCriteria.igual, R0);
		addSearchField("sessao", SearchCriteria.igual, R1);
		addSearchField("numeroProcesso", SearchCriteria.igual, R2);
		addSearchField("orgaoJulgadorSessao", SearchCriteria.igual, R3);
		addSearchField("classeJudicial", SearchCriteria.igual, R4);
		addSearchField("assuntoTrf", SearchCriteria.igual, R5);
		addSearchField("relator", SearchCriteria.igual, R7);
		addSearchField("nomeParte", SearchCriteria.igual, R8);
		addSearchField("orgaoJulgadorPedidoVista", SearchCriteria.igual, R19);
		addSearchField("sustentacaoOral", SearchCriteria.igual, R20);
		addSearchField("preferencia", SearchCriteria.igual, R21);
		addSearchField("destaqueSessao", SearchCriteria.igual, R22);
	}

	/**
	 * Inclui a restrição para filtrar os processos de acordo com a situação do
	 * voto selecionado pelo usuario no popUp do painel do magistrado na sessão.
	 * 
	 * @param voto
	 *            selecionado pelo usuário
	 * @param tipoProcessoDocumentoVoto
	 *            tipo do processoDocumento que seja voto.
	 */
	public void addSearchFields(SituacaoVotoEnum voto, TipoProcessoDocumento tipoProcessoDocumentoVoto) {
		setEjbql(DEFAULT_EJBQL);
		if (voto != null) {
			if (voto == SituacaoVotoEnum.SIM) {
				setEjbql(getDefaultEjbql() + " and " + r6ComVoto + " ");
			} else if (voto == SituacaoVotoEnum.NAO) {
				setEjbql(getDefaultEjbql() + " and " + r6SemVoto + " ");
			}
		}
		addSearchFields();
	}

	/**
	 * Realiza um filtro de acordo com o tipo selecionado na legenda no popUp do
	 * painel do magistrado na sessão.
	 * 
	 * @param sigla
	 *            selecionada para realizar o filtro
	 */
	public void addSearchFields(String sigla) {
		String ejbql = DEFAULT_EJBQL + "and ";
		if (sigla.equals(String.valueOf(TipoSituacaoPautaEnum.AJ))) {
			ejbql += R11;
		} else if (sigla.equals(String.valueOf(TipoSituacaoPautaEnum.EJ))) {
			ejbql += R12;
		} else if (sigla.equals(String.valueOf(TipoSituacaoPautaEnum.JG))) {
			ejbql += R13;
		} else if (sigla.equals(SessaoJulgamentoAction.SIGLAS_LEGENDAS[0])) {
			ejbql += R14;
		} else if (sigla.equals(String.valueOf(AdiadoVistaEnum.PV))) {
			ejbql += R15;
		} else if (sigla.equals(SessaoJulgamentoAction.SIGLAS_LEGENDAS[1])) {
			ejbql += R16;
		} else if (sigla.equals(String.valueOf(AdiadoVistaEnum.AD))) {
			ejbql += R17;
		} else if (sigla.equals(SessaoJulgamentoAction.SIGLAS_LEGENDAS[2])) {
			ejbql += R18;
		}
		setEjbql(ejbql);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("processoTrf", "o.processoTrf.processo.numeroProcesso");
		return map;
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
	public Map<String, SearchField> getSearchFieldMap() {
		return super.getSearchFieldMap();
	}

	@Override
	public void setSearchFieldMap(Map<String, SearchField> searchFieldMap) {
		super.setSearchFieldMap(searchFieldMap);
	}

}