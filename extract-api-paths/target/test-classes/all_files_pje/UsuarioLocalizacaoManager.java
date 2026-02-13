/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.UsuarioLocalizacaoComparator;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.UsuarioLocalizacaoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.PessoaServidor;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

/**
 * Componente de gerenciamento da entidade {@link UsuarioLocalizacao}.
 * 
 * @author cristof
 *
 */
@Name(UsuarioLocalizacaoManager.NAME)
public class UsuarioLocalizacaoManager extends BaseManager<UsuarioLocalizacao> {
	
	public static final String NAME = "usuarioLocalizacaoManager";
	
	@In
	private UsuarioLocalizacaoDAO usuarioLocalizacaoDAO;
	
	/**
     * @return Instância da classe.
     */
    public static UsuarioLocalizacaoManager instance() {
        return ComponentUtil.getComponent(NAME);
    }
    
	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.manager.BaseManager#getDAO()
	 */
	@Override
	protected UsuarioLocalizacaoDAO getDAO() {
		return usuarioLocalizacaoDAO;
	}
	
	/**
	 * Recupera todas as localizações pessoais de uma dada pessoa.
	 * 
	 * @param pessoa a pessoa cujas localizações se pretende obter
	 * @return a lista de localizações
	 */
	public List<UsuarioLocalizacao> getLocalizacoesAtuais(Usuario usuario){
		List<UsuarioLocalizacao> usuarioLocalizacaoList = usuarioLocalizacaoDAO.getLocalizacoesAtuais(usuario);
		
		if(CollectionUtilsPje.isNotEmpty(usuarioLocalizacaoList)){
			Collections.sort(usuarioLocalizacaoList, new UsuarioLocalizacaoComparator());
		}

		return usuarioLocalizacaoList;
	}
	
	/**
	 * Recupera a lista de localizações ativas da pessoa indicada, quando vinculadas a um dado papel.
	 * 
	 * @param pessoa a pessoa cujas localizações se pretende obter
	 * @param papel o papel de interesse
	 * @return a lista de localizações
	 */
	public List<UsuarioLocalizacao> getLocalizacoesAtuais(Pessoa pessoa, Papel papel){
		return usuarioLocalizacaoDAO.getLocalizacoesAtuais(pessoa, papel);
	}
	
	public List<UsuarioLocalizacao> getLocalizacoesAtuaisMagistrado(Usuario usuario){
		return usuarioLocalizacaoDAO.getLocalizacoesAtuaisMagistrado(usuario);
	}
	
	public List<UsuarioLocalizacao> getLocalizacoesAtuais(Pessoa pessoa, Papel papel,Localizacao l){
		return usuarioLocalizacaoDAO.getLocalizacoesAtuais(pessoa, papel,l);
	}
	
	/**
	 * Método responsável por obter uma lista de {@link UsuarioLocalizacao} de
	 * uma determinada {@link Pessoa} a partir da sua lista de papéis.
	 * 
	 * @param pessoa
	 *            a pessoa cujas localizações se pretende obter
	 * @param papeis
	 *            os papéis de interesse
	 * @return <code>List<code>, de localizações
	 */
	public List<UsuarioLocalizacao> getLocalizacoesAtuais(Pessoa pessoa, List<Papel> papeis){
		return usuarioLocalizacaoDAO.getLocalizacoesAtuais(pessoa, papeis);
	}

	/**
	 * Recupera a localização indicada da pessoa, se existente.
	 * 
	 * @param pessoa a pessoa cuja localização se pretende recuperar
	 * @param loc a localização específica
	 * @return a {@link UsuarioLocalizacao} específica, ou null se ela não existir.
	 */
	public UsuarioLocalizacao getLocalizacao(PessoaFisica pessoa, Localizacao loc, Papel papel) {
		return usuarioLocalizacaoDAO.getLocalizacao(pessoa, loc, papel);
	}
	
	/**
	 * Verifica se a pessoa indicada possui uma localizacao diferente da indicada
	 * - se a pessoa não tiver localizacao, retorna false
	 * - se a pessoa tiver apenas a localizacao indicada, retorna false
	 * - se a pessoa tiver outra localizacao indicada, retorna true 
	 * @param pessoa
	 * @param localizacao
	 * @return
	 */
	public boolean verificaOutraLocalizacaoAssociada(Pessoa pessoa, Localizacao localizacao) {
		boolean encontrou = false;
		List<UsuarioLocalizacao> localizacoesPessoa = this.getLocalizacoesAtuais(pessoa);
		for (UsuarioLocalizacao usuarioLocalizacao : localizacoesPessoa) {
			if(localizacao == null || usuarioLocalizacao.getLocalizacaoFisica() != localizacao) {
				encontrou = true;
				break;
			}
		}
		return encontrou;
	}
	
	/**
	 * Indica se a pessoa indicada mantém o papel indicado em razão de alguma de suas
	 * localizações pessoais.
	 * 
	 * @param pessoa a pessoa a respeito da qual se pretende recuperar a informação
	 * @param papel o papel que se pretende investigar
	 * @return true, se houver pelo menos uma localização pessoal que ostente o papel indicado
	 */
	public boolean mantemPapel(Pessoa pessoa, Papel papel) throws PJeBusinessException {
		return usuarioLocalizacaoDAO.mantemPapel(pessoa, papel);
	}
	
