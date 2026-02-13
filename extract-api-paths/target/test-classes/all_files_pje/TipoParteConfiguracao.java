package br.jus.pje.nucleo.entidades;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_tipo_parte_configuracao")
@org.hibernate.annotations.GenericGenerator(name = "gen_tipo_parte_configuracao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tipo_parte_configuracao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class TipoParteConfiguracao implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(generator = "gen_tipo_parte_configuracao")
	@Column(name = "id_tipo_parte_configuracao", unique = true, nullable = false)
	private int idTipoParteConfiguracao;
	
	@OneToOne
	@JoinColumn(name = "id_tipo_parte", nullable = false)
	@NotNull
	private TipoParte tipoParte;
	
	@Column(name = "in_polo_ativo")
	private Boolean poloAtivo;
	
	@Column(name = "in_polo_passivo")
	private Boolean poloPassivo;
	
	@Column(name = "in_outros_participantes")
	private Boolean outrosParticipantes;
	
	@Column(name = "in_pessoa_fisica")
	private Boolean tipoPessoaFisica;
	
	@Column(name = "in_pessoa_juridica")
	private Boolean tipoPessoaJuridica;
	
	@Column(name = "in_ente_autoridade")
	private Boolean enteAutoridade;
	
	
	@Column(name = "in_padrao")
	private Boolean padrao;
	
	@Column(name = "in_oab")
	private Boolean oab;
	
	@Column(name = "in_parte_sigilosa")
	private Boolean parteSigilosa;
	
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "tipoParteConfiguracao")
	private List<TipoParteConfigClJudicial> tipoParteClasseJudicialList = new ArrayList<TipoParteConfigClJudicial>(0);

	public TipoParteConfiguracao() {
	}

	
	public TipoParte getTipoParte() {
		return tipoParte;
	}


	public void setTipoParte(TipoParte tipoParte) {
		this.tipoParte = tipoParte;
	}


	public Boolean getPoloAtivo() {
		return poloAtivo;
	}


	public void setPoloAtivo(Boolean poloAtivo) {
		this.poloAtivo = poloAtivo;
	}


	public Boolean getPoloPassivo() {
		return poloPassivo;
	}


	public void setPoloPassivo(Boolean poloPassivo) {
		this.poloPassivo = poloPassivo;
	}


	public Boolean getOutrosParticipantes() {
		return outrosParticipantes;
	}


	public void setOutrosParticipantes(Boolean outrosParticipantes) {
		this.outrosParticipantes = outrosParticipantes;
	}


	public Boolean getTipoPessoaFisica() {
		return tipoPessoaFisica;
	}


	public void setTipoPessoaFisica(Boolean tipoPessoaFisica) {
		this.tipoPessoaFisica = tipoPessoaFisica;
	}


	public Boolean getTipoPessoaJuridica() {
		return tipoPessoaJuridica;
	}


	public void setTipoPessoaJuridica(Boolean tipoPessoaJuridica) {
		this.tipoPessoaJuridica = tipoPessoaJuridica;
	}


	public Boolean getEnteAutoridade() {
		return enteAutoridade;
	}

	public void setEnteAutoridade(Boolean enteAutoridade) {
		this.enteAutoridade = enteAutoridade;
	}


	public Boolean getPadrao() {
		return padrao;
	}


	public void setPadrao(Boolean padrao) {
		this.padrao = padrao;
	}


	public Boolean getOab() {
		return oab;
	}


	public void setOab(Boolean oab) {
		this.oab = oab;
	}


	public int getIdTipoParteConfiguracao() {
		return idTipoParteConfiguracao;
	}

	public void setIdTipoParteConfiguracao(int idTipoParteConfiguracao) {
		this.idTipoParteConfiguracao = idTipoParteConfiguracao;
	}
	
	
	public List<TipoParteConfigClJudicial> getTipoParteClasseJudicialList() {
		return tipoParteClasseJudicialList;
	}


	public void setTipoParteClasseJudicialList(List<TipoParteConfigClJudicial> tipoParteClasseJudicialList) {
		this.tipoParteClasseJudicialList = tipoParteClasseJudicialList;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + idTipoParteConfiguracao;
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
		TipoParteConfiguracao other = (TipoParteConfiguracao) obj;
		if (idTipoParteConfiguracao != other.idTipoParteConfiguracao)
			return false;
		return true;
	}


	public Boolean getParteSigilosa() {
		return parteSigilosa;
	}


	public void setParteSigilosa(Boolean parteSigilosa) {
		this.parteSigilosa = parteSigilosa;
	}

}