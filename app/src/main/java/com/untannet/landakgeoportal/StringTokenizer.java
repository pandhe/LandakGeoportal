package com.untannet.landakgeoportal;

import java.util.NoSuchElementException;

@SuppressWarnings("rawtypes")
public class StringTokenizer implements java.util.Enumeration{

    protected String text;

    protected int strLength;

    protected String nontokenDelims;

    protected String tokenDelims;

    protected int position;

    protected boolean emptyReturned;

    protected char maxDelimChar;

    protected boolean returnEmptyTokens;

    protected int delimsChangedPosition;

    /**
     * A cache of the token count.  This variable should be -1 if the token
     * have not yet been counted. It should be greater than or equal to zero
     * if the tokens have been counted.
     *
     * @since ostermillerutils 1.00.00
     */
    protected int tokenCount;


    public StringTokenizer(String text, String nontokenDelims, String tokenDelims){
        this(text, nontokenDelims, tokenDelims, false);
    }


    public StringTokenizer(String text, String nontokenDelims, String tokenDelims, boolean returnEmptyTokens){
        setDelims(nontokenDelims, tokenDelims);
        setText(text);
        setReturnEmptyTokens(returnEmptyTokens);
    }

    public StringTokenizer(String text, String delims, boolean delimsAreTokens){
        this(text, (delimsAreTokens ? null : delims), (delimsAreTokens ? delims : null));
    }


    public StringTokenizer(String text, String nontokenDelims){
        this(text, nontokenDelims, null);
    }


    public StringTokenizer(String text){
        this(text, " \t\n\r\f", null);
    }


    public void setText(String text){
        if (text == null){
            throw new NullPointerException();
        }
        this.text = text;
        strLength = text.length();
        emptyReturned = false;
        // set the position to start evaluation to zero
        // unless the string has no length, in which case
        // the entire string has already been examined.
        position = (strLength > 0 ? 0: -1);
        // because the text was changed since the last time the delimiters
        // were changed we need to set the delimiter changed position
        delimsChangedPosition = 0;
        // The token count changes when the text changes
        tokenCount = -1;
    }


    private void setDelims(String nontokenDelims, String tokenDelims){
        this.nontokenDelims = nontokenDelims;
        this.tokenDelims = tokenDelims;
        // If we change delimiters, we do not want to start fresh,
        // without returning empty tokens.
        // the delimiter changed position can never be less than
        // zero, unlike position.
        delimsChangedPosition = (position != -1 ? position : strLength);
        // set the max delimiter
        maxDelimChar = 0;
        for (int i=0; nontokenDelims != null && i < nontokenDelims.length(); i++){
            if (maxDelimChar < nontokenDelims.charAt(i)){
                maxDelimChar = nontokenDelims.charAt(i);
            }
        }
        for (int i=0; tokenDelims != null && i < tokenDelims.length(); i++){
            if (maxDelimChar < tokenDelims.charAt(i)){
                maxDelimChar = tokenDelims.charAt(i);
            }
        }
        // Changing the delimiters may change the number of tokens
        tokenCount = -1;
    }



    public boolean hasMoreTokens(){

        // handle the easy case in which the number
        // of tokens has been counted.
        if (tokenCount == 0){
            return false;
        } else if (tokenCount > 0){
            return true;
        }

        // copy over state variables from the class to local
        // variables so that the state of this object can be
        // restored to the state that it was in before this
        // method was called.
        int savedPosition = position;
        boolean savedEmptyReturned = emptyReturned;

        int workingPosition = position;
        boolean workingEmptyReturned = emptyReturned;
        boolean onToken = advancePosition();
        while(position != workingPosition ||
                emptyReturned != workingEmptyReturned){
            if (onToken){
                // restore object state
                position = savedPosition;
                emptyReturned = savedEmptyReturned;
                return true;
            }
            workingPosition = position;
            workingEmptyReturned = emptyReturned;
            onToken = advancePosition();
        }

        // restore object state
        position = savedPosition;
        emptyReturned = savedEmptyReturned;
        return false;
    }

