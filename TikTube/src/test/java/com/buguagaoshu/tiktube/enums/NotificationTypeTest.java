package com.buguagaoshu.tiktube.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 审核通知里的稿件标题是作者可控文本，会被前端按富文本渲染，
 * 这里锁住转义行为，避免以后有人把 safeTextForRichMessage 去掉
 */
class NotificationTypeTest {

    @Test
    void examineLinkKeepsOnlyItsOwnAnchorTag() {
        String link = NotificationType.createExamineLink(
                "<script>alert(1)</script><img src=x onerror=alert(2)>", 5L);

        assertFalse(link.contains("<script"));
        assertFalse(link.contains("<img"));
        assertTrue(link.startsWith("<a href=\"/studio/upload?edit=5\">"));
        assertTrue(link.endsWith("</a>"));
        // 整串里只剩我们自己拼的那一对 a 标签
        assertEquals(1, countOccurrences(link, "<a "));
        assertEquals(1, countOccurrences(link, "</a>"));
    }

    @Test
    void examineLinkEscapesAttributeBreakout() {
        String link = NotificationType.createExamineLink("a\" onmouseover=\"alert(1)", 5L);

        assertFalse(link.contains("\" onmouseover="));
        assertTrue(link.contains("&quot;"));
    }

    @Test
    void examineContentEscapesTitleAndAdminMessage() {
        String content = NotificationType.createExamineContent(
                "<b>标题</b>", ExamineTypeEnum.OTHER.getCode(), "<script>理由</script>");

        assertFalse(content.contains("<b>"));
        assertFalse(content.contains("<script"));
        assertTrue(content.contains("标题"));
        assertTrue(content.contains("理由"));
        assertTrue(content.contains(ExamineTypeEnum.OTHER.getMsg()));
    }

    @Test
    void examinePassContentEscapesTitle() {
        String content = NotificationType.createExaminePassContent("<svg onload=alert(1)>好稿件");

        assertFalse(content.contains("<svg"));
        assertTrue(content.contains("好稿件"));
        assertTrue(content.contains("已通过审核"));
    }

    private static int countOccurrences(String text, String token) {
        int count = 0;
        for (int i = text.indexOf(token); i >= 0; i = text.indexOf(token, i + token.length())) {
            count++;
        }
        return count;
    }
}
