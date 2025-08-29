package br.jus.pdpj.notificacao.service;

import java.io.Serializable;

import br.jus.cnj.pje.pjecommons.model.services.bnmp.AssinaturaMagistradoDTO;
import br.jus.cnj.pje.pjecommons.model.services.bnmp.SigiloDTO;
import br.jus.cnj.pje.pjecommons.model.services.bnmp.StatusDTO;
import br.jus.cnj.pje.pjecommons.model.services.bnmp.TipoDTO;

public class DocumentoBNMPMinDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private TipoDTO tipo = null;
	private String parametroTipo = "";
	private SigiloDTO sigilo = null;
	private String numero = ""; 
	private String urlPdf = "";
	private int idPeca;
	private AssinaturaMagistradoDTO magistrado = null;
	private String nomeModeloEvento;
	private StatusDTO statusPeca;


	public TipoDTO getTipo() {
		return tipo;
	}
	public void setTipo(TipoDTO tipo) {
		this.tipo = tipo;
	}
	public String getParametroTipo() {
		return parametroTipo;
	}
	public void setParametroTipo(String parametroTipo) {
		this.parametroTipo = parametroTipo;
	}
	public SigiloDTO getSigilo() {
		return sigilo;
	}
	public void setSigilo(SigiloDTO sigilo) {
		this.sigilo = sigilo;
	}
	public String getNumero() {
		return numero;
	}
	public void setNumero(String numeroNotificacao) {
		this.numero = numeroNotificacao;
	}
	public String getUrlPdf() {
		return urlPdf;
	}
	public void setUrlPdf(String urlPdf) {
		this.urlPdf = urlPdf;
	}
	public int getIdPeca() {
		return idPeca;
	}
	public void setIdPeca(int idPeca) {
		this.idPeca = idPeca;
	}
	public AssinaturaMagistradoDTO getMagistrado() {
		return magistrado;
	}
	public void setMagistrado(AssinaturaMagistradoDTO magistrado) {
		this.magistrado = magistrado;
	}
	public String getNomeModeloEvento() {
		return nomeModeloEvento;
	}
	public void setNomeModeloEvento(String nomeModeloEvento) {
		this.nomeModeloEvento = nomeModeloEvento;
	}
	public StatusDTO getStatusPeca() {
		return statusPeca;
	}
	public void setStatusPeca(StatusDTO statusPeca) {
		this.statusPeca = statusPeca;
	}
	
}
