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

import java.util.ArrayList;
import java.util.List;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import br.com.infox.cliente.home.SessaoHome;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.dto.BlocoJulgamentoDTO;
import br.jus.pje.nucleo.entidades.BlocoJulgamento;
import br.jus.pje.nucleo.entidades.ProcessoBloco;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.enums.TipoSituacaoPautaBlocoEnum;

/**
 * Classe de controle para realização do julgamento em bloco  
 *
 */
@Name("julgamentoBlocoAction")
@Scope(ScopeType.CONVERSATION)
public class JulgamentoBlocoAction {
	private List<BlocoJulgamento> blocos;
	private List<BlocoJulgamentoDTO> blocosLote;
	private List<BlocoJulgamento> blocosJulgado;
	private List<BlocoJulgamentoDTO> blocosLoteJulgado;
	private List<BlocoJulgamentoDTO> blocosLoteSelecionados;
	private ProcessoTrf processoInclusaoBloco;
	private boolean marcouTudo;
	private boolean carregaSuggestInclusaoProcessoBloco;
	private boolean expandirBloco;
	private BlocoJulgamento bloco;
	private String nomeBloco;
	private boolean atualizarConsultas;
  	
  	public Integer recuperarSessaoPauta(ProcessoTrf processo, Sessao sessao) {
  		Integer retorno = 0;
  		SessaoPautaProcessoTrf sessaoPauta = this.recuperarSessaoPautaProcessoTrf(processo, sessao);
  		if(sessaoPauta!= null) {
  			retorno = sessaoPauta.getIdSessaoPautaProcessoTrf();
  		}
  		return retorno;
  	}
  	
  	public SessaoPautaProcessoTrf recuperarSessaoPautaProcessoTrf(ProcessoTrf processo, Sessao sessao) 	{
  		return ComponentUtil.getSessaoPautaProcessoTrfManager().getSessaoPautaProcessoTrf(processo, sessao);
  	} 
  	
  	public void marcarTudo() {
	  	for (BlocoJulgamentoDTO b : blocosLote){
			b.setCheck(marcouTudo);
		}
  		if(marcouTudo) {
	  		this.blocosLoteSelecionados = this.blocosLote;
  		} else {
  			this.blocosLoteSelecionados = null;
  			blocosLoteSelecionados = new ArrayList<BlocoJulgamentoDTO>(blocosLote.size());
  		}
  	}

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
  	
  	public boolean selecionarBloco(BlocoJulgamentoDTO bloco) {
  		boolean retorno = true;
  		if(bloco.isCheck()) {
  			retorno = blocosLoteSelecionados.add(bloco);
  		}
  		return retorno;
  	}

