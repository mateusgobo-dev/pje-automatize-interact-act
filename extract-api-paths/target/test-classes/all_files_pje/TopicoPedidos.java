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
@Table(name = TopicoPedidos.TABLE_NAME)
@PrimaryKeyJoinColumn(name = "id_topico")
public class TopicoPedidos extends Topico {

	private static final long serialVersionUID = 1L;
	public static final String TABLE_NAME = "tb_topico_pedidos";

	public TopicoPedidos() {
		setTipoTopico(TipoTopicoEnum.PEDIDOS);
	}

	@Override
	@Transient
	public boolean isUsoUnico() {
		return true;
	}

	@Override
	@Transient
	public Topico getItemTopico() {
		Topico item = new TopicoPedidosPeticao();
		item.setTituloPadrao(item.getTipoTopico().getLabel());
		item.setConteudoPadrao("");
		return item;
	}

	@Transient
	@Override
	public Class<? extends Topico> getEntityClass() {
		return TopicoPedidos.class;
	}
}
