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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_tipo_suspensao")
@org.hibernate.annotations.GenericGenerator(name = "gen_tipo_suspensao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tipo_suspensao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class TipoSuspensao implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<TipoSuspensao,Integer>{

	private static final long serialVersionUID = -7823990890676142171L;

	private Integer id;
	private String descricao;
	private Date dataInicioVigencia;
	private Date dataTerminoVigencia;
	private Boolean prazoSuspencaoDia;
	private Boolean prazoSuspencaoMes;
	private Boolean prazoSuspencaoAno;
	private Boolean prazoSuspencaoObrigatorio = true;
	private Boolean dataPrevistaTermino = true;
	private Boolean acompanhamentoCondicao = true;
	private Boolean ativo = true;
	private List<CondicaoSuspensao> condicoesParaSuspensao = new ArrayList<CondicaoSuspensao>(0);

	@Id
	@GeneratedValue(generator = "gen_tipo_suspensao")
	@Column(name = "id_tipo_suspensao", unique = true, nullable = false)
	public Integer getId(){
		return id;
	}

	public void setId(Integer id){
		this.id = id;
	}

	@Column(name = "ds_tipo_suspensao", nullable = false)
	@NotNull
	public String getDescricao(){
		return descricao;
	}

	public void setDescricao(String descricao){
		this.descricao = descricao;
	}

	@NotNull
	@Temporal(TemporalType.DATE)
	@Column(name = "dt_inicio_vigencia", nullable = false)
	public Date getDataInicioVigencia(){
		return dataInicioVigencia;
	}

	public void setDataInicioVigencia(Date dataInicioVigencia){
		this.dataInicioVigencia = dataInicioVigencia;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "dt_termino_vigencia", nullable = true)
	public Date getDataTerminoVigencia(){
		return dataTerminoVigencia;
	}

	public void setDataTerminoVigencia(Date dataTerminoVigencia){
		this.dataTerminoVigencia = dataTerminoVigencia;
	}

	@Column(name = "in_prazo_suspensao_dia")
	public Boolean getPrazoSuspencaoDia(){
		return prazoSuspencaoDia;
	}

	public void setPrazoSuspencaoDia(Boolean prazoSuspencaoDia){
		this.prazoSuspencaoDia = prazoSuspencaoDia;
	}

	@Column(name = "in_prazo_suspensao_mes")
	public Boolean getPrazoSuspencaoMes(){
		return prazoSuspencaoMes;
	}

	public void setPrazoSuspencaoMes(Boolean prazoSuspencaoMes){
		this.prazoSuspencaoMes = prazoSuspencaoMes;
	}

	@Column(name = "in_prazo_suspensao_ano")
	public Boolean getPrazoSuspencaoAno(){
		return prazoSuspencaoAno;
	}

	public void setPrazoSuspencaoAno(Boolean prazoSuspencaoAno){
		this.prazoSuspencaoAno = prazoSuspencaoAno;
	}

	@Column(name = "in_prazo_suspensao_obrigatorio", nullable = false)
	@NotNull
	public Boolean getPrazoSuspencaoObrigatorio(){
		return prazoSuspencaoObrigatorio;
	}

	public void setPrazoSuspencaoObrigatorio(Boolean prazoSuspencaoObrigatorio){
		this.prazoSuspencaoObrigatorio = prazoSuspencaoObrigatorio;
	}

	@Column(name = "in_data_prevista_termino", nullable = false)
	@NotNull
	public Boolean getDataPrevistaTermino(){
		return dataPrevistaTermino;
	}

	public void setDataPrevistaTermino(Boolean dataPrevistaTermino){
		this.dataPrevistaTermino = dataPrevistaTermino;
	}

	@Column(name = "in_acompanhamento_condicao", nullable = false)
	@NotNull
	public Boolean getAcompanhamentoCondicao(){
		return acompanhamentoCondicao;
	}

	public void setAcompanhamentoCondicao(Boolean acompanhamentoCondicao){
		this.acompanhamentoCondicao = acompanhamentoCondicao;
	}

	@OneToMany(mappedBy = "tipoSuspensao", cascade = CascadeType.ALL)
	public List<CondicaoSuspensao> getCondicoesParaSuspensao(){
		return condicoesParaSuspensao;
	}

	public void setCondicoesParaSuspensao(List<CondicaoSuspensao> condicoesParaSuspensao){
		this.condicoesParaSuspensao = condicoesParaSuspensao;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo(){
		return ativo;
	}

	public void setAtivo(Boolean ativo){
		this.ativo = ativo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ativo == null) ? 0 : ativo.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TipoSuspensao other = (TipoSuspensao) obj;
		if (ativo == null) {
			if (other.ativo != null)
				return false;
		} else if (!ativo.equals(other.ativo))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TipoSuspensao> getEntityClass() {
		return TipoSuspensao.class;
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
