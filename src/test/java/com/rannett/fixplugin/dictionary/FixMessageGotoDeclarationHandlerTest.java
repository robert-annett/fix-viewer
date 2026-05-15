package com.rannett.fixplugin.dictionary;

import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.rannett.fixplugin.settings.FixViewerSettingsState;
import com.rannett.fixplugin.settings.FixViewerSettingsState.DictionaryEntry;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FixMessageGotoDeclarationHandlerTest extends BasePlatformTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        FixViewerSettingsState.getInstance(getProject()).setDictionaryEntries(new ArrayList<>());
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            FixViewerSettingsState.getInstance(getProject()).setDictionaryEntries(new ArrayList<>());
        } finally {
            super.tearDown();
        }
    }

    public void testNavigatesToFieldInsideCurrentMessageType() throws Exception {
        File dictionaryFile = createDictionary("""
                <fix>
                  <fields>
                    <field number="11" name="ClOrdID" type="STRING"/>
                  </fields>
                  <messages>
                    <message name="ExecutionReport" msgtype="8" msgcat="app">
                      <field name="ClOrdID" required="N"/>
                    </message>
                    <message name="TradeCaptureReport" msgtype="AE" msgcat="app">
                      <field name="ClOrdID" required="Y"/>
                    </message>
                  </messages>
                </fix>
                """);
        setCustomDefaultDictionary(dictionaryFile, "FIX.4.2");

        String fixMessage = "8=FIX.4.2|9=120|35=AE|11=ABC123|10=001|";
        myFixture.configureByText("message.fix", withCaretOnTag(fixMessage, "11="));

        FixMessageGotoDeclarationHandler handler = new FixMessageGotoDeclarationHandler();
        PsiElement target = handler.getGotoDeclarationTarget(sourceAtCaret(), myFixture.getEditor());

        assertNotNull(target);
        assertInstanceOf(target, XmlAttribute.class);
        XmlAttribute attribute = (XmlAttribute) target;
        assertEquals("name", attribute.getName());
        XmlTag fieldTag = attribute.getParent();
        assertNotNull(fieldTag);
        assertEquals("field", fieldTag.getName());
        XmlTag messageTag = fieldTag.getParentTag();
        assertNotNull(messageTag);
        assertEquals("message", messageTag.getName());
        assertEquals("AE", messageTag.getAttributeValue("msgtype"));
    }

    public void testFallsBackToGlobalFieldDefinitionWhenMessageDoesNotContainField() throws Exception {
        File dictionaryFile = createDictionary("""
                <fix>
                  <fields>
                    <field number="999" name="PartyRoleQualifier" type="INT"/>
                  </fields>
                  <messages>
                    <message name="TradeCaptureReport" msgtype="AE" msgcat="app">
                      <field name="ClOrdID" required="Y"/>
                    </message>
                  </messages>
                </fix>
                """);
        setCustomDefaultDictionary(dictionaryFile, "FIX.4.2");

        String fixMessage = "8=FIX.4.2|35=AE|999=5|10=001|";
        myFixture.configureByText("message.fix", withCaretOnTag(fixMessage, "999="));

        FixMessageGotoDeclarationHandler handler = new FixMessageGotoDeclarationHandler();
        PsiElement target = handler.getGotoDeclarationTarget(sourceAtCaret(), myFixture.getEditor());

        assertNotNull(target);
        assertInstanceOf(target, XmlAttribute.class);
        XmlAttribute attribute = (XmlAttribute) target;
        XmlTag fieldTag = attribute.getParent();
        assertNotNull(fieldTag);
        XmlTag parent = fieldTag.getParentTag();
        assertNotNull(parent);
        assertEquals("fields", parent.getName());
    }

    public void testReturnsNullWhenOnlyBuiltInDictionaryIsConfigured() {
        String fixMessage = "8=FIX.4.2|35=AE|11=ABC123|10=001|";
        myFixture.configureByText("message.fix", withCaretOnTag(fixMessage, "11="));

        FixMessageGotoDeclarationHandler handler = new FixMessageGotoDeclarationHandler();
        PsiElement target = handler.getGotoDeclarationTarget(sourceAtCaret(), myFixture.getEditor());

        assertNull(target);
    }

    private PsiElement sourceAtCaret() {
        int offset = myFixture.getEditor().getCaretModel().getOffset();
        PsiElement atOffset = myFixture.getFile().findElementAt(offset);
        if (atOffset != null && atOffset.getNode() != null && atOffset.getNode().getElementType() == com.rannett.fixplugin.psi.FixTypes.TAG) {
            return atOffset;
        }
        if (offset > 0) {
            PsiElement left = myFixture.getFile().findElementAt(offset - 1);
            if (left != null && left.getNode() != null && left.getNode().getElementType() == com.rannett.fixplugin.psi.FixTypes.TAG) {
                return left;
            }
        }
        return atOffset;
    }

    private void setCustomDefaultDictionary(File dictionaryFile, String version) {
        DictionaryEntry builtIn = DictionaryEntry.builtIn(version);
        builtIn.setDefaultDictionary(false);
        DictionaryEntry custom = new DictionaryEntry(version, dictionaryFile.getAbsolutePath(), false, true);
        FixViewerSettingsState.getInstance(getProject()).setDictionaryEntries(List.of(builtIn, custom));
    }

    private File createDictionary(String xml) throws Exception {
        File dictionaryFile = File.createTempFile("goto-dictionary", ".xml");
        Files.writeString(dictionaryFile.toPath(), xml);
        return dictionaryFile;
    }

    private String withCaretOnTag(String message, String tagPrefix) {
        int index = message.indexOf(tagPrefix);
        assertTrue(index >= 0);
        return message.substring(0, index + 1) + "<caret>" + message.substring(index + 1);
    }
}
