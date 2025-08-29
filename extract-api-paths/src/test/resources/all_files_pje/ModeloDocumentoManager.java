/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.core.Expressions;
import org.jbpm.graph.exe.ProcessInstance;
import org.jsoup.parser.Parser;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.ibpm.home.AbstractProcessoDocumentoHome;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.ModeloDocumentoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.service.LocalizacaoService;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.servicos.AtividadesLoteService;
import br.jus.cnj.pje.view.fluxo.ProcessoJudicialAction;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoModeloDocumento;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.enums.TipoEditorEnum;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * @author cristof
 * 
 */
@Name("modeloDocumentoManager")
public class ModeloDocumentoManager extends BaseManager<ModeloDocumento>{

	@In(create = true)
	private ModeloDocumentoDAO modeloDocumentoDAO;
	
	@In(create = true)
	private AtividadesLoteService atividadesLoteService;
	
	@In(create = true)
	private LocalizacaoService localizacaoService;
	
	@In(create = false, required = false)
	private ProcessInstance processInstance;

	@In(create = true)
	private Expressions expressions;
	
	@In
	private UsuarioService usuarioService;
	
	public static ModeloDocumentoManager instance() {
		return ComponentUtil.getComponent(ModeloDocumentoManager.class);
	}

	@Override
	protected ModeloDocumentoDAO getDAO(){
		return this.modeloDocumentoDAO;
	}

//	@Begin(join = true)
	public List<ModeloDocumento> findByIds(Integer... ids){
		return this.modeloDocumentoDAO.findByIds(ids);
	}

	/**
	 * Método que busca modelos cadastrados na tarefa, caso haja uma tarefa no contexto,
	 * ou retorna todos os modelos.
	 * @author Tiago Zanon
	 * @since 1.4.7
	 * @return List<ModeloDocumento> contendo os modelos da tarefa ou todos os modelos.
	 * @throws Exception
	 * @deprecated obsoleto, use {@link getModelosDisponiveisPorLocalizacao()}
	 */
	@Deprecated
	public List<ModeloDocumento> getModelosDisponiveis() throws Exception {
		return getModelosDisponiveisPorLocalizacao();
	}
	
	/**
	 * Método que busca modelos cadastrados na tarefa, caso haja uma tarefa no contexto,
	 * ou retorna todos os modelos, sempre filtrando pela localização atual.
	 * @author Tiago Zanon
	 * @since 1.4.7
	 * @return List<ModeloDocumento> contendo os modelos da tarefa ou todos os modelos que pertençam à localização atual ou ao administrador.
	 * @throws Exception
	 */
	public List<ModeloDocumento> getModelosDisponiveisPorLocalizacao() throws Exception{
		List<ModeloDocumento> listaModelos = null;
		if (processInstance != null){
			String actionExpressionStart = "#{modeloDocumento.set(";
			List<Integer> idsList = atividadesLoteService.getIdsListFluxo(processInstance, actionExpressionStart);
			listaModelos = this.modeloDocumentoDAO.findByIds(idsList.toArray(new Integer[]{}));
		}
		else{
			listaModelos = this.modeloDocumentoDAO.findAll();
		}
			
		Localizacao localizacaoOjOjc = Authenticator.getLocalizacaoUsuarioLogado();
		
		// Filtra pela localização do órgão atual e seus ascendentes e descendentes
		List<Localizacao> localizacoesUsuarioList = localizacaoService.getLocalizacoesAscendentesDescendentesOjsOjcs(localizacaoOjOjc);
		removerModelosSemLocalizacoesUsuario(listaModelos, localizacoesUsuarioList);
		
		return listaModelos;
	}
	
	private void removerModelosSemLocalizacoesUsuario (List<ModeloDocumento> listaModelos, List<Localizacao> localizacoesUsuarioList) {
		Localizacao localizacaoModelo = null;		
		for (ModeloDocumento modeloDocumento : listaModelos) {
			localizacaoModelo = getLocalizacaoModelo(modeloDocumento);
			if ((!localizacoesUsuarioList.contains(localizacaoModelo))) {
				listaModelos.remove(modeloDocumento);
			}
		}
	}

	private Localizacao getLocalizacaoModelo(ModeloDocumento md) {
		return modeloDocumentoDAO.getLocalizacaoModelo(md);
	}

//	@Begin(join = true)
	public String obtemConteudo(ModeloDocumento modelo){
		if (modelo == null){
			return null;
		}
		return substituirElModelo(modelo.getModeloDocumento());
	}
	
