package jila.core;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public abstract class Word implements Comparable<Word>, Jsonable {
    protected String word = "";
    protected String translate = "";
    protected boolean known = false;
    protected long count = 1;
    
    //Constructors
    public Word() {
        
    }
    
    public Word(final String word) {
        this.word = word.toLowerCase();
    }
    
    public Word(final String word, final String translate) {
        this(word);
        setTranslate(translate);
    }
    
    
    //Getters
    public String getWord() {
        return word;
    }
    
    public String getTranslate() {
        return translate;
    }
    
    public abstract String getContext();
    
    public boolean isKnown() {
        return known;
    }
    
    public long getCount() {
        return count;
    }
    
    
    //Setters
    public void setTranslate(final String translate) {
        this.translate = translate;
    }
    
    public abstract void setContext(final String context);
    
    public void setKnown(final boolean known) {
        this.known = known;
    }
    
    public void setCount(final long newCount) {
        count = newCount;
    }
    

    //Overridden methods
    @Override
    public String toString() {
        return word;
    }

    @Override
    public int compareTo(final Word that) {
        return word.compareTo(that.word);
    }
    
    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof Word)) {
            return false;
        }
        Word that = (Word) o;
        if (word.equals(that.word) && count == that.count) {
            return true;
        }
        return false;
    }

    @Override
    public JsonObject toJsonObject() {
        return toJsonObject(null);
    }
    
    @Override
    public JsonObject toJsonObject(Map<String, String> attributes) {
        JsonBuilderFactory factory = Json.createBuilderFactory(null);
        JsonObjectBuilder builder = factory.createObjectBuilder();

        builder.add("word", getWord());
        builder.add("translate", getTranslate());
        builder.add("context", getContext());
        builder.add("known", isKnown());
        builder.add("count", getCount());
        
        if (attributes != null) {
            for (Entry<String, String> entry : attributes.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }
        
        return builder.build();
    }
    
    
    //Methods
    public static Comparator<Word> countOrder() {
        return new CountComparator();
    }
    
    
    //Classes
    protected static class CountComparator implements Comparator<Word> {
        @Override
        public int compare(Word o1, Word o2) {
            Long c1 = Long.valueOf(o1.count);
            Long c2 = Long.valueOf(o2.count);
            return c1.compareTo(c2);
        }
        
    }
}
