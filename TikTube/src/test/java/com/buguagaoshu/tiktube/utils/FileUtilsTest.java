package com.buguagaoshu.tiktube.utils;

import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileUtilsTest {

    @Test
    void resolveSafelyRejectsTraversal() throws Exception {
        Path root = Files.createTempDirectory("tiktube-file-test");
        Files.createDirectories(root.resolve("2026/10/10"));
        Path inside = Files.write(root.resolve("2026/10/10/ok.jpg"), new byte[]{1});

        assertEquals(inside.toRealPath(), FileUtils.resolveSafely(root, "2026/10/10/ok.jpg"));

        assertThrows(FileNotFoundException.class, () -> FileUtils.resolveSafely(root, "../../application.yml"));
        assertThrows(FileNotFoundException.class, () -> FileUtils.resolveSafely(root, "..\\..\\application.yml"));
        assertThrows(FileNotFoundException.class, () -> FileUtils.resolveSafely(root, "2026/10/10/../../secret.txt"));
        assertThrows(FileNotFoundException.class, () -> FileUtils.resolveSafely(root, null));
        assertFalse(FileUtils.isSafeSegment("a\\..\\b"));
        assertFalse(FileUtils.isSafeRelativePath("2026/10/10/"));
        assertTrue(FileUtils.isSafeRelativePath("2026/10/10"));
    }

    @Test
    void textIsNeutralisedBeforeRendering() {
        assertEquals("与 x", MyStringUtils.toPlainText("<p>与</p> <b>x</b>", 50));
        assertEquals("", MyStringUtils.toPlainText(null, 50));

        String malicious = "<img src=x onerror=alert(1)> [点我](javascript:alert(2)) 1<2 say \"hi\" <svg onload=alert(3)";
        String safe = MyStringUtils.safeTextForRichMessage(malicious, 500);

        assertFalse(safe.contains("<"));
        assertFalse(safe.contains(">"));
        assertTrue(safe.contains("\\[点我\\]"));
        assertTrue(safe.contains("&quot;"));
    }
}
