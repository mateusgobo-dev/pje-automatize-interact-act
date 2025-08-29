package br.jus.pje.nucleo.entidades;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_documento_certidao")
@org.hibernate.annotations.GenericGenerator(name = "gen_documento_certidao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_documento_certidao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class DocumentoCertidao implements java.io.Serializable{
	
	private static final long serialVersionUID = 5607827904567711771L;

	@Id
	@GeneratedValue(generator = "gen_documento_certidao")
	@Column(name = "id_documento_certidao", unique = true, nullable = false)
	private Long idDocumentoCertidao;
	
	@OneToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}, optional = false)
	@JoinColumn(name = "id_documento", nullable = false)
	@NotNull
	private Documento documento;
	
	@OneToOne
	@JoinColumn(name = "id_processo_documento", nullable = false)
	@NotNull
	private ProcessoDocumento processoDocumento;

	public Long getIdDocumentoCertidao() {
		return idDocumentoCertidao;
	}

	public void setIdDocumentoCertidao(Long idDocumentoCertidao) {
		this.idDocumentoCertidao = idDocumentoCertidao;
	}

	public Documento getDocumento() {
		return documento;
	}

	public void setDocumento(Documento documento) {
		this.documento = documento;
	}

	public ProcessoDocumento getProcessoDocumento() {
		return processoDocumento;
	}

	public void setProcessoDocumento(ProcessoDocumento processoDocumento) {
		this.processoDocumento = processoDocumento;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idDocumentoCertidao == null) ? 0 : idDocumentoCertidao.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		DocumentoCertidao other = (DocumentoCertidao) obj;
		if (idDocumentoCertidao == null) {
			if (other.idDocumentoCertidao != null) {
				return false;
			}
		} else if (!idDocumentoCertidao.equals(other.idDocumentoCertidao)) {
			return false;
		}
		return true;
	}

}
