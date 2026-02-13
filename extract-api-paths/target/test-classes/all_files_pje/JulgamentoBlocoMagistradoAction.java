/**
 * pje-comum
 * Copyright (C) 2009-2015 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.cnj.pje.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;

import br.com.infox.cliente.home.SessaoHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.view.CkEditorNaoGeraDocumentoAbstractAction;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.je.pje.business.dto.RespostaDTO;
import br.jus.je.pje.business.dto.RespostaTiposVotoDTO;
import br.jus.je.pje.business.dto.TipoVotoDTO;
import br.jus.pje.nucleo.entidades.BlocoJulgamento;
import br.jus.pje.nucleo.entidades.NotaSessaoBloco;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoBloco;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoVoto;
import br.jus.pje.nucleo.entidades.VotoBloco;
import br.jus.pje.nucleo.enums.TipoSituacaoPautaBlocoEnum;

/**
 * Classe de controle para realização do julgamento em bloco  
 *
 */
@Name(JulgamentoBlocoMagistradoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class JulgamentoBlocoMagistradoAction extends CkEditorNaoGeraDocumentoAbstractAction implements Serializable, ArquivoAssinadoUploader {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean mostrarDivergentes;
	public static final String NAME = "julgamentoBlocoMagistradoAction";
	private List<BlocoJulgamento> blocosSessao;
	private List<ProcessoBloco> processos;
	private boolean relator;
	private boolean expandirBloco;
	private BlocoJulgamento bloco;
	private String nomeBloco;
	private String conteudoVoto = "";
	private String conteudoEmenta = "";
	private String conteudoRelatorio = "";
	private boolean editarVoto;
	private boolean exibirDocumentosVoto;
	private int idBloco;
	private Sessao sessao;
	private TipoProcessoDocumento tipoSelecionado;
	private TipoVoto tipoVoto;
	private List<ArquivoAssinadoHash> arquivosAssinados = new ArrayList<ArquivoAssinadoHash>();
	private List<TipoVoto> tiposVoto = new ArrayList<TipoVoto>();
    private List<OrgaoJulgador> votoDivergenteList = new ArrayList<OrgaoJulgador>();
    private OrgaoJulgador orgaoJulgadorAcompanhado;

  	
  	public boolean verificarExistenciaBlocos(Sessao sessao) {
  		boolean retorno = false;
  		List<BlocoJulgamento> blocos = ComponentUtil.getBlocoJulgamentoManager().findBySessao(sessao);
  		if( blocos != null ) {
  			retorno = blocos.size() > 0;
  		}
  		return retorno;
  	}
  	
  	public long recuperarQuantidadeProcessos(BlocoJulgamento blocoqtd) {
  		return ComponentUtil.getProcessoBlocoManager().recuperarQuantidadeProcessos(blocoqtd);
  	}
  	
  	public boolean verificarInclusaoBloco(ProcessoTrf processo, Sessao sessao) {
  		return (ComponentUtil.getBlocoJulgamentoManager().pesquisar(processo, sessao) != null );
  	}
  	
  	public void exibirDetalhesBloco(BlocoJulgamento blocoParametro) {
  		if(expandirBloco && blocoParametro == bloco) {
  			setExpandirBloco(false);
  		} else {
  			setExpandirBloco(true);
  			setBloco(blocoParametro);
  		}
  	}
  	
  	public List<ProcessoBloco> recuperarProcessos() {
  		List<ProcessoBloco> processosBloco = null;
  		if(bloco != null) {
			try {
				processosBloco = ComponentUtil.getProcessoBlocoManager().recuperarProcessos(bloco);
			} catch (Exception e) {
				FacesMessages.instance().add(Severity.ERROR, e.getLocalizedMessage());
			}  		
  		}
  		return processosBloco;
		
	}  	
  	
  	public boolean excluirProcesso(ProcessoBloco processoBloco) {
  		boolean retorno = false;
  		try {
			ComponentUtil.getProcessoBlocoManager().remove(processoBloco);
			ComponentUtil.getProcessoBlocoManager().flush();
			retorno = true;
		} catch (PJeBusinessException e) {
			FacesMessages.instance().add(Severity.ERROR, e.getLocalizedMessage());
			retorno = false;
		}
  		
  		return retorno;
  	}
  	
	public void alterarSituacao(String descricaoSituacao) {
		try {
			TipoSituacaoPautaBlocoEnum situacaoNova = TipoSituacaoPautaBlocoEnum.getEnum(descricaoSituacao);
			ComponentUtil.getBlocoJulgamentoManager().atualizarBlocoJulgamento(bloco, situacaoNova);
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, "Situação do bloco de julgamento alterada com sucesso");
		} catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR, e.getLocalizedMessage());
		}
	}
	
	public String recuperarTextoVoto() {
		String retorno = "";
		if(bloco != null && bloco.getAgruparOrgaoJulgador() && bloco.getOrgaoJulgadorRelator() != null ) {
			VotoBloco voto = ComponentUtil.getVotoBlocoManager().recuperarVoto(bloco, bloco.getOrgaoJulgadorRelator());
			if(voto != null) {
				retorno = voto.getTipoVoto().getTipoVoto();
			}
		} else {
			if(bloco.getVotoRelator() != null) {
				retorno = bloco.getVotoRelator().getTipoVoto();
			}
		}
		return retorno;
	}
	
	public void removerVoto() {
		if(ComponentUtil.getPainelDoMagistradoNaSessaoAction().getOrgaoAtual() != null ) {
			VotoBloco voto = ComponentUtil.getVotoBlocoManager().recuperarVoto(bloco, ComponentUtil.getPainelDoMagistradoNaSessaoAction().getOrgaoAtual());
			try {
				ComponentUtil.getVotoBlocoManager().removerVoto(voto);
				tipoVoto = null;
				orgaoJulgadorAcompanhado = null;
			} catch (Exception e) {
				FacesMessages.instance().add(Severity.ERROR, e.getLocalizedMessage());
			}
		} else {
			FacesMessages.instance().add(Severity.INFO, "Não foi possível recuperar o órgão julgador atual do usuário" );
		}
	}
	
	public void votar() {
		try {
			ComponentUtil.getVotoBlocoManager().registrarVotoAgrupadoOrgaoJulgador(tipoVoto, this.bloco, ComponentUtil.getPainelDoMagistradoNaSessaoAction().getOrgaoAtual(), orgaoJulgadorAcompanhado);
		} catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR, e.getLocalizedMessage());
		}
	}
	
	public boolean verificarPresenca() {
		boolean retorno = false;
		if(ComponentUtil.getPainelDoMagistradoNaSessaoAction().getOrgaoAtual() != null ) {
			retorno = ComponentUtil.getBlocoComposicaoManager().verificarPresenca(bloco, ComponentUtil.getPainelDoMagistradoNaSessaoAction().getOrgaoAtual());
		}
		return retorno;
	}
	
	private SessaoProcessoDocumentoVoto recuperarVoto(ProcessoTrf processo, OrgaoJulgador orgao) {
		SessaoProcessoDocumentoVoto processoVoto = ComponentUtil.getSessaoProcessoDocumentoVotoManager().recuperarVoto(ComponentUtil.getPainelDoMagistradoNaSessaoAction().getSessao(), processo, orgao);
		if(processoVoto != null && processoVoto.getProcessoDocumento() != null) {
			setConteudoVoto(processoVoto.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento());
		}
		return processoVoto;
	}
	
	private void recuperarVotoExistenteNovo(ProcessoTrf processo, OrgaoJulgador orgao) {
		editarVoto = true;
		SessaoProcessoDocumentoVoto processoVoto = ComponentUtil.getSessaoProcessoDocumentoVotoManager().recuperarVotoExistenteNovo(ComponentUtil.getPainelDoMagistradoNaSessaoAction().getSessao(), processo, orgao);
		if(processoVoto != null ) {
			if( processoVoto.getProcessoDocumento() == null ) {
				ProcessoDocumento pdNovo;
				try {
					pdNovo = ComponentUtil.getProcessoDocumentoManager().registrarProcessoDocumento("Conteúdo em branco ", "Voto", tipoSelecionado, processo);
					processoVoto.setProcessoDocumento(pdNovo);
					//ComponentUtil.getSessaoProcessoDocumentoManager().mergeAndFlush(processoVoto);
				} catch (PJeBusinessException e) {
					FacesMessages.instance().add(Severity.ERROR, e.getLocalizedMessage());
				}
			}
			setConteudoVoto(processoVoto.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento());
		}
	}
	
	public void exibirDocumentosVoto(ProcessoTrf processo) {
		exibirDocumentosVoto = true;
		OrgaoJulgador relator;
		if(bloco.getAgruparOrgaoJulgador()) {
			relator = bloco.getOrgaoJulgadorRelator();
		} else {
			relator = processo.getOrgaoJulgador();
		}
		SessaoProcessoDocumentoVoto processoVoto = recuperarVoto(processo, relator);
		SessaoProcessoDocumento relatorio = recuperarRelatorio(processo, relator);
		SessaoProcessoDocumento ementa = recuperarEmenta(processo, relator);
		
		if(ComponentUtil.getPainelDoMagistradoNaSessaoAction().getSessao().getDataAberturaSessao() == null || ComponentUtil.getPainelDoMagistradoNaSessaoAction().getSessao().getDataRealizacaoSessao() != null) {
			if( relator != ComponentUtil.getPainelDoMagistradoNaSessaoAction().getOrgaoAtual()) {
				if( processoVoto == null || processoVoto.getLiberacao() == false ) {
					conteudoVoto = "";
					FacesMessages.instance().add(Severity.INFO, "Relator não proferiu ou não liberou o voto.");
				}
				if( relatorio == null || relatorio.getLiberacao() == false ) {
					FacesMessages.instance().add(Severity.INFO, "Relator não proferiu ou não liberou o relatório.");
					conteudoRelatorio = "";
				}
				if( ementa == null || ementa.getLiberacao() == false ) {
					FacesMessages.instance().add(Severity.INFO, "Relator não proferiu ou não liberou a ementa.");
					conteudoEmenta = "";
				}
			}
		}
	}
	
	private SessaoProcessoDocumento recuperarEmenta(ProcessoTrf processo, OrgaoJulgador orgao) {
		SessaoProcessoDocumento ementa = ComponentUtil.getSessaoProcessoDocumentoManager().recuperar(ComponentUtil.getPainelDoMagistradoNaSessaoAction().getSessao(), processo, orgao, ParametroUtil.instance().getTipoProcessoDocumentoEmenta());
		if(ementa!= null && ementa.getProcessoDocumento() != null ) {
			setConteudoEmenta(ementa.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento());
		}
		return ementa;
	}
	
	private SessaoProcessoDocumento recuperarRelatorio(ProcessoTrf processo, OrgaoJulgador orgao) {
		SessaoProcessoDocumento relatorio = ComponentUtil.getSessaoProcessoDocumentoManager().recuperar(ComponentUtil.getPainelDoMagistradoNaSessaoAction().getSessao(), processo, orgao, ParametroUtil.instance().getTipoProcessoDocumentoRelatorio());
		if(relatorio!= null && relatorio.getProcessoDocumento() != null ) {
			setConteudoRelatorio(relatorio.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento());
		}
		return relatorio;
	}

	public void definirDocumentoEmEdicao(int tipo) {
		if(tipo == ParametroUtil.instance().getTipoProcessoDocumentoVoto().getIdTipoProcessoDocumento()) {
			this.tipoSelecionado = ParametroUtil.instance().getTipoProcessoDocumentoVoto();
		} else if(tipo == ParametroUtil.instance().getTipoProcessoDocumentoEmenta().getIdTipoProcessoDocumento()) {
			this.tipoSelecionado = ParametroUtil.instance().getTipoProcessoDocumentoEmenta(); 
		} else if(tipo == ParametroUtil.instance().getTipoProcessoDocumentoRelatorio().getIdTipoProcessoDocumento()) {
			this.tipoSelecionado = ParametroUtil.instance().getTipoProcessoDocumentoRelatorio();
		} else {
			try {
				tipoSelecionado = ComponentUtil.getTipoProcessoDocumentoManager().findById(ParametroUtil.instance().getIdsTipoDocumentoVotoVogalPainelMagistrado()[0]);
			} catch (PJeBusinessException e) {
				FacesMessages.instance().add(Severity.ERROR, "Não foi possível recuperar o tipo de documento de voto do vogal. Mensagem interna: " + e.getLocalizedMessage());
			}
		}
	}

	public void redigirVoto() {
		editarVoto = true;
		if(ComponentUtil.getPainelDoMagistradoNaSessaoAction().getOrgaoAtual() != null && getProcessos() != null && processos.size() > 0) {
			OrgaoJulgador orgao = ComponentUtil.getPainelDoMagistradoNaSessaoAction().getOrgaoAtual();
			ProcessoTrf processo = processos.get(0).getProcessoTrf();
			recuperarVotoExistenteNovo(processo, orgao);
			try {
				tipoSelecionado = ComponentUtil.getTipoProcessoDocumentoManager().findById(ParametroUtil.instance().getIdsTipoDocumentoVotoVogalPainelMagistrado()[0]);
			} catch (PJeBusinessException e) {
				FacesMessages.instance().add(Severity.ERROR, "Não foi possível recuperar o tipo de documento de voto do vogal. Mensagem interna: " + e.getLocalizedMessage());
			}
		}
	}
	
	public void carregarDocumentos() {
		editarVoto = true;
		if(ComponentUtil.getPainelDoMagistradoNaSessaoAction().getOrgaoAtual() != null && getProcessos() != null && processos.size() > 0) {
			OrgaoJulgador orgao = ComponentUtil.getPainelDoMagistradoNaSessaoAction().getOrgaoAtual();
			ProcessoTrf processo = processos.get(0).getProcessoTrf();
			recuperarVotoExistenteNovo(processo, orgao);
			recuperarRelatorio(processo, orgao);
			recuperarEmenta(processo, orgao);
		}
		this.tipoSelecionado = ParametroUtil.instance().getTipoProcessoDocumentoRelatorio();
	}

	public boolean agrupadoEmBloco(SessaoPautaProcessoTrf sessaoPauta) {
		boolean retorno = false;
		if(sessaoPauta != null) {
			retorno = ComponentUtil.getProcessoBlocoManager().agrupadoEmBloco(getSessao(), sessaoPauta.getProcessoTrf());
		}
		return retorno;
	}
	
	public List<NotaSessaoBloco> getAnotacoes(){
		return ComponentUtil.getNotaSessaoBlocoManager().recuperar(bloco);
	}

	private List<ProcessoBloco> getProcessos() {
		if(processos == null ) {
			processos = ComponentUtil.getProcessoBlocoManager().recuperarProcessos(bloco);
		}
		return processos;
	}
	
	public List<TipoVoto> getTiposVoto() {
		if (isRelator()) {
			this.tiposVoto = ComponentUtil.getTipoVotoManager().listTipoVotoAtivoComRelator();
		}
		else {
			this.tiposVoto = ComponentUtil.getTipoVotoManager().tiposVotosVogais();
		}
		return tiposVoto;
	}
	
	public boolean isMostrarDivergentes() {
		mostrarDivergentes = false;
		if(tipoVoto != null) {
			mostrarDivergentes = !isRelator() && ComponentUtil.getTipoVotoManager().isDivergencia(tipoVoto) && ComponentUtil.getVotoBlocoManager().existeDivergente(bloco);
		}
		return mostrarDivergentes;
	}
	
	public String recuperarBloco(SessaoPautaProcessoTrf sessaoPauta) {
		String retorno = "";
		if(sessaoPauta != null) {	
			BlocoJulgamento bloco = ComponentUtil.getBlocoJulgamentoManager().pesquisar(sessaoPauta.getProcessoTrf(), sessaoPauta.getSessao());
			if(bloco != null) {
				retorno = bloco.getBlocoJulgamento();
			}
		}
		return retorno;
	}
	
	public List<OrgaoJulgador> getVotoDivergenteList() {
		votoDivergenteList = ComponentUtil.getVotoBlocoManager().recuperarOrgaosDivergentes(bloco);
		return votoDivergenteList;
	}


	public BlocoJulgamento getBloco() {
		if(bloco == null && idBloco > 0) {
			try {
				bloco = ComponentUtil.getBlocoJulgamentoManager().findById(idBloco);
			} catch (PJeBusinessException e) {
				FacesMessages.instance().add(Severity.ERROR, e.getLocalizedMessage());
			}
		}
		return bloco;
	}

	public void setBloco(BlocoJulgamento bloco) {
		this.bloco = bloco;
	}

	public String getNomeBloco() {
		return nomeBloco;
	}

	public void setNomeBloco(String nomeBloco) {
		this.nomeBloco = nomeBloco;
	}

	public boolean isExpandirBloco() {
		return expandirBloco;
	}

	public void setExpandirBloco(boolean expandirBloco) {
		this.expandirBloco = expandirBloco;
	}

	public int getIdBloco() {
		return idBloco;
	}

	public void setIdBloco(int idBloco) {
		if(this.idBloco != idBloco) {
			try {
				bloco = ComponentUtil.getBlocoJulgamentoManager().findById(idBloco);
				this.idBloco = idBloco;
			} catch (PJeBusinessException e) {
				FacesMessages.instance().add(Severity.ERROR, e.getLocalizedMessage());
			}
			setEditarVoto(false);
			setExibirDocumentosVoto(false);
		}
	}
	
	public Sessao getSessao() {
		sessao = SessaoHome.instance().getInstance();
		if(sessao == null || sessao.getIdSessao() < 1) {
			sessao = ComponentUtil.getPainelDoMagistradoNaSessaoAction().getSessao(); 
		}
		return sessao;
	}

	public void setSessao(Sessao sessao) {
		this.sessao = sessao;
	}

	public List<BlocoJulgamento> getBlocosSessao() {
		if(blocosSessao == null ) {
			blocosSessao = ComponentUtil.getBlocoJulgamentoManager().recuperarBlocosComProcessos(getSessao(), false, false);
		}
		return blocosSessao;		
	}

	public void setBlocosSessao(List<BlocoJulgamento> blocosSessao) {
		this.blocosSessao = blocosSessao;
	}

	public boolean isRelator() {
		if(bloco != null && ComponentUtil.getPainelDoMagistradoNaSessaoAction().getOrgaoAtual() != null) {
			relator = bloco.getAgruparOrgaoJulgador() && (bloco.getOrgaoJulgadorRelator() != null && bloco.getOrgaoJulgadorRelator() == ComponentUtil.getPainelDoMagistradoNaSessaoAction().getOrgaoAtual());
		}
		return relator;
	}

	public boolean isEditarVoto() {
		return editarVoto;
	}

	public void setEditarVoto(boolean editarVoto) {
		this.editarVoto = editarVoto;
	}

	public String getConteudoVoto() {
		return conteudoVoto;
	}

	public void setConteudoVoto(String conteudoVoto) {
		this.conteudoVoto = conteudoVoto;
	}

	public String getConteudoEmenta() {
		return conteudoEmenta;
	}

	public void setConteudoEmenta(String conteudoEmenta) {
		this.conteudoEmenta = conteudoEmenta;
	}

	public String getConteudoRelatorio() {
		return conteudoRelatorio;
	}

	public void setConteudoRelatorio(String conteudoRelatorio) {
		this.conteudoRelatorio = conteudoRelatorio;
	}

	public void concluir() {
		try {
			/*List<ProcessoDocumento> documentosParaAssinatura = ComponentUtil.getBlocoJulgamentoManager().recuperarDocumentosParaAssinatura(blocosLoteSelecionados);
			ComponentUtil.getDocumentoJudicialService().juntarEhGravarAssinaturaDeProcessosDocumentosNaoSigilosos(this.arquivosAssinados, documentosParaAssinatura);
			ComponentUtil.getBlocoJulgamentoManager().registrarAssinaturaCertidao(blocosLoteSelecionados);*/
			FacesMessages.instance().add(Severity.INFO, "Os documentos foram assinadas com sucesso!");
		}
		catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao tentar assinar os documentos " + e.getLocalizedMessage());
		}
	}
	
	@Override
	public void salvar(String conteudo) {
		try {
			if(bloco != null && tipoSelecionado != null) {
				if(ParametroUtil.instance().getTipoProcessoDocumentoRelatorio() == tipoSelecionado) {
					conteudoRelatorio = conteudo;
					ComponentUtil.getBlocoJulgamentoManager().atualizarSessaoProcessoDocumento(conteudo,bloco,tipoSelecionado,ComponentUtil.getPainelDoMagistradoNaSessaoAction().getOrgaoAtual());
				} else if(ParametroUtil.instance().getTipoProcessoDocumentoEmenta() == tipoSelecionado) {
					conteudoEmenta = conteudo;
					ComponentUtil.getBlocoJulgamentoManager().atualizarSessaoProcessoDocumento(conteudo,bloco,tipoSelecionado,ComponentUtil.getPainelDoMagistradoNaSessaoAction().getOrgaoAtual());
				} else {
					conteudoVoto = conteudo;
					ComponentUtil.getBlocoJulgamentoManager().atualizarSessaoProcessoDocumentoVoto(getIdTipoVotoSelecionado(),conteudo,bloco,ComponentUtil.getPainelDoMagistradoNaSessaoAction().getOrgaoAtual(), tipoSelecionado);
				}
				FacesMessages.instance().add(Severity.INFO, "O documento foi alterado com sucesso!");
			} else {
				FacesMessages.instance().add(Severity.ERROR, "Erro ao alterar o documento: deve haver bloco e tipo de documento selecionado");				
			}
		}
		catch (Exception e) {			
			FacesMessages.instance().add(Severity.ERROR, "Erro ao alterar o documento para o bloco: " + e.getLocalizedMessage());			
		}
	}
	
	@Override
	public String getEstilosFormatacao() {
		return ComponentUtil.getEditorEstiloService().recuperarEstilosJSON();
	}

	@Override
	public boolean isFormularioPreenchido() {
		boolean retorno = false;
		if(bloco != null ) {
			if(ParametroUtil.instance().getTipoProcessoDocumentoRelatorio() == tipoSelecionado && conteudoRelatorio != null) {
				retorno = true;
			} else if(ParametroUtil.instance().getTipoProcessoDocumentoEmenta() == tipoSelecionado && conteudoEmenta != null) {
				retorno = true;
			} else if(conteudoVoto != null) {
					retorno = true;
			}
		}
		return retorno;
	}

	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash)
			throws Exception {
		this.arquivosAssinados.add(arquivoAssinadoHash);
	}

	@Override
	public String getActionName() {
		return NAME;
	}
	
	public TipoVotoDTO criarTipoVotoDTO(TipoVoto tipoVoto) {
		TipoVotoDTO tipoVotoDTO = new TipoVotoDTO();
		
		tipoVotoDTO.setIdTipoVoto(tipoVoto.getIdTipoVoto());
		tipoVotoDTO.setTipoVoto(tipoVoto.getTipoVoto());
		tipoVotoDTO.setTextoCertidao(tipoVoto.getTextoCertidao());
		tipoVotoDTO.setRelator(tipoVoto.getRelator());
		tipoVotoDTO.setContexto(tipoVoto.getContexto());
		tipoVotoDTO.setAtivo(tipoVoto.getAtivo());
		tipoVotoDTO.setCor(tipoVoto.getCor());
		
		return tipoVotoDTO;
	}
	
	public List<TipoVotoDTO> criarListaTiposVoto(List<TipoVoto> listTipoVoto) {
		List<TipoVotoDTO> listTipoVotoDTO = new ArrayList<TipoVotoDTO>();
		
		for(TipoVoto tv : listTipoVoto) {
			listTipoVotoDTO.add(criarTipoVotoDTO(tv));
		}
		
		return listTipoVotoDTO;
	}
	
	@Override
	public String obterTiposVoto() {
		RespostaDTO respostaDTO = new RespostaDTO();
		
		try {
			respostaDTO.setSucesso(Boolean.TRUE);
			RespostaTiposVotoDTO respostaTiposVotoDTO = new RespostaTiposVotoDTO();
			respostaTiposVotoDTO.setPodeAlterar(true);
			/*if(spdv.getTipoVoto() != null) {
				TipoVoto tipoVoto = spdv.getTipoVoto();
				respostaTiposVotoDTO.setSelecao(criarTipoVotoDTO(tipoVoto));
			} else if(!VOTO.equalsIgnoreCase(getAbaSelecionada())) {
				TipoVoto tipoVoto = new TipoVoto();
				respostaTiposVotoDTO.setSelecao(criarTipoVotoDTO(tipoVoto));
			}*/
			
			respostaTiposVotoDTO.setTipos(criarListaTiposVoto(ComponentUtil.getTipoVotoManager().listTipoVotoAtivoComRelator()));

			respostaDTO.setResposta(respostaTiposVotoDTO);
		} catch (Exception e) {
			e.printStackTrace();
			respostaDTO.setSucesso(Boolean.FALSE);
			respostaDTO.setMensagem(e.getLocalizedMessage());
		}
		
		String strRetornoTiposVotoJSON = new Gson().toJson(respostaDTO, RespostaDTO.class);

		return strRetornoTiposVotoJSON;
	}
	
	@Override
	public String verificarPluginTipoVoto() throws JSONException {
		JSONObject retorno = new JSONObject();
		retorno.put("sucesso", Boolean.TRUE);
		return retorno.toString();
	}

	public TipoVoto getTipoVoto() {
		if(bloco.getAgruparOrgaoJulgador()) {
			VotoBloco voto = ComponentUtil.getVotoBlocoManager().recuperarVoto(bloco, ComponentUtil.getPainelDoMagistradoNaSessaoAction().getOrgaoAtual());
			if(voto != null) {
				tipoVoto = voto.getTipoVoto();
				orgaoJulgadorAcompanhado = voto.getOjAcompanhado(); 
			}
		}
		return tipoVoto;
	}

	public void setTipoVoto(TipoVoto tipoVoto) {
		this.tipoVoto = tipoVoto;
	}

	public OrgaoJulgador getOrgaoJulgadorAcompanhado() {
		return orgaoJulgadorAcompanhado;
	}

	public void setOrgaoJulgadorAcompanhado(OrgaoJulgador orgaoJulgadorAcompanhado) {
		this.orgaoJulgadorAcompanhado = orgaoJulgadorAcompanhado;
	}

	public boolean isExibirDocumentosVoto() {
		return exibirDocumentosVoto;
	}

	public void setExibirDocumentosVoto(boolean exibirDocumentosVoto) {
		this.exibirDocumentosVoto = exibirDocumentosVoto;
	}
}
