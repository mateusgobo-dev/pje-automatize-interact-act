package br.jus.pje.nucleo.entidades;
 
import java.util.ArrayList;
import java.util.List;

import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;
 
/**
 * classe criada para facilitar a manutençao e controle de objetos envolvidos na desunificacao
 * @author luiz.mendes
 *
 */
public class DesunificacaoVO {
 	
	private Pessoa pessoaSecundaria = null;
	private PessoaFisica pessoaFisicaSecundaria = null;
	private PessoaJuridica pessoaJuridicaSecundaria = null;
	private PessoaAutoridade pessoaAutoridadeSecundaria = null;
	private Usuario usuarioPessoaSecundaria = null;
	private UsuarioLogin usuarioLoginPessoaSecundaria = null;
 	private Unificacao unificacao = null;
 	private UnificacaoPessoas unificacaoPessoa = null;
 	private boolean isTipoPessoaSecundariaCorreto = false;
 	private boolean isTipoPessoaPrincipalCorreto = false;
 	
 	private List<UnificacaoPessoasObjeto> logAcessoUnificadosObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> caractFisUnificadasObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<CaracteristicaFisica> caractFisUnificadas = new ArrayList<CaracteristicaFisica>(0);
 	private List<UnificacaoPessoasObjeto> caractFisUnificadasNaoEncontradasObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> meiosContatoCadastradosUnificadosObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> meiosContatoCadastradosUnificadosAlteradosOuNaoEncontradasObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> meiosContatoProprietariasUnificadosObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> meiosContatoProprietariasUnificadosAlteradosOuNaoEncontradasObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> nomesAlternativosCadastradosUnificadosObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> nomesAlternativosCadastradosUnificadosAlteradosOuNaoEncontradosObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> nomesAlternativosProprietariasUnificadosObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> nomesAlternativosProprietariasUnificadosAlteradosOuNaoEncontradasObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> conexoesPrevencaoUnificadosObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> conexoesPrevencaoUnificadosNaoEncontradosObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> segredosProcessosUnificadosObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> segredosProcessosUnificadosNaoEncontradosObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> sigiloProcessosParteUnificadosObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> sigiloProcessosParteUnificadosNaoEncontradosObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> caixaRepresentanteUnificadosObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> caixaRepresentanteUnificadosNaoEncontradosObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> sessaoEnteExternoUnificadosObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> redistribuicaoProcessosUnificadosObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> redistribuicaoProcessosUnificadosNaoEncontradosObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> processoParteHistoricosUnificadosObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> processoParteHistoricosUnificadosNaoEncontradosObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> processosTagsUnificadosObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> processosTagsUnificadosNaoEncontradosObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> lembretesUnificadosObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> lembretesUnificadosNaoEncontradosObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> permissaoLembretesUnificadosObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> permissaoLembretesUnificadosNaoEncontradosObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> processosUnificadosObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> processosUnificadosNaoEncontradosObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> parametrosUnificadosObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> parametrosUnificadosNaoEncontradosObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> entityLogsUnificadosObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> entityLogsUnificadosNaoEncontradosObject = new ArrayList<UnificacaoPessoasObjeto>(0);
	private List<UnificacaoPessoasObjeto> solicitacoesNoDesvioUnificadasObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> solicitacoesNoDesvioNaoEncontradasObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> sessaoPautaProcessoInclusoraUnificadasObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> sessaoPautaProcessoInclusoraNaoEncontradasObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> sessaoPautaProcessoExclusoraUnificadasObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> sessaoPautaProcessoExclusoraNaoEncontradasObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> sessaoInclusoraUnificadasObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> sessaoInclusoraNaoEncontradasObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> sessaoExclusoraUnificadasObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> sessaoExclusoraNaoEncontradasObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> avisoQuadroAvisoUnificadasObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> avisoQuadroAvisoNaoEncontradasObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> processosDocumentoFavoritosUnificadosObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> processosDocumentoFavoritosNaoEncontradosObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> notasSessaoJulgamentoUnificadasObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> notasSessaoJulgamentoNaoEncontradasObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> modelosProclamacaoJulgamentoUnificadosObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> modelosProclamacaoJulgamentoNaoEncontradosObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> logsHistoricoMovimentacaoUnificadosObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> logsHistoricoMovimentacaoNaoEncontradosObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> visibilidadesDocIdentificacaoUnificadasObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	private List<UnificacaoPessoasObjeto> visibilidadesDocIdentificacaoNaoEncontradasObject = new ArrayList<UnificacaoPessoasObjeto>(0);
 	
