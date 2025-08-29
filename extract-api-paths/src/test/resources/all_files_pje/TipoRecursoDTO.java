package br.jus.pje.nucleo.dto;

public class TipoRecursoDTO extends PJeServiceApiDTO {

	private static final long serialVersionUID = 1L;

	private Integer id;
	private String codigo;
	private String tipoRecurso;
	private Boolean ativo;
	private String codigoTribunal;

	public TipoRecursoDTO() {
		super();
	}

	public TipoRecursoDTO(String codigo, String tipoRecurso, Boolean ativo) {
		super();
		this.codigo = codigo;
		this.tipoRecurso = tipoRecurso;
		this.ativo = ativo;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getTipoRecurso() {
		return tipoRecurso;
	}

	public void setTipoRecurso(String tipoRecurso) {
		this.tipoRecurso = tipoRecurso;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	public String getCodigoTribunal() {
		return codigoTribunal;
	}

	public void setCodigoTribunal(String codigoTribunal) {
		this.codigoTribunal = codigoTribunal;
	}
	
	

}
