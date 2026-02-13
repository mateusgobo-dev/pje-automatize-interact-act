package br.com.infox.cliente.home;

import java.util.Arrays;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.cnj.pje.nucleo.service.PessoaFisicaService;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.servicos.PrazosProcessuaisService;
import br.jus.cnj.pje.servicos.prazos.Calendario;
import br.jus.cnj.pje.view.ProtocolarDocumentoBean;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoExpDocCertidao;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;

@Name(ProcessoExpDocCertidaoHome.NAME)
public class ProcessoExpDocCertidaoHome extends AbstractHome<ProcessoExpDocCertidao> implements ArquivoAssinadoUploader {
	
	private static final long serialVersionUID = 1L;
	public static final String NAME = "processoExpDocCertidaoHome";
	private ProcessoDocumento processoDocumentoCertidao = new ProcessoDocumento();
	private ProcessoParteExpediente ppe;
	private boolean documentoAssinado = false;
	private boolean novoCadastro = true;
	private ProtocolarDocumentoBean protocolarDocumentoBean;
	private ArquivoAssinadoHash arquivoAssinado;
	
	@Logger
	private Log logger;
	
	@In
	private PessoaFisicaService pessoaFisicaService;
	
	@In
	private UsuarioService usuarioService;
	
	@In
	private ProcessoParteExpedienteManager processoParteExpedienteManager;
	
	@In(create = true)
	private PrazosProcessuaisService prazosProcessuaisService;
	
	/**
	 * Retorna uma certidão de uma parte do processo, usando como parametro uma
	 * parte expediente.
	 * 
	 * @param Processo
	 *            parte expediente esperado, retorna null caso não encontre
	 *            nenhuma certidão para aquele processo parte.
	 * @return Retorna o processso documento do tipo certidão para a parte
	 *         solicitada.
	 */
	public ProcessoDocumento retornaCertidao(ProcessoParteExpediente ppe) {		
		ProcessoExpDocCertidao classe = null;
		try {
		classe = (ProcessoExpDocCertidao) EntityUtil.getEntityManager()
				.createQuery("select p from ProcessoExpDocCertidao p where p.processoDocumentoCertidao.ativo = true and p.processoParteExpediente = :ppe")
				.setParameter("ppe", ppe)
				.setFirstResult(0)
				.setMaxResults(1)
				.getSingleResult();
		} catch (Exception e) {
			// necessario para noresultexception, nao faz nada.
		}
		
		if (classe != null) {
			return classe.getProcessoDocumentoCertidao();
		} else {
			return null;
		}
	}
	
	public Boolean expedientePossuiCertidaoAssinada(ProcessoParteExpediente ppe){

		Boolean retorno = Boolean.FALSE;
		ProcessoDocumento procDoc = null;
		
		procDoc = retornaCertidao(ppe);
		if(procDoc != null && procDoc.getProcessoDocumentoBin() != null && procDoc.getProcessoDocumentoBin().getSignatarios().size() > 0 ){
			retorno = Boolean.TRUE;
		}
		
		return retorno; 
	}

	public void carregaCertidao(ProcessoParteExpediente ppeRow) {
		// Guarda o processo parte expediemte selecionado para posterior uso
		setPpe(ppeRow);

		// pega o documento certidao da parte e seta no atributo
		// ProcessoDocumentoCertidao
		setProcessoDocumentoCertidao(retornaCertidao(ppeRow));

		// limpa processo documento bin
		ProcessoDocumentoBinHome.instance().clearInstance();
		ProcessoDocumentoHome.instance().clearInstance();

		// verifica se foi encontrado documento certidão para aquela parte
		// expediente caso nao tenha sido encontrada cria um novo
		// processoDocumento do tipo certidão
		if (null == getProcessoDocumentoCertidao()) {
			this.setNovoCadastro(true);
			setDocumentoAssinado(false);
			ProcessoDocumentoHome.instance().setModeloDocumentoLocalTemp(null);
			ProcessoDocumento pd = new ProcessoDocumento();
			pd.setProcesso(ppeRow.getProcessoDocumento().getProcesso());
			pd.setTipoProcessoDocumento(ParametroUtil.instance().getTipoProcessoDocumentoCertidao());
			pd.setProcessoDocumentoBin(ProcessoDocumentoBinHome.instance().getInstance());
			setProcessoDocumentoCertidao(pd);
		} else {
			this.setNovoCadastro(false);
			ProcessoDocumentoHome.instance().setInstance(processoDocumentoCertidao);
			ProcessoDocumentoBinHome.instance().setInstance(processoDocumentoCertidao.getProcessoDocumentoBin());
			setDocumentoAssinado(ProcessoDocumentoHome.instance().isAssinado());
		}
	}

