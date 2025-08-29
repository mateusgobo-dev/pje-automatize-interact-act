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
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import br.com.infox.cliente.home.SessaoHome;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.dto.DadosBlocoJulgamentoDTO;
import br.jus.pje.nucleo.dto.FiltroProcessoSessaoDTO;
import br.jus.pje.nucleo.entidades.BlocoJulgamento;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoBloco;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.entidades.TipoVoto;
import br.jus.pje.nucleo.enums.TipoInclusaoEnum;


/**
 * Classe de controle da aba de criação de blocos de julgamento de processos na Relação de julgamento 
 *
 */
@Name("abaCriacaoBlocoJulgamentoAction")
@Scope(ScopeType.CONVERSATION)
public class AbaCriacaoBlocoJulgamentoAction {
	private Sessao sessao;
	private String nomeParte;
	private Integer numeroSequencia;
	private Integer digitoVerificador;
	private Integer ano;
	private String ramoJustica;
	private String respectivoTribunal;
	private Integer numeroOrigem;
	private String assunto;
	private String classeJudicial;
	private OrgaoJulgador orgaoJulgador;
	private OrgaoJulgador orgaoJulgadorRelator;
	private TipoVoto tipoVoto;
	private String relator;
	private List<ProcessoTrf> processosPesquisados;
	private List<ProcessoTrf> processosSelecionados;
	private List<ProcessoTrf> processosNaoPautados;
	private Boolean inclusaoConfirmada;
	private boolean marcouTudo;
	private boolean expandirBloco;
	private BlocoJulgamento bloco;
	private String nomeBloco;
	private String propostaVoto;
	private Boolean agruparRelator = Boolean.TRUE;
	private boolean exibeMPConfirmacaoInclusaoBloco;
	private boolean exibeMPConfirmacaoInclusaoBlocoSemValidacao;
	private TipoVoto tipoVotoBloco;
	private boolean habilitarEdicao;
	private Sessao sessaoSugerida;
	
	@Create
	public void init() {
		String numeroOrgaoJustica = ComponentUtil.getParametroService().valueOf("numeroOrgaoJustica");
		if(numeroOrgaoJustica != null){
			this.ramoJustica = numeroOrgaoJustica.substring(0, 1);
			this.respectivoTribunal = numeroOrgaoJustica.substring(1);
		}
	}
	 /**
  	* Método responsável por limpar os filtros de pesquisa [PJEII-10745]
  	*/
  	public void limparCamposPesquisa() {
		this.nomeParte = null;
		this.numeroSequencia = null;
		this.digitoVerificador = null;
		this.ano = null;
		this.ramoJustica = null;
		this.respectivoTribunal = null;
		this.numeroOrigem = null;
		this.orgaoJulgador = null;
		this.tipoVoto = null;
		this.assunto = null;
		this.classeJudicial = null;
		this.relator = null;
	}
  	
  	public void pesquisarProcessosAptosBloco(Sessao sessao) {
  			FacesMessages.instance().clear();
  			try {
  				setSessao(sessao);
  				FiltroProcessoSessaoDTO filtro = new FiltroProcessoSessaoDTO(this.sessao, this.nomeParte, this.numeroSequencia, this.digitoVerificador, this.ano, this.ramoJustica, this.respectivoTribunal, this.numeroOrigem, this.assunto, this.classeJudicial, this.orgaoJulgador, this.tipoVoto, this.relator);
  				processosPesquisados = ComponentUtil.getBlocoJulgamentoManager().pesquisarProcessosAptosBloco(filtro);
  				processosSelecionados = new ArrayList<ProcessoTrf>(processosPesquisados.size());
  				for (ProcessoTrf p : processosPesquisados){
  					p.setCheck(false);
  				}
  				for (ProcessoTrf p : processosSelecionados){
  					p.setCheck(false);
  				}
  			} catch (Exception e) {
  				FacesMessages.instance().add(Severity.ERROR, e.getLocalizedMessage());
  			}  		
  		
  	}
  	
  	public void marcarTudo() {
  		if(marcouTudo) {
	  		this.processosSelecionados.addAll(this.processosPesquisados);
  		} else {
  			this.processosSelecionados = null;
  			processosSelecionados = new ArrayList<ProcessoTrf>(processosPesquisados.size());
  		}
	  	for (ProcessoTrf p : processosPesquisados){
			p.setCheck(marcouTudo);
		}
  	}

