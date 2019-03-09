import com.sun.jdi.Field;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Method;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class MasterCacheTest {
    private MasterCache mc = MasterCache.getInstance();

    @Before
    public void setUp() {
       mc.clear();
    }

    @Test
    public void testConstructor() {
        assertEquals(mc.size(), 0);
    }

    @Test
    public void testGetPutObjects() {
        // put(String object, Method method, LocalVariable var, VariableInfo info)
        TestMethod m = new TestMethod("m");
        TestLocalVariable var = new TestLocalVariable("var");
        VariableInfo info = new VariableInfo(0, "var");
        mc.put("obj", m, var, info);

        assertEquals(mc.size(), 1);
    }

    @Test
    public void testGetPutFields() {
        // put(String object, Field var, VariableInfo info)
        TestField f = new TestField("f");
        VariableInfo info = new VariableInfo(0, "f");
        mc.put("obj",f, info);

        assertEquals(mc.size(), 1);
    }

    @Test
    public void testGetAllObjects() {
        TestMethod m = new TestMethod("m");
        TestLocalVariable var = new TestLocalVariable("var");
        VariableInfo info = new VariableInfo(0, "var");
        mc.put("obj", m, var, info);

        TestMethod m2 = new TestMethod("m2");
        TestLocalVariable var2 = new TestLocalVariable("var2");
        VariableInfo info2 = new VariableInfo(0, "var2");
        mc.put("obj2", m2, var2, info2);

        List<String> objs = mc.getAllObjects();
        assertEquals(objs.size(), 2);
        assertTrue(objs.contains("obj"));
        assertTrue(objs.contains("obj2"));
    }

    @Test
    public void testGetAllMethods() {
        TestMethod m = new TestMethod("m");
        TestLocalVariable var = new TestLocalVariable("var");
        VariableInfo info = new VariableInfo(0, "var");
        mc.put("obj", m, var, info);

        TestMethod m2 = new TestMethod("m2");
        TestLocalVariable var2 = new TestLocalVariable("var2");
        VariableInfo info2 = new VariableInfo(0, "var2");
        mc.put("obj", m2, var2, info2);

        List<Method> methods = mc.getAllMethods("obj");
        assertEquals(methods.size(), 2);
        assertTrue(methods.contains(m));
        assertTrue(methods.contains(m2));
    }

    @Test
    public void testGetAllLocalVariables() {
        TestMethod m = new TestMethod("m");
        TestLocalVariable var = new TestLocalVariable("var");
        VariableInfo info = new VariableInfo(0, "var");
        mc.put("obj", m, var, info);

        TestLocalVariable var2 = new TestLocalVariable("var");
        VariableInfo info2 = new VariableInfo(0, "var");
        mc.put("obj", m, var2, info2);

        List<LocalVariable> vars = mc.getAllLocalVariables("obj", m);
        assertEquals(vars.size(), 2);
        assertTrue(vars.contains(var));
        assertTrue(vars.contains(var2));
    }

    @Test
    public void testGetAllFields() {
        TestMethod m = new TestMethod("m");
        TestField f = new TestField("f");
        VariableInfo info = new VariableInfo(0, "f");
        mc.put("obj", f, info);

        TestField f2 = new TestField("f2");
        VariableInfo info2 = new VariableInfo(0, "f2");
        mc.put("obj", f2, info2);

        List<Field> fields = mc.getAllFields("obj");
        assertEquals(fields.size(), 2);
        assertTrue(fields.contains(f));
        assertTrue(fields.contains(f2));
    }

    @Test
    public void testGetVarHistory() {
        TestMethod m = new TestMethod("m");
        TestLocalVariable var = new TestLocalVariable("var");
        VariableInfo info = new VariableInfo(0, "var");
        mc.put("obj", m, var, info);
        VariableInfo info2 = new VariableInfo(1, "var2");
        mc.put("obj", m, var, info2);

        List<VariableInfo> infos = mc.getHistory("obj", m, var);
        assertEquals(infos.size(), 2);
        assertEquals(infos.get(0), info);
        assertEquals(infos.get(1), info2);
    }

    @Test
    public void testGetFieldHistory() {
        TestField f = new TestField("f");
        VariableInfo info = new VariableInfo(0, "f");
        mc.put("obj", f, info);
        VariableInfo info2 = new VariableInfo(1, "f2");
        mc.put("obj", f, info2);

        List<VariableInfo> infos = mc.getHistory("obj", null, f);
        assertEquals(infos.size(), 2);
        assertEquals(infos.get(0), info);
        assertEquals(infos.get(1), info2);
    }

    @Test
    public void testGetVarsCache() {
        TestMethod m = new TestMethod("m");
        TestLocalVariable var = new TestLocalVariable("var");
        VariableInfo info = new VariableInfo(0, "var");
        mc.put("obj", m, var, info);

        DebugCache dc = mc.getVarsCache("obj", m);
        assertEquals(dc.size(), 1);
        assertTrue(dc.getAllVariables().contains(var));
        assertEquals(dc.getHistory(var).get(0), info);

    }

    @Test
    public void testGetFieldsCache() {
        TestField f = new TestField("f");
        VariableInfo info = new VariableInfo(0, "f");
        mc.put("obj", f, info);
        VariableInfo info2 = new VariableInfo(1, "f2");
        mc.put("obj", f, info2);

        DebugCache dc = mc.getFieldsCache("obj");
        assertEquals(dc.size(), 1);
        assertTrue(dc.getAllFields().contains(f));
        assertEquals(dc.getHistory(f).get(0), info);
    }

    @Test
    public void testClear() {
        assertEquals(mc.size(), 0);
    }

}
