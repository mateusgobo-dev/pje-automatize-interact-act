package br.com.infox.pje.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;

/**
 * Bean para exibição da listagem do relatório de permissão para processos com
 * Segredo de Justiça
 * 
 * @author thiago
 */
public class EstatisticaPermissaoSegredoJusticaBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4455775452684292074L;
	private String vara;
	private List<PermissaoSegredoJusticaListBean> segredoJusticaListBean = new ArrayList<PermissaoSegredoJusticaListBean>();
	private List<UsuarioLogin> usuarios = new ArrayList<UsuarioLogin>();
	private Integer totalProcessosVara;
	private Integer totalUsuarios = 0;

	public String getVara() {
		return vara;
	}

	public void setVara(String vara) {
		this.vara = vara;
	}

	public List<PermissaoSegredoJusticaListBean> getSegredoJusticaListBean() {
		return segredoJusticaListBean;
	}

	public void setSegredoJusticaListBean(List<PermissaoSegredoJusticaListBean> segredoJusticaListBean) {
		this.segredoJusticaListBean = segredoJusticaListBean;
	}

	public List<UsuarioLogin> getUsuarios() {
		return usuarios;
	}

	public void setUsuarios(List<UsuarioLogin> usuarios) {
		this.usuarios = usuarios;
	}

	public Integer getTotalProcessosVara() {
		return totalProcessosVara;
	}

	public void setTotalProcessosVara(Integer totalProcessosVara) {
		this.totalProcessosVara = totalProcessosVara;
	}

	public Integer getTotalUsuarios() {
		return totalUsuarios;
	}

	public void setTotalUsuarios(Integer totalUsuarios) {
		this.totalUsuarios = totalUsuarios;
	}

}