  	public boolean validarSelecao() {
  		boolean retorno = true;
  		if(processosSelecionados == null || processosSelecionados.isEmpty()) {
  			retorno = false;
  		} else {
	  		if(agruparRelator && ComponentUtil.getBlocoJulgamentoManager().recuperarRelator(processosSelecionados) == null) {
	  			retorno = false;
	  		} else {
	  			try {
					if(!agruparRelator ) {
						TipoVoto tipoVoto = ComponentUtil.getBlocoJulgamentoManager().recuperarTipoVoto(processosSelecionados, SessaoHome.instance().getInstance());
						if(tipoVoto == null || !tipoVoto.equals(tipoVotoBloco) ) {
							setExibeMPConfirmacaoInclusaoBloco(true);
							retorno = false;
						}
					}
				} catch (PJeBusinessException e) {
					if(e.getCode().equals("julgamentoBloco.excecaoSemVoto")) {
						setExibeMPConfirmacaoInclusaoBlocoSemValidacao(true);
					} else {
						FacesMessages.instance().add(Severity.ERROR, e.getLocalizedMessage());
					}
					retorno = false;
				}
	  		}
  		}
  		return retorno;
  	}
  	
  	public String recuperarVotoRelator(ProcessoTrf processo){
  		StringBuilder retorno = new StringBuilder();
  		SessaoProcessoDocumentoVoto spdv = ComponentUtil.getSessaoProcessoDocumentoVotoManager().recuperarVotoAntecipado(SessaoHome.instance().getInstance(), processo, processo.getOrgaoJulgador());
		if(spdv == null || spdv.getTipoVoto() == null ){
			retorno.append("Não foi possível recuperar voto de relator para esse processo.");
		} else {
			retorno.append(spdv.getTipoVoto().getTipoVoto());
		}
		return retorno.toString();
	}
  	
  	public boolean incluirBloco(Sessao sessao) {
  		FacesMessages.instance().clear();
  		setExibeMPConfirmacaoInclusaoBloco(false);
  		setExibeMPConfirmacaoInclusaoBlocoSemValidacao(false);
  		boolean retorno = false;
  		if(validarCamposBloco()) {
  			if(bloco == null) {
  				bloco = ComponentUtil.getBlocoJulgamentoManager().findByNome(nomeBloco, sessao);
  			}
	  		if(bloco == null || !bloco.getBlocoJulgamento().equals(nomeBloco)) {
	  			try {
	  				bloco = ComponentUtil.getBlocoJulgamentoManager().criarBloco(new DadosBlocoJulgamentoDTO(nomeBloco, sessao, propostaVoto, orgaoJulgadorRelator, processosSelecionados, agruparRelator, tipoVotoBloco));
	  				retorno = true;
	  			} catch(Exception e) {
	  				FacesMessages.instance().add(Severity.ERROR, e.getLocalizedMessage());
	  			}
	  		} else {
	  			try {
					ComponentUtil.getProcessoBlocoManager().adicionarProcessosBlocos(bloco, processosSelecionados, true);
		  			retorno = true;
				} catch (PJeBusinessException e) {
					FacesMessages.instance().add(Severity.ERROR, e.getLocalizedMessage());
				}
			}
	  		if(retorno) {
	  			for (ProcessoTrf p : processosSelecionados){
	  				processosPesquisados.remove(p);
	  			}
	  			processosSelecionados = null;
				processosSelecionados = new ArrayList<ProcessoTrf>(processosPesquisados.size());
				FacesMessages.instance().add(Severity.INFO, "Processo(s) incluído(s) no bloco com sucesso");
	  		}
  		}
  		limparTela();
  		return retorno;
  	}
  	
  	private boolean validarCamposBloco() {
  		boolean retorno = true;
  		if(nomeBloco == null || nomeBloco.length() < 3 ) {
  			FacesMessages.instance().add(Severity.ERROR, "Informe o nome do bloco");
  			retorno = false;
  		}
  		return retorno;
  	}
  	
