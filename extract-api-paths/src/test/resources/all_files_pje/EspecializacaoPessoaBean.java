/**
 * 
 */
package br.jus.cnj.pje.visao.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;

import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.PessoaAdvogadoManager;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaFisicaEspecializada;
import br.jus.pje.nucleo.entidades.PessoaProcurador;

/**
 * Componente de controle da tela pessoa/especializacao/especializacao.xhtml.
 * 
 * @author cristof
 * 
 */
@Name("especializacaoPessoaBean")
@Scope(ScopeType.PAGE)
public class EspecializacaoPessoaBean implements Serializable{

	private static final long serialVersionUID = -8845035515400025963L;

	@Logger
	private Log log;
	
	@RequestParameter
	private Integer idPessoa;

	private List<String> tiposPessoas;

	private String tipoPessoaEspecializada;

	private Map<String, Class<? extends PessoaFisicaEspecializada>> mapaPessoas;
	
	@In
	private transient PessoaService pessoaService;
	
	@In
	private transient PessoaAdvogadoManager pessoaAdvogadoManager;
	
	@In
	private Conversation conversation;
	
	@In
	private FacesMessages facesMessages;
	
	private PessoaFisica pessoa;
	
	@Create
	@Restrict("#{identity.loggedIn}")
	public void init() throws PJeBusinessException{
		if(idPessoa == null || idPessoa < 1){
			throw new PJeBusinessException("É necessário indicar o identificador da pessoa a ser especializada.");
		}else{
			pessoa = (PessoaFisica) pessoaService.findById(idPessoa.intValue());
			if(pessoa == null){
				throw new PJeBusinessException("Não existe pessoa com o identificador indicado.");
			}
		}
		mapaPessoas = new HashMap<String, Class<? extends PessoaFisicaEspecializada>>();
		if(!Pessoa.instanceOf(pessoa, PessoaAdvogado.class)){
			mapaPessoas.put("Advogado", PessoaAdvogado.class);
		}
		if(!Pessoa.instanceOf(pessoa, PessoaProcurador.class)){
			mapaPessoas.put("Procurador", PessoaProcurador.class);
		}
		tiposPessoas = new ArrayList<String>();
		for (String key : mapaPessoas.keySet()) {
			tiposPessoas.add(key);
		}
		tipoPessoaEspecializada = null;
	}

	public EspecializacaoPessoaBean() {
	}
	
	public void especializar(){
		especializar(pessoa);
	}

	public void especializar(PessoaFisica pessoa) {
		log.info("Especializando ...");
//		if(conversation != null){
//			conversation.changeFlushMode(FlushModeType.AUTO);
//		}
		Class<? extends PessoaFisicaEspecializada> clazz = null;
		if (tipoPessoaEspecializada == null) {
			facesMessages.add(Severity.ERROR, "O tipo de pessoa especializada não foi selecionado.");
			return;
		}
		clazz = mapaPessoas.get(tipoPessoaEspecializada);
		if (pessoa != null){
			try {
				if(PessoaAdvogado.class.isAssignableFrom(clazz)){
					acrescentaPerfilAdvogado(pessoa);
				}else if(PessoaProcurador.class.isAssignableFrom(clazz)){
					acrescentaPerfilProcurador(pessoa);
				}
				
			} catch (PJeBusinessException e) {
				log.error("Erro ao tentar realizar o acúmulo de perfis: {0}.", e.getLocalizedMessage());
			}
		}
	}
		
	@SuppressWarnings("unchecked")
	public void acrescentaPerfilAdvogado(PessoaFisica pessoa) throws PJeBusinessException {
		log.info("Acrescentando perfil advogado para [{0}].", pessoa);
		pessoaService.excluirJusPostulandi(pessoa);
		pessoa = (PessoaFisica) pessoaService.especializa(pessoa, PessoaAdvogado.class);
		log.info("Concluído o acréscimo.");
	}

	@SuppressWarnings("unchecked")
	public void acrescentaPerfilProcurador(PessoaFisica pessoa) throws PJeBusinessException {
		log.info("Acrescentando perfil procurador para [{0}].", pessoa);
		pessoaService.excluirJusPostulandi(pessoa);
		pessoa = (PessoaFisica) pessoaService.especializa(pessoa, PessoaProcurador.class);
		log.info("Concluído o acréscimo.");
	}

	public void cancelarEspecializacao() {
		log.info("Cancelando especialização.");
	}

	public List<String> getTiposPessoas() {
		return tiposPessoas;
	}

	public void setTiposPessoas(List<String> tiposPessoas) {
		this.tiposPessoas = tiposPessoas;
	}

	public String getTipoPessoaEspecializada() {
		return tipoPessoaEspecializada;
	}

	public void setTipoPessoaEspecializada(String tipoPessoaEspecializada) {
		this.tipoPessoaEspecializada = tipoPessoaEspecializada;
	}
	
	public void atribuirJusPostulandi(){
		try {
			if(conversation != null){
				conversation.changeFlushMode(FlushModeType.AUTO);
			}
			pessoa = (PessoaFisica) pessoaService.findById(pessoa.getIdUsuario());
			especializarJusPostulandi(pessoa);
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Erro ao tentar atribuir o perfil de Jus Postulandi para [{0}].", pessoa);
		}
	}
	
	private void especializarJusPostulandi(PessoaFisica pessoa) {
		log.info("Acrescentando perfil jus postulandi para a pessoa " + pessoa.getNome() + ".");
		try {
			pessoaService.tornaJusPostulandi(pessoa);
		} catch (PJeBusinessException e) {
			log.error("Erro ao tentar acrescentar o pefil de Jus Postulandi para {0}: {1}.", pessoa, e.getLocalizedMessage());
		}
		log.info("Concluído o acréscimo.");
	}

	public PessoaFisica getPessoa() {
		return pessoa;
	}

}
