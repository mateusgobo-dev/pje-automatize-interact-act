/*
 IBPM - Ferramenta de produtividade Java
 Copyright (c) 1986-2009 Infox Tecnologia da Informação Ltda.

 Este programa é software livre; você pode redistribuí-lo e/ou modificá-lo 
 sob os termos da GNU GENERAL PUBLIC LICENSE (GPL) conforme publicada pela 
 Free Software Foundation; versão 2 da Licença.
 Este programa é distribuído na expectativa de que seja útil, porém, SEM 
 NENHUMA GARANTIA; nem mesmo a garantia implícita de COMERCIABILIDADE OU 
 ADEQUAÇÃO A UMA FINALIDADE ESPECÍFICA.
 
 Consulte a GNU GPL para mais detalhes.
 Você deve ter recebido uma cópia da GNU GPL junto com este programa; se não, 
 veja em http://www.gnu.org/licenses/   
 */
package br.com.infox.ibpm.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.jus.pje.nucleo.enums.TipoOperacaoLogEnum;
import br.jus.pje.nucleo.util.DateUtil;

public class ConsultaEntidadeLog {
	private String ip;
	private String idEntidade;
	private String nomeEntidade;
	private Date dataInicio;
	private Date dataFim = new Date();
	private Integer idUsuario;
 	private String nomeUsuario;
	private TipoOperacaoLogEnum tipoOperacaoLogEnum = null;
	private Boolean inPesquisa = false;
	private List<Integer> idsUsuarios = new ArrayList<Integer>(0);

	public ConsultaEntidadeLog() {
		dataFim = new Date();
		dataInicio = DateUtil.getBeginningOfDay(dataFim);
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public void setIdEntidade(String idEntidade) {
		this.idEntidade = idEntidade;
	}

	public String getIdEntidade() {
		return idEntidade;
	}

	public String getNomeEntidade() {
		return nomeEntidade;
	}

	public void setNomeEntidade(String nomeEntidade) {
		this.nomeEntidade = nomeEntidade;
	}

	public Integer getIdUsuario() {
		return idUsuario;
	}
	
	public String getNomeUsuario() {
	 	return nomeUsuario;
	 	}
	 	
	public void setNomeUsuario(String nomeUsuario) {
	 	this.nomeUsuario = nomeUsuario;
	 	}
	 		
	 public List<Integer> getIdsUsuarios() {
	 	return idsUsuarios;
	 	}
	 	
	 public void setIdsUsuarios(List<Integer> idsUsuarios) {
	 	this.idsUsuarios = idsUsuarios;
	 	}

	public void setIdUsuario(Integer idUsuario) {
		this.idUsuario = idUsuario;
	}

	public void setTipoOperacaoLogEnum(TipoOperacaoLogEnum tipoOperacaoLogEnum) {
		this.tipoOperacaoLogEnum = tipoOperacaoLogEnum;
	}

	public TipoOperacaoLogEnum getTipoOperacaoLogEnum() {
		return tipoOperacaoLogEnum;
	}

	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	public Date getDataFim() {
		return dataFim;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = br.jus.pje.nucleo.util.DateUtil.getEndOfDay(dataFim);
	}

	public Boolean getInPesquisa() {
		return inPesquisa;
	}

	public void setInPesquisa(Boolean inPesquisa) {
		this.inPesquisa = inPesquisa;
		idsUsuarios.clear();
	}

	@Override
	public String toString() {
		return nomeEntidade;
	}
}
