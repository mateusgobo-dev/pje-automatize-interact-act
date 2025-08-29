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
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.editor.Topico;
import br.jus.pje.nucleo.enums.editor.TipoTopicoEnum;

@Entity
@Table(name = TopicoItemConsideracoes.TABLE_NAME)
@PrimaryKeyJoinColumn(name = "id_topico")
public class TopicoItemConsideracoes extends Topico {
	private static final long serialVersionUID = 1L;
	public static final String TABLE_NAME = "tb_topico_item_consideracoes";
	
	private OrgaoJulgador orgaoJulgador;
	
	public TopicoItemConsideracoes() {
		setTipoTopico(TipoTopicoEnum.ITEM_CONSIDERACOES);
	}
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_orgao_julgador", nullable = false)
	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}
	
	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
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

	@Transient
	@Override
	public Class<? extends Topico> getEntityClass() {
		return TopicoItemConsideracoes.class;
	}
}
