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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ForeignKey;

import br.jus.pje.jt.enums.ObrigacaoFazerEnum;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Entity
@Table(name = ObrigacaoFazer.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_obrigacao_fazer", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_obrigacao_fazer"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ObrigacaoFazer implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<ObrigacaoFazer,Integer> {

	private static final long serialVersionUID = 1L;
	public static final String TABLE_NAME = "tb_obrigacao_fazer";

	private int idObrigacaoFazer;
	private ProcessoParte credor;
	private ProcessoParte devedor;
	private ObrigacaoFazerEnum obrigacao;
	private ProcessoTrf processoTrf;
	private String descricao;
	private Integer prazo;
	private Boolean multaDescumprimento;
	private Boolean cumprida;

	@Id
	@Column(name = "id_obrigacao_fazer", unique = true, nullable = false)
	@GeneratedValue(generator = "gen_obrigacao_fazer")
	@NotNull
	public int getIdObrigacaoFazer() {
		return idObrigacaoFazer;
	}

	public void setIdObrigacaoFazer(int idObrigacaoFazer) {
		this.idObrigacaoFazer = idObrigacaoFazer;
	}

	@ManyToOne
	@JoinColumn(name = "id_credor", nullable = false)
	@ForeignKey(name = "fk_tb_obrig_faz_proc_part_cred")
	public ProcessoParte getCredor() {
		return credor;
	}

	public void setCredor(ProcessoParte credor) {
		this.credor = credor;
	}

	@ManyToOne
	@JoinColumn(name = "id_devedor", nullable = false)
	@ForeignKey(name = "fk_tb_obrig_faz_proc_part_deve")
	public ProcessoParte getDevedor() {
		return devedor;
	}

	public void setDevedor(ProcessoParte devedor) {
		this.devedor = devedor;
	}

	@Column(name = "cd_obrigacao", length = 3, nullable = false)
	@Enumerated(EnumType.STRING)
	@NotNull
	public ObrigacaoFazerEnum getObrigacao() {
		return obrigacao;
	}

	public void setObrigacao(ObrigacaoFazerEnum obrigacao) {
		this.obrigacao = obrigacao;
	}

	@OneToOne
	@JoinColumn(name = "id_processo_trf", nullable = false)
	@ForeignKey(name = "fk_tb_processo_jt_tb_proc_trf")
	@NotNull
	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@Column(name = "ds_descricao")
	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@Column(name = "nr_prazo", nullable = false)
	@NotNull
	public Integer getPrazo() {
		return prazo;
	}

	public void setPrazo(Integer prazo) {
		this.prazo = prazo;
	}

	@Column(name = "in_multa_descumprimento", nullable = false)
	@NotNull
	public Boolean getMultaDescumprimento() {
		return multaDescumprimento;
	}

	public void setMultaDescumprimento(Boolean multaDescumprimento) {
		this.multaDescumprimento = multaDescumprimento;
	}

	@Column(name = "in_cumprida")
	public Boolean getCumprida() {
		return cumprida;
	}

	public void setCumprida(Boolean cumprida) {
		this.cumprida = cumprida;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ObrigacaoFazer> getEntityClass() {
		return ObrigacaoFazer.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdObrigacaoFazer());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
