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
package br.jus.pje.nucleo.entidades.editor;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import br.jus.pje.nucleo.enums.TipoNumeracaoEnum;

@Entity
@Table(name = NumeracaoDocumento.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_numeracao_documento", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_numeracao_documento"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class NumeracaoDocumento implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<NumeracaoDocumento,Integer> {

	private static final long serialVersionUID = 1L;
	public static final String TABLE_NAME = "tb_numeracao_documento";

	private int idNumeracaoDocumento;
	private int nivel;
	private int ordem;
	private TipoNumeracaoEnum tipoNumeracao;
	private String tipo;
	private String separador;

	@Id
	@GeneratedValue(generator = "gen_numeracao_documento")
	@Column(name = "id_numeracao_documento", unique = true, nullable = false)
	public int getIdNumeracaoDocumento() {
		return idNumeracaoDocumento;
	}

	public void setIdNumeracaoDocumento(int idNumeracaoDocumento) {
		this.idNumeracaoDocumento = idNumeracaoDocumento;
	}

	@Column(name = "nr_nivel", nullable = false)
	public int getNivel() {
		return nivel;
	}

	public void setNivel(int nivel) {
		this.nivel = nivel;
	}

	@Column(name = "nr_ordem", nullable = false)
	public int getOrdem() {
		return ordem;
	}

	public void setOrdem(int ordem) {
		this.ordem = ordem;
	}

	@Column(name = "ds_numeracao", nullable = false, length = 30)
	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	@Column(name = "ds_separdor", nullable = false, length = 50)
	public String getSeparador() {
		return separador;
	}

	public void setSeparador(String separador) {
		this.separador = separador;
	}

	@Enumerated
	@Column(name= "tp_numeracao")
	public TipoNumeracaoEnum getTipoNumeracao() {
		return tipoNumeracao;
	}

	public void setTipoNumeracao(TipoNumeracaoEnum tipoNumeracao) {
		this.tipoNumeracao = tipoNumeracao;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends NumeracaoDocumento> getEntityClass() {
		return NumeracaoDocumento.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdNumeracaoDocumento());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
