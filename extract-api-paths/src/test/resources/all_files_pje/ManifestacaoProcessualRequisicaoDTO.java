/*
 * ConsultarProcessoRequisicaoTO.java
 *
 * Data: 29/07/2020
 */
package br.jus.cnj.pje.intercomunicacao.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import br.jus.cnj.pje.ws.client.ConsultaPJeClient;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

/**
 * @author Adriano Pamplona
 */
public class ManifestacaoProcessualRequisicaoDTO implements Serializable {
	private String login;
	private String senha;
	private String numeroProcesso;
	private ProcessoTrf processoTrf;
	private Date dataEnvio;
	private List<Properties> parametros = new ArrayList<>();
	private Integer tamanhoProcesso;
	private Map<Integer, Properties> mapaParametrosDocumento = new HashMap<>();

	//Verificar para remover os atributos abaixo.
	private ConsultaPJeClient consultaPJeClient;
	private Integer competenciaConflito;
	private Boolean isRequisicaoPJE = Boolean.TRUE;
	
	public void addParametro(String chave, String valor) {
		Properties property = new Properties();
		property.setProperty(chave, valor);
		getParametros().add(property);
	}
	
	public void addProcessoDocumento(ProcessoDocumento documento) {
		getProcesso().getProcessoDocumentoList().add(documento);
	}
	
	public List<ProcessoDocumento> getProcessoDocumentoList() {
		return getProcesso().getProcessoDocumentoList();
	}
	
	protected Processo getProcesso() {
		if (getProcessoTrf().getProcesso() == null) {
			getProcessoTrf().setProcesso(new Processo());
		}
		return getProcessoTrf().getProcesso();
	}
	/**
	 * @return login.
	 */
	public String getLogin() {
		return login;
	}

	/**
	 * @param login Atribui login.
	 */
	public void setLogin(String login) {
		this.login = login;
	}

	/**
	 * @return senha.
	 */
	public String getSenha() {
		return senha;
	}

	/**
	 * @param senha Atribui senha.
	 */
	public void setSenha(String senha) {
		this.senha = senha;
	}

	/**
	 * @return numeroProcesso.
	 */
	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	/**
	 * @param numeroProcesso Atribui numeroProcesso.
	 */
	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	/**
	 * @return processoTrf.
	 */
	public ProcessoTrf getProcessoTrf() {
		if (processoTrf == null) {
			processoTrf = new ProcessoTrf();
		}
		return processoTrf;
	}

	/**
	 * @param processoTrf Atribui processoTrf.
	 */
	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	/**
	 * @return dataEnvio.
	 */
	public Date getDataEnvio() {
		return dataEnvio;
	}

	/**
	 * @param dataEnvio Atribui dataEnvio.
	 */
	public void setDataEnvio(Date dataEnvio) {
		this.dataEnvio = dataEnvio;
	}

	/**
	 * @return parametros.
	 */
	public List<Properties> getParametros() {
		return parametros;
	}

	/**
	 * @param parametros Atribui parametros.
	 */
	public void setParametros(List<Properties> parametros) {
		this.parametros = parametros;
	}

	/**
	 * @return tamanhoProcesso.
	 */
	public Integer getTamanhoProcesso() {
		return tamanhoProcesso;
	}

	/**
	 * @param tamanho Atribui tamanho.
	 */
	public void setTamanhoProcesso(Integer tamanho) {
		this.tamanhoProcesso = tamanho;
	}

	/**
	 * @return mapaParametrosDocumento.
	 */
	public Map<Integer, Properties> getMapaParametrosDocumento() {
		return mapaParametrosDocumento;
	}

	/**
	 * @param mapaParametrosDocumento Atribui mapaParametrosDocumento.
	 */
	public void setMapaParametrosDocumento(Map<Integer, Properties> mapaParametrosDocumento) {
		this.mapaParametrosDocumento = mapaParametrosDocumento;
	}

	/**
	 * @return consultaPJeClient.
	 */
	public ConsultaPJeClient getConsultaPJeClient() {
		return consultaPJeClient;
	}

	/**
	 * @param consultaPJeClient Atribui consultaPJeClient.
	 */
	public void setConsultaPJeClient(ConsultaPJeClient consultaPJeClient) {
		this.consultaPJeClient = consultaPJeClient;
	}

	/**
	 * @return competenciaConflito.
	 */
	public Integer getCompetenciaConflito() {
		return competenciaConflito;
	}

	/**
	 * @param competenciaConflito Atribui competenciaConflito.
	 */
	public void setCompetenciaConflito(Integer competenciaConflito) {
		this.competenciaConflito = competenciaConflito;
	}

	/**
	 * @return isRequisicaoPJE.
	 */
	public Boolean getIsRequisicaoPJE() {
		return isRequisicaoPJE;
	}

	/**
	 * @param isRequisicaoPJE Atribui isRequisicaoPJE.
	 */
	public void setIsRequisicaoPJE(Boolean isRequisicaoPJE) {
		this.isRequisicaoPJE = isRequisicaoPJE;
	}
}
