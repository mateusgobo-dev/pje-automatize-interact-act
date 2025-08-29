package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.PessoaProcuradorDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.csjt.pje.commons.util.ParametroJtUtil;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaProcurador;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.enums.SexoEnum;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name(PessoaProcuradorManager.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class PessoaProcuradorManager extends AbstractPessoaFisicaEspecializadaManager<PessoaProcurador, PessoaProcuradorDAO>{
	
	@In
	private PessoaProcuradorDAO pessoaProcuradorDAO;
	
	public static final String NAME = "pessoaProcuradorManager";

	@Override
	public PessoaProcurador especializa(PessoaFisica pessoa)
			throws PJeBusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected BaseDAO<PessoaProcurador> getDAO() {
		return pessoaProcuradorDAO;
	}

	@Override
	public PessoaProcurador desespecializa(PessoaFisica pessoa)
			throws PJeBusinessException {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Este método recupera as {@link PessoaProcurador} ativos da {@link Procuradoria}
	 * @param procuradoria
	 * @return lista de procuradores ativos
	 */
	public List<PessoaProcurador> findProcuradoresAtivosProcuradoria(Procuradoria procuradoria){
		 
		Search s = new Search(PessoaProcurador.class);
		addCriteria(s, 
					Criteria.equals("procuradoria", procuradoria),
					Criteria.bitwiseAnd("pessoa.especializacoes", PessoaFisica.PRO, PessoaFisica.PRO),
					Criteria.equals("ativo", Boolean.TRUE));
				
		return list(s);
	}
	
	public List<PessoaProcurador> getPessoaProcuradores(Procuradoria p){
		return pessoaProcuradorDAO.findByOrgaoRepresentacacao(p);
	}


	public String getTituloProcurador(Sessao sessao) {
		String retorno = "";
		sessao = ComponentUtil.getSessaoManager().recuperarSessao(sessao);
		if(sessao != null && sessao.getPessoaProcurador() != null) {
			String titulo = "";
			boolean feminino = false;
			if(ParametroJtUtil.instance().justicaEleitoral()) {
				if(sessao.getPessoaProcurador().getSexo() != null && SexoEnum.F.equals(sessao.getPessoaProcurador().getSexo())) {
					titulo = "Procuradora ";
					feminino = true;
				} else {
					titulo = "Procurador ";
				}
				if( ParametroUtil.instance().isTerceiroGrau() ) {
					titulo = titulo + "Geral ";
				} else {
					if( ParametroUtil.instance().isSegundoGrau() ) {
						titulo = titulo + "Regional ";
					} 
				}
				if( ParametroUtil.instance().isPrimeiroGrau() ) {
					titulo = "Representante do Ministrio Pblico";
				} else {
					titulo = titulo + "Eleitoral";
				}
				if(feminino) {
					titulo = titulo + ", a Dra. ";
				} else {
					titulo = titulo + ", o Dr. ";
				}
			}
			retorno = titulo + sessao.getPessoaProcurador().getNome();
		}
		return retorno;
	}

}
