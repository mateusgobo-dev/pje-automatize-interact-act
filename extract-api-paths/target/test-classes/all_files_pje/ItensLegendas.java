package br.com.infox.utils;

public class ItensLegendas {
	/**
     * (fernando.junior - 18/01/2013) Adição de novas siglas referentes às novas legendas:
     * DIAP - Divergência com análise pendente
     * DINC - Divergência não concluída/liberada
     * DENC - Destaque não concluído/liberado
     * A - Anotação
     * ANC - Anotação não concluída 
     */
    public static String[] SIGLAS_MAGISTRADO_LEGENDAS = {
            "DI", "DE", "O", "J", "DS", "RP", "PD", "SO", "P", "AA", "ANA",
            "VNE", "VNL", "VL", "VR", "JSP", "JCP", "DIAP", "DINC", "DENC", "A", "ANC"
    };
    
    /**
     * (fernando.junior - 18/01/2013) Adição de novas legendas 
     */
    public static String[][] LEGENDAS_MAGISTRADO_ARRAY = {
	    	{ "/img/divergencia.png", "Divergência", SIGLAS_MAGISTRADO_LEGENDAS[0] },
	    	{ "/img/divergencia-pendente.png", "Divergência com análise pendente", SIGLAS_MAGISTRADO_LEGENDAS[17] },
	    	{ "/img/divergencia-nao-concluida.png", "Divergência não concluída/liberada", SIGLAS_MAGISTRADO_LEGENDAS[18] },
	    	{ "/img/destaque.png", "Destaque", SIGLAS_MAGISTRADO_LEGENDAS[1] },
	    	{ "/img/destaque-nao-concluido.png", "Destaque não concluído/liberado", SIGLAS_MAGISTRADO_LEGENDAS[19] },
	    	{ "/img/anotacao.png", "Anotação", SIGLAS_MAGISTRADO_LEGENDAS[20] },
	    	{ "/img/anotacao-nao-concluida.png", "Anotação não concluída", SIGLAS_MAGISTRADO_LEGENDAS[21] },
            { "/img/martelo.jpg", "Julgados", SIGLAS_MAGISTRADO_LEGENDAS[3] },
            {
                "/img/ico_martelo_preto.png", "Julgados",
                SIGLAS_MAGISTRADO_LEGENDAS[3]
            },
            {
                "/img/deliberado.png", "Deliberação em Sessão",
                SIGLAS_MAGISTRADO_LEGENDAS[4]
            },
            {
                "/img/retirado_pauta.png", "Retirados de Pauta",
                SIGLAS_MAGISTRADO_LEGENDAS[5]
            },
            { "/img/pendente.png", "Pendentes", SIGLAS_MAGISTRADO_LEGENDAS[6] },
            {
                "/img/icone_balao.png", "Sustentações Orais",
                SIGLAS_MAGISTRADO_LEGENDAS[7]
            },
            { "/img/star.jpg", "Preferências", SIGLAS_MAGISTRADO_LEGENDAS[8] },
            {
                "/img/search_red_16.png", "Voto não elaborado",
                SIGLAS_MAGISTRADO_LEGENDAS[11]
            },
            {
                "/img/search_yellow_16.png", "Voto elaborado e não liberado",
                SIGLAS_MAGISTRADO_LEGENDAS[12]
            },
            {
                "/img/search_green_16.png", "Voto elaborado e liberado",
                SIGLAS_MAGISTRADO_LEGENDAS[13]
            },
            {
                "/img/search_gray_16.png", "Voto do relator não liberado",
                SIGLAS_MAGISTRADO_LEGENDAS[14]
            },
            {
                "/img/checkin_green_16.png", "Meus Acórdãos Assinados",
                SIGLAS_MAGISTRADO_LEGENDAS[9]
            },
            {
                "/img/checkin_gray_16.png", "Meus Acórdãos Não Assinados",
                SIGLAS_MAGISTRADO_LEGENDAS[10]
            }
    };
    
