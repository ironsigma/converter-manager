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
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.izylab.izyutils.convertermanager.ConverterManager;
import com.izylab.izyutils.convertermanager.converter.EmptyConverter;
import com.izylab.izyutils.convertermanager.converter.StringLongConverter;
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
	public void testParse() {
		cm.registerConverter(new StringLongConverter());
		
		Long number = cm.convert("20", Long.class);
		assertThat(number, is(notNullValue()));
		assertThat(number, is(20L));
		
		String string = cm.convert(number, String.class);
		assertThat(string, is(notNullValue()));
		assertThat(string, is("20"));
	}
	
}
