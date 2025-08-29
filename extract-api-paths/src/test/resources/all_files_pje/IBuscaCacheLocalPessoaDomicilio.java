package br.jus.cnj.pje.nucleo.service;

import br.jus.pje.nucleo.entidades.PessoaDomicilioEletronico;

public interface IBuscaCacheLocalPessoaDomicilio {
	PessoaDomicilioEletronico buscarDocumento(String numeroDocumento);
}
