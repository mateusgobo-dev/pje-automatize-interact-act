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
package br.jus.pje.jt.entidades;

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
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.jt.enums.SituacaoSessaoEnum;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.PessoaProcurador;
import br.jus.pje.nucleo.entidades.SalaHorario;
import br.jus.pje.nucleo.entidades.TipoSessao;
import br.jus.pje.nucleo.entidades.Usuario;


@Entity
@Table(name = SessaoJT.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_jt_sessao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "SQ_TB_JT_SESSAO"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class SessaoJT implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<SessaoJT,Integer> {

	public static final String TABLE_NAME = "tb_jt_sessao";
	private static final long serialVersionUID = 1L;

	private int idSessao;
	private TipoSessao tipoSessao;
	private OrgaoJulgadorColegiado orgaoJulgadorColegiado;
	private SalaHorario salaHorario;
	private Date dataSessao;
	private Date dataFechamentoPauta;
	private PessoaProcurador pessoaProcurador;
	private String procurador;
	private SituacaoSessaoEnum situacaoSessao;
	private Date dataSituacaoSessao;
	private Usuario usuarioSituacaoSessao;
	
	public SessaoJT() {
	}

	@Id
	@GeneratedValue(generator = "gen_jt_sessao")
	@Column(name = "id_sessao", unique = true, nullable = false)
	public int getIdSessao() {
		return this.idSessao;
	}

	public void setIdSessao(int idSessao) {
		this.idSessao = idSessao;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tipo_sessao", nullable = false)
	@NotNull
	public TipoSessao getTipoSessao() {
		return this.tipoSessao;
	}

	public void setTipoSessao(TipoSessao tipoSessao) {
		this.tipoSessao = tipoSessao;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador_colegiado",  nullable = false)
	@NotNull
	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiado() {
		return this.orgaoJulgadorColegiado;
	}

	public void setOrgaoJulgadorColegiado(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_sala_horario", nullable = false)
	@NotNull
	public SalaHorario getSalaHorario() {
		return this.salaHorario;
	}

	public void setSalaHorario(SalaHorario salaHorario) {
		this.salaHorario = salaHorario;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_sessao", nullable = false)
	@NotNull
	public Date getDataSessao() {
		return dataSessao;
	}

	public void setDataSessao(Date dataSessao) {
		this.dataSessao = dataSessao;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_fechamento_pauta")
	public Date getDataFechamentoPauta() {
		return dataFechamentoPauta;
	}

	public void setDataFechamentoPauta(Date dataFechamentoPauta) {
		this.dataFechamentoPauta = dataFechamentoPauta;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_procurador")
	public PessoaProcurador getPessoaProcurador() {
		return pessoaProcurador;
	}
	
	public void setPessoaProcurador(PessoaProcurador pessoaProcurador) {
		this.pessoaProcurador = pessoaProcurador;
	}
	
	@Column(name = "ds_procurador", length = 150)
	@Length(max = 150)
	public String getProcurador() {
		return procurador;
	}
	
	public void setProcurador(String procurador) {
		this.procurador = procurador;
	}
	
	@Column(name = "in_situacao_sessao", nullable = false)
	@NotNull
	@Enumerated(EnumType.STRING)
	public SituacaoSessaoEnum getSituacaoSessao() {
		return situacaoSessao;
	}

	public void setSituacaoSessao(SituacaoSessaoEnum situacaoSessao) {
		this.situacaoSessao = situacaoSessao;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_situacao_sessao", nullable = false)
	@NotNull
	public Date getDataSituacaoSessao() {
		return dataSituacaoSessao;
	}

	public void setDataSituacaoSessao(Date dataSituacaoSessao) {
		this.dataSituacaoSessao = dataSituacaoSessao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario_situacao_sessao", nullable = false)
	@NotNull
	public Usuario getUsuarioSituacaoSessao() {
		return this.usuarioSituacaoSessao;
	}

	public void setUsuarioSituacaoSessao(Usuario usuarioSituacaoSessao) {
		this.usuarioSituacaoSessao = usuarioSituacaoSessao;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SessaoJT)) {
			return false;
		}
		SessaoJT other = (SessaoJT) obj;
		if (getIdSessao() != other.getIdSessao()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdSessao();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends SessaoJT> getEntityClass() {
		return SessaoJT.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdSessao());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