 	public DesunificacaoVO() {}
 	
 	public Pessoa getPessoaPrincipal() {
 		if(unificacao != null) {
 			return unificacao.getPessoaPrincipal();
 		}else {
 			return null;
 		}
 	}
 
 	public Pessoa getPessoaSecundaria() {
 		return pessoaSecundaria;
 	}
 
 	public void setPessoaSecundaria(Pessoa pessoaSecundaria) {
 		this.pessoaSecundaria = pessoaSecundaria;
 	}
 	
 	public PessoaFisica getPessoaFisicaSecundaria() {
		return pessoaFisicaSecundaria;
	}

	public void setPessoaFisicaSecundaria(PessoaFisica pessoaFisicaSecundaria) {
		this.pessoaFisicaSecundaria = pessoaFisicaSecundaria;
	}
	
	public PessoaJuridica getPessoaJuridicaSecundaria() {
		return pessoaJuridicaSecundaria;
	}

	public void setPessoaJuridicaSecundaria(PessoaJuridica pessoaJuridicaSecundaria) {
		this.pessoaJuridicaSecundaria = pessoaJuridicaSecundaria;
	}

	public PessoaAutoridade getPessoaAutoridadeSecundaria() {
		return pessoaAutoridadeSecundaria;
	}

	public void setPessoaAutoridadeSecundaria(PessoaAutoridade pessoaAutoridadeSecundaria) {
		this.pessoaAutoridadeSecundaria = pessoaAutoridadeSecundaria;
	}
	
	public Usuario getUsuarioPessoaSecundaria() {
		return usuarioPessoaSecundaria;
	}

	public void setUsuarioPessoaSecundaria(Usuario usuarioPessoaSecundaria) {
		this.usuarioPessoaSecundaria = usuarioPessoaSecundaria;
	}
 
 	public Unificacao getUnificacao() {
 		return unificacao;
 	}
 
 	public void setUnificacao(Unificacao unificacao) {
 		this.unificacao = unificacao;
 	}
 
 	public UnificacaoPessoas getUnificacaoPessoa() {
 		return unificacaoPessoa;
 	}
 
 	public void setUnificacaoPessoa(UnificacaoPessoas unificacaoPessoa) {
 		this.unificacaoPessoa = unificacaoPessoa;
 		if(unificacaoPessoa != null) {
 			this.pessoaSecundaria = unificacaoPessoa.getPessoaSecundariaUnificada();
 			this.unificacao = unificacaoPessoa.getUnificacao();
 		}
 	}
 
 	/**
 	 * metodo auxiliar que retorna se a pessoa principal é do tipo fisica
 	 * se unificacao for null retorna false
 	 * @return true / false
 	 * @throws Exception se o tipo de pessoa estiver incorretamente setado no banco de dados
 	 */
 	public boolean isPessoaPrincipalTipoFisica() throws Exception {
 		if(unificacao != null) {
 			if(isTipoPessoaPrincipalCorreto) {
 				return unificacao.getPessoaPrincipal().getInTipoPessoa().equals(TipoPessoaEnum.F);
 			} else {
 				throw new Exception ("tipo de pessoa da pessoa principal incorreto");
 			}
 		}else {
 			return false;
 		}
 	}
 	
 	/**
 	 * metodo auxiliar que retorna se a pessoa principal é do tipo juridica
 	 * se unificacao for null retorna false
 	 * @return true / false
 	 * @throws Exception se o tipo de pessoa estiver incorretamente setado no banco de dados
 	 */
 	public boolean isPessoaPrincipalTipoJuridica() throws Exception {
 		if(unificacao != null) {
 			if(isTipoPessoaPrincipalCorreto) {
 				return unificacao.getPessoaPrincipal().getInTipoPessoa().equals(TipoPessoaEnum.J);
 			} else {
 				throw new Exception ("tipo de pessoa da pessoa principal incorreto");
 			}
 		}else {
 			return false;
 		}
 	}
 	
