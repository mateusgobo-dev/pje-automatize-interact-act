/*
 IBPM - Ferramenta de produtividade Java
 Copyright (c) 1986-2009 Infox Tecnologia da Informação Ltda.

 Este programa é software livre; você pode redistribuí-lo e/ou modificá-lo 
 sob os termos da GNU GENERAL PUBLIC LICENSE (GPL) conforme publicada pela 
 Free Software Foundation; versão 2 da Licença.
 Este programa é distribuído na expectativa de que seja útil, porém, SEM 
 NENHUMA GARANTIA; nem mesmo a garantia implícita de COMERCIABILIDADE OU 
 ADEQUAÇÃO A UMA FINALIDADE ESPECÍFICA.
 
 Consulte a GNU GPL para mais detalhes.
 Você deve ter recebido uma cópia da GNU GPL junto com este programa; se não, 
 veja em http://www.gnu.org/licenses/   
 */
package br.com.infox.ibpm.home;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.el.ELException;

import org.apache.commons.lang.StringEscapeUtils;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.bpm.TaskInstance;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.Base64;
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.home.ProcessoDocumentoBinHome;
import br.com.infox.cliente.home.ProcessoDocumentoHome;
import br.com.infox.ibpm.component.tree.AutomaticEventsTreeHandler;
import br.com.infox.ibpm.component.tree.EventsTipoDocumentoTreeHandler;
import br.com.infox.ibpm.home.api.IProcessoDocumentoBinHome;
import br.com.itx.component.AbstractHome;
import br.com.itx.component.FileHome;
import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.manager.DocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinManager;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBinPessoaAssinatura;

public abstract class AbstractProcessoDocumentoHome<T> extends AbstractHome<ProcessoDocumento> {

	private static final long serialVersionUID = 1L;
	public static final String PETICAO_INSERIDA = "peticaoInseridaMap";
	private static final LogProvider log = Logging.getLogProvider(ProcessoDocumentoHome.class);
	private ModeloDocumento modeloDocumentoCombo;
	private boolean isTruePanelRecibo = Boolean.FALSE;
	private boolean isModelo = Boolean.TRUE;
	private SimpleDateFormat dfCodData = new SimpleDateFormat("HHmmssSSS");
	private Integer idDocumentoRerender;
	private String numeroHash;
	private String documento;
	private String idAgrupamentos;
	private Boolean renderEventTree = Boolean.FALSE;
	private static final String URL_DOWNLOAD_PROCESSO_DOCUMENTO_EXPRESSION = "/downloadProcessoDocumento.seam?id={0}&codIni={1}&md5={2}&isBin={3}";
	
	private ProcessoDocumentoBinManager processoDocumentoBinManager = (ProcessoDocumentoBinManager) Component.getInstance("processoDocumentoBinManager", ScopeType.EVENT);

	public boolean getModelo() {
		return isModelo;
	}

	public void setModelo(boolean isModelo) {
		this.isModelo = isModelo;
	}

	public void setIsTruePanelRecibo(boolean isTruePanelRecibo) {
		this.isTruePanelRecibo = isTruePanelRecibo;
	}

	public Boolean getIsTruePanelRecibo() {
		return isTruePanelRecibo;
	}

	public ModeloDocumento getModeloDocumentoCombo() {
		return modeloDocumentoCombo;
	}

	public void setModeloDocumentoCombo(ModeloDocumento modeloDocumentoCombo) {
		this.modeloDocumentoCombo = modeloDocumentoCombo;
	}

	public void setProcessoDocumentoIdProcessoDocumento(Integer id) {
		setId(id);
	}

	public Integer getProcessoDocumentoIdProcessoDocumento() {
		return (Integer) getId();
	}

	@Override
	protected ProcessoDocumento createInstance() {
		ProcessoDocumento processoDocumento = new ProcessoDocumento();
		ProcessoHome processoHome = ComponentUtil.getComponent(ProcessoHome.class, false);
		if (processoHome != null) {
			processoDocumento.setProcesso(processoHome.getDefinedInstance());
		}
		return processoDocumento;
	}

