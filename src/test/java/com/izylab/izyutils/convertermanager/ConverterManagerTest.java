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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.izylab.izyutils.convertermanager.ConverterManager;
import com.izylab.izyutils.convertermanager.classes.MyChildClass;
import com.izylab.izyutils.convertermanager.classes.MyConcreteClass;
import com.izylab.izyutils.convertermanager.classes.MyImplementationClass;
import com.izylab.izyutils.convertermanager.classes.MyMultiImplementationClass;
import com.izylab.izyutils.convertermanager.classes.MyObjectClass;
import com.izylab.izyutils.convertermanager.converter.EmptyConverter;
import com.izylab.izyutils.convertermanager.converter.NoArgsConverter;
import com.izylab.izyutils.convertermanager.converter.NoReturnConverter;
import com.izylab.izyutils.convertermanager.converter.PrivateMethodConverter;
import com.izylab.izyutils.convertermanager.converter.SameTypeConverter;
import com.izylab.izyutils.convertermanager.converter.StringNumberConverter;
import com.izylab.izyutils.convertermanager.converter.StringLongDuplicateConverter;
import com.izylab.izyutils.convertermanager.internal.Message;

@SuppressWarnings("nls")
public class ConverterManagerTest {

	private ConverterManager cm = new ConverterManager();
	
	@Rule
	public ExpectedException expectedEx = ExpectedException.none();
	
	@Before
	public void setup() {
		cm.clearConverters();
	}
	
	@Test
	public void testNull() {
	    expectedEx.expect(ConverterManagerException.class);
	    expectedEx.expectMessage(Message.CONVERTER_CANNOT_BE_NULL.getString());
		cm.registerConverter(null);
	}
	
	@Test
	public void testEmpty() {
	    expectedEx.expect(ConverterManagerException.class);
	    expectedEx.expectMessage(Message.CONVERTER_HAS_NO_ANNOTATED_METHODS.getString());
		cm.registerConverter(new EmptyConverter());
	}
	
	@Test
	public void testInaccessiblePackageConverter() {
	    expectedEx.expect(ConverterManagerException.class);
	    expectedEx.expectMessage(Message.CONVERTER_NOT_ACCESSIBLE.getString());
		cm.registerConverter(new PackageConverter());
	}
	
	@Test
	public void testInaccessiblePrivateConverter() {
	    expectedEx.expect(ConverterManagerException.class);
	    expectedEx.expectMessage(Message.CONVERTER_NOT_ACCESSIBLE.getString());
		cm.registerConverter(new PrivateConverter());
	}
	
	@Test
	public void testInaccessibleMethod() {
	    expectedEx.expect(ConverterManagerException.class);
	    expectedEx.expectMessage(Message.NOT_ACCESSIBLE.getString());
		cm.registerConverter(new PrivateMethodConverter());
	}
	
	@Test
	public void testNoReturn() {
	    expectedEx.expect(ConverterManagerException.class);
	    expectedEx.expectMessage(Message.NO_RETURN_TYPE.getString());
		cm.registerConverter(new NoReturnConverter());
	}
	
	@Test
	public void testNoArgs() {
	    expectedEx.expect(ConverterManagerException.class);
	    expectedEx.expectMessage(Message.NO_PARAMETERS_FOUND.getString());
		cm.registerConverter(new NoArgsConverter());
	}
	
	@Test
	public void testSameType() {
	    expectedEx.expect(ConverterManagerException.class);
	    expectedEx.expectMessage(Message.SAME_TYPES.getString());
		cm.registerConverter(new SameTypeConverter());
	}
	
	@Test
	public void testDoubleRegister() {
	    expectedEx.expect(ConverterManagerException.class);
	    expectedEx.expectMessage(Message.CONVERTER_ALREADY_REGISTERED.getString());
	    StringNumberConverter conv = new StringNumberConverter();
		cm.registerConverter(conv);
		cm.registerConverter(conv);
	}
	
	@Test
	public void testSimilarRegister() {
	    expectedEx.expect(ConverterManagerException.class);
	    expectedEx.expectMessage(String.format(Message.CONVERTER_SIMILAR_FOUND.getString(),
	    		StringLongDuplicateConverter.class, StringNumberConverter.class));
	    
		cm.registerConverter(new StringNumberConverter());
		cm.registerConverter(new StringLongDuplicateConverter());
	}
	
	@Test
	public void testCanConvertTypes() {
		cm.registerConverter(new StringNumberConverter());
		assertThat(cm.canConvert(String.class, Long.class), is(true));
	}
	
	@Test
	public void testCanConvertObject() {
		cm.registerConverter(new StringNumberConverter());
		assertThat(cm.canConvert("100", Long.class), is(true));
	}
	
	@Test
	public void testClear() {
		cm.registerConverter(new StringNumberConverter());
		assertThat(cm.canConvert(String.class, Long.class), is(true));
		
		cm.clearConverters();
		assertThat(cm.canConvert(String.class, Long.class), is(false));
	}
	
	@Test
	public void testSetConverters() {
		List<Object> converters = Arrays.asList(new Object[] { new StringNumberConverter() });
		
		cm.setConverters(converters);
		assertThat(cm.canConvert(String.class, Long.class), is(true));
	}
	
