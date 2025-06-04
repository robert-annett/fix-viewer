package com.rannett.fixplugin.inspection;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class FixChecksumInspectionTest extends BasePlatformTestCase {

    public void testValidFixMessage() {
        String message = "8=FIX.4.2\u00019=178\u000135=8\u000149=PHLX\u000156=PERS\u000152=20071123-05:30:00.000" +
                "\u000111=ATOMNOCCC9990900\u000120=3\u0001150=E\u000139=E\u000155=MSFT\u0001167=CS\u000154=1" +
                "\u000138=15\u000140=2\u000144=15\u000158=PHLX EQUITY TESTING\u000159=0\u000147=C\u000132=0" +
                "\u000131=0\u0001151=15\u000114=0\u00016=0\u000110=128\u0001";

        myFixture.configureByText("valid.fix", message);
        myFixture.enableInspections(new FixChecksumInspection());
        myFixture.checkHighlighting(); // No error expected
    }

    public void testInvalidFixMessage() {
        String message = "8=FIX.4.2\u00019=178\u000135=8\u000149=PHLX\u000156=PERS\u000152=20071123-05:30:00.000" +
                "\u000111=ATOMNOCCC9990900\u000120=3\u0001150=E\u000139=E\u000155=MSFT\u0001167=CS\u000154=1" +
                "\u000138=15\u000140=2\u000144=15\u000158=PHLX EQUITY TESTING\u000159=0\u000147=C\u000132=0" +
                "\u000131=0\u0001151=15\u000114=0\u00016=0\u0001" +
                "<warning descr=\"Incorrect checksum (expected 128)\">10=999</warning>\u0001";

        myFixture.configureByText("invalid.fix", message);
        myFixture.enableInspections(new FixChecksumInspection());
        myFixture.checkHighlighting(true, false, true);
    }

    public void testQuickFixUpdatesChecksum() {
        String message = "8=FIX.4.2\u00019=178\u000135=8\u000149=PHLX\u000156=PERS\u000152=20071123-05:30:00.000" +
                "\u000111=ATOMNOCCC9990900\u000120=3\u0001150=E\u000139=E\u000155=MSFT\u0001167=CS\u000154=1" +
                "\u000138=15\u000140=2\u000144=15\u000158=PHLX EQUITY TESTING\u000159=0\u000147=C\u000132=0" +
                "\u000131=0\u0001151=15\u000114=0\u00016=0\u0001" +
                "<caret>10=999\u0001";

        myFixture.configureByText("invalid.fix", message);
        myFixture.enableInspections(new FixChecksumInspection());
        var intention = myFixture.findSingleIntention("Correct checksum to 128");
        myFixture.launchAction(intention);

        String expected = "8=FIX.4.2\u00019=178\u000135=8\u000149=PHLX\u000156=PERS\u000152=20071123-05:30:00.000" +
                "\u000111=ATOMNOCCC9990900\u000120=3\u0001150=E\u000139=E\u000155=MSFT\u0001167=CS\u000154=1" +
                "\u000138=15\u000140=2\u000144=15\u000158=PHLX EQUITY TESTING\u000159=0\u000147=C\u000132=0" +
                "\u000131=0\u0001151=15\u000114=0\u00016=0\u000110=128\u0001";
        myFixture.checkResult(expected);
    }

}