	/**
	 * Recupera o identificador do usuário advogado ao qual está vinculado o assistente informado, na respectiva
	 * localização.
	 * 
	 * @param assistente o assistente cujo advogado assistido se pretende descobrir
	 * @param local a localização atual do assistente pesquisado
	 * @return o identificador do advogado assistido
	 * @throws PJeBusinessException caso haja algum erro ao tentar recuperar a informação
	 */
	public Integer getAdvogadoVinculado(Integer idAssistenteAdvogado, UsuarioLocalizacao local) throws PJeBusinessException {
		if(!idAssistenteAdvogado.equals(local.getUsuario().getIdUsuario())){
			throw new IllegalArgumentException("Não se pode tentar recuperar o advogado assistido se a localização paradigma não pertence ao assistente.");
		}
		Search s = new Search(UsuarioLocalizacao.class);
		s.setRetrieveField("usuario.idUsuario");
		s.setMax(1);
		addCriteria(s, 
				Criteria.not(Criteria.equals("usuario.idUsuario", idAssistenteAdvogado)), // ul que não pertence ao assistente informado
				Criteria.equals("responsavelLocalizacao", true), // que é ocupada pelo responsável pela localização
				Criteria.equals("localizacaoFisica", local.getLocalizacaoFisica())); // e que tem a mesma localização que a dada
		List<Integer> ids = list(s);
		return ids.isEmpty() ? null : ids.get(0);
	}
	
	/**
	 * Este método recupera a lista de localizações do servidor informado
	 * @param pessoaServidor
	 * @return
	 */
	public List<UsuarioLocalizacao> getUsuarioLocalizacaoServidor(PessoaServidor pessoaServidor) {
		Search s = new Search(UsuarioLocalizacaoMagistradoServidor.class);
		s.setRetrieveField("usuarioLocalizacao");
		addCriteria(s, Criteria.equals("usuarioLocalizacao.usuario.idUsuario", pessoaServidor.getIdUsuario()));
		addCriteria(s, Criteria.notEquals("usuarioLocalizacao.papel", new ParametroUtil().getPapelMagistrado()));
		return list(s);
	}
	
	/**
	 * Consulta as localizações onde o magistrado é atuante, ou seja, serão retornadas todas as 
	 * localizações onde o cargo é diferente de nulo, pois se o cargo é diferente de nulo então 
	 * a localização foi atribuída devido ao cadastro do magistrado no Orgão Julgador.
	 * 
	 * @param magistrado PessoaMagistrado
	 * @return localizações.
	 */
	public List<UsuarioLocalizacao> consultarLocalizacoesDeMagistradoAtuante(PessoaMagistrado magistrado){
		List<UsuarioLocalizacao> resultado = null;
		if (magistrado != null) {
			resultado = getDAO().consultarLocalizacoesDeMagistradoAtuante(magistrado);
		}
		return resultado;
	}

	/**
	 * Recupera todas as localizações do usuário.
	 * 
	 * @param idUsuario Identificador do usuário.
	 * @return As localizações do usuário.
	 */
	public List<UsuarioLocalizacao> recuperarLocalizacoes(Integer idUsuario)  {
		return getDAO().recuperarLocalizacoes(idUsuario);
	}
	
	/**
 	 * Metodo que verifica se o usuário logado é magistrado auxiliar
 	 * @return Boolean 
 	 */
 	public Boolean isMagistradoAuxiliar(){
 		return getDAO().isMagistradoAuxiliar();
	}

	public UsuarioLocalizacao findByUsuarioLocalizacaoPapel(Usuario usuario, Localizacao loc, Papel papel) {
		return usuarioLocalizacaoDAO.findByUsuarioLocalizacaoPapel(usuario, loc, papel);
	}
	
	public List<UsuarioLocalizacao> consultarUsuarioLocalizacaoPorPapelHerdado(Papel papel){
		return this.getDAO().consultarUsuarioLocalizacaoPorPapelHerdado(papel);
	}

	/**
	 * Consulta as pessoas jurídicas que possuam localização e papel passados por parâmetro.
	 * 
	 * @param localizacao
	 * @param papel
	 * @return Coleção de localizações do usuário.
	 */
	public List<UsuarioLocalizacao> findByPessoaJuridicaLocalizacaoPapel(Localizacao localizacao, Papel papel) {
		return usuarioLocalizacaoDAO.findByTipoPessoaLocalizacaoPapel(TipoPessoaEnum.J, localizacao, papel);
	}
	
