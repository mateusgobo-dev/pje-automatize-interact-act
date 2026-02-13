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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.apache.commons.beanutils.BeanUtils;
import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.enums.MotivoEncerramentoPrisaoEnum;
import br.jus.pje.nucleo.enums.TipoPrisaoEnum;

@Entity
@Table(name = "tb_icr_prisao")
@PrimaryKeyJoinColumn(name = "id_icr_prisao")
public class IcrPrisao extends InformacaoCriminalRelevante implements Serializable {

	private static final long serialVersionUID = 1L;

	private TipoPrisaoEnum inTipoPrisao;
	private Integer nrPrazoPrisao;
	private Date dtEncerramentoPrisao;
	private String dsMotivoEncerramento;
	private EstabelecimentoPrisional estabelecimentoPrisional;
	private MotivoEncerramentoPrisaoEnum motivoEncerramentoPrisao;
	private IcrPrisao prisaoEncerrada;
	private List<IcrTransferenciaReu> icrTransferencias = new ArrayList<IcrTransferenciaReu>(0);
	private List<IcrFuga> icrFugas = new ArrayList<IcrFuga>(0);
	private List<IcrSoltura> icrSoltura = new ArrayList<IcrSoltura>(0);

	public IcrPrisao() {

	}

	public IcrPrisao(InformacaoCriminalRelevante icr) {
		copiarPropriedadesIcr(icr);
	}

