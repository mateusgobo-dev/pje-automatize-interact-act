package br.jus.pje.nucleo.entidades;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import br.jus.pje.nucleo.beans.criminal.ConteudoInformacaoCriminalBean;
import br.jus.pje.nucleo.type.ConteudoInformacaoCriminalType;

@Entity
@Table(name = InformacaoCriminalRascunho.TABLE_NAME)
@SequenceGenerator(allocationSize = 1, name = "gen_informacao_criminal_rascunho", sequenceName = "sq_tb_informacao_criminal_rascunho")
@TypeDefs({ @TypeDef(name = "ConteudoInformacaoCriminalType", typeClass = ConteudoInformacaoCriminalType.class) })
public class InformacaoCriminalRascunho implements Serializable{

	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "tb_informacao_criminal_rascunho";	
	
	private Long id;
	private ProcessoRascunho processoRascunho;
	private ProcessoParteMin processoParte;
	private ConteudoInformacaoCriminalBean informacaoCriminal;
	
	public InformacaoCriminalRascunho(Long id, ProcessoRascunho processoRascunho, ProcessoParteMin processoParte,
			ConteudoInformacaoCriminalBean informacaoCriminal) {
		super();
		this.id = id;
		this.processoRascunho = processoRascunho;
		this.processoParte = processoParte;
		this.informacaoCriminal = informacaoCriminal;
	}

	public InformacaoCriminalRascunho() {
		super();
	}

	@Id
	@GeneratedValue(generator = "gen_informacao_criminal_rascunho")
	@Column(name = "id_informacao_criminal_rascunho", nullable = false)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_rascunho")
	public ProcessoRascunho getProcessoRascunho() {
		return processoRascunho;
	}

	public void setProcessoRascunho(ProcessoRascunho processoRascunho) {
		this.processoRascunho = processoRascunho;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_parte", nullable = false)
	public ProcessoParteMin getProcessoParte() {
		return processoParte;
	}

	public void setProcessoParte(ProcessoParteMin processoParte) {
		this.processoParte = processoParte;
	}

	@Column(name = "json_informacao_criminal")
	@Type(type = "ConteudoInformacaoCriminalType")
	public ConteudoInformacaoCriminalBean getInformacaoCriminal() {
		return informacaoCriminal;
	}

	public void setInformacaoCriminal(ConteudoInformacaoCriminalBean informacaoCriminal) {
		this.informacaoCriminal = informacaoCriminal;
	}
	
	
}
