package br.com.infox.cliente.home;

import java.util.Date;
import java.util.List;

import javax.faces.event.ValueChangeEvent;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.com.infox.cliente.bean.PreCadastroPessoaBean;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.trf.webservice.ConsultaClienteWebService;
import br.com.itx.component.AbstractHome;
import br.com.itx.component.MeasureTime;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.PessoaDocumentoIdentificacaoManager;
import br.jus.cnj.pje.nucleo.manager.TipoDocumentoIdentificacaoManager;
import br.jus.cnj.pje.nucleo.manager.cache.ProcessoParteCache;
import br.jus.cnj.pje.nucleo.service.ConsultaClienteOABMock;
import br.jus.cnj.pje.servicos.VisibilidadePessoaDocumentoIdentificacaoService;
import br.jus.csjt.pje.commons.util.ParametroJtUtil;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.TipoDocumentoIdentificacao;
import br.jus.pje.nucleo.enums.PessoaAdvogadoTipoInscricaoEnum;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;
import br.jus.pje.ws.externo.srfb.entidades.DadosReceitaPessoaFisica;
import br.jus.pje.ws.externo.srfb.entidades.DadosReceitaPessoaJuridica;

@Name("pessoaDocumentoIdentificacaoHome")
@BypassInterceptors
@Scope(ScopeType.CONVERSATION)
public class PessoaDocumentoIdentificacaoHome extends AbstractHome<PessoaDocumentoIdentificacao> {

	private static final long serialVersionUID = -5833376379097035827L;
	private boolean novo = true;
	private boolean editar = false;
	public static final String TIPO_JUSTICA = ComponentUtil.getComponent("tipoJustica");
	private PessoaDocumentoIdentificacao documento;
	private VisibilidadePessoaDocumentoIdentificacaoService visibilidadePessoaDocumentoIdentificacaoService = (VisibilidadePessoaDocumentoIdentificacaoService) Component.getInstance(
	VisibilidadePessoaDocumentoIdentificacaoService.NAME, true);
	private PreCadastroPessoaBean preCadastroPessoaBean = (PreCadastroPessoaBean) Component.getInstance("preCadastroPessoaBean");
	private TipoDocumentoIdentificacaoManager tipoDocumentoIdentificacaoManager = (TipoDocumentoIdentificacaoManager) Component.getInstance("tipoDocumentoIdentificacaoManager");;  
	
	private static final LogProvider log = Logging.getLogProvider(PessoaDocumentoIdentificacaoHome.class);
	
	public PreCadastroPessoaBean getPreCadastroPessoaBean() {
	return preCadastroPessoaBean;
	}

	private ProcessoParteCache processoParteCache;

	private ProcessoParteCache getProcessoParteCache() {
		if (processoParteCache == null) {
			processoParteCache = (ProcessoParteCache) Component.getInstance(ProcessoParteCache.COMPONENT_NAME);
		}

		return processoParteCache;
	}

	@Override
	public void setId(Object id) {
		/* [PJEII-6431] Erro no cadastro de Documentos de Identificação da parte
		 * Quando novo "documento identificacao" era instanciado continuava com id do "documento identificacao" anterior o que gerava sobrescrita de dados.  
		 */
		super.setId(id);
		if(id != null) {	
			/* [PJEII-6431] Erro no cadastro de Documentos de Identificação da parte  
		 	* Chama método para controle de edição/inclusão colocando em edição. Usado nos botões em gravarDocumentoPessoa.xhtml.  
		 	*/  
		 	this.irEditar(); 
		 	PessoaDocumentoIdentificacao documentoIdentificacao = getInstance();
			
		 	if(documentoIdentificacao.getTipoDocumento() != null && documentoIdentificacao.getTipoDocumento().getCodTipo() != null &&
					documentoIdentificacao.getTipoDocumento().getCodTipo().equalsIgnoreCase("OAB")){
				if(documentoIdentificacao.getNumeroDocumento() != null){

					/*
					 * [PJEII-3295] PJE-JT: Sérgio Ricardo : PJE-1.4.5 
					 * Adição de tratamento do parse de numeroDocumento. O trecho a seguir apresentava erro em exemplos onde este atributo não 
					 * possuía a concatenação de estado/número/tipo inscrição, mas apenas seu número. 
					 */		
					
					String tipoInscricao = documentoIdentificacao.getNumeroDocumento().charAt(documentoIdentificacao.getNumeroDocumento().length()-1)+"";
					
					if (tipoInscricao != null && PessoaAdvogadoTipoInscricaoEnum.contains(tipoInscricao)) {						
						documentoIdentificacao.setLetraOAB(PessoaAdvogadoTipoInscricaoEnum.valueOf(tipoInscricao));
						
						// Retira do número do documento a informação concatenada de Estado e tipoInscricao						
						String numeroDocumento = documentoIdentificacao.getNumeroDocumento().replace(documentoIdentificacao.getEstado().getCodEstado().toString(), ""); 
						numeroDocumento = numeroDocumento.replace(tipoInscricao, ""); 
						
						documentoIdentificacao.setNumeroDocumento(numeroDocumento);
					}
					
					/*
					 * [PJEII-3295] PJE-JT: FIM 
					 */		
					
				}
			}		
		}	
	}
	
