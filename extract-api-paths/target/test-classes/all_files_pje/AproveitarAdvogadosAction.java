package br.jus.cnj.pje.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import br.com.infox.cliente.NumeroProcessoUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.UsuarioLoginManager;
import br.jus.cnj.pje.nucleo.service.AproveitarAdvogadosService;
import br.jus.pje.nucleo.dto.AproveitarAdvogadosDTO;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;
import br.com.itx.util.ComponentUtil;

@Name(AproveitarAdvogadosAction.NAME)
@Scope(ScopeType.PAGE)
public class AproveitarAdvogadosAction implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "aproveitarAdvogadosAction";
	
	//CONTROLE
	private Boolean pesquisarNomeParte = true;
	private String nomeParteEditada;
	private ProcessoParte processoParteInicial;
	private List<AproveitarAdvogadosDTO> listaProcessosParteCompleta;
	private List<AproveitarAdvogadosDTO> listaProcessosParteAdvogadosSelecionados = new ArrayList<AproveitarAdvogadosDTO>(0);
	private Integer numeroSequencia;
	private Integer digitoVerificador;
	private Integer ano;
	private String ramoJustica;
	private String respectivoTribunal;
	private Integer numeroOrigem;
	private ProcessoParte processoParte;
	private boolean mostrarInativosPoloAtivo;
	private boolean mostrarInativosPoloPassivo;

	//PESQUISA NOME
	private String nomeUsuarioPesquisaInput;
	private UsuarioLogin usuarioPesquisa;
	
	//PESQUISA PROCESSO
	
	
	private Boolean marcarTodosPoloAtivo = false;
	private Boolean marcarTodosPoloPassivo = false;
	private List<AproveitarAdvogadosDTO> listaProcessosParteAdvogadosPoloAtivo = new ArrayList<AproveitarAdvogadosDTO>(0);
	private List<AproveitarAdvogadosDTO> listaProcessosParteAdvogadosPoloPassivo = new ArrayList<AproveitarAdvogadosDTO>(0);
	
	public void inicializar(ProcessoParte procParte) {
		limparVariaveis(true);
		this.nomeParteEditada = procParte.getNomeParte();
		this.processoParteInicial = procParte;
		this.processoParte = procParte;
		try {
			this.usuarioPesquisa = ComponentUtil.getComponent(UsuarioLoginManager.class).findById(procParte.getPessoa().getIdPessoa());
		} catch (PJeBusinessException e) {
			e.printStackTrace();
			FacesMessages.instance().add("N\u00E3o foi poss\u00EDvel inicializar os dados iniciais da parte - " + e.getLocalizedMessage(), Severity.ERROR);
		}
		String numeroOrgaoJustica = ComponentUtil.getParametroService().valueOf("numeroOrgaoJustica");
		if(numeroOrgaoJustica != null){
			this.ramoJustica = numeroOrgaoJustica.substring(0, 1);
			this.respectivoTribunal = numeroOrgaoJustica.substring(1);
		}
		if(usuarioPesquisa != null) {
			this.nomeUsuarioPesquisaInput = this.usuarioPesquisa.getNome();
			pesquisar();
		}
	}
	
	public void addRemoveAdvogado(AproveitarAdvogadosDTO appvo) {
		if(verificaListaContemAdvogado(listaProcessosParteAdvogadosSelecionados, appvo)) {
			List<AproveitarAdvogadosDTO> listaParaRetirar = new ArrayList<AproveitarAdvogadosDTO>(0);
			
			for (AproveitarAdvogadosDTO advogado : listaProcessosParteAdvogadosSelecionados) {
				if(advogado.getIdPessoa().equals(appvo.getIdPessoa())) {
					listaParaRetirar.add(advogado);
				}
			}
			
			for (AproveitarAdvogadosDTO retirar : listaParaRetirar) {
				listaProcessosParteAdvogadosSelecionados.remove(retirar);
			}
			
		}else {
			listaProcessosParteAdvogadosSelecionados.add(appvo);
		}
	}
	
	private boolean verificaListaContemAdvogado(List<AproveitarAdvogadosDTO> lista, AproveitarAdvogadosDTO appvo) {
		boolean retorno = false;
		for (AproveitarAdvogadosDTO AproveitarAdvogadosDTO : lista) {
			if(AproveitarAdvogadosDTO.getIdPessoa().equals(appvo.getIdPessoa())) {
				retorno = true;
				break;
			}
		}
		return retorno;
	}

	public void marcarDesmarcarTodosPoloAtivo() {
		marcarDesmarcarTodos(marcarTodosPoloAtivo, listaProcessosParteAdvogadosPoloAtivo, listaProcessosParteAdvogadosSelecionados);
		marcarTodosPoloAtivo = !marcarTodosPoloAtivo;
	}
	
	public void marcarDesmarcarTodosPoloPassivo() {
		marcarDesmarcarTodos(marcarTodosPoloPassivo, listaProcessosParteAdvogadosPoloPassivo, listaProcessosParteAdvogadosSelecionados);
		marcarTodosPoloPassivo = !marcarTodosPoloPassivo;
	}
	
	private void marcarDesmarcarTodos(Boolean marcarTodos, List<AproveitarAdvogadosDTO> listaCompleta, List<AproveitarAdvogadosDTO>  listaSelecionados) {
		if(marcarTodos) {
			for (AproveitarAdvogadosDTO appvo : listaCompleta) {
				if(!appvo.isMarcado() && verificaListaContemAdvogado(listaProcessosParteAdvogadosSelecionados, appvo)) {
					listaSelecionados.remove(appvo);
				}
			}
		} else {			
			for (AproveitarAdvogadosDTO appvo : listaCompleta) {
				if(!appvo.isMarcado() && !verificaListaContemAdvogado(listaProcessosParteAdvogadosSelecionados, appvo)) {
					listaSelecionados.add(appvo);
				}
			}
		}
	}
	
	public String formatarCelula(AproveitarAdvogadosDTO advogado) {
		String retorno = "";
		if(listaProcessosParteAdvogadosSelecionados.contains(advogado)) {
			retorno = "advogado-selecionado";
		} else {
			if(advogado.isMarcado()) {
				retorno = "advogado-bloqueado";
			}
			if(!ProcessoParteSituacaoEnum.A.equals(advogado.getInSituacao())) {
				retorno = "advogado-inativo";
			}
		}
		return retorno;
	}

	public void limparVariaveis(boolean limpezaCompleta) {
		if(limpezaCompleta) {
			processoParteInicial = null;
		}
		pesquisarNomeParte = true;
		nomeParteEditada = null;
		listaProcessosParteCompleta = null;
		listaProcessosParteAdvogadosSelecionados = new ArrayList<AproveitarAdvogadosDTO>(0);
		if(this.processoParteInicial != null) {
			try {
				this.usuarioPesquisa = ComponentUtil.getComponent(UsuarioLoginManager.class).findById(this.processoParteInicial.getPessoa().getIdPessoa());
			} catch (PJeBusinessException e) {
				FacesMessages.instance().add("N\u00E3o foi poss\u00EDvel inicializar os dados iniciais da parte.", Severity.ERROR);
			}				
		} else {
			this.usuarioPesquisa = null;
		}
		if(this.usuarioPesquisa != null) {
			this.nomeUsuarioPesquisaInput = this.usuarioPesquisa.getNome();
		} else {
			this.nomeUsuarioPesquisaInput = "";
		}
		limparVariaveisPesquisaProcesso(limpezaCompleta);
	}
	
	public boolean exibePainelResultados() {
		return (listaProcessosParteAdvogadosPoloAtivo.isEmpty() && listaProcessosParteAdvogadosPoloPassivo.isEmpty()) ? false : true;
	}
	
	private void limparVariaveisPesquisaProcesso(boolean limparCompleto) {
		if(limparCompleto) {
			this.numeroSequencia = null;
			this.digitoVerificador = null;
			this.ano = null;
			
			String numeroOrgaoJustica = ComponentUtil.getParametroService().valueOf("numeroOrgaoJustica");
			if(numeroOrgaoJustica != null){
				this.ramoJustica = numeroOrgaoJustica.substring(0, 1);
				this.respectivoTribunal = numeroOrgaoJustica.substring(1);
			} else {
				this.ramoJustica = null;
				this.respectivoTribunal = null;
			}
			this.numeroOrigem = null;
		}
		marcarTodosPoloAtivo = false;
		marcarTodosPoloPassivo = false;
		listaProcessosParteAdvogadosPoloAtivo = new ArrayList<AproveitarAdvogadosDTO>(0);
		listaProcessosParteAdvogadosPoloPassivo = new ArrayList<AproveitarAdvogadosDTO>(0);
	}

	
	public void vincularAdvogadosSelecionados() {
		if(listaProcessosParteAdvogadosSelecionados == null || listaProcessosParteAdvogadosSelecionados.isEmpty()) {
			FacesMessages.instance().add("\u00C9 necess\u00E1rio selecionar pelo menos um advogado para vincular \u00E0 parte.", Severity.ERROR);
		} else {			
			try {
				ComponentUtil.getComponent(AproveitarAdvogadosService.class).vincularAdvogadosSelecionados(processoParteInicial, listaProcessosParteAdvogadosSelecionados);
				listaProcessosParteAdvogadosSelecionados = new ArrayList<AproveitarAdvogadosDTO>(0);
				pesquisar();
				FacesMessages.instance().add("Advogado(s) vinculado(s) com sucesso!", Severity.INFO);
			}catch (Exception e) {
				FacesMessages.instance().add("Ocorreu um erro ao vincular os advogados \u00E0 parte selecionada.", Severity.ERROR);
			}
		}
	}
	
	public void pesquisar() {
		if(pesquisarNomeParte) {
			pesquisarNomeParte();
		}else {
			pesquisarNumeroProcesso();
		} 
		if(listaProcessosParteCompleta == null || listaProcessosParteCompleta.isEmpty()) {
			FacesMessages.instance().add("Nenhum resultado encontrado.", Severity.ERROR);
		} else {
			for (AproveitarAdvogadosDTO advogado : listaProcessosParteCompleta) {
				if(ProcessoParteParticipacaoEnum.A.equals(advogado.getInParticipacao())) {
					if(listaProcessosParteAdvogadosPoloAtivo.contains(advogado)) {
						continue;
					} else {
						listaProcessosParteAdvogadosPoloAtivo.add(advogado);
					}
				}
				if(ProcessoParteParticipacaoEnum.P.equals(advogado.getInParticipacao())) {
					if(listaProcessosParteAdvogadosPoloPassivo.contains(advogado)) {
						continue;
					} else {
						listaProcessosParteAdvogadosPoloPassivo.add(advogado);
					}
				} 
				
			}
		}
		marcarAdvogadosJaVinculados(listaProcessosParteAdvogadosPoloAtivo);
		marcarAdvogadosJaVinculados(listaProcessosParteAdvogadosPoloPassivo);
	}

	private void pesquisarNumeroProcesso() {
		if(numeroSequencia == null || digitoVerificador == null || ano == null || ramoJustica == null || ramoJustica.isEmpty() || respectivoTribunal == null || respectivoTribunal.isEmpty() || numeroOrigem == null ) {
			FacesMessages.instance().add("Insira um n\u00FAmero de processo para a pesquisa.", Severity.ERROR);
		} else {
			limparVariaveisPesquisaProcesso(false);
			listaProcessosParteCompleta = ComponentUtil.getComponent(AproveitarAdvogadosService.class).procurarAdvogadosProcessoParteByNumeroProcesso(numeroSequencia, digitoVerificador, ano, numeroOrigem, ramoJustica, respectivoTribunal);
		}
	}

	private void pesquisarNomeParte() {
		if(usuarioPesquisa == null) {
			FacesMessages.instance().add("Informe o nome da parte.", Severity.ERROR);
		} else {
			limparVariaveisPesquisaProcesso(false);
			listaProcessosParteCompleta = ComponentUtil.getComponent(AproveitarAdvogadosService.class).procurarAdvogadosProcessoParte(usuarioPesquisa.getIdUsuario());
		}
	}
	
	private void marcarAdvogadosJaVinculados(List<AproveitarAdvogadosDTO> lista) {
		if ( processoParteInicial == null) {
			processoParteInicial = this.processoParte;
		}
		for (AproveitarAdvogadosDTO novoAdvogado : lista) {
			for(ProcessoParteRepresentante representanteAtual : processoParteInicial.getProcessoParteRepresentanteList()) {
				if(ProcessoParteSituacaoEnum.A.equals(representanteAtual.getInSituacao()) && ProcessoParteSituacaoEnum.A.equals(representanteAtual.getParteRepresentante().getInSituacao()) && novoAdvogado.getIdPessoa().equals(representanteAtual.getRepresentante().getIdPessoa())) {
					novoAdvogado.setMarcado(true);
				}
			}
		}
	}

	public String stringExibicao() {
		String retorno = "";
		if(pesquisarNomeParte) {
			retorno = "Representantes de "+ usuarioPesquisa.getNome();
		}else if((numeroSequencia!= null &&  
					digitoVerificador != null && 
					ano != null && ramoJustica !=null  && 
					ramoJustica != null && 
					respectivoTribunal!=null )) {
			retorno = "Advogados do processo " + NumeroProcessoUtil.formatNumeroProcesso(numeroSequencia, digitoVerificador, ano, Integer.parseInt(ramoJustica + respectivoTribunal), numeroOrigem);
		}
		return retorno;
	}
	
	public void setPesquisarNomeParte(Boolean pesquisarNomeParte) {
		limparVariaveisPesquisaProcesso(true);
		this.pesquisarNomeParte = pesquisarNomeParte;
	}
	
	public boolean verificarPossibilidadeAproveitar(ProcessoParte pp, boolean isRetificacao ) {
		boolean retorno = false;
		if(Authenticator.instance().isPermiteAproveitarAdvogados() && pp.getPartePrincipal()) {
			retorno = true;
			if (isRetificacao) {
				retorno = ProcessoStatusEnum.D.equals(pp.getProcessoTrf().getProcessoStatus());
			} 
		}
		return retorno;
	}
	
	public void limparListaSelecionados() {
		listaProcessosParteAdvogadosSelecionados = new ArrayList<AproveitarAdvogadosDTO>(0);
	}

	public String getNomeParteEditada() {
		return nomeParteEditada;
	}

	public Boolean getPesquisarNomeParte() {
		
		if(pesquisarNomeParte == null) {
			return true;
		}
		return pesquisarNomeParte;
	}

	public List<AproveitarAdvogadosDTO> getListaProcessosParteAdvogadosPoloAtivo() {
		return listaProcessosParteAdvogadosPoloAtivo;
	}

	public void setListaProcessosParteAdvogadosPoloAtivo(List<AproveitarAdvogadosDTO> listaProcessosParteAdvogadosPoloAtivo) {
		this.listaProcessosParteAdvogadosPoloAtivo = listaProcessosParteAdvogadosPoloAtivo;
	}

	
	public List<AproveitarAdvogadosDTO> getListaProcessosParteAdvogadosSelecionados() {
		return listaProcessosParteAdvogadosSelecionados;
	}

	public void setListaProcessosParteAdvogadosSelecionados(List<AproveitarAdvogadosDTO> listaProcessosParteAdvogadosSelecionados) {
		this.listaProcessosParteAdvogadosSelecionados = listaProcessosParteAdvogadosSelecionados;
	}

	public Boolean getMarcarTodosPoloPassivo() {
		return marcarTodosPoloPassivo;
	}

	public void setMarcarTodosPoloPassivo(Boolean marcarTodosPoloPassivo) {
		this.marcarTodosPoloPassivo = marcarTodosPoloPassivo;
	}
	
	public Boolean getMarcarTodosPoloAtivo() {
		if(marcarTodosPoloAtivo == null) {
			return false;
		}
		return marcarTodosPoloAtivo;
	}

	public void setMarcarTodosPoloAtivo(Boolean marcarTodosPoloAtivo) {
		this.marcarTodosPoloAtivo = marcarTodosPoloAtivo;
	}

	
	public List<AproveitarAdvogadosDTO> getListaProcessosParteAdvogadosPoloPassivo() {
		return listaProcessosParteAdvogadosPoloPassivo;
	}

	public void setListaProcessosParteAdvogadosPoloPassivo(
			List<AproveitarAdvogadosDTO> listaProcessosParteAdvogadosPoloPassivo) {
		this.listaProcessosParteAdvogadosPoloPassivo = listaProcessosParteAdvogadosPoloPassivo;
	}

	public UsuarioLogin getUsuarioPesquisa() {
		return usuarioPesquisa;
	}

	public void setUsuarioPesquisa(UsuarioLogin usuarioPesquisa) {
		this.usuarioPesquisa = usuarioPesquisa;
	}

	public List<UsuarioLogin> autocomplete(Object suggest) {
        return ComponentUtil.getComponent(UsuarioLoginManager.class).findByName((String) suggest);
    }

	public String getNomeUsuarioPesquisaInput() {
		return nomeUsuarioPesquisaInput;
	}

	public void setNomeUsuarioPesquisaInput(String nomeUsuarioPesquisaInput) {
		this.nomeUsuarioPesquisaInput = nomeUsuarioPesquisaInput;
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

	public Integer getAno() {
		return ano;
	}

	public void setAno(Integer ano) {
		this.ano = ano;
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

	public ProcessoParte getProcessoParte() {
		return processoParte;
	}

	public void setProcessoParte(ProcessoParte processoParte) {
		this.processoParte = processoParte;
	}

	public boolean isMostrarInativosPoloAtivo() {
		return mostrarInativosPoloAtivo;
	}

	public void setMostrarInativosPoloAtivo(boolean mostrarInativosPoloAtivo) {
		this.mostrarInativosPoloAtivo = mostrarInativosPoloAtivo;
	}

	public boolean isMostrarInativosPoloPassivo() {
		return mostrarInativosPoloPassivo;
	}

	public void setMostrarInativosPoloPassivo(boolean mostrarInativosPoloPassivo) {
		this.mostrarInativosPoloPassivo = mostrarInativosPoloPassivo;
	}

}