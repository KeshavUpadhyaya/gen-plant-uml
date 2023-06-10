package plantUMLGenerator

import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.Parameter
import java.util.ArrayList
import java.util.HashSet

class PlantUMLGenerator {

	static HashSet<UnorderedPair<Class>> classesWithAssociationsToEachOther;
	static HashSet<UnorderedPair<Class>> classesWithBiDirectionalAssociationsToEachOther;

	def static generate(Object[] objects) {

		var uniqueInterfaces = new HashSet<Class<?>>();
		for (Object obj : objects) {
			uniqueInterfaces.addAll(obj.class.interfaces);
		}

		val interfaceCode = uniqueInterfaces.map[interfaceToPlantUMLCode(it)].join("\n")
		val classCode = objects.map[objectToPlantUMLCode(it)].join("\n")
		val generalizations = objects.map[getGeneralizations(it)].join("\n")
		val realizations = objects.map[getRealizations(it)].join("\n")

		computeClassesWithBiDirectionalAssociationsToEachOther(objects);
		val associations = objects.map[getAssociations(it)].join("\n")

		return '''
			@startuml
			«interfaceCode»
			«classCode»
			
			«generalizations»
			«realizations»
			«associations»
			@enduml
			
		'''
	}

	def static String objectToPlantUMLCode(Object object) {
		val clazz = object.class
		val className = clazz.simpleName

		val fields = clazz.declaredFields.filter[it|it.type.primitive || it.type.enum].map[getFields(it, object)].join(
			"\n")
		val methods = clazz.declaredMethods.filter [ it |
			! getInterfaceMethodNames(object).contains(
				it.name
			) // exclude interface methods
			&& !(it.name.startsWith("get") || it.name.startsWith("set")) // exclude getters and setters
		].map[getMethodDefinition(it)].join("\n");

		return '''
			class «className» {
			  «fields»
			  «methods»
			}
		'''
	}

	def static getInterfaceMethodNames(Object object) {
		var interfaceMethodNames = new HashSet<String>();

		for (Class interface : object.class.interfaces) {
			for (Method method : interface.declaredMethods) {
				interfaceMethodNames.add(method.name)
			}
		}
		return interfaceMethodNames
	}

	def static String interfaceToPlantUMLCode(Class clazz) {
		val className = clazz.simpleName

		val methods = clazz.declaredMethods.map[getMethodDefinition(it)].join("\n");

		return '''
			interface «className» {
			  «methods»
			}
		'''
	}

	def static getFields(Field field, Object object) {
		val modifiers = field.modifiers
		val isPrivate = Modifier.isPrivate(modifiers)
		val fieldName = field.getName
		val fieldType = field.getType.getSimpleName

		val fieldString = fieldName + ': ' + fieldType
		return fieldString

	}

	def static getMethodDefinition(Method method) {
		val modifiers = method.modifiers
		val isPrivate = Modifier.isPrivate(modifiers)
		val methodName = method.getName
		val returnType = method.returnType
		val parameters = getFormattedParameters(method.parameters)

		val methodString = (if(isPrivate) '-' else '+') + '''«methodName» («parameters»): «returnType»'''
		return methodString;
	}

	def static getFormattedParameters(Parameter[] parameters) {
		var paramStrings = new ArrayList<String>();
		if (parameters.length == 0) {
			return '';
		}
		paramStrings.add('''«parameters.get(0).name»: «parameters.get(0).type.simpleName»''')
		for (var i = 1; i < parameters.length; i++) {
			paramStrings.add(''', «parameters.get(i).name»: «parameters.get(i).type.simpleName»''')
		}
		return String.join("", paramStrings);
	}

	def static getGeneralizations(Object obj) {
		val clazz = obj.class
		val className = clazz.simpleName
		val superClass = clazz.superclass

		return if (superClass !== Object) '''«className» --|> «superClass.simpleName»''' else ''
	}

	def static getRealizations(Object obj) {
		val clazz = obj.class
		val className = clazz.simpleName
		val interfaces = clazz.interfaces.map[it|'''«className» ..|> «it.simpleName»'''].join("\n");
		return interfaces;
	}

	def static getAssociations(Object obj) {
		val clazz = obj.class
		val associations = clazz.declaredFields.map[getAssociationText(it, obj)].join("\n");
		return associations
	}

	def static getAssociationText(Field field, Object obj) {
		val className = obj.class.simpleName
		val fieldName = field.getName
		val fieldType = field.getType.getSimpleName

		return if (!field.type.primitive && !field.type.enum &&
			!classesWithBiDirectionalAssociationsToEachOther.contains(
				new UnorderedPair(field.type, obj.class))) '''«className» --> "«fieldName»" «fieldType»''' else ''
	}

	def static String capitalizeFirstLetter(String str) {
		if (str === null || str.isEmpty()) {
			return str;
		}
		return Character.toUpperCase(str.charAt(0)) + str.substring(1);
	}

	def static boolean isAssociated(Class<?> class1, Class<?> class2) {

		val is1to2 = class1.declaredMethods.exists [ method |
			method.name.startsWith("get") && method.parameterCount == 0 && method.returnType == class2
		]
		val is2to1 = class2.declaredMethods.exists [ method |
			method.name.startsWith("get") && method.parameterCount == 0 && method.returnType == class1
		]

		return is1to2 && is2to1;
	}

	def static objectOfType(Class clazz) {
		var constructor = clazz.getConstructor();
		constructor.setAccessible(true);
		var object = constructor.newInstance();
		return object;
	}

	// Assumption getter and setter methods are defined correctly
	def static void invokeSetterMethod(Object object, Class clazz, Object value) {
		var setter = object.class.declaredMethods.filter [ method |
			method.name.startsWith("set") && method.parameterCount == 1 && method.parameterTypes.get(0) === clazz
		].get(0);
		setter.invoke(object, value);
	}

	def static invokeGetterMethod(Object object, Class clazz) {
		var getter = object.class.declaredMethods.filter [ method |
			method.name.startsWith("get") && method.parameterCount == 0 && method.returnType == clazz
		].get(0);
		return getter.invoke(object);
	}

	def static computeClassesWithAssociationsToEachOther(Object[] objects) {
		classesWithAssociationsToEachOther = new HashSet();
		for (Object obj : objects) {
			val declaredFields = obj.class.declaredFields.filter[it|!(it.type.primitive || it.type.enum)]
			for (Field field : declaredFields) {
				if (isAssociated(obj.class, field.type)) {
					var pair = new UnorderedPair<Class>(obj.class, field.type);

					classesWithAssociationsToEachOther.add(pair)
				}
			}
		}
	}

	def static computeClassesWithBiDirectionalAssociationsToEachOther(Object[] objects) {

		computeClassesWithAssociationsToEachOther(objects);
		classesWithBiDirectionalAssociationsToEachOther = new HashSet();

		for (UnorderedPair<Class> pair : classesWithAssociationsToEachOther) {

			// just calling it C and D so that i can understand the flow...
			var classC = pair.first
			var classD = pair.second
			var c1 = objectOfType(classC)
			var c2 = objectOfType(classC)
			var d = objectOfType(classD)

			// point c1 -> d
			invokeSetterMethod(c1, classD, d);

			// point d -> c2
			invokeSetterMethod(d, classC, c2)

			// check if c1.d is null and c2.d points back to d
			if (invokeGetterMethod(c1, classD) === null && invokeGetterMethod(c2, classD) == d) {
				classesWithBiDirectionalAssociationsToEachOther.add(pair)
				println(classesWithBiDirectionalAssociationsToEachOther)
//				println("YES")
			} else {
//				println("NOPE")
			}
		}
	}

}
