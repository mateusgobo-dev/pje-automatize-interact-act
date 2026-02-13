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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ForeignKey;

import br.jus.pje.nucleo.entidades.AplicabilidadeView;

/**
 * Classe que representa a aplicacao dos domínios em uma determinada
 * aplicabilidade de orgão da justiça, classe e sujeito ativo.
 */
@Entity
@Table(name = AplicacaoDominio.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_aplic_dominio", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_aplicacao_dominio"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class AplicacaoDominio implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<AplicacaoDominio,Integer> {

	private static final long serialVersionUID = 861716586026552914L;

	public static final String TABLE_NAME = "tb_aplicacao_dominio";

	private int idAplicacaoDominio;

	private Dominio dominio;
	private AplicabilidadeView aplicabilidade;

	public AplicacaoDominio() {
	}

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "id_dominio")
	@NotNull
	@ForeignKey(name = "id_dominio_fkey")
	public Dominio getDominio() {
		return dominio;
	}

	public void setDominio(Dominio dominio) {
		this.dominio = dominio;
	}

	@Id
	@GeneratedValue(generator = "gen_aplic_dominio")
	@Column(name = "id_aplicacao_dominio", unique = true, nullable = false)
	public int getIdAplicacaoDominio() {
		return idAplicacaoDominio;
	}

	public void setIdAplicacaoDominio(int idAplicacaoDominio) {
		this.idAplicacaoDominio = idAplicacaoDominio;
	}

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "id_aplicabilidade", nullable = false)
	@ForeignKey(name = "id_aplicabilidade_fkey")
	public AplicabilidadeView getAplicabilidade() {
		return aplicabilidade;
	}

	public void setAplicabilidade(AplicabilidadeView aplicabilidade) {
		this.aplicabilidade = aplicabilidade;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends AplicacaoDominio> getEntityClass() {
		return AplicacaoDominio.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdAplicacaoDominio());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
