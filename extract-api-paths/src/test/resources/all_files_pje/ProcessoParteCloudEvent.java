package br.jus.cnj.pje.amqp.model.dto;

import br.jus.cnj.pje.pjecommons.model.amqp.CloudEventPayload;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;

public class ProcessoParteCloudEvent implements CloudEventPayload<ProcessoParteCloudEvent, ProcessoParte>{

	private static final long serialVersionUID = 1L;

	private String numeroProcesso;
	private Long idProcessoPje;
	private Long idProcessoPartePje;
	private Long idPessoaPje;
	private String rji;
	private ProcessoParteSituacaoEnum situacaoParte = ProcessoParteSituacaoEnum.A;

	public ProcessoParteCloudEvent(String numeroProcesso, Long idProcessoPje, Long idProcessoPartePje, Long idPessoaPje, String rji,
			ProcessoParteSituacaoEnum situacaoParte) {
		super();
		this.numeroProcesso = numeroProcesso;
		this.idProcessoPje = idProcessoPje;
		this.idProcessoPartePje = idProcessoPartePje;
		this.idPessoaPje = idPessoaPje;
		this.rji = rji;
		this.situacaoParte = situacaoParte;
	}
	
	public ProcessoParteCloudEvent() {
		super();
	}
	
	public String getNumeroProcesso() {
		return numeroProcesso;
	}
	
	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public Long getIdProcessoPje() {
		return idProcessoPje;
	}

	public void setIdProcessoPje(Long idProcessoPje) {
		this.idProcessoPje = idProcessoPje;
	}

	public Long getIdProcessoPartePje() {
		return idProcessoPartePje;
	}

	public void setIdProcessoPartePje(Long idProcessoPartePje) {
		this.idProcessoPartePje = idProcessoPartePje;
	}

	public Long getIdPessoaPje() {
		return idPessoaPje;
	}

	public void setIdPessoaPje(Long idPessoaPje) {
		this.idPessoaPje = idPessoaPje;
	}
	
	public String getRji() {
		return rji;
	}

	public void setRji(String rji) {
		this.rji = rji;
	}

	public ProcessoParteSituacaoEnum getSituacaoParte() {
		return situacaoParte;
	}

	public void setSituacaoParte(ProcessoParteSituacaoEnum situacaoParte) {
		this.situacaoParte = situacaoParte;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idPessoaPje == null) ? 0 : idPessoaPje.hashCode());
		result = prime * result + ((idProcessoPartePje == null) ? 0 : idProcessoPartePje.hashCode());
		result = prime * result + ((idProcessoPje == null) ? 0 : idProcessoPje.hashCode());
		result = prime * result + ((numeroProcesso == null) ? 0 : numeroProcesso.hashCode());
		result = prime * result + ((rji == null) ? 0 : rji.hashCode());
		result = prime * result + ((situacaoParte == null) ? 0 : situacaoParte.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
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
		ProcessoParteCloudEvent other = (ProcessoParteCloudEvent) obj;
		if (idPessoaPje == null) {
			if (other.idPessoaPje != null) {
				return false;
			}
		} else if (!idPessoaPje.equals(other.idPessoaPje)) {
			return false;
		}
		if (idProcessoPartePje == null) {
			if (other.idProcessoPartePje != null) {
				return false;
			}
		} else if (!idProcessoPartePje.equals(other.idProcessoPartePje)) {
			return false;
		}
		if (idProcessoPje == null) {
			if (other.idProcessoPje != null) {
				return false;
			}
		} else if (!idProcessoPje.equals(other.idProcessoPje)) {
			return false;
		}
		if (numeroProcesso == null) {
			if (other.numeroProcesso != null) {
				return false;
			}
		} else if (!numeroProcesso.equals(other.numeroProcesso)) {
			return false;
		}
		if (rji == null) {
			if (other.rji != null) {
				return false;
			}
		} else if (!rji.equals(other.rji)) {
			return false;
		}
		if (situacaoParte != other.situacaoParte) {
			return false;
		}
		return true;
	}

	@Override
	public ProcessoParteCloudEvent convertEntityToPayload(ProcessoParte entity) {
		ProcessoParteCloudEvent dto = new ProcessoParteCloudEvent(entity.getProcessoTrf().getNumeroProcesso(),
				new Long(entity.getProcessoTrf().getIdProcessoTrf()), 
				new Long(entity.getIdProcessoParte()), 
				new Long(entity.getPessoa().getIdPessoa()), 
				null, 
				entity.getInSituacao());
		
		return dto;
	}

	@Override
	public Long getId(ProcessoParte entity) {
		return (entity != null ? Long.valueOf(entity.getIdProcessoParte()) :  null);
	}

}
