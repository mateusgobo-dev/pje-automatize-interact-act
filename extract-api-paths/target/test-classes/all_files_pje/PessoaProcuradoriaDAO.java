/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaProcuradoria;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.identidade.Papel;

@Name("pessoaProcuradoriaDAO")
public class PessoaProcuradoriaDAO extends BaseDAO<PessoaProcuradoria>{

	@Override
	public Integer getId(PessoaProcuradoria e){
		return e.getIdPessoaProcuradoria();
	}
	
	/* TODO
	 * Foi incluido o comando de exclusão pois utilizando o remove() original ocorrer erro de String index out of range: 0
	 */
	public void remove(PessoaProcuradoria pessoaProcuradoria) {
		StringBuilder sb = new StringBuilder();
		sb.append("delete from tb_pessoa_procuradoria ");
		sb.append("where id_pessoa_procuradoria = :id ");
		String sql = sb.toString();
		sql = sql.replaceAll(":id", String.valueOf(pessoaProcuradoria.getIdPessoaProcuradoria()));
		EntityUtil.createNativeQuery(getEntityManager(), sql, "tb_pessoa_procuradoria").executeUpdate();
		EntityUtil.flush(getEntityManager());
	}

	public UsuarioLocalizacao findUsuarioLocalizacaoPapel(PessoaProcuradoria pessoaProcuradoria, Papel papel) {
		UsuarioLocalizacaoDAO usuarioLocalizacaoDAO = new UsuarioLocalizacaoDAO();
		return usuarioLocalizacaoDAO.getLocalizacoesAtuais(pessoaProcuradoria.getPessoa().getPessoa(), 
														   papel, 
														   pessoaProcuradoria.getProcuradoria().getLocalizacao()).get(0);		
	}
	
	public List getGridAssociacoes(String inTipoProcuradoria, String idPessoa){
		 String sql = getSqlAssociacoes(inTipoProcuradoria, idPessoa);
		 Query query = entityManager.createNativeQuery(sql);
		 
		 return query.getResultList();
	}

	private String getSqlAssociacoes(String inTipoProcuradoria, String idPessoa){
		/*
		 * Colunas do Resultset
		 * 
		 * coluna[0] = Tipo Prcouradoria
		 * coluna[1] = Id Pessoa
		 * coluna[2] = Id Localização
		 * coluna[3] = Id Procuradoria
		 * coluna[4] = Nome da Procuradoria
		 * coluna[5] = Atuação
		 * coluna[6] = Id Jurisdição
		 * coluna[7] = Jurisdição
		 * coluna[8] = Caixa 
		 * 
		 */
		String where = "where id_pessoa = "+idPessoa+" "
				+ "and in_tipo_procuradoria = '"+inTipoProcuradoria+"' ";
		String orderby = "ORDER BY ds_nome, atuacao, jurisdicao ";
		
		return "select * from ("
				+ "select p.in_tipo_procuradoria, pp.id_pessoa, p.id_localizacao, pp.id_procuradoria, p.ds_nome, "
				+ "CASE WHEN pp.in_chefe_procuradoria = true THEN 'Gestor' "
				+ "WHEN (pp.in_chefe_procuradoria = false and ppj.id_jurisdicao > 0) THEN 'Distribuidor' "
				+ "ELSE 'Padrão' "
				+ "END AS atuacao, jur.id_jurisdicao, "
				+ "CASE WHEN pp.in_chefe_procuradoria = true THEN 'Todas' "
				+ "WHEN (pp.in_chefe_procuradoria = false and ppj.id_jurisdicao > 0) THEN jur.ds_jurisdicao "
				+ "ELSE '-' "
				+ "END AS jurisdicao, "
				+ "CASE WHEN pp.in_chefe_procuradoria = true THEN 'Todas' "
				+ "WHEN (pp.in_chefe_procuradoria = false and ppj.id_jurisdicao > 0) THEN 'Todas' "
				+ "ELSE '-' "
				+ "END AS caixa "
				+ "from client.tb_pessoa_procuradoria pp LEFT OUTER JOIN client.tb_pess_proc_jurisdicao ppj ON (pp.id_pessoa_procuradoria  =  ppj.id_pessoa_procuradoria) "
				+ "INNER JOIN client.tb_procuradoria p ON (pp.id_procuradoria = p.id_procuradoria) "
				+ "LEFT OUTER JOIN client.tb_jurisdicao jur ON (jur.id_jurisdicao = ppj.id_jurisdicao) "
				+ "UNION ALL "
				+ "select p.in_tipo_procuradoria, cr.id_pessoa_fisica as id_pessoa, p.id_localizacao, p.id_procuradoria, p.ds_nome, 'Padrão' as atuacao, jur.id_jurisdicao, jur.ds_jurisdicao, 'C' as caixa "
				+ "from client.tb_caixa_representante cr, client.tb_caixa_adv_proc crp, client.tb_procuradoria p, client.tb_jurisdicao jur "
				+ "where cr.id_caixa_adv_proc = crp.id_caixa_adv_proc "
				+ "and crp.id_localizacao = p.id_localizacao "
				+ "and crp.id_jurisdicao = jur.id_jurisdicao "
				+ ") TABELA "
				+ where
				+ orderby;
	}
}
