package br.jus.pje.nucleo.dto;

import java.io.Serializable;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;

public class AproveitarAdvogadosDTO implements Serializable{

	private static final long serialVersionUID = 1L;
	private String 								nomeUsuario;
	private Integer								idPessoa;
	private ProcessoParteParticipacaoEnum		inParticipacao;
	private ProcessoParteSituacaoEnum			inSituacao;
	private String								numeroOAB;
	private String								estadoOAB;
	private String								numeroCPF;
	private Boolean								isEnderecoDesconhecido;
	private Integer								idEnderecoCadastrado;
	private boolean								marcado;
	private String								nomeParte;

	public AproveitarAdvogadosDTO() { }
	
	public AproveitarAdvogadosDTO(
			String 		nomeUsuario, 
			Integer 	idPessoa, 
			String 		inParticipacao, 
			ProcessoParteSituacaoEnum 		inSituacao, 
			String 		numeroOAB, 
			String 		estadoOAB, 
			String 		numeroCPF, 
			String 		enderecoDesconhecido,
			String 		idEnderecoCadastrado, 
			String 		nomeParte) {
		super();
		this.nomeUsuario = nomeUsuario;
		this.idPessoa = idPessoa;
		if(inParticipacao != null) {
			this.inParticipacao = ProcessoParteParticipacaoEnum.valueOf(inParticipacao);			
		}
		this.inSituacao = inSituacao;
		this.numeroOAB = numeroOAB;
		this.estadoOAB = estadoOAB;
		this.numeroCPF = numeroCPF;
		if(enderecoDesconhecido != null) {
			this.isEnderecoDesconhecido = Boolean.valueOf(enderecoDesconhecido);
		}
		if(idEnderecoCadastrado != null && !idEnderecoCadastrado.isEmpty() && !idEnderecoCadastrado.equalsIgnoreCase("null")) {
			this.idEnderecoCadastrado = Integer.parseInt(idEnderecoCadastrado);
		}
		this.nomeParte = nomeParte;
	}

	public Integer getIdPessoa() {
		return idPessoa;
	}

	public void setIdPessoa(Integer idPessoa) {
		this.idPessoa = idPessoa;
	}


	public ProcessoParteParticipacaoEnum getInParticipacao() {
		return inParticipacao;
	}

	public void setInParticipacao(ProcessoParteParticipacaoEnum inParticipacao) {
		this.inParticipacao = inParticipacao;
	}
	
	public void setInParticipacao(String inParticipacao) {
		if(inParticipacao != null) {
			this.inParticipacao = ProcessoParteParticipacaoEnum.valueOf(inParticipacao);			
		}
	}

	public ProcessoParteSituacaoEnum getInSituacao() {
		return inSituacao;
	}

	public void setInSituacao(ProcessoParteSituacaoEnum inSituacao) {
		this.inSituacao = inSituacao;
	}

	public String getNomeUsuario() {
		return nomeUsuario;
	}

	public void setNomeUsuario(String nomeUsuario) {
		this.nomeUsuario = nomeUsuario;
	}

	public String getNumeroOAB() {
		return numeroOAB;
	}

	public void setNumeroOAB(String numeroOAB) {
		this.numeroOAB = numeroOAB;
	}

	public String getEstadoOAB() {
		return estadoOAB;
	}

	public void setEstadoOAB(String estadoOAB) {
		this.estadoOAB = estadoOAB;
	}

	public String getNumeroCPF() {
		return numeroCPF;
	}

	public void setNumeroCPF(String numeroCPF) {
		this.numeroCPF = numeroCPF;
	}

	public Boolean getIsEnderecoDesconhecido() {
		return isEnderecoDesconhecido;
	}

	public void setIsEnderecoDesconhecido(Boolean isEnderecoDesconhecido) {
		this.isEnderecoDesconhecido = isEnderecoDesconhecido;
	}

	public Integer getIdEnderecoCadastrado() {
		return idEnderecoCadastrado;
	}

	public void setIdEnderecoCadastrado(Integer idEnderecoCadastrado) {
		this.idEnderecoCadastrado = idEnderecoCadastrado;
	}
	
	public boolean isMarcado() {
		return marcado;
	}

	public void setMarcado(boolean marcado) {
		this.marcado = marcado;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((inParticipacao == null) ? 0 : inParticipacao.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		boolean retorno = false;
		if (this == obj)
			retorno = true;
		if (obj == null)
			retorno = false;
		if (getClass() != obj.getClass())
			retorno = false;
		AproveitarAdvogadosDTO other = (AproveitarAdvogadosDTO) obj;
		if (nomeUsuario.equals(other.nomeUsuario) && idPessoa.equals(other.getIdPessoa()) && inParticipacao.equals(other.getInParticipacao()) && 
				numeroOAB.equals(other.getNumeroOAB()) && estadoOAB.equals(other.getEstadoOAB()) && numeroCPF.equals(other.getNumeroCPF()) && nomeParte.equals(other.getNomeParte()) ) {
			// os atributos de situação, de endereco e se é advogado selecionado não fazem diferença para avaliar se o registro é o mesmo
			retorno = true;
		}
		return retorno;
	}

	@Override
	public String toString() {
		return "nomeUsuario=" + nomeUsuario + ", numeroOAB=" + numeroOAB + ", estadoOAB=" + estadoOAB + ", numeroCPF=" + numeroCPF + ", marcado=" + marcado;
	}

	public String getNomeParte() {
		return nomeParte;
	}

	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}
}
