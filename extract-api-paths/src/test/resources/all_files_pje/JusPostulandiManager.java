package br.com.infox.pje.manager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ParametroUtil;
import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.JusPostulandiDAO;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.AbstractUsuarioManager;
import br.jus.pje.nucleo.entidades.PessoaFisica;

@Name(JusPostulandiManager.NAME)
public class JusPostulandiManager extends AbstractUsuarioManager<PessoaFisica, JusPostulandiDAO> {
	
	public static final String NAME = "jusPostulandiManager";
	
	@In
	private JusPostulandiDAO jusPostulandiDAO;

	@Override
	protected BaseDAO<PessoaFisica> getDAO() {
		return jusPostulandiDAO;
	}
	
	@Override
	public PessoaFisica persist(PessoaFisica entity)
			throws PJeBusinessException {
		
		if(entity.getTipoPessoa() == null){
			entity.setTipoPessoa(ParametroUtil.instance().getTipoPessoaJusPostulandi());
		}
		
		if(entity.getTipoPessoa() != null){
			throw new PJeBusinessException("pje.jusPostulandiManager.error.tipoPessoaInvalido");
		}
		
		String cpf = InscricaoMFUtil.retiraMascara(entity.getDocumentoCpfCnpj());
		
		if(cpf == null || cpf.trim().isEmpty()){
			throw new PJeBusinessException("pje.jusPostulandiManager.error.informeCpf");
		}
		
		if(entity.getLogin() == null){
			entity.setLogin(cpf);
		}
		
		if(!entity.getLogin().equals(cpf)){
			throw new PJeBusinessException("pje.jusPostulandiManager.error.loginDiferenteCpf");
		}
		
		//se inserindo
		if(entity.getIdPessoa() == null){
			if(entity.getBloqueio() == null){
				entity.setBloqueio(Boolean.FALSE);
			}
		}
		
		return super.persist(entity);
	}

}
