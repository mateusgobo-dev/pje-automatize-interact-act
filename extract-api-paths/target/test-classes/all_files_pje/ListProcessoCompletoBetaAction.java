package br.jus.cnj.pje.view;

import java.io.IOException;
import java.io.OutputStream;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.jus.cnj.pje.nucleo.service.LogAcessoAutosDownloadsService;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;

import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.action.DadosProcessoReferenciaAction;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.com.itx.util.AssinaturaUtil;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.FacesUtil;
import br.jus.cnj.certificado.SigningUtilities;
import br.jus.cnj.pje.business.dao.PapelDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ListProcessoCompletoBetaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoAlertaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoFavoritoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoPeticaoNaoLidaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoMagistradoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteRepresentanteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoTagManager;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.cnj.pje.servicos.DateService;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.view.ListProcessoCompletoBetaAction.PaginadorDocumentos.Conteudo;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.CriterioPesquisa;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.EtiquetaProcesso;
import br.jus.csjt.pje.business.pdf.GeradorPdfUnificado;
import br.jus.csjt.pje.business.pdf.PdfException;
import br.jus.csjt.pje.commons.util.ParametroJtUtil;
import br.jus.csjt.pje.view.action.AjusteMovimentacaoAction;
import br.jus.je.pje.entity.vo.ProcessoParteVO;
import br.jus.je.pje.entity.vo.TimeLineAutosDigitais;
import br.jus.je.pje.entity.vo.TimeLineAutosDigitais.TipoDadoENUM;
import br.jus.pje.nucleo.dto.AutoProcessualDTO;
import br.jus.pje.nucleo.entidades.PrioridadeProcesso;
import br.jus.pje.nucleo.entidades.ProcessoAlerta;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBinPessoaAssinatura;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoFavorito;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoPeticaoNaoLida;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTag;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.entidades.TagMin;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.enums.PJeEnum;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;
import br.jus.pje.nucleo.enums.ProcessoTrfApreciadoEnum;
import br.jus.pje.nucleo.enums.TipoAtuacaoDetalhadaMagistradoEnum;
import br.jus.pje.nucleo.enums.TipoOrigemAcaoEnum;
import br.jus.pje.nucleo.enums.TipoUsuarioExternoEnum;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

/**
 * Componente de controle da tela dos autos digitais
 * xhtml /Painel/painel_usuario/sessao.xhtml. 
 */
@Name(ListProcessoCompletoBetaAction.NAME)
@Scope(ScopeType.PAGE)
public class ListProcessoCompletoBetaAction {
	public final static String  NAME = "listProcessoCompletoBetaAction";
	public final static String  NOME = "N";
	public final static String  NOME_DETALHES = "D";

	private static final String APPLICATION_PDF = "application/pdf";
	private static final String TEXT_HTML = "text/html";
	
	/**
	 * O objeto de log.
	 */
	@Logger
	protected Log logger;
	
	/**
	 * O objeto de envio de mensagens para a tela.
	 */
	@In
	protected FacesMessages facesMessages;
	
	@RequestParameter
	private Integer idProcesso;
	
	@RequestParameter
	private Integer paramIdProcessoDocumento;
	
	@RequestParameter
	private Integer id;
	
	@RequestParameter
	private Integer idProcessoParteExpediente;
	
	@RequestParameter
	private String aba;
	
	private ProcessoTrf processoSelecionado;
	private ProcessoDocumento documentoSelecionado;
	private boolean exibeDadosRelator;
	private boolean exibeValorCausa = true;
	private boolean exibeOutrosInteressados;

	private List<ProcessoParte> poloAtivo = new ArrayList<ProcessoParte>();
	private List<ProcessoParte> poloPassivo = new ArrayList<ProcessoParte>();
	private List<ProcessoParte> poloOutrosInterresados = new ArrayList<ProcessoParte>();
	private TreeSet<ProcessoParteVO> parteSemVinculacaoAtivo = new TreeSet<ProcessoParteVO>();
	private TreeSet<ProcessoParteVO> parteSemVinculacaoPassivo = new TreeSet<ProcessoParteVO>();
	private TreeSet<ProcessoParteVO> parteSemVinculacaoOutrosInteressados = new TreeSet<ProcessoParteVO>();
	private TreeSet<ProcessoParteVO> representantesAtivos = new TreeSet<ProcessoParteVO>();
	private TreeSet<ProcessoParteVO> representantesPassivo = new TreeSet<ProcessoParteVO>();
	private TreeSet<ProcessoParteVO> representantesOutrosInteressados = new TreeSet<ProcessoParteVO>();
	private LinkedHashSet<ProcessoDocumento> documentosAbertos = new LinkedHashSet<ProcessoDocumento>();
	private List<TimeLineAutosDigitais> timeLineAutosDigitais = new ArrayList<TimeLineAutosDigitais>();
	private Map<String,List<TimeLineAutosDigitais>> mapDatas = new LinkedHashMap<String,List<TimeLineAutosDigitais>>();
	private Map<Integer,ProcessoDocumento> mapDocumentos = new HashMap<Integer,ProcessoDocumento>(0);
	private List<ProcessoAlerta> alertasProcesso = new ArrayList<ProcessoAlerta>(0);
	
	private List<EtiquetaProcesso> etiquetasProcesso = new ArrayList<EtiquetaProcesso>(0);
	private List<TagMin> etiquetasUsuario;
	private Map<TagMin, Boolean> etiquetasCheck = new HashMap<TagMin,Boolean>(0);
	private String textoPesquisaTag;
	private boolean exibirPopupTags = false;


	private List<Pendencia> pendenciasProcesso = new ArrayList<Pendencia>(0);
	private Map<TipoAtuacaoDetalhadaMagistradoEnum, Entry<Integer, String>> mapMagistradosProcesso = new LinkedHashMap<TipoAtuacaoDetalhadaMagistradoEnum, Entry<Integer, String>>();
	
	private int paginaAtual = 0;
	private int countPaginas;
	private boolean exibirDocumentos = Boolean.TRUE;
	private boolean exibirMovimentos = Boolean.TRUE;
	private int idProcDocSelecionado;
	private int idDocJavaScript;
	private boolean exibePdf;
	private boolean exibeHtml;

	private transient ProtocolarDocumentoBean protocolarDocumentoBean;
	private String tab;
	private String textoPesquisa;
	private Order orderBy = Order.DESC;
	private PaginadorDocumentos paginador;
	private ParametroDownload parametroDownload = new ParametroDownload();
	private boolean isPessoaJuizoDoProcesso = Boolean.FALSE;
	private boolean isProcessoDeslocadoParaLocalizacaoPessoa = Boolean.FALSE;
	private ProcessoDocumento processoDocumentoAtual;
	private String competencia;
	private Long totalDocumentosNaoLidos;
	private String mensagemErro;

	
	private Boolean permiteVisualizarAbaCertidaoCriminal;
	private Boolean permiteVisualizarAbaInformacaoCriminal;
	private boolean exibirTarefa = false;


	private ProcessoDocumento processoDocumentoVincu;

	private Boolean processoForaDeTramitacao = Boolean.FALSE;


	
	@In
	private PapelDAO papelDAO;

	public boolean getIsAmbienteColegiado() {
		return !ComponentUtil.getComponent(ParametroUtil.class).isPrimeiroGrau();
	}
	
	public enum TipoPendenciaEnum implements PJeEnum {
		T("Antecipação de tutela"), J("Justiça gratuita"), S("Segredo de Justiça"), D("Sigilo de documento"), P("Petições avulsas");

		private String label;

		TipoPendenciaEnum(String label) {
			this.label = label;
		}

		@Override
		public String getLabel() {
			return this.label;
		}
	}

	public class Pendencia {
		private String idPendencia;
		private TipoPendenciaEnum tipo;
		
		public Pendencia(TipoPendenciaEnum tipo) {
			setTipo(tipo);
		}

		public String getIdPendencia() {
			idPendencia = getProcessoSelecionado().getIdProcessoTrf() + getTipo().toString();
			return idPendencia;
		}

		public String getMensagem() {
			StringBuilder msg = new StringBuilder();
			switch (getTipo()) {
				case T:
					msg.append("Há pedido de antecipação de tutela ou de liminar.");
					break;
					
				case J:
					msg.append("Há pedido de concessão de assistência judiciária gratuita.");
					break;

				case S:
					msg.append("Foi formulado pedido de atribuição de segredo de justiça.");
					break;

				case D:
					msg.append("Há pedido de atribuição de sigilo a documento.");
					break;

				case P:
					msg.append("Há petição(ões) avulsa(s) não apreciada(s).");
					break;

				default:
					return null;
			}
			msg.append(" Após a apreciação, exclua a notificação.");
			return msg.toString();
		}
		
		public TipoPendenciaEnum getTipo() {
			return tipo;
		}
		
		protected void setTipo(TipoPendenciaEnum tipo) {
			this.tipo = tipo;
		}

	}

	public class PaginadorDocumentos {
		private int indice = 0;
		private int total = 0;
		private Conteudo conteudo;
		private ListProcessoCompletoBetaManager autosManager;

		public PaginadorDocumentos(Integer idProcesso, int total) {
			setConteudo(new Conteudo());
			getConteudo().setIdProcesso(idProcesso);
			setTotal(total);
			autosManager = ComponentUtil.getComponent(ListProcessoCompletoBetaManager.class);
		}

		public int getIndice() {
			return indice;
		}
		
		private void setIndice(int indice) {
			this.indice = indice;
		}
		
		public int getTotal() {
			return total;
		}
		
		private void setTotal(int total) {
			this.total = total;
		}

		public Conteudo getConteudo() {
			return conteudo;
		}

		private void setConteudo(Conteudo conteudo) {
			this.conteudo = conteudo;
		}


		public boolean isPrimeiro() {
			return (indice <= 0);
		}

		public boolean isUltimo() {
			return (indice == total);
		}

		public boolean isTemProximo() {
			return (indice < total);
		}

		public boolean isTemAnterior() {
			return (indice > 1);
		}

		public void anterior() {
			if(isTemAnterior()) {
				AutoProcessualDTO auto = autosManager.recuperarAutoAnterior(getConteudo().getIdProcesso(), getConteudo().getIdDocumento());
				preencherDocumento(auto.getDocumento(), (auto.getIdDocumentoFavorito() == null ? 0 : auto.getIdDocumentoFavorito().intValue()));
				indice--;
			}
		}

		public void proximo() {
			if(isTemProximo()) {
				AutoProcessualDTO auto = autosManager.recuperarProximoAuto(getConteudo().getIdProcesso(), getConteudo().getIdDocumento() == 0 ? null : getConteudo().getIdDocumento());
				preencherDocumento(auto.getDocumento(), (auto.getIdDocumentoFavorito() == null ? 0 : auto.getIdDocumentoFavorito().intValue()));
				indice++;
			}
		}
		
		public void primeiro() {
			try {
				AutoProcessualDTO auto = autosManager.recuperarPrimeiroAuto(getConteudo().getIdProcesso());
				preencherDocumento(auto.getDocumento(), (auto.getIdDocumentoFavorito() == null ? 0 : auto.getIdDocumentoFavorito().intValue()));
				indice = 1;
			} catch (Exception e) {				
				e.printStackTrace();
			}
			
		}
		
