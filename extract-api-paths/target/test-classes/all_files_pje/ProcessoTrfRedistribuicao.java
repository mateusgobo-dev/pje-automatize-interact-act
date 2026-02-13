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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.enums.TipoDistribuicaoEnum;
import br.jus.pje.nucleo.enums.TipoRedistribuicaoEnum;

@Entity
@Table(name = "tb_proc_trf_redistribuicao")
@org.hibernate.annotations.GenericGenerator(name = "gen_proc_trf_redistribuicao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_proc_trf_redistribuicao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoTrfRedistribuicao implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoTrfRedistribuicao,Integer> {

	private static final long serialVersionUID = 1L;

	private int idProcessoTrfRedistribuicao;
	private ProcessoTrf processoTrf;
	private OrgaoJulgadorColegiado orgaoJulgadorColegiadoAnterior;
	private OrgaoJulgadorColegiado orgaoJulgadorColegiado;
	private OrgaoJulgador orgaoJulgadorAnterior;
	private OrgaoJulgador orgaoJulgador;
	private String motivoRedistribuicao;
	private MotivoRedistribuicao motivoRedistribuicaoObj;
	private Date dataRedistribuicao;
	private TipoRedistribuicaoEnum inTipoRedistribuicao;
	private Jurisdicao jurisdicao;
	private Usuario usuario;
	private ProcessoEvento processoEvento;
	private TipoDistribuicaoEnum inTipoDistribuicao;
	
	//Transient
	private CausaSuspeicao causaSuspeicao;
	private CausaImpedimento causaImpedimento;
	private OrgaoJulgadorCargo orgaoJulgadorCargo;

	public ProcessoTrfRedistribuicao() {
	}

	/**
	 * @return Retorna o id do ProcessoTrf que é igual ao id do processo do core
	 */
	@Id
	@GeneratedValue(generator = "gen_proc_trf_redistribuicao")
	@Column(name = "id_processo_trf_redistribuicao", unique = true, nullable = false)
	public int getIdProcessoTrfRedistribuicao() {
		return idProcessoTrfRedistribuicao;
	}

	public void setIdProcessoTrfRedistribuicao(int idProcessoRedistribuicao) {
		this.idProcessoTrfRedistribuicao = idProcessoRedistribuicao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf", nullable = false)
	@NotNull
	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador_colegiado")
	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiado() {
		return orgaoJulgadorColegiado;
	}

	public void setOrgaoJulgadorColegiado(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_orgao_julgador_colegiado_anterior")
	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiadoAnterior() {
		return orgaoJulgadorColegiadoAnterior;
	}

	public void setOrgaoJulgadorColegiadoAnterior(OrgaoJulgadorColegiado orgaoJulgadorColegiadoAnterior) {
		this.orgaoJulgadorColegiadoAnterior = orgaoJulgadorColegiadoAnterior;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador", nullable = false)
	@NotNull
	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_orgao_julgador_anterior", nullable = false)
	@NotNull
	public OrgaoJulgador getOrgaoJulgadorAnterior() {
		return orgaoJulgadorAnterior;
	}

	public void setOrgaoJulgadorAnterior(OrgaoJulgador orgaoJulgadorAnterior) {
		this.orgaoJulgadorAnterior = orgaoJulgadorAnterior;
	}

	@Column(name = "ds_motivo_redistribuicao", length = 200)
	@Length(max = 200)
	public String getMotivoRedistribuicao() {
		return motivoRedistribuicao;
	}

	public void setMotivoRedistribuicao(String motivoRedistribuicao) {
		this.motivoRedistribuicao = motivoRedistribuicao;
	}
	
	@Transient
	public MotivoRedistribuicao getMotivoRedistribuicaoObj() {
		return motivoRedistribuicaoObj;
	}

	public void setMotivoRedistribuicaoObj(MotivoRedistribuicao motivoRedistribuicaoObj) {
		this.motivoRedistribuicaoObj = motivoRedistribuicaoObj;
		this.motivoRedistribuicao = motivoRedistribuicaoObj.getMotivoRedistribuicao();
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_redistribuicao")
	public Date getDataRedistribuicao() {
		return dataRedistribuicao;
	}

	public void setDataRedistribuicao(Date dataRedistribuicao) {
		this.dataRedistribuicao = dataRedistribuicao;
	}

	@Column(name = "in_tipo_redistribuicao", length = 1)
	@Enumerated(EnumType.STRING)
	public TipoRedistribuicaoEnum getInTipoRedistribuicao() {
		return inTipoRedistribuicao;
	}

	public void setInTipoRedistribuicao(TipoRedistribuicaoEnum inTipoRedistribuicao) {
		this.inTipoRedistribuicao = inTipoRedistribuicao;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_usuario", nullable = false)
	@NotNull
	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	@Transient
	public Jurisdicao getJurisdicao() {
		return jurisdicao;
	}

	public void setJurisdicao(Jurisdicao jurisdicao) {
		this.jurisdicao = jurisdicao;
	}

	@Transient
	public ProcessoEvento getProcessoEvento() {
		return processoEvento;
	}

	public void setProcessoEvento(ProcessoEvento processoEvento) {
		this.processoEvento = processoEvento;
	}

	@Override
	public String toString() {
		return processoTrf.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ProcessoTrfRedistribuicao)) {
			return false;
		}
		ProcessoTrfRedistribuicao other = (ProcessoTrfRedistribuicao) obj;
		if (getIdProcessoTrfRedistribuicao() != other.getIdProcessoTrfRedistribuicao()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoTrfRedistribuicao();
		return result;
	}
	
	/**
	 * PJE-JT: Bernardo Gouvea - 2012-08-27. 
	 * Atributos para capturar a seleção via combo de um complemento do motivo da redistribuição (Uma ref. legislativa "art...I, II etc).
	 */
		
	@Transient
	public CausaSuspeicao getCausaSuspeicao() {
		return causaSuspeicao;
	}

	public void setCausaSuspeicao(CausaSuspeicao causaSuspeicao) {
		this.causaSuspeicao = causaSuspeicao;
	}

	@Transient
	public CausaImpedimento getCausaImpedimento() {
		return causaImpedimento;
	}

	public void setCausaImpedimento(CausaImpedimento causaImpedimento) {
		this.causaImpedimento = causaImpedimento;
	}
	
	/**
	 * PJE-JT: Bernardo Gouvea - 2012-08-28. 
	 * Atributo para exibir o tipo de distribuição corretamente no histórico da redistribuição.
	 */
	@Column(name = "in_tipo_distribuicao", length = 2)
	@Enumerated(EnumType.STRING)
	public TipoDistribuicaoEnum getInTipoDistribuicao() {
		return this.inTipoDistribuicao;
	}

	public void setInTipoDistribuicao(TipoDistribuicaoEnum inTipoDistribuicao) {
		this.inTipoDistribuicao = inTipoDistribuicao;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoTrfRedistribuicao> getEntityClass() {
		return ProcessoTrfRedistribuicao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoTrfRedistribuicao());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

	@Transient
	public OrgaoJulgadorCargo getOrgaoJulgadorCargo() {
		return orgaoJulgadorCargo;
	}

	public void setOrgaoJulgadorCargo(OrgaoJulgadorCargo orgaoJulgadorCargo) {
		this.orgaoJulgadorCargo = orgaoJulgadorCargo;
	}
}