	@Override
	public String remove() {
		ProcessoHome processo = ComponentUtil.getComponent(ProcessoHome.class, false);
		if (processo != null) {
			processo.getInstance().getProcessoDocumentoList().remove(instance);
		}
		return super.remove();
	}

	@Override
	public String remove(ProcessoDocumento obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("processoDocumentoGrid");
		return ret;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String persist() {
		IProcessoDocumentoBinHome procDocBinHome = ComponentUtil.getComponent(ProcessoDocumentoBinHome.class);
		procDocBinHome.isModelo(isModelo);
		if (procDocBinHome.persist() == null) {
			return null;
		}
		getInstance().setProcessoDocumentoBin(procDocBinHome.getInstance());
		getInstance().setUsuarioInclusao(Authenticator.getUsuarioLogado());
		getInstance().setProcesso(ProcessoHome.instance().getInstance());
		setJbpmTask();
		setValido();
		if(getInstance().getDataJuntada() == null && !((getInstance().getProcessoDocumentoBin()).getSignatarios()).isEmpty()){
			getInstance().setDataJuntada(new Date());
		}
		String ret = super.persist();
		if (ret != null) {
			setIdDocumentoRerender(getInstance().getIdProcessoDocumento());
			if (isModelo) {
				List<Integer> lista = (List<Integer>) Contexts.getSessionContext().get(PETICAO_INSERIDA);
				if (lista == null) {
					lista = new ArrayList<Integer>();
				}
				lista.add(getInstance().getProcesso().getIdProcesso());
				Contexts.getSessionContext().set(PETICAO_INSERIDA, lista);
			}
		}
		refreshGrid();
		return ret;
	}

	/**
	 * [PJEII-2350] Inclusão do método para verificar se o documento anexado esta valido.
	 * @return boolean
	 */
	public boolean estaValido() {
		if (getInstance() == null || getInstance().getProcessoDocumentoBin() == null || getInstance().getTipoProcessoDocumento() == null) {
			return false;
		}
		return processoDocumentoBinManager
				.verificaValidacao(getInstance().getProcessoDocumentoBin(), getInstance().getProcessoDocumentoBin()
						.getSignatarios(), getInstance().getTipoProcessoDocumento());
	}

	
	/**
	 * [PJEII-2350] Inclusão do método para verificar se o documento anexado esta valido.
	 * @param pdb
	 * @return boolean
	 */
	public boolean estaValido(ProcessoDocumentoBin pdb) {
		return processoDocumentoBinManager
				.verificaValidacao(pdb, pdb.getSignatarios(), getInstance().getTipoProcessoDocumento());
	}

	/**
	 * [PJEII-2350] Inclusão do método para verificar se o documento anexado esta valido.
	 * @param pd
	 * @return boolean
	 */
	public boolean estaValido(ProcessoDocumento pd) {
		return processoDocumentoBinManager
				.verificaValidacao(pd.getProcessoDocumentoBin(), pd.getProcessoDocumentoBin().getSignatarios(), pd
						.getTipoProcessoDocumento());
	}
	
	/**
	 * [PJEII-3785] Inclusão de método de validação do documento no momento da assinatura.
	 * @param pdb 
	 * @param novaAssinatura 
	 * @return
	 */
	public boolean estaValido(ProcessoDocumentoBin pdb, ProcessoDocumentoBinPessoaAssinatura novaAssinatura) {
		List<ProcessoDocumentoBinPessoaAssinatura> listaAssinaturas = pdb.getSignatarios();
		if (listaAssinaturas == null || listaAssinaturas.isEmpty()) {
			listaAssinaturas = new ArrayList<ProcessoDocumentoBinPessoaAssinatura>();
		}
		listaAssinaturas.add(novaAssinatura);
		
		return processoDocumentoBinManager.verificaValidacao(pdb, listaAssinaturas, getInstance().getTipoProcessoDocumento());
	}
	
	/**
	 * [PJEII-2350] Alteração da verificação do documento anexado, o qual verifica se o documento esta valido.
	 */
	public void setValido() {
		if (getInstance().getProcessoDocumentoBin() != null && !getInstance().getProcessoDocumentoBin().getValido()) {
			getInstance().getProcessoDocumentoBin().setValido(estaValido());
		}
	}

	public String persistSemLista() {
		setValido();
		return super.persist();
	}

	@Override
	public void newInstance() {
		IProcessoDocumentoBinHome procDocBin = ComponentUtil.getComponent(ProcessoDocumentoBinHome.class);
		procDocBin.newInstance();
		super.newInstance();
	}

	protected void refreshGrid() {
		refreshGrid("documentoProcessoGrid");
	}

	protected void setJbpmTask() {
		if (TaskInstance.instance() != null) {
			long idJbpmTask = TaskInstance.instance().getId();
			getInstance().setIdJbpmTask(idJbpmTask);
			getInstance().setExclusivoAtividadeEspecifica(Boolean.TRUE);
		}
	}

	public void processarModelo() {
		if (modeloDocumentoCombo != null) {
			ModeloDocumento modeloDocumento = getEntityManager().merge(modeloDocumentoCombo);
			IProcessoDocumentoBinHome procDocBinHome = ComponentUtil.getComponent(ProcessoDocumentoBinHome.class);
			String conteudoModelo = processarModelo(modeloDocumento.getModeloDocumento());
			procDocBinHome.getInstance().setModeloDocumento(conteudoModelo);
			ProcessoHome.instance().getProcessoDocumentoBin().setModeloDocumento(conteudoModelo);
		}
	}

	/**
	 * Processa um modelo avaliando linha a linha.
	 * 
	 * @param modelo
	 * @return
	 */
	public static String processarModelo(String modelo) {
		if (modelo != null) {
			StringBuilder modeloProcessado = new StringBuilder();
			String[] linhas = modelo.split("\n");
			for (int i = 0; i < linhas.length; i++) {
				if (modeloProcessado.length() > 0) {
					modeloProcessado.append('\n');
				}
				Object o = null;
				try {
					o = interpretarLinhaElComTratamentoParaCaracteresEscapeHtml(linhas[i]);
					modeloProcessado.append(o);
					
				} catch (RuntimeException e) {
					modeloProcessado.append("Erro ao avaliar expressão na linha: '" + linhas[i] + "': " + e.getMessage());
					e.printStackTrace();
				}
			}
			return modeloProcessado.toString();
		}
		return modelo;
	}

	public static String interpretarLinhaElComTratamentoParaCaracteresEscapeHtml(String linha) {
		try {
			return (String) Expressions.instance().createValueExpression(linha).getValue();
		} catch (ELException e) {
			if (e.getCause() instanceof org.jboss.el.parser.ParseException) {
				String linhaSemScape = StringEscapeUtils.unescapeHtml(linha);
				if(linhaSemScape != null && !linhaSemScape.equals(linha)) {
					String retorno = interpretarLinhaElComTratamentoParaCaracteresEscapeHtml(
							linhaSemScape);
					return escapeHtmlExceptTags(retorno);
				}
				
			} 
			throw e;
		}
	}


	private static String escapeHtmlExceptTags(String retorno) {
		if(retorno == null) {
			retorno = "";
		}
		retorno = StringEscapeUtils.escapeHtml(retorno);
		return retorno.replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&quot;","\"").replaceAll("&quot;", "\"");
	}
	
