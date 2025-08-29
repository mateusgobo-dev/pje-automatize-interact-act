package br.com.jt.pje.list;

import java.util.HashMap;
import java.util.Map;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.jt.entidades.PautaSessao;

public abstract class FiltrosPautaVotacaoAntecipadaList<T extends PautaSessao> extends EntityList<T>{

	private static final long serialVersionUID = 1L;
	
	protected static final String DEFAULT_EJBQL = "select o from PautaSessao o ";
	protected static final String DEFAULT_ORDER = "o.processoTrf.processo.numeroProcesso";
	
	
	protected static final String R1 = "o.processoTrf.processo.numeroProcesso like '%' || #{searchProcessosPautaBean.numeroProcesso} || '%'";
	protected static final String R2 = "o.processoTrf.orgaoJulgador = #{searchProcessosPautaBean.orgaoJulgador}";
	protected static final String R3 = "o.processoTrf.classeJudicial = #{searchProcessosPautaBean.classeJudicial}";
	
	protected static final String R4 = "exists (select p from ProcessoTrf p "+
            			 				"inner join p.processoAssuntoList assuntoList "+ 
        			 					"where p = o.processoTrf and assuntoList.assuntoTrf = #{searchProcessosPautaBean.assuntoTrf})";
	
	protected static final String R5 = "exists (select pp.processoTrf from ProcessoParte pp " +
										"where pp.processoTrf = o.processoTrf and " +
										"pp.pessoa.tipoPessoa = #{searchProcessosPautaBean.tipoPessoa}) ";
	
	protected static final String R6 = "exists (select pp.processoTrf from ProcessoParte pp " + 
					 				   "		  where pp.processoTrf = o.processoTrf " +
					 				   "		  and pp.pessoa.idUsuario IN (select pdi.pessoa.idUsuario from PessoaDocumentoIdentificacao pdi " +
					 				   "									  where pdi.tipoDocumento.codTipo = 'CPF' " +
					 				   "									  and pdi.numeroDocumento like '%'||  #{searchProcessosPautaBean.numeroCPF} ||'%'))";
	
	protected static final String R7 = "exists (select pp.processoTrf from ProcessoParte pp " + 
									   "		  where pp.processoTrf = o.processoTrf " +
									   "		  and pp.pessoa.idUsuario IN (select pdi.pessoa.idUsuario from PessoaDocumentoIdentificacao pdi " +
									   "									  where pdi.tipoDocumento.codTipo = 'CPJ' " +
									   " 									  and pdi.numeroDocumento like '%'||  #{searchProcessosPautaBean.numeroCNPJ} ||'%'))";
	
	protected static final String R8 = "o.tipoInclusao = #{searchProcessosPautaBean.tipoInclusao}";
	
	protected static final String R9 = "exists (select p from ProcessoTrf p " +
									  	"				inner join p.processoParteList ppList " +
									  	"				inner join ppList.processoParteAdvogadoList ppaList " +
									  	"				inner join ppaList.pessoaAdvogado pa " +
									  	"				where p = o.processoTrf and pa.ufOAB = #{searchProcessosPautaBean.ufOab})";
	
	protected static final String R10 = "exists (select p from ProcessoTrf p "+
									  	"				inner join p.processoParteList ppList inner join ppList.processoParteAdvogadoList ppaList "+
									  	"				inner join ppaList.pessoaAdvogado pa "+ 
									  	"				where p = o.processoTrf and pa.numeroOAB like concat('%',lower(to_ascii(#{searchProcessosPautaBean.numeroOab})),'%'))";
	
	
	protected static final String R11 = "exists (select p from ProcessoTrf p " +
									  	"				inner join p.processoParteList ppList " +
									  	"				inner join ppList.processoParteAdvogadoList ppaList " +
									  	"				inner join ppaList.pessoaAdvogado pa " +
									  	"				where p = o.processoTrf and pa.letraOAB like concat('%',lower(to_ascii(#{searchProcessosPautaBean.letraOab})),'%'))";
	
	protected static final String R12 = "exists (select pp.processoTrf from ProcessoParte pp "+
										"where pp.processoTrf = o.processoTrf "+ 
										"and pp.inSituacao = 'A' "+
										"and lower(to_ascii(pp.pessoa.nome)) like "+ 
										"'%' || lower(to_ascii(#{searchProcessosPautaBean.nomeParte})) || '%')";
	
	protected static final String R13 = "o.resultadoVotacao = #{searchProcessosPautaBean.resultadoVotacao}";
	
	//TODO fazer filtro de advogado procurador
	protected static final String R14 = "#{searchProcessosPautaBean.nomeAdvogadoProcurador}";
	
	@Override
	protected void addSearchFields() {
		addSearchField("numeroProcesso", SearchCriteria.contendo, R1);
		addSearchField("orgaoJulgador", SearchCriteria.igual, R2);
		addSearchField("classeJudicial", SearchCriteria.igual, R3);
		addSearchField("assuntoTrf", SearchCriteria.igual, R4);
		addSearchField("tipoPessoa", SearchCriteria.igual, R5);
		addSearchField("numeroCPF", SearchCriteria.igual, R6);
		addSearchField("numeroCNPJ", SearchCriteria.igual, R7);
		addSearchField("tipoInclusao", SearchCriteria.igual, R8);
		addSearchField("ufOAB", SearchCriteria.igual, R9);
		addSearchField("numeroOAB", SearchCriteria.igual, R10);
		addSearchField("letraOAB", SearchCriteria.igual, R11);
		addSearchField("nomeParte", SearchCriteria.igual, R12);
		addSearchField("resultadoVotacaoUnico", SearchCriteria.igual, R13);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("orgaoJulgador", "o.processoTrf.orgaoJulgador.orgaoJulgador");
		map.put("classeJudicial", "o.processoTrf.classeJudicial.classeJudicial");
		return map;
	}

	@Override
	protected String getDefaultEjbql() {
		return DEFAULT_EJBQL;
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}
	
}