    /**
     * Returns the next token from this string tokenizer.
     * <p>
     * The current position is set after the token returned.
     *
     * @return the next token from this string tokenizer.
     * @throws NoSuchElementException if there are no more tokens in this tokenizer's string.
     *
     * @since ostermillerutils 1.00.00
     */
    public String nextToken(){
        int workingPosition = position;
        boolean workingEmptyReturned = emptyReturned;
        boolean onToken = advancePosition();
        while(position != workingPosition ||
                emptyReturned != workingEmptyReturned){
            if (onToken){
                // returning a token decreases the token count
                tokenCount--;
                return (emptyReturned ? "" : text.substring(workingPosition, (position != -1) ? position : strLength));
            }
            workingPosition = position;
            workingEmptyReturned = emptyReturned;
            onToken = advancePosition();
        }
        throw new NoSuchElementException();
    }


    public boolean skipDelimiters(){
        int workingPosition = position;
        boolean workingEmptyReturned = emptyReturned;
        boolean onToken = advancePosition();

        // skipping delimiters may cause the number of tokens to change
        tokenCount = -1;

        while(position != workingPosition ||
                emptyReturned != workingEmptyReturned){
            if (onToken){
                // restore the state to just as it was before we found
                // this token and return
                position = workingPosition;
                emptyReturned = workingEmptyReturned;
                return true;
            }
            workingPosition = position;
            workingEmptyReturned = emptyReturned;
            onToken = advancePosition();
        }

        // the end of the string was reached
        // without finding any tokens
        return false;
    }


    public int countTokens(){

        // return the cached token count if a cache
        // is available.
        if (this.tokenCount >=0){
            return this.tokenCount;
        }

        int tokenCount = 0;

        // copy over state variables from the class to local
        // variables so that the state of this object can be
        // restored to the state that it was in before this
        // method was called.
        int savedPosition = position;
        boolean savedEmptyReturned = emptyReturned;

        int workingPosition = position;
        boolean workingEmptyReturned = emptyReturned;
        boolean onToken = advancePosition();
        while(position != workingPosition ||
                emptyReturned != workingEmptyReturned){
            if (onToken){
                tokenCount++;
            }
            workingPosition = position;
            workingEmptyReturned = emptyReturned;
            onToken = advancePosition();
        }

        // restore object state
        position = savedPosition;
        emptyReturned = savedEmptyReturned;

        // Save the token count in case this is called again
        // so we wouldn't have to do so much work.
        this.tokenCount = tokenCount;

        return tokenCount;
    }

    /**
     * Set the delimiters used to this set of (nontoken) delimiters.
     *
     * @param delims the new set of nontoken delimiters (the set of token delimiters will be empty).
     *
     * @since ostermillerutils 1.00.00
     */
    public void setDelimiters(String delims){
        setDelims(delims, null);
    }

    /**
     * Set the delimiters used to this set of delimiters.
     *
     * @param delims the new set of delimiters.
     * @param delimsAreTokens flag indicating whether the first parameter specifies
     *    token or nontoken delimiters: false -- the first parameter specifies nontoken
     *    delimiters, the set of token delimiters is empty; true -- the first parameter
     *    specifies token delimiters, the set of nontoken delimiters is empty.
     *
     * @since ostermillerutils 1.00.00
     */
    public void setDelimiters(String delims, boolean delimsAreTokens){
        setDelims((delimsAreTokens ? null : delims), (delimsAreTokens ? delims : null));
    }

    /**
     * Set the delimiters used to this set of delimiters.
     *
     * @param nontokenDelims the new set of nontoken delimiters.
     * @param tokenDelims the new set of token delimiters.
     *
     * @since ostermillerutils 1.00.00
     */
    public void setDelimiters(String nontokenDelims, String tokenDelims){
        setDelims(nontokenDelims, tokenDelims);
    }

