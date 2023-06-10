package plantUMLGenerator;

import com.google.common.base.Objects;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.CollectionExtensions;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;

@SuppressWarnings("all")
public class PlantUMLGenerator {
  private static HashSet<UnorderedPair<Class>> classesWithAssociationsToEachOther;

  private static HashSet<UnorderedPair<Class>> classesWithBiDirectionalAssociationsToEachOther;

  public static String generate(final Object[] objects) {
    HashSet<Class<?>> uniqueInterfaces = new HashSet<Class<?>>();
    for (final Object obj : objects) {
      CollectionExtensions.<Class<?>>addAll(uniqueInterfaces, obj.getClass().getInterfaces());
    }
    final Function1<Class<?>, String> _function = (Class<?> it) -> {
      return PlantUMLGenerator.interfaceToPlantUMLCode(it);
    };
    final String interfaceCode = IterableExtensions.join(IterableExtensions.<Class<?>, String>map(uniqueInterfaces, _function), "\n");
    final Function1<Object, String> _function_1 = (Object it) -> {
      return PlantUMLGenerator.objectToPlantUMLCode(it);
    };
    final String classCode = IterableExtensions.join(ListExtensions.<Object, String>map(((List<Object>)Conversions.doWrapArray(objects)), _function_1), "\n");
    final Function1<Object, String> _function_2 = (Object it) -> {
      return PlantUMLGenerator.getGeneralizations(it);
    };
    final String generalizations = IterableExtensions.join(ListExtensions.<Object, String>map(((List<Object>)Conversions.doWrapArray(objects)), _function_2), "\n");
    final Function1<Object, String> _function_3 = (Object it) -> {
      return PlantUMLGenerator.getRealizations(it);
    };
    final String realizations = IterableExtensions.join(ListExtensions.<Object, String>map(((List<Object>)Conversions.doWrapArray(objects)), _function_3), "\n");
    PlantUMLGenerator.computeClassesWithBiDirectionalAssociationsToEachOther(objects);
    final Function1<Object, String> _function_4 = (Object it) -> {
      return PlantUMLGenerator.getAssociations(it);
    };
    final String associations = IterableExtensions.join(ListExtensions.<Object, String>map(((List<Object>)Conversions.doWrapArray(objects)), _function_4), "\n");
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("@startuml");
    _builder.newLine();
    _builder.append(interfaceCode);
    _builder.newLineIfNotEmpty();
    _builder.append(classCode);
    _builder.newLineIfNotEmpty();
    _builder.newLine();
    _builder.append(generalizations);
    _builder.newLineIfNotEmpty();
    _builder.append(realizations);
    _builder.newLineIfNotEmpty();
    _builder.append(associations);
    _builder.newLineIfNotEmpty();
    _builder.append("@enduml");
    _builder.newLine();
    _builder.newLine();
    return _builder.toString();
  }

  public static String objectToPlantUMLCode(final Object object) {
    final Class<?> clazz = object.getClass();
    final String className = clazz.getSimpleName();
    final Function1<Field, Boolean> _function = (Field it) -> {
      return Boolean.valueOf((it.getType().isPrimitive() || it.getType().isEnum()));
    };
    final Function1<Field, String> _function_1 = (Field it) -> {
      return PlantUMLGenerator.getFields(it, object);
    };
    final String fields = IterableExtensions.join(IterableExtensions.<Field, String>map(IterableExtensions.<Field>filter(((Iterable<Field>)Conversions.doWrapArray(clazz.getDeclaredFields())), _function), _function_1), 
      "\n");
    final Function1<Method, Boolean> _function_2 = (Method it) -> {
      return Boolean.valueOf(((!PlantUMLGenerator.getInterfaceMethodNames(object).contains(
        it.getName())) && (!(it.getName().startsWith("get") || it.getName().startsWith("set")))));
    };
    final Function1<Method, String> _function_3 = (Method it) -> {
      return PlantUMLGenerator.getMethodDefinition(it);
    };
    final String methods = IterableExtensions.join(IterableExtensions.<Method, String>map(IterableExtensions.<Method>filter(((Iterable<Method>)Conversions.doWrapArray(clazz.getDeclaredMethods())), _function_2), _function_3), "\n");
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("class ");
    _builder.append(className);
    _builder.append(" {");
    _builder.newLineIfNotEmpty();
    _builder.append("  ");
    _builder.append(fields, "  ");
    _builder.newLineIfNotEmpty();
    _builder.append("  ");
    _builder.append(methods, "  ");
    _builder.newLineIfNotEmpty();
    _builder.append("}");
    _builder.newLine();
    return _builder.toString();
  }

  public static HashSet<String> getInterfaceMethodNames(final Object object) {
    HashSet<String> interfaceMethodNames = new HashSet<String>();
    Class<?>[] _interfaces = object.getClass().getInterfaces();
    for (final Class interface_ : _interfaces) {
      Method[] _declaredMethods = interface_.getDeclaredMethods();
      for (final Method method : _declaredMethods) {
        interfaceMethodNames.add(method.getName());
      }
    }
    return interfaceMethodNames;
  }