	public void setPessoaDocRumentoIdentificacaoIdDocumentoIdentificacao(Integer id) {
		setId(id);
	}

	public Integer getPessoaDocumentoIdentificacaoIdDocumentoIdentificacao() {
		return (Integer) getId();
	}

	@Override
	protected PessoaDocumentoIdentificacao createInstance() {
		PessoaDocumentoIdentificacao pessoaDocumentoIdentificacao = new PessoaDocumentoIdentificacao();
		pessoaDocumentoIdentificacao.setPessoa(new Pessoa());
		pessoaDocumentoIdentificacao.setTipoDocumento(new TipoDocumentoIdentificacao());
		pessoaDocumentoIdentificacao.setEstado(new Estado());
		/* [PJEII-6431] Erro no cadastro de Documentos de Identificação da parte  
	 	* Chama método para controle de edição/inclusão colocando em inclusão. Usado nos botões em gravarDocumentoPessoa.xhtml.  
	 	*/  
	 	setIrNovo(true);  
	 	setIrEditar(false);  
		return pessoaDocumentoIdentificacao;
	}

	public static PessoaDocumentoIdentificacaoHome instance() {
		return ComponentUtil.getComponent("pessoaDocumentoIdentificacaoHome");
	}

	@Override
	public String remove(PessoaDocumentoIdentificacao pessoaDocumentoIdentificacao) {
		String ret = super.inactive(pessoaDocumentoIdentificacao);
		refreshGrid("pessoaDocumentoIdentificacaoCadastroGrid");
		return ret;
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		if (instance.getDataExpedicao() != null && instance.getDataExpedicao().after(new Date())) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR,
					"A Data de Expedição não pode ser maior que a atual.");
			return Boolean.FALSE;
		}
		FacesMessages facesMessages = FacesMessages.instance();
		facesMessages.clear();

		getInstance().setUsuarioCadastrador(Authenticator.getUsuarioLogado());
		
		/**
		 * Alterações da JT - Correção de bugs
		 * 
		 * @author Ronny Paterson (ronnysilva@trt8.jus.br) -- PJEII-1278
		 * 
		 *         Seta valores padrões aos campos retirados do form de
		 *         inclusão de documentos de identificação.
		 */
		if(ParametroJtUtil.instance().justicaTrabalho()){
			PessoaHome pessoaHome = PessoaHome.instance();
			if(getInstance().getNome() == null ) {
				getInstance().setNome(pessoaHome.getInstance().getNome());
			}
			if(getInstance().getDocumentoPrincipal() == null ) {
				getInstance().setDocumentoPrincipal(false);
			}
		}

		if (instance.getUsadoFalsamente()) {
			getInstance().setDataUsadoFalsamente(new Date());
		}

		if (instance.getAtivo() && !instance.getUsadoFalsamente()) {


			// verificar impedimentos de cadastro de cpf ou cnpj a partir da
			// situação na receita federal
			PreCadastroPessoaBean preCadastroPessoaBean = getComponent("preCadastroPessoaBean");
			if (instance.getTipoDocumento().getCodTipo().equals("CPF")) {
				try {
					DadosReceitaPessoaFisica dadosReceita = (DadosReceitaPessoaFisica) ConsultaClienteWebService
							.instance().consultaDados(TipoPessoaEnum.F, instance.getNumeroDocumento(), false);
					if (preCadastroPessoaBean.existeImpedimentos(dadosReceita)) {
						return false;
					}

				} catch (Exception e) {
					// não permite cadastro se usuário externo
					if (Authenticator.isUsuarioExterno()) {
						facesMessages.addFromResourceBundle("preCadastroPessoaBean.erro_ws_receita");
						e.printStackTrace();
						return false;
					}
				}
			}

			if (instance.getTipoDocumento().getTipoDocumento().equals("CNPJ")) {
				try {
					DadosReceitaPessoaJuridica dadosReceita = (DadosReceitaPessoaJuridica) ConsultaClienteWebService
							.instance().consultaDados(TipoPessoaEnum.J, instance.getNumeroDocumento(), false);
					if (preCadastroPessoaBean.existeImpedimentos(dadosReceita)) {
						return false;
					}

				} catch (Exception e) {
					// não permite cadastro se usuário externo
					if (Authenticator.isUsuarioExterno()) {
						facesMessages.addFromResourceBundle("preCadastroPessoaBean.erro_ws_receita_advogado");
						e.printStackTrace();
						return false;
					}
				}
			}

			/**
		  	* PJEII-10024 RN-01
		  	*/
		  	if ((instance.getTipoDocumento().getCodTipo().equalsIgnoreCase("CPF") || instance.getTipoDocumento().getTipoDocumento().equalsIgnoreCase("TIT"))) {
			  	StringBuilder sb = new StringBuilder("SELECT pdi FROM PessoaDocumentoIdentificacao pdi ")
			  		.append("WHERE pdi.tipoDocumento = :tipo ")
			  		.append("AND pdi.pessoa = :pessoa ")
			  		.append("AND pdi.idDocumentoIdentificacao != :id");
			  		@SuppressWarnings("unchecked")
			  	List<PessoaDocumentoIdentificacao> documentos = this.getEntityManager().createQuery(sb.toString())
			  	.setParameter("tipo", this.instance.getTipoDocumento())
			  	.setParameter("pessoa", this.instance.getPessoa())
			  	.setParameter("id", this.instance.getIdDocumentoIdentificacao())
			  	.getResultList();
			  		
			  	for (PessoaDocumentoIdentificacao doc : documentos) {
			  		doc.setAtivo(Boolean.FALSE);
			  		this.getEntityManager().merge(doc);
		  		}
		  	}
		}

		return super.beforePersistOrUpdate();
	}

	@Override
	public String update() {
						
		PessoaDocumentoIdentificacao documentoIdentificacao = getInstance();
		PreCadastroPessoaBean preCadastro = PreCadastroPessoaBean.instance();
		
		if(documentoIdentificacao.getTipoDocumento().getTipoPessoa().equals(TipoPessoaEnum.F) ) {
			if (documentoIdentificacao.getTipoDocumento().getCodTipo().equalsIgnoreCase("OAB")) {
				String numeroOAB = documentoIdentificacao.getEstado().getCodEstado() + documentoIdentificacao.getNumeroDocumento() + 
						documentoIdentificacao.getLetraOAB();
				
				numeroOAB = numeroOAB.replaceAll("_", "");
				
				documentoIdentificacao.setNumeroDocumento(numeroOAB);
				String cpfCnpj = documentoIdentificacao.getPessoa().getDocumentoCpfCnpj();
				if (cpfCnpj == null) {
					FacesMessages.instance().addFromResourceBundle("CPF não encontrado. Favor, cadastrar antes um CPF.");
					return null;
				} else {
					preCadastro.setNrDocumentoPrincipal(cpfCnpj);
					PessoaFisica pessoaFisica = preCadastro.getPessoaFisica();
					if(pessoaFisica != null &&
							!ConsultaClienteOABMock.class.isAssignableFrom(Component.getInstance("consultaClienteOAB").getClass())){
						try {
							preCadastro.pesquisarAdvogado();
						} catch (PJeBusinessException e) {
							e.printStackTrace();
						}
					}
					preCadastro.setPessoaFisica(pessoaFisica);

					getProcessoParteCache().refreshProcessoParteByPessoaCache(pessoaFisica.getIdPessoa());
				}
	
			} else if (documentoIdentificacao.getTipoDocumento().getCodTipo().equalsIgnoreCase("CPF")) {
				preCadastro.setNrDocumentoPrincipal(documentoIdentificacao.getNumeroDocumento());
				preCadastro.pesquisarPorDocumento();
				if (preCadastro.getPessoaEncontradaBanco() || preCadastro.getPessoaEncontradaReceita()) {
					getProcessoParteCache().refreshProcessoParteByPessoaCache(preCadastro.getPessoa().getIdPessoa());

					return super.update();
				} else {
					FacesMessages.instance().addFromResourceBundle("CPF não encontrado.");
					return null;
				}
			}
		}
		String retorno = super.update();
		super.setId(null);
		setIrNovo(true);
		setIrEditar(false);
		return retorno;
	}
	

	@Override
	public String persist() {
		PessoaDocumentoIdentificacao documentoIdentificacao = getInstance();
		PreCadastroPessoaBean preCadastro = PreCadastroPessoaBean.instance();

		if (documentoIdentificacao.getTipoDocumento().getCodTipo().equalsIgnoreCase("OAB")) {
			String numeroOAB = documentoIdentificacao.getEstado().getCodEstado() + documentoIdentificacao.getNumeroDocumento() + 
					documentoIdentificacao.getLetraOAB();
			numeroOAB = numeroOAB.replaceAll("_", "");
			documentoIdentificacao.setNumeroDocumento(numeroOAB);
			String cpfCnpj = documentoIdentificacao.getPessoa().getDocumentoCpfCnpj();
			if (cpfCnpj == null) {
				FacesMessages.instance().addFromResourceBundle("CPF não encontrado. Favor, cadastrar antes um CPF.");
				return null;
			} else {
				preCadastro.setNrDocumentoPrincipal(cpfCnpj);
				PessoaFisica pessoaFisica = preCadastro.getPessoaFisica();
				if(pessoaFisica != null &&
						!ConsultaClienteOABMock.class.isAssignableFrom(Component.getInstance("consultaClienteOAB").getClass())){	
					try {
						preCadastro.pesquisarAdvogado();
					} catch (PJeBusinessException e) {
						e.printStackTrace();
					}
				}
				preCadastro.setPessoaFisica(pessoaFisica);
			}

		} else if (documentoIdentificacao.getTipoDocumento().getCodTipo().equalsIgnoreCase("CPF")) {
			preCadastro.setNrDocumentoPrincipal(documentoIdentificacao.getNumeroDocumento());
			preCadastro.pesquisarPorDocumento();
			if (preCadastro.getPessoaEncontradaBanco() || preCadastro.getPessoaEncontradaReceita()) {
				String persist = super.persist();
				this.newInstance();
				return persist;
			} else {
				FacesMessages.instance().addFromResourceBundle("CPF não encontrado.");
				return null;
			}
		}
		return super.persist();
	}

	@Override
	protected String afterPersistOrUpdate(String ret) {
		if (this.getInstance().getTipoDocumento().getIdentificador() && this.getInstance().getDocumentoPrincipal()) {
			Pessoa pessoa = EntityUtil.getEntityManager().find(Pessoa.class,
					getInstance().getPessoa().getIdUsuario());
			pessoa.setNome(getInstance().getNome());
			EntityManager em = EntityUtil.getEntityManager();
			em.persist(pessoa);
			em.flush();
		}
		
		return super.afterPersistOrUpdate(ret);
	}

	@Override
	public void onClickFormTab() {
		super.onClickFormTab();
	}

	public PessoaAdvogadoTipoInscricaoEnum[] getLetraOABValues() {
		return PessoaAdvogadoTipoInscricaoEnum.values();
	} 
	

	public boolean isNovo() {
		return novo;
	}

	public void setIrNovo(boolean novo) {
		this.novo = novo;
	}

	public boolean isEditar() {
		return editar;
	}

	public void setIrEditar(boolean editar) {
		this.editar = editar;
	}

	public void irNovo() {
		newInstance();
		setIrNovo(true);
		setIrEditar(false);
	}
	
	public void irEditar() {
		setIrEditar(true);
		setIrNovo(false);
	}
	
	/**
	 * PJEII-10024 - Método executado no momento da troca do Tipo de documento
	 * responsável por iniciar uma nova inclusão e setar o orgão expedidor caso
	 * seja um CPF.
	 *
	 * @param evento
	 */
	public void valueChangeTipoDocumento(ValueChangeEvent evento) {

		TipoDocumentoIdentificacao tipoNovo = (TipoDocumentoIdentificacao) evento.getNewValue();

		if (tipoNovo != null) {

			this.irNovo();
			setId(null);
			getInstance().setTipoDocumento(tipoNovo);

			if (tipoNovo.getCodTipo().equals("CPF")) {

				getInstance().setOrgaoExpedidor("Secretaria da Receita Federal");
			} else {

				getInstance().setOrgaoExpedidor(null);
			}
		}
	}
	
	
	/**
	 * Método responsável por gravar um documento de identificação de uma Pessoa
	 *
	 * @return <code>String</code>
	 */
	public String gravarDocumento() {
		String retorno = null;
		Pessoa pessoa = PessoaHome.instance().getInstance();
		documento = getInstance();
		documento.setTemporario(true);
		documento.setPessoa(pessoa);
		
		if (documento.getTipoDocumento() == null && isEditar()) {
			documento.setTipoDocumento(
				tipoDocumentoIdentificacaoManager.getTipoDocumentoIdentificacaobyDocumento(this.documento.getIdDocumentoIdentificacao()));
	  	}
		if (documento.getTipoDocumento().getCodTipo().equalsIgnoreCase("CPF") && documento.getOrgaoExpedidor() == null) {
			documento.setOrgaoExpedidor("Secretaria da Receita Federal");
		}
		try {
			if (Boolean.TRUE.equals(documento.getDocumentoPrincipal())) {
				PessoaDocumentoIdentificacaoManager pessoaDocumentoIdentificacaoManager = 
						ComponentUtil.getComponent(PessoaDocumentoIdentificacaoManager.NAME);
				
				if (pessoaDocumentoIdentificacaoManager.verificarExisteTipoDocumentoPrincipalAssociado(documento)) {
					throw new PJeBusinessException(String.format("Já existe um %s marcado como principal.", documento.getTipoDocumento()));
				}
			} else {
				documento.setDocumentoPrincipal(Boolean.FALSE);
			}
			if (documento.getTipoDocumento().getCodTipo().equalsIgnoreCase("CPF") && 
					!InscricaoMFUtil.verificaCPF(documento.getNumeroDocumento())) {
				
				throw new PJeBusinessException("CPF em formato inválido.");
			}
			if (StringUtils.isNotBlank(documento.getTipoDocumento().getMascara())) {
				String documentoValue = documento.getNumeroDocumento().replaceAll("_", "");
				String mascara = documento.getTipoDocumento().getMascara();
	
				if (documento.getTipoDocumento().getCodTipo().equals("OAB")) {
					mascara = "999999";
				}
	
				if (documentoValue.length() != mascara.length()) {
					throw new PJeBusinessException("Documento em formato inválido. Favor verificar.");
				}
			}
			if (this.getVisibilidadePessoaDocumentoIdentificacaoService().verificaDisponibilidadeNumeroDocumento(documento)) {
				throw new PJeBusinessException(documento.getTipoDocumento().getCodTipo() + " já vinculado a outra pessoa.");
			}
			PessoaDocumentoIdentificacao pessoaDocIdentResult = 
					getVisibilidadePessoaDocumentoIdentificacaoService().verificaSeDocumentoJaExistePorNomeTipoNumeroDtExpEOrgExp(pessoa, documento);
	
			if (isExisteDocumento(documento)) {
				if (documento.getIdDocumentoIdentificacao() == pessoaDocIdentResult.getIdDocumentoIdentificacao() || 
						pessoaDocIdentResult.getIdDocumentoIdentificacao() == 0) {
					
					TipoDocumentoIdentificacao tpDoc = documento.getTipoDocumento();
					if (documento.getAtivo() && (tpDoc.getCodTipo().equalsIgnoreCase("CPF") || tpDoc.getCodTipo().equalsIgnoreCase("TIT"))) {
						Boolean existeAtivo = 
								getVisibilidadePessoaDocumentoIdentificacaoService().verificaSeExisteDocumentoAtivoPorTipoPessoa(documento, pessoa);
						
						if (existeAtivo) {
							throw new PJeBusinessException("Já existe um documento ativo para este tipo.");
						}
					}
					retorno = update();
					if (retorno != null) {
						irNovo();
					}
				} else {
					throw new PJeBusinessException("Documento já cadastrado.");
				}
			} else {
				if (pessoaDocIdentResult.getIdDocumentoIdentificacao() != 0) {
					throw new PJeBusinessException("Documento já cadastrado.");
				}
				retorno = persist();
				if (retorno != null) {
					getVisibilidadePessoaDocumentoIdentificacaoService().incluirVisibilidade(Authenticator.getPessoaLogada(), documento);
					irNovo();
					
					refreshGrid("pessoaDocumentoIdentificacaoPreCadastroGrid");
					refreshGrid("pessoaDocumentoIdentificacaoCadastroGrid");
				}
			}
		} catch (PJeBusinessException ex) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, ex.getLocalizedMessage());
			if (documento.getIdDocumentoIdentificacao() != 0) {
				getEntityManager().refresh(this.documento);
			}
		}
		return retorno;
	}

	 /**
	 * Retorna true se o documento de identificação existir.
	 *
	 * @param documentoIdentificacao
	 * Documento de identificação.
	 * @return booleano
	 */
	
	 protected Boolean isExisteDocumento(PessoaDocumentoIdentificacao documentoIdentificacao) {
	
		 Boolean existe = Boolean.FALSE;
	
		 if (documentoIdentificacao != null) {
	
			 StringBuilder sb = new StringBuilder("select count(o) from PessoaDocumentoIdentificacao o ");
			 sb.append("where ");
			 sb.append("o.tipoDocumento = :tipo ");
			 sb.append("and o.pessoa = :pessoa ");
			 sb.append("and o.numeroDocumento = :numero ");
	
			 Query query = EntityUtil.createQuery(sb.toString());
			 query.setParameter("tipo", documentoIdentificacao.getTipoDocumento());
			 query.setParameter("pessoa", documentoIdentificacao.getPessoa());
			 query.setParameter("numero", documentoIdentificacao.getNumeroDocumento());
	
			 try {
				 Long retorno = (Long) query.getSingleResult();
				 existe = retorno > 0;
	
			 } catch (NoResultException no) {
				 existe = Boolean.FALSE;
			 }
		}
		return existe;
	 }

	 /**
	 * Método para obter o visibilidade Service
	 *
	 * @return
	 */
	 public VisibilidadePessoaDocumentoIdentificacaoService getVisibilidadePessoaDocumentoIdentificacaoService() {
		 return this.visibilidadePessoaDocumentoIdentificacaoService;
	 }
	
	 public PessoaDocumentoIdentificacao getDocumento() {
		 return documento;
	 }
	
	 public void setDocumento(PessoaDocumentoIdentificacao documento) {
		 this.documento = documento;
	 }
	 
	/**
	 * Ativa um documento de identificação de uma pessoa
	 * 
	 * @issue	PJEII-19868
	 * @param	documento	documento de identificação de uma pessoa
	 * @return	String
	 */
	public  String active(PessoaDocumentoIdentificacao documento) {
		String retorno = null;
		
		if (!isDocumentoPessoaAtivoExistente(documento)) {
			retorno = "update";

			MeasureTime sw = criarStopWatch();
			if(sw != null) {
				sw.start();
			}
			
			ComponentUtil.setValue(documento, "ativo", Boolean.TRUE);
			atualizarDocumento(documento);
			
			tratarMensagemDocAtivadoSucesso(sw);
		}
		
		return retorno;
	}

	/**
	 * Verifica se já existe um determinado documento de identificação ativo de uma pessoa passados por parâmetro, 
	 * inclusive tratando com mensagem correspondente a situação.
	 * 
	 * @issue	PJEII-19868
	 * @param 	doc		instancia de um objeto PessoaDocumentoIdentificacao
	 * @return	verdadeiro se existir o documento ativo para aquela pessoa, falso se não existir.
	 */
	private boolean isDocumentoPessoaAtivoExistente(PessoaDocumentoIdentificacao doc) {
		boolean retorno = false;
		if (this.getVisibilidadePessoaDocumentoIdentificacaoService().verificaSeExisteDocumentoAtivoPorTipoPessoa(doc, doc.getPessoa())) {
			tratarDocPessoaAtivoExistente(doc);
			retorno = true;
		}
		return retorno;
	}

	/**
	 * Tratamento para o caso de já existir o documento passado para a pessoa, passados por parâmetro.
	 * 
	 * @issue	PJEII-19868
	 * @param	doc		objeto PessoaDocumentoIdentificacao
	 */
	private void tratarDocPessoaAtivoExistente(PessoaDocumentoIdentificacao doc) {
		FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "pessoaDocumentoIdentificacaoHome.documento.ativo.existente");
		this.getEntityManager().refresh(doc);
	}

	
	/**
	 * Tratamento para exibição da mensagem de sucesso ao ativar um documento
	 * 
	 * @issue	PJEII-19868
	 * @param 	sw	objeto criado anteriormente, a ser utilizado na escrita do log no servidor
	 */
	private void tratarMensagemDocAtivadoSucesso(MeasureTime sw) {
		FacesMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO, "pessoaDocumentoIdentificacao.documento.ativado.sucesso");
		if(log.isDebugEnabled()) {
			log.debug(".active(" + instance + ")" + instance != null ? instance.getClass().getName(): "" + "): " 
					+ sw.getTime());
		}
	}

	/**
	 * Cria o objeto StopWatch que será utilizado posteriormente no cálculo de processamento de uma determinada ação.
	 * 
	 * @issue	PJEII-19868
	 * @return	retorna uma instância do objeto StopWatch iniciado
	 */
	private MeasureTime criarStopWatch() {
		MeasureTime sw = null;
		
		if(log.isDebugEnabled()) {
			sw = new MeasureTime();
		}
		return sw;
	}

	/**
	 * Responsável por atualizar atualizar a instância no banco de dados com os dados da instância do objeto 
	 * PessoaDocumentoIdentificacao passado por parâmetro.
	 * 
	 * @issue	PJEII-19868
	 * @param	documento	documento a ser atualizado no banco de dados
	 */
	private void atualizarDocumento(PessoaDocumentoIdentificacao documento) {
		getEntityManager().merge(documento);
		getEntityManager().flush();
	}

	/**
	 * Ativa ou desativa o documento de identificação, dependendo de qual estado o mesmo encontra-se
	 *
	 * @issue	PJEII-19868
	 * @param	documentoIdentificacao
	 * @return	String
	 */		
	public String switchActive(PessoaDocumentoIdentificacao documento) {
		String result = documento.getAtivo() ? this.inactive(documento) : this.active(documento);

		getProcessoParteCache().refreshProcessoParteByPessoaCache(documento.getPessoa().getIdPessoa());

		return result;
 	}
	
	/**
	 * Verifca se o usuário logado é procurador e se for valida 
	 * se possui pemissão para exibir os botões incluir ou gravar.
	 * @return true se o usuário logado for procurador e possuir 
	 * as permissões necessárias para exibir os botões.
	 */
	private boolean isExibirBotoesParaProcurador() {
		if(Authenticator.isProcurador()) {
			return Authenticator.isPermissaoCadastroTodosPapeis() || Authenticator.isRepresentanteGestor();
		}
		return true;
	}
	
	/**
	 * Verifica se o usuário está habilitado para ver o botão incluir.
	 * @return true caso o usuário possa incluir um novo documento de identificação.
	 */
	public boolean isExibirBotaoIncluir() {
		return isExibirBotoesParaProcurador() && (!isEditar() && isNovo());
	}
	
	/**
	 * Verifica se o usuário está habilitado para ver o botão gravar.
	 * @return true caso o usuário possa gravar (alterar) um novo documento de identificação.
	 */
	public boolean isExibirBotaoGravar() {
		return isExibirBotoesParaProcurador() && (isEditar() && !isNovo());
	}
}