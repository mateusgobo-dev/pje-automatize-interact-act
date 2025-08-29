package br.jus.pje.nucleo.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import br.jus.pje.nucleo.beans.criminal.TipoProcessoEnum;



@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessoCriminalDTO implements Serializable {

	private static final long serialVersionUID = 6300675598480327305L;
	
	private Integer id;
	private String nrProcesso;
	private Date dtLocalFato;
	private String dsLocalFato;
	private String dsLongitude;
	private String dsLatitude;
	private String cep;
	private MunicipioDTO municipio = new MunicipioDTO();
	private String nmBairro;
	private String nmLogradouro;
	private String nmNumero;
	private String complemento;
	private String pjeOrigem;
	private ClasseJudicialCriminalDTO classeJudicial;
	private TipoProcessoEnum tipoProcesso;

	private List<ProcessoProcedimentoOrigemDTO> processoProcedimentoOrigemList = new ArrayList<>();

	public ProcessoCriminalDTO() {
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNrProcesso() {
		return nrProcesso;
	}

	public void setNrProcesso(String nrProcesso) {
		this.nrProcesso = nrProcesso;
	}

	public Date getDtLocalFato() {
		return dtLocalFato;
	}

	public void setDtLocalFato(Date dtLocalFato) {
		this.dtLocalFato = dtLocalFato;
	}

	public String getDsLocalFato() {
		return dsLocalFato;
	}

	public void setDsLocalFato(String dsLocalFato) {
		this.dsLocalFato = dsLocalFato;
	}

	public String getDsLongitude() {
		return dsLongitude;
	}

	public void setDsLongitude(String dsLongitude) {
		this.dsLongitude = dsLongitude;
	}

	public String getDsLatitude() {
		return dsLatitude;
	}

	public void setDsLatitude(String dsLatitude) {
		this.dsLatitude = dsLatitude;
	}

	public List<ProcessoProcedimentoOrigemDTO> getProcessoProcedimentoOrigemList() {
		return processoProcedimentoOrigemList;
	}

	public void setProcessoProcedimentoOrigemList(List<ProcessoProcedimentoOrigemDTO> processoProcedimentoOrigemList) {
		this.processoProcedimentoOrigemList = processoProcedimentoOrigemList;
	}

	public String getCep() {
		return cep;
	}

	public void setCep(String cep) {
		this.cep = cep;
	}

	public MunicipioDTO getMunicipio() {
		return municipio;
	}

	public void setMunicipio(MunicipioDTO municipio) {
		this.municipio = municipio;
	}

	public String getNmBairro() {
		return nmBairro;
	}

	public void setNmBairro(String nmBairro) {
		this.nmBairro = nmBairro;
	}

	public String getNmLogradouro() {
		return nmLogradouro;
	}

	public void setNmLogradouro(String nmLogradouro) {
		this.nmLogradouro = nmLogradouro;
	}

	public String getNmNumero() {
		return nmNumero;
	}

	public void setNmNumero(String nmNumero) {
		this.nmNumero = nmNumero;
	}

	public String getComplemento() {
		return complemento;
	}

	public void setComplemento(String complemento) {
		this.complemento = complemento;
	}
	
	public String getPjeOrigem() {
		return pjeOrigem;
	}
	
	public void setPjeOrigem(String pjeOrigem) {
		this.pjeOrigem = pjeOrigem;
	}

	public TipoProcessoEnum getTipoProcesso() {
		return tipoProcesso;
	}

	public void setTipoProcesso(TipoProcessoEnum tipoProcesso) {
		this.tipoProcesso = tipoProcesso;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cep == null) ? 0 : cep.hashCode());
		result = prime * result + ((complemento == null) ? 0 : complemento.hashCode());
		result = prime * result + ((dsLatitude == null) ? 0 : dsLatitude.hashCode());
		result = prime * result + ((dsLocalFato == null) ? 0 : dsLocalFato.hashCode());
		result = prime * result + ((dsLongitude == null) ? 0 : dsLongitude.hashCode());
		result = prime * result + ((dtLocalFato == null) ? 0 : dtLocalFato.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((municipio == null) ? 0 : municipio.hashCode());
		result = prime * result + ((nmBairro == null) ? 0 : nmBairro.hashCode());
		result = prime * result + ((nmLogradouro == null) ? 0 : nmLogradouro.hashCode());
		result = prime * result + ((nmNumero == null) ? 0 : nmNumero.hashCode());
		result = prime * result + ((nrProcesso == null) ? 0 : nrProcesso.hashCode());
		result = prime * result + ((pjeOrigem == null) ? 0 : pjeOrigem.hashCode());
		result = prime * result
				+ ((processoProcedimentoOrigemList == null) ? 0 : processoProcedimentoOrigemList.hashCode());
		result = prime * result + ((tipoProcesso == null) ? 0 : tipoProcesso.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		else if (obj == null) {
			return false;
		}
		else if (getClass() != obj.getClass()) {
			return false;
		}
		ProcessoCriminalDTO other = (ProcessoCriminalDTO) obj;		
		
		if(!isLocalEquals(other)) {
			return false;
		}
		
		if(!isNrProcessoEquals(other)) {
			return false;
		}
		if(!isPjeOrigemEquals(other)) {
			return false;
		}
		if(!isProcessoProcedimentoOrigemListEquals(other)) {
			return false;
		}
		
		if (!isTipoProcessoEquals(other)) {
			return false;
		}
		if (processoProcedimentoOrigemList == null) {
			if (other.processoProcedimentoOrigemList != null) {
				return false;
			}
		} else if (!processoProcedimentoOrigemList.equals(other.processoProcedimentoOrigemList)) {
			return false;
		}
		
		return true;	
	
	}	
	
	private boolean isTipoProcessoEquals(ProcessoCriminalDTO other) {
		if (tipoProcesso == null) {
			if (other.tipoProcesso != null) {
				return false;
			}
		} else if (!tipoProcesso.equals(other.tipoProcesso)) {
			return false;
		}		
		return true;
	}

	private boolean isCepEquals(ProcessoCriminalDTO other) {		
		if (cep == null) {
			if (other.cep != null) {
				return false;
			}
		} else if (!cep.equals(other.cep)) {
			return false;
		}		
		return true;
	}
	
	private boolean isComplementoEquals(ProcessoCriminalDTO other) {		
		if (complemento == null) {
			if (other.complemento != null) {
				return false;
			}
		} else if (!complemento.equals(other.complemento)) {
			return false;
		}	
		return true;
	}
	private boolean isDsLatitudeEquals(ProcessoCriminalDTO other) {
		if (dsLatitude == null) {
			if (other.dsLatitude != null) {
				return false;
			}
		} else if (!dsLatitude.equals(other.dsLatitude)) {
			return false;
		}
		return true;
	}
	private boolean isDsLocalFatoEquals(ProcessoCriminalDTO other) {
		if (dsLocalFato == null) {
			if (other.dsLocalFato != null) {
				return false;
			}
		} else if (!dsLocalFato.equals(other.dsLocalFato)) {
			return false;
		}
		return true;
	}
	
	private boolean isDsLongitudeEquals(ProcessoCriminalDTO other) {
		if (dsLongitude == null) {
			if (other.dsLongitude != null) {
				return false;
			}
		} else if (!dsLongitude.equals(other.dsLongitude)) {
			return false;
		}
		return true;
	}
	
	private boolean isDtLocalFatoEquals(ProcessoCriminalDTO other) {	
		if (dtLocalFato == null) {
			if (other.dtLocalFato != null) {
				return false;
			}
		} else if (!dtLocalFato.equals(other.dtLocalFato)) {
			return false;
		}
		return true;
	}
	
	private boolean isMunicipioEquals(ProcessoCriminalDTO other) {		
		if (municipio == null) {
			if (other.municipio != null) {
				return false;
			}
		} else if (!municipio.equals(other.municipio)) {
			return false;
		}		
		return true;		
	}
	
	private boolean isNmBairroEquals(ProcessoCriminalDTO other) {	
		if (nmBairro == null) {
			if (other.nmBairro != null) {
				return false;
			}
		} else if (!nmBairro.equals(other.nmBairro)) {
			return false;
		}
		return true;
	}
	
	private boolean isNmLogradouroEquals(ProcessoCriminalDTO other) {			
		if (nmLogradouro == null) {
			if (other.nmLogradouro != null) {
				return false;
			}
		} else if (!nmLogradouro.equals(other.nmLogradouro)) {
			return false;
		}		
		return true;
	}
		

	private boolean isNrProcessoEquals(ProcessoCriminalDTO other) {		
		if (nrProcesso == null) {
			if (other.nrProcesso != null) {
				return false;
			}
		} else if (!nrProcesso.equals(other.nrProcesso)) {
			return false;
		}
		return true;
	}
	
	private boolean isPjeOrigemEquals(ProcessoCriminalDTO other) {	 
		if (pjeOrigem == null) {
			if (other.pjeOrigem != null) {
				return false;
			}
		} else if (!pjeOrigem.equals(other.pjeOrigem)) {
			return false;
		}
		return true;
	}
	
	private boolean isProcessoProcedimentoOrigemListEquals(ProcessoCriminalDTO other) {	
		if (processoProcedimentoOrigemList == null) {
			if (other.processoProcedimentoOrigemList != null) {
				return false;
			}
		} else if (!processoProcedimentoOrigemList.equals(other.processoProcedimentoOrigemList)) {
			return false;
		}
		return true;
	}
	
	private boolean isLocalEquals(ProcessoCriminalDTO other) {	
		
		boolean retorno = true;
		
		if(!isCepEquals(other)) {
			retorno = false;
		}	  
		if(!isComplementoEquals(other)) {
			retorno = false;
	    }
	    
		if(!isDsLatitudeEquals(other)) {
			retorno = false;
		}
		if(!isDsLocalFatoEquals(other)) {
			retorno = false;
		}
		
		if(!isDsLongitudeEquals(other)) {
			retorno = false;
		}		

		if(!isDtLocalFatoEquals(other)) {		
			retorno = false;
		}		
		
		if(!isMunicipioEquals(other)) {
			retorno = false;
		}
		
		if(!isNmBairroEquals(other)) {
			retorno = false;
		}
		
		if(!isNmLogradouroEquals(other)) {
			retorno = false;
		}
		if (nmNumero == null) {
			if (other.nmNumero != null) {
				retorno = false;
			}
		} else if (!nmNumero.equals(other.nmNumero)) {
			retorno = false;
		}
		return retorno;	
	}	
	

	public ClasseJudicialCriminalDTO getClasseJudicial() {
		return classeJudicial;
	}

	public void setClasseJudicial(ClasseJudicialCriminalDTO classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

}