 	/**
 	 * metodo auxiliar que retorna se a pessoa principal é do tipo autoridade
 	 * se unificacao for null retorna false
 	 * @return true / false
 	 * @throws Exception se o tipo de pessoa estiver incorretamente setado no banco de dados
 	 */
 	public boolean isPessoaPrincipalTipoAutoridade() throws Exception {
 		if(unificacao != null) {
 			if(isTipoPessoaPrincipalCorreto) {
 				return unificacao.getPessoaPrincipal().getInTipoPessoa().equals(TipoPessoaEnum.A);
 			} else {
 				throw new Exception ("tipo de pessoa da pessoa principal incorreto");
 			}
 		}else {
 			return false;
 		}
 	}
 
 	/**
 	 * metodo auxiliar que retorna se a pessoa secundaria é do tipo fisica
 	 * se pessoaSecundaria for null retorna false
 	 * @return true / false
 	 * @throws Exception se o tipo de pessoa estiver incorretamente setado no banco de dados
 	 */
 	public boolean isPessoaSecundariaTipoFisica() throws Exception {
 		if(pessoaSecundaria != null) {
 			if(isTipoPessoaSecundariaCorreto) {
 				return pessoaSecundaria.getInTipoPessoa().equals(TipoPessoaEnum.F);
 			}else {
 				throw new Exception ("tipo de pessoa da pessoa secundaria incorreto");
 			}
 		}else {
 			return false;
 		}
 	}
 	
 	/**
 	 * metodo auxiliar que retorna se a pessoa secundaria é do tipo juridica
 	 * se pessoaSecundaria for null retorna false
 	 * @return true / false
 	 * @throws Exception 
 	 */
 	public boolean isPessoaSecundariaTipoJuridica() throws Exception {
 		if(pessoaSecundaria != null) {
 			if(isTipoPessoaSecundariaCorreto) {
 				return pessoaSecundaria.getInTipoPessoa().equals(TipoPessoaEnum.J);
 			} else {
 				throw new Exception ("tipo de pessoa da pessoa secundaria incorreto");
 			}
 		}else {
 			return false;
 		}
 	}
 	
 	/**
 	 * metodo auxiliar que retorna se a pessoa secundaria é do tipo autoridade
 	 * se pessoaSecundaria for null retorna false
 	 * @return true / false
 	 * @throws Exception 
 	 */
 	public boolean isPessoaSecundariaTipoAutoridade() throws Exception {
 		if(pessoaSecundaria != null) {
 			if(isTipoPessoaSecundariaCorreto) {
 				return pessoaSecundaria.getInTipoPessoa().equals(TipoPessoaEnum.A);
 			} else {
 				throw new Exception ("tipo de pessoa da pessoa secundaria incorreto");
 			}
 		}else {
 			return false;
 		}
 	}
 
 	public boolean isTipoPessoaSecundariaCorreto() {
 		return isTipoPessoaSecundariaCorreto;
 	}
 
 	public void setTipoPessoaSecundariaCorreto(boolean isTipoPessoaSecundariaCorreto) {
 		this.isTipoPessoaSecundariaCorreto = isTipoPessoaSecundariaCorreto;
 	}
 
 	public boolean isTipoPessoaPrincipalCorreto() {
 		return isTipoPessoaPrincipalCorreto;
 	}
 
 	public void setTipoPessoaPrincipalCorreto(boolean isTipoPessoaPrincipalCorreto) {
 		this.isTipoPessoaPrincipalCorreto = isTipoPessoaPrincipalCorreto;
 	}

	public List<UnificacaoPessoasObjeto> getLogAcessoUnificadosObject() {
		return logAcessoUnificadosObject;
	}
	
	public List<UnificacaoPessoasObjeto> getCaractFisUnificadasObject() {
		return caractFisUnificadasObject;
	}
	
	public List<CaracteristicaFisica> getCaractFisUnificadas() {
		return caractFisUnificadas;
	}

