/**
 * 
 */
package br.jus.cnj.pje.entidades.vo;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Classe para organizar os atributos de consulta de documentos indexados.
 * 
 * @author thiago.figueiredo@tse.jus.br
 *
 */
public class ConsultaDocumentoIndexadoVO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 882126681200216224L;
	
	private SimpleDateFormat simpleDateFormat;
	
	private List<Integer> idsTipoDocumento;
	
	private String conteudo;
	
	private String nomeUsuarioCriacao;
	
	private String nomeUsuarioSignatario;
	
	private Date dataCriacaoInicio;
	
	private Date dataCriacaoFim;
	
	private Date dataAssinaturaInicio;
	
	private Date dataAssinaturaFim;
	
	private String orgaoJulgador;
	
	private String localizacaoUsuario;
	
	private String tipoDocumento;
	
	private String nrProcesso;
	
	public ConsultaDocumentoIndexadoVO(){
		this.simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
	}
	
	public ConsultaDocumentoIndexadoVO(JSONObject filtros) throws JSONException, ParseException{
		this();
		this.tipoDocumento = filtros.getString("tipoDocumento");
		this.nomeUsuarioCriacao = filtros.getString("usuarioCriacao");
		this.dataCriacaoInicio= 
				filtros.getString("dataCriacaoMin").isEmpty()?null:simpleDateFormat.parse(filtros.getString("dataCriacaoMin"));
		this.dataCriacaoFim = 
				filtros.getString("dataCriacaoMax").isEmpty()?null:simpleDateFormat.parse(filtros.getString("dataCriacaoMax"));
		this.nomeUsuarioSignatario = filtros.getString("usuarioAssinatura");
		this.dataAssinaturaInicio= 
				filtros.getString("dataAssinaturaMin").isEmpty()?null:simpleDateFormat.parse(filtros.getString("dataAssinaturaMin"));
		this.dataAssinaturaFim = 
				filtros.getString("dataAssinaturaMax").isEmpty()?null:simpleDateFormat.parse(filtros.getString("dataAssinaturaMax"));
		this.orgaoJulgador = filtros.getString("orgaoJulgador");
		this.conteudo = filtros.getString("conteudo");
	}

	/**
	 * @return the idsTipoDocumento
	 */
	public List<Integer> getIdsTipoDocumento() {
		if(idsTipoDocumento == null){
			idsTipoDocumento = new ArrayList<Integer>();
		}
		return idsTipoDocumento;
	}

	/**
	 * @param idsTipoDocumento the idsTipoDocumento to set
	 */
	public void setIdsTipoDocumento(List<Integer> idsTipoDocumento) {
		this.idsTipoDocumento = idsTipoDocumento;
	}

	/**
	 * @return the conteudo
	 */
	public String getConteudo() {
		return conteudo;
	}

	/**
	 * @param conteudo the conteudo to set
	 */
	public void setConteudo(String conteudo) {
		this.conteudo = conteudo;
	}

	/**
	 * @return the nomeUsuarioCriacao
	 */
	public String getNomeUsuarioCriacao() {
		return nomeUsuarioCriacao;
	}

	/**
	 * @param nomeUsuarioCriacao the nomeUsuarioCriacao to set
	 */
	public void setNomeUsuarioCriacao(String nomeUsuarioCriacao) {
		this.nomeUsuarioCriacao = nomeUsuarioCriacao;
	}

	/**
	 * @return the nomeUsuarioSignatario
	 */
	public String getNomeUsuarioSignatario() {
		return nomeUsuarioSignatario;
	}

	/**
	 * @param nomeUsuarioSignatario the nomeUsuarioSignatario to set
	 */
	public void setNomeUsuarioSignatario(String nomeUsuarioSignatario) {
		this.nomeUsuarioSignatario = nomeUsuarioSignatario;
	}

	/**
	 * @return the dataCriacaoInicio
	 */
	public Date getDataCriacaoInicio() {
		return dataCriacaoInicio;
	}

	/**
	 * @param dataCriacaoInicio the dataCriacaoInicio to set
	 */
	public void setDataCriacaoInicio(Date dataCriacaoInicio) {
		this.dataCriacaoInicio = dataCriacaoInicio;
	}

	/**
	 * @return the dataCriacaoFim
	 */
	public Date getDataCriacaoFim() {
		return dataCriacaoFim;
	}

	/**
	 * @param dataCriacaoFim the dataCriacaoFim to set
	 */
	public void setDataCriacaoFim(Date dataCriacaoFim) {
		this.dataCriacaoFim = dataCriacaoFim;
	}

	/**
	 * @return the dataAssinaturaInicio
	 */
	public Date getDataAssinaturaInicio() {
		return dataAssinaturaInicio;
	}

	/**
	 * @param dataAssinaturaInicio the dataAssinaturaInicio to set
	 */
	public void setDataAssinaturaInicio(Date dataAssinaturaInicio) {
		this.dataAssinaturaInicio = dataAssinaturaInicio;
	}

	/**
	 * @return the dataAssinaturaFim
	 */
	public Date getDataAssinaturaFim() {
		return dataAssinaturaFim;
	}

	/**
	 * @param dataAssinaturaFim the dataAssinaturaFim to set
	 */
	public void setDataAssinaturaFim(Date dataAssinaturaFim) {
		this.dataAssinaturaFim = dataAssinaturaFim;
	}

	/**
	 * @return the orgaoJulgador
	 */
	public String getOrgaoJulgador() {
		return orgaoJulgador;
	}

	/**
	 * @param orgaoJulgador the orgaoJulgador to set
	 */
	public void setOrgaoJulgador(String orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	/**
	 * @return the localizacaoUsuario
	 */
	public String getLocalizacaoUsuario() {
		return localizacaoUsuario;
	}

	/**
	 * @param localizacaoUsuario the localizacaoUsuario to set
	 */
	public void setLocalizacaoUsuario(String localizacaoUsuario) {
		this.localizacaoUsuario = localizacaoUsuario;
	}

	/**
	 * @return the tipoDocumento
	 */
	public String getTipoDocumento() {
		return tipoDocumento;
	}

	/**
	 * @param tipoDocumento the tipoDocumento to set
	 */
	public void setTipoDocumento(String tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}

	/**
	 * @return the nrProcesso
	 */
	public String getNrProcesso() {
		return nrProcesso;
	}

	/**
	 * @param nrProcesso the nrProcesso to set
	 */
	public void setNrProcesso(String nrProcesso) {
		this.nrProcesso = nrProcesso;
	}
	
}