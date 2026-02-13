package br.jus.cnj.pje.entidades.vo;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import br.jus.pje.nucleo.entidades.LembretePermissao;

import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

/**
 * Classe de suporte para notas 
 * @author rafaelmatos
 */
public class LembreteVO implements Serializable{
	
	private static final long serialVersionUID = 4690480548847960760L;
	private Integer idLembrete;
	private ProcessoTrf processoTrf;
	private ProcessoDocumento processoDocumento;
	private String usuarioInclusao;
	private List<LembretePermissao> lembretePermissoes = new ArrayList<>();

	private String descricao;
	private Date dataVisivelAte; 
	private Date dataInclusao;
	private Boolean ativo;
	private Integer idUsuarioInclusao;
	
	public LembreteVO() {
		super();
	}
	
	public LembreteVO(Integer idLembrete, ProcessoTrf processoTrf,
			ProcessoDocumento processoDocumento, String usuarioInclusao,
			String descricao, Date dataVisivelAte, Date dataInclusao,
			Boolean ativo, Integer idUsuarioInclusao) {
		super();
		this.idLembrete = idLembrete;
		this.processoTrf = processoTrf;
		this.processoDocumento = processoDocumento;
		this.usuarioInclusao = usuarioInclusao;
		this.descricao = descricao;
		this.dataVisivelAte = dataVisivelAte;
		this.dataInclusao = dataInclusao;
		this.ativo = ativo;
		this.idUsuarioInclusao = idUsuarioInclusao;
	}
	
	public LembreteVO(Integer idLembrete, ProcessoTrf processoTrf,
			ProcessoDocumento processoDocumento, String usuarioInclusao,
			String descricao, Date dataVisivelAte, Date dataInclusao, 
			Boolean ativo, Integer idUsuarioInclusao, LembretePermissao lembretePermissoes) {
		super();
		this.idLembrete = idLembrete;
		this.processoTrf = processoTrf;
		this.processoDocumento = processoDocumento;
		this.usuarioInclusao = usuarioInclusao;
		this.descricao = descricao;
		this.dataVisivelAte = dataVisivelAte;
		this.dataInclusao = dataInclusao;
		this.ativo = ativo;
		this.idUsuarioInclusao = idUsuarioInclusao;
		this.lembretePermissoes.add(lembretePermissoes);
	}

	public Integer getIdLembrete() {
		return idLembrete;
	}

	public void setIdLembrete(Integer idLembrete) {
		this.idLembrete = idLembrete;
	}

	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	public ProcessoDocumento getProcessoDocumento() {
		return processoDocumento;
	}

	public void setProcessoDocumento(ProcessoDocumento processoDocumento) {
		this.processoDocumento = processoDocumento;
	}

	public String getUsuarioInclusao() {
		return usuarioInclusao;
	}

	public void setUsuarioInclusao(String usuarioInclusao) {
		this.usuarioInclusao = usuarioInclusao;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public Date getDataVisivelAte() {
		return dataVisivelAte;
	}

	public void setDataVisivelAte(Date dataVisivelAte) {
		this.dataVisivelAte = dataVisivelAte;
	}

	public Date getDataInclusao() {
		return dataInclusao;
	}

	public void setDataInclusao(Date dataInclusao) {
		this.dataInclusao = dataInclusao;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	public String getDataVisivelAteFormatado(){
		if (this.dataVisivelAte!=null){
			DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
			return dateFormat.format(this.dataVisivelAte);
		}
		return "";
	}

	public Integer getIdUsuarioInclusao() {
		return idUsuarioInclusao;
	}

	public void setIdUsuarioInclusao(Integer idUsuarioInclusao) {
		this.idUsuarioInclusao = idUsuarioInclusao;
	}
	
	public String getDataInclusaoFormatado(){
		if (this.dataInclusao!=null){
			DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
			return dateFormat.format(this.dataInclusao);
		}
		return "";
	}
	
	public List<LembretePermissao> getLembretePermissoes() {
		return lembretePermissoes;
	}

	public void setLembretePermissoes(List<LembretePermissao> lembretePermissoes) {
		this.lembretePermissoes = lembretePermissoes;
	}
}
