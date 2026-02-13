package br.com.infox.pje.action;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.NumeroProcessoUtil;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.MimetypeUtil;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.core.certificado.DadosAssinatura;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.trf.webservice.ConsultaInstanciaIntercomunicacao;
import br.com.itx.util.AssinaturaUtil;
import br.jus.cnj.certificado.CertificadoICP;
import br.jus.cnj.certificado.CertificadoICPBrUtil;
import br.jus.cnj.certificado.SigningUtilities;
import br.jus.cnj.pje.intercomunicacao.dto.ConsultarProcessoRequisicaoDTO;
import br.jus.cnj.pje.intercomunicacao.dto.ConsultarProcessoRespostaDTO;
import br.jus.cnj.pje.intercomunicacao.service.MNIMediatorService;
import br.jus.cnj.pje.intercomunicacao.service.MNIMediatorServiceAbstract;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.AssuntoTrfManager;
import br.jus.cnj.pje.nucleo.manager.ClasseJudicialManager;
import br.jus.cnj.pje.nucleo.manager.EnderecoWsdlManager;
import br.jus.cnj.pje.nucleo.manager.EventoManager;
import br.jus.cnj.pje.nucleo.manager.JurisdicaoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.TipoProcessoDocumentoManager;
import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.ModalidadeDocumentoIdentificador;
import br.jus.cnj.pje.view.BaseAction;
import br.jus.cnj.pje.view.EntityDataModel;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.EnderecoWsdl;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.PrioridadeProcesso;
import br.jus.pje.nucleo.entidades.ProcessoAssunto;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBinPessoaAssinatura;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.enums.ClasseJudicialInicialEnum;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.nucleo.util.StringUtil;

@Name("dadosProcessoReferenciaAction")
@Scope(ScopeType.PAGE)
public class DadosProcessoReferenciaAction extends BaseAction<ProcessoTrf> implements Serializable {
	/**
	 * @author Leonardo Inácio
  	 */
	private static final long serialVersionUID = 1L;
	
	@In(create = true)
	private ProcessoJudicialManager processoJudicialManager;

	@In
	private ClasseJudicialManager classeJudicialManager;
	
	@In
	private EventoManager eventoManager;
	
	@In
	private TipoProcessoDocumentoManager tipoProcessoDocumentoManager;
	
	@In
	private AssuntoTrfManager assuntoTrfManager;
	
	private ProcessoTrf processoJudicial;
	
	private Integer paginaTabelaMovimentacaoProcessual = 0;
	
	private Integer paginaTabelaDocumentos = 0;
	
	private String idDocumento;
	
	private String numeroProcessoReferencia;
	
	private ProcessoDocumento documentoProcessual;
	
	private DadosAssinatura dadosAssinatura;
	
	private String mensagemErro;
	
	private EnderecoWsdl enderecoWsdl;
	
	@In
	private JurisdicaoManager jurisdicaoManager;
	
	private String idEnderecoWsdl;

	@Override
	protected ProcessoJudicialManager getManager() {
		return this.processoJudicialManager;
	}

	@Override
	public EntityDataModel<ProcessoTrf> getModel() {
		return null;
	}
	
	public Boolean exibirAba() {
		// Se o processo atual possui processo referência...
		if (StringUtil.isNotEmpty(getNumeroProcessoReferencia())) {
			return true;
		} else {
			return false;
		}
	}
	
	private void verificarNivelDeSigiloProcesso() throws PJeBusinessException {
		if (processoJudicial.getSegredoJustica() && Authenticator.isUsuarioExterno()) {
			processoJudicial = null;

			throw new PJeBusinessException("Para acesso aos dados do processo, favor consultar em " + enderecoWsdl + ".");
		}
	}