	/**
	 * Associa um UsuarioLocalizacao para a Pessoa Jurídica passada por parâmetro.
	 * A localização padrão é a localização da procuradoria.
	 * O papel padrão é o 'procuradorChefe'.
	 * 
	 * @param procuradoria Procuradoria
	 * @param removerPapeis Se true serão removidos os usuários/localizações do papel/localização padrão.
	 */
	public void associarLocalizacaoParaRemessa(Procuradoria procuradoria, Boolean removerPapeis) {
		if (procuradoria != null) {
			Papel papelPadrao = ParametroUtil.instance().getPapelProcuradorChefe();
			Localizacao localizacaoPadrao = procuradoria.getLocalizacao();

			associarLocalizacaoParaRemessa(
				procuradoria.getPessoaJuridica(), 
				localizacaoPadrao, 
				papelPadrao, 
				true);
		}
	}
	
	/**
	 * Associa um UsuarioLocalizacao para a Pessoa Jurídica passada por parâmetro.
	 * A localização padrão é a do Tribunal.
	 * O papel padrão é o configurado na tabela de parâmetros ou 'servidor'.
	 * 
	 * @param pessoaJuridica Pessoa Jurídica
	 * @param removerPapeis Se true serão removidos os usuários/localizações do papel/localização padrão.
	 */
	public void associarLocalizacaoParaRemessa(PessoaJuridica pessoaJuridica, Boolean removerPapeis) {
		if (pessoaJuridica != null && pessoaJuridica.getIdPessoaJuridica() != null) {
			Papel papelServidor = ParametroUtil.instance().getPapelServidor();
			Papel papelConfigurado = ParametroUtil.instance().getPapelParaRemessaViaPessoaJuridica();
			Papel papelPadrao = ObjectUtils.firstNonNull(papelConfigurado, papelServidor);
			
			Localizacao localizacaoPadrao = ParametroUtil.instance().getLocalizacaoTribunal();
			associarLocalizacaoParaRemessa(
				pessoaJuridica, 
				localizacaoPadrao, 
				papelPadrao, 
				true);
		}
	}
	
	/**
	 * Retorna true se existir localização para a pessoa jurídica passada por parâmetro. 
	 * A localização padrão usada na pesquisa é a do Tribunal e o papel é o configurado na tabela de parâmetros ou 'servidor.
	 * 
	 * @param pessoaJuridica Pessoa Jurídica.
	 * @return Booleano.
	 */
	public Boolean isExisteLocalizacaoParaRemessa(PessoaJuridica pessoaJuridica) {
		Boolean resultado = Boolean.FALSE;
		
		if (pessoaJuridica != null && pessoaJuridica.getIdPessoaJuridica() != null) {
			Papel papelServidor = ParametroUtil.instance().getPapelServidor();
			Papel papelConfigurado = ParametroUtil.instance().getPapelParaRemessaViaPessoaJuridica();
			Papel papelPadrao = ObjectUtils.firstNonNull(papelConfigurado, papelServidor);
			
			Localizacao localizacaoPadrao = ParametroUtil.instance().getLocalizacaoTribunal();
			
			UsuarioLocalizacao usuarioLocalizacao = findByUsuarioLocalizacaoPapel(
				pessoaJuridica, 
				localizacaoPadrao, 
				papelPadrao);
			resultado = (usuarioLocalizacao != null);
		}
		return resultado;
	}
	
	/**
	 * Associa um UsuarioLocalizacao para a Pessoa Jurídica passada por parâmetro.
	 * A pessoa jurídica passada por parâmetro precisa ter ID e 'associarPapelParaRemessa' = true.
	 * 
	 * @param pessoaJuridica Pessoa Jurídica
	 * @param localizacaoPadrao Localização
	 * @param papelPadrao Papel
	 * @param removerPapeis Se true serão removidos os usuários/localizações do papel/localização padrão.
	 */
	protected void associarLocalizacaoParaRemessa(PessoaJuridica pessoaJuridica, Localizacao localizacaoPadrao, Papel papelPadrao, Boolean removerPapeis) {
		try {
			if (BooleanUtils.isTrue(removerPapeis)) {
				removerPessoaJuridica(localizacaoPadrao, papelPadrao);
			}

			if (pessoaJuridica != null && pessoaJuridica.getIdPessoaJuridica() != null && pessoaJuridica.getAssociarPapelParaRemessa()) {
				UsuarioLocalizacao usuarioLocalizacao = new UsuarioLocalizacao();
				usuarioLocalizacao.setUsuario(pessoaJuridica);
				usuarioLocalizacao.setPapel(papelPadrao);
				usuarioLocalizacao.setLocalizacaoFisica(localizacaoPadrao);
				usuarioLocalizacao.setResponsavelLocalizacao(Boolean.FALSE);
				persist(usuarioLocalizacao);
			}
			
			flush();
		} catch (PJeBusinessException e) {
			throw new AplicationException(e);
		}
	}
	
	/**
	 * Remove todas as pessoas jurídicas da localização e papel informados.
	 * 
	 * @param localizacao
	 * @param papel
	 */
	protected void removerPessoaJuridica(Localizacao localizacao, Papel papel) {
		try {
			List<UsuarioLocalizacao> localizacoes = findByPessoaJuridicaLocalizacaoPapel(localizacao, papel);
			for (UsuarioLocalizacao _localizacao : localizacoes) {
					remove(_localizacao);
			}
		} catch (PJeBusinessException e) {
			throw new AplicationException(e);
		}
	}
}
