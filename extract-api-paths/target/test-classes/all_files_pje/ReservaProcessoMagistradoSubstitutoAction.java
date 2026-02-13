package br.com.infox.pje.action;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.com.infox.view.GenericCrudAction;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.ProcessoMagistradoManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioLocalizacaoMagistradoServidorManager;
import br.jus.cnj.pje.nucleo.manager.VinculacaoUsuarioManager;
import br.jus.cnj.pje.nucleo.service.ProcessoMagistradoService;
import br.jus.pje.nucleo.entidades.ProcessoMagistrado;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SubstituicaoMagistrado;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.entidades.VinculacaoUsuario;
import br.jus.pje.nucleo.enums.TipoRelacaoProcessoMagistradoEnum;
import br.jus.pje.nucleo.enums.TipoVinculacaoUsuarioEnum;

@Name(ReservaProcessoMagistradoSubstitutoAction.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class ReservaProcessoMagistradoSubstitutoAction extends GenericCrudAction<ProcessoMagistrado> {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "reservaProcessoMagistradoSubstitutoAction";
	
	@In
	private ProcessoMagistradoManager processoMagistradoManager;
	
	@In
	private ProcessoMagistradoService processoMagistradoService;
	
	@In
	private SubstituicaoMagistradoAction substituicaoMagistradoAction;
	
	@In
	private ProcessoTrfManager processoTrfManager;
	
	@In
	private UsuarioLocalizacaoMagistradoServidorManager usuarioLocalizacaoMagistradoServidorManager;
	
	@In
	private VinculacaoUsuarioManager vinculacaoUsuarioManager;
	
	private SubstituicaoMagistrado substituicao;
	private List<ProcessoMagistrado> associacoes;
	private List<ProcessoMagistrado> processoMagistradoReservados;	
	private List<ProcessoMagistrado> processoMagistradoVinculados;	
	private Boolean exibeAbaReservaVinculacao = false;
	private Boolean selecionaTodos = false;
	private List<ProcessoReservavel> processosReservaveis = new ArrayList<ProcessoReservavel>();
	
	private UsuarioLocalizacaoMagistradoServidor usuarioLogado;
	private List<ProcessoTrf> processosDistribuidos;
	private Boolean podeEditarReserva;
	
	private static final String ABA = "abaReservaVinculacao";

	/**
	 * @Description Método utilizado para iniciar os dados utilizados e apresentar a aba de 'Reserva/Vinculação' da Substituição selecionada.
	 * @param substituicao selecionada
	 */
	public void carregarAbaReservaVinculacao(SubstituicaoMagistrado substituicao){
		setSubstituicao(substituicao);
		setUsuarioLogado(Authenticator.getUsuarioLocalizacaoMagistradoServidorAtual());
		setPodeEditarReserva(temPermissaoEditarReserva());
		setAssociacoes(null);
		setProcessosDistribuidos(null);
		setProcessosReservaveis(new ArrayList<ProcessoReservavel>());
		setExibeAbaReservaVinculacao(Boolean.TRUE);
		substituicaoMagistradoAction.setTab(ABA);
		newInstance();
	}
	
	/**
	 * @Description Verifica se o usuário logado tem permissão de editar uma 'Reserva/Vinculação'
	 * @return 	- TRUE : Caso o usuário logado seja o próprio magistrado substituto daquela substituição ou um de seus acessores.
	 * 			- FALSE: Todos os outros casos.
	 */
	private Boolean temPermissaoEditarReserva() {
		Usuario usuarioLogado = getUsuarioLogado().getUsuarioLocalizacao().getUsuario();
		if(getSubstituicao().getMagistradoSubstituto().getIdUsuario().equals(usuarioLogado.getIdUsuario()) || 
		   isAcessor(usuarioLogado, getSubstituicao().getMagistradoSubstituto().getPessoa())){
			return true;
		}		
		return false;
	}
	
	public void remove(ProcessoMagistrado reserva) {
		try {
			associacoes = null;
			processosReservaveis = null;
			processoMagistradoService.removerVinculacaoReservaDoRelator(reserva);
			FacesMessages.instance().add(Severity.INFO, "Associação removida com sucesso");
		} catch (PJeBusinessException e) {
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, e.getMessage(), e.getParams());
		}
	}
	
	/**
	 * @Description Verifica se o um usuário está vinculado (Assessora) ao outro usuário (Magistrado).
	 * @param possivelAssessor
	 * @param possivelMagistradoAssessorado
	 * @return
	 */
	private boolean isAcessor(Usuario possivelAssessor, Usuario possivelMagistradoAssessorado){
		List<VinculacaoUsuario> vinculacoesAssessores = vinculacaoUsuarioManager.obterVinculacoesUsuarios(possivelMagistradoAssessorado, TipoVinculacaoUsuarioEnum.EGA);
		for (VinculacaoUsuario vinculacaoAssessor : vinculacoesAssessores){
			if (vinculacaoAssessor.getUsuarioVinculado().equals(possivelAssessor)){
				return true;
			}
		}
			
		return false;
	}
	
	/**
	 * @Description método de controle do componente <h:selectBooleanCheckbox> da tela *substituicaoMagistrado/listView.xhtml
	 */
	public void selectAll(){
		if(selecionaTodos == Boolean.FALSE){
			setAll(Boolean.FALSE);
		}else{
			setAll(Boolean.TRUE);			
		}
	}
	
	private void setAll(Boolean isSelect){
		for (ProcessoReservavel processoReservavel : processosReservaveis) {
			processoReservavel.setSelecionado(isSelect);
		}
	}
	
 	/**
	 * @Description método de controle do componente <h:selectBooleanCheckbox> da tela *substituicaoMagistrado/listView.xhtml
	 */
 	public void selectOne(ProcessoTrf processo){
 		selecionaTodos = Boolean.FALSE;
 		for (ProcessoReservavel processoReservavel : processosReservaveis) {
			if(processoReservavel.getProcessoTrf().equals(processo)){
				if(processoReservavel.getSelecionado()){
					processoReservavel.setSelecionado(Boolean.FALSE);
				}else{
					processoReservavel.setSelecionado(Boolean.TRUE);
				}
				return;
			}
		}
 	}
 		
 	/**
	 * @Description Obtém os processos que um magistrado substituto já se associou (Reservou/Vinculou).  
 	 * @return Lista de Processos (ProcessoMagistrado)
 	 */
	public List<ProcessoMagistrado> getAssociacoes() {
		if(associacoes == null && getSubstituicao() != null){
			associacoes = processoMagistradoManager.obterAssociacoesMagistradoSubstituto(getSubstituicao());
			filtrarAssociacoes();
		}
		return associacoes;
	}
	
	/**
	 * @Description Método utilizado para filtrar os processos que um magistrado substituto já se associou com base nas seguintes regras:
	 * 				- Separa os processos por dois tipos de relação 'Reserva Processual' e 'Vinculação Regimental'.
	 * 				- Recupera todos os processos que foram reservados (Reserva Processual) e também estão vinculados (Vinculação Regimental).
	 * 				- Remove esses processos "duplicados" da lista de processos reservados (Reserva Processual).
	 * 				Sendo assim:
	 * 				- Obtém o total de processos apenas reservados
	 * 				- Obtém o total de processos apenas vinculados
	 * 				- Obtém o total de processos independente do tipo de relação (Reservas + Vinculações)
	 */
	private void filtrarAssociacoes() {
		List<ProcessoMagistrado> reservasVinculadas = new ArrayList<ProcessoMagistrado>();
		setProcessoMagistradoReservados(new ArrayList<ProcessoMagistrado>());
		setProcessoMagistradoVinculados(new ArrayList<ProcessoMagistrado>());
		for (ProcessoMagistrado processoMagistrado : associacoes) {
			if(processoMagistrado.getTipoRelacaoProcessoMagistrado().equals(TipoRelacaoProcessoMagistradoEnum.RESER)){
				getProcessoMagistradoReservados().add(processoMagistrado);
			}else{
				getProcessoMagistradoVinculados().add(processoMagistrado);
			}
		}
		//Recupera os processos reservados que já têm uma vinculação
		for (ProcessoMagistrado processoMagistradoVinculado : getProcessoMagistradoVinculados()) {
			for (ProcessoMagistrado processoMagistradoReservado : getProcessoMagistradoReservados()) {
				if(processoMagistradoVinculado.getProcesso().equals(processoMagistradoReservado.getProcesso())){
					reservasVinculadas.add(processoMagistradoReservado);
					break;
				}
			}
		}
		//Remove os processos "duplicados" da lista.
		getProcessoMagistradoReservados().removeAll(reservasVinculadas);
		associacoes.clear();
		associacoes.addAll(getProcessoMagistradoReservados());
		associacoes.addAll(getProcessoMagistradoVinculados());
	}
	
	/**
	 * @Description Utilizando a quantidade de processos que foram distribuídos no período daquela substituição,
	 * 				a quantidade de processos que o Magistrado Substituto já se associou e se esse Magistrado
	 * 				recebeu ou não estrutura de gabinete. Esse método calcula uma META e retorna o saldo negativo 
	 * 				ou positivo com relação aos processos que o Magistrado substituto se associou.
	 * @return Saldo de Processos associados de acordo com sua meta a cumprir.
	 */
	public Integer obterSaldo(){
		return (getAssociacoes().size() - obterMeta());
	}
	
	public Integer obterMeta(){
		double percentualMeta = 1;
		if (!getSubstituicao().getEstruturaGabineteCedida()) {
			percentualMeta = 0.5;
		}
		return (int) Math.ceil(getProcessosDistribuidos().size() * percentualMeta);
	}
	
	public void reservarProcessosSelecionados(){
		List<ProcessoReservavel> selecionados = new ArrayList<ProcessoReservavel>();
		for (ProcessoReservavel processoReservavel : processosReservaveis) {
			if(processoReservavel.getSelecionado()){
				selecionados.add(processoReservavel);
			}
		}
		// faz um reload dos processos reservaveis para verificar se os selecionados ainda continuam na lista
		processosReservaveis = getProcessosReservaveis();
		StringBuilder mensagem = new StringBuilder("Reserva dos processos: ");
		for (ProcessoReservavel processoReservavelSelecionado: selecionados) {
			try {
				if (processosReservaveis.contains(processoReservavelSelecionado)) {
					processoMagistradoService.registrarVinculacaoReservaDoRelatorAoProcesso(processoReservavelSelecionado.getProcessoTrf(), substituicao);
					if(selecionados.size() > 1){
						mensagem.append(processoReservavelSelecionado.getProcessoTrf().getNumeroProcesso()+", ");
					}else{
						FacesMessages.instance().add(Severity.INFO, "Reserva do processo "+processoReservavelSelecionado.getProcessoTrf().getNumeroProcesso()+" realizada com sucesso");
					}
				} else {
					FacesMessages.instance().add(Severity.ERROR, "Não é mais possível reservar o processo " + processoReservavelSelecionado.getProcessoTrf().getNumeroProcesso());
				}
			} catch (PJeBusinessException e) {
				FacesMessages.instance().add(Severity.ERROR, e.getMessage(), e.getParams());
			}
		}
		if(selecionados.size() > 1){
			mensagem.replace((mensagem.length() - 2), mensagem.length(), " realizada com sucesso!");
			FacesMessages.instance().add(Severity.INFO, mensagem.toString());
		}
		associacoes = null;
		processosReservaveis = null;
	}
	
	
	public class ProcessoReservavel {
		private Boolean selecionado;
		private ProcessoTrf processoTrf;
		
		public ProcessoReservavel(ProcessoTrf processoTrf) {
			this.processoTrf = processoTrf;
			this.selecionado = false;
		}
		
		public Boolean getSelecionado() {
			return selecionado;
		}
		public void setSelecionado(Boolean selecionado) {
			this.selecionado = selecionado;
		}

		public ProcessoTrf getProcessoTrf() {
			return processoTrf;
		}

		public void setProcessoTrf(ProcessoTrf processoTrf) {
			this.processoTrf = processoTrf;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((processoTrf == null) ? 0 : processoTrf.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			ProcessoReservavel other = (ProcessoReservavel) obj;
			if (processoTrf == null && other.processoTrf != null){
				return false;
			} else if (!processoTrf.equals(other.processoTrf))
				return false;
			return true;
		}
	}
	
	public SubstituicaoMagistrado getSubstituicao() {
		return substituicao;
	}

	public void setSubstituicao(SubstituicaoMagistrado substituicao) {
		this.substituicao = substituicao;
	}

	public void setAssociacoes(List<ProcessoMagistrado> associacoes) {
		this.associacoes = associacoes;
	}
	
	public List<ProcessoMagistrado> getProcessoMagistradoReservados() {
		return processoMagistradoReservados;
	}

	public void setProcessoMagistradoReservados(List<ProcessoMagistrado> processoMagistradoReservados) {
		this.processoMagistradoReservados = processoMagistradoReservados;
	}

	public List<ProcessoMagistrado> getProcessoMagistradoVinculados() {
		return processoMagistradoVinculados;
	}

	public void setProcessoMagistradoVinculados(List<ProcessoMagistrado> processoMagistradoVinculados) {
		this.processoMagistradoVinculados = processoMagistradoVinculados;
	}

	public List<ProcessoTrf> getProcessosDistribuidos() {
		if(processosDistribuidos == null && getSubstituicao() != null){
			processosDistribuidos = processoTrfManager.obterProcessosDistribuidosDuranteSubstituicao(getSubstituicao());
		}
		return processosDistribuidos;
	}
	
	public List<ProcessoReservavel> getProcessosReservaveis() {
		if(substituicao != null){
			List<ProcessoTrf> processos = processoMagistradoService.obterProcessosParaVinculacaoReserva(substituicao);
			List<ProcessoReservavel> processosAtualizados = preencheProcessosReservaveis(processos);
			if(processosReservaveis != null && processos != null){
				if(processosReservaveis.containsAll(processosAtualizados)){
					return processosReservaveis;
				}else{
					processosReservaveis = processosAtualizados;
				}
			}else{
				processosReservaveis = processosAtualizados;
			}
		}
		return processosReservaveis;
	}
	
	private List<ProcessoReservavel> preencheProcessosReservaveis(List<ProcessoTrf> processos){
		List<ProcessoReservavel> processosReservaveis = new ArrayList<ProcessoReservavel>();
		for (ProcessoTrf processoTrf : processos) {
			processosReservaveis.add(new ProcessoReservavel(processoTrf));
		}
		return processosReservaveis;
	}
	
	public void setProcessosReservaveis(List<ProcessoReservavel> processosReservaveis) {
		this.processosReservaveis = processosReservaveis;
	}

	public void setProcessosDistribuidos(List<ProcessoTrf> processosDistribuidos) {
		this.processosDistribuidos = processosDistribuidos;
	}
	
	public Boolean getSelecionaTodos() {
		return selecionaTodos;
	}

	public void setSelecionaTodos(Boolean selecionaTodos) {
		this.selecionaTodos = selecionaTodos;
	}

	public UsuarioLocalizacaoMagistradoServidor getUsuarioLogado() {
		return usuarioLogado;
	}

	public void setUsuarioLogado(UsuarioLocalizacaoMagistradoServidor usuarioLogado) {
		this.usuarioLogado = usuarioLogado;
	}

	public Boolean getPodeEditarReserva() {
		return podeEditarReserva;
	}

	public void setPodeEditarReserva(Boolean podeEditarReserva) {
		this.podeEditarReserva = podeEditarReserva;
	}

	public Boolean getExibeAbaReservaVinculacao() {
		return exibeAbaReservaVinculacao;
	}

	public void setExibeAbaReservaVinculacao(Boolean exibeAbaReservaVinculacao) {
		this.exibeAbaReservaVinculacao = exibeAbaReservaVinculacao;
	}
	
}