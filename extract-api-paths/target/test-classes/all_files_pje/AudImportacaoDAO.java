package br.jus.csjt.pje.persistence.dao;

import javax.ejb.Local;

import br.jus.pje.jt.entidades.AudImportacao;
import br.jus.pje.nucleo.entidades.Usuario;

@Local
public interface AudImportacaoDAO {

	public void importarDados(AudImportacao ai);

	public void criarDocumentoAta(AudImportacao ai, Usuario usuarioInclusao);

}
