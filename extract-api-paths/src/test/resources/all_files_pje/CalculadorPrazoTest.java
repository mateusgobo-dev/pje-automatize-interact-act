package br.jus.cnj.pje.servicos.prazos;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import br.jus.cnj.pje.servicos.PrazosProcessuaisServiceTest;

@RunWith(Suite.class)
@SuiteClasses({ 
	CalculadorPrazoManifestacaoContinuoTest.class, 
	CalculadorPrazoManifestacaoDiasUteisTest.class, 
	CalendarioTest.class, 
	PrazosProcessuaisServiceTest.class })
public class CalculadorPrazoTest {

}