	private String substituirElModelo(String html) {
		StringBuilder conteudo = new StringBuilder();
		String[] linhas = html.split("\n");
		for (int i = 0; i < linhas.length; i++){
			try{
				String linha = AbstractProcessoDocumentoHome.interpretarLinhaElComTratamentoParaCaracteresEscapeHtml(linhas[i]);
				conteudo.append(linha);
			} catch (RuntimeException e){
				conteudo.append("Erro de intepretao na linha: '" + linhas[i]);
				conteudo.append("': " + e.getMessage());
				e.printStackTrace();
			}
		}
		return conteudo.toString();
	}
	
	private String substituirElModeloXml(String xml) {
		String xmlPreenchido = xml;
		Matcher m = Pattern.compile("#\\{(.*?)\\}").matcher(xml);
		while (m.find()) {
			String tag = m.group();
			String valor= (String) expressions.createValueExpression(StringUtil.removeHtmlTags(tag)).getValue();
			valor = org.apache.commons.lang3.StringEscapeUtils.escapeXml(valor);
			xmlPreenchido = xmlPreenchido.replace(tag,valor);
		}
		
		return xmlPreenchido;
	}
	
	public String obtemConteudoODT(ModeloDocumento modelo) throws IOException{
		if (modelo == null){
			return null;
		}
		String xmlContent = getConteudoXmlODT(modelo);
		String xmlPreenchido = substituirElModeloXml(xmlContent);
		return substituirConteudoXmlODT(modelo, xmlPreenchido);
	}
	
