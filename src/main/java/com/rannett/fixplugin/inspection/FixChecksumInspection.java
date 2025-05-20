package com.rannett.fixplugin.inspection;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.rannett.fixplugin.psi.FixField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.stream.IntStream;

public class FixChecksumInspection extends LocalInspectionTool  {


    public static final String BEGIN_STRING = "8";
    public static final String CHECK_SUM_TAG = "10";

    @Override
    public ProblemDescriptor @Nullable [] checkFile(@NotNull PsiFile file,
                                                    @NotNull InspectionManager manager,
                                                    boolean isOnTheFly) {
        List<FixField> fields = PsiTreeUtil.getChildrenOfTypeAsList(file, FixField.class);
        if (fields.isEmpty()) return ProblemDescriptor.EMPTY_ARRAY;

        return extractMessages(fields).stream()
                .map(msg -> {
                    String expected = String.format("%03d", calculateChecksum(msg.body));
                    String actual = msg.checksumField.getValue() != null ? msg.checksumField.getValue() : "";

                    if (!expected.equals(actual)) {
                        return manager.createProblemDescriptor(
                                msg.checksumField,
                                "Incorrect checksum (expected " + expected + ")",
                                new FixChecksumQuickFix(expected),
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                isOnTheFly
                        );
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toArray(ProblemDescriptor[]::new);
    }



    private int calculateChecksum(String message) {
        return message.chars()
                .filter(c -> c == 1 || (c >= 32 && c <= 126))
                .sum() % 256;
    }

    private List<MessageBlock> extractMessages(List<FixField> fields) {
        List<MessageBlock> messages = new ArrayList<>();

        for (int i = 0; i < fields.size(); i++) {
            FixField field = fields.get(i);
            if (!BEGIN_STRING.equals(field.getTag())) continue;

            // Start of message found at index i
            int startIndex = i;
            int checksumIndex = -1;

            for (int j = i + 1; j < fields.size(); j++) {
                FixField candidate = fields.get(j);
                if (CHECK_SUM_TAG.equals(candidate.getTag())) {
                    checksumIndex = j;
                    break;
                }
            }

            if (checksumIndex != -1) {
                String body = rebuildFixBody(fields, startIndex, checksumIndex);
                FixField checksumField = fields.get(checksumIndex);
                messages.add(new MessageBlock(body, checksumField));

                // Skip to after this message block
                i = checksumIndex;
            }
        }

        return messages;
    }


    private static String rebuildFixBody(List<FixField> fields, int startIndex, int endIndexExclusive) {
        StringBuilder raw = new StringBuilder();

        for (int i = startIndex; i < endIndexExclusive; i++) {
            FixField field = fields.get(i);
            raw.append(field.getText());

            PsiElement sibling = field.getNextSibling();
            while (sibling != null && !(sibling instanceof FixField)) {
                raw.append(sibling.getText());
                sibling = sibling.getNextSibling();
            }
        }

        return raw.toString();
    }

    private record MessageBlock(String body, FixField checksumField) {}

}

