package compiler;

import java.util.*;

// NFA construction with transition matrix
public class Regex {
    private final char[] tokens;
    private final boolean[] escaped;
    private final int last; // the accepting state

    private boolean[][] matrix;
    private final HashMap<Integer, Range> rangeMap = new HashMap<>();

    private static final char RANGE_MARKER = ' ';

    private static class Range {
        private final Set<Character> allowedCharacters = new HashSet<>();

        public Range(String content) {
            for (int ind = 0; ind < content.length(); ind++) {
                if (ind + 2 < content.length() && content.charAt(ind + 1) == '-') {
                    for (char smb = content.charAt(ind); smb <= content.charAt(ind + 2); smb++) {
                        allowedCharacters.add(smb);
                    }

                    ind += 2;
                } else {
                    allowedCharacters.add(content.charAt(ind));
                }
            }
        }

        public boolean matches(char character) {
            return allowedCharacters.contains(character);
        }
    }

    public Regex(String regex) {
        regex = "(" + regex + ")";

        List<Character> tokenList = new ArrayList<>();
        List<Boolean> escapedList = new ArrayList<>();

        for (int ind = 0; ind < regex.length(); ind++) {
            char character = regex.charAt(ind);

            if (character == '\\' && (ind + 1 < regex.length())) {
                char nextCharacter = regex.charAt(++ind);

                tokenList.add(nextCharacter);
                escapedList.add(true);
            } else if (character == '[') {
                int closingBracketIndex = regex.indexOf(']', ind);

                if (closingBracketIndex == -1) {
                    throw new IllegalArgumentException("Wrong regex");
                }

                String rangeContent = regex.substring(ind + 1, closingBracketIndex);

                int index = tokenList.size();

                tokenList.add(RANGE_MARKER);
                escapedList.add(false);

                rangeMap.put(index, new Range(rangeContent));

                ind = closingBracketIndex;
            } else {
                tokenList.add(character);
                escapedList.add(false);
            }
        }

        last = tokenList.size();
        tokens = new char[last];
        escaped = new boolean[last];

        for (int ind = 0; ind < last; ind++) {
            tokens[ind] = tokenList.get(ind);
            escaped[ind] = escapedList.get(ind);
        }

        createMatrix();
    }

    private void createMatrix() {
        matrix = new boolean[last + 1][last + 1];

        Stack<Integer> subexpOperators = new Stack<>();

        for (int ind = 0; ind < last; ind++) {
            int lp = ind;
            char token = tokens[ind];

            if (!escaped[ind] && (token == '(' || token == '|')) {
                subexpOperators.push(ind);
            } else if (!escaped[ind] && token == ')') {
                List<Integer> orIndices = new ArrayList<>();

                while (!subexpOperators.isEmpty() && !escaped[subexpOperators.peek()] && tokens[subexpOperators.peek()] == '|') {
                    orIndices.add(subexpOperators.pop());
                }

                if (subexpOperators.isEmpty() || escaped[subexpOperators.peek()]) {
                    throw new IllegalArgumentException("Wrong regex");
                }

                lp = subexpOperators.pop(); // '('

                for (int or : orIndices) {
                    matrix[lp][or + 1] = true;
                    matrix[or][ind] = true;
                }

                matrix[ind][ind + 1] = true;
            }

            if (ind < last - 1) {
                char nextToken = tokens[ind + 1];
                boolean nextEscaped = escaped[ind + 1];

                if (!nextEscaped && nextToken == '*') {
                    // bypassing the operand and looping back
                    matrix[lp][ind + 1] = true;
                    matrix[ind + 1][lp] = true;
                } else if (!nextEscaped && nextToken == '+') {
                    matrix[ind + 1][lp] = true; // looping back
                } else if (!nextEscaped && nextToken == '?') {
                    matrix[lp][ind + 1] = true; // bypassing the operand
                }
            }

            if (!escaped[ind] && isOperator(tokens[ind])) {
                matrix[ind][ind + 1] = true;
            }
        }
    }

    public boolean matches(String text) {
        Set<Integer> currentStates = new HashSet<>();

        currentStates.add(0); // initial state
        currentStates = getClosure(currentStates);

        for (int ind = 0; ind < text.length(); ind++) {
            Set<Integer> nextStates = new HashSet<>();

            char character = text.charAt(ind);

            for (int state : currentStates) {
                if (state < last) {
                    char token = tokens[state];
                    boolean tokenEscaped = escaped[state];

                    if (tokenEscaped || !isOperator(token)) {
                        boolean match;

                        if (!tokenEscaped && token == '.') {
                            match = true;
                        } else if (rangeMap.containsKey(state)) {
                            match = rangeMap.get(state).matches(character);
                        } else {
                            match = (token == character);
                        }

                        if (match) nextStates.add(state + 1);
                    }
                }
            }

            currentStates = getClosure(nextStates);
        }

        return currentStates.contains(last);
    }

    private Set<Integer> getClosure(Set<Integer> states) {
        Set<Integer> result = new HashSet<>(states);
        Stack<Integer> stack = new Stack<>();

        for (int state : states) {
            stack.push(state);
        }

        while (!stack.isEmpty()) {
            int s = stack.pop();

            for (int t = 0; t <= last; t++) {
                if (matrix[s][t] && !result.contains(t)) {
                    result.add(t);
                    stack.push(t);
                }
            }
        }

        return result;
    }

    private boolean isOperator(char token) {
        return Set.of('(', ')', '|', '*', '+', '?').contains(token);
    }
}