  public static String interfaceToPlantUMLCode(final Class clazz) {
    final String className = clazz.getSimpleName();
    final Function1<Method, String> _function = (Method it) -> {
      return PlantUMLGenerator.getMethodDefinition(it);
    };
    final String methods = IterableExtensions.join(ListExtensions.<Method, String>map(((List<Method>)Conversions.doWrapArray(clazz.getDeclaredMethods())), _function), "\n");
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("interface ");
    _builder.append(className);
    _builder.append(" {");
    _builder.newLineIfNotEmpty();
    _builder.append("  ");
    _builder.append(methods, "  ");
    _builder.newLineIfNotEmpty();
    _builder.append("}");
    _builder.newLine();
    return _builder.toString();
  }

  public static String getFields(final Field field, final Object object) {
    final int modifiers = field.getModifiers();
    final boolean isPrivate = Modifier.isPrivate(modifiers);
    final String fieldName = field.getName();
    final String fieldType = field.getType().getSimpleName();
    final String fieldString = ((fieldName + ": ") + fieldType);
    return fieldString;
  }

  public static String getMethodDefinition(final Method method) {
    final int modifiers = method.getModifiers();
    final boolean isPrivate = Modifier.isPrivate(modifiers);
    final String methodName = method.getName();
    final Class<?> returnType = method.getReturnType();
    final String parameters = PlantUMLGenerator.getFormattedParameters(method.getParameters());
    String _xifexpression = null;
    if (isPrivate) {
      _xifexpression = "-";
    } else {
      _xifexpression = "+";
    }
    StringConcatenation _builder = new StringConcatenation();
    _builder.append(methodName);
    _builder.append(" (");
    _builder.append(parameters);
    _builder.append("): ");
    _builder.append(returnType);
    final String methodString = (_xifexpression + _builder);
    return methodString;
  }

  public static String getFormattedParameters(final Parameter[] parameters) {
    ArrayList<String> paramStrings = new ArrayList<String>();
    int _length = parameters.length;
    boolean _equals = (_length == 0);
    if (_equals) {
      return "";
    }
    StringConcatenation _builder = new StringConcatenation();
    String _name = (parameters[0]).getName();
    _builder.append(_name);
    _builder.append(": ");
    String _simpleName = (parameters[0]).getType().getSimpleName();
    _builder.append(_simpleName);
    paramStrings.add(_builder.toString());
    for (int i = 1; (i < parameters.length); i++) {
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append(", ");
      String _name_1 = (parameters[i]).getName();
      _builder_1.append(_name_1);
      _builder_1.append(": ");
      String _simpleName_1 = (parameters[i]).getType().getSimpleName();
      _builder_1.append(_simpleName_1);
      paramStrings.add(_builder_1.toString());
    }
    return String.join("", paramStrings);
  }

