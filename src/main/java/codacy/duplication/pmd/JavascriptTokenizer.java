package codacy.duplication.pmd;

/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

import net.sourceforge.pmd.cpd.AbstractTokenizer;
import net.sourceforge.pmd.cpd.SourceCode;
import net.sourceforge.pmd.cpd.TokenEntry;
import net.sourceforge.pmd.cpd.Tokens;

import java.util.ArrayList;
import java.util.List;

public class JavascriptTokenizer extends AbstractTokenizer {

    protected String oneLineCommentStr = "//";
    protected String multiLineCommentStart = "/*";
    protected String multiLineCommentEnd = "*/";

    private List<String> code;
    private int lineNumber = 0;
    private String currentLine;

    protected boolean ignoreComments = true;

    public JavascriptTokenizer() {
        // setting markers for "string" in javascript
        this.stringToken = new ArrayList<>();
        this.stringToken.add("\'");
        this.stringToken.add("\"");

        // setting markers for 'ignorable character' in javascript
        this.ignorableCharacter = new ArrayList<>();
        this.ignorableCharacter.add(";");

        // setting markers for 'ignorable string' in javascript
        this.ignorableStmt = new ArrayList<>();

        // strings do indeed span multiple lines in javascript
        this.spanMultipleLinesString = true;
        // the lines do to end with backslashes
        this.spanMultipleLinesLineContinuationCharacter = '\\';
    }

    public void tokenize(SourceCode tokens, Tokens tokenEntries) {
        code = tokens.getCode();

        for (lineNumber = 0; lineNumber < code.size(); lineNumber++) {
            currentLine = code.get(lineNumber);
            int loc = 0;
            while (loc < currentLine.length()) {
                StringBuilder token = new StringBuilder();
                loc = getTokenFromLine(token, loc);
                if (token.length() > 0 && !isIgnorableString(token.toString())) {
                    token = new StringBuilder(token.toString().toLowerCase());
                    // need to re-think how to link this
                    // if ( CPD.debugEnable ) {
                    // System.err.println("Token added:" + token.toString());
                    // }
                    tokenEntries.add(new TokenEntry(token.toString(), tokens.getFileName(), lineNumber));

                }
            }
        }
        tokenEntries.add(TokenEntry.getEOF());
    }

    private int getTokenFromLine(StringBuilder token, int loc) {
        for (int j = loc; j < currentLine.length(); j++) {
            char tok = currentLine.charAt(j);

            if (!Character.isWhitespace(tok) && !ignoreCharacter(tok)) {
                if (isMultilineCommentStart(currentLine.substring(j))) {
                    return parseMultilineComment(j);
                } else if (isComment(currentLine.substring(j))) {
                    return currentLine.length();
                } else if (isString(tok)) {
                    if (token.length() > 0) {
                        return j; // we need to now parse the string as a
                        // separate token.
                    } else {
                        // we are at the start of a string
                        return parseString(token, j, tok);
                    }
                } else {
                    token.append(tok);
                }
            } else {
                if (token.length() > 0) {
                    return j;
                }
            }
            loc = j;
        }

        return loc + 1;
    }

    private int parseString(StringBuilder token, int loc, char stringDelimiter) {
        int localLoc = loc;
        boolean stringNotEnded = true;

        while (stringNotEnded) {
            boolean escaped = false;
            boolean done = false;

            while (localLoc < currentLine.length() && !done) {
                char tok = currentLine.charAt(localLoc);
                if (escaped && tok == stringDelimiter) { // Found an escaped string
                    escaped = false;
                } else if (tok == stringDelimiter && token.length() > 0) {
                    // We are done, we found the end of the string...
                    done = true;
                } else {
                    escaped = tok == '\\'; // Found an escaped char
                }
                // Adding char to String:" + token.toString());
                token.append(tok);
                localLoc++;
            }

            // Handling multiple lines string
            if (!done && // ... we didn't find the end of the string
                    localLoc >= currentLine.length() && // ... we have reach the end of
                    // the line ( the String is
                    // incomplete, for the moment at
                    // least)
                    spanMultipleLinesString && // ... the language allow multiple
                    // line span Strings
                    lineNumber < code.size() - 1 // ... there is still more lines to
                // parse
                    ) {
                // removes last character, if it is the line continuation (e.g.
                // backslash) character
                if (spanMultipleLinesLineContinuationCharacter != null && token.length() > 0
                        && token.charAt(token.length() - 1) == spanMultipleLinesLineContinuationCharacter) {
                    token.deleteCharAt(token.length() - 1);
                }
                // parsing new line
                currentLine = code.get(++lineNumber);
                localLoc = 0;
            } else {
                stringNotEnded = false;
            }
        }

        return localLoc + 1;
    }

    private int parseMultilineComment(int loc) {
        int localLoc = loc;
        boolean commentNotEnded = true;

        while (commentNotEnded) {
            boolean done = false;

            while (localLoc < currentLine.length() && !done) {
                if (isMultilineCommentEnd(currentLine.substring(localLoc))) {
                    // We are done, we found the end of the string...
                    done = true;
                }
                localLoc++;
            }

            // Handling multiple comments
            if (!done && // ... we didn't find the end of the comment
                    localLoc >= currentLine.length() && // ... we have reached the end of the line
                    lineNumber < code.size() - 1 // ... there is still more lines to parse
                    ) {
                // parsing new line
                currentLine = code.get(++lineNumber);
                // Warning : recursive call !
                localLoc = 0;
            } else {
                commentNotEnded = false;
            }
        }

        return localLoc + 1;
    }

    private boolean ignoreCharacter(char tok) {
        return ignorableCharacter.contains(String.valueOf(tok));
    }

    private boolean isString(char tok) {
        return stringToken.contains(String.valueOf(tok));
    }

    private boolean isComment(String str) {
        return ignoreComments && str.startsWith(oneLineCommentStr);
    }

    private boolean isMultilineCommentStart(String str) {
        return ignoreComments && str.startsWith(multiLineCommentStart);
    }

    private boolean isMultilineCommentEnd(String str) {
        return ignoreComments && str.startsWith(multiLineCommentEnd);
    }

    private boolean isIgnorableString(String token) {
        return ignorableStmt.contains(token);
    }

}
