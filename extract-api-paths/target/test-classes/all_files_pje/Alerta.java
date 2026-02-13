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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

import br.jus.pje.nucleo.enums.CriticidadeAlertaEnum;

/**
 * @author thiago.vieira
 * 
 */
@Entity
@javax.persistence.Cacheable(true)
@Table(name = Alerta.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_alerta", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_alerta"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Alerta implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Alerta,Integer> {

	public static final String TABLE_NAME = "tb_alerta";
	private static final long serialVersionUID = 1L;

	private int idAlerta;
	private String alerta;
	private Date dataAlerta;
	private CriticidadeAlertaEnum inCriticidade;
	private Boolean ativo;
	private OrgaoJulgador orgaoJulgador;
	private OrgaoJulgadorColegiado orgaoJulgadorColegiado;
	private Localizacao localizacao;
	private List<ProcessoAlerta> processoAlertaList = new ArrayList<ProcessoAlerta>(0);

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "gen_alerta")
	@Column(name = "id_alerta", unique = true, nullable = false)
	public int getIdAlerta() {
		return idAlerta;
	}

	public void setIdAlerta(int idAlerta) {
		this.idAlerta = idAlerta;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_alerta", nullable = false)
	@NotNull
	public String getAlerta() {
		return alerta;
	}

	public void setAlerta(String alerta) {
		this.alerta = alerta;
	}

	public void setDataAlerta(Date dataAlerta) {
		this.dataAlerta = dataAlerta;
	}

	@Column(name = "dt_alerta", nullable = false)
	public Date getDataAlerta() {
		return dataAlerta;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "alerta")
	public List<ProcessoAlerta> getProcessoAlertaList() {
		return processoAlertaList;
	}

	public void setProcessoAlertaList(List<ProcessoAlerta> processoAlertaList) {
		this.processoAlertaList = processoAlertaList;
	}

	@Column(name = "in_criticidade", length = 1)
	@Enumerated(EnumType.STRING)
	public CriticidadeAlertaEnum getInCriticidade() {
		return this.inCriticidade;
	}

	public void setInCriticidade(CriticidadeAlertaEnum inCriticidade) {
		this.inCriticidade = inCriticidade;
	}

	@Override
	public String toString() {
		return alerta;
	}

	/**
	 * Método que retorna a lista de processos cadastrados para um alerta. O cadastro de processo para
	 * alerta não pode ser excluído, apenas inativado, ou seja, a relação process_alerta e alerta torna-se inativa.
	 * A lista retornada nesse método é de processos em que a relação processo_alerta e alerta está ativa.
	 * 
	 * @return lista de processos cadastrados para um alerta.
	 */
	@Transient
	public List<ProcessoTrf> getProcessosLista() {
		List<ProcessoTrf> lista = new ArrayList<ProcessoTrf>(0);
		for (ProcessoAlerta p : this.getProcessoAlertaList()) {
			if (p.getAtivo()) {
				lista.add(p.getProcessoTrf());
			}
		}
		return lista;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador")
	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador_colegiado")
	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiado() {
		return orgaoJulgadorColegiado;
	}

	public void setOrgaoJulgadorColegiado(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Alerta> getEntityClass() {
		return Alerta.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdAlerta());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_localizacao")
	public Localizacao getLocalizacao() {
		return localizacao;
	}

	public void setLocalizacao(Localizacao localizacao) {
		this.localizacao = localizacao;
	}
}