	public List<UnificacaoPessoasObjeto> getCaractFisUnificadasNaoEncontradasObject() {
		return caractFisUnificadasNaoEncontradasObject;
	}
	
	public List<UnificacaoPessoasObjeto> getMeioContatoCadastradoUnificadoNaoEncontradoObject() {
		return meiosContatoCadastradosUnificadosAlteradosOuNaoEncontradasObject;
	}
	
	public List<UnificacaoPessoasObjeto> getMeiosContatoCadastradosUnificadosObject() {
		return meiosContatoCadastradosUnificadosObject;
	}
	
	public List<UnificacaoPessoasObjeto> getMeiosContatoProprietariasUnificadosObject() {
		return meiosContatoProprietariasUnificadosObject;
	}
	
	public List<UnificacaoPessoasObjeto> getMeiosContatoProprietariasUnificadosAlteradosOuNaoEncontradasObject() {
		return meiosContatoProprietariasUnificadosAlteradosOuNaoEncontradasObject;
	}
	
	/**
	 * metodo responsavel por inserir na lista de logs de acesso unificados a UnificacaoPessoasObjeto.
	 * faz uma verificacao anterior para certificar que existe somente uma instancia na lista.
	 * @param unifPessObj
	 */
	public void adicionarLogAcessoUnificadoObject(UnificacaoPessoasObjeto unifPessObj) {
		if(!logAcessoUnificadosObject.contains(unifPessObj)) {
			logAcessoUnificadosObject.add(unifPessObj);
		}
	}
	
	/**
	 * metodo responsavel por inserir na lista de coneexao de prevencao unificados.
	 * @param unifPessObj
	 */
	public void adicionarConexaoProcessosUnificadoObject(UnificacaoPessoasObjeto unifPessObj) {
		if(!conexoesPrevencaoUnificadosObject.contains(unifPessObj)) {
			conexoesPrevencaoUnificadosObject.add(unifPessObj);
		}
	}
	
	public void adicionarSegredosProcessosUnificadoObject(UnificacaoPessoasObjeto unifPessObj) {
		if(!segredosProcessosUnificadosObject.contains(unifPessObj)) {
			segredosProcessosUnificadosObject.add(unifPessObj);
		}
	}
	
	public void adicionarSigiloProcessosPartesUnificadoObject(UnificacaoPessoasObjeto unifPessObj) {
		if(!sigiloProcessosParteUnificadosObject.contains(unifPessObj)) {
			sigiloProcessosParteUnificadosObject.add(unifPessObj);
		}
	}
	
	public void adicionarCaixaRepresentanteUnificadoObject(UnificacaoPessoasObjeto unifPessObj) {
		if(!caixaRepresentanteUnificadosObject.contains(unifPessObj)) {
			caixaRepresentanteUnificadosObject.add(unifPessObj);
		}
	}
	
	public void adicionarRedistribuicaoProcessoUnificadoObject(UnificacaoPessoasObjeto unifPessObj) {
		if(!redistribuicaoProcessosUnificadosObject.contains(unifPessObj)) {
			redistribuicaoProcessosUnificadosObject.add(unifPessObj);
		}
	}
	
	public void adicionarProcessoParteHistoricoUnificadoObject(UnificacaoPessoasObjeto unifPessObj) {
		if(!processoParteHistoricosUnificadosObject.contains(unifPessObj)) {
			processoParteHistoricosUnificadosObject.add(unifPessObj);
		}
	}
	
	public void adicionarProcessoTagUnificadoObject(UnificacaoPessoasObjeto unifPessObj) {
		if(!processosTagsUnificadosObject.contains(unifPessObj)) {
			processosTagsUnificadosObject.add(unifPessObj);
		}
	}
	
	public void adicionarLembreteUnificadoObject(UnificacaoPessoasObjeto unifPessObj) {
		if(!lembretesUnificadosObject.contains(unifPessObj)) {
			lembretesUnificadosObject.add(unifPessObj);
		}
	}
	
	public void adicionarPermissaoLembreteUnificadoObject(UnificacaoPessoasObjeto unifPessObj) {
		if(!permissaoLembretesUnificadosObject.contains(unifPessObj)) {
			permissaoLembretesUnificadosObject.add(unifPessObj);
		}
	}
	
