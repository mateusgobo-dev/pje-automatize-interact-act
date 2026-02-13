package br.jus.csjt.pje.view.action;

import java.util.Collections;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.jbpm.ActionTemplate;
import br.com.infox.ibpm.jbpm.ProcessBuilder;
import br.com.infox.ibpm.jbpm.actions.ModeloDocumentoAction;
import br.com.itx.component.SelectItemsQuery;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.list.ResultadoSentencaParteList;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;

/**
 * @author davidhsv
 *
 */
/**
 * @author davidhsv
 *
 */
/**
 * @author davidhsv
 *
 */
@Name("tipoDocumento")
@Scope(ScopeType.SESSION)
@BypassInterceptors
@Startup
public class TipoProcessoDocumentoAction extends ActionTemplate {

	public static final String SUFIXO_VARIAVEL_TIPO_DOCUMENTO = "TipoProcessoDocumento";
	private static final long serialVersionUID = 1L;

	private List<ModeloDocumento> modelosDocumentosDisponiveis;
	
	@Override
	public String getExpression() {
		return "tipoProcessoDocumento.set";
	}

	@Override
	public String getFileName() {
		return "setTipoProcessoDocumento.xhtml";
	}

	@Override
	public String getLabel() {
		return "Atribuir tipo de documento a uma variável";
	}

	@Override
	public boolean isPublic() {
		return false;
	}

	@Override
	public void extractParameters(String expression) {
		if (expression == null || "".equals(expression)) {
			return;
		}
		parameters = getExpressionParameters(expression);
		ProcessBuilder.instance().getCurrentTask().setCurrentVariable((String) parameters[0]);
	}

	
	/**
	 * Método que deve ser utilizado no fluxo (ao criar a tarefa) para definir os
	 * {@link TipoProcessoDocumento} que serão mostrados no combobox do editor.
	 * <br/><br/>
	 * Exemplo de uso: #{tipoDocumento.set('NOME_DO_EDITOR', 12, 13, 14)}
	 * 
	 * @param variavel
	 *            Nome do campo de formulário do fluxo. Ex: Minuta
	 * @param idTipoDocumento
	 *            Ids de {@link TipoProcessoDocumento} que devem aparecer para o
	 *            nó de tarefa.
	 */
	public void set(String variavel, int... idTipoDocumento) {
		variavel += SUFIXO_VARIAVEL_TIPO_DOCUMENTO;
		StringBuilder s = new StringBuilder();
		for (int i : idTipoDocumento) {
			if (s.length() != 0) {
				s.append(",");
			}
			s.append(i);
		}
		TaskInstanceUtil.instance().setVariable(variavel, s.toString());
	}

//	private EntityManager getEntityManager() {
//		EntityManager em = EntityUtil.getEntityManager();
//		return em;
//	}
//
//	/**
//	 * Se estiverem definidos os tipos de documento no fluxo, os retorna.
//	 * <br/><br/>
//	 * Senão, retorna todos os tipos de documentos ativos.
//	 * 
//	 * @param variavel
//	 *            Nome do campo de formulário do fluxo. Ex: Minuta
//	 * @return Lista de tipos de documentos, de acordo com o comportamento
//	 *         descrito acima.
//	 * @see TipoProcessoDocumentoAction#set(String, int...) Método que define os tipos de documentos no fluxo
//	 */
//	@SuppressWarnings("unchecked")
//	public List<TipoProcessoDocumento> getTipoDocumentoItems(String variavel) {
//		String[] tokens = variavel.split("-");
//		String listaTipos = (String) TaskInstanceUtil.instance().getVariable(tokens[0] + SUFIXO_VARIAVEL_TIPO_DOCUMENTO);
//		List<TipoProcessoDocumento> list = null;
//		if (listaTipos != null) {
//			list = getEntityManager().createQuery(
//					"select o from TipoProcessoDocumento o where o.ativo = true "
//							+ "and o.idTipoProcessoDocumento in (" + listaTipos + ") order by tipoProcessoDocumento")
//					.getResultList();
//		} else {
//			list = getEntityManager().createQuery(
//					"select o from TipoProcessoDocumento o where o.ativo = true " + "order by tipoProcessoDocumento")
//					.getResultList();
//
//		}
//		return list;
//	}
//
//  **************************************************	
//  ***	
//	*** Por questões arquiteturais, o método acima foi
//	*** migrado para TipoProcessoDocumentoService.
//  *** 	
//  *************************************************	
	public List<TipoProcessoDocumento> getTipoDocumentoItems(String variavel) {
		DocumentoJudicialService djs = (DocumentoJudicialService) Component.getInstance(DocumentoJudicialService.class);
		return djs.getTiposDocumentoMinuta();
	}
	
	/**
	 * Se estiverem definidos os tipos de documento no fluxo, os retorna. <br/>
	 * <br/>
	 * Senão, retorna resultados do componente
	 * 'tipoProcessoDocumentoTransitoJulgadoItems', ou seja, o comportamento
	 * antigo (fallback) para o 'textEditComboTJ.xhtml'.
	 * 
	 * @param variavel
	 *            Nome do campo de formulário do fluxo. Ex: Minuta
	 * @return Lista de tipos de documentos, de acordo com o comportamento
	 *         descrito acima.
	 * @see TipoProcessoDocumentoAction#set(String, int...) Método que define os tipos de documentos no fluxo
	 */
	@SuppressWarnings("unchecked")
	public List<TipoProcessoDocumento> getTipoDocumentoItemsTransitoJulgado(String variavel) {
		if (isVariavelFluxoTipoDocumentoDefinida(variavel)) {
			return getTipoDocumentoItems(variavel);
		} else {
			return ((SelectItemsQuery) ComponentUtil.getComponent("tipoProcessoDocumentoTransitoJulgadoItems")).getResultList();
		}
	}
	
