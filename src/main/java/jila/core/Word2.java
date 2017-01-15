package jila.core;

public class Word2 extends Word {
    protected Word2[] context = null;
    
    //Constructors
    public Word2() {
        
    }
    
    public Word2(final String word) {
        super(word);
    }
    
    public Word2(final String word, final String translate) {
        super(word, translate);
    }

    
    public Word2[] getContextArray() {
        return context;
    }
    
    
    @Override
    public String getContext() {
        StringBuilder sb = new StringBuilder();
        
       /* Pair p = contextMap.get(sentenceId);
        Word2 pr = p.getPrev();
        while (pr != null) {
            sb.insert(0, pr.getWord() + " ");
            p = pr.getContextMap().get(sentenceId);
            pr = p.getPrev();
        }
        
        p = contextMap.get(sentenceId);
        Word2 next = p.getNext();
        while (next != null) {
            sb.append(next.getWord() + " ");
            p = next.getContextMap().get(sentenceId);
            next = p.getNext();
        }*/
        
        for (int i = 0; i < context.length; i++) {
            String w = context[i].getWord();
            if (i == 0) {
                w = Character.toUpperCase(w.charAt(0)) + w.substring(1);
            }
            sb.append(w + " ");
        }
        
        return sb.toString();
    }

    public void setContext(Word2[] context) {
        this.context = context;
    }
    
/*    public void addSentence(
            int sentenceId, Word2 previous, Word2 next) {
        Pair p = new Pair(previous, next);
        contextMap.put(sentenceId, p);
    }
    
    
    public int getSentenceId() {
        return sentenceId;
    }

    public void setSentenceId(int sentenceId) {
        this.sentenceId = sentenceId;
    }

    public Map<Integer, Pair> getContextMap() {
        return contextMap;
    }

    public void setContextMap(Map<Integer, Pair> contextMap) {
        this.contextMap = contextMap;
    }
*/


    public static class Pair {
        protected Word2 next;
        protected Word2 prev;
        
        public Pair(Word2 prev, Word2 next) {
            this.next = next;
            this.prev = prev;
        }
        

        public Word2 getNext() {
            return next;
        }

        public void setNext(Word2 next) {
            this.next = next;
        }

        public Word2 getPrev() {
            return prev;
        }

        public void setPrev(Word2 prev) {
            this.prev = prev;
        }
    }



    @Override
    public void setContext(String context) {
        // TODO Auto-generated method stub
        
    }
}
