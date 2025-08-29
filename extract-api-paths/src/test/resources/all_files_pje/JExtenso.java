package br.com.infox.editor.interpretadorDocumento;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;

public class JExtenso {  
    private boolean ehMoeda = false;
    
    private ArrayList nro = new ArrayList();

    private BigInteger num;  

    private String Qualificadores[][] = { { "centavo", "centavos" },  
            { "", "" }, { "mil", "mil" }, { "milhão", "milhões" },  
            { "bilhão", "bilhões" }, { "trilhão", "trilhões" },  
            { "quatrilhão", "quatrilhões" }, { "quintilhão", "quintilhões" },  
            { "sextilhão", "sextilhões" }, { "septilhão", "septilhões" } };  

    private String Numeros[][] = {  
            { "zero", "um", "dois", "três", "quatro", "cinco", "seis", "sete",  
                "oito", "nove", "dez", "onze", "doze", "treze", "quatorze",  
                "quinze", "dezesseis", "dezessete", "dezoito", "dezenove" },  
            { "vinte", "trinta", "quarenta", "cinquenta", "sessenta",  
                "setenta", "oitenta", "noventa" },  
            { "cem", "cento", "duzentos", "trezentos", "quatrocentos",  
                "quinhentos", "seiscentos", "setecentos", "oitocentos",  
                "novecentos" } };  

    public JExtenso(double dec) {  
        ehMoeda = true;
        setNumber(new BigDecimal(dec));  
    }  

    public JExtenso(int dec) {  
        ehMoeda = false;
        setNumber(new BigDecimal(dec));  
    }  

    private void setNumber(BigDecimal dec) {  
        // Converte para inteiro arredondando os centavos  
        num = dec.setScale(2, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100)).toBigInteger();  

        // Adiciona valores  
        nro.clear();  
        if (num.equals(BigInteger.ZERO)) {  
            // Centavos  
            nro.add(new Integer(0));  
            // Valor  
            nro.add(new Integer(0));  
        } else {  
            // Adiciona centavos  
            addRemainder(100);  

            // Adiciona grupos de 1000  
            while (!num.equals(BigInteger.ZERO)) {  
            addRemainder(1000);  
            }  
        }  
    }  

    public void show() {  
        Iterator valores = nro.iterator();  

        while (valores.hasNext()) {  
            System.out.println(((Integer) valores.next()).intValue());  
        }  
        System.out.println(toString());  
    }  

    @Override
    public String toString() {  
        StringBuilder buf = new StringBuilder();  

        int ct;  

        for (ct = nro.size() - 1; ct > 0; ct--) {  
            // Se ja existe texto e o atual nÃ£o Ã© zero  
            if (buf.length() > 0 && !ehGrupoZero(ct)) {  
                if (((Integer) nro.get(ct)).intValue() == 1)
                    buf.append(" e ");
                else
                    buf.append(", ");  
            }  
            buf.append(numToString(((Integer) nro.get(ct)).intValue(), ct));  
        }
        if ((ehMoeda) && (buf.length() > 0)) {
            if (ehUnicoGrupo())  
                buf.append(" de ");  
            while (buf.toString().endsWith(" ")){  
                buf.setLength(buf.length() - 1); 
            }
            if (nro.size() == 2 && ((Integer) nro.get(1)).intValue() == 1) {  
                buf.append(" real");  
            } else {  
                buf.append(" reais");  
            }  
            if (((Integer) nro.get(0)).intValue() != 0) {  
                buf.append(" e ");  
            }  
        }  
        if (((Integer) nro.get(0)).intValue() != 0) {  
            buf.append(numToString(((Integer) nro.get(0)).intValue(), 0));  
        }  
        return buf.toString().trim();  
    }  

    private boolean ehPrimeiroGrupoUm() {  
        if (((Integer) nro.get(nro.size() - 1)).intValue() == 1)  
            return true;  
        return false;  
    }  

   private void addRemainder(int divisor) {  
        // Encontra newNum[0] = num modulo divisor, newNum[1] = num dividido  
        // divisor  
        BigInteger[] newNum = num.divideAndRemainder(BigInteger  
            .valueOf(divisor));  

        // Adiciona modulo  
        nro.add(new Integer(newNum[1].intValue()));  

        // Altera numero  
        num = newNum[0];  
    }  

    private boolean temMaisGrupos(int ps) {  
        for (; ps > 0; ps--) {  
            if (((Integer) nro.get(ps)).intValue() != 0) {  
            return true;  
            }  
        }  

        return false;  
    }  

    private boolean ehUltimoGrupo(int ps) {  
        return (ps > 0) && ((Integer) nro.get(ps)).intValue() != 0  
            && !temMaisGrupos(ps - 1);  
    }  

    private boolean ehUnicoGrupo() {  
        if (nro.size() <= 3)  
            return false;  
        if (!ehGrupoZero(1) && !ehGrupoZero(2))  
            return false;  
        for (int i = 1; i < 3; i++) {  
            if (((Integer) nro.get(i)).intValue() != 0)
                return false;
        }  
        return true;  
    }  

    boolean ehGrupoZero(int ps) {  
        if (ps <= 0 || ps >= nro.size())  
            return true;  
        return ((Integer) nro.get(ps)).intValue() == 0;  
    }  

    private String numToString(int numero, int escala) {  
        int unidade = (numero % 10);  
        int dezena = (numero % 100); // * nao pode dividir por 10 pois  
                                // verifica de 0..19  
        int centena = (numero / 100);  
        StringBuilder buf = new StringBuilder();  

        if (numero != 0) {  
            if (centena != 0) {  
                if (dezena == 0 && centena == 1) {  
                    buf.append(Numeros[2][0] );  
                } else {  
                    buf.append(Numeros[2][centena]);  
                }  
            }  

            if ((buf.length() > 0) && (dezena != 0)) {  
                buf.append(" e ");  
            }  
            if (dezena > 19) {  
                dezena /= 10;  
                buf.append(Numeros[1][dezena - 2]);  
                if (unidade != 0) {  
                    buf.append(" e ");  
                    buf.append(Numeros[0][unidade]);  
                }  
            } else if (centena == 0 || dezena != 0) {  
                buf.append(Numeros[0][dezena]);  
            }  

            buf.append(" ");  
            if (numero == 1) {  
                buf.append(Qualificadores[escala][0]);  
            } else {  
                buf.append(Qualificadores[escala][1]);  
            }  
        }
        else if (!ehMoeda)
            buf.append("zero");

        return buf.toString();  
    }  
}  