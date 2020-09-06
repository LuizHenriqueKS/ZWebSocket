package br.zul.websocket.util;

/**
 *
 * @author luizh
 */
public class StrHelper {

    //==========================================================================
    //VARIÁVEIS
    //==========================================================================
    private final String str;
    
    //==========================================================================
    //CONSTRUTORES
    //==========================================================================
    public StrHelper(String str) {
        this.str = str;
    }
    
    //==========================================================================
    //MÉTODOS PÚBLICOS SOBRESCRITOS
    //==========================================================================
    @Override
    public String toString() {
        return str;
    }    
    
    //==========================================================================
    //MÉTODOS PÚBLICOS
    //==========================================================================
    public StrHelper from(String... patterns){
        int start = getLowestStart(patterns);
        if (start==-1) return this;
        return new StrHelper(str.substring(start));
    }
    
    public StrHelper till(String... patterns){
        int end = getLowestEnd(patterns);
        if (end==-1) return this;
        return new StrHelper(str.substring(0, end));
    }

    public int getLowestStart(String... patterns) {
        int lowestStart = -1;
        for (String pattern:patterns){
            int index = str.indexOf(pattern);
            if (index==-1) continue;
            int start = index + pattern.length();
            if (lowestStart>start||lowestStart==-1){
                lowestStart = start;
            }
        }
        return lowestStart;
    }

    public int getLowestEnd(String... patterns) {
        int lowestEnd = -1;
        for (String pattern:patterns){
            int end = str.indexOf(pattern);
            if (end==-1) continue;
            if (lowestEnd>end||lowestEnd==-1){
                lowestEnd = end;
            }
        }
        return lowestEnd;
    }

    public boolean contains(String... patterns) {
        return getLowestStart(patterns)!=-1;
    }
    
}

