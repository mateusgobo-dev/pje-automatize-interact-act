package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.util.Strings;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.manager.ModeloDocumentoLocalManager;
import br.jus.cnj.pje.editor.lool.LibreOfficeManager;
import br.jus.cnj.pje.editor.lool.LoolException;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.LocalizacaoManager;
import br.jus.cnj.pje.nucleo.manager.ModeloDocumentoCKManager;
import br.jus.cnj.pje.nucleo.manager.ModeloDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.TipoModeloDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.TipoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.nucleo.view.ICkEditorController;
import br.jus.cnj.pje.servicos.EditorEstiloService;
import br.jus.cnj.pje.view.AjaxDataUtil;
import br.jus.je.pje.entity.vo.BinarioVO;
import br.jus.pje.nucleo.entidades.GrupoModeloDocumento;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.ModeloDocumentoCK;
import br.jus.pje.nucleo.entidades.ModeloDocumentoLocal;
import br.jus.pje.nucleo.entidades.TipoModeloDocumento;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.Variavel;
import br.jus.pje.nucleo.enums.TipoEditorEnum;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.nucleo.util.StringUtil;

@Name("modeloDocumentoLocalHome")
@SuppressWarnings("unchecked")
public class ModeloDocumentoLocalHome extends AbstractModeloDocumentoLocalHome<ModeloDocumentoLocal> implements ICkEditorController{

	private static final long serialVersionUID = 1L;
	private static final LogProvider log = Logging.getLogProvider(ModeloDocumentoLocalHome.class);
	private static final int LIMITE_INICIAL_CARACTERES_TITULO = 0;
	private static final int LIMITE_FINAL_CARACTERES_TITULO = 20;
	private static final int QTD_MAX_CARACTERES_TITULO = 100;
	
	@In(create=true)
	private AjaxDataUtil ajaxDataUtil;
	
	@In(create = true)
	private EditorEstiloService editorEstiloService;
	
	@In
	private UsuarioService usuarioService;
	
	@In
	private ModeloDocumentoManager modeloDocumentoManager;
	
	@In
	private LocalizacaoManager localizacaoManager;
	
	@In
	private ModeloDocumentoLocalManager modeloDocumentoLocalManager;

	@In
	private ModeloDocumentoCKManager modeloDocumentoCKManager;

	@In(create=true)
	private DateUtil dateUtil;

	@In(create=true)
	private ParametroUtil parametroUtil;
	
	@In
	private TipoModeloDocumentoManager tipoModeloDocumentoManager;
	
	@In
	private TipoProcessoDocumentoManager tipoProcessoDocumentoManager;

	private GrupoModeloDocumento grupoModeloDocumento;
	private TipoModeloDocumento tipoModeloDocumento;
	private TipoProcessoDocumento tipoProcessoDocumento;
	private Localizacao localizacao;
	private Localizacao localizacaoFisicaRoot;
	private Localizacao localizacaoFisicaMaximaRoot;

	
	private List<ModeloDocumento> listaModelosFiltrados;
	private List<ModeloDocumentoLocal> listaModelosACopiar;
	private List<Localizacao> listaDestinosCopia;
	private String modeloDocumentoTexto;
	private LibreOfficeManager libreOfficeManager;
	
	@Override
	public void create() {
		super.create();
		this.localizacaoFisicaMaximaRoot = parametroUtil.getLocalizacaoTribunal();
		this.localizacaoFisicaRoot = getLocalizacaoFisicaAtual();
		this.localizacao = getLocalizacaoFisicaAtual();

		this.getInstance().setTipoEditor(ParametroUtil.instance().getEditor());
		this.getInstance().setLocalizacao(this.localizacao);
	}
	
	public void iniciaLibreOfficeManager() {
		if ( parametroUtil.isEditorLibreOfficeHabilitado() && this.libreOfficeManager == null) {
			this.libreOfficeManager = new LibreOfficeManager("modelo_"+System.currentTimeMillis(), "odt");
			try {
				this.libreOfficeManager.salvarNovoDocumento();
			} catch (LoolException e) {
				e.printStackTrace();
				FacesMessages.instance().add(Severity.ERROR, "Erro ao salvar novo documento no WOPI");
			}
		}
	}