    /**
     * Set the delimiters used to this set of delimiters.
     *
     * @param nontokenDelims the new set of nontoken delimiters.
     * @param tokenDelims the new set of token delimiters.
     * @param returnEmptyTokens true if empty tokens may be returned; false otherwise.
     *
     * @since ostermillerutils 1.00.00
     */
    public void setDelimiters(String nontokenDelims, String tokenDelims, boolean returnEmptyTokens){
        setDelims(nontokenDelims, tokenDelims);
        setReturnEmptyTokens(returnEmptyTokens);
    }


    public int countTokens(String delims){
        setDelims(delims, null);
        return countTokens();
    }


    public int countTokens(String delims, boolean delimsAreTokens){
        setDelims((delimsAreTokens ? null : delims), (delimsAreTokens ? delims : null));
        return countTokens();
    }


    public int countTokens(String nontokenDelims, String tokenDelims){
        setDelims(nontokenDelims, tokenDelims);
        return countTokens();
    }


    public int countTokens(String nontokenDelims, String tokenDelims, boolean returnEmptyTokens){
        setDelims(nontokenDelims, tokenDelims);
        setReturnEmptyTokens(returnEmptyTokens);
        return countTokens();
    }


    private boolean advancePosition(){
        // if we are returning empty tokens, we are just starting to tokenizer
        // and there is a delimiter at the beginning of the string or the string
        // is empty we need to indicate that there is an empty token at the beginning.
        // The beginning is defined as where the delimiters were last changed.
        if (returnEmptyTokens && !emptyReturned &&
                (delimsChangedPosition == position ||
                        (position == -1 && strLength == delimsChangedPosition))){
            if (strLength == delimsChangedPosition){
                // Case in which the string (since delim change)
                // is empty, but because we are returning empty
                // tokens, a single empty token should be returned.
                emptyReturned = true;
                /*//system.out.println("Empty token for empty string.");*/
                return true;
            } else {
                char c = text.charAt(position);
                if (c <= maxDelimChar &&
                        (nontokenDelims != null && nontokenDelims.indexOf(c) != -1) ||
                        (tokenDelims != null && tokenDelims.indexOf(c) != -1)){
                    // There is delimiter at the very start of the string
                    // so we must return an empty token at the beginning.
                    emptyReturned = true;
                    /*//system.out.println("Empty token at beginning.");*/
                    return true;
                }
            }
        }
        // The main loop
        // Do this as long as parts of the string have yet to be examined
        while (position != -1){
            char c = text.charAt(position);
            if (returnEmptyTokens && !emptyReturned && position > delimsChangedPosition){
                char c1 = text.charAt(position - 1);
                // Examine the current character and the one before it.
                // If both of them are delimiters, then we need to return
                // an empty delimiter.  Note that characters that were examined
                // before the delimiters changed should not be reexamined.
                if (c <= maxDelimChar && c1 <= maxDelimChar &&
                        ((nontokenDelims != null && nontokenDelims.indexOf(c) != -1) ||
                                (tokenDelims != null && tokenDelims.indexOf(c) != -1)) &&
                        ((nontokenDelims != null && nontokenDelims.indexOf(c1) != -1) ||
                                (tokenDelims != null && tokenDelims.indexOf(c1) != -1))){
                    emptyReturned = true;
                    /*//system.out.println("Empty token.");*/
                    return true;
                }
            }

            int nextDelimiter = (position < strLength - 1 ? indexOfNextDelimiter(position + 1) : -1);
            if (c > maxDelimChar ||
                    ((nontokenDelims == null || nontokenDelims.indexOf(c) == -1) &&
                            (tokenDelims == null || tokenDelims.indexOf(c) == -1))){
                // token found
				/*//system.out.println("Token: '" +
					text.substring(position, (nextDelimiter == -1 ? strLength : nextDelimiter)) +
					"' at " + position + ".");*/
                position = nextDelimiter;
                emptyReturned = false;
                return true;
            } else if (tokenDelims != null && tokenDelims.indexOf(c) != -1) {
                // delimiter that can be returned as a token found
                emptyReturned = false;
                /*//system.out.println("Delimiter: '" + c + "' at " + position + ".");*/
                position = (position < strLength -1 ? position +1 : -1);
                return true;
            } else {
                // delimiter that is not a token found.
                emptyReturned = false;
                position = (position < strLength -1 ? position +1 : -1);
                return false;
            }
        }
        // handle the case that a token is at the end of the string and we should
        // return empty tokens.
        if (returnEmptyTokens && !emptyReturned && strLength > 0){
            char c = text.charAt(strLength - 1);
            if (c <= maxDelimChar &&
                    (nontokenDelims != null && nontokenDelims.indexOf(c) != -1) ||
                    (tokenDelims != null && tokenDelims.indexOf(c) != -1)){
                // empty token at the end of the string found.
                emptyReturned = true;
                /*//system.out.println("Empty token at end.");*/
                return true;
            }
        }
        return false;
    }


