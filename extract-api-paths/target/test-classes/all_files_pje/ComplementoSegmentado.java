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
package br.jus.pje.nucleo.entidades.lancadormovimento;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

import br.jus.pje.nucleo.entidades.ProcessoEvento;

/**
 * Classe que representa uma instância de um complemento em um movimento do
 * processo.
 */
@Entity
@Table(name = ComplementoSegmentado.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_complemento_segmentado", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_complemento_segmentado"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ComplementoSegmentado implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ComplementoSegmentado,Long> {

	public static final String TABLE_NAME = "tb_complemento_segmentado";
	private static final long serialVersionUID = 1L;

	private Long idComplementoSegmentado;
	private ProcessoEvento movimentoProcesso;
	private TipoComplemento tipoComplemento;
	private String valorComplemento;
	private String texto;
	private Integer ordem;
	private Boolean visibilidadeExterna = null;
	private Boolean multivalorado;

	public ComplementoSegmentado() {
	}

	@Id
	@GeneratedValue(generator = "gen_complemento_segmentado")
	@Column(name = "id_complemento_segmentado", unique = true, nullable = false)
	public Long getIdComplementoSegmentado() {
		return idComplementoSegmentado;
	}

	public void setIdComplementoSegmentado(Long idComplementoSegmentado) {
		this.idComplementoSegmentado = idComplementoSegmentado;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_movimento_processo", nullable = false)
	@ForeignKey(name = "id_movimento_processo_fkey")
	public ProcessoEvento getMovimentoProcesso() {
		return movimentoProcesso;
	}

	public void setMovimentoProcesso(ProcessoEvento movimentoProcesso) {
		this.movimentoProcesso = movimentoProcesso;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_tipo_complemento", nullable = false)
	@ForeignKey(name = "id_tipo_complemento_fkey")
	public TipoComplemento getTipoComplemento() {
		return tipoComplemento;
	}

	public void setTipoComplemento(TipoComplemento tipoComplemento) {
		this.tipoComplemento = tipoComplemento;
	}

	@Column(name = "in_multivalorado")
	public Boolean getMultivalorado() {
		return multivalorado;
	}

	public void setMultivalorado(Boolean multivalorado) {
		this.multivalorado = multivalorado;
	}

	@Column(name = "in_visibilidade_externa")
	public Boolean getVisibilidadeExterna() {
		return visibilidadeExterna;
	}

	public void setVisibilidadeExterna(Boolean visibilidadeExterna) {
		this.visibilidadeExterna = visibilidadeExterna;
	}

	@Column(name = "ds_valor_complemento")
	public String getValorComplemento() {
		return valorComplemento;
	}

	public void setValorComplemento(String valorComplemento) {
		this.valorComplemento = valorComplemento;
	}

	@Column(name = "ds_texto")
	public String getTexto() {
		return texto;
	}

	public void setTexto(String texto) {
		this.texto = texto;
	}

	@Column(name = "vl_ordem")
	public Integer getOrdem() {
		return ordem;
	}

	public void setOrdem(Integer ordem) {
		this.ordem = ordem;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idComplementoSegmentado == null) ? 0 : idComplementoSegmentado.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ComplementoSegmentado)) {
			return false;
		}
		ComplementoSegmentado other = (ComplementoSegmentado) obj;
		if (idComplementoSegmentado == null) {
			if (other.getIdComplementoSegmentado() != null) {
				return false;
			}
		} else if (!idComplementoSegmentado.equals(other.getIdComplementoSegmentado())) {
			return false;
		}
		return true;
	}

    //Implementação de IEntidade    
	@Override
	@javax.persistence.Transient
	public Class<? extends ComplementoSegmentado> getEntityClass() {
		return ComplementoSegmentado.class;
	}

	@Override
	@javax.persistence.Transient
	public Long getEntityIdObject() {
		return getIdComplementoSegmentado();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
