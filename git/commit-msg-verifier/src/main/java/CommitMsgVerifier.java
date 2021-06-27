/*
 * Copyright 2019. http://devonline.academy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;

/**
 * This commit-msg hook verifies a commit message.
 *
 * <p>
 * Read more about hooks:
 * <a href="https://git-scm.com/book/en/v2/Customizing-Git-Git-Hooks">
 * https://git-scm.com/book/en/v2/Customizing-Git-Git-Hooks
 * </a>.
 *
 * <p>
 * Commit message rules:
 *
 * <ul>
 *     <li>{@code 255}. Enter a commit message!</li>
 *     <li>{@code 1}. Separate subject from body with a blank line!</li>
 *     <li>{@code 2}. Limit the subject line to 50 characters!</li>
 *     <li>{@code 3}. Capitalize the subject line!</li>
 *     <li>{@code 4}. Do not end the subject line with a period!</li>
 *     <li>{@code 5}. Use the imperative mood in the subject line!</li>
 *     <li>{@code 6}. Wrap the body at 72 characters!</li>
 *     <li>{@code 254}. If IO error occurs.</li>
 *     <li>{@code 253}. If verbs file not found.</li>
 * </ul>
 *
 * <p>
 * (Read more: <a href="https://chris.beams.io/posts/git-commit/">https://chris.beams.io/posts/git-commit/</a>)
 *
 * @author devonline
 * @link http://devonline.academy/java
 * @since 1.0
 */
public final class CommitMsgVerifier {

    private static final String FORBIDDEN_LAST_CHARACTERS_FOR_SUBJECT = ".!;:, \t";

    private static final int ENTER_A_COMMIT_MESSAGE_ERROR_CODE = 255;

    private static final int SEPARATE_SUBJECT_FROM_BODY_ERROR_CODE = 1;

    private static final int LIMIT_SUBJECT_TO_50_CHARS_ERROR_CODE = 2;

    private static final int CAPITALIZE_SUBJECT_ERROR_CODE = 3;

    private static final int DO_NOT_END_WITH_PERIOD_ERROR_CODE = 4;

    private static final int USE_IMPERATIVE_MOOD_ERROR_CODE = 5;

    private static final int WRAP_BODY_AT_72_CHARS_ERROR_CODE = 6;

    private static final int IO_ERROR_CODE = 254;

    private static final int VERBS_FILE_NOT_FOUND_ERROR_CODE = 253;

    private CommitMsgVerifier() {
    }

    public static void main(final String... args) {
        try {
            final List<String> lines = Files.readAllLines(Paths.get(args[0]).toAbsolutePath());
            verifyThatCommitMsgNotEmpty(lines);

            final String subject = lines.get(0);
            verifyThatSubjectSeparatedFromBody(lines);
            verifyThatSubjectLessThan50Chars(subject);
            verifyThatSubjectIsCapitalized(subject);
            verifyThatSubjectDoesNotEndWithPeriod(subject);
            verifyThatSubjectIsStartedFromVerbInImperativeMood(subject);
            if (isBodyFound(lines)) {
                verifyThatEachBodyLineLessThan72Chars(lines.subList(2, lines.size()));
            }
        } catch (final IOException ex) {
            System.err.printf("%s: %s%n", ex.getClass().getName(), ex.getMessage());
            System.exit(IO_ERROR_CODE);
        } catch (final InvalidCommitMessageException ex) {
            for (final String message : ex.messages) {
                System.err.println(message);
            }
            System.exit(ex.errorCode);
        }
    }

    private static void verifyThatCommitMsgNotEmpty(final List<String> lines) {
        if (lines.isEmpty() || lines.get(0).isBlank()) {
            throw new InvalidCommitMessageException(
                    ENTER_A_COMMIT_MESSAGE_ERROR_CODE,
                    "Enter a commit message!"
            );
        }
    }

    private static void verifyThatSubjectSeparatedFromBody(final List<String> lines) {
        if (lines.size() > 1 && !lines.get(1).isEmpty()) {
            throw new InvalidCommitMessageException(
                    SEPARATE_SUBJECT_FROM_BODY_ERROR_CODE,
                    "Separate subject from body with a blank line!"
            );
        }
    }

    private static void verifyThatSubjectLessThan50Chars(final String subject) {
        final int maxSupportedSubjectLine = 50;
        if (subject.length() > maxSupportedSubjectLine) {
            throw new InvalidCommitMessageException(
                    LIMIT_SUBJECT_TO_50_CHARS_ERROR_CODE,
                    "Limit the subject line to 50 characters!",
                    format("The subject has '%s' characters!", subject.length())
            );
        }
    }

    private static void verifyThatSubjectIsCapitalized(final String subject) {
        if (!Character.isUpperCase(subject.charAt(0))) {
            throw new InvalidCommitMessageException(
                    CAPITALIZE_SUBJECT_ERROR_CODE,
                    "Capitalize the subject line!"
            );
        }
    }

    private static void verifyThatSubjectDoesNotEndWithPeriod(final String subject) {
        final char lastChar = subject.charAt(subject.length() - 1);
        if (FORBIDDEN_LAST_CHARACTERS_FOR_SUBJECT.indexOf(lastChar) != -1) {
            throw new InvalidCommitMessageException(
                    DO_NOT_END_WITH_PERIOD_ERROR_CODE,
                    format("Do not end the subject line with a `%s` character!", lastChar)
            );
        }
    }

    private static void verifyThatSubjectIsStartedFromVerbInImperativeMood(final String subject) throws IOException {
        final Path verbsFilePath = Paths.get(System.getProperty("user.home") + "/.verbs");
        if (!Files.exists(verbsFilePath)) {
            throw new InvalidCommitMessageException(
                    VERBS_FILE_NOT_FOUND_ERROR_CODE,
                    format("Required file with english verbs not found: '%s'. Make this file!", verbsFilePath)
            );
        }
        final List<String> verbs = Files.readAllLines(verbsFilePath);
        final String firstWord = subject.split(" ")[0];
        if (!verbs.contains(firstWord)) {
            throw new InvalidCommitMessageException(
                    USE_IMPERATIVE_MOOD_ERROR_CODE,
                    format(
                            "Use the imperative mood in the subject line: '%s' is not verb or imperative mood!",
                            firstWord
                    ),
                    "HELP MESSAGE TEMPLATE: If applied, this commit will YOUR_SUBJECT_LINE_HERE!",
                    format("CURRENT MESSAGE: If applied, this commit will %s", subject)
            );
        }
    }

    private static boolean isBodyFound(final List<String> lines) {
        return lines.size() >= 3;
    }

    private static void verifyThatEachBodyLineLessThan72Chars(final List<String> lines) {
        final int maxSupportedBodyLine = 72;
        for (final String line : lines) {
            if (line.length() > maxSupportedBodyLine) {
                throw new InvalidCommitMessageException(
                        WRAP_BODY_AT_72_CHARS_ERROR_CODE,
                        "Wrap the body at 72 characters!",
                        format("The following line has '%s' characters: %s", line.length(), line)
                );
            }
        }
    }

    /**
     * @author devonline
     * @link http://devonline.academy/java
     * @since 1.0
     */
    private static final class InvalidCommitMessageException extends RuntimeException {

        private final int errorCode;

        private final List<String> messages;

        private InvalidCommitMessageException(final int errorCode,
                                              final String... messages) {
            this.errorCode = errorCode;
            this.messages = Arrays.asList(messages);
        }
    }
}
