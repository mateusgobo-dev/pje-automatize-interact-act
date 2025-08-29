package br.jus.cnj.pje.servicos;

import java.io.Serializable;

import br.jus.pje.nucleo.entidades.Especialidade;
import br.jus.pje.nucleo.entidades.PessoaPerito;
import br.jus.pje.nucleo.entidades.PessoaPeritoEspecialidade;

public interface PessoaPeritoEspecialidadeService extends Serializable{

	PessoaPeritoEspecialidade findByPessoaPeritoAndEspecialidade(PessoaPerito pessoaPerito, Especialidade especialidade);
}