	public void setIdDocumentoRerender(Integer idDocumentoRerender) {
		this.idDocumentoRerender = idDocumentoRerender;
	}

	public Integer getIdDocumentoRerender() {
		return idDocumentoRerender;
	}

	private boolean isCodDataValido(String codIni, ProcessoDocumento pd) {
		String codData = getCodData(pd);
		if (Strings.isEmpty(codIni) || Strings.isEmpty(codData)) {
			return false;
		} else {
			return codData.equals(codIni);
		}
	}

	public String getCodData(ProcessoDocumento pd) {
		return dfCodData.format(pd.getDataInclusao());
	}

	public String getUrlDownloadProcessoDocumento(ProcessoDocumento processoDocumento) {
		ProcessoDocumentoBin processoDocumentoBin = processoDocumento.getProcessoDocumentoBin();
		boolean isBin = processoDocumentoBin.getNomeArquivo() != null;
		String retorno = MessageFormat.format(URL_DOWNLOAD_PROCESSO_DOCUMENTO_EXPRESSION,
				Integer.toString(processoDocumento.getIdProcessoDocumento()), getCodData(processoDocumento),
				processoDocumentoBin.getMd5Documento(), isBin);
		return new Util().getUrlProject() + retorno;
	}

	/**
	 * Faz validações de segurança antes de baixar do documento e preencher os
	 * dados do fileHome.
	 * 
	 * @param id
	 *            - id do ProcessoDocumento
	 * @param codIni
	 *            - String da data de inclusao no formato <code>HHmmssSSS</code>
	 * @param md5
	 *            - Md5 do ProcessoDocumentoBin
	 * @throws Exception
	 */
	public void downloadDocumento(Integer id, String codIni, String md5) throws Exception {
		FileHome fileHome = FileHome.instance();
		ProcessoDocumento pd = getEntityManager().find(ProcessoDocumento.class, id);
		if (pd == null) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Processo não encontrado: " + id);
			return;
		} else if (!isCodDataValido(codIni, pd)) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Codigo de verificação inválido.");
			return;
		}
		ProcessoDocumentoBin bin = pd.getProcessoDocumentoBin();
		if (!bin.getMd5Documento().equals(md5)) {
			DocumentoJudicialService documentoJudicialService = ComponentUtil.getComponent(DocumentoJudicialService.class);
			documentoJudicialService.updateMD5(bin);
			if(!bin.getMd5Documento().equals(md5)){
				FacesMessages.instance().add(StatusMessage.Severity.ERROR, "O md5 não bate com o do documento.");
				return;
			}
		}

		byte[] data = null;
		boolean isBin = bin.isBinario();
		if (bin.isBinario()) {
			data = DocumentoBinManager.instance().getData(pd.getProcessoDocumentoBin().getNumeroDocumentoStorage());
		} else {
			data = bin.getModeloDocumento().getBytes();
		}
		fileHome.setData(data);
		fileHome.setFileName(isBin ? bin.getNomeArquivo() : pd.getProcessoDocumento() + ".html");
	}

	public String getDocumentoBase64()throws Exception{
		if (getInstance() == null || !getInstance().getProcessoDocumentoBin().isBinario()) {
			return null;
		} else {
			byte[] binario = DocumentoBinManager.instance().getData(getInstance().getProcessoDocumentoBin().getNumeroDocumentoStorage());
			return binario != null ? Base64.encodeBytes(binario) : null;
		}
	}

	public void onSelectProcessoDocumento() {
		AutomaticEventsTreeHandler.instance().clearTree();
		AutomaticEventsTreeHandler.instance().clearList();
		EventsTipoDocumentoTreeHandler.instance().clearTree();
		EventsTipoDocumentoTreeHandler.instance().clearList();
		renderEventTree = false;
		if (instance.getTipoProcessoDocumento() != null && instance.getTipoProcessoDocumento().getAgrupamento() != null) {
			idAgrupamentos = Integer.toString(instance.getTipoProcessoDocumento().getAgrupamento().getIdAgrupamento());
			if (!"".equals(getIdAgrupamentos())) {
				renderEventTree = true;
			}
		}
	}

	public void setNumeroHash(String numeroHash) {
		this.numeroHash = numeroHash;
	}

	public String getNumeroHash() {
		return numeroHash;
	}

	@Override
	public String update() {
		String ret = null;
		IProcessoDocumentoBinHome procDocBinHome = ComponentUtil.getComponent(ProcessoDocumentoBinHome.class);
		if (procDocBinHome.update() != null) {
			ret = super.update();
			refreshGrid();
		}
		return ret;
	}

	public String getDocumento() {
		return documento;
	}

	public void setDocumento(String documento) {
		this.documento = documento;
	}

	public Boolean getRenderEventTree() {
		return renderEventTree;
	}

	public String labelTipoProcessoDocumento() {
		return "Tipo do Documento";
	}

	public String getIdAgrupamentos() {
		return idAgrupamentos;
	}

}