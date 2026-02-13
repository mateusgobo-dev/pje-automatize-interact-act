package br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class LembreteDTO implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private Integer idLembrete;
	private Long idProcessoJudicial;
	private Long idProcessoDocumento;
	private String usuarioInclusao;
	private String descricao;
	private Date dataVisivelAte; 
	private Date dataInclusao;
	private Boolean ativo;
	private Integer idUsuarioInclusao;
	
	public LembreteDTO() {
		super();
	}
	
	public LembreteDTO(Integer idLembrete, 
			Long idProcessoJudicial,
			Long idProcessoDocumento, 
			String usuarioInclusao,
			String descricao, 
			Date dataVisivelAte, 
			Date dataInclusao, 
			Boolean ativo, 
			Integer idUsuarioInclusao) {
		super();
		this.idLembrete = idLembrete;
		this.idProcessoJudicial = idProcessoJudicial;
		this.idProcessoDocumento = idProcessoDocumento;
		this.usuarioInclusao = usuarioInclusao;
		this.descricao = descricao;
		this.dataVisivelAte = dataVisivelAte;
		this.dataInclusao = dataInclusao;
		this.ativo = ativo;
		this.idUsuarioInclusao = idUsuarioInclusao;
	}
	
	public LembreteDTO(Integer idLembrete, 
			Integer idProcessoJudicial, 
			Integer idProcessoDocumento, 
			String usuarioInclusao, 
			String descricao ,
			Date dataVisivelAte, 
			Date dataInclusao, 
			Boolean ativo, 
			Integer idUsuarioInclusao) {
		super();
		
		this.idLembrete = idLembrete;

		if(idProcessoJudicial != null) {
			this.idProcessoJudicial = new Long(idProcessoJudicial);			
		}
		if(idProcessoDocumento != null) {
			this.idProcessoDocumento = new Long(idProcessoDocumento);
		}
		this.usuarioInclusao = usuarioInclusao;
		this.descricao = descricao;
		this.dataVisivelAte = dataVisivelAte;
		this.dataInclusao = dataInclusao;
		this.ativo = ativo;
		this.idUsuarioInclusao = idUsuarioInclusao;		
	}	

	public Integer getIdLembrete() {
		return idLembrete;
	}

	public void setIdLembrete(Integer idLembrete) {
		this.idLembrete = idLembrete;
	}

	public Long getIdProcessoJudicial() {
		return idProcessoJudicial;
	}
	
	public void setIdProcessoJudicial(Long idProcessoJudicial) {
		this.idProcessoJudicial = idProcessoJudicial;
	}
	
	public Long getIdProcessoDocumento() {
		return idProcessoDocumento;
	}
	
	public void setIdProcessoDocumento(Long idProcessoDocumento) {
		this.idProcessoDocumento = idProcessoDocumento;
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
		String retorno = "";
		if (this.dataVisivelAte!=null){
			DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
			retorno = dateFormat.format(this.dataVisivelAte);
		}
		return retorno;
	}

	public Integer getIdUsuarioInclusao() {
		return idUsuarioInclusao;
	}

	public void setIdUsuarioInclusao(Integer idUsuarioInclusao) {
		this.idUsuarioInclusao = idUsuarioInclusao;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idLembrete == null) ? 0 : idLembrete.hashCode());
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
		LembreteDTO other = (LembreteDTO) obj;
		if (idLembrete == null) {
			if (other.idLembrete != null)
				return false;
		} else if (!idLembrete.equals(other.idLembrete))
			return false;
		return true;
	}

}