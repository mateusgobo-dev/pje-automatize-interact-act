package br.com.infox.pje.manager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jbpm.graph.exe.ProcessInstance;

import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.dao.ModeloDocumentoLocalDAO;
import br.jus.cnj.pje.nucleo.service.LocalizacaoService;
import br.jus.cnj.pje.servicos.AtividadesLoteService;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.ModeloDocumentoLocal;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;

@Name("modeloDocumentoLocalManager")
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class ModeloDocumentoLocalManager implements Serializable {

	private static final long serialVersionUID = 1L;

	@In (create = true)
	private ModeloDocumentoLocalDAO modeloDocumentoLocalDAO;
	
	@In(create = false, required = false)
	private ProcessInstance processInstance;
	
	@In(create = true)
	private AtividadesLoteService atividadesLoteService;
	
	// [PJEII-1571]
	@In (create = true)
	private LocalizacaoService localizacaoService;
	// [PJEII-1571] Fim

	
	/**
	 * Método que busca modelos cadastrados na tarefa, caso haja uma tarefa no contexto,
	 * ou retorna todos os modelos, sempre filtrando pela localização atual e pelo tipo de
	 * documento passado como parâmetro.
	 * @author Ronny Paterson
	 * @since 1.4.7.2
	 * @param tipoProcessoDocumento tipo do modelo desejado
	 * @return List<ModeloDocumento> contendo os modelos da tarefa ou todos os modelos que pertençam à localização atual ou ao administrador.
	 * @throws Exception
	 */
	public List<ModeloDocumento> getModeloDocumentoPorTipoDocumento(TipoProcessoDocumento tipoProcessoDocumento) {
		List<ModeloDocumento> listaModelos = null;
		if (processInstance != null){
			String actionExpressionStart = "#{modeloDocumento.set(";
			List<Integer> idsList = atividadesLoteService.getIdsListFluxo(processInstance, actionExpressionStart);
			listaModelos = this.modeloDocumentoLocalDAO.findByTipoDocumentoIdsModeloDocumento(tipoProcessoDocumento, idsList.toArray(new Integer[]{}));
		}
		else{
			listaModelos = this.modeloDocumentoLocalDAO.findAll();
		}
			
		Localizacao localizacaoOjOjc = Authenticator.getLocalizacaoUsuarioLogado();
		
		// Filtra pela localização do órgão atual e seus ascendentes e descendentes
		List<Localizacao> localizacoesUsuarioList = localizacaoService.getLocalizacoesAscendentesDescendentesOjsOjcs(localizacaoOjOjc);
		removerModelosSemLocalizacoesUsuario(listaModelos, localizacoesUsuarioList);
		
		return listaModelos;
	}
	
	private void removerModelosSemLocalizacoesUsuario (List<ModeloDocumento> listaModelos, List<Localizacao> localizacoesUsuarioList) {
		Localizacao localizacaoModelo = null;
		List<ModeloDocumento> listaModelosTemp = new ArrayList<ModeloDocumento>(0);
		for (ModeloDocumento modeloDocumento : listaModelos) {
			localizacaoModelo = getLocalizacaoModelo(modeloDocumento);
			if ((!localizacoesUsuarioList.contains(localizacaoModelo))) {
				listaModelosTemp.add(modeloDocumento);
			}
		}
		if(listaModelosTemp.size() > 0){
			listaModelos.removeAll(listaModelosTemp);
		}
	}
	
	private Localizacao getLocalizacaoModelo(ModeloDocumento md) {
		return modeloDocumentoLocalDAO.getLocalizacaoModelo(md);
	}
	
	/**
	 * Obter o modelo do documento de acordo com o tipo passado
	 * 
	 * @param tipoProcessoDocumento
	 *            tipo do modelo desejado
	 * @return
	 */
	public List<ModeloDocumento> getModeloDocumentoPorTipo(TipoProcessoDocumento tipoProcessoDocumento) {
		return modeloDocumentoLocalDAO.getModeloDocumentoPorTipo(tipoProcessoDocumento);
	}

	/**
	 * [PJEII-5382] Busca lista de modelos de um Tipo de Documento que pertençam
	 * a uma das localizações da lista.
	 * @param tipoDocumento
	 * @param listaLocalizacoes
	 * @return
	 */
	public List<ModeloDocumento> getModeloDocumentoPorTipoDocumentoPorLocalizacoes(
			TipoProcessoDocumento tipoDocumento,
			List<Localizacao> listaLocalizacoes) {
		return modeloDocumentoLocalDAO.getModeloDocumentoPorTipoDocumentoPorLocalizacoes(tipoDocumento, listaLocalizacoes);
	}
	
	/**
	 * [PJEII-5382] Obtém a lista de modelos que pertençam a uma das localizações informadas.
	 * @param listaLocalizacao
	 * @return
	 */
	public List<ModeloDocumento> getModeloDocumentoPorListaLocalizacao(
			List<Localizacao> listaLocalizacao) {
		return modeloDocumentoLocalDAO.getModeloDocumentoPorListaLocalizacao(listaLocalizacao);
	}
	
	/**
	 * Persiste o modelo de documento para a localização específicados
	 * @param modeloDocLoc Objeto ModeloDocumentoLocal populada
	 * @throws Exception
	 */
	public void gravaModelo(ModeloDocumentoLocal modeloDocLoc) throws Exception{
		try {
			modeloDocumentoLocalDAO.persist(modeloDocLoc);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}
	
	/**
	 * Metodo que retorna o ModeloDocumentoLocal pelo idModeloDocumento 
	 * @param idModeloDocumento
	 * @return ModeloDocumentoLocal
	 */
	public ModeloDocumentoLocal findById(int idModeloDocumento) {
		return modeloDocumentoLocalDAO.findById(idModeloDocumento);
	}
}