	@Override
	public void setId(Object id) {
		FacesMessages.instance().clear();
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		if (changed) {
			tipoModeloDocumento = getInstance().getTipoModeloDocumento();
			grupoModeloDocumento = tipoModeloDocumento.getGrupoModeloDocumento();
			tipoProcessoDocumento = getInstance().getTipoProcessoDocumento();
			localizacao = getInstance().getLocalizacao();
			if(isCKEditor()) {
				try {
					ModeloDocumentoCK modeloCK = modeloDocumentoCKManager.findById(getInstance().getIdModeloDocumento());
					if( modeloCK == null ) {
						FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "modeloDocumentoCK.erro.baseInconsistente");
					}
				} catch (PJeBusinessException e) {
					log.error(e.getCause(), e);
					FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "modeloDocumentoCK.erro.baseInconsistente");
				}
			} else if (isLoolEditor()) {
				this.libreOfficeManager = new LibreOfficeManager("modelo_"+System.currentTimeMillis(), "odt");
				try {
					this.libreOfficeManager.carregarModeloDocumento(Base64.getDecoder().decode( getInstance().getModeloDocumento() ));
				} catch (LoolException e) {
					e.printStackTrace();
					FacesMessages.instance().add(Severity.ERROR, "Erro ao carregar conteudo do modelo");
				}
			}
		}
		if (id == null) {
			tipoModeloDocumento = null;
			grupoModeloDocumento = null;
			tipoModeloDocumento = null;
			localizacao = getLocalizacaoFisicaAtual();
		}
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		if (getInstance().getLocalizacao() == null && getLocalizacaoFisicaAtual() != null) {
			getInstance().setLocalizacao(getLocalizacaoFisicaAtual());
		}
		return super.beforePersistOrUpdate();
	}
	
	
	@Override
	public void salvar(String conteudo) {
		if(beforePersistOrUpdate() && isFormularioPreenchido()) {
			try {

				if(ultrapassouQtdeMaxDeCaracteresDoTitulo()) {
					FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "modeloDocumento.erro.qtdeMaxCaracteresTitulo");
					this.ajaxDataUtil.erro();
					return;
				}

				definirModeloDocumento(conteudo);
			} catch (Exception e) {
				log.error(e);
				FacesMessages.instance().add(Severity.ERROR, "Erro ao recuperar conteúdo do documento");
				return;
			}

				if (isManaged()) {
					atualizar();
				} else {
					if(modeloDocumentoManager.existeModeloProcessoDocumento(getInstance().getTituloModeloDocumento())){
						FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "modeloDocumento.erro.modeloExistente");
						this.ajaxDataUtil.erro();
						return;					
					}
					persistir();
				}
				FacesMessages.instance().clear();
				FacesMessages.instance().addFromResourceBundle(Severity.INFO, "pje.message.createRecord");
				this.ajaxDataUtil.sucesso();
			}
		}
	
		private void definirModeloDocumento(String conteudo) throws Exception {
			if (isCKEditor()) {
				getInstance().setModeloDocumento(conteudo);
			} else if (isLoolEditor()) {
				getInstance().setModeloDocumento(getConteudoLibreOfficeBase64());
			}
		}
	
	public void carregarHtml() {
		
		try {
			String html = this.libreOfficeManager.getHtmlContent();
			BinarioVO binario = new BinarioVO();
			binario.setHtml(html);
			binario.setMimeType("text/html");
			Contexts.getSessionContext().set("download-binario", binario);
		} catch (Exception e) {
			e.printStackTrace();
			FacesMessages.instance().add(Severity.ERROR, "Erro ao gerar HTML");
		}
	}

	private String getConteudoLibreOfficeBase64() throws Exception {
		return Base64.getEncoder().encodeToString( IOUtils.toByteArray(this.libreOfficeManager.getContent()) );
	}

	private boolean isLoolEditor() {
		return getInstance().getTipoEditor() != null && getInstance().getTipoEditor().equals(TipoEditorEnum.L);
	}

	/**
	 * Metodo responsavel por verificar se o formulario de criacao de modelo de
	 * documento esta preenchido
	 * 
	 * @return <code>true</code>, caso os campos estejam todos preenchidos
	 */
	public boolean isFormularioPreenchido() {
		return (getInstance().getTipoModeloDocumento() != null 
				&& getInstance().getTipoProcessoDocumento() != null
				&& StringUtil.isNotEmpty(getInstance().getTituloModeloDocumento())
				&& getInstance().getLocalizacao() != null
				&& getInstance().getAtivo() != null && (isEditorPadrao() || getInstance().getTipoEditor()!=null));
	}
	
	public boolean ultrapassouQtdeMaxDeCaracteresDoTitulo() {
		return getInstance().getTituloModeloDocumento().length() > QTD_MAX_CARACTERES_TITULO;
	}

	/**
	 * Metodo responsavel por verificar se o editor utilizado
	 * na criacao do modelo de documento e' do tipo CKEditor 
	 * 
	 * @return <code>Boolean</code>, se for do tipo CKEditor
	 */
	private boolean isCKEditor() {
		return getInstance().getTipoEditor() != null && getInstance().getTipoEditor().equals(TipoEditorEnum.C);
	}
	
	/**
	 * Metodo responsavel por verificar se o editor utilizado
	 * na criacao do modelo de documento e' do tipo TinyMCE 
	 * 
	 * @return <code>Boolean</code>, se for do tipo TinyMCE
	 */
	private boolean isTinyMCE() {
		return getInstance().getTipoEditor() == null || getInstance().getTipoEditor().equals(TipoEditorEnum.T);
	}
	
	/**
	 * Verifica se o tipo do editor estï¿½ parametrizado e foi selecionado
	 * @return
	 */
	public boolean isEditorPadrao() {
		return ParametroUtil.instance().getEditor().equals(TipoEditorEnum.T);
	}
	
	public boolean atualizar() {
		boolean retorno = true;
        if (isTinyMCE() || isLoolEditor()) {
            super.update();
        } else if (isCKEditor()) {
            try {
                ModeloDocumentoCK modeloCK = modeloDocumentoCKManager.findById(getInstance().getIdModeloDocumento());
                if( modeloCK != null ) {
                    modeloDocumentoCKManager.mergeAndFlush(modeloCK);
                } else {
                    retorno = false;
                    FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "modeloDocumentoCK.erro.baseInconsistente");
                }
            } catch (PJeBusinessException e) {
                retorno = false;
                log.error(e.getCause(), e);
                FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "modeloDocumentoCK.erro.recuperarDados");
            }
		}
		return retorno;
	}
	
	
	public boolean persistir() {
		boolean retorno = true;
        if (isTinyMCE() || isLoolEditor()) {
            super.persist();
        } else if (isCKEditor()) {
        	ModeloDocumentoCK modeloCK = modeloDocumentoCKManager.montaModeloDocumentoCK(getInstance());
            try {
            	modeloDocumentoCKManager.persistAndFlush(modeloCK);
            } catch (PJeBusinessException e) {
                retorno = false;
                log.error(e.getCause(), e);
                FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "ModeloDocumentoCK.erroGravarModelo");
            }
        }
		return retorno;
	}

	public Localizacao getLocalizacaoFisicaAtual() {
		return Authenticator.getLocalizacaoFisicaAtual();
	}
	
	public GrupoModeloDocumento getGrupoModeloDocumento() {
		return grupoModeloDocumento;
	}

	public void setGrupoModeloDocumento(GrupoModeloDocumento grupoModeloDocumento) {
		this.grupoModeloDocumento = grupoModeloDocumento;
	}

	public void setTipoModeloDocumento(TipoModeloDocumento tipoModeloDocumento) {
		this.tipoModeloDocumento = tipoModeloDocumento;
	}

	public TipoModeloDocumento getTipoModeloDocumento() {
		return tipoModeloDocumento;
	}

	public TipoProcessoDocumento getTipoProcessoDocumento() {
		return tipoProcessoDocumento;
	}

	public void setTipoProcessoDocumento(TipoProcessoDocumento tipoProcessoDocumento) {
		this.tipoProcessoDocumento = tipoProcessoDocumento;
	}

	public Localizacao getLocalizacao() {
		return localizacao;
	}

	public void setLocalizacao(Localizacao localizacao) {
		this.localizacao = localizacao;
	}

	public List<Variavel> getVariaveis() {
		@SuppressWarnings("rawtypes")
		List list = new ArrayList<Variavel>();
		if (getInstance().getTipoModeloDocumento() != null) {
			StringBuilder sb = new StringBuilder();
			sb.append("select o from Variavel o ");
			sb.append("join o.variavelTipoModeloList tipos ");
			sb.append("where tipos.tipoModeloDocumento = :tipo ");
			sb.append("and o.ativo = true ");
			sb.append("order by o.variavel");
			list = getEntityManager().createQuery(sb.toString())
					.setParameter("tipo", getInstance().getTipoModeloDocumento()).getResultList();
		}
		return list;
	}

	@Override
	public void newInstance() {
		super.newInstance();
		this.getInstance().setTipoEditor(ParametroUtil.instance().getEditor());
	}

	/**
	 * Atualiza a lista de modelos de documento, conforme a localizacao e o tipo de modeloDocumento.
	 * 
	 */
	public void atualizaListaModelos() {	
		try {
			this.setListaModelosFiltrados(modeloDocumentoManager.getModelos(tipoModeloDocumento, localizacao, Authenticator.getPapelAtual()));
		} catch (Exception e) {
			log.error(e.getCause(), e);
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "modeloDocumentoLocal.erro.buscarModelos");
		}
	}
	
	/**
	 * Metodo para gravar o Modelo do Documento conforme a localizacao
	 */
	public void gravarModeloCopiado() {
		
		if (ProjetoUtil.isVazio(listaDestinosCopia)) {
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "modeloDocumentoLocal.erro.selecioneDestinosCopiaModelo");
		} else if (ProjetoUtil.isVazio(listaModelosACopiar)) {
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "modeloDocumentoLocal.erro.selecioneModelosCopia");
		} else {
			for (Localizacao destino : listaDestinosCopia) {
				for(ModeloDocumentoLocal modeloCopiado : listaModelosACopiar) {
					String tituloModeloDocumento = obterTituloModeloDocumento(modeloCopiado);
					ModeloDocumentoLocal modeloDocLoc = prepararModeloDocumento(destino, modeloCopiado, tituloModeloDocumento);

					try {
						modeloDocumentoLocalManager.gravaModelo(modeloDocLoc);
						
						if( modeloDocLoc.getTipoEditor() == TipoEditorEnum.C ) {
							ModeloDocumentoCK modeloCK = new ModeloDocumentoCK(modeloDocLoc.getIdModeloDocumento());
							modeloDocumentoCKManager.persist(modeloCK);
						}
					} catch (Exception e) {
						FacesMessages.instance()
						.addFromResourceBundle(Severity.ERROR, "modeloDocumentoLocal.erro.gravarModelo", 
								modeloCopiado.getTituloModeloDocumento());
						return;
					}
				}
			}
			
			limpar();
			FacesMessages.instance().addFromResourceBundle(Severity.INFO, "modeloDocumentoLocal.sucesso.copiaEfetuada");
		}
		
	}

	/**
	 * Limpa os dados dos objetos 
	 */
	private void limpar() {
		listaDestinosCopia.clear();
		listaModelosACopiar.clear();
		this.setModeloDocumentoTexto(StringUtils.EMPTY);
	}

	/**
	 * Metodo responsavel por preparar uma instancia de ModeloDocumentoLocal para persistencia. Para isso, o modelo tera
	 * as informacoes de ativo, sua localizacao, o modelo do documento, o titulo, o tipo e o tipoProcessoDocumento.
	 * 
	 * @param destino localizacao
	 * @param modeloCopiado
	 * @param tituloModeloDocumento string com o titulo do modelo do documento
	 * @return retorna um objeto preenchido com os seus atritutos, aptos para persistencia em banco
	 */
	private ModeloDocumentoLocal prepararModeloDocumento(Localizacao destino, ModeloDocumentoLocal modeloCopiado, 
			String tituloModeloDocumento) {
		ModeloDocumentoLocal modeloDocLoc = new ModeloDocumentoLocal();
		modeloDocLoc.setAtivo(true);
		modeloDocLoc.setLocalizacao(destino);
		modeloDocLoc.setModeloDocumento(modeloCopiado.getModeloDocumento());
		modeloDocLoc.setTituloModeloDocumento(tituloModeloDocumento);
		modeloDocLoc.setTipoModeloDocumento(modeloCopiado.getTipoModeloDocumento());
		modeloDocLoc.setTipoProcessoDocumento(modeloCopiado.getTipoProcessoDocumento());
		modeloDocLoc.setTipoEditor(modeloCopiado.getTipoEditor());
		return modeloDocLoc;
	}

	/**
	 * Metodo responsavel por obter o titulo do modeloDocumento. O título sera composto tambem pela data/hora no sufixo,
	 * no formato dd/MM/aaaa_hhmmssSSS (inclusive com milissegundo).
	 * 
	 * @param modeloCopiado modeloDocumentoLocal da lista a copiar
	 * @return retorna uma string com o titulo do modelo documento formatado
	 */
	private String obterTituloModeloDocumento(ModeloDocumentoLocal modeloCopiado) {
		StringBuilder tituloModeloDocumento = new StringBuilder();
		String dataFormatada = dateUtil.obterDataHoraComMilissegundo();
		
		if (modeloCopiado.getTituloModeloDocumento().length() > LIMITE_FINAL_CARACTERES_TITULO) {
			tituloModeloDocumento.append(modeloCopiado.getTituloModeloDocumento()
					.substring(LIMITE_INICIAL_CARACTERES_TITULO, LIMITE_FINAL_CARACTERES_TITULO));
		} else {
			tituloModeloDocumento.append(modeloCopiado.getTituloModeloDocumento());
		}
		
		tituloModeloDocumento.append(" -Novo em:");
		tituloModeloDocumento.append(dataFormatada);
		
		return tituloModeloDocumento.toString();
	}
	
	public void preenchePreview() {
		this.setModeloDocumentoTexto(listaModelosACopiar == null || listaModelosACopiar.size() == 0 ? 
				Strings.EMPTY : listaModelosACopiar.get(listaModelosACopiar.size()-1).getModeloDocumento());
	}

	public List<ModeloDocumentoLocal> getListaModelosACopiar() {
		return listaModelosACopiar;
	}

	public void setListaModelosACopiar(List<ModeloDocumentoLocal> listaModelosACopiar) {
		this.listaModelosACopiar = listaModelosACopiar;
	}

	public List<ModeloDocumento> getListaModelosFiltrados() {
		return listaModelosFiltrados;
	}

	public void setListaModelosFiltrados(List<ModeloDocumento> listaModelosFiltrados) {
		this.listaModelosFiltrados = listaModelosFiltrados;
	}

	public List<Localizacao> getListaDestinosCopia() {
		return listaDestinosCopia;
	}

	public void setListaDestinosCopia(List<Localizacao> listaDestinosCopia) {
		this.listaDestinosCopia = listaDestinosCopia;
	}

	public String getModeloDocumentoTexto() {
		return modeloDocumentoTexto;
	}

	public void setModeloDocumentoTexto(String modeloDocumentoTexto) {
		this.modeloDocumentoTexto = modeloDocumentoTexto;
	}

	@Override
	public String getEstilosFormatacao() {
		return editorEstiloService.recuperarEstilosJSON();
	}
	
	public boolean isDocumentoAssinado() {
		return false;
	}
	
	/**
	 * Metodo responsavel por listar os tipos de modelos de documentos
	 * 
	 * @return List<TipoModeloDocumento> 
	 */
	public List<TipoModeloDocumento> listarTipoModeloDocumento() {
		return tipoModeloDocumentoManager.obterTipoModeloDocumentoPorPapelAtual();
	}
	
	/**
	 * Metodo responsavel por listar os tipos de peticoes ou documentos
	 * 
	 * @return List<TipoProcessoDocumento> 
	 */
	public List<TipoProcessoDocumento> listarTipoProcessoDocumento() {
		return tipoProcessoDocumentoManager.obterTipoProcessoDocumentoPorAplicacaoClasseAtual();
	}
		
	public Localizacao getLocalizacaoFisicaRoot() {
		return this.localizacaoFisicaRoot;
	}
	
	public void setLocalizacaoFisicaRoot(Localizacao localizacaoFisicaRoot) {
		this.localizacaoFisicaRoot = localizacaoFisicaRoot;
	}
	
	public Localizacao getLocalizacaoFisicaMaximaRoot() {
		return localizacaoFisicaMaximaRoot;
	}

	public void setLocalizacaoFisicaMaximaRoot(Localizacao localizacaoFisicaMaximaRoot) {
		this.localizacaoFisicaMaximaRoot = localizacaoFisicaMaximaRoot;
	}

	public List<Localizacao> getLocalizacoesFisicasFilhas(){
		return localizacaoManager.getArvoreDescendente(this.getLocalizacaoFisicaRoot().getIdLocalizacao(), true);
	}

	public LibreOfficeManager getLibreOfficeManager() {
		return libreOfficeManager;
	}
	
}