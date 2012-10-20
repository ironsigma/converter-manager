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
package com.izylab.izyutils.convertermanager.internal;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public enum Message {
	CONVERTER_CANNOT_BE_NULL,
	CONVERTER_HAS_NO_ANNOTATED_METHODS,
	NO_PARAMETERS_FOUND,
	MORE_PARAMETERS_FOUND,
	NO_RETURN_TYPE,
	CONVERTER_ALREADY_REGISTERED,
	CONVERTER_SIMILAR_FOUND,
	NOT_ACCESSIBLE,
	SAME_TYPES,
	CONVERTER_NOT_ACCESSIBLE,
	CONV_NULL_TARGET,
	CONV_NO_CONVERTER,
	CONV_UNHANDLED_ERROR,
	CONV_FAILED,
	CONV_MORE_ARGS,
	CONV_LESS_ARGS,
	CONV_ARG_MISMATCH;
	
	private static final ResourceBundle RESOURCE_BUNDLE =
			ResourceBundle.getBundle("com/izylab/izyutils/convertermanager/messages"); //$NON-NLS-1$
	
	public String getString() {
		try {
			return RESOURCE_BUNDLE.getString(this.toString());
		} catch (MissingResourceException e) {
			return '!' + this.toString() + '!';
		}
	}
}
