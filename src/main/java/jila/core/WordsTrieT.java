package jila.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Modified trie. Works only with Word objects.
 * Implements map only for compatibility reasons.
 */
public class WordsTrieT implements Map {
    public static int COUNT = 0;
    public static boolean LOGGING = true;
    protected char c = '\u0000';                        // character
    protected WordsTrieT left, mid, right;  // left, middle, and right subtries
    protected Word word;
    
    /**
     * Constructors
     */
    
    public WordsTrieT() {
        c = 's';
        COUNT++;
    }
    
    public WordsTrieT(char ch, Word word, String letter, String pre) {
        if (word == null || letter == null 
                || ch == '\u0000') {
            return;
        }
        c = ch;       
        COUNT++;
        add(word, letter, pre);
    }
    
    /**
     * Adds the word into the WordsTrie.
     * If the word is already in there, its count
     * will be increased by one. Similar words with the postfix's
     * levenshtein distance <= 3 will be considered as the same.
     * @param word
     */
    public void add(Word word) {
        char st = word.getWord().charAt(0);
        WordsTrieT tree;
        
        if (st < c) {
            if (left != null) {
                left.add(word, word.getWord(), "");
            } else {
                left = new WordsTrieT(st, word, word.getWord(), "");
            }
            
        } else if (st == c) {            
            String letter = word.getWord().substring(1,
                    word.getWord().length());
            String pre = String.valueOf(st);
            st = letter.charAt(0);
            if (mid != null) {
                mid.add(word, letter, pre);
            } else {
                mid = new WordsTrieT(st, word, letter, pre);
            }
            
        } else if (st > c) {
            if (right != null) {
                right.add(word, word.getWord(), "");
            } else {
                right = new WordsTrieT(st, word, word.getWord(), "");
            }
        }
    }
    
    /**
     * Adds the word to the WordsTrie. Inner method
     * @param word      - the word to add
     * @param letter    - current ending of the word
     * @param pre       - prefix (path of letters, which were visited)
     */
    protected void add(Word word, String letter, String pre) {
        if (LOGGING) {
            System.out.println("add(" 
                    + word + ", " + letter + ", " + pre + ")");
        }
        
        char st = letter.charAt(0);
        if (st < c) {
            if (left != null) {
                left.add(word, letter, pre);
            } else {
                left = new WordsTrieT(st, word, letter, pre);
            }
            
        } else if (st == c) {
            // if there is the similar word, increase its counter
/*            if (this.word != null) {
                int dist = dist(this.word, word);
                if (dist <= 3) {
                    if (LOGGING) {
                        System.out.println("Distance: " + dist);
                        System.out.println(this.word + " == " + word);
                    }
                    this.word.setCount(this.word.getCount() + 1);
                    return;
                }
            }*/
            
            if (letter.length() == 1) {
                // Compression
                if (this.word == null) {
                    this.word = word;
                    if (LOGGING) {
                        System.out.println(c + " -> the word saved");
                    }
                    
                } else {
                    if (this.word.getWord().equals(word.getWord())) {
                        this.word.setCount(this.word.getCount() + 1);
                        
                    } else {
                        Word w = this.word;
                        this.word = word;
                        if (LOGGING) {
                            System.out.println(c + " -> the word saved");
                            System.out.println(w.getWord() + " will be moved");
                        }
                        this.add(w, w.getWord()
                                .substring(pre.length(), w.getWord().length()), pre);
                    }
                }
                
            } else {
                if (this.word == null 
                        && left == null && right == null && mid == null) {
                    if (LOGGING) {
                        System.out.println(c + " -> the word saved");
                    }
                    this.word = word;
                    
                } else {
                    if (this.word != null && this.word.getWord().equals(word.getWord())) {
                        this.word.setCount(this.word.getCount() + 1);
                        if (LOGGING) {
                            System.out.println(c + " -> the word was here");
                        }
                        
                    } else {
                        letter = letter.substring(1, letter.length());
                        pre = pre + st;
                        st = letter.charAt(0);
                        if (mid == null) {
                            mid = new WordsTrieT(st, word, letter, pre);
                        } else {
                            mid.add(word, letter, pre);
                        }
                    }
                }
            }
            
        } else if (st > c) {
            if (right != null) {
                right.add(word, letter, pre);
            } else {
                right = new WordsTrieT(st, word, letter, pre);
            }
        }
        
        /*
        // If there no further path opened,
        // save the word here, making compressed leaf
        if (map == null && this.word == null) {
            this.word = word;
            return;
        }
        
        // The word is here already -> increase its counter
        if (this.word != null 
                && this.word.getWord().equals(word.getWord())) {
            this.word.setCount(this.word.getCount() + 1);
            return;
        }
        
        // If length > 0, we need to search further path
        if (letter.length() > 0) {
            Character st = letter.charAt(0);
            if (LOGGING) {
                System.out.print(st + " (" + st.hashCode() + ") -> ");
            }
            WordsTrie tree;
            // There is no further path
            if (map == null || map[st - 97] == null) {
                // if there is the similar word, increase its counter
                if (this.word != null && map == null) {
                    int dist = dist(this.word, word);
                    if (dist <= 3) {
                        if (LOGGING) {
                            System.out.println("Distance: " + dist);
                            System.out.println(this.word + " == " + word);
                        }
                        this.word.setCount(this.word.getCount() + 1);
                        return;
                    }
                }
                // No similar words were found, create further path
                tree = new WordsTrie(
                        word, letter.substring(1, letter.length()), pre + st);
                if (map == null) {
                    map = new WordsTrie[26];
                }
                map[st - 97] = tree;
                
            // The path is found, go further
            } else {
                tree = map[st - 97];
                tree.add(word, letter.substring(1, letter.length()), pre + st);
            }
        }

        // if it this word is not compressed leaf and not the same
        // word that we are searching for, than do nothing
        if (this.word != null && !pre.equals(this.word.getWord()) 
                && !this.word.getWord().equals(word.getWord())) {
            Word w = this.word;
            if (letter.length() == 0) {
                this.word = word;
            } else {
                this.word = null;
            }
            String post = w.getWord().substring(
                    pre.length(), w.getWord().length());
            add(w, post, pre);
            
        // if length == 0, this is the node, where we need
        // to save the specified word
        } else {
            if (letter.length() == 0) {
                this.word = word;
            }
        }*/
    }
    
