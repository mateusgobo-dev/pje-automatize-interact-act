package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.MeioContatoDAO;
import br.jus.pje.nucleo.entidades.MeioContato;
import br.jus.pje.nucleo.entidades.Pessoa;

@Name(MeioContatoManager.NAME)
public class MeioContatoManager extends BaseManager<MeioContato>{
	
	public static final String NAME = "meioContatoManager";
	
	@In
	private MeioContatoDAO meioContatoDAO;

	@Override
	protected MeioContatoDAO getDAO() {
		return meioContatoDAO;
	}

	public List<MeioContato> recuperaMeioContatoCadastrados(Pessoa _pessoa) {
		return meioContatoDAO.recuperaMeioContatoCadastrados(_pessoa);
	}

	public List<MeioContato> recuperaMeioContatoProprietarios(Pessoa _pessoa) {
		return meioContatoDAO.recuperaMeioContatoProprietarios(_pessoa);
	}
}