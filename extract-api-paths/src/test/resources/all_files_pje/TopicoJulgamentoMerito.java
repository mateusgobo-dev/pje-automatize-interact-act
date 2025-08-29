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

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import br.jus.pje.nucleo.entidades.editor.Topico;
import br.jus.pje.nucleo.enums.editor.TipoTopicoEnum;

@Entity
@Table(name = TopicoJulgamentoMerito.TABLE_NAME)
@PrimaryKeyJoinColumn(name = "id_topico")
public class TopicoJulgamentoMerito extends Topico implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final String TABLE_NAME = "tb_topico_julgamento_merito";

	public TopicoJulgamentoMerito() {
		setTipoTopico(TipoTopicoEnum.JULGAMENTO_MERITO);
	}

	@Override
	@Transient
	public Topico getItemTopico() {
		Topico item = new TopicoItemMerito();
		item.setTituloPadrao(item.getTipoTopico().getLabel());
		item.setConteudoPadrao("");
		return item;
	}

	@Override
	@Transient
	public boolean isUsoUnico() {
		return true;
	}

	@Transient
	@Override
	public Class<? extends Topico> getEntityClass() {
		return TopicoJulgamentoMerito.class;
	}
}
