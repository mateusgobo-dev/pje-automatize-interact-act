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
package br.jus.pje.nucleo.entidades.identidade;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ForeignKey;
import org.hibernate.validator.constraints.Length;

@Entity
@javax.persistence.Cacheable(true)
@Table(name = "tb_papel", uniqueConstraints = @UniqueConstraint(columnNames = "ds_identificador"))
@org.hibernate.annotations.GenericGenerator(name = "gen_papel", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_papel"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Papel implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Papel,Integer>, Comparable<Papel> {

	private static final long serialVersionUID = 1L;

	private int idPapel;
	private String nome;
	private String identificador;
	private boolean condicional;

	private String idsPapeisInferiores;
	
	private List<Papel> grupos;
	
	private List<Papel> herdeiros;

	public Papel() {
	}

	@Id
	@GeneratedValue(generator = "gen_papel")
	@Column(name = "id_papel", unique = true, nullable = false)
	public int getIdPapel() {
		return this.idPapel;
	}

	public void setIdPapel(int idPerfil) {
		this.idPapel = idPerfil;
	}

	@Column(name = "ds_nome", length = 100)
	@Length(max = 100)
	public String getNome() {
		return this.nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Column(name = "ds_identificador", length = 100)
	@Length(max = 100)
	@NotNull
	public String getIdentificador() {
		return this.identificador;
	}

	public void setIdentificador(String identificador) {
		this.identificador = identificador;
	}

	@ManyToMany
	@JoinTable(name = "tb_papel_grupo", joinColumns = @JoinColumn(name = "id_papel"), inverseJoinColumns = @JoinColumn(name = "membro_do_grupo"))
	@ForeignKey(name = "tb_papel_grupo_papel_fk", inverseName = "tb_papel_grupo_membro_fk")
	@OrderBy("nome")
	public List<Papel> getGrupos() {
		if(this.grupos == null){
			this.grupos = new ArrayList<Papel>(0);
		}
		return this.grupos;
	}
	
	public void setGrupos(List<Papel> grupos) {
		this.grupos = grupos;
	}
	
	@ManyToMany
	@JoinTable(name = "tb_papel_grupo", inverseJoinColumns = @JoinColumn(name = "id_papel"), joinColumns = @JoinColumn(name = "membro_do_grupo"))
	@OrderBy("nome")
	public List<Papel> getHerdeiros() {
		return herdeiros;
	}
	
	public void setHerdeiros(List<Papel> herdeiros) {
		this.herdeiros = herdeiros;
	}

	@Column(name = "in_condicional")
	public boolean isCondicional() {
		return condicional;
	}

	public void setCondicional(boolean condicional) {
		this.condicional = condicional;
	}
	
	@Column(name = "ids_papeis_inferiores", length = 7500, nullable=true)
	public String getIdsPapeisInferiores() {
		return idsPapeisInferiores;
	}
	
	public void setIdsPapeisInferiores(String idsPapeisInferiores) {
		this.idsPapeisInferiores = idsPapeisInferiores;
	}

	@Transient
	public List<Integer> getListIdsPapeisInferiores() {
		ArrayList<Integer> idsInferiores = new ArrayList<>();
		if(this.idsPapeisInferiores != null) {
			for (String strIdPapel : this.idsPapeisInferiores.split(":")) {
				if(!strIdPapel.isEmpty()) {
					idsInferiores.add(Integer.parseInt(strIdPapel.trim()));
				}
			}
		}
		return idsInferiores;
	}

	@Override
	public String toString() {
		if (this.nome == null) {
			return this.identificador;
		}
		return this.nome;
	}

	@Transient
	public boolean getAtivo() {
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + idPapel;
		return result;
	}

	/**
	 * Indica se um determinado {@link Papel} Ã© negocialmente igual a este. A
	 * igualdade Ã© constatada em duas hipÃ³teses: <li>se o objeto passado for o
	 * mesmo objeto de memÃ³ria que este; ou</li> <li>se o objeto passado for um
	 * {@link Papel} ou herdeiro de papel e seus identificadores forem
	 * idÃªnticos.</li>
	 * 
	 * @param obj
	 *            O objeto a ser comparado
	 * @return true, se o objeto passado for um papel negocialmente idÃªntico a
	 *         este.
	 * 
	 * @see Object#equals(Object)
	 * 
	 * @author Tiago Zanon
	 * @author Haroldo Arouca
	 * @author Rafael Carvalho
	 * @author Paulo CristovÃ£o Filho
	 * @since 1.2.0
	 * @category PJE-JT
	 * @category PJE-103
	 * @category PJE-490
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (Papel.class.isAssignableFrom(obj.getClass())) {
			if (idPapel == ((Papel) obj).getIdPapel()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int compareTo(Papel papel) {
		if(papel == null){
			throw new NullPointerException();
		}
		if(getIdentificador() == null){
			if(papel.getIdentificador() == null){
				return idPapel == papel.getIdPapel() ? 0 : (idPapel > papel.getIdPapel() ? 1 : -1);
			}else{
				return -1;
			}
		}else{
			if(papel.getIdentificador() == null){
				return idPapel == papel.getIdPapel() ? 0 : (idPapel > papel.getIdPapel() ? 1 : -1);
			}else{
				return this.identificador.compareTo(papel.getIdentificador());
			}
		}
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Papel> getEntityClass() {
		return Papel.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdPapel());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
