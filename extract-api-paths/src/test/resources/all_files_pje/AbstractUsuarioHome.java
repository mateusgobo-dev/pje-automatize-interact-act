/*
 * IBPM - Ferramenta de produtividade Java Copyright (c) 1986-2009 Infox Tecnologia da Informação Ltda. Este programa é software livre; você pode
 * redistribuí-lo e/ou modificá-lo sob os termos da GNU GENERAL PUBLIC LICENSE (GPL) conforme publicada pela Free Software Foundation; versão 2 da
 * Licença. Este programa é distribuído na expectativa de que seja útil, porém, SEM NENHUMA GARANTIA; nem mesmo a garantia implícita de
 * COMERCIABILIDADE OU ADEQUAÇÃO A UMA FINALIDADE ESPECÍFICA. Consulte a GNU GPL para mais detalhes. Você deve ter recebido uma cópia da GNU GPL junto
 * com este programa; se não, veja em http://www.gnu.org/licenses/
 */
package br.com.infox.ibpm.home;

import java.util.List;
import br.com.itx.component.AbstractHome;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.view.PjeUtil;
import br.jus.pje.nucleo.entidades.BloqueioUsuario;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;

public abstract class AbstractUsuarioHome<T> extends AbstractHome<Usuario>{

	private static final long serialVersionUID = 1L;

	public void setUsuarioIdUsuario(Integer id){
		setId(id);
	}

	public Integer getUsuarioIdUsuario(){
		return (Integer) getId();
	}

	@Override
	protected Usuario createInstance(){
		return new Usuario();
	}

	@Override
	public String remove(Usuario obj){
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("usuarioGrid");
		return ret;
	}

	public List<Endereco> getEnderecoList(){
		return getInstance() == null ? null : getInstance().getEnderecoList();
	}

	public List<UsuarioLocalizacao> getUsuarioLocalizacaoList(){
		return getInstance() == null ? null : getInstance().getUsuarioLocalizacaoList();
	}

	public List<BloqueioUsuario> getBloqueioUsuarioList(){
		return getInstance() == null ? null : getInstance().getBloqueioUsuarioList();
	}
	
	public void gerarNovaSenha(){
		if(getInstance().getEmail() == null) {
			reportMessage("pje.usuarioService.error.informeEmailUsuario", null, getInstance().getNome());		
			return;
		}
		getInstance().setHashAtivacaoSenha(PjeUtil.instance().gerarHashAtivacao(getInstance().getLogin()));
		persist();
		enviarNovaSenha();
	}
	
	/**
	 * Metodo responsavel por enviar a nova senha por email.
	 */
	private void enviarNovaSenha() {
		try {
			UsuarioService usuarioService = getComponent("usuarioService");
			usuarioService.enviarEmailSenha(getInstance());
			reportMessage("pje.pessoaFisicaHome.info.emailEnviadoComSucesso", null, getInstance().getEmail());
		} catch (PJeBusinessException e) {
			reportMessage(e);
		}
	}
}