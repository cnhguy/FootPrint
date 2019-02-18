import com.sun.tools.jdi.BooleanValueImpl;
import org.junit.Before;
import org.junit.Test;
import com.sun.jdi.Value;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class VariableInfoTest {
    VariableInfo info;

    @Before
    public void setUp() {
        Value val = new TestValue("v");
        info = new VariableInfo(0, val);
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
        Value val2 = new TestValue("v");
        VariableInfo info2 = new VariableInfo(0, val2);
        assertEquals(info, info2);

        Value val3 = new TestValue("v");
        VariableInfo info3 = new VariableInfo(1, val3);
        assertEquals(info, info3);
        assertEquals(info2, info3);

        Value val4 = new TestValue("v4");
        VariableInfo info4 = new VariableInfo(0, val4);
        assertNotEquals(info, info4);
        assertNotEquals(info2, info4);
        assertNotEquals(info3, info4);
    }
}