	public void adicionarProcessosUnificadoObject(UnificacaoPessoasObjeto unifPessObj) {
		if(!processosUnificadosObject.contains(unifPessObj)) {
			processosUnificadosObject.add(unifPessObj);
		}
	}
	
	public void adicionarParametrosUnificadoObject(UnificacaoPessoasObjeto unifPessObj) {
		if(!parametrosUnificadosObject.contains(unifPessObj)) {
			parametrosUnificadosObject.add(unifPessObj);
		}
	}
	
	public void adicionarEntityLogsUnificadoObject(UnificacaoPessoasObjeto unifPessObj) {
		if(!entityLogsUnificadosObject.contains(unifPessObj)) {
			entityLogsUnificadosObject.add(unifPessObj);
		}
	}
	
	public void adicionarSolicitacoesNoDesvioUnificadoObject(UnificacaoPessoasObjeto unifPessObj) {
		if(!solicitacoesNoDesvioUnificadasObject.contains(unifPessObj)) {
			solicitacoesNoDesvioUnificadasObject.add(unifPessObj);
		}
	}
	
	public void adicionarSessaoPautaProcessoInclusoraUnificadoObject(UnificacaoPessoasObjeto unifPessObj) {
		if(!sessaoPautaProcessoInclusoraUnificadasObject.contains(unifPessObj)) {
			sessaoPautaProcessoInclusoraUnificadasObject.add(unifPessObj);
		}
	}
	
	public void adicionarSessaoPautaProcessoExclusoraUnificadoObject(UnificacaoPessoasObjeto unifPessObj) {
		if(!sessaoPautaProcessoExclusoraUnificadasObject.contains(unifPessObj)) {
			sessaoPautaProcessoExclusoraUnificadasObject.add(unifPessObj);
		}
	}
	
	public void adicionarSessaoEnteExternoUnificadoObject(UnificacaoPessoasObjeto unifPessObj) {
		if(!sessaoEnteExternoUnificadosObject.contains(unifPessObj)) {
			sessaoEnteExternoUnificadosObject.add(unifPessObj);
		}
	}
	
	public void adicionarSessaoInclusoraUnificadoObject(UnificacaoPessoasObjeto unifPessObj) {
		if(!sessaoInclusoraUnificadasObject.contains(unifPessObj)) {
			sessaoInclusoraUnificadasObject.add(unifPessObj);
		}
	}
	
	public void adicionarSessaoExclusoraUnificadoObject(UnificacaoPessoasObjeto unifPessObj) {
		if(!sessaoExclusoraUnificadasObject.contains(unifPessObj)) {
			sessaoExclusoraUnificadasObject.add(unifPessObj);
		}
	}
	
	public void adicionarAvisoQuadroAvisoUnificadoObject(UnificacaoPessoasObjeto unifPessObj) {
		if(!avisoQuadroAvisoUnificadasObject.contains(unifPessObj)) {
			avisoQuadroAvisoUnificadasObject.add(unifPessObj);
		}
	}
	
	public void adicionarProcessoDocumentoFavoritoUnificadoObject(UnificacaoPessoasObjeto unifPessObj) {
		if(!processosDocumentoFavoritosUnificadosObject.contains(unifPessObj)) {
			processosDocumentoFavoritosUnificadosObject.add(unifPessObj);
		}
	}
	
	public void adicionarNotaSessaoJulgamentoUnificadaObject(UnificacaoPessoasObjeto unifPessObj) {
		if(!notasSessaoJulgamentoUnificadasObject.contains(unifPessObj)) {
			notasSessaoJulgamentoUnificadasObject.add(unifPessObj);
		}
	}
	
	public void adicionarModeloProclamacaoJulgamentoUnificadoObject(UnificacaoPessoasObjeto unifPessObj) {
		if(!modelosProclamacaoJulgamentoUnificadosObject.contains(unifPessObj)) {
			modelosProclamacaoJulgamentoUnificadosObject.add(unifPessObj);
		}
	}
	
