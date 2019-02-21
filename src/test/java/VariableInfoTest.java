import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class VariableInfoTest {
    VariableInfo info;

    @Before
    public void setUp() {
        info = new VariableInfo(0, "v");
    }

    @Test
    public void testConstructor() {
        assertEquals(info.getLine(), 0);
        assertEquals(info.getValue(), "v");
    }

    @Test
    public void testToString() {
        assertEquals(info.toString(), "line: 0, value: v");
    }

    @Test
    public void testEquals() {

        VariableInfo info2 = new VariableInfo(0, "v");
        assertEquals(info, info2);

        VariableInfo info3 = new VariableInfo(1, "v");
        assertEquals(info, info3);
        assertEquals(info2, info3);

        VariableInfo info4 = new VariableInfo(0, "v4");
        assertNotEquals(info, info4);
        assertNotEquals(info2, info4);
        assertNotEquals(info3, info4);
    }
}
