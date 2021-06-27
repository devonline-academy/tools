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

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sun.misc.Unsafe;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * This test verifies a CommitMsgVerifier logic.
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
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
final class CommitMsgVerifierTest {

    private static final PrintStream ORIGINAL_ERR = System.err;

    private static final String ORIGINAL_USER_HOME = System.getProperty("user.home");

    private static final Runtime ORIGINAL_RUNTIME = Runtime.getRuntime();

    @Mock
    private PrintStream err;

    @Mock
    private Runtime runtime;

    @BeforeEach
    void beforeEach() throws NoSuchFieldException, IllegalAccessException {
        System.setErr(err);
        setRuntime(runtime);
    }

    @Test
    @Order(10)
    void Should_display_error_if_commit_has_empty_content(@TempDir final Path dir) throws IOException {
        final Path file = dir.resolve("msg");
        Files.createFile(file);

        CommitMsgVerifier.main(file.toAbsolutePath().toString());

        verify(err).println("Enter a commit message!");
        verify(runtime).exit(255);
    }

    @Test
    @Order(20)
    void Should_display_error_if_commit_has_empty_subject(@TempDir final Path dir) throws IOException {
        final Path file = dir.resolve("msg");
        Files.writeString(file, System.lineSeparator());

        CommitMsgVerifier.main(file.toAbsolutePath().toString());

        verify(err).println("Enter a commit message!");
        verify(runtime).exit(255);
    }

    @Test
    @Order(30)
    void Should_display_error_if_subject_not_separated_from_body(@TempDir final Path dir) throws IOException {
        final Path file = dir.resolve("msg");
        Files.write(file, List.of("subject", "body"));

        CommitMsgVerifier.main(file.toAbsolutePath().toString());

        verify(err).println("Separate subject from body with a blank line!");
        verify(runtime).exit(1);
    }

    @Test
    @Order(40)
    void Should_display_error_if_subject_has_more_than_50_characters(@TempDir final Path dir) throws IOException {
        final Path file = dir.resolve("msg");
        Files.writeString(file, "A".repeat(51));

        CommitMsgVerifier.main(file.toAbsolutePath().toString());

        verify(err).println("Limit the subject line to 50 characters!");
        verify(err).println("The subject has '51' characters!");
        verify(runtime).exit(2);
    }

    @Test
    @Order(50)
    void Should_display_error_if_subject_starts_with_lower_case_letter(@TempDir final Path dir) throws IOException {
        final Path file = dir.resolve("msg");
        Files.writeString(file, "hello");

        CommitMsgVerifier.main(file.toAbsolutePath().toString());

        verify(err).println("Capitalize the subject line!");
        verify(runtime).exit(3);
    }

    @Test
    @Order(60)
    void Should_display_error_if_subject_ends_with_period(@TempDir final Path dir) throws IOException {
        final Path file = dir.resolve("msg");
        Files.writeString(file, "Hello.");

        CommitMsgVerifier.main(file.toAbsolutePath().toString());

        verify(err).println("Do not end the subject line with a `.` character!");
        verify(runtime).exit(4);
    }

    @Test
    @Order(70)
    @SuppressWarnings("RedundantStringFormatCall")
    void Should_display_error_if_verbs_file_not_found(@TempDir final Path tempUserHome,
                                                      @TempDir final Path dir) throws IOException {
        System.setProperty("user.home", tempUserHome.toAbsolutePath().toString());
        final Path file = dir.resolve("msg");
        Files.writeString(file, "Hello");

        CommitMsgVerifier.main(file.toAbsolutePath().toString());

        verify(err).println(format(
                "Required file with english verbs not found: '%s%s.verbs'. Make this file!",
                System.getProperty("user.home"), File.separatorChar
        ));
        verify(runtime).exit(253);
    }

