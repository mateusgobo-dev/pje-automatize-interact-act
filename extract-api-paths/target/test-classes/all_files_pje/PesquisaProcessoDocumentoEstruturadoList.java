package br.com.infox.editor.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.manager.UsuarioLocalizacaoMagistradoServidorManager;
import br.jus.pje.nucleo.entidades.Cargo;

@Name(PesquisaProcessoDocumentoEstruturadoList.NAME)
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class PesquisaProcessoDocumentoEstruturadoList extends EntityList<Map<String, Object>> {


	private static final String HQL = "select new map(o.idProcessoDocumentoEstruturadoTopico " +
			"as idProcessoDocumentoEstruturadoTopico, o.titulo as titulo, o.conteudo as conteudo," +
			"o.dataModificacao as dataModificacao, " +
			"o.processoDocumentoEstruturado.processoTrf.processo.numeroProcesso as numeroProcesso," +
			"o.pessoa.nome as nomePessoa, o.processoDocumentoEstruturado.idProcessoDocumentoEstruturado as idProcessoDocumentoEstruturado, " +
			"processoDocumentoBin.dataAssinatura as dataAssinatura, processoTrf.idProcessoTrf as idProcessoTrf, " +
			"processoDocumentoBin.idProcessoDocumentoBin as idProcessoDocumentoBin, " +
			"tipoProcessoDocumento.tipoProcessoDocumento as tipoProcessoDocumento, " +
			"processoTrf.classeJudicial.classeJudicialSigla as classeJudicialSigla, " +
			"processoTrf.classeJudicial.classeJudicial as classeJudicial)  " +
			"from ProcessoDocumentoEstruturadoTopico o  " +
			"inner join o.processoDocumentoEstruturado.processoDocumentoTrfLocal.processoDocumento processoDocumento " +
			"inner join processoDocumento.tipoProcessoDocumento tipoProcessoDocumento " +			
			"inner join processoDocumento.processoDocumentoBin processoDocumentoBin " +
			"inner join o.processoDocumentoEstruturado.processoTrf processoTrf ";

	public static final String NAME = "pesquisaProcessoDocumentoEstruturadoList";

	private static final long serialVersionUID = 1L;

	private static final String ORDER_HQL = "case when processoDocumentoBin.dataAssinatura is null then 0 else 1 end, processoDocumentoBin.dataAssinatura desc, o.dataModificacao desc";
	private static final String DEFAULT_ORDER = "defalt_order";

	private static final String R1 = "o.processoDocumentoEstruturado <> #{pesquisaDocumentoAction.processoDocumentoEstruturadoEdicao}";
	private static final String R2 = "processoTrf.orgaoJulgador = #{pesquisaDocumentoAction.orgaoJulgador}";
	private static final String R3 = "exists (select pa from ProcessoDocumentoBinPessoaAssinatura pa where pa.processoDocumentoBin = processoDocumentoBin and pa.pessoa = #{pesquisaDocumentoAction.pessoaMagistrado})";

	private List<Cargo> cargoVisibilidadeList;

	@Override
	protected void addSearchFields() {
		addSearchField("processoDocumentoEstruturado", SearchCriteria.igual, R1);
		addSearchField("orgaoJulgador", SearchCriteria.igual, R2);
		addSearchField("pessoaMagistradoAssinatura", SearchCriteria.igual, R3);
	}
	
	@Override
	public void newInstance() {
	}

	@Override
	public List<Map<String, Object>> list(int maxResult) {
		List<Map<String, Object>> list = super.list(maxResult);
		return list;
	}
	
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append(HQL);
		sb.append("where 1=1 ");
		sb.append("AND (processoDocumentoBin.dataAssinatura is not null ");
		sb.append("  OR ").append(getCondicaoVisibilidadeProcesso());
		sb.append("     ) ");
		sb.append("AND o.idProcessoDocumentoEstruturadoTopico in (#{pesquisaDocumentoAction.resultListId}) ");
		return sb.toString();
	}
	
	//TODO TRATAR segundo grau vai precisar verificar essa regra
	private String getCondicaoVisibilidadeProcesso() {
		UsuarioLocalizacaoMagistradoServidorManager manager = (UsuarioLocalizacaoMagistradoServidorManager) Component.getInstance("usuarioLocalizacaoMagistradoServidorManager");
		cargoVisibilidadeList = manager.getCargoVisibilidadeList(Authenticator.getUsuarioLocalizacaoMagistradoServidorAtual());
		boolean visibilidadeTodosCargos = manager.possuiVisibilidadeTodosCargos(cargoVisibilidadeList);
		StringBuilder sb = new StringBuilder();
		sb.append("(processoTrf.orgaoJulgador = #{orgaoJulgadorAtual} ");
		if (!ParametroUtil.instance().isPrimeiroGrau()) {
			sb.append("AND processoTrf.orgaoJulgadorColegiado = #{orgaoJulgadorColegiadoAtual} ");
		}
		if (!visibilidadeTodosCargos) {
			sb.append(" AND processoTrf.cargo in (#{");
			sb.append(NAME).append(".cargoVisibilidadeList})");
		}
		sb.append(")");
		return sb.toString();
	}

	protected String getDefaultOrder() {
		return ORDER_HQL;
	}
	
	public List<Cargo> getCargoVisibilidadeList() {
		return cargoVisibilidadeList;
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
		map.put(DEFAULT_ORDER, getDefaultOrder());
		return null;
	}
	
	public static PesquisaProcessoDocumentoEstruturadoList instance() {
		return ComponentUtil.getComponent(NAME);
	}
}
