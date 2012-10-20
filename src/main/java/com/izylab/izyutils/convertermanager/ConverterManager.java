/**
 * Copyright 2012 Juan D Frias <jfrias at boxfi.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.izylab.izyutils.convertermanager;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.izylab.izyutils.convertermanager.internal.Message;

/**
 * Conversion manager.
 * 
 * <p>Your, one stop bean conversion.
 * 
 * <p>Quick start
 * 
 * <ol>
 * <li>Write converter</li>
 * <li>Register converter</li>
 * <li>Convert beans</li>
 * </ol>
 * 
 * <p>1. Write a converter
 * 
 * <p>A converter is just a class with one or more methods annotated with 
 * &#064;Converter that take an object and return an object of a different type.
 * Optionally it can take additional arguments.
 * 
 * <p><pre>
 * public MyConverter {
 *   &#064;Converter
 *   public YourObject convert(MyObject object) {
 *   	YourObject yo = new YourObject();
 *   	yo.setAttr(object.getAttr());
 *   	return yo;
 *   }
 *   
 *   &#064;Converter
 *   public MyObject revert(YourObject object) {
 *   	...
 *   }
 *   
 *   &#064;Converter
 *   public MyObject revertConditionally(YourObject object, Boolean includeSubObjects) {
 *   	...
 *   }
 * }</pre>
 * 
 * <p>2. Register a converter
 * 
 * <p>Create a Conversion Manager object to hold the converters and register
 * the new converter.
 * 
 * <p><pre>
 * ConverterManager converterManager = new ConverterManager();
 * converterManager.register(new MyConverter());
 * </pre>
 * 
 * <p>If you are using Spring you can add a converter manager bean and list
 * each one of the converts.
 * 
 * <p><pre>
 * {@code
 * <bean id="converterManager" class="com.example.app.ConverterManager">
 * 	 <property name="converters">
 *     <list>
 *     	 <bean class="com.example.app.MyConverter" />
 *     </list>
 *   </property>  
 * </bean>
 * }</pre>
 * 
 * <p>3. Convert beans
 * 
 * <p>Once the converters are registered, use the converter manager bean
 * to perform the conversions.
 * 
 * <p><pre>YourObject yourObj = converterManager.convert(myObject, YourObject.class);</pre>
 *  
 * <p>The <code>convert</code> method will look through the list of registered
 * converters, if a converter with matching types is found it will perform
 * the conversion.
 * 
 * <p>The <code>convert</code> method will try to search for a converter of the matching
 * object type, if nothing is found it will then try to match on the
 * interface(s) type, and finally on the superclass type.
 * 
 * <p>If no matching converter was found a <code>ConversionFailedException</code>
 * is thrown.
 * 
 * <p>If a converter takes additional arguments they are passed during
 * the <code>convert</code> call.
 * 
 * <p><pre>converterManager.convert(myObject, TargetType.class, arg1);</pre>
 * 
 */
public class ConverterManager {
	private Map<ConverterTypes, ConverterCommand> converterRegister = new HashMap<ConverterTypes, ConverterCommand>();
	
	/**
	 * Registers a converter.
	 * 
	 * @param converter Converter
	 * @throws ConverterManagerException
	 */
	public void registerConverter(Object converter) {
		// a real converter
		if ( converter == null ) {
			throw new ConverterManagerException(Message.CONVERTER_CANNOT_BE_NULL.getString());
		}
		// must be public
		if ( !Modifier.isPublic(converter.getClass().getModifiers()) ) {
			throw new ConverterManagerException(Message.CONVERTER_NOT_ACCESSIBLE.getString());
		}
		
		// track how many converters we added
		int registerSize = converterRegister.size();
		
		// look for annotated methods
		for (Method method : converter.getClass().getMethods()) {
			// method not annotated, skip it
			if ( !method.isAnnotationPresent(Converter.class) ) {
				continue;
			}
			// converter method must be accessible (public)
			if ( !Modifier.isPublic(method.getModifiers()) ) {
				throw new ConverterManagerException(Message.NOT_ACCESSIBLE.getString());
			}
			// converter method must return an object (converted object)
			Class<?> targetType = method.getReturnType();
			if ( targetType == Void.TYPE ) {
				throw new ConverterManagerException(Message.NO_RETURN_TYPE.getString());
			}
			// converter method must take at least one object (object to be converted)
			Class<?>[] types = method.getParameterTypes();
			if ( types.length == 0 ) {
				throw new ConverterManagerException(Message.NO_PARAMETERS_FOUND.getString());
			}
			// converting to the same type? uh? no.
			Class<?> sourceType = types[0];
			if ( sourceType == targetType ) {
				throw new ConverterManagerException(Message.SAME_TYPES.getString());
			}
			// create a lookup key, and see if there already one
			ConverterTypes key = new ConverterTypes(sourceType, targetType);
			ConverterCommand candidate = converterRegister.get(key);
			if ( candidate != null ) {
				// found the same converter already registered
				if ( candidate.getClass() == converter.getClass() ) {
					throw new ConverterManagerException(Message.CONVERTER_ALREADY_REGISTERED.getString());
				}
				// different converter but already one that's doing the same conversion
				throw new ConverterManagerException(String.format(Message.CONVERTER_SIMILAR_FOUND.getString(),
						converter.getClass(), candidate.getClass()));
			}
			// register converter
			converterRegister.put(key, new ConverterCommand(converter, method));
		}
		
		// nothing changed? most likely there's no methods annotated
		if ( registerSize == converterRegister.size() ) {
			throw new ConverterManagerException(Message.CONVERTER_HAS_NO_ANNOTATED_METHODS.getString());
		}
	}
	