    @Test
    @Order(80)
    void Should_display_error_if_subject_not_start_with_verb_in_imperative_mood(@TempDir final Path tempUserHome,
                                                                                @TempDir final Path dir)
            throws IOException {
        createVerbsFile(tempUserHome);

        final Path file = dir.resolve("msg");
        Files.writeString(file, "Hello");

        CommitMsgVerifier.main(file.toAbsolutePath().toString());

        verify(err).println("Use the imperative mood in the subject line: 'Hello' is not verb or imperative mood!");
        verify(err).println("HELP MESSAGE TEMPLATE: If applied, this commit will YOUR_SUBJECT_LINE_HERE!");
        verify(err).println("CURRENT MESSAGE: If applied, this commit will Hello");
        verify(runtime).exit(5);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    @Order(90)
    void Should_display_error_if_any_body_line_contains_more_than_72_characters(final int number,
                                                                                @TempDir final Path tempUserHome,
                                                                                @TempDir final Path dir)
            throws IOException {
        createVerbsFile(tempUserHome);

        final Path file = dir.resolve("msg");
        final List<String> content = new ArrayList<>(List.of("TestVerb subject", ""));
        for (int i = 0; i < number; i++) {
            if (i + 1 == number) {
                content.add("b".repeat(73));
            } else {
                content.add("test");
            }
        }
        Files.write(file, content);

        CommitMsgVerifier.main(file.toAbsolutePath().toString());

        verify(err).println("Wrap the body at 72 characters!");
        verify(err).println("The following line has '73' characters: " + "b".repeat(73));
        verify(runtime).exit(6);
    }
    
    @Test
    @Order(100)
    @SuppressWarnings("UnnecessaryCallToStringValueOf")
    void Should_display_error_if_IO_error_occurs(@TempDir final Path dir) {
        final Path file = dir.resolve("not-found");

        CommitMsgVerifier.main(file.toAbsolutePath().toString());

        verify(err).printf("%s: %s%n", NoSuchFileException.class.getName(), String.valueOf(file.toAbsolutePath()));
        verify(runtime).exit(254);
    }

    @ParameterizedTest
    @Order(110)
    @ArgumentsSource(ValidCommitMessageArgumentsProvider.class)
    void Should_successful_exit(final String content,
                                @TempDir final Path tempUserHome,
                                @TempDir final Path dir) throws IOException {
        createVerbsFile(tempUserHome);

        final Path file = dir.resolve("msg");
        Files.write(file, Arrays.asList(content.split("\n")));

        assertDoesNotThrow(() -> CommitMsgVerifier.main(file.toAbsolutePath().toString()));

        verifyNoInteractions(err);
        verifyNoInteractions(runtime);
    }

    @Test
    @Order(120)
    void Should_create_instance_of_CommitMsg_via_reflection() {
        final Constructor<?>[] constructors = CommitMsgVerifier.class.getDeclaredConstructors();
        assertEquals(1, constructors.length);

        constructors[0].setAccessible(true);
        assertDoesNotThrow(() -> constructors[0].newInstance());
    }

    private void createVerbsFile(final Path tempUserHome) throws IOException {
        System.setProperty("user.home", tempUserHome.toAbsolutePath().toString());
        final Path verbs = tempUserHome.resolve(".verbs");
        if (!Files.exists(verbs)) {
            Files.write(verbs, List.of("TestVerb"));
        }
    }

    @AfterEach
    void afterEach() throws NoSuchFieldException, IllegalAccessException {
        System.setErr(ORIGINAL_ERR);
        setRuntime(ORIGINAL_RUNTIME);
        System.setProperty("user.home", ORIGINAL_USER_HOME);
    }

    private void setRuntime(final Runtime runtime) throws NoSuchFieldException, IllegalAccessException {
        final Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        final Unsafe unsafe = (Unsafe) theUnsafe.get(null);
        final Field currentRuntime = Runtime.class.getDeclaredField("currentRuntime");
        unsafe.putObject(
                unsafe.staticFieldBase(currentRuntime),
                unsafe.staticFieldOffset(currentRuntime),
                runtime
        );
    }

    private static final class ValidCommitMessageArgumentsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext context) {
            final String subject = "TestVerb subject";
            return Stream.of(
                    subject + "A".repeat(50 - subject.length()),
                    subject,
                    subject + "\n",
                    subject + "\n\nBody line",
                    subject + "\n\nFirst body line\nsecond body line",
                    subject + "\n\n" + "b".repeat(72)
            ).map(Arguments::of);
        }
    }
}