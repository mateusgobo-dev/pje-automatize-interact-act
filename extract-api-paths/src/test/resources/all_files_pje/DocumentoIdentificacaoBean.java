package br.jus.pje.nucleo.beans.criminal;

import java.util.Date;

public class DocumentoIdentificacaoBean extends BaseBean{

	private static final long serialVersionUID = 1L;

	private String dsDocumento;
	private String nrDocumento;
	private String nomePessoa;
	private String[] filiacoes;
	private String orgaoExpedidor;
	private Date dtExpedicao;
	private Date dtNascimento;
	private Boolean usadoFalsamente;

	public DocumentoIdentificacaoBean() {
		super();
	}

	public DocumentoIdentificacaoBean(String id, String dsDocumento, String nrDocumento, String nomePessoa, String[] filiacoes,
			String orgaoExpedidor, Date dtExpedicao, Date dtNascimento, Boolean usadoFalsamente) {
		super(id);
		this.dsDocumento = dsDocumento;
		this.nrDocumento = nrDocumento;
		this.nomePessoa = nomePessoa;
		this.filiacoes = filiacoes;
		this.orgaoExpedidor = orgaoExpedidor;
		this.dtExpedicao = dtExpedicao;
		this.dtNascimento = dtNascimento;
		this.usadoFalsamente = usadoFalsamente;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public String getDsDocumento() {
		return dsDocumento;
	}

	public void setDsDocumento(String dsDocumento) {
		this.dsDocumento = dsDocumento;
	}

	public String getNrDocumento() {
		return nrDocumento;
	}

	public void setNrDocumento(String nrDocumento) {
		this.nrDocumento = nrDocumento;
	}

	public String getNomePessoa() {
		return nomePessoa;
	}

	public void setNomePessoa(String nomePessoa) {
		this.nomePessoa = nomePessoa;
	}

	public String[] getFiliacoes() {
		return filiacoes;
	}

	public void setFiliacoes(String[] filiacoes) {
		this.filiacoes = filiacoes;
	}

	public String getOrgaoExpedidor() {
		return orgaoExpedidor;
	}

	public void setOrgaoExpedidor(String orgaoExpedidor) {
		this.orgaoExpedidor = orgaoExpedidor;
	}

	public Date getDtExpedicao() {
		return dtExpedicao;
	}

	public void setDtExpedicao(Date dtExpedicao) {
		this.dtExpedicao = dtExpedicao;
	}

	public Date getDtNascimento() {
		return dtNascimento;
	}

	public void setDtNascimento(Date dtNascimento) {
		this.dtNascimento = dtNascimento;
	}

	public Boolean getUsadoFalsamente() {
		return usadoFalsamente;
	}

	public void setUsadoFalsamente(Boolean usadoFalsamente) {
		this.usadoFalsamente = usadoFalsamente;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		DocumentoIdentificacaoBean other = (DocumentoIdentificacaoBean) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
