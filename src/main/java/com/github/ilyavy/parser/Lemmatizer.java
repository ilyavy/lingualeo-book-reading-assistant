package com.github.ilyavy.parser;

import java.util.*;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import io.github.pepperkit.corenlp.stopwords.StopWordsAnnotator;

/**
 * Lemmatizer based on StanfordCoreNLP library. Created by the manual.
 */
public class Lemmatizer {

    /**
     * Minimal length of the word, which is accounted for.
     */
    protected static final String WORD_LENGTH_THRESHOLD = "4";

    protected StanfordCoreNLP pipeline;

    /**
     * Constructor.
     */
    public Lemmatizer() {
        Properties props;
        props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, stopwords");
        props.setProperty("customAnnotatorClass.stopwords", "io.github.pepperkit.corenlp.stopwords.StopWordsAnnotator");
        props.setProperty("ssplit.isOneSentence", "true");
        props.setProperty("stopwords.shorterThan", WORD_LENGTH_THRESHOLD);
        props.setProperty("stopwords.withLemmasShorterThan", WORD_LENGTH_THRESHOLD);
        props.setProperty("stopwords.withPosCategories",
                "NNP,NNPS," + // proper noun singular and plural
                        "PDT," + // predeterminer
                        "IN,CC," + // conjunction and coordinating conjunction (but, and etc.)
                        "DT," + // determiner - the, a, etc.
                        "UH," + // interjection - my, his, oh, uh etc.
                        "FW," + // foreign word
                        "MD," + // modal verb
                        "RP," + // particle
                        "PRP,PRP$," + // personal pronoun
                        "EX," + // existential there
                        "POS," + // possessive ending: 's
                        "SYM," + // symbol
                        "WDT,WP,WP$," + // wh-determiner (who), wh-pronoun (who, what, whom) and possessive wh-pronoun (whose)
                        "WRB" // wh-adverb
        );
        props.setProperty("stopwords.customListResourcesFilePath", "stopwords.txt");

        this.pipeline = new StanfordCoreNLP(props);
    }

    public List<String> lemmatize(String text) {
        Annotation document = new Annotation(text);
        this.pipeline.annotate(document);

        List<String> lemmas = new ArrayList<>();
        List<CoreLabel> tokens = document.get(CoreAnnotations.TokensAnnotation.class);

        for (CoreLabel token : tokens) {
            if (!token.get(StopWordsAnnotator.class)) {
                lemmas.add(token.get(CoreAnnotations.LemmaAnnotation.class));
            }
        }

        return lemmas;
    }
}
