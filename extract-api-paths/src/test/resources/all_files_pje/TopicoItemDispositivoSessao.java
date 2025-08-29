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
@Table(name = TopicoItemDispositivoSessao.TABLE_NAME)
@PrimaryKeyJoinColumn(name = "id_topico")
public class TopicoItemDispositivoSessao extends Topico {
	private static final long serialVersionUID = 1L;
	public static final String TABLE_NAME = "tb_topico_item_dispositivo_sessao";

	public TopicoItemDispositivoSessao() {
		setTipoTopico(TipoTopicoEnum.IT_DISP_SESSAO);
	}
	
	@Override
	@Transient
	public boolean isItem() {
		return true;
	}
	
	@Override
	@Transient
	public boolean isSomenteLeitura() {
		return true;
	}
	
	@Override
	@Transient
	public boolean isExibirTitulo() {
		return false;
	}

	@Transient
	@Override
	public Class<? extends Topico> getEntityClass() {
		return TopicoItemDispositivoSessao.class;
	}
}
