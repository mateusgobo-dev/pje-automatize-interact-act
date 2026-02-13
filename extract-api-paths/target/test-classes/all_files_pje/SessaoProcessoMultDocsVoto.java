package br.jus.pje.nucleo.entidades;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = SessaoProcessoMultDocsVoto.TABLE_NAME)
public class SessaoProcessoMultDocsVoto implements Comparable<SessaoProcessoMultDocsVoto>, br.jus.pje.nucleo.entidades.IEntidade<SessaoProcessoMultDocsVoto,Integer>, java.io.Serializable {

	public static final String TABLE_NAME = "tb_sessao_proc_mult_docs_voto";

	private static final long serialVersionUID = 1L;

	private int id;
	private SessaoProcessoDocumentoVoto sessaoProcessoDocumentoVoto;
	private ProcessoDocumento processoDocumento;
	private Integer ordemDocumento;

	@org.hibernate.annotations.GenericGenerator(name = "gen_sessao_processo_mult_docs_voto", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_sessao_proc_mult_docs_voto"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
	@Id
	@GeneratedValue(generator = "gen_sessao_processo_mult_docs_voto")
	@Column(name = "id_sessao_proc_mult_docs_voto", unique = true)
	@NotNull
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_sessao_proc_documento_voto")
	@NotNull
	public SessaoProcessoDocumentoVoto getSessaoProcessoDocumentoVoto() {
		return sessaoProcessoDocumentoVoto;
	}
	
	public void setSessaoProcessoDocumentoVoto(SessaoProcessoDocumentoVoto sessaoProcessoDocumentoVoto) {
		this.sessaoProcessoDocumentoVoto = sessaoProcessoDocumentoVoto;
	}

	@OneToOne(fetch = FetchType.EAGER, orphanRemoval = false)
	@JoinColumn(name = "id_processo_documento")
	@NotNull
	public ProcessoDocumento getProcessoDocumento() {
		return processoDocumento;
	}

	public void setProcessoDocumento(ProcessoDocumento processoDocumento) {
		this.processoDocumento = processoDocumento;
	}

	@Column(name = "ordem_doc")
	@NotNull
	public Integer getOrdemDocumento() {
		return ordemDocumento;
	}

	public void setOrdemDocumento(Integer ordemDocumento) {
		this.ordemDocumento = ordemDocumento;
	}

	@Override
	public int compareTo(SessaoProcessoMultDocsVoto o) {
		return o.getProcessoDocumento().getDataInclusao().compareTo(this.getProcessoDocumento().getDataInclusao());
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends SessaoProcessoMultDocsVoto> getEntityClass() {
		return SessaoProcessoMultDocsVoto.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getId());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