	/**
	 * Método responsável por recuperar os dados do processo referência
	 */
	public void recuperarDadosProcessoReferencia() {
		try {
			String numeroProcessoReferencia = getNumeroProcessoReferencia();
			
			// Se o número do processo referência não está no padrão de numeração única do CNJ...
			if(!NumeroProcessoUtil.numeroProcessoValido(numeroProcessoReferencia)) {
				throw new Exception("O número do processo referência não está no padrão de numeração única do CNJ");
			}
			
			enderecoWsdl = getEnderecoWsdlOrigem(ProcessoTrfHome.instance().getInstance());
						
			// Consultar os dados do processo referência na instância especificada
			processoJudicial = consultarProcesso(enderecoWsdl);
			
			// Se o processo não foi encontrado na instância especificada...
			if (processoJudicial == null) {
				enderecoWsdl = ParametroUtil.instance().getEnderecoWsdlAplicacaoOrigem();
				
				// Consultar os dados do processo referência na instância especificada
				processoJudicial = consultarProcesso(enderecoWsdl);				
			}
			
			// Se o processo não foi encontrado na instância especificada...
			if (processoJudicial == null) {
				throw new Exception(
						"O processo " + numeroProcessoReferencia + 
						" não foi encontrado na instância " + enderecoWsdl.getDescricao() + ".");
			}
			
			/*inabilitar a visualização de processos  sigilosos  na opção processo de referência */
			verificarNivelDeSigiloProcesso();
			processarDocumentosVinculados();			
			this.idEnderecoWsdl = String.valueOf(enderecoWsdl.getIdEnderecoWsdl());
		} catch (PJeBusinessException e) {
			FacesMessages.instance().add(Severity.INFO,  e.getCode());
		} catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR, "Ocorreu o seguinte erro ao tentar consultar os dados do processo: " + e.getMessage());
		}
	}

	/**
	 * Método responsável por processar a lista de documentos vinculados aos documentos principais do processo
	 */
	private void processarDocumentosVinculados() {
		ArrayList<ProcessoDocumento> documentosVinculados = new ArrayList<ProcessoDocumento>();
		
		for (ProcessoDocumento documento : processoJudicial.getProcesso().getProcessoDocumentoList()) {
			for (ProcessoDocumento documentoVinculado : documento.getDocumentosVinculados()) {
				documentosVinculados.add(documentoVinculado);
			}
		}
		
		processoJudicial.getProcesso().getProcessoDocumentoList().addAll(documentosVinculados);
		
		// Ordenar a lista de documentos por data de juntada
		Collections.sort(processoJudicial.getProcesso().getProcessoDocumentoList(), new Comparator<ProcessoDocumento>() {
			@Override
			public int compare(ProcessoDocumento dp1, ProcessoDocumento dp2) {
				if (dp1.getDataJuntada() == null || dp2.getDataJuntada() == null) {
					return 0;
				}
				
				return dp2.getDataJuntada().compareTo(dp1.getDataJuntada());
			}
		});
		
		// Ordenar a lista de movimentos por data de inclusão
		Collections.sort(processoJudicial.getProcesso().getProcessoEventoList(), new Comparator<ProcessoEvento>() {
			@Override
			public int compare(ProcessoEvento mp1, ProcessoEvento mp2) {
				if (mp1.getDataAtualizacao() == null || mp2.getDataAtualizacao() == null) {
					return 0;
				}
				
				return mp2.getDataAtualizacao().compareTo(mp1.getDataAtualizacao());
			}
		});
	}
	
	/**
	 * Método responsável por recuperar o número do processo formatado
	 * @return Número do processo formatado
	 */
	public String recuperarNumeroProcessoFormatado() {
		return NumeroProcessoUtil.mascaraNumeroProcesso(processoJudicial.getNumeroProcesso());
	}
	
	/**
	 * Método responsável por recuperar a descrição da jurisdição processual do processo especificado
	 * @return Descrição da jurisdição processual do processo
	 */
	public String recuperarJurisdicaoProcessual() {
		String resultado = null;
		
		try {
			resultado = String.valueOf(processoJudicial.getJurisdicao().getNumeroOrigem());
			br.jus.cnj.pje.ws.Jurisdicao jurisdicao = ConsultaInstanciaIntercomunicacao.instance().recuperarJurisdicaoProcessual(enderecoWsdl, resultado);
			if (jurisdicao != null) {
				resultado += " - " + jurisdicao.getDescricao();
			}
		}
		catch (Exception e) {
			resultado += " - (Sem descrição)";
		}
		return resultado;
	}
	
	/**
	 * Método responsável por recuperar os dados da classe judicial do processo referência
	 * @return Dados da classe judicial do processo referência
	 */
	public String recuperarClasseJudicial() {
		String codigoClasseJudicial = processoJudicial.getClasseJudicial().getCodClasseJudicial();
		
		try {			
			return classeJudicialManager.findByCodigo(codigoClasseJudicial).getClasseJudicial();
		}
		catch (Exception e) {
			return "Classe não cadastrada - " + codigoClasseJudicial;
		}
	}
	
	/**
	 * Método responsável por recuperar a descrição do evento especificado
	 * @param movimentacaoProcessual Dados da movimentação processual
	 * @return Descrição do evento
	 */
	public String recuperarDescricaoEvento(ProcessoEvento movimentacaoProcessual) {
		StringBuilder sb = new StringBuilder("");
		
		if (movimentacaoProcessual != null && movimentacaoProcessual.getEvento() != null && StringUtils.isNotBlank(movimentacaoProcessual.getEvento().getCodEvento())) {
			sb.append(movimentacaoProcessual.getTextoFinal());
		}
		
		return sb.toString();
	}
	
	/**
	 * Método responsável por recuperar a descrição do Tipo de documento especificado
	 * @param tipoDocumento Código do tipo de documento
	 * @return Descrição do tipo de documento
	 */
	public String recuperarTipoDocumento(String tipoDocumento) {
		try {
			return tipoProcessoDocumentoManager.findByCodigoDocumento(tipoDocumento, null).getTipoProcessoDocumento();
		} catch (Exception e) {
			return "Tipo de documento não cadastrado - " + tipoDocumento;
		}
	}
	
	/**
	 * Método responsável por recuperar a descrição do Assunto processual especificado
	 * @param assuntoProcessual Dados do Assunto processual
	 * @return Descrição do Assunto processual
	 */
	public String recuperarAssuntoProcessual(ProcessoAssunto assuntoProcessual) {
		try {
			AssuntoTrf assuntoTrf = assuntoProcessual.getAssuntoTrf();
			return assuntoTrf.getAssuntoTrf();
		} catch (Exception e) {
			return "Assunto não cadastrado - " + assuntoProcessual.getAssuntoTrf().getCodAssuntoTrf();
		}		
	}
	
	/**
	 * Método responsável por recuperar as prioridades do processo
	 * @return Lista de prioridades do processo
	 */
	public String recuperarPrioridadesProcesso() {
		StringBuilder sb = new StringBuilder("");
		
		for (PrioridadeProcesso prioridade : processoJudicial.getPrioridadeProcessoList()) {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			
			sb.append(prioridade.getPrioridade().toUpperCase());
		}
		
		return sb.toString();
	}
	
	/**
	 * Método responsável por recuperar as partes do polo ativo 
	 * @return Lista de partes do polo ativo
	 */
	public List<ProcessoParte> recuperarPartesPoloAtivo() {
		return processoJudicial.getListaParteAtivo();
	}
	
	/**
	 * Método responsável por recuperar as partes do polo passivo 
	 * @return Lista de partes do polo passivo
	 */
	public List<ProcessoParte> recuperarPartesPoloPassivo() {
		return processoJudicial.getListaPartePassivo();
	}
	
	/**
	 * Método responsável por recuperar as partes do polo "Outros interessados" 
	 * @return Lista de partes do polo "Outros interessados"
	 */
	public List<ProcessoParte> recuperarPartesOutrosInteressados() {
		return processoJudicial.getListaParteTerceiro();
	}
	
	/**
	 * Método responsável por retornar a descrição da parte especificada
	 * @param parte Dados da parte
	 * @return Descrição da parte
	 */
	public String recuperarDadosParte(ProcessoParte parte) {
		StringBuilder sb = new StringBuilder();
		sb.append(parte.getPessoa().getNome());
		
		// Recuperar o documento de CPF ou CNPJ da parte
		String documentoCpfCnpj = parte.getPessoa().getDocumentoCpfCnpj();
		
		// Se o documento de CPF ou CNPJ da parte foi informado...
		if (documentoCpfCnpj != null) {
			// Se a pessoa for "Física"...
			if (TipoPessoaEnum.F.equals(parte.getPessoa().getInTipoPessoa())) {
				sb.append(" - CPF: " + StringUtil.formartCpf(documentoCpfCnpj));
			}
			// Se a pessoa for "Jurídica"...
			else if (TipoPessoaEnum.J.equals(parte.getPessoa().getInTipoPessoa())) {
				sb.append(" - CNPJ: " + StringUtil.formatCnpj(documentoCpfCnpj));
			}
		}
				
		return sb.toString();
	}
	
	
	/**
	 * Método responsável por retornar a descrição do representate da parte especificada
	 * @param parte Dados do representante da parte
	 * @return Descrição do representate da parte
	 */
	public String recuperarDadosRepresentanteParte(ProcessoParteRepresentante representanteProcessual) {
		StringBuilder sb = new StringBuilder();
		sb.append(representanteProcessual.getRepresentante().getNome());
		
		// Se os dados da inscrição na OAB não são nulos...
		PessoaDocumentoIdentificacao identificacao = representanteProcessual.getRepresentante().getPessoaDocumentoIdentificacaoList().stream()
			.filter(x -> x.getTipoDocumento().getCodTipo().equals(ModalidadeDocumentoIdentificador.OAB.value()))
			.findFirst()
			.orElse(new PessoaDocumentoIdentificacao());
		if (identificacao != null && identificacao.getNumeroDocumento() != null) {
			sb.append(" - OAB: " + identificacao.getNumeroDocumento());
		}
		
		// Se o documento de identificação principal do representante foi informado...
		if (StringUtil.isNotEmpty(representanteProcessual.getRepresentante().getDocumentoCpfCnpj())) {
			sb.append(" - CPF: " + StringUtil.formartCpf(representanteProcessual.getRepresentante().getDocumentoCpfCnpj()));
		}
		
		sb.append(" (").append(representanteProcessual.getTipoRepresentante().getTipoParte()).append(")");
				
		return sb.toString();
	}
	
	/**
	 * Método responsável por recuperar o documento de identificação especificado
	 * @param pessoa Dados da pessoa para a qual será recuperado o documento de identificação
	 * @param tipoDocumentoIdentificacao Tipo de documento a ser recuperado
	 * @return Dados do documento de identificação
	 */
	private PessoaDocumentoIdentificacao recuperarDocumentoIdentificacao(Pessoa pessoa, ModalidadeDocumentoIdentificador tipoDocumentoIdentificacao) {
		// Percorrer a lista de documentos de identificação da pessoa
		for (PessoaDocumentoIdentificacao documentoIdentificacao : pessoa.getPessoaDocumentoIdentificacaoList()) {
			// Se o documento de identificação atual for "CPF" ou "CNPJ"...
			if (ModalidadeDocumentoIdentificador.CMF.equals(documentoIdentificacao.getTipoDocumento())) {
				return documentoIdentificacao;
			}
		}
		
		return null;
	}
	
	/**
	 * Método responsavel por consultar o documento de processo selecionado pelo usuário
	 * @return Documento de processo
	 * @throws Exception
	 */
	public ProcessoDocumento consultarDocumentoProcesso() throws Exception {
		enderecoWsdl = EnderecoWsdlManager.instance().findById(Integer.parseInt(idEnderecoWsdl));
		
		// Consultar os dados do processo referência na instância especificada
		return consultarDocumento(enderecoWsdl, Integer.valueOf(idDocumento));
	}
	
	/**
	 * Método responsável por recuperar o conteído HTML do documento selecionado pelo usuário
	 * @return Conteúdo HTML do documento selecionado pelo usuário
	 */
	public String recuperarConteudoHtmlDocumento() {
		try {
			// Se o documento foi consultado com sucesso...
			if (documentoProcessual != null && documentoProcessual.getProcessoDocumentoBin() != null) {
				ProcessoDocumentoBin bin = documentoProcessual.getProcessoDocumentoBin();
				return new String(bin.getModeloDocumento());
			} else {
				return "O documento de ID " + idDocumento + " não foi encontrado na instância de origem"; 
			}
			
		} catch (Exception e) {
			return "Ocorreu o seguinte erro ao tentar recuperar o documento de ID " + idDocumento + ": " + e.getMessage();
		}
	}
	
	/**
	 * Método responsável por recuperar os dados da assinatura digital no padrão MNI especificada
	 * @param assinaturaDigital Dados da assinatura digital no padrão MNI
	 * @return Dados da assinatura digital
	 */
	public DadosAssinatura recuperarDadosAssinaturaDigital(ProcessoDocumentoBinPessoaAssinatura assinaturaDigital) {
		if (dadosAssinatura == null) {
			try {
				dadosAssinatura = new DadosAssinatura();
				
				if (AssinaturaUtil.isModoTeste(assinaturaDigital.getAssinatura())) {
					dadosAssinatura.commonName = "Assinatura de teste";
					dadosAssinatura.dataAssinatura = assinaturaDigital.getDataAssinatura();
					dadosAssinatura.assinatura = assinaturaDigital.getAssinatura();
					dadosAssinatura.certChain = assinaturaDigital.getCertChain();
					dadosAssinatura.issuer = "PJe em teste";
					dadosAssinatura.nome = "PJe em teste";
				} else {
					Certificate[] cert = SigningUtilities.getCertChain(assinaturaDigital.getCertChain());
					CertificadoICP certificadoICP = CertificadoICPBrUtil.getInstance((X509Certificate) cert[0], false);
					dadosAssinatura.commonName = ((X509Certificate) cert[0]).getSubjectX500Principal().getName();
					dadosAssinatura.dataAssinatura = assinaturaDigital.getDataAssinatura();
					dadosAssinatura.assinatura = assinaturaDigital.getAssinatura();
					dadosAssinatura.certChain = assinaturaDigital.getCertChain();
					dadosAssinatura.certificate = (X509Certificate) cert[0];
					dadosAssinatura.issuer = dadosAssinatura.certificate.getIssuerX500Principal().getName();
					
					if (certificadoICP != null) {
						dadosAssinatura.nome = certificadoICP.getNome();
						dadosAssinatura.cadastroMF = certificadoICP.getInscricaoMF();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return dadosAssinatura;
	}
	
	/**
	 * Método responsável por informar se o documento especificado é do tipo HTML
	 * @param documentoProcessual Dados do documento processual
	 * @return
	 */
	public Boolean isDocumentoHtml(ProcessoDocumento documentoProcessual) {
		return MimetypeUtil.isMimetypeHtml(documentoProcessual.getProcessoDocumentoBin().getExtensao());
	}
	
	/**
	 * Método responsável por iniciar o download do documento processual selecionado pelo usuário
	 */
	public String iniciarDownloadDocumentoProcessual() {
		try {
			setDocumentoProcessual(consultarDocumentoProcesso());
		} catch (Exception e) {
			mensagemErro = e.getMessage();
			e.printStackTrace();
		}
		
		return "/download.xhtml";
	}
	
	/**
	 * Método responsável por finalizar o download do documento processual selecionado pelo usuário
	 */
	public void finalizarDownloadDocumentoProcessual() {
		byte[] bytes = null;
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();		
		
		try {
			setDocumentoProcessual(consultarDocumentoProcesso());
		} catch (Exception e) {
			mensagemErro = e.getMessage();
			e.printStackTrace();
		}
		
		// Se o documento processual não foi recuperado com sucesso...
		if (documentoProcessual == null) {
			String mensagem = "Ocorreu o seguinte erro ao tentar recuperar o documento de ID " + idDocumento + ": " + mensagemErro;
			bytes = mensagem.getBytes();
		} else { 
			// Recuperar o conteúdo do documento processual
			bytes = documentoProcessual.getProcessoDocumentoBin().getProcessoDocumento();		
			
			// Configurar os parâmetros de download do arquivo selecionado pelo usuário
			response.setContentType(documentoProcessual.getProcessoDocumentoBin().getExtensao());
			response.setContentLength(bytes.length);
			
			TipoProcessoDocumento tipoProcessoDocumento = documentoProcessual.getTipoProcessoDocumento();
			if (MimetypeUtil.isMimetypePdf(documentoProcessual.getProcessoDocumentoBin().getExtensao())){
				response.setHeader("Content-disposition", "filename=\"" + tipoProcessoDocumento.getTipoProcessoDocumento() + "\"");
			} else {
				response.setHeader("Content-disposition", "attachment; filename=\"" + tipoProcessoDocumento.getTipoProcessoDocumento() + "\"");
			}
		}
		
		try {
			OutputStream out = response.getOutputStream();
			out.write(bytes);
			out.flush();
			facesContext.responseComplete();
		} catch (IOException ex) {
			FacesMessages.instance().add("Erro ao descarregar o arquivo: " + documentoProcessual.getTipoProcessoDocumento().getTipoProcessoDocumento());
		}
	}
	
	public Date converterStringParaDate(String dataHora) {
		return DateUtil.stringToDate(dataHora, "yyyyMMddHHmmss");		
	}
	
	/**
	 * Método responsável por retornar o número do processo de referência do processo especificado
	 * @param processoTrf Dados do processo selecionado pelo usuário
	 * @return Número do processo de referência do processo especificado
	 */
	private String getNumeroProcessoReferencia(ProcessoTrf processoTrf) {
		if (processoTrf.getIsIncidente() && processoTrf.getProcessoReferencia() != null) {
			return processoTrf.getProcessoReferencia().getNumeroProcesso();
		}
		
		return processoTrf.getDesProcReferencia();
	}
	
	public ProcessoTrf getProcessoJudicial() {
		return processoJudicial;
	}
	
	public void setPaginaTabelaMovimentacaoProcessual(Integer paginaTabelaMovimentacaoProcessual) {
		this.paginaTabelaMovimentacaoProcessual = paginaTabelaMovimentacaoProcessual;
	}
	
	public Integer getPaginaTabelaMovimentacaoProcessual() {
		return paginaTabelaMovimentacaoProcessual;
	}
	
	public Integer getPaginaTabelaDocumentos() {
		return paginaTabelaDocumentos;
	}
	
	public void setPaginaTabelaDocumentos(Integer paginaTabelaDocumentos) {
		this.paginaTabelaDocumentos = paginaTabelaDocumentos;
	}
	
	public String getIdDocumento() {
		return idDocumento;
	}
	
	public void setIdDocumento(String idDocumento) {
		this.idDocumento = idDocumento;
	}
	
	public String getNumeroProcessoReferencia() {
		if (this.numeroProcessoReferencia == null) {
			this.numeroProcessoReferencia = getNumeroProcessoReferencia(ProcessoTrfHome.instance().getInstance());
		}
		
		return this.numeroProcessoReferencia;
	}
	
	public void setNumeroProcessoReferencia(String numeroProcessoReferencia) {
		this.numeroProcessoReferencia = numeroProcessoReferencia;
	}
	
	public ProcessoDocumento getDocumentoProcessual() {
		return documentoProcessual;
	}
	
	public void setDocumentoProcessual(ProcessoDocumento documentoProcessual) {
		this.documentoProcessual = documentoProcessual;
	}
	
	public String getIdEnderecoWsdl() {
		return idEnderecoWsdl;
	}

	public void setIdEnderecoWsdl(String idEnderecoWsdl) {
		this.idEnderecoWsdl = idEnderecoWsdl;
	}
	
	/**
	 * Retorna o EnderecoWsdl do processo de referência. Se o processo for incidentão então o 
	 * EnderecoWsdl é da instalação atual, caso contrário será recuperado nas tabelas carregadas 
	 * via remessa.
	 * 
	 * @param processo ProcessoTrf.
	 * @return EnderecoWsdl.
	 */
	protected EnderecoWsdl getEnderecoWsdlOrigem(ProcessoTrf processo) {
		// Se o processo for incidental então retorna o WSDL da própria instalação.
		if (!ClasseJudicialInicialEnum.R.equals(processo.getInicial()) && processo.getIsIncidente()) {
			enderecoWsdl = ParametroUtil.instance().getEnderecoWsdlAplicacaoOrigem();
		} else {
			EnderecoWsdlManager enderecoWsdlManager = EnderecoWsdlManager.instance();
			enderecoWsdl = enderecoWsdlManager.obterEnderecoWsdl(processo, true);
		}
		return enderecoWsdl;
	}
	
	/**
	 * Consulta o processo no endpoint do EnderecoWsdl.
	 * 
	 * @param enderecoWsdl EnderecoWsdl
	 * @return ProcessoTrf
	 */
	protected ProcessoTrf consultarProcesso(EnderecoWsdl enderecoWsdl) {
		MNIMediatorService mediator = MNIMediatorServiceAbstract.instance(enderecoWsdl);

		ConsultarProcessoRequisicaoDTO requisicao = new ConsultarProcessoRequisicaoDTO();
		requisicao.setNumeroProcesso(getNumeroProcessoReferencia());
		requisicao.setIncluirDocumentos(true);
		requisicao.setMovimentos(true);
		
		ConsultarProcessoRespostaDTO resposta = mediator.consultarProcesso(requisicao);
		return resposta.getProcessoTrf();
	}
	
	/**
	 * Consulta o documento no endpoint do EnderecoWsdl.
	 * 
	 * @param enderecoWsdl EnderecoWsdl
	 * @param idDocumento ID do documento na instância do endpoint.
	 * @return ProcessoDocumento
	 */
	protected ProcessoDocumento consultarDocumento(EnderecoWsdl enderecoWsdl, Integer idDocumento) {
		MNIMediatorService mediator = MNIMediatorServiceAbstract.instance(enderecoWsdl);

		ConsultarProcessoRequisicaoDTO requisicao = new ConsultarProcessoRequisicaoDTO();
		requisicao.setNumeroProcesso(getNumeroProcessoReferencia());
		requisicao.setIncluirCabecalho(false);
		requisicao.setIncluirDocumentos(false);
		requisicao.setMovimentos(false);
		requisicao.getDocumento().add(String.valueOf(idDocumento));
		
		ConsultarProcessoRespostaDTO resposta = mediator.consultarProcesso(requisicao);
		List<ProcessoDocumento> documentos = resposta.getProcessoDocumentoList();
		return (ProjetoUtil.getTamanho(documentos) > 0 ? documentos.get(0) :  null);
	}
}