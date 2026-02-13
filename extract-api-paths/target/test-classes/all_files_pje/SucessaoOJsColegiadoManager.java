package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.itx.util.ComponentUtil;
import br.com.jt.pje.manager.OrgaoJulgadorColegiadoOrgaoJulgadorManager;
import br.jus.cnj.pje.business.dao.SucessaoOJsColegiadoDAO;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiadoOrgaoJulgador;
import br.jus.pje.nucleo.entidades.SucessaoOJsColegiado;

@Name(SucessaoOJsColegiadoManager.NAME)
public class SucessaoOJsColegiadoManager extends BaseManager<SucessaoOJsColegiado>{

	public static final String NAME = "sucessaoOJsColegiadoManager";

	@In
	private SucessaoOJsColegiadoDAO sucessaoOJsColegiadoDAO;
	
	
	@Override
	protected SucessaoOJsColegiadoDAO getDAO() {
		return sucessaoOJsColegiadoDAO;
	}
		
	
	/**
	 * Dados dois órgãos julgadores singulares, verifica se os mesmo possuem relação de sucessão (dança de cadeiras)
	 * dentro de um dado órgão julgador colegiado.
 
	 * @param possivelOrgaoJulgadorSucedido orgao julgador a verificar se foi sucedido.
	 * @param possivelOrgaoJulgadorSucessor orgao julgador a verificar se foi sucessor
	 * @param orgaoJulgadorColegiado orgao julgador colegiado a verificar se ocorreu a sucessão. 
	 * @return true caso exista relação de sucessão
	 */
	public boolean existeRelacaoSucessao(OrgaoJulgador possivelOrgaoJulgadorSucedido, OrgaoJulgador possivelOrgaoJulgadorSucessor,
			OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		
		Boolean existeRelacaoSucessao = false;
		
		SucessaoOJsColegiado sucessaoOJs = obterSucessaoPeloOrgaoJulgadorSucedido(possivelOrgaoJulgadorSucedido, orgaoJulgadorColegiado);
		while (sucessaoOJs != null){
			if (sucessaoOJs.getOrgaoJulgadorSucessor().equals(possivelOrgaoJulgadorSucessor)){
				existeRelacaoSucessao = true;
				break;
			}
			sucessaoOJs = obterSucessaoPeloOrgaoJulgadorSucedido(sucessaoOJs.getOrgaoJulgadorSucessor(), orgaoJulgadorColegiado);
		}
		
		return existeRelacaoSucessao;
	}
	
	
	/**
	 * Dados dois órgãos julgadores singulares, verifica se os mesmo possuem relação de sucessão (dança de cadeiras)
	 * dentro de um dado órgão julgador colegiado.
 
	 * @param possivelOrgaoJulgadorSucedido orgao julgador a verificar se foi sucedido.
	 * @param possivelOrgaoJulgadorSucessor orgao julgador a verificar se está na linha de sucessão do órgão julgador sucedido.
	 * @param orgaoJulgadorColegiado orgao julgador colegiado a verificar se ocorreu a sucessão. 
	 * @return true caso exista relação de sucessão
	 */
	public boolean existeRelacaoSucessaoRecursiva(OrgaoJulgador possivelOrgaoJulgadorSucedido, OrgaoJulgador possivelOrgaoJulgadorSucessor,	OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		SucessaoOJsColegiado sucessaoOJs = obterSucessaoPeloOrgaoJulgadorSucedido(possivelOrgaoJulgadorSucedido, orgaoJulgadorColegiado);
		if (sucessaoOJs != null){
			if (sucessaoOJs.getOrgaoJulgadorSucessor().equals(possivelOrgaoJulgadorSucessor)){
				return true;
			}
			else{
				return existeRelacaoSucessaoRecursiva(sucessaoOJs.getOrgaoJulgadorSucessor(), possivelOrgaoJulgadorSucessor, orgaoJulgadorColegiado);
			}
		}
		return false;
	}
	
	private SucessaoOJsColegiado obterSucessaoPeloOrgaoJulgadorSucedido(OrgaoJulgador orgaoJulgadorSucedido, 
			OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		return getDAO().obterSucessaoPeloOrgaoJulgadorSucedido(orgaoJulgadorSucedido, orgaoJulgadorColegiado);
	}
	
	private SucessaoOJsColegiado obterSucessaoPeloOrgaoJulgadorSucedido(OrgaoJulgador orgaoJulgadorSucedido, 
			OrgaoJulgadorColegiado orgaoJulgadorColegiado, Date dataReferencia) {
		return getDAO().obterSucessaoPeloOrgaoJulgadorSucedido(orgaoJulgadorSucedido, orgaoJulgadorColegiado, dataReferencia);
	}
	
	/***
	 * Dado um OJ e um OJC, recupera o OJ sucessor ativo atual no colegiado,
	 * para isso busca recursivamente os OJs que se sucederam até encontrar um que esteja ativo no Colegiado
	 * 
	 * @param orgaoJulgadorSucedido
	 * @param orgaoJulgadorColegiado
	 * @return
	 */
	public OrgaoJulgadorColegiadoOrgaoJulgador obterOrgaoJulgadorSucessorAtivo(OrgaoJulgador orgaoJulgadorReferencia, 
			OrgaoJulgadorColegiado orgaoJulgadorColegiado) {

		OrgaoJulgadorColegiadoOrgaoJulgadorManager ojcojManager = ComponentUtil.getComponent(OrgaoJulgadorColegiadoOrgaoJulgadorManager.class);
		List<OrgaoJulgadorColegiadoOrgaoJulgador> ojcojList = ojcojManager.obterAtivos(orgaoJulgadorColegiado);
		
		List<OrgaoJulgador> ojsVerificados = new ArrayList<OrgaoJulgador>();
		OrgaoJulgadorColegiadoOrgaoJulgador ojcojSucessorAtivo = null;
		Date dataUltimaSucessao = null;
		
		while(ojcojSucessorAtivo == null && orgaoJulgadorReferencia != null && !ojsVerificados.contains(orgaoJulgadorReferencia)) {
			ojsVerificados.add(orgaoJulgadorReferencia);
			for (OrgaoJulgadorColegiadoOrgaoJulgador ojcoj : ojcojList) {
				if(ojcoj.getOrgaoJulgador().getIdOrgaoJulgador() == orgaoJulgadorReferencia.getIdOrgaoJulgador()) {
					ojcojSucessorAtivo = ojcoj;
					break;
				}
			}
			if(ojcojSucessorAtivo == null) {
				SucessaoOJsColegiado sucessao = this.obterSucessaoPeloOrgaoJulgadorSucedido(orgaoJulgadorReferencia, orgaoJulgadorColegiado, dataUltimaSucessao);
				if(sucessao != null) {
					dataUltimaSucessao = sucessao.getDataSucessao();
					orgaoJulgadorReferencia = sucessao.getOrgaoJulgadorSucessor();
				}
			}
		}
		return ojcojSucessorAtivo;
	}
	
}