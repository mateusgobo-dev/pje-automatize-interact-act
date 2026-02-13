package br.jus.cnj.pje.nucleo.manager;

import org.jboss.seam.annotations.In;

import br.com.infox.pje.manager.PessoaFisicaManager;
import br.jus.cnj.pje.business.dao.AbstractPessoaFisicaEspecializadaDAO;
import br.jus.cnj.pje.business.dao.ModeloDocumentoDAO;
import br.jus.cnj.pje.business.dao.ParametroDAO;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.view.PjeUtil;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaFisicaEspecializada;
import br.jus.pje.nucleo.enums.StatusSenhaEnum;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;

public abstract class AbstractPessoaFisicaEspecializadaManager<E extends PessoaFisicaEspecializada, D extends AbstractPessoaFisicaEspecializadaDAO<E>>
		extends BaseManager<E> {
	
	public abstract E especializa(PessoaFisica pessoa) throws PJeBusinessException;
	public abstract E desespecializa(PessoaFisica pessoa) throws PJeBusinessException;
	
	@In
	private ParametroDAO parametroDAO;
	
	@In
	private ModeloDocumentoDAO modeloDocumentoDAO;
	
	@In
	private PessoaFisicaManager pessoaFisicaManager;

	
	@SuppressWarnings("unchecked")
	public E findByLogin(String login){
		return ((D)getDAO()).findByLogin(login);
	}
	
	@SuppressWarnings("unchecked")
	public E findByCPF(String cpf){
		cpf = InscricaoMFUtil.retiraMascara(cpf);
		return ((D)getDAO()).findByCPF(cpf);
	}
	
	@SuppressWarnings("unchecked")
	public E findByNome(String nome) {
		return ((D)getDAO()).findByNome(nome);
	}
	
	@SuppressWarnings("unchecked")
	public Boolean checkCPF(String cpf, Integer idPessoaFisica){		
		return ((D)getDAO()).checkCPF(cpf, idPessoaFisica); 
	}
	
	@SuppressWarnings("unchecked")
	public Boolean checkLogin(String login, Integer idPessoaFisica){
		return ((D)getDAO()).checkLogin(login, idPessoaFisica);
	}
	
	@SuppressWarnings("unchecked")
	public E persist(E entity) throws PJeBusinessException {
		String cpf = InscricaoMFUtil.retiraMascara(entity.getNumeroCPF());
		
		if(cpf == null || cpf.trim().isEmpty()){
			throw new PJeBusinessException("pje.abstractPessoaFisicaEspecializadaManager.informeCPF");
		}
		
		if(entity.getNome() == null || entity.getNome().trim().isEmpty()){
			throw new PJeBusinessException("pje.manager.abstractUsarioManager.informeNomeUsuario");
		}
		
		//se inserindo
		if(entity.getIdUsuario() == null){
			//TODO ver se o ID  aser verificado eh o do PessoaFisicaEspecializada ou o do field PessoaFisica
			//verifica se CPF ja foi cadastrado
			if(findByCPF(cpf) != null){
				throw new PJeBusinessException("pje.abstractPessoaFisicaEspecializadaManager.cpfEmUso", null, cpf);
			}
			
			//como o login sera igual ao cpf, verifica se login ja existe
			Boolean loginDisponivel = checkLogin(cpf, entity.getIdUsuario());
			if(!loginDisponivel){
				throw new PJeBusinessException("pje.abstractPessoaFisicaEspecializadaManager.loginEmUso", null, cpf);
			}
			
			//login sera sempre o CPF
			if(entity.getLogin() == null || entity.getLogin().trim().isEmpty() || !entity.getLogin().equals(cpf)){
				entity.setLogin(cpf);
			}
			
			//gera-se nova senha e permite envio do link de ativacao p/ email
			setaDadosUsuarioSenha(entity, cpf);
		}else{
			E pf = ((D)getDAO()).find(entity.getIdUsuario());
			String oldCpf = InscricaoMFUtil.retiraMascara(pf.getNumeroCPF());
			
			//previnindo duplicidade de CPF se o usuario alterar o CPF da pessoa
			if(!cpf.equals(oldCpf)){
				
				//verifica se o CPF esta em uso por outra pessoa que nao a entity
				if(checkCPF(cpf, entity.getIdUsuario())){
					throw new PJeBusinessException("pje.abstractPessoaFisicaEspecializadaManager.cpfEmUso", null, cpf);
				}
				
				// se alterou o cpf, altera-se o login, gera-se nova senha e permite envio do link de ativacao p/ email
				setaDadosUsuarioSenha(entity, cpf);
			}
			
			// se login vazio, gera-se nova senha e permite envio do link de ativacao por email
			if(entity.getLogin() == null){
				Boolean loginDisponivel = checkLogin(cpf, entity.getIdUsuario());
				if(!loginDisponivel){
					throw new PJeBusinessException("pje.abstractPessoaFisicaEspecializadaManager.loginEmUso", null, cpf);
				}
				
				setaDadosUsuarioSenha(entity, cpf);
			}
			
			// se senha vazia, gera-se nova senha e permite envio do link de ativacao por email
			if(entity.getSenha() == null){
				setaDadosUsuarioSenha(entity, cpf);
			}
		}
		
		if(entity.getInTipoPessoa() == null){
			entity.setInTipoPessoa(TipoPessoaEnum.F);
		}
		
		if(entity.getAtivo() == null){
			entity.setAtivo(true);
		}
		
		if(entity.getBloqueio() == null){
			entity.setBloqueio(false);
		}
		
		if(entity.getProvisorio() == null){
			entity.setProvisorio(false);
		}
				
		if(entity.getTipoPessoa() == null){
			throw new PJeBusinessException("pje.manager.abstractUsarioManager.informeTipoPessoa");
		}
		
		if(!entity.getLogin().equals(cpf)){
			throw new PJeBusinessException("pje.manager.abstractUsarioManager.error.loginDiferenteCpf", null, entity.getLogin(), cpf);
		}
		
		return super.persist(entity);
	}
	
	public void inactive(E entity) throws PJeBusinessException{
		entity.getPessoa().suprimePessoaEspecializada(entity);
		pessoaFisicaManager.persistAndFlush(entity.getPessoa());
	}
	
	private void setaDadosUsuarioSenha(E entity, String login){
		entity.setAtivo(true);
		entity.setBloqueio(false);
		entity.setProvisorio(false);
		entity.setLogin(login);
		entity.setHashAtivacaoSenha(PjeUtil.instance().gerarHashAtivacao(entity.getLogin()));
		entity.setStatusSenha(StatusSenhaEnum.I);		
	}
}
