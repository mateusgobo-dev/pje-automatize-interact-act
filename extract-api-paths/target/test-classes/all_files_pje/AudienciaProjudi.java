package br.jus.cnj.intercomunicacao.v222.beans;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class AudienciaProjudi {
	
	
	@XmlAttribute
	private Long identificadorTipoAudiencia;
	
	@XmlAttribute
	private DataHora dataInicio;
	
	@XmlAttribute
	private DataHora dataFim;
	
	@XmlAttribute
	private String cpfConciliador;
	
	@XmlAttribute
	private String cpfRealizador;
	
	@XmlAttribute
	private StatusAudienciaEnum status;

	public Long getIdentificadorTipoAudiencia() {
		return identificadorTipoAudiencia;
	}


	public void setIdentificadorTipoAudiencia(Long identificadorTipoAudiencia) {
		this.identificadorTipoAudiencia = identificadorTipoAudiencia;
	}

	public DataHora getDataInicio() {
		return dataInicio;
	}
	
	public void setDataInicio(DataHora dataInicio) {
		this.dataInicio = dataInicio;
	}
	
	public DataHora getDataFim() {
		return dataFim;
	}
	
	public void setDataFim(DataHora dataFim) {
		this.dataFim = dataFim;
	}

	public String getCpfRealizador() {
		return cpfRealizador;
	}


	public void setCpfRealizador(String cpfRealizador) {
		this.cpfRealizador = cpfRealizador;
	}
	
	
	public String getCpfConciliador() {
		return cpfConciliador;
	}
	
	public void setCpfConciliador(String cpfConciliador) {
		this.cpfConciliador = cpfConciliador;
	}
	
	public StatusAudienciaEnum getStatus() {
		return status;
	}
	
	public void setStatus(StatusAudienciaEnum status) {
		this.status = status;
	}
	
	
	public static void main (String[] args) throws Exception{
		JAXBContext jaxbContext = JAXBContext.newInstance(AudienciaProjudi.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		
		AudienciaProjudi audienciaProjudi = new AudienciaProjudi();
		
		audienciaProjudi.setCpfRealizador("726012310120");
		DataHora dataHora = new DataHora();
		dataHora.setValue("23112014103359");
		DataHora dataHora2 = new DataHora();
		dataHora2.setValue("23112014113359");	
		audienciaProjudi.setDataInicio(dataHora);
		audienciaProjudi.setDataFim(dataHora2);
		audienciaProjudi.setIdentificadorTipoAudiencia(1L);
		audienciaProjudi.setStatus(StatusAudienciaEnum.DESIGNADA);
		
		jaxbMarshaller.marshal(audienciaProjudi, System.out);
		
	}
	
}
