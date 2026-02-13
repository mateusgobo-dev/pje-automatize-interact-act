package br.jus.cnj.pje.webservice;

import javax.ws.rs.core.Response;

public interface InformacaoSessaoService {
	Response recuperarInformacoes(Integer idSessao);
	Response recuperarTodasSessoes(Integer ano,Integer idSessao, Boolean somenteVirtuais, Boolean sessoesFuturas);
	Response recuperarVoto(Integer idSessaoProcesso, Integer idOrgaoJulgador);
}
