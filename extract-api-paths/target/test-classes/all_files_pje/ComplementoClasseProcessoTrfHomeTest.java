package br.com.infox.test.cliente.home;

import org.junit.Test;

import junit.framework.Assert;

import br.com.infox.cliente.home.ComplementoClasseProcessoTrfHome;
import br.jus.pje.nucleo.entidades.ClasseAplicacao;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.ComplementoClasse;
import br.jus.pje.nucleo.entidades.ComplementoClasseProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

public class ComplementoClasseProcessoTrfHomeTest {

	private static final String COMP_CLASSE = "123";
	private static final String COMP_VALOR = "456";

	@Test
	public void getComplementoCompletoTest() {

		ComplementoClasseProcessoTrf ccp = new ComplementoClasseProcessoTrf();
		ccp.setProcessoTrf(processoTrf());
		ccp.setComplementoClasse(complementoClasse());
		ccp.setValorComplementoClasseProcessoTrf(COMP_VALOR);

		Assert.assertEquals(COMP_CLASSE + ":" + COMP_VALOR,
				ComplementoClasseProcessoTrfHome.getComplementoCompleto(ccp));
	}

	private final ProcessoTrf processoTrf() {
		ProcessoTrf pTrf = new ProcessoTrf();

		pTrf.setClasseJudicial(classeJuldicial());
		pTrf.setSelecionadoPauta(false);
		pTrf.setRevisado(false);

		return pTrf;
	}

	private final ClasseJudicial classeJuldicial() {

		ClasseJudicial cj = new ClasseJudicial();

		cj.setCodClasseJudicial("codClasse");
		cj.setClasseJudicial("Classe");
		cj.setAtivo(true);

		return cj;
	}

	private final ComplementoClasse complementoClasse() {

		ComplementoClasse cc = new ComplementoClasse();

		cc.setClasseAplicacao(classeAplicacao());
		cc.setComponenteValidacao("CompValidação");
		cc.setComplementoClasse(COMP_CLASSE);
		cc.setObrigatorio(false);

		return cc;
	}

	private final ClasseAplicacao classeAplicacao() {
		ClasseAplicacao ca = new ClasseAplicacao();

		ca.setAtivo(true);
		ca.setDistribuicaoAutomatica(false);

		return ca;
	}
}