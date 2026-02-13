/**
 * pje-comum
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.nucleo.entidades.editor.topico;

import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import br.jus.pje.nucleo.entidades.editor.Topico;
import br.jus.pje.nucleo.enums.editor.TipoTopicoEnum;

@Entity
@Table(name = TopicoConclusaoMeritoRecurso.TABLE_NAME)
@PrimaryKeyJoinColumn(name = "id_topico")
public class TopicoConclusaoMeritoRecurso extends Topico {
	private static final long serialVersionUID = 1L;
	public static final String TABLE_NAME = "tb_topico_conclusao_merito_recurso";

	public TopicoConclusaoMeritoRecurso() {
		setTipoTopico(TipoTopicoEnum.CON_MERITO_REC);
		
		/*
		 * [PJEII-5151] PJE-JT: Sérgio Ricardo : PJE-1.4.7 
		 * Setar o status inicial do tópico de conclusão como não visível. 
		 */		
		setExibirTitulo(false);
	}
	
	@Override
	@Transient
	public boolean isConclusao() {
		return true;
	}
	
	@Transient
	@Override
	public Class<? extends Topico> getEntityClass() {
		return TopicoConclusaoMeritoRecurso.class;
	}
}
