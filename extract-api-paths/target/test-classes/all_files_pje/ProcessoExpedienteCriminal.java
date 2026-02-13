/**
 * pje-comum
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.nucleo.entidades;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.enums.SituacaoExpedienteCriminalEnum;
import br.jus.pje.nucleo.enums.TipoExpedienteCriminalEnum;

/*
 * Classe responsável pela manutenção dos dados comuns a todos os expedientes criminais
 */

@Entity
@Table(name = "tb_proc_expedente_criminal")
@Inheritance(strategy = InheritanceType.JOINED)
@org.hibernate.annotations.GenericGenerator(name = "gen_proc_exped_criminal", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_id_proc_exped_criminal"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoExpedienteCriminal implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoExpedienteCriminal,Integer>, Comparable<ProcessoExpedienteCriminal> {

	private static final long serialVersionUID = 1L;

	private Integer id;
	private Integer numero;
	private Date dtCriacao;
	private String sintese;
	private Boolean inSigiloso;
	private PessoaMagistrado pessoaMagistrado;
	private ProcessoTrf processoTrf;
	private ProcessoDocumento processoDocumento;
	private TipoProcessoDocumento tipoProcessoDocumento;
	private Pessoa pessoa;
	private SituacaoExpedienteCriminalEnum situacaoExpedienteCriminal;

	@Id
	@GeneratedValue(generator = "gen_proc_exped_criminal")
	@Column(name = "id_processo_expdiente_criminal", unique = true, nullable = false)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@NotNull
	@Column(name = "numero")
	public Integer getNumero() {
		return numero;
	}

	public void setNumero(Integer numero) {
		this.numero = numero;
	}

	@NotNull
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_criacao")
	public Date getDtCriacao() {
		return dtCriacao;
	}

	public void setDtCriacao(Date dtCriacao) {
		this.dtCriacao = dtCriacao;
	}

	@Length(max = 600)
	@Column(name = "ds_sintese", length = 600)
	public String getSintese() {
		return sintese;
	}

	public void setSintese(String sintese) {
		this.sintese = sintese;
	}

	@NotNull
	@Column(name = "in_sigiloso")
	public Boolean getInSigiloso() {
		return inSigiloso;
	}

	public void setInSigiloso(Boolean inSigiloso) {
		this.inSigiloso = inSigiloso;
	}

	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "id_pessoa_magistrado")
	public PessoaMagistrado getPessoaMagistrado() {
		return pessoaMagistrado;
	}

	public void setPessoaMagistrado(PessoaMagistrado pessoaMagistrado) {
		this.pessoaMagistrado = pessoaMagistrado;
	}

	@NotNull
	@ManyToOne
	@JoinColumn(name = "id_processo_trf", nullable = false)
	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@NotNull
	@ManyToOne
	@JoinColumn(name = "id_processo_documento", nullable = false)
	public ProcessoDocumento getProcessoDocumento() {
		return processoDocumento;
	}

	public void setProcessoDocumento(ProcessoDocumento processoDocumento) {
		this.processoDocumento = processoDocumento;
	}

	@ManyToOne
	@JoinColumn(name = "id_tipo_processo_documento")
	public TipoProcessoDocumento getTipoProcessoDocumento() {
		return tipoProcessoDocumento;
	}

	public void setTipoProcessoDocumento(TipoProcessoDocumento tipoProcessoDocumento) {
		this.tipoProcessoDocumento = tipoProcessoDocumento;
	}

	@NotNull
	@ManyToOne
	@JoinColumn(name = "id_pessoa", nullable = false)
	public Pessoa getPessoa() {
		return pessoa;
	}

	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}
	
	public void setPessoa(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoa(pessoa.getPessoa());
		} else {
			setPessoa((Pessoa)null);
		}
	}
	
	@NotNull
	@Column(name = "in_situacao_expdiente_criminal", nullable = false)
	@Enumerated(EnumType.STRING)
	public SituacaoExpedienteCriminalEnum getSituacaoExpedienteCriminal(){
		return situacaoExpedienteCriminal;
	}
	
	public void setSituacaoExpedienteCriminal(SituacaoExpedienteCriminalEnum situacaoExpedienteCriminal){
		this.situacaoExpedienteCriminal = situacaoExpedienteCriminal;
	}

	@Transient
	public String getNumeroExpediente() {
		if(getNumero() != null){
			int tamanho = 4-getNumero().toString().length();
			StringBuilder aux = new StringBuilder();
			for(int i=1; i <= tamanho; i++){
				aux.append("0");
			}
			
			aux.append(getNumero());
			
			if (getNumero() != null && getProcessoTrf() != null) {
				return getProcessoTrf().getNumeroProcesso()+ "." + aux.toString();
			}
		}

		return null;
	}

	@Transient
	public TipoExpedienteCriminalEnum getTipoExpedienteCriminal() {
		if (this instanceof MandadoPrisao) {
			return TipoExpedienteCriminalEnum.MP;
		} else if (this instanceof AlvaraSoltura) {
			return TipoExpedienteCriminalEnum.AS;
		} else if (this instanceof ContraMandado) {
			return TipoExpedienteCriminalEnum.CM;
		}

		return null;
	}

	@Override
	public int compareTo(ProcessoExpedienteCriminal other) {
		int result = 0;
		if(this.getProcessoTrf() != null && other.getProcessoTrf() != null){
			result = this.getProcessoTrf().getNumeroProcesso().compareTo(other.getProcessoTrf().getNumeroProcesso());
		}
		
		if(this.getNumeroExpediente() != null && other.getNumeroExpediente() != null){
			result += this.getNumeroExpediente().compareTo(other.getNumeroExpediente());
		}
		
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoExpedienteCriminal> getEntityClass() {
		return ProcessoExpedienteCriminal.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getId();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