	/**
	 * Cadastra uma nova certidão para uma parte expediente.
	 * 
	 * @param
	 */
	public void cadastrarCertidaoParte() {
		try {
			// [PJEII-851] - Necessário para recuperação do processo em processoHome. 
			ProcessoHome pHome = ProcessoHome.instance();
			pHome.setInstance(ppe.getProcessoExpediente().getProcessoTrf().getProcesso());
			
			ProcessoDocumentoHome pdHome = ProcessoDocumentoHome.instance();
			
			// [PJEII-851] - Recuperação do processo a partir de ProcessoParteExpediente. 
			pdHome.getInstance().setProcesso(ppe.getProcessoExpediente().getProcessoTrf().getProcesso());
			
			pdHome.getInstance().setTipoProcessoDocumento(getProcessoDocumentoCertidao().getTipoProcessoDocumento());
			pdHome.getInstance().setProcessoDocumento("Certidão");
			pdHome.getInstance().setDataInclusao(new Date());
			ProcessoDocumentoBinHome.instance().setIgnoraConteudoDocumento(true);
			pdHome.persist();
			FacesMessages.instance().clear();

			getInstance().setProcessoDocumentoCertidao(pdHome.getInstance());
			getInstance().setProcessoParteExpediente(ppe);
			getInstance().getProcessoDocumentoCertidao().setProcesso(ppe.getProcessoExpediente().getProcessoTrf().getProcesso());
			super.persist();
			
			// [PJEII-851] - Correção de código morto. 
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, "Certidão cadastrada com sucesso.");
			
		} catch (Exception e) {
			e.printStackTrace();
			FacesMessages.instance().add(Severity.ERROR, "Erro ao gravar o documento.");
		}
	}

	/**
	 * Atualizar certidão de uma parte.
	 */
	public void updateCertidaoParte() {
		ProcessoDocumentoHome pdh = ProcessoDocumentoHome.instance();
		pdh.update();
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, "Documento alterado com sucesso.");
	}

	public void assinarCertidaoParte() {
		if (Strings.isEmpty(ProcessoDocumentoBinHome.instance().getInstance().getModeloDocumento())) {
			FacesMessages.instance().add(Severity.ERROR,
					"Para assinatura do documento, é necessário que tenha algum conteúdo no editor.");
			return;
		}
		
		cadastraOuEditaCertidao();
		
		// verifica se o documento tem conteudo para permitir assinatura
		if (!ProcessoDocumentoBinHome.instance().isModeloVazio()) {
			// atualiza parte expediente
			
			/*
			 *  [PJEII-6405] O fim do prazo legal não estava sendo calculado corretamente
			 *  ao assinar a certidão de ciência do expediente. Chamar 
			 *  atoComunicacaoService.registraCienciaPessoal() não resolve o problema,
			 *  pois esse método faz a verificação se a pessoa é apta ou não para dar ciência.
			 *  No contexto deste método, essa regra de apto para ciência é feita no .xhtml.
			 *  O ícone só aparece para quem puder dar ciência no expediente.
			 */
				
			OrgaoJulgador o = ppe.getProcessoJudicial().getOrgaoJulgador();
			Calendario calendario = prazosProcessuaisService.obtemCalendario(o);
			FacesMessages.instance().clear();
			Pessoa p = (Pessoa) pessoaFisicaService.find(usuarioService.getUsuarioLogado().getIdUsuario());
			ppe.setCienciaSistema(false);
			if(p != null && ppe.getDtCienciaParte() == null){
				ppe.setNomePessoaCiencia(p.getNome());
				ppe.setPessoaCiencia(p);
			}
				
			processoParteExpedienteManager.registraCiencia(ppe, new Date(), false, calendario);

			FacesMessages.instance().clear();
			if (this.arquivoAssinado != null) {
				ProcessoDocumentoBinHome.instance().setCertChain(arquivoAssinado.getCadeiaCertificado());
				ProcessoDocumentoBinHome.instance().setSignature(arquivoAssinado.getAssinatura());
			}
			if(ProcessoDocumentoHome.instance().getInstance().getDataJuntada() == null){
			    ProcessoDocumentoHome.instance().getInstance().setDataJuntada(new Date());
			}
			ProcessoDocumentoBinHome.instance().assinarDocumento();
			setDocumentoAssinado(true);
			refreshGrid("processoParteExpedienteMenuGrid");
		} else {
			FacesMessages.instance().add(Severity.ERROR,
					"Para assinatura do documento, é necessário que tenha algum conteúdo no editor.");
		}
	}

	private void cadastraOuEditaCertidao(){
		// edita caso ja exita o documento
		if (!novoCadastro) {
			try {
				updateCertidaoParte();
				FacesMessages.instance().clear();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			// cadastra caso o documento não exista
			try {
				cadastrarCertidaoParte();
				FacesMessages.instance().clear();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}		
	}

	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash) throws Exception {
		this.arquivoAssinado = arquivoAssinadoHash;
	}
	
	@Override
	public String getActionName() {
		return NAME;
	}
		
	public String getDownloadLinks() {
		return DocumentoJudicialService.instance().getDownloadLinks(Arrays.asList(ProcessoDocumentoHome.instance().getInstance()));
	}
	
	public ProtocolarDocumentoBean getProtocolarDocumentoBean() {
		if(protocolarDocumentoBean == null){
			protocolarDocumentoBean =  new ProtocolarDocumentoBean(this.instance.getProcessoParteExpediente().getProcessoJudicial().getIdProcessoTrf(), 
					true, false, true, true, false, false, false, NAME);
		}
		return protocolarDocumentoBean;
	}
	
	public void setProcessoDocumentoCertidao(ProcessoDocumento processoDocumentoCertidao) {
		this.processoDocumentoCertidao = processoDocumentoCertidao;
	}

	public ProcessoDocumento getProcessoDocumentoCertidao() {
		return processoDocumentoCertidao;
	}

	public void setPpe(ProcessoParteExpediente ppe) {
		this.ppe = ppe;
	}

	public ProcessoParteExpediente getPpe() {
		return ppe;
	}

	public void setDocumentoAssinado(boolean documentoAssinado) {
		this.documentoAssinado = documentoAssinado;
	}

	public boolean getDocumentoAssinado() {
		return documentoAssinado;
	}

	public void setNovoCadastro(boolean novoCadastro) {
		this.novoCadastro = novoCadastro;
	}

	public boolean getNovoCadastro() {
		return novoCadastro;
	}

}