package br.jus.cnj.pje.entidades.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Transient;

import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;
import br.jus.pje.nucleo.enums.TipoCalculoMeioComunicacaoEnum;

public class MiniPacVO implements Serializable, Comparable<MiniPacVO> {

	private static final long serialVersionUID = 1L;
	
	private int idProcessoParte;
	private ProcessoParte processoParte;
	private String nome;
	private ProcessoParteParticipacaoEnum inParticipacao;
	private List<Endereco> enderecos = new ArrayList<>(0);
	private Integer prazo;
	private Set<ExpedicaoExpedienteEnum> meios;
	private Boolean pessoal;
	private Boolean ativo;
	private Boolean urgente;
	private TipoCalculoMeioComunicacaoEnum tipoCalculo;
	private TipoProcessoDocumento tipoProcessoDocumento;
	private boolean isHabilitaDomicilioEletronico;
	private Pessoa pessoa;
	private Procuradoria procuradoria;
	private List<ProcessoParteRepresentante> representantesProcessuais = new ArrayList<>(0);

	private boolean disablePessoal = false;

	public boolean isDisablePessoal() {
		return disablePessoal;
	}

	public void setDisablePessoal(boolean disablePessoal) {
		this.disablePessoal = disablePessoal;
	}

	public MiniPacVO() {
	}

	public MiniPacVO(Pessoa pessoa) {
		this.idProcessoParte = pessoa.getIdPessoa();
		this.pessoa = pessoa;
		this.nome = pessoa.getNome();
	}

	public MiniPacVO(ProcessoParte processoParte) {
		this.idProcessoParte = processoParte.getIdProcessoParte();
		this.processoParte = processoParte;
		this.pessoa = processoParte.getPessoa();
		this.procuradoria = processoParte.getProcuradoria();
		this.nome = processoParte.getPessoa().getNomeParte();
		this.representantesProcessuais = processoParte.getProcessoParteRepresentanteList()
				.stream()
				.filter(Objects::nonNull)
				.filter(representate -> ProcessoParteSituacaoEnum.A.equals(representate.getInSituacao()))
				.collect(Collectors.toList());
		this.inParticipacao = processoParte.getInParticipacao();
		this.ativo = processoParte.getIsAtivo();
		this.enderecos = processoParte.getEnderecos();
	}

	public int getIdProcessoParte() {
		return idProcessoParte;
	}

	public void setIdProcessoParte(int idProcessoParte) {
		this.idProcessoParte = idProcessoParte;
	}
	
	public String getNome() {
		return nome;
	}
	
	public void setNome(String nome) {
		this.nome = nome;
	}
	
	public List<Endereco> getEnderecos() {
		return enderecos;
	}

	public void setEndereco(List<Endereco> enderecos) {
		this.enderecos = enderecos;
	}
	
	public Integer getPrazo() {
		return prazo;
	}
	
	public void setPrazo(Integer prazo) {
		this.prazo = prazo;
	}
	
	public Boolean getAtivo() {
		return ativo;
	}
	
	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	
	public ProcessoParteParticipacaoEnum getInParticipacao() {
		return inParticipacao;
	}
	
	public void setInParticipacao(ProcessoParteParticipacaoEnum inParticipacao) {
		this.inParticipacao = inParticipacao;
	}
	
	public Boolean isPessoal() {
		return pessoal;
	}
	
	public void setPessoal(Boolean pessoal) {
		this.pessoal = pessoal;
	}
    
    public Boolean getPessoal() {
        return pessoal;
    }
    
    public Set<ExpedicaoExpedienteEnum> getMeios() {
        return meios;
    }
    
    public void setMeios(Set<ExpedicaoExpedienteEnum> meios) {
        this.meios = meios;
    }
	
	public Boolean getUrgente() {
		return urgente;
	}
	
	public void setUrgente(Boolean urgente) {
		this.urgente = urgente;
	}
	
	public TipoCalculoMeioComunicacaoEnum getTipoCalculo() {
		return tipoCalculo;
	}
	
	public void setTipoCalculo(TipoCalculoMeioComunicacaoEnum tipoCalculo) {
		this.tipoCalculo = tipoCalculo;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + idProcessoParte;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
	    if (this == obj) {
	        return true; 
	    }
	    if (obj == null || getClass() != obj.getClass()) {
	        return false;
	    }
	    MiniPacVO other = (MiniPacVO) obj;
	    return idProcessoParte == other.idProcessoParte && 
	           (inParticipacao == null || inParticipacao.equals(other.getInParticipacao()));
	}

	@Override
	public int compareTo(MiniPacVO o) {

		if (o.getIdProcessoParte() > this.idProcessoParte){
			return 1;
		} else if (o.getIdProcessoParte() < this.idProcessoParte){
			return -1;
		}
		
		if (o.getInParticipacao() != getInParticipacao()) {
			return 1;
		}
		
		return 0;	
	}
    
    public ProcessoParte getProcessoParte() {
        return processoParte;
    }
	
    public void setProcessoParte(ProcessoParte processoParte) {
        this.processoParte = processoParte;
    }
    
    public Pessoa getPessoa() {
		return pessoa;
	}
    
    public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}
    
    public Procuradoria getProcuradoria() {
		return procuradoria;
	}
    
    public void setProcuradoria(Procuradoria procuradoria) {
		this.procuradoria = procuradoria;
	}
    
    public List<ProcessoParteRepresentante> getRepresentantesProcessuais() {
		return representantesProcessuais;
	}
       
    public void setRepresentantesProcessuais(List<ProcessoParteRepresentante> representantesProcessuais) {
		this.representantesProcessuais = representantesProcessuais;
	}

	@Transient
	public TipoProcessoDocumento getTipoProcessoDocumento() {
		return tipoProcessoDocumento;
	}
	public void setTipoProcessoDocumento(TipoProcessoDocumento tipoProcessoDocumento) {
		this.tipoProcessoDocumento = tipoProcessoDocumento;
	}
	
	@Transient
	public boolean getIsHabilitaDomicilioEletronico() {
		return isHabilitaDomicilioEletronico;
	}
	public void setIsHabilitaDomicilioEletronico(Boolean isHabilitaDomicilioEletronico) {
		this.isHabilitaDomicilioEletronico = isHabilitaDomicilioEletronico;
	}

}