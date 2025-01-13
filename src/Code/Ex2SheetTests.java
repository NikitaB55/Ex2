package Code;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class Ex2SheetTests {

    private Ex2Sheet sheet;

    @BeforeEach
    void setUp() {
        sheet = new Ex2Sheet(6, 6); // Initialize a 6x6 sheet
    }

    @Test
    void testSheetInitialization() {
        assertNotNull(sheet);
        assertEquals(6, sheet.width());
        assertEquals(6, sheet.height());
        assertEquals("", sheet.value(0, 0));
    }

    @Test
    void testSetAndRetrieveCellData() {
        sheet.set(0, 0, "42");
        assertEquals("42.0", sheet.value(0, 0));

        sheet.set(1, 1, "Test");
        assertEquals("Test", sheet.value(1, 1));

        sheet.set(2, 2, "=3+4");
        assertEquals("7.0", sheet.value(2, 2));
    }

    @Test
    void testSimpleFormulaEvaluation() {
        sheet.set(0, 0, "5");
        sheet.set(0, 1, "10");
        sheet.set(0, 2, "=A0+A1");

        assertEquals("15.0", sheet.value(0, 2));
    }

    @Test
    void testFormulaWithReferences() {
        sheet.set(0, 0, "8");
        sheet.set(0, 1, "2");
        sheet.set(0, 2, "=A0*A1");
        assertEquals("16.0", sheet.value(0, 2));

        sheet.set(1, 0, "=A0+A1+A2");
        assertEquals("26.0", sheet.value(1, 0));
    }

    @Test
    void testErrorInvalidReference() {
        sheet.set(0, 0, "=X10"); // Invalid cell reference
        assertEquals("ERR_FORM!", sheet.value(0, 0));
    }

    @Test
    void testErrorCycleDetection() {
        sheet.set(0, 0, "=A1");
        sheet.set(0, 1, "=A0");

        assertEquals("ERR_CYCLE!", sheet.value(0, 0));
        assertEquals("ERR_CYCLE!", sheet.value(0, 1));
    }

    @Test
    void testSaveAndLoadSheet() throws IOException {
        sheet.set(0, 0, "15");
        sheet.set(0, 1, "=A0*2");

        File file = new File("sheet_test.csv");
        sheet.save(file.getAbsolutePath());

        Ex2Sheet loadedSheet = new Ex2Sheet();
        loadedSheet.load(file.getAbsolutePath());

        assertEquals("15.0", loadedSheet.value(0, 0));
        assertEquals("30.0", loadedSheet.value(0, 1));

        file.delete(); // Cleanup test file
    }

    @Test
    void testComplexFormulas() {
        sheet.set(0, 0, "20");
        sheet.set(0, 1, "4");
        sheet.set(0, 2, "=A0/A1");
        assertEquals("5.0", sheet.value(0, 2));

        sheet.set(1, 0, "=A0-A1+A2");
        assertEquals("21.0", sheet.value(1, 0));
    }

    @Test
    void testDependencies() {
        sheet.set(0, 0, "50");
        sheet.set(0, 1, "=A0/2");
        sheet.set(0, 2, "=A1+10");

        assertEquals("25.0", sheet.value(0, 1));
        assertEquals("35.0", sheet.value(0, 2));

        sheet.set(0, 0, "60"); // Update A0 and check if dependencies are updated
        assertEquals("30.0", sheet.value(0, 1));
        assertEquals("40.0", sheet.value(0, 2));
    }

    @Test
    void testInvalidFormulaFormat() {
        sheet.set(0, 0, "=5++");
        assertEquals("ERR_FORM!", sheet.value(0, 0));
    }
}
