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
package br.jus.pje.nucleo.entidades.ws.consulta;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "vs_processo_conexo_webservice")
public class ConsultaProcessoConexoWebservice implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer idProcessoTrfConexao;
	private Integer idProcessoTrfConexo;
	private Integer idProcessoTrf;
	private String sessaoJudiciaria;
	private String tipoConexao;
	private Character validaPrevencao;
	private Date dataValidaPrevencao;
	private Integer numeroSequenciaProcessoConexo;
	private Integer numeroDigitoVerificadorProcessoConexo;
	private Integer anoProcessoConexo;
	private Integer numeroOrgaoJusticaProcessoConexo;
	private Integer numeroOrigemProcessoConexo;
	private String nomeOrgaoJulgador;
	private String listaPoloAtivo;
	private String listaPoloPassivo;
	private Date dataRegistro;

	@Id
	@Column(name = "id_processo_trf_conexao", insertable = false, updatable = false)
	public Integer getIdProcessoTrfConexao() {
		return idProcessoTrfConexao;
	}

	public void setIdProcessoTrfConexao(Integer idProcessoTrfConexao) {
		this.idProcessoTrfConexao = idProcessoTrfConexao;
	}

	@Column(name = "id_processo_trf_conexo", insertable = false, updatable = false)
	public Integer getIdProcessoTrfConexo() {
		return idProcessoTrfConexo;
	}

	public void setIdProcessoTrfConexo(Integer idProcessoTrfConexo) {
		this.idProcessoTrfConexo = idProcessoTrfConexo;
	}

	@Column(name = "id_processo_trf", insertable = false, updatable = false)
	public Integer getIdProcessoTrf() {
		return idProcessoTrf;
	}

	public void setIdProcessoTrf(Integer idProcessoTrf) {
		this.idProcessoTrf = idProcessoTrf;
	}

	@Column(name = "ds_sessao_judiciaria", insertable = false, updatable = false)
	public String getSessaoJudiciaria() {
		return sessaoJudiciaria;
	}

	public void setSessaoJudiciaria(String sessaoJudiciaria) {
		this.sessaoJudiciaria = sessaoJudiciaria;
	}

	@Column(name = "tp_tipo_conexao", insertable = false, updatable = false)
	public String getTipoConexao() {
		return tipoConexao;
	}

	public void setTipoConexao(String tipoConexao) {
		this.tipoConexao = tipoConexao;
	}

	@Column(name = "in_valida_prenvencao", insertable = false, updatable = false)
	public Character getValidaPrevencao() {
		return validaPrevencao;
	}

	public void setValidaPrevencao(Character validaPrevencao) {
		this.validaPrevencao = validaPrevencao;
	}

	@Column(name = "dt_valida_prevencao", insertable = false, updatable = false)
	public Date getDataValidaPrevencao() {
		return dataValidaPrevencao;
	}

	public void setDataValidaPrevencao(Date dataValidaPrevencao) {
		this.dataValidaPrevencao = dataValidaPrevencao;
	}

	@Column(name = "nr_sequencia", insertable = false, updatable = false)
	public Integer getNumeroSequenciaProcessoConexo() {
		return numeroSequenciaProcessoConexo;
	}

	public void setNumeroSequenciaProcessoConexo(Integer numeroSequenciaProcessoConexo) {
		this.numeroSequenciaProcessoConexo = numeroSequenciaProcessoConexo;
	}

	@Column(name = "nr_digito_verificador", insertable = false, updatable = false)
	public Integer getNumeroDigitoVerificadorProcessoConexo() {
		return numeroDigitoVerificadorProcessoConexo;
	}

	public void setNumeroDigitoVerificadorProcessoConexo(Integer numeroDigitoVerificadorProcessoConexo) {
		this.numeroDigitoVerificadorProcessoConexo = numeroDigitoVerificadorProcessoConexo;
	}

	@Column(name = "nr_ano", insertable = false, updatable = false)
	public Integer getAnoProcessoConexo() {
		return anoProcessoConexo;
	}

	public void setAnoProcessoConexo(Integer anoProcessoConexo) {
		this.anoProcessoConexo = anoProcessoConexo;
	}

	@Column(name = "nr_identificacao_orgao_justica", insertable = false, updatable = false)
	public Integer getNumeroOrgaoJusticaProcessoConexo() {
		return numeroOrgaoJusticaProcessoConexo;
	}

	public void setNumeroOrgaoJusticaProcessoConexo(Integer numeroOrgaoJusticaProcessoConexo) {
		this.numeroOrgaoJusticaProcessoConexo = numeroOrgaoJusticaProcessoConexo;
	}

	@Column(name = "nr_origem_processo", insertable = false, updatable = false)
	public Integer getNumeroOrigemProcessoConexo() {
		return numeroOrigemProcessoConexo;
	}

	public void setNumeroOrigemProcessoConexo(Integer numeroOrigemProcessoConexo) {
		this.numeroOrigemProcessoConexo = numeroOrigemProcessoConexo;
	}

	@Column(name = "ds_orgao_julgador", insertable = false, updatable = false)
	public String getNomeOrgaoJulgador() {
		return nomeOrgaoJulgador;
	}

	public void setNomeOrgaoJulgador(String nomeOrgaoJulgador) {
		this.nomeOrgaoJulgador = nomeOrgaoJulgador;
	}

	@Transient
	public String getListaPoloAtivo() {
		return listaPoloAtivo;
	}

	public void setListaPoloAtivo(String listaPoloAtivo) {
		this.listaPoloAtivo = listaPoloAtivo;
	}

	@Transient
	public String getListaPoloPassivo() {
		return listaPoloPassivo;
	}

	public void setListaPoloPassivo(String listaPoloPassivo) {
		this.listaPoloPassivo = listaPoloPassivo;
	}

	@Column(name = "dt_registro", insertable = false, updatable = false)
	public Date getDataRegistro() {
		return dataRegistro;
	}

	public void setDataRegistro(Date dataRegistro) {
		this.dataRegistro = dataRegistro;
	}

}