    /**
     * (fernando.junior - 18/01/2013) Adição de novas legendas 
     */
    public static String[][] LEGENDAS_MAGISTRADO_SESSAO_ENCERRADA_ARRAY = {
	    	{ "/img/divergencia.png", "Divergência", SIGLAS_MAGISTRADO_LEGENDAS[0] },
	    	{ "/img/divergencia-pendente.png", "Divergência com análise pendente", SIGLAS_MAGISTRADO_LEGENDAS[17] },
	    	{ "/img/divergencia-nao-concluida.png", "Divergência não concluída/liberada", SIGLAS_MAGISTRADO_LEGENDAS[18] },
	    	{ "/img/destaque.png", "Destaque", SIGLAS_MAGISTRADO_LEGENDAS[1] },
	    	{ "/img/destaque-nao-concluido.png", "Destaque não concluído/liberado", SIGLAS_MAGISTRADO_LEGENDAS[19] },
	    	{ "/img/anotacao.png", "Anotação", SIGLAS_MAGISTRADO_LEGENDAS[20] },
	    	{ "/img/anotacao-nao-concluida.png", "Anotação não concluída", SIGLAS_MAGISTRADO_LEGENDAS[21] },
            {
                "/img/ico_martelo_normal.png",
                "Julgados sem Pendência de lançamentos",
                SIGLAS_MAGISTRADO_LEGENDAS[15]
            },
            {
                "/img/ico_martelo_cinza.png",
                "Julgados com Pendência de lançamentos",
                SIGLAS_MAGISTRADO_LEGENDAS[16]
            },
            {
                "/img/deliberado.png", "Deliberação em Sessão",
                SIGLAS_MAGISTRADO_LEGENDAS[4]
            },
            {
                "/img/retirado_pauta.png", "Retirados de Pauta",
                SIGLAS_MAGISTRADO_LEGENDAS[5]
            },
            { "/img/pendente.png", "Pendentes", SIGLAS_MAGISTRADO_LEGENDAS[6] },
            {
                "/img/icone_balao.png", "Sustentações Orais",
                SIGLAS_MAGISTRADO_LEGENDAS[7]
            },
            { "/img/star.jpg", "Preferências", SIGLAS_MAGISTRADO_LEGENDAS[8] },
            {
                "/img/search_red_16.png", "Voto não elaborado",
                SIGLAS_MAGISTRADO_LEGENDAS[11]
            },
            {
                "/img/search_yellow_16.png", "Voto elaborado e não liberado",
                SIGLAS_MAGISTRADO_LEGENDAS[12]
            },
            {
                "/img/search_green_16.png", "Voto elaborado e liberado",
                SIGLAS_MAGISTRADO_LEGENDAS[13]
            },
            {
                "/img/search_gray_16.png", "Voto do relator não liberado",
                SIGLAS_MAGISTRADO_LEGENDAS[14]
            },
            {
                "/img/checkin_green_16.png", "Meus Acórdãos Assinados",
                SIGLAS_MAGISTRADO_LEGENDAS[9]
            },
            {
                "/img/checkin_gray_16.png", "Meus Acórdãos Não Assinados",
                SIGLAS_MAGISTRADO_LEGENDAS[10]
            }
    };
    
