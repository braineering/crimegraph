/*
  The MIT License (MIT)

  Copyright (c) 2016 Giacomo Marciani and Michele Porretta

  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:


  The above copyright notice and this permission notice shall be included in
  all copies or substantial portions of the Software.


  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  THE SOFTWARE.
 */

package com.acmutv.crimegraph.tool.reflection;

import lombok.Data;
import org.junit.Assert;
import org.junit.Test;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class realizes JUnit tests for {@link ReflectionManager}.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @since 1.0
 * @see ReflectionManager
 */
public class ReflectionManagerTest {

  /**
   * Utility class (for testing use only).
   */
  @Data
  private static class CustomObject {
    private boolean propBoolean = false;
    private int propInt = 10;
    private long propLong = 10;
    private double propDouble = 10.10;
    private String propString = "default";
  }

  /**
   * Tests attributes mapping.
   * @throws IllegalAccessException when errors in reflection.
   * @throws IntrospectionException when errors in reflection.
   * @throws InvocationTargetException when errors in reflection.
   */
  @Test
  public void test_getAttributes()
      throws IllegalAccessException, IntrospectionException, InvocationTargetException {
    CustomObject object = new CustomObject();
    Map<String,Object> actual = ReflectionManager.getAttributes(CustomObject.class, object);
    Map<String,Object> expected = new HashMap<>();
    expected.put("propBoolean", false);
    expected.put("propInt", 10);
    expected.put("propLong", 10L);
    expected.put("propDouble", 10.10);
    expected.put("propString", "default");
    Assert.assertEquals(expected, actual);
  }

  /**
   * Tests reflected getter.
   * @throws IllegalAccessException when errors in reflection.
   * @throws IntrospectionException when errors in reflection.
   * @throws InvocationTargetException when errors in reflection.
   */
  @Test
  public void test_get()
      throws IllegalAccessException, IntrospectionException, InvocationTargetException {
    CustomObject object = new CustomObject();
    Object actualBoolean = ReflectionManager.get(CustomObject.class, object, "propBoolean");
    Object actualInt = ReflectionManager.get(CustomObject.class, object, "propInt");
    Object actualLong = ReflectionManager.get(CustomObject.class, object, "propLong");
    Object actualDouble = ReflectionManager.get(CustomObject.class, object, "propDouble");
    Object actualString = ReflectionManager.get(CustomObject.class, object, "propString");
    Assert.assertNotNull(actualBoolean);
    Assert.assertNotNull(actualInt);
    Assert.assertNotNull(actualLong);
    Assert.assertNotNull(actualDouble);
    Assert.assertNotNull(actualString);
    Assert.assertEquals(false, (boolean) actualBoolean);
    Assert.assertEquals(10, (int) actualInt);
    Assert.assertEquals(10, (long) actualLong);
    Assert.assertEquals(10.10, (double) actualDouble,0);
    Assert.assertEquals("default", (String) actualString);
  }

  /**
   * Tests reflected getter (error).
   */
  @Test
  public void test_get_error() {
    CustomObject object = new CustomObject();
    try {
      ReflectionManager.get(CustomObject.class, object, "prop");
    } catch (IntrospectionException | IllegalAccessException | InvocationTargetException exc) {
      return;
    }
    Assert.fail();
  }

  /**
   * Tests reflected setter for boolean properties.
   * @throws IllegalAccessException when errors in reflection.
   * @throws IntrospectionException when errors in reflection.
   * @throws InvocationTargetException when errors in reflection.
   */
  @Test
  public void test_set()
      throws IllegalAccessException, IntrospectionException, InvocationTargetException {
    CustomObject object = new CustomObject();
    ReflectionManager.set(CustomObject.class, object, "propBoolean", true);
    ReflectionManager.set(CustomObject.class, object, "propInt", 20);
    ReflectionManager.set(CustomObject.class, object, "propLong", 20);
    ReflectionManager.set(CustomObject.class, object, "propDouble", 20.20);
    ReflectionManager.set(CustomObject.class, object, "propString", "custom");
    boolean actualPropBoolean = object.isPropBoolean();
    int actualPropInt = object.getPropInt();
    long actualPropLong = object.getPropLong();
    double actualPropDouble = object.getPropDouble();
    String actualPropString = object.getPropString();
    Assert.assertEquals(true, actualPropBoolean);
    Assert.assertEquals(20, actualPropInt);
    Assert.assertEquals(20, actualPropLong);
    Assert.assertEquals(20.20, actualPropDouble,0);
    Assert.assertEquals("custom", actualPropString);
  }

  /**
   * Tests reflected setter (error).
   */
  @Test
  public void test_set_error() {
    CustomObject object = new CustomObject();
    try {
      ReflectionManager.set(CustomObject.class, object, "prop", "custom");
    }  catch (IntrospectionException | IllegalAccessException | InvocationTargetException exc) {
      return;
    }
    Assert.fail();
  }
}
