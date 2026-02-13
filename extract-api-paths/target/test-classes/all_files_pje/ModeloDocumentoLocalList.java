package br.com.infox.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.ModeloDocumentoLocal;
import br.jus.pje.nucleo.enums.TipoEditorEnum;

@Name(ModeloDocumentoLocalList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ModeloDocumentoLocalList extends EntityList<ModeloDocumentoLocal> {

	public static final String NAME = "modeloDocumentoLocalList";
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_ORDER = "o.tituloModeloDocumento";
	
	private static final String R1 = "o.tipoModeloDocumento =  #{tipoModeloDocumentoSuggest.instance} ";
	private static final String R2 = "o.tipoProcessoDocumento = #{tipoProcessoDocumentoLocalSuggest.instance} ";
	private static final String R3 = "lower(to_ascii(o.tituloModeloDocumento)) like concat('%',lower(to_ascii(#{modeloDocumentoLocalList.entity.tituloModeloDocumento})),'%')";
	private static final String R4 = "o.localizacao = #{localizacaoFisicaSuggest.instance} ";
	
	@Override
	protected void addSearchFields() {
		addSearchField("ativo", SearchCriteria.igual);
		addSearchField("tipoModeloDocumento", SearchCriteria.igual,R1);
		addSearchField("tipoProcessoDocumento", SearchCriteria.igual,R2);
		addSearchField("tituloModeloDocumento", SearchCriteria.contendo,R3);
		addSearchField("localizacao", SearchCriteria.igual,R4);
		addSearchField("tipoEditor", SearchCriteria.igual);
	}
	
	@Override
	public void newInstance() {
		super.newInstance();
		Contexts.removeFromAllContexts("tipoModeloDocumentoSuggest");
		Contexts.removeFromAllContexts("tipoProcessoDocumentoLocalSuggest");
		Contexts.removeFromAllContexts("localizacaoFisicaSuggest");
		entity.setAtivo(true);
		entity.setTipoEditor(TipoEditorEnum.T);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	@Override
	protected String getDefaultEjbql() {
		Integer idLocalizacaoFisicaAtual = Authenticator.getIdLocalizacaoFisicaAtual();
		String identificadorPapelAtual = Authenticator.getIdentificadorPapelAtual();
		StringBuilder sb = new StringBuilder("select o from ModeloDocumentoLocal o ")
				.append(" JOIN o.localizacao loc ")
				.append(" , Localizacao locFisica ")
				.append(" WHERE loc.faixaInferior >= locFisica.faixaInferior ")
				.append(" AND loc.faixaSuperior <= locFisica.faixaSuperior ")
				.append(" AND locFisica.idLocalizacao = "+idLocalizacaoFisicaAtual);

		if(!Authenticator.isPermissaoCadastroTodosPapeis(identificadorPapelAtual)) {
			sb.append(" AND o.idModeloDocumento IN (SELECT md.idModeloDocumento FROM ModeloDocumento md ")
			.append(" JOIN md.tipoModeloDocumento tmd ")
			.append(" JOIN tmd.papeis papeis WHERE tmd.ativo IS TRUE ")
			.append(" AND papeis.identificador = '" + identificadorPapelAtual + "'")
			.append(" ) ");
		}
		
		return sb.toString();
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}
}