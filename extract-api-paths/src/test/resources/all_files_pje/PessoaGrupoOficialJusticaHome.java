package br.com.infox.cliente.home;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.GrupoOficialJustica;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.service.LocalizacaoService;
import br.jus.pje.nucleo.entidades.PessoaGrupoOficialJustica;
import br.jus.pje.nucleo.entidades.PessoaOficialJustica;

@Name("pessoaGrupoOficialJusticaHome")
@BypassInterceptors
public class PessoaGrupoOficialJusticaHome extends AbstractPessoaGrupoOficialJusticaHome<PessoaGrupoOficialJustica>{

	private static final long serialVersionUID = 1L;

	public void addPessoaGrupoOficialJustica(PessoaOficialJustica obj, String gridId){
		if (getInstance() != null){		
			PessoaGrupoOficialJustica pessoaOficialJustica = pesquisarPessoaGrupoOficial(getInstance().getGrupoOficialJustica(), obj.getPessoa());
			
			if (pessoaOficialJustica !=null){
				pessoaOficialJustica.setAtivo(true);			
				getEntityManager().persist(pessoaOficialJustica);
				EntityUtil.flush();
			}else{
				getInstance().setPessoa(obj.getPessoa());
				getInstance().setAtivo(Boolean.TRUE);			
				persist();
			}

			refreshGrid("pessoaOficialJusticaGrid");
			refreshGrid("pessoaGrupoOficialJusticaGrid");
		}
	}

	public void removePessoaGrupoOficialJustica(PessoaGrupoOficialJustica obj, String gridId){
		if (getInstance() != null){		
			remove(obj);
			refreshGrid("pessoaOficialJusticaGrid");
			refreshGrid("pessoaGrupoOficialJusticaGrid");
		}
	}
	
	public String selectPorPerfil() {
		if (!Authenticator.getPapelAtual().getIdentificador().equalsIgnoreCase("admin") || 
				!Authenticator.getPapelAtual().getIdentificador().equalsIgnoreCase("administrador")) {
			
			LocalizacaoService localizacaoService = ComponentUtil.getComponent("localizacaoService");
			
			String subQueryIdsCentralMandado = 
				String.format("select cml.centralMandado.idCentralMandado from CentralMandadoLocalizacao cml where cml.localizacao.idLocalizacao in %s", 
				localizacaoService.getTreeIds(Authenticator.getUsuarioLocalizacaoAtual().getLocalizacaoFisica()));
			
			return " where o.ativo = true and o.grupoOficialJustica.centralMandado.idCentralMandado in ( " + subQueryIdsCentralMandado + " ) ";			
		}
		return " where o.ativo = true ";
	}
	
	@SuppressWarnings("unchecked")
	public PessoaGrupoOficialJustica pesquisarPessoaGrupoOficial(GrupoOficialJustica grupoOficialJustica, Pessoa pessoa) {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from PessoaGrupoOficialJustica o ");
		sb.append("where o.ativo = false and o.grupoOficialJustica = :grupoOficialJustica ");
		sb.append("and o.pessoa = :pessoa");
		EntityManager entityManager = EntityUtil.getEntityManager();
		Query query = entityManager.createQuery(sb.toString());
		query.setParameter("grupoOficialJustica", grupoOficialJustica);
		query.setParameter("pessoa", pessoa);

		List<PessoaGrupoOficialJustica> resultList  =  query.getResultList();
		
		if (resultList.size() > 0){
			return resultList.get(0);
		}else{
			return null;
		}
	}
}