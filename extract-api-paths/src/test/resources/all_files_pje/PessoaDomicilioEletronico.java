package br.jus.pje.nucleo.entidades;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@javax.persistence.Cacheable(true)
@Table(name = "tb_pessoa_domicilio_eletronico", uniqueConstraints = @UniqueConstraint(columnNames = "nr_documento"))
@org.hibernate.annotations.GenericGenerator(name = "gen_pessoa_domicilio_eletronico", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_pessoa_domicilio_eletronico"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1") })
public class PessoaDomicilioEletronico {
	private Long id;
	private String numeroDocumento;
	private String tipoDocumento;
	private boolean habilitado;
	private Date dataAtualizacao;
	private LotePessoasDomicilioEletronico lote;
	private boolean pessoaJuridicaDireitoPublico;
	
	@Id
	@GeneratedValue(generator = "gen_pessoa_domicilio_eletronico")
	@Column(name = "id_pessoa_domicilio_eletronico", unique = true, nullable = false)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "nr_documento")
	public String getNumeroDocumento() {
		return numeroDocumento;
	}

	public void setNumeroDocumento(String numeroDocumento) {
		this.numeroDocumento = numeroDocumento;
	}

	@Column(name = "tipo_documento")
	public String getTipoDocumento() {
		return tipoDocumento;
	}

	public void setTipoDocumento(String tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}

	@Column(name = "in_habilitado")
	public boolean isHabilitado() {
		return habilitado;
	}

	public void setHabilitado(boolean habilitado) {
		this.habilitado = habilitado;
	}

	@Column(name = "dt_atualizacao")
	public Date getDataAtualizacao() {
		return dataAtualizacao;
	}

	public void setDataAtualizacao(Date dataAtualizacao) {
		this.dataAtualizacao = dataAtualizacao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_lote")
	public LotePessoasDomicilioEletronico getLote() {
		return lote;
	}

	public void setLote(LotePessoasDomicilioEletronico lote) {
		this.lote = lote;
	}
	
	@Column(name = "pj_direito_publico")
	public boolean isPessoaJuridicaDireitoPublico() {
		return pessoaJuridicaDireitoPublico;
	}

	public void setPessoaJuridicaDireitoPublico(boolean pessoaJuridicaDireitoPublico) {
		this.pessoaJuridicaDireitoPublico = pessoaJuridicaDireitoPublico;
	}
}