  	public void limparTela() {
  		exibeMPConfirmacaoInclusaoBloco = false;
  		exibeMPConfirmacaoInclusaoBlocoSemValidacao = false;
  		habilitarEdicao = true;
  		inclusaoConfirmada = false;
  		nomeBloco = "";
  		propostaVoto = "";
  		orgaoJulgadorRelator = null;
  		bloco = null;
  		agruparRelator = Boolean.TRUE;
  		tipoVotoBloco = null;
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
  	
  	public boolean selecionarProcesso(ProcessoTrf processo) {
  		boolean retorno = true;
  		if(processo.getCheck() == null || processo.getCheck()) {
  			retorno = processosSelecionados.add(processo);
  		} else {
  			processosSelecionados.remove(processo);
  		}
  		return retorno;
  	}


  	public void selecionarBloco(BlocoJulgamento blocoParametro) {
  		if(blocoParametro != null) {
  			nomeBloco = blocoParametro.getBlocoJulgamento();
  			propostaVoto = blocoParametro.getPropostaVoto();
  			orgaoJulgadorRelator = blocoParametro.getOrgaoJulgadorRelator();
  			agruparRelator = blocoParametro.getAgruparOrgaoJulgador();
  			tipoVotoBloco = blocoParametro.getVotoRelator();
  			habilitarEdicao = false;
  		}
  		this.bloco = blocoParametro;
  	}

  	public void exibirDetalhesBloco(BlocoJulgamento blocoParametro) {
  		setExpandirBloco(true);
  		setBloco(blocoParametro);
  	}

  	public boolean excluirBloco(BlocoJulgamento blocoExcluir) {
  		boolean retorno = false;
  		try {
			ComponentUtil.getBlocoJulgamentoManager().remove(blocoExcluir);
			ComponentUtil.getBlocoJulgamentoManager().flush();
			retorno = true;
		} catch (PJeBusinessException e) {
			retorno = false;
		}
  		return retorno;
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
			retorno = false;
		}
  		
  		return retorno;
  	}
  	
  	public void incluirProcessosBloco(Sessao sessao) {
  		try{
			for (ProcessoTrf p : processosNaoPautados){
				ComponentUtil.getSessaoPautaProcessoTrfManager().pautarProcesso(sessao, p, TipoInclusaoEnum.BL);
			}
  		} catch (Exception e) {
  			FacesMessages.instance().add(Severity.ERROR, e.getLocalizedMessage());
		}
  	}
    
  	public boolean exibirInclusaoSessao(Sessao sessao) {
  		boolean retorno = false;
  		processosNaoPautados = ComponentUtil.getProcessoBlocoManager().recuperaProcessosBlocosNaoPautados(sessao);
  		if(processosNaoPautados != null && processosNaoPautados.size() > 0 ) {
  			retorno = true;
  		}
  		return retorno;
  	}
  	
  	public List<ProcessoTrf> recuperaProcessosBlocosNaoPautados(Sessao sessao) {
  		processosNaoPautados = ComponentUtil.getProcessoBlocoManager().recuperaProcessosBlocosNaoPautados(sessao);
  		return processosNaoPautados;
  	}
  	
  	public List<TipoVoto> getVotosRelator() {
  		return ComponentUtil.getTipoVotoManager().tiposVotosRelator();
  	}
  	
	public List<BlocoJulgamento> recuperarBlocos(Sessao sessao) {
		return ComponentUtil.getBlocoJulgamentoManager().findBySessao(sessao);
	}

	public List<OrgaoJulgador> getOrgaosJulgadores() {
		return ComponentUtil.getOrgaoJulgadorManager().getOrgaoJulgadorListByOjc(SessaoHome.instance().getInstance().getOrgaoJulgadorColegiado());
	}
	
	public List<TipoVoto> getTiposVotos() {
		return ComponentUtil.getTipoVotoManager().recuperaTipos(true);
	}
	
	public String recuperarNomeBloco(ProcessoTrf processo, Sessao sessao) {
		String retorno = "";
		BlocoJulgamento blocoProcesso = ComponentUtil.getBlocoJulgamentoManager().pesquisar(processo, sessao);
		if(blocoProcesso != null)
		{ retorno = blocoProcesso.getBlocoJulgamento(); }

		return retorno;
	}