    public String nextToken(String nontokenDelims, String tokenDelims){
        setDelims(nontokenDelims, tokenDelims);
        return nextToken();
    }


    public String nextToken(String nontokenDelims, String tokenDelims, boolean returnEmptyTokens){
        setDelims(nontokenDelims, tokenDelims);
        setReturnEmptyTokens(returnEmptyTokens);
        return nextToken();
    }


    public String nextToken(String delims, boolean delimsAreTokens){
        return (delimsAreTokens ? nextToken(null, delims) : nextToken(delims, null));
    }


    public String nextToken(String nontokenDelims){
        return nextToken(nontokenDelims, null);
    }


    private int indexOfNextDelimiter(int start){
        char c;
        int next;
        for (next = start; (c = text.charAt(next)) > maxDelimChar ||
                ((nontokenDelims == null || nontokenDelims.indexOf(c) == -1) &&
                        (tokenDelims == null || tokenDelims.indexOf(c) == -1)); next++){
            if (next == strLength - 1){
                // we have reached the end of the string without
                // finding a delimiter
                return (-1);
            }
        }
        return next;
    }


    public boolean hasMoreElements(){
        return hasMoreTokens();
    }


    public Object nextElement(){
        return nextToken();
    }


    public boolean hasNext(){
        return hasMoreTokens();
    }


    public Object next(){
        return nextToken();
    }


    public void remove(){
        throw new UnsupportedOperationException();
    }


    public void setReturnEmptyTokens(boolean returnEmptyTokens){
        // this could effect the number of tokens
        tokenCount = -1;
        this.returnEmptyTokens = returnEmptyTokens;
    }


    public int getCurrentPosition(){
        return this.position;
    }


    public String[] toArray(){
        String[] tokenArray = new String[countTokens()];
        for(int i=0; hasMoreTokens(); i++) {
            tokenArray[i] = nextToken();
        }
        return tokenArray;
    }

    /**
     * Retrieves the rest of the text as a single token.
     * After calling this method hasMoreTokens() will always return false.
     *
     * @return any part of the text that has not yet been tokenized.
     *
     * @since ostermillerutils 1.00.00
     */
    public String restOfText(){
        return nextToken(null, null);
    }

    /**
     * Returns the same value as nextToken() but does not alter
     * the internal state of the Tokenizer.  Subsequent calls
     * to peek() or a call to nextToken() will return the same
     * token again.
     *
     * @return the next token from this string tokenizer.
     * @throws NoSuchElementException if there are no more tokens in this tokenizer's string.
     *
     * @since ostermillerutils 1.00.00
     */
    public String peek(){
        // copy over state variables from the class to local
        // variables so that the state of this object can be
        // restored to the state that it was in before this
        // method was called.
        int savedPosition = position;
        boolean savedEmptyReturned = emptyReturned;
        int savedtokenCount = tokenCount;

        // get the next token
        String retval = nextToken();

        // restore the state
        position = savedPosition;
        emptyReturned = savedEmptyReturned;
        tokenCount = savedtokenCount;

        // return the nextToken;
        return(retval);
    }
}