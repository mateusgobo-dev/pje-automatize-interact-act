package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;

@Name("informacaoCriminalRelevanteManager")
public class InformacaoCriminalRelevanteManager<T extends InformacaoCriminalRelevante> extends
		IcrAssociarTipificacaoDelitoManager<T> {
	@Override
	public void setDtPublicacao(T entity, Date data) {
		// TODO Auto-generated method stub
	}

	@Override
	public Date getDtPublicacao(T entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean possuiDataPublicacao() {
		// TODO Auto-generated method stub
		return false;
	}
}
