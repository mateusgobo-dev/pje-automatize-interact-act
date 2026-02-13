package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.IcrAtribuicaoAutoria;

@Name("icrAAUManager")
public class IcrAtribuicaoAutoriaManager extends InformacaoCriminalRelevanteManager<IcrAtribuicaoAutoria> {
	@Override
	public Date getDtPublicacao(IcrAtribuicaoAutoria entity) {
		// TODO Auto-generated method stub
		return entity.getDataPublicacao();
	}

	@Override
	public Boolean possuiDataPublicacao() {
		// TODO Auto-generated method stub
		return true;
	}
}