	public void adicionarLogHistoricoMovimentacaoUnificadoObject(UnificacaoPessoasObjeto unifPessObj) {
		if(!logsHistoricoMovimentacaoUnificadosObject.contains(unifPessObj)) {
			logsHistoricoMovimentacaoUnificadosObject.add(unifPessObj);
		}
	}
	
	public void adicionarVisibilidadeDocumentoIdentificacaoUnificadaObject(UnificacaoPessoasObjeto unifPessObj) {
		if(!visibilidadesDocIdentificacaoUnificadasObject.contains(unifPessObj)) {
			visibilidadesDocIdentificacaoUnificadasObject.add(unifPessObj);
		}
	}
	
	/**
	 * metodo responsavel por inserir na lista de caracteristicas fisicas unificadas a UnificacaoPessoasObjeto.
	 * faz uma verificacao anterior para certificar que existe somente uma instancia na lista.
	 * @param unifPessObj
	 */
	public void adicionarCaractFisUnificadaObject(UnificacaoPessoasObjeto _unifPessObj) {
		if(!caractFisUnificadasObject.contains(_unifPessObj)) {
			caractFisUnificadasObject.add(_unifPessObj);
		}
	}
	
	/**
	 * metodo responsavel por inserir na lista de meios de contato unificados a UnificacaoPessoasObjeto.
	 * faz uma verificacao anterior para certificar que existe somente uma instancia na lista.
	 * @param _unifPessObj
	 */
	public void adicionarMeioContatoCadastradoUnificadoObject(UnificacaoPessoasObjeto _unifPessObj) {
		if(!meiosContatoCadastradosUnificadosObject.contains(_unifPessObj)) {
			meiosContatoCadastradosUnificadosObject.add(_unifPessObj);
		}
	}
	
	/**
	 * metodo responsavel por inserir na lista de nomes alternativos unificados a UnificacaoPessoaObjeto.
	 * faz uma verificacao anterior para certificar que existe somente uma instancia na lista.
	 * @param unifPessObj
	 */
	public void adicionarNomeAlternativoCadastradoUnificadoObject(UnificacaoPessoasObjeto _unifPessObj) {
		if(!nomesAlternativosCadastradosUnificadosObject.contains(_unifPessObj)) {
			nomesAlternativosCadastradosUnificadosObject.add(_unifPessObj);
		}
	}
	
	public void adicionarMeioContatoProprietariasUnificadoObject(UnificacaoPessoasObjeto _unifPessObj) {
		if(!meiosContatoProprietariasUnificadosObject.contains(_unifPessObj)) {
			meiosContatoProprietariasUnificadosObject.add(_unifPessObj);
		}
	}
	
	public void adicionarNomeAlternativoProprietariasUnificadoObject(UnificacaoPessoasObjeto _unifPessObj) {
		if(!nomesAlternativosProprietariasUnificadosObject.contains(_unifPessObj)) {
			nomesAlternativosProprietariasUnificadosObject.add(_unifPessObj);
		}
	}
	
	public List<UnificacaoPessoasObjeto> getNomesAlternativosCadastradosUnificadosObject() {
		return nomesAlternativosCadastradosUnificadosObject;
	}

	public List<UnificacaoPessoasObjeto> getNomesAlternativosCadastradosUnificadosAlteradosOuNaoEncontradosObject() {
		return nomesAlternativosCadastradosUnificadosAlteradosOuNaoEncontradosObject;
	}

	public List<UnificacaoPessoasObjeto> getNomesAlternativosProprietariasUnificadosObject() {
		return nomesAlternativosProprietariasUnificadosObject;
	}

	public List<UnificacaoPessoasObjeto> getNomesAlternativosProprietariasUnificadosAlteradosOuNaoEncontradasObject() {
		return nomesAlternativosProprietariasUnificadosAlteradosOuNaoEncontradasObject;
	}

	public List<UnificacaoPessoasObjeto> getConexoesPrevencaoUnificadosObject() {
		return conexoesPrevencaoUnificadosObject;
	}
	
	public List<UnificacaoPessoasObjeto> getConexaoProcessoUnificadosNaoEncontradosObject() {
		return conexoesPrevencaoUnificadosNaoEncontradosObject;
	}

