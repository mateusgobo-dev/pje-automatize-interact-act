package br.jus.cnj.pje.amqp.model.dto;

import br.jus.cnj.pje.pjecommons.model.amqp.CloudEventPayload;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.enums.SimNaoFacultativoEnum;

public class ClasseJudicialCloudEvent implements CloudEventPayload<ClasseJudicialCloudEvent, ClasseJudicial>{

	private static final long serialVersionUID = 1L;

	private int idLegacy;
	private String descricao;
	private String codClasseJudicial;
	private String classeJudicialSigla;
	private Boolean pauta;
	private SimNaoFacultativoEnum exigeRevisor;
	
	public ClasseJudicialCloudEvent() {
		super();
	}
	
	public ClasseJudicialCloudEvent(ClasseJudicial classe) {
		super();
		this.idLegacy = classe.getIdClasseJudicial();
		this.descricao = classe.getClasseJudicial();
		this.codClasseJudicial = classe.getCodClasseJudicial();
		this.classeJudicialSigla = classe.getClasseJudicialSigla();
		this.pauta = classe.getPauta();
		this.exigeRevisor = classe.getExigeRevisor();
	}	

	@Override
	public Long getId(ClasseJudicial entity) {
		return (entity != null ? Long.valueOf(entity.getIdClasseJudicial()) :  null);
	}

	@Override
	public ClasseJudicialCloudEvent convertEntityToPayload(ClasseJudicial entity) {
		return new ClasseJudicialCloudEvent(entity);
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public String getCodClasseJudicial() {
		return codClasseJudicial;
	}

	public void setCodClasseJudicial(String codClasseJudicial) {
		this.codClasseJudicial = codClasseJudicial;
	}

	public String getClasseJudicialSigla() {
		return classeJudicialSigla;
	}

	public void setClasseJudicialSigla(String classeJudicialSigla) {
		this.classeJudicialSigla = classeJudicialSigla;
	}

	public Boolean getPauta() {
		return pauta;
	}

	public void setPauta(Boolean pauta) {
		this.pauta = pauta;
	}

	public SimNaoFacultativoEnum getExigeRevisor() {
		return exigeRevisor;
	}

	public void setExigeRevisor(SimNaoFacultativoEnum exigeRevisor) {
		this.exigeRevisor = exigeRevisor;
	}

	public int getIdLegacy() {
		return idLegacy;
	}

	public void setIdLegacy(int idLegacy) {
		this.idLegacy = idLegacy;
	}

}
