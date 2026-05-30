package com.rannett.fixplugin.ui;

import org.junit.Test;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FixDictionaryTreePanelTest {

    @Test
    public void testBuildModelExpandsMessagesComponentsGroupsAndFields() {
        String dictionaryXml = """
                <fix major="4" minor="4">
                  <messages>
                    <message name="Advertisement" msgtype="7" msgcat="app">
                      <field name="AdvId" required="Y"/>
                      <component name="Instrument" required="Y"/>
                      <group name="NoLegs" required="N">
                        <field name="LegSymbol" required="N"/>
                      </group>
                    </message>
                  </messages>
                  <components>
                    <component name="Instrument">
                      <field name="Symbol" required="Y"/>
                    </component>
                  </components>
                  <fields>
                    <field number="2" name="AdvId" type="STRING"/>
                    <field number="55" name="Symbol" type="STRING">
                      <value enum="A" description="Accepted"/>
                      <value enum="R" description="Rejected"/>
                    </field>
                    <field number="555" name="NoLegs" type="NUMINGROUP"/>
                    <field number="600" name="LegSymbol" type="STRING"/>
                  </fields>
                </fix>
                """;

        DefaultTreeModel model = FixDictionaryTreePanel.buildModel(dictionaryXml);
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        DefaultMutableTreeNode message = (DefaultMutableTreeNode) root.getChildAt(0);
        DefaultMutableTreeNode field = (DefaultMutableTreeNode) message.getChildAt(0);
        DefaultMutableTreeNode component = (DefaultMutableTreeNode) message.getChildAt(1);
        DefaultMutableTreeNode componentField = (DefaultMutableTreeNode) component.getChildAt(0);
        DefaultMutableTreeNode group = (DefaultMutableTreeNode) message.getChildAt(2);
        DefaultMutableTreeNode groupField = (DefaultMutableTreeNode) group.getChildAt(0);

        assertEquals("Advertisement (7, app)", message.getUserObject().toString());
        assertEquals("AdvId [required] (2, STRING)", field.getUserObject().toString());
        assertEquals("Instrument component [required]", component.getUserObject().toString());
        assertEquals("Symbol [required] (55, STRING)", componentField.getUserObject().toString());
        assertEquals("NoLegs group [optional] (555)", group.getUserObject().toString());
        assertEquals("LegSymbol [optional] (600, STRING)", groupField.getUserObject().toString());
        assertNull(((FixDictionaryTreePanel.TreeNodePresentation) field.getUserObject()).tooltip());
        assertEquals("AdvId", ((FixDictionaryTreePanel.TreeNodePresentation) field.getUserObject()).fieldName());
        assertEquals(
                "<html><b>Enum values</b><br>A - Accepted<br>R - Rejected</html>",
                ((FixDictionaryTreePanel.TreeNodePresentation) componentField.getUserObject()).tooltip()
        );
        assertEquals("Symbol", ((FixDictionaryTreePanel.TreeNodePresentation) componentField.getUserObject()).fieldName());
    }

    @Test
    public void testBuildModelCanOrderTopLevelMessagesOnly() {
        String dictionaryXml = """
                <fix major="4" minor="4">
                  <messages>
                    <message name="Zulu" msgtype="Z" msgcat="app">
                      <field name="SecondField" required="N"/>
                      <field name="FirstField" required="Y"/>
                    </message>
                    <message name="Alpha" msgtype="AE" msgcat="app">
                      <field name="FirstField" required="Y"/>
                    </message>
                    <message name="Middle" msgtype="B" msgcat="app">
                      <field name="SecondField" required="N"/>
                    </message>
                  </messages>
                  <fields>
                    <field number="1" name="FirstField" type="STRING"/>
                    <field number="2" name="SecondField" type="STRING"/>
                  </fields>
                </fix>
                """;

        DefaultMutableTreeNode dictionaryRoot = rootFor(dictionaryXml, FixDictionaryTreePanel.MessageOrder.DICTIONARY);
        DefaultMutableTreeNode nameRoot = rootFor(dictionaryXml, FixDictionaryTreePanel.MessageOrder.NAME);
        DefaultMutableTreeNode typeRoot = rootFor(dictionaryXml, FixDictionaryTreePanel.MessageOrder.TYPE);
        DefaultMutableTreeNode zuluMessage = (DefaultMutableTreeNode) nameRoot.getChildAt(2);

        assertEquals("Zulu (Z, app)", dictionaryRoot.getChildAt(0).toString());
        assertEquals("Alpha (AE, app)", nameRoot.getChildAt(0).toString());
        assertEquals("Alpha (AE, app)", typeRoot.getChildAt(0).toString());
        assertEquals("Middle (B, app)", typeRoot.getChildAt(1).toString());
        assertEquals("SecondField [optional] (2, STRING)", zuluMessage.getChildAt(0).toString());
        assertEquals("FirstField [required] (1, STRING)", zuluMessage.getChildAt(1).toString());
    }

    private static DefaultMutableTreeNode rootFor(String dictionaryXml, FixDictionaryTreePanel.MessageOrder order) {
        DefaultTreeModel model = FixDictionaryTreePanel.buildModel(dictionaryXml, order);
        return (DefaultMutableTreeNode) model.getRoot();
    }
}
