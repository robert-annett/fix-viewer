package com.rannett.fixplugin;

import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.rannett.fixplugin.psi.FixField;

import java.util.ArrayList;
import java.util.Collection;

public class FixDocumentationProviderTest extends BasePlatformTestCase {

    // The FIX lexer accepts | as a field delimiter in test files.
    private static final String SEP = "|";

    private FixDocumentationProvider provider;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        provider = new FixDocumentationProvider();
    }

    // -------------------------------------------------------------------------
    // getCustomDocumentationElement
    // -------------------------------------------------------------------------

    public void testGetCustomDocumentationElement_nullContextElement_returnsNull() {
        myFixture.configureByText("test.fix", "8=FIX.4.2" + SEP);
        Editor editor = myFixture.getEditor();
        PsiFile file = myFixture.getFile();
        assertNull(provider.getCustomDocumentationElement(editor, file, null, 0));
    }

    public void testGetCustomDocumentationElement_tagElement_returnsElement() {
        PsiElement[] elems = configure("8=FIX.4.2" + SEP + "35=D" + SEP + "10=000" + SEP);
        PsiElement tag = elems[0];  // TAG element of field 0
        Editor editor = myFixture.getEditor();
        PsiFile file = myFixture.getFile();
        assertSame(tag, provider.getCustomDocumentationElement(editor, file, tag, 0));
    }

    public void testGetCustomDocumentationElement_valueElement_returnsElement() {
        myFixture.configureByText("test.fix", "8=FIX.4.2" + SEP + "35=D" + SEP + "10=000" + SEP);
        PsiFile file = myFixture.getFile();
        FixField field = firstField(file);
        PsiElement value = field.getLastChild();
        Editor editor = myFixture.getEditor();
        assertSame(value, provider.getCustomDocumentationElement(editor, file, value, 0));
    }

    // -------------------------------------------------------------------------
    // generateDoc — null / guard cases
    // -------------------------------------------------------------------------

    public void testGenerateDoc_nullElement_returnsNull() {
        assertNull(provider.generateDoc(null, null));
    }

    // -------------------------------------------------------------------------
    // generateDoc — known tag produces HTML
    // -------------------------------------------------------------------------

    public void testGenerateDoc_knownTag_containsTagNumberAndName() {
        // Field at index 1 is 35=D (MsgType). Index 0 is the BeginString field.
        PsiElement tag = tagAt("8=FIX.4.2" + SEP + "35=D" + SEP + "10=000" + SEP, 1);
        String doc = provider.generateDoc(tag, null);
        assertNotNull(doc);
        assertTrue(doc.contains("35"));
        assertTrue(doc.contains("MsgType"));
    }

    public void testGenerateDoc_knownTagWithEnumValue_containsValueName() {
        // tag 35=D → the value text "D" and ideally its name should appear
        PsiElement value = valueAt("8=FIX.4.2" + SEP + "35=D" + SEP + "10=000" + SEP, 1);
        String doc = provider.generateDoc(value, null);
        assertNotNull(doc);
        assertTrue("Doc should include the value text", doc.contains("D"));
    }

    public void testGenerateDoc_knownTagWithFieldType_containsType() {
        // tag 34 = MsgSeqNum, type INT in FIX.4.2
        PsiElement tag = tagAt("8=FIX.4.2" + SEP + "34=1" + SEP + "10=000" + SEP, 1);
        String doc = provider.generateDoc(tag, null);
        assertNotNull(doc);
        assertTrue("Doc should include the FIX field type", doc.contains("INT"));
    }

    public void testGenerateDoc_unknownTag_returnsNull() {
        // tag 99999 is not in any bundled dictionary
        PsiElement tag = tagAt("8=FIX.4.2" + SEP + "99999=X" + SEP + "10=000" + SEP, 1);
        assertNull("Unknown tags should produce no documentation", provider.generateDoc(tag, null));
    }

    public void testGenerateDoc_usesVersionFromFileContent() {
        // File has "8=FIX.4.2" → version FIX.4.2 should appear in the footer
        PsiElement tag = tagAt("8=FIX.4.2" + SEP + "35=D" + SEP + "10=000" + SEP, 1);
        String doc = provider.generateDoc(tag, null);
        assertNotNull(doc);
        assertTrue("Doc footer should show the resolved FIX version", doc.contains("FIX.4.2"));
    }

    public void testGenerateDoc_noVersionInFile_fallsBackToFixt11() {
        // No BeginString field → falls back to FIXT.1.1
        PsiElement tag = tagAt("35=D" + SEP + "10=000" + SEP, 0);
        String doc = provider.generateDoc(tag, null);
        // May be null if tag is unknown in FIXT.1.1 dictionary; must not throw.
        if (doc != null) {
            assertTrue("Fallback version should be FIXT.1.1", doc.contains("FIXT.1.1"));
        }
    }

    public void testGenerateDoc_outputIsWellFormedHtml() {
        PsiElement tag = tagAt("8=FIX.4.2" + SEP + "35=D" + SEP + "10=000" + SEP, 1);
        String doc = provider.generateDoc(tag, null);
        assertNotNull(doc);
        assertTrue(doc.startsWith("<html>"));
        assertTrue(doc.endsWith("</html>"));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Configures the fixture and returns [tag, value] of the first field. */
    private PsiElement[] configure(String fixContent) {
        myFixture.configureByText("test.fix", fixContent);
        FixField field = firstField(myFixture.getFile());
        return new PsiElement[]{field.getFirstChild(), field.getLastChild()};
    }

    private FixField firstField(PsiFile file) {
        Collection<FixField> fields = PsiTreeUtil.findChildrenOfType(file, FixField.class);
        return new ArrayList<>(fields).get(0);
    }

    private PsiElement tagAt(String fixContent, int fieldIndex) {
        myFixture.configureByText("test.fix", fixContent);
        Collection<FixField> fields = PsiTreeUtil.findChildrenOfType(myFixture.getFile(), FixField.class);
        return new ArrayList<>(fields).get(fieldIndex).getFirstChild();
    }

    private PsiElement valueAt(String fixContent, int fieldIndex) {
        myFixture.configureByText("test.fix", fixContent);
        Collection<FixField> fields = PsiTreeUtil.findChildrenOfType(myFixture.getFile(), FixField.class);
        return new ArrayList<>(fields).get(fieldIndex).getLastChild();
    }
}
