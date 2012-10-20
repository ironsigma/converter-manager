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
package com.izylab.izyutils.convertermanager.classes;

import com.izylab.izyutils.convertermanager.Converter;

public class MyImplementationClass implements MyInterface {
	@Converter
	@SuppressWarnings("static-method")
	public String convert(@SuppressWarnings("unused") MyInterface object) {
		return "MyImplementationClass"; //$NON-NLS-1$
	}
}