	public List<UnificacaoPessoasObjeto> getSegredosProcessosUnificadosObject() {
		return segredosProcessosUnificadosObject;
	}

	public List<UnificacaoPessoasObjeto> getSegredosProcessosUnificadosNaoEncontradosObject() {
		return segredosProcessosUnificadosNaoEncontradosObject;
	}

	public List<UnificacaoPessoasObjeto> getSigiloProcessosParteUnificadosObject() {
		return sigiloProcessosParteUnificadosObject;
	}

	public List<UnificacaoPessoasObjeto> getSigiloProcessosParteUnificadosNaoEncontradosObject() {
		return sigiloProcessosParteUnificadosNaoEncontradosObject;
	}

	public List<UnificacaoPessoasObjeto> getCaixaRepresentanteUnificadosObject() {
		return caixaRepresentanteUnificadosObject;
	}

	public List<UnificacaoPessoasObjeto> getCaixaRepresentanteUnificadosNaoEncontradosObject() {
		return caixaRepresentanteUnificadosNaoEncontradosObject;
	}

	public List<UnificacaoPessoasObjeto> getSessaoEnteExternoUnificadosObject() {
		return sessaoEnteExternoUnificadosObject;
	}

	public List<UnificacaoPessoasObjeto> getRedistribuicaoProcessosUnificadosNaoEncontradosObject() {
		return redistribuicaoProcessosUnificadosNaoEncontradosObject;
	}

	public List<UnificacaoPessoasObjeto> getRedistribuicaoProcessosUnificadosObject() {
		return redistribuicaoProcessosUnificadosObject;
	}

	public List<UnificacaoPessoasObjeto> getProcessoParteHistoricosUnificadosObject() {
		return processoParteHistoricosUnificadosObject;
	}

	public List<UnificacaoPessoasObjeto> getProcessoParteHistoricosUnificadosNaoEncontradosObject() {
		return processoParteHistoricosUnificadosNaoEncontradosObject;
	}
	
	public List<UnificacaoPessoasObjeto> getProcessosTagsUnificadosObject() {
		return processosTagsUnificadosObject;
	}

	public List<UnificacaoPessoasObjeto> getProcessosTagsUnificadosNaoEncontradosObject() {
		return processosTagsUnificadosNaoEncontradosObject;
	}
	
	public List<UnificacaoPessoasObjeto> getLembretesUnificadosObject() {
		return lembretesUnificadosObject;
	}

	public List<UnificacaoPessoasObjeto> getLembretesUnificadosNaoEncontradosObject() {
		return lembretesUnificadosNaoEncontradosObject;
	}
	
	public List<UnificacaoPessoasObjeto> getPermissaoLembretesUnificadosObject() {
		return permissaoLembretesUnificadosObject;
	}

	public List<UnificacaoPessoasObjeto> getPermissaoLembretesUnificadosNaoEncontradosObject() {
		return permissaoLembretesUnificadosNaoEncontradosObject;
	}
	
	public List<UnificacaoPessoasObjeto> getProcessosUnificadosObject() {
		return processosUnificadosObject;
	}

	public List<UnificacaoPessoasObjeto> getProcessosUnificadosNaoEncontradosObject() {
		return processosUnificadosNaoEncontradosObject;
	}
	
	public List<UnificacaoPessoasObjeto> getParametrosUnificadosObject() {
		return parametrosUnificadosObject;
	}

	public List<UnificacaoPessoasObjeto> getParametrosUnificadosNaoEncontradosObject() {
		return parametrosUnificadosNaoEncontradosObject;
	}
	
	public List<UnificacaoPessoasObjeto> getEntityLogsUnificadosObject() {
		return entityLogsUnificadosObject;
	}

	public List<UnificacaoPessoasObjeto> getEntityLogsUnificadosNaoEncontradosObject() {
		return entityLogsUnificadosNaoEncontradosObject;
	}
	
	public List<UnificacaoPessoasObjeto> getSolicitacoesNoDesvioUnificadasObject() {
		return solicitacoesNoDesvioUnificadasObject;
	}

	public List<UnificacaoPessoasObjeto> getSolicitacoesNoDesvioNaoEncontradasObject() {
		return solicitacoesNoDesvioNaoEncontradasObject;
	}
	
