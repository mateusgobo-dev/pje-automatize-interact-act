package br.jus.cnj.intercomunicacao.v222.beans;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class ExpedienteProcessualECNJ {
	private ModalidadeMeioExpedicao meioExpedicao;
	private List<PessoaExpediente> pessoasExpediente = new ArrayList<PessoaExpediente>(0);
	private List<String> identificadoresDocumento = new ArrayList<String>(0);
	
	public ModalidadeMeioExpedicao getMeioExpedicao() {
		return meioExpedicao;
	}
	
	public void setMeioExpedicao(ModalidadeMeioExpedicao meioExpedicao) {
		this.meioExpedicao = meioExpedicao;
	}
	
	public List<PessoaExpediente> getPessoasExpediente() {
		return pessoasExpediente;
	}

	public void setPessoasExpediente(List<PessoaExpediente> pessoasExpediente) {
		this.pessoasExpediente = pessoasExpediente;
	}

	public List<String> getIdentificadoresDocumento() {
		return identificadoresDocumento;
	}

	public void setIdentificadoresDocumento(List<String> identificadoresDocumento) {
		this.identificadoresDocumento = identificadoresDocumento;
	}

	public static void main(String[] args) throws Exception{
		JAXBContext jc = JAXBContext.newInstance(ExpedienteProcessualECNJ.class);
		
		
		//Criar marshaller
		Marshaller m = jc.createMarshaller();
		
		ExpedienteProcessualECNJ expedienteProcessualECNJ = new ExpedienteProcessualECNJ();
		expedienteProcessualECNJ.setMeioExpedicao(ModalidadeMeioExpedicao.D);
		expedienteProcessualECNJ.identificadoresDocumento.add("1231165446578-1");
		expedienteProcessualECNJ.identificadoresDocumento.add("1231165446578-2");

		PessoaExpediente pessoaExpediente = new PessoaExpediente();
		Pessoa pessoa = new Pessoa();
		pessoa.setNome("Preencher conforme envio no objeto cabecalho");
		pessoaExpediente.setPessoa(pessoa);
		DataHora dataHora = new DataHora();
		dataHora.setValue(new SimpleDateFormat("ddMMyyyyHHmmss").format(new Date()));
		pessoaExpediente.setDataFim(dataHora);
		pessoaExpediente.setDataInicio(dataHora);
		pessoaExpediente.setPrazo(5);
		pessoaExpediente.setTipoPrazoExpediente(ModalidadeTipoPrazoExpediente.D);
		
		
		
		PessoaExpediente pessoaExpediente2 = new PessoaExpediente();
		Pessoa pessoa2 = new Pessoa();
		pessoa2.setNome("Pessoa 2");
		pessoaExpediente2.setPessoa(pessoa2);
		pessoaExpediente2.setDataFim(dataHora);
		pessoaExpediente2.setDataInicio(dataHora);
		pessoaExpediente2.setPrazo(2);
		pessoaExpediente2.setTipoPrazoExpediente(ModalidadeTipoPrazoExpediente.A);
		
		expedienteProcessualECNJ.getPessoasExpediente().add(pessoaExpediente);
		expedienteProcessualECNJ.getPessoasExpediente().add(pessoaExpediente2);		
		
		//Executar marshal do objeto no arquivo.
		m.marshal(expedienteProcessualECNJ, System.out);
	}
	

}