	private void copiarPropriedadesIcr(InformacaoCriminalRelevante icr) {
		try {
			BeanUtils.copyProperties(this, icr);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	@Column(name = "in_tipo_prisao", nullable = false)
	@NotNull
	@Enumerated(EnumType.STRING)
	// @Type(type = "br.jus.pje.nucleo.enums.TipoPrisaoType")
	public TipoPrisaoEnum getInTipoPrisao() {
		return inTipoPrisao;
	}

	public void setInTipoPrisao(TipoPrisaoEnum inTipoPrisao) {
		this.inTipoPrisao = inTipoPrisao;
	}

	@Column(name = "nr_prazo_prisao", nullable = true)
	public Integer getNrPrazoPrisao() {
		return nrPrazoPrisao;
	}

	public void setNrPrazoPrisao(Integer nrPrazoPrisao) {
		this.nrPrazoPrisao = nrPrazoPrisao;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "dt_encerramento_prisao", nullable = true)
	public Date getDtEncerramentoPrisao() {
		return dtEncerramentoPrisao;
	}

	public void setDtEncerramentoPrisao(Date dtEncerramentoPrisao) {
		this.dtEncerramentoPrisao = dtEncerramentoPrisao;
	}

	@Column(name = "ds_motivo_encerramento", nullable = true)
	@Length(min = 0, max = 255)
	public String getDsMotivoEncerramento() {
		return dsMotivoEncerramento;
	}

	public void setPrisaoEncerrada(IcrPrisao prisaoEncerrada) {
		this.prisaoEncerrada = prisaoEncerrada;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_prisao_encerrada", nullable = true)
	public IcrPrisao getPrisaoEncerrada() {
		return prisaoEncerrada;
	}

	public void setDsMotivoEncerramento(String dsMotivoEncerramento) {
		this.dsMotivoEncerramento = dsMotivoEncerramento;
	}

	@NotNull
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "id_estabelecimento_prisional", nullable = false)
	public EstabelecimentoPrisional getEstabelecimentoPrisional() {
		return estabelecimentoPrisional;
	}

	public void setEstabelecimentoPrisional(EstabelecimentoPrisional estabelecimentoPrisional) {
		this.estabelecimentoPrisional = estabelecimentoPrisional;
	}

	@Column(name = "cd_motivo_encerramento_prisao")
	@Enumerated(EnumType.STRING)
//	@Type(type = "br.jus.pje.nucleo.enums.MotivoEncerramentoPrisaoType")
	public MotivoEncerramentoPrisaoEnum getMotivoEncerramentoPrisao() {
		return motivoEncerramentoPrisao;
	}

	public void setMotivoEncerramentoPrisao(MotivoEncerramentoPrisaoEnum motivoEncerramentoPrisao) {
		this.motivoEncerramentoPrisao = motivoEncerramentoPrisao;
	}

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "icrPrisao")
	public List<IcrTransferenciaReu> getIcrTransferencias() {
		return icrTransferencias;
	}

	public void setIcrTransferencias(List<IcrTransferenciaReu> icrTransferencias) {
		this.icrTransferencias = icrTransferencias;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "icrPrisao")
	public List<IcrFuga> getIcrFugas() {
		return icrFugas;
	}

	public void setIcrFugas(List<IcrFuga> icrFugas) {
		this.icrFugas = icrFugas;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "icrPrisao")
	public List<IcrSoltura> getIcrSoltura() {
		return icrSoltura;
	}

	public void setIcrSoltura(List<IcrSoltura> icrSoltura) {
		this.icrSoltura = icrSoltura;
	}

	@Transient
	public IcrTransferenciaReu getUltimaTransferencia() {
		List<IcrTransferenciaReu> transferencias = getIcrTransferencias();
		if (transferencias != null && !transferencias.isEmpty()) {
			Collections.sort(transferencias);
			for (int i = transferencias.size() - 1; i >= 0; i--) {
				if (transferencias.get(i).getAtivo()) {
					return transferencias.get(i);
				}
			}
		}

		return null;
	}

	@Transient
	public IcrFuga getUltimaFuga() {
		List<IcrFuga> fugas = getIcrFugas();
		if (fugas != null && !fugas.isEmpty()) {
			Collections.sort(fugas);
			for (int i = fugas.size() - 1; i >= 0; i--) {
				if (fugas.get(i).getAtivo()) {
					return fugas.get(i);
				}
			}
		}

		return null;
	}

	@Transient
	public IcrSoltura getUltimaSoltura() {
		List<IcrSoltura> solturas = getIcrSoltura();
		if (solturas != null && !solturas.isEmpty()) {
			Collections.sort(solturas);
			for (int i = solturas.size() - 1; i >= 0; i--) {
				if (solturas.get(i).getAtivo()) {
					return solturas.get(i);
				}
			}
		}

		return null;
	}

	@Transient
	public Boolean possuiTransferenciaFugaSoltura() {
		return (getUltimaTransferencia() != null || getUltimaFuga() != null || getUltimaSoltura() != null);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getDsMotivoEncerramento() == null) ? 0 : dsMotivoEncerramento.hashCode());
		result = prime * result + ((getDtEncerramentoPrisao() == null) ? 0 : dtEncerramentoPrisao.hashCode());
		result = prime * result + ((getEstabelecimentoPrisional() == null) ? 0 : estabelecimentoPrisional.hashCode());
		result = prime * result + ((getInTipoPrisao() == null) ? 0 : inTipoPrisao.hashCode());
		result = prime * result + ((getNrPrazoPrisao() == null) ? 0 : nrPrazoPrisao.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (obj instanceof IcrPrisao)
			return false;
		IcrPrisao other = (IcrPrisao) obj;
		if (getDsMotivoEncerramento() == null) {
			if (other.getDsMotivoEncerramento() != null)
				return false;
		} else if (!dsMotivoEncerramento.equals(other.getDsMotivoEncerramento()))
			return false;
		if (getDtEncerramentoPrisao() == null) {
			if (other.getDtEncerramentoPrisao() != null)
				return false;
		} else if (!dtEncerramentoPrisao.equals(other.getDtEncerramentoPrisao()))
			return false;
		if (getEstabelecimentoPrisional() == null) {
			if (other.getEstabelecimentoPrisional() != null)
				return false;
		} else if (!estabelecimentoPrisional.equals(other.getEstabelecimentoPrisional()))
			return false;
		if (getInTipoPrisao() == null) {
			if (other.getInTipoPrisao() != null)
				return false;
		} else if (!inTipoPrisao.equals(other.getInTipoPrisao()))
			return false;
		if (getNrPrazoPrisao() == null) {
			if (other.getNrPrazoPrisao() != null)
				return false;
		} else if (!nrPrazoPrisao.equals(other.getNrPrazoPrisao()))
			return false;
		return true;
	}

	@Override
	@javax.persistence.Transient
	public Class<? extends InformacaoCriminalRelevante> getEntityClass() {
		return IcrPrisao.class;
	}
}
