package br.com.jt.pje.list;

import java.util.Map;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

public abstract class FiltrosPautaJulgamentoList<T extends ProcessoTrf> extends EntityList<T>{

	private static final long serialVersionUID = 1L;
	
	protected static final String DEFAULT_EJBQL = "select o from ProcessoTrf o ";
	protected static final String DEFAULT_ORDER = "o.processo.numeroProcesso";
	
	protected static final String R1 = "o.processo.numeroProcesso like '%' || #{searchProcessosPautaBean.numeroProcesso} || '%'";
	protected static final String R2 = "o.orgaoJulgador = #{searchProcessosPautaBean.orgaoJulgador}";
	protected static final String R3 = "o.classeJudicial = #{searchProcessosPautaBean.classeJudicial}";
	
	protected static final String R4 = "exists (select p from ProcessoTrf p "+
            			 				"inner join p.processoAssuntoList assuntoList "+ 
        			 					"where p = o and assuntoList.assuntoTrf = #{searchProcessosPautaBean.assuntoTrf})";
	
	protected static final String R5 = "exists (select pp.processoTrf from ProcessoParte pp " +
										"where pp.processoTrf = o and " +
										"pp.pessoa.tipoPessoa = #{searchProcessosPautaBean.tipoPessoa}) ";
	
	protected static final String R6 = "exists (select pp.processoTrf from ProcessoParte pp " + 
					 				   "		  where pp.processoTrf.idProcessoTrf = o.idProcessoTrf " +
					 				   "		  and pp.pessoa.idUsuario IN (select pdi.pessoa.idUsuario from PessoaDocumentoIdentificacao pdi " +
					 				   "									  where pdi.tipoDocumento.codTipo = 'CPF' " +
					 				   "									  and pdi.numeroDocumento like '%'||  #{searchProcessosPautaBean.numeroCPF} ||'%'))";
	
	protected static final String R7 = "exists (select pp.processoTrf from ProcessoParte pp " + 
									   "		  where pp.processoTrf.idProcessoTrf = o.idProcessoTrf " +
									   "		  and pp.pessoa.idUsuario IN (select pdi.pessoa.idUsuario from PessoaDocumentoIdentificacao pdi " +
									   "									  where pdi.tipoDocumento.codTipo = 'CPJ' " +
									   " 									  and pdi.numeroDocumento like '%'||  #{searchProcessosPautaBean.numeroCNPJ} ||'%'))";
	
	protected static final String R8 = "o.idProcessoTrf in (select p.idProcessoTrf from ProcessoTrf p " +
									  	"					   inner join p.processoParteList ppList " +
									  	"					   inner join ppList.processoParteAdvogadoList ppaList " +
									  	"					   inner join ppaList.pessoaAdvogado pa " +
									  	"					   where p = o and pa.ufOAB = #{searchProcessosPautaBean.ufOab})";
	
	protected static final String R9 = "o.idProcessoTrf in (select p.idProcessoTrf from ProcessoTrf p "+
									  	"					   inner join p.processoParteList ppList inner join ppList.processoParteAdvogadoList ppaList "+
									  	"					   inner join ppaList.pessoaAdvogado pa "+ 
									  	"					   where p = o and pa.numeroOAB like concat('%',lower(to_ascii(#{searchProcessosPautaBean.numeroOab})),'%'))";
	
	
	protected static final String R10 = "o.idProcessoTrf in (select p.idProcessoTrf from ProcessoTrf p " +
									  	"					   inner join p.processoParteList ppList " +
									  	"					   inner join ppList.processoParteAdvogadoList ppaList " +
									  	"					   inner join ppaList.pessoaAdvogado pa " +
									  	"					   where p = o and pa.letraOAB like concat('%',lower(to_ascii(#{searchProcessosPautaBean.letraOab})),'%'))";
	
	protected static final String R11 = "exists (select pp.processoTrf from ProcessoParte pp "+
										"where pp.processoTrf = o "+ 
										"and pp.inSituacao = 'A' "+
										"and lower(to_ascii(pp.pessoa.nome)) like "+ 
										"'%' || lower(to_ascii(#{searchProcessosPautaBean.nomeParte})) || '%')";
	
	//TODO fazer filtro de advogado procurador
	protected static final String R12 = "#{searchProcessosPautaBean.nomeAdvogadoProcurador}";
	
	@Override
	protected void addSearchFields() {
		addSearchField("numeroProcesso", SearchCriteria.contendo, R1);
		addSearchField("orgaoJulgador", SearchCriteria.igual, R2);
		addSearchField("classeJudicial", SearchCriteria.igual, R3);
		addSearchField("assuntoTrf", SearchCriteria.igual, R4);
		addSearchField("tipoPessoa", SearchCriteria.igual, R5);
		addSearchField("numeroCPF", SearchCriteria.igual, R6);
		addSearchField("numeroCNPJ", SearchCriteria.igual, R7);
		addSearchField("ufOAB", SearchCriteria.igual, R8);
		addSearchField("numeroOAB", SearchCriteria.igual, R9);
		addSearchField("letraOAB", SearchCriteria.igual, R10);
		addSearchField("nomeParte", SearchCriteria.igual, R11);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
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