    /**
     * Returns the word, if it is in the WordsTrie,
     * returns null, if not.
     * @param word
     * @return
     */
    public Word get(String word) {
        if (LOGGING) {
            System.out.println("get(" + word + ")");
        }
        char st = word.charAt(0);
        
        if (LOGGING) {
            System.out.println(c + " / search -> " + st);
        }
        
        if (st < c) {
            if (left != null) {
                return left.get(word);
            } else {
                return null;
            }
            
        } else if (st == c) {
            if (this.word != null && this.word.getWord().endsWith(word)) {
                return this.word;
            }
            if (word.length() > 1) {
                if (mid != null) {
                    return mid.get(word.substring(1, word.length()));
                } else {
                    return null;
                }
                
            } else {
                return this.word;
            }
            
        } else if (st > c) {
            if (right != null) {
                return right.get(word);
            } else {
                return null;
            }
        }
        
        return null;
    }
    
    @Override
    /**
     * Returns Collection of AbstractWord objects,
     * which are inside the WordsTrie
     */
    public Collection<AbstractWord> values() {
        //System.out.println("Amount of nodes: " + WordsTrieT.COUNT);
        
        Collection<AbstractWord> result = new ArrayList<>();
        if (word != null) {
            result.add(word);
        }
        
        if (left != null) {
            result.addAll(left.values());
        }
        if (mid != null) {
            result.addAll(mid.values());
        }
        if (right != null) {
            result.addAll(right.values());
        }
        return result;
    }
    
/*    *//**
     * Returns AbstractWord array, made from elements
     * inside the WordsTrie
     * @return
     *//*
    public AbstractWord[] toArray() {
        Collection<AbstractWord> list = values();
        AbstractWord[] res = new AbstractWord[list.size()];
        res = list.toArray(res);
        return res;
    }*/
    
    /**
     * Returns levenshtein distance of postfixies' of
     * two Word objects. Returns 100, if the words are
     * absolutely different (have different roots)
     * @param word1
     * @param word2
     * @return
     */
    protected static int dist(Word word1, Word word2) {
        final int L = 3; // the length of the postfix
        String w1 = word1.getWord();
        String w2 = word2.getWord();
        if (Math.abs(w1.length() - w2.length()) > L) {
            return 100;
        }
        
        String w1End;
        String w2End;
        
        int maxL = Math.max(w1.length(), w2.length());
        
        if (!w1.substring(0, maxL - L)
                .equals(w2.substring(0, maxL - L))) {
            return 100;
        }
        
        w1End = w1.substring(maxL - L, w1.length());
        w2End = w2.substring(maxL - L, w2.length());
        
        return Utilities.levenshteinDistance(w1End, w2End);
    }
    
    
    // Test client
    public static void main(String[] args) {
        WordsTrieT tree = new WordsTrieT();
        String w1 = "taste";
        String w2 = "task";
        String w3 = "unity";        
        
        tree.add(new Word(w1));
        
        tree.add(new Word(w2));
        tree.add(new Word(w3));
        /*tree.add(new Word("table"));*/
        /*System.out.println(tree.contains("test"));*/
        /*System.out.println(tree.contains("word"));
        System.out.println(tree.get("word"));*/
        
        
        /*tree.add(new Word("tess"));*/
        
        System.out.println("Size: " + tree.values().size());
        
        System.out.println("retreiving " + w1 + " -------");
        System.out.println(tree.get(w1));
        System.out.println("retreiving " + w2 + " -------");
        System.out.println(tree.get(w2));
        System.out.println("retreiving " + w3 + " -------");
        System.out.println(tree.get(w3));
    }
    
    

    @Override
    public String toString() {
        return "Letter [word=" + word + "]";
    }

    
    // Methods from MAP. UNIMPLEMENTED
    
    @Override
    public void clear() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean containsKey(Object key) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Set entrySet() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object get(Object key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Set keySet() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object put(Object key, Object value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void putAll(Map m) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Object remove(Object key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int size() {
        // TODO Auto-generated method stub
        return 0;
    }
}