	public String getNomeParte() {
		return nomeParte;
	}

	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}

	public Sessao getSessao() {
		return sessao;
	}

	public void setSessao(Sessao sessao) {
		this.sessao = sessao;
	}

	public Integer getNumeroSequencia() {
		return numeroSequencia;
	}

	public void setNumeroSequencia(Integer numeroSequencia) {
		this.numeroSequencia = numeroSequencia;
	}

	public Integer getDigitoVerificador() {
		return digitoVerificador;
	}

	public void setDigitoVerificador(Integer digitoVerificador) {
		this.digitoVerificador = digitoVerificador;
	}

	public Integer getNumeroOrigem() {
		return numeroOrigem;
	}

	public void setNumeroOrigem(Integer numeroOrigem) {
		this.numeroOrigem = numeroOrigem;
	}

	public String getAssunto() {
		return assunto;
	}

	public void setAssunto(String assunto) {
		this.assunto = assunto;
	}

	public Integer getAno() {
		return ano;
	}

	public void setAno(Integer ano) {
		this.ano = ano;
	}

	public String getClasseJudicial() {
		return classeJudicial;
	}

	public void setClasseJudicial(String classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public String getRamoJustica() {
		return ramoJustica;
	}

	public void setRamoJustica(String ramoJustica) {
		this.ramoJustica = ramoJustica;
	}

	public String getRespectivoTribunal() {
		return respectivoTribunal;
	}

	public void setRespectivoTribunal(String respectivoTribunal) {
		this.respectivoTribunal = respectivoTribunal;
	}

	public TipoVoto getTipoVoto() {
		return tipoVoto;
	}

	public void setTipoVoto(TipoVoto tipoVoto) {
		this.tipoVoto = tipoVoto;
	}

	public List<ProcessoTrf> getProcessosPesquisados() {
		return processosPesquisados;
	}

	public void setProcessosPesquisados(List<ProcessoTrf> processosPesquisados) {
		this.processosPesquisados = processosPesquisados;
	}

	public List<ProcessoTrf> getProcessosSelecionados() {
		return processosSelecionados;
	}

	public void setProcessosSelecionados(List<ProcessoTrf> processosSelecionados) {
		this.processosSelecionados = processosSelecionados;
	}

	public String getRelator() {
		return relator;
	}

	public void setRelator(String relator) {
		this.relator = relator;
	}

	public Boolean isInclusaoConfirmada() {
		return inclusaoConfirmada;
	}

	public void setInclusaoConfirmada(Boolean inclusaoConfirmada) {
		limparTela();
		this.inclusaoConfirmada = inclusaoConfirmada;
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

	public String getPropostaVoto() {
		return propostaVoto;
	}

	public void setPropostaVoto(String propostaVoto) {
		this.propostaVoto = propostaVoto;
	}

	public OrgaoJulgador getOrgaoJulgadorRelator() {
		return orgaoJulgadorRelator;
	}

	public void setOrgaoJulgadorRelator(OrgaoJulgador orgaoJulgadorRelator) {
		this.orgaoJulgadorRelator = orgaoJulgadorRelator;
	}
	
	public boolean isExibeMPConfirmacaoInclusaoBloco() {
		return exibeMPConfirmacaoInclusaoBloco;
	}

	public void setExibeMPConfirmacaoInclusaoBloco(boolean exibeMPConfirmacaoInclusaoBloco) {
		this.exibeMPConfirmacaoInclusaoBloco = exibeMPConfirmacaoInclusaoBloco;
	}
	
	public boolean isExibeMPConfirmacaoInclusaoBlocoSemValidacao() {
		return exibeMPConfirmacaoInclusaoBlocoSemValidacao;
	}

	public void setExibeMPConfirmacaoInclusaoBlocoSemValidacao(boolean exibeMPConfirmacaoInclusaoBlocoSemValidacao) {
		this.exibeMPConfirmacaoInclusaoBlocoSemValidacao = exibeMPConfirmacaoInclusaoBlocoSemValidacao;
	}
	
	public Boolean getAgruparRelator() {
		return agruparRelator;
	}

	public void setAgruparRelator(Boolean agruparRelator) {
		this.agruparRelator = agruparRelator;
	}

	public TipoVoto getTipoVotoBloco() {
		return tipoVotoBloco;
	}

	public void setTipoVotoBloco(TipoVoto tipoVotoBloco) {
		this.tipoVotoBloco = tipoVotoBloco;
	}

	public boolean isHabilitarEdicao() {
		return habilitarEdicao;
	}

	public void setHabilitarEdicao(boolean habilitarEdicao) {
		this.habilitarEdicao = habilitarEdicao;
	}

	public Sessao getSessaoSugerida() {
		return sessaoSugerida;
	}

	public void setSessaoSugerida(Sessao sessaoSugerida) {
		this.sessaoSugerida = sessaoSugerida;
	}
}