	public void exibirDetalhesBloco(BlocoJulgamento blocoParametro) {
  		if(expandirBloco && blocoParametro != null ) {
  			if(blocoParametro.equals(bloco)) {
  				setExpandirBloco(false);
  			} else {
  				setBloco(blocoParametro);
  			}
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
  	
  	public List<BlocoJulgamentoDTO> recuperarBlocosLote(Sessao sessao) {
  		blocos = ComponentUtil.getBlocoJulgamentoManager().recuperarBlocosComProcessos(sessao, true, false);
  		blocosLote = new ArrayList<BlocoJulgamentoDTO>(blocos.size());
  		blocosJulgado = ComponentUtil.getBlocoJulgamentoManager().recuperarBlocosComProcessos(sessao, true, true);
  		blocosLoteJulgado = (new ArrayList<BlocoJulgamentoDTO>(blocosJulgado.size()));
  		blocosLoteSelecionados = new ArrayList<BlocoJulgamentoDTO>(blocosLote.size());
  		for (BlocoJulgamento b : blocos){
  			blocosLote.add(new BlocoJulgamentoDTO(b,false));
		}
  		for (BlocoJulgamento b : blocosJulgado){
  			blocosLoteJulgado.add(new BlocoJulgamentoDTO(b,false));
		}
  		blocosLoteSelecionados = new ArrayList<BlocoJulgamentoDTO>(blocosLote.size());
  		return blocosLote;  
	}

	public boolean exibeMenuItemColocarEmJulgamento(BlocoJulgamento bloco) {
		return (TipoSituacaoPautaBlocoEnum.AJ.equals(bloco.getSituacaoJulgamento())  || 
				TipoSituacaoPautaBlocoEnum.NJ.equals(bloco.getSituacaoJulgamento())  || 
				TipoSituacaoPautaBlocoEnum.JG.equals(bloco.getSituacaoJulgamento()) || 
				TipoSituacaoPautaBlocoEnum.AD.equals(bloco.getSituacaoJulgamento()))  
				 &&
				bloco.getSessao().getDataAberturaSessao() != null;
	}
	
	public boolean exibeMenuItemRetirarJulgamento(BlocoJulgamento bloco) {
		return (TipoSituacaoPautaBlocoEnum.AJ.equals(bloco.getSituacaoJulgamento()) || 
				TipoSituacaoPautaBlocoEnum.EJ.equals(bloco.getSituacaoJulgamento()) || 
				TipoSituacaoPautaBlocoEnum.JG.equals(bloco.getSituacaoJulgamento()) || 
				TipoSituacaoPautaBlocoEnum.AD.equals(bloco.getSituacaoJulgamento()))  
				 &&
				bloco.getSessao().getDataAberturaSessao() != null;
	}
	
	public boolean exibeMenuItemRegistrarJulgamento(BlocoJulgamento bloco) {
		return (TipoSituacaoPautaBlocoEnum.AJ.equals(bloco.getSituacaoJulgamento()) || 
				TipoSituacaoPautaBlocoEnum.NJ.equals(bloco.getSituacaoJulgamento()) || 
				TipoSituacaoPautaBlocoEnum.EJ.equals(bloco.getSituacaoJulgamento()) || 
				TipoSituacaoPautaBlocoEnum.AD.equals(bloco.getSituacaoJulgamento()))  
				 &&
				bloco.getSessao().getDataAberturaSessao() != null;
	}
	
	public boolean exibeMenuItemTornarPendenteJulgamento(BlocoJulgamento bloco) {
		return (TipoSituacaoPautaBlocoEnum.NJ.equals(bloco.getSituacaoJulgamento()) || 
				TipoSituacaoPautaBlocoEnum.EJ.equals(bloco.getSituacaoJulgamento()) || 
				TipoSituacaoPautaBlocoEnum.JG.equals(bloco.getSituacaoJulgamento()))  
				 &&
				bloco.getSessao().getDataAberturaSessao() != null;
	}
	
	public boolean exibeMenuItemAdiarProximaSessao(BlocoJulgamento bloco) {
		return (TipoSituacaoPautaBlocoEnum.AJ.equals(bloco.getSituacaoJulgamento()) || 
				TipoSituacaoPautaBlocoEnum.NJ.equals(bloco.getSituacaoJulgamento()) || 
				TipoSituacaoPautaBlocoEnum.EJ.equals(bloco.getSituacaoJulgamento()) || 
				TipoSituacaoPautaBlocoEnum.JG.equals(bloco.getSituacaoJulgamento()))  
				 &&
				bloco.getSessao().getDataAberturaSessao() != null;
	}
	
	public boolean exibeMenuItemIncluirProcessoBloco(BlocoJulgamento bloco) {
		return bloco.getSessao().getDataRealizacaoSessao() == null && (!TipoSituacaoPautaBlocoEnum.JG.equals(bloco.getSituacaoJulgamento())  && 
				!TipoSituacaoPautaBlocoEnum.NJ.equals(bloco.getSituacaoJulgamento())  && 
				!TipoSituacaoPautaBlocoEnum.AD.equals(bloco.getSituacaoJulgamento()));
	}
	
	public void disponibilizaInclusaoBloco(BlocoJulgamentoDTO bloco) {
		setBloco(bloco.getBloco());
		setCarregaSuggestInclusaoProcessoBloco(true);
	}
	
	public void incluirProcessoBloco(Sessao sessao) {
		if(bloco != null && processoInclusaoBloco != null) {
			if(bloco.getAgruparOrgaoJulgador() && !(processoInclusaoBloco.getOrgaoJulgador().equals(bloco.getOrgaoJulgadorRelator()))) {
				FacesMessages.instance().add(Severity.ERROR, "O processo selecionado não é da mesma relatoria do bloco");
			} else {
				try {
		  			ComponentUtil.getProcessoBlocoManager().incluirProcessoBlocoJulgamento(bloco, processoInclusaoBloco, sessao);
		  			setCarregaSuggestInclusaoProcessoBloco(false);
		  			setProcessoInclusaoBloco(null);
		  			FacesMessages.instance().add(Severity.INFO, "Processo incluído com sucesso");				
				} catch (Exception e) {
					FacesMessages.instance().add(Severity.ERROR, e.getLocalizedMessage());
				}
			}
		} else {
			FacesMessages.instance().add(Severity.INFO, "Não há bloco ou processo selecionado para inclusão");
		}
	}
	
	public boolean verificarSituacaoJulgado() {
		boolean retorno = false;
		if(bloco != null) {
			retorno = TipoSituacaoPautaBlocoEnum.verificarSituacaoJulgado(bloco.getSituacaoJulgamento());
		}
		return retorno;
	}
	
	public void alterarSituacao(BlocoJulgamento bloco, String descricaoSituacao) {
		try {
			TipoSituacaoPautaBlocoEnum situacaoNova = TipoSituacaoPautaBlocoEnum.getEnum(descricaoSituacao);
			if(TipoSituacaoPautaBlocoEnum.verificarAlteracaoJulgado(situacaoNova, bloco.getSituacaoJulgamento())) {
				setAtualizarConsultas(true);
				setExpandirBloco(false);
			}
			ComponentUtil.getBlocoJulgamentoManager().atualizarBlocoJulgamento(bloco, situacaoNova);
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, "Situação do bloco de julgamento alterada com sucesso");
		} catch (Exception e) {			
			FacesMessages.instance().add(Severity.ERROR, e.getLocalizedMessage());
		}
	}
	
	public ProcessoTrf recuperarPrimeiroProcesso(BlocoJulgamento bloco) {
		ProcessoTrf retorno = null;
		ProcessoBloco pb = ComponentUtil.getProcessoBlocoManager().recuperaPrimeiroProcessoBloco(bloco);
		if(pb!= null) {
			retorno = pb.getProcessoTrf();
		}
		return retorno;
	}
	
	public SessaoPautaProcessoTrf recuperarPrimeiroProcessoPauta(BlocoJulgamento bloco) {
		SessaoPautaProcessoTrf retorno = null;
		ProcessoTrf processo = recuperarPrimeiroProcesso(bloco);
		if(processo != null) {
			retorno = ComponentUtil.getSessaoPautaProcessoTrfManager().getSessaoPautaProcessoTrf(processo, SessaoHome.instance().getInstance());
		}
		return retorno;
	}
	
	public boolean isMarcouTudo() {
		return marcouTudo;
	}

	public void setMarcouTudo(boolean marcouTudo) {
		this.marcouTudo = marcouTudo;
	}

	public BlocoJulgamento getBloco() {
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

	public List<BlocoJulgamento> getBlocos() {
		if(blocos == null || isAtualizarConsultas()) {
			blocosLote = recuperarBlocosLote(SessaoHome.instance().getInstance());
		}
		return blocos;
	}

	public void setBlocos(List<BlocoJulgamento> blocos) {
		this.blocos = blocos;
	}


	public List<BlocoJulgamentoDTO> getBlocosLote() {
		if(blocosLote == null || isAtualizarConsultas()) {
			blocosLote = recuperarBlocosLote(SessaoHome.instance().getInstance());
		}
		return blocosLote;
	}

	public void setBlocosLote(List<BlocoJulgamentoDTO> blocosLote) {
		this.blocosLote = blocosLote;
	}

  	public List<BlocoJulgamentoDTO> getBlocosLoteSelecionados() {
		if(blocosLoteSelecionados == null || isAtualizarConsultas()) {
			blocosLote = recuperarBlocosLote(SessaoHome.instance().getInstance());
		}
		return blocosLoteSelecionados;
	}

	public void setBlocosLoteSelecionados(List<BlocoJulgamentoDTO> blocosLoteSelecionados) {
		this.blocosLoteSelecionados = blocosLoteSelecionados;
	}

	public boolean isCarregaSuggestInclusaoProcessoBloco() {
		return carregaSuggestInclusaoProcessoBloco;
	}

	public void setCarregaSuggestInclusaoProcessoBloco(boolean carregaSuggestInclusaoProcessoBloco) {
		this.carregaSuggestInclusaoProcessoBloco = carregaSuggestInclusaoProcessoBloco;
	}

	public ProcessoTrf getProcessoInclusaoBloco() {
		return processoInclusaoBloco;
	}

	public void setProcessoInclusaoBloco(ProcessoTrf processoInclusaoBloco) {
		this.processoInclusaoBloco = processoInclusaoBloco;
	}

	public List<BlocoJulgamento> getBlocosJulgado() {
		if(blocosJulgado == null || isAtualizarConsultas()) {
			blocosLote = recuperarBlocosLote(SessaoHome.instance().getInstance());
		}
		return blocosJulgado;
	}

	public void setBlocosJulgado(List<BlocoJulgamento> blocosJulgado) {
		this.blocosJulgado = blocosJulgado;
	}

	public List<BlocoJulgamentoDTO> getBlocosLoteJulgado() {
		if(blocosLoteJulgado == null || isAtualizarConsultas()) {
			blocosLote = recuperarBlocosLote(SessaoHome.instance().getInstance());
		}
		return blocosLoteJulgado;
	}

	public void setBlocosLoteJulgado(List<BlocoJulgamentoDTO> blocosLoteJulgado) {
		this.blocosLoteJulgado = blocosLoteJulgado;
	}

	public boolean isAtualizarConsultas() {
		return atualizarConsultas;
	}

	public void setAtualizarConsultas(boolean atualizarConsultas) {
		this.atualizarConsultas = atualizarConsultas;
	}
}