    public static String[] SIGLAS_PROCURADOR_LEGENDAS = {
            "J", "DS", "RP", "PD", "SO", "P", "AA", "AAM", "ANA", "JSP", "JCP"
        };
    public static String[][] LEGENDAS_PROCURADOR_ARRAY = {
            {
                "/img/ico_martelo_preto.png", "Julgados",
                SIGLAS_PROCURADOR_LEGENDAS[0]
            },
            {
                "/img/deliberado.png", "Deliberação em Sessão",
                SIGLAS_PROCURADOR_LEGENDAS[1]
            },
            {
                "/img/retirado_pauta.png", "Retirados de Pauta",
                SIGLAS_PROCURADOR_LEGENDAS[2]
            },
            { "/img/pendente.png", "Pendentes", SIGLAS_PROCURADOR_LEGENDAS[3] },
            {
                "/img/icone_balao.png", "Sustentações Orais",
                SIGLAS_PROCURADOR_LEGENDAS[4]
            },
            { "/img/star.jpg", "Preferências", SIGLAS_PROCURADOR_LEGENDAS[5] },
            {
                "/img/checkin_green_16.png", "Acórdãos Assinados",
                SIGLAS_PROCURADOR_LEGENDAS[6]
            },
            {
                "/img/checkin_yellow_16.png",
                "Acórdãos Assinados Pelo Magistrado",
                SIGLAS_PROCURADOR_LEGENDAS[7]
            },
            {
                "/img/checkin_gray_16.png", "Acórdãos Não Assinados",
                SIGLAS_PROCURADOR_LEGENDAS[8]
            }
        };
    public static String[][] LEGENDAS_PROCURADOR_SESSAO_ENCERRADA_ARRAY = {
            {
                "/img/ico_martelo_normal.png",
                "Julgados sem Pendência de lançamentos",
                SIGLAS_PROCURADOR_LEGENDAS[9]
            },
            {
                "/img/ico_martelo_cinza.png",
                "Julgados com Pendência de lançamentos",
                SIGLAS_PROCURADOR_LEGENDAS[10]
            },
            {
                "/img/deliberado.png", "Deliberação em Sessão",
                SIGLAS_PROCURADOR_LEGENDAS[1]
            },
            {
                "/img/retirado_pauta.png", "Retirados de Pauta",
                SIGLAS_PROCURADOR_LEGENDAS[2]
            },
            { "/img/pendente.png", "Pendentes", SIGLAS_PROCURADOR_LEGENDAS[3] },
            {
                "/img/icone_balao.png", "Sustentações Orais",
                SIGLAS_PROCURADOR_LEGENDAS[4]
            },
            { "/img/star.jpg", "Preferências", SIGLAS_PROCURADOR_LEGENDAS[5] },
            {
                "/img/checkin_green_16.png", "Acórdãos Assinados",
                SIGLAS_PROCURADOR_LEGENDAS[6]
            },
            {
                "/img/checkin_yellow_16.png",
                "Acórdãos Assinados Pelo Magistrado",
                SIGLAS_PROCURADOR_LEGENDAS[7]
            },
            {
                "/img/checkin_gray_16.png", "Acórdãos Não Assinados",
                SIGLAS_PROCURADOR_LEGENDAS[8]
            }
    };
    
    public static String[] SIGLAS_SECRETARIO_LEGENDAS = {
            "DI", "DE", "O", "J", "DS", "RP", "PD", "SO", "P", "AA", "AAM",
            "ANA", "JSP", "JCP", "DIAP", "DINC", "DENC", "A", "ANC"
    };
    
    public static String[][] LEGENDAS_SECRETARIO_ARRAY = {
	    	{ "/img/divergencia.png", "Divergência", SIGLAS_SECRETARIO_LEGENDAS[0] },
	        { "/img/divergencia-pendente.png", "Divergência com análise pendente", SIGLAS_SECRETARIO_LEGENDAS[14] },
	        { "/img/divergencia-nao-concluida.png", "Divergência não concluída/liberada", SIGLAS_SECRETARIO_LEGENDAS[15] },
	        { "/img/destaque.png", "Destaque", SIGLAS_SECRETARIO_LEGENDAS[1] },
	        { "/img/destaque-nao-concluido.png", "Destaque não concluído/liberado", SIGLAS_SECRETARIO_LEGENDAS[16] },
	        { "/img/anotacao.png", "Anotação", SIGLAS_SECRETARIO_LEGENDAS[17] },
	        { "/img/anotacao-nao-concluida.png", "Anotação não concluída", SIGLAS_SECRETARIO_LEGENDAS[18] },
	        {
                "/img/ico_martelo_preto.png", "Julgados",
                SIGLAS_SECRETARIO_LEGENDAS[3]
            },
            {
                "/img/deliberado.png", "Deliberação em Sessão",
                SIGLAS_SECRETARIO_LEGENDAS[4]
            },
            {
                "/img/retirado_pauta.png", "Retirados de Pauta",
                SIGLAS_SECRETARIO_LEGENDAS[5]
            },
            { "/img/pendente.png", "Pendentes", SIGLAS_SECRETARIO_LEGENDAS[6] },
            {
                "/img/icone_balao.png", "Sustentações Orais",
                SIGLAS_SECRETARIO_LEGENDAS[7]
            },
            { "/img/star.jpg", "Preferências", SIGLAS_SECRETARIO_LEGENDAS[8] },
            {
                "/img/checkin_green_16.png", "Acórdãos Assinados",
                SIGLAS_SECRETARIO_LEGENDAS[9]
            },
            {
                "/img/checkin_yellow_16.png",
                "Acórdãos Assinados Pelo Magistrado",
                SIGLAS_SECRETARIO_LEGENDAS[10]
            },
            {
                "/img/checkin_gray_16.png", "Acórdãos Não Assinados",
                SIGLAS_SECRETARIO_LEGENDAS[11]
            }
    };
    
