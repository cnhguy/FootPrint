import com.sun.jdi.Field;
import com.sun.jdi.LocalVariable;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class DebugCacheTest {

    private DebugCache dc = new DebugCache();

    @Before
    public void setUp() {
        dc.clear();
    }

    @Test
    public void testConstructor() {
        assertEquals(dc.size(), 0);
    }

    @Test
    public void testPutAndGetLV() {
        TestLocalVariable lv = new TestLocalVariable("lv");
        VariableInfo info = new VariableInfo(0, "lv");
        dc.put(lv, info);

        List<VariableInfo> infos = dc.getHistory(lv);
        assertEquals(infos.size(), 1);
        assertEquals(infos.get(0), info);
        assertEquals(infos.get(0).getLine(), 0);
        assertEquals(infos.get(0).getValue(), "lv");
    }

    @Test
    public void testPutAndGetField() {
        TestField f = new TestField("f");
        VariableInfo info = new VariableInfo(0, "f");
        dc.put(f, info);

        List<VariableInfo> infos = dc.getHistory(f);
        assertEquals(infos.size(), 1);
        assertEquals(infos.get(0), info);
        assertEquals(infos.get(0).getLine(), 0);
        assertEquals(infos.get(0).getValue(), "f");
    }

    @Test
    public void testMultiPut() {
        TestLocalVariable lv = new TestLocalVariable("lv");
        VariableInfo info1 = new VariableInfo(0, "lv");
        TestField f = new TestField("f");
        VariableInfo info2 = new VariableInfo(0, "f");

        dc.put(lv, info1);
        dc.put(f, info2);

        VariableInfo info3 = new VariableInfo(1, "lv");
        VariableInfo info4 = new VariableInfo(1, "f");

        dc.put(lv, info3);
        dc.put(f, info4);

        List<VariableInfo> lvInfos = dc.getHistory(lv);
        List<VariableInfo> fInfos = dc.getHistory(f);

        // info should only be added if value has updated
        assertEquals(lvInfos.size(), 1);
        assertEquals(fInfos.size(), 1);

        VariableInfo info5 = new VariableInfo(2, "lv2");
        VariableInfo info6 = new VariableInfo(2, "f2");

        dc.put(lv, info5);
        dc.put(f, info6);

        lvInfos = dc.getHistory(lv);
        fInfos = dc.getHistory(f);

        assertEquals(lvInfos.size(), 2);
        assertEquals(fInfos.size(), 2);
        assertEquals(dc.size(), 2);

    }

    @Test
    public void testGetAllVariables() {
        TestLocalVariable lv1 = new TestLocalVariable("lv");
        VariableInfo info1 = new VariableInfo(0, "lv");
        TestLocalVariable lv2 = new TestLocalVariable("lv2");
        VariableInfo info2 = new VariableInfo(1, "lv2");
        dc.put(lv1, info1);
        dc.put(lv2, info2);

        List<LocalVariable> vars = dc.getAllVariables();
        assertEquals(vars.size(), 2);
        assertEquals(vars.get(0), lv1);
        assertEquals(vars.get(1), lv2);
    }

    @Test
    public void testGetAllFields() {
        TestField f1 = new TestField("lv");
        VariableInfo info1 = new VariableInfo(0, "lv");
        TestField f2 = new TestField("lv2");
        VariableInfo info2 = new VariableInfo(1, "lv2");
        dc.put(f1, info1);
        dc.put(f2, info2);

        List<Field> vars = dc.getAllFields();
        assertEquals(vars.size(), 2);
        assertEquals(vars.get(0), f1);
        assertEquals(vars.get(1), f2);
    }

    @Test
    public void testClear() {
        assertEquals(dc.size(), 0);
    }

    @Test
    public void testGetMostRecentUpdate() {
        TestLocalVariable lv = new TestLocalVariable("lv");
        VariableInfo info1 = new VariableInfo(0, "lv");
        VariableInfo info2 = new VariableInfo(1, "lv2");
        dc.put(lv, info1);
        dc.put(lv, info2);

        VariableInfo recent = dc.getMostRecentUpdate(lv);
        assertEquals(recent, info2);
    }

}