	/**
	 * Remove all converters from the service.
	 */
	public void clearConverters() {
		converterRegister.clear();
	}
	
	
	/**
	 * Set the list of converters.
	 * @param converterList List of converter to register.
	 */
	public void setConverters(List<Object> converterList) {
		converterRegister.clear();
		for ( Object converter : converterList ) {
			registerConverter(converter);
		}
	}
	
	/**
	 * Test to see if we can convert the given object to the specified type.
	 * @param source Object to convert
	 * @param targetType Target type
	 * @return true if object can be converted, false otherwise
	 */
	public boolean canConvert(Object source, Class<?> targetType) {
		return canConvert(source.getClass(), targetType);
	}
	
	/**
	 * Test to see if we can convert the given source type to the target type.
	 * @param sourceType Source type
	 * @param targetType Target type
	 * @return true if the source type can be converted to the target type, false otherwise
	 */
	public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
		return sourceType != null &&
				targetType != null &&
				null != converterRegister.get(new ConverterTypes(sourceType, targetType));
	}
	
	/**
	 * Convert source object to target type.
	 * 
	 * @param source Object to convert
	 * @param targetType Type to convert to
	 * @param args Optional arguments to be passed to the converter
	 * 
	 * @return Converted type
	 * @throws ConversionFailedException
	 */
	@SuppressWarnings("unchecked")
	public <T> T convert(Object source, Class<T> targetType, Object ... args) throws ConversionFailedException {
		// can't convert to null target type
		if ( targetType == null ) {
			throw new ConversionFailedException(Message.CONV_NULL_TARGET.getString());
		}
		// null converts to null
		if ( source == null )
			return null;
		// same type, return same object
		if ( source.getClass() == targetType ) {
			return (T) source;
		}
		// look for converter
		ConverterCommand registeredConverter = getConverter(source, targetType);
		if ( registeredConverter == null ) {
			// not found
			throw new ConversionFailedException(String.format(Message.CONV_NO_CONVERTER.getString(),
					source.getClass(), targetType));
		}
		try {
			// lets convert
			return (T) registeredConverter.convert(source, args);
		} catch ( ConversionFailedException  ex ) {
			// Bubble up conversion errors
			throw ex;
		} catch ( Exception ex ) {
			// Don't know what happened, wrap error
			throw new ConversionFailedException(String.format(Message.CONV_FAILED.getString(),
					source.getClass(), targetType, registeredConverter.getClass()), ex);
		}
	}
	
	// look for a converter
	private ConverterCommand getConverter(Object source, Class<?> targetType) {
		// try the object class
		ConverterCommand registeredConverter = converterRegister.get(new ConverterTypes(source.getClass(), targetType));
		if ( registeredConverter != null ) {
			return registeredConverter;
		}
		// try the object's interfaces
		for ( Class<?> i : source.getClass().getInterfaces() ) {
			registeredConverter = converterRegister.get(new ConverterTypes(i, targetType));
			if ( registeredConverter != null ) {
				return registeredConverter;
			}
		}
		// try the object's supper class
		registeredConverter = converterRegister.get(new ConverterTypes(source.getClass().getSuperclass(), targetType));
		if ( registeredConverter != null ) {
			return registeredConverter;
		}
		// no match
		return null;
	}

	// Class that holds the object and method to use for conversion
	private class ConverterCommand {
		private Object converter;
		private Method method;
		// construct
		public ConverterCommand(Object converter, Method method) {
			this.converter = converter;
			this.method = method;
		}
		// call the converter method with optional arguments
		public Object convert(Object source, Object ... args) throws Exception {
			return method.invoke(converter, appendArgs(source, args));
		}
		// helper method to append arguments to the source object for invoke method
		private <T> T[] appendArgs( T object, T[] args) {
		    T[] objAndargs = java.util.Arrays.copyOf(args, args.length + 1);
		    System.arraycopy(args, 0, objAndargs, 1, args.length);
		    objAndargs[0] = object;
		    return objAndargs;
		}
	}
	
	// Lookup key for converter register
	private class ConverterTypes {
		private Class<?> source;
		private Class<?> target;

		public ConverterTypes(Class<?> source, Class<?> target) {
			this.source = source;
			this.target = target;
		}
	
		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if ((obj == null) || (obj.getClass() != this.getClass()))
				return false;
	
			ConverterTypes other = (ConverterTypes) obj;
			return (source == other.source || (source != null && source.equals(other.source)))
					&& (target == other.target || (target != null && target.equals(other.target)));
		}
	
		@Override
		public int hashCode() {
			int hash = 7;
			hash = 31 * hash + (null == source ? 0 : source.hashCode());
			hash = 31 * hash + (null == target ? 0 : target.hashCode());
			return hash;
		}
	}
}