package br.jus.cnj.pje.entidades.vo;

import java.util.Date;

import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;

/**
 * @author Rafael Matos
 * @link https://www.cnj.jus.br/jira/browse/PJEII-20513
 * @see Componente de controle do frame WEB-INF/xhtml/flx/ordenarDocumentosVotoProcessoSessao.xhtml.
 * @since 27/04/2015
 */
public class OrdenarDocumentosVotoProcessoSessaoVO  implements java.io.Serializable, Comparable<OrdenarDocumentosVotoProcessoSessaoVO> {

	private static final long serialVersionUID = -8520015370009709324L;
	private Integer id;
	private Integer idSessaoProcessoDocumento;
	private String tipoVoto;
	private String votoOrigem;
	private String orgaoJulgador;
	private Date dtVoto;
	private Integer ordemDocumento;
	private ProcessoDocumentoBin processoDocumentoBin;
	
	public OrdenarDocumentosVotoProcessoSessaoVO(Integer id,
			Integer idSessaoProcessoDocumento, String tipoVoto,
			String votoOrigem, String orgaoJulgador, Date dtVoto,
			Integer ordemDocumento, ProcessoDocumentoBin processoDocumentoBin) {
		super();
		this.id = id;
		this.idSessaoProcessoDocumento = idSessaoProcessoDocumento;
		this.tipoVoto = tipoVoto;
		this.votoOrigem = votoOrigem;
		this.orgaoJulgador = orgaoJulgador;
		this.dtVoto = dtVoto;
		this.ordemDocumento = ordemDocumento;
		this.processoDocumentoBin = processoDocumentoBin;
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getIdSessaoProcessoDocumento() {
		return idSessaoProcessoDocumento;
	}

	public void setIdSessaoProcessoDocumento(Integer idSessaoProcessoDocumento) {
		this.idSessaoProcessoDocumento = idSessaoProcessoDocumento;
	}

	public String getTipoVoto() {
		return tipoVoto;
	}

	public void setTipoVoto(String tipoVoto) {
		this.tipoVoto = tipoVoto;
	}

	public String getVotoOrigem() {
		return votoOrigem;
	}

	public void setVotoOrigem(String votoOrigem) {
		this.votoOrigem = votoOrigem;
	}

	public String getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(String orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public Date getDtVoto() {
		return dtVoto;
	}

	public void setDtVoto(Date dtVoto) {
		this.dtVoto = dtVoto;
	}

	public Integer getOrdemDocumento() {
		return ordemDocumento;
	}

	public void setOrdemDocumento(Integer ordemDocumento) {
		this.ordemDocumento = ordemDocumento;
	}

	public ProcessoDocumentoBin getProcessoDocumentoBin() {
		return processoDocumentoBin;
	}

	public void setProcessoDocumentoBin(ProcessoDocumentoBin processoDocumentoBin) {
		this.processoDocumentoBin = processoDocumentoBin;
	}

	@Override
	public int compareTo(OrdenarDocumentosVotoProcessoSessaoVO o) {
		if (this.ordemDocumento < o.ordemDocumento) {
	      return -1;
	    }

	    if (this.ordemDocumento > o.ordemDocumento) {
	      return 1;
	    }

	    return 0;
	}

}