    public static String[][] LEGENDAS_SECRETARIO_SESSAO_ENCERRADA_ARRAY = {
	    	{ "/img/divergencia.png", "Divergência", SIGLAS_SECRETARIO_LEGENDAS[0] },
	        { "/img/divergencia-pendente.png", "Divergência com análise pendente", SIGLAS_SECRETARIO_LEGENDAS[14] },
	        { "/img/divergencia-nao-concluida.png", "Divergência não concluída/liberada", SIGLAS_SECRETARIO_LEGENDAS[15] },
	        { "/img/destaque.png", "Destaque", SIGLAS_SECRETARIO_LEGENDAS[1] },
	        { "/img/destaque-nao-concluido.png", "Destaque não concluído/liberado", SIGLAS_SECRETARIO_LEGENDAS[16] },
	        { "/img/anotacao.png", "Anotação", SIGLAS_SECRETARIO_LEGENDAS[17] },
	        { "/img/anotacao-nao-concluida.png", "Anotação não concluída", SIGLAS_SECRETARIO_LEGENDAS[18] },
	        {
                "/img/ico_martelo_normal.png",
                "Julgados sem Pendência de lançamentos",
                SIGLAS_SECRETARIO_LEGENDAS[12]
            },
            {
                "/img/ico_martelo_cinza.png",
                "Julgados com Pendência de lançamentos",
                SIGLAS_SECRETARIO_LEGENDAS[13]
            },
            {
                "/img/deliberado.png", "Deliberação em Sessão",
                SIGLAS_SECRETARIO_LEGENDAS[4]
            },
            {
                "/img/retirado_pauta.png", "Retirados de Pauta",
                SIGLAS_SECRETARIO_LEGENDAS[5]
            },
            { "/img/pendente.png", "Pendentes", SIGLAS_SECRETARIO_LEGENDAS[6] },
            {
                "/img/icone_balao.png", "Sustentações Orais",
                SIGLAS_SECRETARIO_LEGENDAS[7]
            },
            { "/img/star.jpg", "Preferências", SIGLAS_SECRETARIO_LEGENDAS[8] },
            {
                "/img/checkin_green_16.png", "Acórdãos Assinados",
                SIGLAS_SECRETARIO_LEGENDAS[9]
            },
            {
                "/img/checkin_yellow_16.png",
                "Acórdãos Assinados Pelo Magistrado",
                SIGLAS_SECRETARIO_LEGENDAS[10]
            },
            {
                "/img/checkin_gray_16.png", "Acórdãos Não Assinados",
                SIGLAS_SECRETARIO_LEGENDAS[11]
            }
    };
    
    
    /**
     * (fernando.junior - 17/01/2013) Adição de novas siglas referentes às novas legendas:
     * DIAP - Divergência com análise pendente
     * DINC - Divergência não concluída/liberada
     * DENC - Destaque não concluído/liberado
     * A - Anotação
     * ANC - Anotação não concluída 
     */
    public static String[] SIGLAS_LEGENDAS_VOTO = {
            "DI", "DE", "O", "P", "SO", "VNE", "VNL", "VL", "NA", "VR" , "DIAP" , "DINC" , "DENC" , "A" , "ANC"
    };
    
