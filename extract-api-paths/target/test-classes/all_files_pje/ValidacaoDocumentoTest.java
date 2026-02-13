package br.jus.cnj.pje.assinatura;

import br.jus.pje.nucleo.entidades.*;
import br.jus.pje.nucleo.entidades.identidade.*;
import br.jus.pje.nucleo.enums.ExigibilidadeAssinaturaEnum;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinManager;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumentoPapel.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidacaoDocumentoTest {

	@Test
	public void quandoQualquerSuficienteAssinarDeveValidarDocumento() {
		
		// ### Configurar cenário
		List<TipoProcessoDocumentoPapel> papeisConfiguradosTipoDocumento = configurarCenarioSuficienteObrigatorioFacultativo();
		
		// ### Executar passos
		boolean primeiroSuficienteAssina = assinar(papeisConfiguradosTipoDocumento, assinaturaSuficiente1);
		boolean segundoSuficienteAssina = assinar(papeisConfiguradosTipoDocumento, assinaturaSuficiente2);
		boolean ambosSuficientesAssinam = assinar(papeisConfiguradosTipoDocumento, assinaturaSuficiente1, assinaturaSuficiente2);
		
		// ### Validar retornos (ps: idealmente deve haver um assert por método de teste)
		Assert.assertTrue("Quando algum papel suficiente assinar deveria retornar validado.",
				primeiroSuficienteAssina && segundoSuficienteAssina && ambosSuficientesAssinam);
	}

	@Test
	public void quandoExistirConfiguracaoSuficienteENenhumSuficienteAssinarNaoDeveValidarDocumento() {
		
		// ### Configurar cenário
		List<TipoProcessoDocumentoPapel> papeisConfiguradosTipoDocumento = configurarCenarioSuficienteObrigatorioFacultativo();
		
		// ### Executar passos
		boolean todosMenosSuficienteAssinam = assinar(papeisConfiguradosTipoDocumento, assinaturaObrigatorio1, assinaturaObrigatorio2, assinaturaFacultativo1, assinaturaFacultativo2);
		boolean apenasObrigatoriosAssinam = assinar(papeisConfiguradosTipoDocumento, assinaturaObrigatorio1, assinaturaObrigatorio2);
		boolean apenasFacultativosAssinam = assinar(papeisConfiguradosTipoDocumento, assinaturaFacultativo1, assinaturaFacultativo2);
		boolean ninguemAssina = assinar(papeisConfiguradosTipoDocumento);
		
		// ### Validar retornos
		Assert.assertFalse("Quando existir suficiente na configuração do tipo de documento e nenhum suficiente assinar, não deveria retornar validado.",
				todosMenosSuficienteAssinam || apenasObrigatoriosAssinam || apenasFacultativosAssinam || ninguemAssina);
	}
	
	@Test
	public void quandoNaoExistirSuficienteETodosObrigatoriosAssinaremDeveValidarDocumento() {
		// ### Configurar cenário
		List<TipoProcessoDocumentoPapel> papeisConfiguradosTipoDocumento = configurarCenarioObrigatorioFacultativo();
		
		// ### Executar passos
		boolean todosAssinam = assinar(papeisConfiguradosTipoDocumento, assinaturaObrigatorio1, assinaturaObrigatorio2, assinaturaFacultativo1, assinaturaFacultativo2);
		boolean apenasObrigatoriosAssinam = assinar(papeisConfiguradosTipoDocumento, assinaturaObrigatorio1, assinaturaObrigatorio2);
		
		// ### Validar retornos
		Assert.assertTrue("Quando não houver suficiente na configuração, todos os obrigatórios deveriam assinar para retornar validado.",
				todosAssinam && apenasObrigatoriosAssinam);
	}
	
	@Test
	public void quandoNaoExistirSuficienteEAlgumObrigatorioNaoAssinarNaoDeveValidarDocumento() {
		// ### Configurar cenário
		List<TipoProcessoDocumentoPapel> papeisConfiguradosTipoDocumento = configurarCenarioObrigatorioFacultativo();
		
		// ### Executar passos
		boolean todosMenosPrimeiroObrigatorioAssinam = assinar(papeisConfiguradosTipoDocumento, assinaturaObrigatorio2, assinaturaFacultativo1, assinaturaFacultativo2);
		boolean todosMenosSegundoObrigatorioAssinam = assinar(papeisConfiguradosTipoDocumento, assinaturaObrigatorio1, assinaturaFacultativo1, assinaturaFacultativo2);
		boolean apenasPrimeiroObrigatorioAssina = assinar(papeisConfiguradosTipoDocumento, assinaturaObrigatorio1);
		boolean apenasSegundoObrigatorioAssina = assinar(papeisConfiguradosTipoDocumento, assinaturaObrigatorio2);
		boolean ninguemAssina = assinar(papeisConfiguradosTipoDocumento, assinaturaObrigatorio2);
		
		// ### Validar retornos
		Assert.assertFalse("Quando não houver suficiente na configuração, todos os obrigatórios deveriam assinar para retornar validado.",
				todosMenosPrimeiroObrigatorioAssinam || todosMenosSegundoObrigatorioAssinam || apenasPrimeiroObrigatorioAssina || apenasSegundoObrigatorioAssina || ninguemAssina);
	}
	
	@Test
	public void quandoNaoExistirConfiguracaoSuficienteEObrigatorioDeveValidarDocumento() {
		// ### Configurar cenário
		List<TipoProcessoDocumentoPapel> papeisConfiguradosTipoDocumento = configurarCenarioFacultativo();
		
		// ### Executar passos
		boolean todosFacultativosAssinam = assinar(papeisConfiguradosTipoDocumento, assinaturaFacultativo1, assinaturaFacultativo2);
		boolean primeiroFacultativoAssina = assinar(papeisConfiguradosTipoDocumento, assinaturaFacultativo1);
		boolean segundoFacultativoAssina = assinar(papeisConfiguradosTipoDocumento, assinaturaFacultativo2);
		boolean ninguemAssina = assinar(papeisConfiguradosTipoDocumento);
		
		// ### Validar retornos
		Assert.assertTrue("Quando só houver facultativo na configuração, deveria retornar validado sempre.",
				todosFacultativosAssinam && primeiroFacultativoAssina && segundoFacultativoAssina && ninguemAssina);
	}
	
	@Test
	public void quandoNaoExistirConfiguracaoTipoDocumentoDeveValidarDocumento() {
		// ### Configurar cenário
		List<TipoProcessoDocumentoPapel> papeisConfiguradosTipoDocumento = configurarCenarioSemConfiguracaoPapeisTipoDocumento();
		
		// ### Executar passos
		boolean alguemAssina = assinar(papeisConfiguradosTipoDocumento, assinaturaFacultativo1);
		boolean ninguemAssina = assinar(papeisConfiguradosTipoDocumento);
		
		// ### Validar retornos
		Assert.assertTrue("Quando não houver configuração no tipo de documento, deveria retornar validado sempre.",
				alguemAssina && ninguemAssina);
	}
	
	@Test
	public void quandoJaEstiverValidadoDeveRetornarValidado() {
		// ### Configurar cenário
		documentoBin.getValido();
		when(documentoBin.getValido()).thenReturn(true);
		
		// ### Executar passos
		boolean ninguemAssina = this.processoDocumentoBinManager.verificaValidacao(documentoBin, null, null);
		
		// ### Validar retornos
		Assert.assertTrue("Quando o documento já estiver validado, deveria retornar validado.",
				ninguemAssina);
	}
	
	// Configurações de cenários
	private List<TipoProcessoDocumentoPapel> configurarCenarioSuficienteObrigatorioFacultativo() {
		List<TipoProcessoDocumentoPapel> papeisConfiguradosTipoDocumento = new ArrayList<TipoProcessoDocumentoPapel>();
		papeisConfiguradosTipoDocumento.add(configuracaoTipoDocumentoPapelSuficiente1);
		papeisConfiguradosTipoDocumento.add(configuracaoTipoDocumentoPapelSuficiente2);
		papeisConfiguradosTipoDocumento.add(configuracaoTipoDocumentoPapelObrigatorio1);
		papeisConfiguradosTipoDocumento.add(configuracaoTipoDocumentoPapelObrigatorio2);
		papeisConfiguradosTipoDocumento.add(configuracaoTipoDocumentoPapelFacultativo1);
		papeisConfiguradosTipoDocumento.add(configuracaoTipoDocumentoPapelFacultativo2);
		
		when(tipoProcessoDocumento.getPapeis()).thenReturn(papeisConfiguradosTipoDocumento);
		
		return papeisConfiguradosTipoDocumento;
	}
	
	private List<TipoProcessoDocumentoPapel> configurarCenarioObrigatorioFacultativo() {
		List<TipoProcessoDocumentoPapel> papeisConfiguradosTipoDocumento = new ArrayList<TipoProcessoDocumentoPapel>();
		papeisConfiguradosTipoDocumento.add(configuracaoTipoDocumentoPapelObrigatorio1);
		papeisConfiguradosTipoDocumento.add(configuracaoTipoDocumentoPapelObrigatorio2);
		papeisConfiguradosTipoDocumento.add(configuracaoTipoDocumentoPapelFacultativo1);
		papeisConfiguradosTipoDocumento.add(configuracaoTipoDocumentoPapelFacultativo2);
		
		when(tipoProcessoDocumento.getPapeis()).thenReturn(papeisConfiguradosTipoDocumento);
		
		return papeisConfiguradosTipoDocumento;
	}
	
	private List<TipoProcessoDocumentoPapel> configurarCenarioFacultativo() {
		List<TipoProcessoDocumentoPapel> papeisConfiguradosTipoDocumento = new ArrayList<TipoProcessoDocumentoPapel>();
		papeisConfiguradosTipoDocumento.add(configuracaoTipoDocumentoPapelFacultativo1);
		papeisConfiguradosTipoDocumento.add(configuracaoTipoDocumentoPapelFacultativo2);
		
		when(tipoProcessoDocumento.getPapeis()).thenReturn(papeisConfiguradosTipoDocumento);
		
		return papeisConfiguradosTipoDocumento;
	}
	
	private List<TipoProcessoDocumentoPapel> configurarCenarioSemConfiguracaoPapeisTipoDocumento() {
		List<TipoProcessoDocumentoPapel> papeisConfiguradosTipoDocumento = new ArrayList<TipoProcessoDocumentoPapel>();
		
		when(tipoProcessoDocumento.getPapeis()).thenReturn(papeisConfiguradosTipoDocumento);
		
		return papeisConfiguradosTipoDocumento;
	}
	
	private List<ProcessoDocumentoBinPessoaAssinatura> configuraAssinaturas(
			List<TipoProcessoDocumentoPapel> papeisConfiguradosTipoDocumento,
			ProcessoDocumentoBinPessoaAssinatura... assinaturasPessoas) {
		List<ProcessoDocumentoBinPessoaAssinatura> assinaturas = new ArrayList<ProcessoDocumentoBinPessoaAssinatura>();
		assinaturas.addAll((List<ProcessoDocumentoBinPessoaAssinatura>) Arrays.asList(assinaturasPessoas));
		return assinaturas;
	}
	
	private boolean assinar(List<TipoProcessoDocumentoPapel> papeisConfiguradosTipoDocumento, ProcessoDocumentoBinPessoaAssinatura... assinaturasPessoas) {
		List<ProcessoDocumentoBinPessoaAssinatura> assinaturas = configuraAssinaturas(papeisConfiguradosTipoDocumento, assinaturasPessoas);
		return this.processoDocumentoBinManager.verificaValidacao(documentoBin, assinaturas, tipoProcessoDocumento);
	}
	
	// Configurações/Inicializações de Mocks
	@Before
	public void initTest() {
		this.processoDocumentoBinManager = new ProcessoDocumentoBinManager();
		when(documentoBin.getValido()).thenReturn(false);

		configurarTipoDocumentoPapel(configuracaoTipoDocumentoPapelSuficiente1, papelSuficiente1, ExigibilidadeAssinaturaEnum.S);
		configurarTipoDocumentoPapel(configuracaoTipoDocumentoPapelSuficiente2, papelSuficiente2, ExigibilidadeAssinaturaEnum.S);
		
		configurarTipoDocumentoPapel(configuracaoTipoDocumentoPapelObrigatorio1, papelObrigatorio1, ExigibilidadeAssinaturaEnum.O);
		configurarTipoDocumentoPapel(configuracaoTipoDocumentoPapelObrigatorio2, papelObrigatorio2, ExigibilidadeAssinaturaEnum.O);

		configurarTipoDocumentoPapel(configuracaoTipoDocumentoPapelFacultativo1, papelFacultativo1, ExigibilidadeAssinaturaEnum.F);
		configurarTipoDocumentoPapel(configuracaoTipoDocumentoPapelFacultativo2, papelFacultativo2, ExigibilidadeAssinaturaEnum.F);
		
		configurarAssinatura(assinaturaSuficiente1, papelSuficiente1);
		configurarAssinatura(assinaturaSuficiente2, papelSuficiente2);
		
		configurarAssinatura(assinaturaObrigatorio1, papelObrigatorio1);
		configurarAssinatura(assinaturaObrigatorio2, papelObrigatorio2);
		
		configurarAssinatura(assinaturaFacultativo1, papelFacultativo1);
		configurarAssinatura(assinaturaFacultativo2, papelFacultativo2);
	}

	private void configurarTipoDocumentoPapel(TipoProcessoDocumentoPapel configuracaoTipoDocumentoPapel, Papel papel, ExigibilidadeAssinaturaEnum exigibilidade) {
		when(configuracaoTipoDocumentoPapel.getPapel()).thenReturn(papel);
		when(configuracaoTipoDocumentoPapel.getExigibilidade()).thenReturn(exigibilidade);
	}
	
	private void configurarAssinatura(ProcessoDocumentoBinPessoaAssinatura assinatura, Papel papelAssinador) {
		when(assinatura.getPessoa()).thenReturn(mock(Pessoa.class));
		when(assinatura.getPessoa().getPapelSet()).thenReturn(new HashSet<Papel>(Arrays.asList(papelAssinador)));
	}
	
	private ProcessoDocumentoBinManager processoDocumentoBinManager;
	@Mock
	private ProcessoDocumentoBin documentoBin;
	
	@Mock
	private Papel papelSuficiente1;
	@Mock
	private Papel papelSuficiente2;
	@Mock
	private Papel papelObrigatorio1;
	@Mock
	private Papel papelObrigatorio2;
	@Mock
	private Papel papelFacultativo1;
	@Mock
	private Papel papelFacultativo2;
	
	@Mock
	private TipoProcessoDocumentoPapel configuracaoTipoDocumentoPapelSuficiente1;
	@Mock
	private TipoProcessoDocumentoPapel configuracaoTipoDocumentoPapelSuficiente2;
	@Mock
	private TipoProcessoDocumentoPapel configuracaoTipoDocumentoPapelObrigatorio1;
	@Mock
	private TipoProcessoDocumentoPapel configuracaoTipoDocumentoPapelObrigatorio2;
	@Mock
	private TipoProcessoDocumentoPapel configuracaoTipoDocumentoPapelFacultativo1;
	@Mock
	private TipoProcessoDocumentoPapel configuracaoTipoDocumentoPapelFacultativo2;
	
	@Mock
	private TipoProcessoDocumento tipoProcessoDocumento;
	
	@Mock
	private ProcessoDocumentoBinPessoaAssinatura assinaturaSuficiente1;
	@Mock
	private ProcessoDocumentoBinPessoaAssinatura assinaturaSuficiente2;
	@Mock
	private ProcessoDocumentoBinPessoaAssinatura assinaturaObrigatorio1;
	@Mock
	private ProcessoDocumentoBinPessoaAssinatura assinaturaObrigatorio2;
	@Mock
	private ProcessoDocumentoBinPessoaAssinatura assinaturaFacultativo1;
	@Mock
	private ProcessoDocumentoBinPessoaAssinatura assinaturaFacultativo2;

}
