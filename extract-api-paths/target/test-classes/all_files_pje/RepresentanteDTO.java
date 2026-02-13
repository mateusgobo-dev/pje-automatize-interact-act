package br.jus.cnj.pje.webservice.client.domicilioeletronico.dto;

import java.io.Serializable;

import br.com.infox.cliente.util.ParametroUtil;
import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.ModalidadeRepresentanteProcessual;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * Classe que representa um representante usado pelo Domicílio Eletrônico.
 * 
 */
public class RepresentanteDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private PessoaDTO pessoa;
	private String modalidadeParticipacao;
	
	private String numeroDocumento;
	private String nome;
	private String status;
	private String documentoRepresentado;
	
	/**
	 * Construtor.
	 * 
	 */
	public RepresentanteDTO() {
		// Construtor
	}
	/**
	 * Construtor.
	 * 
	 * @param parte
	 */
	public RepresentanteDTO(ProcessoParteRepresentante parte) {
		if (parte != null) {
			TipoParte tipoRepresentante = parte.getTipoRepresentante();
			if(tipoRepresentante.equals(ParametroUtil.instance().getTipoParteAdvogado())) {
				setModalidadeParticipacao(ModalidadeRepresentanteProcessual.A.toString());
			}
			else {
				setModalidadeParticipacao(ModalidadeRepresentanteProcessual.P.toString());
			}
			
			setPessoa(new PessoaDTO(parte.getRepresentante()));
			Pessoa representante = parte.getRepresentante();
			setNumeroDocumento(StringUtil.removeNaoNumericos(representante.getDocumentoCpfCnpj()));
			setNome(representante.getNome());
			setStatus((representante.getAtivo() ? "A" : "I"));
			
			String cpfCnpj = parte.getProcessoParte().getPessoa().getDocumentoCpfCnpj();
			setDocumentoRepresentado(StringUtil.removeNaoNumericos(cpfCnpj));
		}
	}

	/**
	 * @return the pessoa
	 */
	public PessoaDTO getPessoa() {
		return pessoa;
	}

	/**
	 * @param pessoa the pessoa to set
	 */
	public void setPessoa(PessoaDTO pessoa) {
		this.pessoa = pessoa;
	}

	/**
	 * @return the modalidadeParticipacao
	 */
	public String getModalidadeParticipacao() {
		return modalidadeParticipacao;
	}

	/**
	 * @param modalidadeParticipacao the modalidadeParticipacao to set
	 */
	public void setModalidadeParticipacao(String modalidadeParticipacao) {
		this.modalidadeParticipacao = modalidadeParticipacao;
	}

	/**
	 * @return the numeroDocumento
	 */
	public String getNumeroDocumento() {
		return numeroDocumento;
	}

	/**
	 * @param numeroDocumento the numeroDocumento to set
	 */
	public void setNumeroDocumento(String numeroDocumento) {
		this.numeroDocumento = numeroDocumento;
	}

	/**
	 * @return the nome
	 */
	public String getNome() {
		return nome;
	}

	/**
	 * @param nome the nome to set
	 */
	public void setNome(String nome) {
		this.nome = nome;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	/**
	 * @return the documentoRepresentado
	 */
	public String getDocumentoRepresentado() {
		return documentoRepresentado;
	}
	/**
	 * @param documentoRepresentado the documentoRepresentado to set
	 */
	public void setDocumentoRepresentado(String documentoRepresentado) {
		this.documentoRepresentado = documentoRepresentado;
	}

}
