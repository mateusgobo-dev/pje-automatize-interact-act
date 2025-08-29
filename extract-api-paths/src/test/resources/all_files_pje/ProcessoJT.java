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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ForeignKey;

import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Entity
@Table(name = ProcessoJT.TABLE_NAME, uniqueConstraints = { @UniqueConstraint(columnNames = { "id_processo_trf" }) })
public class ProcessoJT implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8913334761904150035L;

	public static final String TABLE_NAME = "tb_processo_jt";

	private int idProcessoJt;
	private ProcessoTrf processoTrf;
	private AtividadeEconomica atividadeEconomica;
	private MunicipioIBGE municipioIBGE;
	private List<ObrigacaoPagar> obrigacaoPagarList = new ArrayList<ObrigacaoPagar>(0);
	private OrgaoJulgador orgaoJulgadorRelatorOriginario;

	@Id
	@Column(name = "id_processo_jt", unique = true, nullable = false)
	@NotNull
	public int getIdProcessoJt() {
		return idProcessoJt;
	}

	public void setIdProcessoJt(int idProcessoJt) {
		this.idProcessoJt = idProcessoJt;
	}

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "id_processo_trf", nullable = false)
	@ForeignKey(name = "fk_tb_processo_jt_tb_proc_trf")
	@NotNull
	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "id_atividade_economica", nullable = true)
	@ForeignKey(name = "fk_tb_proc_jt_tb_atividad_econ")
	public AtividadeEconomica getAtividadeEconomica() {
		return atividadeEconomica;
	}

	public void setAtividadeEconomica(AtividadeEconomica atividadeEconomica) {
		this.atividadeEconomica = atividadeEconomica;
	}

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "id_municipio", nullable = false)
	@ForeignKey(name = "fk_tb_proc_jt_tb_munic_ibge")
	@NotNull
	public MunicipioIBGE getMunicipioIBGE() {
		return municipioIBGE;
	}

	public void setMunicipioIBGE(MunicipioIBGE municipioIBGE) {
		this.municipioIBGE = municipioIBGE;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "processoJT")
	public List<ObrigacaoPagar> getObrigacaoPagarList() {
		return obrigacaoPagarList;
	}

	public void setObrigacaoPagarList(List<ObrigacaoPagar> obrigacaoPagarList) {
		this.obrigacaoPagarList = obrigacaoPagarList;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_org_julg_relator_originario")
	public OrgaoJulgador getOrgaoJulgadorRelatorOriginario() {
		return orgaoJulgadorRelatorOriginario;
	}

	public void setOrgaoJulgadorRelatorOriginario(OrgaoJulgador orgaoJulgadorRelatorOriginario) {
		this.orgaoJulgadorRelatorOriginario = orgaoJulgadorRelatorOriginario;
	}
}