	/**
	 * Se estiverem definidos os tipos de documento no fluxo, os retorna. <br/>
	 * <br/>
	 * Senão, retorna resultados do componente
	 * 'tipoProcessoDocumentoInternoItems', ou seja, o comportamento
	 * antigo (fallback) para o 'textEditSignature'.
	 * 
	 * @param variavel
	 *            Nome do campo de formulário do fluxo. Ex: Minuta
	 * @return Lista de tipos de documentos, de acordo com o comportamento
	 *         descrito acima.
	 * @see TipoProcessoDocumentoAction#set(String, int...) Método que define os tipos de documentos no fluxo
	 */
	@SuppressWarnings("unchecked")
	public List<TipoProcessoDocumento> getTipoDocumentoItemsProcessoInterno(String variavel) {
		if (isVariavelFluxoTipoDocumentoDefinida(variavel)) {
			return getTipoDocumentoItems(variavel);
		} else {
			return ((SelectItemsQuery) ComponentUtil.getComponent("tipoProcessoDocumentoInternoItems")).getResultList();
		}
	}
	
	
	/**
	 * Se estiverem definidos os tipos de documento no fluxo, retorna os modelos
	 * definidos para o tipoProcessoDocumentoSelecionado. <br/>
	 * <br/>
	 * Senão, retorna os modelos definidos no fluxo, ou seja, o comportamento
	 * antigo.
	 * 
	 * @param variavelFormId
	 *            Nome do campo de formulário do fluxo. Ex: Minuta
	 * @param tipoProcessoDocumentoSelecionado
	 *            {@link TipoProcessoDocumento} selecionado
	 * @return Lista de modelos de documentos, de acordo com o comportamento
	 *         descrito acima.
	 * @throws Exception
	 *             Exceção lançada pelo
	 *             {@link DocumentoJudicialService#getModelosDisponiveis()}
	 * @see TipoProcessoDocumentoAction#set(String, int...) Método que define os tipos de documentos no fluxo
	 * @see ModeloDocumentoAction#set(String, int...) Método que define os modelos de documentos no fluxo
	 */
	@SuppressWarnings("unchecked")
	public List<ModeloDocumento> getModelosDisponiveis(String variavelFormId, TipoProcessoDocumento tipoProcessoDocumentoSelecionado) throws Exception {
		if (tipoProcessoDocumentoSelecionado == null) {
			modelosDocumentosDisponiveis = Collections.EMPTY_LIST;
			return modelosDocumentosDisponiveis;
		}		
		Localizacao localizacaoOjOjc = Authenticator.getLocalizacaoUsuarioLogado();		
		if (variavelFormId != null && isVariavelFluxoTipoDocumentoDefinida(variavelFormId)) {
			// [PJEII-1571] Filtrar a busca de modelos também por Localização
			modelosDocumentosDisponiveis = DocumentoJudicialService.instance().getModeloDocumentoListPorTipoDocumentoPorLocalizacao(tipoProcessoDocumentoSelecionado, localizacaoOjOjc);
		} else {
		    // PJEII-15480 - filtrar os modelos pelo tipo de documento e localizações
			modelosDocumentosDisponiveis = DocumentoJudicialService.instance().getModelosLocais(tipoProcessoDocumentoSelecionado);
			// PJEII-1571 Fim
		}
		
		//Carregar a lista de modelos de documentos de acordo com o tipo de documento selecionado
		((ResultadoSentencaParteList) ComponentUtil.getComponent(ResultadoSentencaParteList.NAME)).setMostraSentencas();
		
		return modelosDocumentosDisponiveis;
	}
	
	/**
	 * Se estiverem definidos os tipos de documento no fluxo, retorna TRUE. <br/>
	 * <br/>
	 * Senão, retorna FALSE.
	 * 
	 * @param variavel
	 *            Nome do campo de formulário do fluxo. Ex: Minuta
	 * @return Se os tipos de documentos foram definidos no fluxo.
	 *         
	 * @see TipoProcessoDocumentoAction#set(String, int...) Método que define os tipos de documentos no fluxo
	 */
	public boolean isVariavelFluxoTipoDocumentoDefinida(String variavel) {
		String[] tokens = variavel.split("-");
		String listaTipos = (String) TaskInstanceUtil.instance().getVariable(tokens[0] + SUFIXO_VARIAVEL_TIPO_DOCUMENTO);
		return listaTipos != null;
	}
	
	public static TipoProcessoDocumentoAction instance() {
		return (TipoProcessoDocumentoAction) Component.getInstance(TipoProcessoDocumentoAction.class);
	}
	
	public void setModelosDocumentosDisponiveis(
			List<ModeloDocumento> modelosDocumentosDisponiveis) {
		this.modelosDocumentosDisponiveis = modelosDocumentosDisponiveis;
	}

	public List<ModeloDocumento> getModelosDocumentosDisponiveis() throws Exception {
		return this.modelosDocumentosDisponiveis;
	}
}