	public List<UnificacaoPessoasObjeto> getSessaoPautaProcessoInclusoraUnificadasObject() {
		return sessaoPautaProcessoInclusoraUnificadasObject;
	}

	public List<UnificacaoPessoasObjeto> getSessaoPautaProcessoInclusoraNaoEncontradasObject() {
		return sessaoPautaProcessoInclusoraNaoEncontradasObject;
	}

	public List<UnificacaoPessoasObjeto> getSessaoPautaProcessoExclusoraUnificadasObject() {
		return sessaoPautaProcessoExclusoraUnificadasObject;
	}

	public List<UnificacaoPessoasObjeto> getSessaoPautaProcessoExclusoraNaoEncontradasObject() {
		return sessaoPautaProcessoExclusoraNaoEncontradasObject;
	}
	
	public List<UnificacaoPessoasObjeto> getSessaoInclusoraUnificadasObject() {
		return sessaoInclusoraUnificadasObject;
	}

	public List<UnificacaoPessoasObjeto> getSessaoInclusoraNaoEncontradasObject() {
		return sessaoInclusoraNaoEncontradasObject;
	}

	public List<UnificacaoPessoasObjeto> getSessaoExclusoraUnificadasObject() {
		return sessaoExclusoraUnificadasObject;
	}

	public List<UnificacaoPessoasObjeto> getSessaoExclusoraNaoEncontradasObject() {
		return sessaoExclusoraNaoEncontradasObject;
	}
	
	public List<UnificacaoPessoasObjeto> getAvisoQuadroAvisoUnificadasObject() {
		return avisoQuadroAvisoUnificadasObject;
	}

	public List<UnificacaoPessoasObjeto> getAvisoQuadroAvisoNaoEncontradasObject() {
		return avisoQuadroAvisoNaoEncontradasObject;
	}

	public List<UnificacaoPessoasObjeto> getProcessosDocumentoFavoritosUnificadosObject() {
		return processosDocumentoFavoritosUnificadosObject;
	}

	public List<UnificacaoPessoasObjeto> getProcessosDocumentoFavoritosNaoEncontradosObject() {
		return processosDocumentoFavoritosNaoEncontradosObject;
	}
	
	public List<UnificacaoPessoasObjeto> getNotasSessaoJulgamentoUnificadasObject() {
		return notasSessaoJulgamentoUnificadasObject;
	}

	public List<UnificacaoPessoasObjeto> getNotasSessaoJulgamentoNaoEncontradasObject() {
		return notasSessaoJulgamentoNaoEncontradasObject;
	}
	
	public List<UnificacaoPessoasObjeto> getModelosProclamacaoJulgamentoUnificadosObject() {
		return modelosProclamacaoJulgamentoUnificadosObject;
	}

	public List<UnificacaoPessoasObjeto> getModelosProclamacaoJulgamentoNaoEncontradosObject() {
		return modelosProclamacaoJulgamentoNaoEncontradosObject;
	}

	public List<UnificacaoPessoasObjeto> getLogsHistoricoMovimentacaoUnificadosObject() {
		return logsHistoricoMovimentacaoUnificadosObject;
	}

	public List<UnificacaoPessoasObjeto> getLogsHistoricoMovimentacaoNaoEncontradosObject() {
		return logsHistoricoMovimentacaoNaoEncontradosObject;
	}

	public UsuarioLogin getUsuarioLoginPessoaSecundaria() {
		return usuarioLoginPessoaSecundaria;
	}

	public void setUsuarioLoginPessoaSecundaria(UsuarioLogin usuarioLoginPessoaSecundaria) {
		this.usuarioLoginPessoaSecundaria = usuarioLoginPessoaSecundaria;
	}

	public List<UnificacaoPessoasObjeto> getVisibilidadesDocIdentificacaoUnificadasObject() {
		return visibilidadesDocIdentificacaoUnificadasObject;
	}

	public List<UnificacaoPessoasObjeto> getVisibilidadesDocIdentificacaoNaoEncontradasObject() {
		return visibilidadesDocIdentificacaoNaoEncontradasObject;
	}
	
}