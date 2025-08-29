package br.jus.cnj.pje.amqp.model.dto;

import br.jus.cnj.pje.pjecommons.model.amqp.CloudEventPayload;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFisicaEspecializada;

/**
 * Classe que representa os dados da pessoa que são enviados para o RabbitMQ.
 * 
 * @author Adriano Pamplona
 */
public class PessoaDomainEventMessage implements CloudEventPayload<PessoaDomainEventMessage, Pessoa> {

	private String nome;
	private String tipo;
	private String documento;

	/**
	 * Construtor.
	 *
	 * @param pessoa
	 */
	public PessoaDomainEventMessage(Pessoa pessoa) {
		super();
		if (pessoa != null) {
			this.setNome(pessoa.getNome());
			this.setTipo(pessoa.getInTipoPessoa().getLabel());
			this.setDocumento(pessoa.getDocumentoCpfCnpj());
		}
	}

	/**
	 * Construtor.
	 *
	 * @param pessoa
	 */
	public PessoaDomainEventMessage(PessoaFisicaEspecializada pessoa) {
		super();
		if (pessoa != null) {
			this.setNome(pessoa.getNome());
			this.setTipo(pessoa.getInTipoPessoa().getLabel());
			this.setDocumento(pessoa.getDocumentoCpfCnpj());
		}
	}

	@Override
	public PessoaDomainEventMessage convertEntityToPayload(Pessoa entity) {
		return new PessoaDomainEventMessage(entity);
	}

	@Override
	public Long getId(Pessoa entity) {
		return (entity != null ? Long.valueOf(entity.getIdPessoa()) : null);
	}

	/**
	 * @return Retorna nome.
	 */
	public String getNome() {
		return nome;
	}

	/**
	 * @param nome Atribui nome.
	 */
	public void setNome(String nome) {
		this.nome = nome;
	}

	/**
	 * @return Retorna tipoPessoa.
	 */
	public String getTipo() {
		return tipo;
	}

	/**
	 * @param tipoPessoa Atribui tipoPessoa.
	 */
	public void setTipo(String tipoPessoa) {
		this.tipo = tipoPessoa;
	}

	/**
	 * @return Retorna documentoCpfCnpj.
	 */
	public String getDocumento() {
		return documento;
	}

	/**
	 * @param documentoCpfCnpj Atribui documentoCpfCnpj.
	 */
	public void setDocumento(String documentoCpfCnpj) {
		this.documento = documentoCpfCnpj;
	}

}
