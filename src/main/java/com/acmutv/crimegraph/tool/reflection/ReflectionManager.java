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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This class realizes Java reflection services.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @since 1.0
 */
public class ReflectionManager {

  /**
   * Gets an object property by reflection.
   * @param type     the object class to reflect.
   * @param object   the object instance to address.
   * @param property the name of the object property to get.
   * @return the property value.
   * @throws IntrospectionException when property cannot be found.
   * @throws InvocationTargetException when getter method cannot be invoked.
   * @throws IllegalAccessException when getter method cannot be accessed.
   */
  public static Object get(Class<?> type, Object object, String property)
      throws IntrospectionException, InvocationTargetException, IllegalAccessException {
    final BeanInfo beanInfo = Introspector.getBeanInfo(type);
    final PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
    Method getter = Arrays.stream(propertyDescriptors)
        .filter(d -> d.getName().equals(property))
        .findFirst().map(PropertyDescriptor::getReadMethod)
        .orElse(null);
    if (getter == null) {
      throw new IntrospectionException("Cannot find setter nethod for property " + property);
    }
    Object result = getter.invoke(object);

    return result;
  }

  /**
   * Maps object attributes.
   * @param type the class to reflect.
   * @param object the instance to address.
   * @param <T> the class to reflect.
   * @return the attributes mapping.
   * @throws IntrospectionException when errors in reflection.
   * @throws InvocationTargetException when errors in reflection.
   * @throws IllegalAccessException when errors in reflection.
   */
  public static <T> Map<String,Object> getAttributes(Class<T> type, T object)
      throws IntrospectionException, InvocationTargetException, IllegalAccessException {
    Map<String,Object> propsmap = new HashMap<>();
    final BeanInfo beanInfo = Introspector.getBeanInfo(type);
    final PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
    for (PropertyDescriptor pd : propertyDescriptors) {
      if (pd.getName().equals("class")) continue;
      final Method getter = pd.getReadMethod();
      if (getter != null) {
        final String attrname = pd.getName();
        final Object attrvalue = getter.invoke(object);
        propsmap.put(attrname, attrvalue);
      }
    }
    return propsmap;
  }

  /**
   * Sets an object property by reflection.
   * @param type     the class to reflect.
   * @param object   the instance to address.
   * @param property the name of the object property to set.
   * @param value    the value to set the property to.
   * @param <T>      the class to reflect.
   * @throws IntrospectionException when property cannot be found.
   * @throws InvocationTargetException when setter method cannot be invoked.
   * @throws IllegalAccessException when setter method cannot be accessed.
   */
  public static <T> void set(Class<T> type, T object, String property, Object value)
      throws IntrospectionException, InvocationTargetException, IllegalAccessException {
    final BeanInfo beanInfo = Introspector.getBeanInfo(type);
    final PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
    Method setter = Arrays.stream(propertyDescriptors)
        .filter(d -> d.getName().equals(property))
        .findFirst().map(PropertyDescriptor::getWriteMethod)
        .orElse(null);
    if (setter == null) {
      throw new IntrospectionException("Cannot find setter nethod for property " + property);
    }

    setter.invoke(object, value);
  }
}