		public void ultimo() {
			try {
				AutoProcessualDTO auto = autosManager.recuperarUltimoAuto(getConteudo().getIdProcesso());
				preencherDocumento(auto.getDocumento(), (auto.getIdDocumentoFavorito() == null ? 0 : auto.getIdDocumentoFavorito().intValue()));
				indice = total;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void ultimoPrincipal() {
			try {
				AutoProcessualDTO auto = autosManager.recuperarUltimoAutoPrincipal(getConteudo().getIdProcesso());
				preencherDocumento(auto.getDocumento(), (auto.getIdDocumentoFavorito() == null ? 0 : auto.getIdDocumentoFavorito().intValue()));
				indice = total;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void atual(Integer idDocumento) {
			AutoProcessualDTO auto = autosManager.recuperarAuto(getConteudo().getIdProcesso(), idDocumento);
			preencherDocumento(auto.getDocumento(), (auto.getIdDocumentoFavorito() == null ? 0 : auto.getIdDocumentoFavorito().intValue()));
			setIndice(auto.getIndice());
		}
		
		private void preencherDocumento(ProcessoDocumento pd, int idDocumentoFavorito) {
			if (pd != null) {
				adicionarIdNaSessao(pd.getIdProcessoDocumento());
				getConteudo().setIdDocumento(pd.getIdProcessoDocumento());
				getConteudo().setIdBinario(pd.getProcessoDocumentoBin().getIdProcessoDocumentoBin());
				getConteudo().setNomeDocumento(processarNomeDocumento(pd));
				getConteudo().setTextoDocumento(processarNomeDocumento(pd));
				getConteudo().setDataDocumento(pd.getDataJuntada());
				getConteudo().setDataDocumentoFormatada(DateUtil.dateToString(pd.getDataJuntada(), "dd/MM/yyyy HH:mm:ss"));
				getConteudo().setIdDocumentoFavorito(idDocumentoFavorito);
				getConteudo().setNomeUsuario(obterNomeUsuarioJuntada(pd).toUpperCase());
				getConteudo().setSigilo(pd.getDocumentoSigiloso());
				String numeroDocumento = (pd.getNumeroDocumento() == null || pd.getNumeroDocumento().trim().length() == 0 ? null : pd.getNumeroDocumento().trim());
				getConteudo().setNumeroDocumento(numeroDocumento);
				ProcessoDocumentoBin bin = pd.getProcessoDocumentoBin();

				String extensao = null;

				if (!bin.isBinario()) {
					extensao = TEXT_HTML;
				} else {
					extensao = bin.getNomeArquivo() != null && bin.getNomeArquivo().toLowerCase().endsWith(".pdf")
							? APPLICATION_PDF
							: bin.getExtensao();
				}

				getConteudo().setAssinaturas(carregarAssinaturas(bin.getSignatarios()));
				getConteudo().setExtensao(extensao);
			}
		}

		private void adicionarIdNaSessao(Integer idDocumento){
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			String idsAtuais = "";
			String attribute = "idsDocumentos";
			if(request.getSession().getAttribute(attribute) != null) {
				idsAtuais = (String) request.getSession().getAttribute(attribute);
			}
			request.getSession().setAttribute(attribute, idsAtuais + idDocumento+",");
		}
        
        /**
        * Obtém o nome do usuário juntado no {@link ProcessoDocumento}.
        *
        * @param processoDocumento
        *
        * @return nome do usuário + sua localização se existir.
        */
       public String obterNomeUsuarioJuntada(ProcessoDocumento processoDocumento) {
           StringBuilder nomeBuilder = new StringBuilder();

           if (processoDocumento.getNomeUsuarioJuntada() != null) {
               if (processoDocumento.getNomeUsuarioJuntada() != null) {
                   nomeBuilder.append(processoDocumento.getNomeUsuarioJuntada());
               }

               if (processoDocumento.getLocalizacaoJuntada() != null) {
                   StringUtil.adicionarHifen(nomeBuilder);
                   nomeBuilder.append(processoDocumento.getLocalizacaoJuntada());
               }
           } else if (processoDocumento.getNomeUsuario() != null) {
               nomeBuilder.append(processoDocumento.getNomeUsuario());
           }

           return nomeBuilder.toString();
       }
		
		private List<DadosAssinatura> carregarAssinaturas(List<ProcessoDocumentoBinPessoaAssinatura> assinaturas) {
			List<DadosAssinatura> dadosAssinaturaList = new ArrayList<DadosAssinatura>(0);
			for (ProcessoDocumentoBinPessoaAssinatura assinatura : assinaturas) {
				if (AssinaturaUtil.isModoTeste(assinatura.getAssinatura())) {
					DadosAssinatura da = new DadosAssinatura();
					da.nome = assinatura.getNomePessoa();
					da.commonName = "Assinatura de teste";
					da.dataAssinatura = DateUtil.dateToString(assinatura.getDataAssinatura(), "dd/MM/yyyy HH:mm:ss");
					da.assinatura = assinatura.getAssinatura();
					da.certChain = assinatura.getCertChain();
					da.issuer = "PJe em teste";
					dadosAssinaturaList.add(da);
				} else {
					try {
						Certificate[] cert = SigningUtilities.getCertChain(assinatura.getCertChain());
						DadosAssinatura da = new DadosAssinatura();
						da.commonName = ((X509Certificate) cert[0]).getSubjectX500Principal().getName();
						da.dataAssinatura = DateUtil.dateToString(assinatura.getDataAssinatura(), "dd/MM/yyyy HH:mm:ss");
						da.assinatura = assinatura.getAssinatura();
						da.certChain = assinatura.getCertChain();
						da.certificate = (X509Certificate) cert[0];
						da.issuer = da.certificate.getIssuerX500Principal().getName();
						da.nome = assinatura.getNomePessoa();
						dadosAssinaturaList.add(da);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			return dadosAssinaturaList;
		}
        
		public class DadosAssinatura {

			public String nome;
			public String commonName;
			public String cadastroMF;
			public String assinatura;
			public String dataAssinatura;
			public String certChain;
			public String issuer;
			public X509Certificate certificate;

			public String getNome() {
				return nome;
			}

			public void setNome(String nome) {
				this.nome = nome;
			}

			public String getCommonName() {
				return commonName;
			}

			public void setCommonName(String commonName) {
				this.commonName = commonName;
			}

			public String getCadastroMF() {
				return cadastroMF;
			}

			public void setCadastroMF(String cadastroMF) {
				this.cadastroMF = cadastroMF;
			}

			public String getDataAssinatura() {
				return dataAssinatura;
			}

			public void setDataAssinatura(String dataAssinatura) {
				this.dataAssinatura = dataAssinatura;
			}

			public X509Certificate getCertificate() {
				return certificate;
			}

			public void setCertificate(X509Certificate certificate) {
				this.certificate = certificate;
			}

			public String getAssinatura() {
				return assinatura;
			}

			public void setAssinatura(String assinatura) {
				this.assinatura = assinatura;
			}

			public String getCertChain() {
				return certChain;
			}

			public void setCertChain(String certChain) {
				this.certChain = certChain;
			}

			public String getIssuer() {
				return issuer;
			}

			public void setIssuer(String issuer) {
				this.issuer = issuer;
			}

		}

		public class Conteudo {
			private int idProcesso;
			private int idDocumento;
			private int idBinario;
			private String numeroDocumento;
			private String nomeDocumento;
			private String textoDocumento;
			private Date dataDocumento;
			private String dataDocumentoFormatada;
			private String arquivo;
			private String extensao;
			private String nomeUsuario;
			private int idDocumentoFavorito;
			private boolean sigilo;
			private String numeroStorage;
			private List<DadosAssinatura> assinaturas = new ArrayList<DadosAssinatura>(0);

			public int getIdProcesso() {
				return idProcesso;
			}
			
			protected void setIdProcesso(int idProcesso) {
				this.idProcesso = idProcesso;
			}
			
			public int getIdDocumento() {
				return idDocumento;
			}
			
			protected void setIdDocumento(int idDocumento) {
				this.idDocumento = idDocumento;
			}
			
			public int getIdBinario() {
				return idBinario;
			}

			public void setIdBinario(int idBinario) {
				this.idBinario = idBinario;
			}

			public String getNomeDocumento() {
				return nomeDocumento;
			}
			
			protected void setNomeDocumento(String nomeDocumento) {
				this.nomeDocumento = nomeDocumento;
			}

			public String getTextoDocumento() {
				return textoDocumento;
			}
			
			protected void setTextoDocumento(String textoDocumento) {
				this.textoDocumento = textoDocumento;
			}

			public Date getDataDocumento() {
				return dataDocumento;
			}
			
			protected void setDataDocumento(Date dataDocumento) {
				this.dataDocumento = dataDocumento;
			}
			
			public String getArquivo() {
				return arquivo;
			}
			
			protected void setArquivo(String arquivo) {
				this.arquivo = arquivo;
			}

			public String getExtensao() {
				return extensao;
			}

			protected void setExtensao(String extensao) {
				this.extensao = extensao;
			}

			public int getIdDocumentoFavorito() {
				return idDocumentoFavorito;
			}

			protected void setIdDocumentoFavorito(int idDocumentoFavorito) {
				this.idDocumentoFavorito = idDocumentoFavorito;
			}
			
			public boolean isFavorito() {
				return (this.idDocumentoFavorito > 0);
			}

			public String getNomeUsuario() {
				return nomeUsuario;
			}

			protected void setNomeUsuario(String nomeUsuario) {
				this.nomeUsuario = nomeUsuario;
			}

			public String getDataDocumentoFormatada() {
				return dataDocumentoFormatada;
			}

			protected void setDataDocumentoFormatada(String dataDocumentoFormatada) {
				this.dataDocumentoFormatada = dataDocumentoFormatada;
			}

			public String getNumeroDocumento() {
				return numeroDocumento;
			}

			protected void setNumeroDocumento(String numeroDocumento) {
				this.numeroDocumento = numeroDocumento;
			}

			public List<DadosAssinatura> getAssinaturas() {
				return assinaturas;
			}

			public void setAssinaturas(List<DadosAssinatura> assinaturas) {
				this.assinaturas = assinaturas;
			}

			public boolean isSigilo() {
				return sigilo;
			}

			public void setSigilo(boolean sigilo) {
				this.sigilo = sigilo;
			}
			
			public String getNumeroStorage() {
				return numeroStorage;
			}

			public void setNumeroStorage(String numeroStorage) {
				this.numeroStorage = numeroStorage;
			}
		}
	}

	public class ParametroDownload {
		private Integer idDocumentoInicio;
		private Integer idDocumentoFim;
		private Date dataInicio;
		private Date dataFim;
		private Integer idTipoDocumento;
		private Order cronologia = Order.DESC;

		public Integer getIdDocumentoInicio() {
			return idDocumentoInicio;
		}
		
		public void setIdDocumentoInicio(Integer idDocumentoInicio) {
			this.idDocumentoInicio = idDocumentoInicio;
		}
		
		public Integer getIdDocumentoFim() {
			return idDocumentoFim;
		}
		
		public void setIdDocumentoFim(Integer idDocumentoFim) {
			this.idDocumentoFim = idDocumentoFim;
		}
		
		public Date getDataInicio() {
			return dataInicio;
		}
		
		public void setDataInicio(Date dataInicio) {
			this.dataInicio = dataInicio;
		}
		
		public Date getDataFim() {
			return dataFim;
		}
		
		public void setDataFim(Date dataFim) {
			this.dataFim = dataFim;
		}

		public Integer getIdTipoDocumento() {
			return idTipoDocumento;
		}

		public void setIdTipoDocumento(Integer idTipoDocumento) {
			this.idTipoDocumento = idTipoDocumento;
		}
		
		public Order getCronologia() {
			return cronologia;
		}

		public void setCronologia(Order cronologia) {
			this.cronologia = cronologia;
		}
	}
	
	private class ListAutoProcessual extends ListPaginada<AutoProcessualDTO>{
		private ListProcessoCompletoBetaManager manager;
		
		public ListAutoProcessual(ListProcessoCompletoBetaManager manager, Search search) {
			this.manager = manager;
			super.setSearch(search);
		}

		@Override
		public List<AutoProcessualDTO> list(Search search) {
			return manager.recuperarAutos(processoSelecionado.getIdProcessoTrf(), exibirDocumentos, exibirMovimentos, search);
		}

		public List<AutoProcessualDTO> listAll(Search search) {
			return manager.recuperarTodosAutos(processoSelecionado.getIdProcessoTrf(), exibirDocumentos, exibirMovimentos, search);
		}
		@Override
		public Long count() {
			return manager.countAutos(processoSelecionado.getIdProcessoTrf(), exibirDocumentos, exibirMovimentos, super.getSearch());
		}
	}

	@Create
	public void init() {
		recuperarProcesso();
		recuperarPolosProcesso();
		recuperarTipoPolos();
		if(isExibeAlertasProcesso()){
			recuperarAlertas();
		}
		recuperarEtiquetasProcesso();
		recuperarPendencias();
		paginarTimeline();
		if(processoSelecionado != null && processoSelecionado.getProcessoStatus().equals(ProcessoStatusEnum.D)){
			carregarPaginador();
		}
		
		ProcessoTrfManager processoTrfManager = ComponentUtil.getComponent(ProcessoTrfManager.class);
		this.setPessoaJuizoDoProcesso(processoTrfManager.isPessoaLogadaJuizoProcesso(this.getProcessoSelecionado()));
		this.setProcessoDeslocadoParaLocalizacaoPessoa(processoTrfManager.isProcessoDeslocadoParaLocalizacaoPessoaLogada(this.getProcessoSelecionado()));
		
		if (this.aba != null) {
			this.tab = aba;
		}

	//	carregarLinksPersonalizaveis();
		inicializarAtributoProcessoForaDeTramitacao();
		atualizaEtiquetasUsuario();
	}
	
	private void carregarPaginador() {
		Long totalDocumentos = ComponentUtil.getComponent(ListProcessoCompletoBetaManager.class).countAutos(processoSelecionado.getIdProcessoTrf(), true, false, null);
		paginador = new PaginadorDocumentos(this.processoSelecionado.getIdProcessoTrf(), totalDocumentos.intValue());
		paginador.ultimoPrincipal();
	}
	
	public void inverterOrdenacao() {
		orderBy = orderBy.equals(Order.DESC) ? Order.ASC : Order.DESC;
		pesquisar();
	}

	private List<Criteria> getCriteriosPesquisa() {
		List<Criteria> criterias = new ArrayList<Criteria>(0);
		if(textoPesquisa != null && textoPesquisa.trim().length() > 0) {
			try {
				criterias.add(Criteria.or(
						Criteria.contains("documento.processoDocumento", textoPesquisa),
						Criteria.contains("documento.documentoPrincipal", textoPesquisa),
						Criteria.contains("documento.tipoProcessoDocumento.tipoProcessoDocumento", textoPesquisa),
						Criteria.contains("movimento.textoFinalExterno", textoPesquisa),
						Criteria.startsWith("documento.idProcessoDocumento", textoPesquisa.trim())));
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		return criterias;
	}
	
	public void pesquisar() {
		timeLineAutosDigitais = null;
		setPaginaAtual(0);
		paginarTimeline();
	}
	
	public void pesquisarTag() {
		if (this.textoPesquisaTag != null) {
			setEtiquetasUsuario(listasEtiquetasUsuario(this.textoPesquisaTag));
			inicializaEtiquetasCheck();
			
		}
		
	}
	
	private void processarMapDatas() {
		mapDatas = new LinkedHashMap<String, List<TimeLineAutosDigitais>>();
		if(!timeLineAutosDigitais.isEmpty()){
			gerarListaAgrupada(timeLineAutosDigitais);
		}
	}
	
	private void gerarListaAgrupada(List<TimeLineAutosDigitais> timeLineAutosDigitais){
		List<TimeLineAutosDigitais> listaPorData = new LinkedList<TimeLineAutosDigitais>();
		removerDocumentosDuplicados(timeLineAutosDigitais);
		Date dataAtual;
		Date dataLaco;
		String i = "";
		if(!timeLineAutosDigitais.isEmpty()) {
			
			 dataAtual = DateUtil.getDataSemHora(timeLineAutosDigitais.get(0).getData());
			 dataLaco = new Date();
			for(TimeLineAutosDigitais time : timeLineAutosDigitais){
				
				dataLaco = DateUtil.getDataSemHora(time.getData());
				if(!dataLaco.equals(dataAtual)){
					i = i + " ";
					mapDatas.put(DateUtil.dateToString(dataAtual,"dd MMM yyyy") + i, listaPorData);
					dataAtual = dataLaco;
					listaPorData = new LinkedList<TimeLineAutosDigitais>();
				}
				listaPorData.add(time);
			}
			
			mapDatas.put(DateUtil.dateToString(dataLaco,"dd MMM yyyy") +  i, listaPorData);
		}

	}

	public void removerDocumentosDuplicados(List<TimeLineAutosDigitais> timeLineAutosDigitais) {
		Set<Integer> idsProcessoDocumentos = new HashSet<>();
		Iterator<TimeLineAutosDigitais> iterator = timeLineAutosDigitais.iterator();
		String codigoDocumentoDestaque = ParametroUtil.getParametro("codigoDocumentoDestaque");
		List<Integer> codigoDocumentoDestaqueList = null;
		if (codigoDocumentoDestaque != null && !codigoDocumentoDestaque.isEmpty()) {
			codigoDocumentoDestaqueList = CollectionUtilsPje.convertStringToIntegerList(codigoDocumentoDestaque);
		}
		while (iterator.hasNext()) {

			TimeLineAutosDigitais item = iterator.next();
			if (!idsProcessoDocumentos.add(item.getId()) && item.getTipoDadoEnum().equals(TipoDadoENUM.D)
					&& codigoDocumentoDestaqueList != null
					&& codigoDocumentoDestaqueList.contains(item.getIdTipoProcessoDocumento())) {
				iterator.remove(); // Já vimos esse ID, então remove
			}
		}
	}

	private void recuperarAutos(int pagina, List<Criteria> criterias) {
		Search search = new Search(AutoProcessualDTO.class);
		search.setMax(50);
		search.setFirst(calcularPosicao(pagina,50));
		search.addOrder("documento.dataJuntada", orderBy);
		if(criterias != null && criterias.size() > 0) {
			try {
				search.addCriteria(criterias);
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}
		}
		ListAutoProcessual list = new ListAutoProcessual(ComponentUtil.getComponent(ListProcessoCompletoBetaManager.class),search);
		setarAutos(list);
	}

	public void paginarTimeline() {
		if(getPaginaAtual() <= getCountPaginas()){
			List<Criteria> criterias = getCriteriosPesquisa();
			recuperarAutos(getPaginaAtual(), criterias);
			processarMapDatas();
			setPaginaAtual(getPaginaAtual() + 1);
		}
	}

	private void setarAutos(ListAutoProcessual autos) {
		if(timeLineAutosDigitais == null || timeLineAutosDigitais.size() == 0) {
			timeLineAutosDigitais = new LinkedList<TimeLineAutosDigitais>();
			countPaginas = autos.getPageCount(autos.count());
		}
		List<AutoProcessualDTO> autosPaginados = new ArrayList<AutoProcessualDTO>(0);
		List<AutoProcessualDTO> listaCompleta = new ArrayList<AutoProcessualDTO>();
		
		String codigoDocumentoDestaque = ParametroUtil.getParametro("codigoDocumentoDestaque");
		
		if(Objects.nonNull(codigoDocumentoDestaque) && !"".equals(codigoDocumentoDestaque)) {
			List<Integer> listaDocumentos = Stream.of(codigoDocumentoDestaque.split(","))
					  .map(String::trim)
					  .map(Integer::parseInt)
					  .collect(Collectors.toList());
			if(getCountPaginas() > 0) {
				autosPaginados = autos.ListPaginada();
				
				List<AutoProcessualDTO> autosCompleto = autos.listAll(autos.getSearch())
						.stream()
						.filter(a -> listaDocumentos.contains(a.getDocumento() != null && a.getDocumento().getTipoProcessoDocumento().getIdTipoProcessoDocumento() != null ? a.getDocumento().getTipoProcessoDocumento().getIdTipoProcessoDocumento() : 0))
						.sorted(Comparator.comparing(a -> listaDocumentos.indexOf(a.getDocumento() != null && a.getDocumento().getTipoProcessoDocumento().getIdTipoProcessoDocumento() != null ? a.getDocumento().getTipoProcessoDocumento().getIdTipoProcessoDocumento() : 0)))
						.collect(Collectors.toList());
				
				List<AutoProcessualDTO> autosPaginadosRemove = autosPaginados
						.stream()
						.filter(a -> !listaDocumentos.contains(a.getDocumento() != null && a.getDocumento().getTipoProcessoDocumento().getIdTipoProcessoDocumento() != null ? a.getDocumento().getTipoProcessoDocumento().getIdTipoProcessoDocumento() : 0))
						.collect(Collectors.toList());
				
				listaCompleta.addAll(autosCompleto);
				
				for(AutoProcessualDTO auto : autosPaginadosRemove) {
							listaCompleta.add(auto);
							
					}
			
				}
			
				for(AutoProcessualDTO auto : listaCompleta) {
					if(auto.getDocumento() != null) {
						setarDocumento(auto.getDocumento());
					}
					else {
						setarMovimento(auto.getMovimento());
					}
				}
		  }else {
				if(getCountPaginas() > 0) {
					autosPaginados = autos.ListPaginada();
					
					for(AutoProcessualDTO auto : autosPaginados) {
						if(auto.getDocumento() != null) {
							setarDocumento(auto.getDocumento());
						}
						else {
							setarMovimento(auto.getMovimento());
						}
					}
				
				}
			  
		  }

			
	}




	private void setarDocumento(ProcessoDocumento documento) {
		List<TimeLineAutosDigitais> documentosVinculados = new LinkedList<TimeLineAutosDigitais>();
		List<TimeLineAutosDigitais> movimentosVinculados = new LinkedList<TimeLineAutosDigitais>();

		TimeLineAutosDigitais timeLineAutosDigitais = null;

		for(ProcessoEvento movimento : documento.getProcessoEventoList()){
			TimeLineAutosDigitais timeLineMovimento = new TimeLineAutosDigitais(TipoDadoENUM.M, 
																				processarNomeMovimento(movimento), 
																				movimento.getDataAtualizacao(), 
																				movimento.getIdProcessoEvento(), 
																				TipoOrigemAcaoEnum.I, 
																				null, 
																				movimento.getAtivo(),
																				false);
			timeLineMovimento.setIdProcessoDocumento(documento.getIdProcessoDocumento());
			movimentosVinculados.add(timeLineMovimento);
		}

		if(documento.getDocumentosVinculados().size() > 0){
			for(ProcessoDocumento documentoVinculado : documento.getDocumentosVinculados()){
				ProcessoDocumentoBin bin = documentoVinculado.getProcessoDocumentoBin();
				String extensao = (bin.getNomeArquivo() != null && bin.getNomeArquivo().toLowerCase().endsWith(".pdf") ? "application/pdf" : bin.getExtensao());
				TimeLineAutosDigitais timeLineDocumento = new TimeLineAutosDigitais(TipoDadoENUM.D, 
																					processarNomeDocumento(documentoVinculado), 
																					documentoVinculado.getDataJuntada(), 
																					documentoVinculado.getIdProcessoDocumento(),
																					DocumentoJudicialService.instance().obterTipoOrigemJuntada(documentoVinculado),
																					bin.getExtensao() == null ? "text/html" : extensao, 
																					documentoVinculado.getAtivo(),
																					documentoVinculado.getDocumentoSigiloso());
				timeLineDocumento.setIdTipoProcessoDocumento(documentoVinculado.getTipoProcessoDocumento().getIdTipoProcessoDocumento());
				documentosVinculados.add(timeLineDocumento);
			}
		}
		
		ProcessoDocumentoBin bin = documento.getProcessoDocumentoBin();
		String extensao = (bin.getNomeArquivo() != null && bin.getNomeArquivo().toLowerCase().endsWith(".pdf") ? "application/pdf" : bin.getExtensao());
		timeLineAutosDigitais = new TimeLineAutosDigitais(TipoDadoENUM.D, 
														  processarNomeDocumento(documento), 
														  documento.getDataJuntada(), 
														  documento.getIdProcessoDocumento(), 
														  DocumentoJudicialService.instance().obterTipoOrigemJuntada(documento),
														  bin.getExtensao() == null ? "text/html" : extensao, 
														  documento.getAtivo(),
														  documento.getDocumentoSigiloso(),
														  documentosVinculados, 
														  movimentosVinculados,
														  !DocumentoJudicialService.instance().mostrarDocumentoComoNaoLido(documento, this.processoSelecionado));
		timeLineAutosDigitais.setIdTipoProcessoDocumento(documento.getTipoProcessoDocumento().getIdTipoProcessoDocumento());
		timeLineAutosDigitais.setIdProcessoDocumento(documento.getIdProcessoDocumento());

		this.timeLineAutosDigitais.add(timeLineAutosDigitais);
	}
	
	private int calcularPosicao(int pagina, int registroPorPagina) {
		return pagina * registroPorPagina;
	}

	/**
	 * Processa as informações para exibição do nome do Documento.
	 * @param procDoc
	 * @return
	 */
	public String processarNomeDocumento(ProcessoDocumento documento){
		StringBuilder sb = new StringBuilder();
		Integer id = documento.getIdProcessoDocumento();
		String texto = documento.getProcessoDocumento();
		String tipo = documento.getTipoProcessoDocumento().getTipoProcessoDocumento();
		
		if(StringUtil.isNotEmpty(texto)){
			texto = texto.replaceAll("_", " ").replaceAll("-", " ");
			sb.append(id);
			sb.append(" - ");
			if(texto.equalsIgnoreCase(tipo)) {
				sb.append(texto);
			}
			else {
				sb.append(tipo);
				sb.append(" (");
				sb.append(texto);
				sb.append(")");
			}
		}
		return sb.toString();
	}
	
	/**
	 * Processa as informações para exibição do nome do Movimento.
	 * @param procDoc
	 * @return
	 */
	public String processarNomeMovimento(ProcessoEvento movimento){
		return movimento == null ? StringUtils.EMPTY : 
			(Authenticator.isUsuarioInterno() ? movimento.getTextoFinalInterno() : movimento.getTextoFinalExterno());
	}

	public PaginadorDocumentos getPaginador() {
		return paginador;
	}

	public boolean isExibirMovimentos() {
		return exibirMovimentos;
	}

	public void setExibirMovimentos(boolean exibirMovimentos) {
		this.exibirMovimentos = exibirMovimentos;
	}

	public boolean isExibirDocumentos() {
		return exibirDocumentos;
	}

	public void setExibirDocumentos(boolean exibirDocumentos) {
		this.exibirDocumentos = exibirDocumentos;
	}

	public int getCountPaginas() {
		return countPaginas;
	}

	public int getPaginaAtual() {
		return paginaAtual;
	}

	private void setPaginaAtual(int paginaAtual) {
		this.paginaAtual = paginaAtual;
	}

	private void setarMovimento(ProcessoEvento movimento) {
		TimeLineAutosDigitais timeLineAutosDigitais = new TimeLineAutosDigitais(TipoDadoENUM.M, 
																				processarNomeMovimento(movimento), 
																				movimento.getDataAtualizacao(), 
																				movimento.getIdProcessoEvento(), 
																				TipoOrigemAcaoEnum.I, 
																				null, 
																				movimento.getAtivo(),
																				false);
		this.timeLineAutosDigitais.add(timeLineAutosDigitais);
	}

	/**
	 * Recupera cada Tipo do Polo de acordo com 
	 * cada regra de negócio estabelecida na
	 * templeteInformaçõesProcesso.xhtml
	 */
	private void recuperarTipoPolos() {
		recuperarParteSemVinculacao();
		recuperarRepresentantes();
	}

	private void recuperarAlertas() {
		setAlertasProcesso(ComponentUtil.getComponent(ProcessoAlertaManager.class).getAlertasProcesso(processoSelecionado));
	}

	private void recuperarEtiquetasProcesso() {
		Long idProcesso = new Long(processoSelecionado.getIdProcessoTrf());
		Integer idLocalizacao = Authenticator.getIdLocalizacaoFisicaAtual();
		String nomeTag = null;
		setEtiquetasProcesso(ComponentUtil.getComponent(ProcessoTagManager.class).listarTags(idProcesso, idLocalizacao, nomeTag));
	}
	
	public void removerEtiquetaProcesso(Integer idTag) {
		try{
			Long idProcesso = new Long(processoSelecionado.getIdProcessoTrf());
			ComponentUtil.getComponent(ProcessoTagManager.class).removerTag(new Long(idProcesso), idTag);
			recuperarEtiquetasProcesso();
			setExibirPopupTags(true);
			facesMessages.clear();
			facesMessages.addFromResourceBundle(Severity.INFO, "Alerta_deleted");
		}catch(Exception e){
			logger.error(e.getLocalizedMessage(), e);
			facesMessages.addFromResourceBundle(Severity.ERROR, "alerta.erroAoRealizarOperacao");
			e.printStackTrace();
		}
		
	}
	
	private List<TagMin> listasEtiquetasUsuario(String nomeEtiqueta) {
		CriterioPesquisa crit = new CriterioPesquisa();
		crit.setTagsString(nomeEtiqueta);
		List<TagMin> tags;
		Integer idLocalizacao = Authenticator.getIdLocalizacaoFisicaAtual();
		Integer idOrgaoJulgadorColegiado = Authenticator.getIdOrgaoJulgadorColegiadoAtual();
		tags = ComponentUtil.getComponent(ProcessoTagManager.class).listarTagsUsuario(crit, idLocalizacao);
		return tags;
	}
	
	private EtiquetaProcesso criarProcessotag(TagMin tag) {
		Long idProcesso = new Long(processoSelecionado.getIdProcessoTrf());
		String nomeTag = tag.getNomeTag();
		Usuario usr = Authenticator.getUsuarioLogado();
		Integer idLocalizacao = Authenticator.getIdLocalizacaoFisicaAtual();
		Integer idOrgaoJulgadorColegiado = Authenticator.getIdOrgaoJulgadorColegiadoAtual();
		try {
			ProcessoTag t = ComponentUtil.getComponent(ProcessoTagManager.class).criarProcessoTag(idProcesso, nomeTag, idLocalizacao ,
					usr.getIdUsuario());	
			//processoTagManager.flush();
			return new EtiquetaProcesso(t.getTag().getId(), t.getTag().getNomeTag(),
					t.getIdUsuarioInclusao(), idProcesso);
		} catch (PJeBusinessException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
		return null;
	}

	public void adicionarTagsProcesso() {
		try {
			if (this.etiquetasCheck.containsValue(Boolean.TRUE)) {
				
				for (Map.Entry<TagMin, Boolean> entry : this.etiquetasCheck.entrySet()) {
					if (entry.getValue() == true) {
						criarProcessotag(entry.getKey());
					}
				}
				this.etiquetasCheck = new LinkedHashMap<TagMin, Boolean>(0);
			}
			facesMessages.clear();
			facesMessages.addFromResourceBundle(Severity.INFO, "etiqueta_created");
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			facesMessages.addFromResourceBundle(Severity.ERROR, "alerta.erroAoRealizarOperacao");
			e.printStackTrace();
		}
		
	}
	
	private void inicializaEtiquetasCheck() {
		this.etiquetasCheck = new LinkedHashMap<TagMin, Boolean>(0);
		for(TagMin etiqueta : this.etiquetasUsuario){
			this.etiquetasCheck.put(etiqueta, false);
		}
	}
	
	private void atualizaEtiquetasUsuario() {
		this.setEtiquetasUsuario(listasEtiquetasUsuario(null));
	}
	
	
	/**
	 * Método responsável por verificar se algum componente checkbox da lista está selecionado.
	 * 
	 * @return Verdadeiro se algum componente checkbox da lista está selecionado. Falso, caso contrário.
	 */
	public boolean verificarCheck(Map<Object, Boolean> map) {
		for (Map.Entry<Object, Boolean> entry : map.entrySet()) {
			if (entry.getValue() == true) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Verifica se o usuario tem permissão para acessar os alertas do processo de acordo com as regras internas.
	 * 
	 * @return true se exibi os alertas do processo
	 */
	public boolean isExibeAlertasProcesso() {
		boolean retorno = ComponentUtil.getComponent(ProcessoAlertaManager.class).exibirAlertasProcesso(getProcessoSelecionado());
		return retorno;
	}
	
	public void inativarAlertaProcesso(ProcessoAlerta processoAlerta) {
		ComponentUtil.getComponent(ProcessoAlertaManager.class).inativarAlertaProcesso(processoAlerta);
		recuperarAlertas();
	}
	
	public void inativarTodosAlertasProcesso() {
		ComponentUtil.getComponent(ProcessoAlertaManager.class).inativarTodosAlertasProcesso(processoSelecionado);
		recuperarAlertas();
	}

	public void excluirPendenciaProcesso(Pendencia pendencia) {
		Iterator<Pendencia> it = pendenciasProcesso.iterator();
		while(it.hasNext()){
			Pendencia p = it.next();
			if(p.getIdPendencia().equals(pendencia.getIdPendencia())){
				it.remove();
				excluirPendenciaProcessoBanco(pendencia);
			}
		}
	}
	
	private void excluirPendenciaProcessoBanco(Pendencia pendencia) {
		switch (pendencia.getTipo()) {
			case T:
				try {
					getProcessoSelecionado().setApreciadoTutelaLiminar(Boolean.TRUE);
					ComponentUtil.getComponent(ProcessoJudicialManager.class).persistAndFlush(getProcessoSelecionado());
				} catch (PJeBusinessException e) {
					e.printStackTrace();
				}
				break;
				
			case J:
				try {
					getProcessoSelecionado().setApreciadoJusticaGratuita(Boolean.TRUE);
					ComponentUtil.getComponent(ProcessoJudicialManager.class).persistAndFlush(getProcessoSelecionado());
				} catch (PJeBusinessException e) {
					e.printStackTrace();
				}
				break;
	
			case S:
				try {
					getProcessoSelecionado().setApreciadoSegredo(ProcessoTrfApreciadoEnum.S);
					ComponentUtil.getComponent(ProcessoJudicialManager.class).persistAndFlush(getProcessoSelecionado());
				} catch (PJeBusinessException e) {
					e.printStackTrace();
				}
				break;
	
			case D:
				try {
					getProcessoSelecionado().setApreciadoSigilo(ProcessoTrfApreciadoEnum.S);
					ComponentUtil.getComponent(ProcessoJudicialManager.class).persistAndFlush(getProcessoSelecionado());
				} catch (PJeBusinessException e) {
					e.printStackTrace();
				}
				break;
	
			case P:
				ProcessoDocumentoPeticaoNaoLidaManager processoDocumentoPeticaoNaoLidaManager = ComponentUtil.getComponent(ProcessoDocumentoPeticaoNaoLidaManager.class);
				List<ProcessoDocumentoPeticaoNaoLida> list = processoDocumentoPeticaoNaoLidaManager.obterProcessoDocumentoPeticaoNaoLida(getProcessoSelecionado());
				for (ProcessoDocumentoPeticaoNaoLida peticao : list) {
					peticao.setRetirado(Boolean.TRUE);
					try {
						processoDocumentoPeticaoNaoLidaManager.persistAndFlush(peticao);
					} catch (PJeBusinessException e) {
						e.printStackTrace();
					}
				}
				break;
	
			default:
				break;
		}
	}

	public void excluirTodasPendenciasProcesso() {
		for (Pendencia pendencia : pendenciasProcesso) {
			excluirPendenciaProcessoBanco(pendencia);
		}
		pendenciasProcesso = new ArrayList<Pendencia>();
	}
	
	/**
	 * Retorna o Set com as partes principais do processo.
	 */
	private void recuperarParteSemVinculacao() {
		recuperarParteSemVinculacao(ProcessoParteParticipacaoEnum.A,poloAtivo,parteSemVinculacaoAtivo);
		recuperarParteSemVinculacao(ProcessoParteParticipacaoEnum.P,poloPassivo,parteSemVinculacaoPassivo);
		recuperarParteSemVinculacao(ProcessoParteParticipacaoEnum.T,poloOutrosInterresados,parteSemVinculacaoOutrosInteressados);
	}
	
	/**
	 * Retorna o Set com os representantes das partes
	 */
	private void recuperarRepresentantes(){
		recuperarRepresentantes(ProcessoParteParticipacaoEnum.A,parteSemVinculacaoAtivo,representantesAtivos);
		recuperarRepresentantes(ProcessoParteParticipacaoEnum.P,parteSemVinculacaoPassivo,representantesPassivo);
		recuperarRepresentantes(ProcessoParteParticipacaoEnum.T,parteSemVinculacaoOutrosInteressados,representantesOutrosInteressados);
	}
	

	/**
	 * Recupera e seta as informações da parte sem vinculação
	 * @param processoParteParticipacaoEnum
	 * @param processoPartePrincipal
	 */
	private void recuperarParteSemVinculacao(ProcessoParteParticipacaoEnum processoParteParticipacaoEnum,List<ProcessoParte> listaPolo, Set<ProcessoParteVO> processoPartePrincipal) {
		ProcessoParteManager processoParteManager = ComponentUtil.getComponent(ProcessoParteManager.class);
		String processosRestricao = ParametroUtil.getParametro("tjrj:download:restricao:processo");
		if (processosRestricao != null && processosRestricao.contains(getProcessoSelecionado().getNumeroProcesso())) {
			int count = 0;
			String totalS = ParametroUtil.getParametro("tjrj:limite:partes");
			int total = totalS == null ? 3 : Integer.parseInt(totalS);
			for(ProcessoParte polo: listaPolo){
				if(polo.getPartePrincipal()){
					ProcessoParteVO processoParte = new ProcessoParteVO();
					processoParte.setNomeParteDetalhes(retornarNomeParte(polo,processoParteParticipacaoEnum,NOME_DETALHES));
					processoParte.setNomeParte(retornarNomeParte(polo, processoParteParticipacaoEnum, NOME));
					processoParte.setProcessoParte(polo);
					processoParte.setRepresentante(Boolean.FALSE);
					processoParte.setProcuradoria(Boolean.FALSE);
					processoParte.setIdRepresentado(polo.getIdProcessoParte());
					processoParte.setPodeVisualizar(processoParteManager.podeVisualizarNomeDoPolo(polo));
					processoPartePrincipal.add(processoParte);
					count++;
					if (count >= total) {
						return;
					}
				}
			}
			return;
		}
		for(ProcessoParte polo: listaPolo){
			if(polo.getPartePrincipal()){
				ProcessoParteVO processoParte = new ProcessoParteVO();
				processoParte.setNomeParteDetalhes(retornarNomeParte(polo,processoParteParticipacaoEnum,NOME_DETALHES));
				processoParte.setNomeParte(retornarNomeParte(polo, processoParteParticipacaoEnum, NOME));
				processoParte.setProcessoParte(polo);
				processoParte.setRepresentante(Boolean.FALSE);
				processoParte.setProcuradoria(Boolean.FALSE);
				processoParte.setIdRepresentado(polo.getIdProcessoParte());
				processoParte.setPodeVisualizar(processoParteManager.podeVisualizarNomeDoPolo(polo));
				processoPartePrincipal.add(processoParte);
			}
		}
	}

	/**
	 * Recupera e seta os representantes
	 * @param representantes 
	 * @param Tipo do polo
	 * @param lista
	 */
	private void recuperarRepresentantes(ProcessoParteParticipacaoEnum tipoParte,Set<ProcessoParteVO> partePrincipal, Set<ProcessoParteVO> representantes) {
		String processosRestricao = ParametroUtil.getParametro("tjrj:download:restricao:processo");
		if (processosRestricao != null && processosRestricao.contains(getProcessoSelecionado().getNumeroProcesso())) {
			int count = 0;
			String totalS = ParametroUtil.getParametro("tjrj:limite:partes");
			int total = totalS == null ? 3 : Integer.parseInt(totalS);
			for(ProcessoParteVO p : partePrincipal){
				processarRepresentantes(p,representantes,tipoParte);
				count++;
				if (count >= total) {
					return;
				}
			}
			return;
		}
		for(ProcessoParteVO p : partePrincipal){
			processarRepresentantes(p,representantes,tipoParte);
		}
	}

	/**
	 * Processa os representates de acordo com a partePrincipal e o tipo da Parte.
	 * Depois seta os valores no set informado. 
	 * @param partePrincipal
	 * @param representantes
	 * @param tipoParte
	 */
	private void processarRepresentantes(ProcessoParteVO partePrincipal, Set<ProcessoParteVO> representantes, ProcessoParteParticipacaoEnum tipoParte) {
		List<ProcessoParte> rep = ComponentUtil.getComponent(ProcessoParteRepresentanteManager.class).recuperarRepresentantesParaExibicao(partePrincipal.getProcessoParte().getIdProcessoParte(), true);
		String processosRestricao = ParametroUtil.getParametro("tjrj:download:restricao:processo");
		if (processosRestricao != null && processosRestricao.contains(getProcessoSelecionado().getNumeroProcesso())) {
			int count = 0;
			String totalS = ParametroUtil.getParametro("tjrj:limite:partes");
			int total = totalS == null ? 3 : Integer.parseInt(totalS);
			for (ProcessoParte p : rep) {
				ProcessoParteVO processoParte = new ProcessoParteVO();
				processoParte.setNomeParteDetalhes(retornarNomeParte(p, tipoParte,NOME_DETALHES));
				processoParte.setNomeParte(retornarNomeParte(p, tipoParte, NOME));
				processoParte.setProcessoParte(p);
				processoParte.setRepresentante(true);
				if (isProcuradoria(processoParte.getProcessoParte().getProcuradoria())) {
					processoParte.setProcuradoria(true);
				}
				processoParte.setIdRepresentado(partePrincipal.getIdRepresentado());
				representantes.add(processoParte);
				count++;
				if (count >= total) {
					return;
				}
			}
			return;
		}
		for (ProcessoParte p : rep) {
			ProcessoParteVO processoParte = new ProcessoParteVO();
			processoParte.setNomeParteDetalhes(retornarNomeParte(p, tipoParte,NOME_DETALHES));
			processoParte.setNomeParte(retornarNomeParte(p, tipoParte, NOME));
			processoParte.setProcessoParte(p);
			processoParte.setRepresentante(true);
			if (isProcuradoria(processoParte.getProcessoParte().getProcuradoria())) {
				processoParte.setProcuradoria(true);
			}
			processoParte.setIdRepresentado(partePrincipal.getIdRepresentado());
			representantes.add(processoParte);
		}
	}
	

	/**
	 * Retonar o nome para devida apresentação de acordo com as regras da page listProcessoCompleto.xhtml
	 * @param polo
	 * @return
	 */
	private String retornarNomeParte(ProcessoParte polo, ProcessoParteParticipacaoEnum tipoPolo, String tipoNome){
		String nome = new String();
		if(tipoNome.equals(NOME_DETALHES)){
			nome = polo.getPessoa().isMenor() ? polo.getNomeParte() : polo.toString();
		}else{
			nome = polo.getPessoa() != null ? polo.getNomeParte() : StringUtils.EMPTY;
		}
		return nome;
	}
	
	public String recuperarPrimeirosNomesPolos() {
		String nomeAtivo = "Não definido";
		String nomePassivo =  "Não definido";
		
		for (ProcessoParteVO parte : parteSemVinculacaoAtivo) {
			if (parte.isPodeVisualizar()) {
				nomeAtivo = parte.getProcessoParte().getNomeParte();
				if(parte.getProcessoParte().getIsBaixado() || parte.getProcessoParte().getIsSuspenso()){
					nomeAtivo = "<span class=text-strike>"+ nomeAtivo +"</span>";
				}
			} else {
				nomeAtivo = "(Em segredo de justiça)";
			}
			break;
		}
		
		for (ProcessoParteVO parte : parteSemVinculacaoPassivo) {
			if (parte.isPodeVisualizar()) {
				nomePassivo = parte.getProcessoParte().getNomeParte();
				if(parte.getProcessoParte().getIsBaixado() || parte.getProcessoParte().getIsSuspenso()){
					nomePassivo = "<span class=text-strike>"+ nomePassivo +"</span>";
				}
			} else {
				nomePassivo = "(Em segredo de justiça)";
			}
			break;
		}
		
		return nomeAtivo + " X " + nomePassivo;
	}
	/**
	 * Recupera as informações de cada polo
	 */
	private void recuperarPolosProcesso() {
		try {
			List<ProcessoParte> partes = ComponentUtil.getComponent(ProcessoParteManager.class).recuperaPartesParaExibicao(processoSelecionado.getIdProcessoTrf(), true, 0, 0);
			for (ProcessoParte processoParte : partes) {
				if(processoParte.getInParticipacao().equals(ProcessoParteParticipacaoEnum.A)) {
					poloAtivo.add(processoParte);
				}
				else if (processoParte.getInParticipacao().equals(ProcessoParteParticipacaoEnum.P)) {
					poloPassivo.add(processoParte);
				}
				else {
					poloOutrosInterresados.add(processoParte);
				}
			}
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Recupera a entidade ProcessoTrf de acordo com o idProcessoTrf 
	 * Informado.
	 * @throws PJeBusinessException
	 */
	private void recuperarProcesso() {
		try {
			Integer idProcessoJudicial = idProcesso != null ? idProcesso : id;
			if(idProcessoJudicial != null){
				setProcessoSelecionado(ComponentUtil.getComponent(ProcessoJudicialManager.class).findById(idProcessoJudicial));
			}
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Recupera os representantes ativos da parte informada
	 * @param idRepresentado
	 * @return
	 */
	public Set<ProcessoParteVO> getRepresentantesAtivos(Integer idRepresentado){
		return processarRepresentantes(idRepresentado,representantesAtivos);
	}

	public List<ProcessoParte> getPartesRepresentantesAtivos(Integer idRepresentado) {
		List<ProcessoParte> representantes = new ArrayList<ProcessoParte>(0);
		Set<ProcessoParteVO> representantesVO = getRepresentantesAtivos(idRepresentado);
		for (ProcessoParteVO representanteVO : representantesVO) {
			representantes.add(representanteVO.getProcessoParte());
		}
		return representantes;
	}
	
	/**
	 * Recupera os representantes passivos da parte informada
	 * @param idRepresentado
	 * @return
	 */
	public Set<ProcessoParteVO> getRepresentantesPassivos(Integer idRepresentado){
		return processarRepresentantes(idRepresentado,representantesPassivo);
	}

	public List<ProcessoParte> getPartesRepresentantesPassivos(Integer idRepresentado) {
		List<ProcessoParte> representantes = new ArrayList<ProcessoParte>(0);
		Set<ProcessoParteVO> representantesVO = getRepresentantesPassivos(idRepresentado);
		for (ProcessoParteVO representanteVO : representantesVO) {
			representantes.add(representanteVO.getProcessoParte());
		}
		return representantes;
	}

	/**
	 * Recupera os representantes dos outros interessados da parte informada
	 * @param idRepresentado
	 * @return
	 */
	public Set<ProcessoParteVO> getRepresentantesOutrosInteressados(Integer idRepresentado){
		return processarRepresentantes(idRepresentado,representantesOutrosInteressados);
	}

	public List<ProcessoParte> getPartesRepresentantesOutrosInteressados(Integer idRepresentado) {
		List<ProcessoParte> representantes = new ArrayList<ProcessoParte>(0);
		Set<ProcessoParteVO> representantesVO = getRepresentantesOutrosInteressados(idRepresentado);
		for (ProcessoParteVO representanteVO : representantesVO) {
			representantes.add(representanteVO.getProcessoParte());
		}
		return representantes;
	}

	
	public LinkedHashSet<ProcessoDocumento> getDocumentosAbertos() {
		return documentosAbertos;
	}

	/**
	 * Processar os presentantes de acordo com o idRepresentado e o Set informado
	 * @param idRepresentado
	 * @param listaRepresentantes
	 * @return
	 */
	private Set<ProcessoParteVO> processarRepresentantes(Integer idRepresentado, TreeSet<ProcessoParteVO> listaRepresentantes) {
		Set<ProcessoParteVO> representantesParte = new TreeSet<ProcessoParteVO>();
		for(ProcessoParteVO representante : listaRepresentantes){
			if(representante.getIdRepresentado() == idRepresentado){
				representantesParte.add(representante);
			}
		}
		return representantesParte.isEmpty() ? new TreeSet<ProcessoParteVO>() : representantesParte;
	}
	
	/**
	 * Verifica se é necessário exibir os dados do relator.
	 * @return
	 */
	public boolean isExibeDadosRelator(){
		if(isInstanciaColegiada()) {
			if(!getMapMagistradosVinculados().isEmpty()){
				exibeDadosRelator = true;
			}
		}
		return exibeDadosRelator;
	}

	public boolean isInstanciaColegiada() {
		return !ParametroUtil.instance().isPrimeiroGrau();
	}

	/**
	 * Verifica se é necessário exibir o valor da causa.
	 * @return
	 */
	public boolean isExibeValorCausa() {
		if(ParametroJtUtil.instance().justicaEleitoral()){
			exibeValorCausa = false;
		}
		return exibeValorCausa;
	}
	
	/**
	 * Verifica se há procuradoria.
	 * @param procuradoria
	 * @return
	 */
	public boolean isProcuradoria(Procuradoria procuradoria) {
		return procuradoria != null;
	}
	
	/**
	 * Verifica a existência de outros interessados.
	 * @return
	 */
	public boolean isExibeOutrosInteressados(){
		if(!parteSemVinculacaoOutrosInteressados.isEmpty()){
			exibeOutrosInteressados = true;
		}
		return exibeOutrosInteressados;
	}
	
	/**
	 * Retorna os magistrados vinculados e seus relacionamento com o processo
	 */
	public Map<TipoAtuacaoDetalhadaMagistradoEnum, Entry<Integer, String>>getMapMagistradosVinculados() {
		if (mapMagistradosProcesso == null || mapMagistradosProcesso.isEmpty()) {
			mapMagistradosProcesso = ComponentUtil.getComponent(ProcessoMagistradoManager.class).obterAtuacaoDetalhadaMagistradosDoProcesso(processoSelecionado);
		}
		return mapMagistradosProcesso;
	}
	
	/**
	 * Retorna se há segredo de justiça
	 * @return
	 */
	public String getSegredoJustica(){
		return retornarStringResposta(getProcessoSelecionado().getSegredoJustica());
	}
	
	/**
	 * Retorna se a justiça foi gratuita 
	 * @return
	 */
	public String getJusticaGratuita(){
		return retornarStringResposta(getProcessoSelecionado().getJusticaGratuita());
	}
	
	/**
	 * Retorna se houve pedido de tutela ou limininar.
	 * @return
	 */
	public String getPedidoTutelaLiminar(){
		return retornarStringResposta(getProcessoSelecionado().getTutelaLiminar());
	}

	public String getPrioridade() {
		List<PrioridadeProcesso> prioridades = getProcessoSelecionado().getPrioridadeProcessoList();
		if (prioridades.size() > 0) {
			return prioridades.stream().map(Object::toString).collect(Collectors.joining(", "));
		} else {
			return "NÃO";
		}
	}

	/**
	 * Processa a resposta para exibição na tela
	 * @param atributo
	 * @return
	 */
	private String retornarStringResposta(Boolean atributo) {
		if(atributo.equals(Boolean.TRUE)){
			return "SIM";
		}
		return "NÃO";
	}
	
	public void processarTipoApresentacao(){
		if(documentoSelecionado!=null){
			exibeHtml = isPossuiModelo(documentoSelecionado);
			exibePdf = !exibeHtml;
		}
	}

	public ProcessoDocumento recuperarDocSelecionado(){
		return recuperarDocSelecionado(idProcDocSelecionado,false);
	}
	
	public ProcessoDocumento recuperarDocSelecionado(Integer id, boolean isSetar){
		if(id != 0 && isSetar){
			documentoSelecionado = mapDocumentos.get(id);
		}
		return mapDocumentos.get(id);
	}
	
	public ProcessoDocumento recuperaDocumentoSelecionado() throws PJeBusinessException{
		ProcessoDocumento processoDocumento = null;
		ProcessoDocumento processoDocumentoVinculado = null;
		if (paramIdProcessoDocumento != null){
			processoDocumento = ComponentUtil.getComponent(ProcessoDocumentoManager.class).findById(paramIdProcessoDocumento);
		} else if (idProcessoParteExpediente != null){
			ProcessoParteExpediente procPartExp = ComponentUtil.getComponent(ProcessoParteExpedienteManager.class).findById(idProcessoParteExpediente);
			processoDocumento = procPartExp.getProcessoDocumento();
			processoDocumentoVinculado = procPartExp.getProcessoExpediente().getProcessoDocumentoVinculadoExpediente();
		}
		if(processoDocumento != null){
			if(paginador != null){
				paginador.preencherDocumento(processoDocumento, 0);
			} else if(processoSelecionado != null){
				paginador = new PaginadorDocumentos(processoSelecionado.getIdProcessoTrf(), 0);
				paginador.preencherDocumento(processoDocumento, 0);
			}
		}
		if (processoDocumentoVinculado != null ) {
			this.processoDocumentoVincu = processoDocumentoVinculado;
		}
		return processoDocumento;
	}

	public ProcessoDocumento recuperaDocumentoVinculado() throws PJeBusinessException{
		ProcessoDocumento processoDocumentoVinculado = null;
		if (idProcessoParteExpediente != null){
			ProcessoParteExpediente procPartExp = ComponentUtil.getComponent(ProcessoParteExpedienteManager.class).findById(idProcessoParteExpediente);
			processoDocumentoVinculado = procPartExp.getProcessoExpediente().getProcessoDocumentoVinculadoExpediente();
		}

		if(processoDocumentoVinculado != null )
			return  processoDocumentoVinculado;

		return null;
	}

	public ProcessoDocumento recuperarDocSelecionado(Integer id){
		return recuperarDocSelecionado(id,true);
	}
	
	public void recuperarDocSelecionadoJavascript(Integer id){
		recuperarDocSelecionado(id);
	}

	public List<ProcessoDocumento> recuperarDocs(TimeLineAutosDigitais timeLine){
		List<ProcessoDocumento> docs = new ArrayList<ProcessoDocumento>();
		//Recuperar os Documentos Vinculados e os inclusos no Movimento e Expedientes
		if(timeLine.getDocumentos() != null && timeLine.getDocumentos().size() != 0){
			if(timeLine.getTipoDadoEnum().equals(TipoDadoENUM.D)){
				ProcessoDocumento pd = recuperarDocSelecionado(timeLine.getId(),false);
				if(pd != null) {
					docs.addAll(pd.getDocumentosVinculados());
				}
			}else{
				for (TimeLineAutosDigitais t: timeLine.getDocumentos()) {
					docs.add(recuperarDocSelecionado(t.getId(),false));
				}
			}
		}
		return docs;
	}

	public ProcessoTrf getProcessoSelecionado() {
		return processoSelecionado;
	}
	
	public void setProcessoSelecionado(ProcessoTrf processoSelecionado) {
		this.processoSelecionado = processoSelecionado;
	}

	public ProcessoDocumento getDocumentoSelecionado() {
		return documentoSelecionado;
	}
	
	public void setDocumentoSelecionado(ProcessoDocumento documentoSelecionado) {
		this.documentoSelecionado = documentoSelecionado;
	}

	public Set<ProcessoParteVO> getParteSemVinculacaoAtivo() {
		return parteSemVinculacaoAtivo;
	}

	public Set<ProcessoParteVO> getParteSemVinculacaoPassivo() {
		return parteSemVinculacaoPassivo;
	}

	public Set<ProcessoParteVO> getParteSemVinculacaoOutrosInteressados() {
		return parteSemVinculacaoOutrosInteressados;
	}

	public List<TimeLineAutosDigitais> getTimeLineAutosDigitais() {
		return timeLineAutosDigitais;
	}

	public Map<String, List<TimeLineAutosDigitais>> getMapDatas() {
		return mapDatas;
	}

	public boolean isExibePainelDocs() {
		if(documentoSelecionado != null){
			return true;
		}
		return false;
	}

	public boolean isExibeLink(TimeLineAutosDigitais time){
		
		return time == null ? false : time.getTipoDadoEnum().equals(TipoDadoENUM.D);
	}
	
	/**
	 * Verifica se o ProcessoDocumentoBin possui modelo,
	 * @param procDoc
	 * @return
	 */
	public boolean isPossuiModelo(ProcessoDocumento procDoc) {
		return procDoc.getProcessoDocumentoBin().getBinario() == false;
	}
	
	public boolean isExibePdf() {
		return exibePdf;
	}

	public boolean isExibeHtml() {
		return exibeHtml;
	}

	/**
	 * Verifica se a NovaPagina que será inserida 
	 * na timeLine deverá ser exibida(ScroolInfinito).
	 * @return
	 */
	public boolean isExibeNovaPagina(){
		boolean exibeNovaPagina = false;
		if(getPaginaAtual() > 1 && getPaginaAtual() <= getCountPaginas()){
			exibeNovaPagina = true;
		}
		return exibeNovaPagina;
	}
	
	public int getIdDocJavaScript() {
		return idDocJavaScript;
	}

	public void setIdDocJavaScript(int idDocJavaScript) {
		this.idDocJavaScript = idDocJavaScript;
	}

	public ProtocolarDocumentoBean getProtocolarDocumentoBean(){
		if(protocolarDocumentoBean == null && processoSelecionado.getIdProcessoTrf() > 0){
			protocolarDocumentoBean = new ProtocolarDocumentoBean(processoSelecionado.getIdProcessoTrf(), ProtocolarDocumentoBean.EXIGE_DOCUMENTO_PRINCIPAL);
		}
		return protocolarDocumentoBean;
	}
	
	public void permitirIncluirPeticoes() {
		getProtocolarDocumentoBean().sincronizarProcessoDocumentoComProcessoTrf(true);
	}
	
	public String getTab() {
		return tab;
	}

	public void setTab(String tab) {
		this.tab = tab;
		carregarUltimoAuto();
	}

	public String getTextoPesquisa() {
		return textoPesquisa;
	}

	public void setTextoPesquisa(String textoPesquisa) {
		this.textoPesquisa = textoPesquisa;
	}

	public List<ProcessoAlerta> getAlertasProcesso() {
		return alertasProcesso;
	}

	private void setAlertasProcesso(List<ProcessoAlerta> alertasProcesso) {
		this.alertasProcesso = alertasProcesso;
	}

	public List<EtiquetaProcesso> getEtiquetasProcesso() {
		return etiquetasProcesso;
	}

	public void setEtiquetasProcesso(List<EtiquetaProcesso> etiquetasProcesso) {
		this.etiquetasProcesso = etiquetasProcesso;
	}

	public List<Pendencia> getPendenciasProcesso() {
		return pendenciasProcesso;
	}

	public void adicionarFavorito() {
		Conteudo conteudo = getPaginador().getConteudo();
		if(conteudo != null) {
			try {
				ProcessoDocumento doc = EntityUtil.getEntityManager().getReference(ProcessoDocumento.class, conteudo.getIdDocumento());
				Usuario usr = Authenticator.getUsuarioLogado();
				ProcessoDocumentoFavorito fav = new ProcessoDocumentoFavorito();
				fav.setProcessoDocumento(doc);
				fav.setUsuario(usr);
				fav.setIndice(getPaginador().getIndice());
				ComponentUtil.getComponent(ProcessoDocumentoFavoritoManager.class).persistAndFlush(fav);
				conteudo.setIdDocumentoFavorito(fav.getIdProcessoDocumentoFavorito());				
			} catch (PJeBusinessException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void removerFavorito() {
		Conteudo conteudo = getPaginador().getConteudo();
		if(conteudo != null) {
			try {
				ProcessoDocumentoFavorito fav = ComponentUtil.getComponent(ProcessoDocumentoFavoritoManager.class).findById(conteudo.getIdDocumentoFavorito());
				if(fav != null) {
					removerFavorito(fav);
				}
			} catch (PJeBusinessException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void removerFavorito(ProcessoDocumentoFavorito entity) {
		try {
			ComponentUtil.getComponent(ProcessoDocumentoFavoritoManager.class).remove(entity);
			Conteudo conteudo = getPaginador().getConteudo();
			if(conteudo != null && entity.getIdProcessoDocumentoFavorito() == conteudo.getIdDocumentoFavorito()) {
				conteudo.setIdDocumentoFavorito(0);
			}
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
	}
	
	public List<ProcessoDocumentoFavorito> recuperarFavoritos() {
		return ComponentUtil.getComponent(ProcessoDocumentoFavoritoManager.class).findByProcesso(getProcessoSelecionado().getIdProcessoTrf(), Authenticator.getIdUsuarioLogado());
	}

	public void downloadAutosFavoritos() {
		List<ProcessoDocumentoFavorito> favoritos = recuperarFavoritos();
		if(favoritos != null && favoritos.size() > 0) {
			List<ProcessoDocumento> docs = new LinkedList<ProcessoDocumento>();
			for (ProcessoDocumentoFavorito fav : favoritos) {
				docs.add(fav.getProcessoDocumento());
			}
			String nomeArquivo = getProcessoSelecionado().getNumeroProcesso() + "_favoritos";
			download(docs, nomeArquivo);
		}
	}
	
	public void downloadAutosLembretes(Set<ProcessoDocumento> documentos) {
		if(documentos != null && documentos.size() > 0) {
			List<ProcessoDocumento> docs = new ArrayList<>(documentos);
			String nomeArquivo = getProcessoSelecionado().getNumeroProcesso() + "_lembretes";
			download(docs, nomeArquivo);
		}
	}
	
	public void downloadAutos() {
		String processosRestricao = ParametroUtil.getParametro("tjrj:download:restricao:processo");
		LogAcessoAutosDownloadsService logAutos = ComponentUtil.getComponent(LogAcessoAutosDownloadsService.class);

		if (processosRestricao != null && processosRestricao.contains(getProcessoSelecionado().getNumeroProcesso())) {
			String horaRestricaoInicio = ParametroUtil.getParametro("tjrj:download:restricao:horario:inicio");
			String horaRestricaoFim = ParametroUtil.getParametro("tjrj:download:restricao:horario:fim");
			Integer horaAtual = DateUtil.obterHora(DateService.instance().getDataHoraAtual());
			if (horaRestricaoInicio != null && horaRestricaoFim != null 
					&& horaAtual >= Integer.parseInt(horaRestricaoInicio)
					&& horaAtual <= Integer.parseInt(horaRestricaoFim)) {
				FacesMessages.instance().clear();
				FacesMessages.instance().add(Severity.ERROR, String.format("Não é permitido realizar download deste processo das %02d:00 até %02d:59 horas", Integer.parseInt(horaRestricaoInicio), Integer.parseInt(horaRestricaoFim)));
				return;
			}
		}
		processosRestricao = null;
		
		Search search = new Search(AutoProcessualDTO.class);
		search.addOrder("documento.dataJuntada", getParametroDownload().getCronologia());
		
		preencheParametrosDownload(search);
		
		List<AutoProcessualDTO> autos = ComponentUtil.getComponent(ListProcessoCompletoBetaManager.class).recuperarAutos(getProcessoSelecionado().getIdProcessoTrf(), true, false, search);
		List<ProcessoDocumento> processoDocumentoList = new LinkedList<ProcessoDocumento>();
		
		for (AutoProcessualDTO auto : autos) {
			List<ProcessoDocumento> docs = getDocumentosFromAutos(auto);
			for (ProcessoDocumento doc : docs) {
				ProcessoDocumentoBin bin = doc.getProcessoDocumentoBin();
				if(!bin.isBinario()) {
					ComponentUtil.getComponent(ListProcessoCompletoBetaManager.class).recuperarConteudoBinario(bin);
				}
				processoDocumentoList.add(doc);
				logAutos.logarDownload(doc);
			}
		}
		if(processoDocumentoList.size() > 0) {
			String nomeArquivo = getProcessoSelecionado().getNumeroProcesso();
			download(processoDocumentoList, nomeArquivo);
		}
	}
	
	private void preencheParametrosDownload(Search search) {
		try {
			search.addCriteria(Criteria.isNull("documento.dataExclusao"));
			if(getParametroDownload().getIdDocumentoInicio() != null && getParametroDownload().getIdDocumentoFim() == null) {
				search.addCriteria(Criteria.greaterOrEquals("documento.idProcessoDocumento", getParametroDownload().getIdDocumentoInicio()));
			}
			else if(getParametroDownload().getIdDocumentoInicio() == null && getParametroDownload().getIdDocumentoFim() != null) {
				search.addCriteria(Criteria.lessOrEquals("documento.idProcessoDocumento", getParametroDownload().getIdDocumentoFim()));
			}
			else if(getParametroDownload().getIdDocumentoInicio() != null && getParametroDownload().getIdDocumentoFim() != null) {
				int inicio = 0;
				int fim = 0;
				int retval = getParametroDownload().getIdDocumentoInicio().compareTo(getParametroDownload().getIdDocumentoFim());
				if(retval <= 0) {
					inicio = getParametroDownload().getIdDocumentoInicio();
					fim = getParametroDownload().getIdDocumentoFim();
				}
				else {
					inicio = getParametroDownload().getIdDocumentoFim();
					fim = getParametroDownload().getIdDocumentoInicio();
				}
				search.addCriteria(Criteria.between("documento.idProcessoDocumento", inicio, fim));
			}

			
			if(getParametroDownload().getDataInicio() != null && getParametroDownload().getDataFim() == null) {
				search.addCriteria(Criteria.greaterOrEquals("documento.dataJuntada", getParametroDownload().getDataInicio()));
			}
			else if(getParametroDownload().getDataInicio() == null && getParametroDownload().getDataFim() != null) {
				search.addCriteria(Criteria.lessOrEquals("documento.dataJuntada", getParametroDownload().getDataFim()));				
			}
			else if(getParametroDownload().getDataInicio() != null && getParametroDownload().getDataFim() != null) {
				Date inicio = null;
				Date fim = null;
				int retval = getParametroDownload().getDataInicio().compareTo(getParametroDownload().getDataFim());
				if(retval <= 0) {
					inicio = getParametroDownload().getDataInicio();
					fim = getParametroDownload().getDataFim();
				}
				else {
					inicio = getParametroDownload().getDataFim();
					fim = getParametroDownload().getDataInicio();
				}
				
				search.addCriteria(Criteria.between("documento.dataJuntada", DateUtil.getBeginningOfDay(inicio), DateUtil.getEndOfDay(fim)));
			}
			
			if(getParametroDownload().getIdTipoDocumento().intValue() != 0) {
				search.addCriteria(Criteria.equals("documento.tipoProcessoDocumento.idTipoProcessoDocumento", getParametroDownload().getIdTipoDocumento()));
			}
			
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
	}
	
	
	public void downloadDocumento() {
		ProcessoDocumento documento = getProcessoDocumentoAtual();
		if(documento != null) {
			setProcessoSelecionado(documento.getProcessoTrf());
			String nomeArquivo = documento.getProcessoDocumento();
			LogAcessoAutosDownloadsService logAutos = ComponentUtil.getComponent(LogAcessoAutosDownloadsService.class);
			logAutos.logarDownload(documento);
			download(Arrays.asList(documento), nomeArquivo);
		}
	}
	
	public ProcessoDocumento getProcessoDocumentoAtual() {
		if(paginador.getConteudo() != null) {
			try {
				int id = paginador.getConteudo().getIdDocumento();
				if(processoDocumentoAtual == null || processoDocumentoAtual.getIdProcessoDocumento() != id) {
					processoDocumentoAtual = ComponentUtil.getComponent(ProcessoDocumentoManager.class).findById(id);
				}
			} catch (PJeBusinessException e) {
				e.printStackTrace();
			}
		}
		return processoDocumentoAtual;
		
	}

	public void imprimirDocumento() {
		ProcessoDocumento documento = getProcessoDocumentoAtual();
		if(documento != null) {
			List<ProcessoDocumento> docs = new ArrayList<ProcessoDocumento>(0);
			docs.add(documento);
			imprimir(docs);
		}
	}

	private void download(List<ProcessoDocumento> documentos, String nomeArquivo) {
		String extensao = ".pdf";

		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
		response.reset();
		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "attachment; filename=\""	+ nomeArquivo + extensao + "\"");
		
		gerarPdf(request, response, documentos, true);
		getPjeUtil().registrarCookieTemporizadorDownload(response);
		facesContext.responseComplete();
	}

	private PjeUtil getPjeUtil() {
		return ComponentUtil.getComponent(PjeUtil.class);
	}

	private void imprimir(List<ProcessoDocumento> documentos) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
		response.reset();
		response.setContentType("application/pdf");
		
		gerarPdf(request, response, documentos, false);
		facesContext.responseComplete();
	}

	private void gerarPdf(HttpServletRequest request, HttpServletResponse response, List<ProcessoDocumento> documentos, boolean pdfCompleto)  {
		OutputStream out = null;
		try {
			out = response.getOutputStream();
			String resourcePath = request.getScheme() + "://"
					+ request.getServerName() + ":" + request.getServerPort()
					+ request.getContextPath();
			GeradorPdfUnificado geradorPdf = new GeradorPdfUnificado();
			geradorPdf.setResurcePath(resourcePath);
			if(pdfCompleto) {
				geradorPdf.setGerarIndiceDosDocumentos(true);
				geradorPdf.gerarPdfUnificado(getProcessoSelecionado(), documentos, out);
			}
			else {
				geradorPdf.gerarPdfSimples(documentos, out);
			}
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (PdfException e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private List<ProcessoDocumento> getDocumentosFromAutos(AutoProcessualDTO auto) {
		List<ProcessoDocumento> docs = new LinkedList<ProcessoDocumento>(); 
		ProcessoDocumento docPrincipal = auto.getDocumento();
		if (docPrincipal != null) {
			docs.add(docPrincipal);
			
			List<ProcessoDocumento> anexos = new ArrayList<>(0);
			anexos.addAll(docPrincipal.getDocumentosVinculados());
			
			CollectionUtilsPje.sortCollection(anexos, true, "numeroOrdem");
			for (ProcessoDocumento documentoVinculado : anexos) {
				docs.add(documentoVinculado);
			}
		}
		return docs;
	}

	private void recuperarPendencias() {
		if(temPermissaoVisualizarPendencias()) {
			recuperarPendenciaAntecipacaoTutela();
			recuperarPendenciaJusticaGratuita();
			recuperarPendenciaSegredoJustica();
			recuperarPendenciaSigiloDocumento();
			recuperarPendenciaPeticoesAvulsas();
		}
	}
	
	private void recuperarPendenciaPeticoesAvulsas() {
		int count = 0;
		try {
			Search search = new Search(ProcessoDocumentoPeticaoNaoLida.class);
			search.addCriteria(Criteria.equals("processoDocumento.processo.idProcesso", getProcessoSelecionado().getIdProcessoTrf()));
			search.addCriteria(Criteria.equals("retirado", Boolean.FALSE));
			count = ComponentUtil.getComponent(ProcessoDocumentoPeticaoNaoLidaManager.class).count(search).intValue();

		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		if(count > 0) {
			getPendenciasProcesso().add(new Pendencia(TipoPendenciaEnum.P));
		}
		
	}

	private void recuperarPendenciaSigiloDocumento() {
		if(getProcessoSelecionado().getApreciadoSigilo().equals(ProcessoTrfApreciadoEnum.A)) {
			getPendenciasProcesso().add(new Pendencia(TipoPendenciaEnum.D));
		}
	}

	private void recuperarPendenciaSegredoJustica() {
		if(getProcessoSelecionado().getSegredoJustica() != null && 
			getProcessoSelecionado().getSegredoJustica() && 
			getProcessoSelecionado().getApreciadoSegredo().equals(ProcessoTrfApreciadoEnum.A)) {
			getPendenciasProcesso().add(new Pendencia(TipoPendenciaEnum.S));
		}
	}

	private void recuperarPendenciaJusticaGratuita() {
		if(getProcessoSelecionado().getJusticaGratuita() != null && 
			getProcessoSelecionado().getJusticaGratuita() && 
			(getProcessoSelecionado().getApreciadoJusticaGratuita() == null ||
			!getProcessoSelecionado().getApreciadoJusticaGratuita())) {
			getPendenciasProcesso().add(new Pendencia(TipoPendenciaEnum.J));
		}
	}

	private void recuperarPendenciaAntecipacaoTutela() {
		if(getProcessoSelecionado().getTutelaLiminar() != null && 
			getProcessoSelecionado().getTutelaLiminar() && 
			(getProcessoSelecionado().getApreciadoTutelaLiminar() == null || 
			!getProcessoSelecionado().getApreciadoTutelaLiminar())) {
			getPendenciasProcesso().add(new Pendencia(TipoPendenciaEnum.T));
		}
	}

	private boolean temPermissaoVisualizarPendencias() {
		boolean ojProcesso = false;
		boolean visualizaIconePendencias = false;
		Integer idPapelPermiteVisualizarIconeAvisosPendencia = null; 
		
		idPapelPermiteVisualizarIconeAvisosPendencia = this.papelDAO.findByCodeName("pje:papel:servidor:permiteVisualizarIconeAvisosPendencia").getIdPapel();	
		
	    if (idPapelPermiteVisualizarIconeAvisosPendencia != null){               
			visualizaIconePendencias = Authenticator.getUsuarioLocalizacaoAtual().getPapel().getIdsPapeisInferiores().contains(Integer.toString(idPapelPermiteVisualizarIconeAvisosPendencia));
	    }
		
		if (visualizaIconePendencias) {
			Integer idOrgaoJulgador = Authenticator.getIdOrgaoJulgadorAtual();
			ojProcesso = (idOrgaoJulgador != null && idOrgaoJulgador.equals(getProcessoSelecionado().getOrgaoJulgador().getIdOrgaoJulgador()));				
		}
		
		return visualizaIconePendencias &&  ojProcesso;
	}

	public ParametroDownload getParametroDownload() {
		return parametroDownload;
	}

	public void setParametroDownload(ParametroDownload parametroDownload) {
		this.parametroDownload = parametroDownload;
	}
	
	public List<SelectItem> getTipoDocumentoSelectItems() {
		List<SelectItem> selectList = new ArrayList<SelectItem>();
		List<TipoProcessoDocumento> tipos = ComponentUtil.getComponent(ListProcessoCompletoBetaManager.class).recuperarTiposDocumentosAutos(getProcessoSelecionado().getIdProcessoTrf());
		for (TipoProcessoDocumento t : tipos) {
			selectList.add(new SelectItem(t.getIdTipoProcessoDocumento(), t.getTipoProcessoDocumento()));
		}
		return selectList;
	}
	
	/**
	 * @param processoParte parte processual
	 * @return String contendo Nome da Parte, OAB(caso seja advogado) e CPF ou CNPJ
	 */
	public String getNomeDoPolo(ProcessoParte processoParte){
		return ComponentUtil.getComponent(ProcessoParteManager.class).recuperaNomeComInformacoesUsadoNoProcesso(processoParte);
	}
	
	public boolean podeVisualizarNomeDoPolo(ProcessoParte processoParte) {
		return ComponentUtil.getComponent(ProcessoParteManager.class).podeVisualizarNomeDoPolo(processoParte);
	}
	
	public boolean isPessoaJuizoDoProcesso() {
		return isPessoaJuizoDoProcesso;
	}

	public void setPessoaJuizoDoProcesso(boolean isPessoaJuizoDoProcesso) {
		this.isPessoaJuizoDoProcesso = isPessoaJuizoDoProcesso;
	}
	
	public boolean isProcessoDeslocadoParaLocalizacaoPessoa() {
		return isProcessoDeslocadoParaLocalizacaoPessoa;
	}

	public void setProcessoDeslocadoParaLocalizacaoPessoa(boolean isProcessoDeslocadoParaLocalizacaoPessoa) {
		this.isProcessoDeslocadoParaLocalizacaoPessoa = isProcessoDeslocadoParaLocalizacaoPessoa;
	}
	
	public Boolean getOrgaoJulgadorValido() {
		return this.isPessoaJuizoDoProcesso;
	}

	/**
	 * Verifica se o usuario logado possui uma localização estruturada
	 * @return verdadeiro se o usuario logado possuir uma localização, e esta for uma estruturada.
	 */
	public Boolean isUsuarioLogadoComLocalizacaoModelo(){
		return Authenticator.getLocalizacaoModeloAtual() != null;
	}
	
	/**
	 * Recupera a mensagem padrão para documentos não juntados ao processo.
	 * @return retorna a string com a mensagem padrão para documentos não juntados ao processo.
	 */
	public String obterNomeUsuarioDocumentoNaoJuntado(){
		return FacesUtil.getMessage("documentoProcesso.label.documento.nao.juntado");
	}
	
	/**
	 * Recupera a descrição da competência do processo judicial.
	 * @return competencia do processo, se não existir, retorna a string "Não Identificada".
	 */
	public String obterCompetenciaProcesso(){
		String competencia = new String();
		if (processoSelecionado.getCompetencia() != null && StringUtils.isNotBlank(processoSelecionado.getCompetencia().getCompetencia())) {
			competencia = processoSelecionado.getCompetencia().getCompetencia();
		} else {
			competencia = FacesUtil.getMessage("competencia.descricao.nao.identificada");
		}
		
		return competencia;
	}

	public String getCompetencia() {
		if (competencia == null) {
			competencia = obterCompetenciaProcesso();
		}

		return competencia;
	}
	
	/**
	 * Verifica se exibe dados da justiça eleitoral
	 * @return
	 */
	public boolean isExibeDadosJusticaoEleitoral() {
		if(ParametroJtUtil.instance().justicaEleitoral()){
			return true;
		}
		return false;
	}
	
	/**
	 * Returna informação da eleição
	 * @return String
	 */
	public String getEleicao(){
		if (processoSelecionado != null && processoSelecionado.getComplementoJE() != null
				&& processoSelecionado.getComplementoJE().getEleicao() != null) {
			return processoSelecionado.getComplementoJE().getEleicao().getDescricao();
					
		}
		return "";
	}
	
	/**
	 * Returna informação do município/UF da eleição
	 * @return String
	 */
	public String getMunicipioUfEleicao(){
		if (processoSelecionado!=null && processoSelecionado.getComplementoJE()!=null){
			return processoSelecionado.getComplementoJE().getMunicipioEleicao()+"/"
					+processoSelecionado.getComplementoJE().getEstadoEleicao().getCodEstado();
					
		}
		return "";
	}
	
	public boolean podeVisualizarAbaAssociados260CE() {
		return (ParametroJtUtil.instance().justicaEleitoral() && Identity.instance().hasRole(Papeis.VISUALIZA_ABA_ASSOCIADOS260CE));

	}
	
	public boolean getIsPermiteVisualizarAbaExpedientes() {
		return Identity.instance().hasRole(Papeis.VISUALIZA_ABA_EXPEDIENTES);
	}
	
	public boolean getIsPermiteVisualizarAbaAudiencias() {
		if(!getIsAmbienteColegiado()) {
			ProcessoTrfHome processoTrfHome = ComponentUtil.getComponent(ProcessoTrfHome.class);
			
			if(Authenticator.isUsuarioInterno() && 
					(this.isPessoaJuizoDoProcesso || processoTrfHome.isRevisor(processoSelecionado))) {
				return true;
			}else if(!Authenticator.getTipoUsuarioExternoAtual().equals(TipoUsuarioExternoEnum.O) || Authenticator.isJusPostulandi()) {
				return true;
			}
		}
		return false;
	}

	public boolean getIsPermiteVisualizarAbaPericias() {
		if(!getIsAmbienteColegiado()) {
			ProcessoTrfHome processoTrfHome = ComponentUtil.getComponent(ProcessoTrfHome.class);
			
			if(Authenticator.isUsuarioInterno() && 
					(this.isPessoaJuizoDoProcesso || processoTrfHome.isRevisor(processoSelecionado))) {
				return true;
			}else if(!Authenticator.getTipoUsuarioExternoAtual().equals(TipoUsuarioExternoEnum.O) || Authenticator.isJusPostulandi()) {
				return true;
			}
		}
		return false;
	}
	
	public boolean getIsPermiteVisualizarAbaCaracteristicas() {
		ProcessoTrfHome processoTrfHome = ComponentUtil.getComponent(ProcessoTrfHome.class);
		
		if(Authenticator.isUsuarioInterno() && 
				(this.isPessoaJuizoDoProcesso || processoTrfHome.isRevisor(processoSelecionado))) {
			return true;
		}else if(!Authenticator.getTipoUsuarioExternoAtual().equals(TipoUsuarioExternoEnum.O) || Authenticator.isJusPostulandi()) {
			return true;
		}
		return false;
	}
	
	public Boolean getIsPermiteVisualizarAbaCertidaoCriminal() {
		if (permiteVisualizarAbaCertidaoCriminal == null) {
			permiteVisualizarAbaCertidaoCriminal = verificarVisibilidadeAbasCriminais(Papeis.VISUALIZA_INFORMACAO_CRIMINAL); 
		}
		return permiteVisualizarAbaCertidaoCriminal;
	}
	
	public Boolean getIsPermiteVisualizarAbaInformacaoCriminal() {
		if (permiteVisualizarAbaInformacaoCriminal == null) {
			permiteVisualizarAbaInformacaoCriminal = verificarVisibilidadeAbasCriminais(Papeis.MANIPULA_INFORMACAO_CRIMINAL); 
		}
		return permiteVisualizarAbaInformacaoCriminal;
	}
	
	private boolean verificarVisibilidadeAbasCriminais(String papel) {
		boolean permitido = false;
		ProcessoTrfHome processoTrfHome = ProcessoTrfHome.instance();
		if (processoTrfHome.isClasseCriminalOuInfracional() && Authenticator.isUsuarioInterno()) {
			ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();
			Integer idOrgaoJulgador = Authenticator.getIdOrgaoJulgadorAtual();
			Integer idOrgaoJulgadorColegiado = Authenticator.getIdOrgaoJulgadorColegiadoAtual();
			if (idOrgaoJulgador != null) {
				permitido = processoTrf.getOrgaoJulgador().getIdOrgaoJulgador() == idOrgaoJulgador;
			} else if (idOrgaoJulgadorColegiado != null) {
				permitido = processoTrf.getOrgaoJulgadorColegiado().getIdOrgaoJulgadorColegiado() == idOrgaoJulgadorColegiado;
			}
			if (!permitido) {
				ProcessoJudicialService processoJudicialService = ComponentUtil.getComponent(ProcessoJudicialService.class);
				permitido = Authenticator.hasRole(papel) 
						&& processoJudicialService.existeFluxoDeslocadoParaLocalizacao(processoTrf);
			}
			if (!permitido) {
				permitido = Authenticator.hasRole(papel) && Papeis.VISUALIZA_INFORMACAO_CRIMINAL.equals(papel);
			}
		}
		return permitido;
	}	

	public boolean getIsPermiteVisualizarAbaJuntarDocumentos() {
		
		Integer idAtendenteN1 = Authenticator.ID_ATENDENTE_N1;	
		Integer idPapelLogado = Authenticator.getIdPapelAtual();		
		
		if (idAtendenteN1.equals(idPapelLogado)) {
			return false;
		}
		if(this.getProcessoSelecionado().getInOutraInstancia()) {
			return !ComponentUtil.getComponent(ParametroUtil.class).isBloquearProcessoRemetido();
		}
		
		return true;
	}
	
	public boolean getIsPermiteVisualizarAbaAssociados() {
		return Identity.instance().hasRole(Papeis.VISUALIZA_ABA_ASSOCIADOS);
	}
	
	public boolean getIsPermiteVisualizarAbaPeticoesAvulsas(){
		ProcessoTrfHome processoTrfHome = ComponentUtil.getComponent(ProcessoTrfHome.class);
		Identity user = Identity.instance();
		
		if (user.hasRole(Papeis.VISUALIZAR_PETICIONAMENTO_AVULSO) && (this.isPessoaJuizoDoProcesso || processoTrfHome.isRevisor(processoSelecionado))) {
			return true;
		} else if (Authenticator.isProcurador() || Authenticator.isAssistenteProcurador()) {
			return true;
		}
		return false;
	}

	
	public boolean getIsPermiteVisualizarAbaMovimentos() {
		if(Authenticator.isUsuarioInterno()) {
			AjusteMovimentacaoAction ajusteMovimentacaoAction = ComponentUtil.getComponent(AjusteMovimentacaoAction.class);
			return ajusteMovimentacaoAction.permiteVisualizarFuncionalidadeMovimentosProcesso(this.getProcessoSelecionado());
		}
		return false;
	}
	
	public boolean getIsPermiteVisualizarAbaProcessoReferencia() {
		if(Authenticator.isUsuarioInterno()) {
			DadosProcessoReferenciaAction dadosProcessoReferenciaAction = ComponentUtil.getComponent(DadosProcessoReferenciaAction.class);
			return dadosProcessoReferenciaAction.exibirAba();
		}
		return false;
	}
	
	public boolean getIsPermiteIncluirLembretes() {
		return Authenticator.isUsuarioInterno();
	}

	public boolean getIsPermiteApreciarPeticaoAvulsaNaoLida() {
		ProcessoTrfHome processoTrfHome = ComponentUtil.getComponent(ProcessoTrfHome.class);
		return Authenticator.isUsuarioInterno() && processoTrfHome.getProcessoDocumentoPeticaoNaoLida().size() > 0;
	}
	
	public boolean getIsPermiteVisualizarSituacoesProcesso() {
		return Identity.instance().hasRole(Papeis.VISUALIZA_SITUACOES_ATUAIS) || Identity.instance().hasRole(Papeis.VISUALIZAR_SITUACOES);
	}
	
	public boolean getIsPermiteIniciarAtividadeDigitalizacao() {
		return Authenticator.isUsuarioInterno() && Identity.instance().hasRole(Papeis.DEFLAGRAR_DIGITALIZACAO) 
				&& (ComponentUtil.getComponent(ParametroService.class).valueOf(Parametros.CODIGO_FLUXO_DIGITALIZACAO) != null);
	}
	
	public boolean getIsPermiteIniciarAtividadeComunicacaoEntreInstancias() {
		return Authenticator.isUsuarioInterno() && this.isPessoaJuizoDoProcesso 
				&& Identity.instance().hasRole(Papeis.PODE_INICIAR_FLUXO_COMUNICACAO_ENTRE_INSTANCIAS);
	}

	public boolean getIsPermiteEditarObjeto() {
		return Identity.instance().hasRole(Papeis.PROCESSO_OBJETO_EDITOR) && this.isPessoaJuizoDoProcesso;
	}
	
	public boolean getIsPermiteRetificarProcesso() {
		return Authenticator.isUsuarioInterno() && this.isPessoaJuizoDoProcesso && Identity.instance().hasRole(Papeis.RECURSO_RETIFICAR_PROCESSO);
	}
	
	/**
	 * Metodo responsavel por definir o ultimo auto digital no frame de "Autos"
	 */
	public void carregarUltimoAuto() {
		if (getTab() != null && getTab().equals("autosDigitais")) {
			if (getPaginador() != null) {
				getPaginador().ultimo();
				if (getPaginador().getConteudo() != null && getPaginador().getConteudo().getIdDocumento() != 0) {
					getPaginador().atual(getPaginador().getConteudo().getIdDocumento());
				}
			}
		}
	}
	
	public void irParaPaginaInicial() {
		this.setTab("autosDigitais");
	}
	
	public Integer getIdTipoProcessoDocumentoPeticaoInicial(){
		Integer idTipoProcessoDocumentoPeticaoInicial = 0;
		
		if(this.processoSelecionado != null){
			idTipoProcessoDocumentoPeticaoInicial = this.processoSelecionado.getClasseJudicial().getTipoProcessoDocumentoInicial().getIdTipoProcessoDocumento();
		}
		
		return idTipoProcessoDocumentoPeticaoInicial;
	}
	
	public boolean isVisualizaDocumentosNaoLidos() {
		return Authenticator.isUsuarioInterno() && 
			!Authenticator.hasRole(Papeis.OCULTAR_AGRUPADOR, Papeis.OCULTAR_AGRUPADOR_DOCUMENTOS_NAO_LIDOS);
	}

	public Long getTotalDocumentosNaoLidos() {
		if (this.totalDocumentosNaoLidos == null) {
			this.totalDocumentosNaoLidos = ComponentUtil.getComponent(ListProcessoCompletoBetaManager.class)
					.countDocumentosNaoLidosAutos(processoSelecionado.getIdProcessoTrf());
		}
		return this.totalDocumentosNaoLidos;
	}

	public void marcarDocumentosComoLidos() {
		ComponentUtil.getComponent(ListProcessoCompletoBetaManager.class)
				.marcarDocumentosComoLidosAutos(processoSelecionado.getIdProcessoTrf());

		this.totalDocumentosNaoLidos = null;

		for (Object data : this.mapDatas.keySet().toArray()) {
			for (TimeLineAutosDigitais auto : this.mapDatas.get(data)) {
				auto.setLido(true);
			}
		}
	}

	public boolean isExibirTarefa() {
		return exibirTarefa;
	}

	public void setExibirTarefa(boolean exibirTarefa) {
		this.exibirTarefa = exibirTarefa;
	}
	
	public boolean getIsPermiteVisualizarPdpjMarketplace() {
		return Authenticator.isUsuarioInterno() && Identity.instance().hasRole(Papeis.PDPJ_VISUALIZAR_MARKETPLACE) 
				&& getUrlPDPJMarketplace() != null;
	}
	
	public String getUrlPDPJMarketplace() {
		return ComponentUtil.getComponent(ParametroService.class).valueOf(Parametros.URL_PDPJ_MARKETPLACE);
	}
	
	public boolean isExibeMsgLimitacaoPartes(){
		String processosRestricao = ParametroUtil.getParametro("tjrj:download:restricao:processo");
		return processosRestricao != null && processosRestricao.contains(getProcessoSelecionado().getNumeroProcesso());
	}

	public ProcessoDocumento getProcessoDocumentoVincu() {
		return processoDocumentoVincu;
	}

	public void setProcessoDocumentoVincu(ProcessoDocumento processoDocumentoVincu) {
		this.processoDocumentoVincu = processoDocumentoVincu;
	}


	public boolean isUsarLimitacaoInicialDePartes() {
		final String usarLimitacaoInicialDePartes = ParametroUtil.getParametro("pje:paginacaoPartes:usarLimitacaoInicialDePartes");
		return (usarLimitacaoInicialDePartes != null && usarLimitacaoInicialDePartes.trim().equalsIgnoreCase("S"));
	}

	
	
	
	public String getMensagemErro() {
		return mensagemErro;
	}

	public void setMensagemErro(String mensagemErro) {
		this.mensagemErro = mensagemErro;
	}
	

	
	
	
	
	public Boolean isProcessoForaDeTramitacao() {
		return this.processoForaDeTramitacao;
	}
		  
	public void setProcessoForaDeTramitacao(Boolean isProcessoForaDeTramitacao) {
		this.processoForaDeTramitacao = isProcessoForaDeTramitacao;
	}
		
	private void inicializarAtributoProcessoForaDeTramitacao() {
			this.processoForaDeTramitacao = ComponentUtil.getTramitacaoProcessualService()
		 				.temSituacao(this.getProcessoSelecionado().getIdProcessoTrf(), Variaveis.PROCESSO_FORA_TRAMITACAO);
		}

	

	

	

	public List<TagMin> getEtiquetasUsuario() {
		return etiquetasUsuario;
	}

	public void setEtiquetasUsuario(List<TagMin> etiquetasUsuario) {
		this.etiquetasUsuario = etiquetasUsuario;
	}
	
	public Map<TagMin, Boolean> getEtiquetasCheck() {
		return etiquetasCheck;
	}

	public void setEtiquetasCheck(Map<TagMin, Boolean> etiquetasCheck) {
		this.etiquetasCheck = etiquetasCheck;
	}
	public boolean isExibirPopupTags() {
		return exibirPopupTags;
	}

	public void setExibirPopupTags(boolean exibirPopupTags) {
		this.exibirPopupTags = exibirPopupTags;
	}
	
	public String getTextoPesquisaTag() {
		return textoPesquisaTag;
	}

	public void setTextoPesquisaTag(String textoPesquisaTag) {
		this.textoPesquisaTag = textoPesquisaTag;
	}
	
	public void exibirCarregarPopupTags() {
		atualizaEtiquetasUsuario();
		setExibirPopupTags(true);
	}
	
	public boolean getIsProcessoBLoqueadoMigracao() {

		return this.getProcessoSelecionado().getInBloqueioMigracao();
	}
}