	@Test
	public void testHappyConvert() {
		cm.registerConverter(new StringNumberConverter());
		
		Long number = cm.convert("20", Long.class);
		assertThat(number, is(notNullValue()));
		assertThat(number, is(20L));
		
		String string = cm.convert(number, String.class);
		assertThat(string, is(notNullValue()));
		assertThat(string, is("20"));
	}
	
	@Test
	public void testNullTarget() {
	    expectedEx.expect(ConversionFailedException.class);
	    expectedEx.expectMessage(Message.CONV_NULL_TARGET.getString());
		cm.convert("20", null);
	}
	
	@Test
	public void testNullObject() {
		String value = cm.convert(null, String.class);
		assertThat(value, is(nullValue()));
	}
	
	@Test
	public void testSameObject() {
		String value = "value";
		String convValue = cm.convert(value, String.class);
		assertThat(value, is(convValue));
		assertTrue(value == convValue);
	}
	
	@Test
	public void testNoConv() {
	    expectedEx.expect(ConversionFailedException.class);
	    expectedEx.expectMessage(String.format(Message.CONV_NO_CONVERTER.getString(), String.class, Boolean.class));
		cm.convert("100", Boolean.class);
	}
	
	@Test
	public void testObjectClass() {
		cm.registerConverter(new MyObjectClass());
		String value = cm.convert(new MyObjectClass(), String.class);
		assertThat(value, is(notNullValue()));
		assertThat(value, is("MyObjectClass"));
	}
	
	@Test
	public void testObjectInterface() {
		cm.registerConverter(new MyImplementationClass());
		String value = cm.convert(new MyImplementationClass(), String.class);
		assertThat(value, is(notNullValue()));
		assertThat(value, is("MyImplementationClass"));
	}
	
	@Test
	public void testObjectMultiInterface() {
		cm.registerConverter(new MyMultiImplementationClass());
		String value = cm.convert(new MyMultiImplementationClass(), String.class);
		assertThat(value, is(notNullValue()));
		assertThat(value, is("MyMultiImplementationClass"));
	}
	
	@Test
	public void testSuperClass() {
		cm.registerConverter(new MyChildClass());
		String value = cm.convert(new MyChildClass(), String.class);
		assertThat(value, is(notNullValue()));
		assertThat(value, is("MyChildClass"));
	}
	
	@Test
	public void testAbstractClass() {
		cm.registerConverter(new MyConcreteClass());
		String value = cm.convert(new MyConcreteClass(), String.class);
		assertThat(value, is(notNullValue()));
		assertThat(value, is("MyConcreteClass"));
	}
	
	@Test
	public void testHandledError() {
	    expectedEx.expect(ConversionFailedException.class);
	    expectedEx.expectMessage("Long conversion failed");
		cm.registerConverter(new MyObjectClass());
		cm.convert(new MyObjectClass(), Long.class);
	}
	
	@Test
	public void testUnHandledError() {
	    expectedEx.expect(ConversionFailedException.class);
	    expectedEx.expectMessage(String.format(Message.CONV_UNHANDLED_ERROR.getString(),
	    		MyObjectClass.class, Integer.class, MyObjectClass.class));
	    
		cm.registerConverter(new MyObjectClass());
		cm.convert(new MyObjectClass(), Integer.class);
	}
	
	@Test
	public void testUnexpected() {
	    expectedEx.expect(ConversionFailedException.class);
	    expectedEx.expectMessage(String.format(Message.CONV_FAILED.getString(),
	    		MyObjectClass.class, Integer.class, MyObjectClass.class));
	    
		cm.registerConverter(new MyObjectClass());
		cm.convert(new MyObjectClass(), Integer.class, (Object[])null);
	}
	
	@Test
	public void testArgs() {
		cm.registerConverter(new StringNumberConverter());
		String value = cm.convert(100, String.class, true);
		assertThat(value, is(notNullValue()));
		assertThat(value, is("100"));
	}
	
	@Test
	public void testNotEnoughArgs() {
	    expectedEx.expect(ConversionFailedException.class);
	    expectedEx.expectMessage(String.format(Message.CONV_LESS_ARGS.getString(),
	    		Integer.class, String.class, StringNumberConverter.class));
		cm.registerConverter(new StringNumberConverter());
		String value = cm.convert(100, String.class);
		assertThat(value, is(notNullValue()));
		assertThat(value, is("100"));
	}
	
	@Test
	public void testMoreArgs() {
	    expectedEx.expect(ConversionFailedException.class);
	    expectedEx.expectMessage(String.format(Message.CONV_MORE_ARGS.getString(),
	    		Integer.class, String.class, StringNumberConverter.class));
		cm.registerConverter(new StringNumberConverter());
		String value = cm.convert(100, String.class, true, true);
		assertThat(value, is(notNullValue()));
		assertThat(value, is("100"));
	}
	
	@Test
	public void testArgMismatch() {
	    expectedEx.expect(ConversionFailedException.class);
	    expectedEx.expectMessage(String.format(Message.CONV_ARG_MISMATCH.getString(),
	    		Integer.class, String.class, StringNumberConverter.class));
		cm.registerConverter(new StringNumberConverter());
		String value = cm.convert(100, String.class, 0);
		assertThat(value, is(notNullValue()));
		assertThat(value, is("100"));
	}
	
	class PrivateConverter {
		/* empty */
	}
}

class PackageConverter {
	/* empty */
}