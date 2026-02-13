/**
 *  pje
 *  Copyright (C) 2013 Conselho Nacional de Justiça
 *
 *  A propriedade intelectual deste programa, tanto quanto a seu código-fonte
 *  quanto a derivação compilada é propriedade da União Federal, dependendo
 *  o uso parcial ou total de autorização expressa do Conselho Nacional de Justiça.
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.home.CadastroAdvogadoHome;
import br.com.infox.cliente.home.ProcessoDocumentoHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.jus.cnj.certificado.Signer.SignatureAlgorithm;
import br.jus.cnj.pje.business.dao.DocumentoPessoaDAO;
import br.jus.cnj.pje.nucleo.service.AssinaturaDigitalService;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.pje.nucleo.entidades.DocumentoPessoa;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.identidade.Papel;

/**
 * Componente de gerenciamento da entidade {@link DocumentoPessoa}.
 * 
 * @author cristof
 *
 */
@Name("documentoPessoaManager")
public class DocumentoPessoaManager extends BaseManager<DocumentoPessoa> {
	
	@Logger
	private Log logger;
		
	@In
	private DocumentoPessoaDAO documentoPessoaDAO;
		
	@In
	private AssinaturaDigitalService assinaturaDigitalService;

	@In
	private UsuarioService usuarioService;

	@In
	private PessoaManager pessoaManager;

	@Override
	protected DocumentoPessoaDAO getDAO() {
		return documentoPessoaDAO;
	}
	
	public DocumentoPessoa getDocumentoPessoal(){
		DocumentoPessoa ret = new DocumentoPessoa();
		ret.setAtivo(true);
		ret.setDataInclusao(new Date());
		return ret;
	}
	
	public boolean validaDocumento(Pessoa pessoaLogada, DocumentoPessoa documento) throws Exception {		
		
		this.assinaturaDigitalService.validarAssinaturaDigitalEhPessoaLogada(
				documento.getDocumentoHtml().getBytes(),
				documento.getAssinatura(),
				documento.getCertChain(),
				SignatureAlgorithm.MD5withRSA.name(),
				pessoaLogada
		);
		
		return true;
	}

	/**
	 * Método gera termo de compromisso para advogado
	 * 
	 * @param advogado
	 * @return termo de compromisso criado
	 */
	public DocumentoPessoa gerarTermoDeCompromisso(PessoaAdvogado advogado) {
		ModeloDocumento md = ParametroUtil.instance().getModeloComprovanteCadastroAdvogado();
		String documentoHtml = md.getModeloDocumento();
		CadastroAdvogadoHome.instance().setId(advogado.getIdUsuario());
		CadastroAdvogadoHome.instance().setInstance(advogado);
		documentoHtml = ProcessoDocumentoHome.processarModelo(documentoHtml);
		DocumentoPessoa termodeCompromisso = CadastroAdvogadoHome.instance().gerarTermodeCompromisso(advogado,
				documentoHtml);
		return termodeCompromisso;
	}
	
	public DocumentoPessoa getUltimoTermoCompromisso(PessoaAdvogado pessoaAdvogado) {
		return documentoPessoaDAO.getUltimoTermoCompromisso(pessoaAdvogado);		
	}

	/**
	 * Método que retorna o último termo de compromisso criado para um jus postulandi 
	 * 
	 * @param pessoa jus postulandi
	 * @return último termo de compromisso para o tipo de pessoa
	 */
	public DocumentoPessoa getUltimoTermoCompromissoJusPostulandi(Pessoa pessoa) {
		return documentoPessoaDAO.getUltimoTermoCompromissoJusPostulandi(pessoa);
	}
	
	/**
	 * Método recebe uma pessoa e suas credenciais e verifica se aquela pessoa 
	 * possui um termo de compromisso assinado
	 * 
	 * @param pessoaLogada Pessoa logada no sistema
	 * @param identity Conjunto de identidades
	 * @return verdadeiro se a pessoa logada já possui um termo de compromisso assinado
	 */
	public boolean possuiTermoCompromissoAssinado(Pessoa pessoaLogada, Identity identity ) {
		boolean possuiTermoAssinado = false;
		
		if (Pessoa.instanceOf(pessoaLogada, PessoaAdvogado.class) && identity.hasRole("advogado") ){
			PessoaAdvogado pessoaAdvogado = ((PessoaFisica) pessoaLogada).getPessoaAdvogado();
						
			DocumentoPessoa docTermoCompromisso = getUltimoTermoCompromisso(pessoaAdvogado );
						
			possuiTermoAssinado = termoCompromissoAssinado( docTermoCompromisso );
		}
				
		return possuiTermoAssinado;
	}
	
	/**
	 * Método recebe uma pessoa se aquela pessoa 
	 * possui um termo de compromisso assinado como jus postulandi
	 * @param pessoaLogada Pessoa logada no sistema
	 * @return verdadeiro se a pessoa logada já possui um termo de compromisso assinado como jus postulandi
	 */
	public boolean possuiTermoCompromissoJusPostulandiAssinado(Pessoa pessoaLogada) {
			boolean retorno = false;
			if( pessoaManager.isExistePessoa(pessoaLogada)) {
				DocumentoPessoa docTermoCompromissoJusPostulandi = getUltimoTermoCompromissoJusPostulandi(pessoaLogada);
				retorno = termoCompromissoAssinado( docTermoCompromissoJusPostulandi );
			}
			return retorno;
	}

	/**
	 * Método verifica se a pessoa tem papel de jus postulandi 
	 * @param pessoaLogada Pessoa logada no sistema
	 * @return verdadeiro se a pessoa logada já possui papel jus postulandi
	 */
	public boolean possuiPapelJusPostulandi(Pessoa pessoaLogada) {
		Papel papelJusPostulandi = ParametroUtil.instance().getPapelJusPostulandi();
		List<UsuarioLocalizacao> localizacoesUsuario = usuarioService.getLocalizacoesAtivas(pessoaLogada, papelJusPostulandi);
		return (localizacoesUsuario != null && localizacoesUsuario.size() > 0 );
	}

	
	/**
	 * Método recebe um termo de compromisso e verifica se o termo está assinado
	 * 
	 * @param docTermoCompromisso Termo de compromisso de um usuário
	 * @return verdadeiro se o termo estiver assinado
	 */
	public boolean termoCompromissoAssinado( DocumentoPessoa docTermoCompromisso ){
		return ( (docTermoCompromisso != null) && !Strings.isEmpty( docTermoCompromisso.getAssinatura()) );
	}
}