  public static String getGeneralizations(final Object obj) {
    final Class<?> clazz = obj.getClass();
    final String className = clazz.getSimpleName();
    final Class<?> superClass = clazz.getSuperclass();
    String _xifexpression = null;
    if ((superClass != Object.class)) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append(className);
      _builder.append(" --|> ");
      String _simpleName = superClass.getSimpleName();
      _builder.append(_simpleName);
      _xifexpression = _builder.toString();
    } else {
      _xifexpression = "";
    }
    return _xifexpression;
  }

  public static String getRealizations(final Object obj) {
    final Class<?> clazz = obj.getClass();
    final String className = clazz.getSimpleName();
    final Function1<Class<?>, String> _function = (Class<?> it) -> {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append(className);
      _builder.append(" ..|> ");
      String _simpleName = it.getSimpleName();
      _builder.append(_simpleName);
      return _builder.toString();
    };
    final String interfaces = IterableExtensions.join(ListExtensions.<Class<?>, String>map(((List<Class<?>>)Conversions.doWrapArray(clazz.getInterfaces())), _function), "\n");
    return interfaces;
  }

  public static String getAssociations(final Object obj) {
    final Class<?> clazz = obj.getClass();
    final Function1<Field, String> _function = (Field it) -> {
      return PlantUMLGenerator.getAssociationText(it, obj);
    };
    final String associations = IterableExtensions.join(ListExtensions.<Field, String>map(((List<Field>)Conversions.doWrapArray(clazz.getDeclaredFields())), _function), "\n");
    return associations;
  }

  public static String getAssociationText(final Field field, final Object obj) {
    final String className = obj.getClass().getSimpleName();
    final String fieldName = field.getName();
    final String fieldType = field.getType().getSimpleName();
    String _xifexpression = null;
    if ((((!field.getType().isPrimitive()) && (!field.getType().isEnum())) && 
      (!PlantUMLGenerator.classesWithBiDirectionalAssociationsToEachOther.contains(
        new UnorderedPair<Class<?>>(field.getType(), obj.getClass()))))) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append(className);
      _builder.append(" --> \"");
      _builder.append(fieldName);
      _builder.append("\" ");
      _builder.append(fieldType);
      _xifexpression = _builder.toString();
    } else {
      _xifexpression = "";
    }
    return _xifexpression;
  }

  public static String capitalizeFirstLetter(final String str) {
    if (((str == null) || str.isEmpty())) {
      return str;
    }
    char _upperCase = Character.toUpperCase(str.charAt(0));
    String _substring = str.substring(1);
    return (Character.valueOf(_upperCase) + _substring);
  }

  public static boolean isAssociated(final Class<?> class1, final Class<?> class2) {
    final Function1<Method, Boolean> _function = (Method method) -> {
      return Boolean.valueOf(((method.getName().startsWith("get") && (method.getParameterCount() == 0)) && Objects.equal(method.getReturnType(), class2)));
    };
    final boolean is1to2 = IterableExtensions.<Method>exists(((Iterable<Method>)Conversions.doWrapArray(class1.getDeclaredMethods())), _function);
    final Function1<Method, Boolean> _function_1 = (Method method) -> {
      return Boolean.valueOf(((method.getName().startsWith("get") && (method.getParameterCount() == 0)) && Objects.equal(method.getReturnType(), class1)));
    };
    final boolean is2to1 = IterableExtensions.<Method>exists(((Iterable<Method>)Conversions.doWrapArray(class2.getDeclaredMethods())), _function_1);
    return (is1to2 && is2to1);
  }

  public static Object objectOfType(final Class clazz) {
    try {
      Constructor constructor = clazz.getConstructor();
      constructor.setAccessible(true);
      Object object = constructor.newInstance();
      return object;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  public static void invokeSetterMethod(final Object object, final Class clazz, final Object value) {
    try {
      final Function1<Method, Boolean> _function = (Method method) -> {
        return Boolean.valueOf(((method.getName().startsWith("set") && (method.getParameterCount() == 1)) && (method.getParameterTypes()[0] == clazz)));
      };
      Method setter = ((Method[])Conversions.unwrapArray(IterableExtensions.<Method>filter(((Iterable<Method>)Conversions.doWrapArray(object.getClass().getDeclaredMethods())), _function), Method.class))[0];
      setter.invoke(object, value);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  public static Object invokeGetterMethod(final Object object, final Class clazz) {
    try {
      final Function1<Method, Boolean> _function = (Method method) -> {
        return Boolean.valueOf(((method.getName().startsWith("get") && (method.getParameterCount() == 0)) && Objects.equal(method.getReturnType(), clazz)));
      };
      Method getter = ((Method[])Conversions.unwrapArray(IterableExtensions.<Method>filter(((Iterable<Method>)Conversions.doWrapArray(object.getClass().getDeclaredMethods())), _function), Method.class))[0];
      return getter.invoke(object);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  public static void computeClassesWithAssociationsToEachOther(final Object[] objects) {
    HashSet<UnorderedPair<Class>> _hashSet = new HashSet<UnorderedPair<Class>>();
    PlantUMLGenerator.classesWithAssociationsToEachOther = _hashSet;
    for (final Object obj : objects) {
      {
        final Function1<Field, Boolean> _function = (Field it) -> {
          return Boolean.valueOf((!(it.getType().isPrimitive() || it.getType().isEnum())));
        };
        final Iterable<Field> declaredFields = IterableExtensions.<Field>filter(((Iterable<Field>)Conversions.doWrapArray(obj.getClass().getDeclaredFields())), _function);
        for (final Field field : declaredFields) {
          boolean _isAssociated = PlantUMLGenerator.isAssociated(obj.getClass(), field.getType());
          if (_isAssociated) {
            Class<?> _class = obj.getClass();
            Class<?> _type = field.getType();
            UnorderedPair<Class> pair = new UnorderedPair<Class>(_class, _type);
            PlantUMLGenerator.classesWithAssociationsToEachOther.add(pair);
          }
        }
      }
    }
  }

  public static void computeClassesWithBiDirectionalAssociationsToEachOther(final Object[] objects) {
    PlantUMLGenerator.computeClassesWithAssociationsToEachOther(objects);
    HashSet<UnorderedPair<Class>> _hashSet = new HashSet<UnorderedPair<Class>>();
    PlantUMLGenerator.classesWithBiDirectionalAssociationsToEachOther = _hashSet;
    for (final UnorderedPair<Class> pair : PlantUMLGenerator.classesWithAssociationsToEachOther) {
      {
        Class classC = pair.getFirst();
        Class classD = pair.getSecond();
        Object c1 = PlantUMLGenerator.objectOfType(classC);
        Object c2 = PlantUMLGenerator.objectOfType(classC);
        Object d = PlantUMLGenerator.objectOfType(classD);
        PlantUMLGenerator.invokeSetterMethod(c1, classD, d);
        PlantUMLGenerator.invokeSetterMethod(d, classC, c2);
        if (((PlantUMLGenerator.invokeGetterMethod(c1, classD) == null) && Objects.equal(PlantUMLGenerator.invokeGetterMethod(c2, classD), d))) {
          PlantUMLGenerator.classesWithBiDirectionalAssociationsToEachOther.add(pair);
          InputOutput.<HashSet<UnorderedPair<Class>>>println(PlantUMLGenerator.classesWithBiDirectionalAssociationsToEachOther);
        } else {
        }
      }
    }
  }
}
