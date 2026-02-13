package br.com.infox.pje.action;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.business.dao.ProcessoDocumentoTarefaDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.LocalizacaoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioLocalizacaoMagistradoServidorManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioLocalizacaoVisibilidadeManager;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.view.BaseAction;
import br.jus.cnj.pje.view.EntityDataModel;
import br.jus.cnj.pje.webservice.json.InformacaoUsuarioSessao;
import br.jus.csjt.pje.business.pdf.GeradorPdfUnificado;
import br.jus.csjt.pje.business.pdf.PdfException;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrfDocumentoImpresso;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;
import br.jus.pje.nucleo.enums.FiltroTempoAgrupadoresEnum;

@Name(ImpressaoMultiplaAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class ImpressaoMultiplaAction extends BaseAction<ProcessoDocumento> {
	
	private static final long serialVersionUID = 5649971889822752993L;

	public static final String NAME = "impressaoMultiplaAction";
	private static final int QTD_LISTA_VAZIA = 0;
	@In
	private ProcessoDocumentoTarefaDAO processoDocumentoTarefaDAO;
	@In
	private UsuarioLocalizacaoMagistradoServidorManager usuarioLocalizacaoMagistradoServidorManager;
	@In
	private LocalizacaoManager localizacaoManager;
	private String numeroProcesso;
	private Date dataInicioJuntada;
	private Date dataFimJuntada;
	private Date dataInicioExpediente;
	private Date dataFimExpediente;
	private ExpedicaoExpedienteEnum meioComunicacao;
	private TipoProcessoDocumento tipoProcessoDocumento;
	private String docImpressos;
 
	private Boolean expedientesCheckAll;	
	private Map<ProcessoDocTarefa, Boolean> expedientesCheck;	
	@In
	private ProcessoParteExpedienteManager processoParteExpedienteManager;	
	@In
	private UsuarioLocalizacaoVisibilidadeManager usuarioLocalizacaoVisibilidadeManager; 
	@In
	private ProcessoDocumentoManager processoDocumentoManager;
	@In
	private ProcessoJudicialService processoJudicialService;
	
	@In
	private UsuarioService usuarioService;
	
	private InformacaoUsuarioSessao informacaoDoUsuario;
	
	private List<ProcessoDocTarefa> listProcDocTarefa;
	
	private int countDoc;
	
	
	public ImpressaoMultiplaAction() {
		this.expedientesCheckAll = Boolean.FALSE;
		
		this.expedientesCheck = new LinkedHashMap<ProcessoDocTarefa, Boolean>(0);
		
	}
	
	/**
	 * M�todo respons�vel pela inicializa��o da classe.
	 */
	@Create
	public void init() {
 
		this.docImpressos = "N";
 
		pesquisar();
		
	}
	
	/**
	 * M�todo respons�vel por proceder com a pesquisa dos expedientes de cada agrupador.
	 */
	public void pesquisar() {
		this.listProcDocTarefa = new ArrayList<ProcessoDocTarefa>();
		this.listProcDocTarefa.addAll(listarTarefasUsuarioWorker());
		this.expedientesCheck = new LinkedHashMap<ProcessoDocTarefa, Boolean>(0);
		
	
	}
	

	/**
	 * M�todo respons�vel por inicializar os campos de pesquisa.
	 */
	public void limpar() {
		this.numeroProcesso = null;
		this.meioComunicacao = null;
		this.tipoProcessoDocumento = null;
		this.docImpressos = "N";
		this.dataFimJuntada = null;
		this.dataInicioJuntada = null;
		this.dataInicioExpediente = null;
		this.dataFimExpediente = null;

	}
	
	


	

	
	/**
	 * M�todo respons�vel por selecionar (ou retirar a sele��o) de todos os componentes checkbox da lista.
	 * 
	 * @param entityDataModel Componente de pagina��o de dados.
	 * @param map Vari�vel que armazena o status dos componentes checkbox da lista.
	 * @param status Vari�vel que indica qual ser� o status dos componentes checkbox da lista.
	 */
	public void selecionarTodosCheck(EntityDataModel<ProcessoDocTarefa> entityDataModel, 
			Map<ProcessoDocTarefa, Boolean> map, boolean status) {
		
		List<ProcessoDocTarefa> list = entityDataModel.getPage();
		for (ProcessoDocTarefa element : list) {
			map.put(element, status);
		}
	}
	
	/**
	 * M�todo respons�vel por verificar se algum componente checkbox da lista est� selecionado.
	 * 
	 * @return Verdadeiro se algum componente checkbox da lista est� selecionado. Falso, caso contr�rio.
	 */
	public boolean verificarCheck(Map<ProcessoDocTarefa, Boolean> map) {
		for (Map.Entry<ProcessoDocTarefa, Boolean> entry : map.entrySet()) {
			if (entry.getValue() == true) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * M�todo respons�vel por imprimir os documentos dos expedientes.
	 * @throws PJeBusinessException 
	 */
	public void imprimirDocumentoSelecionado() throws PJeBusinessException {
		imprimirDocumentos(this.expedientesCheck);

		 
	}
	
	public void confirmarDocImpresso() throws PJeBusinessException {
		confirmarDocs(this.expedientesCheck);
		this.expedientesCheck = new LinkedHashMap<ProcessoDocTarefa, Boolean>(0);
		pesquisar();
	}
	
	private void confirmarDocs(Map<ProcessoDocTarefa, Boolean> map) throws PJeBusinessException {
		List<ProcessoDocumento> listProcessoDocumento = new ArrayList<ProcessoDocumento>();
		boolean bGravou = false;
		ProcessoDocumento procDoc = new ProcessoDocumento();
 		if (map.containsValue(Boolean.TRUE)) {
			for (Map.Entry<ProcessoDocTarefa, Boolean> entry : map.entrySet()) {
				if (entry.getValue() == true) {
					 
						procDoc = new ProcessoDocumento();
						ProcessoTrfDocumentoImpresso processoImpresso = new ProcessoTrfDocumentoImpresso();
		
						procDoc = processoDocumentoManager.findById(entry.getKey().getIdProcessoDocumento());
						
						
										
						if(!listProcessoDocumento.contains(procDoc)) {
							listProcessoDocumento.add(procDoc);	
							if(procDoc.getProcessoTrfDocumentoImpresso() == null) {
								processoImpresso.setIdProcessoDocumento(procDoc.getIdProcessoDocumento());
								processoImpresso.setDataImpressao(new Date());
								processoImpresso.setProcessoTrf(procDoc.getProcessoTrf());
								processoImpresso.setPessoaImpressao(Authenticator.getPessoaLogada());
								EntityUtil.getEntityManager().persist(processoImpresso);
								bGravou = true;
								
							}
					
					}
				}
				if(bGravou) {
					if(Parametros.PJE_FLUXO_AGUARDA_IMPRESSAO != null) {
						if(procDoc != null) {
							List<Integer> listaSinalizacao = new ArrayList<Integer>();
							listaSinalizacao.add(entry.getKey().getIdProcessoDocumento());
							listaSinalizacao.add(entry.getKey().getIdProcessoParteExpediente());
							InformacaoUsuarioSessao informacaoDoUsuario = getUsuarioSesssao();
							if(informacaoDoUsuario != null) {
								listaSinalizacao.add(informacaoDoUsuario.getIdLocalizacaoFisica());
							}else {
								listaSinalizacao.add(null);
							}
							
							processoJudicialService.sinalizarFluxo(procDoc.getProcessoTrf(), Parametros.PJE_FLUXO_AGUARDA_IMPRESSAO,listaSinalizacao, false, true);
						}
					}
					
					
					
					EntityUtil.getEntityManager().flush();	
				}			
				
				bGravou = false;
				
				
			}
		}
	}
 
	private void imprimirDocumentos(Map<ProcessoDocTarefa, Boolean> map) throws PJeBusinessException{
			List<ProcessoDocumento> listProcessoDocumento = new ArrayList<ProcessoDocumento>();
		
			if (map.containsValue(Boolean.TRUE)) {
				for (Map.Entry<ProcessoDocTarefa, Boolean> entry : map.entrySet()) {
					if (entry.getValue() == true) {	
						ProcessoDocumento procDocumento = new ProcessoDocumento();
						procDocumento = processoDocumentoManager.findById(entry.getKey().getIdProcessoDocumento());
					 
						listProcessoDocumento.add(procDocumento);					
						
					}
				}
			}
			
			imprimir(listProcessoDocumento);
			
		
	}
	
	
	
	private List<ProcessoDocTarefa> listarTarefasUsuarioWorker() {
		InformacaoUsuarioSessao informacaoDoUsuario = getUsuarioSesssao();
		List<ProcessoDocTarefa> retorno = new ArrayList<ProcessoDocTarefa>();
		ProcessoDocTarefa procDocTarefa = new ProcessoDocTarefa();
		Integer idTipoProcessoDocumento = 0;
		String meio = "";
		Date dtInicio = null;
		Date dtFim = null;
		Date dtInicioExpediente = null;
		Date dtFimExpediente = null;
		
		if (informacaoDoUsuario != null) {
			String numeroProcesso = this.numeroProcesso;
			if(this.tipoProcessoDocumento != null) {
				idTipoProcessoDocumento = this.tipoProcessoDocumento.getIdTipoProcessoDocumento();
			}
			if(this.meioComunicacao != null) {
				meio = this.meioComunicacao.name() ;
			}
			if(this.dataInicioJuntada != null && this.dataFimJuntada != null) {
				dtInicio = this.dataInicioJuntada;
				dtFim = this.dataFimJuntada;
			}
			if(this.dataInicioExpediente != null && this.dataFimExpediente != null) {
				dtInicioExpediente = this.dataInicioExpediente;
				dtFimExpediente = this.dataFimExpediente;
			}
  
	 
			List<Object[]> processoTarefas = this.processoDocumentoTarefaDAO.carregarListaTarefasUsuario(
					informacaoDoUsuario.getIdOrgaoJulgadorColegiado(),
					informacaoDoUsuario.isServidorExclusivoOJC(),
					informacaoDoUsuario.getIdsOrgaoJulgadorCargoVisibilidade(),
					informacaoDoUsuario.getIdUsuario(),
					informacaoDoUsuario.getIdsLocalizacoesFisicasFilhas(),
					informacaoDoUsuario.getIdLocalizacaoFisica(),
					informacaoDoUsuario.getIdLocalizacaoModelo(),
					informacaoDoUsuario.getIdPapel(),
					informacaoDoUsuario.getVisualizaSigiloso(), 
					informacaoDoUsuario.getNivelAcessoSigilo(),
					false,
					numeroProcesso, null, null, informacaoDoUsuario.getCargoAuxiliar(),idTipoProcessoDocumento,meio, dtInicio, dtFim, this.docImpressos,dtInicioExpediente, dtFimExpediente);
			for(Object[] processo : processoTarefas) {
					procDocTarefa = new ProcessoDocTarefa();
			
					procDocTarefa.setIdProcessoDocumento((int) processo[0]);
					procDocTarefa.setNumeroProcesso((String) processo[1]);
					procDocTarefa.setTpDocumento((String) processo[2]);
					procDocTarefa.setDtJuntada((Timestamp) processo[3]);		
					Character meioComu = (Character) processo[4];
					procDocTarefa.setIdProcessoDocumentoBin((int) processo[5]);	
					procDocTarefa.setDtInicioExpediente((Timestamp) processo[6]);
					if(processo[7] != null) {
						procDocTarefa.setIdProcessoParteExpediente((int) processo[7]);
					}
					if(meioComu != null) {
						procDocTarefa.setMeioExpedicaoExpediente(ExpedicaoExpedienteEnum.valueOf(meioComu.toString()));
					}else {
						procDocTarefa.setMeioExpedicaoExpediente(null);
					}
		
					
					retorno.add(procDocTarefa);
						
					
				}
		 
			}
		
			this.countDoc = retorno.size();
		return retorno;
	}

	private InformacaoUsuarioSessao getUsuarioSesssao() {
		if (this.informacaoDoUsuario == null) {
			
			this.informacaoDoUsuario = usuarioService.recuperarInformacaoUsuarioLogado();
		}
		return informacaoDoUsuario;
	}
	
	
	
	private void imprimir(List<ProcessoDocumento> documentos) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
		response.reset();
		response.setContentType("application/pdf");
		
		gerarPdf(request, response, documentos);
		facesContext.responseComplete();
	}

	private void gerarPdf(HttpServletRequest request, HttpServletResponse response, List<ProcessoDocumento> documentos)  {
		OutputStream out = null;
		try {
			out = response.getOutputStream();
			String resourcePath = request.getScheme() + "://"
					+ request.getServerName() + ":" + request.getServerPort()
					+ request.getContextPath();
			GeradorPdfUnificado geradorPdf = new GeradorPdfUnificado();
			geradorPdf.setResurcePath(resourcePath);
	
			geradorPdf.gerarPdfSimples(documentos, out);
			
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
	

	
	


	
	// GETTERs AND SETTERs

	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	

	public ExpedicaoExpedienteEnum getMeioComunicacao() {
		return meioComunicacao;
	}

	public void setMeioComunicacao(ExpedicaoExpedienteEnum meioComunicacao) {
		this.meioComunicacao = meioComunicacao;
	}
 




	public Map<ProcessoDocTarefa, Boolean> getExpedientesCheck() {
		return expedientesCheck;
	}

	public void setExpedientesCheck(Map<ProcessoDocTarefa, Boolean> expedientesCheck) {
		this.expedientesCheck = expedientesCheck;
	}



	public TipoProcessoDocumento getTipoProcessoDocumento() {
		return tipoProcessoDocumento;
	}

	public void setTipoProcessoDocumento(TipoProcessoDocumento tipoProcessoDocumento) {
		this.tipoProcessoDocumento = tipoProcessoDocumento;
	}
	public FiltroTempoAgrupadoresEnum[] getFiltrosTempoAgrupadores() {
		return FiltroTempoAgrupadoresEnum.values();
	}
	

	public Date getDataInicioJuntada() {
		return dataInicioJuntada;
	}

	public void setDataInicioJuntada(Date dataInicioJuntada) {
		this.dataInicioJuntada = dataInicioJuntada;
	}

	public Date getDataFimJuntada() {
		return dataFimJuntada;
	}

	public void setDataFimJuntada(Date dataFimJuntada) {
		this.dataFimJuntada = dataFimJuntada;
	}

	public Boolean getExpedientesCheckAll() {
		return expedientesCheckAll;
	}

	public void setExpedientesCheckAll(Boolean expedientesCheckAll) {
		this.expedientesCheckAll = expedientesCheckAll;
	}


	public String getDocImpressos() {
		return docImpressos;
	}

	public void setDocImpressos(String docImpressos) {
		this.docImpressos = docImpressos;
	}


	public Date getDataInicioExpediente() {
		return dataInicioExpediente;
	}
	public void setDataInicioExpediente(Date dataInicioExpediente) {
		this.dataInicioExpediente = dataInicioExpediente;
	}

	public Date getDataFimExpediente() {
		return dataFimExpediente;
	}

	public void setDataFimExpediente(Date dataFimExpediente) {
		this.dataFimExpediente = dataFimExpediente;
	}
	
	public class ProcessoDocTarefa {
		private int idProcessoDocumento;
		private String tpDocumento;
		private String numeroProcesso;
		private Timestamp dtJuntada;
		private ExpedicaoExpedienteEnum meioExpedicaoExpediente = ExpedicaoExpedienteEnum.E;
		private int idProcessoDocumentoBin;
		private Timestamp dtInicioExpediente;
		private int idProcessoParteExpediente;
		
		public int getIdProcessoDocumento() {
			return idProcessoDocumento;
		}
		public void setIdProcessoDocumento(int idProcessoDocumento) {
			this.idProcessoDocumento = idProcessoDocumento;
		}
		public String getTpDocumento() {
			return tpDocumento;
		}
		public void setTpDocumento(String tpDocumento) {
			this.tpDocumento = tpDocumento;
		}
		public String getNumeroProcesso() {
			return numeroProcesso;
		}
		public void setNumeroProcesso(String numeroProcesso) {
			this.numeroProcesso = numeroProcesso;
		}
		public Timestamp getDtJuntada() {
			return dtJuntada;
		}
		public void setDtJuntada(Timestamp dtJuntada) {
			this.dtJuntada = dtJuntada;
		}
		public ExpedicaoExpedienteEnum getMeioExpedicaoExpediente() {
			return meioExpedicaoExpediente;
		}
		public void setMeioExpedicaoExpediente(ExpedicaoExpedienteEnum meioExpedicaoExpediente) {
			this.meioExpedicaoExpediente = meioExpedicaoExpediente;
		}
		public int getIdProcessoDocumentoBin() {
			return idProcessoDocumentoBin;
		}
		public void setIdProcessoDocumentoBin(int idProcessoDocumentoBin) {
			this.idProcessoDocumentoBin = idProcessoDocumentoBin;
		}
		public Timestamp getDtInicioExpediente() {
			return dtInicioExpediente;
		}
		public void setDtInicioExpediente(Timestamp dtInicioExpediente) {
			this.dtInicioExpediente = dtInicioExpediente;
		}
		public int getIdProcessoParteExpediente() {
			return idProcessoParteExpediente;
		}
		public void setIdProcessoParteExpediente(int idProcessoParteExpediente) {
			this.idProcessoParteExpediente = idProcessoParteExpediente;
		}
	
		
	}




	public List<ProcessoDocTarefa> getListProcDocTarefa() {
		return listProcDocTarefa;
	}

	public void setListProcDocTarefa(List<ProcessoDocTarefa> listProcDocTarefa) {
		this.listProcDocTarefa = listProcDocTarefa;
	}

	public int getCountDoc() {
		return countDoc;
	}

	public void setCountDoc(int countDoc) {
		this.countDoc = countDoc;
	}

	@Override
	protected BaseManager<ProcessoDocumento> getManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EntityDataModel<ProcessoDocumento> getModel() {
		// TODO Auto-generated method stub
		return null;
	}
}