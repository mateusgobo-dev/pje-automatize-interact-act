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
package br.jus.pje.jt.entidades.estatistica;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.OrderBy;

/**
 * @author Sérgio Pacheco / Sérgio Simoes
 * @since 1.4.3
 * @category PJE-JT
 * @class Quadro
 * @description Classe que representa a definicao de um quadro de boletim
 *              de orgao julgador. 
 */
@Entity
@Table(name = Quadro.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_quadro_boletim", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_quadro_boletim"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Quadro implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Quadro,Integer> {

	public static final String TABLE_NAME = "tb_quadro_boletim";
	private static final long serialVersionUID = 1L;
	
	private Integer idQuadro;	
	
	private Boletim boletim;
	
	private TipoQuadroEnum tipoQuadro;

	private Integer ordem;

	private String nome;
	 
	private String descricao;
		 
	private String tituloRegiao;

	private List<RegiaoQuadro> regioesQuadro = new ArrayList<RegiaoQuadro>(0);
	 
	@Id
	@GeneratedValue(generator = "gen_quadro_boletim")
	@Column(name = "id_quadro", unique = true, nullable = false)
	public Integer getIdQuadro() {
		return this.idQuadro;
	}

	public void setIdQuadro(Integer idQuadro) {
		this.idQuadro = idQuadro;
	}
	
	public Quadro() {
		super();
	}

	public Quadro(Boletim boletim) {
		super();
		this.boletim = boletim;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_boletim", nullable = false)
	@ForeignKey(name = "fk_tb_quadro_boletim_tb_boletim")
	@NotNull
	public Boletim getBoletim() {
		return boletim;
	}

	public void setBoletim(Boletim boletim) {
		this.boletim = boletim;
	}

	@Column(name = "tp_quadro", length = 1, nullable = false)
	@Enumerated(EnumType.STRING)
	public TipoQuadroEnum getTipoQuadro() {
		return tipoQuadro;
	}

	public void setTipoQuadro(TipoQuadroEnum tipoQuadro) {
		this.tipoQuadro = tipoQuadro;
	}

	@Column(name = "nr_ordem", nullable = false)
	public Integer getOrdem() {
		return ordem;
	}

	public void setOrdem(Integer ordem) {
		this.ordem = ordem;
	}
	
	@Column(name = "ds_nome", nullable = false)
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Column(name = "ds_descricao")
	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
	
	@Column(name = "ds_titulo_regiao")
	public String getTituloRegiao() {
		return tituloRegiao;
	}

	public void setTituloRegiao(String tituloRegiao) {
		this.tituloRegiao = tituloRegiao;
	}
	
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "quadro")
	@OrderBy(clause = "nr_ordem" )
	public List<RegiaoQuadro> getRegioesQuadro() {
		return regioesQuadro;
	}

	public void setRegioesQuadro(List<RegiaoQuadro> regioesQuadro) {
		this.regioesQuadro = regioesQuadro;
	}
	
	@Transient
	public String getValorItem(String nome) {
		return null;
	}

	public RegiaoQuadro addRegiaoQuadro() {
		RegiaoQuadro regiaoQuadro = new RegiaoQuadro(this);
		this.regioesQuadro.add(regiaoQuadro);
		return regiaoQuadro;
	}

	public Integer profundidadeItens() {
		Integer maxNivel = 0;
		
		for (RegiaoQuadro regiaoQuadro : this.getRegioesQuadro()) {
			for (ItemQuadro itemQuadro : regiaoQuadro.getItensQuadro()) {
				Integer nivel = itemQuadro.nivel();
				maxNivel = nivel > maxNivel ? nivel : maxNivel;
			}
		}
		
		return maxNivel;
	}

	public Integer maxGeracoes() {
		int max = 0;
		for (RegiaoQuadro regiaoQuadro : getRegioesQuadro()) {
			int geracao = regiaoQuadro.maxGeracoes();
			max = geracao > max ? geracao : max;
		}
		return max;
	}	
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Quadro)) {
			return false;
		}
		Quadro outroQuadro = (Quadro) obj;
		if (getIdQuadro() == 0 && getBoletim() != null) {
			if( getBoletim().equals(outroQuadro.getBoletim()) &&
				getOrdem().equals(outroQuadro.getOrdem())	)
			return true;
		}
		return getIdQuadro().equals(outroQuadro.getIdQuadro());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getIdQuadro() == null) ? 0 : getIdQuadro().hashCode());
		result = prime * result + ((getBoletim() == null) ? 0 : getBoletim().hashCode());
		result = prime * result + ((getOrdem() == null) ? 0 : getOrdem().hashCode());
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Quadro> getEntityClass() {
		return Quadro.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdQuadro();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