	private String substituirConteudoXmlODT(ModeloDocumento modelo, String xml) throws IOException {
		ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(modelo.getModeloDocumento())));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipOutputStream zipOut = new ZipOutputStream(baos);
        for (ZipEntry zipEntry=zis.getNextEntry() ; zipEntry!=null ; zipEntry=zis.getNextEntry()) {
        	zipOut.putNextEntry(zipEntry);
        	if ( "content.xml".equals(zipEntry.getName()) ) {
        		IOUtils.copy(new ByteArrayInputStream(xml.getBytes()), zipOut);
            } else {
            	IOUtils.copy(zis, zipOut);
            }
        }
        zis.closeEntry();
        zis.close();
		
        
        zipOut.close();
        baos.close();
        
        return Base64.getEncoder().encodeToString( baos.toByteArray() );
	}

	private String getConteudoXmlODT(ModeloDocumento modelo) throws IOException {
		ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(modelo.getModeloDocumento().getBytes())));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            if ( "content.xml".equals(zipEntry.getName()) ) {
            	return IOUtils.toString(zis, "UTF-8");
            }
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
        return null;
	}

	public String obtemConteudo(String modelo){
		if (modelo == null){
			return null;
		}
		StringBuilder conteudo = new StringBuilder();
		String[] linhas = modelo.split("\n");
		for (int i = 0; i < linhas.length; i++){
			try{
				String linha = AbstractProcessoDocumentoHome.interpretarLinhaElComTratamentoParaCaracteresEscapeHtml(linhas[i]);
				conteudo.append(linha);
			} catch (RuntimeException e){
				conteudo.append("Erro de intepretação na linha: '" + linhas[i]);
				conteudo.append("': " + e.getMessage());
				e.printStackTrace();
			}
		}
		return conteudo.toString();
	}
	
	public String traduzirModelo(TipoEditorEnum tipoEditor, String modelo) throws PJeBusinessException{
		String retorno = "";
		if (modelo != null){
			StringBuilder conteudo = new StringBuilder();
			if(tipoEditor.equals(TipoEditorEnum.C)) {
				String queFalta = modelo;
				while(queFalta.contains("#{")) {
					int posicaoInicial = queFalta.indexOf("#{");
					int posicaoFinal = queFalta.indexOf("}");
					String traduzir = "";
					if(posicaoFinal > posicaoInicial + 2) {
						traduzir = queFalta.substring(posicaoInicial, posicaoFinal + 1);
						traduzir = traduzir.replaceAll("&#39;", "'");
						String traduzida = "";
						try{
							traduzida = (String) expressions.createValueExpression(traduzir).getValue();
						} catch (Exception e){
							String textoErro = "Erro de interpretação na expressão: '" + traduzir + "': " + e.getMessage();
							throw new PJeBusinessException(textoErro);
						}
						conteudo.append(queFalta.substring(0, posicaoInicial));
						conteudo.append(traduzida);
						queFalta = queFalta.substring(posicaoFinal+1);
					}
				}
				conteudo.append(queFalta);
			} else {
				String[] linhas;
				String modeloCorrigido = modelo.replaceAll("&#39;", "'");
				linhas = modeloCorrigido.split("\n");
				for (int i = 0; i < linhas.length; i++){
					try{
						String linha = AbstractProcessoDocumentoHome.interpretarLinhaElComTratamentoParaCaracteresEscapeHtml(linhas[i]);
						conteudo.append(linha);
					} catch (Exception e){
						String textoErro = "Erro de interpretação na linha: '" + linhas[i] + "': " + e.getMessage();
						throw new PJeBusinessException(textoErro);
					}
				}
			}
			retorno = conteudo.toString();
		}
		return retorno;
	}
	
	/**
	 * Recupera a lista de modelos de um dado tipo de documento, considerando, se existente, as localizações 
	 * informadas.
	 * 
	 * @param tipo o tipo de documento cujos modelos se pretende recuperar
	 * @param locais as localizações às quais estariam vinculados esses modelos.
	 * @return a lista de modelos.
	 * @throws PJeBusinessException
	 */
	public List<ModeloDocumento> getModelos(TipoProcessoDocumento tipo, List<Localizacao> locais) throws PJeBusinessException{
		if (tipo == null) {
			throw new PJeBusinessException("É necessário a seleção de um tipo de documento");
		} else if (locais == null || locais.isEmpty()) {
			throw new PJeBusinessException("Para a seleção de modelos, o usuário necessita ter Localização cadastrada no sistema.");
		} else {
			return modeloDocumentoDAO.getModelos(tipo, locais);
		}
	}
	
	/**
	 * Recupera a lista de modelos de uma dada lista de tipos de documento, considerando, se existente, as localizações 
	 * informadas.
	 * 
	 * @param tipos os tipos de documento cujos modelos se pretende recuperar
	 * @param locais as localizações às quais estariam vinculados esses modelos.
	 * @return a lista de modelos.
	 * @throws PJeBusinessException
	 */
	public List<ModeloDocumento> getModelos(List<TipoProcessoDocumento> tipos, List<Localizacao> locais) throws PJeBusinessException{
		if (tipos == null) {
			throw new PJeBusinessException("Não há lista de tipos para que os modelos sejam filtrados");
		} else if (locais == null || locais.isEmpty()) {
			throw new PJeBusinessException("Para a seleção de modelos, o usuário necessita ter Localização cadastrada no sistema.");
		} else {
			return modeloDocumentoDAO.getModelos(tipos, locais);
		}
	}
	
	/**
	 * Recupera a lista de modelos de um dado tipo de documento, considerando, se existentes, as localizações 
	 * e considerando a pesquisa do usuario pelo titulo ou descrição do conteudo do modelo.
	 * @param tipo o tipo de documento cujos modelos se pretende recuperar
	 * @param tituloOuDescricao descrição do titulo ou conteudo do modelo de documento
	 * @return a lista de modelos.
	 * @throws PJeBusinessException
	 * @author eduardo.pereira@tse.jus.br
	 */
	public List<ModeloDocumento> getModelosPorTipoTituloOuDescricao(TipoProcessoDocumento tipo, 
			String tituloOuDescricao, Integer... idsModelos) throws PJeBusinessException {
		if (tipo == null) {
			throw new PJeBusinessException("É necessário a seleção de um tipo de documento");
		}
		return this.getModelos(tipo, getLocalizacoes(), tituloOuDescricao, idsModelos);
	}
	
	public List<ModeloDocumento> getModelos(TipoProcessoDocumento tipo, List<Localizacao> localizacoes,
			String tituloOuDescricao, Integer... idsModelos) throws PJeBusinessException {
		return modeloDocumentoDAO.getModelos(tipo, localizacoes, tituloOuDescricao, idsModelos);
	}
	
	/**
	 * Recupera as posiveis localizações do usuario logado
	 * @return List<Localizacao>
	 * @throws PJeBusinessException
	 * @author eduardo.pereira@tse.jus.br
	 * @see <a href="http://www.cnj.jus.br/jira/browse/PJEII-20663">PJEII-20663</a>
	 */
	public List<Localizacao> getLocalizacoes() throws PJeBusinessException{
		List<Localizacao> locais = null;
		
		UsuarioLocalizacaoMagistradoServidor ulms = usuarioService.getLocalizacaoAtual().getUsuarioLocalizacaoMagistradoServidor();
		Localizacao local =  null;
		if(ulms != null && ulms.getOrgaoJulgador() != null){
			local = ulms.getOrgaoJulgador().getLocalizacao();
		}else if(ulms != null && ulms.getOrgaoJulgadorColegiado() != null){
			local = ulms.getOrgaoJulgadorColegiado().getLocalizacao();
		}else{
			local =  usuarioService.getLocalizacaoAtual().getLocalizacaoFisica();
		}
		
		locais = localizacaoService.getLocalizacoesAscendentesDescendentes(local);
		
		if (locais == null || locais.isEmpty()) {
			throw new PJeBusinessException("Para a seleção de modelos, o usuário necessita ter Localização cadastrada no sistema.");
		} 
		
		return locais; 
	}
	
	/**
	 * Recupera os modelos com base na localização, filtrando pelo tipo de modelo, se informado.
	 * @param tipo Tipo de modelo, se informado. Se null será tratado
	 * @param localizacao Localização informada
	 * @return Lista de modelos
	 */
	public List<ModeloDocumento> getModelos(TipoModeloDocumento tipo, Localizacao local) throws Exception {
		return modeloDocumentoDAO.getModelos(tipo, local);
	}

	public List<ModeloDocumento> getModelos(TipoModeloDocumento tipo, Localizacao local, Papel papel) throws Exception {
		return modeloDocumentoDAO.getModelos(tipo, local, papel);
	}

	/**
	 * Recupera a lista de modelos considerando as localizações  informadas.
	 * 
	 * @param locais as localizações às quais estariam vinculados esses modelos.
	 * @return a lista de modelos.
	 */
	public List<ModeloDocumento> obterModelosPorLocalizacao(List<Localizacao> localizacoes) {
		return modeloDocumentoDAO.obterModelosPorLocalizacao(localizacoes);
	}
	
	/**
	 * Metodo que retorna lista de modelos por tipoProcessoDocumento
	 * @param tipoProcessoDocumento
	 * @return Lista de modelos
	 */
	public List<ModeloDocumento> recuperaModelosPorTipoProcessoDocumento(TipoProcessoDocumento tipoProcessoDocumento){
		return modeloDocumentoDAO.recuperaModelosPorTipoProcessoDocumento(tipoProcessoDocumento);
	}
	
	/**
	 * Metodo que verifica a existencia do modelo no banco de dados
	 * @param modelo
	 * @return boolean
	 */
	public boolean existeModeloProcessoDocumento(String modelo){
		return modeloDocumentoDAO.existeModelosProcessoDocumento(modelo);
	}
	
	/**
	 * Método responsável por obter o conjunto de Expression Languages que compõem o documento.
	 * 
	 * @param modeloDocumento Conteúdo do documento.
	 * @return Conjunto de ELs que compõem o documento.
	 */
    public Set<String> getElsModelo(String modeloDocumento) {
    	Set<String> els = new HashSet<String>(0);
    	if (StringUtils.isNotBlank(modeloDocumento)) {
        	Matcher m = Pattern.compile("#\\{[\\w\\d \\.\\(\\)]+\\}").matcher(modeloDocumento);
            while (m.find()) {
            	String el = m.group();
            	el = el.replace("#{", "").replace("}", "").trim();
            	String action = el;
            	if (action.contains(".")) {
            		action = action.substring(0, action.indexOf('.'));
            	}
            	els.add(action);
            }
    	}
    	return els;
	}
    
    public void carregarInstanciasParaTraducao(ProcessoTrf processoTrf) {
    	if(processoTrf != null) {
    		ProcessoHome.instance().setInstance(processoTrf.getProcesso());
    		ProcessoTrfHome.instance().setProcessoTrf(processoTrf);
    		ProcessoTrfHome.instance().setInstance(processoTrf);
    		ProcessoTrfHome.instance().getInstance().setProcesso(processoTrf.getProcesso());
    		ProcessoTrfHome.instance().setNumeroProcesso(processoTrf.getNumeroProcesso());
    		ProcessoJudicialAction.instance().setProcessoJudicial(processoTrf);
    	}
    }
}
