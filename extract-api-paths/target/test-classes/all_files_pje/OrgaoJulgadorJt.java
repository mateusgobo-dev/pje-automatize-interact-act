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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

import br.jus.pje.nucleo.entidades.OrgaoJulgador;

/**
 * Classe utilizada para acomodar informacoes da JT que complementam o orgao
 * julgador do CNJ.
 * 
 * @author Rodrigo Cartaxo / Haroldo Arouca
 * @since versao 1.2.0
 * @see OrgaoJulgador, [PJE336]
 * @category PJE-JT
 */

@Entity
@Table(name = OrgaoJulgadorJt.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_orgao_julgador_jt", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_orgao_julgador_jt"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class OrgaoJulgadorJt implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<OrgaoJulgadorJt,Integer> {

	private static final long serialVersionUID = 1L;
	public static final String TABLE_NAME = "tb_orgao_julgador_jt";

	private Integer idOrgaoJulgadorJt;
	private Integer intersticio;
	private OrgaoJulgador orgaoJulgador;

	@Id
	@GeneratedValue(generator = "gen_orgao_julgador_jt")
	@Column(name = "id_orgao_julgador_jt", unique = true, nullable = false)
	public Integer getIdOrgaoJulgadorJt() {
		return idOrgaoJulgadorJt;
	}

	public void setIdOrgaoJulgadorJt(Integer idOrgaoJulgadorJt) {
		this.idOrgaoJulgadorJt = idOrgaoJulgadorJt;
	}

	@Column(name = "nr_intersticio")
	public Integer getIntersticio() {
		return intersticio;
	}

	public void setIntersticio(Integer intersticio) {
		this.intersticio = intersticio;
	}

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "id_orgao_julgador")
	@ForeignKey(name = "orgao_julgador_fkey")
	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends OrgaoJulgadorJt> getEntityClass() {
		return OrgaoJulgadorJt.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdOrgaoJulgadorJt();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