    /**
     * (fernando.junior - 17/01/2013) Adição de novas legendas 
     */
    public static String[][] LEGENDAS_ARRAY_VOTO = {
            { "/img/divergencia.png", "Divergência", SIGLAS_LEGENDAS_VOTO[0] },
            { "/img/divergencia-pendente.png", "Divergência com análise pendente", SIGLAS_LEGENDAS_VOTO[10] },
            { "/img/divergencia-nao-concluida.png", "Divergência não concluída/liberada", SIGLAS_LEGENDAS_VOTO[11] },
            { "/img/destaque.png", "Destaque", SIGLAS_LEGENDAS_VOTO[1] },
            { "/img/destaque-nao-concluido.png", "Destaque não concluído/liberado", SIGLAS_LEGENDAS_VOTO[12] },
            { "/img/anotacao.png", "Anotação", SIGLAS_LEGENDAS_VOTO[13] },
            { "/img/anotacao-nao-concluida.png", "Anotação não concluída", SIGLAS_LEGENDAS_VOTO[14] },
            { "/img/star.jpg", "Preferências", SIGLAS_LEGENDAS_VOTO[3] },
            {
                "/img/icone_balao.png", "Sustentações Orais",
                SIGLAS_LEGENDAS_VOTO[4]
            },
            {
                "/img/search_red_16.png", "Voto não elaborado",
                SIGLAS_LEGENDAS_VOTO[5]
            },
            {
                "/img/search_yellow_16.png", "Voto elaborado e não liberado",
                SIGLAS_LEGENDAS_VOTO[6]
            },
            {
                "/img/search_green_16.png", "Voto elaborado e liberado",
                SIGLAS_LEGENDAS_VOTO[7]
            },
            {
                "/img/search_gray_16.png", "Voto do relator não liberado",
                SIGLAS_LEGENDAS_VOTO[9]
            }
    };
    
    /**
     * (fernando.junior - 17/01/2013) Adição de novas legendas 
     */
    public static String[][] LEGENDAS_ARRAY_VOTO_SECRETARIO = {
        	{ "/img/divergencia.png", "Divergência", SIGLAS_LEGENDAS_VOTO[0] },
        	{ "/img/divergencia-pendente.png", "Divergência com análise pendente", SIGLAS_LEGENDAS_VOTO[10] },
        	{ "/img/divergencia-nao-concluida.png", "Divergência não concluída/liberada", SIGLAS_LEGENDAS_VOTO[11] },
        	{ "/img/destaque.png", "Destaque", SIGLAS_LEGENDAS_VOTO[1] },
        	{ "/img/destaque-nao-concluido.png", "Destaque não concluído/liberado", SIGLAS_LEGENDAS_VOTO[12] },
        	{ "/img/anotacao.png", "Anotação", SIGLAS_LEGENDAS_VOTO[13] },
        	{ "/img/anotacao-nao-concluida.png", "Anotação não concluída", SIGLAS_LEGENDAS_VOTO[14] },
            { "/img/star.jpg", "Preferências", SIGLAS_LEGENDAS_VOTO[3] },
            {
                "/img/icone_balao.png", "Sustentações Orais",
                SIGLAS_LEGENDAS_VOTO[4]
            },
            {
                "/img/search_green_16.png", "Voto elaborado e liberado",
                SIGLAS_LEGENDAS_VOTO[7]
            },
            {
                "/img/search_gray_16.png", "Voto do relator não liberado",
                SIGLAS_LEGENDAS_VOTO[9]
            },
            { "/img/naoAnalisado.png", "Não Analisado", SIGLAS_LEGENDAS_VOTO[8] }
    };
}
