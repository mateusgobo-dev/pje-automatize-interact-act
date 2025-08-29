package br.jus.pje.nucleo.entidades;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tb_lote_pessoas_domicilio_eletronico")
@org.hibernate.annotations.GenericGenerator(name = "gen_tb_lote_pessoas_domicilio_eletronico", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_lote_pessoas_domicilio_eletronico"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1") })
public class LotePessoasDomicilioEletronico {
	private Integer id;
	private String nomeArquivo;
	private Date dataProcessamento;

	@Id
	@GeneratedValue(generator = "gen_tb_lote_pessoas_domicilio_eletronico")
	@Column(name = "id_lote_pessoas_domicilio_eletronico", unique = true, nullable = false)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Column(name = "ds_arquivo")
	public String getNomeArquivo() {
		return nomeArquivo;
	}

	public void setNomeArquivo(String nomeArquivo) {
		this.nomeArquivo = nomeArquivo;
	}

	@Column(name = "dt_processamento")
	public Date getDataProcessamento() {
		return dataProcessamento;
	}

	public void setDataProcessamento(Date dataProcessamento) {
		this.dataProcessamento = dataProcessamento;
	}

}
