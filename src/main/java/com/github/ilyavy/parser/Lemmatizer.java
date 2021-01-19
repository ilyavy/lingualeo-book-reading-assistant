package com.github.ilyavy.parser;

import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

/**
 * Lemmatizer based on StanfordCoreNLP library. Created by the manual.
 */
public class Lemmatizer {

    protected StanfordCoreNLP pipeline;

    /**
     * Constructor.
     */
    public Lemmatizer() {
        // Create StanfordCoreNLP object properties, with POS tagging
        // (required for lemmatization), and lemmatization
        Properties props;
        props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");

        this.pipeline = new StanfordCoreNLP(props);
    }

    // TODO: increase performance, no need to split by sentences
    /**
     * Lemmatizes the text sentence by sentence.
     * @param documentText string representation of text to be lemmatized
     * @return string representation of text with words lemmatized
     */
    public String lemmatize(String documentText) {
        StringBuilder lemmas = new StringBuilder();
        // Create an empty Annotation just with the given text
        Annotation document = new Annotation(documentText);
        // run all Annotators on this text
        this.pipeline.annotate(document);
        // Iterate over all of the sentences found
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            // Iterate over all tokens in a sentence
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                // Retrieve and add the lemma for each word into the
                // list of lemmas
                lemmas.append(" ").append(token.get(CoreAnnotations.LemmaAnnotation.class));
            }
        }
        return lemmas.toString();
    }
}
